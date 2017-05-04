package it.polito.mad14;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad14.myDataStructures.InviteMail;
import it.polito.mad14.myDataStructures.Mail;

public class NewGroupActivityPhase2 extends AppCompatActivity  implements View.OnClickListener{

    private ListView list_friends;
    //TODO: friends dovrebbe essere popolata degli amici --> potremmo tenere una mappa/lista che funga da cache
    // friends viene utilizzata poi nell'adapter a riga 55 per popolare i suggerimenti
    private String[] friends = {"Elena","Martina","Giulia","Eleonora","Elisabetta"};
    private ArrayList<String> friends_added;
    private String groupname;
    private String IDGroup;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_phase2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        groupname = getIntent().getExtras().getString("groupname");
        IDGroup = getIntent().getExtras().getString("groupID");
        description = getIntent().getExtras().getString("groupdescription");

        // lista temporanea che può essere scritta sul db nel momento in cui si passa alla schermata successiva
        friends_added = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_invitation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCompletedAction(view);
            }
        });
        fab.bringToFront();

        // adapter per suggerire gli amici in elenco
        AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_friends);
        tv.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_single_choice,friends));

        list_friends = (ListView) findViewById(R.id.lv_friends);
        list_friends.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {return friends_added.size();}
            @Override
            public Object getItem(int position) {return friends_added.get(position);}
            @Override
            public long getItemId(int position) {return 0;}
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.friend_selected_item, parent, false);
                TextView tv = (TextView) convertView.findViewById(R.id.friend_name);
                tv.setText(friends_added.get(position));
                return convertView;
            }
        });

    }

    public void onClick(View view){
        AutoCompleteTextView et = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_friends);
        String tmp_name = et.getText().toString();
        et.setText("");
        //TODO check se prende il nome completo selezionato o solo la stringa scritta
        friends_added.add(tmp_name);
        list_friends.invalidate();
        list_friends.requestLayout();
    }




    public void onClickCompletedAction(View view) {
        Toast.makeText(NewGroupActivityPhase2.this, "Group Created",
                Toast.LENGTH_SHORT).show();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        for (String user : friends_added) {
            DatabaseReference myRefUser = database.getReference("users/" + user.toString() + "/groups/" + IDGroup);
//            myRefUser.child("Name").setValue(groupname);
//            myRefUser.child("Description").setValue(description);
        }

        Intent intent = new Intent(NewGroupActivityPhase2.this,MainActivity.class);
        startActivity(intent);
    }


}
