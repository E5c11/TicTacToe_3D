package com.esc.test.apps.modelviews;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final SingleLiveEvent<Boolean> loggedIn;
    private final SingleLiveEvent<String> error;
    private final LiveData<Boolean> network;
    private final MutableLiveData<String> displayNameExists;
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError;
    private final MutableLiveData<String> passConError = new MutableLiveData<>();
    private String displayName;
    private String passCon;
    private String password;
    private String email;
    private boolean login = true, netState = true;
    private final UserDetails userDetails;
    private final FirebaseUserRepository fbUserRepo;
    private static final String TAG = "myT";

    @Inject
    public LoginViewModel(UserDetails userDetails, FirebaseUserRepository fbUserRepo, Application app) {
        this.userDetails = userDetails;
        this.fbUserRepo = fbUserRepo;
        loggedIn = fbUserRepo.getLoggedIn();
        error = fbUserRepo.getError();
        network = new ConnectionLiveData(app);
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
        fbUserRepo.connectLogin(email, password);
    }

    private Boolean checkExistingUser() { return userDetails.getEmail() != null; }

    public LiveData<Boolean> getLoggedIn() { return loggedIn; }

    public void isEmailValid(String viewEmail) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(viewEmail);
        if (matcher.matches()) fbUserRepo.isEmailValid(viewEmail);
        else emailError.setValue("Enter a valid email");
    }

    public void newDisplayName(CharSequence ds) {
        fbUserRepo.checkDisplayNameExist(ds);
    }

    public void submitNewUser() {
        if (!validDisplayName() | !validateEmail() | !validatePassCon()) return;
        else fbUserRepo.createUser(email, password, displayName);
    }

    public void loginUser() {
        if (!validateEmail() | !validatePassCon()) return;
        else if (login) getUserDetails();
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
        if (email == null || email.isEmpty()) {
            emailError.setValue("Email cannot be empty");
            return false;
        } else if (errorCheck.equals("Enter a valid email")) return false;
        else if (errorCheck.isEmpty()) return true;
        else {
            isEmailValid(email);
            return false;
        }
    }

    private boolean validatePassCon() {
        if (passCon == null || passCon.isEmpty()) {
            passConError.setValue("Enter a confirmation password");
            return false;
        } else if (!passCon.equals(password)) {
            passConError.setValue("Passwords do not match");
            Log.d(TAG, "validatePassCon: don't match");
            return false;
        } else {
            passConError.setValue("");
            return true;
        }
    }

    private void validatePassword() {
        if (password == null || password.isEmpty()) {
            passwordError.setValue("Password cannot be empty");
        } else if (password.equals(password.toLowerCase()) && !password.matches(".*\\d.*")) {
            passwordError.setValue("Password must contain an uppercase and number");
        } else if (password.equals(password.toLowerCase())) {
            passwordError.setValue("Password must contain an uppercase");
        } else if (!password.matches(".*\\d.*")) {
            passwordError.setValue("Password must contain a number");
        } else if (!(password.length() >= 6)) {
            passwordError.setValue("Password must contain at least 6 characters");
        } else {
            passwordError.setValue("");
        }
    }

    public void setPassword(String viewPassword) {
        password = viewPassword;
        validatePassword();
    }

    public void setPassCon(String viewPassCon) {
        passCon = viewPassCon;
        validatePassCon();
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

    public LiveData<String> getDisplayNameExists() { return displayNameExists; }

    public LiveData<String> getError() { return error; }

    public LiveData<Boolean> getNetwork() {
        return network;
//        return Transformations.map(network, n -> {
//            Log.d(TAG, "getNetwork: " + n);
//            if (n != netState) {
//                Log.d("myT", "checkValidNetworks: " + n + " : " + netState);
//                netState = n;
//                return n;
//            } else return null;
//        });
    }

}
