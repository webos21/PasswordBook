package com.gmail.webos21.passwordbook.web;

import android.widget.TextView;

public class UiLog {

    private TextView view;

    public UiLog(TextView v) {
        this.view = v;
    }

    public void log(final String msg) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.append("\n" + msg);

                final int scrollAmount = view.getLayout().getLineTop(view.getLineCount()) - view.getHeight();
                // if there is no need to scroll, scrollAmount will be <=0
                if (scrollAmount > 0)
                    view.scrollTo(0, scrollAmount);
                else
                    view.scrollTo(0, 0);
            }
        });
    }

}
