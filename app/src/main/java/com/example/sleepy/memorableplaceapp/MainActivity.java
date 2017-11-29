package com.example.sleepy.memorableplaceapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> placesList;
    static ArrayList<LatLng> coordList;
    static ArrayAdapter arrayAdapter;

    Intent intent;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("com.example.sleepy.memorableplaceapp", Context.MODE_PRIVATE);
        intent = new Intent(getApplicationContext(), MapsActivity.class);
        ListView placesListView = (ListView) findViewById(R.id.placesListView);

        placesList = new ArrayList<String>();
        coordList = new ArrayList<LatLng>();
        ArrayList<String> longList = new ArrayList<String>();
        ArrayList<String> latList = new ArrayList<String>();

        placesList.clear();
        longList.clear();
        latList.clear();
        coordList.clear();

        try {
            placesList = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("placesList", ObjectSerializer.serialize(new ArrayList<String>())));
            longList = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longList", ObjectSerializer.serialize(new ArrayList<String>())));
            latList = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latList", ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(placesList.size()>0 && longList.size()>0 && latList.size()>0){
            if(placesList.size() == longList.size() && longList.size() == latList.size()){
                for(int i=0; i<longList.size(); i++){
                    coordList.add(new LatLng(Double.parseDouble(latList.get(i)),Double.parseDouble(longList.get(i))));
                }
            }
        }else{
            placesList.add("Add a New Place...");
            coordList.add(new LatLng(0,0));
        }


        arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, placesList);
        placesListView.setAdapter(arrayAdapter);

        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                intent.putExtra("position", i);
                startActivity(intent);
            }
        });

    }

}
