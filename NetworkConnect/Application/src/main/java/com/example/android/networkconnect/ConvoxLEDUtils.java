package com.example.android.networkconnect;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.android.common.logger.Log;
import com.example.android.networkconnect.ConvoxLed.ConvoxLightPacket;
import com.example.android.networkconnect.ConvoxLed.ConvoxLightPacket.Color;
import com.google.protobuf.Message;

/**
 * Created by carson on 5/9/15.
 */
public class ConvoxLEDUtils {
    private static String luciferURI = "192.168.1.124:666";

    public static void pushLight(Context context, int h, int s, int v) {
        RequestHandler handler = RequestHandler.getRequestHandler(context);
        Color color = Color.newBuilder().setColorSpace(Color.ColorSpace.HSV)
                .addCoordinates(h)
                .addCoordinates(s)
                .addCoordinates(v).build();
        ConvoxLightPacket lightRequest = ConvoxLightPacket.newBuilder()
                .setColor(color)
                .build();

        ProtoBufRequest<ConvoxLightPacket, Message> request =
                new ProtoBufRequest<>(Request.Method.POST, luciferURI,
                        lightRequest, null,
                        new Response.Listener<Message>() {
                            @Override
                            public void onResponse(Message response) {
                                // Don't do anything.
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("NetworkRequest", "Failed HTTP Reques!");
                    }
                });
        handler.sendRequest(request);
    }
}
