package com.example.android.networkconnect;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by carson on 5/10/15.
 */
public final class RequestHandler {
    private static RequestHandler instance = null;
    private RequestQueue mRequestQueue;

    private RequestHandler(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized RequestHandler getRequestHandler(Context context) {
        if(instance == null) {
            instance = new RequestHandler(context);
        }
        return instance;
    }

    public void sendRequest(Request request) {
        mRequestQueue.add(request);
    }
}
