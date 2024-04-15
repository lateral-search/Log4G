package com.ls.mobile.geotool;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.ls.mobile.geotool.common.LSAndroidFileCRUDFacade;
import com.ls.mobile.geotool.common.Log4GravityUtils;
import com.ls.mobile.geotool.common.MessageDisplayerUtility;
import com.ls.mobile.geotool.db.DBTools;
import com.ls.mobile.geotool.db.GravityMobileDBHelper;
import com.ls.mobile.geotool.db.GravityMobileDBInterface;
import com.ls.mobile.geotool.sync.GravityDBSynchronizer;

import java.io.File;

/**
 * LOGS::
 * Log policy::
 * The order in terms of verbosity, from least to most is
 * ERROR, WARN, INFO, DEBUG, VERBOSE. Verbose should never be compiled into
 * an application except during development. Debug logs are compiled in but stripped
 * at runtime. Error, warning and info logs are always kept.
 *
 * add DEBUG logs everywhere:
 * if (BuildConfig.DEBUG) {
 *      Log.i(LOG_TAG, "enter-onCreate");
 * }
 *
 * <p>
 * <p>
 * chapter about prepare app for tablet, and READ monkey tests and etc at the end of the book
 * perform this tests
 * <p>
 * IMPORTANT!::
 * CAMERA Android Quick APIs Reference page 237 CAMERA
 * LOCATION Android Quick APIs Reference page 237 LOCATION
 * <p>
 * other projects Android Quick APIs Reference page 219 RECORDING
 * <p>
 * <p>
 * REFACTOR STATUS    PointStatusInterface
 * <p>
 * SYmc adapters
 * https://developer.android.com/training/sync-adapters/
 * <p>
 * Preguntas pendientes:
 * <p>
 * 1-  Se cargan puntos provenientes de mediciones historicas, pero: de cada uno de esos benchmarks,
 * no hay registro de cual gravimetro fue el utilizado.
 * Resp.: igualmente las mediciones no son utilizadas, solo los benchmarks son utilizados.
 * <p>
 */

/**
 * 1:syncrhonize----->>>
 */
public class SyncActivity extends AppCompatActivity {

    private GravityMobileDBHelper mDB;
    private String destBackupFolderName;
    //private static SyncActivity instance; // ISSUE MEMORY-LEAKS

    // IMPROVEMENT-2::synchronization steps status:
    protected boolean bckDBStatus;
    private boolean exportStatus;
    private boolean importStatus;


    Button syncBtn;
    ProgressBar progressBar;
    ProgressBar progressBar2;
    EditText textLog;

    // Log policy::
    // The order in terms of verbosity, from least to most is
    // ERROR, WARN, INFO, DEBUG, VERBOSE. Verbose should never be compiled into
    // an application except during development. Debug logs are compiled in but stripped
    // at runtime. Error, warning and info logs are always kept.
    private static final String LOG_TAG = SyncActivity.class.getSimpleName();

