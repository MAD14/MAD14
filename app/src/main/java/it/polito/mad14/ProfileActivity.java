package it.polito.mad14;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private String email, bio, encodedImage, displayName, username, profileImage;
    private ImageView imgbt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.setTitle(R.string.title_your_profile);

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        // necessario per avere il tondo della foto profilo in primo piano anche con le API<21
        imgbt = (ImageView) findViewById(R.id.user_profile_photo);

        imgbt.bringToFront();

        FirebaseAuth auth=FirebaseAuth.getInstance();

        email = auth.getCurrentUser().getEmail();
        String email_key = email.replace(".",",");

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/" + email_key);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("Username").getValue().toString();
                if (dataSnapshot.hasChild("Bio")){
                    bio = dataSnapshot.child("Bio").getValue().toString();
                } else {
                    bio = getString(R.string.default_bio);
                }
                TextView tv = (TextView) findViewById(R.id.info1);
                tv.setText(username);
                tv = (TextView) findViewById(R.id.user_profile_short_bio);
                tv.setText(bio);

                if (dataSnapshot.hasChild("ProfileImage")) {
                    profileImage = dataSnapshot.child("ProfileImage").getValue().toString();
                    if (profileImage.equals("no_image")) {
                        imgbt.setImageResource(R.mipmap.person_icon_white);
                    } else {
                        encodedImage = profileImage;
                        byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                        BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), image);
                        imgbt.setImageDrawable(bDrawable);
                    }
                } else {
                    imgbt.setImageResource(R.mipmap.person_icon_white);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });

        displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replace("."," ");

        TextView tv = (TextView) findViewById(R.id.user_profile_name);
        tv.setText(displayName);
        tv = (TextView) findViewById(R.id.info2);
        tv.setText(email);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(ProfileActivity.this,LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.action_edit_profile:
                intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("userEmail",email);
                intent.putExtra("username",username);
                intent.putExtra("bio",bio);
                intent.putExtra("Name", displayName);
                startActivity(intent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}