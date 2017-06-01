package it.polito.mad14.myListView;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import it.polito.mad14.GroupActivity;
import it.polito.mad14.R;

public class FirebaseBackgroundService extends Service {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference myRefNumber,myRefMembers,myRefExpenses,dindon;
    private FirebaseUser user;
    private int numberGroups = 0,readMembers = 0;
    private String userMail,groupID,groupName;
    private String [] info;

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
        userMail = user.getEmail();
        myRefNumber = database.getReference("users/"+user.getEmail().replace(".",","));
        myRefNumber.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("GroupsNumb")) {
                    numberGroups = Integer.valueOf(dataSnapshot.child("GroupsNumb").getValue().toString());
                } else {
                    numberGroups = 0;
                }
                myRefMembers = database.getReference("users/"+user.getEmail().replace(".",",")+"/Not");

                myRefMembers.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(readMembers >= numberGroups){
                            String actionman = dataSnapshot.child("Action").getValue().toString().split("-")[2];
                            if (!actionman.equals(userMail)){
                                groupName = dataSnapshot.child("Name").getValue().toString();
                                groupID = dataSnapshot.getKey().toString();
                                String msg = "You are now in "+groupName;
                                sendNotification(msg,groupName,groupID);
                            }
                        }
                        else{
                            readMembers++;
                        }
                    } 

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        String action = dataSnapshot.child("Action").getValue().toString();
                        info = action.split("-");
                        if (!info[2].equals(userMail)){
                            groupID = dataSnapshot.getKey().toString();
                            groupName = dataSnapshot.child("Name").getValue().toString();
                            DatabaseReference dr = database.getReference("users/"+info[2].replace(".",","));
                            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String nameUser = dataSnapshot.child("Name").getValue().toString();
                                    String surenameUser = dataSnapshot.child("Surname").getValue().toString();
                                    String msg = messageCreation(info,nameUser,surenameUser);
                                    sendNotification(msg,groupName,groupID);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
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

    private void sendNotification(String msg, String groupName, String groupID) {

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
                .setContentTitle(groupName)
                .setContentText(msg)
                .setAutoCancel(true) //si elimina quando ci pigi
                .setDefaults(Notification.DEFAULT_ALL) //vibrazione e suoni delle impostazioni del device
                .setSound(alarmSound);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //System.out.println(mNotificationManager.getActiveNotifications().length);
        dindon = database.getReference("users/"+user.getEmail().replace(".",",")+"/groups/"+groupID+"/News");
        dindon.setValue("True");
        System.out.println("sto settando ");
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

    }

    private String messageCreation(String[] info, String nameUser, String surenameUser) {
        switch (info[0]){
            case "ADD":
                if (info[1].equals("M")){
                    return "New member"+info[3]+"added";
                }
                else{
                    return  info[3]+" has been bought";
                }
            case "DEL":
                if (info[1].equals("M")){
                    return nameUser+" "+surenameUser+" is out";
                }
                else{
                    return "Vino is ";
                }
            case "MOD":
                if (info[1].equals("M")){

                }
                else{
                    return "Vino is bought";
                }
        }
        return "lalala";
    }
}
