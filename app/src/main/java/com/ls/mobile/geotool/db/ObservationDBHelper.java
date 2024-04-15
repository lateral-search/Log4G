package com.ls.mobile.geotool.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ls.mobile.geotool.db.data.model.Observation;
import com.ls.mobile.geotool.db.data.model.Point;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all Database neccessary methods for
 * OBSERVATION object db persistence and querying.
 * TO DO: This could be a future improvement of the database model
 *        until now was not neccessary
 * @deprecated
 */
// REFACTOR::OBS::IN
public class ObservationDBHelper implements GravityMobileDBInterface {

    // Log
    private static final String LOG_TAG = ObservationDBHelper.class.getSimpleName();


    /***********************************************************
     * OBSERVATION :: IN
     */
    public long createObservation(Observation obs,
                                  SQLiteDatabase db,
                                  boolean isSynchronization) {
        // LINE table data
        // SEE METHOD: SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        // fill daya String.valueOf(new Date().getTime())
        //values.put(OBSERVATION_ID, obs.getmId());
        values.put(OBSERVATION_CODE, obs.getCode());
        values.put(OBSERVATION_DATE, obs.getDate());
        values.put(OBSERVATION_G1, obs.getG1());
        values.put(OBSERVATION_G2, obs.getG2());
        values.put(OBSERVATION_G3, obs.getG3());

        // ATTENTION!::If isSynchronization == true data will come from
        // synchronization, and in this case many fields like photos will
        // be not inserted
        if (isSynchronization == false) {
            values.put(OBSERVATION_G1_PHOTO, savePhoto(obs.getG1Photo()));
            values.put(OBSERVATION_G2_PHOTO, savePhoto(obs.getG2Photo()));
            values.put(OBSERVATION_G3_PHOTO, savePhoto(obs.getG3Photo()));
        }
        values.put(OBSERVATION_LATITUDE, obs.getLatitude());
        values.put(OBSERVATION_LONGITUDE, obs.getLongitude());
        values.put(OBSERVATION_HEIGHT, obs.getHeight());
        values.put(OBSERVATION_OFFSET, obs.getOffset());
        values.put(OBSERVATION_REDUCED_G, obs.getReducedG());
        values.put(OBSERVATION_READING, obs.getReading());
        values.put(OBSERVATION_STATUS, obs.getStatus());
        values.put(OBSERVATION_ONE_WAY_VALUE, obs.getOneWayValue());
        values.put(OBSERVATION_DELTA, obs.getDelta());
        values.put(OBSERVATION_RESIDUAL, obs.getResidual());
        values.put(OBSERVATION_LINE_ID, obs.getLineId());
        values.put(OBSERVATION_USER_ID, obs.getUserId());

        long res = db.insert(OBSERVATION_TABLE, null, values);

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

    public long createObservationForReverse(Observation obs,
                                            SQLiteDatabase db) {
        // LINE table data
        // SEE: SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        // fill daya String.valueOf(new Date().getTime())
        //values.put(OBSERVATION_ID, point.getmId());
        values.put(OBSERVATION_CODE, obs.getCode());
        values.put(OBSERVATION_DATE, obs.getDate());
        values.put(OBSERVATION_G1, obs.getG1());
        values.put(OBSERVATION_G2, obs.getG2());
        values.put(OBSERVATION_G3, obs.getG3());
        values.put(OBSERVATION_LATITUDE, obs.getLatitude());
        values.put(OBSERVATION_LONGITUDE, obs.getLongitude());
        values.put(OBSERVATION_STATUS, obs.getStatus());
        values.put(OBSERVATION_ONE_WAY_VALUE, obs.getOneWayValue());
        values.put(OBSERVATION_DELTA, obs.getDelta());
        values.put(OBSERVATION_RESIDUAL, obs.getResidual());
        values.put(OBSERVATION_LINE_ID, obs.getLineId());
        values.put(OBSERVATION_USER_ID, obs.getUserId());

        long res = db.insert(OBSERVATION_TABLE, null, values);

        return res;
    }

    /**
     * OBSERVATION UPDATE
     *
     * @param obs
     * @return
     */
    public int updateObservation(Observation obs,
                                 SQLiteDatabase db) {
        // LINE table data
        // SQLiteDatabase db = getWritableDatabase();
        // Create a container for the data.
        ContentValues values = new ContentValues();
        // fill daya String.valueOf(new Date().getTime())
        //values.put(OBSERVATION_ID, observation.getmId());
        values.put(OBSERVATION_CODE, obs.getCode());
        values.put(OBSERVATION_DATE, obs.getDate());
        values.put(OBSERVATION_G1, obs.getG1());
        values.put(OBSERVATION_G2, obs.getG2());
        values.put(OBSERVATION_G3, obs.getG3());

        // TEST
        values.put(OBSERVATION_G1_PHOTO, savePhoto(obs.getG1Photo()));
        values.put(OBSERVATION_G2_PHOTO, savePhoto(obs.getG2Photo()));
        values.put(OBSERVATION_G3_PHOTO, savePhoto(obs.getG3Photo()));

        values.put(OBSERVATION_LATITUDE, obs.getLatitude());
        values.put(OBSERVATION_LONGITUDE, obs.getLongitude());
        values.put(OBSERVATION_STATUS, obs.getStatus());
        values.put(OBSERVATION_ONE_WAY_VALUE, obs.getOneWayValue());
        values.put(OBSERVATION_DELTA, obs.getDelta());
        values.put(OBSERVATION_RESIDUAL, obs.getResidual());
        //FKs
        values.put(OBSERVATION_LINE_ID, obs.getLineId());
        values.put(OBSERVATION_USER_ID, obs.getUserId());

        // TO DO ADD EVERYWHERE when decided to implement it
        values.put(OBSERVATION_POINT_ID, obs.getPointId());

        values.put(OBSERVATION_READING, obs.getReading());
        values.put(OBSERVATION_REDUCED_G, obs.getReducedG());
        values.put(OBSERVATION_HEIGHT, obs.getHeight());
        values.put(OBSERVATION_OFFSET, obs.getOffset());


        String where = OBSERVATION_ID + " = ?";
        String[] whereArgs = {String.valueOf(obs.getmId())};

        return db.update(OBSERVATION_TABLE, values, where, whereArgs);
    }

    /**
     * Original method.
     * NOTE: it has a copy with different order criteria by OBSERVATION_ID and not by DATE, called
     *     public List<Point> getPointsByLnIdOneWayValOrderByPointId(int lineId
     *
     * @param lineId
     * @param oneWayValue
     * @param orderCriteria
     * @return
     */
    public List<Observation> getObservationByLineIdAndOnwWayVal(int lineId,
                                                          int oneWayValue,
                                                          String orderCriteria,
                                                          SQLiteDatabase db,
                                                          GravityMobileDBHelper gm) {
        String query = "SELECT  * FROM " + OBSERVATION_TABLE +
                " WHERE " + OBSERVATION_LINE_ID + "=" + lineId +
                " AND " + OBSERVATION_ONE_WAY_VALUE + " = " + oneWayValue +
                " ORDER BY " + OBSERVATION_DATE + " " + orderCriteria; //  ASC or DESC
        Cursor cursor = null;
        List<Observation> entryLst = null;
        try {
            cursor = gm.getWritableDatabase().rawQuery(query, null); //
            if (cursor.getCount() != 0) {
                entryLst = new ArrayList<Observation>();
                if (cursor.moveToFirst()) {
                    do {
                        // A new instance for every Point in the list
                        Observation pAux = new Observation();

                        // Build the point with a centralized method.
                        buildObservation(cursor, pAux);

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
     * the new different order criteria (by POI NT_ID)
     *
     * @param lineId
     * @param oneWayValue
     * @param orderCriteria
     * @return
     */
    public List<Observation> getPointsByLnIdOneWayValOrderByPointId(int lineId
            , int oneWayValue
            , String orderCriteria
            , GravityMobileDBHelper gm) {
        String query = "SELECT  * FROM " + OBSERVATION_TABLE +
                " WHERE " + OBSERVATION_LINE_ID + "=" + lineId +
                " AND " + OBSERVATION_ONE_WAY_VALUE + " = " + oneWayValue +
                " ORDER BY " + OBSERVATION_ID + " " + orderCriteria; //  ASC or DESC
        Cursor cursor = null;
        List<Observation> entryLst = null;
        try {
            cursor = gm.getWritableDatabase().rawQuery(query, null);
            if (cursor.getCount() != 0) {
                entryLst = new ArrayList<Observation>();
                if (cursor.moveToFirst()) {
                    do {
                        // A new instance for every Point in the list
                        Observation pAux = new Observation();

                        // Build the point with a centralized method.
                        buildObservation(cursor, pAux);

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
    public List<Observation> getPreexistentPoints(double longitude,
                                            double latitude,
                                            String orderCriteria,
                                            GravityMobileDBHelper gm) {


        String query = "SELECT * FROM " + OBSERVATION_TABLE +
                " WHERE " + OBSERVATION_LATITUDE + " < " + (latitude + 1) +
                " AND " + OBSERVATION_LATITUDE + " > " + (latitude - 1) +
                " AND " + OBSERVATION_LONGITUDE + " < " + (longitude + 1) +
                " AND " + OBSERVATION_LONGITUDE + " > " + (longitude - 1) +
                /* BUSINESS RULE:
                 * ALL CLOSER PREEXISTENT POINTS MUST BE RETRIEVED BY GPS
                 * EVEN THOSE WHICH WHERE ACTUALLY CREATED FOR THIS REASON
                 * THE LINE: " AND  " + POI NT_LINE_ID + " = 0 " +
                 * WILL BE NOT USED
                 */
                // ISSUE-010
                " GROUP BY "  + OBSERVATION_CODE +
                " ORDER BY " + OBSERVATION_LONGITUDE + " " + orderCriteria; //  ASC or DESC

        Cursor cursor = null;
        List<Observation> entryLst = null;
        try {
            cursor = gm.getWritableDatabase().rawQuery(query, null);
            if (cursor.getCount() != 0) {
                entryLst = new ArrayList<Observation>();
                if (cursor.moveToFirst()) {
                    do {
                        // A new instance for every Point in the list
                        Observation pAux = new Observation();

                        // Build the point with a centralized method.
                        buildObservation(cursor, pAux);

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
    private void buildObservation(Cursor cursor, Observation pAux) {
        pAux.setmId(cursor.getInt(cursor.getColumnIndex(OBSERVATION_ID)));
        pAux.setCode(cursor.getString(cursor.getColumnIndex(OBSERVATION_CODE)));
        pAux.setDate(cursor.getString(cursor.getColumnIndex(OBSERVATION_DATE)));
        pAux.setG1(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_G1)));
        pAux.setG2(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_G2)));
        pAux.setG3(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_G3)));
        pAux.setLatitude(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_LATITUDE)));
        pAux.setLongitude(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_LONGITUDE)));
        pAux.setOneWayValue(cursor.getInt(cursor.getColumnIndex(OBSERVATION_ONE_WAY_VALUE)));
        pAux.setStatus(cursor.getString(cursor.getColumnIndex(OBSERVATION_STATUS)));
        //FKs:in
        pAux.setUserId(cursor.getInt(cursor.getColumnIndex(OBSERVATION_USER_ID)));
        pAux.setLineId(cursor.getInt(cursor.getColumnIndex(OBSERVATION_LINE_ID)));
        pAux.setLineId(cursor.getInt(cursor.getColumnIndex(OBSERVATION_POINT_ID)));
        //FKs:out
        pAux.setG1Photo(getPhotoFromDB(cursor, OBSERVATION_G1_PHOTO));
        pAux.setG2Photo(getPhotoFromDB(cursor, OBSERVATION_G2_PHOTO));
        pAux.setG3Photo(getPhotoFromDB(cursor, OBSERVATION_G3_PHOTO));
        pAux.setReading(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_READING)));
        pAux.setReducedG(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_REDUCED_G)));
        pAux.setHeight(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_HEIGHT)));
        pAux.setOffset(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_OFFSET)));
        pAux.setDelta(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_DELTA)));
        pAux.setResidual(cursor.getDouble(cursor.getColumnIndex(OBSERVATION_RESIDUAL)));
    }

    public List<Observation> getPointsByLineId(int lineId
            , String orderCriteria,
    GravityMobileDBHelper gm) {
        String query = "SELECT  * FROM " + OBSERVATION_TABLE
                + " WHERE " + OBSERVATION_LINE_ID + "=" + lineId
                + " ORDER BY " + OBSERVATION_DATE + " "
                + orderCriteria; // " ASC " DESC
        Cursor cursor = null;
        List<Observation> entryLst = new ArrayList<Observation>();
        try {
            cursor = gm.getWritableDatabase().rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    Observation pAux = new Observation();

                    // Build the point with a centralized method.
                    buildObservation(cursor, pAux);
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

    public Observation getObservationByCodeAndLineIdAndOneWayVal(String pointCode,
                                                     int lineId,
                                                     GravityMobileDBHelper gm,
                                                     int oneWayValue) {
        String query = "SELECT  * FROM " + OBSERVATION_TABLE +
                " WHERE " + OBSERVATION_CODE + " ='" + pointCode.trim() + "'"
                + " AND " + OBSERVATION_LINE_ID + " = " + String.valueOf(lineId)
                + " AND " + OBSERVATION_ONE_WAY_VALUE + " = " + String.valueOf(oneWayValue);
        Cursor cursor = null;
        Observation pAux = new Observation();
        try {
            cursor = gm.getWritableDatabase().rawQuery(query, null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();

                // Build the point with a centralized method.
                buildObservation(cursor, pAux);

            }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return pAux;
        }
    }



    public Observation getPointByCodeAndLineId(String pointCode,
                                         int lineId,
                                         GravityMobileDBHelper gm) {
        String query = "SELECT  * FROM " + OBSERVATION_TABLE +
                " WHERE " + OBSERVATION_CODE + " ='" + pointCode.trim() + "'"
                + " AND " + OBSERVATION_LINE_ID + " = " + String.valueOf(lineId);
        Cursor cursor = null;
        Observation pAux = new Observation();
        try {
            cursor = gm.getWritableDatabase().rawQuery(query, null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();

                // Build the point with a centralized method.
                buildObservation(cursor, pAux);

            }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();

            return pAux;
        }
    }

    public Observation getPointById(int id, GravityMobileDBHelper gm) {
        String query = "SELECT  * FROM " + OBSERVATION_TABLE +
                " WHERE " + OBSERVATION_ID + " = " + id;
        Cursor cursor = null;
        Observation pAux = new Observation();
        try {
            cursor = gm.getWritableDatabase().rawQuery(query, null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();

                // Build the point with a centralized method.
                buildObservation(cursor, pAux);

            }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            return pAux;
        }
    }

    public Observation getPointByCode(String code,GravityMobileDBHelper gm) {
        if(!"".equals(code) && code != null) code.trim();

        String query = "SELECT  * FROM " + OBSERVATION_TABLE +
                " WHERE " + OBSERVATION_CODE + " = '" + code + "'";
        Cursor cursor = null;
        Observation pAux = new Observation();
        try {
            cursor = gm. getWritableDatabase().rawQuery(query, null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();

                // Build the point with a centralized method.
                buildObservation(cursor, pAux);

            }

        } catch (Exception e) {
            Log.d(LOG_TAG, "QUERY EXEPTION!: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            return pAux;
        }
    }





    /* *******************************************************
     * OBSERVATION :: OUT
     *********************************************************/

}
// REFACTOR::OBS::OUT