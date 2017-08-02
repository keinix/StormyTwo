package com.keinix.stormytwo.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;


public class GenericAlertDialog extends DialogFragment {

    public static GenericAlertDialog newInstance(String title, String message) {
        GenericAlertDialog dialog = new GenericAlertDialog();

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();

        return new AlertDialog.Builder(getActivity())
                .setTitle(args.getString("title"))
                .setMessage(args.getString("message"))
                .setPositiveButton("OK", null)
                .create();
    }
}
