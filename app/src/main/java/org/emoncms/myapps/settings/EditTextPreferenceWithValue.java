package org.emoncms.myapps.settings;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.emoncms.myapps.R;


/**
 * Created by tamsin on 07/09/16.
 */
public class EditTextPreferenceWithValue extends EditTextPreference {
    private TextView textValue;

    public EditTextPreferenceWithValue(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_with_value);
    }

    public EditTextPreferenceWithValue(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_with_value);
    }

    public EditTextPreferenceWithValue(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_with_value);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        textValue = (TextView) view.findViewById(R.id.pref_value);
        if (textValue != null) {
            textValue.setText(getText());
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (textValue != null) {
            textValue.setText(getText());
        }
    }
}