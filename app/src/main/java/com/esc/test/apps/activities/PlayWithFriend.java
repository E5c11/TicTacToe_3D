package com.esc.test.apps.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.esc.test.apps.R;
import com.esc.test.apps.adapters.FriendRequestAdapter;
import com.esc.test.apps.databinding.FriendListBinding;
import com.esc.test.apps.databinding.FriendsActivityBinding;

import com.esc.test.apps.adapters.ActiveFriendsAdapter;
import dagger.hilt.android.AndroidEntryPoint;
import com.esc.test.apps.modelviews.FriendsModelView;
import com.esc.test.apps.pojos.UserInfo;
import com.google.firebase.database.DatabaseReference;

import javax.inject.Inject;

@AndroidEntryPoint
public class PlayWithFriend extends AppCompatActivity implements ActiveFriendsAdapter.OnClickListener {

    private FriendsModelView friendsModelView;
    private FriendListBinding friendFound;
    private static final String TAG = "myT";
    private FriendsActivityBinding binding;
    private ActiveFriendsAdapter activeAdapter;
    private FriendRequestAdapter requestAdapter;
    @Inject DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FriendsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d("myT", "FriendsActivity");

        friendsModelView = new ViewModelProvider(this).get(FriendsModelView.class);
        activeAdapter = new ActiveFriendsAdapter(getApplication(), this, ref);
        requestAdapter = new FriendRequestAdapter(getApplication(),this);
        setViews();
        setObservers();
        setListeners();
    }

    private void setViews() {
        activeFriends();
        friendRequests();
        friendFound = binding.inviteFriend;
        friendFound.getRoot().setVisibility(View.GONE);

        binding.friendRequestsList.setLayoutManager(new LinearLayoutManager(this));
        binding.friendRequestsList.setAdapter(requestAdapter);
    }

    private void foundNewFriend() {
        friendFound.getRoot().setVisibility(View.VISIBLE);
        friendFound.inviteButton.setOnClickListener(v -> friendsModelView.inviteNewFriend());
    }

    private void setObservers() {
        friendsModelView.getNewFriend().observe(this, s -> {
            foundNewFriend();
            if (s.getDisplay_name() != null) friendFound.friendName.setText(s.getDisplay_name());
            if (s.getStatus() != null) friendFound.friendActive.setText(s.getStatus());
            if (s.getProfilePicture() != null) Glide.with(this).load(s.getProfilePicture()).into(friendFound.friendPp);
        });
        friendsModelView.getStartGame().observe(this, s -> {
            if (s != null) {
                Intent intent = new Intent(this, BoardActivity.class);
                intent.putExtra("friend_game_piece", s[1]);
                intent.putExtra("game_set_id", s[0]);
                Log.d(TAG, "sending game intent, game id: " + s[0] + " friend starting piece: " + s[1]);
                startActivity(intent);
            }
        });
        friendsModelView.getActiveFriends().observe(this, activeAdapter::submitList);
        friendsModelView.getFriendRequests().observe(this, requestAdapter::submitList);
    }

    private void setListeners() {
        binding.friendSearchButton.setOnClickListener(v -> {
            String input = binding.friendSearch.getEditText().getText().toString().trim();
            Log.d(TAG, "setListeners: " + input);
            friendsModelView.findFriend(input);
        });
    }

    private void activeFriends() {
        binding.activeFriendsList.setLayoutManager(new LinearLayoutManager(this));
        binding.activeFriendsList.setAdapter(activeAdapter);
    }

    private void friendRequests() {
        binding.friendRequestsList.setLayoutManager(new LinearLayoutManager(this));
        binding.friendRequestsList.setAdapter(requestAdapter);
    }

    @Override
    public void onItemClick(UserInfo user, String fromList, String btnText) {
        if (fromList.equals(ActiveFriendsAdapter.ACTIVE_LIST)) {
            int resourceId = this.getResources().
                    getIdentifier(btnText, "string", this.getPackageName());
            switch (resourceId) {
                case R.string.start:
                    friendsModelView.startGame(user, false);
                    break;
                case R.string.accept:
                    friendsModelView.startGame(user, true);
                    break;
                case R.string.cancel:
                    friendsModelView.sendGameInvite(user, false);
                    break;
                case R.string.play:
                    friendsModelView.sendGameInvite(user, true);
            }
        } else friendsModelView.acceptInvite(user);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, Home.class));
    }
}