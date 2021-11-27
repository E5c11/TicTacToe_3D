package com.esc.test.apps.viewmodels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.repositories.FirebaseGameRepository;
import com.esc.test.apps.repositories.FirebaseUserRepository;
import com.esc.test.apps.utils.AlertType;
import com.esc.test.apps.utils.Utils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DialogViewModel extends ViewModel {

    private final Application app;
    private final FirebaseUserRepository fbUserRepo;
    private final FirebaseGameRepository fbGameRepo;
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;
    public final LiveData<String> remoteError;
    private AlertType type;
    private String input;

    @Inject
    public DialogViewModel(Application app, FirebaseUserRepository fbUserRepo,
                           FirebaseGameRepository fbGameRepo
    ) {
        this.app = app;
        this.fbUserRepo = fbUserRepo;
        this.fbGameRepo = fbGameRepo;
        remoteError = fbUserRepo.getError();
    }

    private void confirmQuit() {
        fbGameRepo.endGame(null);
    }

    private void confirmDelete() {

    }

    private void changePassword() {
        fbUserRepo.updatePassword(input);
    }

    private void changeEmail() {
        fbUserRepo.updateEmail(input);
    }

    private void changeDisplayName() {

    }

    public void checkAction(boolean submit) {
        switch (type) {
            case PASSWORD:
                String result = Utils.validatePassword(input);
                if (submit && result.equals("")) changePassword();
                else _error.postValue(result);
                break;
            case EMAIL:
                if (submit && Utils.validEmail(input)) changeEmail();
                else  _error.postValue("Please enter a valid email");
                break;
            case DISPLAY_NAME:
                changeDisplayName();
                break;
            case DELETE:
                confirmDelete();
                break;
            case QUIT:
                confirmQuit();
                break;
        }
    }

    public void setType(AlertType type) { this.type = type; }

    public void setInput(String input) { this.input = input; }

}
