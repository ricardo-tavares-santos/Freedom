package com.RWTech.Freedom.ui.hiddenservices.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import com.RWTech.Freedom.R;
import com.RWTech.Freedom.ui.hiddenservices.providers.CookieContentProvider;

public class CookieDeleteDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        final Context context = getContext();

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Delete from db
                        context.getContentResolver().delete(
                                CookieContentProvider.CONTENT_URI,
                                CookieContentProvider.ClientCookie._ID + "=" + arguments.getInt("_id"),
                                null
                        );

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Do nothing
                        break;
                }
            }
        };

        return new AlertDialog.Builder(context)
                .setMessage(R.string.confirm_cookie_deletion)
                .setPositiveButton(R.string.btn_okay, dialogClickListener)
                .setNegativeButton(R.string.btn_cancel, dialogClickListener)
                .create();
    }
}
