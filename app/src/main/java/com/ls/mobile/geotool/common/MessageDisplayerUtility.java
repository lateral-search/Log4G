package com.ls.mobile.geotool.common;

import android.app.Activity;
import android.content.DialogInterface;

import com.ls.mobile.geotool.R;
/**
 * MessageDisplayerUtility
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public abstract class MessageDisplayerUtility {

    /**
     * Display an alert.
     *
     * @param activity
     * @param msgToShow
     */
    public static void displaySimpleAlert(Activity activity,
                                          String msgToShow) {
        // Build Alert
        android.support.v7.app.AlertDialog.Builder myAlertBuilder = new
                android.support.v7.app.AlertDialog.Builder(activity);
        myAlertBuilder.setTitle(R.string.app_name);

        myAlertBuilder.setMessage(msgToShow);

        myAlertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //	Do nothing.
            }
        });
        myAlertBuilder.setCancelable(false);
        myAlertBuilder.show();
    }

}
