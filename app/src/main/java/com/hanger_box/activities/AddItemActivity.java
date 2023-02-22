package com.hanger_box.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.ItemModel;
import com.hanger_box.utils.DialogManager;
import com.hanger_box.utils.GlideImageLoader;
import com.hanger_box.utils.ImageUtils;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.categories;
import static com.hanger_box.common.Common.categories_en;
import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.currentActivity;

public class AddItemActivity extends AppCompatActivity implements View.OnClickListener {

//    private static final int IMAGE_EDIT_ACTIVITY_ID = 1000;
    private static final int ITEM_SELECT_ACTIVITY_ID = 2000;

    private TextView titleTxt;
    private LinearLayout loadingLayout, selectItemTypeLayout, itemImageLayout, imageLinkLayout/*, shopLinkLayout*/;
    private ImageView itemImage;
    private RelativeLayout saveBtn, editBtn, deleteBtn;
    private EditText imageLinkExt/*, shopLinkExt*/, affiliateExt, brandExt, priceExt, commentExt;
    private TextView categoryTxt, currencyTxt/*, shopLinkTxt*/, affiliateTxt, commentTxt;
    private ProgressBar loadingProgress;

    private Boolean fromFavorite = false;
    private String from = "";
    private String state = "new";
    private byte[] imageBitmap;

    private ItemModel item;
    private Boolean loading = false;

    /*
    * image edit view
    * */
    private static final int CAMERA_ACTIVITY_ID = 101;
    private static final int GALLERY_ACTIVITY_ID = 102;
    private final static int CAMERA_PERMISSIONS_RESULT = 103;

    private RelativeLayout imageEditView;
    private Uri imageUri;
    private CropImageView imageView;
    private String imgType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        currentActivity = this;

        titleTxt = findViewById(R.id.titleTxt);
        loadingLayout = findViewById(R.id.loading_layout);

        selectItemTypeLayout = findViewById(R.id.select_item_type_layout);

        imageLinkLayout = findViewById(R.id.image_link_layout);

//        shopLinkLayout = findViewById(R.id.shop_link_layout);

        itemImageLayout = findViewById(R.id.item_image_layout);
        itemImage = findViewById(R.id.item_image);
        itemImage.setOnClickListener(this);
        loadingProgress = findViewById(R.id.loading_progress);
        loadingProgress.setVisibility(View.GONE);

        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        editBtn = findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(this);

        deleteBtn = findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(this);

