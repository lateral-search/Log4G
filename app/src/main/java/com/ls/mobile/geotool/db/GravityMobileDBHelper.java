package com.ls.mobile.geotool.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ls.mobile.geotool.db.data.model.Calibration;
import com.ls.mobile.geotool.db.data.model.Gravimeter;
import com.ls.mobile.geotool.db.data.model.Line;
import com.ls.mobile.geotool.db.data.model.Point;
import com.ls.mobile.geotool.db.data.model.Synchronization;
import com.ls.mobile.geotool.db.data.model.User;
import com.ls.mobile.geotool.workflow.LineStatusInterface;
import com.ls.mobile.geotool.workflow.PointStatusInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Reference guide:
 * https://google-developer-training.gitbooks.io/android-developer-fundamentals-course-concepts/content/en/Unit%204/101_c_sqlite_database.html
 * <p>
 * Reference guide:
 * see new ROOM implementation
 * https://developer.android.com/training/data-storage/room/
 */
public class GravityMobileDBHelper extends SQLiteOpenHelper
        implements GravityMobileDBInterface {

    private Context applicationContext;

    // has to be 1 first time or app will crash
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "gravity_db";

    // Instance variables for the references to writable and readable databases.
    // Storing these references saves lot of work of getting a database reference
    // every time is needed to read or write.
    private SQLiteDatabase mWritableDB;
    private SQLiteDatabase mReadableDB;

    private static boolean isSynchronization = false;
    // Log
    private static final String LOG_TAG = GravityMobileDBHelper.class.getSimpleName();


    // BUG WHEN USING CONSTRAINTS(sqlite issue):
    // FOREIGN KEY(CALIBRATION_ID) REFERENCES CALIBRATION(_ID)
    // OR
    // CREATE INDEX CALIBRATIONINDEX ON GRAVIMETER_TABLE(CALIBRATION_ID);

    // IN::ISSUE-PROD-0-20220301
    /**
     * Singleton
     */
    private static GravityMobileDBHelper sInstance;
    /**
     * @param context
     * @param isSynchro
     */
    public static synchronized GravityMobileDBHelper getInstance(Context context, boolean isSynchro) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
            if (sInstance == null) {
                sInstance = new GravityMobileDBHelper(context.getApplicationContext(), isSynchro);
            }
        isSynchronization = isSynchro;
        return sInstance;
    }
    // OUT::ISSUE-PROD-0-20220301

    /**
     *
     */
    public void finalizeInstance(){
        //Log.i(LOG_TAG,">>>>>>finalizeInstance()INSTANCE value" + sInstance);
        sInstance = null;
        //Log.i(LOG_TAG,">>>>>>finalizeInstance()INSTANCE value NOW" + sInstance);
        //Log.i(LOG_TAG,">>>>>>finalizeInstance()mWritableDB value" + mWritableDB);
       if (null != mWritableDB) {
           mWritableDB.close();
       }
    }

    /**
     * Normal constructor
     *
     * @param context
     * @deprecated
     */
    private GravityMobileDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Local
        applicationContext = context;
        isSynchronization = false;
    }

    /**
     * Constructor: this constructor is for a case in wich an indication about our type operation
     * is a synchronization operation. In an synchronization operation data comes from an interface
     * and many fields in a table will be empty (like photos and certain forein keys)
     * because those are not needed.
     *
     * @param context
     * @param isSynchro
     */
    protected GravityMobileDBHelper(Context context, boolean isSynchro) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Local
        applicationContext = context;
        isSynchronization = isSynchro;
    }

