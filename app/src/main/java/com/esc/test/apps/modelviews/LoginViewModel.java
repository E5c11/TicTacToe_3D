package com.esc.test.apps.modelviews;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.esc.test.apps.other.ResourceProvider;
import dagger.hilt.android.lifecycle.HiltViewModel;
import com.esc.test.apps.datastore.UserDetails;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private boolean emailExists, validEmail = false;
    private final MutableLiveData<Boolean> loggedIn = new MutableLiveData<>();
    private final MutableLiveData<Boolean> changePassFocus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> displayNameExists = new MutableLiveData<>();
    private final MutableLiveData<String> displayNameError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passConError = new MutableLiveData<>();
    private final MutableLiveData<String> passCon = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> displayName = new MutableLiveData<>();
    private final UserDetails userDetails;
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference db;
    private final ResourceProvider rp;

    @Inject
    public LoginViewModel(FirebaseAuth firebaseAuth, ResourceProvider rp,
                          DatabaseReference db, UserDetails userDetails
    ) {
        this.userDetails = userDetails;
        this.firebaseAuth = firebaseAuth;
        this.db = db;
        this.rp = rp;
        logUserIn();
    }

    private void logUserIn() {
        if (checkExistingUser()) connectLogin(userDetails.getEmail(), userDetails.getPassword());
        else {
            Log.d("myT", "first launch");
            loggedIn.setValue(false);
        }
    }

    public void getUserDetails(String email, String password) {
        userDetails.clearPrefs();
        connectLogin(email, password);
    }

    private void connectLogin(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = task.getResult().getUser().getUid();
                userDetails.setUid(uid);
                userDetails.setEmail(email);
                userDetails.setPassword(password);
                getDisplayNameFromDB(uid);
                Log.d("myT", "uid is: " + userDetails.getUid());
                setUserOnline(uid);
                loggedIn.setValue(true);
            }else {
                loggedIn.setValue(false);
                Log.d("myT", "user not created: " + task.getException().getMessage());
                emailError.setValue(task.getException().getMessage());
            }
        });
    }

    private void getDisplayNameFromDB(String uid) {
        db.child(rp.getString(R.string.users)).child(uid).child(rp.getString(R.string.display_name)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) userDetails.setDisplayName(snapshot.getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private Boolean checkExistingUser() { return userDetails.getEmail() != null; }

    public MutableLiveData<Boolean> getLoggedIn() {
        return loggedIn;
    }

    public void isEmailValid(String viewEmail) {
        //Log.d("myT", "checking email");
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(viewEmail);
        if (matcher.matches()) {
            firebaseAuth.fetchSignInMethodsForEmail(viewEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().getSignInMethods().isEmpty()) {
                        Log.d("myTAG", "Is New User!");
                        emailExists = false;
                    } else {
                        Log.d("myTAG", "Existing User!");
                        emailExists = true;
                        emailError.setValue("This email already exists");
                    }
                } else Log.d("myT", "check email exception: " + task.getException());
            });
            validEmail = true;
        }
    }

    public void newDisplayName(CharSequence s) {
        Query query = db.child("users").orderByChild("display_name").equalTo(s.toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) displayNameExists.setValue(false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    String tempDisplay = (String) snapshot.child("display_name").getValue();
                    if (tempDisplay.equals(s.toString())) {
                        displayNameExists.setValue(true);
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void createUser() {
        Log.d("myT", "creating user");
        String email = getEmail().getValue(), password = getPassword().getValue(),
            ds = displayName.getValue();
        if (email != null && password != null)
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = task.getResult().getUser().getUid();
                //Log.d("myTag", "User created " + task.getResult().getUser().getUid());
                db.child("users").child(uid).child("display_name").setValue(getDisplayName().getValue());
                task.getResult().getUser().getIdToken(true).addOnCompleteListener(task1 ->
                        db.child("users").child(uid).child("token").setValue(task1.getResult().getToken()));
                setUserOnline(uid);
                userDetails.setUid(uid);
                userDetails.setEmail(email);
                userDetails.setPassword(password);
                userDetails.setDisplayName(ds);
                loggedIn.setValue(true);
            } else {
                Log.d("myTag", "user not created: " + task.getException());
                loggedIn.setValue(false);
            }
        });
    }

    public void submitNewUser() {
        if (!validDisplayName() | !validateEmail() | !validatePassCon()) return;
        else createUser();
    }

    private boolean validDisplayName() {
        String checkDisplayName = getDisplayName().getValue();
        if (checkDisplayName.isEmpty()) {
            displayNameError.setValue("Enter a display name");
            return false;
        }
        else if (getDisplayNameExists().getValue()) return false;
        else {
            displayNameError.setValue(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String checkEmail = getEmail().getValue();
        Log.d("myT", "validating email");
        if (checkEmail.isEmpty()) {
            emailError.setValue("Email can not be empty");
            return false;
        } else if (!validEmail) {
            emailError.setValue("Please enter a valid email");
            return false;
        } else if (!emailExists) {
            emailError.setValue(null);
            Log.d("myTagEmails", "New email is:" + checkEmail);
            return true;
        } else {
            emailError.setValue("This email already exists");
            Log.d("myTagEmails", "email already exists");
            return false;
        }
    }

    private boolean validatePassCon() {
        String checkPassCon = getPassCon().getValue(), checkPass = getPassword().getValue();
        Log.d("myT", "passCon is: " + checkPassCon);
        if (checkPassCon.equals(checkPass)) {
            Log.d("myT", "passwords match");
            passConError.setValue(null);
            return true;
        } else {
            passConError.setValue("Passwords do not match");
            return false;
        }
    }

    private void validatePassword() {
        String checkPass = getPassword().getValue();
        Log.d("myTagEmails", "password is: " + checkPass);
        if (checkPass.isEmpty()) {
            passwordError.setValue("Password cannot be empty");
            changePassFocus.setValue(false);
        } else if (checkPass.equals(checkPass.toLowerCase()) && !checkPass.matches(".*\\d.*")) {
            passwordError.setValue("Password must contain an uppercase and number");
            changePassFocus.setValue(false);
        } else if (checkPass.equals(checkPass.toLowerCase())) {
            passwordError.setValue("Password must contain an uppercase");
            changePassFocus.setValue(false);
        }else if (!checkPass.matches(".*\\d.*")) {
            passwordError.setValue("Password must contain a number");
            changePassFocus.setValue(false);
        }else if (!(checkPass.length() >= 6)) {
            passwordError.setValue("Password must contain at least 6 characters");
            changePassFocus.setValue(false);
        } else {
            passwordError.setValue(null);
            changePassFocus.setValue(true);
        }
    }

    private void setUserOnline(String uid) {
        DatabaseReference onlineRef = db.child(rp.getString(R.string.users));
        onlineRef.child(uid).child(rp.getString(R.string.status)).setValue(rp.getString(R.string.online));
        onlineRef.child(uid).child(rp.getString(R.string.status)).onDisconnect().setValue(rp.getString(R.string.offline));
    }

    public void setPassword(String viewPassword) {
        password.setValue(viewPassword);
        validatePassword();
    }

    public void setEmail(String viewEmail) {email.setValue(viewEmail);}

    public void setPassCon(String viewPassCon) {passCon.setValue(viewPassCon);}

    public void setDisplayName(String viewDisplayName) {displayName.setValue(viewDisplayName);}

    private MutableLiveData<String> getPassCon() {return passCon;}

    public MutableLiveData<String> getPasswordError() {return passwordError;}

    public MutableLiveData<String> getPassConError() {return passConError;}

    private MutableLiveData<String> getPassword() {return password;}

    private MutableLiveData<String> getEmail() {return email;}

    public MutableLiveData<String> getEmailError() {return emailError;}

    public MutableLiveData<Boolean> getChangePassFocus() {return changePassFocus;}

    public MutableLiveData<Boolean> getDisplayNameExists() {return displayNameExists;}

    private MutableLiveData<String> getDisplayName() {return displayName;}

    public MutableLiveData<String> getDisplayNameError() {return displayNameError;}

}
