package com.ls.mobile.geotool.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ls.mobile.geotool.R;
import com.ls.mobile.geotool.time.DateConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * DBTOOLS
 * <p>
 * What is the default database location of an android app for an unrooted device?? Is it same
 * as for rooted one
 * <p>
 * Yeah, For both the cases it will be same path. /data/data/<application_package_name>/databases
 * <p>
 * Now, on un-rooted device you can not access /data/ directory of device's internal storage.
 * That's why you can not seen the database file.
 * <p>
 * If you want to get the file you can copy database file from internal storage
 * /data/data/<application_package_name>/databases to external storage (sdcard) then using ddms
 * or adb pull get the database file.
 * <p>
 * Also just try command adb pull /data/data/<application_package_name>/databases/<database_file_name>
 * from your system to get the database file.
 * <p>
 * But by default all the android application store database on internal storage
 * path /data/data/<application_package_name>/databases.
 * And its applicable for all devices rooted or un-rooted.
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */

/**
 * CONSOLE RAPID REFERENCE GUIDE:
 * <p>
 * //Push to system app folder
 * adb push example.apk /system/app.
 * <p>
 * //Push to user app folder
 * adb push example.apk /data/app.
 * <p>
 * Maybe overwrite original app, so you had better use below command to backup
 * original app before operation.
 * <p>
 * // Pull android apk from device to local folder.
 * adb pull /system/app/example.apk    /user/app_bak
 * <p>
 * failed to copy '/user/example.apk' to '/system/app/example.apk': Read-only file system.
 * <p>
 * To resolve this problem, you need use adb install command with -r option to
 * force install the apk files, we will introduce it below.
 * <p>
 * // -r means force install.
 * adb install -r /user/example.apk
 * <p>
 * adb install C:/work/example.apk
 * adb uninstall <app package name>
 * <p>
 * adb shell
 * cd /data/app
 * rm com.dev2qa.example.apk
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public abstract class DBTools extends AppCompatActivity {

    private final static String LOG_TAG = DBTools.class.getSimpleName();

    /**
     * Audit log to log important events of the application into the database
     *
     * @param logTag            Name of the actual class
     * @param auditLogData      Data to be logged
     * @param appCompatActivity instance comming from the activity
     * @return
     */
    public static long insertAuditLog(String logTag,
                                      String location,
                                      String auditLogData,
                                      AppCompatActivity appCompatActivity) {
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(appCompatActivity,false);
        // IMPROVEMENT-5:: DB
        long r = 0;
        try {
            r = db.insertAuditLog(logTag, location, auditLogData);

        }finally{
            db.close();
        }
        return r;
    }

    /**
     * Audit log to log important events of the application into the database
     * for using inside a TX of the style:
     *
     *
     *  TransactionalDBHelper tx = new TransactionalDBHelper(context, true);
        tx.beginTransaction();

     tx.createPoint(pointAux);
     try {
     tx.endTransaction();
     tx.getDatabase().close();
     }catch (Exception e1){}
     *
     * @param logTag            Name of the actual class
     * @param auditLogData      Data to be logged
     * @param db                tx instance comming from the activity
     * @return
     */
    public static long insertAuditLogTx(String logTag,
                                        String location,
                                        String auditLogData,
                                        TransactionalDBHelper db
                                        ) {
        return db.getGravityMobileDBHelper().insertAuditLog(logTag, location, auditLogData);
    }

    // Helper method
    public static Bitmap getMiniBitmapForImageView(Bitmap bitmap) {
        Bitmap scaledPhoto = Bitmap.createScaledBitmap(bitmap, 72, 72, true);
        return scaledPhoto;
    }

    public static String getTimeForTransactonDate() {
        return new
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String getTimeForFileNameDate() {
        return new
                SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public static String getUTCTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        //SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
        // TO DO testear cambio de formato
        SimpleDateFormat f = new SimpleDateFormat(DateConverter.APP_DATE_FORMAT);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(cal.getTime());
    }

    // get picture from DB
    public Bitmap getPhotoFromBlob(Cursor cursor, int columnIndex) {
        if (cursor.getBlob(columnIndex) != null) {
            byte[] photo = cursor.getBlob(columnIndex);
            if (photo != null) {
                ByteArrayInputStream imageStream = new ByteArrayInputStream(photo);
                Bitmap thePicture = BitmapFactory.decodeStream(imageStream);
                //contact.setPicture(thePicture);
                return thePicture;
            }
        }
        return null;
    }

    // Save Photo to DB
    // It could be integrated on GBMobileDBHelper
    public static byte[] savePhoto(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] photo = baos.toByteArray();
        return photo;
    }

    /**
     * x
     * Read a file from APP directory.
     * <p>
     * All android app internal data files is saved in:
     * /data/data/<your app package name>
     * <p>
     * files folder — android.content.Context’s  getFilesDir() method can return
     * this folder. This folder is used to save general files.
     * cache folder — android.content.Context’s  getCacheDir() method can return
     * this folder. This folder is used to save cached files.
     * When device internal storage space is low, cache files will be
     * removed by android os automatically to make internal storage space bigger.
     * Generally you need to delete the unused cache files in code timely, totally cache
     * file size is better not more than 1 MB.
     *
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap readPictureFromDirectory(String fileName, Context ctx)
            throws FileNotFoundException {
        Bitmap b = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_test);
        return b;
    }

    /**
     * Save an Image to a directory.
     *
     * @param context
     * @param bitmap
     * @param name
     * @param extension
     */
    public void saveImage(Context context, Bitmap bitmap, String name, String extension) {
        name = name + "." + extension;
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------------------------------------------//
    //------------------------------- IN::DB Backcup Util ---------------------------------------//
    //-------------------------------------------------------------------------------------------//
    // GUIDES::
    // http://hxcaine.com/blog/2010/09/14/backing-up-importing-and-restoring-databases-on-android/
    //-------------------------------------------------------------------------------------------//

    /**
     * Directory which from files are to be read from and written to
     **/
    private static File DATABASE_DIRECTORY;
    // = new File(Environment.getExternalStorageDirectory(),"MyDirectory");

    /**
     * File path of Db to be imported
     **/
    protected static File IMPORT_FILE; //= new File(DATABASE_DIRECTORY,"MyDb.db");
    public static String PACKAGE_NAME; // = "com.example.app";
    public static String DATABASE_NAME; // = "example.db";
    //public static String DATABASE_TABLE = "entryTable";

    /**
     * SAVE
     * <p>
     * Saves the application database to the export directory under MyDb.db
     *
     * @param databaseDirectory: export directory, the place where to leave the database copy
     * @param oriDBName:         origin database name i.e.: gravity.db
     * @param packageName:       package name of the application i.e.: com.example.app
     * @param destDBName:        destination database anyName.db
     * @return true on success operation, false in case of fail
     */
    public static boolean exportDb(File databaseDirectory,
                                   String oriDBName,
                                   String packageName,
                                   String destDBName
    ) {

        DATABASE_DIRECTORY = databaseDirectory;
        IMPORT_FILE = new File(DATABASE_DIRECTORY, oriDBName);
        PACKAGE_NAME = packageName;//"com.example.app";
        DATABASE_NAME = oriDBName;//"example.db";

        // App in use DB directory is:
        // i.e.: /data/data/com.example.app/databases/example.db
        File dbFile =
                new File(Environment.getDataDirectory() +
                        "/data/" + PACKAGE_NAME +
                        "/databases/" + DATABASE_NAME);
        // db Journal file
        File dbFileJournal =
                new File(Environment.getDataDirectory() +
                        "/data/" + PACKAGE_NAME +
                        "/databases/" + DATABASE_NAME + "-journal");

        String filename = destDBName;
        String filenameJournal = destDBName + "-journal";

        // This is a: new File(Environment.getExternalStorageDirectory(),"MyDirectory");
        File exportDir = DATABASE_DIRECTORY;
        File file = new File(exportDir, filename);
        File fileJ = new File(exportDir, filenameJournal);

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        try {
            file.createNewFile();
            copyFile(dbFile, file);
            saveDBJournal(fileJ,dbFileJournal);//db-Journal
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void saveDBJournal(File fileJ,File dbFileJournal){
        try{fileJ.createNewFile();
            copyFile(dbFileJournal, fileJ);
        }catch(IOException ex){
            Log.e(LOG_TAG,"db-Journal could't be saved.");
        }
    }


    /**
     * DELETE DB - (previous DB version)
     *
     * @param ctx : Application context obtained from getApplicationContext()
     * @return true is success, false in the opposite case
     * <p>
     * Ref: https://www.quackit.com/sqlite/tutorial/drop_a_database.cfm
     */
    public static boolean deleteAppDataBase(Context ctx, String dataBaseName) {
        return ctx.deleteDatabase(dataBaseName);
    }

    /**
     * Not for Log4G
     *
     * Replaces current database with the IMPORT_FILE if
     * import database is valid and of the correct type
     **/
    protected static boolean restoreDb() {
        if (!sdIsPresent()) return false;

        // App in use DB directory
        // i.e.: /data/data/com.example.app/databases/example.db
        File exportFile =
                new File(Environment.getDataDirectory() +
                        "/data/" + PACKAGE_NAME +
                        "/databases/" + DATABASE_NAME);
        File importFile = IMPORT_FILE;

        if (!checkDbIsValid(importFile)) return false;

        if (!importFile.exists()) {
            Log.i(LOG_TAG, "File does not exist");
            return false;
        }

        try {
            exportFile.createNewFile();
            copyFile(importFile, exportFile);
            return true;
        } catch (IOException e) {
            Log.i(LOG_TAG, e.getMessage());
            return false;
        }
    }

    /**
     * Not for Log4G
     * <p>
     * Imports the file at IMPORT_FILE
     **/
    protected static boolean importIntoDb(Context ctx) {
        /*if( ! SdIsPresent() ) return false;

        File importFile = IMPORT_FILE;

        if( ! checkDbIsValid(importFile) ) return false;

        try{
            SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase
                    (importFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            Cursor cursor = sqlDb.query(true, DATABASE_TABLE,
                    null, null, null, null, null, null, null
            );

            DbAdapter dbAdapter = new DbAdapter(ctx);
            dbAdapter.open();

            final int titleColumn = cursor.getColumnIndexOrThrow("title");
            final int timestampColumn = cursor.getColumnIndexOrThrow("timestamp");

            // Adds all items in cursor to current database
            cursor.moveToPosition(-1);
            while(cursor.moveToNext()){
                dbAdapter.createQuote(
                        cursor.getString(titleColumn),
                        cursor.getString(timestampColumn)
                );
            }

            sqlDb.close();
            cursor.close();
            dbAdapter.close();
        } catch( Exception e ){
            e.printStackTrace();
            return false;
        }
        */
        return true;
    }

    /**
     * Not for Log4G
     * <p>
     * Given an SQLite database file, this checks if the file
     * is a valid SQLite database and that it contains all the
     * columns represented by DbAdapter.ALL_COLUMN_KEYS
     **/
    protected static boolean checkDbIsValid(File db) {
        /*try{
            SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase
                    (db.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            Cursor cursor = sqlDb.query(true, DATABASE_TABLE,
                    null, null, null, null, null, null, null
            );

            // ALL_COLUMN_KEYS should be an array of keys of essential columns.
            // Throws exception if any column is missing
            for( String s : DbAdapter.ALL_COLUMN_KEYS ){
                cursor.getColumnIndexOrThrow(s);
            }

            sqlDb.close();
            cursor.close();
        } catch( IllegalArgumentException e ) {
            Log.d(LOG_TAG, "Database valid but not the right type");
            e.printStackTrace();
            return false;
        } catch( SQLiteException e ) {
            Log.d(LOG_TAG, "Database file is invalid.");
            e.printStackTrace();
            return false;
        } catch( Exception e){
            Log.i(LOG_TAG, "checkDbIsValid encountered an exception");
            e.printStackTrace();
            return false;
        }
        */
        return true;
    }

    /**
     * Copy File
     *
     * @param src
     * @param dst
     * @throws IOException
     */
    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);

        } catch (Exception e) {
            Log.i(LOG_TAG, e.getMessage());

        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    /**
     * Returns whether an SD card is present and writable
     **/
    public static boolean sdIsPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    //----------------------------------------------------------------------------------------//
    //------------------------------- OUT::DB Backcup -----------------------------------------//
    //----------------------------------------------------------------------------------------//

}