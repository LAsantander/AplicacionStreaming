package com.example.ajplayer;

import android.Manifest;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.security.Permission;
import java.security.Permissions;


public class MainActivity extends AppCompatActivity {
    private LottieAnimationView equalizer;
    private Button boton ;
    private boolean isPlaying = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        boton = findViewById(R.id.btn_paly);
        equalizer = findViewById(R.id.gift_equalizer);



        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isPlaying){
                    stopPlayerService();

                }else{
                    startPlayerService();
                }
            }
        });

    }


    private void startPlayerService() {
        //verficar y solicitar permisos en android superiores a 13

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                return;
            }

        }



        Intent serviceIntent = new Intent(this, RadioService.class);
        startService(serviceIntent);
        isPlaying = true;
        boton.setBackgroundResource(R.drawable.pause);
        if (equalizer != null) {
            try {
                equalizer.setVisibility(View.VISIBLE);
                equalizer.setAnimation(R.raw.equalizer);
                equalizer.setRepeatCount(LottieDrawable.INFINITE);
                equalizer.playAnimation();
            } catch (Exception e) {
                Log.e("LottieError", "error en la carga de la animacion" + e.getMessage());
            }


            Toast.makeText(this, "Reproduccion Iniciada", Toast.LENGTH_SHORT).show();
        }
    }


    private void stopPlayerService(){
        Intent serviceIntent =  new Intent(this, RadioService.class);
        stopService(serviceIntent);
        isPlaying = false;
        boton.setBackgroundResource(R.drawable.play);
        equalizer.cancelAnimation();
        equalizer.setProgress(0);
        equalizer.setVisibility(View.GONE);
        Toast.makeText(this, "Reproduccion detenida", Toast.LENGTH_SHORT).show();


    }



    public void onRequestPermissionsResult( int requestCode, String[]permissions, int [] grandResults){
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);

        if (requestCode == 1){
            if (grandResults.length > 0 && grandResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPlayerService();
                Log.d("MainActivity", "permiso concedido");
            }else  {
                //permiso denegado avisar al usuario
                Toast.makeText(this, "permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onPause(){
        super.onPause();
        //notificaal srvicio para que muestre la notificacion
        Intent intent = new Intent(this, RadioService.class);
        intent.setAction("SHOW_NOTIFICATION");
        startService(intent);

    }


@Override
protected void onResume(){
        super.onResume();
        //notifica al servicio para que oculte la notificacion
    Intent intent = new Intent(this, RadioService.class);
    intent.setAction("HIDE_NOTIFICATION");
    startService(intent);
}





}