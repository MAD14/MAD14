package it.polito.mad14;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

     String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // necessario per avere il tondo della foto profilo in primo piano anche con le API<21
        ImageButton imgbt = (ImageButton)findViewById(R.id.user_profile_photo);
        imgbt.bringToFront();

        FirebaseAuth auth=FirebaseAuth.getInstance();
        String email = auth.getCurrentUser().getEmail();
        String email_key = email.replace(".",",");

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(email_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("Username").getValue().toString();
                TextView tv = (TextView) findViewById(R.id.info1);
                tv.setText(username);
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });
        String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replace("."," ");
        TextView tv = (TextView) findViewById(R.id.user_profile_name);
        tv.setText(displayName);
        tv = (TextView) findViewById(R.id.info1);
        tv.setText(username);
        tv = (TextView) findViewById(R.id.info2);
        tv.setText(email);
    }
}