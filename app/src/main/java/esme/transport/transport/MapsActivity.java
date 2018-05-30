package esme.transport.transport;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        SensorEventListener {

    // Variables Google Map
    private GoogleMap mMap;
    private LocationManager lm;

    // Variables des coordonnées GPS
    private double latitude;
    private double longitude;
    private double latitudeAvant;
    private double longitudeAvant;
    private String nomDeRue;

    // Variables de décision pour marquer dans le fichier
    private boolean Go = false;
    private boolean GeoBegin = false;

    // Variables de calcul statistique
    private double sommeTotal = 0;
    private int nbElements = 0;
    private double moyenne = 0;
    private double sommeETCarre = 0;
    private double ecartType = 0;
    private boolean mouvNulle = false;
    private boolean mouvLent = false;
    private boolean mouvNormal = true;
    private boolean mouvRapide = false;

    // Variables de décision de dessin de tracé
    private int nbTrue;
    private int nbFalse;
    private boolean creux = false;
    private boolean creux2 = false;
    double latCreux;
    double longCreux;

    // Variables de choix de transport (page précèdente)
    final String choix = "choixTransport";
    String modeTransport = "";

    // Le sensor manager (gestionnaire de capteurs)
    SensorManager sensorManager;

    // Variables de l'accéléromètre
    Sensor accelerometer;
    private double aX;
    private double aY;
    private double aZ;

    //private FusedLocationProviderClient mFusedLocationClient;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        if (intent != null) {
            modeTransport = intent.getStringExtra(choix);
        }

        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        TextView textGo = findViewById(R.id.textGo);
        textGo.setVisibility(View.GONE);
        readFile();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (location != null)
            onLocationChanged(location);
        else
            Toast.makeText(this, "Position GPS perdu. Verifiez que le signal GPS est bien activé.",
                    Toast.LENGTH_LONG).show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void readFile() {

        File chemin = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File fichier = new File(chemin, "fichier.txt");
        String ligne;

        try {
            FileReader fReader = new FileReader(fichier);
            BufferedReader bReader = new BufferedReader(fReader);

            int cpt = 0;
            boolean isChoice = false;

            while ((ligne = bReader.readLine()) != null) {
                if (cpt == 0) {
                    ligne = ligne.replaceAll(" :", "");
                    if (modeTransport.equals(ligne))
                        isChoice = true;
                    cpt++;
                }
                else {
                    cpt = 0;
                    if (isChoice) {
                        String tab[] = ligne.replaceAll("\"", "/").split("/");
                        double vitesse = Double.parseDouble(tab[11].replaceAll(",", "."));
                        sommeTotal += vitesse;
                        nbElements++;
                        moyenne = (sommeTotal / nbElements);
                        sommeETCarre += Math.pow(vitesse, 2);
                    }
                }
            }

            ecartType = Math.sqrt((sommeETCarre / nbElements) - Math.pow(moyenne, 2));

        } catch (Exception e) {
            return;
        }

    }

    public void changeGo(View view) {

        if (!Go) {
            Go = true;
            Toast.makeText(this, "C'est parti !", Toast.LENGTH_LONG).show();
            Button button = (Button) view;
            button.setText("Stop");

            TextView textGo = findViewById(R.id.textGo);
            textGo.setVisibility(View.VISIBLE);
        }
        else {
            Go = false;
            Toast.makeText(this, "C'est fini !", Toast.LENGTH_LONG).show();
            Button button = (Button) view;
            button.setText("Go");

            TextView textGo = findViewById(R.id.textGo);
            textGo.setVisibility(View.GONE);
        }
    }

    public void writeInFile(java.text.DecimalFormat df, double currentVitesse) {

        try {
            File chemin = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File fichier = new File(chemin, "fichier.txt");
            FileWriter filewriter = new FileWriter(fichier, true);

            String qualiteMouv = "";

            if (mouvNulle)
                qualiteMouv += "Mouvement nul";
            else if (mouvLent)
                qualiteMouv += "Mouvement lent";
            else if (mouvNormal)
                qualiteMouv += "Mouvement Normal";
            else if (mouvRapide)
                qualiteMouv += "Mouvement Rapide";

            sommeTotal += currentVitesse;
            nbElements++;
            moyenne = (sommeTotal / nbElements);
            sommeETCarre += Math.pow(currentVitesse, 2);
            ecartType = Math.sqrt((sommeETCarre / nbElements) - Math.pow(moyenne, 2));

            String toPrint = modeTransport + " :\n" + String.format("\"" + latitude + "\", ") + String.format(
                    "\"" + longitude + "\", ") + String.format("\"" + df.format(aX) + "\", \"" +
                    df.format(aY) + "\", \"" + df.format(aZ) + "\", \"") + df.format(currentVitesse) + "\", \"" +
                    currentDateFormat() + "\", \"" + qualiteMouv + "\", \"[" +
                    df.format(moyenne-ecartType) + ";"
                    + df.format(moyenne+ecartType) + "]\", \"" + nomDeRue + "\"\n";

            filewriter.write(toPrint);
            filewriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String currentDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmss");
        String currentTimeStamp = dateFormat.format(new Date());
        return currentTimeStamp;
    }

    public void drawPolyline(double currentVitesse) {

        if (currentVitesse > 0) {
            if (nbFalse < 2) {
                if (creux) {
                    mMap.addPolyline((new PolylineOptions())
                            .add(new LatLng(latCreux, longCreux),
                                    new LatLng(latitudeAvant, longitudeAvant))
                            .width(5).color(Color.BLUE)
                            .geodesic(true));
                    creux = false;
                }
                mMap.addPolyline((new PolylineOptions())
                        .add(new LatLng(latitudeAvant, longitudeAvant),
                                new LatLng(latitude, longitude))
                        .width(5).color(Color.BLUE)
                        .geodesic(true));
            }
            nbTrue++;
        }
        else {
            if (nbTrue >= 2) {
                if (!creux) {
                    creux = true;
                    latCreux = latitudeAvant;
                    longCreux = longitudeAvant;
                }
                nbFalse = 0;
            }
            else {
                nbTrue = 0;
                creux = false;
                nbFalse++;
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("entree","yesss goooo");
        latitudeAvant = latitude;
        longitudeAvant = longitude;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        double currentVitesse = location.getSpeed() * 3.6;

        java.text.DecimalFormat df = new java.text.DecimalFormat("0.##");

        if (!GeoBegin) {
            Log.d("Zoom", "Ok");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 18));
            GeoBegin = true;
        }

        if (Go) {

            Log.d("Test", "Ok");

            Geocoder geoCoder = new Geocoder(getBaseContext());
            List<Address> matches = null;
            try {
                matches = geoCoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address bestMatch = (matches.isEmpty() ? null : matches.get(0));

            nomDeRue = bestMatch.getThoroughfare();
            mouvLent = false;
            mouvNormal = false;
            mouvNulle = false;
            mouvRapide = false;

            String chaineAcceleration = "";

            if (currentVitesse == 0) {
                chaineAcceleration = "Mouvement nul : " + df.format(currentVitesse);
                mouvNulle = true;
            }
            else if (currentVitesse < (moyenne - ecartType)) {
                chaineAcceleration = "Mouvement lent : " + df.format(currentVitesse);
                mouvLent = true;
            }
            else if (currentVitesse > (moyenne + ecartType)) {
                chaineAcceleration = "Mouvement rapide : " + df.format(currentVitesse);
                mouvRapide = true;
            }
            else {
                chaineAcceleration = "Mouvement normal : " + df.format(currentVitesse);
                mouvNormal = true;
            }

            TextView textGo = findViewById(R.id.textGo);
            textGo.setText(String.format(chaineAcceleration));
            this.writeInFile(df, currentVitesse);
            this.drawPolyline(currentVitesse);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            aX = event.values[0];
            aY = event.values[1];
            aZ = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
