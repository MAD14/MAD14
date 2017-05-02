package it.polito.mad14;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // necessario per avere il tondo della foto profilo in primo piano anche con le API<21
        ImageButton imgbt = (ImageButton)findViewById(R.id.user_profile_photo);
        imgbt.bringToFront();

        //TODO: fill the information with those coming from the database!!!!
        // - nome, immagine profilo, descrizione

        //TODO allow to switch to the email app to send an email? or to the phone to make a call?

    }
}