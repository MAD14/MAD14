package it.polito.mad14;

import android.content.Intent;
import android.net.ConnectivityManager;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad14.myDataStructures.Mail;
import it.polito.mad14.myListView.CustomAdapterInvitations;

public class InviteToJoinCommunity extends AppCompatActivity {

    private ArrayList<String> emailsToBeSent;
    private String[] listAddress = {""};
    private ListView list_invitation;
    private FirebaseDatabase database;
    private Mail inviteMail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_to_join_community);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        emailsToBeSent = new ArrayList<>();

        database = FirebaseDatabase.getInstance();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_send_invitation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                inviteMail = new Mail();
                //new InviteMail("madapplication14@gmail.com","mobilecourse17");

                // Possibility2 (P2)
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//                            String user_db = user_email.replace(".",",");
//                            Log.e("email_db"," is " + user_db);
                            String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replace("."," ");

//                            String name = database.getReference("users/"+ user_db).child("Name").toString();
//                            String surname = database.getReference("users/"+ user_db).child("Surname").getKey();


//                            Log.e("SendMail", "set_to " + listAddress[0]);
                            inviteMail.set_body(getString(R.string.hi) + "\n" +
                                    displayName + " (" + user_email + ") " + getString(R.string.mail_body_1));
                            inviteMail.set_to(emailsToBeSent);
                            inviteMail.set_subject(getString(R.string.invitation_mail_subject));
                            inviteMail.send();
                        } catch (Exception e) {
                            Log.e("SendMail", e.getMessage(), e);
                        }
                    }
                };
                Thread t = new Thread(r);
                t.start();
                // end P2

                Toast.makeText(InviteToJoinCommunity.this, getString(R.string.emails_sent), Toast.LENGTH_SHORT).show();
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
