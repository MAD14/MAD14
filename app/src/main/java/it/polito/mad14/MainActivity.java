package it.polito.mad14;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myDataStructures.Group;
import it.polito.mad14.myDataStructures.Summary;
import it.polito.mad14.myListView.CustomAdapter;
import it.polito.mad14.myListView.CustomAdapterContacts;
import it.polito.mad14.myListView.CustomAdapterSummary;
import it.polito.mad14.myListView.FirebaseBackgroundService;
import it.polito.mad14.myListView.FirebaseBackgroundService2;


public class MainActivity extends AppCompatActivity {
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static FirebaseDatabase database;
    private static String UserID, selectedCurrency;
    private static DatabaseReference myRef, currencyRef;
    private FloatingActionButton fab_groups;
    private FloatingActionButton fab_contacts;
    final static int GROUP_CREATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedCurrency = "€";

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        // Control internet connection
        if (!isNetworkConnected()) Toast.makeText(this,getString(R.string.no_network_connection),Toast.LENGTH_LONG).show();
        //////////////////////////
        Runnable r = new Runnable() {
            @Override
            public void run() {
                startService(new Intent(MainActivity.this,FirebaseBackgroundService.class));
                System.out.println("inizio!");
            }
        };
        Thread t = new Thread(r);
        t.start();

