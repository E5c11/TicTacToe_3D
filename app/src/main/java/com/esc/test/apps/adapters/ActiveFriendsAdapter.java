package com.esc.test.apps.adapters;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.esc.test.apps.R;
import com.esc.test.apps.databinding.FriendListBinding;

import com.esc.test.apps.pojos.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ActiveFriendsAdapter extends
        ListAdapter<UserInfo, RequestHolder> {

    private final Application app;
    private final OnClickListener listener;
    public static final String ACTIVE_LIST = "active";
    private final  DatabaseReference ref;

    public ActiveFriendsAdapter(Application app, OnClickListener listener, DatabaseReference ref) {
        super(diffCallback);
        this.app = app;
        this.listener = listener;
        this.ref = ref;
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FriendListBinding binding = FriendListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestHolder(binding, listener, app, ref);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestHolder holder, int pos) {
        UserInfo user = getItem(pos);
        holder.bind(user, ACTIVE_LIST);
        holder.bindStatus(user.getUid());
        if (user.getActive_game() != null)
            holder.bindButton(app.getResources().getString(R.string.start));
        else if (user.getGame_request())
            holder.bindButton(app.getResources().getString(R.string.accept));
        else if (!user.getGame_invite() && !user.getGame_request())
            holder.bindButton(app.getResources().getString(R.string.play));
//        else if (getItem(pos).getActive_game() != null) holder.binding.inviteButton.setText(app.getResources().getString(R.string.continue_game));
        else if (user.getGame_invite())
            holder.bindButton(app.getResources().getString(R.string.cancel));
        if (user.getStarter() != null) {
            if (user.getStarter().equals(app.getResources().getString(R.string.opponent)))
                holder.bindStarter(app.getResources().getString(R.string.their_turn));
            else
                holder.bindStarter(app.getResources().getString(R.string.your_turn));
        }
    }

    public interface OnClickListener  {
        void onItemClick(UserInfo userInfo, String list, String btnText);
    }

    public static final DiffUtil.ItemCallback<UserInfo> diffCallback = new DiffUtil.ItemCallback<UserInfo>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserInfo oldItem, @NonNull UserInfo newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserInfo oldItem, @NonNull UserInfo newItem) {
            return oldItem.getActive_game().equals(newItem.getActive_game());
        }
    };
}

class RequestHolder extends RecyclerView.ViewHolder {

    private final FriendListBinding binding;
    private final ActiveFriendsAdapter.OnClickListener listener;
    private final Application app;
    private DatabaseReference ref;

    public RequestHolder(FriendListBinding binding, ActiveFriendsAdapter.OnClickListener listener,
                         Application app, DatabaseReference ref) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;
        this.app = app;
        this.ref = ref;
    }
    public RequestHolder(FriendListBinding binding, ActiveFriendsAdapter.OnClickListener listener,
                         Application app) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;
        this.app = app;
    }
    void bind(UserInfo user, String list) {
        binding.friendName.setText(user.getDisplay_name());
        binding.inviteButton.setOnClickListener(v ->
                listener.onItemClick(user, list, binding.inviteButton.getText().toString()) );

    }
    void bindStatus(String uid) {
        ref.child(uid).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String status;
                    if (snapshot.getValue().equals(app.getString(R.string.online)))
                        status = app.getString(R.string.online);
                    else status = app.getString(R.string.offline);
                    binding.friendActive.setText(status);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    void bindButton(String text) {
        binding.inviteButton.setText(text);
    }
    void bindStarter(String text) {
        binding.friendActive.setText(text);
    }
}
