package it.polito.mad14.myListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    public View getView(int position, View convertView, ViewGroup parent) {
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
                //TODO gestione invio amicizia
            }
        });
        return convertView;
    }
}
