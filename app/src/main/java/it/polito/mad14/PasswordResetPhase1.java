package it.polito.mad14;

import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;

import static it.polito.mad14.RegistrationActivity.VALID_EMAIL_ADDRESS_REGEX;

public class PasswordResetPhase1 extends AppCompatActivity {

    private AutoCompleteTextView emailAddress;
    private Button button;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private String email;
    private FirebaseAuth auth;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset_phase1);

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("users");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        emailAddress = (AutoCompleteTextView) findViewById(R.id.email_address);
        button = (Button) findViewById(R.id.send_email_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = emailAddress.getText().toString();
                        if (isEmailValid(email)) {
                            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(PasswordResetPhase1.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(PasswordResetPhase1.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                    }

                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            Toast.makeText(PasswordResetPhase1.this, "The email is not valid", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private boolean isEmailValid(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        return matcher.find();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}