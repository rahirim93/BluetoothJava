package com.example.bluetoothjava;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;

public class BlueInfo extends AppCompatActivity {
    // Переменные для извлечения информации о блютузе из главного активити
    public static final String EXTRA_BLUENAME = "name";
    public static final String EXTRA_BLUEADDRESS = "address";
    public static final String EXTRA_BLUETYPE = "type";
    String blueAddress;
    String blueName;
    String blueTypeStr;
    int blueType;

    //Сокет, с помощью которого мы будем отправлять данные на Arduino
    BluetoothSocket clientSocket;

    ConnectThread myConnect;

    String message;

    EditText editText;

    ToggleButton toggleButton;

    public Handler h;
    private StringBuilder sb = new StringBuilder();
    final int RECIEVE_MESSAGE = 1;
    private static final String TAG = "bluetooth2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_info);

        blueName = (String) getIntent().getExtras().get(EXTRA_BLUENAME);
        blueAddress = (String) getIntent().getExtras().get(EXTRA_BLUEADDRESS);
        blueType = (int) getIntent().getExtras().get(EXTRA_BLUETYPE);
        blueTypeStr = String.valueOf(blueType);

        init();

        try {
            myConnect = new ConnectThread(blueAddress, this);
            myConnect.start();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка соединения", LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    public void init() {
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

        h = new Handler(Looper.myLooper()) {
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
        try {
            myConnect = new ConnectThread(blueAddress, this);
            myConnect.start();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка соединения", LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (myConnect != null){
            myConnect.cancel();
        }
        super.onDestroy();
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
        try {
            ConnectedThread connectedThread = new ConnectedThread(myConnect.getSocket());
            connectedThread.start();
        } catch (Exception e) {
            Toast.makeText(this, "Отсутствует соединение", LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {

        }

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

    public class ConnectThread extends Thread {
        public String TAG = "BLUETOOTH";
        private final UUID MY_UUID =UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private OutputStream outputStream;
        private InputStream inputStream;

        public BluetoothSocket getSocket() {
            return mmSocket;
        }


        public ConnectThread (String address, Context context) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = bluetoothAdapter.getRemoteDevice(address);
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            //Показать тост если соединение успешно
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), "Соединено", LENGTH_SHORT);
                    toast.show();
                }
            });


            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) { }
            inputStream = tmpIn;
            outputStream = tmpOut;

            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);        // Получаем кол-во байт и само собщение в байтовый массив "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Отправляем в очередь сообщений Handler
                } catch (IOException e) {
                    break;
                }
            }
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(mmSocket);

        }

        public void sendMsg(String message) {
            try {
                outputStream = mmSocket.getOutputStream();  //Получение исходящего потока
                byte[] messageBuffer = message.getBytes();  //Перевод исходящего сообщения в байты
                outputStream.write(messageBuffer);          //Запись в поток
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

}