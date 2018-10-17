package com.example.xuemin.bluetooth2;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "MYAPP";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;
    private Boolean cannotConnect = false;

    public BluetoothConnectionService(Context context){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try{
            start();
        }
        catch(Exception e){
            Log.d(TAG, "Fail to connect");
        }

    }
    /*This thread runs while listening for incoming connections.
    It behaves like a server-side client. It runs until a connection is accepted(or until cancelled).
    * */
    private class AcceptThread extends Thread{
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try{
                //create a new listening server socket
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);

            }catch (IOException e){
                Log.d(TAG, "AcceptThread: IOException " + e.getMessage());
            }
            mmServerSocket = tmp;

        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running");

            BluetoothSocket socket = null;

            try{
                //This is a blocking call and will only return on a successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection....");
            }
            catch (Exception e){
                Log.d(TAG, "AcceptThread: IOException " + e.getMessage());
            }
            if(socket != null){
                connected(socket, mmDevice);
            }
            Log.i(TAG, "End mAcceptThread ");
        }

        public void cancel(){
            Log.d(TAG, "Canceling AcceptThread.");
            try{
                mmServerSocket.close();
            }catch(IOException e){
                Log.d(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }

    }




    /* This thread runs while attempting to make an outgoing connection
    * with a device. It runs straight through; the connection either suceeds or fails.*/
    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.d(TAG, "RUN mConnectThread");

            //Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " +
                        MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.d(TAG, "ConnectThread:Could not create InsecureRfcommSocket: " +
                        e.getMessage());
            }
            mmSocket = tmp;

            //Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            //Make a connection to the BluetoothSocket
            try {
                //This a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");

            } catch (IOException e) {
                //Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed socket.");

                } catch (IOException e1) {
                    Log.d(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run:ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
                cannotConnect = true;

            }
            try{
                connected(mmSocket, mmDevice);
            }
            catch (Exception e){
                Log.d(TAG, e.getMessage());
            }


        }

        public void cancel(){
            try{
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }
    /* Start the chat service. Specifically start AcceptThread to begin a
    * session in listening(server) mode. Called by the Activity onResume() */
    public synchronized void start(){
        Log.d(TAG, "start");
        try{
            //Cancel any thread attempting to make a connection
            if(mConnectThread != null){
                mConnectThread.cancel();
                //create a new thread
                mConnectThread = null;
            }
            if(mInsecureAcceptThread == null){
                mInsecureAcceptThread = new AcceptThread();
                mInsecureAcceptThread.start();
            }
        }
        catch(Exception e){
            Log.d(TAG, e.getMessage());
        }

    }
    /* AcceptThread starts and sits waiting for a connection.
    * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
    * */
    public void startClient(BluetoothDevice device, UUID uuid){
        if(cannotConnect){
            Log.d(TAG, "startClient: Unable to start.");
        }
        else{
            Log.d(TAG, "startClient: Started.");
            //initprogress dialog
            mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth" ,
                    "Please Wait....", true);

            try{
                mConnectThread = new ConnectThread(device, uuid);
                mConnectThread.start();
            }
            catch(Exception e){
                Log.d(TAG, "failed to start connection");
            }

        }

    }
    /* Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data,
    * and receiveing incoming data through input/output streams respectively. */
    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "ConnectedThread: Starting.");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run(){
            byte[] buffer = new byte[1024]; //buffer store for the stream

            int bytes; //bytes returned from read()
            //keep listening to the InputSream until an exception occurs
            while(true){
                //Read from InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading inputStream." + e.getMessage());
                    //cancel();
                    break;
                }
            }
        }
        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to outputstream. " + e.getMessage());
            }
        }
        /* Call this from the main activity to shutdown the connection */
        public void cancel(){
            try{
                mmSocket.close();
            }
            catch(IOException e){
                Log.e(TAG, "failed to close socket " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: starting.");

        //Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /* Write to the ConnectedThread in an unsynchronized manner
    * @param out The bytes to write
    * @see ConnectedThread#write(byte[]) */
    public void write(byte[] out){
        //Create temporary object
        ConnectedThread r;

        //Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);

    }



}
