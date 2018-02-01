package com.techdev_studio.funtain;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.techdev_studio.funtain.tools.shake_listener;

import org.json.JSONObject;

public class GameActivity extends AppCompatActivity {

    private shake_listener mShaker;
    RequestQueue queue;
    TextView tv_count;
    private int play = 0;
    private int user_id = 0 ;
    private int single=0;

    String url_single ="http://192.168.100.1/svc_funtain.php?metodo=shakeSingle&user_id=";
    String url_multi ="http://192.168.100.1/svc_funtain.php?metodo=shakeMulti&user_id=";//{1}&value={2}
    String url_logout = "http://192.168.100.1/svc_funtain.php?metodo=desconectar&user_id=";

    String final_url = "";
    float _x = 0;
    float _y = 0;
    float _z = 0;

    int _acel = 0;

    Button btn_salir;
    boolean full_fn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        user_id = intent.getExtras().getInt("user_id");
        single = intent.getExtras().getInt("single");

        if(single==1)
        {
            final_url = url_single + user_id + "&value=";
        }
        else
        {
            final_url = url_multi + user_id + "&value=";
        }


        tv_count=(TextView)findViewById(R.id.TV_countdown);
        btn_salir=(Button) findViewById(R.id.BT_salir);

        btn_salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mShaker = new shake_listener(this);
        mShaker.setOnShakeListener(new shake_listener.OnShakeListener() {


            @Override
            public void onShake() {
                //vibe.vibrate(500);
                //sendValue();
                assignVals();
            }
        });
        queue = Volley.newRequestQueue(this);
        startLoop();
    }

    private void assignVals(){
        _x= mShaker.getmLastX();
        _y= mShaker.getmLastY();
        _z= mShaker.getmLastZ();

        _acel = Math.round(100*Math.abs(_y)/1000);
    }
    private void startLoop()
    {
        //final Vibrator vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        //vibe.vibrate(500);
        CountDownTimer timer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_count.setText(Math.round(3 - (millisUntilFinished/1000)));
                //sendValue();
            }

            @Override
            public void onFinish() {
                //Toast.makeText(GameActivity.this, "Finished", Toast.LENGTH_SHORT).show();
                tv_count.setText("SHAKE IT!");
                mainLoop();
            }
        }.start();
    }

    private void mainLoop()
    {
        final Handler mHandler = new Handler();
        new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    while (true) {
                        try {
                            Thread.sleep(500);
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    // Write your code here to update the UI.
                                    sendValue();
                                }
                            });
                        } catch (Exception e) {
                            // TODO: handle exception
                            onBackPressed();
                        }
                    }
                }
        }).start();
    }


    @Override
    public void onResume()
    {
        mShaker.resume();
        super.onResume();
    }

    @Override
    public void onPause()
    {
        //mShaker.pause();
        onBackPressed();
        super.onPause();
    }

    private void sendValue()
    {
        if(full_fn)
        {
            if (checkConnected())
            {
                int accel = _acel;
                writeLog("acel " + accel);
                String _url = final_url + accel;
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.GET, _url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                writeLog("Response: " + response.toString());
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub

                            }
                        });

                queue.add(jsObjRequest);
            }
            else
            {
                Toast.makeText(GameActivity.this, "El dispositivo ha sido desconectado!", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }
        else
        {

        }

    }

    public boolean checkConnected()
    {
        String networkSSID = "FUNTAIN_NET";
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
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private void desconectar()
    {
        String _url = url_logout + user_id;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, _url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        writeLog("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        queue.add(jsObjRequest);
    }

    @Override
    public void onBackPressed() {
        desconectar();
        mShaker.pause();
        super.onBackPressed();
    }

    private void writeLog(String message)
    {
        Log.e("log-LoadActivity",message);
    }
}
