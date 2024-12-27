package com.example.ajplayer;

import static androidx.media3.common.util.NotificationUtil.IMPORTANCE_DEFAULT;
import static androidx.media3.common.util.NotificationUtil.createNotificationChannel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import java.net.HttpURLConnection;
import java.net.URL;

public class RadioService extends Service {


    private static final String CHANNEL_ID = "RadioChannel";
    private ExoPlayer exoplayer;


    //nuevo codigo
    private boolean isForeground = false;

    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;


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
               if (shouldReconnect(error)){
                   scheduleReconnect();
               }else {
                   Log.e ("RadioService", "error el rerpoducir el streaming ");
                   stopSelf();
               }

            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
     if (intent != null){
         String action = intent.getAction();
         if ("SHOW_NOTIFICATION".equals(action)){
             showNotification();
         }else if ("HIDE_NOTIFICATION".equals(action)){
             hideNotification();
         }else {
             if (!isForeground){
                 //verificar si el streaming esta disponible antes de reproducir
                 if (isStreamAvailable(url)){
                     exoplayer.play();
                 }else {
                     Log.e("RadioService", "streaming no esta disponible");
                     stopSelf();
                 }

             }
         }
     }
     return START_STICKY;
    }


    public void showNotification(){
        if (!isForeground){
            startForeground(1, createNotification());
            isForeground = true;

        }
    }

    public void hideNotification(){
        if (isForeground){
            stopForeground(true);
            isForeground = false;

        }
    }

    private Notification createNotification(){

        //intent para abrir la app al tocar la notification

        Intent notificationIntent = new Intent (this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


       NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
               . setContentTitle("AJPlay en segundo Plano")
               .setContentText("reproducuiendo en segudo plano")
               .setSmallIcon(R.drawable.iconradio)
               .setContentIntent(pendingIntent)
               .setPriority(NotificationCompat.PRIORITY_DEFAULT)
               .setOngoing(true);
       return builder.build();
    }




    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Canal de Radio", NotificationManager.IMPORTANCE_DEFAULT);
            serviceChannel.setDescription("notificacion de radio");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager !=null){
                manager.createNotificationChannel(serviceChannel);
            }
        }

    }


    //metodo para identificar si el url esta transmitiendo audio
    private boolean isStreamAvailable (String url) {
        HttpURLConnection connection = null;
        try {
            URL streamUrl = new URL(url);
            connection = (HttpURLConnection) streamUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            String contentType = connection.getContentType();

            //verifica el codigo de respuesta y el tipo de contenido

            return responseCode == HttpURLConnection.HTTP_OK &&
                    contentType != null && contentType.startsWith("audio/");
        } catch (Exception e) {
            Log.e("streamCheck", " error en la verificacion del URL: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    // metodo para determinar si el error es por conexion y de debe reconectar

    private boolean shouldReconnect(PlaybackException error) {
        return error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT;

    }

    //metodo para programar el intento  de reconexion

    private void scheduleReconnect(){

        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS){
            reconnectAttempts++;


        Log.e ("RadioService", "reintentado la conexion");
        new Handler(getMainLooper()).postDelayed(()-> {
            if (isStreamAvailable(url)){  //utiliza el metodo creado para verificar la transmicion del url
                reconnectAttempts = 0; //reinicia el contador si se conecta
                exoplayer.prepare(); // vuelve a preparar el reproductor
                exoplayer.play();

            }else{
                scheduleReconnect();; // intento de reconeccion si no se puede conectar
            }
        }, 5000);
        }else{
            Log.e ("RadioSercie ", "streaming no disponible, deteniendo el servicio");
                stopSelf();
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
