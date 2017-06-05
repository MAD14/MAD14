package it.polito.mad14;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad14.myDataStructures.Expense;

public class ExpenseCreation extends AppCompatActivity implements View.OnClickListener{

    private static final int RESULT_BACK = 12;
    private Button bt;
    final static int GET_IMAGE = 1;

    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private Set<String> contacts;
    private FirebaseDatabase database;
    private float nMembers;
    private String IDGroup,sound;

    private ImageButton getExpenseImage;
    private Bitmap expenseImageBitmap;
    private String encodedExpenseImage,strImageUri;

    private EditText et_import, et_name, et_description;
    private String finalDescription;
    private String date;

    private double oldValue;
    private DatabaseReference refUserDebit;
    private DatabaseReference creditBranch;
    private DatabaseReference refDebits;
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
    private String IDExpense;
    private String key;

    private double EURtoUSD, USDtoEUR;
    private String debits;
    private DatabaseReference refExp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R .layout.activity_expense_creation);
        encodedExpenseImage = "no_image";

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

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
        sound = getIntent().getStringExtra("Sound");

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
                    if (!data.getKey().equals(et_author))
                        contacts.add(data.getKey());
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

            refExp = database.getReference("groups/"+IDGroup+"/items").push();
            userRef = database.getReference("users");
            IDExpense = refExp.getKey();


            refExp.child("Price").setValue(price);
            if (!et_description.getText().toString().isEmpty()){
                finalDescription = et_description.getText().toString();
            } else {
                finalDescription = getString(R.string.no_expense_description);
            }
            refExp.child("Description").setValue(finalDescription);
            refExp.child("Name").setValue(et_name.getText().toString());
            refExp.child("Author").setValue(et_author);
            refExp.child("Image").setValue(encodedExpenseImage);
            refExp.child("Date").setValue(date);
            refExp.child("Currency").setValue(groupCurrency);

            // Calculation of credits and debits
            priceEach=Math.round((Double.valueOf(price)/nMembers)*100.0)/100.0;
            // Total credit the owner should receive
            totCredit=priceEach*(nMembers-1);

            // Updating credit field into creditor's group branch
            creditBranch=database.getReference("/users/"+et_author+"/groups/"+IDGroup);
            creditBranch.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Double cOldDebit=Double.valueOf(dataSnapshot.child("Debit").getValue().toString());
                    Double cOldCredit=Double.valueOf(dataSnapshot.child("Credit").getValue().toString());
                    Double cDiff = totCredit - cOldDebit;
                    Double cNewDebit, cNewCredit;
                    if (cDiff>=0){
                        cNewDebit = 0.0;
                        cNewCredit = cOldCredit + cDiff;
                    } else {
                        cNewCredit = cOldCredit;
                        cNewDebit = Math.round((cOldDebit - totCredit)*100.0)/100.0;
                    }
                    // Update the value
                    dataSnapshot.child("Debit").getRef().setValue(cNewDebit);
                    dataSnapshot.child("Credit").getRef().setValue(cNewCredit);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // Updating debits branch of the group


            refDebits = database.getReference("groups/"+IDGroup+"/debits");

            Iterator<String> it=contacts.iterator();
            debits = "";

            while(it.hasNext()) {
                name = it.next();
                if (!name.equals(et_author)) {
                    Log.e("name", name);

                    Runnable r = new Runnable() {
                        String currentName = name;

                        @Override
                        public void run() {
                            userRef.child(currentName).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    newRef = refDebits.push();
                                    // Unique key that identify the transaction
                                    key = newRef.getKey();
                                    debits = debits + key + ",";
                                    refExp.child("Debits").setValue(debits.substring(0,debits.length()-1));

                //                            Log.e("key expense pre", key);
                                    Map<String, Object> expenseMap = new HashMap<>();
                                    expenseMap.put("Product", et_name.getText().toString());
                                    expenseMap.put("Receiver", et_author);
                                    expenseMap.put("Sender", dataSnapshot.getKey());
                                    expenseMap.put("Money", priceEach);
                                    expenseMap.put("DisplayNameReceiver", authorDisplayName);
                                    expenseMap.put("Currency", groupCurrency);
                                    // riga 10 implica che tutto questo sia in un listener!
                                    debitorDisplayName = dataSnapshot.child("Name").getValue().toString() + " " + dataSnapshot.child("Surname").getValue().toString();
                                    expenseMap.put("DisplayNameSender", debitorDisplayName);

                //                            Log.e("key expense during", key);
                                    newRef.updateChildren(expenseMap);

                //                            Log.e("authorDisplayName",authorDisplayName);

                                    Map<String, Object> debitorMap = new HashMap<>();
                                    //updating debitors list inside the author
                                    debitorMap.put("Group", IDGroup);
                                    debitorMap.put("Debitor", currentName);
                                    debitorMap.put("Money", priceEach);
                                    debitorMap.put("Currency", groupCurrency);
                                    debitorMap.put("DisplayName", debitorDisplayName);
                                    userRef.child(et_author).child("credit").child(key).updateChildren(debitorMap);

                                    Map<String, Object> updates1 = new HashMap<>();
                                    updates1.put("Action", "ADD-E-" + auth.getCurrentUser().getEmail() + et_name.getText().toString());
                                    updates1.put("Value", Math.random());
                                    userRef.child(currentName).child("Expenses").child(IDGroup).updateChildren(updates1);

                                    Map<String, Object> creditorMap = new HashMap<>();

                                    //updating each creditor
                                    DatabaseReference refCred = userRef.child(currentName).child("debits");
                                    creditorMap.put("Group", IDGroup);
                                    creditorMap.put("Paying", et_author);
                                    creditorMap.put("DisplayName", authorDisplayName);
                                    creditorMap.put("Money", priceEach);
                                    creditorMap.put("Currency", groupCurrency);
                                    Log.e("currentName", currentName);
                                    refCred.child(key).updateChildren(creditorMap);

                                    // questa scrittura bisogna valutare se metterla con le hash map o no
                                    // TODO: MARCO E' QUESTO!
                                    refUserDebit = userRef.child(currentName).child("groups").child(IDGroup);
                                    refUserDebit.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Double oldDebit = Double.valueOf(dataSnapshot.child("Debit").getValue().toString());
                                            Double oldCredit = Double.valueOf(dataSnapshot.child("Credit").getValue().toString());
                                            Double diff = Math.round((priceEach - oldCredit)*100.0)/100.0;
                                            Double newCredit, newDebit;
                                            if (diff>=0){
                                                newCredit = 0.0;
                                                newDebit = oldDebit + diff;
                                            } else {
                                                newCredit = oldCredit - priceEach;
                                                newDebit = oldDebit;
                                            }
                                            // Updating each summary field for the general GroupView
                                            dataSnapshot.child("Debit").getRef().setValue(newDebit);
                                            dataSnapshot.child("Credit").getRef().setValue(newCredit);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }

                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });


                        }
                    };
                    Thread t = new Thread(r);
                    t.start();
                }

            }

            progressBar = (ProgressBar) findViewById(R.id.progressBar_expense);
            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(ExpenseCreation.this,GroupActivity.class);
                    intent.putExtra("IDExpense",IDExpense);
                    intent.putExtra("author",et_author);
                    intent.putExtra("name",et_name.getText().toString());
                    intent.putExtra("import",price);
                    intent.putExtra("description",finalDescription);
                    intent.putExtra("date",date);
                    intent.putExtra("IDGroup",IDGroup);
                    intent.putExtra("GroupCurrency",groupCurrency);
                    intent.putExtra("GroupName",groupName);

                    intent.putExtra("Sound",sound);
                    Toast.makeText(ExpenseCreation.this,groupName,Toast.LENGTH_SHORT).show();

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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onBackPressed(){
        Log.e("Back","pressed");
        Intent intent = new Intent(ExpenseCreation.this,GroupActivity.class);
        intent.putExtra("IDGroup",IDGroup);
        intent.putExtra("Name",groupName);
        intent.putExtra("Sound",sound);
        startActivity(intent);
        finish();
    }

}
