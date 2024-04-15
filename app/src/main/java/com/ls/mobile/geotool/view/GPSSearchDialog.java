package com.ls.mobile.geotool.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import com.ls.mobile.geotool.PointCRUDActivity;



/**
 * SEQUENCE OF EXCECUTION::
 * ------------------------
 *
 * I/GPSSearchDialog: IN::GPSSearchDialog.CONSTRUCTOR
 *                   OUT::GPSSearchDialog.CONSTRUCTOR
 * I/GPSSearchDialog: IN::GPSSearchDialog.onCreateDialog
 *                   OUT::GPSSearchDialog.onCreateDialog
 *
 * D/ViewRootImpl@40949b2[PointCRUDActivity]: setView = DecorView@e14cd03[PointCRUDActivity] touchMode=true
 *
 * I/GPSSearchDialog: IN::GPSSearchDialog.onResume
 *                   OUT::GPSSearchDialog.onResume
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */

// https://developer.android.com/guide/topics/ui/dialogs
public class GPSSearchDialog extends DialogFragment {

    // ISSUE MEMORY-LEAKS this 4 variables were static
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private View inflaterView;
    private PointCRUDActivity pointCRUDActivity;

    private static final String LOG_TAG = GPSSearchDialog.class.getSimpleName();
    /**
     * Default Constructor
     */
    public GPSSearchDialog(){

        Log.i(LOG_TAG,"OUT::GPSSearchDialog.DEFAULT_CONSTRUCTOR");

    }

    /**
     * Custom constructor: Use this, otherwise modal windows will not work
     *
     * @param inflaterVw: pass an inflaterView as:
     *            inflater = requireActivity().getLayoutInflater();
     *        inflaterView = inflater.inflate(R.layout.activity_field_search, null);
     *
     */
    @SuppressLint("ValidFragment")
    public GPSSearchDialog(View inflaterVw,PointCRUDActivity pointCRUDAct){
        super();
        Log.i(LOG_TAG,"IN::GPSSearchDialog.CONSTRUCTOR");

        inflaterView = inflaterVw;
        pointCRUDActivity = pointCRUDAct;

        Log.i(LOG_TAG,"OUT::GPSSearchDialog.CONSTRUCTOR");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Log.i(LOG_TAG,"IN::GPSSearchDialog.onCreateDialog");

        // Use the Builder class for convenient dialog construction
        builder = new AlertDialog.Builder(getActivity());


        // Get the layout inflater
        //inflater = requireActivity().getLayoutInflater();
        //inflaterView = inflater.inflate(R.layout.activity_field_search, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(this.inflaterView)
                // Add action buttons
//                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    @SuppressLint("NewApi")
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        pointCRUDActivity.stopGPSLocation();
//                    }
//                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @SuppressLint("NewApi")
                    public void onClick(DialogInterface dialog, int id) {
                        //LoginDialogFragment.this.getDialog().cancel();
                        pointCRUDActivity.stopGPSLocation();
                    }
                });


        // Full Screenate the modal screen
        //setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.inflaterView.setForegroundGravity(Gravity.FILL_VERTICAL);
        }

        Log.i(LOG_TAG,"OUT::GPSSearchDialog.onCreateDialog");

        // Set a local pointer to builder
        this.alertDialog = builder.create();

        return alertDialog;
    }

    // THIS OVERRIDE IS THE ONLY "SECRET TRICK" SOLUTION FOR
    // CHILD MODAL WINDOWS AT FULL SCREEN
    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        Log.i(LOG_TAG,"IN::GPSSearchDialog.onResume");

        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setGravity(Gravity.FILL);

        //setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.DialogStyle);

        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        // Set of a style from styles.xml on resources.
        //setStyle(DialogFragment.STYLE_NORMAL, android.R.style.FullScreenDialogStyle);

        //----------------------------------------------------
        // COMPOSITE OF THE REST OF THE ALERT (MODAL WINDOW)
        //----------------------------------------------------
        pointCRUDActivity.search(this,getInflaterView());

        Log.i(LOG_TAG,"OUT::GPSSearchDialog.onResume");
    }

    /**
     * Returns an instance
     * @return
     */
    // ISSUE MEMORY-LEAKS next 3 methods were static
    public AlertDialog.Builder getBuilder(){
        Log.i(LOG_TAG,"getBuilder()");
        return builder;
    }

    public AlertDialog getAlertDialog(){
        Log.i(LOG_TAG,"getAlertDialog()");
        return alertDialog;
    }

    public View getInflaterView(){
        Log.i(LOG_TAG,"getInflaterView()");
        return inflaterView;
    }


}



