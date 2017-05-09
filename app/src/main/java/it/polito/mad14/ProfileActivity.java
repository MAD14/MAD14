package it.polito.mad14;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private String name;
    private String surname;
    private String email;
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // necessario per avere il tondo della foto profilo in primo piano anche con le API<21
        ImageButton imgbt = (ImageButton)findViewById(R.id.user_profile_photo);
        imgbt.bringToFront();

        FirebaseAuth auth=FirebaseAuth.getInstance();
        email=auth.getCurrentUser().getEmail().replace(".",",");

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("Name").getValue().toString();
                surname = dataSnapshot.child("Surname").getValue().toString();
                username = dataSnapshot.child("Username").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });




        //TODO: fill the information with those coming from the database!!!!
        // - nome, immagine profilo, descrizione

        //TODO allow to switch to the email app to send an email? or to the phone to make a call?

    }
}