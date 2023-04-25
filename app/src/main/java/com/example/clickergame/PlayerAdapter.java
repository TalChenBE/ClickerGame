package com.example.clickergame;

import static com.example.clickergame.Finals.WIN_SCORE;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {
    private final Context context;
    private final PlayersModel viewModel;
    private ArrayList<Player> playersList;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private FragmentActivity activity;

    public PlayerAdapter(Context context, FragmentActivity activity, PlayersModel viewModel) {
        this.viewModel = viewModel;
        this.context = context;
        this.playersList = new ArrayList<>();
        this.activity = activity;

        viewModel.getPlayersLiveData().observe(activity, new Observer<ArrayList<Player>>() {
            @Override
            public void onChanged(ArrayList<Player> players) {
                setPlayersList(players);
                if (!players.isEmpty()) {
                    if (players.get(0).getScore() == 0 && !Finals.isDialogShown)
                        showEndGameDialog(false, activity);
                    for (int i = 1; i < players.size(); i++) {
                        if (players.get(i).getScore() == WIN_SCORE && !Finals.isDialogShown){
                            showEndGameDialog(false, activity);
                            break;
                        }
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    private void showEndGameDialog(boolean isWin, FragmentActivity activity) {
        EndGameDialog endGameFrag = (EndGameDialog)  activity.getSupportFragmentManager().findFragmentByTag("End Game dialog");
        if (endGameFrag != null && Objects.requireNonNull(((EndGameDialog) endGameFrag).getDialog()).isShowing())
            return;
        FragmentManager fm = activity.getSupportFragmentManager();
        EndGameDialog endGameDialog = EndGameDialog.newInstance(isWin);
        endGameDialog.show(fm, "End Game dialog");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View playerView = inflater.inflate(R.layout.layout_player_cube, parent, false);
        return new ViewHolder(playerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = playersList.get(position);
        this.selectedPosition = this.viewModel.getPosition();
        switch (player.getMyState()){
            case ACTIVE:
                holder.itemView.setBackgroundResource(R.color.green);
                break;
            case NOT_ACTIVE:
                holder.itemView.setBackgroundResource(R.color.red);
                break;
            case SUSPEND:
                holder.itemView.setBackgroundResource(R.color.orange);
                break;
            default:
                holder.itemView.setBackgroundResource(R.color.green);
                break;
        }

        if (position == 0)
            holder.itemView.setBackgroundResource(R.color.white);
        holder.setPlayer(position, player);
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    public void setPlayersList(ArrayList<Player> playersList){
        this.playersList = playersList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView score;
        private final TextView name;
        private final View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            score = (TextView) itemView.findViewById(R.id.score);
            name = (TextView) itemView.findViewById(R.id.name);
            this.view = itemView;
        }

        public void setPlayer(int position, Player player){
            this.name.setText(player.getName());
            this.score.setText(Long.toString(player.getScore()));
            this.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (player.getMyState() != Finals.State.SUSPEND) {
                        viewModel.setItemSelected(position);
                        if (position == 0){
                            if (player.getScore() == WIN_SCORE)
                                showEndGameDialog(true, activity);
                            viewModel.increasePlayerScore(player);
                            notifyItemChanged(0);
                            player.increaseScore();
                        }
                        else {
                            if (player.getScore() > 0){
                                viewModel.decreasePlayerScore(player);
                                player.decreaseScore();
                            }
                            else {
                                viewModel.removePlayer(player);
                                playersList.remove(player);
                            }
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }
}
