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
package test.org.apache.commons.math3.analysis.differentiation;

import java.io.Serializable;

import test.org.apache.commons.math3.Field;
import test.org.apache.commons.math3.FieldElement;
import test.org.apache.commons.math3.exception.DimensionMismatchException;
import test.org.apache.commons.math3.exception.NumberIsTooLargeException;

/** Class representing both the value and the differentials of a function.
 * <p>This class is the workhorse of the differentiation package.</p>
 * <p>This class is an implementation of the extension to Rall's
 * numbers described in Dan Kalman's paper <a
 * href="http://www.math.american.edu/People/kalman/pdffiles/mmgautodiff.pdf">Doubly
 * Recursive Multivariate Automatic Differentiation</a>, Mathematics Magazine, vol. 75,
 * no. 3, June 2002.</p>. Rall's numbers are an extension to the real numbers used
 * throughout mathematical expressions; they hold the derivative together with the
 * value of a function. Dan Kalman's derivative structures holds all partial derivatives
 * up to any specified order, with respect to any number of free variables. Rall's
 * number therefore can be seen as derivative structures for order one derivative and
 * one free variable, and real numbers can be seen as derivative structures with zero
 * order derivative and no free variables.</p>
 * <p>{@link DerivativeStructure} instances can be used directly thanks to
 * the arithmetic operators to the mathematical functions provided as static
 * methods by this class (+, -, *, /, %, sin, cos ...).</p>
 * <p>Implementing complex expressions by hand using these classes is
 * however a complex and error-prone task, so the classical use is
 * simply to develop computation code using standard primitive double
 * values and to use {@link UnivariateDifferentiator differentiators} to create
 * the {@link DerivativeStructure}-based instances.</p>
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see DSCompiler
 * @version $Id$
 * @since 3.1
 */
