package com.example.android.book_catalog.AdapterUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.book_catalog.MainActivity;
import com.example.android.book_catalog.ManageInventoryActivity;
import com.example.android.book_catalog.R;
import com.santalu.widget.MaskEditText;

public class ReusableMethods {
    private String titleData, authorData, phoneData;

    private EditText mEditTitle, mEditAuthor;
    private MaskEditText mEditPhone;

    private Context context;

    public static void deleteConfirmationDialog(final Context context, final Uri currentBookUri) {

        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setMessage(R.string.delete_book_warning);
        b.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEntry(context, currentBookUri, true);
            }
        });

        b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog ad = b.create();
        ad.show();
    }

    public static void deleteEntry(Context context, Uri currentBookUri, boolean b) {
        if (currentBookUri != null) {
            int rowsDeleted = context.getContentResolver().delete(currentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(context, R.string.manage_act_delete_entry_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.manage_act_delete_entry_ok, Toast.LENGTH_SHORT).show();
            }
        }

        // boolean == confirm deletion
        if (b) ;
        {
            Intent i = new Intent(context, MainActivity.class);
            ((Activity) context).finish();
            context.startActivity(i);
        }
    }

    /**
     * to be studied for future version fo this app
     */
    /*public class InventoryOnKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        }
    }

    public static void containsMandatoryData() {
        titleData = mEditTitle.getText().toString().trim();
        authorData = mEditAuthor.getText().toString().trim();
        phoneData = mEditPhone.getText().toString().trim();

        if (TextUtils.isEmpty(titleData) || TextUtils.isEmpty(authorData) ||
                TextUtils.isEmpty(phoneData)) {
            mandatoryFieldsToast();
        }
    }

    public void mandatoryFieldsToast() {
        Toast toast = Toast.makeText(context, R.string.mandatory_fields_warning, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }*/
}
