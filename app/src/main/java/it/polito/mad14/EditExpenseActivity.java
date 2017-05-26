package it.polito.mad14;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class EditExpenseActivity extends AppCompatActivity {

    private String IDGroup, name, date, description, image, author, value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        IDGroup = getIntent().getStringExtra("IDGroup");
        name = getIntent().getStringExtra("Name");
        date = getIntent().getStringExtra("Date");
        description = getIntent().getStringExtra("Description");
        image = getIntent().getStringExtra("Image");
        author = getIntent().getStringExtra("Author");
        value = getIntent().getStringExtra("Value");

        // modifica immagine

        // mpdifica nome

        // modifica valore
        ImageView et_value = (ImageView) findViewById(R.id.edit_expense_value);
        et_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });

        // modifica descrizione
        ImageView et_description = (ImageView) findViewById(R.id.edit_expense_description);
        et_description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
        // modifica data
        ImageView et_date = (ImageView) findViewById(R.id.edit_expense_date);
        et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                // scrittura su db e ritorno ad attività precedente
            }
        });
    }
}
