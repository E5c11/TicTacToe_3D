package com.esc.test.apps.data.repositories.implementations.remote;

import static com.esc.test.apps.common.utils.DatabaseConstants.DISPLAY_NAME;
import static com.esc.test.apps.common.utils.DatabaseConstants.STATUS;
import static com.esc.test.apps.common.utils.DatabaseConstants.TOKEN;
import static com.esc.test.apps.common.utils.DatabaseConstants.USERS;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.esc.test.apps.R;
import com.esc.test.apps.data.persistence.UserPreferences;
import com.esc.test.apps.common.utils.ExecutorFactory;
import com.esc.test.apps.common.utils.SingleLiveEvent;
import com.esc.test.apps.common.utils.Utils;
import com.esc.test.apps.data.repositories.FbUserRepo;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class FirebaseUserRepository implements FbUserRepo {

    private final FirebaseAuth firebaseAuth;
    private final UserPreferences userPrefs;
    private final DatabaseReference users;
    private final Application app;
    private final FirebaseUser user;
    private Disposable d;
    private String uid;
    private final SingleLiveEvent<Boolean> loggedIn = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> error = new SingleLiveEvent<>();
    private final MutableLiveData<String> displayNameExists = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final ExecutorService executor = ExecutorFactory.getSingleExecutor();
    private static final String TAG = "[Firebase]";
    private int attempt = 0;

    @Inject
    public FirebaseUserRepository(FirebaseAuth firebaseAuth, DatabaseReference db,
                                  Application app, UserPreferences userPref
    ) {
        this.firebaseAuth = firebaseAuth;
        this.app = app;
        this.userPrefs = userPref;
        this.user = firebaseAuth.getCurrentUser();

        d = userPref.getUserPreference().subscribeOn(AndroidSchedulers.mainThread()).doOnNext(prefs -> {
            uid = prefs.getUid();
            Utils.dispose(d);
        }).subscribe();
        users = db.child(USERS);
    }

    @Override
    public void isEmailValid(@NonNull String viewEmail) {
        firebaseAuth.fetchSignInMethodsForEmail(viewEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                attempt = 0;
                if (task.getResult().getSignInMethods().isEmpty())
                    emailError.postValue(app.getString(R.string.email_does_not_exists));
                else emailError.postValue(app.getString(R.string.email_exists));
            } else {
                if (attempt < 3) {
                    attempt ++;
                    isEmailValid(viewEmail);
                } else error.postValue(app.getString(R.string.network_error));
            }
        });
    }

    @Override
    public void connectLogin(@NonNull String email, @NonNull String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            executor.execute(() -> {
                if (task.isSuccessful()) {
                    String uid = task.getResult().getUser().getUid();
                    userPrefs.updateUserJava(uid, email, password);
                    getDisplayNameFromDB(uid);
                    d = userPrefs.getUserPreference().subscribeOn(Schedulers.io()).doOnNext( prefs -> {
                        setToken(prefs.getToken());
                        Utils.dispose(d);
                    }).subscribe();
                    setUserOnline(uid);
                    this.uid = uid;
                    loggedIn.postValue(true);
                    Log.d(TAG, "after set token: " + loggedIn.getValue());
                } else {
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        error.postValue("Password is incorrect");
                    } catch (FirebaseTooManyRequestsException e) {
                        error.postValue(app.getString(R.string.excessive_requests));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("myT", "user not logged in: " + task.getException());
                        if (attempt < 3) {
                            attempt++;
                            connectLogin(email, password);
                        } else error.postValue(app.getString(R.string.not_logged_in));
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
                    userPrefs.updateNameJava(snapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                if (attempt < 3) {
                    attempt ++;
                    getDisplayNameFromDB(uid);
                } else error.postValue(app.getString(R.string.network_error));
            }
        });
    }

    @Override
    public void createUser(@NonNull String email, @NonNull String password, @NonNull String displayName) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> executor.execute(() -> {
                if (task.isSuccessful()) {
                    attempt = 0;
                    String uid = task.getResult().getUser().getUid();
                    setUserOnline(uid);
                    this.uid = uid;
                    userPrefs.updateUserJava(uid, email, password);
                    userPrefs.updateNameJava(displayName);
                    updateDisplayName(displayName);
                    d = userPrefs.getUserPreference().subscribeOn(Schedulers.io()).doOnNext( prefs -> {
                        setToken(prefs.getToken());
                        Utils.dispose(d);
                    }).subscribe();
                    loggedIn.postValue(true);
                } else if (attempt < 3) {
                    attempt++;
                    createUser(email, password, displayName);
                } else error.postValue(app.getString(R.string.network_error));
            })).addOnCanceledListener(() -> {
                if (attempt < 3) {
                    attempt++;
                    createUser(email, password, displayName);
                } else error.postValue(app.getString(R.string.network_error));
            });
    }

    private void setUserOnline(String uid) {
        users.child(uid).child(STATUS).setValue(app.getString(R.string.online)).addOnCompleteListener(task -> {
            Log.d(TAG, "setUserOnline: ");
        });
        users.child(uid).child(STATUS).onDisconnect().setValue(app.getString(R.string.offline));
    }

    @Override
    public void deleteAccount() {
        user.delete()
            .addOnCompleteListener(task -> {
                error.postValue(app.getString(R.string.network_success));
                userPrefs.clearDataJava();
            })
            .addOnFailureListener(fail -> error.postValue(fail.getMessage()));
    }

    @Override
    public void updateDisplayName(@NonNull String displayName) {
        users.child(uid).child(DISPLAY_NAME).setValue(displayName)
            .addOnCompleteListener(task -> {
                error.postValue(app.getString(R.string.network_success));
                userPrefs.updateNameJava(displayName);
            })
            .addOnFailureListener(fail -> error.postValue(fail.getMessage()));
    }

    @Override
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
                                displayNameExists.postValue(app.getString(R.string.display_name_exists));
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                error.postValue(app.getString(R.string.network_error));
            }
        });
    }

    @Override
    public void updateEmail(@NonNull String email) {
        user.updateEmail(email)
                .addOnCompleteListener(task -> {
                    error.postValue(app.getString(R.string.network_success));
                    userPrefs.updateEmailJava(email);
                })
                .addOnFailureListener(fail -> error.postValue(fail.getMessage()));
    }

    @Override
    public void updatePassword(@NonNull String password) {
        user.updatePassword(password)
                .addOnCompleteListener(task -> {
                    error.postValue(app.getString(R.string.network_success));
                    userPrefs.updatePasswordJava(password);
                })
                .addOnFailureListener(fail -> error.postValue(fail.getMessage()));
    }

    @NonNull
    @Override
    public SingleLiveEvent<Boolean> getLoggedIn() { return loggedIn; }

    @NonNull
    @Override
    public MutableLiveData<String> getEmailError() {return emailError;}

    @NonNull
    @Override
    public MutableLiveData<String> getDisplayNameExists() {return displayNameExists;}

    @NonNull
    @Override
    public SingleLiveEvent<String> getError() { return error;}

    @Override
    public void setToken(@NonNull String s) {
        users.child(uid).child(TOKEN).setValue(s).addOnCompleteListener(task -> {
            if (task.isSuccessful()) Log.d(TAG, "setToken: ");
        });
    }
}
