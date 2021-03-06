package com.example.proyect_a;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText buscarMapsEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        buscarMapsEditText = findViewById(R.id.buscarMapsEditText);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        cargar_ubicaciones();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String titulo = marker.getTitle();
                Intent intent = new Intent(MapsActivity.this, UpdateNote.class);
                intent.putExtra("lugar", titulo);
                startActivity(intent);
                return false;
            }
        });
    }

    //Boton para volver al Main
    public void backToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Carga todas las ubicaciones validas de la base ded datos al mapa
    public void cargar_ubicaciones() {
        //Busca en la base de datos y trae las coordenadas
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "notas", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();
        String[] campos = new String[]{"lugar", "latitud", "longitud"};
        Cursor cursor = db.query("notas", campos, null, null, null, null, null);
        LatLng localiz = null;

        //Si existen localizaciones guardadas las carga
        if (cursor.moveToFirst() && cursor.getCount() >= 1) {
            //Añade todos los marcadores
            do {
                String lugar = cursor.getString(0);
                double lat = cursor.getDouble(1);
                double lon = cursor.getDouble(2);
                //Comprueba que la ubicacion no sea la vacia
                if (lat != 0 && lon != 0) {
                    localiz = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(localiz).title(lugar));
                }
            } while (cursor.moveToNext());
        } else {
            Toast.makeText(this, "No se han añadido ubicaciones", Toast.LENGTH_SHORT).show();
        }
    }


    //Centra la camara en las coordenadas del lugar indicado
    public void buscarLugares(View view) {
        //Obtiene la busqueda
        String busqueda = buscarMapsEditText.getText().toString();

        //Realiza la busqueda en la base de datos y obtiene los campos
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "notas", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();
        String[] campos = new String[]{"latitud", "longitud"};
        String[] lugar = new String[]{busqueda};
        Cursor cursor = db.query("notas", campos, "lugar =?", lugar, null, null, null);

        //Si existe y no es no asignada
        if (cursor.moveToFirst() && !busqueda.equals("Lugar no asignado")) {
            //Obtiene las coordenadas del cursor
            Double lat = cursor.getDouble(0);
            Double lon = cursor.getDouble(1);
            //Comprueba que no sean las coordenadas por defecto
            if (lat != 0.0 && lon != 0.0) {
                //Mueve la camara
                LatLng localiz = new LatLng(lat, lon);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(localiz));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
            } else {
                Toast.makeText(this, "Ubicacion no asignada", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "No se ha encontrado la ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    //Refresca la vista del mapa
    public void refresh(View view) {
        cargar_ubicaciones();
    }
}