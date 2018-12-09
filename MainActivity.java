package hi.world.hello.farmcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    private BluetoothSPP bt;
    private Button btnTemp;
    private Button btnHumi;
    private EditText tempText;
    private EditText humiText;

    private boolean STATE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
                STATE = true;
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
                STATE = false;
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
                STATE = false;
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect); //연결시도
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });



    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
                setup();
            }
        }
    }

    public void setup() {

        btnTemp = findViewById(R.id.btnTemp); //최저 온도 데이터 전송
        btnHumi = findViewById(R.id.btnHumi); //최저 습도 데이터 전송

        tempText = findViewById(R.id.tempText);
        humiText = findViewById(R.id.humiText);

        btnTemp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (STATE == true) {
                    if(isStringDouble(tempText.getText().toString())) {
                        bt.send("temp" + tempText.getText().toString(), true);
                        Toast.makeText(getApplicationContext(), "최저 온도가 설정되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "블루투스를 연결해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnHumi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (STATE == true) {
                    if(isStringDouble(humiText.getText().toString())) {
                        bt.send("humi" + humiText.getText().toString(), true);
                        Toast.makeText(getApplicationContext(), "최저 습도가 설정되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "블루투스를 연결해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnOn = findViewById(R.id.btnOn);
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (STATE == true) {
                    bt.send("on", true);
                    Toast.makeText(getApplicationContext(), "시스템이 활성화 되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "블루투스를 연결해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnOff = findViewById(R.id.btnOff);
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (STATE == true) {
                    bt.send("off", true);
                    Toast.makeText(getApplicationContext(), "시스템이 종료되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "블루투스를 연결해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public static boolean isStringDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
