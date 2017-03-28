package com.example.android.apis._debugproxy;

import android.app.ActivityManager;
import android.app.DialogFragment;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by ha271 on 2017/3/22.
 */

public class DebugMsgActivity extends PreferenceActivity {
    public DebugMsgFragment mDebugMsgFragment;
    public DialogFragment mDebugMsgFragmentDialog; // SettingsFragmentDialog
    public static boolean bDrawOverlays_dialog_popup; // Hawk: to prevent show this dialog twice.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDebugMsgFragment = new DebugMsgFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mDebugMsgFragment).commit();

        if (isServiceRunning(this,DebugMsgService.class)) {
            Intent intent = new Intent(this, DebugMsgService.class);
            stopService(intent);
        }
    }

    // callback for API23+
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_CANCELED) { // Hawk: If the user do enable/disable/do-nothing/back_key this sub-setting menu, it will belong to RESULT_CANCELED.
            switch (requestCode) {
                case 1234:
                    if (APPcanDrawOverlays(this))
                        Log.i("Hawk","ooooo APP got OVERLAY_PERMISSION ooooooooooooooooooooo");
                    else
                    {
                        Log.i("Hawk","xxxxx APP NOT got OVERLAY_PERMISSION xxxxxxxxxxxxxxxxx");
                        this.showMyDialog(0);
                    }
                    bDrawOverlays_dialog_popup = false;
                    break;
                default:
            }
        }
    }
    // Settings\Apps\<Setting Icon>\Configure apps\Special access\Draw over other apps
    public void getDrawPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!APPcanDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
                bDrawOverlays_dialog_popup = true;
            }
        } else {
            // Hawk: I don't know this is for what.
            Intent intent = new Intent(context, Service.class);
            startService(intent);
        }
    }


    public void showMyDialog(int i) {
        mDebugMsgFragmentDialog = DebugMsgFragmentDialog.newInstance(i);
        mDebugMsgFragmentDialog.show(getFragmentManager(), "dialog");
    }

    @Override
    public ComponentName startService(Intent service) {
        if(APPcanDrawOverlays(this))
        {
            return super.startService(service);
        }
        else
        {
            getDrawPermission(this);
            return null;
        }
    }

    private  boolean APPcanDrawOverlays(Context context){
        if(Build.VERSION.SDK_INT >= 23) {
            return Settings.canDrawOverlays(context);
        }
        else {

            return true;
        }
    }

    /* check if service is running */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
