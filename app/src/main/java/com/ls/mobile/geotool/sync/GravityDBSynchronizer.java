package com.ls.mobile.geotool.sync;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.ls.mobile.geotool.BuildConfig;
import com.ls.mobile.geotool.R;
import com.ls.mobile.geotool.common.ImageFileLoaderAndSaver;
import com.ls.mobile.geotool.common.Log4GravityUtils;
import com.ls.mobile.geotool.db.DBTools;
import com.ls.mobile.geotool.db.GravityMobileDBHelper;
import com.ls.mobile.geotool.db.GravityMobileDBInterface;
import com.ls.mobile.geotool.workflow.PointStatusInterface;
import com.ls.mobile.geotool.db.data.model.Calibration;
import com.ls.mobile.geotool.db.data.model.Gravimeter;
import com.ls.mobile.geotool.db.data.model.Line;
import com.ls.mobile.geotool.db.data.model.Point;
import com.ls.mobile.geotool.db.data.model.User;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;
import java.util.List;

/**
 * IMPORT::
 * --------
 * Updates the Gravity mobile App DB with data stored in the HOST or MASTER
 * computer.
 * <p>
 * EXPORT::
 * --------
 * Generates the export interface with the gravity data observed in the field, ready
 * to be uploaded to the MASTER computer.
 * <p>
 * EXCEL FILE GENERATION EXAMPLE::
 * <p>
 * // Cell style for header row
 * // CellStyle cs = writer.getWorkbook().createCellStyle();
 * // cs.setFillForegroundColor(HSSFColor.LIME.index);
 * // cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
 * <p>
 * <p>
 * //Cell c = null;
 * <p>
 * // Cell style for header row
 * //CellStyle cs = writer.getWorkbook().createCellStyle();
 * //cs.setFillForegroundColor(HSSFColor.LIME.index);
 * //cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
 * <p>
 * // New Sheet
 * /*Sheet sheet1 = null;
 * sheet1 = writer.getWorkbook().createSheet("myOrder");
 * <p>
 * // Generate column headings
 * Row row = sheet1.createRow(0);
 * <p>
 * c = row.createCell(0);
 * c.setCellValue("Item Number");
 * //c.setCellStyle(cs);
 * <p>
 * c = row.createCell(1);
 * c.setCellValue("Quantity");
 * //c.setCellStyle(cs);
 * <p>
 * c = row.createCell(2);
 * c.setCellValue("Price");
 * //c.setCellStyle(cs);
 * <p>
 * sheet1.setColumnWidth(0, (15 * 500));
 * sheet1.setColumnWidth(1, (15 * 500));
 * sheet1.setColumnWidth(2, (15 * 500));
 * <p>
 * ------------------------------------------------------------------//
 * <p>
 * IMPORT:
 * -------
 * <p>
 * Import data will be read from a file called:
 * <p>
 * /sdcard/basededatos.xls
 * <p>
 * This file must be selected by the user in Android file explorer
 * <p>
 * EXPORT:
 * -------
 * <p>
 * An export excel.xls file containing:
 * <p>
 * a) A LINE-STRUCT (1 tab)
 * b) Many LINES tabs (containing POINTS grouped by LINES)
 * <p>
 * Will be leaved in the path:
 * /sdcard/Android/data/com.ls.mobile.geotool/files/
 * <p>
 * The name of the export file will be:
 * fieldColectedObservs_YYYYMMDDhhmmss.xls
 * <p>
 * where YYYYMMDDhhmmss will be:
 * YYYY: current year
 * MM:   current month
 * DD:   current day
 * hh:   current hour
 * mm:   current minuyrd
 * ss:   seconds
 * <p>
 * A third batch export leaves the pictures taken by the operator in
 * the following path:
 * <p>
 * /data/data/com.ls.mobile.geotool/app_images/
 * <p>
 * The name of each file will be like:
 * <p>
 * 2018-06-12 11:11:11-Ln_LINE-1-Pnt_POINT-1-Dir_0G1
 * <p>
 * where:
 * Ln: line name
 * Pnt: benchmark name-1-
 * Dir: direction (1 forward 0 return)
 * Gx: gx (where 1 <= x <= 3)
 */

