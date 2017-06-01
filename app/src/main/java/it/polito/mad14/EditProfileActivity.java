package it.polito.mad14;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class EditProfileActivity extends AppCompatActivity {

    final static int GET_IMAGE = 1;

    private String email, username, bio, encodedImage, name, providerId,strImageUri;
    private ImageView photo;
    private TextView editBio, editUsername;
    private Bitmap imageBitmap;
    private FirebaseDatabase database;

    private String googleProviderId = "google.com";
    private boolean googleUser;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        encodedImage = "no_image";
        googleUser = false;

        findViewById(R.id.edit_image).bringToFront();

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : user.getProviderData()){
            providerId = profile.getProviderId();
        }
        if (providerId.equals(googleProviderId)){
            googleUser = true;
            ImageView iv = (ImageView) findViewById(R.id.icon_edit_username);
            iv.setVisibility(View.GONE);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_finished);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCompletedAction(view);
            }
        });
        fab.bringToFront();

        email = getIntent().getStringExtra("userEmail");
        username = getIntent().getStringExtra("username");
        bio = getIntent().getStringExtra("bio");
        name = getIntent().getStringExtra("Name");

        photo = (ImageView)findViewById(R.id.user_profile_photo);
        photo.bringToFront();
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(EditProfileActivity.this);
                startActivityForResult(chooseImageIntent, GET_IMAGE);
            }
        });

        database=FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(email.replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("ProfileImage")){
                    encodedImage = dataSnapshot.child("ProfileImage").getValue().toString();
                    byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                    BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), image);
//                    photo.setBackgroundDrawable(bDrawable);
                    photo.setImageDrawable(bDrawable);
                } else {
                    photo.setImageResource(R.mipmap.person_icon_white);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_IMAGE){
            if (resultCode == RESULT_OK) {
                Uri imageUri = ImagePicker.getImageFromResult(this, resultCode, data);
                strImageUri = imageUri.toString();

                CropImage.activity(imageUri)
                        .setMinCropResultSize(100, 100)
                        .setMaxCropResultSize(1000, 1000)
                        .start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), imageBitmap);
                    photo.setBackgroundDrawable(bDrawable);
                    photo.setImageResource(android.R.color.transparent);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] byteArrayImage = baos.toByteArray();
                    encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" +
                            FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ","));
                    myRef.child("ProfileImage").setValue(encodedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
        }
        }
    }

    public void onClickChangeBio(View view){
        AlertDialog.Builder changeBioDialogue = new AlertDialog.Builder(EditProfileActivity.this);
        changeBioDialogue.setTitle(getString(R.string.insert_bio));
        input = new EditText(this);
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
        if (googleUser) {
            Toast.makeText(EditProfileActivity.this,
                    getString(R.string.not_possible_to_change_username),Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder changeUsernameDialogue = new AlertDialog.Builder(EditProfileActivity.this);
            changeUsernameDialogue.setTitle(getString(R.string.insert_username));
            input = new EditText(this);
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

    }

    public void onClickCompletedAction(View view) {

        Toast.makeText(EditProfileActivity.this,getString(R.string.changes_applied),Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EditProfileActivity.this,ProfileActivity.class);
        //intent.putExtra("IDGroup",IDGroup);
        startActivity(intent);
        finish();

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}

