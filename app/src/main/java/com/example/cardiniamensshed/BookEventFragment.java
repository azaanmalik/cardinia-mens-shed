package com.example.cardiniamensshed;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BookEventFragment extends Fragment {

    private TextInputEditText eventNameField, productDescriptionField, productPriceField, productQuantityField;
    private Spinner eventTypeSpinner; // Spinner for event type selection
    private MaterialButton createEventButton, uploadImageButton, skipProductButton;
    private ImageView productImageView;
    private ProgressBar uploadProgressBar;

    private Uri selectedImageUri;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    productImageView.setImageURI(selectedImageUri);
                }
            });

    public BookEventFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_event, container, false);

        initViews(view);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        createEventButton.setOnClickListener(v -> createEvent(false));
        skipProductButton.setOnClickListener(v -> createEvent(true));
        uploadImageButton.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void initViews(View view) {
        eventNameField = view.findViewById(R.id.event_name_field);
        productDescriptionField = view.findViewById(R.id.product_description_field);
        productPriceField = view.findViewById(R.id.product_price_field);
        productQuantityField = view.findViewById(R.id.product_quantity_field);
        createEventButton = view.findViewById(R.id.create_event_button);
        uploadImageButton = view.findViewById(R.id.upload_image_button);
        skipProductButton = view.findViewById(R.id.skip_product_button);
        productImageView = view.findViewById(R.id.product_image_view);
        uploadProgressBar = view.findViewById(R.id.upload_progress_bar);

        // Initialize the event type spinner
        eventTypeSpinner = view.findViewById(R.id.event_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.event_types, android.R.layout.simple_spinner_item); // Load types from resources
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(adapter);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void createEvent(boolean skipProduct) {
        String name = eventNameField.getText().toString().trim();
        String eventType = eventTypeSpinner.getSelectedItem().toString(); // Get selected event type
        String desc = productDescriptionField.getText().toString().trim();
        String priceStr = productPriceField.getText().toString().trim();
        String qtyStr = productQuantityField.getText().toString().trim();

        if (TextUtils.isEmpty(name) || selectedImageUri == null) {
            Toast.makeText(getContext(), "Event name, type, and image are required", Toast.LENGTH_SHORT).show();
            return;
        }

        final double price;
        final int quantity;

        if (!skipProduct) {
            if (TextUtils.isEmpty(desc) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(qtyStr)) {
                Toast.makeText(getContext(), "All product fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                price = Double.parseDouble(priceStr);
                quantity = Integer.parseInt(qtyStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid price or quantity", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            price = 0.0;
            quantity = 0;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadProgressBar.setVisibility(View.VISIBLE);

        SupabaseUploader.uploadEventImage(requireContext(), selectedImageUri, auth.getCurrentUser(), new SupabaseUploader.UploadCallback() {
            @Override
            public void onUploadComplete(String imageUrl) {
                Map<String, Object> event = new HashMap<>();
                event.put("name", name);
                event.put("type", eventType); // Save event type
                event.put("description", desc);
                event.put("price", price);
                event.put("quantity", quantity);
                event.put("owner", auth.getCurrentUser().getUid()); // Save owner's UID
                event.put("imageUrl", imageUrl);
                event.put("timestamp", System.currentTimeMillis()); // Correct timestamp

                firestore.collection("events").add(event)
                        .addOnSuccessListener(doc -> {
                            uploadProgressBar.setVisibility(View.GONE);
                            new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Event Created!")
                                    .setContentText("Your event has been successfully created.")
                                    .setConfirmText("OK")
                                    .show();
                            clearInputs();
                        })
                        .addOnFailureListener(e -> {
                            uploadProgressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to save event", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onUploadFailed(String error) {
                uploadProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearInputs() {
        eventNameField.setText("");
        productDescriptionField.setText("");
        productPriceField.setText("");
        productQuantityField.setText("");
        productImageView.setImageURI(null);
        eventTypeSpinner.setSelection(0); // Reset spinner
        selectedImageUri = null;
    }
}