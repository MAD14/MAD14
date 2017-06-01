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
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.ChildEventListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import it.polito.mad14.GroupActivity;
import it.polito.mad14.MainActivity;
import it.polito.mad14.R;

public class FirebaseBackgroundService extends Service {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference myRefNumber,myRefMembers,myRefExpenses,dindon;
    private FirebaseUser user;
    private boolean notifyMember,notifyExpense;
    private int numberGroups = 0,readMembers = 0;


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
        myRefNumber = database.getReference("users/"+user.getEmail().replace(".",","));
        myRefNumber.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("GroupsNumb")) {
                    numberGroups = Integer.valueOf(dataSnapshot.child("GroupsNumb").getValue().toString());
                } else {
                    numberGroups = 0;
                }
                System.out.println("EXT : #groups : "+numberGroups);
                myRefMembers = database.getReference("users/"+user.getEmail().replace(".",",")+"/Not");
                notifyMember = false;

                myRefMembers.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        System.out.println("onChildAdded : "+readMembers);
                        if(readMembers >= numberGroups){
                            //manda notifica per nuovo gruppo: You have been added to a new group!
                            sendNotification(dataSnapshot.child("Name").getValue().toString(),dataSnapshot.child("Action").getValue().toString(),dataSnapshot.getKey().toString());
                            //String date = dataSnapshot.child("Date").getValue().toString();
                            String name = dataSnapshot.child("Name").getValue().toString();
                            //String name = dataSnapshot.getKey().toString();
                            System.out.println("INT : new group added : "+name+" - "+dataSnapshot.child("Action").getValue().toString());
                        }
                        else{
                            readMembers++;
                            System.out.println("sono i primi");//check
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        //manda notifica novità rispetto agli user
                        //String date = dataSnapshot.child("Date").getValue().toString();
                        String name = dataSnapshot.child("Name").getValue().toString();
                        //String name = dataSnapshot.getKey().toString();
                        System.out.println("new members added : "+name+" - "+dataSnapshot.child("Action").getValue().toString());
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                       //non faccio nulla perchè sono stata io ad eliminarlo!
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    @Override
    public int onStartCommand(Intent i, int flags , int startId){
        return Service.START_STICKY;
    }

    private void sendNotification(String groupName, String action, String groupID) {
        int sdk = Build.VERSION.SDK_INT;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (sdk < Build.VERSION_CODES.JELLY_BEAN){
            //mNotificationManager.getActiveNotifications();
        }
        else{

        }
        int NOTIFICATION_ID = 1;
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(this, GroupActivity.class);
        resultIntent.putExtra("IDGroup",groupID);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(GroupActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_mani_box)
                .setContentTitle("Shared Pocket")
                .setContentText(messageCreation(groupName,action))
                .setAutoCancel(true) //si elimina quando ci pigi
                .setDefaults(Notification.DEFAULT_ALL) //vibrazione e suoni delle impostazioni del device
                .setSound(alarmSound);
        builder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //System.out.println(mNotificationManager.getActiveNotifications().length);
        dindon = database.getReference("users/"+user.getEmail().replace(".",",")+"/groups/"+groupID+"/News");
        dindon.setValue("True");
        System.out.println("sto settando ");
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

    }

    private String messageCreation(String groupName, String action) {
        return "lalala";
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
