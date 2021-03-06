package com.example.xuemin.bluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

public class Settings extends AppCompatActivity {

    //Bluetooth
    BluetoothConnectionService bluetoothConnectionService;
    BluetoothAdapter mBluetoothAdapter;


    android.support.v7.widget.Toolbar toolbar;
    SharedPreferences sharedpreferences;
    EditText function1;
    EditText function2;
    TextView function1new;
    TextView function2new;
    Button btnSendF1;
    Button btnSendF2;
    public static final String configuration = "configuration";
    public static final String function1Text = "function1Key";
    public static final String function2Text = "function2Key";
    private static Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        function1 = findViewById(R.id.etF1);
        function2 =  findViewById(R.id.etF2);

        btnSendF1 = (Button) findViewById(R.id.btnf1);
        btnSendF2 = (Button) findViewById(R.id.btnf2);


        //Initiate Bluetooth
        //initializing bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        sharedpreferences = getSharedPreferences(configuration,
                Context.MODE_PRIVATE);
        if (sharedpreferences.contains(function1Text)) {
            function1.setText(sharedpreferences.getString(function1Text, ""));
        }
        if (sharedpreferences.contains(function1Text)) {
            function2.setText(sharedpreferences.getString(function2Text, ""));
        }

        btnSendF1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                function1new =  findViewById(R.id.etF1new);

                sharedpreferences = getSharedPreferences(configuration,
                        Context.MODE_PRIVATE);

                if (sharedpreferences.contains(function1Text)) {
                    function1new.setText(sharedpreferences.getString(function1Text, ""));
                }
                byte[] bytes = function1new.getText().toString().getBytes(Charset.defaultCharset());

                if(Bluetooth.mBTDevice!= null){

                    if(MazeActivity.bluetoothConnectionServices!=null){
                        MazeActivity.bluetoothConnectionServices.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
                }
                else{
                    toast =  Toast.makeText(Settings.this,"text",Toast.LENGTH_SHORT);
                    toast.setText("Please connect to a device");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
                //display out going messages
            }
        });

        btnSendF2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                function2new =  findViewById(R.id.etF2new);

                sharedpreferences = getSharedPreferences(configuration,
                        Context.MODE_PRIVATE);

                if (sharedpreferences.contains(function1Text)) {
                    function2new.setText(sharedpreferences.getString(function2Text, ""));
                }
                byte[] bytes = function2new.getText().toString().getBytes(Charset.defaultCharset());
                if(Bluetooth.mBTDevice!= null){

                    if(MazeActivity.bluetoothConnectionServices!=null){
                        MazeActivity.bluetoothConnectionServices.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
                }
                else{
                    toast =  Toast.makeText(Settings.this,"text",Toast.LENGTH_SHORT);
                    toast.setText("Please connect to a device");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
                //display out going messages


            }
        });

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        //over write the tool bar title
        // toolbar.setTitle("Robot 23");
        setSupportActionBar(toolbar);
    }

    public void Save(View view) {
        String f1 = function1.getText().toString();
        String f2 = function2.getText().toString();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(function1Text, f1);
        editor.putString(function2Text, f2);
        editor.commit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth:
                Intent intent = new Intent(this, Bluetooth.class);
                startActivity(intent);
            case R.id.map:
                Intent intent1 = new Intent(this, MazeActivity.class);
                startActivity(intent1);
                break;
            case R.id.calib:
                if(Bluetooth.mBTDevice!= null){
                    String explore = "+A1:A2:L90:A1:A2;";
                    byte[] bytes = explore.getBytes(Charset.defaultCharset());
                    if(MazeActivity.bluetoothConnectionServices!=null){
                        MazeActivity.bluetoothConnectionServices.write(bytes);
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
                    if(MazeActivity.bluetoothConnectionServices!=null){
                        MazeActivity.bluetoothConnectionServices.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
                }
                else{
                    toast =  Toast.makeText(Settings.this,"text",Toast.LENGTH_SHORT);
                    toast.setText("Please connect to a device");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.justp:
                if(Bluetooth.mBTDevice!= null){
                    String explore = "+P;";
                    byte[] bytes = explore.getBytes(Charset.defaultCharset());
                    if(MazeActivity.bluetoothConnectionServices!=null){
                        MazeActivity.bluetoothConnectionServices.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
                }
                else{
                    toast =  Toast.makeText(Settings.this,"text",Toast.LENGTH_SHORT);
                    toast.setText("Please connect to a device");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.reset:
                break;
            case R.id.stop:
                if(Bluetooth.mBTDevice!= null){
                    String explore = "+S";
                    byte[] bytes = explore.getBytes(Charset.defaultCharset());
                    if(MazeActivity.bluetoothConnectionServices!=null){
                        MazeActivity.bluetoothConnectionServices.write(bytes);
                    }
                    else{
                        Bluetooth.mBluetoothConnection.write(bytes);
                    }
                }
                else{
                    toast =  Toast.makeText(Settings.this,"text",Toast.LENGTH_SHORT);
                    toast.setText("Please connect to a device");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }



}
