package it.polito.mad14.myListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Contact;

/**
 * Created by Utente on 02/05/2017.
 */

public class CustomAdapterContacts extends BaseAdapter {
    Context context;
    ArrayList<Contact> contactsList;
    LayoutInflater inflater;

    public CustomAdapterContacts(Context context, ArrayList<Contact> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
    }

    @Override
    public int getCount() {
        return contactsList.size();
    }

    @Override
    public Contact getItem(int position) {
        return contactsList.get(position);
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
            convertView = inflater.inflate(R.layout.contact_item, parent, false);

        TextView tv = (TextView) convertView.findViewById(R.id.tv_contact_name_surname);
        tv.setText(contactsList.get(position).getName() + " " + contactsList.get(position).getSurname());
        tv = (TextView) convertView.findViewById(R.id.tv_contact_email);
        tv.setText(contactsList.get(position).getEmail());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Contact number "+ position +" has been clicked!",Toast.LENGTH_SHORT).show();
                //TODO intent to show profile user selected
            }
        });
       return convertView;
    }

    public ArrayList<Contact> getContactsList() {
        return contactsList;
    }

    public void setContactsList(ArrayList<Contact> contactsList) {
        this.contactsList = contactsList;
    }

}
