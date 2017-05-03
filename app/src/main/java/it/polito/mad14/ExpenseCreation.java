package it.polito.mad14;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class ExpenseCreation extends AppCompatActivity implements View.OnClickListener{

    private Button bt;
    private ImageButton getExpenseImage;
    private Bitmap expenseImageBitmap;
    private String encodedExpenseImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R .layout.activity_expense_creation);

        bt = (Button) findViewById(R.id.expense_button);
        bt.setOnClickListener(this);

        getExpenseImage = (ImageButton) findViewById(R.id.insert_image);
        getExpenseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 0);
            }
        });

    }


    public void onClick(View v){
        //TODO sistemare che l'autore è l'utente stesso
        //TODO scrittura su firebase

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getDisplayName();
        String email = user.getEmail();
        String uid = user.getUid();
        Toast.makeText(
                ExpenseCreation.this, name + "\n" + email + "\n" + uid,Toast.LENGTH_SHORT).show();


        /*String et_author = "me stesso";
        EditText et_name = (EditText)findViewById(R.id.expense_name);
        EditText et_description = (EditText)findViewById(R.id.expense_description);
        EditText et_import = (EditText)findViewById(R.id.expense_import);

        String groupName= getIntent().getStringExtra("groupname");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("groups/"+groupName+"/items");

        DatabaseReference ref=myRef.child(et_name.getText().toString());
        ref.child("Price").setValue(et_import.getText().toString());
        ref.child("Description").setValue(et_description.getText().toString());
        ref.child("Name").setValue(et_name.getText().toString());
        ref.child("Author").setValue(et_author);

//        ListView list = (ListView) findViewById(R.id.list_view_expenses);
//        ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();

        Intent intent = new Intent();
        intent.putExtra("author",et_author);
        intent.putExtra("name",et_name.getText().toString());
        intent.putExtra("import",et_import.getText().toString());
        intent.putExtra("description",et_description.getText().toString());
        setResult(RESULT_OK, intent);
        finish();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            expenseImageBitmap = (Bitmap) data.getExtras().get("data");
            BitmapDrawable bDrawable = new BitmapDrawable(getApplicationContext().getResources(),expenseImageBitmap);
            getExpenseImage.setBackgroundDrawable(bDrawable);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            expenseImageBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte[] byteArrayImage = baos.toByteArray();
            encodedExpenseImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        }
    }

}
