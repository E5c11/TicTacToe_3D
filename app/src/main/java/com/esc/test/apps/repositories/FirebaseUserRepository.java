package com.esc.test.apps.repositories;

import static com.esc.test.apps.utils.DatabaseConstants.DISPLAY_NAME;
import static com.esc.test.apps.utils.DatabaseConstants.STATUS;
import static com.esc.test.apps.utils.DatabaseConstants.TOKEN;
import static com.esc.test.apps.utils.DatabaseConstants.USERS;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.esc.test.apps.R;
import com.esc.test.apps.datastore.UserDetails;
import com.esc.test.apps.utils.ExecutorFactory;
import com.esc.test.apps.utils.SingleLiveEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;

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
    private final MutableLiveData<String> displayNameExists = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private static final String TAG = "myT";
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
                    emailError.postValue("This email does not exist");
                else emailError.postValue("This email already exists");
            } else {
                if (attempt < 3) {
                    attempt ++;
                    isEmailValid(viewEmail);
                } else error.postValue("An error has occurred, check network");
            }
        });
    }

    public void connectLogin(String email, String password) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                executor.execute(() -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        userDetails.setUid(uid);
                        userDetails.setEmail(email);
                        userDetails.setPassword(password);
                        Log.d(TAG, "connectUser: " + Thread.currentThread().getName());
                        getDisplayNameFromDB(uid);
                        setToken(userDetails.getToken());
                        setUserOnline(uid);
                        loggedIn.postValue(true);
                        Log.d(TAG, "after set token: " + loggedIn.getValue());
                    } else {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            error.postValue("Password is incorrect");
                        } catch (FirebaseTooManyRequestsException e) {
                            error.postValue("Too many requests, please try again later");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("myT", "user not logged in: " + task.getException());
                            if (attempt < 3) {
                                attempt++;
                                connectLogin(email, password);
                            } else error.postValue("User not logged in");
                        }
                    }
                });
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
                } else error.postValue("An error occurred, check the network");
            }
        });
    }

    public void createUser(String email, String password, String displayName) {
        if (email != null && password != null)
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                executor.execute(() -> {
                    if (task.isSuccessful()) {
                        attempt = 0;
                        String uid = task.getResult().getUser().getUid();
                        users.child(uid).child(DISPLAY_NAME).setValue(displayName);
                        setUserOnline(uid);
                        userDetails.setUid(uid);
                        userDetails.setEmail(email);
                        userDetails.setPassword(password);
                        userDetails.setDisplayName(displayName);
                        setToken(userDetails.getToken());
                        loggedIn.postValue(true);
                    } else if (attempt < 3) {
                        attempt++;
                        createUser(email, password, displayName);
                    } else error.postValue("An error occurred, check the network");
                });
            }).addOnCanceledListener(() -> {
                if (attempt < 3) {
                    attempt++;
                    createUser(email, password, displayName);
                } else error.postValue("An error occurred, check the network");
            });
    }

    private void setUserOnline(String uid) {
        users.child(uid).child(STATUS).setValue(app.getString(R.string.online)).addOnCompleteListener(task -> {
            Log.d(TAG, "setUserOnline: ");
        });
        users.child(uid).child(STATUS).onDisconnect().setValue(app.getString(R.string.offline));
    }

    public void checkDisplayNameExist(CharSequence ds) {
            Query query = users.orderByChild(DISPLAY_NAME).equalTo(ds.toString());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() == null) displayNameExists.setValue("");
                    else {
                        executor.execute(() -> {
                            for(DataSnapshot snap: snapshot.getChildren()) {
                                String tempDisplay = (String) snap.child(DISPLAY_NAME).getValue();
                                if (tempDisplay.equals(ds.toString())) {
                                    displayNameExists.postValue("Display name already exists");
                                }
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError e) {
                    error.postValue("An error occurred, check the network");
                }
            });
    }

    public void updateEmail(String email) {
        getUser().updateEmail(email)
                .addOnCompleteListener(task -> error.postValue(null))
                .addOnFailureListener(fail -> error.postValue(fail.getMessage()));
    }

    public void updatePassword(String password) {
        getUser().updatePassword(password)
                .addOnCompleteListener(task -> error.postValue(null))
                .addOnFailureListener(fail -> error.postValue(fail.getMessage()));
    }

    private FirebaseUser getUser() {
        return firebaseAuth.getCurrentUser();
    }

    public SingleLiveEvent<Boolean> getLoggedIn() { return loggedIn; }

    public MutableLiveData<String> getEmailError() {return emailError;}

    public MutableLiveData<String> getDisplayNameExists() {return displayNameExists;}

    public SingleLiveEvent<String> getError() { return error;}

    public void setToken(String s) {
        users.child(userDetails.getUid()).child(TOKEN).setValue(s).addOnCompleteListener(task -> {
            if (task.isSuccessful()) Log.d(TAG, "setToken: " + Thread.currentThread().getName());});
    }
}
