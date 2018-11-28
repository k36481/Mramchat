package com.satirev.kim.mramchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;

public class DrawingActivity extends AppCompatActivity {

    private final Integer SPINNER_THEME  = android.R.layout.simple_list_item_1;
    private final String TAG = "LOG" + this.getClass().getSimpleName();
    private ArrayMap<String, Integer> mMacroMap;
    private LinearLayout mContainer;
    private Button mBtnAdd;
    private Button mBtnClear;
    private Button mBtnComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        mContainer = findViewById(R.id.drawing_container);
        mBtnAdd = findViewById(R.id.btn_add_macro);
        mBtnClear = findViewById(R.id.btn_clear_macro);
        mBtnComplete = findViewById(R.id.btn_complete_macro);

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.macro, null);
                ((TextView) linearLayout.getChildAt(0)).setText(Integer.toString(mContainer.getChildCount() + 1) + ". 만약");
                mContainer.addView(linearLayout);

                List<String> categories = new ArrayList<String>();
                categories.add("비디오 촬영");
                categories.add("전화 걸기");
                categories.add("검색");
                categories.add("지도");


                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(),SPINNER_THEME , categories);
                final Spinner spinner = (Spinner) ((LinearLayout) linearLayout.getChildAt(2)).getChildAt(1);
                spinner.setAdapter(dataAdapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        Log.d(TAG, adapterView.getItemAtPosition(i).toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

            }
        });

        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContainer.removeAllViews();
                SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
            }
        });

        mBtnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer macro_count = mContainer.getChildCount();
                SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();

                for (int i = 0; i < macro_count; i++) {
                    LinearLayout current_linear = (LinearLayout) mContainer.getChildAt(i);
                    EditText editText = (EditText) current_linear.getChildAt(1);
                    Spinner spinner = (Spinner) ((LinearLayout) current_linear.getChildAt(2)).getChildAt(1);
                    editor.putString("macro" + Integer.toString(i), editText.getText().toString() + "," +spinner.getSelectedItemPosition());
                }
                editor.apply();

                Map<String, ?> keys = sharedPreferences.getAll();
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                    Log.d(TAG, entry.getKey() + ": " +
                            entry.getValue().toString());
                }
                Intent intent = new Intent(DrawingActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);

        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.macro, null);
            ((TextView) linearLayout.getChildAt(0)).setText(Integer.toString(mContainer.getChildCount() + 1) + ". 만약");
            mContainer.addView(linearLayout);
            List<String> categories = new ArrayList<String>();
            categories.add("비디오 촬영");
            categories.add("전화 걸기");
            categories.add("검색");
            categories.add("지도");


            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), SPINNER_THEME, categories);
            final Spinner spinner = (Spinner) ((LinearLayout) linearLayout.getChildAt(2)).getChildAt(1);
            spinner.setAdapter(dataAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, adapterView.getItemAtPosition(i).toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            ((EditText) linearLayout.getChildAt(1)).setText(entry.getValue().toString().split(",")[0]);
            spinner.setSelection(Integer.parseInt(entry.getValue().toString().split(",")[1]));
            Log.d(TAG,entry.getValue().toString());

        }

    }
}
