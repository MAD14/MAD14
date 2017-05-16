package it.polito.mad14;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class EditProfileActivity extends AppCompatActivity {

    private String email, username, bio, encodedImage, name;
    private ImageButton photo;
    private TextView editBio, editUsername;
    private Bitmap imageBitmap;
    private FirebaseDatabase database;
    private boolean hasProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        encodedImage = "no_image";

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_finished);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCompletedAction(view);
            }
        });
        fab.bringToFront();

        hasProfileImage = getIntent().getBooleanExtra("hasProfileImage",false);
        email = getIntent().getStringExtra("userEmail");
        username = getIntent().getStringExtra("username");
        bio = getIntent().getStringExtra("bio");
        name = getIntent().getStringExtra("Name");

        TextView tv = (TextView) findViewById(R.id.user_profile_name);
        tv.setText(name);
        tv = (TextView) findViewById(R.id.info2);
        tv.setText(email);
        editBio = (TextView) findViewById(R.id.user_profile_short_bio);
        editBio.setText(bio);
        editBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickChangeBio(view);
            }
        });

        editUsername = (TextView) findViewById(R.id.info1);
        editUsername.setText(username);
        editUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickChangeUsername(view);
            }
        });


        photo = (ImageButton)findViewById(R.id.user_profile_photo);
        photo.bringToFront();
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(EditProfileActivity.this);
                startActivityForResult(chooseImageIntent, 1);
            }
        });

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(email.replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (hasProfileImage){
                    encodedImage = dataSnapshot.child("ProfileImage").getValue().toString();
                    byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                    BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), image);
                    photo.setBackgroundDrawable(bDrawable);
                } else {
                    photo.setImageResource(R.mipmap.person_icon_white);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            Uri imageUri = ImagePicker.getImageFromResult(this, resultCode, data);
            try {
                imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), imageBitmap);
                photo.setBackgroundDrawable(bDrawable);
                photo.setImageResource(android.R.color.transparent);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                byte[] byteArrayImage = baos.toByteArray();
                encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
            database = FirebaseDatabase.getInstance();
            DatabaseReference myRef=database.getReference("users/"+
                    FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",","));
            myRef.child("ProfileImage").setValue(encodedImage);
        }
    }

    public void onClickChangeBio(View view){
        AlertDialog.Builder changeBioDialogue = new AlertDialog.Builder(EditProfileActivity.this);
        changeBioDialogue.setTitle(getString(R.string.insert_bio));
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        changeBioDialogue.setView(input);
        changeBioDialogue.setPositiveButton(getString(R.string.positive_button_dialogue),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newBio = input.getText().toString();
                        editBio.setText(newBio);
                        database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef=database.getReference("users/"+
                                FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",","));
                        myRef.child("Bio").setValue(newBio);
                    }
                });
        changeBioDialogue.setNegativeButton(getString(R.string.negative_button_dialogue),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        changeBioDialogue.show();
    }

    public void onClickChangeUsername(View view){
        AlertDialog.Builder changeUsernameDialogue = new AlertDialog.Builder(EditProfileActivity.this);
        changeUsernameDialogue.setTitle(getString(R.string.insert_username));
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        changeUsernameDialogue.setView(input);
        changeUsernameDialogue.setPositiveButton(getString(R.string.positive_button_dialogue),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newUsername = input.getText().toString();
                        editUsername.setText(newUsername);
                        database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef=database.getReference("users/"+
                                FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",","));
                        myRef.child("Username").setValue(newUsername);
                    }
                });
        changeUsernameDialogue.setNegativeButton(getString(R.string.negative_button_dialogue),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        changeUsernameDialogue.show();
    }

    public void onClickCompletedAction(View view) {

        Toast.makeText(EditProfileActivity.this,"Changes applied",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EditProfileActivity.this,ProfileActivity.class);
        //intent.putExtra("IDGroup",IDGroup);
        startActivity(intent);
        finish();

    }

}
