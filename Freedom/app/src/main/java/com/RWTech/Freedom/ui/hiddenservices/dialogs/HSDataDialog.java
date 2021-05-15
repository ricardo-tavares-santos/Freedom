package com.RWTech.Freedom.ui.hiddenservices.dialogs;


import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.RWTech.Freedom.R;
import com.RWTech.Freedom.ui.hiddenservices.providers.HSContentProvider;

public class HSDataDialog extends DialogFragment {


    private boolean checkInput(String localPortString, String onionPortString, String serverName) {

        boolean is_set = ((localPortString != null && localPortString.length() > 0) && (onionPortString != null && onionPortString.length() > 0) && (serverName != null && serverName.length() > 0));
        if (!is_set) {
            Toast.makeText(getContext(), R.string.fields_can_t_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout
        final View dialog_view = getActivity().getLayoutInflater().inflate(R.layout.layout_hs_data_dialog, null);

        // Use the Builder class for convenient dialog construction
        final AlertDialog serviceDataDialog = new AlertDialog.Builder(getActivity())
                .setView(dialog_view)
                .setTitle(R.string.hidden_services)
                .create();

        // Buttons action
        Button save = dialog_view.findViewById(R.id.HSDialogSave);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String serverName = ((EditText) dialog_view.findViewById(R.id.hsName)).getText().toString();
                String localPortString = ((EditText) dialog_view.findViewById(R.id.hsLocalPort)).getText().toString();
                String onionPortString = ((EditText) dialog_view.findViewById(R.id.hsOnionPort)).getText().toString();

                if (checkInput(localPortString, onionPortString, serverName))  {
                    Integer localPort = Integer.parseInt(localPortString);
                    Integer onionPort = Integer.parseInt(onionPortString);

                    Boolean authCookie = ((CheckBox) dialog_view.findViewById(R.id.hsAuth)).isChecked();

                    if (checkInput(serverName, localPort, onionPort)) {
                        saveData(serverName, localPort, onionPort, authCookie);
                        Toast.makeText(
                                v.getContext(), R.string.please_restart_Orbot_to_enable_the_changes, Toast.LENGTH_LONG
                        ).show();
                        serviceDataDialog.dismiss();
                    }
                }
            }
        });

        Button cancel = dialog_view.findViewById(R.id.HSDialogCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                serviceDataDialog.cancel();
            }
        });

        return serviceDataDialog;
    }

    private boolean checkInput(String serverName, Integer local, Integer remote) {
        boolean is_ok = true;
        Integer error_msg = 0;

        if ((local < 1 || local > 65535) || (remote < 1 || remote > 65535)) {
            error_msg = R.string.invalid_port;
            is_ok = false;
        }

        if (serverName == null || serverName.length() < 1) {
            error_msg = R.string.name_can_t_be_empty;
            is_ok = false;
        }

        if (!is_ok) {
            Toast.makeText(getContext(), error_msg, Toast.LENGTH_SHORT).show();
        }

        return is_ok;
    }

    private void saveData(String name, Integer local, Integer remote, Boolean authCookie) {

        ContentValues fields = new ContentValues();
        fields.put(HSContentProvider.HiddenService.NAME, name);
        fields.put(HSContentProvider.HiddenService.PORT, local);
        fields.put(HSContentProvider.HiddenService.ONION_PORT, remote);
        fields.put(HSContentProvider.HiddenService.AUTH_COOKIE, authCookie);
        fields.put(HSContentProvider.HiddenService.CREATED_BY_USER, 1);

        ContentResolver cr = getContext().getContentResolver();

        cr.insert(HSContentProvider.CONTENT_URI, fields);
    }
}
