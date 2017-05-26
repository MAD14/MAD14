package it.polito.mad14;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

import it.polito.mad14.myDataStructures.Expense;
import it.polito.mad14.myDataStructures.Summary;
import it.polito.mad14.myListView.CustomAdapterExpenses;
import it.polito.mad14.myListView.CustomAdapterSummaryGroup;


public class GroupActivity extends AppCompatActivity {
    public static final int EXPENSE_CREATION=1;
    private DatabaseReference myReference;
    private String groupName,groupAuthor,groupDescription,groupDate,groupImage,creator;
    private FirebaseDatabase database;

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
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
	    NotificationManager nManager = ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
        nManager.cancelAll();



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


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_group_activity);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this,ExpenseCreation.class);
                intent.putExtra("IDGroup", IDGroup);
                startActivityForResult(intent,EXPENSE_CREATION);
//                startActivity(intent);
                finish();
            }
        });


        //

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXPENSE_CREATION){
            if (resultCode == RESULT_OK){
                ListView list = (ListView) findViewById(R.id.list_view_expenses);
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
        switch (id){
            case R.id.silenzioso:
                //TODO: disattivare le notifiche push
                Toast.makeText(GroupActivity.this,"Notifications disabled",Toast.LENGTH_SHORT).show();
                break;
            case R.id.add_members:
//
                System.out.println("IDGroup : "+IDGroup);
               // myReference = database.getReference("groups/" + IDGroup );
                myReference = database.getInstance().getReference("groups/" + IDGroup );
                System.out.println("id groups : "+IDGroup+" proviamo");
                myReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        groupName = dataSnapshot.child("Name").getValue().toString();
                        groupAuthor = dataSnapshot.child("Author").getValue().toString();
                        groupDescription = dataSnapshot.child("Description").getValue().toString();
                        groupDate = dataSnapshot.child("Date").getValue().toString();
                        groupImage = dataSnapshot.child("Image").getValue().toString();

                        intent = new Intent(GroupActivity.this,AddNewMembersToGroup.class);
                        intent.putExtra("IDgroup",IDGroup);
                        intent.putExtra("Name",groupName);
                        intent.putExtra("Author",groupAuthor);
                        intent.putExtra("Date",groupDate);
                        intent.putExtra("Description",groupDescription);
                        intent.putExtra("Image",groupImage);
                        startActivity(intent);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                break;
//            case R.id.action_edit_group:
//                intent = new Intent(GroupActivity.this, EditGroupActivity.class);
//
//                startActivity(intent);
//                finish();
//                break;
                
            case R.id.info:
                intent = new Intent(GroupActivity.this,InfoGroupActivity.class);
                intent.putExtra("IDGroup",IDGroup);
                myReference = database.getInstance().getReference("groups/" + IDGroup );
                myReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        groupImage = dataSnapshot.child("Image").getValue().toString();
                        groupName = dataSnapshot.child("Name").getValue().toString();
                        groupDescription = dataSnapshot.child("Description").getValue().toString();
                        groupDate = dataSnapshot.child("Date").getValue().toString();
                        groupAuthor = dataSnapshot.child("Author").getValue().toString();
                        groupImage = dataSnapshot.child("Image").getValue().toString();

                        intent.putExtra("Image",groupImage);
                        intent.putExtra("Name",groupName);
                        intent.putExtra("Date",groupDate);
                        intent.putExtra("Author",groupAuthor);
                        intent.putExtra("Image",groupImage);
                        intent.putExtra("Description",groupDescription);
                        startActivity(intent);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
        private static final String ARG_SECTION_NUMBER = "section_number";
        ArrayList<Expense> expensesList = new ArrayList<>();
        private int indexExp=0;
        ArrayList<Summary> summaryList = new ArrayList<>();
        ArrayList<Summary> newSummaryList = new ArrayList<>();
        private int newIndexSummary=0;
        private int indexSummary=0;
        private boolean credit;
        private String user;

        public FirebaseDatabase database;

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
        private ListView list_expenses,list_summary;
        private String name,email;
        private String IDGroup;
        private TextView noExpense_textView, noReport_textView;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            IDGroup = getActivity().getIntent().getStringExtra("IDGroup");
            database = FirebaseDatabase.getInstance();
            user = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
            DatabaseReference myRef_expenses = database.getReference("groups/" + IDGroup + "/items");
            DatabaseReference myRef_summary = database.getReference("groups/" + IDGroup + "/debits");

            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                rootView = inflater.inflate(R.layout.expenses_list_page, container, false);
                list_expenses = (ListView) rootView.findViewById(R.id.list_view_expenses);
                noExpense_textView = (TextView) rootView.findViewById(R.id.noExpenses_tv);


                CustomAdapterExpenses adapter = new CustomAdapterExpenses(getContext(), expensesList);
                list_expenses.setAdapter(adapter);

                myRef_expenses.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        expensesList = new ArrayList<>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                Expense tmp = new Expense(data.child("Name").getValue().toString(),
                                        data.child("Price").getValue().toString(),
                                        data.child("Description").getValue().toString(),
                                        data.child("Author").getValue().toString(),
                                        IDGroup,
                                        data.child("Image").getValue().toString(),
                                        data.child("Date").getValue().toString());
                                indexExp = expensesList.size();
                                expensesList.add(indexExp, tmp);
                        }
                        Collections.sort(expensesList,new Comparator<Expense>(){
                            @Override
                            public int compare(Expense expense1, Expense expense2) {
                                try{
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                    Date d1 = formatter.parse(expense1.getDate());
                                    long timestamp1 = d1.getTime();
                                    Date d2 = formatter.parse(expense2.getDate());
                                    long timestamp2 = d2.getTime();
                                    if (timestamp1 <= timestamp2) {
                                        return 1;
                                    } else {
                                        return -1;
                                    }
                                } catch(ParseException e){
                                    Log.e("error parsing",e.getMessage());
                                }
                                return 0;                            }
                        });
                        CustomAdapterExpenses adapter = new CustomAdapterExpenses(getContext(),expensesList);
                        list_expenses.setAdapter(adapter);
//                        ((CustomAdapterExpenses) list_expenses.getAdapter()).setExpensesList(expensesList);
//                        list_expenses.invalidate();
//                        list_expenses.requestLayout();
                        if (list_expenses.getAdapter().getCount() == 0){
                            noExpense_textView.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Failed to read value.", error.toException());
                    }
                });


                return rootView;
            }
            else { // summary page
                rootView = inflater.inflate(R.layout.summary_page, container, false);
                list_summary = (ListView) rootView.findViewById(R.id.list_view_summary);
                noReport_textView = (TextView) rootView.findViewById(R.id.noReport_tv);

                CustomAdapterSummaryGroup adapter = new CustomAdapterSummaryGroup(getContext(),summaryList,IDGroup);
                list_summary.setAdapter(adapter);

                myRef_summary.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            if (data.child("Receiver").getValue().toString().equals(user) ||
                                    data.child("Sender").getValue().toString().equals(user)) {

                                if (data.child("Receiver").getValue().toString().equals(user)) {
                                    credit = true;
                                    name = data.child("DisplayNameSender").getValue().toString();
                                    email= data.child("Sender").getValue().toString();
                                } else {
                                    credit = false;
                                    name = data.child("DisplayNameReceiver").getValue().toString();
                                    email = data.child("Receiver").getValue().toString();
                                }

                                boolean flag = false;
                                for (int i = 0; i < summaryList.size(); i++) {
                                    if (summaryList.get(i).getName().equals((name))) {
                                        flag = true;
                                        newIndexSummary = i;
                                        break;
                                    }
                                }

                                if (flag) {
                                    if (credit) {
                                        Summary old = summaryList.get(newIndexSummary);
                                        Float val=Float.valueOf(old.getValue());
                                        // old is a debit
                                        if(!old.getCredit())
                                            val=val*(-1);

                                        Double fin = Math.round((val + Float.valueOf(data.child("Money").getValue().toString()))*100.0)/100.0;
                                        if (fin > 0) {
                                            summaryList.remove(newIndexSummary);

                                            summaryList.add(newIndexSummary, new Summary(old.getName(), fin.toString(),old.getEmail(),old.getCurrency(), credit));
                                        }else {
                                            summaryList.remove(newIndexSummary);
                                            summaryList.add(newIndexSummary, new Summary(old.getName(), fin.toString(), old.getEmail(),old.getCurrency(), false));

                                        }

                                    } else {
                                        Summary old = summaryList.get(newIndexSummary);
                                        Float val=Float.valueOf(old.getValue());
                                        // old is a debit
                                        if(!old.getCredit())
                                            val=val*(-1);
                                        Double fin = Math.round((val - Float.valueOf(data.child("Money").getValue().toString()))*100.0)/100.0;
                                        if (fin < 0) {
                                            summaryList.remove(newIndexSummary);

                                            summaryList.add(newIndexSummary, new Summary(old.getName(), fin.toString(),old.getEmail(),old.getCurrency(), credit));
                                        }
                                        else {
                                            summaryList.remove(newIndexSummary);
                                            summaryList.add(newIndexSummary, new Summary(old.getName(), fin.toString(),old.getEmail(),old.getCurrency(), true));

        
                                        }
                                    }
                                } else {
                                    Summary tmp = new Summary(name,

                                            data.child("Money").getValue().toString(),email,
                                            data.child("Currency").getValue().toString(),credit);

                                            
                                    summaryList.add(indexSummary, tmp);
                                    indexSummary++;
                                }
                            }
                        }


//                        tmpList = ((CustomAdapterSummary)list_summary.getAdapter()).getSummaryList();
//                        summaryList.addAll(tmpList);
//                        list_summary.setAdapter(new CustomAdapterSummary(getContext(),summaryList));
                        ((CustomAdapterSummaryGroup) list_summary.getAdapter()).setSummaryList(summaryList);
                        list_summary.invalidate();
                        list_summary.requestLayout();
                        if (list_summary.getAdapter().getCount() == 0){
                            noReport_textView.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Failed to read value.", error.toException());

                    }
                });

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
                    return getString(R.string.tab_expenses);
                case 1:
                    return getString(R.string.tab_report);

            }
            return null;
        }
    }

}
