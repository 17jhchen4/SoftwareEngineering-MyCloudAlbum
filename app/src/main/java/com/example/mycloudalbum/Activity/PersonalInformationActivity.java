package com.example.mycloudalbum.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycloudalbum.R;

import androidx.appcompat.app.AppCompatActivity;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.List;


public class PersonalInformationActivity extends AppCompatActivity {
    Context context = PersonalInformationActivity.this;
    final String TAG="litePalData";
    String userName =null;
    String sex = "man";
    String name = null;
    String personalized_signature;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_information_layout);
        LitePal.initialize(this);

        TextView username_tv = (TextView)findViewById(R.id.userName_ev);
        EditText name_et = findViewById(R.id.name_ev);
        EditText personalized_signature_et = (EditText) findViewById(R.id.personalized_signature_ev);

        Intent it = getIntent();
        String userID = it.getStringExtra("userID");

        ImageButton back_btn = (ImageButton)findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new back_btn_onclick());

        Button save_btn = (Button)findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new save_btn_onclick());

        ImageButton headshot_tv_btn=(ImageButton)findViewById(R.id.personal_headshot_ibtn);
        headshot_tv_btn.setOnClickListener(new headshot_tv_btn_onclick());

//        RadioGroup sex_group = (RadioGroup) findViewById(R.id.radioGroup);
//        sex_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
//            @Override
//            public void onCheckedChanged(android.widget.RadioGroup group, int checkedId) {
//                RadioButton radioButton = group.findViewById(checkedId);
//                if(radioButton.getText().equals("男")){
//                    sex = "man";
//                }
//                else
//                    sex = "woman";
//                Toast.makeText(PersonalInformationActivity.this, radioButton.getText(), Toast.LENGTH_SHORT).show();
//            }
//        });

        List<LitePalActivity> data = DataSupport.where("userID=?", userID).find(LitePalActivity.class);
        for (LitePalActivity data0:data) {//实际只有一条
            Log.d(TAG, "data userID is " + data0.userID);
            Log.d(TAG, "data userName is " + data0.userName);
            Log.d(TAG, "data name is " + data0.name);
            Log.d(TAG, "data sex is " + data0.sex);
            Log.d(TAG, "data personalized_signature is " + data0.personalized_signature);
            Log.d(TAG, "data phoneNumber is " + data0.phoneNumber);
            username_tv.setText(data0.userName);
            name_et.setText(data0.name);
            /*
             *选择框不知怎么展示
             */
            personalized_signature_et.setText(data0.personalized_signature);
        }
    }

    class back_btn_onclick implements View.OnClickListener{
        public void onClick(View v){
//            Intent intent;
//            intent = new Intent(PersonalInformationActivity.this, PersonalHomepageActivity.class);
//            startActivity(intent);
            finish();
        }
    }

    class save_btn_onclick implements View.OnClickListener{
        public void onClick(View v){
            Intent it = getIntent();
            String userID = it.getStringExtra("userID");
            EditText userName_et = findViewById(R.id.userName_ev);
            EditText name_et = findViewById(R.id.name_ev);
            EditText personalized_signature_et = (EditText) findViewById(R.id.personalized_signature_ev);
            userName = userName_et.getText().toString();
            name = name_et.getText().toString();
            personalized_signature = personalized_signature_et.getText().toString();
            List<LitePalActivity> data = DataSupport.where("userID=?", userID).find(LitePalActivity.class);
            for (LitePalActivity data0:data) {//实际只有一条
                data0.setUserName(userName);
                data0.setName(name);
                if(sex.equals("man")){
                    data0.setSex(1);
                }
                else{
                    data0.setSex(0);
                }
                data0.setPersonalized_signature(personalized_signature);
                data0.save();
            }
//            //TODO 为了让更新信息后及时显示，使用关掉activity重新开启的方式
//            Intent intent;
//            intent = new Intent(PersonalInformationActivity.this, MainActivity.class);
//            intent.putExtra("userID",userID);
//            startActivity(intent);
            finish();
        }
    }

    class headshot_tv_btn_onclick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // TODO 调用真机接口
        }
    }
}
