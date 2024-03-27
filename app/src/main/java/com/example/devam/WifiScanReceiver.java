package com.example.devam;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class WifiScanReceiver extends BroadcastReceiver {

//    public List<ScanResult> getResults() {
//        return results;
//    }

    private List<ScanResult> results;
    private WifiManager wifiManager;

    public static final String LIST_SCAN_CHANGED = "com.example";

    @Override
    public void onReceive(Context context, Intent intent) {

        ArrayList<String> ssidList = new ArrayList<>();
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
        results = wifiManager.getScanResults();
//        vérifier si les resultats ne sont pas vide
        if(results != null){
            for(ScanResult scanResult : results)
                ssidList.add(scanResult.SSID);


            Intent listScanChangedIntent = new Intent(LIST_SCAN_CHANGED);
            listScanChangedIntent.putStringArrayListExtra("scanResults", ssidList);
            context.sendBroadcast(listScanChangedIntent);
        }

    }

}