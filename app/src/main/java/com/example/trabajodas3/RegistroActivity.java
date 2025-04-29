package com.example.trabajodas3;

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

public class RegistroActivity extends AppCompatActivity {
    private EditText etUsuario, etContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Vincular elementos de la interfaz
        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        // Configurar acción del botón registrar
        btnRegistrar.setOnClickListener(v -> enviarDatos());
    }

    // Metodo para enviar datos de registro al servidor
    private void enviarDatos() {
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        // Validar campos vacíos
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hilo para conexión de red
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/apedrueza002/WEB/GRUPAL/registro.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);

                // Codificar parámetros
                String parametros = "usuario=" + URLEncoder.encode(usuario, "UTF-8")
                        + "&contrasena=" + URLEncoder.encode(contrasena, "UTF-8");

                // Escribir datos
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

                    // Procesar respuesta en hilo principal
                    runOnUiThread(() -> {
                        try {
                            JSONObject json = new JSONObject(response.toString());
                            if (json.has("success")) {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                finish();  // Cierra la actividad y vuelve a MainActivity
                            } else if (json.has("error")) {
                                Toast.makeText(this, json.getString("error"), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Respuesta no válida", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error del servidor: " + statusCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show());
            } finally {
                // Cerrar conexión
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}