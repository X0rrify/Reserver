package com.reserver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {

    private static OnCallListener onCallListener;

    public interface OnCallListener {
        void onCallReceive(String phoneNumber);
    }

    public void setOnCallListener(OnCallListener listener) {
        onCallListener = listener;
    }

    // Extract the phone number from the incoming call.
    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

        if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
            onCallListener.onCallReceive(intent.getStringExtra("incoming_number"));
        }
    }
}
