package it.polito.mad14;


import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Federico on 20/04/2017.
 */

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText nameView;
    private EditText surnameView;
    private EditText emailView;
    private EditText passView;
    private EditText userView;
    private String name,surname,username,email,password;
    private static final String TAG = RegistrationActivity.class.getName();
    private boolean userNotUsed;

    private View mProgressView;
    private View focusView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Intent intent = getIntent();
        String emailRequested = intent.getStringExtra("emailRequested");

        nameView = (EditText) findViewById(R.id.name);
        surnameView = (EditText) findViewById(R.id.surname);
        userView = (EditText) findViewById(R.id.username);
        emailView = (EditText) findViewById(R.id.email);
        emailView.setText(emailRequested);
        passView = (EditText) findViewById(R.id.password);

        mProgressView = findViewById(R.id.login_progress_registration);

        mAuth=FirebaseAuth.getInstance();


        final Button mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = nameView.getText().toString();
                surname = surnameView.getText().toString();
                username = userView.getText().toString();
                email = emailView.getText().toString();
                password = passView.getText().toString();
                if(checkDataForRegistration()){
                    focusView.requestFocus();
                } else {
                    mProgressView.setVisibility(View.VISIBLE);
                    createAccount();
                }

            }
        });
    }

    public void createAccount() {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            final DatabaseReference myRef = database.getReference("users");
                            userNotUsed = true;

                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                                        if (data.getKey().toString().equals(email.replace(".",","))) {
                                            Toast.makeText(RegistrationActivity.this, data.getKey().toString(),
                                                    Toast.LENGTH_SHORT).show();
                                            userNotUsed = false;
                                        }
                                    }
                                    mProgressView.setVisibility(View.GONE);
                                    if(userNotUsed) {
                                        DatabaseReference ref = myRef.child(email.replace(".",","));
                                        ref.child("Name").setValue(name);
                                        ref.child("Surname").setValue(surname);
                                        ref.child("Email").setValue(email);
                                        ref.child("Password").setValue(password);
                                        ref.child("Username").setValue(username);
                                        Toast.makeText(RegistrationActivity.this, "Added " + name + " " + surname,
                                                Toast.LENGTH_SHORT).show();
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name+"."+surname).build();
                                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates);

                                        mainActivityCall();
                                    } else {
                                        //TODO: new activity con richiesta nuovo username fino a quando non ce n'è uno disponibile
                                        // con ActivityOnResult() che completa l'inserimento
                                        Toast.makeText(
                                                RegistrationActivity.this, "Authentication failed: username already in use!\nPlease select another one",Toast.LENGTH_SHORT).show();
                                        passView.setFocusable(true);

                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    userView.setError(getString(R.string.error_invalid_username));
                                                    focusView = userView;
                                                    focusView.requestFocus();
                                                }
                                            }
                                        });


                                        //TODO: rimozione authentication se abbandono app
                                        //FirebaseAuth.getInstance().signOut();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Log.w("Failed to read value.", error.toException());
                                    Toast.makeText(RegistrationActivity.this, "Failed to read value from DB!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }


                });
    }

    public boolean checkDataForRegistration() {

        boolean check = false;
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()){
            if (password.isEmpty()) {
                passView.setError(getString(R.string.error_field_required));
                focusView = passView;
            }
            if (email.isEmpty()) {
                emailView.setError(getString(R.string.error_field_required));
                focusView = emailView;
            }
            if (username.isEmpty()) {
                userView.setError(getString(R.string.error_field_required));
                focusView = userView;
            }
            check = true;
        } else if (!isEmailValid(email)){
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            check = true;
        } else if (!isPasswordValid(password)) {
            passView.setError(getString(R.string.error_invalid_password));
            focusView = passView;
            check = true;
        }
        return check;
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private boolean isEmailValid(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        return matcher.find();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 6;
    }

    public void mainActivityCall(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("email",email);
        startActivity(intent);
    }
    /*
    @Override
    public void onResume(){
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }*/


}