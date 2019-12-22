package com.example.mycloudalbum.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mycloudalbum.Adapter.FragmentAdapter;
import com.example.mycloudalbum.Bean.AlbumBean;
import com.example.mycloudalbum.Bean.DeleteBean;
import com.example.mycloudalbum.Bean.FinalDeleteBean;
import com.example.mycloudalbum.Bean.RecoverBean;
import com.example.mycloudalbum.Bean.RecycleBinBean;
import com.example.mycloudalbum.Fragment.AlbumFragment;
import com.example.mycloudalbum.Fragment.RecycleFragment;
import com.example.mycloudalbum.ImageLoader;
import com.example.mycloudalbum.Manager.UpdateManager;
import com.example.mycloudalbum.Manager.UploadManager;
import com.example.mycloudalbum.R;
import com.example.mycloudalbum.Util.OkhttpUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
{
    private boolean drawer_flag = false;

    private static final String TAG = "litePalData" ;

    private DrawerLayout drawerLayout;//滑动菜单
    private ImageView bingImage;//必应每日一图
    private Toolbar toolbar;//标题栏
    private NavigationView navigationView;//抽屉菜单
    private DrawerLayout.SimpleDrawerListener simpleDrawerListener;//抽屉菜单监听器
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private int mViewPagerPosition;

    private UploadManager uploadManager;
    private UpdateManager updateManager;
//    private LocalCacheUtil localCacheUtil;
    private ImageLoader imageLoader;

    private FloatingActionsMenu fb_main;
    private FloatingActionButton fb_1,fb_2,fb_3,fb_4;//改良浮动按钮

    private long firstTime = 0;

    private Context context;

    private AlbumBean mAlbumlist = null;
    private RecycleBinBean mRecycleList = null;
    private List<String> titles;

    private String getBingImgUrl = "http://guolin.tech/api/bing_pic";//获取必应每日一图的url
    private String bingImgUrl;//必应每日一图的url

    private String userID = "12345";
    /**
     * 刷新云相册
     */
    private String updateUrl;
    /**
     * 刷新回收站
     */
    private String updateUrlForRecycle;
    /**
     * 删除云相册图片并放入回收站
     */
    private String imgDelete;
    /**
     * 恢复回收站图片并放入云相册
     */
    private String imgRecovery ;
    /**
     * 彻底删除回收站中的图片
     */
    private String imgFinalDelete;


    private boolean optionMenuOn = false;//标示是否要显示optionmenu
    private Menu mMenu;//获取optionmenu

    private mHandler mhandler;

    private Intent intent;
    /*
     *增加intent跳转
     */

    private List<LitePalActivity> data;
    private ImageView headshot;
    private TextView userName;
    private TextView personalized_signature;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_activity_main);
        QMUISwipeBackActivityManager.init(this.getApplication());
        LayoutInflater inflate = LayoutInflater.from(this);
        final View view = inflate.inflate(R.layout.module_navigation_header,null);
        Init();//变量初始化
        updateUrl = "http://203.195.217.253/cloudimg.php?userID="+userID;
        updateUrlForRecycle = "http://203.195.217.253/recycle_bin.php?userID="+userID;
        imgDelete = "http://203.195.217.253/delete.php?userID="+userID;
        imgRecovery = "http://203.195.217.253/recovery.php?userID="+userID;
        imgFinalDelete = "http://203.195.217.253/final_Delete.php?userID="+userID;

        data = DataSupport.where("userID=?",userID).find(LitePalActivity.class);
        headshot = navigationView.getHeaderView(0).findViewById(R.id.navigation_iv);
        userName = navigationView.getHeaderView(0).findViewById(R.id.navigation_name_tv);
        personalized_signature = navigationView.getHeaderView(0).findViewById(R.id.navigation_motto_tv);
        /*
         *展示信息
         */

        for (LitePalActivity data0:data) {//实际只有一条
//            Log.d(TAG, "data userID is " + data0.userID);
//            Log.d(TAG, "data userName is " + data0.userName);
//            Log.d(TAG, "data name is " + data0.name);
//            Log.d(TAG, "data sex is " + data0.sex);
//            Log.d(TAG, "data personalized_signature is " + data0.personalized_signature);
//            Log.d(TAG, "data phoneNumber is " + data0.phoneNumber);
            userName.setText(data0.userName);
            personalized_signature.setText(data0.personalized_signature);
        }

        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if(ab != null)
        {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);//设置导航图标
            ab.setDisplayHomeAsUpEnabled(true);//显示导航按钮
        }

        initViewPager();

        fabListener();//浮动按钮监听

        // bingImage每日一图
        getBingImg(getBingImgUrl);
        bingImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(context);
                messageDialogBuilder.setMessage("如果喜欢这张图片的话还可以下载到本地相册哦~")
                        .setTitle("（悄咪咪地问）")
                        .addAction("好啊", new QMUIDialogAction.ActionListener()
                        {
                            @Override
                            public void onClick(QMUIDialog dialog, int index)
                            {
                                dialog.dismiss();
                                if (bingImgUrl == null)
                                {
                                    bingImgUrl = imageLoader.getUrlFromLocal();
                                }
                                downLoadImg(bingImgUrl);
                            }
                        })
                        .addAction("不用了", new QMUIDialogAction.ActionListener()
                        {
                            @Override
                            public void onClick(QMUIDialog dialog, int index)
                            {
                                dialog.dismiss();
                            }
                        });
                QMUIDialog qmuiDialog = messageDialogBuilder.create();
                qmuiDialog.show();
            }
        });


        //监听抽屉菜单中的按钮事件
        if(navigationView != null)
        {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
            {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
                {
                    menuItem.setChecked(true);
                    Toast.makeText(MainActivity.this, menuItem.getTitle().toString(), Toast.LENGTH_SHORT).show();

                    /*
                     *intent跳转有修改
                     */
                    //TODO 实现抽屉菜单的点击事件by金盛
                    switch (menuItem.getItemId())
                    {
                        case R.id.nav_mine:
                            //个人信息
                            intent= new Intent(MainActivity.this,PersonalInformationActivity.class);
                            intent.putExtra("userID",userID);
                            startActivity(intent);
                            break;

                        case R.id.nav_seting:
                            //设置
                            intent= new Intent(MainActivity.this,SettingActivity.class);
                            intent.putExtra("userID",userID);
                            startActivity(intent);
                            break;

                        case R.id.nav_feedback:
                            //问题反馈
                            intent= new Intent(MainActivity.this,SuggestionActivity.class);
                            intent.putExtra("userID",userID);
                            startActivity(intent);
                            break;

                        case R.id.nav_aboutUs:
                            //关于我们
                            intent= new Intent(MainActivity.this,AboutUsActivity.class);
                            startActivity(intent);
                            break;

                        default:
                            break;
                    }

                    drawerLayout.closeDrawers();
                    return true;
                }
            });
        }

        //设置抽屉菜单列表项点击不变色
        Resources resource=(Resources)getBaseContext().getResources();
        @SuppressLint("ResourceType") ColorStateList csl=(ColorStateList)resource.getColorStateList(R.drawable.navigation_item_selector);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(null);

        simpleDrawerListener = new DrawerLayout.SimpleDrawerListener()
        {
            @Override
            public void onDrawerOpened(View drawerView)
            {
                drawer_flag = true;

                data = DataSupport.where("userID=?",userID).find(LitePalActivity.class);
                headshot = navigationView.getHeaderView(0).findViewById(R.id.navigation_iv);
                userName = navigationView.getHeaderView(0).findViewById(R.id.navigation_name_tv);
                personalized_signature = navigationView.getHeaderView(0).findViewById(R.id.navigation_motto_tv);
                /*
                 *展示信息
                 */

                for (LitePalActivity data0:data) {//实际只有一条
//            Log.d(TAG, "data userID is " + data0.userID);
//            Log.d(TAG, "data userName is " + data0.userName);
//            Log.d(TAG, "data name is " + data0.name);
//            Log.d(TAG, "data sex is " + data0.sex);
//            Log.d(TAG, "data personalized_signature is " + data0.personalized_signature);
//            Log.d(TAG, "data phoneNumber is " + data0.phoneNumber);
                    userName.setText(data0.userName);
                    personalized_signature.setText(data0.personalized_signature);
                }

//                drawerView.setClickable(true);//解决点击空白处的穿透事件
                if (updateManager.getMULTI_SELECT_MODULE())
                {
                    if (mViewPagerPosition == 0)
                    {
                        exitMultiDelete();
                    }
                    else
                    {
                        exitMultiDeleteForRecycle();
                    }
                }
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView)
            {
                drawer_flag = false;
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(simpleDrawerListener);//绑定监听器
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        drawerLayout.removeDrawerListener(simpleDrawerListener);//取消监听器
    }

    private void getBingImg(String BingImgUrl)
    {
        //先从本地读取，在进行网络请求
        Bitmap bitmap = imageLoader.getBitmapFromLocal("bing");
        if (bitmap != null)
        {
            bingImage.setImageBitmap(bitmap);
        }
        OkhttpUtil.sengOkhttpGetRequest(BingImgUrl, new okhttp3.Callback()
        {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                .setTipWord("获取每日一图失败!").create();
                        errorTipDialog.show();
                        bingImage.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                errorTipDialog.dismiss();
                            }
                        }, 1000);
                    }
                });
                //Snackbar.make(bingImage,"获取每日一图失败!请检查当前网络状态后重试",Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
            {
                String imgUrl = response.body().string();
                bingImgUrl = imgUrl;
                if (!imgUrl.equals(imageLoader.getUrlFromLocal()))
                {
                    imageLoader.setUrlToLocal(imgUrl);
                    Glide.with(getApplication()).asBitmap().load(imgUrl).into(new CustomTarget<Bitmap>()
                    {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition)
                        {
                            bingImage.setImageBitmap(resource);
                            imageLoader.setBitmapToLocal("bing", resource);
                            imageLoader.addBitmapToMemoryCache("bing", resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder)
                        {

                        }
                    });
                }
            }
        });

    }

    /**
     * 下载图片到本地相册
     * @param imgUrl 需要下载的图片url
     */
    private void downLoadImg(final String imgUrl)
    {
        //网络请求,将对应图片从其url地址中下载到本地相册中
        final QMUITipDialog waitTipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在保存中").create();
        waitTipDialog.show();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Bitmap mBitmap = null;
                    if (!TextUtils.isEmpty(imgUrl))
                    { //网络图片
                        // 对资源链接
                        URL url = new URL(imgUrl);
                        //打开输入流
                        InputStream inputStream = url.openStream();
                        //对网上资源进行下载转换位图图片
                        mBitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                    }
                    saveFile(mBitmap);
                    imageLoader.addBitmapToMemoryCache(imgUrl, mBitmap);
                    //成功
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            waitTipDialog.dismiss();
                            final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                    .setTipWord("图片保存成功！").create();
                            qmuiTipDialog.show();
                            bingImage.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    qmuiTipDialog.dismiss();
                                }
                            }, 1500);
                        }
                    });
                    //Snackbar.make(bingImage,"图片保存成功！",Snackbar.LENGTH_SHORT).show();
                }
                catch (IOException e)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("图片保存失败！").create();
                            errorTipDialog.show();
                            bingImage.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    errorTipDialog.dismiss();
                                }
                            }, 1500);
                        }
                    });
                    //Snackbar.make(bingImage,"图片保存失败！",Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                    Log.e("error",e.toString());
                }
                catch (Exception e)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("图片保存失败！").create();
                            errorTipDialog.show();
                            bingImage.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    errorTipDialog.dismiss();
                                }
                            }, 1500);
                        }
                    });
                    //Snackbar.make(bingImage,"图片保存失败！请检查网络后重试",Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                    Log.e("error",e.toString());
                }
                finally
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            waitTipDialog.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 保存图片
     * @param bm 需要保存的bitmap
     * @throws IOException
     */
    private void saveFile(Bitmap bm) throws IOException
    {
        File dirFile = new File(Environment.getExternalStorageDirectory().getPath());
        if (!dirFile.exists())
        {
            dirFile.mkdir();
        }
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File myCaptureFile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        //把图片保存后声明这个广播事件通知系统相册有新图片到来
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(myCaptureFile);
        intent.setData(uri);
        this.sendBroadcast(intent);
    }

    /**
     *
     * @param keyCode
     * @param event
     * @return true
     *
     * 重写onKeydown配合drawer_flag实现按返回键关闭滑动菜单
     * 重写onKeydown实现按返回键关闭浮动菜单
     * 重写onKeydown实现双击返回键退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (drawer_flag)
        {
            drawerLayout.closeDrawers();
            return true;
        }

        if (fb_main!=null && fb_main.isExpanded())
        {
            fb_main.collapse();
            return true;
        }

        //实现按返回键退出多选模式by进辉
        if (updateManager.getMULTI_SELECT_MODULE())
        {
            if (mViewPagerPosition == 0)
            {
                exitMultiDelete();
            }
            else
            {
                exitMultiDeleteForRecycle();
            }
            return true;
        }

        if(keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN)
        {
            if (System.currentTimeMillis()-firstTime>2000)
            {
                Toast.makeText(MainActivity.this,"再按一次就要退出程序啦!",Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
            }
            else
            {
                finish();
                System.exit(0);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 进行变量初始化操作
     */
    private void Init()
    {
        drawerLayout = findViewById(R.id.drawer_layout);
        bingImage = findViewById(R.id.iv_bing);
        fb_main = findViewById(R.id.fab_menu);
        fb_1 = findViewById(R.id.fab_1);
        fb_2 = findViewById(R.id.fab_2);
        fb_3 = findViewById(R.id.fab_3);
//        fb_4 = findViewById(R.id.fab_4);

        toolbar = findViewById(R.id.tool_bar);
        navigationView = findViewById(R.id.navigation_view);
        context = this;

        mhandler = new mHandler();

        //获取userID
        Intent it = getIntent();
        if (it != null)
        {
            userID = it.getStringExtra("userID");
        }

        uploadManager = new UploadManager(context, bingImage, mhandler);
        updateManager = UpdateManager.getInstance();
        updateManager.setmContext(this);
        updateManager.setmUserID(userID);
        uploadManager.setmUserID(userID);
        imageLoader = ImageLoader.getInstance();
        imageLoader.setContext(this);
//        localCacheUtil = new LocalCacheUtil(this,"myCloudAlbum");


        viewPager = findViewById(R.id.view_pager);
        /**
         * 用于监听当前ViewPager的位置
         */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                if (fb_main!=null && fb_main.isExpanded())
                {
                    fb_main.collapse();
                }
            }

            @Override
            public void onPageSelected(int position)
            {
                mViewPagerPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
                if (updateManager.getMULTI_SELECT_MODULE())
                {
                    if (mViewPagerPosition == 0)
                    {
                        exitMultiDelete();
                    }
                    else
                    {
                        exitMultiDeleteForRecycle();
                    }
                }
            }
        });
    }

    private class mHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 1)
            {
                UpdateData();
            }

        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        mMenu = menu;
        checkOptionMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.multi_delete_bar, menu);//加载menu文件到布局
        return true;
    }

    private void checkOptionMenu()
    {
        if(null != mMenu)
        {
            if (mViewPagerPosition == 0)
            {
                if(optionMenuOn)
                {
                    MenuItem menuItem =  mMenu.findItem(R.id.multi_recover_select);
                    menuItem.setIcon(R.drawable.download);
                    menuItem.setTitle("下载");
                    for (int i = 0; i < mMenu.size(); i++)
                    {
                        mMenu.getItem(i).setVisible(true);
                        mMenu.getItem(i).setEnabled(true);
                    }
                }
                else
                {
                    for (int i = 0; i < mMenu.size(); i++)
                    {
                        mMenu.getItem(i).setVisible(false);
                        mMenu.getItem(i).setEnabled(false);
                    }
                }
            }
            else if (mViewPagerPosition == 1)
            {
                if(optionMenuOn)
                {
                    MenuItem menuItem =  mMenu.findItem(R.id.multi_recover_select);
                    menuItem.setIcon(R.drawable.recover);
                    menuItem.setTitle("恢复");
                    for (int i = 0; i < mMenu.size(); i++)
                    {
                        mMenu.getItem(i).setVisible(true);
                        mMenu.getItem(i).setEnabled(true);
                    }
                }
                else
                {
                    for (int i = 0; i < mMenu.size(); i++)
                    {
                        mMenu.getItem(i).setVisible(false);
                        mMenu.getItem(i).setEnabled(false);
                    }
                }
            }

        }
    }

    /**
     *
     * @param item 导航栏中的列表项
     * @return 返回是否选中该项
     *
     * 该方法用于监听导航栏中的home按钮并对其重写成为打开抽屉菜单的按钮
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            //监听toolbar点击抽屉菜单选项
            case android.R.id.home:
                if(drawerLayout.isDrawerOpen(GravityCompat.START))
                {
                    drawerLayout.closeDrawers();
                }
                else
                {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;

            case R.id.multi_cancel_select:
                if (updateManager.getMULTI_SELECT_MODULE())
                {
                    if (mViewPagerPosition == 0)
                    {
                        exitMultiDelete();
                    }
                    else
                    {
                        exitMultiDeleteForRecycle();
                    }
                }
                return true;

            case R.id.multi_recover_select:
                if (updateManager.getSelectList().isEmpty())
                {
                    final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                            .setTipWord("啥也没选择!").create();
                    qmuiTipDialog.show();
                    bingImage.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            qmuiTipDialog.dismiss();
                        }
                    }, 500);

                    if (updateManager.getMULTI_SELECT_MODULE())
                    {
                        if (mViewPagerPosition == 0)
                        {
                            exitMultiDelete();
                        }
                        else
                        {
                            exitMultiDeleteForRecycle();
                        }
                    }
                }
                else
                {
                    /**
                     * 云相册多选下载
                     */
                    if (mViewPagerPosition == 0)
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(this);
                        messageDialogBuilder.setMessage("确定要下载选中图片到本地相册吗？")
                                .setTitle("（询问脸）")
                                .addAction("取消", new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index)
                                    {
                                        dialog.dismiss();
                                    }
                                })

                                .addAction("没错", new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int Index)
                                    {
                                        dialog.dismiss();

                                        final QMUITipDialog qmuiWaitDialog = new QMUITipDialog.Builder(context)
                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                                                .setTipWord("正在下载").create();
                                        qmuiWaitDialog.show();

                                        //多个图片的下载
                                        for (int i = 0; i < updateManager.getSelectList().size(); i++)
                                        {
                                            String downloadUrl = updateManager.getSelectList().get(i);
                                            Glide.with(getApplication()).asBitmap().load(downloadUrl).into(new CustomTarget<Bitmap>()
                                            {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition)
                                                {
                                                    try
                                                    {
                                                        saveFile(resource);
                                                    } catch (IOException e)
                                                    {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                @Override
                                                public void onLoadCleared(@Nullable Drawable placeholder)
                                                {

                                                }
                                            });
                                        }

                                        if (updateManager.getMULTI_SELECT_MODULE())
                                        {
                                            exitMultiDelete();
                                        }

                                        qmuiWaitDialog.dismiss();
                                        final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                                .setTipWord("下载成功！").create();
                                        qmuiTipDialog.show();
                                        bingImage.postDelayed(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                qmuiTipDialog.dismiss();
                                            }
                                        }, 500);
                                    }
                                });
                        QMUIDialog qmuiDialog = messageDialogBuilder.create();
                        qmuiDialog.show();
                    }
                    /**
                     * 回收站多选恢复
                     */
                    else
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(this);
                        messageDialogBuilder.setMessage("确定要恢复选中图片到云相册相册吗？")
                                .setTitle("（询问脸）")
                                .addAction("取消", new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index)
                                    {
                                        dialog.dismiss();
                                    }
                                })

                                .addAction("是的", new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int Index)
                                    {
                                        dialog.dismiss();

                                        final QMUITipDialog waitTipDialog = new QMUITipDialog.Builder(context)
                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                                                .setTipWord("正在恢复").create();
                                        waitTipDialog.show();

                                        //多个图片的恢复（网络请求）

                                        OkhttpUtil.handleSelectImgs(imgRecovery, updateManager.getSelectList(), new Callback()
                                        {
                                            @Override
                                            public void onFailure(@NotNull Call call, @NotNull IOException e)
                                            {
                                                waitTipDialog.dismiss();
//                                                Log.e("Upload", e.toString());
                                                runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                .setTipWord("恢复图片失败！请检查网络后重试").create();
                                                        errorTipDialog.show();
                                                        bingImage.postDelayed(new Runnable()
                                                        {
                                                            @Override
                                                            public void run()
                                                            {
                                                                errorTipDialog.dismiss();
                                                            }
                                                        }, 1500);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                                            {
                                                String result = response.body().string();
                                                Log.e("TestRecover", result);
                                                waitTipDialog.dismiss();

                                                Gson gson = new Gson();
                                                final RecoverBean recoverBean = gson.fromJson(result, RecoverBean.class);
                                                if (recoverBean.getResult() == -1)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                    .setTipWord("图片恢复失败！无法连接数据库").create();
                                                            errorTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1500);
                                                        }
                                                    });
                                                }
                                                else if (recoverBean.getResult() == 803)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            updateManager.recoverMultiImgForRecycle(recoverBean.getSuccessUrl());
                                                            if (updateManager.getMULTI_SELECT_MODULE())
                                                            {
                                                                exitMultiDeleteForRecycle();
                                                            }
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                                                                    .setTipWord("部分图片恢复失败！").create();
                                                            errorTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1000);
                                                        }
                                                    });
                                                }
                                                else if (recoverBean.getResult() == 802)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                    .setTipWord("图片恢复失败！").create();
                                                            errorTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1500);
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            updateManager.recoverMultiImgForRecycle(recoverBean.getSuccessUrl());
                                                            if (updateManager.getMULTI_SELECT_MODULE())
                                                            {
                                                                exitMultiDeleteForRecycle();
                                                            }
                                                            waitTipDialog.dismiss();
                                                            final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                                                    .setTipWord("恢复成功！").create();
                                                            qmuiTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    qmuiTipDialog.dismiss();
                                                                }
                                                            }, 500);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                        QMUIDialog qmuiDialog = messageDialogBuilder.create();
                        qmuiDialog.show();
                    }
                }
                return true;

            case R.id.multi_delete_select:

                if (updateManager.getSelectList().isEmpty())
                {
                    final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                            .setTipWord("啥也没删除!").create();
                    qmuiTipDialog.show();
                    bingImage.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            qmuiTipDialog.dismiss();
                        }
                    }, 500);

                    if (updateManager.getMULTI_SELECT_MODULE())
                    {
                        if (mViewPagerPosition == 0)
                        {
                            exitMultiDelete();
                        }
                        else
                        {
                            exitMultiDeleteForRecycle();
                        }
                    }
                    return true;
                }
                else
                {
                    if (mViewPagerPosition == 0)
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(this);
                        messageDialogBuilder.setMessage("确定要删除吗？")
                                .setTitle("（严肃脸）")
                                .addAction("取消", new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index)
                                    {
                                        dialog.dismiss();
                                    }
                                })

                                .addAction(0, "删除", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int Index)
                                    {
                                        dialog.dismiss();

                                        //云相册多选删除网络请求
                                        final QMUITipDialog waitTipDialog = new QMUITipDialog.Builder(context)
                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                                                .setTipWord("正在删除").create();
                                        waitTipDialog.show();

                                        OkhttpUtil.handleSelectImgs(imgDelete, updateManager.getSelectList(), new Callback()
                                        {
                                            @Override
                                            public void onFailure(@NotNull Call call, @NotNull IOException e)
                                            {
                                                waitTipDialog.dismiss();
//                                                Log.e("Upload", e.toString());
                                                runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                .setTipWord("删除图片失败！请检查网络后重试").create();
                                                        errorTipDialog.show();
                                                        bingImage.postDelayed(new Runnable()
                                                        {
                                                            @Override
                                                            public void run()
                                                            {
                                                                errorTipDialog.dismiss();
                                                            }
                                                        }, 1500);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                                            {
                                                String result = response.body().string();
//                                                Log.e("TestDelete", result);
                                                waitTipDialog.dismiss();

                                                Gson gson = new Gson();
                                                    final DeleteBean deleteBean = gson.fromJson(result, DeleteBean.class);
                                                if (deleteBean.getResult() == -1)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                    .setTipWord("图片删除失败！无法连接数据库").create();
                                                            errorTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1500);
                                                        }
                                                    });
                                                }
                                                else if (deleteBean.getResult() == 703)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            updateManager.deleteMultiImg(deleteBean.getSuccessUrl());
                                                            if (updateManager.getMULTI_SELECT_MODULE())
                                                            {
                                                                exitMultiDelete();
                                                            }
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                                                                    .setTipWord("部分图片删除失败！").create();
                                                            errorTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1000);
                                                        }
                                                    });
                                                }
                                                else if (deleteBean.getResult() == 702)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                    .setTipWord("图片删除失败！").create();
                                                            errorTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1500);
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            updateManager.deleteMultiImg(deleteBean.getSuccessUrl());
                                                            if (updateManager.getMULTI_SELECT_MODULE())
                                                            {
                                                                exitMultiDelete();
                                                            }
                                                            final QMUITipDialog successTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                                                    .setTipWord("删除成功！").create();
                                                            successTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    successTipDialog.dismiss();
                                                                }
                                                            }, 500);

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                        QMUIDialog qmuiDialog = messageDialogBuilder.create();
                        qmuiDialog.show();
                    }
                    else if (mViewPagerPosition == 1)
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(this);
                        messageDialogBuilder.setMessage("确定要彻底删除吗？")
                                .setTitle("（严肃脸）")
                                .addAction("取消", new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index)
                                    {
                                        dialog.dismiss();
                                    }
                                })

                                .addAction(0, "确定", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int Index)
                                    {
                                        dialog.dismiss();

                                        //回收站多选删除
                                        final QMUITipDialog waitTipDialog = new QMUITipDialog.Builder(context)
                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                                                .setTipWord("正在删除").create();
                                        waitTipDialog.show();
                                        OkhttpUtil.handleSelectImgs(imgFinalDelete, updateManager.getSelectList(), new Callback()
                                        {
                                            @Override
                                            public void onFailure(@NotNull Call call, @NotNull IOException e)
                                            {
                                                waitTipDialog.dismiss();
                                                runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                .setTipWord("删除图片失败！请检查网络后重试").create();
                                                        errorTipDialog.show();
                                                        bingImage.postDelayed(new Runnable()
                                                        {
                                                            @Override
                                                            public void run()
                                                            {
                                                                errorTipDialog.dismiss();
                                                            }
                                                        }, 1500);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                                            {
                                                String result = response.body().string();
                                                Log.e("TestFinalDelete", result);
                                                waitTipDialog.dismiss();

                                                Gson gson = new Gson();
                                                final FinalDeleteBean finalDeleteBean = gson.fromJson(result, FinalDeleteBean.class);
                                                if (finalDeleteBean.getResult() == -1)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                    .setTipWord("图片删除失败！无法连接数据库").create();
                                                            errorTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1500);
                                                        }
                                                    });
                                                }
                                                else if (finalDeleteBean.getResult() == 903)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            updateManager.deleteMultiImgForRecycle(finalDeleteBean.getSuccessUrl());
                                                            if (updateManager.getMULTI_SELECT_MODULE())
                                                            {
                                                                exitMultiDeleteForRecycle();
                                                            }
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                                                                    .setTipWord("部分图片删除失败！").create();
                                                            errorTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1000);
                                                        }
                                                    });
                                                }
                                                else if (finalDeleteBean.getResult() == 902)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                    .setTipWord("图片删除失败！").create();
                                                            errorTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1500);
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            updateManager.deleteMultiImgForRecycle(finalDeleteBean.getSuccessUrl());
                                                            if (updateManager.getMULTI_SELECT_MODULE())
                                                            {
                                                                exitMultiDeleteForRecycle();
                                                            }
                                                            final QMUITipDialog successTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                                                    .setTipWord("删除成功！").create();
                                                            successTipDialog.show();
                                                            bingImage.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    successTipDialog.dismiss();
                                                                }
                                                            }, 500);

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                        QMUIDialog qmuiDialog = messageDialogBuilder.create();
                        qmuiDialog.show();
                    }
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void exitMultiDelete()
    {
        updateManager.setMULTI_SELECT_MODULE(false);
        for (int i = 0; i < updateManager.getmPhotoWallAdapters().size(); i++)
        {
            updateManager.getmPhotoWallAdapters().get(i).notifyDataSetChanged();
        }
        optionMenuOn = false;
        checkOptionMenu();
    }

    private void exitMultiDeleteForRecycle()
    {
        updateManager.setMULTI_SELECT_MODULE(false);
        updateManager.getmRecycleWallAdapter().notifyDataSetChanged();
        optionMenuOn = false;
        checkOptionMenu();
    }

    /**
     * 监听浮动按钮
     */
    private void fabListener()
    {

        fb_main.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener()
        {

            @Override
            public void onMenuExpanded()
            {
                if (mViewPagerPosition == 0)
                {
                    if (updateManager.getMULTI_SELECT_MODULE())
                    {
                        exitMultiDelete();
                    }
                }
                else
                {
                    if (updateManager.getMULTI_SELECT_MODULE())
                    {
                        exitMultiDeleteForRecycle();
                    }
                }
            }

            @Override
            public void onMenuCollapsed()
            {

            }
        });

        /**
         * 图片上传（云相册）
         */
       fb_1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mViewPagerPosition == 0)
                {
                    uploadManager.uploadImg();
                }
                else if (mViewPagerPosition == 1)
                {
                    uploadManager.uploadImg();
                }
                fb_main.collapse();
            }
        });

        /**
         * 更新数据（云相册/回收站）
         */
        fb_2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UpdateData();
                fb_main.collapse();
            }
        });

        /**
         * 多选模式（云相册/回收站）
         */
        fb_3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mViewPagerPosition == 0)
                {
                    if (updateManager.getmPhotoWallAdapters() != null
                         && !updateManager.getmPhotoWallAdapters().isEmpty())
                    {
                        updateManager.setMULTI_SELECT_MODULE(true);
                        for (int i = 0; i < updateManager.getmPhotoWallAdapters().size(); i++)
                        {
                            updateManager.getmPhotoWallAdapters().get(i).notifyDataSetChanged();
                        }
                        optionMenuOn = true;
                        checkOptionMenu();
                    }
                }
                else if (mViewPagerPosition == 1)
                {
                    if (updateManager.getmRecycleWallAdapter() != null
                            &&!updateManager.getmRecycleWallAdapter().isEmpty())
                    {
                        updateManager.setMULTI_SELECT_MODULE(true);
                        updateManager.getmRecycleWallAdapter().notifyDataSetChanged();
                        optionMenuOn = true;
                        checkOptionMenu();
                    }
                }
                fb_main.collapse();
            }
        });