        imageLinkExt = findViewById(R.id.image_link_txt);
//        shopLinkExt = findViewById(R.id.shop_link_ext);
//        shopLinkTxt = findViewById(R.id.shop_link_txt);
        categoryTxt = findViewById(R.id.category_txt);
        categoryTxt.setOnClickListener(this);
        affiliateExt = findViewById(R.id.affiliate_ext);
        affiliateTxt = findViewById(R.id.affiliate_txt);
        brandExt = findViewById(R.id.brand_txt);
        priceExt = findViewById(R.id.price_txt);
        currencyTxt = findViewById(R.id.currency_txt);
        currencyTxt.setOnClickListener(this);
        commentExt = findViewById(R.id.comment_ext);
        commentTxt = findViewById(R.id.comment_txt);

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.from_camera_layout).setOnClickListener(this);
        findViewById(R.id.from_gallery_layout).setOnClickListener(this);
        findViewById(R.id.from_item_layout).setOnClickListener(this);
        findViewById(R.id.from_link_layout).setOnClickListener(this);

        from = getIntent().getExtras().getString("from");
        if (from.equals("create_top") || from.equals("create_bottom") || from.equals("add_item")) {
            state = "new";
        }else if (from.equals("detail_item")) {
            state = "detail";
        }else if (from.equals("from_camera")) {
            showImageEditview("from_camera");
            state = "edit";
        }else if (from.equals("from_gallery")) {
            showImageEditview("from_gallery");
            state = "edit";
        }else if (from.equals("from_link")) {
            selectItemTypeLayout.setVisibility(View.GONE);
            itemImageLayout.setVisibility(View.GONE);
            saveBtn.setVisibility(View.VISIBLE);
//            shopLinkLayout.setVisibility(View.VISIBLE);
            imageLinkLayout.setVisibility(View.VISIBLE);
            state = "edit";
        }
        fromFavorite = getIntent().getBooleanExtra("from_favorite", false);
        item = (ItemModel) getIntent().getExtras().getSerializable("item");
        changeSate();

        loadingLayout.setVisibility(View.GONE);
        loading = false;

        /*
        * Image Edit View
        * */
        imageEditView = findViewById(R.id.image_edit_layout);
        imageEditView.setVisibility(View.GONE);
        ArrayList<String> permissions = new ArrayList();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        ArrayList requirePermissions = cm.checkPermissions(permissions);
        if (!requirePermissions.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions((String[]) requirePermissions.toArray(new String[requirePermissions.size()]),
                        CAMERA_PERMISSIONS_RESULT);
            }
        }

        findViewById(R.id.img_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingLayout.setVisibility(View.GONE);
                imageEditView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.img_save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingLayout.setVisibility(View.GONE);
                Bitmap bm = imageView.getCroppedImage();
                itemImage.setImageBitmap(bm);
                Intent returnIntent = new Intent();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] byteArray = stream.toByteArray();
                imageBitmap = byteArray;
                loadingProgress.setVisibility(View.GONE);
                saveBtn.setVisibility(View.VISIBLE);
                state = "edit";
                imageView.setImageBitmap(null);
                imageEditView.setVisibility(View.GONE);
            }
        });

        imageView = findViewById(R.id.img_image_view);

        findViewById(R.id.img_rotate_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.rotateImage(90);
            }
        });
    }

    private void changeSate() {
        if (state.equals("new")) {
            titleTxt.setText(getString(R.string.add_item_title));
            selectItemTypeLayout.setVisibility(View.VISIBLE);
//            shopLinkExt.setVisibility(View.VISIBLE);
//            shopLinkTxt.setVisibility(View.GONE);
            affiliateExt.setVisibility(View.VISIBLE);
            affiliateTxt.setVisibility(View.GONE);
            commentExt.setVisibility(View.VISIBLE);
            commentTxt.setVisibility(View.GONE);
            saveBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        }else if (state.equals("edit")) {
            titleTxt.setText(getString(R.string.edit_item_title));
            selectItemTypeLayout.setVisibility(View.GONE);
            imageLinkExt.setEnabled(true);
//            shopLinkExt.setVisibility(View.VISIBLE);
//            shopLinkTxt.setVisibility(View.GONE);
            affiliateExt.setVisibility(View.VISIBLE);
            affiliateTxt.setVisibility(View.GONE);
            brandExt.setEnabled(true);
            priceExt.setEnabled(true);
            commentExt.setVisibility(View.VISIBLE);
            commentTxt.setVisibility(View.GONE);
            saveBtn.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
            if (item != null) {
                itemRefresh();
            }
        }else if (state.equals("detail")) {
            titleTxt.setText(getString(R.string.detail_item_title));
            selectItemTypeLayout.setVisibility(View.GONE);
            imageLinkExt.setEnabled(false);
//            shopLinkExt.setVisibility(View.GONE);
//            shopLinkTxt.setVisibility(View.VISIBLE);
            affiliateExt.setVisibility(View.GONE);
            affiliateTxt.setVisibility(View.VISIBLE);
            brandExt.setEnabled(false);
            priceExt.setEnabled(false);
            commentExt.setVisibility(View.GONE);
            commentTxt.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.GONE);
            if (fromFavorite) {
                editBtn.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.GONE);
            }else {
                editBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.VISIBLE);
            }
            if (item != null) {
                itemRefresh();
            }
        }
    }

    private void itemRefresh() {
//        itemImage.setVisibility(View.VISIBLE);
        itemImage.setImageBitmap(null);
        loadingProgress.setVisibility(View.VISIBLE);
        RequestOptions options = new RequestOptions()
                .priority(Priority.HIGH);

        new GlideImageLoader(itemImage,
                loadingProgress).load(item.getImage(), options);
        imageLinkLayout.setVisibility(View.GONE);
        if (state.equals("edit")) {
//            shopLinkLayout.setVisibility(View.VISIBLE);
//            if (item.getShopUrl() == null || item.getShopUrl().equals("")) {
//                shopLinkLayout.setVisibility(View.GONE);
//            }else {
//                shopLinkLayout.setVisibility(View.VISIBLE);
//                shopLinkExt.setText(item.getShopUrl());
//            }
            affiliateExt.setText(item.getAffiliateUrl());
            commentExt.setText(item.getComment());
        }else if (state.equals("detail")) {
//            if (item.getShopUrl() == null || item.getShopUrl().equals("")) {
//                shopLinkLayout.setVisibility(View.GONE);
//            }else {
//                shopLinkLayout.setVisibility(View.VISIBLE);
//                String html = "<a href="+item.getShopUrl()+" style='color:blue; text-decoration:underline;'\n" +
//                        "                                target='_blank' rel='nofollow'>"+item.getShopUrl()+"</a>";
//                Spanned result = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
//                shopLinkTxt.setText(result);
//                shopLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
//            }
            if (item.getAffiliateUrl() == null || item.getAffiliateUrl().equals("")) {
            }else {
                String html = item.getAffiliateUrl();
                if (!item.getAffiliateUrl().contains("<a")) {
                    html = "<a href="+item.getAffiliateUrl()+" target='_blank' rel='nofollow sponsored noopener' " +
                            "style='color:blue; word-wrap:break-word;'>"+item.getAffiliateUrl()+"</a>";
                }
                Spanned result = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
                affiliateTxt.setText(result);
                affiliateTxt.setMovementMethod(LinkMovementMethod.getInstance());
            }
            commentTxt.setText(item.getComment());
        }
        brandExt.setText(item.getBrand());
        priceExt.setText(String.valueOf(item.getPrice()));
        int catId = Integer.valueOf(item.getCategoryId());
        if (categories.length > 0) {
            if (catId >= categories.length) catId = categories.length - 1;
            if (Common.lang.equals("ja")) {
                categoryTxt.setText(categories[catId]);
            }else {
                categoryTxt.setText(categories_en[catId]);
            }
        }
        currencyTxt.setText(item.getCurrency());
    }

    private void checkItem() {
        if (imageBitmap == null && imageLinkExt.getText().toString().equals("")) {
            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_input_image), null, null);
            return;
        }
        if (categoryTxt.getText().toString().equals("")) {
            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_input_category), null, null);
            return;
        }

        saveItem();
    }

    private void saveItem() {
        String catName = categoryTxt.getText().toString();
        int catId = 0;
        for (int i=0; i<categories.length; i++) {
            if (Common.lang.equals("ja")) {
                if (categories[i].equals(catName)) {
                    catId = i;
                    break;
                }
            }else {
                if (categories_en[i].equals(catName)) {
                    catId = i;
                    break;
                }
            }
        }
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;

        RequestBody requestBody = new FormBody.Builder()
                .add("image_url", imageLinkExt.getText().toString())
                .add("shop_url", "")
                .add("affiliate_url", affiliateExt.getText().toString())
                .add("brand", brandExt.getText().toString())
                .add("category_id", String.valueOf(catId))
                .add("price", priceExt.getText().toString())
                .add("currency", currencyTxt.getText().toString())
                .add("comment", commentExt.getText().toString())
                .add("userID", Common.me.getId())
                .add("lang", Common.lang)
                .build();

        if (imageBitmap != null) {
            final MediaType MEDIA_TYPE = MediaType.parse("image/png");
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "cropped_image.jpeg", RequestBody.create(MEDIA_TYPE, imageBitmap))
                    .addFormDataPart("shop_url", "")
                    .addFormDataPart("affiliate_url", affiliateExt.getText().toString())
                    .addFormDataPart("brand", brandExt.getText().toString())
                    .addFormDataPart("category_id", String.valueOf(catId))
                    .addFormDataPart("price", priceExt.getText().toString())
                    .addFormDataPart("currency", currencyTxt.getText().toString())
                    .addFormDataPart("comment", commentExt.getText().toString())
                    .addFormDataPart("userID", Common.me.getId())
                    .addFormDataPart("lang", Common.lang)
                    .build();
        }

        Request request = new Request.Builder()
                .url(Config.SERVER_URL + Config.CREATE_ITEM_URL)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        loading = false;
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingLayout.setVisibility(View.GONE);
                                loading = false;
                                try {
                                    JSONObject result = new JSONObject(mMessage);
                                    try {
                                        String err = result.getString("message");
                                        cm.showAlertDlg("", getString(R.string.error_item_image), null, null);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        item = new ItemModel(result.getJSONObject("data"));
                                        if (from.equals("create_top")) {
                                            LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "top_item");
                                        }else if (from.equals("create_bottom")) {
                                            LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "bottom_item");
                                        }
                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("item", item);
                                        returnIntent.putExtra("delete_flag", false);
                                        setResult(Activity.RESULT_OK,returnIntent);
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateItem() {
        String catName = categoryTxt.getText().toString();
        int catId = 0;
        for (int i=0; i<categories.length; i++) {
            if (Common.lang.equals("ja")) {
                if (categories[i].equals(catName)) {
                    catId = i;
                    break;
                }
            }else {
                if (categories_en[i].equals(catName)) {
                    catId = i;
                    break;
                }
            }
        }

        loadingLayout.setVisibility(View.VISIBLE);
        loading = false;
        RequestBody requestBody = new FormBody.Builder()
                .add("id", item.getId())
                .add("image_url", item.getImage())
                .add("shop_url", "")
                .add("affiliate_url", affiliateExt.getText().toString())
                .add("brand", brandExt.getText().toString())
                .add("category_id", String.valueOf(catId))
                .add("price", priceExt.getText().toString())
                .add("currency", currencyTxt.getText().toString())
                .add("comment", commentExt.getText().toString())
                .add("userID", Common.me.getId())
                .add("lang", Common.lang)
                .build();

        if (imageBitmap != null) {
            final MediaType MEDIA_TYPE = MediaType.parse("image/png");
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id", item.getId())
                    .addFormDataPart("image", "cropped_image.jpeg", RequestBody.create(MEDIA_TYPE, imageBitmap))
                    .addFormDataPart("shop_url", "")
                    .addFormDataPart("affiliate_url", affiliateExt.getText().toString())
                    .addFormDataPart("brand", brandExt.getText().toString())
                    .addFormDataPart("category_id", String.valueOf(catId))
                    .addFormDataPart("price", priceExt.getText().toString())
                    .addFormDataPart("currency", currencyTxt.getText().toString())
                    .addFormDataPart("comment", commentExt.getText().toString())
                    .addFormDataPart("userID", Common.me.getId())
                    .addFormDataPart("lang", Common.lang)
                    .build();
        }

        Request request = new Request.Builder()
                .url(Config.SERVER_URL + Config.UPDATE_ITEM_URL)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        loading = false;
                        cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                    }
                });
                String mMessage = e.getMessage().toString();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingLayout.setVisibility(View.GONE);
                                loading = false;
                                try {
                                    JSONObject result = new JSONObject(mMessage);
                                    try {
                                        cm.showAlertDlg("", result.getString("message"), null, null);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        item = new ItemModel(result.getJSONObject("data"));
                                        if (from.equals("create_top")) {
                                            LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "top_item");
                                        }else if (from.equals("create_bottom")) {
                                            LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "bottom_item");
                                        }
                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("item", item);
                                        returnIntent.putExtra("delete_flag", false);
                                        setResult(Activity.RESULT_OK,returnIntent);
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void showImageEditview(String type) {
        loadingLayout.setVisibility(View.GONE);
        selectItemTypeLayout.setVisibility(View.GONE);
        itemImageLayout.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);
