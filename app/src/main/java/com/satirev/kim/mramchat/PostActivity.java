package com.satirev.kim.mramchat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class PostActivity extends AppCompatActivity {

    private ScrollView mScrollPostContainer;
    private LinearLayout mPostContainer;
    private Button mBtnWritePost;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_container);
        mPostContainer = findViewById(R.id.linear_post_container);
        mBtnWritePost = findViewById(R.id.btn_post_write);
        mScrollPostContainer = findViewById(R.id.scrollView_post);
        mScrollPostContainer.requestDisallowInterceptTouchEvent(true);
        final LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();

        mBtnWritePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostActivity.this, PostWriteActivity.class);
                startActivity(intent);
            }
        });

        mDatabase.getReference("/posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                final String key = dataSnapshot.getKey();
                String userName = dataSnapshot.child("userName").getValue().toString();
                String mainText = dataSnapshot.child("mainText").getValue().toString();
                Integer like = Integer.parseInt(dataSnapshot.child("like").getValue().toString());
                LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.post_box, null);
                ((TextView) view.getChildAt(0)).setText(userName + "  likes:" + like);
                ((TextView) view.getChildAt(1)).setText(mainText);
                mPostContainer.addView(view);
                view.setTag(key);

                view.getChildAt(0).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        mDatabaseRef.child("posts").child(key).child("like").runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Integer like = mutableData.getValue(Integer.class);
                                like += 1;
                                mutableData.setValue(like);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                            }
                        });
                        return false;
                    }
                });

                view.getChildAt(1).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
//                        if (motionEvent.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
                        Integer maxline = ((TextView) view).getMaxLines();
                        if (maxline == 5) {
                            ((TextView) view).setMaxLines(Integer.MAX_VALUE);
                        } else {
                            ((TextView) view).setMaxLines(5);
                        }
//                        }
                        return false;
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String userName = dataSnapshot.child("userName").getValue().toString();
                String mainText = dataSnapshot.child("mainText").getValue().toString();
                Integer like = Integer.parseInt(dataSnapshot.child("like").getValue().toString());
                LinearLayout linearLayout = mPostContainer.findViewWithTag(dataSnapshot.getKey());
                ((TextView) linearLayout.getChildAt(0)).setText(userName + "  likes:" + like);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
