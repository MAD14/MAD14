package it.polito.mad14;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class NewGroupActivityPhase2 extends AppCompatActivity  implements View.OnClickListener{

    private ListView list;
    //TODO: friends dovrebbe essere popolata degli amici --> potremmo tenere una mappa/lista che funga da cache
    // friends viene utilizzata poi nell'adapter a riga 55 per popolare i suggerimenti
    private String[] friends = {"Elena","Martina","Giulia","Eleonora","Elisabetta"};
    private ArrayList<String> friends_added;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_phase2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // lista temporanea che può essere scritta sul db nel momento in cui si passa alla schermata successiva
        friends_added = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),NewGroupActivityPhase3.class);
                startActivity(intent);
            }
        });

        // adapter per suggerire gli amici in elenco
        AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_friends);
        tv.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_single_choice,friends));

        list = (ListView) findViewById(R.id.lv_friends);
        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {return friends_added.size();}
            @Override
            public Object getItem(int position) {return friends_added.get(position);}
            @Override
            public long getItemId(int position) {return 0;}
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.friend_selected_item, parent, false);
                TextView tv = (TextView) convertView.findViewById(R.id.friend_name);
                tv.setText(friends_added.get(position));
                return convertView;
            }
        });

    }

    public void onClick(View view){
        AutoCompleteTextView et = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_friends);
        String tmp_name = et.getText().toString();
        friends_added.add(tmp_name);
        list.invalidate();
        list.requestLayout();
    }
}
