package com.esc.test.apps.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.esc.test.apps.R;
import com.esc.test.apps.common.adaptors.ActiveFriendsAdapter;
import com.esc.test.apps.common.adaptors.FriendRequestAdapter;
import com.esc.test.apps.data.persistence.UserPreferences;
import com.esc.test.apps.data.models.pojos.UserInfo;
import com.esc.test.apps.databinding.FriendListBinding;
import com.esc.test.apps.databinding.FriendsActivityBinding;
import com.esc.test.apps.domain.viewmodels.FriendsModelView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PlayWithFriend extends Fragment implements ActiveFriendsAdapter.OnClickListener {

    public PlayWithFriend() { super(R.layout.friends_activity); }

    private FriendsModelView friendsModelView;
    private FriendListBinding friendFound;
    private static final String TAG = "myT";
    private FriendsActivityBinding binding;
    private ActiveFriendsAdapter activeAdapter;
    private FriendRequestAdapter requestAdapter;

    @Inject UserPreferences user;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FriendsActivityBinding.bind(view);
        Log.d("myT", "FriendsActivity");

        friendsModelView = new ViewModelProvider(this).get(FriendsModelView.class);
        activeAdapter = new ActiveFriendsAdapter(requireContext(), this, user);
        requestAdapter = new FriendRequestAdapter(requireContext(),this);
        setViews();
        setObservers();
        setListeners();
    }

    private void setViews() {
        activeFriends();
        friendRequests();
        friendFound = binding.inviteFriend;
        friendFound.getRoot().setVisibility(View.GONE);

        binding.friendRequestsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.friendRequestsList.setAdapter(requestAdapter);
    }

    private void foundNewFriend() {
        friendFound.getRoot().setVisibility(View.VISIBLE);
        friendFound.inviteButton.setOnClickListener(v -> friendsModelView.inviteNewFriend());
    }

    private void setObservers() {
        friendsModelView.newFriend.observe(getViewLifecycleOwner(), s -> {
            foundNewFriend();
            if (s.getDisplay_name() != null) friendFound.friendName.setText(s.getDisplay_name());
            if (s.getStatus() != null) friendFound.friendActive.setText(s.getStatus());
            if (s.getProfilePicture() != null)
                Glide.with(this).load(s.getProfilePicture()).into(friendFound.friendPp);
        });
        friendsModelView.startGame.observe(getViewLifecycleOwner(), s -> {
            if (s != null) {
                NavDirections action =
                        PlayWithFriendDirections.actionPlayWithFriendToBoardActivity(s[0], s[1]);
                NavHostFragment.findNavController(this).navigate(action);
            }
        });
        friendsModelView.listsReady.observe(getViewLifecycleOwner(), ready -> {
            if (ready) setListObservers();
        });
    }

    private void setListObservers() {
        friendsModelView.friends.observe(getViewLifecycleOwner(), activeAdapter::submitList);
        friendsModelView.requests.observe(getViewLifecycleOwner(), requestAdapter::submitList);
    }

    private void setListeners() {
        binding.friendSearchButton.setOnClickListener(v -> {
            String input = binding.friendSearch.getEditText().getText().toString().trim();
            friendsModelView.findFriend(input);
        });
        onBackPressed();
    }

    private void activeFriends() {
        binding.activeFriendsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.activeFriendsList.setAdapter(activeAdapter);
    }

    private void friendRequests() {
        binding.friendRequestsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.friendRequestsList.setAdapter(requestAdapter);
    }

    @Override
    public void onItemClick(UserInfo user, String fromList, String btnText) {
        if (fromList.equals(ActiveFriendsAdapter.ACTIVE_LIST)) {
            switch (btnText) {
                case "Start":
                    friendsModelView.startGame(user, false);
                    break;
                case "Accept":
                    friendsModelView.startGame(user, true);
                    break;
                case "Cancel":
                    friendsModelView.sendGameInvite(user, false);
                    break;
                case "Play":
                    friendsModelView.sendGameInvite(user, true);
                    break;
            }
        } else friendsModelView.acceptInvite(user);
    }
    private void onBackPressed() {
        requireActivity().getOnBackPressedDispatcher()
            .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    NavDirections action = PlayWithFriendDirections.actionPlayWithFriendToHome();
                    NavHostFragment.findNavController(PlayWithFriend.this).navigate(action);
                }
            });
    }
}