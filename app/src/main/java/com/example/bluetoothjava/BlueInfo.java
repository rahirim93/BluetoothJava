package com.example.bluetoothjava;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BlueInfo extends AppCompatActivity {
    String tagLifeCycle = "lifeCycle";

    //Сокет, с помощью которого мы будем отправлять данные на Arduino
    BluetoothSocket clientSocket;

    ConnectThread myConnect;


    String message;

    EditText editText;

    public static final String EXTRA_BLUENAME = "name";
    public static final String EXTRA_BLUEADDRESS = "address";
    public static final String EXTRA_BLUETYPE = "type";

    String blueAddress;

    ToggleButton toggleButton;

    Handler h;
    private StringBuilder sb = new StringBuilder();
    final int RECIEVE_MESSAGE = 1;
    private static final String TAG = "bluetooth2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_info);

        Log.i(tagLifeCycle, "onCreate"); // Для теста жизненного цикла

        String blueName = (String) getIntent().getExtras().get(EXTRA_BLUENAME);
        blueAddress = (String) getIntent().getExtras().get(EXTRA_BLUEADDRESS);
        int blueType = (int) getIntent().getExtras().get(EXTRA_BLUETYPE);
        String blueTypeStr = String.valueOf(blueType);

        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    try {
                        myConnect.sendMsg("0");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // The toggle is disabled
                    try {
                        myConnect.sendMsg("1");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        TextView name = findViewById(R.id.name);
        name.setText(blueName);

        TextView address = findViewById(R.id.address);
        address.setText(blueAddress);

        TextView type = findViewById(R.id.type);
        type.setText(blueTypeStr);

        editText = findViewById(R.id.editTextSendData);

        TextView tempTextView = findViewById(R.id.tempTextView);    //TextView для вывода температуры


        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // если приняли сообщение в Handler
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);                                                // формируем строку
                        int endOfLineIndex = sb.indexOf("\r\n");                            // определяем символы конца строки
                        if (endOfLineIndex > 0) {                                            // если встречаем конец строки,
                            String sbprint = sb.substring(0, endOfLineIndex);               // то извлекаем строку
                            sb.delete(0, sb.length());                                      // и очищаем sb
                            tempTextView.setText("Ответ от Arduino: " + sbprint);             // обновляем TextView
                            //btnOff.setEnabled(true);
                            //btnOn.setEnabled(true);
                        }
                        //Log.d(TAG, "...Строка:"+ sb.toString() +  "Байт:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        Button buttonConnect = findViewById(R.id.buttonConnect);
        buttonConnect.setClickable(false);                          //Заблокировать кнопку

        //Подключение при запуске активити
        if (myConnect == null){
            myConnect = new ConnectThread(blueAddress);
            myConnect.start();
        }
//        while (!myConnect.getSocket().isConnected()){
//            Toast.makeText(this, "Подключение", Toast.LENGTH_SHORT).show();
//        }
        ConnectedThread connectedThread = new ConnectedThread(myConnect.getSocket());
        connectedThread.start();
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
        myConnect.start();

    }

    @Override
    protected void onDestroy() {
        Log.i(tagLifeCycle, "onDestroy"); // Для теста жизненного цикла

        if (myConnect != null){
            myConnect.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        Log.i(tagLifeCycle, "onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.i(tagLifeCycle, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        Log.i(tagLifeCycle, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.i(tagLifeCycle, "onResume");
        super.onResume();
    }

    public void sendData(View view) {
        try {
            message = String.valueOf(editText.getText());
            myConnect.sendMsg(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buttonReceive(View view) {
        ConnectedThread connectedThread = new ConnectedThread(myConnect.getSocket());
        connectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Получаем кол-во байт и само собщение в байтовый массив "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Отправляем в очередь сообщений Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Данные для отправки: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Ошибка отправки данных: " + e.getMessage() + "...");
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}