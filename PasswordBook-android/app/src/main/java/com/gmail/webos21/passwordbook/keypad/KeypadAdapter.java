package com.gmail.webos21.passwordbook.keypad;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.gmail.webos21.passwordbook.R;

public class KeypadAdapter extends BaseAdapter {
    private Context mContext;

    // Declare button click listener variable
    private OnClickListener mOnButtonClick;

    // Create and populate keypad buttons array with CalculatorButton enum values
    private KeypadButton[] mButtons = {
            KeypadButton.ONE, KeypadButton.TWO, KeypadButton.THREE,
            KeypadButton.FOUR, KeypadButton.FIVE, KeypadButton.SIX,
            KeypadButton.SEVEN, KeypadButton.EIGHT, KeypadButton.NINE,
            KeypadButton.DUMMY, KeypadButton.ZERO, KeypadButton.BACKSPACE};

    public KeypadAdapter(Context c) {
        mContext = c;
    }

    // Method to set button click listener variable
    public void setOnButtonClickListener(OnClickListener listener) {
        mOnButtonClick = listener;
    }

    public int getCount() {
        return mButtons.length;
    }

    public Object getItem(int position) {
        return mButtons[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ButtonView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        Button btn;

        if (convertView == null) { // if it's not recycled, initialize some attributes
            btn = new Button(mContext);
            if (Build.VERSION.SDK_INT < 23) {
                btn.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
            } else {
                btn.setTextAppearance(android.R.style.TextAppearance_Large);
            }

            KeypadButton keypadButton = mButtons[position];

            switch (keypadButton.mCategory) {
                case NUMBER:
                    btn.setBackgroundResource(android.R.drawable.btn_default);
                    break;
                case OPERATOR:
                    btn.setBackgroundResource(android.R.drawable.btn_default);
                    break;
                case DUMMY:
                    btn.setBackgroundResource(android.R.drawable.btn_default);
                    btn.setEnabled(false);
                    break;
                default:
                    btn.setBackgroundResource(android.R.drawable.btn_default);
                    break;
            }

            // Set OnClickListener of the button to mOnButtonClick
            if (keypadButton != KeypadButton.DUMMY) {
                btn.setOnClickListener(mOnButtonClick);
            } else {
                btn.setClickable(false);
            }

            // Set CalculatorButton enumeration as tag of the button so that we
            // will use this information from our main view to identify what to do
            btn.setTag(keypadButton);
        } else {
            btn = (Button) convertView;
        }

        btn.setText(mButtons[position].getText());
        return btn;
    }

}
