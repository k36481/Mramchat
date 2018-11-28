package com.satirev.kim.mramchat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostWriteActivity extends AppCompatActivity {

    private EditText mEdtxtPostWrite;
    private Button mBtnPostWriteConfirm;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_write);
        mEdtxtPostWrite = findViewById(R.id.edtxt_post_write);
        mBtnPostWriteConfirm = findViewById(R.id.btn_post_write_confirm);

        mDatabase  = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();

        mBtnPostWriteConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Post post = new Post(mAuth.getCurrentUser().getEmail(),mEdtxtPostWrite.getText().toString());
                mDatabaseRef.child("posts").push().setValue(post);
                onBackPressed();
            }
        });

    }
}
