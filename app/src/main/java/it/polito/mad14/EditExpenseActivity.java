package it.polito.mad14;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class EditExpenseActivity extends AppCompatActivity {

    private static final int CHANGE_IMAGE = 1;
    private String IDGroup, name, date, description, image, author, value, encodedImage,strImageUri,currentUser;
    private Bitmap imageBitmap;
    private FirebaseDatabase database;
    private DatabaseReference myRef, reference, newRef;
    private ProgressBar progressBar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ArrayList<String> membersList = new ArrayList<>();
    private EditText input;
    private TextView editDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        IDGroup = getIntent().getStringExtra("IDGroup");
        name = getIntent().getStringExtra("Name");
        date = getIntent().getStringExtra("Date");
        description = getIntent().getStringExtra("Description");
        image = getIntent().getStringExtra("Image");
        author = getIntent().getStringExtra("Author");
        value = getIntent().getStringExtra("Value");

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(name);

        editDescription = (TextView) findViewById(R.id.tv_description);


        // modifica immagine
        if (image.equals("no_image")){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.expense_base);
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            collapsingToolbar.setBackground(d);
        } else{
            encodedImage = image;
            byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
            BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), image);
            collapsingToolbar.setBackground(bDrawable);
        }


        // modifica nome

        // modifica valore
        ImageView et_value = (ImageView) findViewById(R.id.edit_expense_value);
        et_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });

        // modifica descrizione
        ImageView et_description = (ImageView) findViewById(R.id.edit_expense_description);
        et_description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder changeBioDialogue = new AlertDialog.Builder(EditExpenseActivity.this);
                changeBioDialogue.setTitle(getString(R.string.insert_description));
                input = new EditText(getApplicationContext());
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
                                DatabaseReference myRef = database.getReference("groups/"+ IDGroup + "/items/"+ name);
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
        });


        // modifica data
        ImageView et_date = (ImageView) findViewById(R.id.edit_expense_date);
        et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
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
                    collapsingToolbar= (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
                    collapsingToolbar.setBackground(bDrawable);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] byteArrayImage = baos.toByteArray();
                    encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    image = encodedImage;
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
        progressBar.bringToFront();

        myRef = database.getReference("groups/"+IDGroup+"/items"+name);
        myRef.child("Author").setValue(author);
        myRef.child("Date").setValue(date);
        myRef.child("Description").setValue(description);
        myRef.child("Image").setValue(image);
        myRef.child("Price").setValue(value);


        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (String user : membersList){
                    String userID = user;
                    newRef = database.getReference("users/"+userID+"/Expenses/"+IDGroup);
                    newRef.child("Value").setValue(Math.random());
                }
            }
        };
        Thread t = new Thread(r);
        t.start();


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EditExpenseActivity.this,getString(R.string.changes_applied),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditExpenseActivity.this,InfoExpenseActivity.class);
                intent.putExtra("IDGroup",IDGroup);
                intent.putExtra("Image",image);
                intent.putExtra("Name",name);
                intent.putExtra("Date",date);
                intent.putExtra("Author",author);
                intent.putExtra("Description",description);
                startActivity(intent);
                progressBar.setVisibility(View.GONE);
                finish();
            }
        },2000);


    }


}
