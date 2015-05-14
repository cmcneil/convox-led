package org.convox.lights;

import org.convox.common.logger.Log;
import org.convox.lights.ConvoxLed.ConvoxLightConfig;
import org.convox.lights.ConvoxLed.ConvoxLightConfig.Color;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by carson on 5/9/15.
 */
public class ConvoxLEDUtils {
    private static String serverIP = "192.168.1.124";
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
                InetAddress server = InetAddress.getByName(serverIP);
                DatagramPacket datagramPacket = new DatagramPacket(
                        message, messageLength, server, serverPort);
                socket.send(datagramPacket);
            } catch (Exception e) {
                Log.d("CONVOX_LED_NETWORK", e.getMessage());
            }
        }
    }

    public static class FireAndForgetExecutor {
        private static Executor executor = Executors.newFixedThreadPool(NUM_THREADS);
        public static void exec(Runnable command) {
            executor.execute(command);
        }
    }

    public static void pushLedPacket(int h, int s, int v) {
        Color color = Color.newBuilder().setColorSpace(Color.ColorSpace.HSV)
                .addCoordinates(h)
                .addCoordinates(s)
                .addCoordinates(v).build();
        ConvoxLightConfig lightRequest = ConvoxLightConfig.newBuilder()
                .addColor(color)
                .build();
        FireAndForgetExecutor.exec(new UDPLightPackTask(lightRequest));

        // Thread pool or threads?
        // new Thread(new UDPLightPackTask(lightRequest)).start();
    }
}
