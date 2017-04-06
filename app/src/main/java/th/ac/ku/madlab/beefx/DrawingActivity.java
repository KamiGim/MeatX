package th.ac.ku.madlab.beefx;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.location.LocationListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import th.ac.ku.madlab.kubeef.R;

public class DrawingActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    LinearLayout mDrawingPad;
    Bitmap img;
    Drawable d;
    ImageButton reset_button;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;

    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    double latitude;
    double longtitude;

    private static final String TAG = "GPS_Google";

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    String lat;
    String provider;
    protected boolean gps_enabled,network_enabled;

    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

//        Bundle b = getIntent().getExtras();
//        img = (Bitmap) b.get("img");

        byte[] byteArray = getIntent().getByteArrayExtra("img");
        img = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        final DrawingView mDrawingView=new DrawingView(this);
        mDrawingPad=(LinearLayout)findViewById(R.id.DrawingPad);
        mDrawingPad.addView(mDrawingView);

        reset_button = (ImageButton)findViewById(R.id.buReset);

        File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File file = new File(dir,"resize.png");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            img.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
//            img.recycle();
//                scaled.recycle();
        } catch (Exception e) {}

        String newPath = file.getAbsolutePath();

        d = Drawable.createFromPath(newPath);
        mDrawingPad.setBackground(d);

        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("setbackground","true");
                mDrawingView.clear();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        checkLocation();

//        TelephonyManager telephonyManager;
//
//        telephonyManager = (TelephonyManager) getSystemService(Context.
//                TELEPHONY_SERVICE);
//        deviceId = telephonyManager.getDeviceId();

//        String subscriberId = telephonyManager.getSubscriberId();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {

            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        latitude = location.getLatitude();
        longtitude = location.getLongitude();

        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void buScan(View view) {
        Log.d("button :","press");
        mDrawingPad.buildDrawingCache(true);
        mDrawingPad.setDrawingCacheEnabled(true);
        Bitmap b = Bitmap.createBitmap(mDrawingPad.getDrawingCache(true));

        int w = b.getWidth();
        int h = b.getHeight();
        Bitmap scale = Bitmap.createScaledBitmap(img, w, h, true);
//        img.recycle();

        try {
            //Write file
            String filenameOri = "original.png";
            FileOutputStream stream1 = this.openFileOutput(filenameOri, Context.MODE_PRIVATE);
            scale.compress(Bitmap.CompressFormat.PNG, 100, stream1);

            //Write file
            String filenameRedMask = "imgWithRedMask.png";
            FileOutputStream stream2 = this.openFileOutput(filenameRedMask, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, stream2);

            //Cleanup
            stream1.close();
            scale.recycle();

            //Cleanup
            stream2.close();
            b.recycle();

            //Pop intent
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("imgOri", filenameOri);
            intent.putExtra("imgRedMask", filenameRedMask);
            intent.putExtra("lat", String.valueOf(latitude));
            intent.putExtra("long", String.valueOf(longtitude));

            String url="http://madlab.cpe.ku.ac.th/supab/tracking.php?log="+  String.valueOf(longtitude)+"&lat="+
                String.valueOf(latitude)+"&device_id="+deviceId;
            Log.d("Tracking Url",url);

//            new MyAsyncTaskgetNews().execute(url);

//            String url="http://madlab.cpe.ku.ac.th/supab/insert.php?UserName=root&Password=1234";
//            new MyAsyncTaskgetNews().execute(url);

            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