//        shopLinkLayout.setVisibility(View.GONE);
        imageLinkLayout.setVisibility(View.GONE);

        imgType = type;
        if (type.equals("from_camera")) {
            camera_call();
        }else {
            gallery_call();
        }
    }

    private void deleteItem() {
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.DELETE_ITEM_URL + "/" + item.getId();

        RequestBody requestBody = new FormBody.Builder()
                .add("userID", Common.me.getId())
                .add("lang", Common.lang)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        loading = false;
                    }
                });
                String mMessage = e.getMessage().toString();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                final String mMessage = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        loading = false;
                        try {
                            JSONObject result = new JSONObject(mMessage);
                            try {
                                cm.showAlertDlg("", result.getString("message"), null, null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                if (from.equals("create_top")) {
                                    LocalStorageManager.saveObjectToLocal(null, "top_item");
                                }else if (from.equals("create_bottom")) {
                                    LocalStorageManager.saveObjectToLocal(null, "bottom_item");
                                }
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("item", item);
                                returnIntent.putExtra("delete_flag", true);
                                setResult(Activity.RESULT_OK,returnIntent);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (loading) return;
        Intent intent;
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.save_btn:
                if (item == null) {
                    checkItem();
                }else {
                    updateItem();
                }
                break;
            case R.id.edit_btn:
                state = "edit";
                changeSate();
                break;
            case R.id.delete_btn:
                if (state.equals("detail")) {
                    Common.cm.showAlertDlg(getString(R.string.alert_delete_item_title), getString(R.string.alert_delete_item_msg),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteItem();
                                }
                            }, null);
                }
                break;
            case R.id.item_image:
                if (state.equals("detail"))
                    return;
                DialogManager.showRadioDialog(currentActivity, null,
                        getResources().getStringArray(R.array.image_types), 0, null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    showImageEditview("from_camera");
                                }else {
                                    showImageEditview("from_gallery");
                                }
                            }
                        });

                break;
            case R.id.category_txt:
                if (state.equals("detail"))
                    return;
                if (categories != null) {
                    if (Common.lang.equals("ja")) {
                        DialogManager.showRadioDialog(currentActivity, null,
                                categories, 0, null, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        categoryTxt.setText(categories[which]);
                                    }
                                });
                    }else {
                        DialogManager.showRadioDialog(currentActivity, null,
                                categories_en, 0, null, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        categoryTxt.setText(categories_en[which]);
                                    }
                                });
                    }
                }

                break;
            case R.id.currency_txt:
                if (state.equals("detail"))
                    return;

                DialogManager.showRadioDialog(currentActivity, null,
                        getResources().getStringArray(R.array.currency), 0, null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currencyTxt.setText(getResources().getStringArray(R.array.currency)[which]);
                            }
                        });

                break;
            case R.id.from_camera_layout:
                showImageEditview("from_camera");

                break;
            case R.id.from_gallery_layout:
                showImageEditview("from_gallery");

                break;
            case R.id.from_item_layout:
                if (from.equals("create_top") || from.equals("create_bottom")) {
                    intent = new Intent(getApplicationContext(), ItemSelectActivity.class);
                    startActivityForResult(intent, ITEM_SELECT_ACTIVITY_ID);
                }

                break;
            case R.id.from_link_layout:
                selectItemTypeLayout.setVisibility(View.GONE);
                itemImageLayout.setVisibility(View.GONE);
                saveBtn.setVisibility(View.VISIBLE);
