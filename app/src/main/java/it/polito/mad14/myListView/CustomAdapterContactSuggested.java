package it.polito.mad14.myListView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.polito.mad14.AddNewContacts;
import it.polito.mad14.OtherProfileActivity;
import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Contact;

/**
 * Created by Utente on 02/05/2017.
 */

public class CustomAdapterContactSuggested extends BaseAdapter {

    Context context;
    ArrayList<Contact> partialNames;
    LayoutInflater inflater;
    private ListView list;
    private String encodedImage;
    private DatabaseReference myRef;
    private String image;
    private ImageButton img;


    public CustomAdapterContactSuggested(Context context, ArrayList<Contact> partialNames) {
        this.context = context;
        this.partialNames = partialNames;
    }

    @Override
    public int getCount() {
       return partialNames.size();
    }

    @Override
    public Contact getItem(int position) {
        return partialNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.contact_item_to_be_added, parent, false);

        TextView tv = (TextView) convertView.findViewById(R.id.tv_contact_name_surname_suggestion);
        tv.setText(partialNames.get(position).getName() + " " + partialNames.get(position).getSurname());
        tv = (TextView) convertView.findViewById(R.id.tv_contact_email_suggestion);
        tv.setText(partialNames.get(position).getUsername());

        img = (ImageButton) convertView.findViewById(R.id.add_contact_suggestion);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,context.getString(R.string.friends_added),Toast.LENGTH_SHORT).show();

                img.setImageResource(R.mipmap.check_icon_green);

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        String UserID=FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
                        myRef = FirebaseDatabase.getInstance().getReference("users/"+UserID+"/contacts/"+partialNames.get(position).getEmail().replace(".",","));
                        DatabaseReference currentUser = FirebaseDatabase.getInstance().getReference("users/"+partialNames.get(position).getEmail().replace(".",","));

                        myRef.child("Name").setValue(partialNames.get(position).getName());
                        myRef.child("Surname").setValue(partialNames.get(position).getSurname());
                        myRef.child("Username").setValue(partialNames.get(position).getUsername());
                        myRef.child("Email").setValue(partialNames.get(position).getEmail());

                        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child("ProfileImage").getValue()!=null) {
                                    image=dataSnapshot.child("ProfileImage").getValue().toString();
                                    myRef.child("Image").setValue(image);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                };
                Thread t = new Thread(r);
                t.start();

            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,OtherProfileActivity.class);
                intent.putExtra("Email",partialNames.get(position).getEmail());
                intent.putExtra("Username",partialNames.get(position).getUsername());
                intent.putExtra("Name",partialNames.get(position).getName());
                intent.putExtra("Surname",partialNames.get(position).getSurname());
                intent.putExtra("Image",image);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    public ArrayList<Contact> getPartialNames() {
        return partialNames;
    }

    public void setPartialNames(ArrayList<Contact> partialNames) {
        this.partialNames = partialNames;
    }
}
