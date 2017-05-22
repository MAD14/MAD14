package it.polito.mad14;


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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myListView.CustomAdapterContactSuggested;

public class AddNewContacts extends AppCompatActivity {

    // List of all contacts
    private ArrayList<Contact> searchNames = new ArrayList<>();
    // Filtered list of contacts after user begins typing in search field
    private ArrayList<Contact> partialNames = new ArrayList<>();

    // List of names matching criteria are listed here
    private ListView list;

    // Field where user enters his search criteria
    private AutoCompleteTextView nameCapture;

    // Adapter for myList
    private CustomAdapterContactSuggested adapter;

    private DatabaseReference myRef;
    private String actualName;

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

        actualName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replace("."," ");

        actualName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replace("."," ");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        list = (ListView) findViewById(R.id.list_view_contact_suggestion);
        adapter = new CustomAdapterContactSuggested(getApplicationContext(), partialNames);
        list.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        loading.setVisibility(View.VISIBLE);
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
    private void AlterAdapter() {
        if (nameCapture.getText().toString().isEmpty()) {
            partialNames.clear();
            adapter.notifyDataSetChanged();
        }
        else {
            partialNames.clear();
            for (int i = 0; i < searchNames.size(); i++) {
                if (searchNames.get(i).toString().toUpperCase().contains(nameCapture.getText().toString().toUpperCase()) &&
                        !(searchNames.get(i).toString().toUpperCase().contains(actualName.toUpperCase()))) {
                    partialNames.add(searchNames.get(i));
                    adapter.notifyDataSetChanged();

                }
                adapter.notifyDataSetChanged();
            }
        }
    }

}
