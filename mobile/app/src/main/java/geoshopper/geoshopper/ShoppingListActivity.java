package geoshopper.geoshopper;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ShoppingListActivity extends AppCompatActivity {

    // List view
    private ListView lv;

    // Listview Adapter
    ArrayAdapter<String> adapter;
    ArrayList<String> products;
    ArrayList<String> sizes;
    ArrayList<String> sizesFromRequest;
    ArrayList<String> productsFromRequest;
    // Search EditText
    EditText inputSearch;
    Context context;
    Spinner spinner;
    SpinnerAdapter spinnerAdapter;
    ArrayList<String> selectedProducts;
    ArrayList<String> selectedSizes;
    String longitude;
    String latitude;
    String type;
    String selectedProduct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Listview Data

        lv = (ListView) findViewById(R.id.list_view);
        inputSearch = (EditText) findViewById(R.id.inputSearch);

        // Adding items to listview
        products = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.product_name,products);
        lv.setAdapter(adapter);
        context = this;

        selectedProducts = new ArrayList<String>();
        selectedSizes =new ArrayList<String>();

        spinner = (Spinner) findViewById(R.id.spinner);
        sizes = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.product_name,sizes);
        spinner.setAdapter(spinnerAdapter);

        longitude = getIntent().getStringExtra("longitude");
        latitude = getIntent().getStringExtra("latitude");
        type = getIntent().getStringExtra("type");
        if(getIntent().getStringExtra("products")!=null) selectedProducts = (ArrayList<String>) getIntent().getSerializableExtra("products");
        if(getIntent().getStringExtra("sizes")!=null) selectedSizes = (ArrayList<String>) getIntent().getSerializableExtra("sizes");
        System.out.println("intend " + longitude + " " + latitude + " " + type);

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                lv.setVisibility(View.VISIBLE);
                if(cs.length()==1){
                    productsRequest(cs.toString());
                }
                else if(cs.length()==0){
                    adapter.clear();
                }
                    ShoppingListActivity.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String product = lv.getItemAtPosition(position).toString();
                sizesRequest(product);
                inputSearch.setText(product);
                selectedProduct = product;
                lv.setVisibility(View.INVISIBLE);
            }
        });




        Button add = (Button) findViewById(R.id.dodaj);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(selectedProduct == null){
                    Toast.makeText(getApplicationContext(), "Nie zaznaczyłeś produktu", Toast.LENGTH_SHORT).show();
                }
                else if(spinner.getSelectedItem() == null){
                    Toast.makeText(getApplicationContext(), "Nie zaznaczyłeś rozmiaru", Toast.LENGTH_SHORT).show();
                }
                else {
                    String product = selectedProduct;
                    String size = spinner.getSelectedItem().toString();

                    adapter.clear();
                    sizes.clear();
                    products.clear();
                    inputSearch.setText("");
                    spinnerAdapter = new ArrayAdapter<String>(context, R.layout.list_item, R.id.product_name, sizes);
                    spinner.setAdapter(spinnerAdapter);
                    adapter = new ArrayAdapter<String>(context, R.layout.list_item, R.id.product_name, products);
                    lv.setAdapter(adapter);
                    selectedProducts.add(product);
                    selectedSizes.add(size);
                }
            }
        });

        Button end = (Button) findViewById(R.id.Podsumowanie);


        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppingListActivity.this, SummaryActivity.class);
                intent.putExtra("products", selectedProducts);
                intent.putExtra("sizes", selectedSizes);
                intent.putExtra("type", "CHEAPEST");
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                startActivity(intent);
            }
        });

    }

    public void productsRequest(String text) {
        final RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://192.168.137.1:3000/api/products/?product=";
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url+=encodedUrl;
        productsFromRequest = new ArrayList<String>();

// Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONArray jsonArray = new JSONArray(response);


                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                System.out.println("Shoppinglistactibity if 1 getProducts json " + json.getString("name"));
                                productsFromRequest.add(json.getString("name"));
                            }
                            products.clear();
                            products.addAll(productsFromRequest);
                            adapter = new ArrayAdapter<String>(context, R.layout.list_item, R.id.product_name,products);
                            lv.setAdapter(adapter);
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

    public void sizesRequest(String text) {
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.137.1:3000/api/products/?product=";
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url+=encodedUrl;
        System.out.println("Shoppinglistactibity if 1 getSizes url " + url);
        sizesFromRequest = new ArrayList<String>();

// Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            System.out.println("Shoppinglistactibity if 1 getSizes response " + jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                System.out.println("Shoppinglistactibity if 1 getSizes json " + json.getString("size"));
                                sizesFromRequest.add(json.getString("size"));
                            }
                            sizes.clear();
                            sizes.addAll(sizesFromRequest);
                            spinnerAdapter = new ArrayAdapter<String>(context, R.layout.list_item, R.id.product_name,sizes);
                            spinner.setAdapter(spinnerAdapter);
                            System.out.println("Shoppinglistactibity if 1 sizes getcount " + spinner.getCount());
                            if(spinner.getCount()>0) spinner.setSelection(0,true);
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


}
