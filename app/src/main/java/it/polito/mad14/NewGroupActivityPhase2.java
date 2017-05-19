package it.polito.mad14;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myDataStructures.Mail;

public class NewGroupActivityPhase2 extends AppCompatActivity  implements View.OnClickListener{

    private ListView list_friends;
    //TODO: friends deve essere popolata degli amici  ++++ molto importante
    private ArrayList<Contact> friends;
    private int friendsIndex=0;
    private ArrayList<String> friends_added;

    private ArrayList<String> emailsToBeSent = new ArrayList<>();


    private int nFriends=0;

    private String groupName,groupAuthor,groupDescr,groupDate,groupImage,creator;
    private String IDGroup;
    private String MyID, newUser;
    private FirebaseDatabase database;
    private DatabaseReference temp_reference, myRefGroup;
    private Uri groupImageUri;
    private String noImage = "no_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_phase2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        groupName = getIntent().getStringExtra("Name");
        groupAuthor= getIntent().getStringExtra("Author");
        groupDescr= getIntent().getStringExtra("Description");
        groupDate= getIntent().getStringExtra("Date");
        groupImage = getIntent().getStringExtra("Image");
        if (!getIntent().getStringExtra("Image").equals(noImage)){
            groupImageUri = Uri.parse(getIntent().getStringExtra("Image"));
            try {
                Bitmap imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(groupImageUri));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] byteArrayImage = baos.toByteArray();
                groupImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        } else {groupImage = noImage;}

        IDGroup=getIntent().getStringExtra("IDGroup");
        Toast.makeText(NewGroupActivityPhase2.this, IDGroup,
                Toast.LENGTH_SHORT).show();
        MyID=FirebaseAuth.getInstance().getCurrentUser().getEmail();  // here no replace directly nel for

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

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef=database.getReference("users/"+
                FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",")
                        +"/contacts");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.hasChild("Image")) {
                        friends.add(
                                new Contact(data.child("Name").getValue().toString(),
                                        data.child("Surname").getValue().toString(), data.child("Username").getValue().toString(),
                                        data.child("Email").getValue().toString(),
                                        data.child("Image").getValue().toString()));
                    } else{
                        friends.add(
                                new Contact(data.child("Name").getValue().toString(),
                                        data.child("Surname").getValue().toString(), data.child("Username").getValue().toString(),
                                        data.child("Email").getValue().toString(),
                                        "no_image"));
                    }
                    friendsIndex++;
                }
                // adapter per suggerire gli amici in elenco
                final AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_friends);
                tv.setAdapter(new ArrayAdapter<>(
                        NewGroupActivityPhase2.this,android.R.layout.simple_list_item_single_choice,friends));
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
    }

    public void onClick(View view){
        AutoCompleteTextView et = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_friends);
        String tmp_name = et.getText().toString();
        String[] parts = tmp_name.split(" - ");
        String contUsername = parts[1];
        et.setText("");
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
        if (flag) {
            friends_added.add(nFriends,cont.getEmail().toString());

            emailsToBeSent.add(nFriends,cont.getEmail().toString());

            nFriends++;
            list_friends.invalidate();
            list_friends.requestLayout();
        } else {
            Toast.makeText(NewGroupActivityPhase2.this,"User not found",Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickCompletedAction(View view) {
        Toast.makeText(NewGroupActivityPhase2.this, "Group Created",
                Toast.LENGTH_SHORT).show();

        database = FirebaseDatabase.getInstance();

        friends_added.add(nFriends,MyID);
        nFriends++;

        Runnable r = new Runnable() {
            @Override
            public void run() {
                // Insertion of the group in each user
                DatabaseReference myRefUser = database.getReference("users");
                for (String user : friends_added) {
                    String newUser=user.replace(".",",");
                    DatabaseReference ref=myRefUser.child(newUser).child("groups").child(IDGroup);
                    ref.child("Name").setValue(groupName);
                    ref.child("Author").setValue(groupAuthor);
                    ref.child("Description").setValue(groupDescr);
                    ref.child("Date").setValue(groupDate);
                    ref.child("Image").setValue(groupImage);
                    ref.child("Credit").setValue("0");
                    ref.child("Debit").setValue("0");
                }

                //Insertion of each user into the group and set debits credits to 0 -> Other parameters can be added
                myRefGroup = database.getReference("groups/" + IDGroup + "/members/");
                for (String user : friends_added) {
                    newUser = user.replace(".", ",");
                    myRefGroup.child(newUser).child("Debits").setValue("0");
                    myRefGroup.child(newUser).child("Credits").setValue("0");
                }

                //

                myRefGroup = database.getReference("groups/"+ IDGroup + "/members");
                temp_reference = database.getReference("users");
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
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }
        };
        Thread t = new Thread(r);
        t.start();





//        //send e-mail to members
//        final Mail inviteMail = new Mail();
//        final DatabaseReference myRef = database.getReference("users");
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//                    String user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//                    String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replace("."," ");
//
//                    database = FirebaseDatabase.getInstance();
////                    Log.e("SendMail", "set_to " + listAddress[0]);
//                    inviteMail.set_body("Hi! \n" + displayName + " (email : "+
//                            user_email + ") is inviting you to join a group called " + groupName+
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

        Intent intent = new Intent(NewGroupActivityPhase2.this,MainActivity.class);
        intent.putExtra("IDGroup",IDGroup);
        intent.putExtra("Image",groupImage);
        intent.putExtra("Author",groupAuthor);
        intent.putExtra("Date",groupDate);
        intent.putExtra("Description",groupDescr);
        setResult(RESULT_OK,intent);
        finish();

    }


}

