package com.ls.mobile.geotool.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ls.mobile.geotool.PointCRUDActivity;
import com.ls.mobile.geotool.R;
import com.ls.mobile.geotool.common.MessageDisplayerUtility;

/**
 * This class contains the GPS service, which is the neccessary
 * workflow implementation to use the GPS through log4G
 *
 * @author lateralsearch.
 *
 * SERVICE DEVELOPMENT GUIDE:
 *
 * https://developer.android.com/reference/android/app/Service
 * <p>
 * CODE:
 * https://www.codota.com/code/java/classes/android.location.LocationManager
 * <p>
 * LOCATION SERVICE GUIDES: things  always IMPORTANT!!!!!
 * The following article explains the principle very well :
 * http://developer.android.com/guide/topics/location/obtaining-user-location.html
 *
 * This starts the (background?) service to check and track
 * cells with or without GPS enabled.
 * <p>
 * https://stackoverflow.com/questions/8828639/get-gps-location-via-a-service-in-android
 *
 * Flow for obtaining user location
 *
 * Here's the typical flow of procedures for obtaining the user location:
 *
 *  1- Start application.
 *  2- Sometime later, start listening for updates from desired location providers.
 *  3- Maintain a "current best estimate" of location by filtering out new, but less accurate fixes.
 *  4- Stop listening for location updates.
 *  5- Take advantage of the last best location estimate.
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 *
 */
public class GeotoolService extends Service {

    // NOTIFICATION MANAGER
    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.geotool_service_started;

    // LOCATION
    private LocationManager mLocationManager = null;
    private Location mCurrentLocation = null; // LOCATION to retrieve to CLIENTS

    // Constants
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    // To execute System alert in case GPS is deactivated
    private WindowManager manager;
    private View view;

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    // Log
    private static final String LOG_TAG = GeotoolService.class.getSimpleName();


    /**
     * IN::CLASS::LOCAL BINDER::
     * <p>
     * Class used for the client Binder.
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC. Local Binder returns GeotoolService wich is a "LOCAL" service
     */
    public class LocalBinder extends Binder {
        public GeotoolService getGeotoolServiceInstance() {
            return GeotoolService.this;
        }
    }
    /**
     *  OUT:: CLASS::LOCAL BINDER
     */


    /**
     * IBinder
     */
    @Override
    public IBinder onBind(Intent intent) {
        // onBind()
        // The system invokes this method by calling bindService()
        // when another component wants to bind with the service (such as to perform RPC). In your
        // implementation of this method, you must provide an interface that clients use to communicate
        // with the service by returning an IBinder. You must always implement this method; however,
        // if you don't want to allow binding, you should return null.
        //return null;
        return mBinder;
    }


    /**
     * ON CREATE
     *
     * Called by the system when the service is first created.
     * Do not call this method directly.
     */
    @Override
    public void onCreate() {

        Log.i(LOG_TAG, "******************************************************onCreate");
        Log.i(LOG_TAG, "******************************************************onCreate");
        Log.i(LOG_TAG, "******************************************************onCreate");

        // Display a notification about us starting.  We put an icon in the status bar.
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        /**
         * LOCATION MANAGER INITIALIZATION::
         * Acquire a reference to the system Location Manager
         * https://developer.android.com/guide/topics/location/strategies
         */
         if (mLocationManager == null) {
             Log.i(LOG_TAG, "initializeLocationManager");
             mLocationManager = (LocationManager)
                        this.getSystemService(Context.LOCATION_SERVICE);
         }
         // LOCATION LISTENER uodates remove to re activate
         try {
                 mLocationManager.removeUpdates(locationListener);
         }catch(NullPointerException n){
             // ISSUE MEMORY-LEAKS
             Log.e(LOG_TAG, n.getMessage());
         }catch(IllegalArgumentException ex){
             Log.e(LOG_TAG, ex.getMessage());
         }catch(Exception e){
             Log.e(LOG_TAG, e.getMessage());
         }

         // GPS ENABLE/ DISABLE
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // ALERT DIALOG: requests user action to enable
            // location services, then when the user clicks the "OK" button,
            gpsDisabledSystemAlert();

        } else {

            /**
             * IN:LOCATION LISTENER ACTIVATION
             */
            try {
                // Register the LOOPER
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

                Location l = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(l != null){
                    Log.i(LOG_TAG,"<<<<<<<<<LONG: " + l.getLongitude() + "---" + "LAT: " + l.getLatitude() );

                    Log.i(LOG_TAG,"HORIZONTAL ACCURACY: " + l.getAccuracy()  );
                    Log.i(LOG_TAG,"UTC TIME: " + l.getTime()  );
                    Log.i(LOG_TAG,"ALTITUDE: " + l.getAltitude()  );
                    Log.i(LOG_TAG,"BEARING DEGREES: " + l.getBearing());
                    //Log.i(LOG_TAG,"SPEED ACCURACY m/s: " + l.getSpeedAccuracyMetersPerSecond());
                    Log.i(LOG_TAG,"SPEED: " + l.getSpeed());
                }


            } catch (java.lang.SecurityException ex) {
                Log.i(LOG_TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(LOG_TAG, "network provider does not exist, " + ex.getMessage());
            }
            /**
             * OUT:LOCATION LISTENER ACTIVATION
             */


        }// END::ELSE GPS ENABLED
    }



