package com.ls.mobile.geotool.db;

/**
 * DATABASE SQL SCRIPTS AND KEYS INTERFACE
 */
public interface GravityMobileDBInterface {
    public static final String SQL_ASC = "ASC";
    public static final String SQL_DESC = "DESC";
    // Tables
    public static final String USER_TABLE = "USER";
    public static final String GRAVIMETER_TABLE = "GRAVIMETER";
    public static final String CALIBRATION_TABLE = "CALIBRATION";
    public static final String LINE_TABLE = "LINE";
    public static final String POINT_TABLE = "POINT";
    // REFACTOR::OBS
    public static final String OBSERVATION_TABLE = "OBSERVATION";
    public static final String SYNC_TABLE = "SYNCHRONIZATION";
    public static final String AUDIT_LOG_TABLE = "AUDIT_LOG";

    // TABLE USER Column names.
    public static final String USER_ID = "_ID";
    public static final String USER_NAME = "NAME";
    // string array of columns.
    public static final String[] USER_COLUMNS = { USER_ID, USER_NAME };

    // TABLE GRAVIMETER Column names.
    public static final String GRAVIMETER_ID = "_ID";
    public static final String GRAVIMETER_NAME = "NAME";
    // string array of columns.
    public static final String[] GRAVIMETER_COLUMNS = { GRAVIMETER_ID,
                                                        GRAVIMETER_NAME };

    // TABLE CALIBRATION Column names.
    public static final String CALIBRATION_ID = "_ID";
    // index of the calibration value inside the table
    public static final String CAL_VALUE_INDEX = "INDEX_NBR";
    public static final String CALIBRATION_VALUE = "VALUE";
    public static final String CAL_GRAVIMETER_ID = "GRAVIMETER_ID";
    // string array of columns.
    public static final String[] CALIBRATION_COLUMNS = { CALIBRATION_ID
            ,CAL_VALUE_INDEX
            ,CALIBRATION_VALUE
            ,CAL_GRAVIMETER_ID };

    // TABLE LINE Column names.
    public static final String LINE_ID = "_ID";
    public static final String LINE_NAME = "NAME";
    public static final String LINE_DATE = "DATE";
    public static final String LINE_STATUS = "STATUS";
    public static final String LINE_DRIFT_RATE = "DRIFT_RATE";
    public static final String LINE_USER_ID = "USER_ID";
    public static final String LINE_GRAVIMETER_ID = "GRAVIMETER_ID";
    // string array of columns.
    public static final String[] LINE_COLUMNS = { LINE_ID
            ,LINE_NAME
            ,LINE_DATE
            ,LINE_STATUS
            ,LINE_DRIFT_RATE
            ,LINE_USER_ID
            ,LINE_GRAVIMETER_ID};

    // TABLE POINT Column names.
    public static final String POINT_ID = "_ID";
    public static final String POINT_CODE = "CODE";
    public static final String POINT_DATE = "DATE";
    public static final String POINT_G1 = "G1";
    public static final String POINT_G2 = "G2";
    public static final String POINT_G3 = "G3";
    public static final String POINT_LATITUDE = "LATITUDE";
    public static final String POINT_LONGITUDE = "LONGITUDE";
    public static final String POINT_STATUS = "STATUS"; // REGISTRADA-ANOMALA-EDITADA-PENDIENTE
    public static final String POINT_ONE_WAY_VALUE = "ONE_WAY_VALUE"; // TO  - RIGHT
    public static final String POINT_USER_ID = "USER_ID";
    public static final String POINT_LINE_ID = "LINE_ID";
    public static final String POINT_G1_PHOTO = "G1_PHOTO";
    public static final String POINT_G2_PHOTO = "G2_PHOTO";
    public static final String POINT_G3_PHOTO = "G3_PHOTO";
    public static final String POINT_HEIGHT = "HEIGHT";
    public static final String POINT_REDUCED_G = "REDUCED_G";
    public static final String POINT_READING = "READING";
    public static final String POINT_OFFSET = "OFFSET";
    public static final String POINT_DELTA = "DELTA";
    public static final String POINT_RESIDUAL = "RESIDUAL";


