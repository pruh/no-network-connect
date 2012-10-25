package com.example.no_network_connect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    private static final String TAG = MainActivity.class.getSimpleName();

    private WifiManager wm;
    
    private TextView ssidTv;
    private TextView passTv;
    
    private BroadcastReceiver wifiStateChangeReceiver;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        
        ssidTv = (TextView) findViewById(R.id.ssid_edit);
        passTv = (TextView) findViewById(R.id.pass_edit);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        wifiStateChangeReceiver = new BroadcastReceiver() {
            
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkInfo ni = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "Network state changed: " + ni);
                
                WifiInfo wi = wm.getConnectionInfo();
                Log.d(TAG, "Corresponding wifi info: " + wi);
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        
        registerReceiver(wifiStateChangeReceiver, filter);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if (wifiStateChangeReceiver != null) {
            unregisterReceiver(wifiStateChangeReceiver);
            wifiStateChangeReceiver = null;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onConnectClicked(final View view) {
        view.setEnabled(false);
        
        // Android requires this params to be in quotes
        final String ssid = String.format("\"%s\"", ssidTv.getText().toString());
        final String passphrase = String.format("\"%s\"", passTv.getText().toString());
        
        createNetwork(ssid, passphrase);
        connectToNetwork(ssid);
        
        Handler handler = new Handler();
        
        handler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                disableNetwork(ssid);
            }
            
        }, 1000);

        handler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                view.setEnabled(true);
            }
            
        }, 3000);
    }

    private void createNetwork(String ssid, String passphrase) {
        deleteNetworkIfExists(ssid);
        
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = ssid;
        wc.preSharedKey = passphrase;
        wm.addNetwork(wc);
    }

    private void deleteNetworkIfExists(String ssid) {
        for (WifiConfiguration wc : wm.getConfiguredNetworks()) {
            if (ssid.equals(wc.SSID)) {
                wm.removeNetwork(wc.networkId);
                wm.saveConfiguration();
                break;
            }
        }
    }

    private void connectToNetwork(String ssid) {
        for (WifiConfiguration wc : wm.getConfiguredNetworks()) {
            if (ssid.equals(wc.SSID)) {
                Log.d(TAG, "start connecting");
                wm.disconnect();
                wm.enableNetwork(wc.networkId, true);
                wm.reconnect();
                break;
            }
        }
    }

    private void disableNetwork(String ssid) {
        for (WifiConfiguration wc : wm.getConfiguredNetworks()) {
            if (ssid.equals(wc.SSID)) {
                Log.d(TAG, "network disabled");
                wm.disableNetwork(wc.networkId);
                break;
            }
        }
    }
}
