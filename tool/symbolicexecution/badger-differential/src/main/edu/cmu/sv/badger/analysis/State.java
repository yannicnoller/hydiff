/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.badger.analysis;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 */
public abstract class State implements Comparable<State> {
  private final PathCondition pc;
  
  public State(PathCondition pc) {
    this.pc = pc;
  }
  
  public PathCondition getPathCondition() {
    return this.pc;
  }
  
  // Should this return a double, or would we like to parameterize this (e.g. to subclasses of Number)?
  public abstract double getOldWC();
  public abstract double getNewWC();
}
