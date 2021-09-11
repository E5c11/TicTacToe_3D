package com.esc.test.apps.modelviews;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.utils.SingleLiveEvent;
import com.esc.test.apps.pojos.UserInfo;
import com.esc.test.apps.repositories.FirebaseGameRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FriendsModelView extends ViewModel {

    private final FirebaseGameRepository fbGameRepo;
    private final SingleLiveEvent<UserInfo> newFriend;
    private final SingleLiveEvent<String[]> startGame;
    private final LiveData<List<UserInfo>> friends;
    private final LiveData<List<UserInfo>> requests;
    public static final String TAG = "myT";

    @Inject
    public FriendsModelView(FirebaseGameRepository fbGameRepo) {
        this.fbGameRepo = fbGameRepo;
        newFriend = fbGameRepo.getNewFriend();
        startGame = fbGameRepo.getStartGame();
        friends = fbGameRepo.getActiveFriends();
        requests = fbGameRepo.getFriendRequests();
    }

    public void findFriend(String friend_name) {
        fbGameRepo.findFriend(friend_name);
    }

    public void acceptInvite(UserInfo user) {
        fbGameRepo.acceptInvite(user);
    }

    public void startGame(UserInfo user, boolean firstPlayer) {
        fbGameRepo.startGame(user, firstPlayer);
    }

    public void changeInviteState(String uid) {
        fbGameRepo.changeInviteState(uid);
    }

    public void sendGameInvite(UserInfo user, boolean startGame) {
        fbGameRepo.sendGameInvite(user, startGame);
    }

    public LiveData<List<UserInfo>> getActiveFriends() {
        return friends;
    }

    public LiveData<List<UserInfo>> getFriendRequests() {
        return requests;
    }

    public void inviteNewFriend() {
        fbGameRepo.inviteNewFriend();
    }

    public SingleLiveEvent<UserInfo> getNewFriend() {return newFriend;}

    public SingleLiveEvent<String[]> getStartGame() {return startGame;}
}
