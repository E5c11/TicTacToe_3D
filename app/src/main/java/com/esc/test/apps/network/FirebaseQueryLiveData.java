package com.esc.test.apps.network;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseQueryLiveData extends LiveData<DataSnapshot> {

    private boolean listenerRemovePending;
    private Query query;
    private final EventListener valueListener = new EventListener();
    private final Handler handler = new Handler();

    public FirebaseQueryLiveData(Query query) { this.query = query; }

    private final Runnable removeListener = () -> {
        query.removeEventListener(valueListener);
        listenerRemovePending = false;
    };

    @Override
    protected void onActive() {
        if (listenerRemovePending) handler.removeCallbacks(removeListener);
        else query.addValueEventListener(valueListener);
        listenerRemovePending = false;
    }

    @Override
    protected void onInactive() {
        handler.postDelayed(removeListener, 500);
        listenerRemovePending = true;
    }

    private class EventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) { setValue(dataSnapshot); }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    }
}
