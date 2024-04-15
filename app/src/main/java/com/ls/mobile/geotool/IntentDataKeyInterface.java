package com.ls.mobile.geotool;

/**
 * Keys used for Activity Intents.
 *
 * @author Lateral Search
 */
public interface IntentDataKeyInterface {

    // Line Selection Activity Screen
    public static final String LINE_SELECTION_ACTIVITY_LINE_ID_INT = "LINE_ID";

    // Line Data Activity Screen
    // key id of boolean flag for new or update operation discrimination
    public static final String LINE_DATA_ACTIVITY_TYPE_OPERATION = "TYPE_OPERATION";
    public static final String LINE_DATA_ACTIVITY_NEW = "NEW";
    public static final String LINE_DATA_ACTIVITY_UPDATE = "UPDATE";
    public static final String LINE_DATA_ACTIVITY_POINT_ID_INT = "POINT_ID";
    public static final String LINE_DATA_ACTIVITY_POINT_CODE_STR = "POINT_CODE";
    public static final String LINE_DATA_ACT_POINT_ONEWAY_VAL_INT = "ONE_WAY_VALUE";
    public static final String LINE_DATA_ACTIVITY_LINE_ID_INT = "LINE_ID";

    // PointCRUD Activity Screen
    // ISSUE - 004
    /**
     * Key POINT_CRUD_ACT_ONEWAY_VAL_INT holds a value when user comes from a CRUD operation
     * performed on PointCRUDActivity.
     * User is returning to previous screen "LineDataActivity". Such screen has 2 tabs,
     * a first tab for FORWARD POINTS and a second for RETURN POINTS.
     * The case is: when a RETURN crud operation was performed, the user must be redirected
     * to the RETURN screen which is the correspondent and correct, and not to the other.
     *
     */
    public static final String POINT_CRUD_ACT_ONEWAY_VAL_INT = "ONE_WAY_VALUE";

}
