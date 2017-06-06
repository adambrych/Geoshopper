package geoshopper.geoshopper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.R.attr.width;
import static geoshopper.geoshopper.R.attr.height;

public class SummaryActivity extends AppCompatActivity {

    ArrayList<String> products;
    ArrayList<String> sizes;
    String longitude;
    String latitude;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        PrepareTable();


        Button back = (Button) findViewById(R.id.Powrot);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, ShoppingListActivity.class);
                intent.putExtra("products", products);
                intent.putExtra("sizes", sizes);
                intent.putExtra("type", "CHEAPEST");
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                startActivity(intent);
                finish();
            }
        });

        Button end = (Button) findViewById(R.id.Szukaj);

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject request = new JSONObject();
                    JSONArray array = new JSONArray();
                    for (int i = 0; i < products.size(); i++) {
                        JSONObject product = new JSONObject();
                        product.put("name", products.get(i));
                        product.put("size", sizes.get(i));
                        array.put(product);
                    }

                    JSONObject coords = new JSONObject();
                    coords.put("latitude", latitude);
                    coords.put("longitude", longitude);
                    request.put("type", type);
                    request.put("products", array);
                    request.put("coords", coords);
                    sendRequestJson(request);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void PrepareTable() {
        products = (ArrayList<String>) getIntent().getSerializableExtra("products");
        sizes = (ArrayList<String>) getIntent().getSerializableExtra("sizes");
        longitude = getIntent().getStringExtra("longitude");
        latitude = getIntent().getStringExtra("latitude");
        type = getIntent().getStringExtra("type");

        final TableLayout ll = (TableLayout) findViewById(R.id.table);
        if (products != null) {
            for (int i = 0; i < products.size(); i++) {
                String product = products.get(i);
                String size = sizes.get(i);

                final TableRow row = new TableRow(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(width / 3, height / 3);
                row.setLayoutParams(lp);
                Button deleteButton = new Button(this);
                deleteButton.setText("Usuń");

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TableRow row = (TableRow) v.getParent();
                        int index = ll.indexOfChild(row);
                        System.out.print("row number " + index);
                        products.remove(index);
                        sizes.remove(index);
                        ll.removeViewAt(index);
                    }
                });

                TextView qty = new TextView(this);
                qty.setText(product + " " + size);
                row.addView(qty);
                row.addView(deleteButton);
                ll.addView(row, i);
            }
        }
    }

    void sendRequestJson(final JSONObject json) {
        final String URL = "http://192.168.137.1:3000/api/shops";
        final RequestQueue queue = Volley.newRequestQueue(this);
        System.out.println("Request \n" + json.toString());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, URL, json, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {


                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("jsonArray", response.toString());
                editor.putString("request", json.toString());
                editor.commit();
                finish();

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


