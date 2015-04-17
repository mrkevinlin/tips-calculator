package com.scepticallistic.kevin.tipcalc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PercentDialogFragment extends DialogFragment {

    private static final String LOG_TAG = PercentDialogFragment.class.getSimpleName();
    private PercentDialogListener callback;
    private TextView customText;
    double per;

    public interface PercentDialogListener {
        public void addPercentToSpinner(double p);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            callback = (PercentDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement PercentDialogListener");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View inflator = inflater.inflate(R.layout.fragment_percent_dialog, null);

        customText = (EditText) inflator.findViewById(R.id.custom_percent_text);

        builder.setView(inflator)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!TextUtils.isEmpty(customText.getText())) {
                            try {
                                per = Double.parseDouble(customText.getText().toString());
                            } catch (NumberFormatException e) {

                            }
                        }
                        callback.addPercentToSpinner(per);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.addPercentToSpinner(0);
    }
}
