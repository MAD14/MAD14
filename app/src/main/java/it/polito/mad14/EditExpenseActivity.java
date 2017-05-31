package it.polito.mad14;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
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
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditExpenseActivity extends AppCompatActivity {

    private static final int CHANGE_IMAGE = 1;
    private String IDGroup, name, date, description, image, author, value, encodedImage,strImageUri, IDExpense,oldName;
    private Bitmap imageBitmap;
    private FirebaseDatabase database;
    private DatabaseReference myRefNew, reference, newRef;
    private ProgressBar progressBar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ArrayList<String> membersList = new ArrayList<>();
    private EditText input;
    private TextView editDescription,tv_value, tv_date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        database = FirebaseDatabase.getInstance();

        IDGroup = getIntent().getStringExtra("IDGroup");
        IDExpense = getIntent().getStringExtra("IDExpense");
        name = getIntent().getStringExtra("Name");
        date = getIntent().getStringExtra("Date");
        description = getIntent().getStringExtra("Description");
        image = getIntent().getStringExtra("Image");
        author = getIntent().getStringExtra("Author");
        value = getIntent().getStringExtra("Value");
        oldName = name;

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(name);

        editDescription = (TextView) findViewById(R.id.tv_description);

        TextView tv_author = (TextView) findViewById(R.id.tv_author);
        tv_author.setText(author);

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

        // modifica valore
        ImageView et_value = (ImageView) findViewById(R.id.edit_expense_value);
        tv_value = (TextView) findViewById(R.id.tv_value);
        tv_value.setText(value);
        et_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder changeBioDialogue = new AlertDialog.Builder(EditExpenseActivity.this);
                changeBioDialogue.setTitle(getString(R.string.insert_value));
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
                                String newValue = input.getText().toString();
                                value = newValue;
                                tv_value.setText(newValue);
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

        // modifica descrizione
        ImageView et_description = (ImageView) findViewById(R.id.edit_expense_description);
        editDescription.setText(description);
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
        final ImageView et_date = (ImageView) findViewById(R.id.edit_expense_date);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_date.setText(date);
        et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Calendar now = Calendar.getInstance();
                final Calendar c = Calendar.getInstance();

                DatePickerDialog dpd = new DatePickerDialog(EditExpenseActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                date = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                                tv_date.setText(date);
                            }
                        }, c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DATE));
                dpd.show();


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

        DatabaseReference myRefNew = database.getReference("groups/"+ IDGroup + "/items/"+ IDExpense);
        myRefNew.child("Price").setValue(value);
        myRefNew.child("Date").setValue(date);
        myRefNew.child("Description").setValue(description);
        myRefNew.child("Name").setValue(name);
        myRefNew.child("Image").setValue(encodedImage);


        Runnable r = new Runnable() {
            @Override
            public void run() {
                Map<String, Object> updates = new HashMap<>();
                updates.put("Action","MOD-E-"+oldName);
                updates.put("Value",Math.random());
                for (String user : membersList){
                    //creazione mappa
                    String userID = user;
                    //upload della mappa
                    database.getReference("users").child(userID).child("Not").child(IDGroup).updateChildren(updates);
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
                intent.putExtra("Import",value);
                startActivity(intent);
                progressBar.setVisibility(View.GONE);
                finish();
            }
        },2000);
    }

    public void onClickChangeName(View view) {
        AlertDialog.Builder changeUsernameDialogue = new AlertDialog.Builder(EditExpenseActivity.this);
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
                        name = newName;
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
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(EditExpenseActivity.this);
        startActivityForResult(chooseImageIntent, CHANGE_IMAGE);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}