/*   @Override
    public synchronized void close() {
        if (sInstance != null)
            sInstance.mWritableDB.close();
            mWritableDB.close();
    }
*/


    public void createDatabase(){
        try {
            if(mWritableDB == null){
                mWritableDB = getWritableDatabase();
            }
            mWritableDB.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------------//
    //---------------------------------------------------------------------------//

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the database
        db.execSQL(USER_TABLE_CREATE);
        db.execSQL(GRAVIMETER_TABLE_CREATE);
        db.execSQL(LINE_TABLE_CREATE);
        db.execSQL(CALIBRATION_TABLE_CREATE);
        db.execSQL(POINT_TABLE_CREATE);
        db.execSQL(OBSERVATION_TABLE_CREATE); // REFACTOR::OBS
        db.execSQL(AUDIT_TABLE_CREATE);
        db.execSQL(SYNC_TABLE_CREATE);

        // ISSUE-009: TEST MUST BE COMMENTED FOR PRODUCTION
        //fillDBTestData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(GravityMobileDBHelper.class.getName(),
                "Upgrading database from version "
                        + oldVersion + " to "
                        + newVersion + ", this will destroy all old data.......");
        // DROP TABLES
        db.execSQL("DROP TABLE IF EXISTS " + AUDIT_LOG_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + POINT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + OBSERVATION_TABLE); // REFACTOR::OBS
        db.execSQL("DROP TABLE IF EXISTS " + LINE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CALIBRATION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + GRAVIMETER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SYNC_TABLE);

        onCreate(db);
    }


    /****************************************************************
     ****************************************************************
     *  QUERIES:IN
     ****************************************************************
     ****************************************************************/
    @Override
    public synchronized void close() {
        super.close();
    }

    /***********************************************************
     * USER :: IN
     */
    public User getUserById(int userId) {
        String query = "SELECT * FROM " + USER_TABLE +
                " WHERE " + USER_ID + " = " + userId;
        Cursor cursor = null;
        User entry = new User();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    entry.setmId(cursor.getInt(cursor.getColumnIndex(USER_ID)));
                    entry.setName(cursor.getString(cursor.getColumnIndex(USER_NAME)));
                } else {
                    entry = null;
                }
        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entry;
        }
    }

    /**
     * READABLE DB GET
     * @return
     */
    /*private SQLiteDatabase getReadableDB(){
        if (mReadableDB == null) {
            mReadableDB = getReadableDatabase();
        }
        return mReadableDB;
    }*/

    public List<String> getAllUserNames() {
        String query = "SELECT " + USER_NAME + " FROM " + USER_TABLE +
                " ORDER BY " + USER_NAME + " ASC ";
        Cursor cursor = null;
        List<String> userNameLst = new ArrayList<String>();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        userNameLst.add(cursor.getString(
                                cursor.getColumnIndex(USER_NAME)));
                    } while (cursor.moveToNext());
                }
        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return userNameLst;
        }
    }


    public User getUserByName(String userName) {
        String query = "SELECT * FROM " + USER_TABLE +
                " WHERE " + USER_NAME + " = '" + userName + "'";
        Cursor cursor = null;
        User entry = new User();
        try{
                cursor = getWritableDatabase().rawQuery(query, null);

                // CHECK IF IT'S IMPLEMENTED in all queries
                //try {
                // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
                // "if there's anything to look at, look at it" conditionals.
                //  if (cursor != null && cursor.moveToFirst()) {

                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    entry.setmId(cursor.getInt(cursor.getColumnIndex(USER_ID)));
                    entry.setName(cursor.getString(cursor.getColumnIndex(USER_NAME)));
                } else {
                    entry = null;
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entry;
        }
    }

    public User userQuery(int position) {
        String query = "SELECT  * FROM " + USER_TABLE +
                " ORDER BY " + USER_NAME + " ASC " +
                "LIMIT " + position + ",1";

        Cursor cursor = null;
        User entry = new User();
        try {
            //  CHECK THIS:get a readable database if it doesn't exist.
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    entry.setmId(cursor.getInt(cursor.getColumnIndex(USER_ID)));
                    entry.setName(cursor.getString(cursor.getColumnIndex(USER_NAME)));
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entry;
        }
    }


    /**
     *
     * @param userName
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long createUser(String userName) {
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        values.put(USER_NAME, userName);
        return db.insert(USER_TABLE, null, values);
    }

    /**
     * USER :: OUT
     **********************************************************/


    /***********************************************************
     * GRAVIMETER(1) :: IN
     */
    public List<Gravimeter> getAllGravimeters() {
        String query = "SELECT  * FROM " + GRAVIMETER_TABLE +
                " ORDER BY " + GRAVIMETER_NAME + " ASC ";

        Cursor cursor = null;
        List<Gravimeter> entryLst = new ArrayList<Gravimeter>();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        Gravimeter gAux = new Gravimeter();
                        gAux.setmId(cursor.getInt(cursor.getColumnIndex(GRAVIMETER_ID)));
                        gAux.setName(cursor.getString(cursor.getColumnIndex(GRAVIMETER_NAME)));
                        entryLst.add(gAux);
                    } while (cursor.moveToNext());
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entryLst;
        }
    }

    public Gravimeter getGravimetersByID(int id) {
        String query = "SELECT  * FROM " + GRAVIMETER_TABLE +
                " WHERE " + GRAVIMETER_ID + " = " + id;

        Cursor cursor = null;
        Gravimeter gravimeter = new Gravimeter();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    //do {
                    //Gravimeter gAux = new Gravimeter();
                    gravimeter.setmId(cursor.getInt(cursor.getColumnIndex(GRAVIMETER_ID)));
                    gravimeter.setName(cursor.getString(cursor.getColumnIndex(GRAVIMETER_NAME)));

                    //}while(cursor.moveToNext());
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return gravimeter;
        }
    }

    public List<String> getAllGravimetersNames() {
        String query = "SELECT " + GRAVIMETER_NAME + " FROM " + GRAVIMETER_TABLE +
                " ORDER BY " + GRAVIMETER_NAME + " ASC ";
        Cursor cursor = null;
        List<String> gravimeterNameLst = new ArrayList<String>();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        gravimeterNameLst.add(cursor.getString(
                                cursor.getColumnIndex(GRAVIMETER_NAME)));
                    } while (cursor.moveToNext());
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return gravimeterNameLst;
        }
    }

    public Gravimeter getGravimeterByName(String gravimeterName) {
        String query = "SELECT * FROM " + GRAVIMETER_TABLE +
                " WHERE " + GRAVIMETER_NAME + " = '" + gravimeterName + "'";
        Cursor cursor = null;
        Gravimeter entry = new Gravimeter();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    entry.setmId(cursor.getInt(cursor.getColumnIndex(GRAVIMETER_ID)));
                    entry.setName(cursor.getString(cursor.getColumnIndex(GRAVIMETER_NAME)));
                } else {
                    entry = null;
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entry;
        }
    }
    /**
     * GRAVIMETER(1) :: OUT
     **********************************************************/


    /***********************************************************
     * CALIBRATION :: IN
     */
    public long createCalibration(Calibration calibration) {
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        values.put(CAL_VALUE_INDEX, calibration.getCalibrationValueIndex());
        values.put(CALIBRATION_VALUE, calibration.getCalibrationValue());
        values.put(CAL_GRAVIMETER_ID, calibration.getGravimeterId());

        return db.insert(CALIBRATION_TABLE, null, values);
    }

    public List<Calibration> getCalibrationByGravimeterId(int gravimeterId) {
        String query = "SELECT  * FROM " + CALIBRATION_TABLE +
                " WHERE " + CAL_GRAVIMETER_ID + " = " + gravimeterId +
                " ORDER BY " + CAL_VALUE_INDEX + " ASC ";

        Cursor cursor = null;
        List<Calibration> entryLst = new ArrayList<Calibration>();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        Calibration cAux = new Calibration();
                        cAux.setmId(cursor.getInt(cursor.getColumnIndex(CALIBRATION_ID)));
                        cAux.setCalibrationValueIndex(cursor.getInt(cursor.getColumnIndex(CAL_VALUE_INDEX)));
                        cAux.setCalibrationValue(cursor.getDouble(cursor.getColumnIndex(CALIBRATION_VALUE)));
                        cAux.setGravimeterId(cursor.getInt(cursor.getColumnIndex(CAL_GRAVIMETER_ID)));

                        entryLst.add(cAux);
                    } while (cursor.moveToNext());
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entryLst;
        }
    }
    /**
     * CALIBRATION :: OUT
     **********************************************************/


    /**********************************************************
     * GRAVIMETER(2) :: IN
     */
    public Gravimeter gravimeterQuery(int position) {
        String query = "SELECT  * FROM " + GRAVIMETER_TABLE +
                " ORDER BY " + GRAVIMETER_NAME + " ASC " +
                "LIMIT " + position + ",1";
        Cursor cursor = null;
        Gravimeter entry = new Gravimeter();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    entry.setmId(cursor.getInt(cursor.getColumnIndex(GRAVIMETER_ID)));
                    entry.setName(cursor.getString(cursor.getColumnIndex(GRAVIMETER_NAME)));
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entry;
        }
    }

    public long createGravimeter(Gravimeter gravimeter) {
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        values.put(GRAVIMETER_NAME, gravimeter.getName());

        long res = db.insert(GRAVIMETER_TABLE, null, values);

        return res;
    }
    /**
     * GRAVIMETER(2) :: OUT
     **********************************************************/


    /**************************************************************
     * LINE :: IN
     */
    public Line getLineByLineId(int lineId) {
        String query = "SELECT  * FROM " + LINE_TABLE
                + " WHERE " + LINE_ID + " = " + lineId;
        Cursor cursor = null;
        Line entry = new Line();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    entry.setmId(cursor.getInt(cursor.getColumnIndex(LINE_ID)));
                    entry.setName(cursor.getString(cursor.getColumnIndex(LINE_NAME)));
                    entry.setDate(cursor.getString(cursor.getColumnIndex(LINE_DATE)));
                    entry.setStatus(cursor.getString(cursor.getColumnIndex(LINE_STATUS)));
                    entry.setDriftRate(cursor.getDouble(cursor.getColumnIndex(LINE_DRIFT_RATE)));
                    entry.setUserId(cursor.getInt(cursor.getColumnIndex(LINE_USER_ID)));
                    entry.setGravimeterId(cursor.getInt(cursor.getColumnIndex(LINE_GRAVIMETER_ID)));
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entry;
        }
    }


    public List<Line> getAllLinesByGravimeterName(String name) {
        String query = "SELECT  * FROM " + LINE_TABLE;
        //+ " WHERE " + LINE_TABLE + "." + LINE_NAME + "= " + name
        //+ " AND " + LINE_TABLE + "." + LINE_ID + "="
        //          + GRAVIMETER_TABLE + "." + GRAVIMETER_ID
        //+ " ORDER BY " + LINE_DATE
        //+ " ASC ";
        Cursor cursor = null;
        List<Line> entryLst = new ArrayList<Line>();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        Line lAux = new Line();
                        lAux.setmId(cursor.getInt(cursor.getColumnIndex(LINE_ID)));
                        lAux.setName(cursor.getString(cursor.getColumnIndex(LINE_NAME)));
                        lAux.setDate(cursor.getString(cursor.getColumnIndex(LINE_DATE)));
                        lAux.setStatus(cursor.getString(cursor.getColumnIndex(LINE_STATUS)));
                        lAux.setDriftRate(cursor.getDouble(cursor.getColumnIndex(LINE_DRIFT_RATE)));
                        lAux.setUserId(cursor.getInt(cursor.getColumnIndex(LINE_USER_ID)));
                        lAux.setGravimeterId(cursor.getInt(cursor.getColumnIndex(LINE_GRAVIMETER_ID)));
                        entryLst.add(lAux);
                    } while (cursor.moveToNext());
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entryLst;
        }
    }

    // LINE QUERY
    public List<Line> getAllLinesByGravimeterId(int gravimeterId) {
        String query = "SELECT  * FROM " + LINE_TABLE
                + " WHERE " + LINE_GRAVIMETER_ID + " = " + gravimeterId;
        Cursor cursor = null;
        List<Line> entryLst = new ArrayList<Line>();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        Line lAux = new Line();
                        lAux.setmId(cursor.getInt(cursor.getColumnIndex(LINE_ID)));
                        lAux.setName(cursor.getString(cursor.getColumnIndex(LINE_NAME)));
                        lAux.setDate(cursor.getString(cursor.getColumnIndex(LINE_DATE)));
                        lAux.setStatus(cursor.getString(cursor.getColumnIndex(LINE_STATUS)));
                        lAux.setDriftRate(cursor.getDouble(cursor.getColumnIndex(LINE_DRIFT_RATE)));
                        lAux.setUserId(cursor.getInt(cursor.getColumnIndex(LINE_USER_ID)));
                        lAux.setGravimeterId(cursor.getInt(cursor.getColumnIndex(LINE_GRAVIMETER_ID)));
                        entryLst.add(lAux);
                    } while (cursor.moveToNext());
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entryLst;
        }
    }

    public List<Line> getAllLines() {
        String query = "SELECT  * FROM " + LINE_TABLE;
        Cursor cursor = null;
        List<Line> entryLst = new ArrayList<Line>();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        Line lAux = new Line();
                        lAux.setmId(cursor.getInt(cursor.getColumnIndex(LINE_ID)));
                        lAux.setName(cursor.getString(cursor.getColumnIndex(LINE_NAME)));
                        lAux.setDate(cursor.getString(cursor.getColumnIndex(LINE_DATE)));
                        lAux.setStatus(cursor.getString(cursor.getColumnIndex(LINE_STATUS)));
                        lAux.setDriftRate(cursor.getDouble(cursor.getColumnIndex(LINE_DRIFT_RATE)));
                        lAux.setUserId(cursor.getInt(cursor.getColumnIndex(LINE_USER_ID)));
                        lAux.setGravimeterId(cursor.getInt(cursor.getColumnIndex(LINE_GRAVIMETER_ID)));
                        entryLst.add(lAux);
                    } while (cursor.moveToNext());
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entryLst;
        }
    }

    /**
     * @param userId
     * @param gravimeterId
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long createLine(int userId, int gravimeterId) {
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        values.put(LINE_NAME, String.valueOf("LINE-" + new Date().getTime()));
        values.put(LINE_DATE, DBTools.getTimeForTransactonDate());
        values.put(LINE_STATUS, LineStatusInterface.LINE_STATUS_INCOMPLETE);
        values.put(LINE_DRIFT_RATE, 0);
        values.put(LINE_USER_ID, userId);
        values.put(LINE_GRAVIMETER_ID, gravimeterId);

        return db.insert(LINE_TABLE, null, values);
        //  Other implementation with the next code
        //getWritableDatabase()
        //Cursor cursor = null;
        //List<Line> entryLst = new ArrayList<Line>();
        //try {
        //    if (mWritableDB == null) {
        //        mWritableDB= getWritableDatabase();
        //        cursor = mWritableDB.rawQuery(query, null);
        //        if(cursor.moveToFirst()) {
    }

    /**
     * LINE UPDATE
     *
     * @param line
     * @return
     */
    public int updateLine(Line line) {
        // LINE table data
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();

        //values.put(LINE_ID, line.getmId());
        values.put(LINE_NAME, line.getName());
        values.put(LINE_DRIFT_RATE, line.getDriftRate());
        values.put(LINE_STATUS, line.getStatus());
        values.put(LINE_DATE, line.getDate());
        // check USER_ID and gravimeter_ID

        String where = LINE_ID + " = ?";
        String[] whereArgs = {String.valueOf(line.getmId())};

        return db.update(LINE_TABLE, values, where, whereArgs);
    }


    /**
     * CLOSE LINE: close a valid line
     *
     * @param lineId
     * @return
     */
    public int closeLine(int lineId, String status) {
        // LINE table data
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        // fill daya String.valueOf(new Date().getTime())
        values.put(LINE_STATUS, status);
        values.put(LINE_DATE, DBTools.getTimeForTransactonDate());

        String where = LINE_ID + " =  ? ";
        String[] whereArgs = {String.valueOf(lineId)};
        return db.update(LINE_TABLE, values, where, whereArgs);
    }

    /**
     * RETURN LINE: return a valid line
     *
     * @param lineId
     * @return
     */
    public int returnLine(int lineId, String status) {
        // LINE table data
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        // fill daya String.valueOf(new Date().getTime())
        values.put(LINE_STATUS, status);
        values.put(LINE_DATE, DBTools.getTimeForTransactonDate());

        String where = LINE_ID + " =  ? ";
        String[] whereArgs = {String.valueOf(lineId)};
        return db.update(LINE_TABLE, values, where, whereArgs);
    }
    /**
     * LINE :: OUT
     **********************************************************/


    /***********************************************************
     * POINT :: IN
     */
    public long createPoint(Point point) {
        // LINE table data
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        // fill daya String.valueOf(new Date().getTime())
        //values.put(POINT_ID, point.getmId());
        values.put(POINT_CODE, point.getCode());
        values.put(POINT_DATE, point.getDate());
        values.put(POINT_G1, point.getG1());
        values.put(POINT_G2, point.getG2());
        values.put(POINT_G3, point.getG3());

        // ATTENTION!::If isSynchronization == true data will come from
        // synchronization, and in this case many fields like photos will
        // be not inserted
        if (isSynchronization == false) {
            values.put(POINT_G1_PHOTO, savePhoto(point.getG1Photo()));
            values.put(POINT_G2_PHOTO, savePhoto(point.getG2Photo()));
            values.put(POINT_G3_PHOTO, savePhoto(point.getG3Photo()));
        }
        values.put(POINT_LATITUDE, point.getLatitude());
        values.put(POINT_LONGITUDE, point.getLongitude());
        values.put(POINT_HEIGHT, point.getHeight());
        values.put(POINT_OFFSET, point.getOffset());
        values.put(POINT_REDUCED_G, point.getReducedG());
        values.put(POINT_READING, point.getReading());
        values.put(POINT_STATUS, point.getStatus());
        values.put(POINT_ONE_WAY_VALUE, point.getOneWayValue());
        values.put(POINT_DELTA, point.getDelta());
        values.put(POINT_RESIDUAL, point.getResidual());
        values.put(POINT_LINE_ID, point.getLineId());
        values.put(POINT_USER_ID, point.getUserId());

        long res = db.insert(POINT_TABLE, null, values);

        // IMPROVEMENT-5:: DB
        //db.close();

        return res;
        // optional code
        //getWritableDatabase()
        //Cursor cursor = null;
        //List<Line> entryLst = new ArrayList<Line>();
        //try {
        //    if (mWritableDB == null) {
        //        mWritableDB= getWritableDatabase();
        //        cursor = mWritableDB.rawQuery(query, null);
        //        if(cursor.moveToFirst()) {
    }

    // REFACTOR::OBS


    /**
     * HELPER::Save Photo to DB
     *
     * @param bitmap containing the photo data
     * @return a byte Array of the compressed bitmap byte[]
     */
    private byte[] savePhoto(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] photo = baos.toByteArray();
        return photo;
    }

    public long createPointForReverse(Point point) {
        // LINE table data
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        // fill daya String.valueOf(new Date().getTime())
        //values.put(POINT_ID, point.getmId());
        values.put(POINT_CODE, point.getCode());
        values.put(POINT_DATE, point.getDate());
        values.put(POINT_G1, point.getG1());
        values.put(POINT_G2, point.getG2());
        values.put(POINT_G3, point.getG3());
        values.put(POINT_LATITUDE, point.getLatitude());
        values.put(POINT_LONGITUDE, point.getLongitude());
        values.put(POINT_STATUS, point.getStatus());
        values.put(POINT_ONE_WAY_VALUE, point.getOneWayValue());
        values.put(POINT_DELTA, point.getDelta());
        values.put(POINT_RESIDUAL, point.getResidual());
        values.put(POINT_LINE_ID, point.getLineId());
        values.put(POINT_USER_ID, point.getUserId());

        long res = db.insert(POINT_TABLE, null, values);

        return res;
    }

    /**
     * POINT UPDATE
     *
     * @param point
     * @return
     */
    public int updatePoint(Point point) {
        // LINE table data
        SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        // fill daya String.valueOf(new Date().getTime())
        //values.put(POINT_ID, point.getmId());
        values.put(POINT_CODE, point.getCode());
        values.put(POINT_DATE, point.getDate());
        values.put(POINT_G1, point.getG1());
        values.put(POINT_G2, point.getG2());
        values.put(POINT_G3, point.getG3());

        //ALWAYS TEST THIS BY HAND: if data volume increases don't store in db
        values.put(POINT_G1_PHOTO, savePhoto(point.getG1Photo()));
        values.put(POINT_G2_PHOTO, savePhoto(point.getG2Photo()));
        values.put(POINT_G3_PHOTO, savePhoto(point.getG3Photo()));

        values.put(POINT_LATITUDE, point.getLatitude());
        values.put(POINT_LONGITUDE, point.getLongitude());
        values.put(POINT_STATUS, point.getStatus());
        values.put(POINT_ONE_WAY_VALUE, point.getOneWayValue());
        values.put(POINT_DELTA, point.getDelta());
        values.put(POINT_RESIDUAL, point.getResidual());
        values.put(POINT_LINE_ID, point.getLineId());
        values.put(POINT_USER_ID, point.getUserId());

        values.put(POINT_READING, point.getReading());
        values.put(POINT_REDUCED_G, point.getReducedG());
        values.put(POINT_HEIGHT, point.getHeight());
        values.put(POINT_OFFSET, point.getOffset());


        String where = POINT_ID + " = ?";
        String[] whereArgs = {String.valueOf(point.getmId())};

        return db.update(POINT_TABLE, values, where, whereArgs);
    }

    /**
     * Original method.
     * NOTE: it has a copy with different order criteria by POINT_ID and not by DATE, called
     *     public List<Point> getPointsByLnIdOneWayValOrderByPointId(int lineId
     *
     * @param lineId
     * @param oneWayValue
     * @param orderCriteria
     * @return
     */
    public List<Point> getPointsByLineIdAndOnwWayVal(int lineId
            , int oneWayValue
            , String orderCriteria) {
        String query = "SELECT  * FROM " + POINT_TABLE +
                " WHERE " + POINT_LINE_ID + "=" + lineId +
                " AND " + POINT_ONE_WAY_VALUE + " = " + oneWayValue +
                " ORDER BY " + POINT_DATE + " " + orderCriteria; //  ASC or DESC
        Cursor cursor = null;
        List<Point> entryLst = null;
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    entryLst = new ArrayList<Point>();
                    if (cursor.moveToFirst()) {
                        do {
                            // A new instance for every Point in the list
                            Point pAux = new Point();

                            // Build the point with a centralized method.
                            buildPoint(cursor, pAux);

                            // Add to the list
                            entryLst.add(pAux);
                        } while (cursor.moveToNext());
                    }
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entryLst;
        }
    }

    /**
     * //ISSUE-001:
     * The original getPointsByLineIdAndOnwWayVal is used in too many places
     * of the application that is neccessary to create this one to avoid any problem cause
     * the new different order criteria (by POINT_ID)
     *
     * @param lineId
     * @param oneWayValue
     * @param orderCriteria
     * @return
     */
    public List<Point> getPointsByLnIdOneWayValOrderByPointId(int lineId
            , int oneWayValue
            , String orderCriteria) {
        String query = "SELECT  * FROM " + POINT_TABLE +
                " WHERE " + POINT_LINE_ID + "=" + lineId +
                " AND " + POINT_ONE_WAY_VALUE + " = " + oneWayValue +
                " ORDER BY " + POINT_ID + " " + orderCriteria; //  ASC or DESC
        Cursor cursor = null;
        List<Point> entryLst = null;
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    entryLst = new ArrayList<Point>();
                    if (cursor.moveToFirst()) {
                        do {
                            // A new instance for every Point in the list
                            Point pAux = new Point();

                            // Build the point with a centralized method.
                            buildPoint(cursor, pAux);

                            // Add to the list
                            entryLst.add(pAux);
                        } while (cursor.moveToNext());
                    }
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entryLst;
        }
    }

    /**
     * Preexistent POINTS are BENCHMARKS which the user load to the log4g
     * <p>
     * through the excell interface.
     *
     * @param longitude
     * @param latitude
     * @param orderCriteria ASC or DESC
     * @return TEST:
     * update point set latitude = -34.62592031 where point._id > 2039
     * update point set longitude = -58.49601340 where point._id > 2039
     */
    public List<Point> getPreexistentPoints(double longitude,
                                            double latitude,
                                            String orderCriteria) {


        String query = "SELECT * FROM " + POINT_TABLE +
                " WHERE " + POINT_LATITUDE + " < " + (latitude + 1) +
                " AND " + POINT_LATITUDE + " > " + (latitude - 1) +
                " AND " + POINT_LONGITUDE + " < " + (longitude + 1) +
                " AND " + POINT_LONGITUDE + " > " + (longitude - 1) +
                /* BUSINESS RULE:
                 * ALL CLOSER PREEXISTENT POINTS MUST BE RETRIEVED BY GPS
                 * EVEN THOSE WHICH WHERE ACTUALLY CREATED FOR THIS REASON
                 * THE LINE: " AND  " + POINT_LINE_ID + " = 0 " +
                 * WILL BE NOT USED
                 */
                // ISSUE-010
                " GROUP BY "  + POINT_CODE +
                " ORDER BY " + POINT_LONGITUDE + " " + orderCriteria; //  ASC or DESC

        Cursor cursor = null;
        List<Point> entryLst = null;
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    entryLst = new ArrayList<Point>();
                    if (cursor.moveToFirst()) {
                        do {
                            // A new instance for every Point in the list
                            Point pAux = new Point();

                            // Build the point with a centralized method.
                            buildPoint(cursor, pAux);

                            // Add to the list
                            entryLst.add(pAux);
                        } while (cursor.moveToNext());
                    }
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entryLst;
        }
    }

    /**
     * Common method to build a Point
     *
     * @param cursor
     * @param pAux
     * @return
     */
    private void buildPoint(Cursor cursor, Point pAux) {
        pAux.setmId(cursor.getInt(cursor.getColumnIndex(POINT_ID)));
        pAux.setCode(cursor.getString(cursor.getColumnIndex(POINT_CODE)));
        pAux.setDate(cursor.getString(cursor.getColumnIndex(POINT_DATE)));
        pAux.setG1(cursor.getDouble(cursor.getColumnIndex(POINT_G1)));
        pAux.setG2(cursor.getDouble(cursor.getColumnIndex(POINT_G2)));
        pAux.setG3(cursor.getDouble(cursor.getColumnIndex(POINT_G3)));
        pAux.setLatitude(cursor.getDouble(cursor.getColumnIndex(POINT_LATITUDE)));
        pAux.setLongitude(cursor.getDouble(cursor.getColumnIndex(POINT_LONGITUDE)));
        pAux.setOneWayValue(cursor.getInt(cursor.getColumnIndex(POINT_ONE_WAY_VALUE)));
        pAux.setStatus(cursor.getString(cursor.getColumnIndex(POINT_STATUS)));
        pAux.setUserId(cursor.getInt(cursor.getColumnIndex(POINT_USER_ID)));
        pAux.setLineId(cursor.getInt(cursor.getColumnIndex(POINT_LINE_ID)));

        pAux.setG1Photo(getPhotoFromDB(cursor, POINT_G1_PHOTO));
        pAux.setG2Photo(getPhotoFromDB(cursor, POINT_G2_PHOTO));
        pAux.setG3Photo(getPhotoFromDB(cursor, POINT_G3_PHOTO));
        pAux.setReading(cursor.getDouble(cursor.getColumnIndex(POINT_READING)));
        pAux.setReducedG(cursor.getDouble(cursor.getColumnIndex(POINT_REDUCED_G)));
        pAux.setHeight(cursor.getDouble(cursor.getColumnIndex(POINT_HEIGHT)));
        pAux.setOffset(cursor.getDouble(cursor.getColumnIndex(POINT_OFFSET)));
        pAux.setDelta(cursor.getDouble(cursor.getColumnIndex(POINT_DELTA)));
        pAux.setResidual(cursor.getDouble(cursor.getColumnIndex(POINT_RESIDUAL)));
    }

    public List<Point> getPointsByLineId(int lineId
            , String orderCriteria) {
        String query = "SELECT  * FROM " + POINT_TABLE
                + " WHERE " + POINT_LINE_ID + "=" + lineId
                + " ORDER BY " + POINT_DATE + " "
                + orderCriteria; // " ASC " DESC
        Cursor cursor = null;
        List<Point> entryLst = new ArrayList<Point>();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        Point pAux = new Point();

                        // Build the point with a centralized method.
                        buildPoint(cursor, pAux);
                        entryLst.add(pAux);

                    } while (cursor.moveToNext());
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return entryLst;
        }
    }

    /**
     * Helper method :: get picture from DB
     */
    private Bitmap getPhotoFromDB(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);

        if (cursor.getBlob(columnIndex) != null) {
            byte[] photo = cursor.getBlob(columnIndex);
            if (photo != null) {
                ByteArrayInputStream imageStream = new ByteArrayInputStream(photo);
                Bitmap thePicture = BitmapFactory.decodeStream(imageStream);
                return thePicture;
                //contact.setPicture(thePicture);
            }
        }
        return null;
    }

    public Point getPointByCodeAndLineIdAndOneWayVal(String pointCode, int lineId, int oneWayValue) {
        String query = "SELECT  * FROM " + POINT_TABLE +
                " WHERE " + POINT_CODE + " ='" + pointCode.trim() + "'"
                + " AND " + POINT_LINE_ID + " = " + String.valueOf(lineId)
                + " AND " + POINT_ONE_WAY_VALUE + " = " + String.valueOf(oneWayValue);
        Cursor cursor = null;
        Point pAux = new Point();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();

                    // Build the point with a centralized method.
                    buildPoint(cursor, pAux);

                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return pAux;
        }
    }



    public Point getPointByCodeAndLineId(String pointCode, int lineId) {
        String query = "SELECT  * FROM " + POINT_TABLE +
                " WHERE " + POINT_CODE + " ='" + pointCode.trim() + "'"
                + " AND " + POINT_LINE_ID + " = " + String.valueOf(lineId);
        Cursor cursor = null;
        Point pAux = new Point();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();

                    // Build the point with a centralized method.
                    buildPoint(cursor, pAux);

                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return pAux;
        }
    }

    public Point getPointById(int id) {
        String query = "SELECT  * FROM " + POINT_TABLE +
                " WHERE " + POINT_ID + " = " + id;
        Cursor cursor = null;
        Point pAux = new Point();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();

                    // Build the point with a centralized method.
                    buildPoint(cursor, pAux);

                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            return pAux;
        }
    }

    public Point getPointByCode(String code) {
        if(!"".equals(code) && code != null) code.trim();

        String query = "SELECT  * FROM " + POINT_TABLE +
                " WHERE " + POINT_CODE + " = '" + code + "'";
        Cursor cursor = null;
        Point pAux = new Point();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();

                    // Build the point with a centralized method.
                    buildPoint(cursor, pAux);

                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            return pAux;
        }
    }





    /* *******************************************************
     * POINT :: OUT
     *********************************************************/


    /* *******************************************************
     * SYNCHRONIZATION :: IN
     *********************************************************/
    public long registerSyncExcecution(int status,
                                       String interfaceFileName,
                                       String fileDirName) {
        SQLiteDatabase db = getWritableDatabase();

        // Create a container for the data.
        ContentValues values = new ContentValues();
        values.put(SYNC_DATE, DBTools.getTimeForTransactonDate());
        values.put(SYNC_STATUS, status);
        values.put(SYNC_FILE_NAME, interfaceFileName);
        values.put(SYNC_DIR_NAME, fileDirName);

        return db.insert(SYNC_TABLE, null, values);
    }

    public Synchronization getLastSynchronization() {
        String query = "SELECT  * FROM " + SYNC_TABLE +
                " ORDER BY " + SYNC_DATE + " DESC LIMIT 1";
        Cursor cursor = null;
        Synchronization entry = new Synchronization();
        try {
                cursor = getWritableDatabase().rawQuery(query, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    entry.setmId(cursor.getInt(cursor.getColumnIndex(SYNC_ID)));
                    entry.setDate(cursor.getString(cursor.getColumnIndex(SYNC_DATE)));
                    entry.setStatus(cursor.getInt(cursor.getColumnIndex(SYNC_STATUS)));
                    // 0-ERROR / 1-0k
                    entry.setDate(cursor.getString(cursor.getColumnIndex(SYNC_FILE_NAME)));
                }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            return entry;
        }
    }
    /*
     * SYNCHRONIZATION :: OUT
     **********************************************************/


    /* **********************************************************
     * AUDIT :: IN
     */
    public long insertAuditLog(String logTag, String location, String auditData) {
        SQLiteDatabase db = getWritableDatabase();

            // Create a container for the data.
            ContentValues values = new ContentValues();
            values.put(AUDIT_LOGGED_DATA, auditData);
            values.put(AUDIT_LOG_TAG, logTag);
            values.put(AUDIT_LOCATION, location);
            values.put(AUDIT_DATE, DBTools.getTimeForTransactonDate());

        return db.insert(AUDIT_LOG_TABLE, null, values);
    }
    /* **********************************************************
     * AUDIT :: OUT
     */


    //------------------------------ QUERIES:OUT ---------------------------------//
    //------------------------------ QUERIES:OUT ---------------------------------//
    //------------------------------ QUERIES:OUT ---------------------------------//


    //------------------------------ IN::TEST DATA ---------------------------------//
    //------------------------------ IN::TEST DATA ---------------------------------//
    //------------------------------ IN::TEST DATA ---------------------------------//

    //
    // TEST DATA DB:IN   //
    //
    private void fillDBTestData(SQLiteDatabase db) {
        // USER table data
        String[] user = {"USER-1", "USER-2",
                "USER-3", "USER-4", "USER-5", "USER-6",
                "USER-7"};
        // Create a container for the data.
        ContentValues values = new ContentValues();
        // fill daya
        for (int i = 0; i < user.length; i++) {
            // Put column/value pairs into the container.
            // put() overrides existing values.
            values.put(USER_NAME, user[i]);
            db.insert(USER_TABLE, null, values);
        }

        // GRAVIMETER table data
        String[] gravimeter = {"GRAVIMETRO-1", "GRAVIMETRO-2",
                "GRAVIMETRO-3", "GRAVIMETRO-4", "GRAVIMETRO-5", "GRAVIMETRO-6",
                "GRAVIMETRO-7"};
        // Create a container for the data.
        values.clear();
        // fill daya
        for (int i = 0; i < gravimeter.length; i++) {
            // Put column/value pairs into the container.
            // put() overrides existing values.
            values.put(GRAVIMETER_NAME, gravimeter[i]);
            db.insert(GRAVIMETER_TABLE, null, values);
        }

        // CALIBRATION table data
        String[] calibration = {"1", "2",
                "3", "4", "5", "6",
                "7"};
        // Create a container for the data.
        values.clear();
        // fill daya
        for (int i = 0; i < calibration.length; i++) {
            // Put column/value pairs into the container.
            // put() overrides existing values.
            values.put(CALIBRATION_VALUE, calibration[i]);
            db.insert(CALIBRATION_TABLE, null, values);
        }


        // LINE table data
        String[] line = {"LINE-1", "LINE-2", "LINE-3", "LINE-4",
                "LINE-5", "LINE-6", "LINE-7"};
        // Create a container for the data.
        values.clear();
        // fill daya
        for (int i = 0; i < line.length; i++) {
            values.put(LINE_NAME, line[i]);
            values.put(LINE_DATE, "2018-06-12 11:11:11");
            if(i==1){
                values.put(LINE_STATUS, LineStatusInterface.LINE_STATUS_RETURNED);
            }else {
                values.put(LINE_STATUS, "INCOMPLETA");
            }
            values.put(LINE_USER_ID, 1);
            if (i == 1) {
                values.put(LINE_GRAVIMETER_ID, 8);
            } else {
                values.put(LINE_GRAVIMETER_ID, 1);
            }

            db.insert(LINE_TABLE, null, values);
        }


        // POINT table data
        //String[] point = {"POINT-1", "POINT-2", "POINT-3", "POINT-4",
        //        "POINT-5", "POINT-6", "POINT-7"};
        // Create a container for the data.
        values.clear();
        // fill daya
        for (int i = 1; i < 15; i++) {
            values.put(POINT_CODE, "POINT-" + i);
            values.put(POINT_DATE, "2018-06-12 11:11:11");
            values.put(POINT_G1, 123.123);
            values.put(POINT_G2, 123.123);
            values.put(POINT_G3, 123.123);
            values.put(POINT_LATITUDE, 123.123);
            values.put(POINT_LONGITUDE, 123.123);

            //TEST

            try {
                Bitmap bitmap = DBTools.readPictureFromDirectory("ic_test.png", applicationContext);
                //DBTools.rere
                values.put(POINT_G1_PHOTO, savePhoto(bitmap));
                values.put(POINT_G2_PHOTO, savePhoto(bitmap));
                values.put(POINT_G3_PHOTO, savePhoto(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (i > 7) {
                values.put(POINT_ONE_WAY_VALUE, PointStatusInterface.POINT_ONEWAYVALUE_RETURN);
            } else {
                values.put(POINT_ONE_WAY_VALUE, PointStatusInterface.POINT_ONEWAYVALUE_FORWARD);
            }
            values.put(POINT_STATUS, PointStatusInterface.POINT_STATUS_REGISTERED);
            values.put(POINT_USER_ID, 1);
            values.put(POINT_LINE_ID, 1);

            db.insert(POINT_TABLE, null, values);
        }

        // ISSUE-009 TEST COMMENTED FOR PRODUCTION
        // DATA FOR GRAVITY CALCULATIONS
         //fillReducedGTideCalcTestFwd(db);
         //fillReducedGTideCalcTestRev(db);

        // ISSUE-009  FOR PRODUCTION - PLEASE comment!!!
         //searchHistoricBenchmarksCP1407TestDataSet(db);

    }


    private void fillReducedGTideCalcTestFwd(SQLiteDatabase db) {

        // Create a container for the data.
        ContentValues values = new ContentValues();
        values.clear();

        //LINE-2
        /* FWRD
                LAT              LONG              HEIGHT
        agca	-20.04023900       -63.52110100     -0.000
        h059	-19.80880548       -63.48147970     1111.242
        h060	-19.73406146       -63.42347688     1096.433
        h061	-19.76459831       -63.33127261     1090.067
        caga	-19.79019883       -63.22417229     828.903
        boqn	-19.79066799       -63.19472173     815.900
       */
        String[] point = {"agca", "h059", "h060", "h061", "caga", "boqn"};
        double[] lat = {-20.04023900, -19.80880548, -19.73406146, -19.76459831, -19.79019883, -19.79066799};
        double[] lon = {-63.52110100, -63.48147970, -63.42347688, -63.33127261, -63.22417229, -63.19472173};
        double[] h = {-0.000, 1111.242, 1096.433, 1090.067, 828.903, 815.900};

        /*
        name    timestamp           offset  read1       read2       read3       reading     reduced_G
        agca	2016-09-04 13:23	0.000	1877.840	1877.840	1877.840	1877.840	1920.606
        h059	2016-09-04 14:09	0.000	1800.500	1800.500	1800.500	1800.500	1841.565
        h060	2016-09-04 14:44	0.000	1813.180	1813.180	1813.180	1813.180	1854.558
        h061	2016-09-04 15:10	0.000	1824.120	1824.122	1824.122	1824.121	1865.766
        caga	2016-09-04 16:45	0.000	1897.165	1897.165	1897.165	1897.165	1940.508
        boqn	2016-09-04 17:10	0.000	1899.690	1899.690	1899.690	1899.690	1943.099
        */

        double[] read1 = {1877.840, 1800.500, 1813.180, 1824.120, 1897.165, 1899.690};
        double[] read2 = {1877.840, 1800.500, 1813.180, 1824.122, 1897.165, 1899.690};
        double[] read3 = {1877.840, 1800.500, 1813.180, 1824.122, 1897.165, 1899.690};
        double[] reading = {1877.840, 1800.500, 1813.180, 1824.121, 1897.165, 1899.690};
        double[] readucedG = {1920.606, 1841.565, 1854.558, 1865.766, 1940.508, 1943.099};

        String[] timestamp = {"2016-09-04 13:23", "2016-09-04 14:09", "2016-09-04 14:44",
                "2016-09-04 15:10", "2016-09-04 16:45", "2016-09-04 17:10"};

        // POINT table data
        for (int i = 0; i < point.length; i++) {
            values.put(POINT_CODE, point[i]);
            values.put(POINT_DATE, timestamp[i]);
            values.put(POINT_G1, read1[i]);
            values.put(POINT_G2, read2[i]);
            values.put(POINT_G3, read3[i]);
            values.put(POINT_LATITUDE, lat[i]);
            values.put(POINT_LONGITUDE, lon[i]);
            values.put(POINT_HEIGHT, h[i]);
            values.put(POINT_READING, reading[i]);
            values.put(POINT_REDUCED_G, readucedG[i]);
            values.put(POINT_OFFSET, 0.000);

            try {
                Bitmap bitmap = DBTools.readPictureFromDirectory("ic_test.png", applicationContext);
                //DBTools.rere
                values.put(POINT_G1_PHOTO, savePhoto(bitmap));
                values.put(POINT_G2_PHOTO, savePhoto(bitmap));
                values.put(POINT_G3_PHOTO, savePhoto(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }

            values.put(POINT_ONE_WAY_VALUE, PointStatusInterface.POINT_ONEWAYVALUE_FORWARD);

            values.put(POINT_STATUS, PointStatusInterface.POINT_STATUS_REGISTERED);
            values.put(POINT_USER_ID, 1);
            values.put(POINT_LINE_ID, 2); // Gravimeter loaded on synch (TEST DATA)

            db.insert(POINT_TABLE, null, values);
        }
    }

    private void fillReducedGTideCalcTestRev(SQLiteDatabase db) {
        // Create a container for the data.
        ContentValues values = new ContentValues();
        values.clear();

        //LINE-2
        /* REV
                LAT                LONG             HEIGHT
        agca	-20.04023900       -63.52110100     -0.000
        h059	-19.80880548       -63.48147970     1111.242
        h060	-19.73406146       -63.42347688     1096.433
        h061	-19.76459831       -63.33127261     1090.067
        caga	-19.79019883       -63.22417229     828.903
        boqn	-19.79066799       -63.19472173     815.900
       */

        String[] point = {"boqn", "caga", "h061", "h060", "h059", "agca"};
        double[] lat = {-19.79066799, -19.79019883, -19.76459831, -19.73406146, -19.80880548, -20.04023900};
        double[] lon = {-63.19472173, -63.22417229, -63.33127261, -63.42347688, -63.48147970, -63.52110100};
        double[] h = {815.900, 828.903, 1090.067, 1096.433, 1111.242, -0.000};

        /* REV
        name    timestamp           offset  read1       read2       read3       reading     reduced_G
        boqn	"2016-09-04 17:18"	0.000	1899.700	1899.700	1899.700	1899.700	1943.111
        caga	"2016-09-04 17:45"	0.000	1897.100	1897.100	1897.100	1897.100	1940.457
        h061	"2016-09-04 18:19"	0.000	1824.222	1824.222	1824.222	1824.222	1865.946
        h060	"2016-09-04 19:00"	0.000	1813.072	1813.072	1813.072	1813.072	1854.534
        h059	"2016-09-04 19:33"	0.000	1800.580	1800.580	1800.580	1800.580	1841.745
        agca	"2016-09-04 20:38"	0.000	1877.782	1877.780	1877.780	1877.781	1920.630
        */

        double[] read1 = {1899.700, 1897.100, 1824.222, 1813.072, 1800.580, 1877.782};
        double[] read2 = {1899.700, 1897.100, 1824.222, 1813.072, 1800.580, 1877.780};
        double[] read3 = {1899.700, 1897.100, 1824.222, 1813.072, 1800.580, 1877.780};
        double[] reading = {1899.700, 1897.100, 1824.222, 1813.072, 1800.580, 1877.781};
        double[] reducedG = {1943.111, 1940.457, 1865.946, 1854.534, 1841.745, 1920.630};

        String[] timestamp = {"2016-09-04 17:18", "2016-09-04 17:45", "2016-09-04 18:19",
                "2016-09-04 19:00", "2016-09-04 19:33", "2016-09-04 20:38"};

        // POINT table data
        for (int i = 0; i < point.length; i++) {
            values.put(POINT_CODE, point[i]);
            values.put(POINT_DATE, timestamp[i]);
            values.put(POINT_G1, read1[i]);
            values.put(POINT_G2, read2[i]);
            values.put(POINT_G3, read3[i]);
            values.put(POINT_LATITUDE, lat[i]);
            values.put(POINT_LONGITUDE, lon[i]);
            values.put(POINT_HEIGHT, h[i]);
            values.put(POINT_READING, reading[i]);
            values.put(POINT_REDUCED_G, reducedG[i]);
            values.put(POINT_OFFSET, 0.000);

            try {
                Bitmap bitmap = DBTools.readPictureFromDirectory("ic_test.png", applicationContext);
                //DBTools.rere
                values.put(POINT_G1_PHOTO, savePhoto(bitmap));
                values.put(POINT_G2_PHOTO, savePhoto(bitmap));
                values.put(POINT_G3_PHOTO, savePhoto(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }

            values.put(POINT_ONE_WAY_VALUE, PointStatusInterface.POINT_ONEWAYVALUE_RETURN);

            values.put(POINT_STATUS, PointStatusInterface.POINT_STATUS_REGISTERED);
            values.put(POINT_USER_ID, 1);
            values.put(POINT_LINE_ID, 2); // Gravimeter loaded on synch (TEST DATA)

            db.insert(POINT_TABLE, null, values);
        }
    }


    /**
     * SEARCH FOR A CLOSER POINT TEST
     * Data used to TEST the GPS
     * <p>
     * How to TEST:
     * This is a test of the functionallity which searches benchmarks in the log4G database
     * wich will be prevuously loaded into the application before start the field working.
     * <p>
     * This test dataSet loads coordinates that are closer to the CP1407 in Argentina
     * whose approximate coordinates are -34.62593847, -58.49591695
     * <p>
     * To test in a different location will be neccessary to rewrite the set of coordinates
     * of the following variables:
     * double[] lat = {};
     * double[] lon = {};
     * with new coordinates closer to a distance <30mts of the
     * location where we going to execute the test:
     */
    private void searchHistoricBenchmarksCP1407TestDataSet(SQLiteDatabase db) {
        // Create a container for the data.
        ContentValues values = new ContentValues();
        values.clear();

        String[] point = {"tst1", "tst2", "tst3", "tst4", "tst5", "tst6"};
        double[] lat = {-34.62592031, -34.62592045, -34.62592036, -34.62592039, -34.62592032, -20.04023900};
        double[] lon = {-58.49601340, -58.49601350, -58.49601343, -58.49601341, -58.49601366, -58.49601388};
        double[] h = {815.900, 828.903, 1090.067, 1096.433, 1111.242, -0.000};

        String[] timestamp = {"2016-09-04 17:18", "2016-09-04 17:45", "2016-09-04 18:19",
                "2016-09-04 19:00", "2016-09-04 19:33", "2016-09-04 20:38"};

        // POINT table data
        for (int i = 0; i < point.length; i++) {
            values.put(POINT_CODE, point[i]);
            values.put(POINT_DATE, timestamp[i]);
            //values.put(POINT_G1, h[i]);
            //values.put(POINT_G2, h[i]);
            //values.put(POINT_G3, h[i]);
            values.put(POINT_LATITUDE, lat[i]);
            values.put(POINT_LONGITUDE, lon[i]);
            values.put(POINT_HEIGHT, h[i]);
            //values.put(POINT_READING, h[i]);
            //values.put(POINT_REDUCED_G, h[i]);
            values.put(POINT_OFFSET, 0.000);
            try {
                Bitmap bitmap = DBTools.readPictureFromDirectory("ic_test.png", applicationContext);
                //DBTools.rere
                values.put(POINT_G1_PHOTO, savePhoto(bitmap));
                values.put(POINT_G2_PHOTO, savePhoto(bitmap));
                values.put(POINT_G3_PHOTO, savePhoto(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }

            values.put(POINT_ONE_WAY_VALUE, PointStatusInterface.POINT_ONEWAYVALUE_FORWARD);
            values.put(POINT_STATUS, PointStatusInterface.POINT_STATUS_REGISTERED);
            values.put(POINT_USER_ID, 0);
            values.put(POINT_LINE_ID, 0); // Gravimeter loaded on synch (TEST DATA)

            long res = db.insert(POINT_TABLE, null, values);
            Log.i(LOG_TAG, "TEST ROW INSERTED::" + res);
        }
    }

    /**
     * END DATA FOR TEST
     */


}