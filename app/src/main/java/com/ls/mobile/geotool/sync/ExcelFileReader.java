package com.ls.mobile.geotool.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;


/**
 * Based on Apache POI Interface And Classs:
 *
 * In AndroidManifest.xml file add “WRITE_EXTERNAL_STORAGE ” permission as we require to access the
 * external storage for saving the Excel file.
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
 *
 * Add the poi-3.7.jar (or updated version) that you have downloaded from Apache site.
 * Add this jar to your project’s External Jars.
 *
 * Apache POI Interface And Classs
 *
 Workbook: interface to represent excel file.
 XSSFWorkbook: a class implement Workbook, operate XLSX file.
 HSSFWorkbook: a class implement Workbook, operate XLS file.
 Sheet: interface to represent excel sheet.
 XSSFSheet: a class implement Sheet, operate XLSX file.
 HSSFSheet: a class implement Sheet, operate XLS file.
 Row: interface to represent row in sheet.
 XSSFRow: a class implement Row, operate XLSX file.
 HSSFRow: a class implement Row, operate XLS file.
 Cell: interface to represent cell in row.
 XSSFCell: a class implement Cell, operate XLSX file.
 HSSFCell: a class implement Cell, operate XLS file.
 *
 * @MemLeaks Q&A: 0k
 *
 * @author lateralSearch
 *
 *
 */
public class ExcelFileReader{

    private static HSSFWorkbook hssfWorkbook;
    // Log
    private static final String LOG_TAG = ExcelFileReader.class.getSimpleName();

    /**
     * Constructor
     * @param context
     * @param filename
     * @param uri
     */
    public ExcelFileReader(Context context, String filename, Uri uri ){
        readExcelFile(context,filename,uri );
    }

    /**
     *
     * @param context
     * @param filename
     * @param uri
     */
    private static void readExcelFile(Context context, String filename, Uri uri ) {

            if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
            {
                Log.e(LOG_TAG, "Storage not available or read only");
                return;
            }

           // GUIDE:https://developer.android.com/training/data-storage/files#java
            try{
                /**
                 * An absolute hierarchical URI reference
                 * follows the pattern:
                 * <scheme>://<authority><absolute path>?<query>#<fragment>
                 *
                 * Here is an example of how you can get an InputStream from the URI.
                 * In this snippet, the lines of the file are being read into a string:
                 *
                 * https://developer.android.com/guide/topics/providers/document-provider
                 */
                InputStream myInput = context.getContentResolver().openInputStream(uri);

                // Create a POIFSFileSystem object
                POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

                // Create a workbook using the File System
                hssfWorkbook = new HSSFWorkbook(myFileSystem);

            }catch (Exception e){
                e.printStackTrace();
                // Toast.makeText(context, "cell Value: " + myCell.toString(), Toast.LENGTH_SHORT).show();
                //System.out.println("-e.getMessage(--" + e.getMessage() );
                //System.out.println("-e.toString(--" + e.toString());
            }

            return;
        }



    public static boolean isExternalStorageReadOnly() {
            String extStorageState = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
                return true;
            }
            return false;
        }

        public static boolean isExternalStorageAvailable() {
            String extStorageState = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
                return true;
            }
            return false;
        }


    /**
     * Get entire WorkBook ready for data iteration
     * @return HSSFWorkbook instance
     */
    public HSSFWorkbook getWorkbook() {
        return hssfWorkbook;
    }



}


