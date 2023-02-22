package com.hanger_box.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.hanger_box.R;
import com.hanger_box.SwipeDisabledViewPager;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.fragments.CreateFragment;
import com.hanger_box.fragments.FavoriteFragment;
import com.hanger_box.fragments.LibrariesFragment;
import com.hanger_box.fragments.MyItemsFragment;
import com.hanger_box.fragments.MyPageFragment;
import com.hanger_box.models.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;

import static com.hanger_box.common.Common.cm;

public class MainActivity extends AppCompatActivity implements LibrariesFragment.TagClickListener {

    private static final int FAVORITE_EDIT_ACTIVITY_ID = 4001;
    private static final int LIBRARY_EDIT_ACTIVITY_ID = 1003;
    private static final int LIBRARY_ADD_ACTIVITY_ID = 3003;
    private static final int ITEM_EDIT_ACTIVITY_ID = 1002;
    private static final int ITEM_ADD_ACTIVITY_ID = 2002;

    private BottomNavigationView navigation;
    private SwipeDisabledViewPager viewPager;
    private TextView titleTxt;
    private ImageView createLogoImg;
    private RelativeLayout addBtn;
    private RelativeLayout settingBtn;
    private RelativeLayout backBtn, searchBtn, closeBtn;
    private EditText tagSearchExt;

    private FavoriteFragment favoriteFragment;
    private MyItemsFragment myItemsFragment;
    private CreateFragment createFragment;
    private LibrariesFragment librariesFragment;
    private MyPageFragment myPageFragment;
    private LinearLayout loadingLayout;

    private AdView bannerAdView;
    private boolean adLoaded = false;
    private int selectedIndex = 2;
    private Boolean loading = false;
    private Boolean searchFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Common.currentActivity = this;

        favoriteFragment = new FavoriteFragment();
        myItemsFragment = new MyItemsFragment();
        createFragment = new CreateFragment();
        librariesFragment = new LibrariesFragment();
        librariesFragment.setTagClickListener(this);
        myPageFragment = new MyPageFragment();
        loadingLayout = findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.GONE);
        loading = false;

        titleTxt = findViewById(R.id.titleTxt);
        titleTxt.setText(getResources().getString(R.string.app_name));
        titleTxt.setVisibility(View.INVISIBLE);

        createLogoImg = findViewById(R.id.create_logo);

        addBtn = findViewById(R.id.add_btn);
        addBtn.setVisibility(View.GONE);

        settingBtn = findViewById(R.id.setting_btn);
        settingBtn.setVisibility(View.GONE);

        backBtn = findViewById(R.id.back_btn);
        backBtn.setVisibility(View.GONE);
        searchBtn = findViewById(R.id.search_btn);
        searchBtn.setVisibility(View.GONE);
        closeBtn = findViewById(R.id.close_btn);
        closeBtn.setVisibility(View.GONE);

        tagSearchExt = findViewById(R.id.tag_search_ext);
        tagSearchExt.setVisibility(View.GONE);

        bannerAdView = (AdView) findViewById(R.id.bannerAdView);
        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // setting adLoaded to true
                adLoaded = true;
                showBannerAd();
                // Showing a simple Toast message to user when an ad is loaded
//                Toast.makeText (MainActivity.this, "Ad is Loaded", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // setting adLoaded to false
                adLoaded = false;
                showBannerAd();
                // Showing a simple Toast message to user when and ad is failed to load
//                Toast.makeText (MainActivity.this, "Ad Failed to Load ", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdOpened() {

                // Showing a simple Toast message to user when an ad opens and overlay and covers the device screen
//                Toast.makeText (MainActivity.this, "Ad Opened", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdClicked() {

                // Showing a simple Toast message to user when a user clicked the ad
//                Toast.makeText (MainActivity.this, "Ad Clicked", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdClosed() {

                // Showing a simple Toast message to user when the user interacted with ad and got the other app and then return to the app again
//                Toast.makeText (MainActivity.this, "Ad is Closed", Toast.LENGTH_LONG).show();

            }
        });

        //initializing the Google Admob SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

                loadBannerAd();
                //Showing a simple Toast Message to the user when The Google AdMob Sdk Initialization is Completed
