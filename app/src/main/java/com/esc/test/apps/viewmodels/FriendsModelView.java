package com.esc.test.apps.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.data.datastore.UserPreferences;
import com.esc.test.apps.utils.SingleLiveEvent;
import com.esc.test.apps.data.pojos.UserInfo;
import com.esc.test.apps.repositories.FirebaseGameRepository;
import com.esc.test.apps.utils.Utils;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class FriendsModelView extends ViewModel {

    private final FirebaseGameRepository fbGameRepo;
    public final SingleLiveEvent<UserInfo> newFriend;
    public final SingleLiveEvent<String[]> startGame;
    public LiveData<List<UserInfo>> friends;
    public LiveData<List<UserInfo>> requests;
    private UserPreferences userPref;
    private Disposable d;
    public static final String TAG = "myT";

    @Inject
    public FriendsModelView(FirebaseGameRepository fbGameRepo, UserPreferences userPref) {
        this.fbGameRepo = fbGameRepo;
        newFriend = fbGameRepo.newFriend;
        startGame = fbGameRepo.startGame;
        d = userPref.getUserPreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
            friends = fbGameRepo.getActiveFriends(pref.getUid());
            requests = fbGameRepo.getFriendRequests(pref.getUid());
            Utils.dispose(d);
        }).subscribe();
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

    public void sendGameInvite(UserInfo user, boolean startGame) {
        fbGameRepo.sendGameInvite(user, startGame);
    }

    public void inviteNewFriend() {
        fbGameRepo.inviteNewFriend();
    }
}
