package it.polito.mad14;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.polito.mad14.myListView.FirebaseBackgroundService;

public class StartFirebaseAtBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("BROADCAST INIT");
        context.startService(new Intent(FirebaseBackgroundService.class.getName()));
    }

}
