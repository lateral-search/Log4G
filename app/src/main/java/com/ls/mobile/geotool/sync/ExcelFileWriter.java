package com.ls.mobile.geotool.sync;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.ls.mobile.geotool.common.Log4GravityUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * In AndroidManifest.xml file add “WRITE_EXTERNAL_STORAGE ” permission as we require to access the
 * external storage for saving the Excel file.
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 * <p>
 * Add the poi-3.7.jar (or updated version) that you have downloaded from the link provided above.
 * Add this jar to your project’s External Jars.
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 */
public class ExcelFileWriter {


    private static Workbook workbook = new HSSFWorkbook();

    // Log
    private static final String LOG_TAG = ExcelFileWriter.class.getSimpleName();

    public ExcelFileWriter() {
    }

    // This is how we add/enter the value in the cell.
    // The above code will add the value “Item Number” in the 0th row and 0th column.
    //
    //  Row row = sheet1.createRow(0);
    //        c = row.createCell(0);
    //        c.setCellValue("Item Number");
    //        c.setCellStyle(cs);

    /**
     * EXAMPLE METHOD:
     * @param context
     * @param fileName
     * @return
     * @deprecated Use this deprecated as an example of how to build an xls
     */
    private static boolean saveExcelFile(Context context, String fileName) {

        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(LOG_TAG, "Storage not available or read only");
            return false;
        }

        boolean success = false;

        // New Workbook:: This instanciation is neccessary cause
        // sucesives exports will make overwriten of data.
        Workbook wb = new HSSFWorkbook();

        Cell c = null;

        // Cell style for header row
        CellStyle cs = wb.createCellStyle();
        //cs.setFillForegroundColor(HSSFColor.LIME.index);
        //cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        // New Sheet
        Sheet sheet1 = null;
        sheet1 = wb.createSheet("myOrder");

        // Generate column headings
        Row row = sheet1.createRow(0);

        c = row.createCell(0);
        c.setCellValue("Item Number");
        c.setCellStyle(cs);

        c = row.createCell(1);
        c.setCellValue("Quantity");
        c.setCellStyle(cs);

        c = row.createCell(2);
        c.setCellValue("Price");
        c.setCellStyle(cs);

        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));
        sheet1.setColumnWidth(2, (15 * 500));

        // Create a path where we will place our List of objects on external storage
        File file = new File(context.getExternalFilesDir(null), fileName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w(LOG_TAG, "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w(LOG_TAG, "Error writing " + file, e);
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
        return success;
    }

    /**
     * Save to a custom user directory defined for Log4G export process.
     *
     * @param context:           the context taken from the Activity
     * @param fileName:          Name of the .xls file
     * @param destDirectoryName: Predefined user backup directory in Log4Gravity
     * @return true if success / false the oposite
     */
    public boolean saveFileToCustomDirectory(Context context,
                                             String fileName,
                                             String destDirectoryName) {
        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(LOG_TAG, "Storage not available or read only");
            return false;
        }

        boolean success = false;

        // USER BACKUP DIRECTORY PATH
        File bckDirectory = Log4GravityUtils.getCustomStorageDir(context, destDirectoryName);
        File file = new File(bckDirectory.getAbsolutePath(), fileName); // FILE TO WRITE

        Log.i(LOG_TAG, "EXPORT USER DIRECTORY IN:: " + bckDirectory.getAbsolutePath());

        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            workbook.write(os);
            Log.i(LOG_TAG, "WRITING...." + file);
            success = true;
        } catch (IOException e) {
            Log.i(LOG_TAG, "ERROR WRITING" + file, e);
        } catch (Exception e) {
            Log.i(LOG_TAG, "ERROR SAVING", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
                Log.i(LOG_TAG, "ERROR CLOSING Output Stream :: ", ex);
            }
        }
        return success;
    }

    /**
     * This is how we add/enter the value in the cell.
     * The above code will add the value “Item Number” in the
     * 0th row and 0th column.
     * <p>
     * Row row = sheet1.createRow(0);
     * c = row.createCell(0);
     * c.setCellValue("Item Number");
     * c.setCellStyle(cs);
     */
    public boolean saveFile(Context context, String fileName) {

        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(LOG_TAG, "Storage not available or read only");
            return false;
        }

        boolean success = false;

        // https://developer.android.com/training/data-storage/files#java
        // Create a path where we will place our List of objects
        // on external storage
        File file = new File(context.getExternalFilesDir(null), fileName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            workbook.write(os);
            Log.w(LOG_TAG, "WRITING...." + file);
            success = true;
        } catch (IOException e) {
            Log.w(LOG_TAG, "ERROR WRITING" + file, e);
        } catch (Exception e) {
            Log.w(LOG_TAG, "ERROR SAVING", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
        return success;
    }

    /**
     * @return
     */
    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available for read and write
     *
     * @return
     */
    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    // ---------------------- GETTERS - SETTERS ---------------------------- //

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }


}
