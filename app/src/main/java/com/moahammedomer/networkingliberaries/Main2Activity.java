package com.moahammedomer.networkingliberaries;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    RecyclerView recyclerView;
    MyRecyclerViewAdapter adapter;
    ArrayList<Product> list;
    ProgressBar progressBar;
    SearchView sv;
    private String response_url = "http://09950569.ngrok.io/request_data.php";
    private String searchString;
    public static final String NAME_EXTRA = "name";
    public static final String IMAGE_EXTRA = "image";
    public static final String DESCRIPTION_EXTRA = "description";
    public static final String PRICE_EXTRA = "price";
    public static final String COUNTRY_EXTRA = "country";
    public static final String CATEGORY_EXTRA = "category";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        recyclerView = findViewById(R.id.recycler);
        progressBar = findViewById(R.id.progress);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        // for caching images
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        list = new ArrayList<>();
        MyRecyclerViewAdapter.OnItemClickListener listener = (view, position) ->{
            Intent intent = new Intent(Main2Activity.this, ProductDetailActivity.class);
            Product product = list.get(position);
            intent.putExtra(NAME_EXTRA, product.getName());
            intent.putExtra(PRICE_EXTRA, product.getPrice());
            intent.putExtra(DESCRIPTION_EXTRA, product.getDescription());
            intent.putExtra(COUNTRY_EXTRA, product.getCountry());
            intent.putExtra(CATEGORY_EXTRA, product.getCategory());
            intent.putExtra(IMAGE_EXTRA, product.getImageUrl());
            startActivity(intent);

        };
        adapter = new MyRecyclerViewAdapter(this, list, listener);
        searchString = "";
        loadProductList();
    }


    @Override
    protected void onResume() {
        super.onResume();
//        loadProductList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView=(SearchView)search.getActionView();
        search(searchView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(Main2Activity.this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.search:
                //

        }
    return true;
    }

    private void loadProductList(){

        Log.e("main", "requesting");
        StringRequest request = new StringRequest(Request.Method.POST, response_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("mian", response);
                        try {
                            JSONArray array = new JSONArray(response);
                            list.clear();
                            for(int i = 0; i < array.length(); i++){
                                JSONObject object = array.getJSONObject(i);
                                list.add(new Product(object.getInt("id"),
                                        object.getString("name"),
                                        object.getDouble("price"),
                                        object.getString("description"),
                                        object.getString("category"),
                                        object.getString("country")));
                            }
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Toast.makeText(Main2Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Main2Activity.this, error.getMessage() + "error accured", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
//                Log.e("main", "search string " + searchString);
//                map.put("search", searchString);
                return map;
            }
        };

        MySingleton.getInstance(Main2Activity.this).addToRequestQueue(request);
    }

    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e("main", "onSubmit");
                list = getMatchProducts(query);
                Log.e("main", String.valueOf(list.size()));
                adapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("main", "onChange");
                list = getMatchProducts(newText);
                Log.e("main", String.valueOf(adapter.getItemCount()));
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    public ArrayList<Product> getMatchProducts(String search){
        ArrayList<Product> arrayList = new ArrayList<>();
        int length = list.size();
        for(int i = 0; i < length; i++){
            if(list.get(i).getName().contains(search)){
                arrayList.add(list.get(i));
            }
        }
        Log.e("main", "arraylist size : " + arrayList.size());
        return arrayList;
    }
}
