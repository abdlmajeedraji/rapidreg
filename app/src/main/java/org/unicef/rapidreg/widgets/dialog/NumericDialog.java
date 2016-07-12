package org.unicef.rapidreg.widgets.dialog;

import android.content.Context;
import android.content.res.Configuration;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.unicef.rapidreg.forms.childcase.CaseField;
import org.unicef.rapidreg.service.cache.ItemValues;

public class NumericDialog extends BaseDialog {

    private EditText editText;

    public NumericDialog(Context context, CaseField caseField, ItemValues itemValues, TextView resultView, ViewSwitcher viewSwitcher) {
        super(context, caseField, itemValues, resultView, viewSwitcher);
    }

    @Override
    public void initView() {
        editText = new EditText(getContext());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setRawInputType(Configuration.KEYBOARD_12KEY);
        editText.setText(resultView.getText());
        getBuilder().setView(editText);
    }

    @Override
    public Number getResult() {
        return Integer.parseInt(editText.getText().toString());
    }
}