/**
 * CHECK MAIL:
 * <p>
 * <p>
 * Esta estructura está bien, pero como te comenté arriba, no hay que
 * combinarla con la db que te pasé sino que más bien vendría en un
 * "archivo separado", como en line_struct.xls. Más allá de ese detalle,
 * los campos están bien hasta la columna "offset". Estaría bueno agregar
 * la columna reading (el promedio de g1, g2, g3) y reduced_g (la gravedad
 * reducida usando la formula que te había dado cuando nos reunimos la
 * primera vez, que hace falta para el chequeo de consistencia).
 * <p>
 * 1-) Los campos offset, absolute_g, uncertainty los agrego en la tabla de
 * la base de datos que tiene la aplicacion mobile?
 * <p>
 * Offset si, porque eso se carga en el campo para identificar
 * el offset entre la medición de gravedad y la marca GPS precisa.
 * Absolute_g y uncertainty no hacen falta.
 * <p>
 * 2-)   Sobre absolute_g: PREGUNTA: el tema de la gravedad absoluta,
 * lo hablamos, pero creo que al final no lo definimos, segun entiendo,
 * las medidas de gravedad absoluta "absolute_g", van a necesitar agregarlas
 * en ciertos puntos durante el trabajo de campo, entonces habria que
 * agregar un campo editable de gravedad absoluta en la pantalla de
 * carga de datos en un punto, era asi efectivamente?
 * <p>
 * No, el valor de gravedad absoluta no es necesario para el trabajo de
 * campo. Solamente se usa para el ajuste final. Te lo mandé porque
 * estaba en la estructura, pero podés ignorarlo tranquilo :)
 */

public class GravityDBSynchronizer {

    private static Activity activity;
    private static String destBackupFolderName;
    private static EditText synchroLog;

    // Synchronization steps
    private static boolean isImportBenchmrksExecOk = true;
    private static boolean isImportGravimetersExecOk = true;
    private static boolean isExportDataExecOk = true;
    private static boolean isSavedPhotosExecOk = true;
    // Synchronization final Result
    private static boolean synchrnzd = false;

    // Processed images counter
    private ImageProcessedCounter counter = new ImageProcessedCounter();

    // UI Logger Flag
    private static boolean isUISyncLogEnabled = false;

    // Header for exported data file .xls
    private final static String[] header = {
            "row","line", "benchmark", "instrument", "direction", "epoch"
            , "timestamp", "year", "month", "day", "hour", "minute"
            , "raw_data_1", "raw_data_2", "raw_data_3", "reading"
            , "reduced_g", "line status", "user" };
    // Log
    private static final String LOG_TAG = GravityDBSynchronizer.class.getSimpleName();

    /**
     * Constructor
     *
     * @param act // Activity is neccesary to get UI elements
     */
    public GravityDBSynchronizer(Activity act) {
        activity = act;
    }

    /**
     * Constructor
     *
     * @param act // Activity is neccesary to get UI elements
     */
    public GravityDBSynchronizer(Activity act,
                                 String destBackupFolderName_) {
        activity = act;
        destBackupFolderName = destBackupFolderName_;

    }

    /**
     * Log App synchronization events
     */
    public void setSynchLog() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Sets a log object
                synchroLog = activity.findViewById(R.id.textLog);
                isUISyncLogEnabled = true;
                // guide::https://stackoverflow.com/questions/13033052/pass-ui-controls-from-activity-to-a-class <<<---
                // guide::https://stackoverflow.com/questions/6030982/how-to-access-activity-ui-from-my-class
                synchroLog.setText("LOG4Gravity:IMPORT", TextView.BufferType.NORMAL);
                //synchroLog.setGravity(Gravity.VERTICAL_GRAVITY_MASK);
                synchroLog.setTextIsSelectable(true);
                // VERTICAL SCROLL
                // guide::https://alvinalexander.com/source-code/android/android-edittext-isscrollcontainer-example-how-make-multiline-scrolling
                synchroLog.setVerticalScrollBarEnabled(true);
                synchroLog.setScrollContainer(true);
                //synchroLog.setOverScrollMode(View.OVER_SCROLL_ALWAYS);

