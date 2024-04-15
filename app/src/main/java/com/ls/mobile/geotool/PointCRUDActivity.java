package com.ls.mobile.geotool;

import com.ls.mobile.geotool.R;



import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ls.mobile.geotool.common.LSAndroidFileCRUDFacade;
import com.ls.mobile.geotool.db.DBTools;
import com.ls.mobile.geotool.db.GravityMobileDBHelper;
import com.ls.mobile.geotool.db.GravityMobileDBInterface;
import com.ls.mobile.geotool.db.data.model.Calibration;
import com.ls.mobile.geotool.db.data.model.Line;
import com.ls.mobile.geotool.db.data.model.Point;
import com.ls.mobile.geotool.db.data.model.User;
import com.ls.mobile.geotool.gravity.CssObservation;
import com.ls.mobile.geotool.validator.PointCRUDActivityValidator;

import com.ls.mobile.geotool.view.GPSSearchDialog;
import com.ls.mobile.geotool.view.PreexistentPointsListViewBuilder;
import com.ls.mobile.geotool.workflow.LineStatusInterface;
import com.ls.mobile.geotool.workflow.PointStatusInterface;
import com.ls.mobile.geotool.workflow.PointWorkflowResolver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains the methods and logic neccesary to perform CRUD
 * operations over POINTS, the UI apertaining to this Activity is the
 * Point modification UI, where the user operates over a single POINT.
 * Furthermore this class has the GPS logic wich is acceced throught a
 * button placed at the bottom of the named UI.
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class PointCRUDActivity extends AppCompatActivity {

    // Text
    TextView tVPointCode;
    TextView tVGPSLat;
    TextView tVGPSLong;
    TextView tVOffset;
    TextView tVGPSAlt;
    TextView tVUTCTime;
    TextView tVGravimeter;
    TextView tVUserCode;
    TextView tVMeasureG1;
    TextView tVMeasureG2;
    TextView tVMeasureG3;
    TextView tVMeasureAux;

    // Otras preguntas y comentarios::
    // 1-) Los campos offset, absolute_g, uncertainty los agrego en la tabla
    // de la base de datos que tiene la aplicacion mobile???
    // RESP:Offset si, porque eso se carga en el campo para identificar el offset
    // entre la medición de gravedad y la marca GPS precisa. Absolute_g y
    // uncertainty no hacen falta.
    //
    // TextView tVOffset;

    // Buttons
    Button btnPtCode;
    Button btnSave;
    Button btnSearch;
    Button btnCancel;
    Button btnGPSLat;
    Button btnGPSLong;
    Button btnH;
    Button btnOffset;

    // Image Button
    ImageView iVMeasureG1;
    ImageView iVMeasureG2;
    ImageView iVMeasureG3;
    ImageView imageViewAux;

    // Image Temp
    //ImageView iVTemp1;
    //ImageView iVTemp2;
    //ImageView iVTemp3;

    // Point to show
    private Point point;

    // PHOTOS FLAGS
    private static final String G1_CHECK = "G1taken";
    private static final String G2_CHECK = "G2taken";
    private static final String G3_CHECK = "G3taken";

    private String OPERATION_TYPE;
    private int intentLineId;

    // Log
    private static final String LOG_TAG = PointCRUDActivity.class.getSimpleName();

    // TMP IMAGES NAMES
    private static final String PIC1_TMP = "PIC1_TMP";
    private static final String PIC2_TMP = "PIC2_TMP";
    private static final String PIC3_TMP = "PIC3_TMP";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_crud);

        // Delete temp photos directory
        deleteTempPhotos();

        // Text
        tVPointCode = findViewById(R.id.tVPointCode);
        tVPointCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // code to execute when EditText loses focus
                    String pCode = tVPointCode.getText().toString();
                    if (!"".equals(pCode)) {
                        if (tVPointCode.getText().length() == 4) {
                            Log.i(LOG_TAG, "FOCUS CHANGE");

                        }

                    }
                }
            }
        });

        tVGPSLat = findViewById(R.id.tVGPSLat);
        tVGPSLong = findViewById(R.id.tVGPSLong);
        tVUTCTime = findViewById(R.id.tVUTCTime);
        tVGPSAlt = findViewById(R.id.tVGPSAlt);
        tVOffset = findViewById(R.id.tVOffset);
        tVGravimeter = findViewById(R.id.tVGravimeter);
        tVUserCode = findViewById(R.id.tVUserCode);
        tVMeasureG1 = findViewById(R.id.tVMeasureG1);
        tVMeasureG2 = findViewById(R.id.tVMeasureG2);
        tVMeasureG3 = findViewById(R.id.tVMeasureG3);

        // Buttons
        btnPtCode = findViewById(R.id.btnPtCode);
        btnPtCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPointCodeInflaterView();
            }
        });
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePoint();
            }
        });
        btnSearch = findViewById(R.id.btnSearch); // MOD-000 "BUSCAR EN CAMPO" changes to "VALIDAR UBICACION"
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!PointCRUDActivityValidator.isPointCodeInHistoricData(
                        getPointCRUDActivityThis(),tVPointCode)) {
                    search();
                }

            }
        });
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete temp photos directory
                deleteTempPhotos();
                goToLineDataActivity();
            }
        });
        btnGPSLat = findViewById(R.id.btnGPSLat);
        btnGPSLat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLatitude();
            }
        });
        btnGPSLong = findViewById(R.id.btnGPSLong);
        btnGPSLong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLongitude();
            }
        });

        btnH = findViewById(R.id.btnH);
        btnH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editAltitude();
            }
        });

        btnOffset = findViewById(R.id.btnOffset);
        btnOffset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOffset();
            }
        });

        // IMAGE BUTTONS
        iVMeasureG1 = (ImageView) findViewById(R.id.iVMeasureG1);
        iVMeasureG1.setTag("img1"); // Tag to univocally identify the empty imageView
        iVMeasureG1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ViewAux holds the value used by the function onActivityResult( called when
                // the camara is shoting. onActivityResult() calls to the alert asking for G(n) value
                setImageViewAux(iVMeasureG1);
                settVMeasureAux(tVMeasureG1);
                takePhoto(PIC1_TMP);

                //noinspection StatementWithEmptyBody
                if (OPERATION_TYPE.equals(IntentDataKeyInterface.LINE_DATA_ACTIVITY_UPDATE)) {
                    // UPDATE
                    // 1A.000 SET TIME OF OBSERVATION
                    tVUTCTime.setText(DBTools.getUTCTime());
                    getPoint().setDate(tVUTCTime.getText().toString());
                } else {
                    // NEW POINT
                    // 1A.000 SET TIME OF OBSERVATION
                    tVUTCTime.setText(DBTools.getUTCTime());
                    getPoint().setDate(tVUTCTime.getText().toString());
                }
            }
        });
        iVMeasureG2 = (ImageView) findViewById(R.id.iVMeasureG2);
        iVMeasureG2.setTag("img2"); // Tag to univocally identify the empty imageView
        iVMeasureG2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ViewAux holds the value used by the function onActivityResult( called when
                // the camara is shoting. onActivityResult() calls to the alert asking for G(n) value
                setImageViewAux(iVMeasureG2);
                settVMeasureAux(tVMeasureG2);
                takePhoto(PIC2_TMP);
            }
        });
        iVMeasureG3 = (ImageView) findViewById(R.id.iVMeasureG3);
        iVMeasureG3.setTag("img3"); // Tag to univocally identify the empty imageView
        iVMeasureG3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ViewAux holds the value used by the function onActivityResult( called when
                // the camara is shoting. onActivityResult() calls to the alert asking for G(n) value
                setImageViewAux(iVMeasureG3);
                settVMeasureAux(tVMeasureG3);
                takePhoto(PIC3_TMP);
            }
        });

        // Values from Session.
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String sessionUserName = pref.getString(SharedDataKeyInterface.SETUP_ACTIVITY_USER_NAME_STR, null);
        String sessionGravimeterName = pref.getString(SharedDataKeyInterface.SETUP_ACTIVITY_GRAVIMETER_NAME_STR, null);

        // Set GRAVIMETER_NAME on the UI
        tVGravimeter.setText(sessionGravimeterName);
        tVUserCode.setText(sessionUserName);

        // Type of operation (CREATE or UPDATE)
        OPERATION_TYPE = getIntent().getStringExtra(IntentDataKeyInterface.LINE_DATA_ACTIVITY_TYPE_OPERATION);
        intentLineId = getIntent().getIntExtra(IntentDataKeyInterface.LINE_DATA_ACTIVITY_LINE_ID_INT, 0);

        if (OPERATION_TYPE.equals(IntentDataKeyInterface.LINE_DATA_ACTIVITY_UPDATE)) {
            int intentPointId = getIntent().getIntExtra(IntentDataKeyInterface.LINE_DATA_ACTIVITY_POINT_ID_INT, 0);
            String intentPointCode = getIntent().getStringExtra(IntentDataKeyInterface.LINE_DATA_ACTIVITY_POINT_CODE_STR);
            int intentOneWayValue = getIntent().getIntExtra(IntentDataKeyInterface.LINE_DATA_ACT_POINT_ONEWAY_VAL_INT, 8);
            // UPDATE
            initUpdatePointOperation( intentPointId, // ISSUE-001
                                    intentPointCode,
                                       intentLineId,
                                   intentOneWayValue );

        } else {
            //CREATE
            initCreatePointOperation(intentLineId);
        }
    }

    /**
     * IN::INITIALIZE POINT FOR CREATION :
     * <p>
     * GPS LOCATION:
     * Ref.: https://developer.android.com/guide/topics/media/camera
     * Location Permission - If your application tags images with GPS location information,
     * you must request the ACCESS_FINE_LOCATION permission. Note that, if your app targets
     * Android 5.0 (API level 21) or higher, you also need to declare that your app uses
     * the device's GPS:
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     * <p>
     * // Needed only if your app targets Android 5.0 (API level 21) or higher.
     * <p>
     * <uses-feature android:name="android.hardware.location.gps" />
     * <uses-permission android:name="android.permission.INTERNET" />
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     */
    private void initCreatePointOperation(int lineId) {
        // Is not neccesary to VALIDATE IF CODE ENTERED EXISTED PREVIOUSLY
        // El punto podria existir, pero en una locacion distinta,
        // "Usted esta intentado crear un punto pre-existente pero en una ubicacion
        //  que excede los limites de  permitida para dicho punto"
        //  GravityMobileDBHelper db = new GravityMobileDBHelper(getApplicationContext());
        //  String pointCode = tVPointCode.getText().toString();
        //  db.getPointByCodeAndLineId(pointCode,intentLineId);

        // SET POINT DB OBJECT DATA.
        point = new Point();

        // Set STATUS to REGISTERED for the object to persist
        PointWorkflowResolver stat = new PointWorkflowResolver();
        getPoint().setStatus(stat.getNewEntityWrkflwStat());

        // Time will be set after, see comment:: // 1A.000 SET TIME OF OBSERVATION
        // Is not possible to create  RETURN POINTS by hand, just the system can.
        getPoint().setOneWayValue(PointStatusInterface.POINT_ONEWAYVALUE_FORWARD);
        getPoint().setLineId(lineId);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        int sessionUserId = pref.getInt(SharedDataKeyInterface.SETUP_ACTIVITY_USER_ID_INT, 0);
        getPoint().setUserId(sessionUserId);

    }
    /********************************************************************************
     * OUT::INITIALIZE POINT FOR CREATION :
     ********************************************************************************/


    /********************************************************************************
     * IN::INITIALIZE POINT FOR UPDATE
     ********************************************************************************/
    private void initUpdatePointOperation(int pointId, // ISSUE-001
                                          String pointCode,
                                          int lineId,
                                          int oneWayValue) {

        // Check if line is in RETURNED state,
        // in affirmative case apply business rules
        isReturnedLineCheckingAndUIBlocking(lineId);

        // Get Point Data from DB
        // ISSUE-001
        // point = loadPoint(pointCode, lineId, oneWayValue);
        point = loadPointById(pointId);

        // Set STATUS
        PointWorkflowResolver stat = new PointWorkflowResolver();
        getPoint().setStatus(stat.getWrkflwStatusAfterUpdate(point.getStatus()));

        // Set View with data from DB to modify
        tVPointCode.setText(point.getCode());
        tVGPSLat.setText(String.valueOf(point.getLatitude()));
        tVGPSLong.setText(String.valueOf(point.getLongitude()));

        tVUTCTime.setText(String.valueOf(point.getDate()));
        tVMeasureG1.setText(String.valueOf(point.getG1()));
        tVMeasureG2.setText(String.valueOf(point.getG2()));
        tVMeasureG3.setText(String.valueOf(point.getG3()));
        tVGPSAlt.setText(String.valueOf(point.getHeight()));
        tVOffset.setText(String.valueOf(point.getOffset()));
        /** Business Rule: GRAVIMETER IS NOT UPDATEABLE */
        //tVGravimeter.setText(String.valueOf(point.get));
        /** Business Rule: USER IS NOT UPDATEABLE */
        //tVUserCode.setText(String.valueOf(point.()));

        // GET PHOTOS MINIATURES
        try {
            iVMeasureG1.setImageBitmap(
                    DBTools.getMiniBitmapForImageView(point.getG1Photo()));
        } catch (Exception e) {
            Log.w(LOG_TAG, "ERROR:", e.fillInStackTrace());
        }
        try {
            iVMeasureG2.setImageBitmap(
                    DBTools.getMiniBitmapForImageView(point.getG2Photo()));
        } catch (Exception e) {
            Log.w(LOG_TAG, "ERROR:", e.fillInStackTrace());
        }
        try {
            iVMeasureG3.setImageBitmap(
                    DBTools.getMiniBitmapForImageView(point.getG3Photo()));
        } catch (Exception e) {
            Log.w(LOG_TAG, "ERROR:", e.fillInStackTrace());
        }

       // IMPROVEMENT-6:: HISTORIC POINT CHECKS
       if(PointCRUDActivityValidator.isPointCodeInHistoricDataNoAlert(getPointCRUDActivityThis(),
               tVPointCode)) {
           // Hide Buttons
           btnPtCode.setVisibility(View.GONE);
           btnGPSLong.setVisibility(View.GONE);
           btnGPSLat.setVisibility(View.GONE);
           btnH.setVisibility(View.GONE);
       }


    }
    /**
     * OUT::INITIALIZE POINT FOR UPDATE :
     */

    /*****************************************************************
     *
     *  HELPERS :: IN
     *
     ****************************************************************/


    /**
     * Line is in RETURNED state::
     * RULE: Fwd Points of line in RETURNED state cant be edited
     * RULE: Location and code of Ret Points of line in RETURNED state can't be edited
     * <p>
     * Line is in CLOSED state:: Edition is disabled
     *
     * @param lineId
     */
    private void isReturnedLineCheckingAndUIBlocking(int lineId) {
        // InvalidateEditPointCodeBtn if Line is in RETURNED status
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        Line l = db.getLineByLineId(lineId);
        if (l.getStatus().equals(LineStatusInterface.LINE_STATUS_RETURNED)) {
            // Hide Point Code edit Button
            btnPtCode.setVisibility(View.GONE);
            btnGPSLat.setVisibility(View.GONE);
            btnGPSLong.setVisibility(View.GONE);
            btnH.setVisibility(View.GONE);
            // Hide GPS search button
            // ISSUE-003 btnSearch.setVisibility(View.GONE);

        } else if (l.getStatus().equals(LineStatusInterface.LINE_STATUS_CLOSED)) {
            // Hide Point Code edit Button
            btnPtCode.setVisibility(View.GONE);
            btnGPSLat.setVisibility(View.GONE);
            btnGPSLong.setVisibility(View.GONE);
            btnH.setVisibility(View.GONE);
            btnOffset.setVisibility(View.GONE);
            // Hide GPS search button
            btnSearch.setVisibility(View.GONE);

            btnSave.setVisibility(View.GONE);
        }
    }

    /**
     * Load data from SQLite database
     *
     * @param pointCode
     * @param lineId
     * @param oneWayValue
     * @return
     *
     * @deprecated use instead: loadPointById(int pointId)
     */
    private Point loadPoint(String pointCode, int lineId, int oneWayValue) {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        return db.getPointByCodeAndLineIdAndOneWayVal(pointCode, lineId, oneWayValue);
    }

    /**
     * ISSUE-001
     * Load data from SQLite database by POINT ID
     *
     * @param pointId
     * @return
     */
    private Point loadPointById(int pointId) {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        return db.getPointById(pointId);
    }

    public PointCRUDActivity getPointCRUDActivityThis(){
        return this;
    }





    /**
     * SAVE BUTTON
     */
    private void savePoint() {
        Log.i(LOG_TAG, "Click!, SAVE POINT!");

        //https://developer.android.com/guide/topics/ui/dialogs
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(R.string.point_crud_act_ask_save);
        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (OPERATION_TYPE.equals(IntentDataKeyInterface.LINE_DATA_ACTIVITY_UPDATE)) {

                    /*******************
                     * UPDATE POINT
                     *******************/
                    if (validateFormInputDataForUpdate()) {

                        // g_reading is the mean of the observations G1,G2,G3 over the POINT
                        getPoint().setReading(getGReading(getPoint()));

                        //SET REDUCED G
                        getPoint().setReducedG(getReducedGCalculationResult());

                        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                        int res = db.updatePoint(getPoint());
                        if (res != -1) {
                            // ISSUE - 004
                            afterSaveAlert(getString(R.string.point_crud_act_up_pnt) + " " +
                                    String.valueOf(res) + " " +
                                    getString(R.string.point_crud_act_reg),
                                    getPoint().getOneWayValue());

                            /*
                            Toast toast = Toast.makeText(PointCRUDActivity.this, "Se ha actualizado "
                                    + res + "registro", Toast.LENGTH_LONG);
                            toast.show();
                            */
                        }
                    }

                } else {

                    /*******************
                     * CREATE NEW POINT
                     */
                    if (PointCRUDActivityValidator.validateFormInputDataForCreate(
                            getPointCRUDActivityThis(),
                            getPoint(),
                            tVPointCode,
                            tVGPSLat,
                            tVGPSLong,
                            tVGPSAlt,
                            tVOffset,
                            tVUTCTime,
                            tVUserCode,
                            iVMeasureG1,
                            iVMeasureG2,
                            iVMeasureG3,
                            tVMeasureG1,
                            tVMeasureG2,
                            tVMeasureG3)) {

                        // g_reading is the mean of the observations G1,G2,G3 over the POINT
                        getPoint().setReading(getGReading(getPoint()));

                        //SET REDUCED G
                        getPoint().setReducedG(getReducedGCalculationResult());

                        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                        // IMPROVEMENT-5:: DB
                        long res = -1;
                        try {
                            res = db.createPoint(getPoint());
                        }catch(Exception e){
                            Log.d(LOG_TAG, "ERROR:", e.fillInStackTrace());
                        }finally{
                            try {
                                db.close();
                            }catch(Exception e1){
                                Log.d(LOG_TAG, "ERROR:", e1.fillInStackTrace());
                            }
                        }

                        if (res != -1) {
                            // ISSUE - 004
                            afterSaveAlert(getString(R.string.point_crud_act_new_pnt) +
                                    String.valueOf(res),getPoint().getOneWayValue());
                        }


                    }
                }
            }//END:onClick()
        });
        alertBuilder.setNegativeButton(R.string.cancel, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    /**
     * REDUCED_G
     * Call to reduced_g calculation function
     */
    private double getReducedGCalculationResult() {
        CssObservation cssObservation = new CssObservation();

        // GRAVIMETER_ID Values from Session.
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        int sessionGravId = pref.getInt(SharedDataKeyInterface.SETUP_ACTIVITY_GRAVIMETER_ID_INT, 0);

        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        List<Calibration> calibration = db.getCalibrationByGravimeterId(sessionGravId);
        db.finalizeInstance();
        double reducedG = cssObservation.computeReducedG(calibration, getPoint());

        return reducedG;
    }

    /**
     * G_READING
     * g_reading is the mean of the observations G1,G2,G3 over the POINT
     */
    private double getGReading(Point p) {
        double g_reading = (point.getG1() + point.getG2() + point.getG3()) / 3;
        return g_reading;
    }

    /**
     * VALIDATE INPUT DATA for UPDATE
     *
     * @return true if valid
     */
    private boolean validateFormInputDataForUpdate() {
        String msg = getString(R.string.point_crud_act_input_please);
        // VALIDATION: POINT COD. not empty
        if (tVPointCode.getText().length() > 0) {
            point.setCode(tVPointCode.getText().toString());
        } else {
            inputErrorAlert(msg + getString(R.string.point_crud_act_cod_punto));
            return false;
        }

/*
        // ISSUE-003 RELATED THE NEXT COULD BE AN IMPROVEMENT::

        // VALIDATE POINT CODE AND COORDS
        // NOT NECCESSARY!!!! VALIDATE FORWARD POINT(not neccessary, cause creating POINT manually means)
        // VALIDATE POINT CODE EXIST PREVIOUSLY
        // IF POINT CODE NOT EXIST, JUST USE IT
        // IF YES EXIST, VALIDATE IF POINT CODE IS IN THE SAME LOCATION AROUND 30mts
        // IF NOT AROUND 30Mts, THROW ERROR
        // IF YES AROUND 30Mts, OFFER TO USING IT OR

        GravityMobileDBHelper db = new GravityMobileDBHelper(getApplicationContext());
        Point p = db.getPointByCode(pCode);
        if(p.getCode() == null || "".equals(p.getCode()) ){
            // Do nothing: IF POINT CODE NOT EXIST, JUST USE IT
        }else{
            // IF YES EXIST, VALIDATE IF POINT CODE IS IN THE SAME LOCATION AROUND 30mts

        }

        // VALIDATOR is an improvement in case it could be useful 4 the client
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
                inputErrorAlert(getString(R.string.point_crud_act_lat_limit));
                return false;
            } else if (latitude == 0) {
                inputErrorAlert(msg + getString(R.string.point_crud_act_latitude));
                //inputErrorAlert(getString(R.string.point_crud_act_lat_zero));
                return false;
            } else {
                point.setLatitude(latitude);
            }

        } else {
            inputErrorAlert(msg + getString(R.string.point_crud_act_latitude));
            return false;
        }
        // VALIDATE LONGITUDE
        if (tVGPSLong.getText().length() > 0) {
            double longitude = Double.valueOf(tVGPSLong.getText().toString());
            if (longitude > 180 || longitude < -180) {
                inputErrorAlert(getString(R.string.point_crud_act_lon_limit));
                return false;
            } else if (longitude == 0) {
                inputErrorAlert(msg + getString(R.string.point_crud_act_longitude));
                //inputErrorAlert(getString(R.string.point_crud_act_lon_zero));
                return false;
            } else {
                point.setLongitude(longitude);
            }
        } else {
            inputErrorAlert(msg + getString(R.string.point_crud_act_longitude));
            return false;
        }
        // VALIDATE ALTITUDE
        if (tVGPSAlt.getText().length() > 0) {
            double altitude = Double.valueOf(tVGPSAlt.getText().toString());
            point.setHeight(altitude);

        } else {
            inputErrorAlert(msg + getString(R.string.point_crud_act_in_alt));
            return false;
        }
        // VALIDATE OFFSET
        if (tVOffset.getText().length() > 0) {
            point.setOffset(Double.valueOf(tVOffset.getText().toString()));
        } else {
            inputErrorAlert(msg + getString(R.string.point_crud_act_in_offset));
            return false;
        }
        // DATE
        point.setDate(tVUTCTime.getText().toString());
        // USER
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        User u = db.getUserByName(tVUserCode.getText().toString());
        point.setUserId(u.getmId());

        // This is a predefined icon to indicate ImageView is empty
        Drawable defaultImageDrawable = getDrawable(R.drawable.ic_home_black_24dp);
        // VALIDATE IMAGES
        String backgroundImageName1 = String.valueOf(iVMeasureG1.getTag());
        String backgroundImageName2 = String.valueOf(iVMeasureG2.getTag());
        String backgroundImageName3 = String.valueOf(iVMeasureG3.getTag());

        if (backgroundImageName1.equals("img1")
                || backgroundImageName2.equals("img2")
                || backgroundImageName3.equals("img3")) {

            inputErrorAlert(getString(R.string.point_crud_act_photos));
            return false;
        }

        // VALIDATE G1, G2, G3
        if (tVMeasureG1.getText().length() > 0) {
            point.setG1(Double.valueOf(tVMeasureG1.getText().toString()));
        } else {
            inputErrorAlert(msg + getString(R.string.point_crud_act_g1));
            return false;
        }

        if (tVMeasureG2.getText().length() > 0) {
            point.setG2(Double.valueOf(tVMeasureG2.getText().toString()));
        } else {
            inputErrorAlert(msg + getString(R.string.point_crud_act_g2));
            return false;
        }

        if (tVMeasureG3.getText().length() > 0) {
            point.setG3(Double.valueOf(tVMeasureG3.getText().toString()));
        } else {
            inputErrorAlert(msg + getString(R.string.point_crud_act_g3));
            return false;
        }

        return true;
    }// END VALIDATIONS



    /**
     * ERROR ALERT
     * @param errorMessage
     */
    public void inputErrorAlert(String errorMessage) {
        Log.i(LOG_TAG, "Click!, User Input Error");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(errorMessage);
        // ISSUE MEMORY-LEAKS
        alertBuilder.setPositiveButton(R.string.ok, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    /**
     *  ISSUE-004 SOLUTION: Cuando presionas volver desde la pantalla de observación, siempre
     *  vuelve a IDA aunque hayas venido desde RETORNO. Esto es medio molesto.
     *
     * @param message message to the user
     * @param oneWayValue where we go RETURN or FORWARD
     */
    private void afterSaveAlert(String message, int oneWayValue) {
        Log.i(LOG_TAG, "Click!, User Input Error");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // GO TO LINEDATAACTIVITY telling ONEWAYVALUE to process in such ACTIVITY
                goToLineDataActivityFromOneWay(oneWayValue);
            }
        });
        // ISSUE-008
        alertBuilder.setCancelable(false);
        alertBuilder.create();
        alertBuilder.show();
    }

    /***********************************************************************************
     * IN :: SEARCH BUTTON:[POPUP WINDOW]
     *
     * Search button opens search screen where mobile GPS will feed with location info
     * Here the User will select the actual GPS location.
     * if happends the case where a previous observation POINT exist in the system,
     * and such POINT is closer to actual location, User can select such location,
     **********************************************************************************/

    // UI UP SIDE
    TextView aFieldSearchLat;
    TextView aFieldSearchLong;
    TextView aFieldSearchPtCd;
    double aFieldSearchHVar;// Actual Altitude detected by GPS

    // UI BOTTOM SIDE
    TextView aFieldSearchPrevLat;
    TextView aFieldSearchPrevLon;
    TextView aFieldSearchPrevPtCd;
    ListView aFieldSearchPrevPtLst; // ISSUE 001/003
    double aFieldSearchPrevHVar;// Historic Altitude

    // Big Button at the Bottom (is used to anounce gps status)
    Button syncBtn;

    private void setGPSPModalWSyncBtn(String m) {
        if (syncBtn != null) {
            syncBtn.setText(m);
        }
    }

    // Big Button at the Bottom (small text)
    TextView txtSatelliteInfo;

    private void setGPSPModalWTxtSatInfo(String m) {
        if (txtSatelliteInfo != null) {
            txtSatelliteInfo.setText(m);
        }
    }

    /**
     * SEARCH by GPS functionality
     */
    Button aFieldSearchActualBtn;
    Button aFieldSearchOldBtn;
    Button aFieldSearchRefreshBtn; // ISSUE MEMORY-LEAKS

    /**
     * MAKE MODAL WINDOWS SELECTION BUTTONS HIDDEN
     * until GPS is active
     *
     * @param visible
     */
    private void setVisibleFieldSearchSelectionButtons(boolean visible) {
        if (visible) {
            aFieldSearchActualBtn.setVisibility(View.VISIBLE);
            aFieldSearchRefreshBtn.setVisibility(View.VISIBLE);
            // ISSUE-003 // ISSUE MEMORY-LEAKS
            setOldPointsSelBtnVisibility();
        } else {
            aFieldSearchActualBtn.setVisibility(View.GONE);
            aFieldSearchRefreshBtn.setVisibility(View.GONE);
            // ISSUE MEMORY-LEAKS
            setOldPointsSelBtnVisibility();
        }
    }
    // Hide or show the button 'selection' for a list of proxy POINTS
    private void setOldPointsSelBtnVisibility(){
        if(aFieldSearchPrevPtLst.getAdapter() != null &&
                !aFieldSearchPrevPtLst.getAdapter().isEmpty()) {//Show if textbox has DATA
            aFieldSearchOldBtn.setVisibility(View.VISIBLE);
        } else {//Hide if no data in the textbox
            // ISSUE MEMORY-LEAKS
            aFieldSearchOldBtn.setVisibility(View.GONE);
        }
    }

    /**********************************************************************************
     **********************************************************************************
     **********************************************************************************
     * IN :: SEARCH BUTTON::[POPUP WINDOW] w/ Inflater Layout
     **********************************************************************************/
    //Rows
    private PreexistentPointsListViewBuilder listViewRow;

    @Deprecated
    public void search_() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        GPSSearchDialog newFragment = new GPSSearchDialog();
        newFragment.show(fragmentManager, "dialog");

    }

    public void search() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        LayoutInflater inflater = (PointCRUDActivity.this).getLayoutInflater();
        View inflaterView = inflater.inflate(R.layout.activity_field_search, null);

        GPSSearchDialog newFragment = new GPSSearchDialog(inflaterView, this);
        newFragment.show(fragmentManager, "dialog");

    }


    /**
     *
     * @param newFragment
     * @param inflaterView
     */
    // THIS METHOD IS CALLED BY GPSSearchDialog.
    public void search(GPSSearchDialog newFragment, View inflaterView ) {
        Log.i(LOG_TAG, "Click!, SEARCH POINT");

        FragmentManager fragmentManager = getSupportFragmentManager();
        //LayoutInflater inflater = (PointCRUDActivity.this).getLayoutInflater();
        //View inflaterView = inflater.inflate(R.layout.activity_field_search, null);
        //GPSSearchDialog newFragment = new GPSSearchDialog(inflaterView, this);
        //newFragment.show(fragmentManager, "dialog");

        // MOD-000::IN User will use directly the 'Select' button
        // src: https://stackoverflow.com/questions/48185964/programmatically-accessing-alertdialog-positivebutton/48186046
        // This code is neccessary to call programmatically to the PositiveButton in the
        // modal window, or Alert Dialog

        newFragment.getAlertDialog()
                        .setTitle(R.string.app_name);

        // MOD-000::OUT User will use directly the 'Select' button
        // 1-Get the layout inflater
        // 2-Inflate and set the layout for the dialog
        // 3-Pass null as the parent view because its going in the dialog layouta
        // 4-Add action buttons

        // BIG Button WITH Dynamic TEXT
        syncBtn = inflaterView.findViewById(R.id.syncBtn);
        // SET TEXT "SEARCHING SATELLITES"
        syncBtn.setText(R.string.point_crud_act_search_sat);
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncBtn.setText(R.string.point_crud_act_wait);
            }
        });

        // Satellite Info
        txtSatelliteInfo = inflaterView.findViewById(R.id.txtSatelliteInfo);

        /**
         * UI UPPER SIDE
         */
        aFieldSearchLat = inflaterView.findViewById(R.id.aFieldSearchLat);
        aFieldSearchLong = inflaterView.findViewById(R.id.aFieldSearchLong);
        aFieldSearchPtCd = inflaterView.findViewById(R.id.aFieldSearchPtCd);
        // SET NEW POINT CODE
        aFieldSearchPtCd.setText(tVPointCode.getText());

        aFieldSearchActualBtn = inflaterView.findViewById(R.id.aFieldSearchActualBtn);
        aFieldSearchActualBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentLocation != null) {
                    // SET LOCATION DATA ON PARENT SCREEN "POINTCRUDACTIVITY"
                    tVGPSLat.setText(aFieldSearchLat.getText());
                    tVGPSLong.setText(aFieldSearchLong.getText());
                    tVGPSAlt.setText(String.valueOf(aFieldSearchHVar));// Var is not in UI
                    // SET POINT DB OBJECT DATA
                    // ISSUE MEMORY-LEAKS
                    getPoint().setLatitude(Double.valueOf(aFieldSearchLat.getText().toString()));
                    getPoint().setLongitude(Double.valueOf(aFieldSearchLong.getText().toString()));
                    getPoint().setHeight(aFieldSearchHVar);// Var is not in UI

                } else {
                    // NO GPS DATA RETRIEVED
                    tVGPSLat.setText("0");
                    tVGPSLong.setText("0");
                    tVGPSAlt.setText("0");
                    getPoint().setLongitude(0);
                    getPoint().setLatitude(0);
                    getPoint().setHeight(0);
                }
                /**
                 * STOP GPS LOCATION
                 * */
                // MOD-000:: User will use directly the 'Select' button
                // stopLocationSystem();
                // now inside button positive code
                newFragment.getAlertDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        });

        // ISSUE MEMORY-LEAKS::IN:
        // The GPS started to low perform after many alternative POINTS
        // circumscribed to some other main POINT location where saved. That was because the list
        // was updated in real time, meaning that every time the device changed it's location
        // the "closer to that location" POINT list was recalculated. The solution is just
        // calculate the list one time, and check if the user is moving far from that limit
        // of closeness to the main point, if this happens the list will be recalculated.
        aFieldSearchRefreshBtn = inflaterView.findViewById(R.id.aFieldSearchRefreshBtn);
        aFieldSearchRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the list that shows near POINTS,
                // because it's going to be re-filled.
                listViewRow.clear();
                listViewRow.buildRowList();
                if (mCurrentLocation != null) {
                    makeUseOfNewLocation();
                } else {
                    // NO GPS DATA RETRIEVED
                }
            }
        });
        // ISSUE MEMORY-LEAKS::OUT:


        /**
         * UI BOTTOM SIDE
         * ( Historic data benchmarks or preexistent POINTS )
         */
        aFieldSearchPrevLat = inflaterView.findViewById(R.id.aFieldSearchPrevLat);
        aFieldSearchPrevLon = inflaterView.findViewById(R.id.aFieldSearchPrevLon);
        aFieldSearchPrevPtCd = inflaterView.findViewById(R.id.aFieldSearchPrevPtCd);

        // ISSUE 001/003::IN  List of historic POINTS of the same name
        aFieldSearchPrevPtLst= inflaterView.findViewById(R.id.aFieldSearchPrevPtLst);

        // Set an empty Adapter
        String[] values = new String[0];
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, values);
        aFieldSearchPrevPtLst.setAdapter(adapter);

        // Rows of preexistent points.
        // listViewRow is an object to resolve the data fill of a listView
        // ISSUE MEMORY-LEAKS : CHange to use ApplicationContext
        listViewRow = new PreexistentPointsListViewBuilder(aFieldSearchPrevPtLst, adapter, getApplicationContext());

        // Listener: to react to selections in the list,
        aFieldSearchPrevPtLst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Toast.makeText(getApplicationContext(),
                        "row nbr: " + position, Toast.LENGTH_SHORT)
                        .show();
                // Get the user selection
                String item = parent.getItemAtPosition(position).toString();
                Log.i(LOG_TAG, "ITEM:" +item);

                // Get the row selected by the user in a Map.
                HashMap row = listViewRow.getRowByElementIndex(position);

                // Set User selection on UI on preexistent POINT text boxes
                aFieldSearchPrevPtCd.setText(String.valueOf(row.get(listViewRow.POINT_CODE)));
                aFieldSearchPrevLat.setText(String.valueOf(row.get(listViewRow.POINT_LATITUDE)));
                aFieldSearchPrevLon.setText(String.valueOf(row.get(listViewRow.POINT_LONGITUDE)));
                // POINT_HEIGHT Var is not in UI but is handled in background
                // ISSUE MEMORY-LEAKS
                aFieldSearchPrevHVar = Double.valueOf(String.valueOf(row.get(listViewRow.POINT_HEIGHT)));

            }
        });// ISSUE 001/003::OUT

        aFieldSearchOldBtn = inflaterView.findViewById(R.id.aFieldSearchOldBtn);

        // MAKE MODAL WINDOWS SELECTION BUTTONS HIDDEN
        setVisibleFieldSearchSelectionButtons(false);

        aFieldSearchOldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                                 // ISSUE 001/003::
                                 if (!"".equals(aFieldSearchPrevPtCd.getText())) {

                                      // SET LOCATION DATA ON PARENT SCREEN "POINTCRUDACTIVITY"
                                      tVPointCode.setText(aFieldSearchPrevPtCd.getText());
                                      tVGPSLat.setText(aFieldSearchPrevLat.getText());
                                      tVGPSLong.setText(aFieldSearchPrevLon.getText());
                                      tVGPSAlt.setText(String.valueOf(aFieldSearchPrevHVar)); // Var is not in UI

                                      // SET POINT DB OBJECT DATA
                                      // ISSUE MEMORY-LEAKS
                                      getPoint().setLatitude(Double.valueOf(aFieldSearchPrevLat.getText().toString()));
                                      getPoint().setLongitude(Double.valueOf(aFieldSearchPrevLon.getText().toString()));
                                      getPoint().setCode((String) aFieldSearchPrevPtCd.getText());
                                      getPoint().setHeight(aFieldSearchPrevHVar);// Var is not in UI

                                     /**
                                      * STOP GPS LOCATION
                                      * */
                                     // MOD-000:: User will use directly select with the 'Select' button
                                     // stopLocationSystem();
                                     // MOD-000::now this call is inside BUTTON_POSITIVE code
                                     newFragment.getAlertDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();

                                 } else {

                                     // MOD-000::
                                     Toast.makeText(getApplicationContext(),
                                             "Seleccione un valor de la lista!", Toast.LENGTH_LONG)
                                             .show();
                                     // MOD-000:: NO GPS DATA RETRIEVED

                                 }
            }
        });


        newFragment.getBuilder().setView(inflaterView);
        //newFragment.getBuilder().create();

        // The specified child already has a parent.
        // You must call removeView() on the child's parent first.
        //newFragment.getBuilder().show();

        // In case of error with the parent.
        //if(tv!= null){
        //    ((ViewGroup)tv.getParent()).removeView(tv); // <- fix


        /**
         *  START GPS LOCATION
         * */
        startLocationSystem();
    }
    /**********************************************************************************
     * OUT :: SEARCH BUTTON::[POPUP WINDOW] w/ Inflater Layout
     **********************************************************************************
     **********************************************************************************
     **********************************************************************************/

    // EDIT POINT CODE BUTTON
    private void editPointCodeInflaterView() {
        Log.d(LOG_TAG, "Click!, Edit POINT CODE");

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(R.string.point_crud_act_ask_p_code);
        // Get the layout inflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layouta
        // Add action buttons
        LayoutInflater inflater = (PointCRUDActivity.this).getLayoutInflater();
        // View
        View inflaterView = inflater.inflate(R.layout.alert_dialog_custom, null);
        alertBuilder.setView(inflaterView);

        final EditText userInput = (EditText) inflaterView.findViewById(R.id.inputData);
        userInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT);

        // MAX. CHAR LENGTH: 4
        userInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        userInput.setHint("CODIGO DEL PUNTO");

        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String uInputStr = userInput.getText().toString();
                if (!"".equals(uInputStr)) {
                    if (!(uInputStr.length() == 4)) {
                        String msg = getString(R.string.point_crud_act_4_in);
                        inputErrorAlert(msg + getString(R.string.point_crud_act_codigo));

                    } else if (uInputStr.replaceAll("[^a-z0-9]", "").length() != 4) { // "[^a-zA-Z0-9]"
                        String msg = getString(R.string.point_crud_act_just_az_09);
                        inputErrorAlert(msg + getString(R.string.point_crud_act_codigo));

                    } else { // condition to enter always
                        /**
                         * VALIDATION: POINT Code must be unique for actual line on the FWD
                         * NOTE: This validation will only be executed when RETURN operation
                         * was not executed at the time
                         */
                        // Check if RETURN was executed
                        String pointCodeAux = uInputStr.trim();
                        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                        List<Point> retPointLst = db.getPointsByLineIdAndOnwWayVal(intentLineId,
                                PointStatusInterface.POINT_ONEWAYVALUE_RETURN,
                                GravityMobileDBInterface.SQL_ASC);
                        if (retPointLst == null) {// RETURN wasn't executed

                            // Check POINT CODE entered is unique in the same line
                            db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                            Point preexistentPoint = db.getPointByCodeAndLineId(pointCodeAux, intentLineId);
                            // Check preexistence in DB of POINTS
                            db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
                            Point preexstntPointOnDB = db.getPointByCode(pointCodeAux);

                            // Preexistent POINT in same LINE
                            if (pointCodeAux.equals(preexistentPoint.getCode())) {
                                inputErrorAlert(getString(R.string.point_crud_act_point_cod_repeated));

                                //Preexistent POINT in POINT DB
                            } else if (pointCodeAux.equals(preexstntPointOnDB.getCode())) {
                                inputErrorAlert(getString(R.string.point_crud_act_pnt_dbcod_repeated));
                                tVPointCode.setText(userInput.getText());
                                tVGPSLong.setText(String.valueOf(preexstntPointOnDB.getLongitude()));
                                tVGPSLat.setText(String.valueOf(preexstntPointOnDB.getLatitude()));
                                tVGPSAlt.setText(String.valueOf(preexstntPointOnDB.getHeight()));
                                // Hide Buttons
                                btnPtCode.setVisibility(View.GONE);
                                btnGPSLong.setVisibility(View.GONE);
                                btnGPSLat.setVisibility(View.GONE);
                                btnH.setVisibility(View.GONE);

                            } else { // Normal FLUX
                                tVPointCode.setText(userInput.getText());

                            }
                        }
                        //else{
                        // IF RETURN WAS EXECUTED, POINT CODES COULDNT BE MODIFIED ANYMORE
                        // AND tVPointCode.setText(userInput.getText()); WILL NOT BE NECCESSARY
                        //}
                    }
                }
            }
        });
        // ISSUE MEMORY-LEAKS
        alertBuilder.setNegativeButton(R.string.cancel, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    private void setupFloatingLabelError() {
        final TextInputLayout floatingUsernameLabel = (TextInputLayout) findViewById(R.id.inputData_text_in_lyt);
        floatingUsernameLabel.getEditText().addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                if (text.length() > 0 && text.length() <= 4) {
                    floatingUsernameLabel.setError("Debe ingresar 4 digitos para: CODE");//getString(R.string.username_required));
                    floatingUsernameLabel.setErrorEnabled(true);
                } else {
                    floatingUsernameLabel.setErrorEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    // EDIT LAT. BUTTON
    private void editLatitude() {
        Log.d(LOG_TAG, "Click!, Edit Latitude");
        // https://developer.android.com/guide/topics/ui/dialogs
        // OJO!!! https://blog.codeonion.com/2015/10/03/android-how-to-fix-null-object-reference-error/
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle(R.string.app_name);//R.string.alert_title);
        alertBuilder.setMessage(R.string.point_crud_act_in_long);//R.string.alert_message);
        // Get the layout inflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layouta
        // Add action buttons
        LayoutInflater inflater = (PointCRUDActivity.this).getLayoutInflater();
        // View
        View inflaterView = inflater.inflate(R.layout.alert_dialog_custom, null);
        alertBuilder.setView(inflaterView);
        final EditText userInput = (EditText) inflaterView.findViewById(R.id.inputData);

        // MAX. CHAR LENGTH: 20
        userInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        userInput.setInputType( InputType.TYPE_NUMBER_FLAG_SIGNED |
                                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                InputType.TYPE_CLASS_NUMBER );
        userInput.setHint("Latitud");

        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                tVGPSLat.setText(userInput.getText());
            }
        });
        // ISSUE MEMORY-LEAKS
        alertBuilder.setNegativeButton(R.string.cancel, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    // EDIT LONG. BUTTON
    private void editLongitude() {
        Log.d(LOG_TAG, "Click!, Edit Longitude");
        // https://developer.android.com/guide/topics/ui/dialogs
        // OJO!!! https://blog.codeonion.com/2015/10/03/android-how-to-fix-null-object-reference-error/
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle("Log4Gravity");//R.string.alert_title);
        alertBuilder.setMessage("Ingrese un valor de longitud");//R.string.alert_message);
        // Get the layout inflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layouta
        // Add action buttons
        LayoutInflater inflater = (PointCRUDActivity.this).getLayoutInflater();
        // View
        View inflaterView = inflater.inflate(R.layout.alert_dialog_custom, null);
        alertBuilder.setView(inflaterView);

        final EditText userInput = (EditText) inflaterView.findViewById(R.id.inputData);
        // MAX. CHAR LENGTH: 20
        userInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        userInput.setInputType( InputType.TYPE_NUMBER_FLAG_SIGNED |
                                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                InputType.TYPE_CLASS_NUMBER );
        userInput.setHint("Longitud");

        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                tVGPSLong.setText(userInput.getText().toString());
            }
        });
        // ISSUE MEMORY-LEAKS
        alertBuilder.setNegativeButton(R.string.cancel, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    // EDIT ALTITUDE
    private void editAltitude() {
        Log.d(LOG_TAG, "Click!, Edit Altitude");

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(R.string.point_crud_act_in_alt);
        // Get the layout inflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layouta
        // Add action buttons
        LayoutInflater inflater = (PointCRUDActivity.this).getLayoutInflater();
        // View
        View inflaterView = inflater.inflate(R.layout.alert_dialog_custom, null);
        alertBuilder.setView(inflaterView);
        final EditText userInput = (EditText) inflaterView.findViewById(R.id.inputData);

        // MAX. CHAR LENGTH: 20
        userInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        userInput.setInputType( InputType.TYPE_NUMBER_FLAG_SIGNED |
                                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                InputType.TYPE_CLASS_NUMBER );
        userInput.setHint(getString(R.string.point_crud_act_altitude));

        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                tVGPSAlt.setText(userInput.getText());
            }
        });
        // ISSUE MEMORY-LEAKS
        alertBuilder.setNegativeButton(R.string.cancel, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    // EDIT OFFSET
    private void editOffset() {
        Log.d(LOG_TAG, "Click!, Edit Offset");

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(R.string.point_crud_act_in_offset);
        // Get the layout inflater1
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layouta
        // Add action buttons
        LayoutInflater inflater = (PointCRUDActivity.this).getLayoutInflater();
        // View
        View inflaterView = inflater.inflate(R.layout.alert_dialog_custom, null);
        alertBuilder.setView(inflaterView);
        final EditText userInput = (EditText) inflaterView.findViewById(R.id.inputData);

        // MAX. CHAR LENGTH: 20
        userInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        userInput.setInputType( InputType.TYPE_NUMBER_FLAG_SIGNED |
                                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                InputType.TYPE_CLASS_NUMBER );
        userInput.setHint(getString(R.string.point_crud_act_offset)); // ISSUE-013

        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                tVOffset.setText(userInput.getText());
            }
        });
        // ISSUE MEMORY-LEAKS
        alertBuilder.setNegativeButton(R.string.ok, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    // GPS DISABLED ERROR
    private void gpsDisabledAlert() {
        Log.d(LOG_TAG, "Click!, Edit Longitude");
        // https://developer.android.com/guide/topics/ui/dialogs
        // OJO!!! https://blog.codeonion.com/2015/10/03/android-how-to-fix-null-object-reference-error/
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle("Log4Gravity");//R.string.alert_title);
        alertBuilder.setMessage("GPS se encuentra desactividado, para correcto funcionamiento de la aplicacion deberia activarlo. Desea hacerlo?");//R.string.alert_message);
        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // enableLocationSettings() {
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(settingsIntent);
            }
        });
        // ISSUE MEMORY-LEAKS
        alertBuilder.setNegativeButton(R.string.cancel, new MyOnClickListener());
        alertBuilder.create();
        alertBuilder.show();
    }

    /**
     * ************************************************************************************
     * ****    *****    *****    *****    *****    *****    *****    *****    *****    ****
     * *****    *****    *****    *****    *****    *****    *****    *****    *****
     * ****    *****    *****    *****    *****    *****    *****    *****    *****    ****
     * *****    *****    *****    *****    *****    *****    *****    *****    *****
     * ************************************************************************************
     * // TAKE PHOTOS::IN
     * // SEE: https://developer.android.com/guide/topics/media/camera
     * <p>
     * Camera Permission - Your application must request permission to use a device camera.
     * <uses-permission android:name="android.permission.CAMERA" />
     * <p>
     * ---------------------------------------------------------------------------------------
     * IMPORTANT!!!:
     * ------------
     * In the next thread there are a description of an issue,
     * Basically, the camera permision must be removed because ANDROID version we are using
     * see:
     * https://stackoverflow.com/questions/35973235/
     * android-permission-denial-starting-intent-with-revoked-permission-android-perm
     * <p>
     * Remove this permission::<uses-permission android:name="android.permission.CAMERA"/>
     * ----------------------------------------------------------------------------------------
     * <p>
     * Note: If you are using the camera by invoking an existing camera app, your application
     * does not need to request this permission.
     * <p>
     * Camera Features - Your application must also declare use of camera features, for example:
     * <uses-feature android:name="android.hardware.camera" />
     * <p>
     * <uses-feature android:name="android.hardware.camera" android:required="false" />
     * <p>
     * Storage Permission - If your application saves images or videos to the device's
     * external storage (SD Card), you must also specify this in the manifest.
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * <p>
     * SEE: https://developer.android.com/guide/topics/media/camera
     * Caution: Always check for exceptions when using Camera.open(). Failing to check
     * for exceptions if the camera is in use or does not exist will cause your application to
     * be shut down by the system.
     * <p>
     * <p>
     * PHOTO ISSUE IN ANDROID:: Use density-independent pixels
     * <p>
     * See: https://medium.com/@rodrigolmti/android-get-camera-thumbnail-and-full-image-1bddfdc5347e
     * <p>
     * <p>
     * The first pitfall you must avoid is you must design
     * your UI using density-independent pixels (dp) as your unit of measurement. One dp is a
     * virtual pixel unit that's roughly equal to one pixel on a medium-density screen (160dpi;
     * the "baseline" density). Android translates this value to the appropriate number of real pixels
     * for each other density.
     * <p>
     * imagine you transfer one image 1080p or something better, to another activity by the intent,
     * probably you will receive an exception OutOfMemmoryError (One of various exceptions) because
     * android can’t handle large files like this, this is why exists the thumbnail, it is a tiny
     * bitmap which you can handle and transfer like you wish.
     * <p>
     * <p>
     * <p>
     * In a nutshell:
     * <p>
     * If size of your images are fairly small and number of images are also less, go for storing
     * directly in database, as it is simpler to manage.
     * However, if size of your images in large and also number of images is high, go for saving
     * in file system. Typically, database don't show any performance issues until 10MB for an
     * average mobile
     * <p>
     * PERMISSION WILL NEEDED:
     * <p>
     * <provider
     * android:name="android.support.v4.content.FileProvider"
     * android:authorities="com.ls.mobile.geotool"
     * android:exported="false"
     * android:grantUriPermissions="true">
     * <meta-data
     * android:name="android.support.FILE_PROVIDER_PATHS"
     * android:resource="@xml/file_paths" />
     * </provider>
     * <p>
     * AND A res/xml/file_paths.xml file...
     * <?xml version="1.0" encoding="utf-8"?>
     * <paths xmlns:android="http://schemas.android.com/apk/res/android">
     * <external-path name="my_images" path="Android/data/com.example.package.name/files/Pictures" />
     * </paths>
     */

    // Integer used to identify  response from the camera when it finishes.
    private static final int CAMERA_REQUEST = 1888;

    // NORMAL PHOTO TAKE
    // https://medium.com/@rodrigolmti/android-get-camera-thumbnail-and-full-image-1bddfdc5347e
    private void takePhoto(String name) {
        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        //this guarantee the app will not crash if the camera is unavailable.
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(name);

            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(LOG_TAG, ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.ls.mobile.geotool",
                        photoFile);

                // Once you have create an new Uri with the path of the file
                // you need to add this extra in your Intent to tell to Android
                // where he can save the picture.
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // now you just need to catch the data in onActivityResult
                //startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    /**
     * onActivityResult:: will be called by the system when user
     * confirms what a photo was taken
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Remember, if you try to catch the full-sized image android will not return
        // the image or the url with the intent, to retrieve the image you need to
        // use the file path you generate before.
        // It's not the same handling an entire resolution picture
        // compared with handling a thumbnail
        // For a big size picture we need to create a file where
        // Android will save the picture after she was taken,
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {

                // --------------- FULL IMAGE --------------- //
                // Get the full image from picture taken
                try {
                    // https://stackoverflow.com/questions/32244851
                    // /androidjava-lang-outofmemoryerror-failed-to-allocate-a-23970828-byte-allocatio
                    // OutOfMemoryError is the most common problem occured in android
                    // while especially dealing with bitmaps. This error is thrown by the
                    // Java Virtual Machine (JVM) when an object cannot be allocated due to
                    // lack of memory space and also, the garbage collector cannot free some space.
                    // You can add below entities in your manifest
                    // file android:hardwareAccelerated="false" , android:largeHeap="true"
                    // it will work for some environment's.
                    // currentPhotoPath is the path of the empty .jpg inside temp
                    File file = new File(currentPhotoPath);
                    Bitmap fullImageBitmap = MediaStore.Images.Media.getBitmap(
                            getApplicationContext().getContentResolver()
                            , Uri.fromFile(file));

                    /*ImageFileLoaderAndSaver.saveImageToCustomDirProxy(Context ctx
                            , Bitmap bitmap
                            , String imageName
                            , String imageDir)*/

                    if (fullImageBitmap != null) {
                        // -------- THUMBNAILS -------- //
                        processThumbnail(fullImageBitmap);
                    }

                } catch (IOException ex) {
                    Log.i(LOG_TAG, ex.getMessage());
                }
            }
        }
    }

    /**
     * Helper method to generate and set thumbnails on App ImageView
     * controls starting from the original picture
     *
     * @param sPhoto photo taken with the device camera
     */

    /**
     * IMPORTANT this is a good point to check or KILL OLD ACTIVITIES
     * <p> that can be remaining in memory
     * <p>
     * <p>
     * I ran into this problem when I didn't kill off my old activity when moving on to a new activity.
     * I fixed it with finish();
     * <p>
     * Intent goToMain = new Intent(this,MainActivity.class);
     * startActivity(goToMain);
     * finish();
     *
     * @param sPhoto
     */
    private void processThumbnail(Bitmap sPhoto) {
        //OLD Bitmap photo = (Bitmap) data.getExtras().get("data");
        if (sPhoto != null) {
            // Scale photo to store on database
            Bitmap photo = Bitmap.createScaledBitmap(sPhoto,
                    (sPhoto.getWidth() / 7), (sPhoto.getHeight() / 7), true);

            // Set photo in the POINT object ( in memory ),
            if (getImageViewAux().equals(iVMeasureG1)) {
                getPoint().setG1Photo(photo);
                iVMeasureG1.setTag(G1_CHECK); // Tag to flag ImageView as proccessed
            } else if (getImageViewAux().equals(iVMeasureG2)) {
                getPoint().setG2Photo(photo);
                iVMeasureG2.setTag(G2_CHECK); // Tag to flag ImageView as proccessed
            } else if (getImageViewAux().equals(iVMeasureG3)) {
                getPoint().setG3Photo(photo);
                iVMeasureG3.setTag(G3_CHECK); // Tag to flag ImageView as proccessed
            }
            // THUMBNAILS: Scale Photo to show in buttons
            Bitmap scaledPhoto = Bitmap.createScaledBitmap(photo, 72, 72, true);
            getImageViewAux().setImageBitmap(scaledPhoto);
            // INPUT GRAVITY OBSERVATION VALUE
            editGravityObservation(gettVMeasureAux());
        } else {
            inputErrorAlert(getString(R.string.point_crud_act_photo_err));
        }
    }

    /**
     * PHOTO TAKE AND STORE IN A FILE :: IN
     */
    // Note: We are using getUriForFile(Context, String, File) which returns
    // a content:// URI. For more recent apps targeting Android 7.0 (API level 24)
    // and higher, passing a file:// URI across a package boundary causes a
    // FileUriExposedException. Therefore, we now present a more generic way of
    // storing images using a FileProvider.
    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent(String imageFileName) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(imageFileName);
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.ls.mobile.geotool", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // CREATE TEMP IMAGE FILE
    private String currentPhotoPath;

    private File createImageFile(String imageFileName) throws IOException {

        // LOG4G photos temp location directory
        File tempStorageDir = LSAndroidFileCRUDFacade.getPublicStorageDir(this, null, "temp");
        // ORI:File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                tempStorageDir  /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Delete Temp photos taken with camera
     */
    private void deleteTempPhotos() {
        try {
            File f = LSAndroidFileCRUDFacade.getPublicStorageDir(this, null, "temp");
            f.delete();
        } catch (Exception ex) {
            Log.i(LOG_TAG, ex.getMessage());
        }

    }

    /**
     * PHOTO TAKE AND STORE IN A FILE :: OUT
     **********************************************************************/

    /**
     * ALERT for EDIT G1,G2 or G3
     * @param tView
     */
    private void editGravityObservation(final TextView tView) {
        Log.d(LOG_TAG, "Click!, Edit G");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(R.string.point_crud_act_obs);
        // Get the layout inflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layouta
        // Add action buttons
        LayoutInflater inflater = (PointCRUDActivity.this).getLayoutInflater();
        // View
        View inflaterView = inflater.inflate(R.layout.alert_dialog_custom, null);
        alertBuilder.setView(inflaterView);
        final EditText userInput = (EditText) inflaterView.findViewById(R.id.inputData);
        // MAX. CHAR LENGTH: 8
        userInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        userInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        userInput.setHint(R.string.point_crud_act_gravim_read);
        // Buttons
        alertBuilder.setPositiveButton(R.string.ok, new MyOnClickListener());


        // ISSUE-002
        // alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        //    public void onClick(DialogInterface dialog, int which) {// DO NOTHING
        //    }
        //});

        // ISSUE-002 Prevents accidental closing by touching outside the alert window limits
        alertBuilder.setCancelable(false);

        // ISSUE-002 Alerts user when input is empty
        final AlertDialog dialog = alertBuilder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("".equals(userInput.getText().toString().trim())){
                    userInput.setError("Este campo es requerido");
                    return;
                }else{
                    tView.setText(userInput.getText());
                }
                dialog.dismiss();
            }
        });
    }

    // GETTERS and SETTERS
    public TextView gettVMeasureAux() {
        return tVMeasureAux;
    }

    public void settVMeasureAux(TextView tVMeasureAux) {
        this.tVMeasureAux = tVMeasureAux;
    }

    public ImageView getImageViewAux() {
        return imageViewAux;
    }

    public void setImageViewAux(ImageView imageViewAux) {
        this.imageViewAux = imageViewAux;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    /*************************************************************************************
     * TAKE PHOTOS::OUT
     * ***********************************************************************************
     *****    *****    *****    *****    *****    *****    *****    *****    *****    ****
     *    *****    *****    *****    *****    *****    *****    *****    *****    *****
     *****    *****    *****    *****    *****    *****    *****    *****    *****    ****
     *    *****    *****    *****    *****    *****    *****    *****    *****    *****
     *************************************************************************************/

    /**
     * CANCEL BUTTON
     */
    private void goToLineDataActivity() {
        Log.d(LOG_TAG, "Click!, Going LineDataActivity");
        // ISSUE MEMORY-LEAKS
        Intent intent = new Intent(getApplicationContext(), LineDataActivity.class);
        // LINE_SELECTION_ACTIVITY is where the LINE_ID_INT was set
        intent.putExtra(IntentDataKeyInterface.LINE_SELECTION_ACTIVITY_LINE_ID_INT, intentLineId);
        // ISSUE-004
        // In case user is returning from PointCRUDActivity from a crud operation
        // over a RETURN POINT
        int oneWayValueInformedFromLDActivity = getIntent().getIntExtra(IntentDataKeyInterface.LINE_DATA_ACT_POINT_ONEWAY_VAL_INT,
                PointStatusInterface.POINT_ONEWAYVALUE_FORWARD);

        //Log.i(LOG_TAG, ">>>>>>>>>>LINE_DATA_ACT_POINT_ONEWAY_VAL_INT" + oneWayValueInformedFromLDActivity);

        intent.putExtra(IntentDataKeyInterface.POINT_CRUD_ACT_ONEWAY_VAL_INT, oneWayValueInformedFromLDActivity);

        startActivityForResult(intent, 0);
    }

    /**
     * GO TO LineDataActivity after CRUD operation
     *
     * @param oneWayValue PointStatusInterface.POINT_ONEWAYVALUE_RETURN or
     *                    PointStatusInterface.POINT_ONEWAYVALUE_FORWARD
     */
    private void goToLineDataActivityFromOneWay(int oneWayValue) {
        //Log.d(LOG_TAG, "Click!, Going LineDataActivity from oneWayValue:" + oneWayValue + " operation");
        // ISSUE MEMORY-LEAKS
        Intent intent = new Intent(getApplicationContext(), LineDataActivity.class);
        // LINE_SELECTION_ACTIVITY is where the LINE_ID_INT was set
        intent.putExtra(IntentDataKeyInterface.LINE_SELECTION_ACTIVITY_LINE_ID_INT, intentLineId);
        // ISSUE-004
        // In the case user is returning from PointCRUDActivity after a crud operation
        //  to LineDataActivity, we specify in the intent to which of the tabs
        // it must be redirected depending of the case ( if it is RETURN POINT,
        // or a FORWARD POINT)
        intent.putExtra(IntentDataKeyInterface.POINT_CRUD_ACT_ONEWAY_VAL_INT, oneWayValue);
        startActivityForResult(intent, 0);
    }

    /*******************************************************************************
     *******************************************************************************
     *******************************************************************************
     *******************************************************************************
     *******************************************************************************
     *
     * IN :: GPS LOCATION
     *
     ******************************************************************************/

    /**
     * IN::LOCATION VARIABLES
     */
    private LocationManager mLocationManager = null;
    private Location mCurrentLocation = null; // LOCATION for retrieve to CLIENTS

    // Constants
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    // To execute System alert in case GPS is deactivated
    private WindowManager manager;
    private View view;

    /*******************************************************************
     * GNSSTATUS :: IN
     *
     * Get GPS Satellites Information from the Android system
     */
    /* Android N (7.0) and above status and listeners */
    private GnssStatus mGnssStatus;
    // GNSSTATUS LISTENER gives GPS nbr of satellites, etc
    private GnssStatus.Callback mGnssStatusListener = null;
    // NOT USED
    private GnssMeasurementsEvent.Callback mGnssMeasurementsListener;
    // NOT USED
    private OnNmeaMessageListener mOnNmeaMessageListener;
    // NOT USED
    private GnssNavigationMessage.Callback mGnssNavMessageListener;
    // SATELLITES FOUNDED
    private int numberOfSatellites = 0;

    /**
     * GNSSTATUS CALL BACK
     */
    @SuppressLint("MissingPermission")
    private void registerGnssStatusListener() {
        Log.i(LOG_TAG, "registerGnssStatusCallbackCall()");
        if (mLocationManager != null) {
            mLocationManager.registerGnssStatusCallback(mGnssStatusListener);
        }
    }

    /**
     * IN :: UNREGISTER GnssStatus
     */
    private void unregisterGnssStatusListener() {
        Log.i(LOG_TAG, "unregisterGnssStatusCallback()");
        try {
            if (mLocationManager != null) {
                mLocationManager.unregisterGnssStatusCallback(mGnssStatusListener);
            }
        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }


    /**********************************************************
     * IN :: GNSSTATUS LISTENER
     **********************************************************/
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    private void addGnssStatusListener() {
        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                setGPSPModalWSyncBtn("Buscando Satellites...");
            }

            @Override
            public void onStopped() {
                setGPSPModalWSyncBtn("Pausa...");
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                setGPSPModalWSyncBtn("Satelite detectado");
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                mGnssStatus = status;
                numberOfSatellites = mGnssStatus.getSatelliteCount();
                String m = "Satelites encontrados: " + numberOfSatellites;
                setGPSPModalWTxtSatInfo(m);
                setGPSPModalWSyncBtn(m);
            }
        };
        // REGISTER LISTENER
        mLocationManager.registerGnssStatusCallback(mGnssStatusListener);
    }
    /*******************************************************************
     * OUT :: GNSSTATUS LISTENER
     *******************************************************************/

    /**
     * IN::LISTENER 4 LOCATION ::
     * Define a listener that responds to location updates
     * https://developer.android.com/guide/topics/location/strategies
     **********************************************************************/
    final LocationListener locationListener = new LocationListener() {
        // Called when a new location is found by the location provider.
        public void onLocationChanged(Location location) {
            Log.i(LOG_TAG, ">>> locationListener.onLocationChanged()");
            // Set GPS location to retrieve
            mCurrentLocation = location;
            //Log.i(LOG_TAG, ">>>locListnrACTUAL LAT.:: " + String.valueOf(location.getLatitude()));
            //Log.i(LOG_TAG, ">>>locListnrACTUAL LONG.:: " + String.valueOf(location.getLongitude()));
            updateCurrentLocationOnGPSScreen();
            if(aFieldSearchOldBtn.getVisibility() == aFieldSearchOldBtn.GONE) {
                Log.i(LOG_TAG,"aFieldSearchOldBtn.getVisibility()" + aFieldSearchOldBtn.getVisibility());
                Log.i(LOG_TAG,"aFieldSearchOldBtn.GONE" + aFieldSearchOldBtn.GONE);
                setVisibleFieldSearchSelectionButtons(true);
            }
        }

           // ISSUE MEMORY-LEAKS::IN
           /**
            * Updates Longitude and Latitude on GPS screen
            */
            private void updateCurrentLocationOnGPSScreen(){
                if (mCurrentLocation != null){
                   double actualLatitude = mCurrentLocation.getLatitude();
                   double actualLongitude = mCurrentLocation.getLongitude();
                   double altitude = mCurrentLocation.getAltitude();
                   // SET Location VALUES on SCREEN LAT/LONG
                   aFieldSearchLat.setText(String.valueOf(actualLatitude));
                   aFieldSearchLong.setText(String.valueOf(actualLongitude));
                   aFieldSearchHVar = altitude;
                }
            }// ISSUE MEMORY-LEAKS::OUT

            public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(LOG_TAG, "locationListener.onStatusChanged()");
            // ISSUE MEMORY-LEAKS:: could be a better location probably
            String msg = "";
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    msg = "GPS Fuera de servicio";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    msg = "GPS Temporalmente NO Disponible";
                    break;
                case LocationProvider.AVAILABLE:
                    msg = "GPS Disponible";
                    break;
            }
            Log.i(LOG_TAG, "EXECUTING:onStatusChanged::" + msg);

            // TOAST GPS status MSG
            Toast.makeText(PointCRUDActivity.this, msg, Toast.LENGTH_LONG).show();
        }

        public void onProviderEnabled(String provider) {
            Log.i(LOG_TAG, "EXECUTING:onProviderEnabled");
            // manager != null => system dialog was displayed
            if (manager != null) {
                manager.removeView(view);
                manager = null;
            }
        }

        public void onProviderDisabled(String provider) {
            Log.i(LOG_TAG, "EXECUTING:onProviderDisabled");
            // Show error when GPS disabled
            gpsDisabledSystemAlert();
        }
    };
    /**
     * LOCATION LISTENER::OUT
     * *********************
     **************************************************************************/

    /***************************
     *  OUT::LOCATION VARIABLES
     **************************/

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "ON START");
        // REQUEST USER PERMISSION
        requestLocationPermission();
        // Register GNSSTATUS LISTENER
        registerGnssStatusListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "ON PAUSE");
        if (mLocationManager != null) {
            stopLocationSystem();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "ON RESUME");
    }

    /************************************************************
     * IN::HELPERS
     ***********************************************************/

    /**
     * LOCATION MANAGER INITIALIZATION::
     * Acquire a reference to the system Location Manager
     * https://developer.android.com/guide/topics/location/strategies
     *
     private void initLocationManager() {
     if (mLocationManager == null) {
     Log.i(LOG_TAG, "initializeLocationManager");
     mLocationManager = (LocationManager)
     this.getSystemService(Context.LOCATION_SERVICE);
     }
     startLocationManager();
     }*/

    /**
     * START GPS LOCATION
     */


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startLocationSystem() {

        if (mLocationManager == null) {
            Log.i(LOG_TAG, "initializeLocationManager");

            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            // Add GNSSTATUS LISTENER
            addGnssStatusListener();
        }

        // GPS ENABLE/ DISABLE
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // ALERT DIALOG: requests user action to enable
            // location services, then when the user clicks the "OK" button,
            gpsDisabledSystemAlert();
        }

        try {
            // GET A CACHED LOCATION IF IT EXISTS
            mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            /**
             * RULE: We are not going to update UI with the lastKnowLocation cause
             * it could generate human error by using last coords lecture in a new
             * different POINT
             */

            // REGISTER FOR UPDATES
            // LOOPER
            //https://stackoverflow.com/questions/13398626/android-locationmanager-constructors-looper
            HandlerThread handlerThread = new HandlerThread(LOG_TAG);
            // Register LISTENER with Location Manager AND to receive location updates
            // https://developer.android.com/training/permissions/requesting
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener,
                    handlerThread.getLooper());

        } catch (java.lang.SecurityException ex) {
            Log.i(LOG_TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(LOG_TAG, "network provider does not exist, " + ex.getMessage());
        }
        /**
         * OUT:LOCATION LISTENER ACTIVATION
         */
    }

    /**
     * STOP GPS LOCATION SYSTEM
     */
    @SuppressLint("MissingPermission")
    private void stopLocationSystem() {
        if (mLocationManager != null) {
            try {
                // RESET LAST KNOW LOCATION:
                // reset last Know Location cause next POINTs will be in different locations.
                // If the GPS isn't working in the sucesive locations, lastKnowLocation
                // could be propense to user errors.
                mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).reset();

                // UNREGISTER LOCATION LISTENER (AND RESET lastKnowLocation)
                mLocationManager.removeUpdates(locationListener);
            } catch (Exception e) {
                Log.i(LOG_TAG, e.getMessage());
            }

            // UNREGISTER GnssStatus
            unregisterGnssStatusListener();

            // Nullable
            mLocationManager = null;

        }
    }

    public final void stopGPSLocation(){
        stopLocationSystem();
    }

    /**
     * SYSTEM ALERT CALL
     * <p>
     * It needs SYSTEM_ALERT_WINDOW permission.
     * Remember to add this permissin in Manifest file.
     * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
     */
    private void gpsDisabledSystemAlert() {
        Log.d(LOG_TAG, "Click!, Edit POINT CODE");

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PointCRUDActivity.this);
        alertBuilder.setTitle("Log4Gravity");//R.string.alert_title);
        alertBuilder.setMessage("No podra utilizar la Log4Gravity si no actia el GPS. \n, " +
                "Desea activarlo ahora?");//R.string.alert_message);

        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // LAUNCH ANDROID SETTINGS, ALLOWING USER TO ACTIVATE GPS ON DEVICE
                Intent androidSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(androidSettingsIntent);
            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // DO NOTHING, AND CLOSE
                finish();
            }
        });
        alertBuilder.create();
        alertBuilder.show();
    }

    /**
     * IN::GEOTOOL_GPS_PERMISSION_REQUEST
     */
    private static final int GEOTOOL_GPS_PERMISSION_REQUEST = 0;

    /**
     * Requests the {@link android.Manifest.permission#ACCESS_FINE_LOCATION}
     * permission.
     * If an additional rationale should be displayed,
     * the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                PointCRUDActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            // "permission.ACCESS_FINE_LOCATION REQUIRED" pasar a R.location"
            Snackbar.make(findViewById(R.id.main_layout), "R.permission.ACCESS_FINE_LOCATION REQUIRED",
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request the permission
                            ActivityCompat.requestPermissions(PointCRUDActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    GEOTOOL_GPS_PERMISSION_REQUEST);
                        }
                    }
            ).show();

        } else {
            Snackbar.make(findViewById(R.id.main_layout),
                    R.string.system_gps_unavailable,
                    Snackbar.LENGTH_SHORT
            ).show();
            // Request permission
            // The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(
                    PointCRUDActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    GEOTOOL_GPS_PERMISSION_REQUEST);
        }
    }
    /**
     * OUT::GEOTOOL_GPS_PERMISSION_REQUEST
     */

    /**
     * UPDATE LOCATION ON UI
     */
    private final static float PREEXISTENT_POINT_RADIUS_OF_LOC = 30.0f;

    private void makeUseOfNewLocation() {

        if (mCurrentLocation != null) {
            Log.i(LOG_TAG, ">>>>>>>>>>>MAKEUSEOFNEWLOCATION:IN:>>>>>>>>>");
            double actualLatitude = mCurrentLocation.getLatitude();
            double actualLongitude = mCurrentLocation.getLongitude();
            double altitude = mCurrentLocation.getAltitude();

            // SET Location VALUES on SCREEN LAT/LONG
            aFieldSearchLat.setText(String.valueOf(actualLatitude));
            aFieldSearchLong.setText(String.valueOf(actualLongitude));
            aFieldSearchHVar = altitude;

            // SEARCH FOR PREEXISTENT POINTS BY GPS
            // mCurrentLocation.distanceTo();
            List<Point> pointLst = getPreexistentPoints(actualLatitude, actualLongitude);
            if (pointLst != null) {
                Iterator<Point> pointLstIter = pointLst.iterator();
                //boolean isFirst=true;
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
                        //Log.i(LOG_TAG, "actualLatitude" + actualLatitude);
                        //Log.i(LOG_TAG, "actualLongitude" + actualLongitude);
                        //Log.i(LOG_TAG, "destination Latitude" + pointAux.getLatitude());
                        //Log.i(LOG_TAG, "destination Longitude" + pointAux.getLongitude());
                        //Log.i(LOG_TAG, "DISTANCE BETWEEN RESULT::" + floatResult[0]);

                        // CHECK IF PREEXISTENT POINT, IS IN A RADIUS OF 30 MTRS
                        if (floatResult[0] <= PREEXISTENT_POINT_RADIUS_OF_LOC) {
                            // ISSUE 001/003:: ADD FOUND VALUE TO A LISTVIEW OF
                            // PREEXISTENT POINTS ON GPS SEARCH UI
                            // aFieldSearchPrevLat.setText(String.valueOf(pointAux.getLatitude()));
                            // aFieldSearchPrevLon.setText(String.valueOf(pointAux.getLongitude()));
                            // aFieldSearchPrevHVar = pointAux.getHeight();
                            // aFieldSearchPrevPtCd.setText(pointAux.getCode());

                            HashMap<String,String> row = new HashMap<>();
                            row.put(listViewRow.POINT_CODE, pointAux.getCode());
                            row.put(listViewRow.POINT_LATITUDE, String.valueOf(pointAux.getLatitude()));
                            row.put(listViewRow.POINT_LONGITUDE, String.valueOf(pointAux.getLongitude()));
                            row.put(listViewRow.POINT_HEIGHT, String.valueOf(pointAux.getHeight()));

                            //Log.i(LOG_TAG, "PREEXISTENT POINT with CODE: " + pointAux.getCode() + " :FOUND");

                            // Add row to the View object.
                            listViewRow.addRow(row);
                            //break; //ISSUE 001/003
                        }

                    } catch (IllegalArgumentException ia) {
                        Log.i(LOG_TAG, ia.getMessage());
                    } catch (Exception ex) {
                        Log.i(LOG_TAG, ex.getMessage());
                    }
                }// END::while(pointLstIter)

                // ISSUE 001/003
                // Build the list
                if (listViewRow.isEmpty()) {
                    listViewRow.buildRowList();
                }
                // ISSUE MEMORY-LEAKS:
                // GPS search window:Set selection butons VISIBLE
                setVisibleFieldSearchSelectionButtons(true);
            }

        } else {
            // SET SEARCH SCREEN LAT/LONG
            aFieldSearchLat.setText("0.0");
            aFieldSearchLong.setText("0.0");
            aFieldSearchHVar = 0.0;
            Toast.makeText(this, "NO GPS DATA RECEIVED",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * CHECK FOR PREEXISTENT POINT MANUALLY (check on DB, not by GPS):
     *
     * A PREEXISTENT POINT is a point stored in the database of Log4G.
     * If the actual location of a POINT that is being analized, exists inside a radius of
     * 30 meters from the location of a PREEXISTENT POINT, that preexistent point data,
     * principally LOCATION and CODE must be used as data location of such POINT for analysis.
     */
    private void searchPreexistentPointNoGPS(double actualLatitude,
                                             double actualLongitude,
                                             double altitude) {

        // SET Location VALUES on SCREEN LAT/LONG
        aFieldSearchLat.setText(String.valueOf(actualLatitude));
        aFieldSearchLong.setText(String.valueOf(actualLongitude));
        aFieldSearchHVar = altitude;

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
                    //Log.i(LOG_TAG, "actualLatitude" + actualLatitude);
                    //Log.i(LOG_TAG, "actualLongitude" + actualLongitude);
                    //Log.i(LOG_TAG, "destination Latitude" + pointAux.getLatitude());
                    //Log.i(LOG_TAG, "destination Longitude" + pointAux.getLongitude());
                    //Log.i(LOG_TAG, "DISTANCE BETWEEN RESULT::" + floatResult[0]);

                    // SELECT THE PREEXISTENT POINT, IF ITS IN A RADIUS OF 30 MTRS
                    if (floatResult[0] <= PREEXISTENT_POINT_RADIUS_OF_LOC) {
                        // SET SEARCH SCREEN PREEXISTENT LAT/LONG FOUND
                        aFieldSearchPrevLat.setText(String.valueOf(pointAux.getLatitude()));
                        aFieldSearchPrevLon.setText(String.valueOf(pointAux.getLongitude()));
                        aFieldSearchPrevHVar = pointAux.getHeight();
                        aFieldSearchPrevPtCd.setText(pointAux.getCode());
                        // SHOW MESSAGE
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


    /*******************************************************************************
     *
     *
     * OUT :: GPS LOCATION
     *
     *
     *******************************************************************************
     *******************************************************************************
     *******************************************************************************
     *******************************************************************************
     *******************************************************************************
     *
     ******************************************************************************/


    /**
     * Get a list of preexistent POINTS (historical stored on db ) at the current location
     *
     * @param latitude
     * @param longitude
     * @return List of preexistent points
     */
    private List<Point> getPreexistentPoints(double latitude, double longitude) {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),false);
        List<Point> preexistentPoints = db.getPreexistentPoints(longitude, latitude, "ASC");
        db.finalizeInstance(); // ISSUE MEMORY-LEAKS
        return preexistentPoints;
    }

    /**
     * Validate if actual GPS Location is at the desired location
     *
     * @param actualLatitude
     * @param actualLongitude
     * @param desiredLatitude
     * @param desiredLongitude
     * @return
     *
     */
    private boolean validateUserLocationAgainstDesiredLoc(
                                           double actualLatitude,
                                           double actualLongitude,
                                           double desiredLatitude,
                                           double desiredLongitude
                                           ){
        boolean booleanResult = false;
        float[] floatResult = new float[1];

        try {
            Location.distanceBetween(actualLatitude,
                    actualLongitude,
                    desiredLatitude,
                    desiredLongitude,
                    floatResult
            );
            //Log.i(LOG_TAG, "actualLatitude" + actualLatitude);
            //Log.i(LOG_TAG, "actualLongitude" + actualLongitude);
            //Log.i(LOG_TAG, "destination Latitude" + desiredLatitude);
            //Log.i(LOG_TAG, "destination Longitude" + desiredLongitude);
            //Log.i(LOG_TAG, "DISTANCE BETWEEN RESULT::" + floatResult[0]);

            // IS 0k, IF IT IS IN A RADIUS OF 30 MTRS
            if (floatResult[0] <= PREEXISTENT_POINT_RADIUS_OF_LOC) {
                booleanResult = true;
            }
        } catch (IllegalArgumentException ia) {
            Log.i(LOG_TAG, ia.getMessage());
        } catch (Exception ex) {
            Log.i(LOG_TAG, ex.getMessage());
        }
        return booleanResult;
    }

    private static class MyOnClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // DO NOTHING
        }
    }


    // https://developer.android.com/guide/topics/ui/dialogs
//    @SuppressLint("ValidFragment")
//    public static class GPSSearchDialog____ extends DialogFragment {
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            // Use the Builder class for convenient dialog construction
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            // Get the layout inflater
//            LayoutInflater inflater = requireActivity().getLayoutInflater();
//            // Inflate and set the layout for the dialog
//            // Pass null as the parent view because its going in the dialog layout
//            builder.setView(inflater.inflate(R.layout.activity_field_search, null))
//                    // Add action buttons
//                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            // nothing
//                        }
//                    })
//                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            //LoginDialogFragment.this.getDialog().cancel();
//                        }
//                    });
//            // Full Screenate the modal screen
//            setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
//            return builder.create();
//        }
//
//        // THIS OVERRIDE IS THE SOLUTION FOR CHILD MODAL WINDOWS AT FULL SCREEN
//        @Override
//        public void onResume() {
//            super.onResume();
//            ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
//            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//            getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
//        }
//
//
//        public Dialog getGPSSearchDialog(){
//            return getDialog();
//        }
//
//    }




}// End class




