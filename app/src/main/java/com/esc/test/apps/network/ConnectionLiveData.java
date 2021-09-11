package com.esc.test.apps.network;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.HashSet;
import java.util.Set;

public class ConnectionLiveData extends LiveData<Boolean> {

    private ConnectivityManager.NetworkCallback networkCallback;
    private final ConnectivityManager cm;
    private final Set<Network> validNetworks = new HashSet<>();

    public ConnectionLiveData(Context context) {
        cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    }

    private void checkValidNetworks() {
        postValue(validNetworks.size() > 0);
    }

    @Override
    protected void onActive() {
        networkCallback = createNetworkCallback();
        cm.registerDefaultNetworkCallback(networkCallback);
    }

    @Override
    protected void onInactive() {
        cm.unregisterNetworkCallback(networkCallback);
    }

    private ConnectivityManager.NetworkCallback createNetworkCallback() {
        return new ConnectivityManager.NetworkCallback () {
            @Override
            public void onAvailable(@NonNull Network network) {
                NetworkCapabilities netCap = cm.getNetworkCapabilities(network);
                boolean internet = netCap.hasCapability(NET_CAPABILITY_INTERNET);
                if (internet) validNetworks.add(network);
                checkValidNetworks();
            }
            @Override
            public void onLost(@NonNull Network network) {
                validNetworks.remove(network);
                checkValidNetworks();
            }
        };
    }
}
