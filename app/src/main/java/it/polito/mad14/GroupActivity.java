package it.polito.mad14;

import android.app.Activity;
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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.polito.mad14.myDataStructures.Expense;
import it.polito.mad14.myDataStructures.Group;
import it.polito.mad14.myDataStructures.Summary;
import it.polito.mad14.myListView.CustomAdapter;
import it.polito.mad14.myListView.CustomAdapterExpenses;
import it.polito.mad14.myListView.CustomAdapterSummary;


public class GroupActivity extends AppCompatActivity {
    public static final int EXPENSE_CREATION=1;


    private ListView list;
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
    private String IDGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        list = (ListView) findViewById(R.id.list_view_expenses);


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
        IDGroup = myIntent.getStringExtra("IDGroup");

        Toast.makeText(GroupActivity.this, IDGroup,
                Toast.LENGTH_SHORT).show();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_group_activity);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this,ExpenseCreation.class);
                intent.putExtra("IDGroup", IDGroup);
//                startActivity(intent);
                startActivityForResult(intent,EXPENSE_CREATION);
            }
        });


        //

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXPENSE_CREATION){
            if (resultCode == RESULT_OK){
                list.invalidate();
                list.requestLayout();
            }
        }
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
        ArrayList<Expense> expensesList = new ArrayList<>();
        private int indexExp=0;
        ArrayList<Summary> summaryList = new ArrayList<>();
        private int indexSum=0;

        public PlaceholderFragment() {
        }

        /**
         * ELENA:
         *  qui di seguito metto delle variabili che servono per popolare le view, che verranno poi
         *  popolate tramite la lettura dal database!
         */

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

            String IDGroup=getActivity().getIntent().getStringExtra("IDGroup");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("groups/" + IDGroup + "/items");

            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {

                            Iterator<Expense> it = expensesList.iterator();
                            boolean flag = false;
                            while (it.hasNext()) {
                                if (it.next().getName().equals(data.child("Name").getValue().toString()))
                                    flag = true;
                            }
                            if (!flag) {
                                Expense tmp = new Expense(data.child("Name").getValue().toString(),
                                        data.child("Price").getValue().toString(),
                                        data.child("Description").getValue().toString(),
                                        data.child("Author").getValue().toString());
                                expensesList.add(indexExp, tmp);
                                indexExp++;
                            }
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Failed to read value.", error.toException());

                    }
                });
                // popolamento della pagina
                View rootView = inflater.inflate(R.layout.expenses_list_page, container, false);
                ListView list = (ListView) rootView.findViewById(R.id.list_view_expenses);
                CustomAdapterExpenses adapter = new CustomAdapterExpenses(this.getActivity(),expensesList);
                list.setAdapter(adapter);

                return rootView;
            }
            else {
                //TODO: lettura da db per popolare la lista->non ancora implementato
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
