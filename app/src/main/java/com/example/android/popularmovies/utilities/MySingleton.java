package com.example.android.popularmovies.utilities;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Baraa Hesham on 3/7/2018.
 */

public class MySingleton {

    private static MySingleton singleton;
    private RequestQueue requestQueue;
    private static Context mContext;


    private MySingleton(Context context) {
        mContext=context;
        requestQueue=getRequestQueue();
    }

    public RequestQueue getRequestQueue(){

        if (requestQueue==null){
            requestQueue= Volley.newRequestQueue(mContext.getApplicationContext());

        }return requestQueue;
    }
public static synchronized MySingleton getInstance(Context context){
        if (singleton==null){
            singleton=new MySingleton(context);

        }return singleton;
}

public<T> void addToRequestQue(Request<T> request){
requestQueue.add(request);
}
}


