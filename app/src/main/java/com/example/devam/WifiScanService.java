package com.example.devam;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WifiScanService extends Service {
    public static final String WIFI_LIST_UPDATED = "wifi_list_updated";
    private WifiManager wifiManager;
    private WifiScanReceiver wifiScanReceiver;
    private ExecutorService executorService;
    private List<ScanResult> scanResults;

    public static final String TAG = "WifiScanService";
    public final IBinder binder = new WifiScanBinder();
    public class WifiScanBinder extends Binder {
        public WifiScanService getService(){
            return WifiScanService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(WIFI_SERVICE);
        this.executorService = Executors.newFixedThreadPool(5);
        this.wifiScanReceiver = new WifiScanReceiver();
//        registerReceiver(listWifiChanged, new IntentFilter(WIFI_JOB_UPDATED));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(wifiManager.isWifiEnabled()){
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if((wifiConnection != null) && wifiConnection.isConnected()){
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                Log.i(TAG, "Connecté amty pr e: " + wifiInfo.getSSID());
            } else {
                this.executorService.execute(new WifiScanExecutor(startId));
            }
            return START_STICKY;
        }
        else {
            Toast.makeText(this, "Wifi disabled", Toast.LENGTH_LONG);
            return START_REDELIVER_INTENT;
        }

    }

    private class WifiScanExecutor implements Runnable {
        private int startId;

        WifiScanExecutor(int startId) {
            this.startId = startId;
        }

        @Override
        public void run() {
            Log.i(TAG, "WifiService begins the scan");
            scanWifi();
        }
    }

    public void scanWifi() {
        wifiManager.startScan();
        registerReceiver( wifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public class WifiScanReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            ArrayList<String> ssidList = new ArrayList<>();
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (ActivityCompat.checkSelfPermission(context.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if((wifiConnection != null) && !wifiConnection.isConnected()){
                List<ScanResult> results = wifiManager.getScanResults();
            //        vérifier si les resultats ne sont pas vide
                if(results != null){
                    for(ScanResult scanResult : results)
                        ssidList.add(scanResult.SSID);


                    Intent listScanChangedIntent = new Intent(WIFI_LIST_UPDATED);
                    listScanChangedIntent.putStringArrayListExtra("scanResults", ssidList);
                    context.sendBroadcast(listScanChangedIntent);
                }
            }
            else {
                stopSelf();
            }



        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(listWifiChanged);
//        unregisterReceiver(wifiScanReceiver);
    }
}