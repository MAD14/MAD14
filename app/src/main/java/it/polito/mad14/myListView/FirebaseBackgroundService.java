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
    private String userMail,groupID,groupName,notificationSound;
    private String [] info;
    int NOTIFICATION_ID = 0;

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
                                String msg = getResources().getString(R.string.new_group)+" "+groupName;
                                sendNotification(msg,groupName,groupID,"True");
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
                            notificationSound = dataSnapshot.child("Sound").getValue().toString();
                            if (info[1].equals("M") && (info[0].equals("ADD") || info[0].equals("DEL")) && info.length == 4){
                                DatabaseReference dr;
                                if(info[0].equals("ADD")){dr = database.getReference("users/"+info[3].replace(".",","));}
                                else{dr = database.getReference("users/"+info[2].replace(".",","));}
                                dr.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String nameUser = dataSnapshot.child("Name").getValue().toString();
                                        String surnameUser = dataSnapshot.child("Surname").getValue().toString();
                                        String msg = messageCreation(info,nameUser,surnameUser);
                                        sendNotification(msg,groupName,groupID,notificationSound);
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if (!info[0].equals("SIL")){
                                String msg = messageCreation(info,"nameUser","surnameUser");
                                sendNotification(msg,groupName,groupID,notificationSound);
                            }
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

    private void sendNotification(String msg, String groupName, String groupID,String notificationSound) {
        NOTIFICATION_ID = (NOTIFICATION_ID+1)%10;
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(this, GroupActivity.class);
        resultIntent.putExtra("IDGroup",groupID);
        resultIntent.putExtra("GroupName",groupName);
        resultIntent.putExtra("Sound",notificationSound);
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
                .setSound(alarmSound);
        if (notificationSound.equals("True")){builder.setDefaults(Notification.DEFAULT_ALL);} //vibrazione e suoni delle impostazioni del device}
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
                    if (!info[3].equals("MANY")){return nameUser+" "+surenameUser+" is added";}
                    else{return getResources().getString(R.string.new_members);}
                }
                else{
                    return  info[3]+" "+getResources().getString(R.string.new_exp);
                }
            case "DEL":
                if (info[1].equals("M")){
                    return nameUser+" "+surenameUser+" "+getResources().getString(R.string.out);
                }
                else{
                    return info[2]+" "+getResources().getString(R.string.removed);
                }
            case "MOD":
                if (info[1].equals("M")){
                    return getResources().getString(R.string.infoMod);
                }
                else{
                    return info[2]+" "+getResources().getString(R.string.expMod);
                }
        }
        return "";
    }
}
