package me.vincentdefeo.udptester;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by ghzmdr on 30/04/15.
 */

public class PacketClientTask extends AsyncTask<Void, Void, Boolean>
{
    protected static boolean alreadyConnected = false, justConnected = true;

    private String message = "UDP Tester";
    private String response;

    private String host;
    private int port;
    private String remoteMsg, localMsg;

    private MainActivity parentActivity;

    DatagramSocket socket = null;


    @Override
    protected void onCancelled()
    {
        socket.close();
    }

    PacketClientTask(String host, int port, MainActivity parentActivity, String message)
    {
        super();

        this.host= host;
        this.port = port;

        this.message = message;
        this.parentActivity = parentActivity;
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        try {
            InetAddress hostAddr = InetAddress.getByName(host);


            try {
                socket = new DatagramSocket(8080);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            socket.connect(hostAddr, port);

            if (!alreadyConnected)
            {
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, message.length(), hostAddr, port);
                logSocketData(socket);
                socket.send(packet);
                alreadyConnected = true;
                justConnected = true;
            }

            byte[] recBuffer = new byte[200];
            DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
            socket.receive(recPacket);
            byte[] finalData = new byte[recPacket.getLength()];
            System.arraycopy(recBuffer, 0, finalData, 0, recPacket.getLength());

            response = new String(finalData, "UTF-8");

        } catch (IOException  e) {
            e.printStackTrace();
            alreadyConnected = false;
            return false;
        } finally {
            socket.close();
        }

        return true;
    }

    private void logSocketData(DatagramSocket socket)
    {
        remoteMsg = "REMOTE:\n" + (socket.isConnected() ? "" : "NOT " + "CONNECTED ") + socket.getInetAddress() + " : " + socket.getPort();
        localMsg = "\nLOCAL:\n" + (socket.isBound() ? "" : "NOT " + "BOUND") + socket.getLocalAddress() + " : " + socket.getLocalPort();

        Log.d("PACKET", remoteMsg);
        Log.d("PACKET", localMsg);
    }

    @Override
    protected void onPostExecute(Boolean success)
    {
        super.onPostExecute(success);

        if (justConnected)
        {
            new AlertDialog.Builder(parentActivity)
                    .setTitle("SOCKET INFO")
                    .setMessage(remoteMsg + "\n" + localMsg)
                    .setCancelable(true)
                    .create()
                    .show();
        }

        if (success)
        {
            SnackbarManager.show(
                    Snackbar.with(parentActivity)
                            .text((justConnected ? "Connected" : (success ? "Packet recieved" : "Socket Error")))
            );


            parentActivity.showResult(response != null ? response : "Error retrieving response");
        }

        if (success) justConnected = false;
    }
}
