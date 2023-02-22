package com.hanger_box.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.utils.ImageUtils;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.hanger_box.common.Common.cm;

public class ImageEditActivity extends AppCompatActivity {

    private static final int CAMERA_ACTIVITY_ID = 101;
    private static final int GALLERY_ACTIVITY_ID = 102;
    private final static int CAMERA_PERMISSIONS_RESULT = 102;

    private LinearLayout loadingLayout;
    private Uri imageUri;
    private CropImageView imageView;

    private String from, imageName, fromPage, imgPath, queryImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        Common.currentActivity = this;

        loadingLayout = findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.GONE);
        from = getIntent().getExtras().getString("from");
        fromPage = getIntent().getExtras().getString("from_page");
        imageName = getIntent().getExtras().getString("image_name");

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
        }else {
            if (from.equals("from_camera")) {
                camera_call();
            }else {
                gallery_call();
            }
        }

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingLayout.setVisibility(View.VISIBLE);
                Bitmap bm = imageView.getCroppedImage();
                Intent returnIntent = new Intent();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (fromPage.equals("profile")) {
                    bm.compress(Bitmap.CompressFormat.JPEG, 15, stream);
                }else {
                    bm.compress(Bitmap.CompressFormat.JPEG, 25, stream);
                }
                byte[] byteArray = stream.toByteArray();
//                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                returnIntent.putExtra("bitmap", byteArray);
                returnIntent.putExtra("image_name", imageName);
                setResult(Activity.RESULT_OK,returnIntent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        finish();
                    }
                }, 2000);
            }
        });

        imageView = findViewById(R.id.image_view);
        if (fromPage.equals("profile")) {
            imageView.setAspectRatio(1,1);
        }

        findViewById(R.id.rotate_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.rotateImage(90);
            }
        });

    }

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_ACTIVITY_ID:
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
//                    try
//                    {
//                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
//                        imageView.setImageBitmap(bitmap);
//                    }
//                    catch(Exception e)
//                    {
//                        e.printStackTrace();
//                    }
                    break;
                case GALLERY_ACTIVITY_ID:
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
        }else {
            this.finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_RESULT:
                if (from.equals("from_camera")) {
                    camera_call();
                }else {
                    gallery_call();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Common.currentActivity = this;
    }
}