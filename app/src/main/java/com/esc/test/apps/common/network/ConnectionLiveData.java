package com.esc.test.apps.common.network;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

import com.esc.test.apps.common.utils.SingleLiveEvent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ConnectionLiveData extends SingleLiveEvent<Boolean> {

    private ConnectivityManager.NetworkCallback networkCallback;
    private final ConnectivityManager cm;
    private final Set<Network> validNetworks = new HashSet<>();
    private boolean networkState = true;
    private static final String TAG = "myT";

    public ConnectionLiveData(Context context) {
        cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    }

    private void checkValidNetworks() {
        boolean netAvailable = validNetworks.size() > 0;
        boolean netValid;
        if (netAvailable) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("https://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                netValid = (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
                Log.d(TAG, "checkValidNetworks: internet " + netValid);
            } catch (IOException e) {
                netValid = false;
                Log.d(TAG, "checkValidNetworks: " + e.getMessage());
            }
        } else netValid = false;
        if (netValid != networkState) {
            networkState = netValid;
            postValue(netValid);
        }
    }

    @Override
    protected void onActive() {
        networkCallback = createNetworkCallback();
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NET_CAPABILITY_INTERNET)
                .build();
        cm.registerNetworkCallback(networkRequest, networkCallback);
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
