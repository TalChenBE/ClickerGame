package com.example.clickergame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

public class EndGameDialog extends DialogFragment {
    private PlayersModel viewModel;
    private boolean isWin;

    public EndGameDialog() {
    }

    public static EndGameDialog newInstance(boolean isWin) {
        EndGameDialog fragment = new EndGameDialog();
        Bundle args = new Bundle();
        args.putBoolean("WIN", isWin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isWin = getArguments().getBoolean("WIN");
        } else {
            isWin = false;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Finals.isDialogShown = true;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        viewModel = new ViewModelProvider(requireActivity()).get(PlayersModel.class);
        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_dialog_end_game, null, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("playerKey");
        editor.commit();
        viewModel.setMyPlayerKey(null);
        if (this.isWin){
            dialogBuilder.setTitle(R.string.win);
            dialogBuilder.setIcon(R.drawable.party_emojii);
            viewModel.resetGame();
        }
        else{
            dialogBuilder.setTitle(R.string.lost);
            dialogBuilder.setIcon(R.drawable.sad_emojii);
            ((ImageView) view.findViewById(R.id.imageView)).setImageResource(R.drawable.try_again);
        }
        dialogBuilder.setView(view);
        dialogBuilder.setNegativeButton(R.string.return_home, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getParentFragmentManager().popBackStackImmediate("BBB", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                dialog.dismiss();
            }
        });
        return dialogBuilder.create();
    }
}