    /**
     * // ISSUE MEMORY-LEAKS
     * Singleton
     * This singleton is neccessary to provide an instance of the present class
     * to SyncAppTask class declared inside the present file
     *
     * @deprecated
     */
  //  public static SyncActivity singleton() {
        // Instance is always != null
        // because is initializiated in onCreate()
    //    return instance;
    //}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "enter-onCreate");
        }

        // IMPROVEMENT-2::synchronization steps status:
        bckDBStatus = false;
        exportStatus = false;
        importStatus = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        //instance = this;

        syncBtn = findViewById(R.id.syncBtn);
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                synchronize(arg0);
            }
        });
        // Circular progress bar
        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(20);
        // Time ellapsed progress bar
        progressBar2 = findViewById(R.id.progressBar2);

        textLog = findViewById(R.id.textLog);
    }

    /**
     * MAIN METHOD OF SYNCHRONIZATION PROCESS
     *
     * @param arg0
     * @return
     */
    private void synchronize(View arg0) {
        // ARE YOU SHURE?...DATA WILL BE BACKUPED AND DELETED
        String msg = "CONFIRMA SINCRONIZAR?: \n" +
                " \n" +
                "Se guardara un backup de la base de datos. \n" +
                "Se guardara un archivo .xls con las lineas resueltas.\n" +
                "Se guardaran las fotos. \n" +
                " \n" +
                "Los archivos estaran disponibles en: \n" +
                "I.Storage>Android>data>\n" +
                "com.ls.mobile.geotool>\n" +
                "files>YYYYMMDDhhmmssLOG4G-BCK\n" +
                " \n" +
                "NO podra sincronizar sin el archivo de importacion \n" +
                "con tablas de calibracion de gravimetros, y DB de puntos.\n" +
                " \n" +
                "ACEPTAR: ATENCION! exportara la sesion de trabajo, \n" +
                "y luego se borraran los datos para una nueva sesion.\n" +
                " \n" +
                "OMITIR: Puede presionar omitir y acceder directamente \n" +
                "a la ultima sesion de trabajo .\n";


        // guide:https://developer.android.com/guide/topics/ui/dialogs
        // ISSUE MEMORY-LEAKS use getApplicationContext() instead of .this
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.app_name);
        alertBuilder.setMessage(msg);
        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // IMPROVEMENT-2::synchronization steps status:
                bckDBStatus = false;
                exportStatus = false;
                importStatus = false;

                // IMPROVEMENT-3::Disable sync button to avoid double execution.
                syncBtn.setClickable(false);
                syncBtn.setEnabled(false);

                // Open file explorer pointing to SD memory directory
                chooseSyncInterfaceFile();
            }
        });
        alertBuilder.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Go to next Screen
                goToSetupActivity();
            }
        });
        // ISSUE-8 prevent accidental closing of some alerts
        alertBuilder.setCancelable(false);
        alertBuilder.create();
        alertBuilder.show();

        // Reminder:IMAGES DIR:
        ///data/data/com.ls.mobile.geotool/app_images

        // Reminder:DB DIR:

        // EXPORT INTERFACE:
        // /sdcard/Android/data/com.ls.mobile.geotool/files/fieldColectedObservs_20190323172637.xls
    }

    // ----------------- * FILE CHOSER::IN * --------------------------- //
    // ----------------- * FILE CHOSER::IN * --------------------------- //
    // ----------------- * FILE CHOSER::IN * --------------------------- //
    private static final int READ_REQUEST_CODE = 42;
    private Uri interfaceUri;

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     * <p>
     * https://developer.android.com/guide/topics/providers/document-provider
     */
    public void chooseSyncInterfaceFile() {
        // OPEN FILE PICKER (system's file browser.).
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // SRC: Internet
        // Filter to show only the file type MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // i.e.: it would be "*/*". or
        // i.e.: intent.setType("image/*");
        // MS Excel has the following observed MIME types::
        //    application/vnd.ms-excel (official)
        //    application/msexcel
        //    application/x-msexcel
        //    application/x-ms-excel
        //    application/x-excel
        //    application/x-dos_ms_excel
        //    application/xls
        //    application/x-xls
        //    application/vnd.openxmlformats-officedocument.spreadsheetml.sheet (xlsx)
        intent.setType("application/vnd.ms-excel");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * After the user selects a document in the picker, onActivityResult() gets called. The
     * resultData parameter contains the URI that points to the selected document. Extract the URI
     * using getData(). When you have it, you can use it to retrieve the document the user wants.
     * <p>
     * https://developer.android.com/guide/topics/providers/document-provider
     *
     * @param requestCode
     * @param resultCode
     * @param resultData
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            interfaceUri = null;

            if (resultData != null) {

                // URI corresponding to interface with data to LOAD
                // on LOG4G
                interfaceUri = resultData.getData();
                if (BuildConfig.DEBUG) {
                    if (interfaceUri != null) {
                        Log.i(LOG_TAG, "Uri: " + interfaceUri.toString());
                    } else Log.i(LOG_TAG, "Uri: NULL!");
                }

                // BACKUP LAST DATABASE
                if (backupLog4GDataBase()) {

                    //---------------------------------------------//
                    // RUN ON ACTIVITY THREAD::
                    // runOnUiThread: cause all procesess wich touch
                    // UI objects MUST run on activity Thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // THREAD:: WHERE SYNCHRONIZATION
                            // EXECUTION PROCESS STARTS
                            new SyncAppTask().execute();
                        }
                    });
                    //---------------------------------------------//
                }
            }
        }
    }
    // ----------------- * FILE CHOSER::OUT * ----------------- //
    // ----------------- * FILE CHOSER::OUT * ----------------- //
    // ----------------- * FILE CHOSER::OUT * ----------------- //

    /**
     * GUIDE:
     * https://developer.android.com/training/data-storage/files
     * <p>
     * backupDBToSynchDirNDrop DB.
     * GUIDE:
     * https://developer.android.com/guide/topics/data/backup
     * <p>
     * GUIDE:
     * https://developer.android.com/training/data-storage/files
     * <p>
     * Choose internal or external storage
     * <p>
     * All Android devices have two file storage areas: "internal" and "external" storage.
     * These names come from the early days of Android, when most devices offered built-in
     * non-volatile memory (internal storage), plus a removable storage medium such as a
     * micro SD card (external storage). Many devices now divide the permanent storage
     * space into separate "internal" and "external" partitions. So even without a removable
     * storage medium, these two storage spaces always exist, and the API behavior is the same
     * regardless of whether the external storage is removable.
     * <p>
     * Because the external storage might be removable, there are some differences between
     * these two options as follows.
     * <p>
     * Internal storage:
     * It's always available.
     * Files saved here are accessible by only your app.
     * When the user uninstalls your app, the system removes all your app's files from internal storage.
     * Internal storage is best when you want to be sure that neither the user nor other apps can
     * access your files.
     * <p>
     * External storage:
     * It's not always available, because the user can mount the external storage as USB storage
     * and in some cases remove it from the device.
     * It's world-readable, so files saved here may be read outside of your control.
     * When the user uninstalls your app, the system removes your app's files from here only
     * if you save them in the directory from getExternalFilesDir().
     * External storage is the best place for files that don't require access restrictions
     * and for files that you want to share with other apps or allow the user to
     * access with a computer.
     * <p>
     * Public files//Private files::
     * After you request storage permissions and verify that storage is available,
     * you can save two different types of files:
     * <p>
     * Public files: Files that should be freely available to other apps and to the user.
     * When the user uninstalls your app, these files should remain available to the user.
     * For example, photos captured by your app or other downloaded files should
     * be saved as public files.
     * <p>
     * Private files: Files that rightfully belong to your app and will be deleted when
     * the user uninstalls your app. Although these files are technically accessible by the
     * user and other apps because they are on the external storage, they don't provide
     * value to the user outside of your app.
     *
     * @return true if DB was copied, false if the opposite happends
     */
    public boolean backupLog4GDataBase() {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "backupDBToSynchDir");
        }

        // IMPROVEMENT-2::synchronization steps status (bckDBStatus):
        // Status Flag - Copy DB to a backup location
        // boolean copied = false;

        // APP SQLITE DATABASE PATH
        File dbPath = getApplicationContext().getDatabasePath(GravityMobileDBHelper.DATABASE_NAME);
        String aPath = dbPath.getAbsolutePath();
        // NAME OF THE COPY OF SQLITE DATABASE
        String destDBName = DBTools.getTimeForFileNameDate();
        // DESTINATION FOLDER NAME ( i.e.: 20190327150702LOG4G-BCK )
        destBackupFolderName = DBTools.getTimeForFileNameDate() + "LOG4G-BCK";
        // REGISTER DB SYNC ENTRY // Maybe usefull for other implementations
        GravityMobileDBHelper db = GravityMobileDBHelper.getInstance(getApplicationContext(),true);
        db.registerSyncExcecution(GravityMobileDBInterface.SYNC_STATUS_0K,
                "FILE_NAME",
                destBackupFolderName);

        // FILE POINTING TO DESTINATION FOLDER PATH
        File pathToDestBackupFolderName;
        try {
            pathToDestBackupFolderName =
                    LSAndroidFileCRUDFacade.getPublicStorageDir(this, null, destBackupFolderName);

           /* MAYBE USEFULL FOR OTHER IMPLEMENTATIONS:
            LogConfigurator configurator = new LogConfigurator();
            configurator.setFileName(Environment.getExternalStorageDirectory() + "/" + "myfile.txt");
            configurator.setRootLevel(Level.DEBUG);
            configurator.configure();

            Logger log = Logger.getLogger("MyApplication")
           */

            // Example params:
            // destBackupFolderName:: 20190326231458LOG4G-BCK
            // pathToDestBackupFolderName:: null
            // pathToSourceDB:: /data/user/0/com.ls.mobile.geotool/databases
            // destDatabaseName:: 20190326231459.db
            // IMPROVEMENT-2::synchronization steps status (bckDBStatus):
            bckDBStatus = DBTools.exportDb(pathToDestBackupFolderName,
                    GravityMobileDBHelper.DATABASE_NAME,
                    Log4GravityUtils.LOG4GRAVITY_PATH,
                    destDBName);

        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }

        if (bckDBStatus) { // IMPROVEMENT-2::synchronization steps status:
            // Display ALERT
            MessageDisplayerUtility.displaySimpleAlert(this, this.getString(R.string.sync_activity_msg1));
            return true;

        } else {
            // Display ALERT
            MessageDisplayerUtility.displaySimpleAlert(this, this.getString(R.string.sync_activity_msg2));
            //When application is executed for the first time ever since it was installed,
            //there will be an error cause DB will not be present. FOr this case
            // we can DO something for the case, checking if is the first running
            // of the app, etc. Or maybe we can write an empty db on the storage unit
            return true;

        }
    }

    /**
     * Go To Setup Activity screen.
     * It must be called after a correct synchronization
     */
    private void goToSetupActivity() {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
    }

    protected ProgressBar getTimeElapsedProgrBar() {
        return progressBar2;
    }

    /**
     * AsyncTasks: USEFUL WHEN WE DON'T WANT TO BLOCK THE UI
     * <p>
     * AsyncTasks should ideally be used for short operations (a few seconds at the most.) If you need
     * to keep threads running for long periods of time, it is highly recommended you use the various
     * APIs provided by the java.util.concurrent package such as Executor,
     * ThreadPoolExecutor and FutureTask.
     * <p>
     * The three types used by an asynchronous task are the following:
     * <p>
     * Params, the type of the parameters sent to the task upon execution.
     * Progress, the type of the progress units published during
     * the background computation.
     * Result, the type of the result of the background computation.
     * <p>
     * Not all types are always used by an asynchronous task. To mark a type as unused, simply use
     * the type Void:
     * <p>
     * private class MyTask extends AsyncTask<Void, Void, Void> { ... }
     * <p>
     * https://developer.android.com/reference/android/os/AsyncTask
     * https://stackoverflow.com/questions/18854060/how-to-implement-progressbar-while-loading-data?rq=1
     * https://www.journaldev.com/9708/android-asynctask-example-tutorial
     */
    class SyncAppTask extends AsyncTask<String, Integer, String> { //<PARAMS, PROGRESS, RESULT

        // Synchronizer object performs IMPORT and EXPORT from and to EXCEL file
        // And photo export to SD card
        GravityDBSynchronizer synchronizer;

        // Results
        private String resultMsg = "";

        // Log
        private final String LOG_TAG_SYNCAPPTASK = SyncAppTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            if (BuildConfig.DEBUG) {
                Log.i(LOG_TAG_SYNCAPPTASK, "onPreExecute");
            }

            getTimeElapsedProgrBar().setProgress(0);
            getTimeElapsedProgrBar().setMax(100);
            int progressbarstatus = 0;
            synchronizer = new GravityDBSynchronizer(SyncActivity.this);
        }

        @Override
        protected String doInBackground(String... params) {
            if (BuildConfig.DEBUG) {
                Log.i(LOG_TAG_SYNCAPPTASK, "doInBackground");
            }

            try {
                // Synchronizer object performs IMPORT and EXPORT from and to EXCEL file
                // And photo export to user backup LOg4G Directory
                // ISSUE MEMORY-LEAKS: was SyncActivity.singleton().destBackupFolderName
                GravityDBSynchronizer synchronizer = new GravityDBSynchronizer(SyncActivity.this,
                        SyncActivity.this.destBackupFolderName);


                // Enable events Log on UI
                synchronizer.setSynchLog();

                // SEE METHOD BELOW: onProgressUpdate(Integer... progress)
                // method can be invoked from doInBackground to publish updates on the UI
                // thread while the background computation is still running. Each call to
                // this method will trigger the execution of onProgressUpdate on the UI thread.
                // onProgressUpdate will not be called if the task has been canceled.
                publishProgress(15);

                /**-------------------------------//
                 * EXPORT DATA FROM LOG4G:
                 * -------------------------------//
                 * File output will be placed at:
                 * /sdcard/Android/data/com.ls.mobile.geotool/files/ (only if device has SD card)
                 * otherwise will be placed at:
                 * /Internal Storage/Android/data/com.ls.mobile.geotool/files/
                 * The name of the file will be  fieldColectedObservs_YYYYMMddHHmmss.xls
                 */
                resultMsg = synchronizer.exportDBDataToExcel();
                publishProgress(55);

                /**-----------------------------------//
                 * DELETE OLD DB
                 * A backup of the DB was made at
                 * the begining of sync, if export
                 * fails(this could be because an ilegal
                 * access of the DB. The backuped DB
                 * must be chequed to see what happends).
                 *-----------------------------------/*/
                // IMPROVEMENT-2::synchronization steps status:
                // If a backup of DB was performed successfully, we can delete the ori.
                // If the application is running for first time (no DB present), will
                // be not neccessary to delete anything.
                if(bckDBStatus) {
                    // ISSUE MEMORY-LEAKS: were using singleton() instead of .this
                    DBTools.deleteAppDataBase(SyncActivity.this.getApplicationContext(),
                            GravityMobileDBHelper.DATABASE_NAME);
                }
                /**--------------------------------//
                 * CREATE A NEW EMPTY DB
                 * Only Test data could be present
                 *--------------------------------/*/
                mDB = GravityMobileDBHelper.getInstance(SyncActivity.this,false);// ISSUE MEMORY-LEAKS
                mDB.createDatabase();

                /**------------------------------------------//
                 * IMPORT DATA TO LOG4G DB:
                 * ------------------------------------------//
                 * Data import file will be selected by hand
                 * named "basededatos.xls"
                 * PATH: /sdcard/basededatos.xls
                 */
                resultMsg = resultMsg + synchronizer.importBenchmarksAndGravimetersToDB(
                        getApplicationContext()
                        , interfaceUri.getLastPathSegment()
                        , interfaceUri);
                publishProgress(100);

            } catch (Exception e) {
                e.printStackTrace();
                Log.i(LOG_TAG_SYNCAPPTASK, "EXCEPTION!!@doInBackground() :: " + e.getMessage());
            }
            return "completed";
        }

        /**
         * @param progress
         */
        protected void onProgressUpdate(Integer... progress) {
            if (BuildConfig.DEBUG) {
                Log.i(LOG_TAG_SYNCAPPTASK, "onProgressUpdate::PERCENTAGE DONE: " + progress[0] + "%");
            }
            getTimeElapsedProgrBar().setProgress(progress[0]);
        }

        /**
         * @param result
         */
        protected void onPostExecute(String result) {
            if (BuildConfig.DEBUG) {
                Log.i(LOG_TAG_SYNCAPPTASK, "onPostExecute");
            }

            // Show Sync results and calculations
            // https://developer.android.com/guide/topics/ui/dialogs
            // ISSUE MEMORY-LEAKS: doesn't work using getApplicationContext()
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SyncActivity.this);
            alertBuilder.setTitle(R.string.app_name);
            alertBuilder.setMessage(resultMsg);
            /**
             * GO TO NEXT SCREEN
             */
            alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (synchronizer.isSynchronized()) {
                        // Go to next Screen
                        goToSetupActivity();
                    }
                }
            });
            // ISSUE-8 prevent accidental closing of some alerts
            alertBuilder.setCancelable(false);
            alertBuilder.create();
            alertBuilder.show();
        }



    }// SYNCH APP NESTED CLASS :: END

}// SyncActivity CLASS :: END

