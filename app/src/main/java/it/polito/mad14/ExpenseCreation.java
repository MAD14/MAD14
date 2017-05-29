package it.polito.mad14;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpenseCreation extends AppCompatActivity implements View.OnClickListener{

    private Button bt;
    final static int GET_IMAGE = 1;

    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private Set<String> contacts;
    private FirebaseDatabase database;
    private float nMembers;
    private String IDGroup;

    private ImageButton getExpenseImage;
    private Bitmap expenseImageBitmap;
    private String encodedExpenseImage,strImageUri;

    private EditText et_import, et_name, et_description;
    private String finalDescription;
    private boolean hasImage;
    private String date;

    private double oldValue;
    private double oldDebit;
    private DatabaseReference refUserDebit;
    private DatabaseReference creditBranch;
    private Double priceEach;
    private Double totCredit;
    private String authorDisplayName, et_author, name;

    private String debitorDisplayName;
    private DatabaseReference newRef,refDeb, userRef;

    private Spinner selectCurrency;
    private String selectedCurrency;
    private String groupCurrency, groupName;
    private ArrayAdapter<String> spinnerAdapter;
    private String price;

    private double EURtoUSD, USDtoEUR;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R .layout.activity_expense_creation);
        encodedExpenseImage = getString(R.string.no_image);
        hasImage = false;

        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyy HH:mm");
        date = format1.format(Calendar.getInstance().getTime());

        bt = (Button) findViewById(R.id.expense_button);
        bt.setOnClickListener(this);

        et_name = (EditText)findViewById(R.id.expense_name);
        et_description = (EditText)findViewById(R.id.expense_description);
        et_import = (EditText)findViewById(R.id.expense_import);
        et_import.setRawInputType(Configuration.KEYBOARD_12KEY);

        auth=FirebaseAuth.getInstance();
        authorDisplayName = auth.getCurrentUser().getDisplayName().replace("."," ");

        IDGroup= getIntent().getStringExtra("IDGroup");
        groupName = getIntent().getStringExtra("GroupName");

        contacts= new HashSet<>();

        database = FirebaseDatabase.getInstance();

        selectCurrency = (Spinner) findViewById(R.id.spinner_currency);
        String[] currencies = new String[]{"€","$"};
        spinnerAdapter = new ArrayAdapter<>(this,R.layout.spinner_item,currencies);
        selectCurrency.setAdapter(spinnerAdapter);

        DatabaseReference currencyRef = database.getReference("currencies");
        currencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                USDtoEUR = Double.parseDouble(dataSnapshot.child("USDtoEUR").getValue().toString());
                EURtoUSD = Double.parseDouble(dataSnapshot.child("EURtoUSD").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        DatabaseReference refCurrency=database.getReference("groups/"+IDGroup);

        refCurrency.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupCurrency=dataSnapshot.child("Currency").getValue().toString();
                int spinnerPosition = spinnerAdapter.getPosition(groupCurrency);
                selectCurrency.setSelection(spinnerPosition);
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

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
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(ExpenseCreation.this);
                startActivityForResult(chooseImageIntent, 1);
            }
        });
    }


    public void onClick(View v){
        et_author = auth.getCurrentUser().getEmail().replace(".",",");

        if(isImportValid(et_import.getText().toString()) && hasName(et_name.getText().toString())){

            selectedCurrency = selectCurrency.getSelectedItem().toString();
            if (!selectedCurrency.equals(groupCurrency)) {
                if (selectedCurrency.equals("$") && groupCurrency.equals("€")){
                    double oldPrice = Double.parseDouble(et_import.getText().toString());
                    double newPrice = (double)Math.round(oldPrice*USDtoEUR*100.0)/100.0;
                    price = String.valueOf(newPrice);
                } else if (selectedCurrency.equals("€") && groupCurrency.equals("$")){
                    double oldPrice = Double.parseDouble(et_import.getText().toString());
                    double newPrice = (double)Math.round(oldPrice*EURtoUSD*100.0)/100.0;
                    price = String.valueOf(newPrice);
                }
            } else {
                price = et_import.getText().toString();
            }

            DatabaseReference myRef = database.getReference("groups/"+IDGroup+"/items");
            userRef = database.getReference("users");
            DatabaseReference ref = myRef.child(et_name.getText().toString());

            ref.child("Price").setValue(price);
            if (!et_description.getText().toString().isEmpty()){
                finalDescription = et_description.getText().toString();
            } else {
                finalDescription = getString(R.string.no_expense_description);
            }
            ref.child("Description").setValue(finalDescription);
            ref.child("Name").setValue(et_name.getText().toString());
            ref.child("Author").setValue(et_author);
            ref.child("Image").setValue(encodedExpenseImage);
            ref.child("Date").setValue(date);
            ref.child("Currency").setValue(groupCurrency);

            // Calculation of credits and debits
            priceEach=Math.round((Double.valueOf(price)/nMembers)*100.0)/100.0;
            // Total credit the owner should receive
            totCredit=priceEach*(nMembers-1);

            // Updating credit field into creditor's group branch
            creditBranch=database.getReference("/users/"+et_author+"/groups/"+IDGroup+"/Credit/");
            creditBranch.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    oldValue=Double.valueOf(dataSnapshot.getValue().toString());
                    // Update the value
                    dataSnapshot.getRef().setValue(oldValue+totCredit);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // Updating debits branch of the group
            DatabaseReference refDebits=database.getReference("groups/"+IDGroup+"/debits");

            Iterator<String> it=contacts.iterator();
            while(it.hasNext()){

                name=it.next();
                if(!name.equals(et_author)) {
                    //updating of the group's info
                    newRef = refDebits.push();

                    // Unique key that identify the transaction
                    String key = newRef.getKey();
                    newRef.child("Product").setValue(et_name.getText().toString());
                    newRef.child("Receiver").setValue(et_author);
                    newRef.child("Sender").setValue(name);
                    newRef.child("Money").setValue(priceEach);
                    newRef.child("DisplayNameReceiver").setValue(authorDisplayName);
                    newRef.child("Currency").setValue(groupCurrency);

                    Log.e("authorDisplayName",authorDisplayName);

                    //updating debitors list inside the author
                    refDeb = userRef.child(et_author).child("credits").child(key);
                    refDeb.child("Group").setValue(IDGroup);
                    refDeb.child("Debitor").setValue(name);
                    refDeb.child("Money").setValue(priceEach);
                    refDeb.child("Currency").setValue(groupCurrency);


                    userRef.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            debitorDisplayName = dataSnapshot.child("Name").getValue().toString() + " " + dataSnapshot.child("Surname").getValue().toString();
                            newRef.child("DisplayNameSender").setValue(debitorDisplayName);
                            Log.e("debitor","debitor " +debitorDisplayName);
                            refDeb.child("DisplayName").setValue(debitorDisplayName);
                            if (name == et_author){
                                userRef.child(name).child("Expenses").child(IDGroup).child("Value").setValue("L'HO CREATO IO!!!");
                            } else {
                                userRef.child(name).child("Expenses").child(IDGroup).child("Value").setValue(Math.random());

                            }

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    //updating each creditor
                    DatabaseReference refCred = userRef.child(name).child("debits").child(key);
                    refCred.child("Group").setValue(IDGroup);
                    refCred.child("Paying").setValue(et_author);
                    refCred.child("DisplayName").setValue(authorDisplayName);
                    refCred.child("Money").setValue(priceEach);
                    refCred.child("Currency").setValue(groupCurrency);
                    refUserDebit = userRef.child(name).child("groups").child(IDGroup).child("Debit");
                    refUserDebit.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            oldDebit=Double.valueOf(dataSnapshot.getValue().toString());
                            // Updating each summary field for the general GroupView
                            dataSnapshot.getRef().setValue(oldDebit+priceEach);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });
                }
            }

            progressBar = (ProgressBar) findViewById(R.id.progressBar_expense);
            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(ExpenseCreation.this,GroupActivity.class);
                    intent.putExtra("author",et_author);
                    intent.putExtra("name",et_name.getText().toString());
                    intent.putExtra("import",price);
                    intent.putExtra("description",finalDescription);
                    intent.putExtra("expenseImage",hasImage);
                    intent.putExtra("date",date);
                    intent.putExtra("IDGroup",IDGroup);
                    intent.putExtra("Currency",groupCurrency);
                    intent.putExtra("GroupName",groupName);
                    setResult(RESULT_OK, intent);
                    startActivity(intent);
                    progressBar.setVisibility(View.GONE);
                    finish();
                }
            }, 2000);



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
                    expenseImageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), expenseImageBitmap);
                    getExpenseImage.setBackgroundDrawable(bDrawable);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    expenseImageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] byteArrayImage = baos.toByteArray();
                    encodedExpenseImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    hasImage = true;
                    Log.d("IMAGE", encodedExpenseImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
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
