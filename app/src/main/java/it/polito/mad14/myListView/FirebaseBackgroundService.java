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

import it.polito.mad14.GroupActivity;
import it.polito.mad14.MainActivity;
import it.polito.mad14.R;

public class FirebaseBackgroundService extends Service {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef,myRef2;
    private FirebaseUser user;
    private ArrayList<DatabaseReference> myRefsExpenses = new ArrayList<>();
    private ArrayList<DatabaseReference> myRefsMembers = new ArrayList<>();
    private ArrayList<String>groupsIDList = new ArrayList<>();
    private ArrayList<String>groupsNameList = new ArrayList<>();
    private int indexGroup = 0;
    private boolean notify;
    private String nm,id;
    //ArrayList<Summary> creditsList = new ArrayList<>();


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
        myRef = database.getReference("users/"+user.getEmail().replace(".",",")+"/groups");
        notify = false;
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public  void onChildAdded(DataSnapshot snapshot, String previousChildName){
                System.out.println(snapshot.getKey()+" - "+previousChildName);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println("RIMOSSO : "+dataSnapshot.getKey().toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError e){

            }



        /*myRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    id = data.getKey().toString();
                    nm = data.child("Name").getValue().toString();
                    System.out.println("STO ASCOLTANDO : "+id);
                    if (!groupsIDList.contains(id)){//se non lo contiene vuol dire che mi hanno aggiunto!!!
                        indexGroup = groupsIDList.size();
                        groupsIDList.add(indexGroup,id);
                        groupsNameList.add(indexGroup,nm);
                        myRefsExpenses.add(indexGroup,database.getReference("users/"+user.getEmail().replace(".",",")+"/groups/"+id+"NumMembers"));
                        ValueEventListener listener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                System.out.println("STA CAMBIANDO DAVVERO!!!!");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                throw databaseError.toException();
                            }
                        };
                        myRefsExpenses.get(indexGroup).addValueEventListener(listener);

                        //NOTIFICA NUOVO GRUPPO SE NON È LA PRIMA VOLTA !!
                        if(notify){ //PENSARE A QUESTO IF
                            System.out.println("mando la notifica");
                            sendNotification("You have been added to a new group!",id);
                        }
                        //AGGIUNGI IL RELATIVO LISTENER
                        //addMembersListener(id,nm);

                    }
                    else{
                        System.out.println("non AGGIUNGO IN LISTA");
                    }
                }
                notify = true;
            }
            public void onCancelled(DatabaseError e){

            }*/
        });
    }

    private void addMembersListener(final String IDGroup, final String NameGroup) {
        myRefsExpenses.add(indexGroup,database.getReference("users/"+user.getEmail().replace(".",",")+"/groups/"+IDGroup+"NumMembers"));
        myRefsExpenses.get(indexGroup).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("I MEMBRI STANNO CAMBAINDO IN "+NameGroup);
                //sendNotification("news about members in group "+NameGroup,IDGroup);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
        Intent notificationIntent = new Intent(this, GroupActivity.class);
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
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }
}
