package com.esc.test.apps.repositories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.esc.test.apps.R;
import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.other.SingleLiveEvent;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseUserRepository {

    private final FirebaseAuth firebaseAuth;
    private final UserDetails userDetails;
    private final DatabaseReference users;
    private final Application app;
    private final SingleLiveEvent<Boolean> loggedIn = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> error = new SingleLiveEvent<>();
    private final MutableLiveData<Boolean> displayNameExists = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private static final String TAG = "myT";
    private static final String STATUS = "status";
    private static final String DISPLAY_NAME = "display_name";
    private static final String USERS = "users";
    private static final String TOKEN = "token";
    private int attempt = 0;

    @Inject
    public FirebaseUserRepository(FirebaseAuth firebaseAuth, UserDetails userDetails,
                                  DatabaseReference db, Application app
    ) {
        this.firebaseAuth = firebaseAuth;
        this.userDetails = userDetails;
        this.app = app;
        users = db.child(USERS);
    }

    public void isEmailValid(String viewEmail) {
        firebaseAuth.fetchSignInMethodsForEmail(viewEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                attempt = 0;
                if (task.getResult().getSignInMethods().isEmpty())
                    emailError.setValue("This email does not exists");
                else emailError.setValue("This email already exists");
            } else {
                if (attempt < 3) {
                    attempt ++;
                    isEmailValid(viewEmail);
                } else error.setValue("An error has occurred, check network");
            }
        });
    }

    public void connectLogin(String email, String password) {
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
                Log.d("myT", "user not logged in: " + task.getException().getMessage());
                if (attempt < 3) {
                    attempt ++;
                    connectLogin(email, password);
                } else error.setValue("User not logged in");
            }
        });
    }

    private void getDisplayNameFromDB(String uid) {
        users.child(uid).child(DISPLAY_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    attempt = 0;
                    userDetails.setDisplayName(snapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                if (attempt < 3) {
                    attempt ++;
                    getDisplayNameFromDB(uid);
                } else error.setValue("An error occurred, check the network");
            }
        });
    }

    public void createUser(String email, String password, String displayName) {
        Log.d("myT", "creating user");
        if (email != null && password != null)
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    attempt = 0;
                    String uid = task.getResult().getUser().getUid();
                    //Log.d("myTag", "User created " + task.getResult().getUser().getUid());
                    users.child(uid).child(DISPLAY_NAME).setValue(displayName);
                    task.getResult().getUser().getIdToken(true).addOnCompleteListener(task1 ->
                            users.child(uid).child(TOKEN).setValue(task1.getResult().getToken()));
                    setUserOnline(uid);
                    userDetails.setUid(uid);
                    userDetails.setEmail(email);
                    userDetails.setPassword(password);
                    userDetails.setDisplayName(displayName);
                    loggedIn.setValue(true);
                } else if (attempt < 3){
                    attempt ++;
                    createUser(email, password, displayName);
                    Log.d("myTag", "user not created: " + Thread.currentThread().getName());
                } else Log.d("myTag", "user not created: cancelled last");
            }).addOnCanceledListener(() -> {
                if (attempt < 3) {
                    attempt ++;
                    createUser(email, password, displayName);
                } else Log.d("myTag", "user not created: cancelled");
            });
    }

    private void setUserOnline(String uid) {
        users.child(uid).child(STATUS).setValue(app.getString(R.string.online));
        users.child(uid).child(STATUS).onDisconnect().setValue(app.getString(R.string.offline));
    }

    public void checkDisplayNameExist(CharSequence ds) {
        Query query = users.orderByChild(DISPLAY_NAME).equalTo(ds.toString());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) displayNameExists.setValue(false);
                else {
                    for(DataSnapshot snap: snapshot.getChildren()) {
                        String tempDisplay = (String) snap.child(DISPLAY_NAME).getValue();
                        if (tempDisplay.equals(ds.toString())) {
                            displayNameExists.setValue(true);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public SingleLiveEvent<Boolean> getLoggedIn() {
        return loggedIn;
    }

    public MutableLiveData<String> getEmailError() {return emailError;}

    public LiveData<Boolean> getDisplayNameExists() {return displayNameExists;}
}
