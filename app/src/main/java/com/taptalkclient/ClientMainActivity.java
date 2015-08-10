package com.taptalkclient;

import android.app.Activity;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.taptalkclient.constants.ServiceDiscoveryConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClientMainActivity extends Activity {

    public static final String TAG = "##ClientMainActivity##";

    private Socket clientSocket;
    private  InetAddress serverAddress;
    private boolean sendData = true;
    private int serverPort;
    private TextView textView;
    private Button talkButton;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager mNsdManager;
    private NsdServiceInfo mService;
    private AudioThread mAudioRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // mNsdManager = (NsdManager)getSystemService(Context.NSD_SERVICE);
        textView = (TextView)findViewById(R.id.textView);
        talkButton = (Button)findViewById(R.id.button_talk);
        talkButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    transferData();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    tearDownConnection();
                }
                return true;
            }
        });

        //initializeResolveListener();
        //initializeDiscoveryListener();
    }

    private void transferData(){
        Log.e(TAG, "Data sending started....");
        sendData = true;
        new ClientThread().execute();
    }

    private void tearDownConnection(){
        sendData = false;
        if(mAudioRecorder != null)
            mAudioRecorder.stopIt();
        Log.e(TAG, "Data sending stopped....");
        try {
            if(clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(ServiceDiscoveryConstants.SERVICE_NAME)) {
                    mService = serviceInfo;
                    int port = mService.getPort();
                    InetAddress host = mService.getHost();
                    serverAddress = host;
                    serverPort = port;
                    textView.setText("Service found/nPort: " + port + "/nIP: " + host.getHostAddress());
                }

            }
        };
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (service.getServiceType().equals(ServiceDiscoveryConstants.SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    if (service.getServiceName().contains(ServiceDiscoveryConstants.SERVICE_NAME)) {
                        Log.d(TAG, "Legitimate service found");
                        mNsdManager.resolveService(service, mResolveListener);
                    }
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
        mNsdManager.discoverServices(ServiceDiscoveryConstants.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    class ClientThread extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                clientSocket = new Socket("192.168.1.107", 8855);//serverAddress, serverPort);
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream())), true);
                int i = 1;
                mAudioRecorder = new AudioThread(out);
                //while(sendData){
                 //   out.println(i);
                //    i++;
                //}

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }

}
