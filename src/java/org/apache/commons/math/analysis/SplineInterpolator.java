/*
 * 
 * Copyright (c) 2003-2004 The Apache Software Foundation. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *  
 */
package org.apache.commons.math.analysis;

import java.io.Serializable;

/**
 * Computes a natural (a.k.a. "free", "unclamped") cubic spline interpolation for the data set.
 * <p>
 * The {@link #interpolate(double[], double[])} method returns a {@link PolynomialSplineFunction}
 * consisting of n cubic polynomials, defined over the subintervals determined by the x values,  
 * x[0] < x[i] ... < x[n].  The x values are referred to as "knot points."
 * <p>
 * The value of the PolynomialSplineFunction at a point x that is greater than or equal to the smallest
 * knot point and strictly less than the largest knot point is computed by finding the subinterval to which
 * x belongs and computing the value of the corresponding polynomial at <code>x - x[i] </code> where
 * <code>i</code> is the index of the subinterval.  See {@link PolynomialSplineFunction} for more details.
 * <p>
 * The interpolating polynomials satisfy: <ol>
 * <li>The value of the PolynomialSplineFunction at each of the input x values equals the 
 *  corresponding y value.</li>
 * <li>Adjacent polynomials are equal through two derivatives at the knot points (i.e., adjacent polynomials 
 *  "match up" at the knot points, as do their first and second derivatives).</li>
 * </ol>
 * <p>
 * The cubic spline interpolation algorithm implemented is as described in R.L. Burden, J.D. Faires, 
 * <u>Numerical Analysis</u>, 4th Ed., 1989, PWS-Kent, ISBN 0-53491-585-X, pp 126-131.
 *
 * @version $Revision: 1.15 $ $Date: 2004/04/02 21:16:21 $
 *
 */
public class SplineInterpolator implements UnivariateRealInterpolator, Serializable {
    
    /**
     * Computes an interpolating function for the data set.
     * @param x the arguments for the interpolation points
     * @param y the values for the interpolation points
     * @return a function which interpolates the data set
     */
    public UnivariateRealFunction interpolate(double x[], double y[]) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Dataset arrays must have same length.");
        }
        
        if (x.length < 3) {
            throw new IllegalArgumentException
                ("At least 3 datapoints are required to compute a spline interpolant");
        }
        
        // Number of intervals.  The number of data points is n + 1.
        int n = x.length - 1;   
        
        for (int i = 0; i < n; i++) {
            if (x[i]  >= x[i + 1]) {
                throw new IllegalArgumentException("Dataset x values must be strictly increasing.");
            }
        }
        
        double h[] = new double[n];
        for (int i = 0; i < n; i++) {
            h[i] = x[i + 1] - x[i];
        }
        
        double alpha[] = new double[n];
        for (int i = 1; i < n; i++) {
            alpha[i] = 3d * (y[i + 1] * h[i - 1] - y[i] * (x[i + 1] - x[i - 1])+ y[i - 1] * h[i]) /
                            (h[i - 1] * h[i]);
        }
        
        double l[] = new double[n + 1];
        double mu[] = new double[n];
        double z[] = new double[n + 1];
        l[0] = 1d;
        mu[0] = 0d;
        z[0] = 0d;
        for (int i = 1; i < n; i++) {
            l[i] = 2d * (x[i+1]  - x[i - 1]) - h[i - 1] * mu[i -1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }
       
        // cubic spline coefficients --  b is linear, c quadratic, d is cubic (original y's are constants)
        double b[] = new double[n];
        double c[] = new double[n + 1];
        double d[] = new double[n];
        
        l[n] = 1d;
        z[n] = 0d;
        c[n] = 0d;
        
        for (int j = n -1; j >=0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
            b[j] = (y[j + 1] - y[j]) / h[j] - h[j] * (c[j + 1] + 2d * c[j]) / 3d;
            d[j] = (c[j + 1] - c[j]) / (3d * h[j]);
        }
        
        PolynomialFunction polynomials[] = new PolynomialFunction[n];
        double coefficients[] = new double[4];
        for (int i = 0; i < n; i++) {
            coefficients[0] = y[i];
            coefficients[1] = b[i];
            coefficients[2] = c[i];
            coefficients[3] = d[i];
            polynomials[i] = new PolynomialFunction(coefficients);
        }
        
        return new PolynomialSplineFunction(x, polynomials);
    }

}
