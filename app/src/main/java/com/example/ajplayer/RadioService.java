package com.example.ajplayer;

import static androidx.media3.common.util.NotificationUtil.createNotificationChannel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class RadioService extends Service {


    private static final String CHANNEL_ID = "RadioChannel";
    private ExoPlayer exoplayer;


    final String url = "https://uk5freenew.listen2myradio.com/live.mp3?typeportmount=s1_29306_stream_83580268";


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();



        exoplayer = new ExoPlayer.Builder(this).build();
        MediaItem mediaItem = MediaItem.fromUri(url);
        exoplayer.setMediaItem(mediaItem);
        exoplayer.prepare();

        //deteccion de errores del exoplayer
        exoplayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e ("Radioservice", "error en el Exoplayer: "+ error.getMessage());
                stopSelf();
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //inicia la reproduccion

        startForeground(1, createNotification()); // aqui inicia el servicio en primer plano con notificacion
        exoplayer.play();
        return START_STICKY;
    }


    private Notification createNotification(){

        //intent para abrir la app al tocar la notification

        Intent notificationIntent = new Intent (this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


       NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
               . setContentTitle("Radio en Stremig")
               .setContentText("reproducuiendo en segudo plano")
               .setSmallIcon(R.drawable.iconradio)
               .setContentIntent(pendingIntent)
               .setPriority(NotificationCompat.PRIORITY_LOW)
               .setOngoing(true);
       return builder.build();
    }




    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID,
                    "Canal de Radio",
                    NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager !=null){
                manager.createNotificationChannel(serviceChannel);
            }
        }

    }

    public void onDestroy(){
        super.onDestroy();
        if (exoplayer !=null){
            exoplayer.release();
        }
    }

    public IBinder onBind(Intent intent){
        return null;
    }


}
