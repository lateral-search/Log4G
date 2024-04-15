package com.ls.mobile.geotool.common;

import android.util.Log;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Date;

/**
 * MATLAB TO JAVA FUNCTIONS:
 *
 * @author Andres H Pityla C
 */

/**
 * // NOTES:
 * <p>
 * // There are some usefull artifacts for working with matrices
 * // ALL INFO IS HERE: https://java-matrix.org/
 * <p>
 * // JSCIENCE::comes with geographic package
 * // http://jscience.org/api/index.html
 * <p>
 * // UJML::
 * <p>
 * // COLT:
 * // From CERN
 * <p>
 * // JAMA:
 * // http://math.nist.gov/javanumerics/jama/
 * // https://math.nist.gov/javanumerics/jama/doc/
 * <p>
 * // Very simple, just 6 classes
 * // http://www.jscience.org/        org.jscience.geography.coordinates/xyz
 * <p>
 * // OJALGO:
 * // http://ojalgo.org/
 * // Quaternions and Abstract Algebra
 *
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 *
 */
public abstract class LSMatJLab {

    private final static String LOG_TAG = LSMatJLab.class.getSimpleName();

    // MATLAB returns of a function are declared i.e.:
    //
    // function retval = diffuse (sx, sy, sz, lv)
    //   ## ( BODY )
    //   retval = (sx * lv(1) + sy * lv(2) + sz * lv(3)) ./ ns;
    //   retval(retval < 0) = 0;
    // endfunction

    /**
     * MATLAB hours
     * Duration in hourscollapse all in page
     * Syntax
     * H = hours(X)
     * <p>
     * % H = hours(X) returns an array of hours equivalent
     * %     to the values in X.
     * <p>
     * Description
     * example
     * H = hours(X) returns an array of hours equivalent to the values in X.
     * If X is a numeric array, then H is a duration array in units of hours.
     * If X is a duration array, then H is a double array with each element equal to the number
     * of hours in the corresponding element of X.
     * The hours function converts between duration and double values. To display a duration in
     * units of hours, set its Format property to 'h'.
     */
    public static double[][] hours(double[][] hoursQuantity) {
        Log.i(LOG_TAG, "LSMatJLab.hours");
        double[][] result = new double[hoursQuantity.length - 1][1];

        for (int i = 0; i < hoursQuantity.length - 1; i++) {
            // Get % of hours
            // 1 Hour is 60 min, 50 min is 60min/50min
            // result[i][0] = hoursQuantity[i][0] / 60;
            result[i][0] = hoursQuantity[i][0] / (60 * 60 * 1000);

            //Log.i(LOG_TAG, "LSMatJLab.hours.result:" + result[i][0]);
        }
        return result;
    }

    /**
     * MATLAB diff java implementation.
     * Only works for one column matrices (vector)
     *
     * @param value
     * @return
     */
    public static double[][] diff(double[][] value) {
        // value.length - 1 : because in diff we input n elements, and diff returns n-1 elements
        double[][] result = new double[value.length - 1][1];
        for (int k = 0; k < value.length - 1; k++) {
            // value.length - 1 is effect of cause value[k + 1][0] see below
            result[k][0] = value[k + 1][0] - value[k][0];
        }
        return result;
    }

    /**
     * diffDateInHours
     * <p>
     * Substract two dates
     * long diff = Math.abs(d1.getTime() - d2.getTime());
     * long diffDays = diff / (24 * 60 * 60 * 1000);
     * <p>
     * 1 minute = 60 seconds
     * 1 hour   = 60 x 60 = 3600
     * 1 day    = 3600 x 24 = 86400
     * <p>
     * long secondsInMilli = 1000;
     * long minutesInMilli = secondsInMilli * 60;
     * long hoursInMilli = minutesInMilli * 60;
     * long daysInMilli = hoursInMilli * 24;
     * <p>
     * long elapsedHours = different / hoursInMilli;
     * different = different % hoursInMilli;
     *
     * @param value
     * @return
     */
    public static double[][] diffDate(Date[][] value) {
        double[][] result = new double[value.length][1];

        for (int k = 0; k < value.length - 1; k++) {
            // Get msec from each, and subtract.
            double diff = (value[k + 1][0]).getTime() - (value[k][0]).getTime();//Math.abs(
            // Get quantity of time in HH:mm:ss
            // result[k][0] = diff / (60 * 60 * 1000); //(24 * 60 * 60 * 1000);
            result[k][0] = diff;

            //Log.i(LOG_TAG, "diffDate.diffHoursInMinutes DOUBLE::" + result[k][0]);
        }

        return result;
    }


