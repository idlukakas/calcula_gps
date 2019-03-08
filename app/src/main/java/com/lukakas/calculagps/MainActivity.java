package com.lukakas.calculagps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location firstLocation;

    private double latInitial, longInitial, latAtual, longAtual;

    private TextView travelledDistanceResultTextView;
    private TextInputLayout placeTextInputLayout;

    private Chronometer pastTimeChronometer;
    private boolean running;

    private Button allowGpsPermissionButton;
    private Button enableGpsButton;
    private Button disableGpsButton;
    private Button startRouteButton;
    private Button stopRouteButton;

    private static final int REQUEST_PERMISSSION_GPS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        travelledDistanceResultTextView = findViewById(R.id.travelledDistanceResultTextView);
        pastTimeChronometer = findViewById(R.id.pastTimeChronometer);
        placeTextInputLayout = findViewById(R.id.placeTextInputLayout);
        pastTimeChronometer = findViewById(R.id.pastTimeChronometer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        allowGpsPermissionButton = findViewById(R.id.allowGpsPermissionButton);
        enableGpsButton = findViewById(R.id.enableGpsButton);
        disableGpsButton = findViewById(R.id.disableGpsButton);
        startRouteButton = findViewById(R.id.startRouteButton);
        stopRouteButton = findViewById(R.id.stopRouteButton);

        allowGpsPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(
                        view.getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            2000,
                            5,
                            locationListener
                    );
                } else {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[] {
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            REQUEST_PERMISSSION_GPS
                    );
                }
            }
        });

        enableGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(
                        view.getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
                    intent.putExtra("enabled", true);
                    sendBroadcast(intent);
                } else {
                    Toast.makeText(MainActivity.this,
                            "É necessário dar permissão de acesso ao GPS",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        disableGpsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (isLocationEnabled(view.getContext())) {
                    // Desligar o GPS
                } else {
                    Toast.makeText(view.getContext(),
                            "O GPS não está ativado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pastTimeChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                locationListener =
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                float distance = location.distanceTo(firstLocation);

                                travelledDistanceResultTextView.setText(Float.toString(distance));
                            }

                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {

                            }

                            @Override
                            public void onProviderEnabled(String s) {

                            }

                            @Override
                            public void onProviderDisabled(String s) {

                            }
                        };
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String placeText = placeTextInputLayout.getEditText().getText().toString();

                Uri uri =
                        Uri.parse(
                                String.format(Locale.getDefault(),
                                        "geo:%f,%f?q=%s",
                                        latAtual,
                                        longAtual,
                                        placeText
                                )
                        );
                Intent intent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                uri
                        );
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        locationListener =
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        latInitial = location.getLatitude();
                        longInitial = location.getLongitude();
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                };
    }

    public void startRouteButton(View v) {
        if (isLocationEnabled(this)) {
            if (!running) {
                locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                locationListener =
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                firstLocation = location;
                            }
                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {

                            }

                            @Override
                            public void onProviderEnabled(String s) {

                            }

                            @Override
                            public void onProviderDisabled(String s) {

                            }
                        };

                pastTimeChronometer.setBase(SystemClock.elapsedRealtime());
                pastTimeChronometer.start();
                running = true;
            }
        } else {
            Toast.makeText(this, "É necessário habilitar o GPS", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopRouteButton(View v) {
        pastTimeChronometer.stop();
        pastTimeChronometer.setBase(SystemClock.elapsedRealtime());
        running = false;

        Toast.makeText(this, "Tem que exibir alguma coisa aqui", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSSION_GPS){
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            2000,
                            5,
                            locationListener
                    );
                }
            } else {
                Toast.makeText(this,
                        getString(R.string.no_gps_no_app),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }
}
