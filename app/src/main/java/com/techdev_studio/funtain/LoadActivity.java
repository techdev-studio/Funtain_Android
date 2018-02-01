package com.techdev_studio.funtain;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadActivity extends AppCompatActivity {

    private String networkSSID = "FUNTAIN_NET";
    private String networkPass = "tds_funtain";

    boolean full_fn = false;
    View rootview=null;
    private String mac_adr = "0.0.0.0";
    private String ip_adr = "aa:aa:aa:aa";
    RequestQueue queue;

    private int intentos = 0;
    Button btn_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        //check wifi enabled
        //try connect if enabled
        //check connection if not prompt retry
        //if connected start second activity
        rootview = getWindow().getDecorView().getRootView();
        btn_connect=(Button)findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpCon();
            }
        });


        getMacIp();

        CountDownTimer timer = new CountDownTimer(2000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Toast.makeText(LoadActivity.this, "Buscando FUNTAIN cercana.", Toast.LENGTH_SHORT).show();
                btn_connect.setVisibility(View.VISIBLE);
                if (full_fn)
                {
                    setUpCon();
                }
                else
                {
                    Intent intent = new Intent(LoadActivity.this,InteractionActivity.class);
                    intent.putExtra("user_id", 100);
                    startActivity(intent);
                    finish();
                }
            }
        }.start();

        queue = Volley.newRequestQueue(this);
    }

    private void getMacIp()
    {
        try
        {
            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            mac_adr = info.getMacAddress();

            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            ip_adr = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        }
        catch (Exception ex)
        {
            writeLog(ex.toString());
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        intentos++;
    }

    private void setUpCon()
    {
        writeLog("intentos" + intentos);
        if(checkWifi())
        {
            if (checkConnected())
            {
                if(mac_adr.isEmpty() || ip_adr.isEmpty())
                {
                    getMacIp();
                }
                registerFuntain(mac_adr,ip_adr);
            }
            else
            {
                if(intentos<5)
                {
                    CountDownTimer timer = new CountDownTimer(5000, 5000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            connect_to_funtain();
                        }

                        @Override
                        public void onFinish() {
                            setUpCon();
                        }
                    }.start();
                }
                else
                {
                    cannotConnectDialog();
                }
            }

        }
        else
        {
            promptUserWIfi();
        }
    }

    private boolean checkWifi()
    {
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled())
        {
            //wifi is enabled
            //try connect to funtain
            return true;
        }
        else
        {
            return false;
        }
    }
    private void connect_to_funtain()
    {
        intentos++;
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.preSharedKey = "\""+ networkPass +"\"";

        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                Toast.makeText(LoadActivity.this, "Funtain Hallada, conectando!", Toast.LENGTH_SHORT).show();
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                break;
            }
        }
    }

    public boolean checkConnected()
    {
        writeLog("check con");
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;

        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            String ssid = wifiInfo.getSSID();
//            WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
//            WifiInfo info = wifiManager.getConnectionInfo ();
//            String ssid  = info.getSSID();
            writeLog(ssid);
            if (ssid.equals("\"" + networkSSID + "\""))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private void cannotConnectDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Funtain no encontrado :(");
        builder.setMessage("No es posible conectar! \nAsegúrate de estar cerca de un dispositivo FUNTAIN!")
                .setPositiveButton("Retry?", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        intentos=0;
                        setUpCon();
                    }
                })
                .setNegativeButton("Cerrar App", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        finish();
                    }
                });
        // Create the AlertDialog object and return it
        //builder.create();
        builder.show();
    }

    private void promptUserWIfi()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("WiFi no encendido");
        builder.setMessage("Por favor activa el WiFi de tu dispositivo para poder conectarte a FUNTAIN.")
                .setPositiveButton("Config. WIFI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Cerrar App", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        finish();
                    }
                });
        // Create the AlertDialog object and return it
        builder.show();
    }

    private void registerFuntain(final String mac, final String ip)
    {

        String url= "http://192.168.100.1/svc_funtain.php?metodo=registrar";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        valReg(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("mac", mac);
                params.put("ip", ip);


                return params;
            }
        };
        queue.add(postRequest);
    }

    private void valReg(String response)
    {
        boolean _continue = false;
        int user_id = 0;
        try {
            JSONObject jobject = new JSONObject(response);
            user_id=jobject.getInt("user_id");
            _continue=true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(_continue)
        {
            Toast.makeText(LoadActivity.this, "Conectado a FUNTAIN!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoadActivity.this,InteractionActivity.class);
            intent.putExtra("user_id", user_id);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(LoadActivity.this, "ERROR AL CONECTAR!", Toast.LENGTH_SHORT).show();
        }

    }

    private void writeLog(String message)
    {
        Log.e("log-LoadActivity",message);
    }
}
