package it.polito.mad14.myListView;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Summary;

/**
 * Created by Utente on 01/05/2017.
 */

public class CustomAdapterSummaryGroup extends BaseAdapter {
    Context context;
    ArrayList<Summary> summaryList;
    LayoutInflater inflater;
    private String encodedImage;
    private FirebaseDatabase database;
    private String IDGroup;
    private String name;
    private String user;
    private String groupCurrency;
    private Boolean flag=false,flag2=false;
    private Summary summ;
    private Boolean cd;
    private String val;
    private DatabaseReference dataref;
    private Button button;
    private String value;

    public CustomAdapterSummaryGroup(Context context, ArrayList<Summary> summaryList, String IDGroup, String groupCurrency) {
        this.context = context;
        this.summaryList = summaryList;
        this.IDGroup = IDGroup;
        this.groupCurrency = groupCurrency;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.personal_summary_item, parent, false);

        TextView tv = (TextView) convertView.findViewById(R.id.summary_name);
        tv.setText(summaryList.get(position).getName());
        TextView currency = (TextView) convertView.findViewById(R.id.summary_currency);
        currency.setText(groupCurrency);

        database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser().getEmail().replace(".", ",");

        summ = summaryList.get(position);
        name = summ.getEmail();
        val = summ.getValue();
        cd = summ.getCredit();

        button = (Button) convertView.findViewById(R.id.button_payment);

        value = summaryList.get(position).getValue();
        Matcher matcher = Pattern.compile("^[\\-0-9]+\\.[0-9]{1}$").matcher(value);
        if (matcher.find()) {
            value = value + "0";
        } else {
            matcher = Pattern.compile("^[\\-0-9]+$").matcher(value);
            if (matcher.find()) {
                value = value + ".00";
            }}

        tv = (TextView) convertView.findViewById(R.id.summary_import);

        if (cd) {
            // se è true verde
            tv.setTextColor(ContextCompat.getColor(context, R.color.green));
            currency.setTextColor(ContextCompat.getColor(context, R.color.green));
            String credit = "+" + value;
            tv.setText(credit);

            button.setText(R.string.confirm);
            button.setTextColor(context.getResources().getColor(R.color.darkgreen));
            button.setBackgroundColor(context.getResources().getColor(R.color.lightgreen));

            // check if it's a pending transaction
            DatabaseReference ref= database.getReference("groups/" + IDGroup + "PendingPayment");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.child("Email").getValue().toString().equals(name)){
                            // change text in button --> bold
                            button.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            // se è false rosso
            String debit;
            tv.setTextColor(ContextCompat.getColor(context, R.color.red));
            currency.setTextColor(ContextCompat.getColor(context, R.color.red));
            if (summaryList.get(position).getValue().contains("-")) {
                debit = value;
            }else {
                debit = "-" + value;
            }
            tv.setText(debit);

            button.setText(R.string.pay);
            button.setTextColor(context.getResources().getColor(R.color.darkred));
            button.setBackgroundColor(context.getResources().getColor(R.color.lightred));
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialogAlert = new AlertDialog.Builder(context);
                dialogAlert.setTitle("");
                dialogAlert.setMessage(R.string.settle_expense);
                dialogAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // check su debitor and creditor: if me=creditor no ACK if me = debitor waiting ACK

                        if (cd) {
                            // controllare anche nei pending nel caso ci sia da eliminare
                            DatabaseReference myRef = database.getReference("groups/" + IDGroup + "/debits");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                                        if ((data.child("Sender").getValue().toString().equals(name) &&
                                                data.child("Receiver").getValue().toString().equals(user)) ||
                                                (data.child("Sender").getValue().toString().equals(user) &&
                                                        data.child("Receiver").getValue().toString().equals(name))) {

                                            // remove data from debitor and creditor
                                            DatabaseReference creditor = database.getReference("users/" + data.child("Receiver").getValue().toString() + "/credits");
                                            creditor.child(data.getKey()).removeValue();
                                            DatabaseReference debitor = database.getReference("users/" + data.child("Sender").getValue().toString() + "/debits");
                                            debitor.child(data.getKey()).removeValue();
                                            // remove data from Group branch
                                            data.getRef().removeValue();

                                            //delte also from pending if exist
                                            DatabaseReference pending = database.getReference("groups/"+IDGroup+"/PendingPayment");
                                            pending.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for(DataSnapshot data: dataSnapshot.getChildren()){
                                                        if(data.child("Creditor").getValue().toString().equals(user) && data.child("Debitor").getValue().toString().equals(name)){
                                                            data.getRef().removeValue();
                                                            break;
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                    // deletion from the list plus notification data changed
                                    summaryList.remove(position);
                                    notifyDataSetChanged();
                                    notifyDataSetInvalidated();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }else{

                            dataref=database.getReference("groups/"+IDGroup+"/PendingPayment");
                            flag2=false;
                            dataref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot data: dataSnapshot.getChildren()){
                                        if (data.child("Creditor").getValue().toString().equals(name) &&
                                                data.child("Debitor").getValue().toString().equals(user)){
                                            flag2=true;
                                            break;
                                        }
                                    }
                                    if(!flag2){
                                        dataref.child("Creditor").setValue(name);
                                        dataref.child("Debitor").setValue(user);
                                        dataref.child("Money").setValue(val);
                                        // serve solo per mettere ! per i creditori...nelle card biognerà poi controllare che se è nella lista si deve attivare il !
                                        Toast.makeText(context, R.string.confirm_user_payment, Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(context, R.string.wait_confirm, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });



                        }

                    }
                });
                AlertDialog alert = dialogAlert.create();
                alert.show();

            }

        });

        return convertView;
    }



    public ArrayList<Summary> getSummaryList() {
        return summaryList;
    }

    public void setSummaryList(ArrayList<Summary> summaryList){
        this.summaryList = summaryList;
    }
}
