package org.telegram.camera.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Collection of functions for work with files
 *
 * @author Danil Kolikov
 * @author Gleb Zernov
 */
public class FileUtils {
    /**
     * Current qualities of pictures. It's a value from 0 to 100
     */
    private final static int PICTURE_QUALITY = 90;
    /**
     * Folder to save photos and videos
     */
    private static final String FOLDER = "TelegramCamera";
    private static final String TAG = "File Utils";


    /**
     * Check if external storage is mounted
     *
     * @return True, if external storage is mounted, False, otherwise
     */
    public static boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Create a File for saving an image or video
     *
     * @param type Type of a file
     */
    public static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), FOLDER);

        return makeOutputFile(type, mediaStorageDir);
    }

    /**
     * Create output file
     *
     * @param type      Type of file
     * @param directory Directory to place file
     * @return New file
     */
    private static File makeOutputFile(int type, File directory) {
        // Create the storage directory if it does not exist
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.d(FOLDER, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(directory, "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(directory, "VID_" + timeStamp + ".mp4");
        } else {
            mediaFile = null;
        }

        return mediaFile;
    }

    /**
     * Delete file
     *
     * @param file A file
     */
    public static void deleteFile(File file) {
        if (!file.delete()) {
            Log.e(TAG, "File " + file.getName() + " isn't deleted");
        }
    }

    /**
     * Delete file
     *
     * @param uri A Uri of file
     */
    public static void deleteUri(Uri uri) {
        File file = new File(uri.getPath());
        deleteFile(file);
    }

    /**
     * Transform array of byte into bitmap
     *
     * @param data         Bytes
     * @param screenWidth  Width of picture
     * @param screenHeight Height of picture
     * @param rotation     Rotation of the screen
     * @param front        Is photo captured by front camera
     * @return Constructed bitmap
     */
    public static Bitmap getPicture(byte[] data, int screenWidth, int screenHeight, int rotation, boolean front) {
        Bitmap result = null;
        //image rotation
        Log.d(TAG, "width " + screenWidth + " height " + screenHeight);
        if (data != null) {
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.d(TAG, "rotation " + rotation);
            if (!front) {
                if (rotation != 90) {
                    return bm;
                }
                Matrix mtx = new Matrix();
                mtx.postRotate(90);
                return Bitmap.createBitmap(bm, 0, 0, screenWidth, screenHeight, mtx, true);
            }
            Log.d(TAG, "rotation " + rotation);
            Matrix mtx = new Matrix();
            mtx.postScale(-1, 1);
            // Setting post rotate to 90 due to portrait orientation
            mtx.postRotate(90);
            bm = Bitmap.createBitmap(bm, 0, 0, screenWidth, screenHeight, mtx, true);
            result = bm;
        }

        return result;
    }

    /**
     * Change quality of a bitmap
     *
     * @param bitmap A Bitmap
     * @return Compressed Bitmap
     */
    public static byte[] compress(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, PICTURE_QUALITY, stream);
        return stream.toByteArray();
    }

    /**
     * Rotate image to specified angle
     *
     * @param bitmap Bitmap to rotate
     * @param angle  Angle
     * @return Rotated bitmap
     */
    public static Bitmap rotate(Bitmap bitmap, float angle) {
        Matrix mtx = new Matrix();
        mtx.postRotate(-angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, true);
    }

    /**
     * Close {@link Closeable} without rethrowing exception. <br>
     * Exception that is thrown will be logged using {@link Log#e}
     *
     * @param c A Closeable
     */
    private static void closeQuietly(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            Log.e("QUIET_CLOSE", "Can't close resource", e);
        }
    }

    /**
     * Register file in Android Gallery
     *
     * @param uri     Uri of Photo or Video file
     * @param context Current context
     */
    public static void addFileToGallery(Uri uri, Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * Save file to external storage
     */
    public static class SaveFileTask extends AsyncTask<Object, Object, File> {
        private final Context context;
        private Bitmap bitmap;

        public SaveFileTask(Context context, Bitmap bitmap) {
            this.bitmap = bitmap;
            this.context = context;
        }

        @Override
        protected void onPostExecute(File result) {
            if (result == null) {
                return;
            }
            addFileToGallery(Uri.fromFile(result), context);
//            context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, result);
        }

        @Override
        protected File doInBackground(Object... params) {
            FileOutputStream output = null;
            try {
                File pictureFile = FileUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file");
                    return null;
                }
                output = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, PICTURE_QUALITY, output);
//                ContentValues values = new ContentValues();
//
//                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//                values.put(MediaStore.MediaColumns.DATA, pictureFile.getPath());
//                Log.d(TAG, "File saved");
                return pictureFile;
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
                return null;
            } finally {
                FileUtils.closeQuietly(output);
            }
        }
    }
}
