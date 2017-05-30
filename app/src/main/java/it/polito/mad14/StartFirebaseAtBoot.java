package it.polito.mad14;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.polito.mad14.myListView.FirebaseBackgroundService;

public class StartFirebaseAtBoot extends BroadcastReceiver {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRefNumber,myRefMembers;
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("BROADCAST INIT AT BOOT");
        Intent service = new Intent(context, FirebaseBackgroundService.class);
        context.startService(service);
    }

}
