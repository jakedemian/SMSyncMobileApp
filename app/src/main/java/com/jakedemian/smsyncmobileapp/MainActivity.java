package com.jakedemian.smsyncmobileapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import static android.Manifest.permission_group.SMS;
import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {
    private String realAddress = "http://smsync-web-server-app-smsync-web-server.44fs.preview.openshiftapps.com";
    private String testAddress = "http://10.4.75.224:8080";
    private static Socket mSocket;
    {
        try{
            mSocket = IO.socket(this.testAddress);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("connected!");
        }
    };

    private Emitter.Listener onRelayMsgToApp = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String msg = "";
                    String to = "";
                    try {
                        msg = data.getString("msg");
                        to = data.getString("to");
                    } catch (JSONException e) {
                        System.out.println("Couldn't get message...");
                    }

                    System.out.println("message received:  " + msg + " -- send to --> " + to);

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(to, null, msg, null, null);
                }
            });
        }
    };

    public static void messageReceived(String msg, String from){
        System.out.println(msg + ":  from  :" + from);
        mSocket.emit("msgToClient", "{\"msg\":\"" + msg + "\", \"from\":\"" + from + "\"}");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSocket.connect();
        mSocket.on("connect", onConnect);
        mSocket.on("relayMsgToApp", onRelayMsgToApp);
    }
}
