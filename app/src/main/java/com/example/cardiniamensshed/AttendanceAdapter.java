package com.example.cardiniamensshed;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private boolean isDeleting = false;

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_record, parent, false);
        return new AttendanceViewHolder(view);
    }

    // Update the onBindViewHolder method in AttendanceAdapter.java
    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = attendanceRecords.get(position);

        Log.d("AttendanceAdapter", "Record: timestamp=" + record.getTimestamp() + ", eventName=" + record.getEventName());

        // Format the timestamp with the action type
        String actionType = record.getType() != null ? record.getType().toUpperCase() : "UNKNOWN";
        String displayText = actionType + " - " + record.getTimestamp();
        holder.timestampTextView.setText(displayText);

        String eventName = record.getEventName();
        if (eventName == null || eventName.isEmpty()) {
            holder.eventNameTextView.setText("Event: N/A");
        } else {
            holder.eventNameTextView.setText("Event: " + eventName);
        }

        holder.deleteButton.setEnabled(!isDeleting);
        holder.deleteButton.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();

            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Delete Timestamp")
                    .setContentText("Are you sure you want to delete this timestamp?")
                    .setConfirmText("Yes")
                    .setCancelText("No")
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        deleteTimestamp(record.getId(), context);
                    })
                    .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                    .show();
        });
    }


    @Override
    public int getItemCount() {
        return attendanceRecords.size();
    }

    public void setAttendanceRecords(List<AttendanceRecord> records) {
        attendanceRecords.clear();
        attendanceRecords.addAll(records);
        notifyDataSetChanged();
    }

    private void deleteTimestamp(String id, Context context) {
        if (id == null || id.isEmpty()) {
            Toast.makeText(context, "Invalid timestamp ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        isDeleting = true;
        notifyDataSetChanged();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show();
            isDeleting = false;
            notifyDataSetChanged();
            return;
        }

        firestore.collection("users")
                .document(userId)
                .collection("attendance")
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Timestamp deleted successfully", Toast.LENGTH_SHORT).show();
                    isDeleting = false;
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete timestamp", Toast.LENGTH_SHORT).show();
                    isDeleting = false;
                    notifyDataSetChanged();
                });
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        TextView timestampTextView;
        ImageView deleteButton;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            deleteButton = itemView.findViewById(R.id.delete_button);
            eventNameTextView = itemView.findViewById(R.id.event_name_text_view);
        }
    }
}
