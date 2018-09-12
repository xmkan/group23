package com.example.xuemin.bluetooth2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.w3c.dom.Text;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity{
    private static final String TAG = "Bluetooth";
    //////// Start of declaration for views
    //toolbar
    android.support.v7.widget.Toolbar toolbar;
    //buttons
    Button btnStartConnection;
    Button btnSend;
    Button btnEnableDisable_Discoverable;
    //textviews
    TextView incomingMessages;
    StringBuilder messages;
    TextView outgoingMessagesTV;
    StringBuilder outgoingMessages = new StringBuilder();
    TextView noDevicesTV;
    TextView noPairedTV;
    TextView commandLogTV;
    //edit text
    EditText etSend;
    //Listviews
    ListView lvNewDevices;
    ListView lvPairedDevices;
    //ProgressBar
    ProgressBar progressBar;
    //////// End of declaration for views

    ////////Start of Bluetooth variables
    // i need to declare this as global variable so that the other activity can retrieve the same connection
    //for calling method from BluetoothConnectionService class
    public static BluetoothConnectionService mBluetoothConnection;

    //UUID for bluetooth serial connection
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    
    // an array list of type bluetooth devices and hold the bluetooth devices that it discovers
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> pairedBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    public DeviceListAdapter pairedDeviceListAdapter;

    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothDevice mBTDevice;
    ////////End of Bluetooth variables

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //checking of permissions
        int ACTION_REQUEST_MULTIPLE_PERMISSION = 1;  // Any number
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int pCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            pCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            pCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_ADMIN");
            pCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH");
            if(pCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH}, ACTION_REQUEST_MULTIPLE_PERMISSION);
                Log.d(TAG, "Permissions granted");
            }
        }

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        //over write the tool bar title
        // toolbar.setTitle("Robot 23");
        setSupportActionBar(toolbar);

        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();
        noDevicesTV = (TextView) findViewById(R.id.noDevicesTV);

        noPairedTV = (TextView) findViewById(R.id.noPairedTV);

        lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);
        pairedBTDevices = new ArrayList<>();


        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        etSend = (EditText) findViewById(R.id.editText);

        incomingMessages = (TextView) findViewById(R.id.incomingMessage);
        incomingMessages.setMovementMethod(new ScrollingMovementMethod());
        messages = new StringBuilder();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        outgoingMessagesTV = (TextView) findViewById(R.id.outgoingMessage);
        outgoingMessagesTV.setMovementMethod(new ScrollingMovementMethod());

        commandLogTV = (TextView) findViewById(R.id.commandLogTV);
        commandLogTV.setMovementMethod(new ScrollingMovementMethod());

        progressBar = (ProgressBar) findViewById(R.id.determinateBar);

        //Broadcasts when bond state changes(i.e:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        // Register for broadcasts when discovery has finished
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mBroadcastReceiver3, filter1);

        //to track if the bluetooth is connected to any device or not
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mBroadcastReceiver5, filter2);


        //initializing bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //whenever btnONOFF is clicked, this method gets called
        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enabledDisableBT();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                //display out going messages
                outgoingMessages.append(etSend.getText() + "\n");
                outgoingMessagesTV.setText(outgoingMessages);

                etSend.setText("");
            }
        });

        lvNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //first cancel discovery because its very memory intensive
                mBluetoothAdapter.cancelDiscovery();

                Log.d(TAG, "onItemClick: You Clicked on a device.");
                String deviceName = mBTDevices.get(i).getName();
                String deviceAddress = mBTDevices.get(i).getAddress();

                Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

                Toast.makeText(Bluetooth.this,"You clicked on " + deviceName, Toast.LENGTH_SHORT).show();

                //create the bond
                //NOTE: Requires API 17+? I think this is JellyBean
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                    Log.d(TAG, "Trying to connect with " + deviceName);
                    Toast.makeText(Bluetooth.this,"Trying to connect with " + deviceName, Toast.LENGTH_SHORT).show();
                    mBTDevices.get(i).createBond();
                    //assign the bluetooth device to the device that we clicked on
                    mBTDevice = mBTDevices.get(i);
                    mBluetoothConnection = new BluetoothConnectionService(Bluetooth.this);

                }
            }
        });

        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //first cancel discovery because its very memory intensive
                mBluetoothAdapter.cancelDiscovery();

                Log.d(TAG, "onItemClick: You Clicked on a device.");
                String deviceName = pairedBTDevices.get(i).getName();
                String deviceAddress = pairedBTDevices.get(i).getAddress();

                Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

                Toast.makeText(Bluetooth.this,"You clicked on " + deviceName, Toast.LENGTH_SHORT).show();

                //create the bond
                //NOTE: Requires API 17+? I think this is JellyBean
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                    Log.d(TAG, "Trying to pair with " + deviceName);
                    Toast.makeText(Bluetooth.this,"Trying to connect with " + deviceName, Toast.LENGTH_SHORT).show();
                    pairedBTDevices.get(i).createBond();
                    //assign the bluetooth device to the device that we clicked on
                    mBTDevice = pairedBTDevices.get(i);
                    mBluetoothConnection = new BluetoothConnectionService(Bluetooth.this);
                }
            }
        });


    }

    //for importing the menu into the main page
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String msg =" ";
        switch(item.getItemId()){
            case R.id.bluetooth:
                break;
            case R.id.map:
                Intent intent1 = new Intent(Bluetooth.this, MazeActivity.class);
                startActivity(intent1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* create method for starting connection, remember the connection will fail and app will crash if you havent paired first*/
    public void startConnection(){
        if(mBTDevice == null){
            Toast.makeText(Bluetooth.this,"Please connect to a device first! ", Toast.LENGTH_SHORT).show();
        }
        else{
            startBTConnection(mBTDevice,MY_UUID_INSECURE);
        }

    }
    /* starting chat service method */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        if(device == null){
            Toast.makeText(Bluetooth.this,"Please connect to a device first! ", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection. ");
        mBluetoothConnection.startClient(device,uuid);
    }

    // a broadcast receiver to handle incoming messages
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            messages.append(text + "\n");

            incomingMessages.setText(messages);

        }
    };

    /* broadcast receiver for changes made to bluetooth states such as:
     * 1) Bluetooth status */
    //create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //when the bluetooth status changed, use ACTION_STATE_CHANGED
            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                //find the integer that defines the state
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"onReceive: STATE OFF");
                        Toast.makeText(Bluetooth.this, "BT State Off", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        Toast.makeText(Bluetooth.this, "BT State Turning Off", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        Toast.makeText(Bluetooth.this, "BT State On", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        Toast.makeText(Bluetooth.this, "BT State Turning On", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };
    /* broadcast receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //when the bluetooth mode changed, use ACTION_SCAN_MODE_CHANGED
            if(action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                //the mode is in integer form
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, mBluetoothAdapter.ERROR);

                switch(mode){
                    //device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG,"mBroadcastReceiver2: Discoverability Enabled.");
                        Toast.makeText(Bluetooth.this, "Discoverability Enabled.", Toast.LENGTH_SHORT).show();
                        break;
                    //device is not in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections");
                        Toast.makeText(Bluetooth.this, "Discoverability Disabled. Able to receive connections", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting");
                        Toast.makeText(Bluetooth.this, "BT Connecting", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected");
                        Toast.makeText(Bluetooth.this, "BT Connected", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };


    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            pairedDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, pairedBTDevices);
            mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            //add in pair devices
            if(pairedDevices.size() > 0){
                for(BluetoothDevice device1 : pairedDevices){
                    if(pairedBTDevices.contains(device1)){
                        Log.d(TAG, "onReceive: " + device1.getName() + " " + device1.getAddress() + " already exist in paired list.");
                    }
                    else{
                        pairedBTDevices.add(device1);
                        lvPairedDevices.setAdapter(pairedDeviceListAdapter);
                    }
                }
            }


            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(pairedDevices.contains(device)){
                        //if paired devices is being found, then initiate connection

                    }
                    else{
                        if(mBTDevices.contains(device)){
                            Log.d(TAG, "onReceive: " + device.getName() + " " + device.getAddress() + " already exist.");
                        }
                        else{
                            mBTDevices.add(device);
                            Log.d(TAG, "onReceive: " + device.getName() + " " + device.getAddress());
                            lvNewDevices.setAdapter(mDeviceListAdapter);
                        }
                    }

            }
            else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                // Indicate scanning in the title
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Bluetooth.this, "Discovery Finished", Toast.LENGTH_SHORT).show();
                if (mDeviceListAdapter.getCount() == 0) {
                    noDevicesTV.setVisibility(View.VISIBLE);
                }
                if(pairedDeviceListAdapter.getCount() == 0){
                    noPairedTV.setVisibility(View.VISIBLE);
                }
            }
        }
    };


    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case 1: bonded already
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //assigning global bluetooth device to device
                    mBTDevice = mDevice;
                    Log.d(TAG, "Bluetooth Device bonded: " + mDevice.getName());
                    pairedBTDevices.add(mDevice);
                    pairedDeviceListAdapter = new DeviceListAdapter(Bluetooth.this, R.layout.device_adapter_view, pairedBTDevices);
                    lvPairedDevices.setAdapter(pairedDeviceListAdapter);

                    mBTDevices.remove(mDevice);
                    mDeviceListAdapter = new DeviceListAdapter(Bluetooth.this, R.layout.device_adapter_view, mBTDevices);
                    lvNewDevices.setAdapter(mDeviceListAdapter);

                }
                //case 2: creating a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case 3: breaking a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
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
                Toast.makeText(Bluetooth.this, "BT Connected to: "+ deviceName, Toast.LENGTH_SHORT).show();
            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)){
                BluetoothDevice device1 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device1.getName();
                Toast.makeText(Bluetooth.this, "BT about to disconnected from: " + deviceName, Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device2 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device2.getName();
                Toast.makeText(Bluetooth.this, "BT is disconnected: "+ deviceName, Toast.LENGTH_SHORT).show();
                startBTConnection(device2, MY_UUID_INSECURE);
            }
        }
    };

    //to make the broadcastreceiver close when the app got destroyed or hanged
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);

    }



    public void enabledDisableBT(){
        int REQUEST_BLUETOOTH = 1;
        //Phone does not support bluetooth so let user know and exit
        if(mBluetoothAdapter == null){
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        //checking if the bluetooth is enabled
        //when the bluetooth is disabled
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Toast.makeText(Bluetooth.this, "Enabling Bluetooth", Toast.LENGTH_SHORT).show();
            //using an intent to enable the bluetooth device
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);

            //a filter that intercepts the changes in the bluetooth status
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent); //the broadcast receiver will catch the state change of bluetooth
        }
        //when the bluetooth is enabled, we will disable the bluetooth
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            Toast.makeText(Bluetooth.this, "Disabling Bluetooth", Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.disable();
            //clearing the listview when it's off
            lvNewDevices.setAdapter(null);
            lvPairedDevices.setAdapter(null);
            noPairedTV.setVisibility(View.GONE);
            noDevicesTV.setVisibility(View.GONE);
            //a filter that intercepts the changes in the bluetooth status
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }


    }
    // for Button : btnDiscoverable_on_off
    public void btnEnableDisable_Discoverable(View view){
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //using .putExtra to define the discoverable duration
        //this will be the amount of time that your device will be discoverable to other devices
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }

    // for Button : btnFindUnpairedDevices
    public void btnDiscover(View view) {
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
            // Indicate scanning in the title
            progressBar.setVisibility(View.VISIBLE);

            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "btnDiscover: Canceling discovery.");

                mBluetoothAdapter.startDiscovery();
                Toast.makeText(Bluetooth.this, "Start Discovery", Toast.LENGTH_SHORT).show();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3,discoverDevicesIntent);
            }

            if(!mBluetoothAdapter.isDiscovering()){
                Toast.makeText(Bluetooth.this, "Start Discovery", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3,discoverDevicesIntent);
            }
        }
        else{
            Toast.makeText(Bluetooth.this,"Please turn on bluetooth first!", Toast.LENGTH_SHORT).show();
        }

    }

}
