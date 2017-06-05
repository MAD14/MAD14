package it.polito.mad14;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.polito.mad14.myDataStructures.Contact;
//import it.polito.mad14.myListView.CustomAdapterContactSuggested;

public class AddNewContacts extends AppCompatActivity {

    // List of all contacts
    public ArrayList<Contact> searchNames = new ArrayList<>();
    // Filtered list of contacts after user begins typing in search field
    private ArrayList<Contact> partialNames = new ArrayList<>();

    private ArrayList<Contact> alreadyAddedAsFriend = new ArrayList<>();

    private ArrayList<Contact> actualFriends = new ArrayList<>();

    // List of names matching criteria are listed here
    private ListView list;

    // Field where user enters his search criteria
    private AutoCompleteTextView nameCapture;

    // Adapter for myList
    private CustomAdapterContactSuggested adapter;

    private DatabaseReference myRef;
    private String actualEmail,currentUser,actualName;

    private ProgressBar progressBar;
    private TextView loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        loading = (TextView) findViewById(R.id.loading_tv);

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        actualName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replace("."," ");

        actualEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        list = (ListView) findViewById(R.id.list_view_contact_suggestion);
        adapter = new CustomAdapterContactSuggested(getApplicationContext(), partialNames);
        list.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        loading.setVisibility(View.VISIBLE);

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference myRefUser = database.getReference("users").child(currentUser);
        myRefUser.child("contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    actualFriends.add(
                            new Contact(data.child("Name").getValue().toString(),
                                    data.child("Surname").getValue().toString(), data.child("Username").getValue().toString(),
                                    data.child("Email").getValue().toString(),
                                    "no_image"));
                }
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            searchNames.add(
                                    new Contact(data.child("Name").getValue().toString(),
                                            data.child("Surname").getValue().toString(), data.child("Username").getValue().toString(),
                                            data.child("Email").getValue().toString(),
                                            "no_image"));
                        }

                        searchNames = cleanDuplicates(searchNames,actualFriends);
                        progressBar.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);

                        nameCapture = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_new_contacts);
                        nameCapture.setVisibility(View.VISIBLE);

                        AlterAdapter();

                        nameCapture.addTextChangedListener(new TextWatcher() {
                            // As the user types in the search field, the list is
                            @Override
                            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                                AlterAdapter();
                            }
                            // Not used for this program
                            @Override
                            public void afterTextChanged(Editable arg0) {
                            }
                            // Not uses for this program
                            @Override
                            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



    }
    private void AlterAdapter() {
        if (nameCapture.getText().toString().isEmpty()) {
            partialNames.clear();
            adapter.notifyDataSetChanged();
        }
        else {
            partialNames.clear();
            for (int i = 0; i < searchNames.size(); i++) {
                if (searchNames.get(i).toString().toUpperCase().contains(nameCapture.getText().toString().toUpperCase())
                        && !(searchNames.get(i).getEmail().equals(actualEmail))
                        && !checkAlreadyAdded(searchNames.get(i))) {
                    partialNames.add(searchNames.get(i));
                    adapter.notifyDataSetChanged();

                }
                adapter.notifyDataSetChanged();
            }
        }
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


    public ArrayList<Contact> cleanDuplicates(ArrayList<Contact> list1, ArrayList<Contact> list2){

        for (Iterator<Contact> it = list1.iterator(); it.hasNext(); ){
            Contact tmp1 = it.next();
                for (Contact tmp2 : list2) {
                    if (tmp1.getEmail().equals(tmp2.getEmail())) {
                        it.remove();
                        break;
                    }
                }
        }
        return list1;
    }

    public Boolean checkAlreadyAdded(Contact possibleNewContact){
        for (Contact c : alreadyAddedAsFriend){
            if (c.getEmail().equals(possibleNewContact.getEmail()))
                return true;
        }
        return false;
    }



    class CustomAdapterContactSuggested extends BaseAdapter {

        Context context;
        ArrayList<Contact> partialNames;
        LayoutInflater inflater;
        private ListView list;
        private DatabaseReference myRef;
        private String image;
        private ImageButton img;
        private Boolean clicked = false;

        public CustomAdapterContactSuggested(Context context, ArrayList<Contact> partialNames) {
            this.context = context;
            this.partialNames = partialNames;
        }

        private class ViewHolder{
            ImageButton image;
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

            final ViewHolder holder;

            if (inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null){
                convertView = inflater.inflate(R.layout.contact_item_to_be_added, parent, false);
                holder = new ViewHolder();
                holder.image = (ImageButton) convertView.findViewById(R.id.add_contact_suggestion);
                holder.image.setTag(position);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            clicked = false;

            TextView tv = (TextView) convertView.findViewById(R.id.tv_contact_name_surname_suggestion);
            tv.setText(partialNames.get(position).getName() + " " + partialNames.get(position).getSurname());
            tv = (TextView) convertView.findViewById(R.id.tv_contact_email_suggestion);
            tv.setText(partialNames.get(position).getUsername());

            holder.image.setImageResource(R.mipmap.plus_icon);
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("position",String.valueOf(position));
                    Log.e("clicked",String.valueOf(clicked));

                    if (!clicked) {
                        Toast.makeText(context, context.getString(R.string.friends_added), Toast.LENGTH_SHORT).show();
                        clicked = true;
                        holder.image.setImageResource(R.mipmap.check_icon_green);

                        for (Contact c : searchNames){
                            if (c.getEmail().equals(partialNames.get(position).getEmail())) {
                                searchNames.remove(c);
                                break;
                            }
                        }

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                String UserID = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
                                myRef = FirebaseDatabase.getInstance().getReference("users/" + UserID + "/contacts/" + partialNames.get(position).getEmail().replace(".", ","));
                                DatabaseReference currentUser = FirebaseDatabase.getInstance().getReference("users/" + partialNames.get(position).getEmail().replace(".", ","));

                                myRef.child("Name").setValue(partialNames.get(position).getName());
                                myRef.child("Surname").setValue(partialNames.get(position).getSurname());
                                myRef.child("Username").setValue(partialNames.get(position).getUsername());
                                myRef.child("Email").setValue(partialNames.get(position).getEmail());

                                currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child("ProfileImage").getValue() != null) {
                                            image = dataSnapshot.child("ProfileImage").getValue().toString();
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
                    } else {
                        Toast.makeText(context, context.getString(R.string.friends_already_added), Toast.LENGTH_SHORT).show();
                    }
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
}


