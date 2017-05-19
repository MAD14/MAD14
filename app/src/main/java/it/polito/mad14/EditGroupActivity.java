package it.polito.mad14;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.ArrayList;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myListView.CustomAdapterInfoGroup;

public class EditGroupActivity extends AppCompatActivity {

    private String IDGroup, groupName, dateCreation, description, encodedImage;
    private Bitmap imageBitmap;
    private ImageButton imgbt;
    private ImageButton photo;
    private TextView editDescription;
    private ImageButton editName;
    private EditText input;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private int indexMembers;
    private ArrayList<Contact> membersList = new ArrayList<>();
    private ListView list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        IDGroup = getIntent().getStringExtra("IDGroup");
        groupName = getIntent().getStringExtra("Name");
        dateCreation = getIntent().getStringExtra("Date");
        description = getIntent().getStringExtra("Description");

        CollapsingToolbarLayout toolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar.setTitle(groupName);


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


// TODO: bisogna inserire l'immagine anche nell'attività base
//        photo = (ImageButton)findViewById(R.id.group_photo);
//        photo.bringToFront();
//        photo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent chooseImageIntent = ImagePicker.getPickImageIntent(EditGroupActivity.this);
//                startActivityForResult(chooseImageIntent, 1);
//            }
//        });


//        findViewById(R.id.edit_image).bringToFront();

        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("groups");
        myRef.child(IDGroup).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               // TODO management of the image
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
            DatabaseReference myRef=database.getReference("groups/"+IDGroup);
            myRef.child("GroupImage").setValue(encodedImage);
        }
    }

    public void onClickCompletedAction(View view) {

        Toast.makeText(EditGroupActivity.this,getString(R.string.changes_applied),Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EditGroupActivity.this,InfoGroupActivity.class);
        intent.putExtra("IDGroup",IDGroup);
        startActivity(intent);
        finish();

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
        //TODO manage the change of the group image

    }
}