//                Toast.makeText (MainActivity.this, "AdMob Sdk Initialize "+ initializationStatus.toString(), Toast.LENGTH_LONG).show();

            }
        });


        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(5);
        navigation = findViewById(R.id.navigation);
        navigation.getMenu().clear();
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                navigation.getMenu().getItem(0).setIcon(R.drawable.ic_favorites);
                navigation.getMenu().getItem(1).setIcon(R.drawable.ic_myitems);
                navigation.getMenu().getItem(2).setIcon(R.drawable.ic_create);
                navigation.getMenu().getItem(3).setIcon(R.drawable.ic_galleries);
                navigation.getMenu().getItem(4).setIcon(R.drawable.ic_mypage);
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.navigation_favorite:
                        if (Common.me == null) {
                            intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        viewPager.setCurrentItem(0);
                        item.setIcon(R.drawable.ic_favorites_delete);
                        return true;
                    case R.id.navigation_my_items:
                        if (Common.me == null) {
                            intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        item.setIcon(R.drawable.ic_myitems_full);
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.navigation_create:
                        item.setIcon(R.drawable.ic_create_full);
                        viewPager.setCurrentItem(2);
                        return true;
                    case R.id.navigation_libraries:
                        item.setIcon(R.drawable.ic_galleries_full);
                        viewPager.setCurrentItem(3);
                        return true;
                    case R.id.navigation_my_page:
                        if (Common.me == null) {
                            intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        item.setIcon(R.drawable.ic_mypage_full);
                        viewPager.setCurrentItem(4);
                        return true;
                }
                return false;
            }
        });
        navigation.setItemIconTintList(null);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedIndex = position;
                if (viewPager.getCurrentItem() == 0) {
                    showBannerAd();
                    addBtn.setVisibility(View.GONE);
                    searchBtn.setVisibility(View.GONE);
                    closeBtn.setVisibility(View.GONE);
                    backBtn.setVisibility(View.GONE);
                    tagSearchExt.setVisibility(View.GONE);
                    settingBtn.setVisibility(View.GONE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setVisibility(View.VISIBLE);
                    titleTxt.setText(getResources().getString(R.string.favorite_nav_title));
//                    if (Common.addFavorite) {
                        favoriteFragment.refresh();
//                        Common.addFavorite = false;
//                    }
                } else if (viewPager.getCurrentItem() == 1) {
                    showBannerAd();
                    addBtn.setVisibility(View.VISIBLE);
                    searchBtn.setVisibility(View.GONE);
                    closeBtn.setVisibility(View.GONE);
                    backBtn.setVisibility(View.GONE);
                    tagSearchExt.setVisibility(View.GONE);
                    settingBtn.setVisibility(View.GONE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setVisibility(View.VISIBLE);
                    titleTxt.setText(getResources().getString(R.string.my_items_nav_title));
//                    if (Common.initMyItem) {
                        myItemsFragment.refresh();
//                        Common.initMyItem = false;
//                    }
                } else if (viewPager.getCurrentItem() == 2) {
                    hideBannerAd();
                    addBtn.setVisibility(View.GONE);
                    searchBtn.setVisibility(View.GONE);
                    closeBtn.setVisibility(View.GONE);
                    backBtn.setVisibility(View.GONE);
                    tagSearchExt.setVisibility(View.GONE);
                    settingBtn.setVisibility(View.GONE);
                    titleTxt.setVisibility(View.INVISIBLE);
                    createLogoImg.setVisibility(View.VISIBLE);
                } else if (viewPager.getCurrentItem() == 3) {
                    showBannerAd();
                    settingBtn.setVisibility(View.GONE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setText(getResources().getString(R.string.libraries_nav_title));
                    if (searchFlag) {
                        addBtn.setVisibility(View.GONE);
                        searchBtn.setVisibility(View.GONE);
                        closeBtn.setVisibility(View.VISIBLE);
                        backBtn.setVisibility(View.VISIBLE);
                        titleTxt.setVisibility(View.GONE);
                        tagSearchExt.setVisibility(View.VISIBLE);
                        tagSearchExt.setHint(R.string.tag_search_placeholder);
                    }else {
                        addBtn.setVisibility(View.VISIBLE);
                        searchBtn.setVisibility(View.VISIBLE);
                        closeBtn.setVisibility(View.GONE);
                        backBtn.setVisibility(View.GONE);
                        titleTxt.setVisibility(View.VISIBLE);
                        tagSearchExt.setVisibility(View.GONE);
//                        if (Common.initLibrary) {
                            librariesFragment.refresh();
//                            Common.initLibrary = false;
//                        }
                    }
                } else if (viewPager.getCurrentItem() == 4) {
                    showBannerAd();
                    addBtn.setVisibility(View.GONE);
                    searchBtn.setVisibility(View.GONE);
                    closeBtn.setVisibility(View.GONE);
                    backBtn.setVisibility(View.GONE);
                    tagSearchExt.setVisibility(View.GONE);
                    settingBtn.setVisibility(View.VISIBLE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setVisibility(View.VISIBLE);
                    titleTxt.setText(getResources().getString(R.string.my_page_nav_title));
//                    myPageFragment.refresh();
                } else {
                    showBannerAd();
                    addBtn.setVisibility(View.INVISIBLE);
                    searchBtn.setVisibility(View.GONE);
                    closeBtn.setVisibility(View.GONE);
                    backBtn.setVisibility(View.GONE);
                    tagSearchExt.setVisibility(View.GONE);
                    settingBtn.setVisibility(View.INVISIBLE);
                    createLogoImg.setVisibility(View.INVISIBLE);
                    titleTxt.setVisibility(View.VISIBLE);
                    titleTxt.setText(R.string.app_name);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.me == null)  return;
                if (selectedIndex == 1) {
                    myItemsFragment.addItemBtnClicked();
                    backBtn.setVisibility(View.VISIBLE);
                }else if (selectedIndex == 3) {
                    Intent intent = new Intent(getApplicationContext(), AddLibraryActivity.class);
                    intent.putExtra("from_activity", "main");
                    intent.putExtra("from", "add_library");
                    startActivityForResult(intent, LIBRARY_ADD_ACTIVITY_ID);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedIndex == 3) {
                    addBtn.setVisibility(View.VISIBLE);
                    searchBtn.setVisibility(View.VISIBLE);
                    closeBtn.setVisibility(View.GONE);
                    backBtn.setVisibility(View.GONE);
                    titleTxt.setVisibility(View.VISIBLE);
                    tagSearchExt.setVisibility(View.GONE);
                    tagSearchExt.setText("");
                    searchFlag = false;
                }else if (selectedIndex == 1) {
                    myItemsFragment.backBtnClicked();
                    backBtn.setVisibility(View.GONE);
                }
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBtn.setVisibility(View.GONE);
                searchBtn.setVisibility(View.GONE);
                closeBtn.setVisibility(View.VISIBLE);
                backBtn.setVisibility(View.VISIBLE);
                titleTxt.setVisibility(View.GONE);
                tagSearchExt.setVisibility(View.VISIBLE);
                tagSearchExt.setHint(R.string.tag_search_placeholder);
                searchFlag = true;
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagSearchExt.setText("");
            }
        });

        tagSearchExt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
//                if (!s.toString().equals("")) {
                    librariesFragment.changedSearchTag(s.toString());
//                }
            }
        });

        final String selectedFg = getIntent().getExtras().getString("back");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (selectedFg.equals("my_page")) {
                    navigation.setSelectedItemId(R.id.navigation_my_page);
                }else if (selectedFg.equals("library")) {
                    searchFlag = true;
                    navigation.setSelectedItemId(R.id.navigation_libraries);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tagSearchExt.setText(getIntent().getExtras().getString("key_word"));
                        }
                    }, 500);
                }else {
                    navigation.setSelectedItemId(R.id.navigation_create);
                }
            }
        }, 500);
        navigation.getMenu().clear();
        navigation.inflateMenu(R.menu.navigation);
    }

    private void getCategories() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.CATEGORY_ITEMS_URL;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject result = new JSONObject(mMessage);
                            try {
                                cm.showAlertDlg("", result.getString("message"), null, null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                JSONArray catObjs = result.getJSONArray("data");
                                if (catObjs != null) {
                                    Common.categories = new CharSequence[catObjs.length()];
                                    Common.categories_en = new CharSequence[catObjs.length()];
                                    for (int i=0; i<catObjs.length(); i++) {
                                        try {
                                            JSONObject object = (JSONObject) catObjs.get(i);
                                            Common.categories[i] = object.getString("name");
                                            Common.categories_en[i] = object.getString("slug");
                                        } catch (JSONException e2) {
                                            e2.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.activity_main_drawer, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.menuChangeProfile:
                        intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menuChangePassword:
                        if (Common.me.getType().equals("FACEBOOK") || Common.me.getType().equals("GOOGLE")) {
                            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_password_sns), null, null);
                            return true;
                        }
                        intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menuLinkImage:
                        intent = new Intent(getApplicationContext(), ImageHelpActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menuPrivacy:
                        intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menuCompany:
                        intent = new Intent(getApplicationContext(), IntroActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menuLogout:
                        LocalStorageManager.saveObjectToLocal(null, "top_item");
                        LocalStorageManager.saveObjectToLocal(null, "bottom_item");
                        LocalStorageManager.saveObjectToLocal(null, "login_token");
                        LocalStorageManager.saveObjectToLocal(null, "account");
                        Common.me = null;
                        Common.setLocale();

                        intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void loadBannerAd()
    {
        // Creating  a Ad Request
        AdRequest adRequest = new AdRequest.Builder().build();

        // load Ad with the Request
        bannerAdView.loadAd(adRequest);

        // Showing a simple Toast message to user when an ad is Loading
//        Toast.makeText (MainActivity.this, "Banner Ad is loading ", Toast.LENGTH_LONG).show();

    }

    private void showBannerAd()
    {
        if ( adLoaded )
        {
            if (selectedIndex != 2) {
                bannerAdView.setVisibility(View.VISIBLE);
            }else {
                hideBannerAd();
            }
            //showing the ad Banner Ad if it is loaded

            // Showing a simple Toast message to user when an banner ad is shown to the user
//            Toast.makeText (MainActivity.this, "Banner Ad Shown", Toast.LENGTH_LONG).show();
        }
        else
        {
            //Load the banner ad if it is not loaded
            loadBannerAd();
        }
    }

    private void hideBannerAd()
    {
        // Hiding the Banner
        bannerAdView.setVisibility(View.GONE);
    }

    void changeTabIndexFromUrl(String changedUrl){
//        if (changedUrl.equals(Config.SERVER_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_home);
//        }else if (changedUrl.equals(Config.FACEBOOK_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_category);
//        }else if (changedUrl.equals(Config.SERVER_URL + Config.BOOKS_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_video);
//        }else if (changedUrl.equals(Config.SCHEDULE_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_favorite);
//        }else if (changedUrl.equals(Config.SERVER_URL + Config.PROFILE_URL)) {
//            navigation.setSelectedItemId(R.id.navigation_profile);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.currentActivity = this;
        Common.setLocale();
        navigation.getMenu().clear();
        navigation.inflateMenu(R.menu.navigation);
        switch (selectedIndex)
        {
            case 0:
                navigation.setSelectedItemId(R.id.navigation_favorite);
                break;
            case 1:
                navigation.setSelectedItemId(R.id.navigation_my_items);
                break;
            case 2:
                navigation.setSelectedItemId(R.id.navigation_create);
                break;
            case 3:
                navigation.setSelectedItemId(R.id.navigation_libraries);
                break;
            case 4:
                navigation.setSelectedItemId(R.id.navigation_my_page);
                break;
        }
        navigation.setSelectedItemId(selectedIndex);
        if (Common.categories.length == 0) {
            getCategories();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FAVORITE_EDIT_ACTIVITY_ID) {
            favoriteFragment.onActivityResult(requestCode, resultCode, data);
        }else if (requestCode == ITEM_EDIT_ACTIVITY_ID || requestCode == ITEM_ADD_ACTIVITY_ID) {
            myItemsFragment.onActivityResult(requestCode, resultCode, data);
        }else if (requestCode == LIBRARY_EDIT_ACTIVITY_ID || requestCode == LIBRARY_ADD_ACTIVITY_ID) {
            librariesFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onTagClick(String key) {
        navigation.setSelectedItemId(R.id.navigation_libraries);
        selectedIndex = 3;
        addBtn.setVisibility(View.GONE);
        searchBtn.setVisibility(View.GONE);
        closeBtn.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
        titleTxt.setVisibility(View.GONE);
        tagSearchExt.setVisibility(View.VISIBLE);
        tagSearchExt.setHint(R.string.tag_search_placeholder);
        searchFlag = true;
        tagSearchExt.setText(key);
    }

    class MyAdapter extends FragmentStatePagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return favoriteFragment;
                case 1:
                    return myItemsFragment;
                case 2:
                    return createFragment;
                case 3:
                    return librariesFragment;
                case 4:
                    return myPageFragment;
                default:
                    throw new IllegalArgumentException("Unknown Tab!!");
            }
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

}
