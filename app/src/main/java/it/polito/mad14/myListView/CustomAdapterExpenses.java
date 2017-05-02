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
import it.polito.mad14.myDataStructures.Expense;

/**
 * Created by Utente on 01/05/2017.
 */

public class CustomAdapterExpenses extends BaseAdapter {
    Context context;
    ArrayList<Expense> expensesList;
    LayoutInflater inflater;

    public CustomAdapterExpenses(Context context, ArrayList<Expense> expensesList) {
        this.context = context;
        this.expensesList = expensesList;
    }

    @Override
    public int getCount() {
        return expensesList.size();
    }

    @Override
    public Object getItem(int position) {
        return expensesList.get(position);
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
            convertView = inflater.inflate(R.layout.expense_item,parent,false);

        TextView tv = (TextView) convertView.findViewById(R.id.expense_name);
        tv.setText(expensesList.get(position).getName());
        tv = (TextView) convertView.findViewById(R.id.expense_import);
        tv.setText(expensesList.get(position).getValue());
        tv = (TextView) convertView.findViewById(R.id.expense_description);
        tv.setText(expensesList.get(position).getDescription());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Expense number "+ position +" has been clicked!",Toast.LENGTH_SHORT).show();
                //TODO intent ad attività di informazioni della spesa
            }
        });

        return convertView;
    }

    public ArrayList<Expense> getExpensesList() {
        return expensesList;
    }

    public void setExpensesList(ArrayList<Expense> expensesList) {
        this.expensesList = expensesList;
    }
}
