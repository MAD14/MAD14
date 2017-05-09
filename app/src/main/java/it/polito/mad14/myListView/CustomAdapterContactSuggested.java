package it.polito.mad14.myListView;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Contact;

/**
 * Created by Utente on 02/05/2017.
 */

public class CustomAdapterContactSuggested extends BaseAdapter {

    Context context;
    ArrayList<Contact> partialNames;
    LayoutInflater inflater;

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
        tv.setText(partialNames.get(position).getEmail());

        ImageButton img = (ImageButton) convertView.findViewById(R.id.add_contact_suggestion);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Friends added",Toast.LENGTH_SHORT).show();
                //TODO gestione invio amicizia nella pagina main
                String UserID=FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users/"+UserID+"/contacts/"+partialNames.get(position).getEmail().replace(".",","));
                myRef.child("Name").setValue(partialNames.get(position).getName());
                myRef.child("Surname").setValue(partialNames.get(position).getSurname());
                myRef.child("Username").setValue(partialNames.get(position).getUsername());
                myRef.child("Email").setValue(partialNames.get(position).getEmail());

            }
        });
        return convertView;
    }
}
