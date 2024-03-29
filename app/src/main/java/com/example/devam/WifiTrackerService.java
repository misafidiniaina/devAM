package com.example.devam;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class WifiTrackerService extends Service {
    public static final String WIFI_CONNECTED = "wifi_connected";
    public static final String WIFI_DISCONNECTED = "wifi_disconnected";
    public static final String WIFI_DISABLED = "wifi_disabled";
    private Intent wifiScanIntent;
    private String currentWifiSSID;


    private WifiManager wifiManager;
    private BroadcastReceiver wifiReceiver;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiReceiver = new WifiStateReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        this.wifiScanIntent = new Intent(this, WifiScanService.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Not intended to be bound
        return null;
    }

    // Get current wifi SSID (optional, can be exposed through a method)
    public String getCurrentWifiSSID() {
        return currentWifiSSID;
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                Intent wifiConnectedIntent = new Intent(WIFI_CONNECTED);
                Intent wifiDisconnectedIntent = new Intent(WIFI_DISCONNECTED);
                Intent wifiDisabled = new Intent(WIFI_DISABLED);

//                mandeh ny wifi
                if (wifiManager.isWifiEnabled()){
                    //                rehefa connecte am wifi net iray
                    if (wifiInfo != null && !wifiInfo.getSSID().equals("<unknown ssid>")) { // Check for valid connection
                        stopService(wifiScanIntent);
                        currentWifiSSID = wifiInfo.getSSID();
//                    wifiConnectedIntent.putExtra("wifi_is_connected", true);
                        wifiConnectedIntent.putExtra("wifi_ssid", currentWifiSSID);
                        sendBroadcast(wifiConnectedIntent);
                    }
//                deconnecte
                    else {
                    startService(wifiScanIntent);
//                    wifiManager.startScan();
                        sendBroadcast(wifiDisconnectedIntent);
                    }
                }
//                tsy mandeh ny wifi
                else {
                    stopService(wifiScanIntent);
                    sendBroadcast(wifiDisabled);
                }
            }
        }
    }
}