package com.ls.mobile.geotool.common;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * LSContentProviderUtils
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 * @deprecated
 *
 */
public class LSContentProviderUtils {

    // Log
    private final static String LOG_TAG = LSContentProviderUtils.class.getSimpleName();

    /**
     * IN:: Verify that external storage is available
     * <p>
     * Because the external storage might be unavailable—such as
     * when the user has mounted the storage to a PC or has
     * removed the SD card that provides the external storage—you
     * should always verify that the volume is available before
     * accessing it. You can query the external storage state by
     * calling getExternalStorageState(). If the returned state is
     * MEDIA_MOUNTED, then you can read and write your files.
     * If it's MEDIA_MOUNTED_READ_ONLY, you can only read the files.
     *
     * @return
     */
    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    /**
     * OUT:: Verify that external storage is available
     */

    //------------------------------------------------------------//

    /**
     * Save to a public directory:
     * <p>
     * If you want to save public files on the external storage,
     * use the getExternalStoragePublicDirectory() method to get a File
     * representing the appropriate directory on the external storage.
     * The method takes an argument specifying the type of file you
     * want to save so that they can be logically organized with
     * other public files, such as Environment.DIRECTORY_MUSIC
     * or Environment.DIRECTORY_PICTURES
     *
     * @param publicFileType : Type of file obtained Environment.XXXXXXX
     * @param entityName     : Name of the file to be saved
     * @return file : an instance of File pointing to the directory
     *                created or referenced
     */
    public static File getPublicItemStorageDir(String entityName, String publicFileType) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                publicFileType), entityName);

        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    /**
     * Get an InputStream
     * Here is an example of how you can get an InputStream from the URI.
     * In this snippet, the lines of the file are being read into a string:
     *
     * @param uri
     * @param contentResolver
     * @return
     * @throws IOException
     */
    public static String readTextFromUri(Uri uri, ContentResolver contentResolver) throws IOException {
        InputStream inputStream = contentResolver.openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        reader.close();
        return stringBuilder.toString();
    }

    /**
     * Note that you should not do this operation on the UI thread. Do it in the background,
     * using AsyncTask. Once you open the bitmap, you can display it in an ImageView.
     *
     * @param uri
     * @param contentResolver
     * @return
     * @throws IOException
     */
    public static Bitmap getBitmapFromUri(Uri uri, ContentResolver contentResolver) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                contentResolver.openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    /**
     * Open a file for writing and append some text to it.
     */
    private static final int EDIT_REQUEST_CODE = 44;
    public static void editDocument(Activity activity, String filter) {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's
        // file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Example: Filter to show only text files.
        // intent.setType("text/plain");
        intent.setType(filter);

        activity.startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    /**
     *  Here are some examples of how you might call this method.
     *  The first parameter is the MIME type, and the second parameter is the name
     *  of the file you are creating:
     *
     *  createFile("text/plain", "foobar.txt");
     *  createFile("image/png", "mypicture.png");
     *  createFile("application/x-sqlite3", "database.db");
     */
    // Unique request code.
    private static final int WRITE_REQUEST_CODE = 43;

    private void createFile(String mimeType, String fileName,Activity activity) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        activity.startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

}