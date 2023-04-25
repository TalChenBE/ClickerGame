package com.example.clickergame;

import static com.example.clickergame.Finals.WIN_SCORE;
import static com.example.clickergame.Finals.selfKey;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ForegroundService extends Service {
    private static final int NOTIFICATION_ID1 = 1;
    private MyWorker worker;
    private NotificationManager mNotiMgr;
    private Notification.Builder mNotifyBuilder;

    public ForegroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        initForeground();
        super.onCreate();
    }

    private void initForeground() {
        String CHANNEL_ID = "my_channel_01";
        if (mNotiMgr==null)
            mNotiMgr= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "My main channel",
                NotificationManager.IMPORTANCE_DEFAULT);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(channel);
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mNotifyBuilder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Player Current Score...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent);
        startForeground(NOTIFICATION_ID1, updateNotification(Integer.toString(1)));
    }

    private Notification updateNotification(String details) {
        mNotifyBuilder.setContentText(details).setOnlyAlertOnce(false);
        Notification noti = mNotifyBuilder.build();
        noti.flags = Notification.FLAG_ONLY_ALERT_ONCE;
        mNotiMgr.notify(NOTIFICATION_ID1, noti);
        return noti;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.worker = new MyWorker();
        worker.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stopSelf();
        mNotiMgr.cancelAll();
        mNotiMgr.deleteNotificationChannel("my_channel_01");
    }

    private class MyWorker extends Thread {
        private final DatabaseReference database;

        public MyWorker() {
            FirebaseDatabase.getInstance();
            this.database = FirebaseDatabase.getInstance().getReference("players");
        }

        @Override
        public void run() {
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    pullData(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            database.limitToFirst(16).addValueEventListener(postListener);
        }

        private void pullData(DataSnapshot dataSnapshot){
            boolean isFound = false;
            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                Player player = postSnapshot.getValue(Player.class);
                if (player != null && player.getKey() != null && selfKey != null && player.getKey().equals(selfKey)){
                    isFound = true;
                    mNotifyBuilder.setContentText(Long.toString(player.getScore()));
                    mNotiMgr.notify(NOTIFICATION_ID1, mNotifyBuilder.build());
                    if (player.getScore() == 0 || player.getScore() == WIN_SCORE){
                        interrupt();
                        stopSelf();
                    }
                }
            }
            if (!isFound){
                mNotifyBuilder.setContentText("You have been eliminated");
                mNotiMgr.notify(NOTIFICATION_ID1, mNotifyBuilder.build());
                try{
                    Thread.sleep(5000);
                }catch (Exception e){}
                interrupt();
                stopSelf();
            }
        }
    }
}