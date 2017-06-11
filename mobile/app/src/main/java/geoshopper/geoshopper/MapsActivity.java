package geoshopper.geoshopper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.location.LocationServices;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private Marker marker;
    private ArrayList<LatLng> MarkerPoints;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private GoogleApiClient mGoogleApiClient;
    private Location myLocation;
    private LatLng search;
    private int actualDistanse;
    private Polyline polylineSearch = null;
    private ArrayList<Polyline> polylineList = null;
    private String mode = "driving";
    private String type = "CHEAPEST";
    private Boolean list = false;
    private int zoom = 15;
    private boolean back = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        polylineList = new ArrayList<Polyline>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        MarkerPoints = new ArrayList<>();
        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            Toast.makeText(getApplicationContext(), "Brak dostępu do internetu", Toast.LENGTH_SHORT).show();
        }

        if (!isLocationEnabled(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Brak dostępu do lokalizacji", Toast.LENGTH_SHORT).show();
        }



    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng center = new LatLng(0, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return false;
                }
                myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (myLocation != null) {
                    LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    if (marker != null) marker.remove();
                    marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Tu jesteś"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                    //shops(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), true);
                }
                return false;
            }


        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker markerClick) {
                if(myLocation != null && polylineSearch != null){
                    markerClick.hideInfoWindow();
                        polylineSearch.remove();
                        polylineSearch = null;
                        String url = getUrl(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), markerClick.getPosition());

                        Log.d("onMapClick", url.toString());
                         road(url, markerClick, false);
                }
                else if(myLocation != null && polylineList != null && polylineList.size()>0){
                    String url = getUrl(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), markerClick.getPosition());

                    Log.d("onMapClick", url.toString());
                    distance(url, markerClick, false);
                }
                return false;
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }

    private void addDrawerItems() {
        System.out.println("addDrawerItems");
        final ArrayList<String> osArray = new ArrayList<String>();
        osArray.add("Szukaj ręcznie");
        osArray.add("Zmień tryb na pieszo");
        osArray.add("Zmień trasę na najkrótszą");
        osArray.add("Lista zakupów");
        osArray.add("Wyszukaj sklepy w okolicy");
        final Context context = this;
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id == 0) {
                    findViewById(R.id.search).setVisibility(View.VISIBLE);
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else if (id == 1) {
                    if(mode.equals("driving")) {
                        osArray.set(1, "Zmień tryb na jazda samochodem");
                        mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, osArray);
                        mDrawerList.setAdapter(mAdapter);
                        mode = "walking";
                        if(polylineSearch != null)polylineSearch.remove();
                        polylineSearch = null;
                        roadForList();
                    }
                    else{
                        mode = "driving";
                        osArray.set(1, "Zmień tryb na pieszo");
                        mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, osArray);
                        mDrawerList.setAdapter(mAdapter);
                        if(polylineSearch != null)polylineSearch.remove();
                        polylineSearch = null;
                        roadForList();
                    }

                    mDrawerLayout.closeDrawer(mDrawerList);
                } else if (id == 2) {
                    if(type.equals("CHEAPEST")) {
                        osArray.set(2, "Zmień trasę na najtańszą");
                        mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, osArray);
                        mDrawerList.setAdapter(mAdapter);
                        type = "SHORTEST";
                        roadForList();
                    }
                    else{
                        type = "CHEAPEST";
                        osArray.set(2, "Zmień trasę na najkrótszą");
                        mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, osArray);
                        mDrawerList.setAdapter(mAdapter);
                        roadForList();
                    }

                    mDrawerLayout.closeDrawer(mDrawerList);
                }else if (id == 3) {
                    if(myLocation != null){
                        back = true;
                        mDrawerLayout.closeDrawer(mDrawerList);
                        Intent intent = new Intent(MapsActivity.this, ShoppingListActivity.class);
                        intent.putExtra("latitude", String.valueOf(myLocation.getLatitude()));
                        intent.putExtra("longitude", String.valueOf(myLocation.getLongitude()));
                        intent.putExtra("type", type);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Brak dostępu do internetu", Toast.LENGTH_SHORT).show();
                    }

                }
                else if(id == 4){
                    if(myLocation!=null) {
                        list = false;
                        shops(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), true);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Brak dostępu do internetu", Toast.LENGTH_SHORT).show();
                    }
                    mDrawerLayout.closeDrawer(mDrawerList);
                }

                //Toast.makeText(MapsActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDrawer() {
        System.out.println("setupDrawer");
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                System.out.println("");
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                System.out.println("onDrawerClosed");
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        System.out.println("onPostCreate");
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        System.out.println("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("onOptionsItemSelected");

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onSearch(View view) {
        EditText location_tf = (EditText) findViewById(R.id.TFaddress);
        String location = location_tf.getText().toString();
        if (location != null && location != "") {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addressList = null;
            try {
                list = false;
                mMap.clear();
                if(polylineSearch != null) polylineSearch.remove();
                polylineSearch = null;
                addressList = geocoder.getFromLocationName(location, 1);
                if (addressList.size() > 0) {
                    Address address = addressList.get(0);
                    search = new LatLng(address.getLatitude(), address.getLongitude());
                    myLocation.setLatitude(address.getLatitude());
                    myLocation.setLongitude(address.getLongitude());
                    if (marker != null) marker.remove();
                    marker = mMap.addMarker(new MarkerOptions().position(search).title("Tu jestes"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(search));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                    roadForList();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        findViewById(R.id.search).setVisibility(View.INVISIBLE);
    }


    public void shops(final LatLng origin, final boolean drawRoad) {
        final RequestQueue queue = Volley.newRequestQueue(this);
        if(polylineSearch!=null) polylineSearch.remove();
        polylineSearch = null;
        String url = "http://192.168.137.1:3000/api/shops?latitude=" + origin.latitude + "&longitude=" + origin.longitude;
        //String url = "http://192.168.43.86:3000/api/shops";

// Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            MarkerPoints.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                LatLng point = new LatLng(json.getDouble("latitude"), json.getDouble("longitude"));
                                MarkerPoints.add(point);

                                // Creating MarkerOptions
                                MarkerOptions options = new MarkerOptions();
                                options.position(point);
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                options.title(json.getString("name"));
                                System.out.println("shop name " + json.getString("name"));
                                options.snippet(json.getString("city") + " " + json.getString("street"));
                                switch(json.getString("name")){
                                    case "Piotr i Paweł":
                                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.piotripawel));
                                        break;
                                    case "Biedronka":
                                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.biedronka));
                                        break;
                                    case "Społem":
                                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.spolem));
                                        break;
                                    case "Carrefour":
                                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.carrefour));
                                        break;

                                }
                                mMap.addMarker(options);
                            }
                            if(drawRoad == true && MarkerPoints.size() > 0) {
                                String url = getUrl(origin, MarkerPoints.get(0));
                                Log.d("onMapClick", url.toString());
                               road(url, false);
                                //move map camera
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error request");
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        String modes = "mode=" + mode;

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + modes;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }


    @Override
    public void onLocationChanged(Location location) {
        System.out.println("onlocation changed");
        myLocation.setLatitude(location.getLatitude());
        myLocation.setLongitude(location.getLongitude());
        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Tu jesteś"));
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
    public void onConnected(Bundle connectionHint) {
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
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (myLocation != null && back == true) {
            back = false;
            mMap.clear();
            if(polylineSearch != null) polylineSearch.remove();
            polylineSearch = null;
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            if (marker != null) marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Tu jesteś"));
            //shops(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), true);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            System.out.println("onConnected sadas");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String savedPref = sharedPreferences.getString("jsonArray", "");
            System.out.println("response " + savedPref);
            if(savedPref != null && !savedPref.equals("")){
                try {
                    System.out.println("jsonArray");
                    list = true;
                    JSONArray jsonArray = new JSONArray(savedPref);
                    LatLng previeus;
                    LatLng point = null;
                    MarkerPoints.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        JSONObject coords = json.getJSONObject("coords");
                        previeus = point;
                        point = new LatLng(coords.getDouble("latitude"), coords.getDouble("longitude"));
                        MarkerPoints.add(point);

                        // Creating MarkerOptions
                        MarkerOptions options = new MarkerOptions();
                        options.position(point);
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        options.title(json.getString("name"));
                        options.snippet(json.getString("city") + " " + json.getString("street"));
                        options.snippet(options.getSnippet() + "\n" +"Produkty:");

                        JSONArray jsonProducts = json.getJSONArray("products");
                        Double suma = 0.0;
                        for(int j=0; j<jsonProducts.length(); j++){
                            JSONObject product = jsonProducts.getJSONObject(j);
                            suma += product.getDouble("price");
                            options.snippet(options.getSnippet() + "\n" +  product.getString("name") + " " + product.getString("size") + " " + product.getString("price") + "zł");
                        }
                        suma = Math.floor(suma * 100) / 100;
                        System.out.println("suma " + suma);
                        options.snippet(options.getSnippet() + "\n" + "Suma: " + suma + "zł");




                        switch(json.getString("name")){
                            case "Piotr i Paweł":
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.piotripawel));
                                break;
                            case "Biedronka":
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.biedronka));
                                break;
                            case "Społem":
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.spolem));
                                break;
                            case "Carrefour":
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.carrefour));
                                break;

                        }
                        mMap.addMarker(options);
                        String url;
                        if(i==0) url = getUrl(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), point);
                        else url = getUrl(previeus, point);
                        Log.d("onMapClick", url.toString());
                        road(url, true);
                        //move map camera
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void road(String url, final Marker markerClick, final Boolean list){
        final RequestQueue queue = Volley.newRequestQueue(this);
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject jObject;
                        List<List<HashMap<String, String>>> routes = null;

                        try {
                            jObject = new JSONObject(response);
                            Log.d("ParserTask",response.toString());
                            DataParser parser = new DataParser();
                            Log.d("ParserTask", parser.toString());

                            // Starts parsing data
                            routes = parser.parse(jObject);
                            System.out.println("przed actualdistance " + actualDistanse);
                            actualDistanse = parser.distance(jObject);
                            System.out.println("po actualdistance " + actualDistanse);

                            int index = markerClick.getSnippet().indexOf("Odległość");
                            if(index != -1){
                                markerClick.setSnippet(markerClick.getSnippet().substring(0, index));
                                markerClick.setSnippet(markerClick.getSnippet() + "Odległość " + actualDistanse + " metrów");
                            }
                            else markerClick.setSnippet(markerClick.getSnippet() + "\n" + "Odległość " + actualDistanse + " metrów");

                            Log.d("ParserTask","Executing routes");
                            Log.d("ParserTask",routes.toString());

                            ArrayList<LatLng> points;
                            PolylineOptions lineOptions = null;

                            // Traversing through all the routes
                            for (int i = 0; i < routes.size(); i++) {
                                points = new ArrayList<>();
                                lineOptions = new PolylineOptions();

                                // Fetching i-th route
                                List<HashMap<String, String>> path = routes.get(i);

                                // Fetching all the points in i-th route
                                for (int j = 0; j < path.size(); j++) {
                                    HashMap<String, String> point = path.get(j);

                                    double lat = Double.parseDouble(point.get("lat"));
                                    double lng = Double.parseDouble(point.get("lng"));
                                    LatLng position = new LatLng(lat, lng);

                                    points.add(position);
                                }

                                // Adding all the points in the route to LineOptions
                                lineOptions.addAll(points);
                                lineOptions.width(10);
                                if(list == false )lineOptions.color(Color.RED);
                                else lineOptions.color(Color.GREEN);

                                Log.d("onPostExecute","onPostExecute lineoptions decoded");

                            }

                            // Drawing polylineSearch in the Google Map for the i-th route
                            if(lineOptions != null) {
                                if(list == false) polylineSearch = mMap.addPolyline(lineOptions);
                                else{
                                    Polyline line = mMap.addPolyline(lineOptions);
                                    polylineList.add(line);
                                }

                            }
                            else {
                                Log.d("onPostExecute","without Polylines drawn");
                            }
                            markerClick.showInfoWindow();

                        } catch (Exception e) {
                            Log.d("ParserTask",e.toString());
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error request");
            }
        });
        queue.add(stringRequest);
    }

    public void road(String url, final Boolean list){
        final RequestQueue queue = Volley.newRequestQueue(this);
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject jObject;
                        List<List<HashMap<String, String>>> routes = null;

                        try {
                            jObject = new JSONObject(response);
                            Log.d("ParserTask",response.toString());
                            DataParser parser = new DataParser();
                            Log.d("ParserTask", parser.toString());

                            // Starts parsing data
                            routes = parser.parse(jObject);
                            System.out.println("przed actualdistance " + actualDistanse);
                            actualDistanse = parser.distance(jObject);
                            System.out.println("po actualdistance " + actualDistanse);
                            Log.d("ParserTask","Executing routes");
                            Log.d("ParserTask",routes.toString());

                            ArrayList<LatLng> points;
                            PolylineOptions lineOptions = null;

                            // Traversing through all the routes
                            for (int i = 0; i < routes.size(); i++) {
                                points = new ArrayList<>();
                                lineOptions = new PolylineOptions();

                                // Fetching i-th route
                                List<HashMap<String, String>> path = routes.get(i);

                                // Fetching all the points in i-th route
                                for (int j = 0; j < path.size(); j++) {
                                    HashMap<String, String> point = path.get(j);

                                    double lat = Double.parseDouble(point.get("lat"));
                                    double lng = Double.parseDouble(point.get("lng"));
                                    LatLng position = new LatLng(lat, lng);

                                    points.add(position);
                                }

                                // Adding all the points in the route to LineOptions
                                lineOptions.addAll(points);
                                lineOptions.width(10);
                                if(list == false )lineOptions.color(Color.RED);
                                else lineOptions.color(Color.GREEN);
                                Log.d("onPostExecute","onPostExecute lineoptions decoded");

                            }

                            // Drawing polylineSearch in the Google Map for the i-th route
                            if(lineOptions != null) {
                                if(list == false) polylineSearch = mMap.addPolyline(lineOptions);
                                else{
                                    Polyline line = mMap.addPolyline(lineOptions);
                                    polylineList.add(line);
                                }

                            }
                            else {
                                Log.d("onPostExecute","without Polylines drawn");
                            }


                        } catch (Exception e) {
                            Log.d("ParserTask",e.toString());
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error request");
            }
        });
        queue.add(stringRequest);
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

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    void roadForList(){

        list=true;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String savedPref = sharedPreferences.getString("request", "");
        if(savedPref !=null && savedPref != "") {
            if(polylineList!=null) deletePolylines();

            final String URL = "http://192.168.137.1:3000/api/shops";
            final RequestQueue queue = Volley.newRequestQueue(this);

            JSONObject json = null;
            try {
                json = new JSONObject(savedPref);
                json.put("type",type);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println("Roadforlist Request \n" + json.toString());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, URL, json, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                    LatLng previeus;
                    LatLng point = null;
                    MarkerPoints.clear();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject json = null;

                        json = response.getJSONObject(i);

                        JSONObject coords = json.getJSONObject("coords");
                        previeus = point;
                        point = new LatLng(coords.getDouble("latitude"), coords.getDouble("longitude"));
                        MarkerPoints.add(point);

                        // Creating MarkerOptions
                        MarkerOptions options = new MarkerOptions();
                        options.position(point);
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        options.title(json.getString("name"));

                        options.snippet(json.getString("city") + " " + json.getString("street"));
                        options.snippet(options.getSnippet() + "\n" + "Produkty:");

                        JSONArray jsonProducts = json.getJSONArray("products");
                        Double suma = 0.0;
                        for (int j = 0; j < jsonProducts.length(); j++) {
                            JSONObject product = jsonProducts.getJSONObject(j);
                            suma += product.getDouble("price");
                            options.snippet(options.getSnippet() + "\n" + product.getString("name") + " " + product.getString("size") + " " + product.getString("price") + "zł");
                        }
                        suma = Math.floor(suma * 100) / 100;
                        System.out.println("suma2 " + suma);
                        options.snippet(options.getSnippet() + "\n" + "Suma: " + suma + "zł");


                        switch (json.getString("name")) {
                            case "Piotr i Paweł":
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.piotripawel));
                                break;
                            case "Biedronka":
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.biedronka));
                                break;
                            case "Społem":
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.spolem));
                                break;
                            case "Carrefour":
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.carrefour));
                                break;

                        }
                        mMap.addMarker(options);
                        String url;
                        if (i == 0)
                            url = getUrl(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), point);
                        else url = getUrl(previeus, point);
                        Log.d("onMapClick", url.toString());
                        road(url, true);

                    }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.print("error");
                }
            }) {
            };
            queue.add(jsonArrayRequest);

        }
    }


    public void distance(String url, final Marker markerClick, final Boolean list){
        final RequestQueue queue = Volley.newRequestQueue(this);
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject jObject;
                        List<List<HashMap<String, String>>> routes = null;

                        try {
                            jObject = new JSONObject(response);
                            Log.d("ParserTask",response.toString());
                            DataParser parser = new DataParser();
                            Log.d("ParserTask", parser.toString());

                            // Starts parsing data
                            routes = parser.parse(jObject);
                            System.out.println("przed actualdistance " + actualDistanse);
                            actualDistanse = parser.distance(jObject);
                            System.out.println("po actualdistance " + actualDistanse);

                            int index = markerClick.getSnippet().indexOf("Odległość");
                            if(index != -1){
                                markerClick.setSnippet(markerClick.getSnippet().substring(0, index));
                                markerClick.setSnippet(markerClick.getSnippet() + "Odległość " + actualDistanse + " metrów");
                            }
                            else markerClick.setSnippet(markerClick.getSnippet() + "\n" + "Odległość " + actualDistanse + " metrów");
                            markerClick.showInfoWindow();
                            Log.d("ParserTask","Executing routes");
                            Log.d("ParserTask",routes.toString());



                        } catch (Exception e) {
                            Log.d("ParserTask",e.toString());
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error request");
            }
        });
        queue.add(stringRequest);
    }

    void deletePolylines(){
        System.out.println("delete polyline " + polylineList.size());
        for(int i=0; i<polylineList.size(); i++){
            polylineList.get(i).remove();
        }
        System.out.println(polylineList.size());
        while (polylineList.size()>0) polylineList.remove(0);
    }


}
