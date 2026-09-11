package com.hermes.promptpad;

import android.app.RemoteInput;
import android.content.*;
import android.os.Bundle;

public class ReplyReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx, Intent intent) {
        Bundle results = RemoteInput.getResultsFromIntent(intent);
        if (results != null && results.getCharSequence("reply") != null)
            NotificationTarget.post(ctx, results.getCharSequence("reply").toString());
    }
}
