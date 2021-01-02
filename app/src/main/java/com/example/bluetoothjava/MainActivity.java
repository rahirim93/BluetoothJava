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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.bluetoothjava.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    ArrayList<BluetoothDevice> blueArray = new ArrayList<>();
    ArrayList<String> blueArrayNames = new ArrayList<>();

    ArrayAdapter<String> arrayAdapter;// = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, blueArrayClean);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        ListView listView = findViewById(R.id.listView);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Создание слушателя
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> listView,
                                    View itemView,
                                    int position,
                                    long id) {
                Intent intent = new Intent(MainActivity.this, BlueInfo.class);
                intent.putExtra(BlueInfo.EXTRA_BLUENAME, blueArray.get((int) id).getName());
                intent.putExtra(BlueInfo.EXTRA_BLUEADDRESS, blueArray.get((int) id).getAddress());
                intent.putExtra(BlueInfo.EXTRA_BLUETYPE, blueArray.get((int) id).getType());
                startActivity(intent);
            }
        };
        //Назначение слушателя для спискового представления
        listView.setOnItemClickListener(itemClickListener);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Получаем силу сигнала получаемого от найденного устройства
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                Toast.makeText(MainActivity.this,"  RSSI: " + rssi + "dBm", Toast.LENGTH_SHORT).show();

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                setListAdapter();

                if (device != null){
                    if (!blueArray.contains(device)){
                        blueArray.add(device);
                        if (device.getName() != null){
                            blueArrayNames.add(device.getName());
                        } else blueArrayNames.add("null");
                    }
                }
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void setListAdapter (){
        ListView listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, blueArrayNames);
        listView.setAdapter(arrayAdapter);
    }

    public void discover(View view){
        blueArray.clear();
        blueArrayNames.clear();
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