package com.example.clickergame;

import static com.example.clickergame.Finals.selfKey;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlayersModel extends AndroidViewModel {
    MutableLiveData<ArrayList<Player>> playersLiveData;
    MutableLiveData<Integer> itemSelectedLive;
    MutableLiveData<Player> playerLiveData;
    Integer itemSelected;
    ArrayList<Player> playersList;
    Player player;
    private final DatabaseReference database;
    private final Context context;

    public PlayersModel(Application app) {
        super(app);
        this.playersLiveData = new MutableLiveData<>();
        this.itemSelectedLive = new MutableLiveData<>();
        this.playerLiveData = new MutableLiveData<>();
        this.itemSelected = RecyclerView.NO_POSITION;
        this.itemSelectedLive.setValue(this.itemSelected);
        FirebaseDatabase.getInstance();
        this.database = FirebaseDatabase.getInstance().getReference("players");
        this.database.keepSynced(true);
        this.playersList = new ArrayList<Player>();
        this.context = app.getApplicationContext();
    }

    public void initPlayersList() {
        this.playersLiveData.setValue(playersList);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pullData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        database.limitToFirst(16).addValueEventListener(postListener);
    }

    private void pullData(DataSnapshot dataSnapshot){
        this.playersList = new ArrayList<>();
        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            Player temp = postSnapshot.getValue(Player.class);
            if (this.player.getKey() != null && temp.isEqual(this.player))
                this.playersList.add(0, new Player(temp));
            else if (this.player.getKey() != null)
                this.playersList.add(new Player(temp));
        }
        if (this.playersList.isEmpty())
            this.playersList.add(0, this.player);
        this.playersLiveData.setValue(this.playersList);
    }

    public MutableLiveData<ArrayList<Player>> getPlayersLiveData() {
        return this.playersLiveData;
    }

    public MutableLiveData<Player> getPlayerLiveData() {
        return this.playerLiveData;
    }

    public void setItemSelected(int position){
        this.itemSelected = position;
        this.itemSelectedLive.setValue(this.itemSelected);
    }

    public int getPosition(){
        return this.itemSelected;
    }

    public void setPlayer(Player player) {
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String playerJson = sharedPreferences.getString("playerKey", null);
        if (playerJson != null){
            this.player = gson.fromJson(playerJson, Player.class);
            this.player.setMyState(Finals.State.ACTIVE);
            onPauseUpdatePlayer();
            selfKey = this.player.getKey();
            return;
        }
        this.player = player;
        this.player.setId(this.playersList.size() + 1);
        this.player.setVisibility(false);
        String key = this.database.push().getKey();
        this.player.setKey(key);
        this.database.child(player.getKey()).setValue(player);
        selfKey = key;
    }

    public void onPauseUpdatePlayer() {
        if (this.player != null && this.player.getKey() != null)
            this.database.child(this.player.getKey()).child("myState").setValue(this.player.getMyState());
    }

    public void removePlayer(Player player){
        if (player.getKey().equals(this.player.getKey())){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("playerKey");
            editor.commit();
        }
        this.database.child(player.getKey()).removeValue();
    }

    public void increasePlayerScore(Player player){
        this.database.child(player.getKey()).child("score").setValue(ServerValue.increment(1));
    }

    public void decreasePlayerScore(Player player){
        this.database.child(player.getKey()).child("score").setValue(ServerValue.increment(-1));
    }

    public Player getPlayer(int position){
        return this.playersList.get(position);
    }

    public Player getMyPlayer(){
        return this.player;
    }

    public void setMyPlayerVisibility(boolean visibility){
        this.player.setVisibility(visibility);
    }

    public void setMyPlayerKey(String key){
        this.player.setKey(key);
    }

    public boolean getMyPlayerVisibility() {
        return this.player.isVisibility();
    }

    public void resetGame() {
        if (!playersList.isEmpty()){
            this.database.removeValue();
            selfKey = null;
            playersList = new ArrayList<>();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("playerKey");
            editor.commit();
        }
    }
}
