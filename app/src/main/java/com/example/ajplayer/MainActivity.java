package com.example.ajplayer;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


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


}