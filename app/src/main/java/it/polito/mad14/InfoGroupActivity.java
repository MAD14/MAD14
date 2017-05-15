package it.polito.mad14;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myListView.CustomAdapterInfoGroup;

public class InfoGroupActivity extends AppCompatActivity {

    private String IDGroup;
    private String groupName;
    private String description;
    private String dateCreation;
    private ListView list;
    private ArrayList<Contact> membersList = new ArrayList<>();
    private FirebaseDatabase database;
    private int indexMembers;
    private TextView tvDate,tvDescription;
    private DatabaseReference myRefName;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_group);
        tvDate = (TextView)findViewById(R.id.tv_date_creation);
        tvDescription = (TextView)findViewById(R.id.tv_description);
        IDGroup = getIntent().getStringExtra("IDGroup");
        database = FirebaseDatabase.getInstance();

        list = (ListView) findViewById(R.id.lv_members_group);
        list.setAdapter(new CustomAdapterInfoGroup(InfoGroupActivity.this,membersList));
        list.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        myRefName = database.getReference("groups/" + IDGroup );
        myRefName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupName = dataSnapshot.child("Name").getValue().toString();
                description = dataSnapshot.child("Description").getValue().toString();
                dateCreation = dataSnapshot.child("Date").getValue().toString();
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                setTitle(groupName);
                tvDate.setText(dateCreation);
                tvDescription.setText(description);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        myRef = database.getReference("groups/" + IDGroup + "/members");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    Contact tmp = new Contact(data.child("Name").getValue().toString(),
                            data.child("Surname").getValue().toString(),
                            data.child("Username").getValue().toString(),
                            data.child("Email").getValue().toString());
                    indexMembers = membersList.size();
                    membersList.add(indexMembers, tmp);
                }

                ((CustomAdapterInfoGroup)list.getAdapter()).setMembersList(membersList);
                list.invalidate();
                list.requestLayout();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getIntent().getStringExtra("groupID"), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }
}