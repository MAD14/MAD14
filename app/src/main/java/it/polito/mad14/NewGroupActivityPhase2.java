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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myDataStructures.InviteMail;
import it.polito.mad14.myDataStructures.Mail;

public class NewGroupActivityPhase2 extends AppCompatActivity  implements View.OnClickListener{

    private ListView list_friends;
    //TODO: friends dovrebbe essere popolata degli amici --> potremmo tenere una mappa/lista che funga da cache
    // friends viene utilizzata poi nell'adapter a riga 55 per popolare i suggerimenti
    //private String[] friends = {"Elena","Martina","Giulia","Eleonora","Elisabetta"};
    private ArrayList<Contact> friends;
    private int friendsIndex=0;
    private ArrayList<String> friends_added;
    private int nFriends=0;
    private ArrayList<String> emailsToBeSent;
    private String[] listAddress = {""};
    private ListView list_invitation;
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

        emailsToBeSent = new ArrayList<>();

        list_invitation = (ListView) findViewById(R.id.lv_invitation);
        list_invitation.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {return emailsToBeSent.size();}
            @Override
            public Object getItem(int position) {return emailsToBeSent.get(position);}
            @Override
            public long getItemId(int position) {return 0;}
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.contact_item, parent, false);
                TextView tv = (TextView) findViewById(R.id.tv_contact_email);
//                tv.setText(emailsToBeSent.get(position));
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
        et.setText("");
        //TODO check se prende il nome completo selezionato o solo la stringa scritta
        friends_added.add(tmp_name);
        list_friends.invalidate();
        list_friends.requestLayout();
    }


    public void onClickInvite(View view) throws Exception{
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_invite);
        String emailInserted = actv.getText().toString();
        if (isEmailValid(emailInserted)) {
            listAddress[0] = emailInserted;
            emailsToBeSent.add(emailInserted);
            list_invitation.invalidate();
            list_invitation.requestLayout();
            Toast.makeText(NewGroupActivityPhase2.this, "Email sent",
                    Toast.LENGTH_SHORT).show();
            final Mail inviteMail = new Mail();
            //new InviteMail("madapplication14@gmail.com","mobilecourse17");

            // Possibility1 (P1)
            /*new AsyncTask<Void, Void, Void>() {
                @Override
                public Void doInBackground(Void... arg) {
                    try {
                        inviteMail.set_to(listAddress);
                        inviteMail.send();
                    } catch (Exception e) {
                        Log.e("SendMail", e.getMessage(), e);
                    }
                    return null;
                }
            }.execute(); */
            // end P1
            // Possibility2 (P2)
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        Log.e("SendMail","set_to " + listAddress[0]);
                        inviteMail.set_body("Hello! \n" +
                                "You received an invite to join a group in MAD14 from one of your friend.\n" +
                                "Lets join our community downloading our app at this link:\n" +
                                "https://teddyapplication.com/welcome\n" +
                                "To join the group .... \n" +
                                "Your MAD14 team");
                        inviteMail.set_to(listAddress);
                        inviteMail.set_subject("Invite to join MAD14");
                        inviteMail.send();
                    } catch (Exception e) {
                        Log.e("SendMail", e.getMessage(), e);
                    }
                }
            };

            Thread t = new Thread(r);
            t.start();
            // end P2
            actv.setText("");
        } else{
            Toast.makeText(NewGroupActivityPhase2.this, "Email not valid.",
                    Toast.LENGTH_SHORT).show();
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
        startActivity(intent);
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private boolean isEmailValid(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        return matcher.find();
    }

}
