package com.ls.mobile.geotool.common;

import android.content.Context;

import java.io.File;

/**
 * Statics helpers to simplify code understanding.
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public class Log4GravityUtils {

    public static final String LOG4GRAVITY_PATH = "com.ls.mobile.geotool";

    /**
     * Returns the path defined to make applications exports or
     * other files that will be accessed by the user.
     *
     * @param destDirectoryName
     *
     * @return
     */
    public static File getCustomStorageDir(Context ctx, String destDirectoryName) {
        // FILE POINTING TO DESTINATION FOLDER PATH
        File pathToDestBackupFolderName =
                LSAndroidFileCRUDFacade.getPublicStorageDir(ctx,null,destDirectoryName);
        return pathToDestBackupFolderName;
    }



    /**
     * Helper method to build image file name Log4Gravity
     *
     * @param date
     * @param lineName
     * @param pointCode
     * @param oneWayValue
     * @return
     */
    public static String imageNameBuilder(String date,
                                          String lineName,
                                          String pointCode,
                                          int oneWayValue) {
        return date
                + "-Ln_" + lineName
                + "-Pnt_" + pointCode
                + "-Dir_" + oneWayValue;
    }


}
