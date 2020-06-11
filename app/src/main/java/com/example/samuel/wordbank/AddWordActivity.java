package com.example.samuel.wordbank;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.samuel.wordbank.data.Word;
import com.example.samuel.wordbank.data.WordStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AddWordActivity extends AppCompatActivity {

    private TextInputLayout etname;
    private TextInputLayout  etTranslate;
    private TextInputLayout  etMeaning;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentKey;
    private String currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        etname = (TextInputLayout ) findViewById(R.id.text_input_layout_name);
        etTranslate = (TextInputLayout ) findViewById(R.id.text_input_layout_translate);
        etMeaning = (TextInputLayout ) findViewById(R.id.text_input_layout_meaning);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }

        currentKey = extras.getString("currentWord");
        if(TextUtils.isEmpty(currentKey)){
            return;
        }

        database.getReference().child("users/"+ currentUser.getUid() +"/words").child(currentKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Word word = dataSnapshot.getValue(Word.class);
                etname.getEditText().setText(word.getWord());
                etMeaning.getEditText().setText(word.getMeaning());
                etTranslate.getEditText().setText(word.getTranslate());
                currentStatus = word.getStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void saveWord(View view) {
        String name = etname.getEditText().getText().toString();
        String translate = etTranslate.getEditText().getText().toString();
        String meaning = etMeaning.getEditText().getText().toString();

        if(TextUtils.isEmpty(name)) {
            etname.setError("Name is required");
            Snackbar.make(view, "Name is required", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }

        String key = "";
        String status = WordStatus.ADDED;

        if(TextUtils.isEmpty(currentKey)){
            key = myRef.child("users/"+ currentUser.getUid() +"/words/").push().getKey();
        }else {
            status = currentStatus;
            key = currentKey;
        }

        Word word = new Word(currentUser.getUid(), name, status, System.currentTimeMillis(),
                translate, meaning, key);
        Map<String, Object> wordValues = word.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("users/"+ currentUser.getUid() +"/words/" + key, wordValues);
        myRef.updateChildren(childUpdates);

        Toast.makeText(AddWordActivity.this, "Word Saved", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(AddWordActivity.this, WordsActivity.class));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddWordActivity.this, WordsActivity.class));
    }
}