    // string array of columns.
    public static final String[] POINT_COLUMNS = {
            POINT_ID
            ,POINT_CODE
            ,POINT_DATE
            ,POINT_G1
            ,POINT_G2
            ,POINT_G3
            ,POINT_LATITUDE
            ,POINT_LONGITUDE
            ,POINT_STATUS
            ,POINT_ONE_WAY_VALUE
            ,POINT_USER_ID
            ,POINT_LINE_ID
            ,POINT_G1_PHOTO
            ,POINT_G2_PHOTO
            ,POINT_G3_PHOTO
            ,POINT_HEIGHT
            ,POINT_REDUCED_G
            ,POINT_READING
            ,POINT_OFFSET
            ,POINT_DELTA
            ,POINT_RESIDUAL
    };

    // REFACTOR::OBS
    // TABLE OBSERVATION Column names.
    public static final String OBSERVATION_ID = "_ID";
    public static final String OBSERVATION_CODE = "CODE";
    public static final String OBSERVATION_DATE = "DATE";
    public static final String OBSERVATION_G1 = "G1";
    public static final String OBSERVATION_G2 = "G2";
    public static final String OBSERVATION_G3 = "G3";
    public static final String OBSERVATION_LATITUDE = "LATITUDE";
    public static final String OBSERVATION_LONGITUDE = "LONGITUDE";
    public static final String OBSERVATION_STATUS = "STATUS"; // REGISTRADA-ANOMALA-EDITADA-PENDIENTE
    public static final String OBSERVATION_ONE_WAY_VALUE = "ONE_WAY_VALUE"; // TO  - RIGHT
    public static final String OBSERVATION_USER_ID = "USER_ID";
    public static final String OBSERVATION_LINE_ID = "LINE_ID";
    public static final String OBSERVATION_POINT_ID = "POINT_ID"; // REFACTOR::OBS
    public static final String OBSERVATION_G1_PHOTO = "G1_PHOTO";
    public static final String OBSERVATION_G2_PHOTO = "G2_PHOTO";
    public static final String OBSERVATION_G3_PHOTO = "G3_PHOTO";
    public static final String OBSERVATION_HEIGHT = "HEIGHT";
    public static final String OBSERVATION_REDUCED_G = "REDUCED_G";
    public static final String OBSERVATION_READING = "READING";
    public static final String OBSERVATION_OFFSET = "OFFSET";
    public static final String OBSERVATION_DELTA = "DELTA";
    public static final String OBSERVATION_RESIDUAL = "RESIDUAL";


    // string array of columns.
    // REFACTOR::OBS
    public static final String[] OBSERVATION_COLUMNS = {
             OBSERVATION_ID
            ,OBSERVATION_CODE
            ,OBSERVATION_DATE
            ,OBSERVATION_G1
            ,OBSERVATION_G2
            ,OBSERVATION_G3
            ,OBSERVATION_LATITUDE
            ,OBSERVATION_LONGITUDE
            ,OBSERVATION_STATUS
            ,OBSERVATION_ONE_WAY_VALUE
            ,OBSERVATION_USER_ID
            ,OBSERVATION_LINE_ID
            ,OBSERVATION_POINT_ID // REFACTOR::OBS
            ,OBSERVATION_G1_PHOTO
            ,OBSERVATION_G2_PHOTO
            ,OBSERVATION_G3_PHOTO
            ,OBSERVATION_HEIGHT
            ,OBSERVATION_REDUCED_G
            ,OBSERVATION_READING
            ,OBSERVATION_OFFSET
            ,OBSERVATION_DELTA
            ,OBSERVATION_RESIDUAL
    };

