package com.example.cardiniamensshed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AttendanceFragment extends Fragment {

    private RecyclerView attendanceRecyclerView;
    private Button addTimestampButton;
    private TextView emptyStateText;
    private ProgressBar loadingIndicator;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private AttendanceAdapter attendanceAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        attendanceRecyclerView = view.findViewById(R.id.attendance_recycler_view);
        addTimestampButton = view.findViewById(R.id.add_timestamp_button);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        loadingIndicator = view.findViewById(R.id.loading_indicator);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        setupAddTimestampButton();

        return view;
    }

    private void setupRecyclerView() {
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        attendanceAdapter = new AttendanceAdapter();
        attendanceRecyclerView.setAdapter(attendanceAdapter);

        loadAttendanceRecords();
    }

    private void loadAttendanceRecords() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference attendanceRef = firestore.collection("users")
                .document(userId)
                .collection("attendance");

        loadingIndicator.setVisibility(View.VISIBLE);

        attendanceRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    loadingIndicator.setVisibility(View.GONE);

                    if (e != null) {
                        Toast.makeText(getContext(), "Error loading attendance records", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        emptyStateText.setVisibility(View.GONE);
                        attendanceRecyclerView.setVisibility(View.VISIBLE);

                        List<AttendanceRecord> records = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            AttendanceRecord record = new AttendanceRecord();

                            record.setId(doc.getId());
                            record.setTimestamp(doc.getString("timestamp"));
                            record.setType(doc.getString("type"));
                            record.setEventId(doc.getString("eventId"));
                            record.setEventName(doc.getString("eventName"));
                            record.setEventDescription(doc.getString("eventDescription"));

                            // Optional debug log
                            Log.d("AttendanceFragment", "Loaded: timestamp=" + record.getTimestamp() + ", eventName=" + record.getEventName());

                            records.add(record);
                        }

                        attendanceAdapter.setAttendanceRecords(records);
                    } else {
                        emptyStateText.setVisibility(View.VISIBLE);
                        attendanceRecyclerView.setVisibility(View.GONE);
                    }
                });
    }


    private void setupAddTimestampButton() {
        addTimestampButton.setOnClickListener(v -> {
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

            if (userId == null) {
                Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sample event data
            String eventId = "event123";
            String eventName = "Monthly Gathering";
            String eventDescription = "Our regular monthly get-together.";

            CollectionReference attendanceRef = firestore.collection("users")
                    .document(userId)
                    .collection("attendance");

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("timestamp", timestamp);
            recordMap.put("type", "join");
            recordMap.put("eventId", eventId);
            recordMap.put("eventName", eventName);
            recordMap.put("eventDescription", eventDescription);

            loadingIndicator.setVisibility(View.VISIBLE);

            attendanceRef.add(recordMap)
                    .addOnSuccessListener(documentReference -> {
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Attendance added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to add attendance", Toast.LENGTH_SHORT).show();
                    });
        });
    }

}
