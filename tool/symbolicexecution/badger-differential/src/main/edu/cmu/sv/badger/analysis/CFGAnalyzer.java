package edu.cmu.sv.badger.analysis;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.hub.se.cfg.CFG;
import de.hub.se.cfg.CFGAnalysis;
import de.hub.se.cfg.CFGBuilder;
import de.hub.se.cfg.CFGNode;
import de.hub.se.cfg.CFGTarget;
import de.hub.se.cfg.CFGUtility;
import gov.nasa.jpf.symbc.ChangeAnnotation;
import gov.nasa.jpf.vm.Instruction;

/**
 * Handles CFG construction and provides information about reachability of the patches.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 */
public class CFGAnalyzer {

    public static CFGAnalysis cfga = null;
    public static Set<Integer> targetNodeIDs = new HashSet<>();

    public static void printCFG() {
        if (cfga != null) {
            for (CFG cfg : cfga.getAllIncludedCFG()) {
                System.out.println(cfg.getMethodName());
                System.out.println(cfg);
            }
        }
    }

    public static boolean isPatchReachble(Instruction insn) {
        if (cfga == null) {
            return true;
        }
        String method = insn.getMethodInfo().getFullName();
        int sourceLineNumber = insn.getLineNumber();
        CFGNode node = cfga.getNodeByMethodAndSourceLine(method, sourceLineNumber, true);
        
        if (node == null) {
            // method was not analyzed, assume true.
            System.out.println(">> WARNING: method was not analyzed regarding CFG: " + method);
            return true;
        }
        
        for (Integer targetID : targetNodeIDs) {
            if (node.getDistance(targetID) != null) {
                return true;
            }
        }
        return false;
    }

    public static Integer getClosestDistanceToPatch(Instruction insn) {
        if (cfga == null) {
            return null;
        }
        String method = insn.getMethodInfo().getFullName();
        int sourceLineNumber = insn.getLineNumber();
        CFGNode node = cfga.getNodeByMethodAndSourceLine(method, sourceLineNumber, true);
        if (node == null) {
            return null;
        }
        Integer closestDistance = null;
        for (Integer targetID : targetNodeIDs) {
            Integer distance = node.getDistance(targetID);
            if (distance != null) {
                if (closestDistance == null || distance < closestDistance) {
                    closestDistance = distance;
                }
            }
        }
        return closestDistance;
    }

    public static void importCFG(String folderPath) {
        cfga = CFGUtility.deserialize(folderPath);
        gatherTargetNodeIDs(cfga);
    }

    public static void buildCFG(String path, String pathToChangeAnnotationClass, String classesToSkipStr,
            String exportDir) {
        Set<String> classes = loadInput(path);

        Set<String> classesToSkip = new HashSet<>();
        for (String clazz : classesToSkipStr.split(",")) {
            classesToSkip.add(clazz);
        }

        /* add ChangeAnnotation.class as additional classes */
        cfga = CFGBuilder.genCFGForClasses(classes, classesToSkip, pathToChangeAnnotationClass);

        /* Calculate distances to change methods. */
        cfga.calculateDistancesToTargets(ChangeAnnotation.signatureOfChangeMethods);
        gatherTargetNodeIDs(cfga);

        /* Export CFG file if asked for. */
        if (exportDir != null && !exportDir.isEmpty()) {
            CFGUtility.serialize(cfga, exportDir);
        }

    }

    public static Set<String> loadInput(String inputRawString) {
        Set<String> inputClasses = new HashSet<>();
        for (String input : inputRawString.split(":")) {
            if (input.endsWith(".class")) {
                // single class file, has to be a relative path from a directory on the class
                // path
                inputClasses.add(input);
            } else if (input.endsWith(".jar")) {
                // JAR file
                extractJar(input, inputClasses);
                addToClassPath(input);
            } else {
                // directory
                System.out.println("Loading dir: " + input);
                loadDirectory(input, inputClasses);
                addToClassPath(input);
            }
        }
        return inputClasses;
    }
    
    private static void addToClassPath(String url) {
        try {
            File file = new File(url);
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file.toURI().toURL() });
        } catch (Exception e) {
            throw new RuntimeException("Error adding location to class path: " + url);
        }

    }

    private static void loadDirectory(String input, Set<String> inputClasses) {
        final int dirprefix;
        if (input.endsWith("/"))
            dirprefix = input.length();
        else
            dirprefix = input.length() + 1;
        try {
            Files.walk(Paths.get(input)).filter(Files::isRegularFile).forEach(filePath -> {
                String name = filePath.toString();
                if (name.endsWith(".class")) {
//                    inputClasses.add(name.substring(dirprefix));
                    inputClasses.add(name);
                }

            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading from directory: " + input);
        }
    }

    /**
     * Extracts all class file names from a JAR file (possibly nested with more JARs).
     * 
     * @param file
     *            The name of the file.
     * @param classes
     *            Class names will be stored in here.
     */
    public static void extractJar(String file, Set<String> classes) {
        try {
            // open JAR file
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();

            // iterate JAR entries
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.endsWith(".class")) {
                    entryName = entryName.substring(0, entryName.length()-6);
                    classes.add(entryName);
                } else if (entryName.endsWith(".jar")) {
                    // load nested JAR
                    // extractJar(entryName, classes); TODO YN: skip for now
                }
            }

            // close JAR file
            jarFile.close();

        } catch (IOException e) {
            throw new RuntimeException("Error reading from JAR file: " + file);
        }
    }

    private static void gatherTargetNodeIDs(CFGAnalysis cfga) {
        targetNodeIDs = new HashSet<>();
        for (CFGTarget processedTarget : cfga.getProcessedTargets()) {
            targetNodeIDs.add(cfga
                    .getNodeByMethodAndSourceLine(processedTarget.getMethod(), processedTarget.getSourceLineNumber())
                    .getId());
        }
    }

}
