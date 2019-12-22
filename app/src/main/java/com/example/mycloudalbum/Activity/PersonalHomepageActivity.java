package com.example.mycloudalbum.Activity;
/*
 *修改
 */
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mycloudalbum.R;

import androidx.appcompat.app.AppCompatActivity;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.List;

public class PersonalHomepageActivity extends AppCompatActivity {
    private static final String TAG = "litePalData" ;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_homepage_layout);
        LitePal.initialize(this);

        Intent it = getIntent();
        userID = it.getStringExtra("userID");

        ImageButton personal_homepage_btn = (ImageButton) findViewById(R.id.personal_headshot);
        personal_homepage_btn.setOnClickListener(new personal_headshot_onclick());

        /*
         *增加用户名和个性签名的textview
         */
        TextView userName = (TextView) findViewById(R.id.userName);

        TextView personalized_signature = (TextView) findViewById(R.id.personalized_signature);

        Button setting_btn = (Button) findViewById(R.id.setting_btn);
        setting_btn.setOnClickListener(new setting_onclick());

        Button personal_information_btn = (Button) findViewById(R.id.personal_information_btn);
        personal_information_btn.setOnClickListener(new personal_information_onclick());

        Button aboutUs_btn = (Button) findViewById(R.id.aboutUs_btn);
        aboutUs_btn.setOnClickListener(new aboutUs_onclick());

        /*
         *展示信息
         */
        //显示信息
        List<LitePalActivity> litePalData = DataSupport.findAll(LitePalActivity.class);
        for(LitePalActivity litePal:litePalData){
            Log.d(TAG,"userID is " + litePal.userID);
            Log.d(TAG,"userName is " + litePal.userName);
            Log.d(TAG,"name is " + litePal.name);
            Log.d(TAG,"sex is " + litePal.sex);
            Log.d(TAG,"personalized_signature is " + litePal.personalized_signature);
            Log.d(TAG,"phoneNumber is " + litePal.phoneNumber);
        }

//        List<LitePalActivity> data = DataSupport.where("userID=?",userID).find(LitePalActivity.class);
//        for (LitePalActivity data0:data) {//实际只有一条
//            userID = data0.userID.toString();//使用上次的userID
//            Log.d(TAG, "data userID is " + data0.userID);
//            Log.d(TAG, "data userName is " + data0.userName);
//            Log.d(TAG, "data name is " + data0.name);
//            Log.d(TAG, "data sex is " + data0.sex);
//            Log.d(TAG, "data personalized_signature is " + data0.personalized_signature);
//            Log.d(TAG, "data phoneNumber is " + data0.phoneNumber);
//            userName.setText(data0.userName.toString());
//            personalized_signature.setText(data0.personalized_signature.toString());
//        }

    }

    class personal_headshot_onclick implements View.OnClickListener {
        public void onClick(View v) {
            // TODO 调用真机接口
        }
    }

    class setting_onclick implements View.OnClickListener{
        public void onClick(View v){
            Intent intent;
            intent = new Intent(PersonalHomepageActivity.this, SettingActivity.class);
            intent.putExtra("userID",userID);
            startActivity(intent);
        }
    }

    class personal_information_onclick implements View.OnClickListener{
        public void onClick(View v){
            Intent intent;
            intent = new Intent(PersonalHomepageActivity.this, PersonalInformationActivity.class);
            intent.putExtra("userID",userID);
            startActivity(intent);
        }
    }

    class aboutUs_onclick implements View.OnClickListener{
        public void onClick(View v){
            Intent intent;
            intent = new Intent(PersonalHomepageActivity.this, AboutUsActivity.class);
            startActivity(intent);
        }
    }
}