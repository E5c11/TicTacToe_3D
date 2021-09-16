package com.esc.test.apps.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.network.ConnectionLiveData;
import com.esc.test.apps.utils.SingleLiveEvent;
import com.esc.test.apps.repositories.FirebaseUserRepository;
import com.esc.test.apps.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final SingleLiveEvent<Boolean> loggedIn;
    private final SingleLiveEvent<String> error;
    private final ConnectionLiveData network;
    private final MutableLiveData<String> displayNameExists;
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError;
    private final MutableLiveData<String> passConError = new MutableLiveData<>();
    private String displayName;
    private String passCon;
    private String password;
    private String email;
    private boolean login = true;
    private final UserDetails userDetails;
    private final FirebaseUserRepository fbUserRepo;
    private static final String TAG = "myT";

    @Inject
    public LoginViewModel(UserDetails userDetails, FirebaseUserRepository fbUserRepo,
                          ConnectionLiveData network
    ) {
        this.userDetails = userDetails;
        this.fbUserRepo = fbUserRepo;
        this.network = network;
        loggedIn = fbUserRepo.getLoggedIn();
        error = fbUserRepo.getError();
        emailError = fbUserRepo.getEmailError();
        displayNameExists = fbUserRepo.getDisplayNameExists();
        logUserIn();
    }

    private void logUserIn() {
        if (checkExistingUser())
            fbUserRepo.connectLogin(userDetails.getEmail(), userDetails.getPassword());
        else {
            Log.d("myT", "first launch");
            loggedIn.setValue(false);
        }
    }

    public void getUserDetails() {
        userDetails.clearPrefs();
        Log.d(TAG, "getUserDetails: ");
        fbUserRepo.connectLogin(email, password);
    }

    private Boolean checkExistingUser() { return userDetails.getEmail() != null; }

    public void isEmailValid(String viewEmail) {
        if (Utils.validEmail(viewEmail)) fbUserRepo.isEmailValid(viewEmail);
        else emailError.setValue("Enter a valid email");
    }

    public void newDisplayName(CharSequence ds) {
        fbUserRepo.checkDisplayNameExist(ds);
    }

    public void submitNewUser() {
        if (!validDisplayName() | !validateEmail() | !validatePassCon()) {
            error.setValue("kill login");
            return;
        } else fbUserRepo.createUser(email, password, displayName);
    }

    public void loginUser() {
        Log.d(TAG, "loginUser: " + !password.isEmpty());
        if (!validateEmail() | password.isEmpty()) {
            error.setValue("kill login");
            return;
        }
        else {
            Log.d(TAG, "loginUser: yes");
            getUserDetails();
        }
    }

    private boolean validDisplayName() {
        String checkDN = displayNameExists.getValue();
        if (displayName == null || displayName.isEmpty()) {
            displayNameExists.setValue("Enter a display name");
            return false;
        } else return !checkDN.equals("Display name already exists");
    }

    private boolean validateEmail() {
        String errorCheck = emailError.getValue();
        Log.d(TAG, "validateEmail: " + email + " " + errorCheck + " " + login);
        if (email == null || email.isEmpty()) {
            emailError.setValue("Email cannot be empty");
            return false;
        } else if (errorCheck.equals("This email already exists") && login) {
            Log.d(TAG, "validateEmail: " + true);
            return true;
        }
        else {
            Log.d(TAG, "validateEmail: " + false);
            return errorCheck.equals("This email does not exist") && !login;
        }
    }

    private boolean validatePassCon() {
        if (passCon == null || passCon.isEmpty()) {
            passConError.setValue("Enter a confirmation password");
            return false;
        } else if (!passCon.equals(password)) {
            passConError.setValue("Passwords do not match");
            return false;
        } else {
            passConError.setValue("");
            return true;
        }
    }

    private void validatePassword() { passwordError.setValue(Utils.validatePassword(password)); }

    public void setPassword(String viewPassword) {
        password = viewPassword;
        validatePassword();
        Log.d(TAG, "setPassword: " + viewPassword);
    }

    public void setPassCon(String viewPassCon) {
        passCon = viewPassCon;
        validatePassCon();
        Log.d(TAG, "setPassCon: ");
    }

    public void setLogin(boolean loginPage) { login = loginPage;}

    public void setEmail(String viewEmail) { email = viewEmail; }

    public void setDisplayName(String viewDisplayName) {displayName = viewDisplayName;}

    public LiveData<String> getPasswordError() {return passwordError;}

    public LiveData<String> getPassConError() {return passConError;}

    public LiveData<String> getEmailError() {
        return Transformations.map(emailError, msg -> {
            switch (msg) {
                case "This email already exists":
                    return login ? "" : msg;
                case "This email does not exist":
                    return login ? msg : "";
                case "Enter a valid email":
                case "Email cannot be empty":
                    return msg;
                default:
                    return "an error has occurred, try again";
            }
        });
    }

    public LiveData<Boolean> getLoggedIn() {
        return Transformations.map(loggedIn, s -> {
            Log.d(TAG, "getLoggedIn: " + s);
            return s;
        });
    }

    public LiveData<String> getDisplayNameExists() { return displayNameExists; }

    public LiveData<String> getError() { return error; }

    public LiveData<Boolean> getNetwork() { return network; }

}
