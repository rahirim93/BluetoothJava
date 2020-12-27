package com.example.bluetoothjava;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.bluetoothjava.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    ArrayList<BluetoothDevice> blueArray = new ArrayList<>();
    ArrayList<BluetoothDevice> blueArrayClean = new ArrayList<>();
    ArrayList<String> blueArrayCleanNames = new ArrayList<>();

    ArrayAdapter<String> arrayAdapter;// = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, blueArrayClean);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                setListAdapter();
                blueArray.add(device); //добавление устройств в нефильтрованный список

                //фильтрация и добавление в чистый список
                if (device != null) {
                    if (!blueArrayClean.contains(device)) {
                        blueArrayClean.add(device);
                        if (device.getName() != null){
                            blueArrayCleanNames.add(device.getName());
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        }

    };

    public void setListAdapter (){
        ListView listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, blueArrayCleanNames);
        listView.setAdapter(arrayAdapter);
    }

    public void discover(View view){
        blueArrayCleanNames.clear();
        if (!bluetoothAdapter.isEnabled()) { //Если блютуз выключен, сделать запрос на включение
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (bluetoothAdapter.isEnabled()){ //Если блютуз включен, начать поиск устройств
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}