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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myDataStructures.InviteMail;
import it.polito.mad14.myDataStructures.Mail;

public class NewGroupActivityPhase2 extends AppCompatActivity  implements View.OnClickListener{

    private ListView list_friends;
    //TODO: friends deve essere popolata degli amici  ++++ molto importante
    private ArrayList<Contact> friends;
    private int friendsIndex=0;
    private ArrayList<String> friends_added;

    private int nFriends=0;

    private String groupName,groupAuthor,groupDescr,groupDate,groupImage;
    private String IDGroup;
    private String MyID;


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
        groupImage= getIntent().getStringExtra("Image");
        IDGroup=getIntent().getStringExtra("IDGroup");
        Toast.makeText(NewGroupActivityPhase2.this, IDGroup,
                Toast.LENGTH_SHORT).show();

        MyID=FirebaseAuth.getInstance().getCurrentUser().getEmail();  // here no replace directly nel for
        // lista temporanea che può essere scritta sul db nel momento in cui si passa alla schermata successiva
        friends_added = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_invitation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCompletedAction(view);
            }
        });
        fab.bringToFront();

        // adapter per suggerire gli amici in elenco
        AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_friends);
        tv.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_single_choice,friends));

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


        friends=new ArrayList<>();

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef=database.getReference("users/"+
                FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",")
                        .toString()+"/contacts");

        // TODO da controllare dove viene aggiunto l'amico e creare il ramo contact decidendo quali info

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    friends.add(friendsIndex,new Contact(data.child("Name").toString(),data.child("Surname").toString(),
                            data.child("Username").toString(),data.child("Email").toString()));
                    friendsIndex++;

                }

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
        et.setText("Type the username or look for a friend");
        //TODO check se prende il nome completo selezionato o solo la stringa scritta
        Iterator<Contact> it=friends.iterator();
        boolean flag=false;
        while(it.hasNext()){
            Contact cont=it.next();
            if(cont.getUsername().equals(tmp_name))
                flag=true;
        }
        if (flag) {
            friends_added.add(tmp_name);
            list_friends.invalidate();
            list_friends.requestLayout();
        }
    }




    public void onClickCompletedAction(View view) {
        Toast.makeText(NewGroupActivityPhase2.this, "Group Created",
                Toast.LENGTH_SHORT).show();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //Insertion of each user into the group and set debits credits to 0 -> Other parameters can be added
        DatabaseReference myRefGroup=database.getReference("groups/"+IDGroup+"/members/");
        friends_added.add(nFriends,MyID);
        nFriends++;
        for (String user : friends_added) {
            String newUser=user.replace(".",",");
            myRefGroup.child(newUser).child("Debits").setValue("0");
            myRefGroup.child(newUser).child("Credits").setValue("0");
        }
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

        }

        Intent intent = new Intent(NewGroupActivityPhase2.this,MainActivity.class);
        intent.putExtra("IDGroup",IDGroup);
        startActivity(intent);
    }


}
