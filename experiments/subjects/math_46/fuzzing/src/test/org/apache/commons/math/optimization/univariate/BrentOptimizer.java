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
package test.org.apache.commons.math.optimization.univariate;

import test.org.apache.commons.math.exception.MathUserException;
import test.org.apache.commons.math.exception.NotStrictlyPositiveException;
import test.org.apache.commons.math.exception.NumberIsTooSmallException;
import test.org.apache.commons.math.optimization.ConvergenceChecker;
import test.org.apache.commons.math.optimization.GoalType;
import test.org.apache.commons.math.util.FastMath;
import test.org.apache.commons.math.util.MathUtils;

/**
 * Implements Richard Brent's algorithm (from his book "Algorithms for
 * Minimization without Derivatives", p. 79) for finding minima of real
 * univariate functions. This implementation is an adaptation partly
 * based on the Python code from SciPy (module "optimize.py" v0.5).
 * If the function is defined on some interval {@code (lo, hi)}, then
 * this method finds an approximation {@code x} to the point at which
 * the function attains its minimum.
 * <br/>
 * The user is responsible for calling {@link
 * #setConvergenceChecker(ConvergenceChecker) ConvergenceChecker}
 * prior to using the optimizer.
 *
 * @version $Id$
 * @since 2.0
 */
public class BrentOptimizer extends AbstractUnivariateRealOptimizer {
    /**
     * Golden section.
     */
    private static final double GOLDEN_SECTION = 0.5 * (3 - FastMath.sqrt(5));
    /**
     * Minimum relative tolerance.
     */
    private static final double MIN_RELATIVE_TOLERANCE = 2 * FastMath.ulp(1d);
    /**
     * Relative threshold.
     */
    private final double relativeThreshold;
    /**
     * Absolute threshold.
     */
    private final double absoluteThreshold;

    /**
     * The arguments are used implement the original stopping criterion
     * of Brent's algorithm.
     * {@code abs} and {@code rel} define a tolerance
     * {@code tol = rel |x| + abs}. {@code rel} should be no smaller than
     * <em>2 macheps</em> and preferably not much less than <em>sqrt(macheps)</em>,
     * where <em>macheps</em> is the relative machine precision. {@code abs} must
     * be positive.
     *
     * @param rel Relative threshold.
     * @param abs Absolute threshold.
     * @throws NotStrictlyPositiveException if {@code abs <= 0}.
     * @throws NumberIsTooSmallException if {@code rel < 2 * Math.ulp(1d)}.
     */
    public BrentOptimizer(double rel,
                          double abs) {
        if (rel < MIN_RELATIVE_TOLERANCE) {
            throw new NumberIsTooSmallException(rel, MIN_RELATIVE_TOLERANCE, true);
        }
        if (abs <= 0) {
            throw new NotStrictlyPositiveException(abs);
        }
        relativeThreshold = rel;
        absoluteThreshold = abs;
    }

    /** {@inheritDoc} */
    @Override
    protected UnivariateRealPointValuePair doOptimize() throws MathUserException {
        final boolean isMinim = getGoalType() == GoalType.MINIMIZE;
        final double lo = getMin();
        final double mid = getStartValue();
        final double hi = getMax();

        // Optional additional convergence criteria.
        final ConvergenceChecker<UnivariateRealPointValuePair> checker
            = getConvergenceChecker();

        double a;
        double b;
        if (lo < hi) {
            a = lo;
            b = hi;
        } else {
            a = hi;
            b = lo;
        }

        double x = mid;
        double v = x;
        double w = x;
        double d = 0;
        double e = 0;
        double fx = computeObjectiveValue(x);
        if (!isMinim) {
            fx = -fx;
        }
        double fv = fx;
        double fw = fx;

        UnivariateRealPointValuePair previous = null;
        UnivariateRealPointValuePair current
            = new UnivariateRealPointValuePair(x, isMinim ? fx : -fx);

        int iter = 0;
        while (true) {
            final double m = 0.5 * (a + b);
            final double tol1 = relativeThreshold * FastMath.abs(x) + absoluteThreshold;
            final double tol2 = 2 * tol1;

            // Default stopping criterion.
            final boolean stop = FastMath.abs(x - m) <= tol2 - 0.5 * (b - a);
            if (!stop) {
                double p = 0;
                double q = 0;
                double r = 0;
                double u = 0;

                if (FastMath.abs(e) > tol1) { // Fit parabola.
                    r = (x - w) * (fx - fv);
                    q = (x - v) * (fx - fw);
                    p = (x - v) * q - (x - w) * r;
                    q = 2 * (q - r);

                    if (q > 0) {
                        p = -p;
                    } else {
                        q = -q;
                    }

                    r = e;
                    e = d;

                    if (p > q * (a - x) &&
                        p < q * (b - x) &&
                        FastMath.abs(p) < FastMath.abs(0.5 * q * r)) {
                        // Parabolic interpolation step.
                        d = p / q;
                        u = x + d;

                        // f must not be evaluated too close to a or b.
                        if (u - a < tol2 || b - u < tol2) {
                            if (x <= m) {
                                d = tol1;
                            } else {
                                d = -tol1;
                            }
                        }
                    } else {
                        // Golden section step.
                        if (x < m) {
                            e = b - x;
                        } else {
                            e = a - x;
                        }
                        d = GOLDEN_SECTION * e;
                    }
                } else {
                    // Golden section step.
                    if (x < m) {
                        e = b - x;
                    } else {
                        e = a - x;
                    }
                    d = GOLDEN_SECTION * e;
                }

                // Update by at least "tol1".
                if (FastMath.abs(d) < tol1) {
                    if (d >= 0) {
                        u = x + tol1;
                    } else {
                        u = x - tol1;
                    }
                } else {
                    u = x + d;
                }

                double fu = computeObjectiveValue(u);
                if (!isMinim) {
                    fu = -fu;
                }

                // Update a, b, v, w and x.
                if (fu <= fx) {
                    if (u < x) {
                        b = x;
                    } else {
                        a = x;
                    }
                    v = w;
                    fv = fw;
                    w = x;
                    fw = fx;
                    x = u;
                    fx = fu;
                } else {
                    if (u < x) {
                        a = u;
                    } else {
                        b = u;
                    }
                    if (fu <= fw ||
                        MathUtils.equals(w, x)) {
                        v = w;
                        fv = fw;
                        w = u;
                        fw = fu;
                    } else if (fu <= fv ||
                               MathUtils.equals(v, x) ||
                               MathUtils.equals(v, w)) {
                        v = u;
                        fv = fu;
                    }
                }

                previous = current;
                current = new UnivariateRealPointValuePair(x, isMinim ? fx : -fx);

                // User-defined convergence checker.
                if (checker != null) {
                    if (checker.converged(iter, previous, current)) {
                        return current;
                    }
                }
            } else { // Default termination (Brent's criterion).
                return current;
            }
            ++iter;
        }
    }
}
