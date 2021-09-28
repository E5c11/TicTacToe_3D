package com.esc.test.apps.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.repositories.FirebaseUserRepository;
import com.esc.test.apps.utils.Utils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final FirebaseUserRepository fbUserRepo;
    private final MediatorLiveData<String> editTextError = new MediatorLiveData<>();
    private int type;
    private String password;

    @Inject
    public ProfileViewModel(FirebaseUserRepository fbUserRepo) {
        this.fbUserRepo = fbUserRepo;
        editTextError.addSource(fbUserRepo.getDisplayNameExists(), editTextError::setValue);
        editTextError.addSource(fbUserRepo.getEmailError(), editTextError::setValue);
    }

    private void checkDisplayName(CharSequence ds) { fbUserRepo.checkDisplayNameExist(ds); }

    private void checkEmail(CharSequence email) {
        if (Utils.validEmail(email.toString())) fbUserRepo.isEmailValid(email.toString());
        else editTextError.setValue("Enter a valid email");
    }

    private void checkPassword(CharSequence pass) {
        editTextError.setValue(Utils.validatePassword(pass.toString()));
    }

    public void setEditType(int inputType) { type = inputType; }

    private boolean validatePassCon(String passCon) {
        if (passCon == null || passCon.isEmpty()) {
            editTextError.setValue("Enter a confirmation password");
            return false;
        } else if (!passCon.equals(password)) {
            editTextError.setValue("Passwords do not match");
            return false;
        } else {
            editTextError.setValue("");
            return true;
        }
    }

    public void changeDisplayName() {

    }

    public void changeEmail() {

    }

    public void changePassword() {

    }

    public void deleteAccount() {

    }

    public void checkDetails(CharSequence details) {
        switch (type) {
            case Utils.TEXT_INPUT:
                checkDisplayName(details);
                break;
            case Utils.EMAIL_INPUT:
                checkEmail(details);
                break;
            case Utils.PASSWORD_INPUT:
                checkPassword(details);
                break;
        }
    }

    public LiveData<String> getEditTextError() { return editTextError; }

}
