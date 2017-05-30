package it.polito.mad14.myListView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private String encodedImage;
    private String currency;
    private String value;
    private Button button;


    public CustomAdapterSummary(Context context, ArrayList<Summary> summaryList, String currency) {
        this.context = context;
        this.summaryList = summaryList;
        this.currency = currency;
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
        TextView tvCurrency = (TextView) convertView.findViewById(R.id.summary_currency);
        tvCurrency.setText(currency);

        button = (Button) convertView.findViewById(R.id.button_payment);

        value = summaryList.get(position).getValue();
        Matcher matcher = Pattern.compile("^[0-9]+\\.[0-9]{1}$").matcher(value);
        if (matcher.find()) {
            value = value + "0";
        } else {
            matcher = Pattern.compile("^[0-9]+$").matcher(value);
            if (matcher.find()) {
                value = value + ".00";
        }}

        tv = (TextView) convertView.findViewById(R.id.summary_import);

        if (summaryList.get(position).getCredit()) {
            // se è true verde
            tv.setTextColor(ContextCompat.getColor(context,R.color.green));
            tvCurrency.setTextColor(ContextCompat.getColor(context,R.color.green));
            value = summaryList.get(position).getValue().replace("+","");
            String newValue = "+" + value;
            tv.setText(newValue);
            button.setBackgroundResource(R.mipmap.expense_icon_green);
            button.getLayoutParams().height = (int)context.getResources().getDimension(R.dimen.icon_dimension_group);
            button.getLayoutParams().width = (int)context.getResources().getDimension(R.dimen.icon_dimension_group);

        } else {
            // se è false rosso
            tv.setTextColor(ContextCompat.getColor(context,R.color.red));
            tvCurrency.setTextColor(ContextCompat.getColor(context,R.color.red));
            value = summaryList.get(position).getValue().replace("-","");
            String newValue = "-" + value;
            tv.setText(newValue);
            button.setBackgroundResource(R.mipmap.expense_icon_red);
            button.getLayoutParams().height = (int)context.getResources().getDimension(R.dimen.icon_dimension_group);
            button.getLayoutParams().width = (int)context.getResources().getDimension(R.dimen.icon_dimension_group);
        }


        return convertView;
    }

    public ArrayList<Summary> getSummaryList() {
        return summaryList;
    }

    public void setSummaryList(ArrayList<Summary> summaryList){
        this.summaryList = summaryList;
    }
}