        //////////////////////////
       /*Runnable r1 = new Runnable() {

            @Override
            public void run() {
                startService(new Intent(MainActivity.this,FirebaseBackgroundService2.class));
                System.out.println("inizio!");
            }
        };
        Thread t1 = new Thread(r1);

        t1.start();*/
        //////////////////////////

        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                animateFab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };

        mViewPager.setOnPageChangeListener(onPageChangeListener);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        database = FirebaseDatabase.getInstance();
        UserID = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        fab_groups = (FloatingActionButton) findViewById(R.id.fab_groups_page);
        fab_contacts = (FloatingActionButton) findViewById(R.id.fab_contacts_page);

        currencyRef = database.getReference("users/" + UserID);
        currencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("MyCurrency")){
                    LayoutInflater li = LayoutInflater.from(MainActivity.this);
                    View promptsView = li.inflate(R.layout.spinner_first_currency, null);
                    AlertDialog.Builder alertDialogueBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogueBuilder.setView(promptsView);
                    alertDialogueBuilder.setTitle(getString(R.string.choose_your_currency));
                    final Spinner currencySpinner = (Spinner) promptsView.findViewById(R.id.spinner_your_currency);
                    String[] currencies = new String[]{"EUR (€)","USD ($)"};
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>
                            (MainActivity.this,R.layout.spinner_item,currencies);
                    currencySpinner.setAdapter(spinnerAdapter);
                    alertDialogueBuilder.setPositiveButton(getString(R.string.positive_button_dialogue),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String str = currencySpinner.getSelectedItem().toString();
                                    String[] parts = str.split(" ");
                                    selectedCurrency = parts[1].replace("(","").replace(")","");
                                    currencyRef.child("MyCurrency").setValue(selectedCurrency);
                                    Toast.makeText(MainActivity.this, getString(R.string.currency_set_to) + " " +
                                            selectedCurrency + ". " + getString(R.string.currency_advisor), Toast.LENGTH_LONG).show();
                                }
                            });
                    AlertDialog chooseYourCurrency = alertDialogueBuilder.create();
                    chooseYourCurrency.show();
                    chooseYourCurrency.setCanceledOnTouchOutside(false);
                } else {
                    selectedCurrency = dataSnapshot.child("MyCurrency").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_personal_profile:
                intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.action_invite_new_members:
                intent = new Intent(MainActivity.this, InviteToJoinCommunity.class);
                intent.putExtra("sender", UserID);
                startActivity(intent);
                break;
//            case R.id.action_join_a_group:
//                intent = new Intent(MainActivity.this, JoinGroupActivity.class);
//                startActivity(intent);
//                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        /**
         * ELENA:
         *  qui di seguito metto delle variabili che servono per popolare le view, che verranno poi
         *  popolate tramite la lettura dal database!
         */

        private ArrayList<Group> groupsList=new ArrayList<>();
        private ArrayList<Contact> contactsList=new ArrayList<>();
        private int indexGroup=0;
        private int indexContact=0;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private ArrayList<Summary> summaryList = new ArrayList<>();
        ArrayList<Summary> creditsList = new ArrayList<>();
        ArrayList<Summary> debitsList = new ArrayList<>();
        ArrayList<Summary> tmpList = new ArrayList<>();
        DatabaseReference myRef_summary_debits,myRef_summary_credits;
        Map<String,Summary> tot= new HashMap<>();
        private String noImage = "no_image";
        private TextView noGroup_textView, noSummary_textView, noContact_textView;
        private int indexSummary=0;
        private boolean credit;
        private double USDtoEUR, EURtoUSD;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private View rootView;
        private ListView list,list_summary;


        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {


            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                rootView = inflater.inflate(R.layout.groups_list_page, container, false);
                list = (ListView) rootView.findViewById(R.id.list_view_main_activity);
                noGroup_textView = (TextView) rootView.findViewById(R.id.noGroup_tv);

                myRef = database.getReference("users/"+UserID+"/groups/");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        groupsList = new ArrayList<>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                try {
                                    String id = data.getKey();
                                    String nm = data.child("Name").getValue().toString();
                                    String own = data.child("Author").getValue().toString();
                                    String dat = data.child("Date").getValue().toString();
                                    String news = data.child("News").getValue().toString();
                                    String sound = data.child("Sound").getValue().toString();
                                    String credit = "0";
                                    if (data.hasChild("Credit")) {
                                        credit = data.child("Credit").getValue().toString();
                                    }
                                    String debit = "0";
                                    if (data.hasChild("Debit")) {
                                        debit = data.child("Debit").getValue().toString();
                                    }
                                    String image = data.child("Image").getValue().toString();
                                    String currency = data.child("Currency").getValue().toString();
                                    indexGroup = groupsList.size();
                                    groupsList.add(indexGroup, new Group(id, nm, own, dat, credit, debit, image, currency,news,sound));
                                }
                                    catch(Error e){
                                        Toast.makeText(getContext(), e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                        }

                        Collections.sort(groupsList,new Comparator<Group>(){
                            @Override
                            public int compare(Group group1, Group group2) {
                                //TODO: bisognerà aggiungere il controllo sul campo "LastChange" per far si che non dipenda dalla data di creazione, ma dall'ultima modifica!
                                try{
                                    SimpleDateFormat formatter =  new SimpleDateFormat("dd/MM/yyy HH:mm");
                                    Date d1 = formatter.parse(group1.getDate());
                                    long timestamp1 = d1.getTime();
                                    Date d2 = formatter.parse(group2.getDate());
                                    long timestamp2 = d2.getTime();
                                    if (timestamp1 <= timestamp2) {
                                        return -1;
                                    } else {
                                        return 1;
                                    }
                                } catch(ParseException e){
                                    Log.e("error parsing",e.getMessage());
                                }
                                return 0;                            }
                        });


                        ((CustomAdapter) list.getAdapter()).setGroupList(groupsList);
                        if (list.getAdapter().getCount() == 0){
                            noGroup_textView.setVisibility(View.VISIBLE);
                        }
                        list.invalidate();
                        list.requestLayout();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Failed to read value.", error.toException());
                    }
                });

                CustomAdapter adapter = new CustomAdapter(getContext(),groupsList);
                list.setAdapter(adapter);



                    return rootView;

            }
            else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {

                rootView = inflater.inflate(R.layout.personal_section_page, container, false);
                list_summary = (ListView) rootView.findViewById(R.id.lv_personal_section);
                noSummary_textView = (TextView) rootView.findViewById(R.id.noSummary_tv);

                DatabaseReference currencyRef = database.getReference("currencies");
                currencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        USDtoEUR = Double.parseDouble(dataSnapshot.child("USDtoEUR").getValue().toString());
                        EURtoUSD = Double.parseDouble(dataSnapshot.child("EURtoUSD").getValue().toString());

                        String userID = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
                        myRef_summary_debits = database.getReference("users/" + userID + "/debits");
                        myRef_summary_credits = database.getReference("users/" + userID + "/credits");

                        CustomAdapterSummary adapter = new CustomAdapterSummary(getContext(),summaryList,selectedCurrency);
                        list_summary.setAdapter(adapter);

                        myRef_summary_debits.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    credit = false; // debits section --> it's a debit
                                    Summary tmp = new Summary(data.child("DisplayName").getValue().toString().replace(",", "."),
                                            data.child("Money").getValue().toString(),data.child("Paying").getValue().toString(),
                                            data.child("Currency").getValue().toString(),
                                            credit);
                                    indexSummary = debitsList.size();
                                    debitsList.add(indexSummary, tmp);
                                }
                                //tmpList = ((CustomAdapterSummary)list_summary.getAdapter()).getSummaryList();

                                //debitsList.addAll(tmpList);

                                Iterator<Summary> itDeb = debitsList.iterator();
                                while (itDeb.hasNext()) {
                                    Summary sum = itDeb.next();
                                    if (tot.containsKey(sum.getName())) {
                                        Double newtot;
                                        if (sum.getCurrency().equals("€") && selectedCurrency.equals("$")){
                                            Double money = Double.valueOf(sum.getValue())*EURtoUSD;
                                            Float past = Float.valueOf(tot.get(sum.getName()).getValue());
                                            newtot = Math.round((past - money) * 100.0) / 100.0;
                                        } else if (sum.getCurrency().equals("$") && selectedCurrency.equals("€")){
                                            Double money = Double.valueOf(sum.getValue())*USDtoEUR;
                                            Float past = Float.valueOf(tot.get(sum.getName()).getValue());
                                            newtot = Math.round((past - money) * 100.0) / 100.0;
                                        } else {
                                            Float past = Float.valueOf(tot.get(sum.getName()).getValue());
                                            newtot = Math.round((past - Float.valueOf(sum.getValue())) * 100.0) / 100.0;
                                            //Toast.makeText(getContext(),"newtot: "+newtot.toString(),Toast.LENGTH_SHORT).show();
                                        }
                                        boolean flag = true;
                                        if (newtot < 0)
                                            flag = false;
                                        tot.put(sum.getName(), new Summary(sum.getName(), Double.toString(newtot), sum.getEmail(),selectedCurrency,flag));


                                    } else {
                                        if (sum.getCurrency().equals("€") && selectedCurrency.equals("$")){
                                            Double money = -1*Math.round(Double.valueOf(sum.getValue())*EURtoUSD * 100.0)/100.0;
                                            sum.setValue(String.valueOf(money));
                                        }else if (sum.getCurrency().equals("$") && selectedCurrency.equals("€")){
                                            Double money = -1*Math.round(Double.valueOf(sum.getValue())*USDtoEUR * 100.0)/100.0;
                                            sum.setValue(String.valueOf(money));
                                        }else{
                                            Double money = -1*Double.valueOf(sum.getValue());
                                            sum.setValue(String.valueOf(money));
                                        }
                                        tot.put(sum.getName(), sum);
                                    }

                                }


                                myRef_summary_credits.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                            credit = true; // credits section --> it's a credit
                                            Summary tmp = new Summary(data.child("DisplayName").getValue().toString().replace(",","."),
                                                    data.child("Money").getValue().toString(),
                                                    data.child("Debitor").getValue().toString(),
                                                    data.child("Currency").getValue().toString(),
                                                    credit);
                                            indexSummary = creditsList.size();
                                            creditsList.add(indexSummary, tmp);
                                        }

                                        //tmpList = ((CustomAdapterSummary)list_summary.getAdapter()).getSummaryList();
                                        //creditsList.addAll(tmpList);
                                        Iterator<Summary> itCred=creditsList.iterator();
                                        while(itCred.hasNext()){
                                            Summary sum = itCred.next();
                                            if(tot.containsKey(sum.getName())){
                                                Double newtot;
                                                if (sum.getCurrency().equals("€") && selectedCurrency.equals("$")){
                                                    Double money = Double.valueOf(sum.getValue())*EURtoUSD;
                                                    Float past = Float.valueOf(tot.get(sum.getName()).getValue());
                                                    newtot = Math.round((past + money) * 100.0) / 100.0;
                                                } else if (sum.getCurrency().equals("$") && selectedCurrency.equals("€")){
                                                    Double money = Double.valueOf(sum.getValue())*USDtoEUR;
                                                    Float past = Float.valueOf(tot.get(sum.getName()).getValue());
                                                    newtot = Math.round((past + money) * 100.0) / 100.0;
                                                } else {
                                                    Float past = Float.valueOf(tot.get(sum.getName()).getValue());
                                                    newtot = Math.round((past + Float.valueOf(sum.getValue())) * 100.0) / 100.0;
                                                }

                                                tot.put(sum.getName(),new Summary(sum.getName(),Double.toString(newtot),sum.getEmail(),
                                                        selectedCurrency, true));

                                            }else{
                                                if (sum.getCurrency().equals("€") && selectedCurrency.equals("$")){
                                                    Double money = Math.round(Double.valueOf(sum.getValue())*EURtoUSD * 100.0)/100.0;
                                                    sum.setValue(String.valueOf(money));
                                                }else if (sum.getCurrency().equals("$") && selectedCurrency.equals("€")){
                                                    Double money = Math.round(Double.valueOf(sum.getValue())*USDtoEUR * 100.0)/100.0;
                                                    sum.setValue(String.valueOf(money));
                                                }
                                                tot.put(sum.getName(),sum);
                                            }

                                        }
                                        summaryList = new ArrayList<>(tot.values());
                                        list_summary.setAdapter(new CustomAdapterSummary(getContext(), summaryList,selectedCurrency));

                                        if (list_summary.getAdapter().getCount() == 0){
                                            noSummary_textView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        Log.w("Failed to read value.", error.toException());
                                    }
                                });

                                summaryList = new ArrayList<>(tot.values());
                                list_summary.setAdapter(new CustomAdapterSummary(getContext(), summaryList,selectedCurrency));

                            }
                            @Override
                            public void onCancelled(DatabaseError error){
                                Log.w("Failed to read value.", error.toException());
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return rootView;

            } else {
                rootView = inflater.inflate(R.layout.contacts_section_page, container, false);
                list = (ListView) rootView.findViewById(R.id.lv_contacts_page);
                noContact_textView = (TextView) rootView.findViewById(R.id.noContact_tv);

                //TODO: prendere i dati degli amici e visualizzarli qui
                //con il formato contact_item
                myRef = database.getReference("users/"+UserID+"/contacts/");
                if (myRef!=null) {
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                Iterator<Contact> it = contactsList.iterator();
                                boolean flag = false;
                                while (it.hasNext()) {
                                    if (it.next().getEmail().equals(data.getKey().replace(",","."))){
                                        flag = true;
                                    }
                                }
                                if (!flag){
                                    try {
                                        String name = data.child("Name").getValue().toString();
                                        String surname =  data.child("Surname").getValue().toString();
                                        String username = data.child("Username").getValue().toString();
                                        String email = data.child("Email").getValue().toString();
                                        if (data.hasChild("Image")) {
                                            contactsList.add(indexContact,new Contact(name,surname,username,email,data.child("Image").getValue().toString()));

                                        }else {
                                            contactsList.add(indexContact,new Contact(name,surname,username,email,"no_image"));                                        }

                                        indexContact++;

                                    }catch(Error e){
                                        Toast.makeText(getContext(), e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            if (list.getAdapter().getCount() == 0){
                                noContact_textView.setVisibility(View.VISIBLE);
                            }

                            list.invalidate();
                            list.requestLayout();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.w("Failed to read value.", error.toException());
                        }
                    });
                }

                CustomAdapterContacts adapter = new CustomAdapterContacts(getContext(),contactsList);
                list.setAdapter(adapter);
                return rootView;
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {// Show 2 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_groups);
                case 1:
                    return getString(R.string.tab_my_section);
                case 2:
                    return getString(R.string.tab_contacts);
            }
            return null;
        }
    }


    private void animateFab(int position) {
        switch (position) {
            case 0:
                fab_groups.show();
                fab_contacts.hide();
                fab_groups.bringToFront();
                break;
            case 2:
                fab_contacts.show();
                fab_groups.hide();
                fab_contacts.bringToFront();
                break;

            default:
                fab_groups.hide();
                fab_contacts.hide();
                fab_groups.bringToFront();
                break;
        }
    }

    public void onClickNewGroup(View view) {
        Intent intent = new Intent(view.getContext(), NewGroupActivityPhase1.class);
        startActivityForResult(intent,GROUP_CREATION);
    }

    public void onClickNewContact(View view) {
        Intent intent = new Intent(view.getContext(), AddNewContacts.class);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GROUP_CREATION) {
            if(resultCode == RESULT_OK){
                ListView list = (ListView) findViewById(R.id.lv_contacts_page);

                Group tmp = new Group(getIntent().getStringExtra("IDGroup"),
                        getIntent().getStringExtra("Name"),
                        getIntent().getStringExtra("Author"),
                        getIntent().getStringExtra("Date"),
                        "0",
                        "0",
                        "no_image",
                        getIntent().getStringExtra("Currency"),
                        "False",
                        "True");
                ((CustomAdapter) list.getAdapter()).getGroupList().add(tmp);

//                list.setAdapter(new CustomAdapter(MainActivity.this,groupList));
               ((CustomAdapter) list.getAdapter()).notifyDataSetChanged();
                Log.e("provaResult","received and added");
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}


