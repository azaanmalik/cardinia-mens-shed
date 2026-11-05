package com.example.cardiniamensshed;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EventDetailsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";

    private String eventId;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private Event currentEvent;

    private ImageView eventImage;
    private TextView eventName, eventCategory, eventDate, eventTime, eventLocation, eventDescription;
    private MaterialButton joinEventButton, deleteButton;

    private boolean isJoined = false;

    public static EventDetailsFragment newInstance(String eventId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        eventImage = view.findViewById(R.id.event_image);
        eventName = view.findViewById(R.id.event_name);
        eventCategory = view.findViewById(R.id.event_category);
        eventDate = view.findViewById(R.id.event_date);
        eventTime = view.findViewById(R.id.event_time);
        eventLocation = view.findViewById(R.id.event_location);
        eventDescription = view.findViewById(R.id.event_description);
        joinEventButton = view.findViewById(R.id.join_event_button);
        deleteButton = view.findViewById(R.id.delete_button);

        loadEventDetails();

        return view;
    }

    private void loadEventDetails() {
        if (TextUtils.isEmpty(eventId)) {
            return;
        }

        DocumentReference eventRef = firestore.collection("events").document(eventId);
        eventRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentEvent = documentSnapshot.toObject(Event.class);
                        if (currentEvent != null) {
                            eventName.setText(currentEvent.getName());
                            eventCategory.setText(currentEvent.getCategory());
                            eventDescription.setText(currentEvent.getDescription());

                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
                            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                            Date eventDateObj = new Date(currentEvent.getTimestamp());
                            eventDate.setText(dateFormat.format(eventDateObj));
                            eventTime.setText(timeFormat.format(eventDateObj));

                            if (!TextUtils.isEmpty(currentEvent.getType())) {
                                eventLocation.setText(currentEvent.getType());
                            } else {
                                eventLocation.setText("N/A");
                            }

                            if (!TextUtils.isEmpty(currentEvent.getImageUrl())) {
                                Glide.with(requireContext())
                                        .load(currentEvent.getImageUrl())
                                        .apply(new RequestOptions()
                                                .placeholder(R.drawable.profile_image_background)
                                                .error(R.drawable.profile_image_error)
                                                .centerCrop())
                                        .into(eventImage);
                            } else {
                                eventImage.setImageResource(R.drawable.profile_image_background);
                            }

                            setupButtons();
                        }
                        checkIfJoined();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load event details.", Toast.LENGTH_SHORT).show());
    }

    private void setupButtons() {
        joinEventButton.setOnClickListener(v -> showJoinLeaveDialog());

        if (auth.getCurrentUser() != null && auth.getCurrentUser().getUid().equals(currentEvent.getOwner())) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> showDeleteConfirmation());
        } else {
            deleteButton.setVisibility(View.GONE);
        }
    }

    private void showJoinLeaveDialog() {
        new SweetAlertDialog(requireContext(), isJoined ? SweetAlertDialog.WARNING_TYPE : SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(isJoined ? "Leave Event?" : "Join Event?")
                .setContentText(isJoined ? "Are you sure you want to leave this event?" : "Are you sure you want to join this event?")
                .setConfirmText(isJoined ? "Leave" : "Join")
                .setCancelText("Cancel")
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    recordAttendanceAndToggleJoinState();
                })
                .show();
    }

    private void recordAttendanceAndToggleJoinState() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null || eventId == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String action = isJoined ? "leave" : "join";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create attendance record
        Map<String, Object> recordMap = new HashMap<>();
        recordMap.put("timestamp", timestamp);
        recordMap.put("type", action);
        recordMap.put("eventId", eventId);
        recordMap.put("eventName", currentEvent.getName());
        recordMap.put("eventDescription", currentEvent.getDescription());

        // First record the attendance
        firestore.collection("users")
                .document(userId)
                .collection("attendance")
                .add(recordMap)
                .addOnSuccessListener(documentReference -> {
                    // Then update the join status
                    CollectionReference userEventsRef = firestore.collection("user_events").document(userId).collection("joined_events");

                    if (isJoined) {
                        userEventsRef.document(eventId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    isJoined = false;
                                    updateJoinButton();
                                    syncWithJoinEventFragment();
                                    Toast.makeText(getContext(), "You have left the event.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to leave the event.", Toast.LENGTH_SHORT).show());
                    } else {
                        userEventsRef.document(eventId).set(currentEvent)
                                .addOnSuccessListener(aVoid -> {
                                    isJoined = true;
                                    updateJoinButton();
                                    syncWithJoinEventFragment();
                                    Toast.makeText(getContext(), "You have joined the event.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to join the event.", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to record attendance.", Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmation() {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Delete Event")
                .setContentText("Are you sure you want to permanently delete this event?")
                .setConfirmText("Delete")
                .setCancelText("Cancel")
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    deleteEvent();
                })
                .show();
    }

    private void deleteEvent() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            showDeleteError("User not logged in");
            return;
        }

        // First record the deletion timestamp (existing functionality)
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Map<String, Object> recordMap = new HashMap<>();
        recordMap.put("timestamp", timestamp);
        recordMap.put("type", "delete");
        recordMap.put("eventId", eventId);
        recordMap.put("eventName", currentEvent.getName());
        recordMap.put("eventDescription", currentEvent.getDescription());

        firestore.collection("users")
                .document(userId)
                .collection("attendance")
                .add(recordMap)
                .addOnSuccessListener(documentReference -> {
                    // After recording timestamp, delete from all places
                    firestore.collection("events").document(eventId).delete()
                            .addOnSuccessListener(aVoid -> {
                                // Delete from all users' joined_events
                                firestore.collection("user_events").get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            for (DocumentSnapshot userDoc : querySnapshot) {
                                                userDoc.getReference().collection("joined_events")
                                                        .document(eventId)
                                                        .delete();
                                            }
                                            // Delete all chat messages for this event
                                            firestore.collection("event_chats")
                                                    .document(eventId)
                                                    .collection("messages")
                                                    .get()
                                                    .addOnSuccessListener(messagesQuery -> {
                                                        for (DocumentSnapshot messageDoc : messagesQuery) {
                                                            messageDoc.getReference().delete();
                                                        }
                                                        showDeleteSuccess();
                                                    });
                                        });
                            })
                            .addOnFailureListener(e -> showDeleteError(e.getMessage()));
                })
                .addOnFailureListener(e -> showDeleteError("Failed to record deletion timestamp"));
    }

    private void showDeleteSuccess() {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Deleted!")
                .setContentText("Event has been successfully deleted")
                .setConfirmText("OK")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    requireActivity().onBackPressed();
                    syncWithJoinEventFragment();
                })
                .show();
    }

    private void showDeleteError(String message) {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("Failed to delete event: " + message)
                .show();
    }

    private void checkIfJoined() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null || eventId == null) return;

        firestore.collection("user_events").document(userId).collection("joined_events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isJoined = documentSnapshot.exists();
                    updateJoinButton();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to check event status.", Toast.LENGTH_SHORT).show());
    }

    private void updateJoinButton() {
        joinEventButton.setText(isJoined ? "Leave" : "Join");
    }

    private void syncWithJoinEventFragment() {
        if (getActivity() instanceof DashboardActivity) {
            ((DashboardActivity) getActivity()).reloadJoinEventFragment();
        }
    }
}