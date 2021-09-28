package com.esc.test.apps.viewmodels;

import android.app.Application;

import androidx.lifecycle.ViewModel;

import com.esc.test.apps.repositories.FirebaseGameRepository;
import com.esc.test.apps.repositories.FirebaseUserRepository;
import com.esc.test.apps.utils.AlertType;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DialogViewModel extends ViewModel {

    private final Application app;
    private final FirebaseUserRepository fbUserRepo;
    private final FirebaseGameRepository fbGameRepo;
    private AlertType type;

    @Inject
    public DialogViewModel(Application app, FirebaseUserRepository fbUserRepo,
                           FirebaseGameRepository fbGameRepo
    ) {
        this.app = app;
        this.fbUserRepo = fbUserRepo;
        this.fbGameRepo = fbGameRepo;
    }

    private void confirmQuit() {
        fbGameRepo.endGame(null);
    }

    private void confirmDelete() {

    }

    private void changePassword() {

    }

    private void changeEmail() {

    }

    private void changeDisplayName() {

    }

    public void checkAction() {
        switch (type) {
            case PASSWORD:
                changePassword();
                break;
            case DISPLAY_NAME:
                changeDisplayName();
                break;
            case EMAIL:
                changeEmail();
                break;
            case DELETE:
                confirmDelete();
                break;
            case QUIT:
                confirmQuit();
                break;
        }
    }

    public void setType(AlertType type) {
        this.type = type;
    }

}
