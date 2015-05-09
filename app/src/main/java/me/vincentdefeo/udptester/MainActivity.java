package me.vincentdefeo.udptester;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.EventListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity
{
    public final static String TAG = "UDP_SENDER";

    @InjectView(R.id.status)        TextView statusText;

    @InjectView(R.id.reset_fab)     FloatingActionButton resetFab;
    @InjectView(R.id.fab_menu)      FloatingActionsMenu fabMenu;

    @InjectView(R.id.local_port)    EditText localPortET;
    @InjectView(R.id.message)       EditText messageET;


    @InjectView(R.id.server_host)   EditText serverHostET;
    @InjectView(R.id.server_port)   EditText serverPortET;

    @InjectView(R.id.response_data) TextView responseText;

    private PacketClientTask pct;
    private boolean receive = false, send = false, resetted = false;
    private boolean buttonAlreadyAnimated = false;

    @OnClick(R.id.send_receive_fab)
    public void sendAndReceive()
    {
        send = true;
        receive = true;

        startUDP();
        statusText.setText("SENDING");
    }

    @OnClick(R.id.send_fab)
    public void send()
    {
        send = true;
        receive = false;

        startUDP();
        statusText.setText("SENDING");
    }


    @OnClick(R.id.receive_fab)
    public void recieve()
    {
        send = false;
        receive = true;

        startUDP();
        statusText.setText("LISTENING FOR INCOMING PACKETS");
    }

    @OnClick(R.id.reset_fab)
    public void resetConnection()
    {
        resetted = true;
        pct.cancel(true);

        resetFab.setVisibility(View.GONE);
        fabMenu.setVisibility(View.VISIBLE);

        receive = false;
        send = false;

        statusText.setText("IDLE");
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

        if (receive) {
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

        String message = messageET.getText().toString();

        pct = new PacketClientTask(serverHost, serverPort, localPort, MainActivity.this, message, send, receive);
        pct.execute();
    }

    protected void showResult(String data)
    {
        if (receive) {
            responseText.setText(data);

            if (send)
                PacketClientTask.alreadyConnected = false;

            if (!resetted)
                recieve();
        }

        Log.d(TAG, "DATA: " + data);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        resetFab.setVisibility(View.GONE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009688")));

        responseText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = responseText.getText().toString();
                new ResultViewDialog(MainActivity.this, text).show();
            }
        });
    }

    public void showFeedback(boolean justConnected, String remoteMsg, String localMsg, String payload)
    {
        if (justConnected) {
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("SOCKET INFO")
                    .setMessage(remoteMsg + "\n" + localMsg)
                    .setCancelable(true)
                    .create()
                    .show();
        }


        SnackbarManager.show(
                Snackbar.with(this)
                        .text((justConnected ? (!receive ? "Sent" : "Connected") : (payload != null ? "Packet received" : "Socket Error")))
                        .eventListener(new EventListener() {
                            @Override
                            public void onShow(Snackbar snackbar) {
                                if (!buttonAlreadyAnimated) {
                                    resetFab.animate().translationYBy(-100);
                                    fabMenu.animate().translationYBy(-100);
                                    buttonAlreadyAnimated = true;
                                }
                            }

                            @Override
                            public void onShowByReplace(Snackbar snackbar) {

                            }

                            @Override
                            public void onShown(Snackbar snackbar) {

                            }

                            @Override
                            public void onDismiss(Snackbar snackbar) {
                                if (buttonAlreadyAnimated) {
                                    resetFab.animate().translationYBy(100);
                                    fabMenu.animate().translationYBy(100);
                                    buttonAlreadyAnimated = false;
                                }
                            }

                            @Override
                            public void onDismissByReplace(Snackbar snackbar) {

                            }

                            @Override
                            public void onDismissed(Snackbar snackbar) {

                            }
                        }));



        if (receive) {
            showResult(payload != null ? payload : "Error retrieving response");
            if (payload != null) PacketClientTask.justConnected = false;
        } else statusText.setText("IDLE");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            //new InfoDialog(this).show();
            final AlertDialog infoDialog = new AlertDialog.Builder(this)
                    .setTitle("Info")
                    .setMessage(R.string.info_text)


                    .setPositiveButton("See Sources", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://github.com/ghzmdr/UDP-Tester"));
                            startActivity(i);
                        }
                    })
                    .create();
            infoDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NEVERMIND", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    infoDialog.dismiss();
                }
            });
            infoDialog.show();
            ((TextView) infoDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        } else if (id == R.id.action_reset) {
            serverHostET.setText("");
            serverPortET.setText("");
            localPortET.setText("");
            messageET.setText("");
            responseText.setText("");
            /*
            item.setIcon(R.drawable.ic_chevron_up_white_24dp);
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            item.setIcon(R.drawable.ic_delete_white_24dp);
                        }
                    },
                    1000);
            */
        }

        return super.onOptionsItemSelected(item);
    }

}
