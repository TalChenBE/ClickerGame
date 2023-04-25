package com.example.clickergame;

import static com.example.clickergame.Finals.PLAYER_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity implements HomePageClicker.FragHomePageListener {
    private NetworkBroadcastReceiver r = new NetworkBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeTheme();
        setContentView(R.layout.activity_main);
    }

    public void changeTheme(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkTheme = preferences.getBoolean("switch_theme", false);
        if (isDarkTheme)
            setTheme(R.style.Theme_ClickerGame_Dark);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        GameSettings prefFrag = (GameSettings) getSupportFragmentManager().findFragmentByTag("prefFrag");
        if (prefFrag != null)
            return true;
        if (item.getItemId() == R.id.settings) {
            showGameSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showGameSettings() {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(android.R.id.content, new GameSettings(), "prefFrag")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void OnClickJoinGame(String playerName) {
        Bundle bundle = new Bundle();
        bundle.putString(PLAYER_NAME, playerName);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainerView, GameBoardClicker.class, bundle, "GameBoardClicker")
                    .addToBackStack("BBB")
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(r);
        PlayersModel viewModel = new ViewModelProvider(this).get(PlayersModel.class);
        Player player = viewModel.getMyPlayer();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean allowNotification = sharedPreferences.getBoolean("allow_notification", false);
        if (!isMyServiceRunning(ForegroundService.class) && allowNotification){
            Intent intent = new Intent(this,
                    ForegroundService.class);
            intent.setAction("Start Foreground Service");
            startService(intent);
        }
        if (player != null && player.getKey() != null) {
            Gson gson = new Gson();
            String playerJson = gson.toJson(player);
            player.setMyState(Finals.State.NOT_ACTIVE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("playerKey", playerJson);
            editor.commit();
            viewModel.onPauseUpdatePlayer();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter i = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(r, i);
        Gson gson = new Gson();
        PlayersModel viewModel = new ViewModelProvider(this).get(PlayersModel.class);
        Player player = viewModel.getMyPlayer();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String playerJson = sharedPreferences.getString("playerKey", null);
        if (playerJson != null){
            player = gson.fromJson(playerJson, Player.class);
            player.setMyState(Finals.State.ACTIVE);
            viewModel.onPauseUpdatePlayer();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isMyServiceRunning(ForegroundService.class)){
            Intent intent = new Intent(this,
                    ForegroundService.class);
            intent.setAction("Stop Foreground Service");
            stopService(intent);
        }
    }
}
