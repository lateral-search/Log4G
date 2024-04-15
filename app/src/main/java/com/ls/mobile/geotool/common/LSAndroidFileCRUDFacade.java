package com.ls.mobile.geotool.common;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Utilities for data CRUD, operations,
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public class LSAndroidFileCRUDFacade {

    private final static String LOG_TAG = LSAndroidFileCRUDFacade.class.getSimpleName();

    //------------------------ CHECKS -------------------------//
    //------------------------ CHECKS -------------------------//

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    //------------------------ DELETE -------------------------//
    //------------------------ DELETE -------------------------//

    /**
     * https://developer.android.com/training/data-storage/files#java
     * <p>
     * Delete a file
     * <p>
     * You should always delete files that your app no longer need.
     * The most straightforward way to delete a file is to call delete() on the File object.
     * <p>
     * myFile.delete();
     * <p>
     * If the file is saved on internal storage, you can also ask the Context to
     * locate and delete a file by calling deleteFile():
     * <p>
     * myContext.deleteFile(fileName);
     * <p>
     * Note: When the user uninstalls your app, the Android system deletes the following:
     * All files you saved on internal storage.
     * All files you saved external storage using getExternalFilesDir().
     * However, you should manually delete all cached files created with getCacheDir()
     * on a regular basis and also regularly delete other files you no longer need.
     */
    public static boolean deleteFile(Context ctx, String fileName) {
        return ctx.deleteFile(fileName);
    }

    //------------------------GET A DIRECTORY-------------------------//
    //------------------------GET A DIRECTORY-------------------------//

    /**
     * Returns and instance of File, pointing to the custom user(App predefined)directory
     * where the user can take app files i.e.: from any export performed.
     *
     * @param ctx Context from Activity
     * @param environmentType a type from the well know list using Environment.DIRECTORY_PICTURES
     *                        or another one desired.
     *                        If none of the pre-defined sub-directory names suit your files, you
     *                        can instead call getExternalFilesDir() and pass null. This returns
     *                        the root directory for your app's private directory on the
     *                        external storage.
     * @param dirName         if doesn't exist it will be created
     *
     * @return an instance of file pointing to the directory desired
     */
    public static File getPublicStorageDir(Context ctx, String environmentType, String dirName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(ctx.getExternalFilesDir(
                environmentType), dirName);
        if (!file.mkdirs()) {
            //Log.i(LOG_TAG, "Directory not created");
        }
        return file;
    }

    //------------------------ CREATE -------------------------//
    //------------------------ CREATE -------------------------//

    /**
     * To create a new file in one of these directories, you can use the File() constructor,
     * passing the File provided by one of the above methods that specifies your internal
     * storage directory.
     * For example:
     * <p>
     * File file = new File(context.getFilesDir(), filename);
     */
    public static File createFile(Context ctx, String fileName) {
        File file = new File(ctx.getFilesDir(), fileName);
        return file;
    }

    //------------------------ WRITE -------------------------//
    //------------------------ WRITE -------------------------//

    /**
     * Open an existing file:
     * To read an existing file, call openFileInput(name), passing the name of the file.
     * You can get an array of all your app's file names by calling fileList().
     * <p>
     * Write a file:
     * When saving a file to internal storage, you can acquire the appropriate directory as a
     * File by calling one of two methods:
     * <p>
     * getFilesDir()
     * Returns a File representing an internal directory for your app.
     * getCacheDir()
     * Returns a File representing an internal directory for your app's temporary cache files.
     * Be sure to delete each file once it is no longer needed and implement a reasonable size
     * limit for the amount of memory you use at any given time, such as 1MB.
     * <p>
     * Caution: If the system runs low on storage, it may delete your cache files without warning.
     */
    public static boolean writeFile(Context ctx, String filename, String fileContents) {
        FileOutputStream outputStream;

        try {
            outputStream = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}