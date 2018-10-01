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

import org.json.JSONException;
import org.json.JSONObject;

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
    Button startBtn;
    BluetoothAdapter mBluetoothAdapter;
    public static TextView x_coor;
    public static TextView y_coor;
    public static TextView arena;
    TextView startX;
    TextView startY;
    Button setStart;
    public boolean isAutoUpdate = true;
    public boolean listenForUpdate = false;
    private String storedMsg;
    private String storedInfo;





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
        startBtn = (Button) findViewById(R.id.startBtn);

        statusreceiveTV = (TextView) findViewById(R.id.statusreceiveTV);
        x_coor = (TextView) findViewById(R.id.x_coor);
        y_coor = (TextView) findViewById(R.id.y_coor);
        //arena= (TextView)findViewById(R.id.arenainfo);

        //startX = findViewById(R.id.start_x);
        //startY = findViewById(R.id.start_y);
        //setStart = findViewById(R.id.setStartBtn);


        maze = (LinearLayout) findViewById(R.id.maze);
        maze.getViewTreeObserver().addOnGlobalLayoutListener(new MyGlobalListenerClass());

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));


        //to track if the bluetooth is connected to any device or not
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mBroadcastReceiver5, filter2);
      // if(Bluetooth.mBTDevice !=null){
            //String update = "sendArena";
            //byte[] bytes = update.getBytes(Charset.defaultCharset());
           // Bluetooth.mBluetoothConnection.write(bytes);

        //}



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
                finish();
               // Intent intent = new Intent(this, Bluetooth.class);
                //startActivity(intent);
                break;
            case R.id.map:
                break;
            case R.id.settings:
                Intent intentSet= new Intent(this,Settings.class);
                startActivity(intentSet);
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
            isAutoUpdate = false;

            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String update = "sendArena";
                    byte[] bytes = update.getBytes(Charset.defaultCharset());
                    Bluetooth.mBluetoothConnection.write(bytes);
                    listenForUpdate = true;
                    if(storedMsg!=null){
                        pixelGridView.updateDemoRobotPos(storedMsg);
                    }
                }
            });
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        }
        else{
            automanualBtn.setText("AUTO");
            isAutoUpdate = true;
            listenForUpdate = false;
            updateBtn.setEnabled(false);
        }
    }

    public void clear(View view) {
        clearMap();
    }

    //Method to clear map
    private void clearMap(){
        pixelGridView.clearMap();
        pixelGridView.invalidate();
        pixelGridView.wpHide();
        Toast.makeText(this,"Map Cleared",Toast.LENGTH_SHORT).show();
    }

    public void setWayPoint(View view) {
        String x_coordinates = x_coor.getText().toString();
        String y_coordinates = y_coor.getText().toString();

        if(x_coordinates.equals("") || y_coordinates.equals("")){
            Toast.makeText(this, "Please select waypoint first!", Toast.LENGTH_SHORT).show();
        }
        else{
            if(Bluetooth.mBTDevice!=null){
                if(Bluetooth.mBTDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    pixelGridView.wpShow();
                    pixelGridView.setWaypoint(Integer.parseInt(x_coor.getText().toString()),Integer.parseInt(y_coor.getText().toString()));
                    String waypoint = "SETWAYPOINT(" + x_coor.getText() + "," + y_coor.getText() + ")";
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
    }

    public void setStartPoint(View view){
        String x_coordinates = x_coor.getText().toString();
        String y_coordinates = y_coor.getText().toString();

        if(x_coordinates.equals("") || y_coordinates.equals("")){
            Toast.makeText(this, "Please select waypoint first!", Toast.LENGTH_SHORT).show();
        }
        else{
            if(Bluetooth.mBTDevice!=null) {
                if (Bluetooth.mBTDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    String startpoint = "STARTPOINT(" + x_coor.getText() + "," + y_coor.getText() + ")";
                    int x = Integer.valueOf(x_coor.getText().toString());
                    int y = Integer.valueOf(y_coor.getText().toString());
                    byte[] bytes = startpoint.getBytes(Charset.defaultCharset());
                    Bluetooth.mBluetoothConnection.write(bytes);
                    pixelGridView.setStartPoint(x,y);
                    //pixelGridView.setCellchecked(x,y);
                }
                else{
                    Toast.makeText(this, "Please connect to a device", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Please connect to a device", Toast.LENGTH_SHORT).show();
            }
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
            String text1 = automanualBtn.getText().toString();
            // receive format MDF|current_y|current_x|robotfacing|exploredStr|obstacleStr
            if(text.contains("MDF")){
                String grid[] = text.split("\\|");
                String position = grid[1]+"|"+grid[2]+"|"+grid[3];
                if(text1.equals("AUTO")){

                String  exploredMap = grid[5];
                String obstacleMap = grid[6];
                pixelGridView.updateRobotPos(position);
                pixelGridView.updateArena(exploredMap,obstacleMap);
                }
            }
            byte[] read = text.getBytes();
            String mpInfo = new String(read);
            if (mpInfo.contains("grid")){
                if(isAutoUpdate==true || listenForUpdate==true) {
                    try {
                        JSONObject obj = new JSONObject(mpInfo);
                        String mpUp = obj.getString("grid");
                        pixelGridView.updateDemoArenaMap(mpUp);
                        pixelGridView.invalidate();
                        listenForUpdate=false;
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }

            }

            if(mpInfo.contains("robotPosition")) {
                if (isAutoUpdate == true || listenForUpdate == true) {
                    //update robot position
                    pixelGridView.updateDemoRobotPos(mpInfo);
                    pixelGridView.invalidate();
                    listenForUpdate=false;
                }
                else{
                    storedMsg=mpInfo;
                }
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
                Bluetooth.mBTDevice = device;
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
                Bluetooth.mBTDevice = null;
                Bluetooth.mBluetoothConnection.startClient(device2,MY_UUID_INSECURE);
            }
        }
    };

        public void turnLeft(View view) {
            if(Bluetooth.mBTDevice != null){
                String left = "tl";
                byte[] bytes = left.getBytes(Charset.defaultCharset());
                Bluetooth.mBluetoothConnection.write(bytes);
            }
            else{
                Toast.makeText(MazeActivity.this, "Please connect to a device first! ", Toast.LENGTH_SHORT).show();
            }

        }

        public void forward(View view) {
            if(Bluetooth.mBTDevice != null){
                String forward = "f";
                byte[] bytes = forward.getBytes(Charset.defaultCharset());
                Bluetooth.mBluetoothConnection.write(bytes);
            }
            else {
                Toast.makeText(MazeActivity.this, "Please connect to a device first! ", Toast.LENGTH_SHORT).show();
            }
        }

        public void reverse(View view) {
            if(Bluetooth.mBTDevice != null){
                String reverse = "r";
                byte[] bytes = reverse.getBytes(Charset.defaultCharset());
                Bluetooth.mBluetoothConnection.write(bytes);
            }
            else {
                Toast.makeText(MazeActivity.this, "Please connect to a device first! ", Toast.LENGTH_SHORT).show();
            }
        }

        public void turnRight(View view) {
            if(Bluetooth.mBTDevice != null){
                String right = "tr";
                byte[] bytes = right.getBytes(Charset.defaultCharset());
                Bluetooth.mBluetoothConnection.write(bytes);
            }
            else {
                Toast.makeText(MazeActivity.this, "Please connect to a device first! ", Toast.LENGTH_SHORT).show();
            }
        }

}
