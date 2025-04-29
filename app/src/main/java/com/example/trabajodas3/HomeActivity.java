package com.example.trabajodas3;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btnMapa).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MapaActivity.class)));

        findViewById(R.id.btnHorarios).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, HorariosActivity.class)));

        findViewById(R.id.btnAlarmas).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, AlarmasActivity.class)));

        findViewById(R.id.btnHistorial).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, HistorialActivity.class)));
    }

}