package com.example.app5service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import com.example.common.IMyAidlInterface;
import androidx.core.app.NotificationCompat;

public class MusicBoundService extends Service {

    private static final String START_FOREGROUND_SERVICE = "start foreground service";
    private static final String STOP_FOREGROUND_ACTION = "stop foreground service";
    private static final String UNBIND = "unbind";
    private static final int NOTIFICATION_ID = 1;
    private static String CHANNEL_ID = "Music player style" ;

    private MediaPlayer mPlayer;
    private Integer[] songNames = {R.raw.badnews, R.raw.sorcererz, R.raw.sitnexttome, R.raw.tslamp, R.raw.humility};
    private Notification notification;

    @Override
    public void onCreate() {

        this.createNotificationChannel();

        final Intent notificationIntent = new Intent(getApplicationContext(),
                MusicBoundService.class);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0) ;

        notification =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setOngoing(true).setContentTitle("Music Playing")
                        .setContentText("Click to Access Music Player")
                        .setTicker("Music is playing!")
                        .setFullScreenIntent(pendingIntent, false)
                        .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals(START_FOREGROUND_SERVICE)) {
            startForeground(NOTIFICATION_ID, notification);
        }

        else if(intent.getAction().equals(STOP_FOREGROUND_ACTION)) {
            stopForeground(true);
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music player notification";
            String description = "The channel for music player notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private final IMyAidlInterface.Stub mBinder = new IMyAidlInterface.Stub() {

        @Override
        public void playSong(int songNum) {

            mPlayer = MediaPlayer.create(getApplicationContext(), songNames[songNum]);
            mPlayer.start();

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    Intent intnt = new Intent();
                    intnt.setAction(UNBIND);
                    sendBroadcast(intnt);
                }
            });
        }

        @Override
        public void pauseSong() {
            mPlayer.pause();
        }

        @Override
        public void resumeSong() {
            mPlayer.start();
        }

        @Override
        public void stopSong() {
            mPlayer.stop();
            mPlayer.release();
        }

    };
}
