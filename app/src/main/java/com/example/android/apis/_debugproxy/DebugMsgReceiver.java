package com.example.android.apis._debugproxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by ha271 on 2017/3/22.
 */

public class DebugMsgReceiver extends BroadcastReceiver {
    DebugMsgService.DebugMsgService_Listener mListener;
    public DebugMsgReceiver(DebugMsgService.DebugMsgService_Listener listener){
        mListener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        if("com.example.android.apis.debugmsg".equals(intent.getAction())){
            String className = intent.getStringExtra("ClassName");
            Log.d("Hawk","ClassName: " + className);
            mListener.onDebugMsgUpdate(className);
        }
    }
}
