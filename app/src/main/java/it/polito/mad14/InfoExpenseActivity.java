package it.polito.mad14;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class InfoExpenseActivity extends AppCompatActivity {

    private TextView tvValue,tvDescription,tvAuthor;
    private String IDGroup;
    private FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_expense);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String expenseName = intent.getStringExtra("Name");
        String value = intent.getStringExtra("Import");
        String description = intent.getStringExtra("Description");
        String author = intent.getStringExtra("Author");
//        String image = intent.getStringExtra("Image");

        setTitle(expenseName);
        tvAuthor = (TextView)findViewById(R.id.tv_author);
        tvAuthor.setText(author);
        tvValue = (TextView)findViewById(R.id.tv_value);
        tvValue.setText(value);
        tvDescription = (TextView)findViewById(R.id.tv_description);
        if (!description.equals(""))
            tvDescription.setText(description);

        IDGroup = getIntent().getStringExtra("IDGroup");
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRefName = database.getReference("groups/" + IDGroup);
        myRefName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //TODO: prendere dal db l'immagine della spesa

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }
}