    // TABLE AUDIT_LOG Column names.
    public static final String AUDIT_ID = "_ID";
    public static final String AUDIT_LOG_TAG = "LOG_TAG";
    public static final String AUDIT_LOCATION = "LOCATION";
    public static final String AUDIT_LOGGED_DATA = "LOGGED_DATA";
    public static final String AUDIT_DATE = "DATE";
    // string array of columns.
    public static final String[] AUDIT_COLUMNS = { AUDIT_ID,
                                                   AUDIT_LOG_TAG,
                                                   AUDIT_LOCATION,
                                                   AUDIT_LOGGED_DATA,
                                                   AUDIT_DATE };

    /*
     * CREATE TABLES QUERIES:IN
     * Id will auto-increment if no value passed
     */
    // USER
    public static final String USER_TABLE_CREATE =
            "CREATE TABLE " + USER_TABLE + " (" +
                    USER_ID + " INTEGER PRIMARY KEY, " +
                    USER_NAME + " TEXT );";

    // GRAVIMETER
    public  static final String GRAVIMETER_TABLE_CREATE =
            "CREATE TABLE " + GRAVIMETER_TABLE + " (" +
                    GRAVIMETER_ID + " INTEGER PRIMARY KEY, " +
                    GRAVIMETER_NAME + " TEXT " + " );";

    // CALIBRATION
    public  static final String CALIBRATION_TABLE_CREATE =
            "CREATE TABLE " + CALIBRATION_TABLE + " (" +
                              CALIBRATION_ID + " INTEGER PRIMARY KEY, " +
                              CAL_VALUE_INDEX + " INTEGER, " +
                              CALIBRATION_VALUE + " TEXT, " +
                              CAL_GRAVIMETER_ID + " INTEGER REFERENCES " +
                              GRAVIMETER_TABLE + "("+ GRAVIMETER_ID +"));"; // TO DO  CONSTRAINTS

    // LINE
    public  static final String LINE_TABLE_CREATE =
            "CREATE TABLE " + LINE_TABLE + " (" +
                    LINE_ID + " INTEGER PRIMARY KEY, " +
                    LINE_NAME    + " TEXT, " +
                    LINE_DATE    + " TEXT, " +
                    LINE_STATUS  + " TEXT, " +
                    LINE_DRIFT_RATE   + " REAL, " +
                    LINE_USER_ID  + " INTEGER REFERENCES " + USER_TABLE+"("+ USER_ID + ")," +
                    LINE_GRAVIMETER_ID + " INTEGER REFERENCES " + GRAVIMETER_TABLE + "(" + GRAVIMETER_ID +"));";

    // POINT
    public static final String POINT_TABLE_CREATE =
             "CREATE TABLE " + POINT_TABLE + " (" +
                    POINT_ID + " INTEGER PRIMARY KEY, " +
                    POINT_CODE + " TEXT, " +
                    POINT_DATE + " TEXT, " +
                    POINT_G1  + " REAL, " +
                    POINT_G2  + " REAL, " +
                    POINT_G3  + " REAL, " +
                    POINT_G1_PHOTO + " BLOB, " +
                    POINT_G2_PHOTO + " BLOB, " +
                    POINT_G3_PHOTO + " BLOB, " +
                    POINT_LATITUDE  + " REAL, " +
                    POINT_LONGITUDE  + " REAL, " +
                    POINT_HEIGHT  + " REAL, " +
                    POINT_REDUCED_G  + " REAL, " +
                    POINT_READING  + " REAL, " +
                    POINT_OFFSET  + " REAL, " +
                    POINT_STATUS + " TEXT, " +
                    POINT_ONE_WAY_VALUE + " TEXT, " +
                    POINT_DELTA     + " REAL, " +
                    POINT_RESIDUAL  + " REAL, " +
                    POINT_USER_ID + " INTEGER REFERENCES " + USER_TABLE +"("+ USER_ID + ")," +
                    POINT_LINE_ID + " INTEGER REFERENCES " + LINE_TABLE +"("+ LINE_ID + ") );";

