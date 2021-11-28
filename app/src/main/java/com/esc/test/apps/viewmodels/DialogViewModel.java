package com.esc.test.apps.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
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
    private final MediatorLiveData<String> _error = new MediatorLiveData<>();
    public final LiveData<String> error = _error;
    private AlertType type;
    private String input;

    @Inject
    public DialogViewModel(Application app, FirebaseUserRepository fbUserRepo,
                           FirebaseGameRepository fbGameRepo
    ) {
        this.app = app;
        this.fbUserRepo = fbUserRepo;
        this.fbGameRepo = fbGameRepo;
        addErrorObservers();
    }

    private void addErrorObservers() {
        _error.addSource(fbUserRepo.getEmailError(), error -> {
            if (error.equals("This email does not exist")) _error.postValue("");
            else _error.postValue(error);
        });
        _error.addSource(fbUserRepo.getDisplayNameExists(), _error::postValue);
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
        fbUserRepo.updateDisplayName(input);
    }

    public void checkAction(boolean submit) {
        switch (type) {
            case PASSWORD:
                String result = Utils.validatePassword(input);
                if (submit && result.equals("")) changePassword();
                else _error.postValue(result);
                break;
            case EMAIL:
                boolean validEmail = Utils.validEmail(input);
                if (submit && validEmail) changeEmail();
                else if (validEmail) fbUserRepo.isEmailValid(input);
                else  _error.postValue("Please enter a valid email");
                break;
            case DISPLAY_NAME:
                if (input.length() > 2) fbUserRepo.checkDisplayNameExist(input);
                else if (submit) changeDisplayName();
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