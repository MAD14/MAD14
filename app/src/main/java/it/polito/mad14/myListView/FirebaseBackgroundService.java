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
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import it.polito.mad14.GroupActivity;
import it.polito.mad14.MainActivity;
import it.polito.mad14.R;

public class FirebaseBackgroundService extends Service {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRefGroups,myRefExpenses,myRefMembers,myRefInitial;
    private FirebaseUser user;
    private ArrayList<String>groupsIDList = new ArrayList<>();
    private ArrayList<String>membersIDList = new ArrayList<>();
    private ArrayList<String>expencesIDList = new ArrayList<>();
    private ArrayList<String>groupsNameList = new ArrayList<>();
    private ArrayList<String>groupsIDListTMP,membersIDListTMP,expencesIDListTMP;
    private ArrayList<String>groupsNameListTMP;
    private int indexGroup = 0,indexGroupTMP = 0, indexMembers = 0, indexMembersTMP = 0;
    private boolean notifyGroup,notifyMember,notifyExpense;
    private String nm,id;


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
        myRefInitial = database.getReference("users/"+user.getEmail().replace(".",",")+"/groups");
        notifyGroup = false;
        Runnable rGroups = new Runnable() {
            @Override
            public void run() {
                try {
                    myRefInitial.addValueEventListener( new ValueEventListener(){
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            System.out.println(notifyGroup);
                            groupsIDListTMP = new ArrayList<>();
                            groupsNameListTMP = new ArrayList<>();
                            for (DataSnapshot data : dataSnapshot.getChildren()){
                                if (!notifyGroup){
                                    id = data.getKey().toString();
                                    //nm = data.child("Name").getValue().toString();
                                    System.out.println(id+" - "+nm);
                                    indexGroup = groupsIDList.size();
                                    groupsIDList.add(indexGroup,id);
                                    //groupsNameList.add(indexGroup,nm);
                                }
                                else{
                                    //creo la lista temporanea x capire cosa sia successo
                                    id = data.getKey().toString();
                                    //nm = data.child("Name").getValue().toString();
                                    System.out.println(id+" - "+nm);
                                    indexGroupTMP = groupsIDListTMP.size();
                                    groupsIDListTMP.add(indexGroupTMP,id);
                                    //groupsNameListTMP.add(indexGroup,nm);
                                    //notifica
                                    //aggoirna le liste
                                }
                            }
                            if(notifyGroup){
                                if(groupsIDListTMP.size() > groupsIDList.size()){//è stato aggiunto un gruppo
                                    System.out.println("aggiunto");
                                    //cerco cosa è stato aggiunto
                                    groupsIDList.clear();
                                    groupsIDList.addAll(groupsIDListTMP);
                                    groupsNameList.clear();
                                    //groupsNameList.addAll(groupsNameListTMP);
                                    //manda la notifica

                                }
                                else if(groupsIDListTMP.size() < groupsIDList.size()){ // è stato eliminato un gruppo
                                    System.out.println("cancellato");
                                    groupsIDList.clear();
                                    groupsIDList.addAll(groupsIDListTMP);
                                    groupsNameList.clear();
                                    //groupsNameList.addAll(groupsNameListTMP);
                                    //manda la notifica
                                }
                            }
                            notifyGroup = true;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        };
        /*Thread tGroups = new Thread(rGroups);
        tGroups.start();*/
        // end P2

        myRefMembers = database.getReference("users/"+user.getEmail().replace(".",",")+"/Members");
        notifyGroup = false;
        Runnable rMembers = new Runnable() {
            @Override
            public void run() {
                try {
                    myRefMembers.addValueEventListener( new ValueEventListener(){
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            membersIDListTMP = new ArrayList<>();
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                if (!notifyMember) {
                                    id = data.getKey().toString();
                                    //nm = data.child("Name").getValue().toString();
                                    System.out.println(id+" - "+nm);
                                    indexMembers = membersIDList.size();
                                    membersIDList.add(indexMembers,id);
                                    //memberNameList.add(indexGroup,nm);
                                } else {
                                    //creo la lista temporanea x capire cosa sia successo
                                    id = data.getKey().toString();
                                    //nm = data.child("Name").getValue().toString();
                                    System.out.println(id+" - "+nm);
                                    indexMembersTMP = membersIDListTMP.size();
                                    membersIDListTMP.add(indexMembersTMP,id);
                                    //memberNameListTMP.add(indexGroup,nm);
                                    System.out.println("mando notifica per member");
                                    //aggoirna le liste
                                }

                            }
                            if(notifyMember){
                                if(membersIDListTMP.size() == membersIDList.size()){//è stato modificato qualcosa
                                    System.out.println("aggiunto");
                                    //cerco cosa è stato aggiunto
                                    groupsIDList.clear();
                                    groupsIDList.addAll(groupsIDListTMP);
                                    groupsNameList.clear();
                                    //groupsNameList.addAll(groupsNameListTMP);
                                    //manda la notifica

                                }
                            }
                            notifyMember = true;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        };
        Thread tMembers = new Thread(rMembers);
        tMembers.start();


        myRefExpenses = database.getReference("users/"+user.getEmail().replace(".",",")+"/Expenses");
        Runnable rExpences = new Runnable() {
            @Override
            public void run() {
                try {
                    myRefExpenses.addValueEventListener( new ValueEventListener(){
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            System.out.println("expences");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        };
        Thread tExpences = new Thread(rExpences);
        tExpences.start();

    }

            private void addNewListeners() {
                myRefGroups = database.getReference("users/"+user.getEmail().replace(".",",")+"/groups");
                myRefGroups.addChildEventListener(new ChildEventListener() {
                    private int ref = 0;
                    @Override
                    public  void onChildAdded(DataSnapshot snapshot, String previousChildName){
                        if(ref < 1){
                            System.out.println("E' troppo presto");                }
                /*id = snapshot.getKey().toString();
                nm = snapshot.child("Name").getValue().toString();
                System.out.println(id+" - "+nm);
                if (!groupsIDList.contains(id)){//se non lo contiene vuol dire che mi hanno aggiunto!!!
                    indexGroup = groupsIDList.size();
                    groupsIDList.add(indexGroup,id);
                    groupsNameList.add(indexGroup,nm);
                    System.out.println("aggiungi in lista");
                    //if(notify){ //NOTIFCA AGGIUNTA GRUPPO
                    System.out.println("mando la notifica");
                    sendNotification("You have been added to a new group!",id);
                    //}
                }
                else{
                    System.out.println("non AGGIUNGO IN LISTA");
                }*/
                        else{
                            System.out.println("E' troppo presto");
                        }

                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        System.out.println("CAMBIATO : "+dataSnapshot.child("Name").getValue().toString()+" - "+s);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        int i = groupsIDList.indexOf(dataSnapshot.getKey().toString());
                        groupsIDList.remove(i);
                        groupsNameList.remove(i);
                        sendNotification(dataSnapshot.child("Name").getValue().toString()+" has been removed","");
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError e){
                    }
                });
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
                mNotificationManager.notify(NOTIFICATION_ID, builder.build());


            }

    /*private boolean checkIfAppIsRunningInForeground() {
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
    }*/
        }
