package com.techdev_studio.funtain;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class InteractionActivity extends AppCompatActivity {

    private String networkSSID = "FUNTAIN_NET";
    private int g_single = 2;
    private int g_user_id = 0;

    ImageButton btn_single, btn_group;
    RequestQueue queue;

    boolean full_fn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interaction);

        Intent intent = getIntent();
        g_user_id = intent.getExtras().getInt("user_id");

        btn_group=(ImageButton) findViewById(R.id.btn_group);
        btn_single=(ImageButton) findViewById(R.id.btn_single);

        btn_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAsGroup();
            }
        });

        btn_single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAsSingle();
            }
        });

        queue = Volley.newRequestQueue(this);
    }


    private void setAsSingle()
    {
        g_single = 1;
        if(full_fn)
        {
            if(checkConnected())
            {
                interactionFuntain(g_user_id,g_single);
            }
        }
        else
        {
            Intent intent = new Intent(InteractionActivity.this, GameActivity.class);
            intent.putExtra("single",g_single);
            intent.putExtra("user_id", 100);
            startActivity(intent);
            finish();
        }
    }

    private void setAsGroup()
    {
        g_single = 0;
        if(full_fn)
        {
            if(checkConnected())
            {
                interactionFuntain(g_user_id,g_single);
            }
        }
        else
        {
            Intent intent = new Intent(InteractionActivity.this, GameActivity.class);
            intent.putExtra("single",g_single);
            intent.putExtra("user_id", 100);
            startActivity(intent);
            finish();
        }
    }

    private void valSingleReg(String response)
    {
        boolean _success = false;
        try {
            JSONObject jobject = new JSONObject(response);
            _success=jobject.getBoolean("success");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(_success)
        {
                Intent intent = new Intent(InteractionActivity.this, GameActivity.class);
                intent.putExtra("single",g_single);
                intent.putExtra("user_id", g_user_id);
                startActivity(intent);
        }
    }

    private void interactionFuntain(final int user_id, final int single)
    {
        String url= "http://192.168.100.1/svc_funtain.php?metodo=interaction";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        valSingleReg(response);
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
                params.put("user_id", user_id + "");
                params.put("single", single + "");

                return params;
            }
        };
        queue.add(postRequest);
    }

    public boolean checkConnected()
    {
        writeLog("check con");
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;

        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            String ssid = wifiInfo.getSSID();
            writeLog(ssid);
            if (ssid.equals("\"" + networkSSID + "\""))
            {
                return true;
            }
            else
            {
                //disconnected, return
                Toast.makeText(InteractionActivity.this, "Conexión perdida intente de nuevo.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(InteractionActivity.this, LoadActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        }
        else
        {
            //disconnected, return
            Toast.makeText(InteractionActivity.this, "Conexión perdida intente de nuevo.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(InteractionActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
            return false;
        }
    }

    private void writeLog(String message)
    {
        Log.e("log-LoadActivity",message);
    }
}
