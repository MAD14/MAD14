package it.polito.mad14;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import it.polito.mad14.myDataStructures.Group;

public class LoadingActivity extends AppCompatActivity {

    private static String UserID;
    private static FirebaseDatabase database;
    private static DatabaseReference myRef;
    private ArrayList<Group> groupsList = new ArrayList<>();
    private int indexGroup = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        database = FirebaseDatabase.getInstance();
        UserID = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        RotateAnimation anim = new RotateAnimation(0f, 350f, 15f, 15f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(700);

        // Start animating the image
        ImageView img = (ImageView) findViewById(R.id.splash);
        Animation animation = AnimationUtils.loadAnimation(LoadingActivity.this, R.anim.zoom_in_animation);
        img.startAnimation(animation);


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
                            groupsList.add(indexGroup, new Group(id, nm, own, dat));
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
                // Actions to do after 10 seconds
                // Later.. stop the animation
//                splash.setAnimation(null);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3500);


    }
}