public class DerivativeStructure implements FieldElement<DerivativeStructure>, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20120730L;

    /** Compiler for the current dimensions. */
    private transient DSCompiler compiler;

    /** Combined array holding all values. */
    private final double[] data;

    /** Build an instance with all values and derivatives set to 0.
     * @param compiler compiler to use for computation
     */
    private DerivativeStructure(final DSCompiler compiler) {
        this.compiler = compiler;
        this.data     = new double[compiler.getSize()];
    }

    /** Build an instance with all values and derivatives set to 0.
     * @param variables number of variables
     * @param order derivation order
     */
    public DerivativeStructure(final int variables, final int order) {
        this(DSCompiler.getCompiler(variables, order));
    }

    /** Build an instance representing a constant value.
     * @param variables number of variables
     * @param order derivation order
     * @param value value of the constant
     * @see #DerivativeStructure(int, int, int, double)
     */
    public DerivativeStructure(final int variables, final int order, final double value) {
        this(variables, order);
        this.data[0] = value;
    }

    /** Build an instance representing a variable.
     * <p>Instances built using this constructor are considered
     * to be the free variables with respect to which differentials
     * are computed. As such, their differential with respect to
     * themselves is +1.</p>
     * @param variables number of variables
     * @param order derivation order
     * @param index index of the variable (from 0 to {@code variables - 1})
     * @param value value of the variable
     * @exception NumberIsTooLargeException if index is equal to variables or larger
     * @see #DerivativeStructure(int, int, double)
     */
    public DerivativeStructure(final int variables, final int order,
                               final int index, final double value)
        throws NumberIsTooLargeException {
        this(variables, order, value);

        if (index >= variables) {
            throw new NumberIsTooLargeException(index, variables, false);
        }

        if (order > 0) {
            // the derivative of the variable with respect to itself is 1.0
            data[DSCompiler.getCompiler(index, order).getSize()] = 1.0;
        }

    }

    /** Linear combination constructor.
     * The derivative structure built will be a1 * ds1 + a2 * ds2
     * @param a1 first scale factor
     * @param ds1 first base (unscaled) derivative structure
     * @param a2 second scale factor
     * @param ds2 second base (unscaled) derivative structure
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public DerivativeStructure(final double a1, final DerivativeStructure ds1,
                               final double a2, final DerivativeStructure ds2)
        throws DimensionMismatchException {
        this(ds1.compiler);
        compiler.checkCompatibility(ds2.compiler);
        compiler.linearCombination(a1, ds1.data, 0, a2, ds2.data, 0, data, 0);
    }

    /** Linear combination constructor.
     * The derivative structure built will be a1 * ds1 + a2 * ds2 + a3 * ds3
     * @param a1 first scale factor
     * @param ds1 first base (unscaled) derivative structure
     * @param a2 second scale factor
     * @param ds2 second base (unscaled) derivative structure
     * @param a3 third scale factor
     * @param ds3 third base (unscaled) derivative structure
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public DerivativeStructure(final double a1, final DerivativeStructure ds1,
                               final double a2, final DerivativeStructure ds2,
                               final double a3, final DerivativeStructure ds3)
        throws DimensionMismatchException {
        this(ds1.compiler);
        compiler.checkCompatibility(ds2.compiler);
        compiler.checkCompatibility(ds3.compiler);
        compiler.linearCombination(a1, ds1.data, 0, a2, ds2.data, 0, a3, ds3.data, 0, data, 0);
    }

    /** Linear combination constructor.
     * The derivative structure built will be a1 * ds1 + a2 * ds2 + a3 * ds3 + a4 * ds4
     * @param a1 first scale factor
     * @param ds1 first base (unscaled) derivative structure
     * @param a2 second scale factor
     * @param ds2 second base (unscaled) derivative structure
     * @param a3 third scale factor
     * @param ds3 third base (unscaled) derivative structure
     * @param a4 fourth scale factor
     * @param ds4 fourth base (unscaled) derivative structure
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public DerivativeStructure(final double a1, final DerivativeStructure ds1,
                               final double a2, final DerivativeStructure ds2,
                               final double a3, final DerivativeStructure ds3,
                               final double a4, final DerivativeStructure ds4)
        throws DimensionMismatchException {
        this(ds1.compiler);
        compiler.checkCompatibility(ds2.compiler);
        compiler.checkCompatibility(ds3.compiler);
        compiler.checkCompatibility(ds4.compiler);
        compiler.linearCombination(a1, ds1.data, 0, a2, ds2.data, 0,
                                   a3, ds3.data, 0, a4, ds4.data, 0,
                                   data, 0);
    }

    /** Copy constructor.
     * @param instance to copy
     */
    private DerivativeStructure(final DerivativeStructure ds) {
        this.compiler = ds.compiler;
        this.data     = ds.data.clone();
    }

    /** Get the number of free parameters.
     * @return number of free parameters
     */
    public int getFreeParameters() {
        return compiler.getFreeParameters();
    }

    /** Get the derivation order.
     * @return derivation order
     */
    public int getOrder() {
        return compiler.getOrder();
    }

    /** Get the value part of the derivative structure.
     * @return value part of the derivative structure
     * @see #getPartialDerivative(int...)
     */
    public double getValue() {
        return data[0];
    }

    /** Get a partial derivative.
     * @param orders derivation orders with respect to each variable (if all orders are 0,
     * the value is returned)
     * @return partial derivative
     * @see #getValue()
     * @exception DimensionMismatchException if the numbers of variables does not
     * match the instance
     * @exception NumberIsTooLargeException if sum of derivation orders is larger
     * than the instance limits
     */
    public double getPartialDerivative(final int ... orders)
        throws DimensionMismatchException, NumberIsTooLargeException {
        return data[compiler.getPartialDerivativeIndex(orders)];
    }

    /** '+' operator.
     * @param a right hand side parameter of the operator
     * @return this+a
     */
    public DerivativeStructure add(final double a) {
        final DerivativeStructure ds = new DerivativeStructure(this);
        ds.data[0] += a;
        return ds;
    }

    /** '+' operator.
     * @param a right hand side parameter of the operator
     * @return this+a
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public DerivativeStructure add(final DerivativeStructure a)
        throws DimensionMismatchException {
        compiler.checkCompatibility(a.compiler);
        final DerivativeStructure ds = new DerivativeStructure(this);
        compiler.add(data, 0, a.data, 0, ds.data, 0);
        return ds;
    }

    /** '-' operator.
     * @param a right hand side parameter of the operator
     * @return this-a
     */
    public DerivativeStructure subtract(final double a) {
        return add(-a);
    }

    /** '-' operator.
     * @param a right hand side parameter of the operator
     * @return this-a
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public DerivativeStructure subtract(final DerivativeStructure a)
        throws DimensionMismatchException {
        compiler.checkCompatibility(a.compiler);
        final DerivativeStructure ds = new DerivativeStructure(this);
        compiler.subtract(data, 0, a.data, 0, ds.data, 0);
        return ds;
    }

    /** {@inheritDoc} */
    public DerivativeStructure multiply(final int n) {
        return multiply((double) n);
    }

    /** '&times;' operator.
     * @param a right hand side parameter of the operator
     * @return this&times;a
     */
    public DerivativeStructure multiply(final double a) {
        final DerivativeStructure ds = new DerivativeStructure(this);
        for (int i = 0; i < ds.data.length; ++i) {
            ds.data[i] *= a;
        }
        return ds;
    }

    /** '&times;' operator.
     * @param a right hand side parameter of the operator
     * @return this&times;a
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public DerivativeStructure multiply(final DerivativeStructure a)
        throws DimensionMismatchException {
        compiler.checkCompatibility(a.compiler);
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.multiply(data, 0, a.data, 0, result.data, 0);
        return result;
    }

    /** '&divides;' operator.
     * @param a right hand side parameter of the operator
     * @return this&divides;a
     */
    public DerivativeStructure divide(final double a) {
        final DerivativeStructure ds = new DerivativeStructure(this);
        for (int i = 0; i < ds.data.length; ++i) {
            ds.data[i] /= a;
        }
        return ds;
    }

    /** '&divides;' operator.
     * @param a right hand side parameter of the operator
     * @return this&divides;a
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public DerivativeStructure divide(final DerivativeStructure a)
        throws DimensionMismatchException {
        compiler.checkCompatibility(a.compiler);
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.divide(data, 0, a.data, 0, result.data, 0);
        return result;
    }

    /** '%' operator.
     * @param a right hand side parameter of the operator
     * @return this%a
     */
    public DerivativeStructure remainder(final double a) {
        final DerivativeStructure ds = new DerivativeStructure(this);
        ds.data[0] = ds.data[0] % a;
        return ds;
    }

    /** '%' operator.
     * @param a right hand side parameter of the operator
     * @return this%a
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public DerivativeStructure remainder(final DerivativeStructure a)
        throws DimensionMismatchException {
        compiler.checkCompatibility(a.compiler);
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.remainder(data, 0, a.data, 0, result.data, 0);
        return result;
    }

    /** unary '-' operator.
     * @return -this
     */
    public DerivativeStructure negate() {
        final DerivativeStructure ds = new DerivativeStructure(compiler);
        for (int i = 0; i < ds.data.length; ++i) {
            ds.data[i] = -data[i];
        }
        return ds;
    }

    /** {@inheritDoc} */
    public DerivativeStructure reciprocal() {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.pow(data, 0, -1, result.data, 0);
        return result;
    }

    /** Square root.
     * @return square root of the instance
     */
    public DerivativeStructure sqrt() {
        return rootN(2);
    }

    /** Cubic root.
     * @return cubic root of the instance
     */
    public DerivativeStructure cbrt() {
        return rootN(3);
    }

    /** N<sup>th</sup> root.
     * @param n order of the root
     * @return n<sup>th</sup> root of the instance
     */
    public DerivativeStructure rootN(final int n) {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.rootN(data, 0, n, result.data, 0);
        return result;
    }

    /** {@inheritDoc} */
    public Field<DerivativeStructure> getField() {
        return new Field<DerivativeStructure>() {

            /** {@inheritDoc} */
            public DerivativeStructure getZero() {
                return new DerivativeStructure(compiler.getFreeParameters(), compiler.getOrder(), 0.0);
            }

            /** {@inheritDoc} */
            public DerivativeStructure getOne() {
                return new DerivativeStructure(compiler.getFreeParameters(), compiler.getOrder(), 1.0);
            }

            /** {@inheritDoc} */
            public Class<? extends FieldElement<DerivativeStructure>> getRuntimeClass() {
                return DerivativeStructure.class;
            }

        };
    }

    /** Power operation.
     * @param p power to apply
     * @return this<sup>p</sup>
     */
    public DerivativeStructure pow(final double p) {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.pow(data, 0, p, result.data, 0);
        return result;
    }

    /** Integer power operation.
     * @param n power to apply
     * @return this<sup>n</sup>
     */
    public DerivativeStructure pow(final int n) {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.pow(data, 0, n, result.data, 0);
        return result;
    }

    /** Power operation.
     * @param e exponent
     * @return this<sup>e</sup>
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public DerivativeStructure pow(final DerivativeStructure e)
        throws DimensionMismatchException {
        compiler.checkCompatibility(e.compiler);
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.pow(data, 0, e.data, 0, result.data, 0);
        return result;
    }

    /** Exponential.
     * @return exponential of the instance
     */
    public DerivativeStructure exp() {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.exp(data, 0, result.data, 0);
        return result;
    }

    /** Natural logarithm.
     * @return logarithm of the instance
     */
    public DerivativeStructure log() {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.log(data, 0, result.data, 0);
        return result;
    }

    /** Cosine operation.
     * @return cos(this)
     */
    public DerivativeStructure cos() {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.cos(data, 0, result.data, 0);
        return result;
    }

    /** Sine operation.
     * @return sin(this)
     */
    public DerivativeStructure sin() {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.sin(data, 0, result.data, 0);
        return result;
    }

    /** Tangent operation.
     * @return tan(this)
     */
    public DerivativeStructure tan() {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.tan(data, 0, result.data, 0);
        return result;
    }

    /** Arc cosine operation.
     * @return acos(this)
     */
    public DerivativeStructure acos() {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.acos(data, 0, result.data, 0);
        return result;
    }

    /** Arc sine operation.
     * @return asin(this)
     */
    public DerivativeStructure asin() {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.asin(data, 0, result.data, 0);
        return result;
    }

    /** Arc tangent operation.
     * @return tan(this)
     */
    public DerivativeStructure atan() {
        final DerivativeStructure result = new DerivativeStructure(compiler);
        compiler.atan(data, 0, result.data, 0);
        return result;
    }

    /** Two arguments arc tangent operation.
     * @param y first argument of the arc tangent
     * @param x second argument of the arc tangent
     * @return atan2(y, x)
     * @exception DimensionMismatchException if number of free parameters or orders are inconsistent
     */
    public static DerivativeStructure atan2(final DerivativeStructure y, final DerivativeStructure x)
        throws DimensionMismatchException {
        y.compiler.checkCompatibility(x.compiler);
        final DerivativeStructure result = new DerivativeStructure(y.compiler);
        y.compiler.atan2(y.data, 0, x.data, 0, result.data, 0);
        return result;
    }

    /** Evaluate Taylor expansion a derivative structure.
     * @param offsets parameters offsets (dx, dy, ...)
     * @return value of the Taylor expansion at x+dx, y.dy, ...
     */
    public double taylor(final double ... offsets) {
        return compiler.taylor(data, 0, offsets);
    }

    /**
     * Replace the instance with a data transfer object for serialization.
     * @return data transfer object that will be serialized
     */
    private Object writeReplace() {
        return new DataTransferObject(compiler.getFreeParameters(), compiler.getOrder(), data);
    }

    /** Internal class used only for serialization. */
    private static class DataTransferObject implements Serializable {

        /** Serializable UID. */
        private static final long serialVersionUID = 20120730L;

        /** Number of variables.
         * @Serial
         */
        private final int variables;

        /** Derivation order.
         * @Serial
         */
        private final int order;

        /** Partial derivatives.
         * @Serial
         */
        private final double[] data;

        /** Simple constructor.
         * @param variables number of variables
         * @param order derivation order
         * @param data partial derivatives
         */
        public DataTransferObject(final int variables, final int order, final double[] data) {
            this.variables = variables;
            this.order     = order;
            this.data      = data;
        }

        /** Replace the deserialized data transfer object with a {@link DerivativeStructure}.
         * @return replacement {@link DerivativeStructure}
         */
        private Object readResolve() {
            final DerivativeStructure ds = new DerivativeStructure(variables, order);
            System.arraycopy(data, 0, ds.data, 0, ds.data.length);
            return ds;
        }

    }

}
