package it.polito.mad14.myListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Contact;

/**
 * Created by Utente on 12/05/2017.
 */

public class CustomAdapterInfoGroup extends BaseAdapter {

    Context context;
    ArrayList<Contact> membersList;
    LayoutInflater inflater;

    public CustomAdapterInfoGroup(Context context, ArrayList<Contact> membersList) {
        this.context = context;
        this.membersList = membersList;
    }

    @Override
    public int getCount() {
        return membersList.size();
    }

    @Override
    public Contact getItem(int position) {
        return membersList.get(position);
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
            convertView = inflater.inflate(R.layout.contact_item,parent,false);

        //TODO mettere immagine profilo
        TextView tv = (TextView) convertView.findViewById(R.id.tv_contact_name_surname);
        tv.setText(membersList.get(position).getName()+ " "+ membersList.get(position).getSurname());
        tv = (TextView) convertView.findViewById(R.id.tv_contact_username);
        tv.setText(membersList.get(position).getUsername());

        return convertView;
    }

    public ArrayList<Contact> getMembersList() {
        return membersList;
    }

    public void setMembersList(ArrayList<Contact> membersList) {
        this.membersList = membersList;
    }
}
