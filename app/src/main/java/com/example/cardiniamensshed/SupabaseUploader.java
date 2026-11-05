package com.example.cardiniamensshed;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

/**
 * Compatibility facade retaining the original class name.
 * Image uploads now use Firebase Storage authenticated by Firebase Auth.
 * No Supabase service-role key, public-write bucket, or second auth provider is required.
 */
public final class SupabaseUploader {
    private SupabaseUploader() {}

    public interface UploadCallback {
        void onUploadComplete(String url);
        void onUploadFailed(String error);
    }

    private static void uploadImageToBucket(Context context, Uri imageUri, FirebaseUser user,
                                            String folder, UploadCallback callback) {
        if (user == null || imageUri == null) {
            callback.onUploadFailed("Sign in and choose an image before uploading.");
            return;
        }
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference target = FirebaseStorage.getInstance().getReference()
                .child(folder).child(user.getUid()).child(fileName);
        target.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception cause = task.getException();
                        throw cause == null ? new IllegalStateException("Upload failed") : cause;
                    }
                    return target.getDownloadUrl();
                })
                .addOnSuccessListener(url -> callback.onUploadComplete(url.toString()))
                .addOnFailureListener(error -> callback.onUploadFailed(error.getMessage()));
    }

    public static void uploadProfilePicture(Context context, Uri imageUri, FirebaseUser user,
                                            UploadCallback callback) {
        uploadImageToBucket(context, imageUri, user, "profile-pictures", callback);
    }

    public static void uploadEventImage(Context context, Uri imageUri, FirebaseUser user,
                                        UploadCallback callback) {
        uploadImageToBucket(context, imageUri, user, "event-images", callback);
    }
}
