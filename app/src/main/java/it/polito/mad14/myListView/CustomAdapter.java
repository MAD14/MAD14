package it.polito.mad14.myListView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.polito.mad14.GroupActivity;
import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Expense;
import it.polito.mad14.myDataStructures.Group;

/**
 * Created by Utente on 30/04/2017.
 */

public class CustomAdapter extends BaseAdapter{

    Context context;
    ArrayList<Group> groupList;
    LayoutInflater inflater;
    Set<String> members=new HashSet<>();

    private String encodedImage;
    private DatabaseReference memRef,users, myRef;
    private FirebaseDatabase database;
    private Group group;
    private String currentUser;

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

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        TextView tv = (TextView) convertView.findViewById(R.id.group_name);
        tv.setText(groupList.get(position).getName());
        tv = (TextView) convertView.findViewById(R.id.group_summary1);
        tv.setText(context.getString(R.string.credit) + ": " + groupList.get(position).getCredit() + groupList.get(position).getCurrency());
        tv = (TextView) convertView.findViewById(R.id.group_summary2);
        tv.setText(context.getString(R.string.debit) + ": "+ groupList.get(position).getDebit() + groupList.get(position).getCurrency());

        ImageView imgbt = (ImageView) convertView.findViewById(R.id.group_icon);
        if (groupList.get(position).getImage().equals("no_image")) {
            imgbt.setImageResource(R.mipmap.group_icon);
        } else {
            encodedImage = groupList.get(position).getImage();
            byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
            BitmapDrawable bDrawable = new BitmapDrawable(context.getResources(), image);
            imgbt.setImageDrawable(bDrawable);
        }

        ImageView imageBell = (ImageView) convertView.findViewById(R.id.group_notification);
        if (groupList.get(position).getNews().equals("True")){
            imageBell.setVisibility(View.VISIBLE);
        } else {
            imageBell.setVisibility(View.INVISIBLE);
        }


        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(context, GroupActivity.class);
                intent.putExtra("IDGroup",groupList.get(position).getID());
                intent.putExtra("GroupCurrency",groupList.get(position).getCurrency());
                intent.putExtra("GroupName",groupList.get(position).getName());
                intent.putExtra("Sound",groupList.get(position).getSound());
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                AlertDialog.Builder dialogAlert = new AlertDialog.Builder(context);
                dialogAlert.setTitle("");
                dialogAlert.setMessage(context.getString(R.string.delete_group_request));

                dialogAlert.setPositiveButton(context.getString(R.string.positive_button_dialogue), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,int id) {

                        database = FirebaseDatabase.getInstance();
                        group = groupList.get(position);

                        if ((groupList.get(position).getCredit().equals("0") || groupList.get(position).getCredit().equals("0.0")) && (groupList.get(position).getDebit().equals("0") || groupList.get(position).getDebit().equals("0.0"))) {
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    // remove value from group
                                    // salvo membri in set
                                    memRef = database.getReference("groups/" + group.getID() + "/members");
                                    users = database.getReference("users");
                                    myRef = database.getReference("users/" + currentUser);
                                    memRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            int i = 0;
                                            Map<String, Object> updates = new HashMap<>();

                                            updates.put("Action","DEL-M-"+currentUser.replace(".",",")+"-DUMMY");

                                            updates.put("Value",Math.random());
                                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                if (data.getKey().equals(currentUser)){//se sono sul user che sta eliminando, cancello tutto
                                                    users.child(data.getKey()).child("groups").child(group.getID()).removeValue();
                                                    users.child(data.getKey()).child("Not").child(group.getID()).removeValue();
                                                    memRef.child(currentUser).removeValue();
                                                    i++;
                                                }
                                                else{
                                                    users.child(data.getKey()).child("Not").child(group.getID()).updateChildren(updates);
                                                    i++;
                                                }


                                            }
                                            if (i==1){
                                                database.getReference("groups/"+group.getID()).removeValue();
                                            }
                                            // decremento groupNumb
                                            DatabaseReference groupCounter = myRef.child("GroupsNumb");
                                            groupCounter.runTransaction(new Transaction.Handler(){
                                                @Override
                                                public Transaction.Result doTransaction(MutableData mutableData) {
                                                    Integer currentValue = mutableData.getValue(Integer.class);
                                                    if (currentValue == null) {
                                                        mutableData.setValue(0);
                                                    } else {
                                                        mutableData.setValue(currentValue - 1);
                                                    }

                                                    return Transaction.success(mutableData);
                                                }

                                                @Override
                                                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                                                }
                                            });
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });

                                    // remove value from group list
                                    groupList.remove(position);

                                }
                            };
                            Thread t = new Thread(r);
                            t.start();
                            Toast.makeText(context, context.getString(R.string.deleting_group), Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                            notifyDataSetInvalidated();



                        } else {
                            Toast.makeText(context,context.getString(R.string.delete_group_error),Toast.LENGTH_LONG).show();
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

                return true;
            }
        });

        return convertView;
    }

    public ArrayList<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(ArrayList<Group> groupList) {
        this.groupList = groupList;
    }
}
