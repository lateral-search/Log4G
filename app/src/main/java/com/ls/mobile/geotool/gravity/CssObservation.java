package com.ls.mobile.geotool.gravity;

import android.util.Log;

import com.ls.mobile.geotool.db.data.model.Calibration;
import com.ls.mobile.geotool.db.data.model.Point;
import com.ls.mobile.geotool.time.DateConverter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import umontreal.ssj.functionfit.SmoothingCubicSpline;

/**
 * CSSOBSERVATIONS (MATLAB comments)::
 *
 * class to hold one single observation.
 * % It references the benchmark class to know where the measurement
 * % was done. It also holds information like instrument, direction,
 *   raw_data, etc.
 *
 * @author Andres Hernan Pityla C
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public class CssObservation {

    private static final String LOG_TAG = CssObservation.class.getSimpleName();

    public CssObservation() {
    }

    /**
     * COMPUTE REDUCED G::
     * <p>
     * This function is called every time a POINT is recorded or modified.
     * This function is not called to make the LINE CLOSING.
     * <p>
     * ORIGINAL MATLAB FUNCTION::
     * function self = compute_reduced_g(self, calibration)
     * <p>
     * MAIL DEMIAN:
     * Estaría bueno agregar la columna reading (el promedio de g1, g2, g3) y reduced_g
     * (la gravedad reducida usando la formula que te había dado cuando nos reunimos la primera vez,
     * que hace falta para el chequeo de consistencia).
     *
     * @param calibration
     * @param point
     */
    public double computeReducedG(List<Calibration> calibration, Point point) {
        // % from Kevin's function reduceLCR.m
        // % LCR   Reduction of Lacoste Romberg gravimeter readings
        // % This function applies the calibration curve established
        //   by the manufacturer for each gravimeter,
        //   and also applies a gravity tide correction

        // g_reading is the mean of the observations G1,G2,G3 over the POINT
        double g_reading = (point.getG1() + point.getG2() + point.getG3()) / 3; // MATLAB ln:139

        //Log.i(LOG_TAG, "##138#CSSObservation::calibration" + calibration.iterator().toString());
        //Log.i(LOG_TAG, "##141#CSSObservation::self.reading = g_reading" + g_reading);

        // ORIGINAL MATLAB CODE of the next section:
        // % the grvmtr readings corresponding to the values in the calib curve
        // f(x) in MATLAB: gx =100*(0:1:np-1);
        // ...
        // and.... the MATLAB SYNTAX::
        // The colon operator also allows you to create an equally spaced vector
        // of values using the more general form start:step:end.
        //        B = 0:10:100
        // B = 0    10    20    30    40    50    60    70    80    90   100
        //
        // if B = 100*(0:10:100)
        // ans = 0    1000    2000    3000    4000    5000    6000    etc....
        //
        //   (1.0)**This is why  gx[i] = gx[i] * 100; int the line

        // MATLAB ln:142
        int np = calibration.size();

        // IN : MATLAB ln:143  % the grvmtr readings corresponding to the values in the calib curve
        double[] calibrationVector = new double[np]; //
        double[] gx = new double[np];                //
        for (int i = 0; i < gx.length; i++) {
            gx[i] = i;

            //Log.i(LOG_TAG, "i VALUE:" + i);
            //Log.i(LOG_TAG, "gx[i] VALUE:" + gx[i]);

            gx[i] = gx[i] * 100; // (1.0) **See comment above
            //Log.i(LOG_TAG, "gx[i] = gx[i] * 100; VALUE:" + gx[i]);

            calibrationVector[i] = calibration.get(i).getCalibrationValue();

            //Log.i(LOG_TAG, "calibrationVector[i] getCalibrationValue(); VALUE:" + calibration.get(i).getCalibrationValue());
            //Log.i(LOG_TAG, "calibrationVector[i] calibrationValueIndex(); VALUE:" + calibration.get(i).getCalibrationValueIndex());
        }
        // OUT : MATLAB ln:143

        //noinspection ImplicitArrayToString
        //Log.i(LOG_TAG, "##149#CSSObservation::gx=100*(0:1:np-1);" + gx.toString());

        // IN : MATLAB ln:145
        if (g_reading < gx[1] || g_reading > gx[gx.length - 1]) { //
            Log.i(LOG_TAG, "ERROR:The g_reading falls outside the calibrated range of this instrument!");
        }
        // OUT : MATLAB ln:145

        // % apply the calibration curve
        // SOURCE INFO: https://uk.mathworks.com/help/matlab/ref/spline.html
        // SYNTAX:
        // s = spline(x,y,xq)
        // pp = spline(x,y)

        // s = spline(x,y,xq) returns
        // RESULT: s = corresponding a vector of interpolated values
        // to the query points in xq.
        // The values of "s" are determined by cubic spline interpolation of x and y.

        // IN MATLAB: the function: spline(gx, calibration, g_reading); Returns the cubic spline
        // interpolant of points @var{x} and @var{y}.

        //## When called with two arguments, return the piecewise polynomial @var{pp}
        //## that may be used with @code{ppval} to evaluate the polynomial at specific
        //## points.

        //## When called with a third input argument, @code{spline} evaluates the spline
        //## at the points @var{xi}.  The third calling form
        //## @code{spline (@var{x}, @var{y}, @var{xi})} is equivalent to
        //## @code{ppval (spline (@var{x}, @var{y}), @var{xi})}.

        // gg=spline(gx, calibration, g_reading);
        // gx[i] = gx[i] * 100;
        // calibration
        // g_reading = (point.getG1() + point.getG2() + point.getG3())/3;

        /** SOURCE::
         * https://stackoverflow.com/questions/20303172/proper-implementation-of-cubic-spline-interpolation
         *
         * Splines are piecewise(in parts "pieces") polynomials that are smoothly
         * connected together. For a spline of degree n, each segment is a polynomial
         * of degree n. The pieces are connected so that the spline is
         * continuous up to its derivative of degree n-1 at the knots, namely,
         * the joining points of the polynomial pieces.
         *
         * How can splines be constructed?
         * The zero-th order spline is the following
         /   1       -0.5 < x < 0.5
         B(0)(X)= |    0.5     |x| = 0.5
         \   0       otherwise

         All the other splines can be constructed as
         b(n)(x)=b(0)(x)*b(0)(x)*......*b(0)(x)
         where the convolution is taken n-1 times.

         Cubic splines::
         The most popular splines are cubic splines, whose expression is
         /    2/3 - |x|^2  +  (|x|^3)/2       0 <= |x| < 1
         B(3)(X)= |    (2-|x|)^3)/6                     1 <= |x| <= 2
         \     0                              2 <  |x|

         //* SPLINE cubics

         SPLINE is not interpolation but approximation to use them you do not need any derivation.
         If you have ten points: p0,p1,p2,p3,p4,p5,p6,p7,p8,p9 then cubic spline starts/ends with
         triple point. If you create function to 'draw' SPLINE cubic curve patch then to assure
         continuity the call sequence will be like this:

         spline(p0,p0,p0,p1);
         spline(p0,p0,p1,p2);
         spline(p0,p1,p2,p3);
         spline(p1,p2,p3,p4);
         etc.....
         spline(p8,p9,p9,p9);
         */

        /**
         * * The spline is computed with a smoothing parameter rho \in[0, 1]
         * which represents its accuracy with respect to the initial (x_i, y_i)nodes.
         * The smoothing spline minimizes
         *
         * By setting rho= 1, we obtain the interpolating spline;
         * and we obtain a linear function by setting rho= 0. The weights
         * @f$w_i>0@f$, which default to 1, can be used to change the contribution of
         * each point in the error term. A large value w_i will give a large
         * weight to the ith point, so the spline will pass closer to it. Here
         * is a small example that uses smoothing splines:
         *
         * @code
         *
         *    int n;
         *    double[] X = new double[n];
         *    double[] Y = new double[n];
         *    // here, fill arrays X and Y with n data points (x_i, y_i)
         *    // The points must be sorted with respect to x_i.
         *
         *    double rho = 0.1;
         *    SmoothingCubicSpline fit = new SmoothingCubicSpline(X, Y, rho);
         *
         *    int m = 40;
         *    double[] Xp = new double[m+1];       // Xp, Yp are spline points
         *    double[] Yp = new double[m+1];
         *    double h = (X[n-1] - X[0]) / m;      // step
         *
         *    for (int i = 0; i <= m; i++) {
         *       double z = X[0] + i * h;
         *       Xp[i] = z;
         *       Yp[i] = fit.evaluate(z);          // evaluate spline at z
         *    }
         */
        //  gx,calibration.,g_reading,rho must be sorted in increasing order.

        /** SmoothingCubicSpline INPUT ARGS:
         *
         * x - the xi coordinates :                gx[i] = gx[i] * 100; ( i.e.: 0 100 200 300..etc. )
         * y - the yi coordinates :                calibrationVector[size()] (All provided by the
         *                                         manufacturer.
         * w -  weight 4 each point, must be >0::  (point.getG1() + point.getG2() + point.getG3())/3
         *                                         // IMPORTANT!!:: w must be an array.
         * rho- the smoothing parameter :          0.1
         *
         * IMPORTANT NOTE!!!! ::
         * // SPLINES ARE USED JUST FOR CALIBRATION CURVE CALCULATIONS, THIS BELONGS TO
         * // EACH GRAVIMETER, AND THE CALIBRATIONS VALUES ARE PROVIDED BY THE MANUFACTURER
         */
        // APPLY CALIBRATION CURVE :: IN
        double rho = 1;
        double[] g_r = new double[gx.length]; //WEIGHT OF THE CONTROL POINT
        g_r[0] = g_reading;

        SmoothingCubicSpline gg = new SmoothingCubicSpline(gx, calibrationVector, g_r, rho);
        // APPLY CALIBRATION CURVE :: OUT

        /**
         * GUIDE: SEE MATLAB SPLINES HELP:
         * El archivo de calibracion contiene las y del chart
         * Los 1000 en 1000 de 0 a 7000 son las x del chart
         * El pto de control es un pto que va a ser el valor de X del
         * pto que deseamos ubicar en la curva
         * El spline del x anterior va a ser el Y del pto en cuestion
         * Ese Y va a ser un punto intermedio en la curva de
         * calibracion, que fue dado por el spline
         *
         *spline de MATLAB  devuelve el valor de y para el valor del punto de control
         * gg es Y
         */

        // % compute gravity tide
        // MAT:tide = self.gtide(self);  [MATLAB version]
        double tide = calculateGTide(point);

        //Log.i(LOG_TAG, "##155#CSSObservation::tide = self.gtide(self);" + tide);

        // % apply tidal correction
        // MAT:  reduced_g = gg                 + tide;
        //double reduced_g = gg.getWeights()[0] + tide;
        double ggResult = gg.evaluate(g_reading);
        double reduced_g = ggResult + tide;

        //Log.i(LOG_TAG, "##152#CSSObservation::GG.EVALUATE (CUBIC SPLINE EVAL RESULT)::" + ggResult);
        //Log.i(LOG_TAG, "##155#reduced_g = gg.evaluate(g_reading) + tide;" + reduced_g);

        //% note that the sign convention for the gravity tide
        //% means it must be added rather than subtracted

        //reduced_g = reduced_g + offset * 0.3086;
        Log.i(LOG_TAG, "REDUCED_G:: " + reduced_g);

        return reduced_g;
    }

    /**
     * CALCULATE GTIDE::
     *
     * %GTIDE Computes the gravity tide with or w/o an earth tide correction
     % This matlab function follows the general structure of the
     % fortran subroutine TIDE.FOR written by R. Forsberg, which is based
     % on the equations of Longman (1959). The (optional) earth tide correction
     % is that used in the R. Forsberg's fortran code GRREDU.FOR
     %
     % USAGE:   tide = gtide(time,lat,lon,height,etidec)
     %
     % INPUT ARGUMENTS:
     %    time   date and time stated in days since UTC noon 31 Dec 1899
     %             (see matlab function JD1900.m)
     %     lat   station latitude in degrees (N is +ve)
     %     lon   station longitude in degrees (E is +ve)
     %  height   station height in meters. Technically this should be an
     %             orthometric height (i.e. height above sea level) but
     %             if ellipsoidal height is used instead, the resulting
     %             error is negligible
     % etidec    determines if an earth tide correction (ETC) is applied
     %             if etidec=1     ETC is applied
     %             if etidec=0     ETC is not applied
     %
     % OUTPUT ARGUMENT:
     %    tide   gravity tide in milligals
     %
     % Input Argument Sizes
     %    The physical arguments time,lat,lon and height must follow these
     % rules. They can (1) all be scalar, or (2) one of the can be a vector
     % and the others scalar, or (3) all four arguments can be vectors, but
     % they must have the same size and shape - in which can tide(i) will
     % be computed at [ lat(i) lon(i) ht(i)] at time(i).
     %
     % See also function JD1900.m

     % Version 1.0           Michael Bevis             17 April 2009

     * @param aPoint
     */

    /**
     * GUIDE::
     * http://bluewhaleprocessing.com/bwpupdates/installers/documentation-on-line/ch08.html
     */
    public static double calculateGTide(Point aPoint) {
        // LINE 28: on CSSObservation.m
        // % cal2jd returns the same values as Kevin's AJD.
        // self.epoch     = cal2jd(year,month,day + hour/24 + minute/1440);
        double julianDate = 0;
        try {
            /*
             * Although the <code>Date</code> class is intended to reflect
             * coordinated universal time (UTC), it may not do so exactly,
             * depending on the host environment of the Java Virtual Machine.
             * Nearly all modern operating systems assume that 1&nbsp;day&nbsp;=
             */
            Date time;

            if (aPoint.getDate().contains(" ")) {
                time = DateConverter.getDateFrom_yyyy_MM_ddbHH_mm(aPoint.getDate());

            } else {
                time = DateConverter.getDateFromString(aPoint.getDate());

            }

            //Log.i(LOG_TAG, "time   :" + time);

            Calendar calendar = DateConverter.getGregorianCalendarWithoutTZ(time);
            //Log.i(LOG_TAG, "calendar   :" + calendar);
            julianDate = DateConverter.toJulianDate(calendar);
            //Log.i(LOG_TAG, "julianDate   :" + julianDate);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        double lat = aPoint.getLatitude();
        //Log.i(LOG_TAG, "lat   :" + lat);
        double lon = aPoint.getLongitude();
        //Log.i(LOG_TAG, "lon   :" + lon);
        double height = aPoint.getHeight();
        //Log.i(LOG_TAG, "height   :" + height);
        int etidec = 1; // harcoded on the MATLAB code

        //Log.i(LOG_TAG, "aPoint.getLatitude()LIKE MATLAB ##210#CSSObservationTIDE::" + aPoint.getLatitude());


        // Dummy Matlab Code
        if (etidec != 0 && etidec != 1) { // ~ =   !=
            Log.i(LOG_TAG, "etidec must be 0 or 1");
        }

        double dtr = Math.PI / 180;
        //Log.i(LOG_TAG, "dtr   :" + dtr);
        double e = 0.054899720;
        //Log.i(LOG_TAG, "e    :" + e);
        double c = 3.84402e10;
        //Log.i(LOG_TAG, "c    :" + c);
        double c1 = 1.495e13;
        //Log.i(LOG_TAG, "c1    :" + c1);
        double aprim = 1 / (c * (1 - e * e)); //
        //Log.i(LOG_TAG, "aprim    :" + aprim);
        double i = 0.08979719;
        //Log.i(LOG_TAG, "i    :" + i);
        double omega = 0.4093146162;
        //Log.i(LOG_TAG, "omega    :" + omega);
        double ss = 1.993e33;
        //Log.i(LOG_TAG, "ss    :" + ss);
        double mm = 7.3537e25;
        //Log.i(LOG_TAG, "mm    :" + mm);
        double my = 6.670e-8;
        //Log.i(LOG_TAG, "my    :" + my);
        double m = 0.074804;
        //Log.i(LOG_TAG, "m    :" + m);

        // %  computation point
        double coslambda = Math.cos(lat * dtr);
        //Log.i(LOG_TAG, "coslambda    :" + coslambda);
        double sinlambda = Math.sin(lat * dtr);
        //Log.i(LOG_TAG, "sinlambda    :" + sinlambda);

        //     r = 6.378270e8.      /sqrt(1+0.006738*sinlambda.^2)+ height*100;
        double r = 6.378270e8 / Math.sqrt(1 + 0.006738 * Math.pow(sinlambda, 2)) + height * 100;
        //Log.i(LOG_TAG, "r    :" + r);
        double ll = lon * dtr;
        //Log.i(LOG_TAG, "ll    :" + ll);

        // % some fundamental time-predictable quantities
        //   DateConverter.toGregorianDate(time);
        //   double tt  = time/36525;
        double tt = julianDate / 36525;
        //Log.i(LOG_TAG, "tt    :" + tt);
        double tt2 = Math.pow(tt, 2); //  tt.^2;
        //Log.i(LOG_TAG, "tt2    :" + tt2);
        double tt3 = Math.pow(tt, 3); // tt.^;
        //Log.i(LOG_TAG, "tt3    :" + tt3);
        double s = 4.720023438 + 8399.7093 * tt + 4.40695e-5 * tt2 + 3.29e-8 * tt3;
        //Log.i(LOG_TAG, "s    :" + s);
        double p = 5.835124721 + 71.018009 * tt - 1.80546e-4 * tt2 - 2.181e-7 * tt3;
        //Log.i(LOG_TAG, "p    :" + p);
        double h = 4.881627934 + 628.33195 * tt + 5.27960e-6 * tt2;
        //Log.i(LOG_TAG, "h    :" + h);
        double N = 4.523588570 - 33.757153 * tt + 3.67488e-5 * tt2 + 3.870e-8 * tt3;
        //Log.i(LOG_TAG, "N    :" + N);
        double p1 = 4.908229467 + 3.0005264e-2 * tt + 7.9024e-6 * tt2 + 5.81e-8 * tt3;
        //Log.i(LOG_TAG, "p1    :" + p1);
        double e1 = 0.01675104 - 4.18e-5 * tt - 1.26e-7 * tt2;
        //Log.i(LOG_TAG, "e1    :" + e1);

        // %  reciproc distances
        //     a1prim= 1./(c1*(1-         e1.^2));
        double a1prim = 1 / (c1 * (1 - Math.pow(e1, 2)));
        //Log.i(LOG_TAG, "a1prim    :" + a1prim);
        double resd = 1 / c + aprim * e * Math.cos(s - p) + aprim * e * e * Math.cos(2 * (s - p))
                + 15e0 / 8 * aprim * m * e * Math.cos(s - 2 * h + p)
                + aprim * m * m * Math.cos(2 * (s - h));
        //Log.i(LOG_TAG, "resd    :" + resd);

        //     resdd  = 1/c1 + a1prim.*e1.*Math.cos(h-p1);
        double resdd = 1 / c1 + a1prim * e1 * Math.cos(h - p1);
        //Log.i(LOG_TAG, "resdd    :" + resdd);

        // %  longitude of moons ascending node
        double cosii = Math.cos(omega) * Math.cos(i) - Math.sin(omega) * Math.sin(i) * Math.cos(N);
        //Log.i(LOG_TAG, "cosii    :" + cosii);
        double sinii = Math.sqrt(1 - Math.pow(cosii, 2));
        // sinii =      sqrt(1-         cosii.^2);
        //Log.i(LOG_TAG, "sinii    :" + sinii);

        double ii = Math.atan(sinii / cosii);
        // ii    =      atan(sinii./cosii);
        //Log.i(LOG_TAG, "ii    :" + ii);

        double ny = Math.asin(Math.sin(i) * Math.sin(N) / sinii);
        //ny    =           asin(sin(i)*sin(N)./sinii);
        //Log.i(LOG_TAG, "ny    :" + ny);

        // %  longitude and rigth ascension
        //
        //     t     = 2 *      pi *    rem(time        ,1) + ll;
        double t = 2 * Math.PI * (julianDate % 1) + ll;
        //Log.i(LOG_TAG, "t    :" + t);
        //Log.i(LOG_TAG, "(julianDate % 1)    :" + (julianDate % 1));

        // rem(time      ,1)  Return the remainder of the division x / y.
        // The remainder is computed using the expression x - y .* fix (x ./ y)
        // fix (x) Truncate fractional portion of x and return the integer portion.
        // This is equivalent to rounding towards zero. If x is complex,
        // return fix (real (x)) + fix (imag (x)) * I.
        // fix ([-2.7, 2.7]) ⇒ -2    2
        double ksi1 = t + h;
        //Log.i(LOG_TAG, "ksi1    :" + ksi1);
        double ksi = ksi1 - ny;
        //Log.i(LOG_TAG, "ksi    :" + ksi);

        //     l1    = h + 2*e1.*sin(h-p1);
        double l1 = h + 2 * e1 * Math.sin(h - p1);
        //Log.i(LOG_TAG, "l1    :" + l1);
        double alfa = 2 * Math.atan((Math.sin(omega) * Math.sin(N) / sinii) / (1 + Math.cos(N) * Math.cos(ny)
                + Math.sin(N) * Math.sin(ny) * Math.cos(omega)));
        //Log.i(LOG_TAG, "alfa    :" + alfa);
        double sigma = s - N + alfa;
        //Log.i(LOG_TAG, "sigma    :" + sigma);

        //     l     = sigma +
        //             2*e*sin(s-p) +
        //             5e0/4*e*e*sin(2*(s-p))  +
        //             15e0/4*m*e*sin(s - 2*h + p) +
        //             11e0/8*m*m*sin(2*(s -h));
        double l = sigma +
                2 * e * Math.sin(s - p) +
                5e0 / 4 * e * e * Math.sin(2 * (s - p)) +
                15e0 / 4 * m * e * Math.sin(s - 2 * h + p) +
                11e0 / 8 * m * m * Math.sin(2 * (s - h));

        //Log.i(LOG_TAG, "l    :" + l);

        // %  zenith angles
        // costheta = sinlambda.*sinii.*sin(l) +
        // coslambda.*(cos(ii/2).^2.*cos(l-ksi) ... +
        // sin(ii/2).^2.*cos(l+ksi));
        double costheta = sinlambda * sinii * Math.sin(l) +
                coslambda * Math.pow(Math.cos(ii / 2), 2) * Math.cos(l - ksi) +
                Math.pow(Math.sin(ii / 2), 2) * Math.cos(l + ksi);

        //Log.i(LOG_TAG, "costheta    :" + costheta);

        double cosphi = sinlambda * Math.sin(omega) * Math.sin(l1)
                + coslambda * (Math.pow(Math.cos(omega / 2), 2) * Math.cos(l1 - ksi1)
                + Math.pow(Math.sin(omega / 2), 2) * Math.cos(l1 + ksi1));
        //Log.i(LOG_TAG, "cosphi    :" + cosphi);

        // %  gravities
        double gs = my * ss * r * Math.pow(resdd, 3) * (3 * Math.pow(cosphi, 2) - 1);
        double gm = my * mm * r * Math.pow(resd, 3) * (3 * Math.pow(costheta, 2) - 1)
                + (3 / 2) * my * mm * Math.pow(r, 2) * Math.pow(resd, 4) * (5 * Math.pow(costheta, 3) - 3 * costheta);
        double g0 = gm + gs;

        //Log.i(LOG_TAG, "APRIM::gs = my*ss*r*Math.pow(resdd" + gs);
        //Log.i(LOG_TAG, "APRIM::gm = my*mm*r*Math.pow(resd" + gm);
        //Log.i(LOG_TAG, "APRIM::g0    = gm + gs;" + g0);

        // %  transformation from the cgs unit gal to mgal
        double tide = g0 * 1000;
        //Log.i(LOG_TAG, "APRIM::double tide   = g0 * 1000;" + tide);

        // % perform earth tide correction if desired
        if (etidec == 1) {
            double delta = 1.14;
            tide = tide * delta + 0.00483 - 0.01573 * Math.pow(Math.cos(lat * dtr), 2);
            //Log.i(LOG_TAG, "APRIM::tide = tide * delta + 0.00483 - 0" + tide);

        }
        return tide;

    }// End::private void gtide(Point p) {

}