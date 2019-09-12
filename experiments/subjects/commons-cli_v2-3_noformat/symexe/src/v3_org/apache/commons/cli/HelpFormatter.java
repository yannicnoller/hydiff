/*
 * $Header: /home/cvs/jakarta-commons-sandbox/cli/src/java/org/apache/commons/cli/CommandLine.java,v 1.4 2002/06/06 22:32:37 bayard Exp $
 * $Revision: 1.4 $
 * $Date: 2002/06/06 22:32:37 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package v3_org.apache.commons.cli;

import static gov.nasa.jpf.symbc.ChangeAnnotation.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A formatter of help messages for the current command line options
 *
 * @author Slawek Zachcial
 * @author John Keyes (john at integralsource.com)
 **/
public class HelpFormatter {
    // --------------------------------------------------------------- Constants

    public static final int DEFAULT_WIDTH = change(80, 74); // changed: 80 to 74
    public static final int DEFAULT_LEFT_PAD = 1;
    public static final int DEFAULT_DESC_PAD = 3;
    public static final String DEFAULT_SYNTAX_PREFIX = "usage: ";
    public static final String DEFAULT_OPT_PREFIX = "-";
    public static final String DEFAULT_LONG_OPT_PREFIX = "--";
    public static final String DEFAULT_ARG_NAME = "arg";

    // ------------------------------------------------------------------ Static

    // -------------------------------------------------------------- Attributes

    public int defaultWidth;
    public int defaultLeftPad;
    public int defaultDescPad;
    public String defaultSyntaxPrefix;
    public String defaultNewLine;
    public String defaultOptPrefix;
    public String defaultLongOptPrefix;
    public String defaultArgName;

    // ------------------------------------------------------------ Constructors
    public HelpFormatter() {
        defaultWidth = DEFAULT_WIDTH;
        defaultLeftPad = DEFAULT_LEFT_PAD;
        defaultDescPad = DEFAULT_DESC_PAD;
        defaultSyntaxPrefix = DEFAULT_SYNTAX_PREFIX;
        defaultNewLine = System.getProperty("line.separator");
        defaultOptPrefix = DEFAULT_OPT_PREFIX;
        defaultLongOptPrefix = DEFAULT_LONG_OPT_PREFIX;
        defaultArgName = DEFAULT_ARG_NAME;
    }

    // ------------------------------------------------------------------ Public

    public void printHelp(String cmdLineSyntax, Options options) {
        printHelp(defaultWidth, cmdLineSyntax, null, options, null, false);
    }

    public void printHelp(String cmdLineSyntax, Options options, boolean autoUsage) {
        printHelp(defaultWidth, cmdLineSyntax, null, options, null, autoUsage);
    }

    public void printHelp(String cmdLineSyntax, String header, Options options, String footer) {
        printHelp(cmdLineSyntax, header, options, footer, false);
    }

    public void printHelp(String cmdLineSyntax, String header, Options options, String footer, boolean autoUsage) {
        printHelp(defaultWidth, cmdLineSyntax, header, options, footer, autoUsage);
    }

    public void printHelp(int width, String cmdLineSyntax, String header, Options options, String footer) {
        printHelp(width, cmdLineSyntax, header, options, footer, false);
    }

    public void printHelp(int width, String cmdLineSyntax, String header, Options options, String footer,
            boolean autoUsage) {
        PrintWriter pw = new PrintWriter(System.out);
        printHelp(pw, width, cmdLineSyntax, header, options, defaultLeftPad, defaultDescPad, footer, autoUsage);
        pw.flush();
    }

