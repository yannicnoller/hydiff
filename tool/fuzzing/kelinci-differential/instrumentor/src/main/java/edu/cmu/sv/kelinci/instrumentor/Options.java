package edu.cmu.sv.kelinci.instrumentor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.kohsuke.args4j.Option;

import de.hub.se.cfg.CFGTarget;

/**
 * Options for the Kelinci Instrumentor. Currently -i and -o flags to specify input and output, respectively, and
 * -threads to specify the number of runner threads.
 * 
 * YN: Additionally -mode specifies the instrumentation mode, e.g. instrument all "jump" instructions or all "labels".
 * 
 * @author rodykers
 * 
 *         Modified by Yannic Noller (YN), May 22, 2018
 *
 */
public class Options {

    @Option(name = "-i", usage = "Specify input file/dir", required = true)
    private String input;
    private HashSet<String> inputClasses;
    private HashSet<String> inputClassesFullPath;

    public String getRawInput() {
        return input;
    }

    public HashSet<String> getInput() {
        if (inputClasses == null) {
            inputClasses = new HashSet<>();
            inputClassesFullPath = new HashSet<>();
            if (input.endsWith(".class")) {
                // single class file, has to be a relative path from a directory on the class
                // path
                inputClasses.add(input);
                inputClassesFullPath.add(input);
            } else if (input.endsWith(".jar")) {
                // JAR file
                JarFileIO.extractJar(input, inputClasses, inputClassesFullPath);
                addToClassPath(input);
            } else {
                // directory
                System.out.println("Loading dir: " + input);
                loadDirectory(input, inputClasses, inputClassesFullPath);
                addToClassPath(input);
            }
        }
        return inputClasses;
    }

    public HashSet<String> getInputFullPath() {
        getInput();
        return inputClassesFullPath;
    }

    /*
     * Add an element to the class path. Can be either a directory or a JAR.
     */
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

    private void loadDirectory(String input, HashSet<String> inputClasses, HashSet<String> inputClassesFullPath) {
        final int dirprefix;
        if (input.endsWith("/"))
            dirprefix = input.length();
        else
            dirprefix = input.length() + 1;
        try {
            Files.walk(Paths.get(input)).filter(Files::isRegularFile).forEach(filePath -> {
                String name = filePath.toString();
                System.out.println("Found file " + name);
                if (name.endsWith(".class")) {
                    inputClasses.add(name.substring(dirprefix));
                    inputClassesFullPath.add(name);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading from directory: " + input);
        }
    }

    @Option(name = "-o", usage = "Specify output file/dir", required = true)
    private String output;

    public String getOutput() {
        return output;
    }

    public boolean outputJar() {
        boolean outjar = output.endsWith(".jar");
        if (outjar && !input.endsWith(".jar"))
            throw new RuntimeException("Cannot output JAR if the input is not a JAR");
        return output.endsWith(".jar");
    }

    @Option(name = "-skipmain", usage = "Don't instrument main", required = false)
    private boolean skipmain;

    public boolean skipMain() {
        return skipmain;
    }

    /* YN: instrumentation mode */
    public enum InstrumentationMode {
        JUMPS, LABELS, REGRESSION, REGRESSION_NO_DEC, REGRESSION_NO_CFG
    };

    @Option(name = "-mode", usage = "Specify instrumentation mode (jumps, labels, regression)", required = false)
    private InstrumentationMode instrumentationMode = InstrumentationMode.JUMPS;

    public InstrumentationMode getInstrumentationMode() {
        return instrumentationMode;
    }

    /* YN: CFG generation */
    @Option(name = "-export-cfgdir", usage = "Specify directory to store generated CFG", required = false)
    private String cfgDir;

    public String getCFGExportDir() {
        return cfgDir;
    }

    /* YN: CFG Distance Target Definition */
    @Option(name = "-dist-target", usage = "Specify target(s) for distance calculation to store generated CFG: \"method1:line1,method2:line2\"", required = false)
    private String distanceTargets;
    private Set<String> targets;

    public String getRawDistanceTargets() {
        return distanceTargets;
    }

    public Set<String> getDistanceTargets() {
        if (targets == null) {
            targets = new HashSet<>(CFGTarget.parseDistanceTargetArgument(distanceTargets));
        }
        return targets;
    }

    /* YN: skip classes */
    @Option(name = "-skipclass", usage = "Specify class file(s) that should be skipped \"ca/uhn/fhir/util/XmlUtil.class,mca/uhn/fhir/util/XmlUtil2.class\"", required = false)
    private String skipClasses;
    private Set<String> allClassesToSkip;

    public String getRawSkipClasses() {
        return skipClasses;
    }

    public Set<String> getAllClassesToSkip() {
        if (allClassesToSkip == null) {
            allClassesToSkip = new HashSet<>();
            if (skipClasses != null) {
                for (String clazz : skipClasses.split(",")) {
                    allClassesToSkip.add(clazz);
                }
            }
        }
        return allClassesToSkip;
    }

    /**
     * Singleton
     */
    private static Options options;

    public static void resetInstance() {
        options = null;
    }

    public static Options v() {
        if (null == options) {
            options = new Options();
        }
        return options;
    }

    private Options() {
    }

}
