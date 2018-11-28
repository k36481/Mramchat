package com.satirev.kim.mramchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    final private Integer mREQUEST_CUSTOM_MACRO = 1000;
    private ArrayList<String> mMacroStrings;
    private ArrayMap<String, Integer> mMacroMap;
    private Intent mMacroData;
    private TextView mTxtview_Current_user;
    private EditText mEdtxt_Login_username;
    private EditText mEdtxt_Login_password;
    private Button mBtnSignIn;
    private Button mBtnPost;
    private Button mBtnDrawing;
    private Button mBtn_Sign_out;

    private FirebaseAuth mAuth;
    private String TAG = "LOG" + this.getClass().getSimpleName();

    private void invalidateLogin() {
        if (mAuth.getCurrentUser() != null) {
            mTxtview_Current_user.setText(mAuth.getCurrentUser().getEmail());
            mEdtxt_Login_username.setVisibility(View.GONE);
            mEdtxt_Login_password.setVisibility(View.GONE);
            mBtnSignIn.setText("TALK");
            mBtnPost.setVisibility(View.VISIBLE);
            mBtn_Sign_out.setVisibility(View.VISIBLE);
            mBtnDrawing.setVisibility(View.VISIBLE);
        } else {
            mTxtview_Current_user.setText("Sign Out");
            mEdtxt_Login_username.setVisibility(View.VISIBLE);
            mEdtxt_Login_password.setVisibility(View.VISIBLE);
            mBtnSignIn.setText("SIGN IN");
            mBtnDrawing.setVisibility(View.GONE);
            mBtnPost.setVisibility(View.GONE);
            mBtn_Sign_out.setVisibility(View.GONE);
            ScaleAnimation grow = new ScaleAnimation(
                    0f, 1f, 0f, 1f
            );
            grow.setDuration(500);
            grow.setInterpolator(new BounceInterpolator());
            mEdtxt_Login_username.startAnimation(grow);
            mEdtxt_Login_password.startAnimation(grow);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEdtxt_Login_username = findViewById(R.id.login_email);
        mEdtxt_Login_password = findViewById(R.id.login_password);
        mBtnSignIn = findViewById(R.id.login_button);
        mTxtview_Current_user = findViewById(R.id.current_user);
        mBtn_Sign_out = findViewById(R.id.logout_button);
        mBtnDrawing = findViewById(R.id.drawing_view);
        mBtnPost = findViewById(R.id.post_enter);

        mAuth = FirebaseAuth.getInstance();

        mBtn_Sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                invalidateLogin();
            }
        });

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(LoginActivity.this, MessageActivity.class);
                    startActivity(intent);
                } else {
                    String name = mEdtxt_Login_username.getText().toString();
                    String password = mEdtxt_Login_password.getText().toString();
                    if (name.equals("") || password.equals("")) {
                        AlertDialog.Builder msgbox = new AlertDialog.Builder(LoginActivity.this);
                        msgbox.setMessage("노크하고 오세요");
                        msgbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        msgbox.create().show(); //login success
                    } else {
                        mAuth.signInWithEmailAndPassword(name, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "signInWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            Toast.makeText(getApplicationContext(), "Welcome! " + user.getEmail(), Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(LoginActivity.this, MessageActivity.class);
                                            startActivity(intent);
//                                            invalidateLogin();
                                        } else {
                                            Log.d(TAG, "signInWithEmail:fail");
                                            AlertDialog.Builder msgbox = new AlertDialog.Builder(LoginActivity.this);
                                            msgbox.setMessage("그런 사람 없어요");
                                            msgbox.setNeutralButton("다시 시도", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            });
                                            msgbox.show();
                                        }
                                    }
                                });
                    }
                }
            }
        });

        mBtnDrawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, DrawingActivity.class);
                startActivityForResult(intent, mREQUEST_CUSTOM_MACRO);
            }
        });

        mBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, PostActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onStart() {
        Log.d(TAG, "login activity start");
        super.onStart();
        invalidateLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1000: //
                    mMacroData = data;
                    Log.d("onActivityResult", data.toString());
                    break;
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "login activity new intent");
        super.onNewIntent(intent);
        setIntent(intent);
    }
}

