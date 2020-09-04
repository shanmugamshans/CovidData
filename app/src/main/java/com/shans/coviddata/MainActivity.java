package com.shans.coviddata;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RequestQueue requestQueue;
    private TextView district;
    private TextView district1;
    private TextView district2;
    private TextView district3;
    private Button search;
    private TextView indiacoronacount;

    final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        indiacoronacount = findViewById(R.id.indiacoronacount);
        district = findViewById(R.id.district);
        district1 = findViewById(R.id.district1);
        district2 = findViewById(R.id.district2);
        district3 = findViewById(R.id.district3);
        search = findViewById(R.id.search);
        formatter.applyPattern("#,###,###");

        search.setOnClickListener(this);
        if(HaveNetwork()) {
            Jsondata();
            worlddata();

        }
        else if(!HaveNetwork()) {
            internet_dialog();
        }

    }

    private void worlddata() {
        String url = "https://corona.lmao.ninja/v2/countries/india";
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String country = jsonObject.getString("cases");
                    indiacoronacount.setText(formatter.format(Integer.parseInt(country)));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    public void Jsondata() {
        String url = "https://api.covid19india.org/state_district_wise.json";
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET
                , url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = response.getJSONObject("Tamil Nadu");
                    JSONObject jsonObject1 = jsonObject.getJSONObject("districtData");
                    JSONObject city = jsonObject1.getJSONObject("Chennai");
                    JSONObject city1 = jsonObject1.getJSONObject("Chengalpattu");
                    JSONObject city2 = jsonObject1.getJSONObject("Thiruvallur");
                    JSONObject city3 = jsonObject1.getJSONObject("Madurai");



                    district.setText(formatter.format(Integer.parseInt(city.getString("confirmed"))));
                    district1.setText(formatter.format(Integer.parseInt(city1.getString("confirmed"))));
                    district2.setText(formatter.format(Integer.parseInt(city2.getString("confirmed"))));
                    district3.setText(formatter.format(Integer.parseInt(city3.getString("confirmed"))));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    @Override
    public void onClick(View v) {
        if(HaveNetwork()){
            Intent intent = new Intent(MainActivity.this,Searchpage.class);
            startActivity(intent);
            finish();
        }else if(!HaveNetwork()){
            finish();
            startActivity(getIntent());
        }

    }
    public Boolean HaveNetwork(){
        boolean have_wifi = false;
        boolean have_mobiledata = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

        for(NetworkInfo info:networkInfo){
            if(info.getTypeName().equalsIgnoreCase("WIFI"))
                if(info.isConnected())
                    have_wifi = true;
            if(info.getTypeName().equalsIgnoreCase("MOBILE"))
                if(info.isConnected())
                    have_mobiledata = true;
        }

        return have_mobiledata || have_wifi;
    }
    public void internet_dialog(){
        //initialize dialog
        Dialog dialog = new Dialog(this);
        //set content view
        dialog.setContentView(R.layout.internet_alert_dialog);

        //dialog.setCancelable(false);---- prevent back button

        //set outside touch
        dialog.setCanceledOnTouchOutside(false);
        //set dialog width and height
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set transparent background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //set animation
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

        Button internet_try_again = dialog.findViewById(R.id.internet_try_again);
        internet_try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        MainActivity.this.finish();
    }

}
