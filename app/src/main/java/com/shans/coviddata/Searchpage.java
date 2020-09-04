package com.shans.coviddata;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Searchpage extends AppCompatActivity {

    private RequestQueue queue;
    private TextView district_name;
    private TextView confirmed;
    private TextView active;
    private TextView recovered;
    private TextView deceased;
    private TextView tdyconfirmed;
    private TextView tdyrecovered;
    private TextView tdydeceased;
    private PieChart coronapiechart;

    final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private static final String[] nameofdistrict = new String[]{"Ariyalur",
            "Chengalpattu","Chennai","Coimbatore","Cuddalore","Dharmapuri","Dindigul",
            "Erode", "Kallakurichi","Kancheepuram","Kanyakumari","Karur","Krishnagiri",
            "Madurai","Nagapattinam","Namakkal","Nilgiris","Perambalur","Pudukkottai",
            "Ramanathapuram","Ranipet","Salem","Sivaganga","Tenkasi","Thanjavur","Theni",
            "Thiruvallur","Thiruvarur","Thoothukkudi","Tiruchirappalli","Tirunelveli","Tirupathur",
            "Tiruppur","Tiruvannamalai","Vellore","Viluppuram","Virudhunagar"
    };

    ArrayList<PieEntry> pieEntries = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchpage);

        formatter.applyPattern("#,###,###");
        coronapiechart = findViewById(R.id.coronapiechart);
        queue = Volley.newRequestQueue(this);
        district_name = findViewById(R.id.district_name);
        confirmed = findViewById(R.id.confirmed);
        active = findViewById(R.id.active);
        recovered = findViewById(R.id.recovered);
        deceased = findViewById(R.id.deceased);
        tdyconfirmed = findViewById(R.id.tdyconfirmed);
        tdyrecovered = findViewById(R.id.tdyrecovered);
        tdydeceased = findViewById(R.id.tdydeceased);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchbar_tool,menu);
        MenuItem menuItem = menu.findItem(R.id.searchbtn);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Here.!");

        final androidx.appcompat.widget.SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setBackgroundColor(Color.BLUE);
        searchAutoComplete.setTextColor(Color.GREEN);
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.holo_blue_light);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nameofdistrict);
        searchAutoComplete.setAdapter(adapter);

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String queryString=(String)parent.getItemAtPosition(position);
                searchAutoComplete.setText("" + queryString);
                Searchdata(queryString);
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(HaveNetwork())
                    Searchdata(query);
                else if(!HaveNetwork()){
                    internet_dialog();
                }
                int fil = query.compareToIgnoreCase(String.valueOf(nameofdistrict));
                if(fil==1){
                    Toast.makeText(getApplicationContext(), "Selected: "+query,Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "No Match found",Toast.LENGTH_LONG).show();
                    district_name.setText("");confirmed.setText("");active.setText("");recovered.setText("");
                    deceased.setText("");tdyconfirmed.setText("");tdydeceased.setText("");tdyrecovered.setText("");
                    pieEntries.clear();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void Searchdata(String query) {
        final String city = query.substring(0,1).toUpperCase()+query.substring(1).toLowerCase().trim();
        String url = "https://api.covid19india.org/state_district_wise.json";
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObject = response.getJSONObject("Tamil Nadu");
                            JSONObject district = jsonObject.getJSONObject("districtData");
                            JSONObject name = district.getJSONObject(city);
                            JSONObject todaydata = name.getJSONObject("delta");

                            district_name.setText(city.toUpperCase());
                            confirmed.setText(name.getString("confirmed"));
                            active.setText(name.getString("active"));
                            recovered.setText(name.getString("recovered"));
                            deceased.setText(name.getString("deceased"));
                            tdyconfirmed.setText(todaydata.getString("confirmed"));
                            tdyrecovered.setText(todaydata.getString("recovered"));
                            tdydeceased.setText(todaydata.getString("deceased"));

                            pieEntries = new ArrayList<>();
                            pieEntries.add(new PieEntry(Integer.parseInt(confirmed.getText().toString()),"Confirmed"));
                            pieEntries.add(new PieEntry(Integer.parseInt(active.getText().toString()),"Acitve"));
                            pieEntries.add(new PieEntry(Integer.parseInt(recovered.getText().toString()),"Recovered"));
                            pieEntries.add(new PieEntry(Integer.parseInt(deceased.getText().toString()),"Deceased"));

                            PieDataSet pieDataSet = new PieDataSet(pieEntries,city.toUpperCase());
                            pieDataSet.setColors(Searchpage.COLORFUL_COLORS);
                            pieDataSet.setValueTextColor(Color.BLACK);
                            pieDataSet.setValueTextSize(12f);

                            PieData pieData = new PieData(pieDataSet);
                            coronapiechart.setData(pieData);
                            coronapiechart.getDescription().setEnabled(false);
                            coronapiechart.setDrawEntryLabels(false);
                            coronapiechart.setCenterText("Corona");
                            coronapiechart.animate();

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
        queue.add(request);

    }
    //pie chart color
    public static final int[] COLORFUL_COLORS = {
            Color.parseColor("#ff3838"), Color.parseColor("#17c0eb"), Color.parseColor("#3ae374"),
            Color.parseColor("#3d3d3d")
    };
    @Override
    public void onBackPressed() {
        Intent intent =new Intent(Searchpage.this,MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
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
}
