package it.polito.mad14;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myListView.CustomAdapterContactSuggested;

public class AddNewContacts extends AppCompatActivity implements View.OnClickListener{

    // List of all contacts //TODO: deve essere riempita dei contatti dal db e resa un ArrayList<Contact>
    private ArrayList<String> searchNames = new ArrayList<String>();
    // Filtered list of contacts after user begins typing in search field
    private ArrayList<Contact> partialNames = new ArrayList<>();

    // List of names matching criteria are listed here
    private ListView list;

    // Field where user enters his search criteria
    private EditText nameCapture;

    // Adapter for myList
    private CustomAdapterContactSuggested adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set list adapter
        list = (ListView) findViewById(R.id.list_view_contact_suggestion);
        adapter = new CustomAdapterContactSuggested(this,partialNames);
        list.setAdapter(adapter);


        //TODO: searchNames deve essere popolata dei nomi-cognomi dal db!
        searchNames.add("Tom Arnold");
        searchNames.add("Zeb Arnold");
        searchNames.add("Dan Bateman");
        searchNames.add("Tommy Canders");
        searchNames.add("Elijah Arnman");
        searchNames.add("Tomas Muster");
        searchNames.add("Stefan Edberg");
        searchNames.add("Ivan Lendl");


        nameCapture = (EditText) findViewById(R.id.edit_name_search);
        nameCapture.setText("Tom");

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
                // TODO Auto-generated method stub

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
                if (searchNames.get(i).toString().toUpperCase().contains(nameCapture.getText().toString().toUpperCase())) {
                    partialNames.add(new Contact(searchNames.get(i).toString(),"","username","email")); //TODO poi basterà prendere il contatto dall'altra lista
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View view){
        Intent intent = new Intent(AddNewContacts.this,OtherProfileActivity.class);
        //TODO bisognerà passare le info di quale profilo si vuole vedere
        startActivity(intent);
    }
}
