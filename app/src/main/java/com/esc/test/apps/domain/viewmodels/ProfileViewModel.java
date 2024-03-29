package com.esc.test.apps.domain.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.common.network.ConnectionLiveData;
import com.esc.test.apps.common.utils.Utils;
import com.esc.test.apps.data.repositories.FbUserRepo;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final FbUserRepo fbUserRepo;
    private final ConnectionLiveData network;
    private final MediatorLiveData<String> editTextError = new MediatorLiveData<>();
    private int type;
    private String password;

    @Inject
    public ProfileViewModel(FbUserRepo fbUserRepo, ConnectionLiveData network) {
        this.fbUserRepo = fbUserRepo;
        this.network = network;
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

    public LiveData<Boolean> getNetwork() { return network; }

}
