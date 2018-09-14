package com.example.xuemin.bluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.xuemin.bluetooth2.PixelGridView;

import java.nio.charset.Charset;
import java.util.UUID;

public class MazeActivity extends AppCompatActivity {
    private static final String TAG = "MazeActivity";
    BluetoothConnectionService bluetoothConnectionService;
    android.support.v7.widget.Toolbar toolbar;
    ImageButton upButton;
    PixelGridView pixelGridView;
    LinearLayout maze;
    Button automanualBtn;
    Button updateBtn;
    BluetoothAdapter mBluetoothAdapter;
    public static TextView x_coor;
    public static TextView y_coor;



    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    StringBuilder messages;

    TextView statusreceiveTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze);

        //initializing bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        //over write the tool bar title
        // toolbar.setTitle("Robot 23");
        setSupportActionBar(toolbar);

        upButton = (ImageButton) findViewById(R.id.upBtn);

        automanualBtn = (Button) findViewById(R.id.automanualBtn);
        updateBtn = (Button) findViewById(R.id.updateBtn);

        statusreceiveTV = (TextView) findViewById(R.id.statusreceiveTV);
        x_coor = (TextView) findViewById(R.id.x_coor);
        y_coor = (TextView) findViewById(R.id.y_coor);

        maze = (LinearLayout) findViewById(R.id.maze);
        maze.getViewTreeObserver().addOnGlobalLayoutListener(new MyGlobalListenerClass());

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));


        //to track if the bluetooth is connected to any device or not
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mBroadcastReceiver5, filter2);

    }

    //for importing the menu into the main page
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String msg = " ";
        switch (item.getItemId()) {
            case R.id.bluetooth:
                Intent intent = new Intent(this, Bluetooth.class);
                startActivity(intent);
                break;
            case R.id.map:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void explore(View view) {
        String left = "beginExplore";
        byte[] bytes = left.getBytes(Charset.defaultCharset());
        Bluetooth.mBluetoothConnection.write(bytes);
    }

    public void fastest(View view) {
        String left = "beginFastest";
        byte[] bytes = left.getBytes(Charset.defaultCharset());
        Bluetooth.mBluetoothConnection.write(bytes);
    }

    public void toggleAutoManual(View view) {
        String text = automanualBtn.getText().toString();
        if(text.equals("AUTO")){
            automanualBtn.setText("MANUAL");
            updateBtn.setEnabled(true);
        }
        else{
            automanualBtn.setText("AUTO");
            updateBtn.setEnabled(false);
        }
    }

    public void setWayPoint(View view) {
        if(Bluetooth.mBTDevice!=null){
            if(Bluetooth.mBTDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                String waypoint = "WAYPOINT(" + x_coor.getText() + "," + y_coor.getText() + ")";
                byte[] bytes = waypoint.getBytes(Charset.defaultCharset());
                Bluetooth.mBluetoothConnection.write(bytes);
            }
            else{
                Toast.makeText(this, "Please connect to a device", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Please connect to a device", Toast.LENGTH_SHORT).show();
        }

    }

    class MyGlobalListenerClass implements ViewTreeObserver.OnGlobalLayoutListener{
        public void onGlobalLayout(){
            pixelGridView=(PixelGridView)findViewById(R.id.pixelGridView);
            pixelGridView.setTotalWidth(pixelGridView.getWidth());
            pixelGridView.setTotalHeight(pixelGridView.getHeight());
            pixelGridView.setNumColumns(15);
            pixelGridView.setNumRows(20);
        }
    }

    public static void setCoordinates(int x, int y){
        x_coor.setText(String.valueOf(x));
        y_coor.setText(String.valueOf(y));
    }


    // a broadcast receiver to handle incoming messages
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            messages = new StringBuilder();
            messages.append(text);

            if (text.equals("exploring") || text.equals("fastest path") || text.equals("turning left") ||
                    text.equals("turning right") || text.equals("moving forward") || text.equals("reversing") || text.equals("stop")) {

                statusreceiveTV.setText(messages.toString().toUpperCase());


            }
        }
    };


    private final BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                Toast.makeText(MazeActivity.this, "BT Connected to: "+ deviceName, Toast.LENGTH_SHORT).show();

            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)){
                BluetoothDevice device1 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device1.getName();
                Toast.makeText(MazeActivity.this, "BT about to disconnected from: " + deviceName, Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device2 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device2.getName();
                Toast.makeText(MazeActivity.this, "BT is disconnected: "+ deviceName, Toast.LENGTH_SHORT).show();
                //startBTConnection(device2, MY_UUID_INSECURE);
            }
        }
    };

        public void turnLeft(View view) {
            String left = "tl";
            byte[] bytes = left.getBytes(Charset.defaultCharset());
            Bluetooth.mBluetoothConnection.write(bytes);
        }

        public void forward(View view) {
            String forward = "f";
            byte[] bytes = forward.getBytes(Charset.defaultCharset());
            Bluetooth.mBluetoothConnection.write(bytes);
        }

        public void reverse(View view) {
            String reverse = "r";
            byte[] bytes = reverse.getBytes(Charset.defaultCharset());
            Bluetooth.mBluetoothConnection.write(bytes);
        }

        public void turnRight(View view) {
            String right = "tr";
            byte[] bytes = right.getBytes(Charset.defaultCharset());
            Bluetooth.mBluetoothConnection.write(bytes);
        }

}
