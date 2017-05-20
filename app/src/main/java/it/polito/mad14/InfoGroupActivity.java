package it.polito.mad14;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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
    private String groupDescription;
    private String dateCreation;
    private String groupAuthor;
    private String encodedImage;
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

        groupName = getIntent().getStringExtra("Name");
        groupDescription = getIntent().getStringExtra("Description");
        dateCreation = getIntent().getStringExtra("Date");
        groupAuthor = getIntent().getStringExtra("Author");

        CollapsingToolbarLayout toolbar = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        toolbar.setTitle(groupName);

        tvDate.setText(dateCreation);

        if (!groupDescription.equals("")){
            tvDescription.setText(groupDescription);
        } else {
            tvDescription.setText("-");
        }

        encodedImage = getIntent().getStringExtra("Image");

        if (encodedImage.equals("no_image")) {
            toolbar.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.backgroundDark));
            toolbar.getBackground().setAlpha(3);
        } else {
            byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
            BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), image);
            toolbar.setBackground(bDrawable);
        }

        myRef = database.getReference("groups/" + IDGroup + "/members");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    Contact tmp;
                    if (data.hasChild("ProfileImage")) {
                        tmp = new Contact(data.child("Name").getValue().toString(),
                                data.child("Surname").getValue().toString(),
                                data.child("Username").getValue().toString(),
                                data.child("Email").getValue().toString(),
                                data.child("ProfileImage").getValue().toString());
                    } else {
                        tmp = new Contact(data.child("Name").getValue().toString(),
                                data.child("Surname").getValue().toString(),
                                data.child("Username").getValue().toString(),
                                data.child("Email").getValue().toString(),
                                "no_image");
                    }
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_info_group);
        fab.bringToFront();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InfoGroupActivity.this, EditGroupActivity.class);
                intent.putExtra("IDGroup",IDGroup);
                intent.putExtra("Name",groupName);
                intent.putExtra("Date",dateCreation);
                intent.putExtra("Description",groupDescription);
                intent.putExtra("Image",encodedImage);
                startActivity(intent);
                finish();
            }
        });

        list.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

    }


}