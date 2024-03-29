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

package test.org.apache.commons.math.ode.jacobians;

import test.org.apache.commons.math.ode.FirstOrderDifferentialEquations;


/** This interface represents {@link FirstOrderDifferentialEquations
 * first order differential equations} with parameters.
 *
 * @see FirstOrderIntegratorWithJacobians
 *
 * @version $Revision$ $Date$
 * @since 2.1
 */

public interface ParameterizedODE
    extends FirstOrderDifferentialEquations {

    /** Get the number of parameters.
     * @return number of parameters
     */
    int getParametersDimension();

    /** Set a parameter.
     * @param i index of the parameters (must be between 0
     * and {@link #getParametersDimension() getParametersDimension() - 1})
     * @param value value for the parameter
     */
    void setParameter(int i, double value);

}
