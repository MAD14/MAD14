package it.polito.mad14.myListView;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import it.polito.mad14.GroupActivity;
import it.polito.mad14.R;

public class FirebaseBackgroundService2 extends Service {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRefExpenses,myRefMembers;
    private FirebaseUser user;
    private boolean notifyExpense;
    private Date mostRecentDateE;
    private long mostRecentTimestampE;
    private String valueMostRecentE,nameMostRecentE,IDMostRecentE;
    private String IDNotificated;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("SONO NEL SERVICE");
        user = FirebaseAuth.getInstance().getCurrentUser();
        myRefExpenses = database.getReference("users/"+user.getEmail().replace(".",",")+"/Expenses");
        notifyExpense = false;
               /* Runnable rMembers = new Runnable() {
            @Override
            public void run() {
                try {*/
        myRefExpenses.addValueEventListener( new ValueEventListener(){
            public void onDataChange(DataSnapshot dataSnapshot) {
                //membersIDListTMP = new ArrayList<>();
                SimpleDateFormat formatter =  new SimpleDateFormat("dd/MM/yyy HH:mm");
                int i = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (notifyExpense) {
                        if( i == 0 ){
                            try {
                                valueMostRecentE = data.child("Value").getValue().toString();
                                mostRecentDateE = formatter.parse(data.child("Date").getValue().toString());
                                mostRecentTimestampE = mostRecentDateE.getTime();
                                IDMostRecentE = data.getKey().toString();
                                nameMostRecentE = data.child("Name").getValue().toString();
                            } catch (ParseException e) {
                                System.out.println("Mannaggia");
                            }
                            i++;
                        }
                        else{
                            Date d1 = null;
                            try {
                                System.out.println("QUI: "+data.getKey().toString());
                                d1 = formatter.parse(data.child("Date").getValue().toString());
                                long timestamp1 = d1.getTime();
                                if (timestamp1 > mostRecentTimestampE){
                                    mostRecentTimestampE = timestamp1;
                                    valueMostRecentE = data.child("Value").getValue().toString();
                                    IDMostRecentE = data.getKey().toString();
                                    nameMostRecentE = data.child("Name").getValue().toString();
                                }
                            } catch (ParseException e) {
                                System.out.println("Mannaggia");
                            }
                        }
                    }
                }
                if(notifyExpense){
                    if(!valueMostRecentE.equals("NEW") && !valueMostRecentE.equals("I'M THE AUTHOR")){
                        System.out.println("MANDA PER NUOVa spesa");
                        //sendNotification(nameMostRecentE+": new expences!",IDMostRecentE);
                    }
                }
                else{
                    notifyExpense = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


             /*   } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        };
        Thread tMembers = new Thread(rMembers);
        tMembers.start();*/

    }
    @Override
    public int onStartCommand(Intent i, int flags , int startId){
        return Service.START_STICKY;
    }

    private void sendNotification(String msg, String IDGruop) {
        //NOTIFICATION
       /* NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.expense_icon)
                        .setContentTitle("Shared Pocket")
                        .setContentText("You've received new messages.");
        System.out.println("l'ho costriuto");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent =
                PendingIntent.getService(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());*/
        int NOTIFICATION_ID = 1;
        //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
       /* Intent notificationIntent = new Intent(this, GroupActivity.class);
        notificationIntent.putExtra("IDGroup",IDGruop);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle("Shared Pocket")
                .setContentText(msg)
                .setAutoCancel(true) //si elimina quando ci pigi
                .setDefaults(Notification.DEFAULT_ALL); //vibrazione e suoni delle impostazioni del device
                //.setSound(alarmSound)
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());*/

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(this, GroupActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(GroupActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_mani_box)
                .setContentTitle("Shared Pocket")
                .setContentText(msg)
                .setAutoCancel(true) //si elimina quando ci pigi
                .setDefaults(Notification.DEFAULT_ALL) //vibrazione e suoni delle impostazioni del device
                .setSound(alarmSound);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //System.out.println(mNotificationManager.getActiveNotifications().length);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());


    }

    private boolean checkIfAppIsRunningInForeground() {
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningAppProcessInfo appProcessInfo : activityManager.getRunningAppProcesses()) {
            if(appProcessInfo.processName.contains(this.getPackageName())) {
                return checkIfAppIsRunningInForegroundByAppImportance(appProcessInfo.importance);
            }
        }
        return false;
    }
    private boolean checkIfAppIsRunningInForegroundByAppImportance(int appImportance) {
        switch (appImportance) {
            //user is aware of app
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE:
                return true;
            //user is not aware of app
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY:
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE:
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE:
            default:
                return false;
        }
    }
}

