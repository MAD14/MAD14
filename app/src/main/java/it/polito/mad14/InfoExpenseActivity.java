package it.polito.mad14;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class InfoExpenseActivity extends AppCompatActivity {

    private TextView tvValue,tvDescription,tvAuthor,tvDate;
    private String IDGroup, expenseName, description, date, value, author, IDExpense;
    private FirebaseDatabase database;
    private String image,encodedImage;
    private String currentUser;
    private Bitmap imageBitmap;
    private CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_expense);
        this.setTitle(R.string.title_activity_info_expense);

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        Intent intent = getIntent();
        IDGroup = getIntent().getStringExtra("IDGroup");
        IDExpense = getIntent().getStringExtra("IDExpense");
        expenseName = intent.getStringExtra("Name");
        value = intent.getStringExtra("Import");
        description = intent.getStringExtra("Description");
        author = intent.getStringExtra("Author");
        image = intent.getStringExtra("Image");
        date = intent.getStringExtra("Date");

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(expenseName);

        if (image.equals("no_image")){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.expense_base);
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            collapsingToolbar.setBackground(d);
        } else{
            encodedImage = image;
            byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
            BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(), image);
            collapsingToolbar.setBackground(bDrawable);
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
        if (author.replace(".",",").equals(currentUser)) {
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
                    intent.putExtra("IDExpense",IDExpense);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
