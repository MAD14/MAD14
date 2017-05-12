package it.polito.mad14;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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
    private EditText nameCapture;

    // Adapter for myList
    private CustomAdapterContactSuggested adapter;

    private String name,surname,username,email,image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        list = (ListView) findViewById(R.id.list_view_contact_suggestion);
        adapter = new CustomAdapterContactSuggested(getApplicationContext(), partialNames);
        list.setAdapter(adapter);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    searchNames.add(new Contact(data.child("Name").getValue().toString(),
                            data.child("Surname").getValue().toString(), data.child("Username").getValue().toString(),
                            data.child("Email").getValue().toString()));
                }
                ((CustomAdapterContactSuggested)list.getAdapter()).setPartialNames(searchNames);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        nameCapture = (EditText) findViewById(R.id.edit_name_search);

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


    private void AlterAdapter() {
        if (nameCapture.getText().toString().isEmpty()) {
            partialNames.clear();
            adapter.notifyDataSetChanged();
        }
        else {
            partialNames.clear();
            for (int i = 0; i < searchNames.size(); i++) {

                // TODO Qui da implementare la ricerca nome? cognome?email?
                if (searchNames.get(i).toString().toUpperCase().contains(nameCapture.getText().toString().toUpperCase())) {
                    //partialNames.add(new Contact(searchNames.get(i).toString(),"","username","email")); //TODO poi basterà prendere il contatto dall'altra lista
                    partialNames.add(searchNames.get(i));
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

}
