package sinha.deepankar.nix.networkz;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Author: Deepankar Sinha
 * Created on: 13-10-2016
 */

public class WifiNetworkz {

    private WifiConfiguration netConfig;
    private Context context;
    private WifiManager wifiManager;
    private boolean debug = false;




    WifiNetworkz(Context context) {
        wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        this.context=context;
    }

    public boolean isApOn(){
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        }
        catch (Throwable e) {
            if(debug)
                Log.e("WifiNetworkz","Could not get wifi status. Error: "+e);
        }
        return false;
    }

    public boolean createAP(String SSID, String passWord){
        Method[] mMethods = wifiManager.getClass().getDeclaredMethods();

        for(Method mMethod: mMethods){

            if(mMethod.getName().equals("setWifiApEnabled")) {
                netConfig = new WifiConfiguration();
                if(passWord==""){
                    netConfig.SSID = SSID;
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                }else{
                    netConfig.SSID = SSID ;
                    netConfig.preSharedKey = passWord;
                    netConfig.hiddenSSID = true;
                    netConfig.status = WifiConfiguration.Status.ENABLED;
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                }
                try {
                    mMethod.invoke(wifiManager, netConfig,true);
                    wifiManager.saveConfiguration();
                    return true;

                } catch (Exception e) {
                    if(debug){
                        Log.e("WifiNetworkz","Could not start AP. Error: "+e.getMessage());
                    }
                }
            }
        }
        return false;
    }

    public boolean stopAP(){
        Method method;
        try {
            method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, netConfig, false);
            return true;
        } catch (Exception e) {
            if(debug)
                Log.e("WifiNetworkz","Could not stop AP. Error: "+e.toString());
        }
        return false;
    }

    public boolean connectToAP(String ssid, String password){
        if(!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
        List<ScanResult> list = wifiManager.getScanResults();
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";

        boolean noResult = true;

        for( ScanResult i : list ) {
            if(i.SSID != null && i.SSID.equals(ssid)) {
                String Capabilities =  i.capabilities;
                Log.d ("WifiNetworkz", i.SSID + " capabilities : " + Capabilities);

                if (Capabilities.contains("WPA2") || Capabilities.contains("WPA")) {
                    conf.preSharedKey = "\"" + password + "\"";
                }
                else if (Capabilities.contains("WEP")) {
                    conf.wepKeys[0] = "\"" + password + "\"";
                    conf.wepTxKeyIndex = 0;
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                }else{
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                }
                wifiManager.addNetwork(conf);
                noResult = false;
                break;
            }
        }

        if(noResult) {
            return false;
        }

        List<WifiConfiguration> Conf_list = wifiManager.getConfiguredNetworks();
        String currentSSID;
        for( WifiConfiguration i : Conf_list )
            if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.disconnect();
                do {
                    wifiManager.enableNetwork(i.networkId, true);
//                    wifiManager.reconnect();
                    WifiInfo wi = wifiManager.getConnectionInfo();
                    currentSSID = wi.getSSID();
                } while (!currentSSID.equals("\"" + ssid + "\""));

                return true;
            }
        return false;
    }

    public void debug(boolean bool){
        debug=bool;
    }

}
