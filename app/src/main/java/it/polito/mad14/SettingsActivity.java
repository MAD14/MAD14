package it.polito.mad14;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
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
    private Button changeCurrency;
    private String selectedCurrency, buttonText;
    private static FirebaseDatabase database;
    private static DatabaseReference currencyRef;

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
        Log.e("myapp", lang+" = "+conf.locale+" = "+conf.locale.getDisplayName());
        res.updateConfiguration(conf, res.getDisplayMetrics());

        changeCurrency = (Button) findViewById(R.id.button_currency);
        changeCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickChangeCurrency();
            }
        });

        database = FirebaseDatabase.getInstance();
        String UserID = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        currencyRef = database.getReference("users/" + UserID);
        currencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selectedCurrency = dataSnapshot.child("MyCurrency").getValue().toString();
                if (selectedCurrency.equals("€")) buttonText = "EUR (" + selectedCurrency + ")";
                else buttonText = "USD (" + selectedCurrency + ")";
                changeCurrency.setText(buttonText);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ImageButton italy = (ImageButton) findViewById(R.id.italy_flag);
        italy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeToItalian();
            }
        });

        ImageButton uk = (ImageButton) findViewById(R.id.uk_flag);
        uk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeToEnglish();
            }
        });

    }

    private void ChangeToItalian(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("LANGUAGE", "it");
        editor.commit();
        Intent intent = new Intent(SettingsActivity.this, LoadingActivity.class);
        startActivity(intent);
    }

    private void ChangeToEnglish(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("LANGUAGE", "en");
        editor.commit();
        Intent intent = new Intent(SettingsActivity.this, LoadingActivity.class);
        startActivity(intent);
    }

    private void onClickChangeCurrency(){
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
                        selectedCurrency = parts[1].replace("(","").replace(")","");
                        currencyRef.child("MyCurrency").setValue(selectedCurrency);
                        Toast.makeText(SettingsActivity.this, getString(R.string.currency_set_to) + " " +
                                selectedCurrency + ".", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SettingsActivity.this, LoadingActivity.class);
                        startActivity(intent);
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

    }
}