package it.polito.mad14;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import it.polito.mad14.myDataStructures.Group;

public class LoadingActivity extends AppCompatActivity {

    private static String UserID;
    private static FirebaseDatabase database;
    private static DatabaseReference myRef;
    private ArrayList<Group> groupsList = new ArrayList<>();
    private int indexGroup = 0;
    private String noImage = "no_image";
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        database = FirebaseDatabase.getInstance();
        UserID = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");


//        // Start animating the image
        img = (ImageView) findViewById(R.id.splash);
        final Animation translateAnim= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translation_to_middle_screen);
        img.startAnimation(translateAnim);

        myRef = database.getReference("users/" + UserID + "/groups/");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    Iterator<Group> it = groupsList.iterator();
                    boolean flag = false;
                    while (it.hasNext()) {
                        if (it.next().getID().equals(data.getKey()))
                            flag = true;
                    }
                    if (!flag) {

                        try {
                            String id = data.getKey();
                            String nm = data.child("Name").getValue().toString();
                            String own = data.child("Author").getValue().toString();
                            String dat = data.child("Date").getValue().toString();
                            String news = data.child("News").getValue().toString();
                            String credit = "0";
                            if (data.hasChild("Credit")) {
                                credit = data.child("Credit").getValue().toString();
                            }
                            String debit = "0";
                            if (data.hasChild("Debit")) {
                                debit = data.child("Debit").getValue().toString();
                            }
                            String image;
                            if (data.child("Image").getValue().toString().equals(noImage) ) {
                                image = null;
                            } else {
                                image = data.child("Image").getValue().toString();
                            }
                            String currency = data.child("Currency").toString();
                            groupsList.add(indexGroup, new Group(id, nm, own, dat, credit, debit, image, currency,news));
                            indexGroup++;
                        } catch (Error e) {
                            Toast.makeText(LoadingActivity.this, e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Failed to read value.", error.toException());
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 4500);


    }
}