//                shopLinkLayout.setVisibility(View.VISIBLE);
                imageLinkLayout.setVisibility(View.VISIBLE);
                state = "edit";
                changeSate();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentActivity = this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAMERA_ACTIVITY_ID:
                imageEditView.setVisibility(View.VISIBLE);
                try
                {
                    String filePath = ImageUtils.getRealPathFromURI(imageUri.toString());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
                    imageView.setImageBitmap(bitmap);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                break;
            case GALLERY_ACTIVITY_ID:
                imageEditView.setVisibility(View.VISIBLE);
                if (data != null) {
                    Uri selectedImage = data.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case ITEM_SELECT_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    item = (ItemModel) data.getSerializableExtra("item");
                    if (from.equals("create_top")) {
                        LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "top_item");
                        finish();
                    }else if (from.equals("create_bottom")) {
                        LocalStorageManager.saveObjectToLocal(cm.convertToStringFromHashMap(item.getMap()), "bottom_item");
                        finish();
                    }
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                break;
        }
    }

    /*
    * Image Edit View
    * */

    private void camera_call()
    {
        ContentValues values = new ContentValues();
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(intent1, CAMERA_ACTIVITY_ID);
    }

    private void gallery_call()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),GALLERY_ACTIVITY_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_RESULT:
//                if (imgType.equals("from_camera")) {
//                    camera_call();
//                }else {
//                    gallery_call();
//                }
                break;
        }
    }

}