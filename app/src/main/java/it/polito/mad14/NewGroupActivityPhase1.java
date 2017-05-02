package it.polito.mad14;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NewGroupActivityPhase1 extends AppCompatActivity {

    Button createGroup;
    EditText editName;
    EditText editDescription;
    private String groupName;
    private String groupDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_phase1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createGroup = (Button) findViewById(R.id.group_create_button);
        editName = (EditText) findViewById(R.id.group_name);
        editDescription = (EditText) findViewById(R.id.group_description);

        createGroup.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        groupName = editName.getText().toString();
                        if (EditIsAlphanumeric(groupName)) {
                            groupDescription = editDescription.getText().toString();
                            // DB ACCESS
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("groups").push();
                            Map<String,String> dict=new HashMap<>();
                            dict.put("Name",groupName);
                            dict.put("Description",groupDescription);
                            myRef.setValue(dict);

                            String IDGroup=myRef.getKey();

                            Intent intent = new Intent(NewGroupActivityPhase1.this, NewGroupActivityPhase2.class);
                            intent.putExtra("groupname", groupName);
                            intent.putExtra("groupdescription", groupDescription);
                            intent.putExtra("groupID",IDGroup);
                            startActivity(intent);

                        } else {
                            groupName = "";
                            Toast.makeText(NewGroupActivityPhase1.this, "Group Name is not valid.\nMust contains numbers or letters",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }


                });
    }

    private boolean EditIsAlphanumeric(String ToControl) {
        //TODO: Replace this with your own logic
        return ToControl.replaceAll("\\s+","").matches("[a-zA-Z0-9]+");
    }



}
