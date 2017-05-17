package it.polito.mad14;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpenseCreation extends AppCompatActivity implements View.OnClickListener{

    private Button bt;

    private FirebaseAuth auth;
    private Set<String> contacts;
    private FirebaseDatabase database;
    private float nMembers;
    private String IDGroup;

    private ImageButton getExpenseImage;
    private Bitmap expenseImageBitmap;
    private String encodedExpenseImage;

    private EditText et_import, et_name, et_description;
    private String finalDescription;
    private boolean hasImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R .layout.activity_expense_creation);
        encodedExpenseImage = getString(R.string.no_image);
        hasImage = false;

        bt = (Button) findViewById(R.id.expense_button);
        bt.setOnClickListener(this);

        et_name = (EditText)findViewById(R.id.expense_name);
        et_description = (EditText)findViewById(R.id.expense_description);
        et_import = (EditText)findViewById(R.id.expense_import);
        et_import.setRawInputType(Configuration.KEYBOARD_12KEY);

        auth=FirebaseAuth.getInstance();

        IDGroup= getIntent().getStringExtra("IDGroup");

        contacts= new HashSet<>();

        database = FirebaseDatabase.getInstance();
        DatabaseReference refMembers=database.getReference("groups/"+IDGroup+"/members");

        refMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nMembers=dataSnapshot.getChildrenCount();
                // collecting into a set the names of the members
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    contacts.add(data.getKey().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        getExpenseImage = (ImageButton) findViewById(R.id.insert_image);
        getExpenseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(cameraIntent, 0);
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(ExpenseCreation.this);
                startActivityForResult(chooseImageIntent, 1);
            }
        });
    }


    public void onClick(View v){
        String et_author = auth.getCurrentUser().getEmail().replace(".",",");

        if(isImportValid(et_import.getText().toString()) && hasName(et_name.getText().toString())){
            DatabaseReference myRef = database.getReference("groups/"+IDGroup+"/items");
            DatabaseReference userRef= database.getReference("users");
            DatabaseReference ref=myRef.child(et_name.getText().toString());
            ref.child("Price").setValue(et_import.getText().toString());
            if (!et_description.getText().toString().isEmpty()){
                finalDescription = et_description.getText().toString();
            } else {
                finalDescription = getString(R.string.no_expense_description);
            }
            ref.child("Description").setValue(finalDescription);
            ref.child("Name").setValue(et_name.getText().toString());
            ref.child("Author").setValue(et_author);
            ref.child("ExpenseImage").setValue(encodedExpenseImage);

            DatabaseReference refDebits=database.getReference("groups/"+IDGroup+"/debits");
            // 2 decimals
            double priceEach=Math.round((Double.valueOf(et_import.getText().toString())/nMembers)*100.0)/100.0;

            Iterator<String> it=contacts.iterator();
            while(it.hasNext()){

                String name=it.next();
                if(!name.equals(et_author)) {
                    //updating of the group's info
                    DatabaseReference newRef = refDebits.push();

                    //TODO punto 1
                    String key = newRef.getKey();

                    newRef.child("Product").setValue(et_name.getText().toString());
                    newRef.child("Receiver").setValue(et_author);
                    newRef.child("Sender").setValue(name);
                    newRef.child("Money").setValue(priceEach);
                    //updating debitors list inside the author
                    DatabaseReference refDeb = userRef.child(et_author).child("credits").child(key);
                    refDeb.child("Group").setValue(IDGroup);
                    refDeb.child("Debitor").setValue(name);
                    refDeb.child("Money").setValue(priceEach);
                    //updating each creditor
                    DatabaseReference refCred = userRef.child(name).child("debits").child(key);
                    refCred.child("Group").setValue(IDGroup);
                    refCred.child("Paying").setValue(et_author);
                    refCred.child("Money").setValue(priceEach);
                }
            }
            //notificare che le spese sono cambiate
            Intent intent = new Intent(ExpenseCreation.this,GroupActivity.class);
            intent.putExtra("author",et_author);
            intent.putExtra("name",et_name.getText().toString());
            intent.putExtra("import",et_import.getText().toString());
            intent.putExtra("description",finalDescription);
            intent.putExtra("expenseImage",hasImage);
            intent.putExtra("IDGroup",IDGroup);
            setResult(RESULT_OK, intent);
            startActivity(intent);
            finish();
        } else {
            if(!isImportValid(et_import.getText().toString())){
                et_import.setError(getString(R.string.error_invalid_import));
                et_import.requestFocus();
            } else if (et_import.getText().toString().isEmpty()){
                et_import.setError((getString(R.string.error_field_required)));
                et_import.requestFocus();
            }
            if(!hasName(et_name.getText().toString())){
                et_name.setError(getString(R.string.error_field_required));
                et_name.requestFocus();
            }
        }

    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            expenseImageBitmap = (Bitmap) data.getExtras().get("data");
            BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(),expenseImageBitmap);
            getExpenseImage.setBackgroundDrawable(bDrawable);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            expenseImageBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte[] byteArrayImage = baos.toByteArray();
            encodedExpenseImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            Uri imageUri = ImagePicker.getImageFromResult(this, resultCode, data);
            try {
                expenseImageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), expenseImageBitmap);
                getExpenseImage.setBackgroundDrawable(bDrawable);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                expenseImageBitmap.compress(Bitmap.CompressFormat.JPEG,25,baos);
                byte[] byteArrayImage = baos.toByteArray();
                encodedExpenseImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                hasImage = true;
                Log.d("IMAGE",encodedExpenseImage);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }

        }
    }

    private boolean hasName(String name){
        if (name.isEmpty()) return false;
        else return true;
    }

    private boolean isImportValid(String value) {
        Matcher matcher = Pattern.compile("^[0-9]+\\.[0-9]{2}$").matcher(value);
        if (matcher.find()) {
            return true;
        } else {
            matcher = Pattern.compile("^[0-9]+\\.[0-9]{1}$").matcher(value);
            if (matcher.find()) {
                value = value + "0";
                et_import.setText(value);
                return true;
            } else {
                matcher = Pattern.compile("^[0-9]+\\.$").matcher(value);
                if (matcher.find()) {
                    value = value + "00";
                    et_import.setText(value);
                    return true;
                } else {
                    matcher = Pattern.compile("^[0-9]+$").matcher(value);
                    if (matcher.find()) {
                        value = value + ".00";
                        et_import.setText(value);
                        return true;
                    }
                }

            }
        }
        if (value.isEmpty()) Toast.makeText(ExpenseCreation.this,getString(R.string.error_field_required), Toast.LENGTH_SHORT).show();

        return false;
    }

}
