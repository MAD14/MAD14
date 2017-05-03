package it.polito.mad14;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class NewGroupActivityPhase1 extends AppCompatActivity {

    private Button createGroup;
    private ImageButton insertImage;
    private EditText editName;
    private EditText editDescription;
    private String groupName;
    private String groupDescription;

    private Bitmap targetImageBitmap = null;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_phase1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createGroup = (Button) findViewById(R.id.group_create_button);
        editName = (EditText) findViewById(R.id.group_name);
        editDescription = (EditText) findViewById(R.id.group_description);
        insertImage = (ImageButton) findViewById(R.id.insert_image);

        insertImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        });

        createGroup.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        groupName = editName.getText().toString();
                        if (EditIsAlphanumeric(groupName)) {
                            groupDescription = editDescription.getText().toString();
                            // DB ACCESS
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("groups").push();
                            Map<String,String> dict=new HashMap<>();
                            dict.put("Name",groupName);
                            dict.put("Description",groupDescription);
                            dict.put("Image", encodedImage);
                            myRef.setValue(dict);

                            String IDGroup=myRef.getKey();

                            Intent intent = new Intent(NewGroupActivityPhase1.this, NewGroupActivityPhase2.class);
                            intent.putExtra("groupname", groupName);
                            intent.putExtra("groupdescription", groupDescription);
                            intent.putExtra("groupID",IDGroup);
                            startActivity(intent);

                        } else {
                            groupName = "";
                            Toast.makeText(NewGroupActivityPhase1.this, "Group Name is not valid.\nMust contains numbers or letters",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }


                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            Uri targetImageUri = data.getData();
            try {
                targetImageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetImageUri));
                BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(),targetImageBitmap);
                insertImage.setBackgroundDrawable(bDrawable);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                targetImageBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] byteArrayImage = baos.toByteArray();
                encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean EditIsAlphanumeric(String ToControl) {
        //TODO: Replace this with your own logic
        return ToControl.replaceAll("\\s+","").matches("[a-zA-Z0-9]+");
    }



}
