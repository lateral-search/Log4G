package com.ls.mobile.geotool.validator;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/**
 * @deprecated
 */
public abstract class InputValidator implements TextWatcher {
        private final TextView textView;

        public InputValidator(TextView textView) {
            this.textView = textView;
        }

        public abstract void validate(TextView textView, String text);

        @Override
        final public void afterTextChanged(Editable s) {
            String text = textView.getText().toString();
            validate(textView, text);
        }

        @Override
        final public void
        beforeTextChanged(CharSequence s, int start, int count, int after) {
            /* Needs to be implemented, but we are not using it. */
        }

        @Override
        final public void
        onTextChanged(CharSequence s, int start, int before, int count) {
            /* Needs to be implemented, but we are not using it. */
        }
    }
