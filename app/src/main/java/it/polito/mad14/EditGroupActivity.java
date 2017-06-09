package it.polito.mad14;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myListView.CustomAdapterInfoGroup;

public class EditGroupActivity extends AppCompatActivity {

    private static final int CHANGE_IMAGE = 1;
    private String IDGroup, groupName, dateCreation, description, encodedImage,groupAuthor,strImageUri;
    private Bitmap imageBitmap;
    private ImageButton imgbt;
    private ImageButton photo;
    private TextView editDescription;
    private ImageButton editName;
    private EditText input;
    private FirebaseDatabase database;
    private DatabaseReference myRef,newRef, reference;
    private int indexMembers;
    private ArrayList<String> membersList = new ArrayList<>();
    private ListView list;
    private String groupPhoto;
    private ProgressBar progressBar;
    private String currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        this.setTitle(R.string.title_edit_group);

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        IDGroup = getIntent().getStringExtra("IDGroup");
        groupName = getIntent().getStringExtra("Name");
        dateCreation = getIntent().getStringExtra("Date");
        description = getIntent().getStringExtra("Description");
        groupPhoto = getIntent().getStringExtra("Image");
        groupAuthor = getIntent().getStringExtra("Author");

        CollapsingToolbarLayout toolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar.setTitle(groupName);

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        TextView tv = (TextView) findViewById(R.id.tv_date_creation);
        tv.setText(dateCreation);
        editDescription = (TextView) findViewById(R.id.tv_description);
        if (!editDescription.equals(""))
            editDescription.setText(description);
        editDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickChangeDescription(view);
            }
        });

        tv = (TextView) findViewById(R.id.tv_date_creation);
        tv.setText(dateCreation);

        editName = (ImageButton) findViewById(R.id.edit_name_button);

        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickChangeName(view);
            }
        });

//        findViewById(R.id.edit_image).bringToFront();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("groups/"+IDGroup+"/members");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    membersList.add(data.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        encodedImage = groupPhoto;
        byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
        BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), image);
        toolbar.setBackground(bDrawable);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_finished_edit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCompletedAction(view);
            }
        });
        fab.bringToFront();

        findViewById(R.id.edit_group_description).bringToFront();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_IMAGE) {
            Uri imageUri = ImagePicker.getImageFromResult(this, resultCode, data);
            strImageUri = imageUri.toString();

            CropImage.activity(imageUri)
                    .setMinCropResultSize(100,100)
                    .setMaxCropResultSize(1000,1000)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), imageBitmap);
                    CollapsingToolbarLayout toolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
                    toolbar.setBackground(bDrawable);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] byteArrayImage = baos.toByteArray();
                    encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    groupPhoto = encodedImage;
                    database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("groups/" + IDGroup);
                    myRef.child("Image").setValue(encodedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void onClickCompletedAction(View view) {
        progressBar = (ProgressBar)findViewById(R.id.progressBar_edit);
        progressBar.setVisibility(View.VISIBLE);

        myRef = database.getReference("users/"+currentUser+"/groups/"+IDGroup);
        myRef.child("Name").setValue(groupName);
        myRef.child("Description").setValue(description);
        myRef.child("Image").setValue(groupPhoto);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Map<String, Object> updates = new HashMap<>();

                updates.put("Action","MOD-M-"+currentUser);

                updates.put("Name",groupName);
                updates.put("Value",Math.random());
                for (String user : membersList){
                    String userID = user;
                    database.getReference("users").child(user).child("Not").child(IDGroup).updateChildren(updates);
                    newRef = database.getReference("users/"+userID+"/groups/"+IDGroup);
                    newRef.child("Name").setValue(groupName);
                    newRef.child("Description").setValue(description);
                    newRef.child("Image").setValue(encodedImage);
                }
            }
        };
        Thread t = new Thread(r);
        t.start();


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EditGroupActivity.this,getString(R.string.changes_applied),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditGroupActivity.this,InfoGroupActivity.class);
                intent.putExtra("IDGroup",IDGroup);
                intent.putExtra("Image",groupPhoto);
                intent.putExtra("Name",groupName);
                intent.putExtra("Date",dateCreation);
                intent.putExtra("Author",groupAuthor);
                intent.putExtra("Description",description);
                startActivity(intent);
                progressBar.setVisibility(View.GONE);
                finish();
            }
        },2000);


    }

    public void onClickChangeDescription(View view){
        AlertDialog.Builder changeBioDialogue = new AlertDialog.Builder(EditGroupActivity.this);
        changeBioDialogue.setTitle(getString(R.string.insert_description));
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
                        String newDescription = input.getText().toString();
                        description = newDescription;
                        if (newDescription.equals("")) {
                            editDescription.setText("-");
                        }else {
                            editDescription.setText(newDescription);
                        }
                        DatabaseReference myRef = database.getReference("groups/"+ IDGroup);
                        myRef.child("Description").setValue(newDescription);
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

    public void onClickChangeName(View view) {
        AlertDialog.Builder changeUsernameDialogue = new AlertDialog.Builder(EditGroupActivity.this);
        changeUsernameDialogue.setTitle(getString(R.string.insert_group_name));
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
                        String newName = input.getText().toString();
                        groupName = newName;
                        CollapsingToolbarLayout toolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
                        toolbar.setTitle(newName);
                        database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("groups/" + IDGroup);
                        myRef.child("Name").setValue(newName);
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

    public void onClickChangePhoto(View view){
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(EditGroupActivity.this);
        startActivityForResult(chooseImageIntent, CHANGE_IMAGE);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
