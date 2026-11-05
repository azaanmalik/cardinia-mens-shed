package com.example.cardiniamensshed;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChatRoomFragment extends Fragment implements ChatAdapter.OnPurchaseClickListener {

    private Spinner eventSpinner;
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageView sendButton;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    private String selectedEventId;
    private String currentUserId;
    private boolean isOwner = false;
    private Event currentEvent;

    private ListenerRegistration messagesListener;
    private ListenerRegistration joinedEventsListener;
    private ListenerRegistration eventExistenceListener;

    private List<Event> joinedEvents = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    private Set<String> bannedUserIds = new HashSet<>();
    private boolean isBanned = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);

        eventSpinner = view.findViewById(R.id.community_spinner);
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId, isOwner, this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        setupSpinner();
        setupSendButton();
        loadJoinedEvents();

        return view;
    }

    private void setupSpinner() {
        spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(spinnerAdapter);

        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < joinedEvents.size()) {
                    Event selectedEvent = joinedEvents.get(position);
                    loadEventChat(selectedEvent.getId());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedEventId = null;
                chatMessages.clear();
                chatAdapter.notifyDataSetChanged();
                messageInput.setEnabled(false);
                sendButton.setEnabled(false);
            }
        });
    }

    private void loadJoinedEvents() {
        joinedEventsListener = firestore.collection("user_events")
                .document(currentUserId)
                .collection("joined_events")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error loading events", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    joinedEvents.clear();
                    List<String> eventNames = new ArrayList<>();

                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Event event = snapshot.toObject(Event.class);
                        if (event != null) {
                            event.setId(snapshot.getId());
                            joinedEvents.add(event);
                            eventNames.add(event.getName());
                        }
                    }

                    updateSpinner(eventNames);

                    if (joinedEvents.size() == 1) {
                        eventSpinner.setSelection(0);
                        loadEventChat(joinedEvents.get(0).getId());
                    } else if (!joinedEvents.isEmpty() && (selectedEventId == null || !containsEventId(selectedEventId))) {
                        eventSpinner.setSelection(0);
                        loadEventChat(joinedEvents.get(0).getId());
                    } else if (joinedEvents.isEmpty()) {
                        chatMessages.clear();
                        chatAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "You haven't joined any events yet", Toast.LENGTH_SHORT).show();
                        messageInput.setEnabled(false);
                        sendButton.setEnabled(false);
                    }
                });
    }

    private boolean containsEventId(String id) {
        for (Event e : joinedEvents) {
            if (e.getId().equals(id)) return true;
        }
        return false;
    }

    private void updateSpinner(List<String> eventNames) {
        spinnerAdapter.clear();
        spinnerAdapter.addAll(eventNames);
        spinnerAdapter.notifyDataSetChanged();
    }

    private void loadEventChat(String eventId) {
        // Only clear when switching event
        chatMessages.clear();
        chatAdapter.notifyDataSetChanged();

        if (messagesListener != null) messagesListener.remove();
        if (eventExistenceListener != null) eventExistenceListener.remove();

        selectedEventId = eventId;
        isBanned = false;
        bannedUserIds.clear();
        messageInput.setEnabled(true);
        sendButton.setEnabled(true);

        firestore.collection("event_chats").document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        firestore.collection("event_chats").document(eventId)
                                .set(new HashMap<String, Object>());
                    }
                    firestore.collection("event_bans")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(banSnapshot -> {
                                if (banSnapshot.exists()) {
                                    List<String> bans = (List<String>) banSnapshot.get("bannedUserIds");
                                    if (bans != null) bannedUserIds = new HashSet<>(bans);
                                }
                                isBanned = bannedUserIds.contains(currentUserId);
                                if (isBanned) {
                                    new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Chatroom does not exist.")
                                            .setContentText("You have been removed/banned from this chatroom.")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(sDialog -> {
                                                sDialog.dismissWithAnimation();
                                                removeNonExistentEvent(eventId);
                                            }).show();
                                    messageInput.setEnabled(false);
                                    sendButton.setEnabled(false);
                                    return;
                                } else {
                                    proceedLoadEventChat(eventId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                proceedLoadEventChat(eventId);
                            });
                });
    }

    private void proceedLoadEventChat(String eventId) {
        eventExistenceListener = firestore.collection("events").document(eventId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error loading event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentEvent = documentSnapshot.toObject(Event.class);
                        if (currentEvent != null) {
                            currentEvent.setId(documentSnapshot.getId());
                            isOwner = currentUserId.equals(currentEvent.getOwner());

                            chatAdapter = new ChatAdapter(chatMessages, currentUserId, isOwner, this);
                            chatRecyclerView.setAdapter(chatAdapter);
                            loadChatMessages();
                        }
                    } else {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Chatroom does not exist.")
                                .setContentText("This event chatroom has been deleted.")
                                .setConfirmText("OK")
                                .setConfirmClickListener(sDialog -> {
                                    sDialog.dismissWithAnimation();
                                    removeNonExistentEvent(eventId);
                                }).show();
                        messageInput.setEnabled(false);
                        sendButton.setEnabled(false);
                    }
                });
    }

    private void removeNonExistentEvent(String eventId) {
        for (int i = 0; i < joinedEvents.size(); i++) {
            if (joinedEvents.get(i).getId().equals(eventId)) {
                joinedEvents.remove(i);
                List<String> eventNames = new ArrayList<>();
                for (Event event : joinedEvents) {
                    eventNames.add(event.getName());
                }
                updateSpinner(eventNames);
                break;
            }
        }
        chatMessages.clear();
        chatAdapter.notifyDataSetChanged();
        messageInput.setEnabled(false);
        sendButton.setEnabled(false);
    }

    // *** SEAMLESS CHAT UI: Do NOT clear chatMessages on send, only here and only once. ***
    private void loadChatMessages() {
        if (messagesListener != null) messagesListener.remove();
        messagesListener = firestore.collection("event_chats")
                .document(selectedEventId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error loading messages", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    chatMessages.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        chatMessages.add(message);
                    }
                    chatAdapter.notifyDataSetChanged();
                    if (!chatMessages.isEmpty()) {
                        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    }
                });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            if (isBanned) {
                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Chatroom does not exist.")
                        .setContentText("You cannot send messages. You are banned from this chatroom.")
                        .setConfirmText("OK")
                        .show();
                return;
            }

            String messageText = messageInput.getText().toString().trim();

            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(getContext(), "Cannot send empty message", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedEventId == null) {
                Toast.makeText(getContext(), "Please select an event", Toast.LENGTH_SHORT).show();
                return;
            }

            sendMessage(messageText);
        });
    }

    private void sendMessage(String messageText) {
        sendMessage(messageText, false);
    }

    private void sendMessage(String messageText, boolean isPurchase) {
        firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener(userDoc -> {
                    String senderName = "User";
                    String profileImage = "";
                    if (userDoc.exists()) {
                        senderName = userDoc.getString("username") != null ? userDoc.getString("username") : "User";
                        profileImage = userDoc.getString("profileImageUrl") != null ? userDoc.getString("profileImageUrl") : "";
                    }

                    Map<String, Object> message = new HashMap<>();
                    message.put("sender", currentUserId);
                    message.put("senderName", senderName);
                    message.put("profileImage", profileImage);
                    message.put("message", messageText);
                    message.put("timestamp", System.currentTimeMillis());
                    message.put("isOwner", isOwner);
                    message.put("isPurchaseMessage", isPurchase);

                    if (isOwner && currentEvent != null) {
                        message.put("productName", currentEvent.getName());
                        message.put("productPrice", currentEvent.getPrice());
                        message.put("productQuantity", currentEvent.getQuantity());
                    }

                    firestore.collection("event_chats")
                            .document(selectedEventId)
                            .collection("messages")
                            .add(message)
                            .addOnSuccessListener(documentReference -> {
                                messageInput.setText(""); // Don't clear chatMessages here!
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                            });
                });
    }




    @Override
    public void onPurchaseClick(int availableQuantity) {
        if (currentEvent == null || isBanned) return;

        if (currentEvent.getQuantity() <= 0) {
            new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("All items sold.")
                    .setContentText("This product is sold out.")
                    .setConfirmText("OK")
                    .show();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_purchase, null);
        EditText quantityInput = dialogView.findViewById(R.id.quantity_input);
        TextView totalPriceText = dialogView.findViewById(R.id.total_price);

        quantityInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int quantity = Integer.parseInt(s.toString());
                    if (quantity > 0 && quantity <= availableQuantity) {
                        double total = quantity * currentEvent.getPrice();
                        totalPriceText.setText(String.format("Total: $%.2f", total));
                    } else {
                        totalPriceText.setText("Total: $0.00");
                    }
                } catch (NumberFormatException e) {
                    totalPriceText.setText("Total: $0.00");
                }
            }
        });

        new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Purchase Product")
                .setCustomView(dialogView)
                .setConfirmText("Proceed to Payment")
                .setCancelText("Cancel")
                .setConfirmClickListener(sDialog -> {
                    try {
                        int quantity = Integer.parseInt(quantityInput.getText().toString());
                        if (quantity <= 0 || quantity > availableQuantity) {
                            Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        sDialog.dismissWithAnimation();
                        showPaymentDialog(quantity);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Please enter valid quantity", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showPaymentDialog(int quantity) {
        View paymentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_payment, null);
        EditText cardNumber = paymentView.findViewById(R.id.card_number);
        EditText expiryDate = paymentView.findViewById(R.id.expiry_date);
        EditText cvv = paymentView.findViewById(R.id.cvv);
        TextView totalPrice = paymentView.findViewById(R.id.total_price);

        double total = quantity * currentEvent.getPrice();
        totalPrice.setText(String.format("Total: $%.2f", total));

        new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Payment Details")
                .setCustomView(paymentView)
                .setConfirmText("Confirm Payment")
                .setCancelText("Cancel")
                .setConfirmClickListener(sDialog -> {
                    if (TextUtils.isEmpty(cardNumber.getText())) {
                        Toast.makeText(getContext(), "Enter card number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(expiryDate.getText())) {
                        Toast.makeText(getContext(), "Enter expiry date", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(cvv.getText())) {
                        Toast.makeText(getContext(), "Enter CVV", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    processPurchase(quantity, cardNumber.getText().toString(), expiryDate.getText().toString(), cvv.getText().toString());
                    sDialog.dismissWithAnimation();
                })
                .show();
    }

    private void processPurchase(int quantity, String cardNumber, String expiry, String cvv) {
        // First, check the latest quantity (avoid race conditions)
        firestore.collection("events").document(selectedEventId)
                .get()
                .addOnSuccessListener(doc -> {
                    int latestQty = doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 0;
                    if (latestQty < quantity) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Not enough items")
                                .setContentText("Not enough quantity available.")
                                .setConfirmText("OK")
                                .show();
                        return;
                    }
                    // Proceed with atomic decrement
                    firestore.runTransaction(transaction -> {
                        DocumentReference eventRef = firestore.collection("events").document(selectedEventId);
                        DocumentSnapshot snapshot = transaction.get(eventRef);
                        Long qty = snapshot.getLong("quantity");
                        if (qty == null || qty < quantity) {
                            throw new RuntimeException("Not enough quantity");
                        }
                        transaction.update(eventRef, "quantity", qty - quantity);
                        return null;
                    }).addOnSuccessListener(aVoid -> {
                        // Store purchase record
                        Map<String, Object> purchase = new HashMap<>();
                        purchase.put("productId", selectedEventId);
                        purchase.put("buyerId", currentUserId);
                        purchase.put("cardNumber", cardNumber);
                        purchase.put("expiry", expiry);
                        purchase.put("cvv", cvv);
                        purchase.put("timestamp", System.currentTimeMillis());
                        purchase.put("quantity", quantity);
                        purchase.put("productName", currentEvent.getName());

                        firestore.collection("purchases")
                                .add(purchase);

                        // Add a purchase message to the chat
                        String purchaseMsg = String.format("Purchased %d units of %s", quantity, currentEvent.getName());
                        sendMessage(purchaseMsg, true);

                        // Optionally, show sold out message
                        firestore.collection("events").document(selectedEventId)
                                .get()
                                .addOnSuccessListener(evDoc -> {
                                    int newQty = evDoc.getLong("quantity") != null ? evDoc.getLong("quantity").intValue() : 0;
                                    if (newQty == 0) {
                                        Map<String, Object> soldOutMsg = new HashMap<>();
                                        soldOutMsg.put("sender", "system");
                                        soldOutMsg.put("message", "All items sold.");
                                        soldOutMsg.put("timestamp", System.currentTimeMillis());
                                        soldOutMsg.put("isOwner", false);
                                        soldOutMsg.put("isPurchaseMessage", false);
                                        firestore.collection("event_chats")
                                                .document(selectedEventId)
                                                .collection("messages")
                                                .add(soldOutMsg);
                                    }
                                });

                        Toast.makeText(getContext(), "Purchase successful!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Purchase failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to check quantity.", Toast.LENGTH_SHORT).show();
                });
    }

    // --- Chatroom Ownership & Permissions ---

    // Allow owner to delete chatroom (event + chat + bans + purchases)
    // And remove users from chatroom (add them to bannedUserIds)
    // UI for these actions should be placed where appropriate (e.g., in an Owner menu or button).
    // Here are utility functions for those features, call these from owner-only buttons.

    private void deleteChatroom() {
        if (!isOwner) return;
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Delete Chatroom?")
                .setContentText("Are you sure you want to delete this chatroom? This cannot be undone.")
                .setConfirmText("Yes, Delete")
                .setCancelText("Cancel")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    // Delete event doc
                    firestore.collection("events").document(selectedEventId).delete();
                    // Delete chat messages
                    firestore.collection("event_chats").document(selectedEventId)
                            .collection("messages")
                            .get().addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot messageDoc : querySnapshot) {
                                    messageDoc.getReference().delete();
                                }
                            });
                    // Delete bans
                    firestore.collection("event_bans").document(selectedEventId).delete();
                    // Delete purchases
                    firestore.collection("purchases")
                            .whereEqualTo("productId", selectedEventId)
                            .get().addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot docu : querySnapshot) {
                                    docu.getReference().delete();
                                }
                            });
                    // Remove event from joined_events of all users
                    firestore.collection("user_events").get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot userDoc : querySnapshot) {
                                    userDoc.getReference().collection("joined_events")
                                            .document(selectedEventId).delete();
                                }
                            });
                    // Remove from spinner
                    removeNonExistentEvent(selectedEventId);
                })
                .show();
    }

    private void removeUserFromChatroom(String userIdToBan) {
        if (!isOwner || userIdToBan == null) return;
        firestore.collection("event_bans")
                .document(selectedEventId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> bans = doc.exists() && doc.get("bannedUserIds") != null
                            ? (List<String>) doc.get("bannedUserIds") : new ArrayList<>();
                    if (!bans.contains(userIdToBan)) {
                        bans.add(userIdToBan);
                        firestore.collection("event_bans")
                                .document(selectedEventId)
                                .set(new HashMap<String, Object>() {{ put("bannedUserIds", bans); }});
                    }
                    // Remove user from joined_events
                    firestore.collection("user_events").document(userIdToBan)
                            .collection("joined_events")
                            .document(selectedEventId)
                            .delete();
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messagesListener != null) messagesListener.remove();
        if (joinedEventsListener != null) joinedEventsListener.remove();
        if (eventExistenceListener != null) eventExistenceListener.remove();
    }
}