package com.satirev.kim.mramchat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.speech.tts.TextToSpeech.ERROR;

public class MessageActivity extends AppCompatActivity {

    private Runnable mRunnable;
    private Handler mHandler;
    private static SpeechRecognizer mSpeechRecognizer;
    private final int LOGOUT = 101;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private Button mBtnVoice;
    private View mViewVoice;
    private RecognitionListener mRecognitionListener;
    private Button mSendBtn;
    private String mEmail;
    private EditText mEdtxt_message;
    private LinearLayout mBody;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private String TAG = "LOG" + this.getClass().getSimpleName();
    private TextToSpeech mTTS;

    private float mTTS_speed;
    private float mTTS_pitch;
    private boolean mTTsRead = true;
    private boolean mSpeechRecognition = true;

    void doPermAudio() {
        int MY_PERMISSIONS_RECORD_AUDIO = 1;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MessageActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_RECORD_AUDIO);
        }
    }

    private void writeNewText(String username, String text) {
        Message message = new Message(username, text);
        mDatabaseRef.child("messages").push().setValue(message);
    }

    private void textAnimation(LinearLayout view, String message) {
        boolean contains = false;
        String[] words = {"시발", "씨발", "새끼", "창년", "창녀", "지랄", "염병", "좆", "닥쳐", "미친놈"};
        for (String word : words) {
            if (message.contains(word)) {
                ((TextView) view.getChildAt(1)).setTextColor(Color.RED);
                ScaleAnimation grow = new ScaleAnimation(
                        3f, 1f, 3f, 1f
                );
                grow.setDuration(800);
                grow.setInterpolator(new BounceInterpolator());
                view.startAnimation(grow);
                contains = true;
                break;
            }
        }
        if (!contains) {
            ScaleAnimation grow = new ScaleAnimation(
                    1.3f, 1f, 1.3f, 1f
            );
            grow.setDuration(200);
            grow.setInterpolator(new BounceInterpolator());
            view.startAnimation(grow);
        }

    }

    private void voiceAnimation(View view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1f, 0f, 1f, 1f
        );
        scaleAnimation.setDuration(1500);
        scaleAnimation.setInterpolator(new LinearInterpolator());
        view.startAnimation(scaleAnimation);
    }

    private void mInitVoiceListener() {
//        mRecognitionListener = null;
        mRecognitionListener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d(TAG, "onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float v) {
//                Log.d(TAG, "onRmsChanged");
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.d(TAG, "onBufferReceived");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech");

            }

            @Override
            public void onError(int i) {
                Log.d(TAG, "onError: " + Integer.toString(i));
                mSpeechRecognizer.destroy();
                if (mSpeechRecognition) {
                    mListen();
                }
            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d(TAG, "onResults");
                mSendBtn.performClick();
                mEdtxt_message.setText("");
                mSpeechRecognizer.destroy();
                String result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
                if (result.contains("모두 삭제")) {
                    mDatabaseRef.child("messages").setValue(null);
                    mBody.removeAllViews();
                }
                if (result.contains("현재 시각")) {
                    final Calendar c = Calendar.getInstance();
                    Integer mHour = c.get(Calendar.HOUR_OF_DAY);
                    Integer mMinute = c.get(Calendar.MINUTE);
                    String myTime = "지금시각은 " + String.valueOf(mHour) + " 시 "
                            + String.valueOf(mMinute) + " 분입니다. ";
                    mTTS.speak(myTime, TextToSpeech.QUEUE_FLUSH, null);
                }
                SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);
                Map<String, ?> keys = sharedPreferences.getAll();
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                    Log.d(TAG,entry.getValue().toString().split(",")[1]);
                    if (result.contains(entry.getValue().toString().split(",")[0])){
                        String action =  entry.getValue().toString().split(",")[1];
                        if (action.equals("0")){
                            Intent i = new Intent();
                            i.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                            startActivity(i);
                            break;
                        }
                        if (action.equals("1")){
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_DIAL);
                            startActivity(i);
                            break;
                        }
                        if (action.equals("2")){
                            String query = result.replace("검색","");
                            Uri uri = Uri.parse("https://www.google.com/search?q="+query);
                            Intent gSearchIntent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(gSearchIntent);
                        }
                        if (action.equals("3")){
                            double latitude = 37.229277;
                            double longitude = 127.085270;
                            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            startActivity(intent);
                        }
                    }
                }
                //반복을 위하여
                if (mSpeechRecognition) {
                    mListen();
                }