                /* UI log Auto-Scroll not working but adding a new thread hangs it
                synchroLog.setOnScrollChangeListener();OR THIS ONE???
                synchroLog.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        Log.i(LOG_TAG,"onTextChanged*****-**-*-*-");

                        synchroLog.scrollBy(0,100);

                    }
                }); */

            }// END run()
        });
    }

    //---------------------- IMPORT::IN ------------------------------//
    //---------------------- IMPORT::IN ------------------------------//
    //---------------------- IMPORT::IN ------------------------------//

    /**
     * Import all benchmarks from the matlab MASTER Application
     * (1st stage of synchronization process)
     *
     * @param context
     * @param filename
     * @param uri
     * @return
     */
    public String importBenchmarksAndGravimetersToDB(Context context, String filename, Uri uri) {
        if (BuildConfig.DEBUG) {
            Log.v(LOG_TAG, "METHOD:exportDB");
        }

        ExcelFileReader fileReader = new ExcelFileReader(context, filename, uri);
        HSSFWorkbook hssfWorkbook = fileReader.getWorkbook();

        // Interface uses only one sheet to store points
        HSSFSheet sheet = hssfWorkbook.getSheetAt(0);

        // Iterate through the cells.
        Iterator rowIter = sheet.rowIterator();

        // First row is a header
        rowIter.next();

        // In case of upload error, we can make a UI alert
        // informing wich row has the problem.
        int rowCounter = 1;

        // IMPROVEMENT-1::synchronization TX: is not neccessary here.

        // IN::while(rowIter.hasNext())
        while (rowIter.hasNext()) {

            HSSFRow myRow = (HSSFRow) rowIter.next();
            Iterator<Cell> cellIter = myRow.cellIterator();
            //----------------------------
            // basededatos.xls:: file:
            // Interface Row CELL NAMES
            //  COLUMN(x)  NAME
            //----------------------------
            //   0         name
            //   1         lat
            //   2         lon
            //   3         height
            //   4         offset
            //   5,6,7     x,y,z
            //   8         absolute_g
            //   9         uncertainty
            //----------------------------

            try {
                // Build the Point to Save
                Point pointAux = new Point();

                //Log.v(LOG_TAG,"See code on Cell class: "
                //      + String.valueOf(cellIter.next().getCellType()));

                // This order cant be changed, cause is an array build from interface data
                // there is no data extraction using keys. The system just read a sequence
                // from the interface, and data must be ordered in proper order cause thats the
                // file format(same used in matlab).
                pointAux.setCode((cellIter.next()).getStringCellValue());         // 0
                pointAux.setLatitude((cellIter.next()).getNumericCellValue());    // 1
                pointAux.setLongitude((cellIter.next()).getNumericCellValue());   // 2
                pointAux.setHeight((cellIter.next()).getNumericCellValue());      // 3
                // SEE:Demian MAIL for why just previous values must be imported
                //pointAux.setOffset((double)cellIter.next());                    // 4
                //pointAux.setX((double)cellIter.next());                         // 5,6,7
                //pointAux.setAbsoluteG((double)cellIter.next());                 // 8
                //pointAux.setAbsoluteG((double)cellIter.next());                 // 9

                // Save POINT (benchmark import)
                // IN::ISSUE-PROD-0-20220301
                GravityMobileDBHelper mDB = GravityMobileDBHelper.getInstance(context, true);
                // OUT::ISSUE-PROD-0-20220301
                // IMPROVEMENT-5:: DB
                long res = -1;
                try {
                    res = mDB.createPoint(pointAux);
                }catch(Exception e){
                    Log.d(LOG_TAG, "ERROR:", e.fillInStackTrace());
                }finally{
                    mDB.close();
                }

                // UI Log
                if (isUISyncLogEnabled) {
                    logRow("ROW-BENCHMARK IMPORTED::" + myRow.toString());
                }

                // Log
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "ROW SAVED:: " + myRow.toString());
                }

                rowCounter++;

            } catch (Exception e) {

                // Set Error state
                isImportBenchmrksExecOk = false;
                e.printStackTrace();

                // UI Log
                if (isUISyncLogEnabled) {
                    logRow("EXCEPTION WHILE IMPORTING ROW: "
                            + rowCounter + " \n"
                            + e.getMessage());
                }
                Log.e(LOG_TAG, e.getMessage());

                return activity.getString(R.string.synchrnzrMsg1)
                        + rowCounter
                        + activity.getString(R.string.synchrnzrMsg2)
                        + activity.getString(R.string.synchrnzrMsg3)
                        + activity.getString(R.string.synchrnzrMsg4)
                        + activity.getString(R.string.synchrnzrMsg5);
            }

        }// OUT::while(rowIter.hasNext())

        /** ---- Call IMPORT GRAVIMETERS ---- */
        String gravimetersImportRes = importGravimetersToDB(hssfWorkbook);
        String di = activity.getString(R.string.synchrnzrDataImp);
        String resMsg = activity.getString(R.string.synchrnzrMsg6);

        return  di +  rowCounter + resMsg + gravimetersImportRes + " \n";

    }

    /**
     * Import gravimeters calibrations tables
     * (2nd stage of synchronization process)
     *
     * @param hssfWorkbook
     * @return a String containing a result message
     */
    private String importGravimetersToDB(HSSFWorkbook hssfWorkbook) {
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "METHOD:importDBGravimeters");
        }

        // Interface uses one sheet for each gravimeter
        int numberOfSheets = hssfWorkbook.getNumberOfSheets();
        int i;

        /** --------------------- */
        /** - for iteration :: IN */
        /** --------------------- */
        // counter starts in i = 1 cause Sheet(0) was processed
        for (i = 1; i < numberOfSheets; i++) {
            HSSFSheet sheet = hssfWorkbook.getSheetAt(i);

            // We now need something to iterate through the cells.
            Iterator rowIter = sheet.rowIterator();

            // GRAVIMETER :: Build a Gravimeter to Save
            Gravimeter gravimeterAux = new Gravimeter();
            gravimeterAux.setName(sheet.getSheetName());

            if (BuildConfig.DEBUG) {
                Log.i(LOG_TAG, "Sheet NAME:: " + sheet.getSheetName());
            }

            // Save Gravimeter
            // IN::ISSUE-PROD-0-20220301
            GravityMobileDBHelper dbHelper = GravityMobileDBHelper.getInstance(activity.getApplicationContext(),false);
            // OUT::ISSUE-PROD-0-20220301
            // IMPROVEMENT-5:: DB
            long gravimeterId = -1;
            try {
                gravimeterId = dbHelper.createGravimeter(gravimeterAux);
            }catch(Exception e){
                Log.d(LOG_TAG, "ERROR:", e.fillInStackTrace());
            }finally{
                dbHelper.close();
            }

            // In case of upload error, we can make an UI alert informing wich row has the problem.
            int rowCounter = 1;

            // UI Log
            if (isUISyncLogEnabled) {
                logRow("IMPORTING GRAVIMETER::" + sheet.getSheetName());
            }

            /** IN :: WHILE(rowIter.hasNext()) */
            while (rowIter.hasNext()) {

                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator<Cell> cellIter = myRow.cellIterator();

                try {
                    // CALIBRATION :: Build the Calibration to Save
                    Calibration calibrationAux = new Calibration();

                    //Log.v(LOG_TAG,"See code on Cell class: "
                    //      + String.valueOf(cellIter.next().getCellType()));

                    calibrationAux.setGravimeterId((int) gravimeterId);

                    if (BuildConfig.DEBUG) {
                        Log.i(LOG_TAG, "gravimeterId:: " + (int) gravimeterId);
                    }

                    // Process calibration value in case it comes in a String Cell type
                    Cell cellAux = cellIter.next();
                    double calibrationValue;

                    if (cellAux.getCellType() == Cell.CELL_TYPE_STRING) {
                        calibrationValue = Double.valueOf(cellAux.getStringCellValue());
                    } else {
                        calibrationValue = (double) cellAux.getNumericCellValue();
                    }

                    calibrationAux.setCalibrationValue(calibrationValue);
                    if (BuildConfig.DEBUG) {
                        Log.i(LOG_TAG, "CalibrationValue:: " + calibrationValue);
                    }

                    calibrationAux.setCalibrationValueIndex(rowCounter);
                    if (BuildConfig.DEBUG) {
                        Log.i(LOG_TAG, "CalibrationValueIndex:: " + rowCounter);
                    }

                    // Save each Calibration value and index
                    // IN::ISSUE-PROD-0-20220301
                    GravityMobileDBHelper mDB = GravityMobileDBHelper.getInstance(activity.getApplicationContext(), true);
                    // OUT::ISSUE-PROD-0-20220301
                    mDB.createCalibration(calibrationAux);

                    // UI Log
                    if (isUISyncLogEnabled) {
                        logRow("ROW-CALIBRATION IMPORTED::" + myRow.toString());
                    }
                    if (BuildConfig.DEBUG) {
                        Log.i(LOG_TAG, "ROW SAVED:: " + myRow.toString());
                    }

                    rowCounter++;

                } catch (Exception e) {
                    // Set Error state
                    isImportGravimetersExecOk = false;
                    e.printStackTrace();
                    // UI Log
                    if (isUISyncLogEnabled) {
                        logRow("EXCEPTION WHILE IMPORTING ROW: "
                                + rowCounter + " \n"
                                + e.getMessage());
                    }
                    Log.e(LOG_TAG, e.getMessage());
                    return "Tambien se producjo un ERROR al leer la FILA numero: " + rowCounter
                            + " del archivo gravimeters.xml. Por favor revise los datos del archivo. \n"
                            + "La sincronizacion se efectuo en forma inconsistente, y la aplicacion NO "
                            + "podra ser utilizada hasta que se corrija basededatos.xls  \n"
                            + "EXCEPTION: \n" + e.getLocalizedMessage();
                }
            }
            /** OUT :: WHILE(rowIter.hasNext()) */

        }
        /** ------------------------ */
        /** - for iteration :: OUT - */
        /** ------------------------ */

        // i18n
        int qSheets = numberOfSheets - 1;
        return qSheets + " :Tablas de gravimetros importadas.\n";

    }
    //---------------------- IMPORT::OUT ------------------------------//
    //---------------------- IMPORT::OUT ------------------------------//
    //---------------------- IMPORT::OUT ------------------------------//


    //---------------------- EXPORT::IN------------------------------//
    //---------------------- EXPORT::IN------------------------------//
    //---------------------- EXPORT::IN------------------------------//

    /**
     * OBSERVATIONS:
     * ------------
     * <p>
     * Output example of an fieldColectedObservs_yyyyMMddHHMMss.xls :
     * <p>
     * CL18
     * 2018-06-09 22:45
     * -19,1710255
     * -63,0588611
     * 1974,890
     * 1974,890
     * 1974,890
     * 0
     * 47
     * <p>
     * Every sheet will be named with the gravimeter used in the field work
     * <p>
     * LINE STRUCT:
     * -----------
     * The first sheet will be LINE STRUCT.
     * <p>
     * benchmark   instrument  direction  epoch	        timestamp	    year	month day hour
     * POINT NAME  g1024	   '1'   	  2456084,21875	6/5/2012 17:15	2012	6	  5   17
     * <p>
     * minute	raw_data_1	raw_data_2	raw_data_3	reading	         reduced_g
     * 15	    1509,329	1509,33	    1509,329	1509,3293333333	 1543,1984733256
     */
    public String exportDBDataToExcel() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "ENTER METHOD::exportDBDataToExcel");
        }

        // UI Log
        if (isUISyncLogEnabled) {
            logRow("LOG4Gravity:EXPORT");
        }

        ExcelFileWriter writer = new ExcelFileWriter();
        writer.setWorkbook(new HSSFWorkbook());

        // First Sheet LINE-STRUCT
        String result = "";
        result = buildLineStruct(writer);

        // Second to N Sheets OBSERVATIONS
        result = result + buildExportObservationsSheet(writer);

        // Save exported data
        String date = DBTools.getTimeForFileNameDate();
        String ObservsfileName = "fieldColectedObservs_" + date + ".xls";
        boolean saveSuccess = writer.saveFileToCustomDirectory(activity.getApplicationContext(),
                ObservsfileName,
                destBackupFolderName);

        // UI Log
        if (isUISyncLogEnabled) {
            if (saveSuccess) {
                logRow("CREATED EXPORT FILE: " + ObservsfileName);
            } else {
                logRow("ERROR!!! EXPORT FILE COULDNT BE CREATED");
            }
        }

        return "DATA EXPORT: \n" + result;
    }

    /**
     * LINE STRUCT:
     * -----------
     * The first sheet will be LINE STRUCT.
     * <p>
     * benchmark   instrument  direction  epoch	        timestamp	    year	month day hour
     * POINT NAME  g1024	   '1'   	  2456084,21875	6/5/2012 17:15	2012	6	  5   17
     * <p>
     * minute	raw_data_1	raw_data_2	raw_data_3	reading	         reduced_g
     * 15	    1509,329	1509,33	    1509,329	1509,3293333333	 1543,1984733256
     *
     * @return
     */
    private String buildLineStruct(ExcelFileWriter excelWriter) {

        String resultMsg = "";

        // Create LINE-STRUCT Sheet
        Sheet lineStructSheet = null;
        lineStructSheet = excelWriter.getWorkbook().createSheet("LINE-STRUCT");

        // UI Log
        if (isUISyncLogEnabled) {
            logRow("EXPORTING LINE-STRUCT Sheet...");
        }

        // First ROW is the HEADER
        // rowIndex is the counter of rows
        int rowIndex = 0;

        // Accumulator for linesWNoPoints
        int linesWNoPointsAccumul = 0;

        /** ------------ */
        /** BUILD HEADER */
        /** ------------ */
        // Generate column headings
        Row row = lineStructSheet.createRow(rowIndex);
        // rowIndex = 0 is the HEADER
        setHeader(row);

        /** ------------------- */
        /** BUILD BODY CONTENTS */
        /** ------------------- */
        // Get LINE data from DB to populate interface file
        GravityMobileDBHelper mDB = GravityMobileDBHelper.getInstance(activity.getApplicationContext(), true);
        List<Line> lineLst = mDB.getAllLines();

        /** ---------- Iterate LINE :: IN --------- */
        Iterator<Line> lineLstIter = lineLst.iterator();
        while (lineLstIter.hasNext()) {

            // AUX var, the Actual LINE in process
            Line lineAux = lineLstIter.next();

            // UI Log
            if (isUISyncLogEnabled) {
                logRow(">>>EXPORTING LINE: " + lineAux.getName());
            }

            // Get GRAVIMETER
            int gravimeterId = lineAux.getGravimeterId();
            mDB = GravityMobileDBHelper.getInstance(activity.getApplicationContext(), true);
            Gravimeter gravimeter = mDB.getGravimetersByID(gravimeterId);

            // Get all POINTS from the actual LINE
            mDB = GravityMobileDBHelper.getInstance(activity.getApplicationContext(), false);
            List<Point> pointLst = mDB.getPointsByLineId(
                    lineAux.getmId()
                    , GravityMobileDBInterface.SQL_ASC);

            if (pointLst != null) {
                /** ------- ITERATE POINTS :: IN -------- */
                Iterator<Point> pointIter = pointLst.iterator();
                while (pointIter.hasNext()) {

                    // Increment rowIndex (a row in the .xls)
                    // for next processing
                    rowIndex++;

                    // CREATE NEW ROW to populate
                    row = lineStructSheet.createRow(rowIndex);

                    // AUX point actual being iterated
                    Point pointAux = pointIter.next();

                    // USER by POINT
                    mDB = GravityMobileDBHelper.getInstance(activity.getApplicationContext(), true);
                    User user = mDB.getUserById(pointAux.getUserId());

                    Cell c = null;
                    int cellIndex = 1;

                    // blank
                    c = row.createCell(cellIndex);
                    c.setCellValue(rowIndex);
                    // Line Name
                    c = row.createCell(cellIndex++);
                    c.setCellValue(lineAux.getName());
                    //benchmark
                    c = row.createCell(cellIndex++);
                    c.setCellValue(pointAux.getCode());
                    //instrument
                    c = row.createCell(cellIndex++);
                    c.setCellValue(gravimeter.getName());
                    //direction
                    c = row.createCell(cellIndex++);
                    c.setCellValue(pointAux.getOneWayValue());
                    //epoch
                    c = row.createCell(cellIndex++);
                    c.setCellValue(" ");
                    //timestamp
                    c = row.createCell(cellIndex++);
                    c.setCellValue(pointAux.getDate());
                    //year
                    c = row.createCell(cellIndex++);
                    c.setCellValue(" ");
                    //month
                    c = row.createCell(cellIndex++);
                    c.setCellValue(" ");
                    //day
                    c = row.createCell(cellIndex++);
                    c.setCellValue(" ");
                    //hour
                    c = row.createCell(cellIndex++);
                    c.setCellValue(" ");
                    // minute
                    c = row.createCell(cellIndex++);
                    c.setCellValue(" ");
                    // raw_data_1
                    c = row.createCell(cellIndex++);
                    c.setCellValue(pointAux.getG1());
                    // raw_data_2
                    c = row.createCell(cellIndex++);
                    c.setCellValue(pointAux.getG2());
                    // raw_data_3
                    c = row.createCell(cellIndex++);
                    c.setCellValue(pointAux.getG3());
                    // reading
                    c = row.createCell(cellIndex++);
                    c.setCellValue(pointAux.getReading());
                    // reduced_g
                    c = row.createCell(cellIndex++);
                    c.setCellValue(pointAux.getReducedG());
                    // status
                    c = row.createCell(cellIndex++);
                    c.setCellValue(lineAux.getStatus());
                    // user
                    c = row.createCell(cellIndex++);
                    c.setCellValue(user.getName());


                    // UI Log
                    if (isUISyncLogEnabled) {
                        logRow("ADDING BNCHMRK: " + pointAux.getCode());
                    }
                    // lineStructSheet.setColumnWidth(2, (15 * 500));

                    // SAVE IMAGES
                    saveImagesToDirectory(lineAux, pointAux);

                }
                /** ------- ITERATE POINTS :: OUT -------- */
            } else {
                linesWNoPointsAccumul++;
            }

        }
        /** ---------- Iterate LINE :: OUT --------- */

        return rowIndex + " :Registros exportados correctamente en LINE-STRUCT.\n" +
                linesWNoPointsAccumul + " :LINEAS sin PUNTOS asociados.\n" +
                counter.getQProcessed() + " :Fotos exportadas.\n" +
                counter.getQError() + " :Fotos NO exportadas debido a error.\n";
    }

    /**
     * Helper method to export pictures from DB to an Android directory
     *
     * @param lineAux
     * @param pointAux
     */
    private void saveImagesToDirectory(Line lineAux, Point pointAux) {

        //
        ImageFileLoaderAndSaver imagesSaver =
                new ImageFileLoaderAndSaver(activity.getApplicationContext(),
                        destBackupFolderName,
                        true);

        String imageName = Log4GravityUtils.imageNameBuilder(
                pointAux.getDate()
                , lineAux.getName()
                , pointAux.getCode()
                , pointAux.getOneWayValue());

        // Save Photo G1 to a backup folder
        try {
            // destBackupFolderName is a local variable, set when this class is instanciated
            imagesSaver.saveImageToCustomDirProxy(activity.getApplicationContext()
                    , pointAux.getG1Photo()
                    , imageName + "G1"
                    , destBackupFolderName);//"images");

            if (isUISyncLogEnabled) {
                logRow("EXPORTING: " + imageName + "G1");
            }
            counter.countProcessed();

        } catch (Exception e) {
            isSavedPhotosExecOk = false;
            e.printStackTrace();
            // UI Log
            if (isUISyncLogEnabled) {
                logRow("EXPORTING IMAGE ERROR: " + imageName + "G1"
                        + e.getMessage());
            }
            counter.countError();
        }
        // Save Photo G2 to a backup folder
        // destBackupFolderName is a local variable, set when this class is instanciated
        try {
            imagesSaver.saveImageToCustomDirProxy(activity.getApplicationContext()
                    , pointAux.getG2Photo()
                    , imageName + "G2"
                    , destBackupFolderName);//"images");

            if (isUISyncLogEnabled) {
                logRow("EXPORTING: " + imageName + "G2");
            }
            counter.countProcessed();

        } catch (Exception e) {
            isSavedPhotosExecOk = false;
            e.printStackTrace();
            // UI Log
            if (isUISyncLogEnabled) {
                logRow("EXPORTING IMAGE ERROR: " + imageName + "G2"
                        + e.getMessage());
            }
            counter.countError();
        }
        // Save Photo G3 to a backup folder
        // destBackupFolderName is a local variable, set when this class is instanciated
        try {
            imagesSaver.saveImageToCustomDirProxy(activity.getApplicationContext()
                    , pointAux.getG3Photo()
                    , imageName + "G3"
                    , destBackupFolderName);//"images");

            if (isUISyncLogEnabled) {
                logRow("EXPORTING: " + imageName + "G3");
            }
            counter.countProcessed();

        } catch (Exception e) {
            isSavedPhotosExecOk = false;
            e.printStackTrace();
            // UI Log
            if (isUISyncLogEnabled) {
                logRow("EXPORTING IMAGE ERROR: " + imageName + "G3"
                        + e.getMessage());
            }
            counter.countError();
        }
    }

    /**
     * Set Header of the .xls (LINE-STRUCT) tab
     *
     * @param row
     */
    private void setHeader(Row row) {
        Cell c = null;
        for (int i = 0; i < header.length; i++) {
            c = row.createCell(i);
            c.setCellValue(header[i]);
        }
    }

    /**
     * EXPORT OBSERVATIONS:
     * --------------------
     * <p>
     * Output example of an fieldColectedObservs_yyyyMMddHHMMss.xls :
     * <p>
     * CL18
     * 2018-06-09 22:45
     * -19,1710255
     * -63,0588611
     * 1974,890
     * 1974,890
     * 1974,890
     * 0
     * 47
     * <p>
     * Every sheet will be named with the gravimeter used in the field work
     */
    private String buildExportObservationsSheet(ExcelFileWriter excelWriter) {

        /** ------------------- */
        /** BUILD BODY CONTENTS */
        /** ------------------- */
        // Get LINE data from DB to populate interface file
        GravityMobileDBHelper mDB = GravityMobileDBHelper.getInstance(activity.getApplicationContext(), true);
        List<Line> lineLst = mDB.getAllLines();

        int qPointsExported = 0;
        int sheetCounter = 0;

        /** ---------- Iterate LINE :: IN --------- */
        Iterator<Line> lineLstIter = lineLst.iterator();
        while (lineLstIter.hasNext()) {

            // AUX var, the Actual LINE in process
            Line lineAux = lineLstIter.next();

            // GRAVIMETER
            int gravimeterId = lineAux.getGravimeterId();
            mDB = GravityMobileDBHelper.getInstance(activity.getApplicationContext(), true);
            Gravimeter gravimeter = mDB.getGravimetersByID(gravimeterId);

            //Log.d(LOG_TAG,"gravimeterId:"+gravimeterId);

            // Create the FORWARD-SHEET for this gravimeter
            Sheet observationFwdSheet = null;
            String sheetName = lineAux.getName() + "-" + gravimeter.getName() + "F" + sheetCounter;
            observationFwdSheet = excelWriter.getWorkbook().createSheet(sheetName);

            // UI Log
            if (isUISyncLogEnabled) {
                logRow(">>>CREATING FWD SHEET: " + sheetName);
            }

            // FORWARD-SHEET Data export
            qPointsExported = qPointsExported
                    + buildSheet(lineAux
                    , PointStatusInterface.POINT_ONEWAYVALUE_FORWARD
                    , observationFwdSheet);

            // Create the RETURN-SHEET for this gravimeter
            Sheet observationRtrnSheet = null;
            sheetName = lineAux.getName() + "-" + gravimeter.getName() + "R" + sheetCounter;
            observationRtrnSheet = excelWriter.getWorkbook().createSheet(sheetName);

            sheetCounter++;

            // UI Log
            if (isUISyncLogEnabled) {
                logRow(">>>CREATING RET SHEET: " + sheetName);
            }

            // RETURN-SHEET Data export
            qPointsExported =
                    qPointsExported
                            + buildSheet(lineAux
                            , PointStatusInterface.POINT_ONEWAYVALUE_RETURN
                            , observationRtrnSheet);

        }
        /** ---------- Iterate LINE :: OUT --------- */

        return qPointsExported + " :PUNTOS exportados correctamente.\n" +
                " \n";
    }

    /**
     * EXPORT OBSERVATIONS:
     * --------------------
     * For every observation Sheet this method is called
     *
     * @param lineAux
     * @param oneWayValue
     * @param observationsSheet
     * @return an integer representing number of registers processed
     */
    private int buildSheet(Line lineAux
            , int oneWayValue
            , Sheet observationsSheet) {

        // Get all POINTS from the actual LINE
        GravityMobileDBHelper mDB = GravityMobileDBHelper.getInstance(activity.getApplicationContext(), true);

        List<Point> pointLst = mDB.getPointsByLineIdAndOnwWayVal(lineAux.getmId()
                , oneWayValue
                , GravityMobileDBInterface.SQL_ASC);

        // First ROW is the HEADER
        // rowIndex is the counter of rows
        int rowIndex = 0;

        Row row = null;

        // If a LINE is empty, the sheet will be created anyway
        if (pointLst != null) {
            /** ------- ITERATE POINTS :: IN ---------*/
            Iterator<Point> pointIter = pointLst.iterator();
            while (pointIter.hasNext()) {

                // CREATE NEW ROW to populate
                row = observationsSheet.createRow(rowIndex);

                // AUX point actual being iterated
                Point pointAux = pointIter.next();

                // USER by POINT
                mDB = GravityMobileDBHelper.getInstance(activity.getApplicationContext(), true);
                User user = mDB.getUserById(pointAux.getUserId());

                Cell c = null;

                //benchmark
                c = row.createCell(0);
                c.setCellValue(pointAux.getCode());
                //date
                c = row.createCell(1);
                c.setCellValue(pointAux.getDate());
                //lat
                c = row.createCell(2);
                c.setCellValue(pointAux.getLatitude());
                //long
                c = row.createCell(3);
                c.setCellValue(pointAux.getLongitude());
                // raw_data_1
                c = row.createCell(4);
                c.setCellValue(pointAux.getG1());
                // raw_data_2
                c = row.createCell(5);
                c.setCellValue(pointAux.getG2());
                // raw_data_3
                c = row.createCell(6);
                c.setCellValue(pointAux.getG3());
                // Offset-RQ-LACKOFDEF-000
                c = row.createCell(7);
                c.setCellValue(pointAux.getOffset());
                //????
                c = row.createCell(8);
                c.setCellValue("????");
                // status
                c = row.createCell(9);
                c.setCellValue(lineAux.getStatus());
                // user
                c = row.createCell(10);
                c.setCellValue(user.getName());

                // Increment rowIndex (a row in the .xls)
                // for next processing
                rowIndex++;

                if (BuildConfig.DEBUG) {
                    Log.i(LOG_TAG, "rowIndex:" + rowIndex);
                }
            }
            /** ------- ITERATE POINTS :: OUT -------- */
        } else {
            return 0;
        }
        return rowIndex;
    }

    //---------------------- EXPORT::OUT------------------------------//
    //---------------------- EXPORT::OUT------------------------------//
    //---------------------- EXPORT::OUT------------------------------//

    /**
     * Helper method to build log contents
     *
     * @param msg
     */
    private static void logRow(final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchroLog.getText().append("\n" + msg);
                synchroLog.setScrollY(synchroLog.getScrollY() + 1000);
                //Log.i(LOG_TAG,"synchroLog.getScrollY():" + synchroLog.getScrollY());

                //Log.i(LOG_TAG,"synchroLog.getScrollY() + 1:" + synchroLog.getScrollY() +1);
                // autoscroll
                synchroLog.setVerticalScrollbarPosition(synchroLog.getScrollY() + 1000);
            }
        });
    }

    /**
     * Returns synchronization execution status
     *
     * @return true if synchronization was executed successfully
     */
    public static boolean isSynchronized() {
        if (isImportBenchmrksExecOk && isImportGravimetersExecOk
                && isExportDataExecOk && isSavedPhotosExecOk) {
            synchrnzd = true;
        }
        return synchrnzd;
    }

    /**
     * NESTE CLASS::IN
     * ImageProcessedCounter obtect to accumulate discrete data
     */
    private class ImageProcessedCounter { // ISSUE MEMORY-LEAKS
        private int qImagesProcessed = 0;
        private int qImagesErr = 0;

        ImageProcessedCounter() {
        }



        public void countProcessed() {
            qImagesProcessed++;
        }

        public void countError() {
            qImagesErr++;
        }

        public int getQProcessed() {
            return qImagesProcessed;
        }

        public int getQError() {
            return qImagesErr;
        }
    }
    /**
     * ImageProcessedCounter obtect to accumulate discrete data
     * NESTED CLASS::OUT
     */


}