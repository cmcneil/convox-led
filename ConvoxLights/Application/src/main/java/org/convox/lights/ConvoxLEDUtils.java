package org.convox.lights;

//import org.convox.common.logger.Log;
import android.util.Log;

import org.convox.lights.ConvoxLed.ConvoxLightConfig;
import org.convox.lights.ConvoxLed.ConvoxLightConfig.Color;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by carson on 5/9/15.
 */
public class ConvoxLEDUtils {
    private static String serverIP = "192.168.1.124";
//    private static String serverIP = "10.0.2.2"; // Host machine IP.
//    private static String serverIP = "192.168.1.134";
    private static int serverPort = 666;
    private static final int NUM_THREADS = 10;
    private static DatagramSocket socket;
    private static final String TAG = "CONVOX_LED";

    private static DatagramSocket getSocketInstance() {
        if (socket == null) {
            try {
                socket = new DatagramSocket();
            } catch (Exception e) {
                Log.i("CONVOX_LED", e.getMessage());
            }
        }
        return socket;
    }

    public static class UDPLightPackTask implements Runnable {

        private ConvoxLightConfig packet;

        public UDPLightPackTask(ConvoxLightConfig packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                byte[] message = packet.toByteArray();
                int messageLength = message.length;
                InetAddress server = InetAddress.getByName(serverIP);
                DatagramPacket datagramPacket = new DatagramPacket(
                        message, messageLength, server, serverPort);
                getSocketInstance().send(datagramPacket);
            } catch (Exception e) {
                Log.i(TAG, e.getMessage());
            }
        }
    }

    public static class FireAndForgetExecutor {
        private static Executor executor = Executors.newFixedThreadPool(NUM_THREADS);
        public static void exec(Runnable command) {
            executor.execute(command);
        }
    }

    public static void pushLedPacket(int r, int g, int b) {
        Color color = Color.newBuilder().setColorSpace(Color.ColorSpace.RGB)
                .addCoordinates(r)
                .addCoordinates(g)
                .addCoordinates(b).build();
        ConvoxLightConfig lightRequest = ConvoxLightConfig.newBuilder()
                .addColors(color)
                .setPeriod(4000)
                .setTransitionSteps(200)
                .setCircleCompression(0.5f)
                .build();
        FireAndForgetExecutor.exec(new UDPLightPackTask(lightRequest));
    }

    public static void fillLights(int r, int g, int b) {
        Color color = Color.newBuilder().setColorSpace(Color.ColorSpace.RGB)
                .addCoordinates(r)
                .addCoordinates(g)
                .addCoordinates(b).build();
        ConvoxLightConfig lightConfig = ConvoxLightConfig.newBuilder()
                .addColors(color)
                .setPeriod(4000)
                .setTransitionSteps(2000)
                .setCircleCompression(1.0f)
                .build();
        FireAndForgetExecutor.exec(new UDPLightPackTask(lightConfig));
    }

    public static void sendConfig(float[][] colors) {
        ConvoxLightConfig.Builder lightConfigBuilder = ConvoxLightConfig.newBuilder();
        for (float[] color : colors) {
            if (color.length < 3) {
                Log.e(TAG, "MALFORMED COLOR!");
                return;
            }
            int[] rgb = hsvToRgb(color[0] / 360f, color[1], color[2]);
            Color colorProto = Color.newBuilder().setColorSpace(Color.ColorSpace.RGB)
                    .addCoordinates(rgb[0])
                    .addCoordinates(rgb[1])
                    .addCoordinates(rgb[2]).build();
            lightConfigBuilder.addColors(colorProto);
        }
        lightConfigBuilder.setPeriod(2000)
                .setTransitionSteps(2000)
                .setCircleCompression(1.0f);
        Log.d(TAG, "Config sent: " + lightConfigBuilder.build());
        FireAndForgetExecutor.exec(new UDPLightPackTask(lightConfigBuilder.build()));
    }

    public static void fillLights(float h, float s, float v) {
        // The hue that we get from the ColorPicker library is in degrees (out of 360). I'm going
        // to fix that library, but until then, we have to normalize here.
        int[] rgb = hsvToRgb(h / 360f, s, v);
        fillLights(rgb[0], rgb[1], rgb[2]);
    }

    public static int[] hsvToRgb(float hue, float saturation, float value) {
        int h = (int)(hue * 6) % 6;
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h) {
            case 0: return rgbToArray(value, t, p);
            case 1: return rgbToArray(q, value, p);
            case 2: return rgbToArray(p, value, t);
            case 3: return rgbToArray(p, q, value);
            case 4: return rgbToArray(t, p, value);
            case 5: return rgbToArray(value, p, q);
            default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
        }
    }

    public static int[] rgbToArray(float r, float g, float b) {
        int[] color = new int[3];
        color[0] = (int)(r * 255);
        color[1] = (int)(g * 255);
        color[2] = (int)(b * 255);
        return color;
    }
}
