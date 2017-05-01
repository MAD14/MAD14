package it.polito.mad14.myListView;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import it.polito.mad14.GroupActivity;
import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Group;

/**
 * Created by Utente on 30/04/2017.
 */

public class CustomAdapter extends BaseAdapter{

    Context context;
    ArrayList<Group> groupList;
    LayoutInflater inflater;

    public CustomAdapter(Context context, ArrayList<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupList.get(position);
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
            convertView = inflater.inflate(R.layout.group_item,parent,false);

        TextView tv = (TextView) convertView.findViewById(R.id.group_name);
        tv.setText(groupList.get(position).getName());
        tv = (TextView) convertView.findViewById(R.id.group_summary1);
        tv.setText("Credit: " + String.valueOf(position) +"€");
        tv = (TextView) convertView.findViewById(R.id.group_summary2);
        tv.setText("Debit: "+ String.valueOf(position) +"€");

        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
//                Toast.makeText(context,"Group number "+position+" has been clicked!",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, GroupActivity.class);
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}
