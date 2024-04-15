package com.ls.mobile.geotool.validator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.ls.mobile.geotool.PointCRUDActivity;
import com.ls.mobile.geotool.R;
import com.ls.mobile.geotool.db.GravityMobileDBHelper;
import com.ls.mobile.geotool.db.data.model.Point;
import com.ls.mobile.geotool.db.data.model.User;
import com.ls.mobile.geotool.workflow.PointStatusInterface;

import java.util.Iterator;
import java.util.List;

/**
 * PointCRUDActivityValidator
 *
 * @author LATERAL SEARCH
 *
 * 2018
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public class PointCRUDActivityValidator {

    private Context applicationContext_; // ISSUE MEMORY-LEAKS
    private static float PREEXISTENT_POINT_RADIUS_OF_LOC = 30.0f;

    private String errorMsg = "";
    private TextView tVPointCode_;// ISSUE MEMORY-LEAKS
    private Point point_;// ISSUE MEMORY-LEAKS

    private final static String LOG_TAG = PointCRUDActivityValidator.class.getSimpleName();

    // Constructor
    public PointCRUDActivityValidator(TextView tVPointCode,
                                      Point point,
                                      Context applicationContext,
                                      float radiusOfLocation) {
        tVPointCode_ = tVPointCode;
        point_ = point;
        applicationContext_ = applicationContext;
        PREEXISTENT_POINT_RADIUS_OF_LOC = radiusOfLocation;
    }

    public boolean validatePointCode() {
        return (isPointCodeUniqueInSameLine()
                && isPointInUniqueLocation());
    }

    /**
     * Point code cannot be repeated in FWD Point of the same line
     *
     * @return
     */
    private boolean isPointCodeUniqueInSameLine() {
        GravityMobileDBHelper db =
                GravityMobileDBHelper.getInstance(applicationContext_,false);
        Point preexistentPoint = db.getPointByCodeAndLineIdAndOneWayVal(point_.getCode()
                , point_.getLineId()
                , PointStatusInterface.POINT_ONEWAYVALUE_FORWARD);

        if (point_.getCode().equals(preexistentPoint.getCode())) {
            errorMsg = applicationContext_.getString(R.string.point_crud_act_val_code_not_unique);
            return false;
        } else {
            return true;
        }


    }

    /**
     * Point location cannot be repeated in same line FWD Point
     *
     * @return
     */
    private boolean isPointInUniqueLocation() {
        return true;
    }


    private void checkForPreexistentPointOnLocation(double actualLatitude,
                                                    double actualLongitude,
                                                    TextView aFieldSearchPrevLat,
                                                    TextView aFieldSearchPrevLon,
                                                    double aFieldSearchPrevHVar,
                                                    TextView aFieldSearchPrevPtCd) {

        // SEARCH FOR PREEXISTENT POINTS
        List<Point> pointLst = getPreexistentPoints(actualLatitude, actualLongitude);
        if (pointLst != null) {
            Iterator<Point> pointLstIter = pointLst.iterator();

            while (pointLstIter.hasNext()) {
                float[] floatResult = new float[1];
                Point pointAux = pointLstIter.next();
                try {
                    Location.distanceBetween(actualLatitude,
                            actualLongitude,
                            pointAux.getLatitude(),
                            pointAux.getLongitude(),
                            floatResult
                    );
                    Log.i(LOG_TAG, "actualLatitude" + actualLatitude);
                    Log.i(LOG_TAG, "actualLongitude" + actualLongitude);
                    Log.i(LOG_TAG, "destination Latitude" + pointAux.getLatitude());
                    Log.i(LOG_TAG, "destination Longitude" + pointAux.getLongitude());
                    Log.i(LOG_TAG, "DISTANCE BETWEEN RESULT::" + floatResult[0]);

                    // SELECT THE PREEXISTENT POINT, IF ITS IN A RADIUS OF X MTRS
                    if (floatResult[0] <= PREEXISTENT_POINT_RADIUS_OF_LOC) {
                        // SET SEARCH SCREEN PREEXISTENT LAT/LONG FOUND
                        aFieldSearchPrevLat.setText(String.valueOf(pointAux.getLatitude()));
                        aFieldSearchPrevLon.setText(String.valueOf(pointAux.getLongitude()));
                        aFieldSearchPrevHVar = pointAux.getHeight();
                        aFieldSearchPrevPtCd.setText(pointAux.getCode());

                        // SHOW MESSAGE??

                        // Exit
                        break;
                    }
                } catch (IllegalArgumentException ia) {
                    Log.i(LOG_TAG, ia.getMessage());
                } catch (Exception ex) {
                    Log.i(LOG_TAG, ex.getMessage());
                }
            }
        }
    }

    /**
     * @param latitude
     * @param longitude
     * @return List of preexistent points
     */
    private List<Point> getPreexistentPoints(double latitude,
                                             double longitude) {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(this.applicationContext_,false);
        List<Point> preexistentPoints = db.getPreexistentPoints(longitude, latitude, "ASC");
        // ISSUE MEMORY-LEAKS
        db.finalizeInstance();
        return preexistentPoints;
    }

    /**
     * Returns error text to show to the user.
     *
     * @return
     */
    public String getErrorMsg() {
        return errorMsg;
    }


    /**
     * VALIDATE INPUT DATA for CREATE
     *
     * @return true if valid
     */
    @SuppressLint("NewApi")
    public static boolean validateFormInputDataForCreate(
                                          PointCRUDActivity pCRUDActivity,
                                          Point point,
                                          TextView tVPointCode,
                                          TextView tVGPSLat,
                                          TextView tVGPSLong,
                                          TextView tVGPSAlt,
                                          TextView tVOffset,
                                          TextView tVUTCTime,
                                          TextView tVUserCode,
                                          ImageView iVMeasureG1,
                                          ImageView iVMeasureG2,
                                          ImageView iVMeasureG3,
                                          TextView tVMeasureG1,
                                          TextView tVMeasureG2,
                                          TextView tVMeasureG3  ) {
        String msg = pCRUDActivity.getString(R.string.point_crud_act_input_please);

        // VALIDATION: POINT COD. not empty
        if (tVPointCode.getText().length() > 0) {
            point.setCode(tVPointCode.getText().toString());
        } else {
            pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_cod_punto));
            return false;
        }

        // VALIDATE PREEXISTENCE OF POINT CODE IN THE SAME LINE.
        // POINT CODE is not in preexistent codes on DB but REPEATED in new points created
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(pCRUDActivity.getApplicationContext(),false);
        Point pAux = db.getPointByCodeAndLineIdAndOneWayVal(tVPointCode.getText().toString(),
                                                         point.getLineId(),
                                                        PointStatusInterface.POINT_ONEWAYVALUE_FORWARD);

        if(tVPointCode.getText().toString().equals(pAux.getCode())){
            pCRUDActivity.inputErrorAlert(pCRUDActivity.getString(R.string.point_crud_act_point_cod_repeated));
            return false;
        }



        // ISSUE-003 RELATED THE NEXT COULD BE AN IMPROVEMENT::
        // THE next comment could be outdated.
        // VALIDATE POINT CODE AND COORDS
        // NOT NECCESSARY!!!! VALIDATE FORWARD POINT(not neccessary, cause creating POINT manually means)
        // VALIDATE POINT CODE EXIST PREVIOUSLY
        // IF POINT CODE NOT EXIST, JUST USE IT
        // IF YES EXIST, VALIDATE IF POINT CODE IS IN THE SAME LOCATION AROUND 30mts
        // IF NOT AROUND 30Mts, THROW ERROR
        // IF YES AROUND 30Mts, OFFER TO USING IT OR


        /*
        // VALIDATOR is an improvement in case it could be useful 4 other implementations
        // this validation is used on edit COD.PTO buton
        // but didnt work when value comes from gps popup
        // cause is a different use case
        PointCRUDActivityValidator validator =
                 new PointCRUDActivityValidator(tVPointCode,
                         point,
                         this.getApplicationContext(),
                         PREEXISTENT_POINT_RADIUS_OF_LOC);
     */
        // ISSUE - 003
        // 20190820 possible improvement VALIDATE GPS LOCATION
        //if(!validator.validatePointCode()){
        //    inputErrorAlert(msg + validator.getErrorMsg());
        //   // inputErrorAlert(msg + getString(R.string.point_crud_act_cod_punto));
        //    return false;
        //}


        // ---------------------------------------------------------------------------------//
        // VALIDATION -90 +90:
        // The valid range of latitude in degrees is -90 and +90 for the southern and northern
        // hemisphere respectively. Longitude is in the range -180 and +180 specifying coordinates
        // west and east of the Prime Meridian, respectively.
        // For reference, the Equator has a latitude of 0°, the North pole has a latitude of
        // 90° north (written 90° N or +90°), and the South pole has a latitude of -90°.
        // ---------------------------------------------------------------------------------//

        // VALIDATE LATITUDE
        if (tVGPSLat.getText().length() > 0) {
            double latitude = Double.valueOf(tVGPSLat.getText().toString());
            if (latitude > 90 || latitude < -90) {
                pCRUDActivity.inputErrorAlert(pCRUDActivity.getString(R.string.point_crud_act_lat_limit));
                return false;
            } else if (latitude == 0) {
                pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_latitude));
                //inputErrorAlert(getString(R.string.point_crud_act_lat_zero));
                return false;
            } else {
                point.setLatitude(latitude);
            }
            
        } else {
            pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_latitude));
            return false;
        }
        // VALIDATE LONGITUDE
        if (tVGPSLong.getText().length() > 0) {
            double longitude = Double.valueOf(tVGPSLong.getText().toString());
            if (longitude > 180 || longitude < -180) {
                pCRUDActivity.inputErrorAlert(pCRUDActivity.getString(R.string.point_crud_act_lon_limit));
                return false;
            } else if (longitude == 0) {
                pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_longitude));
                //inputErrorAlert(getString(R.string.point_crud_act_lon_zero));
                return false;
            } else {
                point.setLongitude(longitude);
            }
        } else {
            pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_longitude));
            return false;
        }
        // VALIDATE ALTITUDE
        if (tVGPSAlt.getText().length() > 0) {
            double altitude = Double.valueOf(tVGPSAlt.getText().toString());
            point.setHeight(altitude);

        } else {
            pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_in_alt));
            return false;
        }
        // VALIDATE OFFSET
        if (tVOffset.getText().length() > 0) {
            point.setOffset(Double.valueOf(tVOffset.getText().toString()));
        } else {
            pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_in_offset));
            return false;
        }
        // DATE
        point.setDate(tVUTCTime.getText().toString());
        // USER
        db = GravityMobileDBHelper.getInstance(pCRUDActivity.getApplicationContext(),false);
        User u = db.getUserByName(tVUserCode.getText().toString());
        point.setUserId(u.getmId());

        // This is a predefined icon to indicate ImageView is empty
        Drawable defaultImageDrawable = pCRUDActivity.getDrawable(R.drawable.ic_home_black_24dp);
        // VALIDATE IMAGES
        String backgroundImageName1 = String.valueOf(iVMeasureG1.getTag());
        String backgroundImageName2 = String.valueOf(iVMeasureG2.getTag());
        String backgroundImageName3 = String.valueOf(iVMeasureG3.getTag());

        if (backgroundImageName1.equals("img1")
                || backgroundImageName2.equals("img2")
                || backgroundImageName3.equals("img3")) {

            pCRUDActivity.inputErrorAlert(pCRUDActivity.getString(R.string.point_crud_act_photos));
            return false;
        }

        // VALIDATE G1, G2, G3
        if (tVMeasureG1.getText().length() > 0) {
            point.setG1(Double.valueOf(tVMeasureG1.getText().toString()));
        } else {
            pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_g1));
            return false;
        }

        if (tVMeasureG2.getText().length() > 0) {
            point.setG2(Double.valueOf(tVMeasureG2.getText().toString()));
        } else {
            pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_g2));
            return false;
        }

        if (tVMeasureG3.getText().length() > 0) {
            point.setG3(Double.valueOf(tVMeasureG3.getText().toString()));
        } else {
            pCRUDActivity.inputErrorAlert(msg + pCRUDActivity.getString(R.string.point_crud_act_g3));
            return false;
        }

        return true;
    }// END VALIDATIONS


    /**
     *
     * @param pCRUDActivity
     * @param tVPointCode
     * @return
     */
    @SuppressLint("NewApi")
    public static boolean isPointCodeInHistoricData(
            PointCRUDActivity pCRUDActivity,
            TextView tVPointCode) {

        // VALIDATION: POINT COD. not empty
        if (tVPointCode.getText().length() > 0) {

            // VALIDATE IF IT'S A PREEXISTENT POINT
            GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(pCRUDActivity.getApplicationContext(),false);

            // POINT_TABLE.LINE_ID=0 ARE preexistent points on DB
            Point preexistentPointAux = db.getPointByCodeAndLineId(tVPointCode.getText().toString(),
                    0);

            // Point CODE is preexistent
            if (tVPointCode.getText().toString().equals(preexistentPointAux.getCode())) {
                pCRUDActivity.inputErrorAlert(pCRUDActivity.getString(R.string.point_crud_act_gps_disabled));
                return true;
            }
        }

        return false;
    }


    /**
     *
     * @param pCRUDActivity
     * @param tVPointCode
     * @return
     */
    @SuppressLint("NewApi")
    public static boolean isPointCodeInHistoricDataNoAlert(
            PointCRUDActivity pCRUDActivity,
            TextView tVPointCode) {

        // VALIDATION: POINT COD. not empty
        if (tVPointCode.getText().length() > 0) {

            // VALIDATE IF IT'S A PREEXISTENT POINT
            GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(pCRUDActivity.getApplicationContext(),false);

            // POINT_TABLE.LINE_ID=0 ARE preexistent points on DB
            Point preexistentPointAux = db.getPointByCodeAndLineId(tVPointCode.getText().toString(),
                    0);

            // Point CODE is preexistent
            if (tVPointCode.getText().toString().equals(preexistentPointAux.getCode())) {
                return true;
            }
        }

        return false;
    }



}// END-CLASS


