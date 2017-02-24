package com.rahil.filescanner.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.rahil.filescanner.R;

public class ProgressDialogFragment extends DialogFragment {

    private String progressMessage;
    private static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    private ProgressDialogActionListener progressDialogActionListener;

    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MESSAGE, message);
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);
        return fragment;
    }

    public ProgressDialogFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            progressDialogActionListener = (ProgressDialogActionListener) activity;
        } catch (ClassCastException exception) {
            throw new ClassCastException(activity.getClass().getSimpleName() + " must implement "
                    + ProgressDialogActionListener.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);

        Bundle arguments = getArguments();
        progressMessage = arguments != null && arguments.containsKey(EXTRA_MESSAGE)
                ? arguments.getString(EXTRA_MESSAGE)
                : getString(R.string.scanning_sd_card);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(progressMessage);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface dialog, int keyCode, android.view.KeyEvent event) {

                if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                    if (progressDialogActionListener != null) {
                        progressDialogActionListener.onStopClicked();
                    }
                    dialog.dismiss();
                    return true;
                } else
                    return false; // pass on to be processed as normal
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.stop_scan), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (progressDialogActionListener != null) {
                    progressDialogActionListener.onStopClicked();
                }
                dialogInterface.dismiss();
            }
        });
        return dialog;
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    public interface ProgressDialogActionListener {
        void onStopClicked();
    }
}
