/*Acknoledgement: https://www.youtube.com/playlist?list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt*/

package com.example.buiquocanh.mygooglemap;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOACATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 10f;
    private static final LatLng mDefaultLocation = new LatLng(10.762622,106.660172);
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int CURRENT_MARKER_TAG = 9999;
    private FusedLocationProviderClient mFusedLocationClient;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private Location currentLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Place currentMarkedPlace;
    private Marker currentMarker;
    private ClinicInfo clinicInfo;
    private List<Marker> groundOverlays;
    private String jsonString;
    private String message;
    private List<Clinic> items;
    private String clinicSnippet;



    private boolean locationPermissionGranted = false;

    private int[] ids = {28, 30, 47, 50, 72, 73, 95};
    private String[] types = {"DENTIST", "DOCTOR", "HEALTH", "HOSPITAL", "PHARMACY", "PHYSIOTHERAPIST", "VETERINARY CARE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        clinicSnippet = "";
        items = new ArrayList<>();
        groundOverlays = new ArrayList<>();
        clinicInfo = new ClinicInfo();
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        toastMessage("MAP IS READY");
        mMap = googleMap;



        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        getCurrentLocation();

        updateLocationUI();


        new GetClinic().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater tdlMenuInflater = getMenuInflater();
        tdlMenuInflater.inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.searchBar:
                openSearchBar();
                return true;
            case R.id.addNewClinic:
                addNewClinic();
                return true;
            case R.id.filterType:
                chooseFilter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            locationPermissionGranted = false;
                            return;
                        }
                    }
                    locationPermissionGranted = true;
                    //inititalize map
                    initMap();
                }
            }
        }
    }

    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOACATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOACATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                initMap();
            } else
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        } else
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);

    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void getCurrentLocation() {
        try {
            if (locationPermissionGranted) {
                Task locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            currentLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(currentLocation.getLatitude(),
                                            currentLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_clinic_marker_64))
                .snippet(getContentFromPlace(currentMarkedPlace));
        if (currentMarker != null)
            currentMarker.remove();

        currentMarker = mMap.addMarker(options);
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                currentLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void openSearchBar() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(), PLACE_AUTOCOMPLETE_REQUEST_CODE);
            dialog.show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
            toastMessage("ERROR: SERVICE IS NOT AVAILABLE");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                currentMarkedPlace = PlaceAutocomplete.getPlace(this, data);
                Log.d(TAG, "PLACE ID: " + currentMarkedPlace.getId());
                moveCamera(currentMarkedPlace.getLatLng(), DEFAULT_ZOOM, currentMarkedPlace.getAddress().toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                toastMessage("ERROR: CANNOT GET RESULT");
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void addNewClinic() {
        if (currentMarkedPlace != null) {
            if (isClinical()) {
                clinicInfo.setLocationId(currentMarkedPlace.getId());
                clinicInfo.setLocationName(currentMarkedPlace.getName());
                clinicInfo.setAddress(currentMarkedPlace.getAddress());
                clinicInfo.setRating(currentMarkedPlace.getRating());
                clinicInfo.setSpecializations(currentMarkedPlace.getPlaceTypes());
                clinicInfo.setAvrPrice(currentMarkedPlace.getPriceLevel());
                clinicInfo.setLatLng(currentMarkedPlace.getLatLng());
                clinicInfo.setContent(getContentFromPlace(currentMarkedPlace));
                openInputDialogForLeadPhysician();
            } else
                toastMessage("THIS LOCATION IS NOT CLINICAL");
        }
        else
            toastMessage("ERROR: NO INDICATOR FOR LOCATION");
    }

    private boolean isClinical() {
        for (int id: currentMarkedPlace.getPlaceTypes()) {
            for (int i = 0; i < ids.length; i++) {
                if ((id == ids[i]))
                    return true;
            }
        }
        return false;
    }

    private void openInputDialogForLeadPhysician() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a new clinic");
        builder.setMessage("Set lead physician info");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);


        // Set up the buttons
        builder.setPositiveButton("NEXT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().matches("")) {
                    clinicInfo.setLeadPhysician(input.getText().toString());
                    openInputDialogForImpression();
                }
                else {
                    openInputDialogForImpression();
                    toastMessage("YOU LEFT THE INPUT BAR EMPTY");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void openInputDialogForImpression() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a new clinic");
        builder.setMessage("Set impression info");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("COMPLETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().matches("")) {
                    clinicInfo.setImpression(input.getText().toString());
                    new PostClinic().execute();
                }
                else {
                    openInputDialogForImpression();
                    toastMessage("YOU LEFT THE INPUT BAR EMPTY");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openInputDialogForLeadPhysician();
            }
        });

        builder.show();
    }



    private void setIconOnLocation(LatLng latLng, String title, int i) {
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_clinic_64))
                .snippet(getSnippet(items.get(i).getId(), items.get(i).getLeadPhysician(), items.get(i).getImpression(), items.get(i).getContent()));

        // Add an overlay to the map, retaining a handle to the GroundOverlay object.
        Marker locationMarker = mMap.addMarker(options);
        groundOverlays.add(locationMarker);
    }

    private void removeAllGroundOverlay() {
        for (Marker groundOverlay : groundOverlays)
            groundOverlay.remove();
    }

    private String getContentFromPlace(Place place) {
        String address = "Address: " + place.getAddress().toString() + "\n";
        String name = "Name: " + place.getName().toString() + "\n";
        String rating;
        if (place.getRating() > 0)
            rating = "Rating: " + Float.toString(place.getRating()*2) + "\n";
        else
            rating = "";
        String avrPrice;
        if (place.getPriceLevel() > 0)
            avrPrice = "Average price: " + Integer.toString(place.getPriceLevel()) + "\n";
        else
            avrPrice = "";

        return address + name + rating + avrPrice;
    }

    private String getSnippet(String id, String leadPhysician, String impression, String content) {
        return content + "\nLead Physician: " + leadPhysician + "\nImpression: " + impression;
    }

    private class GetClinic extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            jsonString = HttpHandler.getJson("http://10.0.2.2:3004/clinic");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            JSONObject root = null;
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    items.add(new Clinic(object.getString("id"), object.getString("leadPhysician"), object.getString("impression"), object.getString("content")));
                    getPlaceFromIDandDisplayOnMap(object.getString("id"), i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class PostClinic extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            message = HttpHandler.postJson(clinicInfo.getLocationId(), clinicInfo.getLeadPhysician(), clinicInfo.getImpression(), clinicInfo.getContent(), "http://10.0.2.2:3004/clinic");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            removeAllGroundOverlay();
            new GetClinic().execute();
            toastMessage("Map Update: New Clinic Info");
        }
    }

    private class Clinic {
        private String id, content, leadPhysician, impression;

        public Clinic(String id, String leadPhysician, String impression, String content) {
            this.id = id;
            this.leadPhysician = leadPhysician;
            this.impression = impression;
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLeadPhysician() {
            return leadPhysician;
        }

        public void setLeadPhysician(String leadPhysician) {
            this.leadPhysician = leadPhysician;
        }

        public String getImpression() {
            return impression;
        }

        public void setImpression(String impression) {
            this.impression = impression;
        }

    }

    private void getPlaceFromIDandDisplayOnMap(String id, final int i) {
        mGeoDataClient.getPlaceById(id).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    Log.d(TAG, "Place found: " + myPlace.getName());
                    Log.d(TAG, "LATLONG" + myPlace.getLatLng().toString());
                    setIconOnLocation(myPlace.getLatLng(), myPlace.getName().toString(), i);
                    places.release();
                } else {
                    Log.d(TAG, "Place not found.");
                }
            }
        });
    }

    private void chooseFilter() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        mBuilder.setTitle("Clinic Filter");
        final Spinner spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.ClinicTypes));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        mBuilder.setView(view);

        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!spinner.getSelectedItem().toString().equalsIgnoreCase("Choose an item....")) {
                    removeAllGroundOverlay();
                    filterClinic(spinner.getSelectedItem().toString());
                }
            }
        });

        mBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mBuilder.show();
    }

    private void filterClinic(String type) {
        int index = getIndexOfType(type);
        if (type.equalsIgnoreCase("ALL")) {
            for (int i = 0; i < items.size(); i++)
                getPlaceFromIDandDisplayOnMap(items.get(i).getId(), i);
        }
        else if (index >= 0) {
            for (int i = 0; i < items.size(); i++) {
                filterClinicAndDisplayOnMap(items.get(i).getId(), i, index);
            }
        }
    }

    private int getIndexOfType(String type) {
        for (int i = 0; i < types.length; i++) {
            if (type.equalsIgnoreCase(types[i]))
                return i;
        }
        return -1;
    }

    private void filterClinicAndDisplayOnMap(String id, final int i, final int index) {
        mGeoDataClient.getPlaceById(id).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    boolean isValid = false;
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    for (int code : myPlace.getPlaceTypes()) {
                        if (code == ids[index])
                            isValid = true;
                    }
                    if (isValid)
                        setIconOnLocation(myPlace.getLatLng(), myPlace.getName().toString(), i);

                    places.release();
                } else {
                    Log.d(TAG, "Place not found.");
                }
            }
        });
    }

}

