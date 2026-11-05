package com.example.cardiniamensshed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messageList;
    private final String currentUserId;
    private final boolean isOwner;
    private final OnPurchaseClickListener purchaseClickListener;

    public interface OnPurchaseClickListener {
        void onPurchaseClick(int quantity);
    }

    public ChatAdapter(List<ChatMessage> messageList, String currentUserId, boolean isOwner,
                       OnPurchaseClickListener purchaseClickListener) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.isOwner = isOwner;
        this.purchaseClickListener = purchaseClickListener;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView senderName, messageText;
        ImageView profileImage;

        public ChatViewHolder(View view) {
            super(view);
            senderName = view.findViewById(R.id.sender_name);
            messageText = view.findViewById(R.id.message_text);
            profileImage = view.findViewById(R.id.profile_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSender().equals(currentUserId) ? 1 : 0;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == 1 ? R.layout.item_chat_right : R.layout.item_chat_left;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.messageText.setText(message.getMessage());

        // Name
        if (message.getSender().equals(currentUserId)) {
            holder.senderName.setText("You");
        } else if (message.getSenderName() != null && !message.getSenderName().isEmpty()) {
            holder.senderName.setText(message.getSenderName());
        } else {
            holder.senderName.setText("User");
        }

        // Profile Image
        if (message.getProfileImage() != null && !message.getProfileImage().isEmpty()) {
            Glide.with(holder.profileImage.getContext())
                    .load(message.getProfileImage())
                    .circleCrop()
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}