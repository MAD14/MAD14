package it.polito.mad14.myListView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.polito.mad14.InfoExpenseActivity;
import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Expense;

/**
 * Created by Utente on 01/05/2017.
 */

public class CustomAdapterExpenses extends BaseAdapter {
    private Context context;
    private ArrayList<Expense> expensesList;
    private LayoutInflater inflater;
    private String encodedImage, IDExpense;
    private FirebaseDatabase database;
    private Expense expense;

    private String debtor;
    private Double value, totValue;
    private DatabaseReference debitsRef, expenseItemRef;
    //private String[] parts;
    private List<String> parts;
    private Runnable r;
    private int i;


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
        tv = (TextView) convertView.findViewById(R.id.expense_currency);
        tv.setText(expensesList.get(position).getCurrency());

        IDExpense = expensesList.get(position).getID();

        ImageView imgbt = (ImageView) convertView.findViewById(R.id.expense_icon);
        String imageStr = expensesList.get(position).getImage();
        if (imageStr.equals("no_image")) {
            encodedImage = imageStr;
            imgbt.setBackgroundResource(R.mipmap.expense_icon);
        } else {
            encodedImage = imageStr;
            byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
            BitmapDrawable bDrawable = new BitmapDrawable(context.getResources(), image);
            imgbt.setImageDrawable(bDrawable);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InfoExpenseActivity.class);
                intent.putExtra("IDExpense",IDExpense);
                intent.putExtra("IDGroup",expensesList.get(position).getGroup());
                intent.putExtra("Name",expensesList.get(position).getName());
                intent.putExtra("Import",expensesList.get(position).getValue());
                intent.putExtra("Description",expensesList.get(position).getDescription());
                intent.putExtra("Author",expensesList.get(position).getAuthor().replace(",","."));
                intent.putExtra("Image",expensesList.get(position).getImage());
                intent.putExtra("Date",expensesList.get(position).getDate());
                context.startActivity(intent);

            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                AlertDialog.Builder dialogAlert = new AlertDialog.Builder(context);
                dialogAlert.setTitle("");
                dialogAlert.setMessage(context.getString(R.string.delete_item_request));
                dialogAlert.setPositiveButton(context.getString(R.string.positive_button_dialogue), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                        database = FirebaseDatabase.getInstance();
                        expense = expensesList.get(position);

                        if(expense.getAuthor().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",","))) {
                            // remove value from expense list
                            expensesList.remove(position);
                            notifyDataSetChanged();
                            notifyDataSetInvalidated();
                            // remove value from group
                            expenseItemRef = database.getReference("groups/" + expense.getGroup() + "/items/" + expense.getID());

                            expenseItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("Debits")){
                                        String debits = dataSnapshot.child("Debits").getValue().toString();
                                        parts = new ArrayList<String>(Arrays.asList(debits.split(",")));
                                        for (i=0; i<parts.size(); i++){

                                            r = new Runnable() {
                                                String key = parts.get(i).replace(",","");
                                                @Override
                                                public void run() {
                                                    debitsRef = database.getReference("groups/" + expense.getGroup() + "/debits");

                                                    debitsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot data : dataSnapshot.getChildren()){
                                                                if (data.getKey().equals(key)){
                                                                    debtor = data.child("Sender").getValue().toString();
                                                                    value = Double.valueOf(data.child("Money").getValue().toString());
                                                                    database.getReference("users/" + debtor + "/debits/" + data.getKey()).removeValue();
                                                                    database.getReference("users/" + debtor + "/groups/" + expense.getGroup()).
                                                                            addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    Double oldDebit=Double.valueOf(dataSnapshot.child("Debit").getValue().toString());
                                                                                    Double oldCredit = Double.valueOf(dataSnapshot.child("Credit").getValue().toString());
                                                                                    Double diff = Math.round((value - oldDebit)*100.0)/100.0;
                                                                                    Double newDebit,newCredit;
                                                                                    if (diff>=0){
                                                                                        newDebit = 0.0;
                                                                                        newCredit = diff;
                                                                                    } else {
                                                                                        newDebit = Math.round((oldDebit-value)*100.0)/100.0;
                                                                                        newCredit = oldCredit;
                                                                                    }
                                                                                    dataSnapshot.child("Debit").getRef().setValue(String.valueOf(newDebit));
                                                                                    dataSnapshot.child("Credit").getRef().setValue(String.valueOf(newCredit));

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });
                                                                    totValue = value * parts.size();
                                                                    database.getReference("users/"+expense.getAuthor()+"/groups/"+expense.getGroup()).
                                                                            addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    Double oldDebit=Double.valueOf(dataSnapshot.child("Debit").getValue().toString());
                                                                                    Double oldCredit = Double.valueOf(dataSnapshot.child("Credit").getValue().toString());
                                                                                    Double diff = Math.round((totValue - oldCredit)*100.0)/100.0;
                                                                                    Double newDebit,newCredit;
                                                                                    if (diff>=0){
                                                                                        newCredit = 0.0;
                                                                                        newDebit = diff;
                                                                                    } else {
                                                                                        newCredit = Math.round((oldCredit-totValue)*100.0)/100.0;
                                                                                        newDebit = oldDebit;
                                                                                    }
                                                                                    dataSnapshot.child("Debit").getRef().setValue(String.valueOf(newDebit));
                                                                                    dataSnapshot.child("Credit").getRef().setValue(String.valueOf(newCredit));
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });

                                                                    debitsRef.child(key).removeValue();
                                                                    DatabaseReference ref2 = database.getReference("users/" + expense.getAuthor() + "/credit");
                                                                    ref2.child(key).removeValue();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }
                                            };

                                            Thread t = new Thread(r);
                                            t.start();


                                        }
                                        expenseItemRef.removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                         /*   DatabaseReference debits = database.getReference("groups/" + expense.getGroup() + "/debits");
                            debits.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                                        String currentExpense = data.getKey();
                                        if (currentExpense.equals(expense.getID())) {
                                            String creditor = data.child("Receiver").getValue().toString();
                                            String debitor = data.child("Sender").getValue().toString();
                                            database.getReference("users/" + creditor + "/credits/" + currentExpense).removeValue();
                                            database.getReference("users/" + debitor + "/debits/" + currentExpense).removeValue();
                                            data.getRef().removeValue();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });*/

                            Toast.makeText(context, context.getString(R.string.deleting_expense), Toast.LENGTH_SHORT).show();
                            //METTI AGGIORNAMENTO NOT
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    DatabaseReference membersRef = database.getReference("groups/" + expense.getGroup() + "/members");
                                    membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            DatabaseReference users = database.getReference("users");
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("Action","DEL-E-"+expense.getName());
                                            updates.put("Value",Math.random());
                                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                users.child(data.getKey()).child("Not").child(expense.getGroup()).updateChildren(updates);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            };
                            Thread t = new Thread(r);
                            t.start();


                        }else{
                            Toast.makeText(context, context.getString(R.string.impossible_del_exp), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialogAlert.setNegativeButton(context.getString(R.string.negative_button_dialogue),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert=dialogAlert.create();
                alert.show();
                //Toast.makeText(context,"LOng click on Expense number "+ position +" has been clicked!",Toast.LENGTH_SHORT).show();
                return true;
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
