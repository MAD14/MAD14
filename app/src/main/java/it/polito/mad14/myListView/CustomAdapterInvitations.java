package it.polito.mad14.myListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad14.R;

/**
 * Created by Utente on 04/05/2017.
 */

public class CustomAdapterInvitations extends BaseAdapter {

    Context context;
    ArrayList<String> emailsToBeSent;
    LayoutInflater inflater;

    public CustomAdapterInvitations(Context context, ArrayList<String> emailsToBeSent) {
        this.context = context;
        this.emailsToBeSent = emailsToBeSent;
    }

    @Override
    public int getCount() {
        return emailsToBeSent.size();
    }

    @Override
    public Object getItem(int position) {
        return emailsToBeSent.get(position);
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
            convertView = inflater.inflate(R.layout.invitation_item, parent, false);

        TextView tv = (TextView) convertView.findViewById(R.id.tv_contact_email);
        tv.setText(emailsToBeSent.get(position));

        return convertView;
    }


}