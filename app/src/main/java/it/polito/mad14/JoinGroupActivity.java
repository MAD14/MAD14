package it.polito.mad14;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinGroupActivity extends AppCompatActivity {

    private EditText eGroupId;
    private EditText eJoinCode;
    private String groupId, joinCode;
    private String verifiedJoinCode;
    private Button joinButton;
    private boolean groupFound;

    private View mProgressView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        mProgressView = findViewById(R.id.search_group_progress);

        eGroupId = (EditText) findViewById(R.id.join_group_id);
        eJoinCode = (EditText) findViewById(R.id.join_group_code);

        joinButton = (Button) findViewById(R.id.join_group_button);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupId = eGroupId.getText().toString();
                joinCode = eJoinCode.getText().toString();
                groupFound = false;
                InputMethodManager imm = (InputMethodManager)getSystemService(JoinGroupActivity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                mProgressView.setVisibility(View.VISIBLE);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("groups");

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            if (data.getKey().equals(groupId)) {
                                groupFound = true;
                                verifiedJoinCode = data.child("join_code").getValue().toString();
                            }
                        }
                        mProgressView.setVisibility(View.GONE);
                        if(groupFound) {
                            Toast.makeText(JoinGroupActivity.this, "Gruppo Trovato",
                                    Toast.LENGTH_SHORT).show();
                            if (JoinCodeCheck()) {
                                //TODO: l'utente deve unirsi al gruppo
                                mainActivityCall();
                            } else {
                                eJoinCode.setError(getString(R.string.error_group_join_code_incorrect));
                                eJoinCode.requestFocus();
                            }
                        } else {
                            eGroupId.setError(getString(R.string.error_group_id_not_found));
                            eGroupId.requestFocus();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Failed to read value.", error.toException());
                        Toast.makeText(JoinGroupActivity.this, "Failed to read DB!.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    public boolean JoinCodeCheck() {
        if (joinCode.equals(verifiedJoinCode)){
            return true;
        } else {
            return false;
        }
    }

    public void mainActivityCall(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
