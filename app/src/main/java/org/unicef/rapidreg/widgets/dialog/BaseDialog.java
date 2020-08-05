package org.unicef.rapidreg.widgets.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.BaseAlertDialog;
import org.unicef.rapidreg.forms.Field;
import org.unicef.rapidreg.service.cache.ItemValuesMap;
import org.unicef.rapidreg.widgets.viewholder.GenericViewHolder;

public abstract class BaseDialog {
    protected Field field;
    protected TextView resultView;
    protected ViewSwitcher viewSwitcher;
    protected ItemValuesMap itemValues;

    private BaseAlertDialog.Builder builder;
    protected Context context;

    public BaseDialog(final Context context, final Field field, final ItemValuesMap itemValues,
                      final TextView resultView, final ViewSwitcher viewSwitcher) {
        this.field = field;
        this.resultView = resultView;
        this.viewSwitcher = viewSwitcher;
        this.context = context;
        this.itemValues = itemValues;

        createDialogBuilder(context);
    }

    private void createDialogBuilder(Context context) {
        builder = new BaseAlertDialog.Builder(context);
        builder.setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setNeutralButton(R.string.clear, (dialog, which) -> {
                    dialog.dismiss();
                    resultView.setText("");
                    itemValues.removeItem(field.getName());
                    if (viewSwitcher != null) {
                        viewSwitcher.setDisplayedChild(GenericViewHolder.FORM_NO_ANSWER_STATE);
                    }
                });
    }

    public void show() {
        initView();
        AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (!TextUtils.isEmpty(verifyResult())) {
                MessageDialog messageDialog = new MessageDialog(context);
                messageDialog.setTitle(R.string.invalid_value);
                messageDialog.setMessage(verifyResult());
                messageDialog.setMessageColor(context.getResources().getColor(R.color.primero_font_medium));
                messageDialog.setPositiveButton(R.string.ok, subview -> messageDialog.dismiss());
                messageDialog.show();
            } else {
                if (viewSwitcher != null) {
                    if (getResult() != null && !TextUtils.isEmpty(getResult().toString())) {
                        viewSwitcher.setDisplayedChild(GenericViewHolder.FORM_HAS_ANSWER_STATE);
                    } else {
                        viewSwitcher.setDisplayedChild(GenericViewHolder.FORM_NO_ANSWER_STATE);
                    }
                }
                dialog.dismiss();
                itemValues.addItem(field.getName(), getResult());
                resultView.setText(getDisplayText());
            }
        });
        changeDialogDividerColor(context, dialog);
    }

    public void changeDialogDividerColor(Context context, Dialog dialog) {
        int titleDividerId = context.getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(ContextCompat.getColor(context, R.color.primero_blue));
        }
    }

    protected BaseAlertDialog.Builder getBuilder() {
        return builder;
    }

    protected Context getContext() {
        return context;
    }

    protected String getDisplayText() {
        return getResult() == null ? null : field.getSingleSelectedOptions(getResult().toString());
    }

    public String verifyResult() {
        return null;
    }

    public abstract void initView();

    public abstract Object getResult();
}
