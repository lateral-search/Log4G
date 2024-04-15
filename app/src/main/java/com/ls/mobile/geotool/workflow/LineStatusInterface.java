package com.ls.mobile.geotool.workflow;

import com.ls.mobile.geotool.R;
import android.support.v7.app.AppCompatActivity;

/**
 * LINE STATUS KEYS INTERFACE
 */
public interface LineStatusInterface {
    public static final String LINE_STATUS_CLOSED = "CERRADA";
    public static final String LINE_STATUS_INCONSISTENCE = "INCONSISTENTE";
    public static final String LINE_STATUS_INCOMPLETE = "EXPLORANDO";//"INCOMPLETA";
    public static final String LINE_STATUS_RETURNED = "RETORNANDO";


    // wrote to a res file, and internationalize
    //findViewById(R.id.LINE_STATUS_VALID);
    //findViewById(R.id.LINE_STATUS_INCONSISTENCE);
    //findViewById(R.id.LINE_STATUS_INCOMPLETE);
}
