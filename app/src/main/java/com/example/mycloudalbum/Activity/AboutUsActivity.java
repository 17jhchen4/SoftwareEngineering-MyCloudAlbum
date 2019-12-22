package com.example.mycloudalbum.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.TextView;

import com.example.mycloudalbum.R;

import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {
    Context context=this;
    private String data[] = {"林金盛 17jslin4@stu.edu.cn",
            "唐正尧 17zytang@stu.edu.cn",
            "董毅豪 17yhdong@stu.edu.cn",
            "陈进辉 17jhchen@stu.edu.cn"};//假数据
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutus_layout);

        ImageButton back_btn = (ImageButton)findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new back_btn_onclick());

        TextView appName = findViewById(R.id.appName_tv);
        appName.setText(getAppName(context));

        TextView version = findViewById(R.id.version_tv);
        String str = "Version " + getVersionName(context)+getVersionCode(context);
        version.setText(str);

        //ArraryAdapter适配器，通过泛型来指定要适配的数据类型，然后在构造函数中把要适配的数据传入。
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,data));
    }

    class back_btn_onclick implements View.OnClickListener {
        public void onClick(View v) {
            finish();
        }
    }
    /**
     * 获取版本名称
     *
     * @param context 上下文
     *
     * @return 版本名称
     */
    public static String getVersionName(Context context) {

        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //返回版本号
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * 获取版本号
     *
     * @param context 上下文
     *
     * @return 版本号
     */
    public static int getVersionCode(Context context) {

        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //返回版本号
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;

    }

    /**
     * 获取App的名称
     *
     * @param context 上下文
     *
     * @return 名称
     */
    public static String getAppName(Context context) {
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //获取应用 信息
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            //获取albelRes
            int labelRes = applicationInfo.labelRes;
            //返回App的名称
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
