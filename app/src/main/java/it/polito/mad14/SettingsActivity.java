package it.polito.mad14;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private Button changeCurrencyEUR, changeCurrencyUSD;
    private ImageButton italy, uk;
    private String selectedCurrency, buttonText;
    private static FirebaseDatabase database;
    private static DatabaseReference currencyRef;
    private TextView language, currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String def = Locale.getDefault().getDisplayLanguage();
        String lang = prefs.getString("LANGUAGE",def);
        conf.locale = new Locale(lang);
        res.updateConfiguration(conf, res.getDisplayMetrics());

        this.setTitle(R.string.title_settings);

        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();

        language = (TextView) findViewById(R.id.language);
        language.setText(getString(R.string.change_language));
        currency = (TextView) findViewById(R.id.currency);
        currency.setText(getString(R.string.choose_your_currency));

        italy = (ImageButton) findViewById(R.id.italy_flag);
        uk = (ImageButton) findViewById(R.id.uk_flag);
        int paddingDP = 35;
        float density = getResources().getDisplayMetrics().density;
        int paddingPixels = (int)(paddingDP*density);
        if (lang.equals("it")){
            italy.setPadding(paddingPixels,paddingPixels,paddingPixels,paddingPixels);
        } else {
            uk.setPadding(paddingPixels,paddingPixels,paddingPixels,paddingPixels);
        }

        changeCurrencyEUR = (Button) findViewById(R.id.button_currency_EUR);
        changeCurrencyUSD = (Button) findViewById(R.id.button_currency_USD);

        changeCurrencyEUR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCurrency = "€";
                currencyRef.child("MyCurrency").setValue(selectedCurrency);
                changeCurrencyEUR.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                changeCurrencyEUR.setTextColor(getResources().getColor(R.color.white));
                changeCurrencyUSD.setBackgroundResource(android.R.drawable.btn_default);
                changeCurrencyUSD.setTextColor(getResources().getColor(R.color.writing));
//                Toast.makeText(SettingsActivity.this, getString(R.string.currency_set_to) + " " +
//                        selectedCurrency + ".", Toast.LENGTH_SHORT).show();
            }
        });

        changeCurrencyUSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCurrency = "$";
                currencyRef.child("MyCurrency").setValue(selectedCurrency);
                changeCurrencyUSD.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                changeCurrencyUSD.setTextColor(getResources().getColor(R.color.white));
                changeCurrencyEUR.setBackgroundResource(android.R.drawable.btn_default);
                changeCurrencyEUR.setTextColor(getResources().getColor(R.color.writing));
//                Toast.makeText(SettingsActivity.this, getString(R.string.currency_set_to) + " " +
//                        selectedCurrency + ".", Toast.LENGTH_SHORT).show();
            }
        });

        database = FirebaseDatabase.getInstance();
        String UserID = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        currencyRef = database.getReference("users/" + UserID);
        currencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selectedCurrency = dataSnapshot.child("MyCurrency").getValue().toString();
                if (selectedCurrency.equals("€")) {
                    //buttonText = "EUR (" + selectedCurrency + ")";
                    changeCurrencyEUR.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    changeCurrencyEUR.setTextColor(getResources().getColor(R.color.white));
                }
                else{
                    //buttonText = "USD (" + selectedCurrency + ")";
                    changeCurrencyUSD.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    changeCurrencyUSD.setTextColor(getResources().getColor(R.color.white));
                }
                //changeCurrency.setText(buttonText);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        italy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeToItalian();
            }
        });

        uk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeToEnglish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_settings);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, LoadingActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void ChangeToItalian(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("LANGUAGE", "it");
        editor.commit();
        finish();
        startActivity(getIntent());
    }

    private void ChangeToEnglish(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("LANGUAGE", "en");
        editor.commit();
        finish();
        startActivity(getIntent());
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    /*private void onClickChangeCurrency(){
        LayoutInflater li = LayoutInflater.from(SettingsActivity.this);
        View promptsView = li.inflate(R.layout.spinner_first_currency, null);
        AlertDialog.Builder alertDialogueBuilder = new AlertDialog.Builder(SettingsActivity.this);
        alertDialogueBuilder.setView(promptsView);
        alertDialogueBuilder.setTitle(getString(R.string.choose_your_currency));
        final Spinner currencySpinner = (Spinner) promptsView.findViewById(R.id.spinner_your_currency);
        String[] currencies = new String[]{"EUR (€)","USD ($)"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>
                (SettingsActivity.this,R.layout.spinner_item,currencies);
        currencySpinner.setAdapter(spinnerAdapter);
        alertDialogueBuilder.setPositiveButton(getString(R.string.positive_button_dialogue),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = currencySpinner.getSelectedItem().toString();
                        String[] parts = str.split(" ");
                        buttonText = str;
                        selectedCurrency = parts[1].replace("(","").replace(")","");
                        currencyRef.child("MyCurrency").setValue(selectedCurrency);
                        changeCurrency.setText(buttonText);
                        Toast.makeText(SettingsActivity.this, getString(R.string.currency_set_to) + " " +
                                selectedCurrency + ".", Toast.LENGTH_LONG).show();
                    }
                });
        alertDialogueBuilder.setNegativeButton(getString(R.string.negative_button_dialogue),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog chooseYourCurrency = alertDialogueBuilder.create();
        chooseYourCurrency.show();

    }*/
}