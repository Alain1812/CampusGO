package com.example.trabajodas3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsuario, etContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Enlazar los campos del layout
        etUsuario = findViewById(R.id.etLoginUsuario);
        etContrasena = findViewById(R.id.etLoginContrasena);
        Button btnIniciarSesion = findViewById(R.id.btnIniciarSesion);

        // Cuando se pulsa el botón, se intenta iniciar sesión
        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
        // Obtener lo que el usuario ha escrito
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        // Si hay campos vacíos, mostrar aviso
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un hilo para la conexión con el servidor
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                // URL del archivo PHP en el servidor
                URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/apedrueza002/WEB/GRUPAL/login.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);

                // Parámetros POST
                String parametros = "usuario=" + URLEncoder.encode(usuario, "UTF-8")
                        + "&contrasena=" + URLEncoder.encode(contrasena, "UTF-8");

                try (OutputStream os = conn.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(parametros);
                }

                // Procesar respuesta
                int statusCode = conn.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    runOnUiThread(() -> {
                        try {
                            JSONObject json = new JSONObject(response.toString());
                            if (json.has("success")) {
                                int userId = json.getInt("user_id");

                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                prefs.edit()
                                        .putInt("user_id", userId)
                                        .apply();

                                // Redirigir a HomeActivity
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, json.getString("error"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Error en la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error del servidor: " + statusCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}