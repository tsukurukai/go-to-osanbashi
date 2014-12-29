package tsukurukai.gotoosanbashi.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

public class LoadingDialogFragment extends DialogFragment {
    private ProgressDialog progressDialog;

    public static LoadingDialogFragment newInstance() {
        LoadingDialogFragment fragment = new LoadingDialogFragment();
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        setCancelable(false);
        setRetainInstance(true);
        return progressDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progressDialog = null;
    }
}
