package com.example.cardiniamensshed;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private static final String TAG = "EventsAdapter";

    public interface OnEventClickListener {
        void onEventAction(Event event, boolean isJoining);
        void onEventClicked(Event event);
        void onDeleteEvent(Event event);
    }

    private List<Event> events;
    private List<String> joinedEventIds = new ArrayList<>();
    private OnEventClickListener listener;
    private String currentUserId;

    public EventsAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            this.currentUserId = null;
        }
    }

    public void setJoinedEventIds(List<String> joinedEventIds) {
        this.joinedEventIds = joinedEventIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        boolean isJoined = joinedEventIds.contains(event.getId());
        holder.bind(event, isJoined);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, categoryTextView, ownerTextView, descriptionTextView, timeTextView;
        ImageView eventImageView;
        Button joinLeaveButton, deleteButton;
        Context context;

        EventViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;

            nameTextView = itemView.findViewById(R.id.event_name);
            categoryTextView = itemView.findViewById(R.id.event_category);
            ownerTextView = itemView.findViewById(R.id.event_owner);
            descriptionTextView = itemView.findViewById(R.id.event_description);
            timeTextView = itemView.findViewById(R.id.event_time);
            eventImageView = itemView.findViewById(R.id.event_image);
            joinLeaveButton = itemView.findViewById(R.id.join_leave_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onEventClicked(events.get(pos));
                }
            });
        }

        void bind(Event event, boolean isJoined) {
            nameTextView.setText(event.getName());
            categoryTextView.setText(event.getCategory());
            ownerTextView.setText("Owner: " + event.getOwner());
            descriptionTextView.setText(event.getDescription());

            String formattedDate = new SimpleDateFormat("EEE, dd MMM yyyy 'at' hh:mm a", Locale.getDefault())
                    .format(new Date(event.getTimestamp()));
            timeTextView.setText("Date: " + formattedDate);

            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(event.getImageUrl())
                        .apply(new RequestOptions()
                                .circleCrop()
                                .placeholder(R.drawable.profile_image_background)
                                .error(R.drawable.profile_image_error))
                        .into(eventImageView);
            } else {
                eventImageView.setImageResource(R.drawable.profile_image_background);
            }

            joinLeaveButton.setText(isJoined ? "Leave" : "Join");
            joinLeaveButton.setOnClickListener(v -> listener.onEventAction(event, !isJoined));

            deleteButton.setVisibility(View.VISIBLE); // Always show the delete button
            deleteButton.setOnClickListener(v -> listener.onDeleteEvent(event));
        }
    }
}