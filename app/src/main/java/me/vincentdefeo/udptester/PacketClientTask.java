package me.vincentdefeo.udptester;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ghzmdr on 30/04/15.
 */

public class PacketClientTask extends AsyncTask<Void, Void, String>
{
    protected static boolean alreadyConnected = false, justConnected;

    private String message = "UDP Tester";

    private String hostname;
    private int port, localPort = 0;
    private String remoteMsg, localMsg;

    private MainActivity parentActivity;

    DatagramSocket socket = null;

    private boolean receive = false, send = false;


    @Override
    protected void onCancelled()
    {
        reset();
    }

    private void reset()
    {
        if (socket != null) socket.close();
        alreadyConnected = false;
    }

    PacketClientTask(String hostname, int port, @Nullable Integer localPort, MainActivity parentActivity, @Nullable String message, boolean send, boolean receive)
    {
        this.hostname = hostname;
        this.port = port;
        if (localPort != null) this.localPort = localPort;

        if (message != null) this.message = message;
        this.parentActivity = parentActivity;
        this.receive = receive;
        this.send = send;
    }

    @Override
    protected String doInBackground(Void... params)
    {
        String payload = null;
        try {

            if (localPort != 0) socket = new DatagramSocket(localPort);
            else socket = new DatagramSocket();

            socket.connect(InetAddress.getByName(hostname), port);

            if (!alreadyConnected && send)
            {
                byte[] requestBuffer = message.getBytes();
                DatagramPacket connectionPacket = new DatagramPacket(requestBuffer, requestBuffer.length);
                Log.d("TEST THIS", "" + socket.isConnected());
                socket.send(connectionPacket);
                alreadyConnected = true;
                justConnected = true;
            }

            if (receive)
            {
                byte[] recBuffer = new byte[3000];
                DatagramPacket packet = new DatagramPacket(recBuffer, recBuffer.length);
                socket.receive(packet);

                byte[] dataBuffer = new byte[packet.getLength()];
                System.arraycopy(recBuffer, 0, dataBuffer, 0, packet.getLength());
                payload = new String(dataBuffer, "UTF-8");
            }
            logSocketData(socket);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
            return payload;
        }
    }

    private void logSocketData(DatagramSocket socket)
    {
        remoteMsg = "REMOTE:\n" + (socket.isConnected() ? "" : "NOT " + "CONNECTED ") + socket.getInetAddress() + " : " + socket.getPort();
        localMsg = "\nLOCAL:\n" + (socket.isBound() ? "" : "NOT " + "BOUND") + socket.getLocalAddress() + " : " + socket.getLocalPort();

        Log.d("PACKET", remoteMsg);
        Log.d("PACKET", localMsg);
    }

    @Override
    protected void onPostExecute(String payload)
    {
        super.onPostExecute(payload);


        if (justConnected)
        new AlertDialog.Builder(parentActivity)
                .setTitle("SOCKET INFO")
                .setMessage(remoteMsg + "\n" + localMsg)
                .setCancelable(true)
                .create()
                .show();

        SnackbarManager.show(
                Snackbar.with(parentActivity)
                        .text((justConnected ? (!receive ? "Sent" : "Connected") : (payload != null ? "Packet received" : "Socket Error")))
        );

        if (receive) {
            parentActivity.showResult(payload != null ? payload : "Error retrieving response");
            if (payload != null) justConnected = false;
        }
    }
}
