package com.example.ajplayer;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button boton ;
    private boolean isPlaying = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        boton = findViewById(R.id.btn_paly);



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


    private void startPlayerService(){

        Intent serviceIntent = new Intent(this, RadioService.class);
        startService(serviceIntent);
        isPlaying = true;
        boton.setBackgroundResource(R.drawable.pause);
        Toast.makeText(this, "Reproduccion Iniciada", Toast.LENGTH_SHORT).show();
    }


    private void stopPlayerService(){
        Intent serviceIntent =  new Intent(this, RadioService.class);
        stopService(serviceIntent);
        isPlaying = false;
        boton.setBackgroundResource(R.drawable.play);
        Toast.makeText(this, "Reproduccion detenida", Toast.LENGTH_SHORT).show();


    }


}