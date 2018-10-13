package com.example.xuemin.bluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.UUID;

public class MazeActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MazeActivity";
    BluetoothConnectionService bluetoothConnectionService;
    android.support.v7.widget.Toolbar toolbar;
    ImageButton upButton;
    ImageButton downBtn;
    ImageButton rightBtn;
    ImageButton leftBtn;
    PixelGridView pixelGridView;
    MapDecoder md;
    Robot robot;
    LinearLayout maze;
    Button automanualBtn;
    Button updateBtn;
    Button startBtn;
    Button explorationBtn;
    Button fastestBtn;
    Button tiltBtn;
    BluetoothAdapter mBluetoothAdapter;
    public static TextView x_coor;
    public static TextView y_coor;
    TextView btConnectedTVList;
    TextView fastestreceiveTV;
    TextView explorationreceiveTV;
    TextView p1String;
    TextView p2String;
    private static Toast toast;
    public static TextView arena;
    TextView startX;
    TextView startY;
    Button setStart;
    public boolean isAutoUpdate = true;
    public boolean listenForUpdate = false;
    private String storedMsg;
    private String storedInfo;
    // variables for timer
    long startExploreTime = 0;
    long startFastTime =0;
    private boolean stillExplore = false;
    private boolean stillFast = false;
    private SensorManager sensorManager;
    private Sensor sensor;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private long lastUpdate;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;


    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    StringBuilder messages;

    TextView statusreceiveTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze);

        robot = new Robot(1,1,"NORTH");
        //initializing bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        //over write the tool bar title
        // toolbar.setTitle("Robot 23");
        setSupportActionBar(toolbar);
        //direction buttons
        upButton = (ImageButton) findViewById(R.id.upBtn);
        downBtn = (ImageButton) findViewById(R.id.downBtn);
        rightBtn = (ImageButton) findViewById(R.id.rightBtn);
        leftBtn = (ImageButton) findViewById(R.id.leftBtn);

        automanualBtn = (Button) findViewById(R.id.automanualBtn);
        updateBtn = (Button) findViewById(R.id.updateBtn);
        startBtn = (Button) findViewById(R.id.startBtn);
        explorationBtn = (Button) findViewById(R.id.explorationBtn);
        fastestBtn = (Button) findViewById(R.id.fastestBtn);
        tiltBtn = (Button) findViewById(R.id.tiltBtn);

        statusreceiveTV = (TextView) findViewById(R.id.statusreceiveTV);
        x_coor = (TextView) findViewById(R.id.x_coor);
        y_coor = (TextView) findViewById(R.id.y_coor);
        btConnectedTVList = (TextView) findViewById(R.id.btConnectedTVList);
        fastestreceiveTV = (TextView) findViewById(R.id.fastestreceiveTV);
        explorationreceiveTV = (TextView) findViewById(R.id.explorationreceiveTV);
        p1String = (TextView)findViewById(R.id.p1String);
        p2String = (TextView)findViewById(R.id.p2String);

        //declaring Sensor Manager and sensor type
        /*sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);*/

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

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
        // to display connected bluetooth device
        if(Bluetooth.mBTDevice != null){
            btConnectedTVList.setText(Bluetooth.mBTDevice.getName());
        }
        else{
            btConnectedTVList.setText("Not Connected");
        }

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
                Intent intent = new Intent(this, Bluetooth.class);
                startActivity(intent);
            case R.id.map:
                break;
            case R.id.calib:
               if(Bluetooth.mBTDevice!= null){
                   String explore = "+A1:A2:L90:A1:A2;";
                   byte[] bytes = explore.getBytes(Charset.defaultCharset());
                   if(bluetoothConnectionService!=null){
                       bluetoothConnectionService.write(bytes);
                   }
                   else{
                       Bluetooth.mBluetoothConnection.write(bytes);
                   }
               }
               else{
                   toast =  Toast.makeText(this,"text",Toast.LENGTH_SHORT);
                   toast.setText("Please connect to a device");
                   toast.setDuration(Toast.LENGTH_SHORT);
                   toast.show();
               }
               break;
            case R.id.settings:
                Intent intentSet= new Intent(this,Settings.class);
                startActivity(intentSet);
                break;
            case R.id.p:
                if(Bluetooth.mBTDevice!= null){
                    String explore = "+A1:A2:L90:A1:A2:P;";
                    byte[] bytes = explore.getBytes(Charset.defaultCharset());
                    if(bluetoothConnectionService!=null){
                        bluetoothConnectionService.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
                }
                else{
                    toast =  Toast.makeText(MazeActivity.this,"text",Toast.LENGTH_SHORT);
                    toast.setText("Please connect to a device");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.justp:
                if(Bluetooth.mBTDevice!= null){
                    String explore = "+P;";
                    byte[] bytes = explore.getBytes(Charset.defaultCharset());
                    if(bluetoothConnectionService!=null){
                        bluetoothConnectionService.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
                }
                else{
                    toast =  Toast.makeText(MazeActivity.this,"text",Toast.LENGTH_SHORT);
                    toast.setText("Please connect to a device");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void explore(View view) {
        if(Bluetooth.mBTDevice != null){
            /*String startpoint = "-STARTPOINT(2,2)";
            byte[] start = startpoint.getBytes(Charset.defaultCharset());
            Bluetooth.mBluetoothConnection.write(start);*/

            String explore = "-beginExplore";
            byte[] bytes = explore.getBytes(Charset.defaultCharset());
            if(bluetoothConnectionService!=null){
                bluetoothConnectionService.write(bytes);
            }
            else{
                Bluetooth.mBluetoothConnection.write(bytes);
            }
            String p = "+P;";
            byte[] bytes1 = p.getBytes(Charset.defaultCharset());
            if(bluetoothConnectionService!=null){
                bluetoothConnectionService.write(bytes1);
            }
            else{
                Bluetooth.mBluetoothConnection.write(bytes1);
            }
            stillExplore = true;
            startExploreTime = System.currentTimeMillis();
            timerHandlerExplore.postDelayed(timerRunnableExplore, 0);
            explorationBtn.setEnabled(false);
            disableDirection();
        }
        else{
            toast =  Toast.makeText(MazeActivity.this,"text",Toast.LENGTH_SHORT);
            toast.setText("Please connect to a device");
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    // Handler for exploration timer function
    Handler timerHandlerExplore = new Handler();
    Runnable timerRunnableExplore = new Runnable() {
        @Override
        public void run() {
            if(!stillExplore){
                return;
            }
            long millis = System.currentTimeMillis() - startExploreTime;
            int seconds = (int) (millis / 1000.0);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            explorationreceiveTV.setText("Min : Seconds " + String.format("%d:%d", ((int) minutes), ((int) seconds)));

            timerHandlerExplore.postDelayed(this, 0);
        }

    };

    public void fastest(View view) {
        if(Bluetooth.mBTDevice != null){
            String fastest = "-beginFastest";
            byte[] bytes = fastest.getBytes(Charset.defaultCharset());
            if(bluetoothConnectionService!=null){
                bluetoothConnectionService.write(bytes);
            }
            else{
                Bluetooth.mBluetoothConnection.write(bytes);
            }
            stillFast = true;
            startFastTime = System.currentTimeMillis();
            timerHandlerFastest.postDelayed(timerRunnableFastest, 0);
            fastestBtn.setEnabled(false);
            disableDirection();
        }
        else{
            toast =  Toast.makeText(MazeActivity.this,"text",Toast.LENGTH_SHORT);
            toast.setText("Please connect to a device");
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // Handler for fastest path timer function
    Handler timerHandlerFastest = new Handler();
    Runnable timerRunnableFastest = new Runnable() {
        @Override
        public void run() {
            if(!stillFast){
                return;
            }
            long millis = System.currentTimeMillis() - startFastTime;
            int seconds = (int) (millis / 1000.0);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            fastestreceiveTV.setText("Min : Seconds "+String.format("%d:%d", ((int)minutes), ((int)seconds)));
            timerHandlerFastest.postDelayed(this, 0);
        }
    };

    //Enable directional buttons
    private void enableDirection(){
        upButton.setEnabled(true);
        downBtn.setEnabled(true);
        leftBtn.setEnabled(true);
        rightBtn.setEnabled(true);
    }
    //Disable directional buttons
    private void disableDirection(){
        upButton.setEnabled(false);
        downBtn.setEnabled(false);
        leftBtn.setEnabled(false);
        rightBtn.setEnabled(false);
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

                    String update = "-sendArena";
                    byte[] bytes = update.getBytes(Charset.defaultCharset());
                    if(bluetoothConnectionService!=null){
                        bluetoothConnectionService.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
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
            toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
            toast.setText("Please select waypoint first!");
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            if(Bluetooth.mBTDevice!=null){
                if(Bluetooth.mBTDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    pixelGridView.wpShow();
                    pixelGridView.setWaypoint(Integer.parseInt(x_coor.getText().toString()),Integer.parseInt(y_coor.getText().toString()));
                    String waypoint = "-SETWAYPOINT(" + x_coor.getText() + "," + y_coor.getText() + ")";
                    byte[] bytes = waypoint.getBytes(Charset.defaultCharset());
                    pixelGridView.invalidate();
                    if(bluetoothConnectionService!=null){
                        bluetoothConnectionService.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
                }
                else{
                    toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                    toast.setText("Please connect to a device!");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Please connect to a device!");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    public void setStartPoint(View view){
        String x_coordinates = x_coor.getText().toString();
        String y_coordinates = y_coor.getText().toString();
        if(x_coordinates.equals("") || y_coordinates.equals("")){
            x_coor.setText("2");
            y_coor.setText("2");
        }

        /*if(x_coordinates.equals("") || y_coordinates.equals("")){
            Toast.makeText(this, "Please select waypoint first!", Toast.LENGTH_SHORT).show();
        }*/

        if(Bluetooth.mBTDevice!=null) {
                if (Bluetooth.mBTDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    String startpoint = "-STARTPOINT(" + x_coor.getText().toString() + "," + y_coor.getText().toString() + ")";
                    int x = Integer.valueOf(x_coor.getText().toString());
                    int y = Integer.valueOf(y_coor.getText().toString());
                    byte[] bytes = startpoint.getBytes(Charset.defaultCharset());
                    if(bluetoothConnectionService!=null){
                        bluetoothConnectionService.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
                    /* requested by algo to send 2,2
                    //Log.d("test123",startpoint);
                    //String robotPosition = y + "|" + x + "|" + "SOUTH";
                    //pixelGridView.updateRobotPos(robotPosition);
                    //pixelGridView.invalidate();
                    //robot = new Robot(x,y,"SOUTH");
                    //robot.setPosition(x,y);
                    //robot.setDirection("SOUTH");
                    //pixelGridView.setCellchecked(x,y);*/
                }
                else{
                    toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                    toast.setText("Please connect to a device!");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Please connect to a device!");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();

            }

    }

    public void tilt(View view) {
        if(tiltBtn.getText() == "Tilt Off"){
            tiltBtn.setText("Tilt On");
        }
        else{
            tiltBtn.setText("Tilt Off");
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

            //for status update
            if (text.equals("exploring") || text.equals("fastest path") || text.equals("L90") ||
                    text.equals("R90") || text.equals("F1") || text.equals("B1") || text.equals("stop")) {
                if(text.equals("R90")){
                    String turnright = "Turn Right";
                    statusreceiveTV.setText(turnright.toUpperCase());
                }
                if(text.equals("L90")){
                    String turnleft = "Turn Left";
                    statusreceiveTV.setText(turnleft.toUpperCase());
                }
                if(text.equals("F1")){
                    String forward = "Moving Forward";
                    statusreceiveTV.setText(forward.toUpperCase());
                }
                if(text.equals("B1")){
                    String reverse = "Reversing";
                    statusreceiveTV.setText(reverse.toUpperCase());
                }
                else{
                    statusreceiveTV.setText(messages.toString().toUpperCase());
                }
            }

            String text1 = automanualBtn.getText().toString();
            // receive format MDF|current_y|current_x|robotfacing|exploredStr|obstacleStr
            if(text.contains("MDF")){
                String grid[] = text.split("\\|");
                //y|x|direction
                String position = grid[1]+"|"+grid[2]+"|"+grid[3];
                if(text1.equals("AUTO")){
                robot.setPosition(Integer.parseInt(grid[2]), Integer.parseInt(grid[1]));
                robot.setDirection(grid[3]);
                String  exploredMap = grid[4];
                String obstacleMap = grid[5];
                if(exploredMap.matches("^[0-9a-fA-F]+$") && obstacleMap.matches("^[0-9a-fA-F]+$")){
                    pixelGridView.updateRobotPos(position);
                    pixelGridView.updateArena(exploredMap,obstacleMap);
                    pixelGridView.invalidate();
                    p1String.setText(exploredMap);
                    p2String.setText(obstacleMap);
                }
                else{
                    toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                    toast.setText("Incorrect String format");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }

                }
            }

            /* byte[] read = text.getBytes();
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
            }*/
            if(text.contains("arrowPosition")) {
                if (isAutoUpdate == true || listenForUpdate == true) {
                    pixelGridView.arrowpost = true;
                    pixelGridView.getArrowPosition(text);
                    pixelGridView.invalidate();
                    listenForUpdate=false;
                }
            }
            else{
                pixelGridView.arrowpost = false;
            }

            if(text.contains("endexplore")){
                stillExplore=false;
                explorationBtn.setEnabled(true);
                enableDirection();
            }

            if(text.contains("endfastest")){
                stillFast=false;
                fastestBtn.setEnabled(true);
                enableDirection();
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
                btConnectedTVList.setText(device.getName());
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("BT Connected to: "+ deviceName);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();

            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)){
                BluetoothDevice device1 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device1.getName();
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("BT about to disconnected from:"+ deviceName);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();

            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device2 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device2.getName();
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("BT is disconnected:"+ deviceName);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
                Bluetooth.mBTDevice = null;
                btConnectedTVList.setText("Not Connected");
                try{
                    bluetoothConnectionService = new BluetoothConnectionService(MazeActivity.this);
                    bluetoothConnectionService.startClient(device2, MY_UUID_INSECURE);

                    //Bluetooth.mBluetoothConnection = bluetoothConnectionService;
                }
                catch(Exception e){
                    Log.d(TAG, "MazeActivity Reconnect " + e.getMessage());
                }


            }
        }
    };

        public void turnLeft(View view) {
            if(Bluetooth.mBTDevice != null){
                //String left = "tl";
                String left = "+L90";
                byte[] bytes = left.getBytes(Charset.defaultCharset());if(bluetoothConnectionService!=null){
                    bluetoothConnectionService.write(bytes);
                }
                else{
                    Bluetooth.mBluetoothConnection.write(bytes);
                }
                String position = robot.getPosition();
                String direction = robot.getDirection();
                String grid[] = position.split(",");
                int x = Integer.parseInt(grid[0]);
                int y = Integer.parseInt(grid[1]);
                turnLeftMovement(x,y,direction);
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Please connect to a device first!");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }

        }


    public void forward(View view) {
            if(Bluetooth.mBTDevice != null){
                //String forward = "f";
                String forward = "+F1";
                byte[] bytes = forward.getBytes(Charset.defaultCharset());if(bluetoothConnectionService!=null){
                    bluetoothConnectionService.write(bytes);
                }
                else{
                    Bluetooth.mBluetoothConnection.write(bytes);
                }
                String position = robot.getPosition();
                String direction = robot.getDirection();
                String grid[] = position.split(",");
                int x = Integer.parseInt(grid[0]);
                int y = Integer.parseInt(grid[1]);
                forwardMovement(x,y,direction);
            }
            else {
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Please connect to a device first!");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    public void turnRight(View view) {
        if(Bluetooth.mBTDevice != null){
            //String right = "tr";
            String right = "+R90";
            byte[] bytes = right.getBytes(Charset.defaultCharset());
            if(bluetoothConnectionService!=null){
                bluetoothConnectionService.write(bytes);
            }
            else{
                Bluetooth.mBluetoothConnection.write(bytes);
            }
            String position = robot.getPosition();
            String direction = robot.getDirection();
            String grid[] = position.split(",");
            int x = Integer.parseInt(grid[0]);
            int y = Integer.parseInt(grid[1]);
            turnRightMovement(x,y,direction);

        }
        else {
            toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
            toast.setText("Please connect to a device first!");
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }



    public void reverse(View view) {
            if(Bluetooth.mBTDevice != null){
                //String reverse = "r";
                String reverse = "+B1";
                byte[] bytes = reverse.getBytes(Charset.defaultCharset());
                if(bluetoothConnectionService!=null){
                    bluetoothConnectionService.write(bytes);
                }
                else{
                    Bluetooth.mBluetoothConnection.write(bytes);
                }
                String position = robot.getPosition();
                String direction = robot.getDirection();
                String grid[] = position.split(",");
                int x = Integer.parseInt(grid[0]);
                int y = Integer.parseInt(grid[1]);
                reverseMovement(x,y,direction);

            }
            else {
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Please connect to a device first!");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    private void reverseMovement(int x, int y, String direction) {
        if(direction == "NORTH"){
            y = y - 1;
            if(y > 0 && y < 19){
                robot.setPosition(x,y);
                robot.setDirection("NORTH");
                String updatedposition = y+"|"+x+"|"+"NORTH";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();

            }
        }
        if(direction == "EAST"){
            x = x - 1;
            if(x > 0 && x < 14){
                robot.setPosition(x,y);
                robot.setDirection("EAST");
                String updatedposition = y+"|"+x+"|"+"EAST";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        if(direction == "WEST"){
            x = x + 1;
            if(x > 0 && x < 14){
                robot.setPosition(x,y);
                robot.setDirection("WEST");
                String updatedposition = y+"|"+x+"|"+"WEST";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(direction == "SOUTH"){
            y = y + 1;
            if(y > 0 && y < 19){
                robot.setPosition(x,y);
                robot.setDirection("SOUTH");
                String updatedposition = y+"|"+x+"|"+"SOUTH";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }



    private void turnRightMovement(int x, int y, String direction) {
        if(direction == "NORTH"){
            if(y > 0 && y < 19){
                robot.setPosition(x,y);
                robot.setDirection("EAST");
                String updatedposition = y+"|"+x+"|"+"EAST";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(direction == "EAST"){
            if(x > 0 && x < 14){
                robot.setPosition(x,y);
                robot.setDirection("SOUTH");
                String updatedposition = y+"|"+x+"|"+"SOUTH";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        if(direction == "WEST"){
            if(x > 0 && x < 14){
                robot.setPosition(x,y);
                robot.setDirection("NORTH");
                String updatedposition = y+"|"+x+"|"+"NORTH";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(direction == "SOUTH"){
            if(y > 0 && y < 19){
                robot.setPosition(x,y);
                robot.setDirection("WEST");
                String updatedposition = y+"|"+x+"|"+"WEST";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private void turnLeftMovement(int x, int y, String direction) {

        if(direction == "NORTH"){
            if(y > 0 && y < 19){
                robot.setPosition(x,y);
                robot.setDirection("WEST");
                String updatedposition = y+"|"+x+"|"+"WEST";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(direction == "EAST"){
            if(x > 0 && x < 14){
                robot.setPosition(x,y);
                robot.setDirection("NORTH");
                String updatedposition = y+"|"+x+"|"+"NORTH";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        if(direction == "WEST"){
            if(x > 0 && x < 14){
                robot.setPosition(x,y);
                robot.setDirection("SOUTH");
                String updatedposition = y+"|"+x+"|"+"SOUTH";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(direction == "SOUTH"){
            if(y > 0 && y < 19){
                robot.setPosition(x,y);
                robot.setDirection("EAST");
                String updatedposition = y+"|"+x+"|"+"EAST";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private void forwardMovement(int x, int y, String direction) {
        if(direction == "NORTH"){
            y = y + 1;
            if(y > 0 && y < 19){
                robot.setPosition(x,y);
                robot.setDirection("NORTH");
                String updatedposition = y+"|"+x+"|"+"NORTH";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(direction == "EAST"){
            x = x + 1;
            if(x > 0 && x < 14){
                robot.setPosition(x,y);
                robot.setDirection("EAST");
                String updatedposition = y+"|"+x+"|"+"EAST";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        if(direction == "WEST"){
            x = x - 1;
            if(x > 0 && x < 14){
                robot.setPosition(x,y);
                robot.setDirection("WEST");
                String updatedposition = y+"|"+x+"|"+"WEST";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(direction == "SOUTH"){
            y = y - 1;
            if(y > 0 && y < 19){
                robot.setPosition(x,y);
                robot.setDirection("SOUTH");
                String updatedposition = y+"|"+x+"|"+"SOUTH";
                pixelGridView.updateRobotPos(updatedposition);
                pixelGridView.invalidate();
            }
            else{
                toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                toast.setText("Invalid Move! ");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long actualTime = event.timestamp;

        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0,
                    event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0,
                    event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer,
                    mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
        }

        if (tiltBtn.getText().equals("Tilt Off")) {


            if (((actualTime - lastUpdate) > 1000000000)) {
                float x = mOrientation[2];
                float y = mOrientation[1];

                if (Math.abs(x) > Math.abs(y)) {
                    if (x > 0.7 ) {
                        String position = robot.getPosition();
                        String direction = robot.getDirection();
                        String grid[] = position.split(",");
                        int robotx = Integer.parseInt(grid[0]);
                        int roboty = Integer.parseInt(grid[1]);
                        turnRightMovement(robotx,roboty,direction);
                        toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                        toast.setText("Command is : Move Right");
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.show();

                        if (Bluetooth.mBTDevice  !=null) {
                            String update = "+R90";
                            byte[] bytes = update.getBytes(Charset.defaultCharset());
                            if(bluetoothConnectionService!=null){
                                bluetoothConnectionService.write(bytes);
                            }
                            else{
                                Bluetooth.mBluetoothConnection.write(bytes);
                            }
                        }

                    }
                    if (x < -0.7) {
                        String position = robot.getPosition();
                        String direction = robot.getDirection();
                        String grid[] = position.split(",");
                        int robotx = Integer.parseInt(grid[0]);
                        int roboty = Integer.parseInt(grid[1]);
                        turnLeftMovement(robotx,roboty,direction);
                        toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                        toast.setText("Command is : Move Left");
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.show();
                        if (Bluetooth.mBTDevice  !=null) {
                            String update = "+L90";
                            byte[] bytes = update.getBytes(Charset.defaultCharset());
                            if(bluetoothConnectionService!=null){
                                bluetoothConnectionService.write(bytes);
                            }
                            else{
                                Bluetooth.mBluetoothConnection.write(bytes);
                            }
                        }
                        //sendMessage(message);
                        //Toast.makeText(getActivity(), "Command is : " + message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (y > 0.3) {
                        String position = robot.getPosition();
                        String direction = robot.getDirection();
                        String grid[] = position.split(",");
                        int robotx = Integer.parseInt(grid[0]);
                        int roboty = Integer.parseInt(grid[1]);
                        forwardMovement(robotx,roboty,direction);
                        toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                        toast.setText("Command is : Move Up");
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.show();
                        if (Bluetooth.mBTDevice  !=null) {
                            String update = "+F1";
                            byte[] bytes = update.getBytes(Charset.defaultCharset());
                            if(bluetoothConnectionService!=null){
                                bluetoothConnectionService.write(bytes);
                            }
                            else{
                                Bluetooth.mBluetoothConnection.write(bytes);
                            }
                        }
                    }
                    if (y < -0.7) {
                        String position = robot.getPosition();
                        String direction = robot.getDirection();
                        String grid[] = position.split(",");
                        int robotx = Integer.parseInt(grid[0]);
                        int roboty = Integer.parseInt(grid[1]);
                        reverseMovement(robotx,roboty,direction);
                        toast =  Toast.makeText(MazeActivity.this,"",Toast.LENGTH_SHORT);
                        toast.setText("Command is : Move Down");
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.show();
                       if (Bluetooth.mBTDevice  !=null){
                           String update = "+B1";
                           byte[] bytes = update.getBytes(Charset.defaultCharset());
                           if(bluetoothConnectionService!=null){
                               bluetoothConnectionService.write(bytes);
                           }
                           else{
                               Bluetooth.mBluetoothConnection.write(bytes);
                           }
                        }

                    }
                }

                lastUpdate = actualTime;

           }

        } else {
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);*/

        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetometer,
                SensorManager.SENSOR_DELAY_FASTEST);
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    //to make the broadcastreceiver close when the app got destroyed or hanged
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        try{
            unregisterReceiver(mBroadcastReceiver5);
        }
        catch(Exception e){
            Log.e(TAG, e.getMessage()+"mBroadcastReceiver5");
        }
        try{
            unregisterReceiver(mReceiver);
        }
        catch(Exception e){
            Log.e(TAG, e.getMessage()+"mReceiver");
        }

        if(toast != null){
            toast.cancel();
        }
        finish();
    }

}