//
//        fb_4.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                Snackbar.make(fb_main,"fab4被点击啦",Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener()
//                {
//                    @Override
//                    public void onClick(View v)
//                    {
//                        Toast.makeText(MainActivity.this, "Okk~", Toast.LENGTH_SHORT).show();
//                    }
//                }).show();
//                fb_main.collapse();
//            }
//        });
    }

    private void UpdateData()
    {
        final QMUITipDialog qmuiWaitDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在更新数据").create();
        qmuiWaitDialog.show();

        if (mViewPagerPosition == 0)
        {
            OkhttpUtil.sengOkhttpGetRequest("http://203.195.217.253/cloudimg.php?userID="+userID, new Callback()
            {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e)
                {
                    qmuiWaitDialog.dismiss();
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final QMUITipDialog qmuiFailDialog = new QMUITipDialog.Builder(context)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("获取数据失败!请检测网络后重试").create();
                            qmuiFailDialog.show();
                            bingImage.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    qmuiFailDialog.dismiss();
                                }
                            }, 1500);
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                {
                    qmuiWaitDialog.dismiss();
                    String json = response.body().string();
                    Gson gson = new Gson();
                    final AlbumBean albumlist = gson.fromJson(json, AlbumBean.class);
                    int resultCode = albumlist.getResult_code();
                    if (resultCode == 501)
                    {
                        //云相册数据更新
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateManager.updateAlbum(albumlist);
                                final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                        .setTipWord("刷新成功！").create();
                                qmuiTipDialog.show();
                                bingImage.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        qmuiTipDialog.dismiss();
                                    }
                                }, 500);

                            }
                        });
                    }
                    else if (resultCode == -1)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final QMUITipDialog qmuiFailDialog = new QMUITipDialog.Builder(context)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                        .setTipWord("连接数据库失败!请稍后重试").create();
                                qmuiFailDialog.show();
                                bingImage.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        qmuiFailDialog.dismiss();
                                    }
                                }, 1500);
                            }
                        });
                    }
                    else
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final QMUITipDialog qmuiFailDialog = new QMUITipDialog.Builder(context)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                        .setTipWord("查询出错!请稍后重试").create();
                                qmuiFailDialog.show();
                                bingImage.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        qmuiFailDialog.dismiss();
                                    }
                                }, 1500);
                            }
                        });
                    }
                }
            });

        }
        else if (mViewPagerPosition == 1)
        {
            OkhttpUtil.sengOkhttpGetRequest("http://203.195.217.253/recycle_bin.php?userID="+userID, new Callback()
            {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e)
                {
                    qmuiWaitDialog.dismiss();
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final QMUITipDialog qmuiFailDialog = new QMUITipDialog.Builder(context)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("获取数据失败!请检测网络后重试").create();
                            qmuiFailDialog.show();
                            bingImage.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    qmuiFailDialog.dismiss();
                                }
                            }, 1500);
                        }
                    });

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                {
                    qmuiWaitDialog.dismiss();
                    String json = response.body().string();
                    Gson gson = new Gson();
                    final RecycleBinBean recycleBinBean = gson.fromJson(json, RecycleBinBean.class);
                    int resultCode = recycleBinBean.getResult_code();
                    if (resultCode == 601)
                    {
                        //TODO 回收站数据更新
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateManager.updateRecycle(recycleBinBean.getResult());
                                final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                        .setTipWord("刷新成功！").create();
                                qmuiTipDialog.show();
                                bingImage.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        qmuiTipDialog.dismiss();
                                    }
                                }, 500);
                            }
                        });
                    }
                    else if (resultCode == -1)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final QMUITipDialog qmuiFailDialog = new QMUITipDialog.Builder(context)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                        .setTipWord("连接数据库失败!请稍后重试").create();
                                qmuiFailDialog.show();
                                bingImage.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        qmuiFailDialog.dismiss();
                                    }
                                }, 1500);
                            }
                        });
                    }
                    else
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final QMUITipDialog qmuiFailDialog = new QMUITipDialog.Builder(context)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                                        .setTipWord("当前回收站暂无图片!").create();
                                qmuiFailDialog.show();
                                bingImage.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        qmuiFailDialog.dismiss();
                                    }
                                }, 1500);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 对ViewPager以及相应的Fragment进行初始化
     */
    private void initViewPager()
    {
        tabLayout = findViewById(R.id.tab_layout);
        titles = new ArrayList<>();
        titles.add("云相册");
        titles.add("回收站");
        for (int i = 0; i < titles.size() ; i++)
        {
            tabLayout.addTab(tabLayout.newTab().setText(titles.get(i)));
        }
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //获取云相册数据
                    Response response = OkhttpUtil.sengOkhttpGetRequestForSyn("http://203.195.217.253/cloudimg.php?userID="+userID);
                    String json = response.body().string();
                    Gson gson = new Gson();
                    mAlbumlist = gson.fromJson(json, AlbumBean.class);
                    int resultCode = mAlbumlist.getResult_code();
                    if (resultCode != 501)
                    {
                        mAlbumlist = null;
                    }

                    //获取回收站数据
                    Response responseForRecycle = OkhttpUtil.sengOkhttpGetRequestForSyn("http://203.195.217.253/recycle_bin.php?userID="+userID);
                    String recycleJson = responseForRecycle.body().string();
                    Gson recycleGson = new Gson();
                    mRecycleList = recycleGson.fromJson(recycleJson, RecycleBinBean.class);
                    int resultCodeForRecycle = mRecycleList.getResult_code();
                    if (resultCodeForRecycle == -1)
                    {
                        mRecycleList = null;
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final QMUITipDialog qmuiFailDialog = new QMUITipDialog.Builder(context)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                        .setTipWord("连接数据库失败!请稍后重试").create();
                                qmuiFailDialog.show();
                                bingImage.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        qmuiFailDialog.dismiss();
                                    }
                                }, 1500);
                            }
                        });
                    }

                    //准备数据
                    List<Fragment> fragments = new ArrayList<>();
                    //云相册fragment
                    AlbumFragment albumFragment = new AlbumFragment();
                    albumFragment.setmAlbumBeanList(mAlbumlist);
                    fragments.add(albumFragment);
                    //回收站fragment
                    RecycleFragment recycleFragment = new RecycleFragment();
                    List<String> recycleList = null;
                    if (mRecycleList != null)
                    {
                        recycleList = mRecycleList.getResult();
                    }
                    recycleFragment.setmImgUrlList(recycleList);
                    fragments.add(recycleFragment);
                    final FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(),fragments,titles);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            viewPager.setAdapter(fragmentAdapter);
                            tabLayout.setupWithViewPager(viewPager);
                            tabLayout.setTabsFromPagerAdapter(fragmentAdapter);
                        }
                    });
                    Looper.prepare();
                    Looper.loop();


                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final QMUITipDialog qmuiFailDialog = new QMUITipDialog.Builder(context)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("获取数据失败!请检测网络后重试").create();
                            qmuiFailDialog.show();
                            bingImage.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    qmuiFailDialog.dismiss();
                                }
                            }, 1500);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        uploadManager.activityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    class navigation_iv_onclick implements View.OnClickListener {
        public void onClick(View v) {
            // TODO 调用真机接口
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        uploadManager.requestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