    // OBSERVATION
    // REFACTOR::OBS
    public static final String OBSERVATION_TABLE_CREATE =
            "CREATE TABLE " + OBSERVATION_TABLE + " (" +
                    OBSERVATION_ID + " INTEGER PRIMARY KEY, " +
                    OBSERVATION_CODE + " TEXT, " +
                    OBSERVATION_DATE + " TEXT, " +
                    OBSERVATION_G1  + " REAL, " +
                    OBSERVATION_G2  + " REAL, " +
                    OBSERVATION_G3  + " REAL, " +
                    OBSERVATION_G1_PHOTO + " BLOB, " +
                    OBSERVATION_G2_PHOTO + " BLOB, " +
                    OBSERVATION_G3_PHOTO + " BLOB, " +
                    OBSERVATION_LATITUDE  + " REAL, " +
                    OBSERVATION_LONGITUDE  + " REAL, " +
                    OBSERVATION_HEIGHT  + " REAL, " +
                    OBSERVATION_REDUCED_G  + " REAL, " +
                    OBSERVATION_READING  + " REAL, " +
                    OBSERVATION_OFFSET  + " REAL, " +
                    OBSERVATION_STATUS + " TEXT, " +
                    OBSERVATION_ONE_WAY_VALUE + " TEXT, " +
                    OBSERVATION_DELTA     + " REAL, " +
                    OBSERVATION_RESIDUAL  + " REAL, " +
                    OBSERVATION_USER_ID + " INTEGER REFERENCES " + USER_TABLE +"("+ USER_ID + ")," +
                    OBSERVATION_LINE_ID + " INTEGER REFERENCES " + LINE_TABLE +"("+ LINE_ID + ")," +
                    OBSERVATION_POINT_ID + " INTEGER REFERENCES " + POINT_TABLE +"("+ POINT_ID + ") ); ";
                    // REFACTOR::OBS



    // SYNC
    /**
     * SYNC_TABLE
     * Stores every sync attempt result and date
     * THe important is the last synchronization DATE, wich we use to
     * compare  against the interface file to validate:
     * interface file CREATION DATE > LAST SUCCESSFUL SYNCHRONIZATION DATE
     */
    // TABLE SYNC Column names.
    public static final String SYNC_ID = "_ID";
    public static final String SYNC_DATE = "DATE";
    public static final String SYNC_STATUS = "STATUS"; // 0 - error / 1 - 0k
    public static final String SYNC_FILE_NAME = "FILE_NAME";
    public static final String SYNC_DIR_NAME = "DIR_NAME";

    // string array of columns.
    public static final String[] SYNC_COLUMNS = {
            SYNC_ID
            ,SYNC_DATE
            ,SYNC_STATUS
            ,SYNC_FILE_NAME
            ,SYNC_DIR_NAME
    };

    // SYNC STATUS STATES.
    public static final int SYNC_STATUS_0K = 1;
    public static final int SYNC_STATUS_ERR = 0;

    //SYNC CREATE TABLE
    public  static final String SYNC_TABLE_CREATE =
            "CREATE TABLE " + SYNC_TABLE + " (" +
                    SYNC_ID + " INTEGER PRIMARY KEY, " +
                    SYNC_DATE + " TEXT, " +
                    SYNC_STATUS  + " TEXT, " +
                    SYNC_FILE_NAME + " TEXT, " +
                    SYNC_DIR_NAME + " TEXT  );";

    // AUDIT LOG
    public static final String AUDIT_TABLE_CREATE =
            "CREATE TABLE " + AUDIT_LOG_TABLE + " (" +
                              AUDIT_ID + " INTEGER PRIMARY KEY, " +
                              AUDIT_DATE     + " TEXT, " +
                              AUDIT_LOG_TAG  + " TEXT, " +
                              AUDIT_LOCATION + " TEXT, " +
                              AUDIT_LOGGED_DATA + " TEXT );";

    /**
     * CREATE TABLES QUERIES:OUT
     */


}
