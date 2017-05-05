package it.polito.mad14;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ExpenseCreation extends AppCompatActivity implements View.OnClickListener{

    private Button bt;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R .layout.activity_expense_creation);

        bt = (Button) findViewById(R.id.expense_button);
        bt.setOnClickListener(this);

        auth=FirebaseAuth.getInstance();

    }


    public void onClick(View v){

        String et_author = auth.getCurrentUser().getEmail();
        EditText et_name = (EditText)findViewById(R.id.expense_name);
        EditText et_description = (EditText)findViewById(R.id.expense_description);
        EditText et_import = (EditText)findViewById(R.id.expense_import);

        String IDGRoup= getIntent().getStringExtra("IDGroup");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("groups/"+IDGRoup+"/items");

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
        finish();



    }

    public void onClickImage(View v){
        //TODO: inserire l'immagine della spesa
    }
}
