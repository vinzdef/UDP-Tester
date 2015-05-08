package me.vincentdefeo.udptester;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity
{

    public final static String TAG = "UDP_SENDER";

    @InjectView(R.id.server_host) EditText serverHostET;
    @InjectView(R.id.server_port) EditText serverPortET;
    @InjectView(R.id.local_port) EditText localPortET;
    @InjectView(R.id.message) EditText messageET;

    @InjectView(R.id.response_data) TextView responseText;

    @InjectView(R.id.reset_fab)
    FloatingActionButton resetFab;

    @InjectView(R.id.fab_menu)
    FloatingActionsMenu fabMenu;

    @InjectView(R.id.status) TextView statusText;

    PacketClientTask pct;
    private boolean recieve = false, send = false;

    private boolean resetted = false;

    @OnClick(R.id.send_recieve_fab)
    public void sendAndReceive()
    {
        send = true;
        recieve = true;

        startUDP();
        statusText.setText("LISTENING FOR INCOMING PACKETS");
    }

    @OnClick(R.id.send_fab)
    public void send()
    {
        send = true;
        recieve = false;

        startUDP();
        statusText.setText("SENDING");
    }


    @OnClick(R.id.recieve_fab)
    public void recieve()
    {
        send = false;
        recieve = true;

        startUDP();
        statusText.setText("LISTENING FOR INCOMING PACKETS");
    }

    private void startUDP()
    {
        resetted = false;
        fabMenu.collapse();

        String requiredMsg = "These fields are required:\n";
        boolean invalidForm = false;

        if (serverHostET.getText().toString().equals("")) {
            requiredMsg += "\nHOSTNAME";
            invalidForm = true;
        }

        if (serverPortET.getText().toString().equals("")) {
            requiredMsg += "\nHOST PORT";
            invalidForm = true;
        }

        if (invalidForm) {
            new AlertDialog.Builder(this).setTitle("Invalid Parameters").setMessage(requiredMsg).create().show();
            return;
        }

        if (recieve) {
            fabMenu.setVisibility(View.GONE);
            resetFab.setVisibility(View.VISIBLE);
        }

        String serverHost = serverHostET.getText().toString();
        int serverPort = Integer.parseInt(serverPortET.getText().toString());

        Integer localPort = null;

        try {
            localPort = Integer.parseInt(localPortET.getText().toString());
        } catch (Exception e){

        }

        String message = null;
        if (messageET.getText().toString() != "")
            message = messageET.getText().toString();

        pct = new PacketClientTask(serverHost, serverPort, localPort, MainActivity.this, message, send, recieve);
        pct.execute();
    }

    @OnClick(R.id.reset_fab)
    public void resetConnection()
    {
        resetted = true;
        pct.cancel(true);

        resetFab.setVisibility(View.GONE);
        fabMenu.setVisibility(View.VISIBLE);

        recieve = false;
        send = false;

        statusText.setText("IDLE");
    }

    protected void showResult(String data)
    {
        if (recieve) responseText.setText(data);

        if (!(send && recieve))
            PacketClientTask.alreadyConnected = false;

        if (!resetted && recieve)
            sendAndReceive();

        Log.d(TAG, "DATA: " + data);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        resetFab.setVisibility(View.GONE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009688")));
    }
/*

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
}
