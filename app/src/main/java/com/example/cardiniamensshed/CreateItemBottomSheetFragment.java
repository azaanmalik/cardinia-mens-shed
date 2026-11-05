package com.example.cardiniamensshed;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CreateItemBottomSheetFragment extends BottomSheetDialogFragment {

    private TextInputEditText nameField, descriptionField, priceField, quantityField;
    private MaterialButton createButton;
    private String itemType;

    private OnCreateItemListener listener;

    public interface OnCreateItemListener {
        void onItemCreated(String type, String name, String description, double price, int quantity);
    }

    public static CreateItemBottomSheetFragment newInstance(String type) {
        CreateItemBottomSheetFragment fragment = new CreateItemBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateItemListener) {
            listener = (OnCreateItemListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCreateItemListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_item_bottom_sheet, container, false);

        if (getArguments() != null) {
            itemType = getArguments().getString("type");
        }

        nameField = view.findViewById(R.id.name_field);
        descriptionField = view.findViewById(R.id.description_field);
        priceField = view.findViewById(R.id.price_field);
        quantityField = view.findViewById(R.id.quantity_field);
        createButton = view.findViewById(R.id.create_button);

        createButton.setOnClickListener(v -> {
            String name = nameField.getText() != null ? nameField.getText().toString().trim() : "";
            String description = descriptionField.getText() != null ? descriptionField.getText().toString().trim() : "";
            String priceStr = priceField.getText() != null ? priceField.getText().toString().trim() : "";
            String quantityStr = quantityField.getText() != null ? quantityField.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                    TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(quantityStr)) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            int quantity;

            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid positive price", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid positive quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onItemCreated(itemType, name, description, price, quantity);
            }

            dismiss();
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
