package it.polito.mad14;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class OtherProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        // necessario per avere il tondo della foto profilo in primo piano anche con le API<21
        ImageButton imgbt = (ImageButton)findViewById(R.id.user_profile_photo);
        imgbt.bringToFront();

        //TODO: fill the information with those coming from the database!!!! prese dal intent
        // - nome, immagine profilo, descrizione

        //TODO make active the button to add as a friend

        //TODO allow to switch to the email app to send an email? or to the phone to make a call?
    }

    public void onClickAddAsFriend(View view){
        // TODO da gestire la richiesta di amicizia come nell'altro punto
    }
}
