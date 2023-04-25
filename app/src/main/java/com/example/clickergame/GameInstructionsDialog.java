package com.example.clickergame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class GameInstructionsDialog extends DialogFragment  {

    public GameInstructionsDialog() {
    }

    public static GameInstructionsDialog newInstance() {
        GameInstructionsDialog fragment = new GameInstructionsDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.instructions);
        dialogBuilder.setIcon(R.drawable.instructions_icon);
        dialogBuilder.setMessage("In this game you can do .....");
        dialogBuilder.setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        return dialogBuilder.create();
    }
}