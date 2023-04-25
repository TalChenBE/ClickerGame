package com.example.clickergame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
    private boolean isConnected = false;
    private Context context;

    public NetworkBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Protocol Description Unit
        this.context = context;
        String status = "Not connected to Internet";
        boolean checkInternet = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] activeNetwork = cm.getAllNetworkInfo();
        if (activeNetwork != null) {
            for (NetworkInfo networkInfo : activeNetwork) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    status = "Internet connection available";
                    checkInternet = true;
                    break;
                }
            }
        }
        if (checkInternet){
            this.isConnected = true;
        } else {
            this.isConnected = false;
            tryConnection();
        }
    }

    private void tryConnection() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setTitle("No internet Connection");
            builder.setMessage("Internet not available?");
            builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (!isConnected)
                        tryConnection();
                }
            });
            builder.setNegativeButton("Quit game", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    System.exit(0);
                }
            });
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
