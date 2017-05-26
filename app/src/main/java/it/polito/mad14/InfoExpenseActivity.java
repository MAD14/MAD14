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
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class InfoExpenseActivity extends AppCompatActivity {

    private TextView tvValue,tvDescription,tvAuthor,tvDate;
    private String IDGroup, expenseName, description, date, value, author;
    private FirebaseDatabase database;
    private String image;
    private String currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_expense);

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        CollapsingToolbarLayout toolbar = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);

        Intent intent = getIntent();
        IDGroup = getIntent().getStringExtra("IDGroup");
        expenseName = intent.getStringExtra("Name");
        value = intent.getStringExtra("Import");
        description = intent.getStringExtra("Description");
        author = intent.getStringExtra("Author");
        image = intent.getStringExtra("Image");
        date = intent.getStringExtra("Date");

        byte[] decodedImage = Base64.decode(image, Base64.DEFAULT);
        final Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
        BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), image);
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            toolbar.setBackgroundDrawable(bDrawable);
        } else {
            toolbar.setBackground(bDrawable);
        }

        setTitle(expenseName);
        tvAuthor = (TextView)findViewById(R.id.tv_author);
        tvAuthor.setText(author);
        tvValue = (TextView)findViewById(R.id.tv_value);
        tvValue.setText(value);
        tvDescription = (TextView)findViewById(R.id.tv_description);
        if (!description.equals(""))
            tvDescription.setText(description);
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvDate.setText(date);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_edit_expense);
        if (author.replace(".",",") == currentUser) {
            fab.setVisibility(View.VISIBLE);
            fab.bringToFront();
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(InfoExpenseActivity.this, EditExpenseActivity.class);
                    intent.putExtra("IDGroup",IDGroup);
                    intent.putExtra("Name",expenseName);
                    intent.putExtra("Date",date);
                    intent.putExtra("Description",description);
                    intent.putExtra("Image",image);
                    intent.putExtra("Author",author);
                    intent.putExtra("Value",value);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}
