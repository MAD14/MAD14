package it.polito.mad14;


import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
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

import java.util.ArrayList;
import java.util.Iterator;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myListView.CustomAdapterContactSuggested;

public class AddNewContacts extends AppCompatActivity {

    // List of all contacts
    private ArrayList<Contact> searchNames = new ArrayList<>();
    // Filtered list of contacts after user begins typing in search field
    private ArrayList<Contact> partialNames = new ArrayList<>();

    private ArrayList<Contact> actualFriends = new ArrayList<>();

    // List of names matching criteria are listed here
    private ListView list;

    // Field where user enters his search criteria
    private AutoCompleteTextView nameCapture;

    // Adapter for myList
    private CustomAdapterContactSuggested adapter;

    private DatabaseReference myRef;
    private String actualEmail,currentUser;

    private ProgressBar progressBar;
    private TextView loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        loading = (TextView) findViewById(R.id.loading_tv);


        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        actualEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        list = (ListView) findViewById(R.id.list_view_contact_suggestion);
        adapter = new CustomAdapterContactSuggested(getApplicationContext(), partialNames);
        list.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        loading.setVisibility(View.VISIBLE);

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference myRefUser = database.getReference("users").child(currentUser);
        myRefUser.child("contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    actualFriends.add(
                            new Contact(data.child("Name").getValue().toString(),
                                    data.child("Surname").getValue().toString(), data.child("Username").getValue().toString(),
                                    data.child("Email").getValue().toString(),
                                    "no_image"));
                }
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            searchNames.add(
                                    new Contact(data.child("Name").getValue().toString(),
                                            data.child("Surname").getValue().toString(), data.child("Username").getValue().toString(),
                                            data.child("Email").getValue().toString(),
                                            "no_image"));
                        }

                        searchNames = cleanDuplicates(searchNames,actualFriends);
                        progressBar.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);

                        nameCapture = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_new_contacts);
                        nameCapture.setVisibility(View.VISIBLE);

                        AlterAdapter();

                        nameCapture.addTextChangedListener(new TextWatcher() {
                            // As the user types in the search field, the list is
                            @Override
                            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                                AlterAdapter();
                            }
                            // Not used for this program
                            @Override
                            public void afterTextChanged(Editable arg0) {
                            }
                            // Not uses for this program
                            @Override
                            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



    }
    private void AlterAdapter() {
        if (nameCapture.getText().toString().isEmpty()) {
            partialNames.clear();
            adapter.notifyDataSetChanged();
        }
        else {
            partialNames.clear();
            for (int i = 0; i < searchNames.size(); i++) {
                if (searchNames.get(i).toString().toUpperCase().contains(nameCapture.getText().toString().toUpperCase()) &&
                        !(searchNames.get(i).getEmail().equals(actualEmail))) {
                    partialNames.add(searchNames.get(i));
                    adapter.notifyDataSetChanged();

                }
                adapter.notifyDataSetChanged();
            }
        }
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


    public ArrayList<Contact> cleanDuplicates(ArrayList<Contact> list1, ArrayList<Contact> list2){

        for (Iterator<Contact> it = list1.iterator(); it.hasNext(); ){
            Contact tmp1 = it.next();
                for (Contact tmp2 : list2) {
                    if (tmp1.getEmail().equals(tmp2.getEmail())) {
                        it.remove();
                        break;
                    }
                }
        }
        return list1;
    }
}
