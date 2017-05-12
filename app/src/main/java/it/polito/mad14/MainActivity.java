package it.polito.mad14;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.media.session.PlaybackStateCompat;
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

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.polito.mad14.myDataStructures.Contact;
import it.polito.mad14.myDataStructures.Expense;
import it.polito.mad14.myDataStructures.Group;
import it.polito.mad14.myDataStructures.Summary;
import it.polito.mad14.myListView.CustomAdapter;
import it.polito.mad14.myListView.CustomAdapterContacts;
import it.polito.mad14.myListView.CustomAdapterSummary;

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
    private static String UserID;
    private static DatabaseReference myRef;
    private FloatingActionButton fab_groups;
    private FloatingActionButton fab_contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


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
                //
                break;
            case R.id.action_personal_profile:
                Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.action_invite_new_members:
                intent = new Intent(MainActivity.this, InviteToJoinCommunity.class);
                intent.putExtra("sender",UserID);
                startActivity(intent);
                break;
            case R.id.action_join_a_group:
                intent = new Intent(MainActivity.this, JoinGroupActivity.class);
                startActivity(intent);
                break;
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
        ArrayList<Contact> tmpList_contacts = new ArrayList<>();

        private int indexSummary=0;
        private boolean credit;

        //DatabaseReference myRefGroup = database.getReference("groups");

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
        private ListView list,list_summary,list_contacts;

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {


            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                rootView = inflater.inflate(R.layout.groups_list_page, container, false);
                list = (ListView) rootView.findViewById(R.id.list_view_main_activity);

                myRef = database.getReference("users/"+UserID+"/groups/");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {

                            Iterator<Group> it = groupsList.iterator();
                            boolean flag = false;
                            while (it.hasNext()) {
                                if (it.next().getID().equals(data.getKey()))
                                    flag = true;
                            }
                            if (!flag) {

                                try {
                                    String id = data.getKey();
                                    String nm = data.child("Name").getValue().toString();
                                    String own = data.child("Author").getValue().toString();
                                    String dat = data.child("Date").getValue().toString();
                                    groupsList.add(indexGroup, new Group(id, nm, own, dat));
                                    indexGroup++;
                                }
                                    catch(Error e){
                                        Toast.makeText(getContext(), e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }

                            }
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
                View rootView = inflater.inflate(R.layout.personal_section_page, container, false);
                list_summary = (ListView) rootView.findViewById(R.id.lv_personal_section);

                // popolamento della pagina
                //TODO: riepilogo dei soldi che si devono agli amici
                String userID = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
                DatabaseReference myRef_summary_debits = database.getReference("users/" + userID + "/debits");
                DatabaseReference myRef_summary_credits = database.getReference("users/" + userID + "/credits");

                CustomAdapterSummary adapter = new CustomAdapterSummary(getContext(),summaryList);
                list_summary.setAdapter(adapter);

                myRef_summary_debits.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            credit = false; // debits section --> it's a debit
                            Summary tmp = new Summary(data.child("Paying").getValue().toString(),
                                    data.child("Money").getValue().toString(),
                                    credit);
                            indexSummary = debitsList.size();
                            debitsList.add(indexSummary, tmp);
                        }
                        tmpList = ((CustomAdapterSummary)list_summary.getAdapter()).getSummaryList();
                        debitsList.addAll(tmpList);
                        list_summary.setAdapter(new CustomAdapterSummary(getContext(),debitsList));
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Failed to read value.", error.toException());
                    }
                });
                myRef_summary_credits.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            credit = true; // credits section --> it's a credit
                            Summary tmp = new Summary(data.child("Debitor").getValue().toString(),
                                    data.child("Money").getValue().toString(),
                                    credit);
                            indexSummary = creditsList.size();
                            creditsList.add(indexSummary, tmp);
                        }
                        tmpList = ((CustomAdapterSummary)list_summary.getAdapter()).getSummaryList();
                        creditsList.addAll(tmpList);
                        list_summary.setAdapter(new CustomAdapterSummary(getContext(),creditsList));
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Failed to read value.", error.toException());

                    }
                });

                creditsList.addAll(debitsList);
                summaryList = creditsList;
                 adapter = new CustomAdapterSummary(getContext(),summaryList);
                list_summary.setAdapter(adapter);

                //
                //TODO: possibilità di segnare che si è pagato qualcuno
                //TODO: grafico riepilogo crediti/debiti

                //in questa vista possiamo aggiungere un bottone con scritto "salda/saldato" in modo da linkare subito alla conferma di pagamento effettuato/ricevuto
                return rootView;

            } else {
                final View rootView = inflater.inflate(R.layout.contacts_section_page, container, false);
                // popolamento della pagina
                list = (ListView) rootView.findViewById(R.id.lv_contacts_page);
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
                                        contactsList.add(indexContact,new Contact(name,surname,username,email));
                                        indexContact++;

                                    }catch(Error e){
                                        Toast.makeText(getContext(), e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
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
                    return "GROUPS";
                case 1:
                    return "MY SECTION";
                case 2:
                    return "CONTACTS";
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
        startActivity(intent);
    }

    public void onClickNewContact(View view) {
        Intent intent = new Intent(view.getContext(), AddNewContacts.class);
        startActivity(intent);
    }
}


