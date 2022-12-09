package com.esc.test.apps.domain.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.esc.test.apps.common.utils.SingleLiveEvent;
import com.esc.test.apps.common.utils.Utils;
import com.esc.test.apps.data.models.pojos.UserInfo;
import com.esc.test.apps.data.persistence.UserPreferences;
import com.esc.test.apps.data.repositories.FbGameRepo;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class FriendsModelView extends AndroidViewModel {

    private final FbGameRepo fbGameRepo;
    public final SingleLiveEvent<UserInfo> newFriend;
    public final SingleLiveEvent<String[]> startGame;
    public final SingleLiveEvent<Boolean> listsReady = new SingleLiveEvent<>();
    public LiveData<List<UserInfo>> friends;
    public LiveData<List<UserInfo>> requests;
    private Disposable d;
    public static final String TAG = "myT";

    @Inject
    public FriendsModelView(Application app, FbGameRepo fbGameRepo, UserPreferences userPref) {
        super(app);
        this.fbGameRepo = fbGameRepo;
        newFriend = fbGameRepo.getNewFriend();
        startGame = fbGameRepo.getStartGame();
        d = userPref.getUserPreference().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .doOnNext( pref -> {
                friends = fbGameRepo.getActiveFriends(pref.getUid());
                requests = fbGameRepo.getFriendRequests(pref.getUid());
                listsReady.postValue(true);
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
