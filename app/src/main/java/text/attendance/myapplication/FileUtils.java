package text.attendance.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    public static String getPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        // Handle different types of URIs
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getFilePathFromUri(context, uri);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getFilePathFromUri(Context context, Uri uri) {
        String filePath = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    String fileName = cursor.getString(nameIndex);
                    File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

                    // Copy the file from content resolver to accessible location
                    try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                         FileOutputStream outputStream = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                    filePath = file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            Log.e("FileUtils", "Error retrieving file path: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return filePath;
    }
}
