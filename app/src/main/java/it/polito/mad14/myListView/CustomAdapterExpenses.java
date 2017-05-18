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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
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
    private String encodedImage;


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

        ImageView imgbt = (ImageView) convertView.findViewById(R.id.expense_icon);
        if (!expensesList.get(position).getImage().equals("no_image")) {
            encodedImage = expensesList.get(position).getImage();
            byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
            BitmapDrawable bDrawable = new BitmapDrawable(context.getResources(), image);
            imgbt.setImageDrawable(bDrawable);
        } else {
            imgbt.setImageResource(R.mipmap.person_icon);

        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InfoExpenseActivity.class);
                intent.putExtra("Name",expensesList.get(position).getName());
                intent.putExtra("Import",expensesList.get(position).getValue());
                intent.putExtra("Description",expensesList.get(position).getDescription());
                intent.putExtra("Author",expensesList.get(position).getAuthor().replace(",","."));
                intent.putExtra("Image",expensesList.get(position).getImage());
                context.startActivity(intent);

            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                AlertDialog.Builder dialogAlert = new AlertDialog.Builder(context);
                dialogAlert.setTitle("");
                dialogAlert.setMessage("Do you want to delete this item?");
                dialogAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                        final FirebaseDatabase database=FirebaseDatabase.getInstance();
                        final Expense expense=expensesList.get(position);
                        // remove value from expense list
                        expensesList.remove(position);
                        // remove value from group
                        DatabaseReference myRef=database.getReference("groups/"+expense.getGroup()+"/items");
                        myRef.child(expense.getName()).removeValue();
                        DatabaseReference debits=database.getReference("groups/"+expense.getGroup()+"/debits");
                        debits.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot data : dataSnapshot.getChildren()){
                                    if(data.child("Product").getValue().toString().equals(expense.getName())){
                                        String creditor= data.child("Receiver").getValue().toString();
                                        String debitor=data.child("Sender").getValue().toString();
                                        database.getReference("users/"+creditor+"/credits/"+data.getKey()).removeValue();
                                        database.getReference("users/"+debitor+"/debits/"+data.getKey()).removeValue();
                                        data.getRef().removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        Toast.makeText(context,"Deleting expense",Toast.LENGTH_SHORT).show();
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
