package com.example.bluetoothjava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class BlueInfo extends AppCompatActivity {

    public static final MyBlue EXTRA_DRINKID = new MyBlue("", "");
    public static final String EXTRA_BLUENAME = "name";
    public static final String EXTRA_BLUEADDRESS = "address";
    public static final String EXTRA_BLUETYPE = "type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_info);

        String blueName = (String) getIntent().getExtras().get(EXTRA_BLUENAME);
        String blueAddress = (String) getIntent().getExtras().get(EXTRA_BLUEADDRESS);
        int blueType = (int) getIntent().getExtras().get(EXTRA_BLUETYPE);
        String blueTypeStr = String.valueOf(blueType);
        //MyBlue myBlue = getIntent().getExtras().get
        //int drinkId = (Integer)getIntent().getExtras().get(EXTRA_DRINKID);
        //MyBlue myBlue = EXTRA_DRINKID;


        TextView name = findViewById(R.id.name);
        name.setText(blueName);

        TextView address = findViewById(R.id.address);
        address.setText(blueAddress);

        TextView type = findViewById(R.id.type);
        type.setText(blueTypeStr);


    }
}