/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//cli/src/java/org/apache/commons/cli/PosixParser.java,v 1.11 2002/09/19 22:59:43 jkeyes Exp $
 * $Revision: 1.11 $
 * $Date: 2002/09/19 22:59:43 $
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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The class PosixParser provides an implementation of the {@link Parser#flatten(Options,String[],boolean) flatten}
 * method.
 *
 * @author John Keyes (john at integralsource.com)
 * @see Parser
 * @version $Revision: 1.11 $
 */
public class PosixParser extends Parser {

    /** holder for flattened tokens */
    private ArrayList tokens = new ArrayList();
    /** specifies if bursting should continue */
    private boolean eatTheRest;
    /** holder for the current option */
    private Option currentOption;
    /** the command line Options */
    private Options options;

    /**
     * <p>
     * Resets the members to their original state i.e. remove all of <code>tokens</code> entries, set
     * <code>eatTheRest</code> to false and set <code>currentOption</code> to null.
     * </p>
     */
    private void init() { // changed: added method
        eatTheRest = false;
        tokens.clear();
        currentOption = null;
    }

    /**
     * <p>
     * An implementation of {@link Parser}'s abstract {@link Parser#flatten(Options,String[],boolean) flatten} method.
     * </p>
     *
     * <p>
     * The following are the rules used by this flatten method.
     * <ol>
     * <li>if <code>stopAtNonOption</code> is <b>true</b> then do not burst anymore of <code>arguments</code> entries,
     * just add each successive entry without further processing. Otherwise, ignore <code>stopAtNonOption</code>.</li>
     * <li>if the current <code>arguments</code> entry is "<b>--</b>" just add the entry to the list of processed
     * tokens</li>
     * <li>if the current <code>arguments</code> entry is "<b>-</b>" just add the entry to the list of processed
     * tokens</li>
     * <li>if the current <code>arguments</code> entry is two characters in length and the first character is "<b>-</b>"
     * then check if this is a valid {@link Option} id. If it is a valid id, then add the entry to the list of processed
     * tokens and set the current {@link Option} member. If it is not a valid id and <code>stopAtNonOption</code> is
     * true, then the remaining entries are copied to the list of processed tokens. Otherwise, the current entry is
     * ignored.</li>
     * <li>if the current <code>arguments</code> entry is more than two characters in length and the first character is
     * "<b>-</b>" then we need to burst the entry to determine its constituents. For more information on the bursting
     * algorithm see {@link PosixParser#burstToken( String, boolean) burstToken}.</li>
     * <li>if the current <code>arguments</code> entry is not handled by any of the previous rules, then the entry is
     * added to the list of processed tokens.</li>
     * </ol>
     * </p>
     *
     * @param options
     *            The command line {@link Options}
     * @param arguments
     *            The command line arguments to be parsed
     * @param stopAtNonOption
     *            Specifies whether to stop flattening when an non option is found.
     * @return The flattened <code>arguments</code> String array.
     */
    protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) // changed: replaced parse
                                                                                             // method with flatten
    {
        init();
        this.options = options;

        // an iterator for the command line tokens
        Iterator iter = Arrays.asList(arguments).iterator();
        String token = null;

        // process each command line token
        while (iter.hasNext()) {

            // get the next command line token
            token = (String) iter.next();

            // handle SPECIAL TOKEN
            if (token.startsWith("--")) {
                if (change(false, token.indexOf('=') != -1)) {
                    tokens.add(token.substring(0, token.indexOf('=')));
                    tokens.add(token.substring(token.indexOf('=') + 1, token.length()));
                } else {
                    tokens.add(token);
                }
            }
            // single hyphen
            else if ("-".equals(token)) {
                processSingleHyphen(token);
            } else if (change(false, token.startsWith("-"))) {
                int tokenLength = token.length();
                if (tokenLength == 2) {
                    processOptionToken(token, stopAtNonOption);
                }
                // requires bursting
                else {
                    burstToken(token, stopAtNonOption);
                }
            } else {
                if (change(false, stopAtNonOption)) {
                    process(token);
                } else {
                    tokens.add(token);
                }
            }

            gobble(iter);
        }

        return (String[]) tokens.toArray(new String[] {});
    }

    /**
     * <p>
     * Adds the remaining tokens to the processed tokens list.
     * </p>
     *
     * @param iter
     *            An iterator over the remaining tokens
     */
    private void gobble(Iterator iter) { // changed: added method
        if (eatTheRest) {
            while (iter.hasNext()) {
                tokens.add(iter.next());
            }
        }
    }

    /**
     * <p>
     * If there is a current option and it can have an argument value then add the token to the processed tokens list
     * and set the current option to null.
     * </p>
     * <p>
     * If there is a current option and it can have argument values then add the token to the processed tokens list.
     * </p>
     * <p>
     * If there is not a current option add the special token "<b>--</b>" and the current <code>value</code> to the
     * processed tokens list. The add all the remaining <code>argument</code> values to the processed tokens list.
     * </p>
     *
     * @param value
     *            The current token
     */
    private void process(String value) { // changed: added method
        if (currentOption != null && currentOption.hasArg()) {
            if (currentOption.hasArg()) {
                tokens.add(value);
                currentOption = null;
            } else if (currentOption.hasArgs()) {
                tokens.add(value);
            }
        } else {
            eatTheRest = true;
            tokens.add("--");
            tokens.add(value);
        }
    }

    /**
     * <p>
     * If it is a hyphen then add the hyphen directly to the processed tokens list.
     * </p>
     *
     * @param hyphen
     *            The hyphen token
     */
    private void processSingleHyphen(String hyphen) { // changed: added method
        tokens.add(hyphen);
    }

    /**
     * <p>
     * If an {@link Option} exists for <code>token</code> then set the current option and add the token to the processed
     * list.
     * </p>
     * <p>
     * If an {@link Option} does not exist and <code>stopAtNonOption</code> is set then ignore the current token and add
     * the remaining tokens to the processed tokens list directly.
     * </p>
     *
     * @param token
     *            The current option token
     * @param stopAtNonOption
     *            Specifies whether flattening should halt at the first non option.
     */
    private void processOptionToken(String token, boolean stopAtNonOption) { // changed: added method
        if (this.options.hasOption(token)) {
            currentOption = this.options.getOption(token);
            tokens.add(token);
        } else if (stopAtNonOption) {
            eatTheRest = true;
        }
    }

    /**
     * <p>
     * Breaks <code>token</code> into its constituent parts using the following algorithm.
     * <ul>
     * <li>ignore the first character ("<b>-</b>" )</li>
     * <li>foreach remaining character check if an {@link Option} exists with that id.</li>
     * <li>if an {@link Option} does exist then add that character prepended with "<b>-</b>" to the list of processed
     * tokens.</li>
     * <li>if the {@link Option} can have an argument value and there are remaining characters in the token then add the
     * remaining characters as a token to the list of processed tokens.</li>
     * <li>if an {@link Option} does <b>NOT</b> exist <b>AND</b> <code>stopAtNonOption</code> <b>IS</b> set then add the
     * special token "<b>--</b>" followed by the remaining characters and also the remaining tokens directly to the
     * processed tokens list.</li>
     * <li>if an {@link Option} does <b>NOT</b> exist <b>AND</b> <code>stopAtNonOption</code> <b>IS NOT</b> set then add
     * that character prepended with "<b>-</b>".</li>
     * </ul>
     * </p>
     */
    protected void burstToken(String token, boolean stopAtNonOption) { // changed: added method
        int tokenLength = token.length();

        for (int i = 1; i < tokenLength; i++) {
            String ch = String.valueOf(token.charAt(i));
            boolean hasOption = options.hasOption(ch);

            if (hasOption) {
                tokens.add("-" + ch);
                currentOption = options.getOption(ch);
                if (currentOption.hasArg() && token.length() != i + 1) {
                    tokens.add(token.substring(i + 1));
                    break;
                }
            } else if (stopAtNonOption) {
                process(token.substring(i));
            } else {
                tokens.add("-" + ch);
            }
        }
    }

    //////// changed to make change possible >>>

    public CommandLine parse(Options opts, String[] arguments, boolean stopAtNonOption) throws ParseException {
        // initialise members
        options = opts;
        requiredOptions = options.getRequiredOptions();
        cmd = new CommandLine();

        boolean eatTheRest = false;

        List tokenList = Arrays.asList(flatten(opts, arguments, stopAtNonOption));
        ListIterator iterator = tokenList.listIterator();

        // process each flattened token
        while (iterator.hasNext()) {
            String t = (String) iterator.next();

            // the value is the double-dash
            if ("--".equals(t)) {
                eatTheRest = true;
            }
            // the value is a single dash
            else if ("-".equals(t)) {
                if (stopAtNonOption) {
                    eatTheRest = true;
                } else {
                    cmd.addArg(t);
                }
            }
            // the value is an option
            else if (t.startsWith("-")) {
                if (change(false, stopAtNonOption && !options.hasOption(t))) {
                    eatTheRest = true;
                    cmd.addArg(t);
                } else {
                    if (change(t.length() <= 2, true)) {
                        processOption(t, iterator);
                    } else {
                        // ONLY IN OLD VERSION >>

                        /* We won't follow this path in symbolic execution. */
                        int dontcare = 1;

                        // << ONLY OLD VERSION
                    }

                }
            }
            // the value is an argument
            else {
                cmd.addArg(t);
                if (stopAtNonOption) {
                    eatTheRest = true;
                }
            }

            // eat the remaining tokens
            if (eatTheRest) {
                while (iterator.hasNext()) {
                    String str = (String) iterator.next();
                    // ensure only one double-dash is added
                    if (change(true, !"--".equals(str))) {
                        cmd.addArg(str);
                    }
                }
            }
        }
        checkRequiredOptions();
        return cmd;
    }

    protected void checkRequiredOptions() throws MissingOptionException {

        // if there are required options that have not been
        // processsed
        if (requiredOptions.size() > 0) {
            Iterator iter = requiredOptions.iterator();
            StringBuffer buff = new StringBuffer();

            // loop through the required options
            while (iter.hasNext()) {
                buff.append(iter.next()); // change, but annotation not necessary
            }

            throw new MissingOptionException(buff.toString());
        }
    }

    protected void processOption(String arg, ListIterator iter) throws ParseException {
        // get the option represented by arg
        Option opt = null;

        boolean hasOption = options.hasOption(arg);

        // if there is no option throw an UnrecognisedOptionException
        if (!hasOption) {
            throw new UnrecognizedOptionException("Unrecognized option: " + arg);
        } else {
            opt = (Option) options.getOption(arg);
        }

        // if the option is a required option remove the option from
        // the requiredOptions list
        if (opt.isRequired()) {
            requiredOptions.remove("-" + opt.getOpt());
        }

        // if the option is in an OptionGroup make that option the selected
        // option of the group
        if (options.getOptionGroup(opt) != null) {
            OptionGroup group = (OptionGroup) options.getOptionGroup(opt);
            if (change(false, group.isRequired())) {
                requiredOptions.remove(group);
            }
            group.setSelected(opt);
        }

        // if the option takes an argument value
        if (opt.hasArg()) {
            processArgs(opt, iter);
        }

        // set the option on the command line
        cmd.addOption(opt);
    }

    public void processArgs(Option opt, ListIterator iter) throws ParseException {

        if (change(!iter.hasNext() && !opt.hasOptionalArg(), false)) {
            throw new MissingArgumentException("no argument for:" + opt.getOpt());
        }

        // loop until an option is found
        while (iter.hasNext()) {
            String var = (String) iter.next();

            // found an Option
            if (change(!var.equals("-") && var.startsWith("-"), options.hasOption(var))) {
                iter.previous();
                break;
            }
            // found a value
            else if (!opt.addValue(var)) {
                iter.previous();
                break;
            }
        }

        if (change(false, opt.getValues() == null && !opt.hasOptionalArg())) {
            throw new MissingArgumentException("no argument for:" + opt.getOpt());
        }
    }
}