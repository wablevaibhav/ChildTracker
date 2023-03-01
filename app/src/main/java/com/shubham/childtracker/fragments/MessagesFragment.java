package com.shubham.childtracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.shubham.childtracker.R;
import com.shubham.childtracker.Adapters.MessageAdapter;
import com.shubham.childtracker.models.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import static com.shubham.childtracker.Activities.ParentSignedInActivity.CHILD_EMAIL_EXTRA;
import static com.shubham.childtracker.Activities.ParentSignedInActivity.CHILD_MESSAGES_EXTRA;

public class MessagesFragment extends Fragment {
    private static final String TAG = "MessagesFragmentTAG";
    private DatabaseReference databaseReference;
    private ArrayList<Message> messagesList;
    private String childEmail;
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private TextView txtNoMessages;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        txtNoMessages = view.findViewById(R.id.txtNoMessages);

        getData();

        if (messagesList.isEmpty()) {
            txtNoMessages.setVisibility(View.VISIBLE);
            recyclerViewMessages.setVisibility(View.GONE);
        } else {
            txtNoMessages.setVisibility(View.GONE);
            recyclerViewMessages.setVisibility(View.VISIBLE);
            recyclerViewMessages.setHasFixedSize(true);
            recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));

            messagesList.sort(Collections.reverseOrder());    //descending order
            initializeAdapter(messagesList);
            initializeItemTouchHelper();
        }


    }

    private void getData() {
        Bundle bundle = requireActivity().getIntent().getExtras();
        if (bundle != null) {
            @SuppressWarnings("unchecked")
            HashMap<String, Message> messages = (HashMap<String, Message>) bundle.getSerializable(CHILD_MESSAGES_EXTRA);
            messagesList = new ArrayList<>(messages.values());
            childEmail = bundle.getString(CHILD_EMAIL_EXTRA);
        }
    }

    private void initializeAdapter(ArrayList<Message> messages) {
        messageAdapter = new MessageAdapter(getContext(), messages);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void initializeItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int position = viewHolder.getAdapterPosition();

                deleteMessage(messagesList.get(position));
                messagesList.remove(position);
                messageAdapter.notifyItemRemoved(position);
                messageAdapter.notifyItemRangeChanged(position, messagesList.size());
                if (messagesList.isEmpty()) {
                    txtNoMessages.setVisibility(View.VISIBLE);
                    recyclerViewMessages.setVisibility(View.GONE);
                }
            }

        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerViewMessages);
    }

    private void deleteMessage(final Message message) {
        Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
                final String key = nodeShot.getKey();
                Log.i(TAG, "onDataChange: key: " + key);
                Query query = databaseReference.child("childs").child(Objects.requireNonNull(key)).child("messages").orderByChild("timeReceived").equalTo(message.getTimeReceived());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
                            snapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
