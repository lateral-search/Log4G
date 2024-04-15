package com.ls.mobile.geotool.gps;

import android.location.Location;

/**
 * Don;t use this class, use the Locator comming with JAVA
 *
 * @deprecated
 */
public class GPSDistanceBtwnCoordsCalculator {

    private static final String LOG_TAG = GPSDistanceBtwnCoordsCalculator.class.getSimpleName();
        /**
         * Uses the Haversine formula to calculate the distance (meters) between to lat-long coordinates
         *
         * @param latitude1  The first point's latitude
         * @param longitude1 The first point's longitude
         * @param latitude2  The second point's latitude
         * @param longitude2 The second point's longitude
         * @return The distance between the two points in meters
         */
        public static double calculateDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        /*
            Haversine formula:
            A = sinÂ²(Î”lat/2) + cos(lat1).cos(lat2).sinÂ²(Î”long/2)
            C = 2.atan2(âˆša, âˆš(1âˆ’a))
            D = R.c
            R = radius of earth, 6371 km.
            All angles are in radians
            */

            double deltaLatitude = Math.toRadians(Math.abs(latitude1 - latitude2));
            double deltaLongitude = Math.toRadians(Math.abs(longitude1 - longitude2));
            double latitude1Rad = Math.toRadians(latitude1);
            double latitude2Rad = Math.toRadians(latitude2);

            double a = Math.pow(Math.sin(deltaLatitude / 2), 2) +
                    (Math.cos(latitude1Rad) * Math.cos(latitude2Rad) * Math.pow(Math.sin(deltaLongitude / 2), 2));

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return 6371 * c * 1000; //Distance in meters

        }

        /**
         * Converts given meters/second to nautical mile/hour.
         *
         * @param mps meters per second
         * @return knots
         */
        public static double mpsToKnots(double mps) {
            // Google "meters per second to knots"
            return mps * 1.94384449;
        }

        /**
         * Checks bundle in the Location object for satellties used in fix.
         * @param loc The location object to query
         * @return satellites used in fix, or 0 if no value found.
         */
        public static int getBundledSatelliteCount(Location loc){
            int sat = 0;

            if(loc.getExtras() != null){
                sat = loc.getExtras().getInt("satellites",0);

                if (sat == 0) {
                    //Provider gave us nothing, let's look at our bundled count
                    sat = loc.getExtras().getInt("SATELLITES_FIX", 0);

                }
            }

            return sat;
        }


    }


/*

 Here is source code of the Program to Calculate Distance between two points using Latitude & Longitude in Android. The program is successfully compiled and run on a Windows system using Eclipse Ide. The program output is also shown below.

Main Activity

package com.example.changeiamge;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.renderscript.Type;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnCheckedChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText lat_dest = (EditText) findViewById(R.id.dest_latitude);
        EditText lon_dest = (EditText) findViewById(R.id.dest_lon);

        Button but1 = (Button) findViewById(R.id.button1);
        but1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Auto-generated method stub

                               Location mylocation = new Location("");
                                  Location dest_location = new Location("");
                               String lat = lat_dest.getText().toString();
                       String lon = lon_dest.getText().toString();
                               dest_location.setLatitude(Double.parseDouble(lat));
                       dest_location.setLongitude(Double.parseDouble(lon));
                               Double my_loc = 0.00;
                               mylocation.setLatitude(my_loc);
                       mylocation.setLongitude(my_loc);
                               Double distance = mylocation.distanceTo(dest_location);//in meters
                               Toast.makeText(this, "Distance"+Double.toString(distance),
                        Toast.LENGTH_LONG).show();
            }
        });

    }
}
 */