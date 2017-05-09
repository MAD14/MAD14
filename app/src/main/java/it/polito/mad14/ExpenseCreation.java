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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExpenseCreation extends AppCompatActivity implements View.OnClickListener{

    private Button bt;
    private FirebaseAuth auth;
    private Set<String> contacts;
    private FirebaseDatabase database;
    private float nMembers;
    private String IDGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R .layout.activity_expense_creation);

        bt = (Button) findViewById(R.id.expense_button);
        bt.setOnClickListener(this);

        auth=FirebaseAuth.getInstance();

        IDGroup= getIntent().getStringExtra("IDGroup");

        contacts= new HashSet<>();

        database = FirebaseDatabase.getInstance();
        DatabaseReference refMembers=database.getReference("groups/"+IDGroup+"/members");

        refMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                nMembers=dataSnapshot.getChildrenCount();

                // collecting into a set the names of the members
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    contacts.add(data.getKey().toString());
                }


            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


    }


    public void onClick(View v){

        String et_author = auth.getCurrentUser().getEmail().replace(".",",");
        EditText et_name = (EditText)findViewById(R.id.expense_name);
        EditText et_description = (EditText)findViewById(R.id.expense_description);
        final EditText et_import = (EditText)findViewById(R.id.expense_import);

        DatabaseReference myRef = database.getReference("groups/"+IDGroup+"/items");
        DatabaseReference userRef= database.getReference("users");
        DatabaseReference ref=myRef.child(et_name.getText().toString());
        ref.child("Price").setValue(et_import.getText().toString());
        ref.child("Description").setValue(et_description.getText().toString());
        ref.child("Name").setValue(et_name.getText().toString());
        ref.child("Author").setValue(et_author);

        DatabaseReference refDebits=database.getReference("groups/"+IDGroup+"/debits");
        // 2 decimals
        double priceEach=Math.round((Double.valueOf(et_import.getText().toString())/nMembers)*100.0)/100.0;

        Iterator<String> it=contacts.iterator();
        while(it.hasNext()){

            String name=it.next();
            if(!name.equals(et_author)) {
                //updating of the group's info
                DatabaseReference newRef = refDebits.push();
                newRef.child("Receiver").setValue(et_author);
                newRef.child("Sender").setValue(name);
                newRef.child("Money").setValue(priceEach);
                //updating debitors list inside the author
                DatabaseReference refDeb = userRef.child(et_author).child("credits").push();
                refDeb.child("Group").setValue("IDGroup");
                refDeb.child("Debitor").setValue(name);
                refDeb.child("Money").setValue(priceEach);
                //updating each creditor
                DatabaseReference refCred = userRef.child(name).child("debits").push();
                refCred.child("Group").setValue(IDGroup);
                refCred.child("Paying").setValue(et_author);
                refCred.child("Money").setValue(priceEach);
            }


        }


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
