package it.polito.mad14;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class NewGroupActivityPhase1 extends AppCompatActivity {
    final static int GET_IMAGE = 1;
    private Button createGroup;
    private ImageButton insertImage;
    private EditText editName;
    private EditText editDescription;
    private String groupName;
    private String groupDescription;
    private Spinner selectCurrency;

    private Bitmap targetImageBitmap = null;
    private String encodedImage;
    private String strImageUri, noImage = "no_image";
    private FirebaseAuth mAuth;
    private String date;
    private String groupCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_phase1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        encodedImage = null;
        strImageUri = noImage;

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyy HH:mm");
        date = format1.format(Calendar.getInstance().getTime());
        Log.e("date",date);
        createGroup = (Button) findViewById(R.id.group_create_button);
        editName = (EditText) findViewById(R.id.group_name);
        editDescription = (EditText) findViewById(R.id.group_description);

        selectCurrency = (Spinner) findViewById(R.id.spinner_currency);
        String[] currencies = new String[]{"€","$"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.spinner_item,currencies);
        selectCurrency.setAdapter(adapter);

        insertImage = (ImageButton) findViewById(R.id.insert_image);
        insertImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent chooseImageIntent = ImagePicker.getPickImageIntent(NewGroupActivityPhase1.this);
                startActivityForResult(chooseImageIntent, GET_IMAGE);

            }
        });

        mAuth=FirebaseAuth.getInstance();

        createGroup.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        groupName = editName.getText().toString();
                        if (EditIsAlphanumeric(groupName)) {
                            groupDescription = editDescription.getText().toString();
                            groupCurrency = selectCurrency.getSelectedItem().toString();
                            // DB ACCESS
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("groups").push();
                            String author = mAuth.getCurrentUser().getEmail().toString();

                            Map<String,String> dict = new HashMap<>();
                            dict.put("Name",groupName);
                            dict.put("Description",groupDescription);
                            dict.put("Author",author);
                            dict.put("Date",date);
                            dict.put("Currency",groupCurrency);

                            if (encodedImage == null) {
                                dict.put("Image", noImage);
                            } else {
                                dict.put("Image",encodedImage);
                            }
                            myRef.setValue(dict);

                            String IDGroup = myRef.getKey().toString();


                            Intent intent = new Intent(NewGroupActivityPhase1.this, NewGroupActivityPhase2.class);
                            intent.putExtra("IDGroup",IDGroup);
                            intent.putExtra("Name",groupName);
                            intent.putExtra("Description",groupDescription);
                            intent.putExtra("Author",author);
                            intent.putExtra("Date",date);
                            intent.putExtra("Currency",groupCurrency);
                            intent.putExtra("Image",strImageUri);
                            startActivity(intent);
                            finish();
                        } else {
                            groupName = "";
                            Toast.makeText(NewGroupActivityPhase1.this, getString(R.string.group_name_error),
                                    Toast.LENGTH_SHORT).show();

                        }

                    }


                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IMAGE){
            if (resultCode == RESULT_OK){
                Uri imageUri = ImagePicker.getImageFromResult(this, resultCode, data);
                strImageUri = imageUri.toString();

                CropImage.activity(imageUri)
                        .setMinCropResultSize(100,100)
                        .setMaxCropResultSize(1000,1000)
                        .start(this);

//                Intent intent = CropImage.activity(imageUri).getIntent(NewGroupActivityPhase1.this);
//                startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);

//                CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(this);


            }

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    targetImageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), targetImageBitmap);
                    insertImage.setImageDrawable(bDrawable);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    targetImageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] byteArrayImage = baos.toByteArray();
                    encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("CropImageError",error.getMessage());
            }

        }
    }


    private boolean EditIsAlphanumeric(String ToControl) {
        //TODO: Replace this with your own logic
        return ToControl.replaceAll("\\s+","").matches("[a-zA-Z0-9]+");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


}
