package it.polito.mad14;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myDataStructures.Mail;

public class AddNewMembersToGroup extends AppCompatActivity {
    private String IDGroup,newUser,actualUser;
    private ArrayList<String> friends_added;
    private ArrayList<Contact> friends;
    private ArrayList<String> members;
    private int friendsIndex = 0,membersIndex = 0;
    private ListView list_friends;
    private int nFriends = 0;
    private ArrayList<String> emailsToBeSent = new ArrayList<>();
    private String groupName,groupAuthor,groupDescr,groupDate,groupImage,creator,oldValue;
    private FirebaseDatabase database;
    private Mail inviteMail;
    private DatabaseReference temp_reference,myRefGroup,myRefGroup2;
    private AutoCompleteTextView actv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_members_to_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent myIntent = getIntent();
        IDGroup = myIntent.getStringExtra("IDgroup");
        groupName = getIntent().getStringExtra("Name");
        groupAuthor= getIntent().getStringExtra("Author");
        groupDescr= getIntent().getStringExtra("Description");
        groupDate= getIntent().getStringExtra("Date");
        groupImage = getIntent().getStringExtra("Image");

        friends_added = new ArrayList<>();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_invitation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCompletedAction(view);
            }
        });
        fab.bringToFront();

        friends=new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef=database.getReference("users/"+
                FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",")
                +"/contacts");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if(data.child("Image").getValue()!=null){
                    friends.add(friendsIndex,new Contact(data.child("Name").getValue().toString(),
                            data.child("Surname").getValue().toString(),
                            data.child("Username").getValue().toString(),
                            data.child("Email").getValue().toString(),
                            data.child("Image").getValue().toString()));
                    friendsIndex++;}
                    else{
                        friends.add(friendsIndex,new Contact(data.child("Name").getValue().toString(),
                                data.child("Surname").getValue().toString(),
                                data.child("Username").getValue().toString(),
                                data.child("Email").getValue().toString(),
                                "no_image"));

                    }
                }
                // adapter per suggerire gli amici in elenco
                actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_friends);
                actv.setAdapter(new ArrayAdapter<>(
                        AddNewMembersToGroup.this,android.R.layout.simple_list_item_single_choice,friends));
                list_friends = (ListView) findViewById(R.id.lv_friends);
                list_friends.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {return friends_added.size();}
                    @Override
                    public Object getItem(int position) {return friends_added.get(position);}
                    @Override
                    public long getItemId(int position) {return 0;}
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null)
                            convertView = getLayoutInflater().inflate(R.layout.friend_selected_item, parent, false);
                        TextView tv = (TextView) convertView.findViewById(R.id.friend_name);
                        tv.setText(friends_added.get(position));
                        return convertView;
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Failed to read value.", error.toException());

            }
        });

        members = new ArrayList<>();


        DatabaseReference myRef2 = database.getReference("groups/"+IDGroup+"/members");

        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    members.add(membersIndex,data.getKey().toString().replace(",","."));
                    friendsIndex++;
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Failed to read value.", error.toException());

            }
        });
        Iterator<String> it2=members.iterator();

        while(it2.hasNext()){
            String cont=it2.next();
            System.out.println(cont);
        }
    }

    public void onClick(View view){//quando pigio add
        AutoCompleteTextView et = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_friends);
        String tmp_name = et.getText().toString();
        String[] parts = tmp_name.split(" - ");
        if (parts.length == 2){
            String contUsername = parts[1];
            et.setText("");
            //TODO check se prende il nome completo selezionato o solo la stringa scritta
            Iterator<Contact> it=friends.iterator();

            boolean flag=false;
            Contact cont=null;
            while(it.hasNext()){
                cont=it.next();
                if(cont.getUsername().toString().equals(contUsername)) {
                    flag = true;
                    break;
                }
            }
            if (flag && !members.contains(cont.getEmail().toString())) {
                friends_added.add(nFriends,cont.getEmail().toString());
                emailsToBeSent.add(nFriends,cont.getEmail().toString());

                nFriends++;
                list_friends.invalidate();
                list_friends.requestLayout();
            } else {
                if(members.contains(cont.getEmail().toString())){
                    Toast.makeText(AddNewMembersToGroup.this,getString(R.string.member_already_in_the_group),Toast.LENGTH_SHORT).show();
                }
                else{
                    System.out.println("non trovato");
                    Toast.makeText(AddNewMembersToGroup.this,getString(R.string.user_not_found),Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            //System.out.println("ti ho presoooo :P");
            Toast.makeText(AddNewMembersToGroup.this,getString(R.string.user_not_found),Toast.LENGTH_SHORT).show();
        }
        Iterator<String> it2=emailsToBeSent.iterator();

        while(it2.hasNext()){
            String cont=it2.next();
            System.out.println(cont);
        }
    }

    public void onClickCompletedAction(View view) {

        Iterator<String> it2=emailsToBeSent.iterator();

        while(it2.hasNext()){
            String cont=it2.next();
            System.out.println(cont);
        }
        Toast.makeText(AddNewMembersToGroup.this, getString(R.string.members_added),Toast.LENGTH_SHORT).show();
        //System.out.print("author " + groupAuthor);

        // Insertion of the group in each user
        DatabaseReference myRefUser = database.getReference("users");
        for (String user : friends_added) {
            String newUser = user.replace(".",",");
            DatabaseReference ref = myRefUser.child(newUser).child("groups").child(IDGroup);
            ref.child("Name").setValue(groupName);
            ref.child("Author").setValue(groupAuthor);
            ref.child("Description").setValue(groupDescr);
            ref.child("Date").setValue(groupDate);
            ref.child("Image").setValue(groupImage);
        }

        myRefGroup = database.getReference("groups/" + IDGroup + "/members/");
        for (String user : friends_added) {
            String newUser = user.replace(".", ",");
            myRefGroup.child(newUser).child("Debits").setValue("0");
            myRefGroup.child(newUser).child("Credits").setValue("0");
        }

        temp_reference = database.getReference("users");

        myRefGroup2 = database.getReference("groups/" + IDGroup + "/members/");
        myRefGroup2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Log.e("data key", data.getKey());
                    Log.e("1---------", "----");
                    actualUser = data.getKey();
                    temp_reference.child(actualUser).child("Members").child(IDGroup).child("Name").setValue(groupName);
                    temp_reference.child(actualUser).child("Members").child(IDGroup).child("Value").setValue(Math.random());
                    Log.e("3---------", "bdfgd");

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        temp_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (String user : friends_added)
                {
                    newUser = user.replace(".", ",");
                    myRefGroup.child(newUser).child("Name").setValue(dataSnapshot.child(newUser).child("Name").getValue().toString());
                    myRefGroup.child(newUser).child("Surname").setValue(dataSnapshot.child(newUser).child("Surname").getValue().toString());
                    myRefGroup.child(newUser).child("Email").setValue(dataSnapshot.child(newUser).child("Email").getValue().toString());
                    myRefGroup.child(newUser).child("Username").setValue(dataSnapshot.child(newUser).child("Username").getValue().toString());

                    temp_reference.child(newUser).child("Members").child(IDGroup).child("Value").setValue("x");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        
//        //mando le mail
//        inviteMail = new Mail();
//
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//                    //String user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//                    //String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replace("."," ");
//
////                    Log.e("SendMail", "set_to " + listAddress[0]);
//                    inviteMail.set_body("Hi! \n"  + " (email : "+
//                             ") is inviting you to join a group called " + groupName+
//                            " whose code is "+IDGroup+".\n\n" +
//
//                            "We cannot wait for your association!\n" +
//                            "Your MAD14 team");
//                    inviteMail.set_to(emailsToBeSent);
//                    inviteMail.set_subject("Invite to join MAD14");
//                    inviteMail.send();
//                } catch (Exception e) {
//                    Log.e("SendMail", e.getMessage(), e);
//                }
//            }
//        };
//        Thread t = new Thread(r);
//        t.start();

        Intent intent = new Intent(AddNewMembersToGroup.this,GroupActivity.class);
        intent.putExtra("IDGroup",IDGroup);
        startActivity(intent);
    }

}
