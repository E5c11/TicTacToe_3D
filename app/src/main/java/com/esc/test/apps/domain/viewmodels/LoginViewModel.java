package com.esc.test.apps.domain.viewmodels;

import static com.esc.test.apps.common.utils.ExtensionsKt.validEmail;
import static com.esc.test.apps.common.utils.ResourceKt.isError;
import static com.esc.test.apps.common.utils.ResourceKt.isLoading;
import static com.esc.test.apps.common.utils.ResourceKt.isSuccess;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.common.network.ConnectionLiveData;
import com.esc.test.apps.common.utils.SingleLiveEvent;
import com.esc.test.apps.common.utils.Utils;
import com.esc.test.apps.data.repositories.FbUserRepo;
import com.esc.test.apps.domain.usecases.login.LoginUsecase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final SingleLiveEvent<Boolean> loggedIn;
    private Single<Boolean> _loginState;
    public final Single<Boolean> loginState = _loginState;
    private Single<Boolean> _loadingState;
    public final Single<Boolean> loadingState = _loadingState;
    public final SingleLiveEvent<String> error;
    public final ConnectionLiveData network;
    private final MutableLiveData<String> displayNameExists;
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError;
    private final MutableLiveData<String> passConError = new MutableLiveData<>();
    private Disposable d;
    private String displayName;
    private String passCon;
    private String password;
    private String email;
    private boolean login = true;
    private final FbUserRepo fbUserRepo;
    private final LoginUsecase loginUsecase;
    private static final String TAG = "myT";

    @Inject
    public LoginViewModel(
            FbUserRepo fbUserRepo, ConnectionLiveData network, LoginUsecase loginUsecase
    ) {
        this.fbUserRepo = fbUserRepo;
        this.network = network;
        loggedIn = fbUserRepo.getLoggedIn();
        error = fbUserRepo.getError();
        emailError = fbUserRepo.getEmailError();
        displayNameExists = fbUserRepo.getDisplayNameExists();
        this.loginUsecase = loginUsecase;
        logUserIn();
    }

    private void logUserIn() {
        d = loginUsecase.invoke().observeOn(Schedulers.io()).subscribe( resource -> {
            _loadingState = Single.just(isLoading(resource));
            if (isSuccess(resource)) {
                if (resource.component2()) loggedIn.postValue(true);
                else loggedIn.postValue(false);
                _loginState = Single.just(true);
                d.dispose();
            }
            else if (isError(resource)) {
                error.postValue(resource.getError().getMessage());
                d.dispose();
            }
        });
    }

    public void getUserDetails() {
        loginUsecase.invoke(email, password);
    }

    public void isEmailValid(String viewEmail) {
        if (validEmail(viewEmail)) fbUserRepo.isEmailValid(viewEmail);
        else emailError.setValue("Enter a valid email");
    }

    public void newDisplayName(CharSequence ds) {
        fbUserRepo.checkDisplayNameExist(ds);
    }

    public void submitNewUser(String email, String displayName) {
        setEmail(email);
        setDisplayName(displayName);
        if (!validDisplayName() | !validateEmail() | !validatePassCon()) {
            error.setValue("kill login");
        } else fbUserRepo.createUser(email, password, displayName);
    }

    public void loginUser(String email, String password) {
        setEmail(email);
        setPassword(password);
        if (!validateEmail() | password.isEmpty()) error.setValue("kill login");
        else getUserDetails();
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
            Log.d(TAG, "getEmailError: " + msg);
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
        return loggedIn;
    }

    public LiveData<String> getDisplayNameExists() { return displayNameExists; }

}
