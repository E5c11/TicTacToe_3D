package com.esc.test.apps.modelviews;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.other.SingleLiveEvent;
import com.esc.test.apps.repositories.FirebaseUserRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final SingleLiveEvent<Boolean> loggedIn;
    private final SingleLiveEvent<String> error;
    private final MutableLiveData<Boolean> changePassFocus = new MutableLiveData<>();
    private final LiveData<Boolean> displayNameExists;
    private final MutableLiveData<String> displayNameError = new MutableLiveData<>();
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
    public LoginViewModel(UserDetails userDetails, FirebaseUserRepository fbUserRepo) {
        this.userDetails = userDetails;
        this.fbUserRepo = fbUserRepo;
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

    public void getUserDetails(String email, String password) {
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

    private boolean validDisplayName() {
        if (displayName.isEmpty()) {
            displayNameError.setValue("Enter a display name");
            return false;
        } else if (displayNameExists.getValue()) return false;
        else {
            displayNameError.setValue(null);
            return true;
        }
    }

    private boolean validateEmail() {
        Log.d("myT", "validating email");
        String errorCheck = emailError.getValue();
        if (email.isEmpty()) {
            emailError.setValue("Email can not be empty");
            return false;
        } else if (errorCheck.equals("Enter a valid email")) return false;
        else if (errorCheck.isEmpty()) return true;
        else {
            isEmailValid(email);
            return false;
        }
    }

    private boolean validatePassCon() {
        Log.d("myT", "passCon is: " + passCon);
        if (passCon.equals(password)) {
            Log.d("myT", "passwords match");
            passConError.setValue(null);
            return true;
        } else {
            passConError.setValue("Passwords do not match");
            return false;
        }
    }

    private void validatePassword() {
        if (password.isEmpty()) {
            passwordError.setValue("Password cannot be empty");
            changePassFocus.setValue(false);
        } else if (password.equals(password.toLowerCase()) && !password.matches(".*\\d.*")) {
            passwordError.setValue("Password must contain an uppercase and number");
            changePassFocus.setValue(false);
        } else if (password.equals(password.toLowerCase())) {
            passwordError.setValue("Password must contain an uppercase");
            changePassFocus.setValue(false);
        }else if (!password.matches(".*\\d.*")) {
            passwordError.setValue("Password must contain a number");
            changePassFocus.setValue(false);
        }else if (!(password.length() >= 6)) {
            passwordError.setValue("Password must contain at least 6 characters");
            changePassFocus.setValue(false);
        } else {
            passwordError.setValue(null);
            changePassFocus.setValue(true);
        }
    }

    public void setPassword(String viewPassword) {
        password = viewPassword;
        validatePassword();
    }

    public void setLogin(boolean loginPage) { login = loginPage;}

    public void setEmail(String viewEmail) { email = viewEmail; }

    public void setPassCon(String viewPassCon) {passCon = viewPassCon;}

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
                    return msg;
                default:
                    return "an error has occurred, try again";
            }
        });
    }

    public LiveData<Boolean> getChangePassFocus() {return changePassFocus;}

    public LiveData<Boolean> getDisplayNameExists() {return displayNameExists;}

    public LiveData<String> getDisplayNameError() {return displayNameError;}

    public LiveData<String> getError() { return error; }

}
