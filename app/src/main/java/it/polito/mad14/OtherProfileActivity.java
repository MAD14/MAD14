package it.polito.mad14;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OtherProfileActivity extends AppCompatActivity {

    private String username;
    private String email;
    private String displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        // necessario per avere il tondo della foto profilo in primo piano anche con le API<21
        ImageButton imgbt = (ImageButton) findViewById(R.id.user_profile_photo);
        imgbt.bringToFront();

        email = getIntent().getStringExtra("email");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(email.replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("Username").getValue().toString();
                TextView tv = (TextView) findViewById(R.id.info1);
                tv.setText(username);
                displayName = dataSnapshot.child("Name").getValue().toString() + " " + dataSnapshot.child("Surname").getValue().toString();
                tv = (TextView) findViewById(R.id.user_profile_name);
                tv.setText(displayName);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        TextView tv = (TextView) findViewById(R.id.info2);
        tv.setText(email);

    }

    public void onClickAddAsFriend(View view) {

        //TODO gestione invio amicizia nella pagina main
        String UserID = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users/" + UserID + "/contacts/" + email.replace(".",","));
        String[] parts = displayName.split(" ");
        myRef.child("Name").setValue(parts[0]);
        myRef.child("Surname").setValue(parts[1]);
        myRef.child("Username").setValue(username);
        myRef.child("Email").setValue(email);

        Toast.makeText(OtherProfileActivity.this, "Added as friend", Toast.LENGTH_SHORT).show();

    }
}
