package com.ls.mobile.geotool.gps;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Iterator;

/**
 * GPSManager:
 *
 * @deprecated
 */
public class GPSManager {

    private final Context mContext;
    private final LocationManager locationManager;


    // Log
    private static final String LOG_TAG = GPSManager.class.getSimpleName();

    /**
     * Constructor
     */
    public GPSManager(LocationManager lMngr, Context ctx) {
        locationManager = lMngr;
        mContext = ctx;
    }

    /**
     * GpsStatus
     * Retrieves information about the current status of the GPS engine.
     * This should only be called from the GpsStatus.Listener.onGpsStatusChanged
     * callback to ensure that the data is copied atomically. The caller may either pass
     * in a GpsStatus object to set with the latest status information, or pass null to
     * create a new GpsStatus object.
     Parameters:
     status - object containing GPS status details, or null.
     Returns:
     status object containing updated GPS status.
     *
     *
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public int getSatellites() {


        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return null;
        }
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        int count = 0;
        if (gpsStatus != null) {
            Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
            Iterator<GpsSatellite> sat = satellites.iterator();

            int i = 0;
            String strGpsStats="";

            while (sat.hasNext()) {
                count++;
                GpsSatellite satellite = sat.next();
                strGpsStats += (i++) + ": " + satellite.getPrn() +
                                 "," + satellite.usedInFix() + "," +
                                       satellite.getSnr() +
                                 "," + satellite.getAzimuth() +
                                 "," + satellite.getElevation() + "\n\n";

                Log.i("value:" + "-", strGpsStats += (i++) + ": " + satellite.getPrn() + ","
                               + satellite.usedInFix() + ","
                               + satellite.getSnr() + ","
                               + satellite.getAzimuth() + ","
                               + satellite.getElevation() + "\n\n");
            }
            //tv.setText(strGpsStats);
            Log.v("satellite", "satellite " + count);
        }

        return count;
    }


}// End class GPSManager



