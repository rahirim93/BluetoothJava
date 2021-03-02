package com.example.bluetoothjava;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BlueInfo extends AppCompatActivity {
    //Сокет, с помощью которого мы будем отправлять данные на Arduino
    BluetoothSocket clientSocket;

    ConnectThread myConnect;


    public static final MyBlue EXTRA_DRINKID = new MyBlue("", "");
    public static final String EXTRA_BLUENAME = "name";
    public static final String EXTRA_BLUEADDRESS = "address";
    public static final String EXTRA_BLUETYPE = "type";

    String blueAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_info);

        String blueName = (String) getIntent().getExtras().get(EXTRA_BLUENAME);
        blueAddress = (String) getIntent().getExtras().get(EXTRA_BLUEADDRESS);
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

    public void btnConnect(View view) {
        //Мы хотим использовать тот bluetooth-адаптер, который задается по умолчанию
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

        //Пытаемся проделать эти действия
        try{
            //Устройство с данным адресом - наш Bluetooth Bee
            //Адрес опредеяется следующим образом: установите соединение
            //между ПК и модулем (пин: 1234), а затем посмотрите в настройках
            //соединения адрес модуля. Скорее всего он будет аналогичным.
            BluetoothDevice device = bluetooth.getRemoteDevice(blueAddress);

            //Инициируем соединение с устройством
            Method m = device.getClass().getMethod(
                    "createRfcommSocket", new Class[] {int.class});

            clientSocket = (BluetoothSocket) m.invoke(device, 1);
            clientSocket.connect();

            //В случае появления любых ошибок, выводим в лог сообщение
        } catch (IOException e) {
            Log.d("BLUETOOTH_", e.getMessage());
        } catch (SecurityException e) {
            Log.d("BLUETOOTH_", e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d("BLUETOOTH_", e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d("BLUETOOTH_", e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d("BLUETOOTH_", e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d("BLUETOOTH_", e.getMessage());
        }

        //Выводим сообщение об успешном подключении
        Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_LONG).show();
    }

    public void connectMine(View view) {
        myConnect = new ConnectThread(blueAddress);
        myConnect.run();
    }

    @Override
    protected void onDestroy() {
        myConnect.cancel();
        super.onDestroy();
    }
}