    /**
     * onCreate and onStart differences
     * Services can be started when a client calls the Context.startService(Intent) method.
     * If the service isn’t already running, Android starts it and calls its onCreate method
     * followed by the onStart method.
     * If the service is already running, its onStart method is
     * invoked again with the new intent. So it’s quite possible and normal for a service’s
     * onStart method to be called repeatedly in a single run of the service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(LOG_TAG, "onStartCommand::Received start id: " +
                startId +
                ": " +
                intent);

        // Service is not restarted. Used for services which are periodically
        // triggered anyway. The service is only restarted if the runtime has
        // pending startService() calls since the service termination.
        return START_NOT_STICKY;
    }


    /**
     * DESTROY
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<onDestroy");
        Log.i(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<onDestroy");
        Log.i(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<onDestroy");

        if (mLocationManager != null) {
            try {
                // Remove the listener you previously added
                mLocationManager.removeUpdates(locationListener);
            } catch (Exception ex) {
                Log.i(LOG_TAG, "fail to remove location listners, ignore", ex);
            }
        }

        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.geotool_service_started, Toast.LENGTH_SHORT).show();
    }




    /**
     * IN::LISTENER 4 LOCATION ::
     *
     * Define a listener that responds to location updates
     * https://developer.android.com/guide/topics/location/strategies
     */
    final LocationListener locationListener = new LocationListener() {
        //
        public void onLocationChanged(Location location) {
            Log.i(LOG_TAG, "locationListener.onLocationChanged()");
            // Called when a new location is found by the network location provider.
            if(location!=null) {
                // Set GPS location to retrieve
                mCurrentLocation = location;

                Log.i(LOG_TAG, ">>>>>>>>>>>??????" + String.valueOf(location.getLatitude()));
                Log.i(LOG_TAG, ">>>>>>>>>>>??????" + String.valueOf(location.getLongitude()));

                // REMOVE LISTENER!!
                removeListeners();
            }
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(LOG_TAG, "locationListener.onStatusChanged()");
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

            // TOAST MESSAGE
            Toast.makeText(GeotoolService.this,msg,Toast.LENGTH_LONG).show();
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
     */



    /******************************************************************************
     * IN :: GETTERS / SETTERS
     ******************************************************************************/
    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    /**
     * OUT :: GETTERS / SETTERS
     */



    /**************************************************************************
     * ************************************************************************
     *
     * IN :: HELPERS
     *
     **************************************************************************
     * ************************************************************************/


    /**
     * NOTIFICATION ON BAR
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.geotool_service_started);

        // IMPROVEMENT?::The PendingIntent to launch our activity if the user selects this notification
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        //        new Intent(this, GeotoolService.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)     //status icon
                    .setTicker(text)                                          // the status text
                    .setWhen(System.currentTimeMillis())                      // the time stamp
                    .setContentTitle(getText(R.string.geotool_service_label)) // the label of the entry
                    .setContentText(text)                                     // the contents of the entry
                    //.setContentIntent(contentIntent)                        // The intent to send when the entry is clicked
                    .build();

            // Send the notification.
            mNM.notify(NOTIFICATION, notification);

        }
    }

    /**
     * SYSTEM ALERT CALL
     * <p>
     * It needs SYSTEM_ALERT_WINDOW permission.
     * Remember to add this permissin in Manifest file.
     * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
     */
    private void gpsDisabledSystemAlert() {

        // SET LOCAL VARIABLE (WindowManager)
        manager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.alpha = 1.0f;
        layoutParams.packageName = getApplicationContext().getPackageName();
        layoutParams.buttonBrightness = 1f;
        layoutParams.windowAnimations = android.R.style.Animation_Dialog;

        // SET LOCAL VARIABLE (View)
        view = View.inflate(getApplicationContext(), R.layout.alert_dialog_gps, null);

        TextView text = (TextView) view.findViewById(R.id.alertDialogGPSTextV);
        text.setText("No podra utilizar Log4Gravity si no activa el GPS");

        manager.addView(view, layoutParams);
    }


    /**
     *  Remove the listener you previously added
     */
    private void removeListeners() {
        mLocationManager.removeUpdates(locationListener);
    }

    /**
     * OUT :: HELPERS
     */

}


/**
 * https://stackoverflow.com/questions/2272378/android-using-method-from-a-service-in-an-activity
 * <p>
 * https://stuff.mit.edu/afs/sipb/project/android/docs/training/basics/location/currentlocation.html
 * <p>
 * CLIENT EXAMPLE
 * <p>
 * public class Client extends Activity {
 * <p>
 * boolean mBounded;
 * LSLocationService mServer;
 * TextView text;
 * Button button;
 *
 * @Override public void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * setContentView(R.layout.main);
 * <p>
 * text = (TextView)findViewById(R.id.text);
 * button = (Button) findViewById(R.id.button);
 * button.setOnClickListener(new OnClickListener() {
 * <p>
 * public void onClick(View v) {
 * mServer.switchSpeaker(true);
 * }
 * });
 * <p>
 * }
 * @Override protected void onStart() {
 * super.onStart();
 * Intent mIntent = new Intent(this, LSLocationService.class);
 * bindService(mIntent, mConnection, BIND_AUTO_CREATE);
 * };
 * <p>
 * ServiceConnection mConnection = new ServiceConnection() {
 * <p>
 * public void onServiceDisconnected(ComponentName name) {
 * Toast.makeText(Client.this, "Service is disconnected", 1000).show();
 * mBounded = false;
 * mServer = null;
 * }
 * <p>
 * public void onServiceConnected(ComponentName name, IBinder service) {
 * Toast.makeText(Client.this, "Service is connected", 1000).show();
 * mBounded = true;
 * LocalBinder mLocalBinder = (LocalBinder)service;
 * mServer = mLocalBinder.getServerInstance();
 * }
 * };
 * @Override protected void onStop() {
 * super.onStop();
 * if(mBounded) {
 * unbindService(mConnection);
 * mBounded = false;
 * }
 * };
 * <p>
 * <p>
 * }
 */


        /*

               activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Sets a log object
                synchroLog = activity.findViewById(R.id.textLog);
                isUISyncLogEnabled = true;
                // https://stackoverflow.com/questions/13033052/pass-ui-controls-from-activity-to-a-class <<<---
                // https://stackoverflow.com/questions/6030982/how-to-access-activity-ui-from-my-class
                synchroLog.setText( "LOG4Gravity:IMPORT" ,TextView.BufferType.NORMAL);
                //synchroLog.setGravity(Gravity.TOP);
                synchroLog.setTextIsSelectable(true);
                // VERTICAL SCROLL
                //https://alvinalexander.com/source-code/android/android-edittext-isscrollcontainer-example-how-make-multiline-scrolling
                synchroLog.setVerticalScrollBarEnabled(true);
                synchroLog.setScrollContainer(true);
            }
        });
         */

