package com.example.cardiniamensshed;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class JoinEventFragment extends Fragment implements EventsAdapter.OnEventClickListener {

    private EditText searchField;
    private Spinner spinnerCategory;
    private Button dateButton;
    private RecyclerView eventsRecyclerView;
    private EventsAdapter eventsAdapter;
    private Button createEventButton;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private List<Event> eventsList = new ArrayList<>();
    private List<String> joinedEventIds = new ArrayList<>();
    private Set<String> eventTypes = new HashSet<>();

    private String selectedDate = null;
    private String selectedCategory = null;

    public JoinEventFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_event, container, false);

        searchField = view.findViewById(R.id.search_field);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        dateButton = view.findViewById(R.id.button_pick_date);
        eventsRecyclerView = view.findViewById(R.id.events_recycler_view);
        createEventButton = view.findViewById(R.id.create_event_button);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsAdapter = new EventsAdapter(eventsList, this);
        eventsRecyclerView.setAdapter(eventsAdapter);

        setupCategorySpinner();

        dateButton.setOnClickListener(v -> openDatePicker());

        view.findViewById(R.id.search_button).setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            selectedCategory = spinnerCategory.getSelectedItem().toString();
            fetchEvents(query.isEmpty() ? null : query);
        });

        createEventButton.setOnClickListener(v -> openCreateEventScreen());

        loadJoinedEvents(() -> fetchEvents(null));

        return view;
    }

    private void setupCategorySpinner() {
        firestore.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isAdded()) {
                        eventTypes.clear();
                        eventTypes.add("All");

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String type = doc.getString("type");
                            if (!TextUtils.isEmpty(type)) {
                                eventTypes.add(type.trim());
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(eventTypes));
                        spinnerCategory.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to load categories.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    calendar.set(year1, month1, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());
                    dateButton.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void fetchEvents(@Nullable String query) {
        firestore.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setId(doc.getId());

                        String eventName = event.getName();
                        String eventType = event.getType() != null ? event.getType().trim() : "";
                        String eventCategory = event.getCategory() != null ? event.getCategory().trim() : "";

                        boolean hasValidName = !TextUtils.isEmpty(eventName);

                        boolean matchesSearch = query == null || (
                                (eventName != null && eventName.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))) ||
                                        (eventType.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))) ||
                                        (eventCategory.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))));

                        boolean matchesCategory = selectedCategory == null
                                || selectedCategory.equalsIgnoreCase("All")
                                || (eventType.equalsIgnoreCase(selectedCategory.trim()));

                        boolean matchesDate = true;
                        if (selectedDate != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String eventDate = sdf.format(new java.util.Date(event.getTimestamp()));
                            matchesDate = selectedDate.equals(eventDate);
                        }

                        if (hasValidName && matchesSearch && matchesCategory && matchesDate) {
                            eventsList.add(event);
                        }
                    }

                    if (eventsList.isEmpty()) {
                        if (isAdded() && getContext() != null) { // Ensure context is valid
                            Toast.makeText(getContext(), "No events found for the selected criteria.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    eventsAdapter.setJoinedEventIds(joinedEventIds);
                    eventsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (isAdded() && getContext() != null) { // Ensure context is valid
                        Toast.makeText(getContext(), "Failed to fetch events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadJoinedEvents(final Runnable afterLoad) {
        String userId = getCurrentUserId();
        if (userId == null) {
            afterLoad.run();
            return;
        }

        firestore.collection("user_events").document(userId).collection("joined_events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    joinedEventIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        joinedEventIds.add(doc.getId());
                    }
                    afterLoad.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load joined events.", Toast.LENGTH_SHORT).show();
                    afterLoad.run();
                });
    }

    // Update the onEventAction method in JoinEventFragment.java
    @Override
    public void onEventAction(Event event, boolean isJoining) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String action = isJoining ? "join" : "leave";
        String message = isJoining ? "Join Event?" : "Leave Event?";
        String confirmText = isJoining ? "Join" : "Leave";

        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(message)
                .setContentText("Are you sure you want to " + action + " this event?")
                .setConfirmText(confirmText)
                .setCancelText("No")
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                    // First record the timestamp
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    Map<String, Object> recordMap = new HashMap<>();
                    recordMap.put("timestamp", timestamp);
                    recordMap.put("type", action);
                    recordMap.put("eventId", event.getId());
                    recordMap.put("eventName", event.getName());
                    recordMap.put("eventDescription", event.getDescription());

                    firestore.collection("users")
                            .document(userId)
                            .collection("attendance")
                            .add(recordMap)
                            .addOnSuccessListener(documentReference -> {
                                // After recording timestamp, update the join status
                                if (isJoining) {
                                    firestore.collection("user_events").document(userId).collection("joined_events")
                                            .document(event.getId()).set(event)
                                            .addOnSuccessListener(aVoid -> {
                                                joinedEventIds.add(event.getId());
                                                eventsAdapter.notifyDataSetChanged();
                                                Toast.makeText(getContext(), "Joined the event successfully.", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to join the event.", Toast.LENGTH_SHORT).show());
                                } else {
                                    firestore.collection("user_events").document(userId).collection("joined_events")
                                            .document(event.getId()).delete()
                                            .addOnSuccessListener(aVoid -> {
                                                joinedEventIds.remove(event.getId());
                                                eventsAdapter.notifyDataSetChanged();
                                                Toast.makeText(getContext(), "Left the event successfully.", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to leave the event.", Toast.LENGTH_SHORT).show());
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to record attendance.", Toast.LENGTH_SHORT).show());
                })
                .show();
    }


    @Override
    public void onDeleteEvent(Event event) {
        String currentUserId = getCurrentUserId();

        if (currentUserId != null && currentUserId.equals(event.getOwner())) {
            new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Delete Event?")
                    .setContentText("Are you sure you want to delete this event?")
                    .setConfirmText("Yes")
                    .setCancelText("No")
                    .showCancelButton(true)
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        // Delete from main events collection
                        firestore.collection("events").document(event.getId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Delete from all users' joined_events
                                    firestore.collection("user_events").get()
                                            .addOnSuccessListener(querySnapshot -> {
                                                for (DocumentSnapshot userDoc : querySnapshot) {
                                                    userDoc.getReference().collection("joined_events")
                                                            .document(event.getId())
                                                            .delete();
                                                }
                                                // Delete all chat messages for this event
                                                firestore.collection("event_chats")
                                                        .document(event.getId())
                                                        .collection("messages")
                                                        .get()
                                                        .addOnSuccessListener(messagesQuery -> {
                                                            for (DocumentSnapshot messageDoc : messagesQuery) {
                                                                messageDoc.getReference().delete();
                                                            }
                                                            // Update UI
                                                            eventsList.remove(event);
                                                            eventsAdapter.notifyDataSetChanged();
                                                            Toast.makeText(getContext(),
                                                                    "Event deleted successfully.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        });
                                            });
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(),
                                        "Failed to delete the event.",
                                        Toast.LENGTH_SHORT).show());
                    })
                    .show();
        } else {
            new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Not Allowed")
                    .setContentText("You are not allowed to delete this event.")
                    .setConfirmText("OK")
                    .show();
        }
    }

    @Override
    public void onEventClicked(Event event) {
        // Navigate to EventDetailsFragment when an event is clicked
        EventDetailsFragment detailsFragment = EventDetailsFragment.newInstance(event.getId());
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, detailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void refreshEvents() {
        loadJoinedEvents(() -> fetchEvents(null));
    }

    private void openCreateEventScreen() {
        BookEventFragment bookEventFragment = new BookEventFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, bookEventFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}