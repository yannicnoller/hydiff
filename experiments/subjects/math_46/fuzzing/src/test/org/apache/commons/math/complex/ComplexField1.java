/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.org.apache.commons.math.complex;

import java.io.Serializable;

import test.org.apache.commons.math.Field;

/**
 * Representation of the complex numbers field.
 * <p>
 * This class is a singleton.
 * </p>
 * @see Complex1
 * @version $Id$
 * @since 2.0
 */
public class ComplexField1 implements Field<Complex1>, Serializable  {

    /** Serializable version identifier. */
    private static final long serialVersionUID = -6130362688700788798L;

    /** Private constructor for the singleton.
     */
    private ComplexField1() {
    }

    /** Get the unique instance.
     * @return the unique instance
     */
    public static ComplexField1 getInstance() {
        return LazyHolder.INSTANCE;
    }

    /** {@inheritDoc} */
    public Complex1 getOne() {
        return Complex1.ONE;
    }

    /** {@inheritDoc} */
    public Complex1 getZero() {
        return Complex1.ZERO;
    }

    // CHECKSTYLE: stop HideUtilityClassConstructor
    /** Holder for the instance.
     * <p>We use here the Initialization On Demand Holder Idiom.</p>
     */
    private static class LazyHolder {
        /** Cached field instance. */
        private static final ComplexField1 INSTANCE = new ComplexField1();
    }
    // CHECKSTYLE: resume HideUtilityClassConstructor

    /** Handle deserialization of the singleton.
     * @return the singleton instance
     */
    private Object readResolve() {
        // return the singleton instance
        return LazyHolder.INSTANCE;
    }

}
