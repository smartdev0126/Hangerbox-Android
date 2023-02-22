package com.hanger_box.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.ItemModel;
import com.hanger_box.models.UserModel;
import com.hanger_box.utils.DialogManager;
import com.hanger_box.utils.ImageUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.hanger_box.common.Common.cm;
import static com.hanger_box.common.Common.countries;
import static com.hanger_box.common.Common.countries_en;
import static com.hanger_box.common.Common.currentActivity;

public class ProfileEditActivity extends AppCompatActivity {

    private LinearLayout loadingLayout, emailLayout;
    private ImageView avatarImg;
    private EditText nicknameExt, profileExt;
    private TextView emailTxt, countryTxt;

    private byte[] imageBitmap;
    private int selectedId = 0;
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
        setContentView(R.layout.activity_profile_edit);
        Common.currentActivity = this;

        loadingLayout = findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.GONE);

        avatarImg = findViewById(R.id.user_avatar);
        if (Common.me.getAvatar() != null && Common.me.getAvatar() != "") {
            Glide.with(Common.currentActivity)
                    .load(Common.me.getAvatar())
                    .placeholder(getResources().getDrawable(R.drawable.ic_profile_gray))
                    .into(avatarImg);
        }else {
            avatarImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_profile_gray));
        }

        emailTxt = findViewById(R.id.email_txt);
        emailTxt.setText(Common.me.getEmail());
        emailLayout = findViewById(R.id.email_layout);
        if (Common.me.getType().equals("FACEBOOK") || Common.me.getType().equals("GOOGLE")) {
            emailLayout.setVisibility(View.GONE);
        }

        nicknameExt = findViewById(R.id.nickname_ext);
        nicknameExt.setText(Common.me.getName());

        countryTxt = findViewById(R.id.country_txt);

        if (Common.lang.equals("ja")) {
            for (int i = 0; i < countries.length; i++) {
                if (Common.me.getCountryName().equals(countries[i]))
                    selectedId = i;
            }
        }else {
            for (int i = 0; i < countries_en.length; i++) {
                if (Common.me.getCountryName().equals(countries_en[i]))
                    selectedId = i;
            }
        }
        if (Common.lang.equals("ja")) {
            countryTxt.setText(String.valueOf(countries[selectedId]));
        }else {
            countryTxt.setText(String.valueOf(countries_en[selectedId]));
        }


        profileExt = findViewById(R.id.profile_ext);
        profileExt.setText(Common.me.getProfile());

        avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.showRadioDialog(Common.currentActivity, null,
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
            }
        });

        findViewById(R.id.account_delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.cm.showAlertDlg(getString(R.string.alert_delete_account_title), getString(R.string.alert_delete_account_msg),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingLayout.setVisibility(View.VISIBLE);

                                OkHttpClient client = new OkHttpClient.Builder()
                                        .connectTimeout(30, TimeUnit.SECONDS)
                                        .writeTimeout(30, TimeUnit.SECONDS)
                                        .readTimeout(30, TimeUnit.SECONDS)
                                        .build();

                                final String path = Config.SERVER_URL + Config.DELETE_ACCOUNT_URL;
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
                                                loadingLayout.setVisibility(View.GONE);
                                                try {
                                                    JSONObject result = new JSONObject(mMessage);
                                                    try {
                                                        cm.showAlertDlg("", result.getString("message"), null, null);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                        LocalStorageManager.saveObjectToLocal(null, "login_token");
                                                        LocalStorageManager.saveObjectToLocal(null, "account");
                                                        Common.me = null;
                                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                        startActivity(intent);
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
                        }, null);
            }
        });

        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingLayout.setVisibility(View.VISIBLE);
                final MediaType MEDIA_TYPE = MediaType.parse("image/png");

                RequestBody requestBody = new FormBody.Builder()
                        .add("name", nicknameExt.getText().toString())
                        .add("country_name", String.valueOf(countries[selectedId]))
                        .add("profile", profileExt.getText().toString())
                        .add("userID", Common.me.getId())
                        .add("lang", Common.lang)
                        .build();

                if (imageBitmap != null) {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("image", "cropped_image.jpeg", RequestBody.create(MEDIA_TYPE, imageBitmap))
                            .addFormDataPart("name", nicknameExt.getText().toString())
                            .addFormDataPart("country_name", String.valueOf(countries[selectedId]))
                            .addFormDataPart("profile", profileExt.getText().toString())
                            .addFormDataPart("userID", Common.me.getId())
                            .addFormDataPart("lang", Common.lang)
                            .build();
                }

                Request request = new Request.Builder()
                        .url(Config.SERVER_URL + Config.UPDATE_PROFILE_URL)
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
                                        try {
                                            JSONObject result = new JSONObject(mMessage);
                                            try {
                                                cm.showAlertDlg("", result.getString("message"), null, null);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Common.me = new UserModel(result.getJSONObject("data"));
                                                Common.addFavorite = true;
                                                Common.initMyItem = true;
                                                Common.initLibrary = true;
                                                LocalStorageManager.saveObjectToLocal(Common.cm.convertToStringFromHashMap(Common.me.getMap()), "account");
                                                Intent intent = new Intent( currentActivity, MainActivity.class );
                                                intent.putExtra("back", "my_page");
                                                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                                                startActivity( intent );
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
        });

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        countryTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.lang.equals("ja")) {
                    DialogManager.showRadioDialog(Common.currentActivity, null,
                            countries, selectedId, null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedId = which;
                                    countryTxt.setText(countries[which]);
                                }
                            });
                }else {
                    DialogManager.showRadioDialog(Common.currentActivity, null,
                            countries_en, selectedId, null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedId = which;
                                    countryTxt.setText(countries_en[which]);
                                }
                            });
                }
            }
        });


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
                Intent returnIntent = new Intent();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] byteArray = stream.toByteArray();
                imageBitmap = byteArray;
                avatarImg.setImageBitmap(bm);
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
        imageView.setAspectRatio(1,1);
    }

    private void showImageEditview(String type) {
        loadingLayout.setVisibility(View.GONE);
        imgType = type;
        if (type.equals("from_camera")) {
            camera_call();
        }else {
            gallery_call();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.currentActivity = this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadingLayout.setVisibility(View.GONE);
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
                if (imgType.equals("from_camera")) {
                    camera_call();
                }else {
                    gallery_call();
                }
                break;
        }
    }
}