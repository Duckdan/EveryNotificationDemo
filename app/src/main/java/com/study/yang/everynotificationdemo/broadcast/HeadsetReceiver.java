package com.study.yang.everynotificationdemo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class HeadsetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        String msg = intentAction + "====" + event.getKeyCode();
        Log.e("intent::", msg);
        Toast.makeText(context, "===" + msg, Toast.LENGTH_SHORT).show();
    }
}
