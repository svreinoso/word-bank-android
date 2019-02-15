package com.example.samuel.wordbank;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samuel.wordbank.data.Word;
import com.example.samuel.wordbank.data.WordStatus;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WordsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "WordsActivity";
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Word> words;
    private List<Word> wordsFiltered;
    private int currentPosition;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(new Intent(getApplicationContext(), AddWordActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        headerView = navigationView.getHeaderView(0);
        TextView navHeaderTitle = (TextView) headerView.findViewById(R.id.tvNavheaderTitle);
        TextView navHeaderSubTitle = (TextView) headerView.findViewById(R.id.tvNavHeaderSubTItle);
        navHeaderTitle.setText(currentUser.getDisplayName());
        navHeaderSubTitle.setText(currentUser.getEmail());

        getProthoUrl();

        loadData();

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Word word = words.get(position);
                Toast.makeText(getApplicationContext(), word.getName() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

                currentPosition = position;
                PopupMenu menu = new PopupMenu (WordsActivity.this, view);
                menu.setOnMenuItemClickListener (new PopupMenu.OnMenuItemClickListener ()
                {
                    @Override
                    public boolean onMenuItemClick (MenuItem item)
                    {
                        int id = item.getItemId();
                        switch (id)
                        {
                            case R.id.item_edit:
                                Intent intent = new Intent(WordsActivity.this, AddWordActivity.class);
                                intent.putExtra("currentWord", words.get(currentPosition).getKey());
                                startActivity(intent);
                                break;
                            case R.id.item_set_as_learning:
                                changeStatus(WordStatus.LEARNING, currentPosition);
                                break;
                            case R.id.item_set_as_learned:
                                changeStatus(WordStatus.LEARNED, currentPosition);
                                break;
                            case R.id.item_delete:
                                myRef.child("words").child(words.get(currentPosition).getKey()).removeValue();
                                break;
                        }
                        return true;
                    }
                });
                menu.inflate (R.menu.word_row);
                menu.show();
            }
        }));
    }

    private void getProthoUrl() {
        Uri uri = currentUser.getPhotoUrl();
        ImageView iv = (ImageView) headerView.findViewById(R.id.imageView);
        Picasso.get().load(uri.toString() + "?type=large").into(iv);
    }

    private void changeStatus(int newStatus, int positio){
        Word word = words.get(positio);

        myRef.child("words").child(word.getKey()).child("status").setValue(newStatus);
    }

    private void loadData(){

        myRef.child("words").orderByChild("userId").equalTo(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                words = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Word word = noteDataSnapshot.getValue(Word.class);
                    words.add(word);
                }
                wordsFiltered = words;
                mAdapter = new MyAdapter(wordsFiltered, WordsActivity.this);
                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void filterByStatus(int status) {

        wordsFiltered = new ArrayList<Word>();
        if(status == 0){
            wordsFiltered = words;
        }else {

            for (Word word : words) {
                if(word.getStatus() == status){
                    wordsFiltered.add(word);
                }
            }
        }

        mAdapter = new MyAdapter(wordsFiltered, WordsActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void filterByDate(Calendar startDate, Calendar endDate){

        wordsFiltered = new ArrayList<Word>();
        for (Word word : words) {
            if(word.getCreatedDate() >= startDate.getTimeInMillis() & word.getCreatedDate() <= endDate.getTimeInMillis())
            {
                wordsFiltered.add(word);
            }
        }
        mAdapter = new MyAdapter(wordsFiltered, WordsActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }

    private void getFilterdates() {
        final Calendar startDateCalendar = Calendar.getInstance();
        final Calendar endDateCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                endDateCalendar.set(Calendar.YEAR, year);
                endDateCalendar.set(Calendar.MONTH, monthOfYear);
                endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
                endDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                endDateCalendar.set(Calendar.MINUTE, 0);
                endDateCalendar.set(Calendar.SECOND, 0);

                filterByDate(startDateCalendar, endDateCalendar);

            }

        };

        DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                startDateCalendar.set(Calendar.YEAR, year);
                startDateCalendar.set(Calendar.MONTH, monthOfYear);
                startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                startDateCalendar.set(Calendar.MINUTE, 0);
                startDateCalendar.set(Calendar.SECOND, 0);

                new DatePickerDialog(WordsActivity.this, endDate, endDateCalendar
                        .get(Calendar.YEAR), endDateCalendar.get(Calendar.MONTH),
                        endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
                showMessage("Select end date");

            }

        };


        new DatePickerDialog(WordsActivity.this, startDate, startDateCalendar
                .get(Calendar.YEAR), startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

        showMessage("Select start date");

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.words, menu);
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
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        startActivity(new Intent(WordsActivity.this, MainActivity.class));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.filter_by_learning) {
            showMessage("Showing Learning");
            filterByStatus(WordStatus.LEARNING);
        } else if (id == R.id.filter_by_learned) {
            showMessage("Showing Learned");
            filterByStatus(WordStatus.LEARNED);
        } else if (id == R.id.filter_by_added) {
            showMessage("Showing Added");
            filterByStatus(WordStatus.ADDED);
        } else if (id == R.id.show_all) {
            showMessage("Showing All");
            filterByStatus(0);
        }else if (id == R.id.filter_by_date_range) {
           getFilterdates();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showMessage(String message) {
        Toast.makeText(WordsActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
