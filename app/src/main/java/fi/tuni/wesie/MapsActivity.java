package fi.tuni.wesie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager manager;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Places
        Places.initialize(getApplicationContext(), "AIzaSyCB59BRagw7ZSAhen1IO_rSjRCMsjAEvaw");
    }

    /*
    private void search() {
        imgButton.((TextView v, int actionId, KeyEvent event) -> {
            int AUTOCOMPLETE_REQUEST_CODE = 1;

            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                locate();
            }

            return false;
        });
    }
    */

    private void locate() {
        /*
        String search = mSearchText.getText().toString();
        Geocoder coder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = coder.getFromLocationName(search,1);
        } catch (IOException e) {
            Log.d("locate()","not working " + e.getMessage());
        }

        if(list.size() > 0) {
            Address address = list.get(0);
            Log.d("Osote", address.toString());
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()), 14f, address.getFeatureName());
        }
        */
    }

    /**
     *
     * @param latlng
     * @param zoom
     * @param title
     */
    private void moveCamera(LatLng latlng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
        if (!title.equals("CurrentLocation")) {
            MarkerOptions option = new MarkerOptions().position(latlng);
            mMap.addMarker(option);
        }
    }

    /**
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocationPermission();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        SetOnClick();
    }

    /**
     *
     */
    private void moveHome() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng currentlocation = new LatLng(loc.getLatitude(), loc.getLongitude());
        moveCamera(currentlocation, 10f, "CurrentLocation");
    }

    /**
     *
     */
    private void SetOnClick() {
        mMap.setOnMapClickListener((LatLng arg0) -> {
            Log.d("SetOnClick", arg0.toString());
            // TODO: set marker on the clicked location
        });
    }

    /**
     *
     */
    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
    }

    /**
     *
     * @param view
     */
    public void search(View view) {
        ImageButton button = findViewById(view.getId());
        button.setOnClickListener((View v) -> {
            int AUTOCOMPLETE_REQUEST_CODE = 1;
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    /**
     *
     * @param view
     */
    public void GoToSelf(View view) {
        ImageButton button = findViewById(view.getId());
        button.setOnClickListener((View v) -> moveHome());
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                moveCamera(place.getLatLng(),14f,place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("onActivityResult", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
