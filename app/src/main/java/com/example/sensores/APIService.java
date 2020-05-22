package com.example.sensores;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

public class APIService extends Service {
    private RequestQueue requestQueue;
    private HandlerThread handlerThread;
    private Handler handler;



    private final int TEMPO_ENTRE_POSTS_SEGUNDOS = 30;

    @Override
    public void onCreate() {
        Log.d("APIService","onCreate");

        handlerThread = new HandlerThread("HandlerThread");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("APIService","onStart");

        //Previne que seja executado em subsequentes chamadas a onStartCommand
        if(!handlerThread.isAlive()) {
            Log.d("APIService","Posts iniciadas");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    SEND();
                    System.out.println("RODOU!!!!!!!!!");
                    handler.postDelayed(this, 1000 * TEMPO_ENTRE_POSTS_SEGUNDOS);
                }
            };
            handler.post(runnable);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("APIService","POSTS terminados");
        handlerThread.quit();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String jsonBuilder(){
        return "{'Cidade': 'Guarulhos'," +
                "'Estado': 'SP'," +
                "'Bairro': 'Jd dos Cardoso'," +
                "'Rua': 'Rua Seis'," +
                "'Pais': 'Brasil'," +
                "'Latitude': '"+ Sensores.Latitude+"'," +
                "'Longitude': '"+ Sensores.Longitude+"'," +
                "'Luminosidade': '"+ Sensores.Luminosidade+"'," +
                "'Temperatura': '25'," +
                "'Umidade': '48'," +
                "'Proximidade': '"+ Sensores.Proximidade+"'}";
    }

    public void SEND(){
        String json = jsonBuilder();
        Submit(json);
    }

    private void Submit(String data)
    {
        final String savedata= data;
        String URL="https://sensoresapi.azurewebsites.net/api/Sensores";

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject objres=new JSONObject(response);
                    Toast.makeText(getApplicationContext(),objres.toString(),Toast.LENGTH_LONG).show();


                } catch (JSONException e) {


                }
                //Log.i("VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                //Log.v("VOLLEY", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return savedata == null ? null : savedata.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    //Log.v("Unsupported Encoding while trying to get the bytes", data);
                    return null;
                }
            }

        };
        requestQueue.add(stringRequest);
    }

}
