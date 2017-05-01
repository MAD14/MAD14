package it.polito.mad14;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.polito.mad14.myDataStructures.Expense;
import it.polito.mad14.myDataStructures.Group;
import it.polito.mad14.myDataStructures.Summary;
import it.polito.mad14.myListView.CustomAdapter;
import it.polito.mad14.myListView.CustomAdapterExpenses;
import it.polito.mad14.myListView.CustomAdapterSummary;

public class GroupActivity extends AppCompatActivity {

    private String groupname;
    private ListView list;
    private ArrayList<Expense> expensesList;
    private int indexExp=0;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        expensesList = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Intent myIntent = getIntent();
        groupname = myIntent.getStringExtra("groupname");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("groups/" + groupname + "/items");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_group_activity);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this,ExpenseCreation.class);
                intent.putExtra("groupname", groupname);
                startActivity(intent);
            }
        });


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Expense tmp = new Expense(data.child("Name").getValue().toString(),
                            data.child("Price").getValue().toString(),
                            data.child("Description").getValue().toString(),
                            data.child("Author").getValue().toString());
                    expensesList.add(tmp);
                }

                list = (ListView) findViewById(R.id.list_view_expenses);
                ((CustomAdapterExpenses) list.getAdapter()).setExpensesList(expensesList);

                list.invalidate();
                list.requestLayout();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Failed to read value.", error.toException());

            }
        });
        //

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * ELENA:
         *  qui di seguito metto delle variabili che servono per popolare le view, che verranno poi
         *  popolate tramite la lettura dal database!
         */
        ArrayList<Expense> expensesList = new ArrayList<>();
        ArrayList<Summary> summaryList = new ArrayList<>();


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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                //TODO: lettura da db per popolare la lista (possibile popolarle entrambe in una lettura)
                expensesList = new ArrayList<>();

                // popolamento della pagina
                View rootView = inflater.inflate(R.layout.expenses_list_page, container, false);
                ListView list = (ListView) rootView.findViewById(R.id.list_view_expenses);
                CustomAdapterExpenses adapter = new CustomAdapterExpenses(this.getActivity(),expensesList);
                list.setAdapter(adapter);

                return rootView;
            }
            else {
                //TODO: lettura da db per popolare la lista
                summaryList.add(new Summary("Elena","21.50",false)); // questa sarà da sostituire con la lettura da db
                summaryList.add(new Summary("Michela","10.30",true));

                // popolamento della pagina
                View rootView = inflater.inflate(R.layout.summary_page, container, false);
                ListView list = (ListView) rootView.findViewById(R.id.list_view_summary);
                CustomAdapterSummary adapter = new CustomAdapterSummary(this.getActivity(),summaryList);
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
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "EXPENSES";
                case 1:
                    return "REPORT";

            }
            return null;
        }
    }
}
