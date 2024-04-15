package com.ls.mobile.geotool.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @MemLeaks Q&A: 0k
 *
 */
public class ImageFileLoaderAndSaver {

    // i.e.: /data/data/com.ls.mobile.geotool/app_images
    private static final String APP_IMAGES_DIRECTORY = "images";
    private String directoryName;
    private String fileName = "image.png";
    private Context context;
    private boolean external;

    /**
     * Constructor
     */
    public ImageFileLoaderAndSaver(Context context) {
        this.context = context;
    }

    /**
     * Custom Constructor
     */
    public ImageFileLoaderAndSaver(Context context,
                                   String directoryName,
                                   boolean external) {
        this.context = context;
        this.directoryName = directoryName;
        this.external = external;
    }

    //------------------------------ IN::CUSTOM ---------------------------------//
    //----------------EXTERNAL SAVE TO A USER ALLOWED DIRECTORY -----------------//
    //--------------------(USER can access from device)--------------------------//
    //---------------------------EXTERNAL SAVE-----------------------------------//

    /**
     * Proxies the ImageFileLoaderAndSaver class
     * A simple method to save a picture in one step
     *
     * @param ctx
     * @param bitmap
     * @param imageName
     * @param imageDir  mnemonic of the diretory where image files
     *                  will be copied. I.e.: imageDir = "images", files will be
     *                  placed into directory:
     *                  /data/data/com.ls.mobile.geotool/app_images
     * @return
     */
    public void saveImageToCustomDirProxy(Context ctx
            , Bitmap bitmap
            , String imageName
            , String imageDir) throws Exception {

        try {
            setFileName(imageName).     //"myImage.png").
                    setDirectoryName(imageDir). // "images"
                    saveCustom(bitmap);
        } catch (Exception e) {
            throw new Exception(e);
        }
        //return true;
    }

    /**
     * SAVE CUSTOM
     * Save a bitmap image to a custom directory outside application
     *
     * @param bitmapImage
     */
    public void saveCustom(Bitmap bitmapImage) throws Exception {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(createCustomFile());
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Exception(e);
            }
        }
    }

    /**
     * @return
     */
    @NonNull
    private File createCustomFile() {
        File directory;
        if (external) {
            // Saves on external appDir [appdir:com.ls.appname.etc]/files/[customName]
            directory = getCustomStorageDir(directoryName);
        } else {
            directory = context.getDir(directoryName, Context.MODE_PRIVATE);
        }
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e("ImageSaver", "Error creating directory " + directory);
        }

        return new File(directory, fileName);
    }

    /**
     *
     * @param destDirectoryName
     *
     * @return
     */
    private File getCustomStorageDir(String destDirectoryName) {
        // FILE POINTING TO DESTINATION FOLDER PATH
        return Log4GravityUtils.getCustomStorageDir(context,destDirectoryName);
    }


    //--------------------------- OUT::CUSTOM -----------------------------------//
    //----------------EXTERNAL SAVE TO A USER ALLOWED DIRECTORY -----------------//
    //--------------------(USER can access from device)--------------------------//
    //---------------------------EXTERNAL SAVE-----------------------------------//


    //--------------------------------- IN --------------------------------------//
    //-------------------- NORMAL SAVE TO APP DIRECTORY -------------------------//
    //--------------------(USER can't access from device)------------------------//
    //---------------------------INTERNAL SAVE-----------------------------------//

    /**
     * Proxies the ImageFileLoaderAndSaver class
     * A simple method to save a picture in one step
     *
     * @param ctx
     * @param bitmap
     * @param imageName
     * @param imageDir  mnemonic of the diretory where image files
     *                  will be copied. I.e.: imageDir = "images", files will be
     *                  placed into directory:
     *                  /data/data/com.ls.mobile.geotool/app_images
     *
     */
    public void saveImageToDirProxy(Context ctx
            , Bitmap bitmap
            , String imageName
            , String imageDir) throws Exception {

        try {
            setFileName(imageName).     //"myImage.png").
                    setDirectoryName(imageDir). // "images"
                    save(bitmap);
        } catch (Exception e) {
            throw new Exception(e);
        }
        //return true;
    }

    /**
     * SAVE
     *
     * @param bitmapImage
     */
    public void save(Bitmap bitmapImage) throws Exception {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(createFile());
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Exception(e);
            }
        }
    }

    /**
     * @return
     */
    @NonNull
    private File createFile() {
        File directory;
        if (external) {
            // Saves on internal appDir [appdir:com.ls.appname.etc]/app_images
            directory = getAlbumStorageDir(directoryName);
        } else {
            directory = context.getDir(directoryName, Context.MODE_PRIVATE);
        }
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e("ImageSaver", "Error creating directory " + directory);
        }

        return new File(directory, fileName);
    }

    /**
     * @param albumName
     * @return
     */
    private File getAlbumStorageDir(String albumName) {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
    }


    //--------------------------------- OUT -------------------------------------//
    //-------------------- NORMAL SAVE TO APP DIRECTORY -------------------------//
    //--------------------(USER can't access from device)------------------------//
    //---------------------------INTERNAL SAVE-----------------------------------//


    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * @return
     */
    public Bitmap load() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(createFile());
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Set file name
     *
     * @param fileName
     * @return
     */
    public ImageFileLoaderAndSaver setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * Set storage location
     *
     * @param external
     * @return
     */
    public ImageFileLoaderAndSaver setExternal(boolean external) {
        this.external = external;
        return this;
    }

    /**
     * Set the location path for the file
     *
     * @param directoryName
     * @return
     */
    public ImageFileLoaderAndSaver setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
        return this;
    }


}