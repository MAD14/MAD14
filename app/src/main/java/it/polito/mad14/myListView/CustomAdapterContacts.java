package it.polito.mad14.myListView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import it.polito.mad14.OtherProfileActivity;
import it.polito.mad14.R;
import it.polito.mad14.myDataStructures.Contact;

/**
 * Created by Utente on 02/05/2017.
 */

public class CustomAdapterContacts extends BaseAdapter {
    Context context;
    ArrayList<Contact> contactsList;
    LayoutInflater inflater;
    private String encodedImage;

    public CustomAdapterContacts(Context context, ArrayList<Contact> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
    }

    @Override
    public int getCount() {
        return contactsList.size();
    }

    @Override
    public Contact getItem(int position) {
        return contactsList.get(position);
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
            convertView = inflater.inflate(R.layout.contact_item, parent, false);

        TextView tv = (TextView) convertView.findViewById(R.id.tv_contact_name_surname);
        tv.setText(contactsList.get(position).getName() + " " + contactsList.get(position).getSurname());
        tv = (TextView) convertView.findViewById(R.id.tv_contact_username);
        tv.setText(contactsList.get(position).getUsername());

        ImageView imgbt = (ImageView) convertView.findViewById(R.id.expense_icon);
        if (!contactsList.get(position).getImage().equals("no_image")) {
            encodedImage = contactsList.get(position).getImage();
            byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
            BitmapDrawable bDrawable = new BitmapDrawable(context.getResources(), image);
            imgbt.setImageDrawable(bDrawable);
        } else {
            Drawable bDrawable = context.getDrawable(R.drawable.person_icon_deawable);
            imgbt.setImageDrawable(bDrawable);


        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,OtherProfileActivity.class);
                intent.putExtra("Email",contactsList.get(position).getEmail());
                intent.putExtra("Surname",contactsList.get(position).getSurname());
                intent.putExtra("Name",contactsList.get(position).getName());
                intent.putExtra("Username",contactsList.get(position).getUsername());
                intent.putExtra("Image",contactsList.get(position).getImage());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
       return convertView;
    }

    public ArrayList<Contact> getContactsList() {
        return contactsList;
    }

    public void setContactsList(ArrayList<Contact> contactsList) {
        this.contactsList = contactsList;
    }

}
