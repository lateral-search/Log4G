package com.ls.mobile.geotool.debug;

/**
 * GUIDE::
 * https://stackoverflow.com/questions/2630158/detect-application-heap-size-in-android/44644983#44644983
 * <p>
 * POST OF A ANDROID PLATFORM DEVELOPER::
 * GUIDE::
 * https://stackoverflow.com/questions/2298208/how-do-i-discover-memory-usage-of-my-application-in-android/2299813#2299813
 *
 * @deprecated
 */
public class DebugTools {


    /**
     * SEE:
     * https://stackoverflow.com/questions/32244851/androidjava-lang-outofmemoryerror-failed-to-allocate-a-23970828-byte-allocatio
     *     My problem solved after adding

     dexOptions {
     incremental true
     javaMaxHeapSize "4g"
     preDexLibraries true
     dexInProcess = true
     }

     in Build.Gradle file

     */


    /**
     * Getting the max heap size that the app can use:
     */
    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory();

    /**
     * Getting how much of the heap your app currently uses:
     */
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();


    /**
     * Getting how much of the heap your app can now use (available memory) :
     */
    long availableMemory = maxMemory - usedMemory;

    /**
     * To format each of them
     */
    //String formattedMemorySize= Formatter.formatShortFileSize(context,memorySize);


    /**
     * Some operations are quicker than java heap space manager.
     * Delaying operations for some time can free memory space.
     * You can use this method to escape heap size error
     */
    /* waitForGarbageCollector(new Runnable() {
        public void run() {
            // Your operations.
        }
    }); */

    /**
     * Measure used memory and give garbage collector time to free up some
     * space.
     * <p>
     * https://stackoverflow.com/questions/2298208/how-do-i-discover-memory-usage-of-my-application-in-android/2299813#2299813
     *
     * @param callback Callback operations to be done when memory is free.
     */
    public static void waitForGarbageCollector(final Runnable callback) {

        Runtime runtime;
        long maxMemory;
        long usedMemory;
        double availableMemoryPercentage = 1.0;
        final double MIN_AVAILABLE_MEMORY_PERCENTAGE = 0.1;
        final int DELAY_TIME = 5 * 1000;

        runtime =
                Runtime.getRuntime();

        maxMemory =
                runtime.maxMemory();

        usedMemory =
                runtime.totalMemory() -
                        runtime.freeMemory();

        availableMemoryPercentage =
                1 -
                        (double) usedMemory /
                                maxMemory;

        if (availableMemoryPercentage < MIN_AVAILABLE_MEMORY_PERCENTAGE) {

            try {
                Thread.sleep(DELAY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            waitForGarbageCollector(
                    callback);
        } else {

            // Memory resources are availavle, go to next operation:

            callback.run();
        }
    }

/*
    Runtime rt = Runtime.getRuntime();
            rt.maxMemory();
    //ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    //am.getMemoryClass();
*/


}
