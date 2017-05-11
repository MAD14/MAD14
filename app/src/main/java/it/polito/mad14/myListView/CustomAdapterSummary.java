package it.polito.mad14.myListView;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Expense;
import it.polito.mad14.myDataStructures.Summary;

/**
 * Created by Utente on 01/05/2017.
 */

public class CustomAdapterSummary extends BaseAdapter {
    Context context;
    ArrayList<Summary> summaryList;
    LayoutInflater inflater;

    public CustomAdapterSummary(Context context, ArrayList<Summary> summaryList) {
        this.context = context;
        this.summaryList = summaryList;
    }

    @Override
    public int getCount() {
        return summaryList.size();
    }

    @Override
    public Object getItem(int position) {
        return summaryList.get(position);
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
            convertView = inflater.inflate(R.layout.personal_summary_item,parent,false);

        TextView tv = (TextView) convertView.findViewById(R.id.summary_name);
        tv.setText(summaryList.get(position).getName());

        tv = (TextView) convertView.findViewById(R.id.summary_import);
        if (summaryList.get(position).getCredit()) {
            // se è true verde
            tv.setTextColor(ContextCompat.getColor(context,R.color.green));
            tv.setText("+"+summaryList.get(position).getValue().toString());
        } else {
            // se è false rosso
            tv.setTextColor(ContextCompat.getColor(context,R.color.red));
            tv.setText("-"+summaryList.get(position).getValue().toString());
        }

        return convertView;
    }

    public ArrayList<Summary> getSummaryList() {
        return summaryList;
    }

    public void setSummaryList(ArrayList<Summary> summaryList) {
        this.summaryList = summaryList;
    }
}