    /**
     * dateDaysDiff
     * <p>
     * Compute the difference between two dates and return the result in days
     *
     * @param d1 Date to be substracted
     * @param d2 Date to substract to d1
     * @return the result in numbers of days
     */
    public long dateDaysDiff(Date d1, Date d2) {
        // Get msec from each, and subtract.
        long diff = d2.getTime() - d1.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    /**
     * ones(nbrRows,nbrCols)
     * <p>
     * Creates array of all ones collapse all in page
     * i.e: ones(3,1)
     * will return:
     * 1
     * 1
     * 1
     * <p>
     * Syntax
     * X = ones
     * X = ones(n)
     * X = ones(sz1,...,szN)
     * X = ones(sz)
     * X = ones(classname)
     * X = ones(n,classname)
     * X = ones(sz1,...,szN,classname)
     * X = ones(sz,classname)
     * X = ones('like',p)
     * X = ones(n,'like',p)
     * X = ones(sz1,...,szN,'like',p)
     * X = ones(sz,'like',p)
     * Description
     * X = ones returns the scalar 1.
     * example
     * X = ones(n) returns an n-by-n matrix of ones.
     * example
     * X = ones,...,szN) returns an sz1-by-...-by-szN array of ones where sz1,...,szN indicates the size of each dimension. For example, ones(2,3) returns a 2-by-3 array of ones.
     * example
     * X = ones(sz) returns an array of ones where the size vector, sz, defines size(X). For example, ones([2,3]) returns a 2-by-3 array of ones.
     * X = ones(classname) returns a scalar 1 where classname specifies the data type. For example, ones('int8') returns a scalar, 8-bit integer 1.
     */
    public static double[][] ones(int rows, int cols) {
        double[][] result = new double[rows][cols];
        for (int m = 0; m < rows; m++) {
            for (int n = 0; n < cols; n++) {
                result[m][n] = 1;
            }
        }
        return result;
    }

    /**
     * MATLAB: diag
     * <p>
     * Create diagonal matrix or get diagonal elements of matrixcollapse all in page
     * Syntax
     * D = diag(v)
     * D = diag(v,k)
     * <p>
     * x = diag(A)
     * x = diag(A,k)
     * <p>
     * Description
     * example
     * D = diag(v) returns a square diagonal matrix with the elements of vector v on the main diagonal.
     * example
     * D = diag(v,k) places the elements of vector v on the kth diagonal. k=0 represents the main diagonal, k>0 is above the main diagonal, and k<0 is below the main diagonal.
     * example
     * x = diag(A) returns a column vector of the main diagonal elements of A.
     * example
     * x = diag(A,k) returns a column vector of the elements on the kth diagonal of A.
     * Examples
     * collapse all
     * Create Diagonal Matrices
     * Open Live Script
     * Create a 1-by-5 vector.
     * <p>
     * v = [2 1 -1 -2 -5];
     * Use diag to create a matrix with the elements of v on the main diagonal.
     * <p>
     * D = diag(v)
     * D = 5×5
     * <p>
     * 2     0     0     0     0
     * 0     1     0     0     0
     * 0     0    -1     0     0
     * 0     0     0    -2     0
     * 0     0     0     0    -5
     */
    public static double[][] diag(double[] diagValuesVector) {
        RealMatrix rm = MatrixUtils.createRealDiagonalMatrix(diagValuesVector);
        return rm.getData();
    }

    /**
     * IN MATLAB::
     * <p>
     * M = SIZE(X,DIM) returns the length of the dimension specified
     * by the scalar DIM.  For example, SIZE(X,1) returns the number
     * of rows. If DIM > NDIMS(X), M will be 1.
     * <p>
     * IN JAVA::
     * <p>
     * int[][] foo = new int[][] {
     * new int[] { 1, 2, 3 },
     * new int[] { 1, 2, 3, 4},
     * };
     * <p>
     * System.out.println(foo.length); //2
     * System.out.println(foo[0].length); //3
     * System.out.println(foo[1].length); //4
     *
     * @param value
     * @return
     */
    public static int size(double[][] value, int d) {
        return value[d].length;
    }

    /**
     * JAVA IMPLEMENTATION of MATLAB::repmat
     * <p>
     * Repeat copies of arraycollapse all in page
     * Syntax
     * B = repmat(A,n)
     * B = repmat(A,r1,...,rN)
     * B = repmat(A,r)
     * Description
     * example
     * B = repmat(A,n) returns an array containing n copies of A in the row and column dimensions.
     * The size of B is size(A)*n when A is a matrix.
     */
    public static double[][] repmat(double[][] inputMatrix, int size) {
        int oldY = inputMatrix.length;
        int oldX = inputMatrix[0].length;
        int newArryHeight = oldY * size;
        int newArryWidth = oldX * size;
        double[][] current = new double[newArryHeight][newArryWidth];

        // Go through the new array length
        for (int x = 0; x < current.length; x++) {
            int indexY = 0;
            // Loop up to the given size
            for (int count = 0; count < size; count++) {
                for (int k = 0; k < oldY; k++) {
                    int indexX = 0;
                    // Loop up to the given size
                    for (int z = 0; z < size; z++) {
                        for (int j = 0; j < oldX; j++) {
                            // Fill the new array from old values
                            current[indexY][indexX] = inputMatrix[k][j];
                            indexX++;
                        }
                    }
                    indexY++;
                }
            }
        }
        // PRINT ARRAY TO LOG RESULT
        /*System.out.println("======================================");
        for (int i = 0; i < current.length; i++) {
            for (int j = 0; j < current[0].length; j++) {
                System.out.print(current[i][j]);
            }
            System.out.println();
        }*/
        return current;
    }
}