//                if (result.contains("리모컨")){
//                    ConsumerIrManager consumerIrManager;
//                    consumerIrManager = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
//                    if (consumerIrManager == null){
//                        Log.d(TAG,"consumerIrManager null");
////                        return;
//                    }
//                    if (!consumerIrManager.hasIrEmitter()){
//                        Log.d(TAG,"IrEmitter null");
////                        return;
//                    }
//                    Log.d(TAG,"IR available");
//                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {
                Log.d(TAG, "onPartialResults");
                ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String text = "";
                for (String result : results) {
                    text += result;
                    Log.d(TAG, result);
                }
                mEdtxt_message.setText(text);
                int pos = mEdtxt_message.getText().length();
                mEdtxt_message.setSelection(pos);
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        };
    }

    private void mListen() {
//        AudioManager am=(AudioManager)getBaseContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
//        am.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(20000));
//                intent.putExtra("android.speech.extra.DICTATION_MODE", true);
//                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말해보세요");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
//                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
        mSpeechRecognizer.startListening(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            doPermAudio();
        }


        mBody = findViewById(R.id.body);
        mEdtxt_message = findViewById(R.id.message_edit);
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSendBtn = findViewById(R.id.sendBtn);
        mBtnVoice = findViewById(R.id.btnVoice);
        mViewVoice = findViewById(R.id.progress_voice);
        mAuth = FirebaseAuth.getInstance();
        mEmail = mAuth.getCurrentUser().getEmail();

        mInitVoiceListener();

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeNewText(mEmail, mEdtxt_message.getText().toString());
                if (mEdtxt_message.getText().toString().contains("모두 삭제")) {
                    mDatabaseRef.child("messages").setValue(null);
                    mBody.removeAllViews();
                }
                mEdtxt_message.setText("");
            }
        });

        mBtnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListen();
//                try {
//                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
//                } catch (ActivityNotFoundException a) {
//                    Toast.makeText(getApplicationContext(), "some problem here...", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != ERROR) {
                    // 언어를 선택한다.
                    mTTS.setLanguage(Locale.KOREAN);
                }
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference();
        database.getReference("/messages").

                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String username = dataSnapshot.child("username").getValue().toString();
                        final String message = dataSnapshot.child("text").getValue().toString();
                        if (username.equals(mAuth.getCurrentUser().getEmail())) {
                            LinearLayout view = (LinearLayout) inflater.inflate(R.layout.message_box_right, null);
                            ((TextView) view.getChildAt(0)).setText(username);
                            ((TextView) view.getChildAt(1)).setText(message);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.END;
                            mBody.addView(view, layoutParams);
                            view.requestFocus();
                            mEdtxt_message.requestFocus();
                            textAnimation(view, message);

//                            final Calendar c = Calendar.getInstance();
//                            Integer mHour = c.get(Calendar.HOUR_OF_DAY);
//                            Integer mMinute = c.get(Calendar.MINUTE);
//                            String time = String.valueOf(mHour) + "시 "
//                                    + String.valueOf(mMinute) + "분";
//                            ((TextView) view.getChildAt(0)).setText(time);

                            view.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                                    return false;
                                    //when false, only touch down event is detected
                                }

                            });
                        } else {
                            LinearLayout view = (LinearLayout) inflater.inflate(R.layout.message_box_left, null);
                            ((TextView) view.getChildAt(0)).setText(username);
                            ((TextView) view.getChildAt(1)).setText(message);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.START;
                            mBody.addView(view, layoutParams);
                            textAnimation(view, message);

                            view.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                                    return false;
                                }

                            });
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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


//        handler.removeCallbacks(runnable);
//        remove when you want

    }

    @Override
    protected void onStart() {
        super.onStart();
//        mHandler = new Handler();
//        mRunnable = new Runnable() {
//            @Override
//            public void run() {
//                writeNewText(mAuth.getCurrentUser().getEmail(),"경험치");
//                mHandler.postDelayed(this,2000);
//            }
//        };
//        mHandler.post(mRunnable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "message activity result");
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                //음성인식 결과로 행동을 취함
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    writeNewText(mEmail, result.get(0));
                    if (result.get(0).contains("뒤로 가기") && !result.get(0).contains("하지마")) {
                        finish();
                    }
                    if (result.get(0).equals("모두 삭제")) {
                        mDatabaseRef.child("messages").setValue(null);
                        mBody.removeAllViews();
                    }
                    if (result.get(0).contains("빨리") && result.get(0).contains("말") && !result.get(0).contains("하지마")) {
                        mTTS.setSpeechRate(3.5f);
                    }
                    if (result.get(0).contains("천천히") && result.get(0).contains("말") && !result.get(0).contains("하지마")) {
                        mTTS.setSpeechRate(1f);
                    }

                } else
                    Log.d(TAG, "voice recognition failed");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
            mTTS = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if ()
//        mHandler.removeCallbacks(mRunnable);
    }
}

