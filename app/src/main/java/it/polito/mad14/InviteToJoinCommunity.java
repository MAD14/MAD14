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
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad14.myDataStructures.Mail;
import it.polito.mad14.myListView.CustomAdapterInvitations;

public class InviteToJoinCommunity extends AppCompatActivity {

    private ArrayList<String> emailsToBeSent;
    private String[] listAddress = {""};
    private ListView list_invitation;
    private String nameSurnameString = "Elena Daraio"; //TODO sarà da sostituire con il nome dell'utente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_to_join_community);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emailsToBeSent = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_send_invitation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Mail inviteMail = new Mail();
                //new InviteMail("madapplication14@gmail.com","mobilecourse17");
                // Possibility2 (P2)
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {                            
                            Intent intent = getIntent();
                            //System.out.println("AAAAAAAAAAAAAAAAAa");
                            String key = intent.getStringExtra("sender");
                            //System.out.println(key);
                            DatabaseReference myRef = database.getReference("users");
                            myRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String name = dataSnapshot.child("Name").getValue().toString();
                                    //System.out.println(name);
                                    String surname = dataSnapshot.child("Surname").getValue().toString();
                                    //System.out.println(surname);
                                    String nameSurnameString = name+" "+surname;
                                    //System.out.println(nameSurnameString);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) { }
                            });
                            //System.out.println(nameSurnameString);

//                            Log.e("SendMail", "set_to " + listAddress[0]);
                            inviteMail.set_body("Hi! \n" +
                                    key + " invites you to join \"Shared Expenses\" Community. You can do it downloading the application from the store (or at this link: www.chesssonoforte.it).\n" +
                                    "This application will allow you to easily manage expenses shared with your friends.\n\n" +
                                    "We cannot wait for your association!\n" +
                                    "Your MAD14 team");
                            inviteMail.set_to(emailsToBeSent);
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

                Toast.makeText(InviteToJoinCommunity.this, "Emails have been sent.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(InviteToJoinCommunity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        list_invitation = (ListView) findViewById(R.id.lv_invitation);
        list_invitation.setAdapter(new CustomAdapterInvitations(InviteToJoinCommunity.this,emailsToBeSent));

    }


    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private boolean isEmailValid(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }

    public void onClickInviteButton(View view) {
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_invite);
        String emailInserted = actv.getText().toString();
        if (isEmailValid(emailInserted)) {
            emailsToBeSent.add(emailInserted);
            list_invitation.invalidate();
            list_invitation.requestLayout();
            actv.setText("");
        }
    }
}
