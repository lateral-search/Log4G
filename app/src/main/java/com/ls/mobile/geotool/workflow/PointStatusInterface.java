package com.ls.mobile.geotool.workflow;

/**
 * POINT STATUS KEYS INTERFACE
 *
 * POINT STATUS WORKFLOW:
 *    PENDING(reverse)---> REGISTERED ---> ANOMALOUS ---> CORRECTED
 *
 */
public interface PointStatusInterface {

    // places where keys are implemented
    // change keys as String POINT_STATUS_REGISTERED = "OBSERVADA"
    // by String POINT_STATUS_REGISTERED = "OBSERVED"
    // and take the internationalized name from the string.xml

    // POINT STATUS WORKFLOW:
    //    PENDING(reverse)---> REGISTERED ---> ANOMALOUS ---> CORRECTED

    // Point was just created
    public static final String POINT_STATUS_PENDING = "PENDIENTE";

    // Point was readed
    public static final String POINT_STATUS_REGISTERED = "OBSERVADA";

    // Point had an anomaly and was corrected
    public static final String POINT_STATUS_EDITED = "CORREGIDA";

    // Point observation is wrong
    public static final String POINT_STATUS_ANOMALY = "ANOMALA";

    //
    public static final int POINT_ONEWAYVALUE_FORWARD = 0;
    public static final int POINT_ONEWAYVALUE_RETURN = 1;
}
