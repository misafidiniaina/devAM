package com.example.devam;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "MyChannel";
    private NotificationManager notificationManager;
    private Intent wifiIntent;
    public static final String LIST_SCAN_CHANGED = "com.example";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        this.wifiIntent = new Intent(this, WifiScanService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(listWifiChanged, new IntentFilter(LIST_SCAN_CHANGED), Context.RECEIVER_NOT_EXPORTED);
        }


//        TextView text = findViewById(R.id.textView2);
//        TextInputEditText inputEditText = findViewById(R.id.Textinput);
//        Button btn = findViewById(R.id.notifBtn);
//        Button cancelBtn = findViewById(R.id.cancelBtn);
//        Button updateBtn = findViewById(R.id.updateBtn);
        //       List<String> wifiList = Arrays.asList("PublicWifi 1:","PublicWifi 2","houifii","accesspoint","coco","alika");
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showNotification(wifiList);
//            }
//        });


//        cancelBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cancelNotification();
//            }
//        });


//        updateBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //////////////////////// tsy de ilaina ////////////////////
//                String inputText = inputEditText.getText().toString();
//                text.setText(inputText);
//                ///////////////////////////***********////////////////////
//            }
//        });


//        //////////////**************************/////////////////////////////////////////////
//        // tsy de ilaina firy
//        inputEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                text.setText(inputEditText.getText().toString());
//                updateNotification(inputEditText.getText().toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//        /////////////////*****************************//////////////////////////////////////////
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(this.wifiIntent);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(this.wifiIntent);
        cancelNotification();
    }

    private BroadcastReceiver listWifiChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), LIST_SCAN_CHANGED)) {
                ArrayList<String> wifiScanResults = intent.getStringArrayListExtra("scanResults");
                showNotification(wifiScanResults);
            }
        }
    };
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

    protected void updateNotification(String text){
        Intent activityIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);

        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Available wifi")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

}



