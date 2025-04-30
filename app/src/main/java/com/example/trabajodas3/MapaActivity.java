package com.example.trabajodas3;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapaActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map;

    private final double CAMPUS_LAT = 42.8490;
    private final double CAMPUS_LON = -2.6727;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private Marker userMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_mapa);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Crear LocationRequest y LocationCallback
        locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    mostrarUbicacionEnMapa(locationResult.getLastLocation());
                }
            }
        };

        // Pedir permisos
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        // Marcador del campus
        GeoPoint campusPoint = new GeoPoint(CAMPUS_LAT, CAMPUS_LON);
        Marker campusMarker = new Marker(map);
        campusMarker.setPosition(campusPoint);
        campusMarker.setIcon(resizeIcon(R.drawable.ic_upv_location, 100, 100));
        campusMarker.setTitle("Campus de Álava");
        map.getOverlays().add(campusMarker);

        map.getController().setZoom(15.0);
        map.getController().setCenter(campusPoint);

        // ⚡ Botón flotante para cambiar transporte
        FloatingActionButton fabSelectorTransporte = findViewById(R.id.fabSelectorTransporte);
        fabSelectorTransporte.setOnClickListener(v -> mostrarSelectorTransporte());

        // Mostrar selector transporte al iniciar (si quieres solo la primera vez puedes luego controlar eso)
        mostrarSelectorTransporte();
    }


    private void requestPermissionsIfNecessary(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
                return;
            }
        }
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void mostrarUbicacionEnMapa(Location location) {
        GeoPoint userPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (userMarker == null) {
            userMarker = new Marker(map);
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            userMarker.setTitle("Tu ubicación actual");
            userMarker.setIcon(resizeIcon(R.drawable.ic_user_location, 100, 100));
            map.getOverlays().add(userMarker);
        }

        userMarker.setPosition(userPoint);
        map.getController().animateTo(userPoint);

        map.invalidate(); // refrescar el mapa
    }
    private Drawable resizeIcon(int drawableId, int width, int height) {
        Drawable image = ContextCompat.getDrawable(this, drawableId);
        if (image != null) {
            Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, false);
            return new BitmapDrawable(getResources(), resized);
        }
        return null;
    }
    private void mostrarSelectorTransporte() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_transporte, null);
        bottomSheetDialog.setContentView(sheetView);

        sheetView.findViewById(R.id.btnPie).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            cargarModoAPie();
        });

        sheetView.findViewById(R.id.btnBici).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            cargarModoBici();
        });

        sheetView.findViewById(R.id.btnBus).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            cargarModoBus();
        });

        bottomSheetDialog.show();
    }
    private void cargarModoAPie() {
        map.getOverlays().clear();

        GeoPoint campusPoint = new GeoPoint(CAMPUS_LAT, CAMPUS_LON);
        Marker campusMarker = new Marker(map);
        campusMarker.setPosition(campusPoint);
        campusMarker.setIcon(resizeIcon(R.drawable.ic_upv_location, 100, 100));
        campusMarker.setTitle("Campus de Álava");
        map.getOverlays().add(campusMarker);

        if (userMarker != null) {
            map.getOverlays().add(userMarker);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    GeoPoint userPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    calcularRutaAPieOSRM(userPoint, campusPoint);
                }
            });
        }
    }

    private void calcularRutaAPieOSRM(GeoPoint origen, GeoPoint destino) {
        new Thread(() -> {
            try {
                String urlString = String.format(
                        Locale.US,
                        "https://router.project-osrm.org/route/v1/foot/%f,%f;%f,%f?overview=full&geometries=geojson&alternatives=true",
                        origen.getLongitude(), origen.getLatitude(),
                        destino.getLongitude(), destino.getLatitude()
                );

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray routes = json.getJSONArray("routes");
                Log.d("RUTAS", "Número de rutas recibidas: " + routes.length());
                runOnUiThread(() -> {
                    try {
                        for (int i = 0; i < routes.length(); i++) {
                            JSONObject route = routes.getJSONObject(i);
                            JSONArray coords = route.getJSONObject("geometry").getJSONArray("coordinates");

                            List<GeoPoint> puntosRuta = new ArrayList<>();
                            for (int j = 0; j < coords.length(); j++) {
                                JSONArray coord = coords.getJSONArray(j);
                                double lon = coord.getDouble(0);
                                double lat = coord.getDouble(1);
                                puntosRuta.add(new GeoPoint(lat, lon));
                            }

                            Polyline linea = new Polyline();
                            linea.setPoints(puntosRuta);

                            if (i == 0) {
                                linea.setColor(0xFF2196F3); // Azul fuerte para principal
                                linea.setWidth(10f);
                            } else {
                                linea.setColor(0x552196F3); // Azul más claro para alternativas
                                linea.setWidth(6f);
                            }

                            map.getOverlays().add(linea);
                        }

                        map.invalidate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void cargarModoBici() {
        // Limpiar el mapa y restaurar marcadores base
        map.getOverlays().clear();

        // Añadir marcador del campus
        GeoPoint campusPoint = new GeoPoint(CAMPUS_LAT, CAMPUS_LON);
        Marker campusMarker = new Marker(map);
        campusMarker.setPosition(campusPoint);
        campusMarker.setIcon(resizeIcon(R.drawable.ic_upv_location, 100, 100));
        campusMarker.setTitle("Campus de Álava");
        map.getOverlays().add(campusMarker);

        // Restaurar marcador de usuario si existe
        if (userMarker != null) {
            map.getOverlays().add(userMarker);
        }

        // Centrar mapa en el campus
        map.getController().setCenter(campusPoint);
        map.getController().setZoom(14.0);

        // Cargar rutas de bicicleta desde GeoJSON
        cargarRutasBiciGeoJSON();

        // Opcional: Actualizar con la ubicación actual
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    mostrarUbicacionEnMapa(location);
                }
            });
        }
    }

    private void cargarRutasBiciGeoJSON() {
        new Thread(() -> {
            try {
                InputStream is = getAssets().open("viasciclistas23.geojson");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder jsonContent = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
                reader.close();

                JSONObject geoJson = new JSONObject(jsonContent.toString());
                JSONArray features = geoJson.getJSONArray("features");

                runOnUiThread(() -> {
                    try {
                        for (int i = 0; i < features.length(); i++) {
                            JSONObject feature = features.getJSONObject(i);

                            // Validar si "geometry" existe y no es null
                            if (!feature.has("geometry") || feature.isNull("geometry")) {
                                Log.e("GeoJSON", "Feature sin geometry en posición: " + i);
                                continue;
                            }

                            JSONObject geometry = feature.getJSONObject("geometry");

                            // Validar el tipo de geometría
                            if (!geometry.getString("type").equals("LineString")) {
                                continue;
                            }

                            // Validar si "coordinates" existe
                            if (!geometry.has("coordinates")) {
                                Log.e("GeoJSON", "Geometry sin coordinates en posición: " + i);
                                continue;
                            }

                            JSONArray coordinates = geometry.getJSONArray("coordinates");
                            List<GeoPoint> puntosRuta = new ArrayList<>();

                            for (int j = 0; j < coordinates.length(); j++) {
                                JSONArray coord = coordinates.getJSONArray(j);
                                if (coord.length() < 2) continue;
                                double lon = coord.getDouble(0);
                                double lat = coord.getDouble(1);
                                puntosRuta.add(new GeoPoint(lat, lon));
                            }

                            Polyline rutaBici = new Polyline();
                            rutaBici.setPoints(puntosRuta);
                            rutaBici.setColor(Color.parseColor("#4CAF50"));
                            rutaBici.setWidth(8.0f);
                            map.getOverlays().add(rutaBici);
                        }

                        map.invalidate();
                        Toast.makeText(MapaActivity.this, "Rutas ciclistas cargadas", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Log.e("GeoJSON", "Error crítico: " + e.getMessage());
                        Toast.makeText(MapaActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e("GeoJSON", "Error al leer archivo: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(MapaActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void cargarModoBus() {
        // Limpiar el mapa y restaurar marcadores base
        map.getOverlays().clear();

        // Añadir marcador del campus
        GeoPoint campusPoint = new GeoPoint(CAMPUS_LAT, CAMPUS_LON);
        Marker campusMarker = new Marker(map);
        campusMarker.setPosition(campusPoint);
        campusMarker.setIcon(resizeIcon(R.drawable.ic_upv_location, 100, 100));
        campusMarker.setTitle("Campus de Álava");
        map.getOverlays().add(campusMarker);

        // Restaurar marcador de usuario si existe
        if (userMarker != null) {
            map.getOverlays().add(userMarker);
        }

        // Centrar mapa en el campus
        map.getController().setCenter(campusPoint);
        map.getController().setZoom(14.0);

        // Cargar rutas de autobús desde GTFS
        cargarRutasBusGTFS();

        // Actualizar ubicación del usuario
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    mostrarUbicacionEnMapa(location);
                }
            });
        }
    }

    private void cargarRutasBusGTFS() {
        new Thread(() -> {
            try {
                List<route> routes = leerRoutesTXT();
                List<trip> trips = leerTripsTXT();
                Map<String, List<GeoPoint>> shapes = leerShapesTXT();

                runOnUiThread(() -> {
                    for (route ruta : routes) {
                        for (trip trip : trips) {
                            if (trip.getRouteId().equals(ruta.getRouteId())) {
                                String shapeId = trip.getShapeId();
                                if (shapes.containsKey(shapeId)) {
                                    List<GeoPoint> puntos = shapes.get(shapeId);
                                    Polyline linea = new Polyline();
                                    linea.setPoints(puntos);
                                    linea.setColor(Color.parseColor("#" + ruta.getRouteColor()));
                                    linea.setWidth(10f);
                                    map.getOverlays().add(linea);
                                    Log.d("BusDebug", "Ruta " + shapeId + " cargada");
                                } else {
                                    Log.e("BusDebug", "Shape_id " + shapeId + " no existe");
                                }
                            }
                        }
                    }
                    map.invalidate();
                });
            } catch (Exception e) {
                Log.e("Bus", "Error crítico: " + e.getMessage());
            }
        }).start();
    }

    private List<route> leerRoutesTXT() {
        List<route> routes = new ArrayList<>();
        try {
            InputStream is = getAssets().open("routes.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            boolean primeraLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                String[] datos = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Manejar comas en campos
                String routeId = datos[0].trim();
                String shortName = datos[2].replace("\"", "").trim();
                String color = datos[7].replace("\"", "").trim();

                if (color.isEmpty()) color = "000000"; // Color por defecto si está vacío
                routes.add(new route(routeId, shortName, color));
            }
            reader.close();
        } catch (Exception e) {
            Log.e("Bus", "Error leyendo routes.txt: " + e.getMessage());
        }
        return routes;
    }

    private List<trip> leerTripsTXT() {
        List<trip> trips = new ArrayList<>();
        try {
            InputStream is = getAssets().open("trips.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            boolean primeraLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                String[] datos = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                String routeId = datos[0].trim();
                String shapeId = datos.length > 7 ? datos[7].trim() : "";
                trips.add(new trip(routeId, shapeId));
            }
            reader.close();
        } catch (Exception e) {
            Log.e("Bus", "Error leyendo trips.txt: " + e.getMessage());
        }
        return trips;
    }

    private Map<String, List<GeoPoint>> leerShapesTXT() {
        Map<String, List<GeoPoint>> shapes = new HashMap<>();
        try {
            InputStream is = getAssets().open("shapes.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            boolean primeraLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                String[] datos = line.split(",");
                String shapeId = datos[0].trim();
                double lat = Double.parseDouble(datos[1].trim());
                double lon = Double.parseDouble(datos[2].trim());

                if (!shapes.containsKey(shapeId)) {
                    shapes.put(shapeId, new ArrayList<>());
                }
                shapes.get(shapeId).add(new GeoPoint(lat, lon));
            }
            reader.close();
        } catch (Exception e) {
            Log.e("Bus", "Error leyendo shapes.txt: " + e.getMessage());
        }
        return shapes;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startLocationUpdates();
            }
        }
    }
    private class route {
        private String routeId;
        private String routeShortName;
        private String routeColor;

        public route(String routeId, String routeShortName, String routeColor) {
            this.routeId = routeId;
            this.routeShortName = routeShortName;
            this.routeColor = routeColor;
        }

        // Getters
        public String getRouteId() { return routeId; }
        public String getRouteShortName() { return routeShortName; }
        public String getRouteColor() { return routeColor; }
    }

    private class shape {
        private String shapeId;
        private List<GeoPoint> coordinates;

        public shape(String shapeId, List<GeoPoint> coordinates) {
            this.shapeId = shapeId;
            this.coordinates = coordinates;
        }

        // Getters
        public String getShapeId() { return shapeId; }
        public List<GeoPoint> getCoordinates() { return coordinates; }
    }

    public class trip {
        private final String routeId;
        private final String shapeId; // Usar String para manejar números y texto

        public trip(String routeId, String shapeId) {
            this.routeId = routeId;
            this.shapeId = shapeId.replaceAll("[^0-9]", ""); // Eliminar no numéricos
        }

        public String getRouteId() { return routeId; }
        public String getShapeId() { return shapeId; }
    }
}


