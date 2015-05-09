package me.vincentdefeo.udptester;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ghzmdr on 09/05/15.
 */
public class ResultViewDialog extends Dialog {
    @InjectView(R.id.result_text) TextView resultText;

    public ResultViewDialog(Context context, String text) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.result_dialog);
        ButterKnife.inject(this);
        resultText.setText(text);
    }
}
