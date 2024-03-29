package com.example.devam;

import static com.example.devam.WifiTrackerService.WIFI_CONNECTED;
import static com.example.devam.WifiTrackerService.WIFI_DISABLED;
import static com.example.devam.WifiTrackerService.WIFI_DISCONNECTED;
import static com.example.devam.WifiScanService.WIFI_LIST_UPDATED;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.TransportInfo;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "MyChannel";
    public static final String TAG = "MainActivity";
    private static final int UPDATE_INTERVAL = 5000;
    private TextView title, message;
    private ImageView image;
    private Button action_btn;

    private NotificationManager notificationManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Intent wifiScanIntent;
    private Intent wifiTrackingIntent;
    private WifiTrackerService wifiTrackerService;
    private ConnectivityManager connectivityManager;
    private boolean wifiIsConnected;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        this.wifiScanIntent = new Intent(this, WifiScanService.class);
        this.wifiTrackingIntent = new Intent(this, WifiTrackerService.class);
        startService(this.wifiTrackingIntent);
//        startService(this.wifiScanIntent);

        title = findViewById(R.id.title);
        image = findViewById(R.id.representation);
        message = findViewById(R.id.text_message);
        action_btn = findViewById(R.id.button_action);
        action_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });

        wifiTrackerService = new WifiTrackerService();

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(whoIsConnected, new IntentFilter(WIFI_CONNECTED), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(listWifiChanged, new IntentFilter(WIFI_LIST_UPDATED), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(wifiDisconnected, new IntentFilter(WIFI_DISCONNECTED), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(wifiDisabled, new IntentFilter(WIFI_DISABLED), Context.RECEIVER_NOT_EXPORTED);
        }

//        nataon Steve vao androany 29 mars
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                updateWifiInfo(network);
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(this.wifiScanIntent);
        stopService(this.wifiTrackingIntent);
        cancelNotification();
    }

    private final BroadcastReceiver listWifiChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), WIFI_LIST_UPDATED)) {
                ArrayList<String> wifiScanResults = intent.getStringArrayListExtra("scanResults");
                if (wifiScanResults != null) {
                    for (String ssid : wifiScanResults) {
                        Log.i(TAG, ssid);
                    }
                    showNotification(wifiScanResults);
                }
            }
        }
    };

    private final BroadcastReceiver whoIsConnected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), WIFI_CONNECTED)) {
                String wifiSSID = intent.getStringExtra("wifi_ssid");
                showNotification(wifiSSID);
                wifiIsConnected = true;
                showHome(true);
            }

        }
    };

    private final BroadcastReceiver wifiDisconnected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), WIFI_DISCONNECTED)) {
                wifiIsConnected = false;
                showHome(false);
//                mi_scan indray
            }

        }
    };

    private final BroadcastReceiver wifiDisabled = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiIsConnected = false;
            showHome(false);
            showNotification("Tsy mandeh ny wifi");
        }
    };

    private void showHome(boolean wifiIsConnected){
        if (wifiIsConnected){
            image.setImageResource(R.drawable.connect);
            title.setText("SUPER !!!");
            message.setText("you are connected to :" + wifiTrackerService.getCurrentWifiSSID());
            action_btn.setVisibility(View.GONE);
        }
        else {
            image.setImageResource(R.drawable.nonet);
            title.setText("OOPS !!!");
            message.setText("You're not connected to \na network");
            action_btn.setVisibility(View.VISIBLE);
        }
    }
    protected void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "My Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    protected void showNotification(String elem) {
        Intent activityIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icone)
                .setContentTitle("Connection status")
                .setContentText("you are connected to :" +elem)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true);


        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
    protected void showNotification(List<String> list) {
        Intent activityIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Available Wifi")
                .setContentText(list.size()+" wifi are available in da place")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true);

        NotificationCompat.InboxStyle inboxStyle= new NotificationCompat.InboxStyle();
        for (String wifi : list){
            StringBuilder wifiList = new StringBuilder();
            wifiList.append("   - ").append(wifi);
            inboxStyle.addLine(wifiList);
        }
        builder.setStyle(inboxStyle);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
    protected void cancelNotification(){
        notificationManager.cancelAll();
    }

    private void updateWifiInfo(Network network){

        if (network == null)
            return;

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);

        
        if ((networkCapabilities != null) && (linkProperties != null)) {
            // Check for location permission if needed (API level 29+)
//            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission);
//            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

                for(LinkAddress address : linkProperties.getLinkAddresses()){
                    Log.i(TAG, address.getAddress().toString());
                }

//            } else {
//                // Handle case where location permission is not granted
//                // (e.g., inform user or disable functionality)
//            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

//        verify if the device is connected
        if(wifiIsConnected){
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
            updateWifiInfo(connectivityManager.getActiveNetwork());

//        create and schedule the timer for periodic updates
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        updateWifiInfo(connectivityManager.getActiveNetwork());
//                    }
//                });
//            }
//        }, 0, UPDATE_INTERVAL);
        }

    }
}



