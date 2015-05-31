package org.convox.lights;

import org.convox.common.logger.Log;
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
    private static String luciferIP = "192.168.1.124";
    private static String serverIP = "10.0.2.2"; // Host machine IP.
    private static int serverPort = 666;
    private static final int NUM_THREADS = 10;

    public static class UDPLightPackTask implements Runnable {

        private ConvoxLightConfig packet;

        public UDPLightPackTask(ConvoxLightConfig packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                // Send to server.
                DatagramSocket socket = new DatagramSocket();
                byte[] message = packet.toByteArray();
                int messageLength = message.length;
                InetAddress server = InetAddress.getByName(luciferIP);
                DatagramPacket datagramPacket = new DatagramPacket(
                        message, messageLength, server, serverPort);
                socket.send(datagramPacket);
                Log.i("CONVOX_LED", "server: " + server + ", data: " + datagramPacket);
            } catch (Exception e) {
                Log.i("CONVOX_LED_NETWORK", e.getMessage());
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

        // Thread pool or threads?
        // new Thread(new UDPLightPackTask(lightRequest)).start();
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
                .build();
        FireAndForgetExecutor.exec(new UDPLightPackTask(lightConfig));
    }

    public static void fillLights(float h, float s, float v) {
        int[] rgb = hsvToRgb(h, s, v);
        fillLights(rgb[0], rgb[1], rgb[2]);
    }

    public static int[] hsvToRgb(float hue, float saturation, float value) {
        // The hue that we get from the ColorPicker library is in degrees (out of 360). I'm going
        // to fix that library, but until then, we have to normalize here.
        Log.i("CONVOX_LED", "Selected color: h:" + hue + ", s:" + saturation + ", v:" + value);
        int h = (int)(hue / 360f * 6);
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
        color[0] = (int)(r * 256);
        color[1] = (int)(g * 256);
        color[2] = (int)(b * 256);
        return color;
    }
}
