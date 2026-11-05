package com.example.cardiniamensshed;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseUploader {

    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_API_KEY = BuildConfig.SUPABASE_ANON_KEY; // Replace with your actual API key

    // Buckets
    private static final String PROFILE_PICTURES_BUCKET = "profile-pictures";
    private static final String EVENT_IMAGES_BUCKET = "event-images";

    public interface UploadCallback {
        void onUploadComplete(String url);
        void onUploadFailed(String error);
    }

    // General upload method that takes bucket name and file prefix
    private static void uploadImageToBucket(Context context, Uri imageUri, FirebaseUser user, String bucketName, String filePrefix, UploadCallback callback) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        if (user == null || SUPABASE_URL.isEmpty() || SUPABASE_API_KEY.isEmpty()) {
            progressDialog.dismiss();
            callback.onUploadFailed("Storage is not configured. Set SUPABASE_URL and SUPABASE_ANON_KEY; secure upload policies or a verified backend are also required.");
            return;
        }
        progressDialog.show();

        new Thread(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());

            try {
                String filePath = FileUtils.getPath(context, imageUri);

                if (filePath == null) {
                    filePath = createFileFromUri(context, imageUri).getAbsolutePath();
                }

                File file = new File(filePath);
                String fileName = filePrefix + "_" + user.getUid() + "_" + System.currentTimeMillis() + ".jpg";

                RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/storage/v1/object/" + bucketName + "/" + fileName)
                        .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                        .addHeader("apikey", SUPABASE_API_KEY)
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + bucketName + "/" + fileName;
                    mainHandler.post(() -> {
                        progressDialog.dismiss();
                        callback.onUploadComplete(publicUrl);
                    });
                } else {
                    String errorMessage = "Upload failed with code: " + response.code() + " message: " + response.message();
                    mainHandler.post(() -> {
                        progressDialog.dismiss();
                        callback.onUploadFailed(errorMessage);
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    progressDialog.dismiss();
                    callback.onUploadFailed("Upload error: " + e.getMessage());
                });
            }
        }).start();
    }

    // Existing method for profile pictures (for backward compatibility)
    public static void uploadProfilePicture(Context context, Uri imageUri, FirebaseUser user, UploadCallback callback) {
        uploadImageToBucket(context, imageUri, user, PROFILE_PICTURES_BUCKET, "profile", callback);
    }

    // New method for event images
    public static void uploadEventImage(Context context, Uri imageUri, FirebaseUser user, UploadCallback callback) {
        uploadImageToBucket(context, imageUri, user, EVENT_IMAGES_BUCKET, "event", callback);
    }

    private static File createFileFromUri(Context context, Uri uri) throws Exception {
        File file = new File(context.getCacheDir(), "temp_image.jpg");
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return file;
    }
}
