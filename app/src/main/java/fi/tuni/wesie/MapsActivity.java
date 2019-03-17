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
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private GoogleMap mMap;
    private LocationManager manager;
    private PlacesClient placesClient;
    private String clikedPlace;

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
        placesClient = Places.createClient(this);
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
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        SetOnClick();
        mMap.setOnPoiClickListener(this);
        moveHome();
        mMap.setInfoWindowAdapter(customInfo());
    }

    private GoogleMap.InfoWindowAdapter customInfo() {
        GoogleMap.InfoWindowAdapter result = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.windowlayout, null);
                LatLng latLng = marker.getPosition();
                TextView info = v.findViewById(R.id.Info);
                info.setText(clikedPlace);
                return v;
            }
        };
        return result;
    }

    /**
     *
     */
    private void moveHome() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            mMap.clear();
            MarkerOptions mark = new MarkerOptions().position(arg0);
            mMap.addMarker(mark);
        });
    }

    /**
     *
     */
    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{ACCESS_FINE_LOCATION}, 1);
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

    @Override
    public void onPoiClick(PointOfInterest poi) {
        // Define a Place ID.
        String placeId = poi.placeId;

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.PHONE_NUMBER,
                Place.Field.PRICE_LEVEL,
                Place.Field.RATING);

        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.d("onPoiClick", ": " + place.toString());
            mMap.clear();
            clikedPlace = new String (place.getName() + "\n" +
                            "Rating: " + place.getRating() + "\n" +
                            "Price level: " + place.getPriceLevel() + "\n" +
                            "Phone number: " + place.getPhoneNumber());
            Marker mark = mMap.addMarker(new MarkerOptions().position(poi.latLng));
            mark.showInfoWindow();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                // Handle error with given status code.
                Log.e("onPoiClick", "Place not found: " + exception.getMessage());
            }
        });
    }
}