    public void printHelp(PrintWriter pw, int width, String cmdLineSyntax, String header, Options options, int leftPad,
            int descPad, String footer) throws IllegalArgumentException {
        printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer, false);
    }

    public void printHelp(PrintWriter pw, int width, String cmdLineSyntax, String header, Options options, int leftPad,
            int descPad, String footer, boolean autoUsage) throws IllegalArgumentException {
        if (cmdLineSyntax == null || cmdLineSyntax.length() == 0) {
            throw new IllegalArgumentException("cmdLineSyntax not provided");
        }

        if (autoUsage) {
            printUsage(pw, width, cmdLineSyntax, options);
        } else {
            printUsage(pw, width, cmdLineSyntax);
        }

        if (header != null && header.trim().length() > 0) {
            printWrapped(pw, width, header);
        }
        printOptions(pw, width, options, leftPad, descPad);
        if (footer != null && footer.trim().length() > 0) {
            printWrapped(pw, width, footer);
        }
    }

    /**
     * <p>
     * Prints the usage statement for the specified application.
     * </p>
     *
     * @param pw
     *            The PrintWriter to print the usage statement
     * @param width
     *            ??
     * @param appName
     *            The application name
     * @param options
     *            The command line Options
     *
     */
    public void printUsage(PrintWriter pw, int width, String app, Options options) {
        // initialise the string buffer
        StringBuffer buff = new StringBuffer(defaultSyntaxPrefix).append(app).append(" ");

        // create a list for processed option groups
        ArrayList list = new ArrayList();

        // temp variable
        Option option;

        // iterate over the options
        for (Iterator i = options.getOptions().iterator(); i.hasNext();) {
            // get the next Option
            option = (Option) i.next();

            // check if the option is part of an OptionGroup
            OptionGroup group = options.getOptionGroup(option);

            // if the option is part of a group and the group has not already
            // been processed
            if (group != null && !list.contains(group)) {

                // add the group to the processed list
                list.add(group);

                // get the names of the options from the OptionGroup
                Collection names = group.getNames();

                buff.append("[");

                // for each option in the OptionGroup
                for (Iterator iter = names.iterator(); iter.hasNext();) {
                    buff.append(iter.next());
                    if (iter.hasNext()) {
                        buff.append(" | ");
                    }
                }
                buff.append("]");
            }
            // if the Option is not part of an OptionGroup
            else {
                // if the Option is not a required option
                if (!option.isRequired()) {
                    buff.append("[");
                }

                // changed: added checks ->
                boolean tmp = change(true, !" ".equals(option.getOpt()));
                if (tmp) {
                    buff.append("-").append(option.getOpt());
                } else {
                    buff.append("--").append(option.getLongOpt());
                }
                // <-

                if (option.hasArg()) {
                    buff.append(" ");
                }

                // if the Option has a value
                if (option.hasArg()) {
                    buff.append(option.getArgName()); // changed: " arg" to option.getArgName()
                }

                // if the Option is not a required option
                if (!option.isRequired()) {
                    buff.append("]");
                }
                buff.append(" ");
            }
        }

        // call printWrapped
        printWrapped(pw, width, buff.toString().indexOf(' ') + 1, buff.toString());
    }

    public void printUsage(PrintWriter pw, int width, String cmdLineSyntax) {
        int argPos = cmdLineSyntax.indexOf(' ') + 1;
        printWrapped(pw, width, defaultSyntaxPrefix.length() + argPos, defaultSyntaxPrefix + cmdLineSyntax);
    }

    public void printOptions(PrintWriter pw, int width, Options options, int leftPad, int descPad) {
        StringBuffer sb = new StringBuffer();
        renderOptions(sb, width, options, leftPad, descPad);
        pw.println(sb.toString());
    }

    public void printWrapped(PrintWriter pw, int width, String text) {
        printWrapped(pw, width, 0, text);
    }

    public void printWrapped(PrintWriter pw, int width, int nextLineTabStop, String text) {
        StringBuffer sb = new StringBuffer(text.length());
        renderWrappedText(sb, width, nextLineTabStop, text);
        pw.println(sb.toString());
    }

    // --------------------------------------------------------------- Protected

    protected StringBuffer renderOptions(StringBuffer sb, int width, Options options, int leftPad, int descPad) {
        final String lpad = createPadding(leftPad);
        final String dpad = createPadding(descPad);

        /* OLD VERSION >> */
        StringBuffer sb_old = new StringBuffer(sb);
        int max_old = 0;
        StringBuffer optBuf_old;
        List prefixList_old = new ArrayList();
        Option option_old;
        for (Iterator i_old = options.getOptions().iterator(); i_old.hasNext();) {
            option_old = (Option) i_old.next();
            optBuf_old = new StringBuffer(8);
            optBuf_old.append(lpad).append(defaultOptPrefix).append(option_old.getOpt());
            if (option_old.hasLongOpt()) {
                optBuf_old.append(',').append(defaultLongOptPrefix).append(option_old.getLongOpt());
            }
            if (option_old.hasArg()) {
                // FIXME - should have a way to specify arg name per option
                optBuf_old.append(' ').append(defaultArgName);
            }
            prefixList_old.add(optBuf_old);
            max_old = optBuf_old.length() > max_old ? optBuf_old.length() : max_old;
        }
        for (Iterator i_old = prefixList_old.iterator(); i_old.hasNext();) {
            optBuf_old = (StringBuffer) i_old.next();
            if (optBuf_old.length() < max_old) {
                optBuf_old.append(createPadding(max_old - optBuf_old.length()));
            }
            optBuf_old.append(dpad);
        }
        Collections.sort(prefixList_old, new StringBufferComparator());
        int nextLineTabStop_old = max_old + descPad;
        String opt_old;
        int optOffset_old = leftPad + defaultOptPrefix.length();

        for (Iterator i_old = prefixList_old.iterator(); i_old.hasNext();) {
            optBuf_old = (StringBuffer) i_old.next();
            opt_old = optBuf_old.toString();
            if (opt_old.indexOf(',') != -1) {
                opt_old = opt_old.substring(optOffset_old, opt_old.indexOf(',', optOffset_old));
            } else {
                opt_old = opt_old.substring(optOffset_old, opt_old.indexOf(' ', optOffset_old));
            }
            option_old = options.getOption("-" + opt_old);

            renderWrappedText(sb_old, width, nextLineTabStop_old,
                    optBuf_old.append(option_old.getDescription()).toString());
            if (i_old.hasNext()) {
                sb_old.append(defaultNewLine);
            }
        }
        /* << OLD VERSION */

        /* NEW VERSION >> */
        StringBuffer sb_new = new StringBuffer(sb);
        int max_new = 0;
        StringBuffer optBuf_new;
        List prefixList_new = new ArrayList();
        Option option_new;
        List optList_new = options.helpOptions(); // change: use helpOptions() instead of getOptions()
        Collections.sort( optList_new, new StringBufferComparator() );
        for ( Iterator i_new = optList_new.iterator(); i_new.hasNext(); )
        {
           option_new = (Option) i_new.next();
           optBuf_new = new StringBuffer(8);

           if (option_new.getOpt().equals(" ")) // changed: processing adjusted
           {
               optBuf_new.append(lpad).append("   " + defaultLongOptPrefix).append(option_new.getLongOpt());
           }
           else
           {
               optBuf_new.append(lpad).append(defaultOptPrefix).append(option_new.getOpt());
               if ( option_new.hasLongOpt() )
               {
                   optBuf_new.append(',').append(defaultLongOptPrefix).append(option_new.getLongOpt());
               }

           }

           if( option_new.hasArg() ) {
               if( option_new.hasArgName() ) {
                   optBuf_new.append(" <").append( option_new.getArgName() ).append( '>' );
               }
               else {
                   optBuf_new.append(' ');
               }
           }

           prefixList_new.add(optBuf_new);
           max_new = optBuf_new.length() > max_new ? optBuf_new.length() : max_new;
        }
        int x_new = 0;
        for ( Iterator i_new = optList_new.iterator(); i_new.hasNext(); )
        {
           option_new = (Option) i_new.next();
           optBuf_new = new StringBuffer( prefixList_new.get( x_new++ ).toString() );

           if ( optBuf_new.length() < max_new )
           {
               optBuf_new.append(createPadding(max_new - optBuf_new.length()));
           }
           optBuf_new.append( dpad );
           
           int nextLineTabStop_new = max_new + descPad;
           renderWrappedText(sb_new, width, nextLineTabStop_new,
                             optBuf_new.append(option_new.getDescription()).toString());
           if ( i_new.hasNext() )
           {
               sb_new.append(defaultNewLine);
           }
        }
        /* << NEW VERSION */
        
        boolean changed = change(false, sb_new.equals(sb_old));
        if (changed) {
            return sb_new;
        } else {
            return sb_old;
        }
    }

    protected StringBuffer renderWrappedText(StringBuffer sb, int width, int nextLineTabStop, String text) {
        int pos = findWrapPos(text, width, 0);
        if (pos == -1) {
            sb.append(rtrim(text));
            return sb;
        } else {
            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }

        // all following lines must be padded with nextLineTabStop space characters
        final String padding = createPadding(nextLineTabStop);

        while (true) {
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, nextLineTabStop);
            if (pos == -1) {
                sb.append(text);
                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }

    }

    /**
     * Finds the next text wrap position after <code>startPos</code> for the text in <code>sb</code> with the column
     * width <code>width</code>. The wrap point is the last postion before startPos+width having a whitespace character
     * (space, \n, \r).
     *
     * @param sb
     *            text to be analyzed
     * @param width
     *            width of the wrapped text
     * @param startPos
     *            position from which to start the lookup whitespace character
     * @return postion on which the text must be wrapped or -1 if the wrap position is at the end of the text
     */
    protected int findWrapPos(String text, int width, int startPos) {
        int pos = -1;
        // the line ends before the max wrap pos or a new line char found
        if (((pos = text.indexOf('\n', startPos)) != -1 && pos <= width)
                || ((pos = text.indexOf('\t', startPos)) != -1 && pos <= width)) {
            return pos;
        } else if ((startPos + width) >= text.length()) {
            return -1;
        }

        // look for the last whitespace character before startPos+width
        pos = startPos + width;
        char c;
        while (pos >= startPos && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r') {
            --pos;
        }
        // if we found it - just return
        if (pos > startPos) {
            return pos;
        } else {
            // must look for the first whitespace chearacter after startPos + width
            pos = startPos + width;
            while (pos <= text.length() && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r') {
                ++pos;
            }
            return pos == text.length() ? -1 : pos;
        }
    }

    protected String createPadding(int len) {
        StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < len; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    protected String rtrim(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }

        int pos = s.length();
        while (pos >= 0 && Character.isWhitespace(s.charAt(pos - 1))) {
            --pos;
        }
        return s.substring(0, pos);
    }

    // ------------------------------------------------------- Package protected

    // ----------------------------------------------------------------- Private

    // ----------------------------------------------------------- Inner classes

    private static class StringBufferComparator implements Comparator {
        public int compare(Object o1, Object o2) // changed: added actual implementation
        {
            String str1 = stripPrefix(o1.toString());
            String str2 = stripPrefix(o2.toString());
            int retNew = str1.compareTo(str2);

            int retOld = o1.toString().compareTo(o2.toString());

            return change(retOld, retNew);
        }

        private String stripPrefix(String strOption) {
            // Strip any leading '-' characters
            int iStartIndex = strOption.lastIndexOf('-');
            if (iStartIndex == -1) {
                iStartIndex = 0;
            }
            return strOption.substring(iStartIndex);

        }
    }
}
