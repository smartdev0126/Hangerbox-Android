package com.hanger_box.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hanger_box.R;
import com.hanger_box.adapters.AdapterComment;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.fragments.LibrariesFragment;
import com.hanger_box.models.ItemModel;
import com.hanger_box.models.LibraryModel;
import com.hanger_box.models.ModelComment;
import com.hanger_box.models.UserModel;
import com.hanger_box.utils.DialogManager;
import com.hanger_box.utils.GlideImageLoader;
import com.hanger_box.utils.ImageUtils;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
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

public class AddLibraryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ITEM_SELECT_ACTIVITY_ID = 2000;
    private final static int WRITE_PERMISSIONS_RESULT = 102;

    private TextView titleTxt;
    private LinearLayout loadingLayout, codi1ImageLayout, codi2ImageLayout, commentsLayout;
    private ImageView itemImage, codi1Image, codi2Image;
    private de.hdodenhof.circleimageview.CircleImageView sendIcon, saveCommentIcon;
    private RelativeLayout saveBtn, editBtn, deleteBtn, shareLayout, selectItemLayout, shareBtns;
    private TextView addTagBtn, affiliateTitleTxt, codi1CanDescTxt, codi2CanDescTxt, codi1CommentDesc, codi2CommentDesc, commentLb;
    private LinearLayout codi1GetImgLayout, codi2GetImgLayout, addTagLayout/*, shopLinkLayout*/;
    private EditText /*shopLinkExt,*/ affiliateExt, brandExt, priceExt, commentExt, codi1CommentExt, codi2CommentExt, otherCommentExt, addTagExt;
    private TextView categoryTxt, currencyTxt, /*shopLinkTxt,*/ affiliateTxt, commentTxt, codi1CommentTxt, codi2CommentTxt, showNumTxt;
    private ProgressBar loadingProgress1, loadingProgress2, loadingProgress3;

    private RecyclerView recyclerView;
    private ArrayList<ModelComment> commentList;
    private AdapterComment adapterComment;

    private TagView tagGroup;

    private String from = "";
    private String fromAct = "";
    private String state = "new";
    private byte[] itemImgBitmap, codi1ImgBitmap, codi2ImgBitmap;

    private LibraryModel library;
    private Boolean loading = false;
    private Boolean shareShown = false;
    private ArrayList<com.hanger_box.models.Tag> tagList;
    private ItemModel libraryItem;
    //private Uri shareImgFile;
    private File shareImgFile;


    /*
     * image edit view
     * */
    private static final int CAMERA_ACTIVITY_ID = 101;
    private static final int GALLERY_ACTIVITY_ID = 103;
    private final static int CAMERA_PERMISSIONS_RESULT = 104;

    private RelativeLayout imageEditView;
    private Uri imageUri;
    private CropImageView imageView;
    private String imageName, imgType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_library);

        currentActivity = this;

        titleTxt = findViewById(R.id.titleTxt);

        // layouts
        loadingLayout = findViewById(R.id.loading_layout);
        selectItemLayout = findViewById(R.id.add_item_layout);
        shareLayout = findViewById(R.id.share_layout);
        codi1GetImgLayout = findViewById(R.id.codi1_get_img_layout);
        codi2GetImgLayout = findViewById(R.id.codi2_get_img_layout);
        commentsLayout = findViewById(R.id.comments_layout);
        addTagLayout = findViewById(R.id.add_tag_layout);
//        shopLinkLayout = findViewById(R.id.shop_link_layout);
        shareBtns = findViewById(R.id.share_btns);
        shareBtns.setVisibility(View.GONE);

        // buttons
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
        editBtn = findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(this);
        deleteBtn = findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(this);

        // images
        itemImage = findViewById(R.id.library_item_image);
        itemImage.setOnClickListener(this);
        codi1ImageLayout = findViewById(R.id.codi1_img_layout);
        codi1Image = findViewById(R.id.codi1_img);
        codi1Image.setOnClickListener(this);
        codi2ImageLayout = findViewById(R.id.codi2_img_layout);
        codi2Image = findViewById(R.id.codi2_img);
        codi2Image.setOnClickListener(this);
        loadingProgress1 = findViewById(R.id.loading_progress1);
        loadingProgress1.setVisibility(View.GONE);
        loadingProgress2 = findViewById(R.id.loading_progress2);
        loadingProgress2.setVisibility(View.GONE);
        loadingProgress3 = findViewById(R.id.loading_progress3);
        loadingProgress3.setVisibility(View.GONE);

        // text
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
        commentLb = findViewById(R.id.comment_lb);
        commentExt = findViewById(R.id.comment_ext);
        commentTxt = findViewById(R.id.comment_txt);
        showNumTxt = findViewById(R.id.show_num_txt);
        affiliateTitleTxt = findViewById(R.id.affiliate_title_txt);
        codi1CanDescTxt = findViewById(R.id.codi1_can_desc);
        codi2CanDescTxt = findViewById(R.id.codi2_can_desc);
        codi1CommentDesc = findViewById(R.id.codi1_comment_desc);
        codi2CommentDesc = findViewById(R.id.codi2_comment_desc);

        codi1CommentExt = findViewById(R.id.codi1_comment_ext);
        codi1CommentTxt = findViewById(R.id.codi1_comment_txt);
        codi2CommentExt = findViewById(R.id.codi2_comment_ext);
        codi2CommentTxt = findViewById(R.id.codi2_comment_txt);
        otherCommentExt = findViewById(R.id.comment);
        sendIcon = findViewById(R.id.comment_icon);
        saveCommentIcon = findViewById(R.id.save_comment_icon);
        saveCommentIcon.setOnClickListener(this);
        addTagExt = findViewById(R.id.tag_ext);
        recyclerView = findViewById(R.id.recyclecomment);


        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.codi1_from_camera_layout).setOnClickListener(this);
        findViewById(R.id.codi1_from_gallery_layout).setOnClickListener(this);
        findViewById(R.id.codi2_from_camera_layout).setOnClickListener(this);
        findViewById(R.id.codi2_from_gallery_layout).setOnClickListener(this);
        findViewById(R.id.tag_add_btn).setOnClickListener(this);
        findViewById(R.id.share_btn).setOnClickListener(this);
        findViewById(R.id.add_item_btn).setOnClickListener(this);
        findViewById(R.id.facebook_share).setOnClickListener(this);
        findViewById(R.id.twitter_share).setOnClickListener(this);
        findViewById(R.id.instagram_share).setOnClickListener(this);
        findViewById(R.id.email_share).setOnClickListener(this);

        tagList = new ArrayList<com.hanger_box.models.Tag>();
        tagGroup = (TagView) findViewById(R.id.tag_group);
        tagGroup.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(Tag tag, int position) {

                if (state.equals("detail")) {
                    Intent intent = new Intent( currentActivity, MainActivity.class );
                    intent.putExtra("back", "library");
                    intent.putExtra("key_word", tag.text.substring(1));
                    intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                    startActivity( intent );
                }
            }
        });
        tagGroup.setOnTagDeleteListener(new TagView.OnTagDeleteListener() {
            @Override
            public void onTagDeleted(TagView view, Tag tag, int position) {
                tagGroup.remove(position);
                tagList.remove(position);
            }
        });

        library = (LibraryModel) getIntent().getExtras().getSerializable("library");
        if (library != null)
            libraryItem = library.getItem();
        loadingLayout.setVisibility(View.GONE);
        loading = false;
        from = getIntent().getExtras().getString("from");
        fromAct = getIntent().getExtras().getString("from_activity");
        if (from.equals("add_library")) {
            state = "new";
        }else if (from.equals("detail_library")) {
            state = "detail";
        }

        changeSate();

        if (state.equals("detail") && Common.me != null)
            updateLibrary(false);


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
                //itemImage.setImageBitmap(bm);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                if (imageName.equals("cropped_image")) {
                    loadingProgress1.setVisibility(View.GONE);
                    itemImage.setImageBitmap(bm);
                    itemImgBitmap = byteArray;
                }else if (imageName.equals("codi1_image")) {
                    loadingProgress2.setVisibility(View.GONE);
                    codi1Image.setImageBitmap(bm);
                    codi1ImageLayout.setVisibility(View.VISIBLE);
                    codi1GetImgLayout.setVisibility(View.GONE);
                    codi1ImgBitmap = byteArray;
                }else if (imageName.equals("codi2_image")) {
                    loadingProgress3.setVisibility(View.GONE);
                    codi2Image.setImageBitmap(bm);
                    codi2ImageLayout.setVisibility(View.VISIBLE);
                    codi2GetImgLayout.setVisibility(View.GONE);
                    codi2ImgBitmap = byteArray;
                }
                saveBtn.setVisibility(View.VISIBLE);
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
            titleTxt.setText(getString(R.string.add_library_title));
            selectItemLayout.setVisibility(View.VISIBLE);
            if (libraryItem != null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RequestOptions options = new RequestOptions()
                                .priority(Priority.HIGH);

                        if (!libraryItem.getImage().contains("https://api.hanger-box.com/storage/items/large/")) {
                            new GlideImageLoader(itemImage,
                                    loadingProgress1).load("https://api.hanger-box.com/storage/items/large/" + libraryItem.getImage(), options);
                        }else {
                            new GlideImageLoader(itemImage,
                                    loadingProgress1).load(libraryItem.getImage(), options);
                        }
                    }
                }, 500);

                selectItemLayout.setVisibility(View.GONE);
//                shopLinkExt.setVisibility(View.VISIBLE);
//                shopLinkTxt.setVisibility(View.GONE);
                affiliateExt.setVisibility(View.VISIBLE);
                affiliateTxt.setVisibility(View.GONE);
                brandExt.setEnabled(true);
                priceExt.setEnabled(true);
                commentLb.setVisibility(View.GONE);
                commentExt.setVisibility(View.VISIBLE);
                commentTxt.setVisibility(View.GONE);
                codi1CanDescTxt.setVisibility(View.VISIBLE);
                codi2CanDescTxt.setVisibility(View.VISIBLE);
                codi1CommentDesc.setVisibility(View.GONE);
                codi2CommentDesc.setVisibility(View.GONE);
                codi1CommentExt.setVisibility(View.VISIBLE);
                codi1CommentTxt.setVisibility(View.GONE);
                codi2CommentExt.setVisibility(View.VISIBLE);
                codi2CommentTxt.setVisibility(View.GONE);
                otherCommentExt.setVisibility(View.GONE);
                sendIcon.setVisibility(View.GONE);
                saveCommentIcon.setVisibility(View.GONE);
                commentsLayout.setVisibility(View.GONE);
                addTagLayout.setVisibility(View.VISIBLE);
                shareLayout.setVisibility(View.GONE);

                affiliateTitleTxt.setText(getResources().getText(R.string.affiliate_url));
//                if (libraryItem.getShopUrl() == null || libraryItem.getShopUrl().equals("")) {
//                    shopLinkLayout.setVisibility(View.GONE);
//                }else {
//                    shopLinkLayout.setVisibility(View.VISIBLE);
//                    shopLinkExt.setText(libraryItem.getShopUrl());
//                }
                affiliateExt.setText(libraryItem.getAffiliateUrl());
                commentExt.setText(libraryItem.getComment());
                brandExt.setText(libraryItem.getBrand());
                priceExt.setText(String.valueOf(libraryItem.getPrice()));
                int catId = Integer.parseInt(libraryItem.getCategoryId());
                if (categories.length > 0) {
                    if (catId >= categories.length) catId = categories.length - 1;
                    if (Common.lang.equals("ja")) {
                        categoryTxt.setText(categories[catId]);
                    }else {
                        categoryTxt.setText(categories_en[catId]);
                    }
                }
                currencyTxt.setText(libraryItem.getCurrency());
            }
            codi1GetImgLayout.setVisibility(View.VISIBLE);
            codi1ImageLayout.setVisibility(View.GONE);
            codi2GetImgLayout.setVisibility(View.VISIBLE);
            codi2ImageLayout.setVisibility(View.GONE);
            saveBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
            showNumTxt.setVisibility(View.GONE);
        }else if (state.equals("edit")) {
            titleTxt.setText(getString(R.string.edit_library_title));
            selectItemLayout.setVisibility(View.GONE);
//            shopLinkExt.setVisibility(View.VISIBLE);
//            shopLinkTxt.setVisibility(View.GONE);
            affiliateExt.setVisibility(View.VISIBLE);
            affiliateTxt.setVisibility(View.GONE);
            brandExt.setEnabled(true);
            priceExt.setEnabled(true);
            commentLb.setVisibility(View.GONE);
            commentExt.setVisibility(View.VISIBLE);
            commentTxt.setVisibility(View.GONE);
            codi1CanDescTxt.setVisibility(View.VISIBLE);
            codi2CanDescTxt.setVisibility(View.VISIBLE);
            codi1CommentDesc.setVisibility(View.GONE);
            codi2CommentDesc.setVisibility(View.GONE);
            codi1CommentExt.setVisibility(View.VISIBLE);
            codi1CommentTxt.setVisibility(View.GONE);
            codi2CommentExt.setVisibility(View.VISIBLE);
            codi2CommentTxt.setVisibility(View.GONE);
            otherCommentExt.setVisibility(View.GONE);
            sendIcon.setVisibility(View.GONE);
            saveCommentIcon.setVisibility(View.GONE);
            commentsLayout.setVisibility(View.VISIBLE);
            addTagLayout.setVisibility(View.VISIBLE);
            shareLayout.setVisibility(View.GONE);
            saveBtn.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
            showNumTxt.setVisibility(View.GONE);
            if (library != null) {
                libraryRefresh();
            }
        }else if (state.equals("detail")) {
            loadComments();

            titleTxt.setText(getString(R.string.detail_library_title));
            selectItemLayout.setVisibility(View.GONE);
//            shopLinkExt.setVisibility(View.GONE);
//            shopLinkTxt.setVisibility(View.VISIBLE);
            affiliateExt.setVisibility(View.GONE);
            affiliateTxt.setVisibility(View.VISIBLE);
            brandExt.setEnabled(false);
            priceExt.setEnabled(false);
            commentLb.setVisibility(View.GONE);
            commentExt.setVisibility(View.GONE);
            commentTxt.setVisibility(View.VISIBLE);
            codi1CanDescTxt.setVisibility(View.GONE);
            codi2CanDescTxt.setVisibility(View.GONE);
            codi1CommentDesc.setVisibility(View.GONE);
            codi2CommentDesc.setVisibility(View.GONE);
            codi1CommentExt.setVisibility(View.GONE);
            codi1CommentTxt.setVisibility(View.VISIBLE);
            codi2CommentExt.setVisibility(View.GONE);
            codi2CommentTxt.setVisibility(View.VISIBLE);
            otherCommentExt.setVisibility(View.VISIBLE);
            sendIcon.setVisibility(View.VISIBLE);
            saveCommentIcon.setVisibility(View.VISIBLE);
            commentsLayout.setVisibility(View.VISIBLE);
            addTagLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
            if (Common.me != null) {
                if (Common.me.getId().equals(library.getUserId())) {
                    editBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.VISIBLE);
                    readFinished();
                }
            }
            showNumTxt.setVisibility(View.VISIBLE);
            if (library != null) {
                libraryRefresh();


                /*
                Glide.with(this).asBitmap().load(library.getCodiImage1()).into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        shareImgFile = getLocalBitmapUri(resource, "share", AddLibraryActivity.this);
                    }
                    public void onLoadFailed(Drawable errorDrawable) {
                        Log.e(">>>", ">>>");
                    }
                });


                 */


                File folder = getExternalFilesDir(Environment.DIRECTORY_DCIM);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                if (folder.canWrite()) {
                    String fName = "shared_image.png";
                    shareImgFile = new File(folder, fName);
                    if (shareImgFile.exists()) {
                        shareImgFile.delete();
                    }
                    try {
                        shareImgFile.createNewFile();
                        Glide.with(this)
                                .asBitmap()
                                .load(library.getCodiImage1())
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                                        try {
                                            FileOutputStream out = new FileOutputStream(shareImgFile);
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                            out.flush();
                                            out.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    ArrayList<String> permissions = new ArrayList();
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    ArrayList requirePermissions = cm.checkPermissions(permissions);
                    if (!requirePermissions.isEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions((String[]) requirePermissions.toArray(new String[requirePermissions.size()]),
                                    WRITE_PERMISSIONS_RESULT);
                        }
                    }
                }

            }
        }
    }
    /*
    public static Uri getLocalBitmapUri(Bitmap bmp, String name, Activity activity) {
        File externalFilesDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(externalFilesDir, name + ".png");
        if (file.exists()) {
            return FileProvider.getUriForFile(activity, "com.hanger_box.fileProvider", file);
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri bmpUri = FileProvider.getUriForFile(activity, "com.hanger_box.fileProvider", file);
            return bmpUri;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            return null;
        }
    }
    */

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"_id"}, "_data=? ", new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("_id"));
            cursor.close();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return Uri.withAppendedPath(uri, "" + id);
        } else if (!imageFile.exists()) {
            return null;
        } else {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentResolver resolver = context.getContentResolver();
                Uri picCollection = MediaStore.Images.Media.getContentUri("external_primary");
                ContentValues picDetail = new ContentValues();
                picDetail.put("_display_name", imageFile.getName());
                picDetail.put("mime_type", "image/jpg");
                picDetail.put("relative_path", "DCIM/" + UUID.randomUUID().toString());
                picDetail.put("is_pending", (Integer) 1);
                Uri finaluri = resolver.insert(picCollection, picDetail);
                picDetail.clear();
                picDetail.put("is_pending", (Integer) 0);
                resolver.update(picCollection, picDetail, null, null);
                return finaluri;
            }
            ContentValues values = new ContentValues();
            values.put("_data", filePath);
            return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }


    private void libraryRefresh() {
//        itemImage.setImageBitmap(null);
        RequestOptions options = new RequestOptions()
                .priority(Priority.HIGH);

        loadingProgress1.setVisibility(View.VISIBLE);
        if(libraryItem == null) {
            cm.showAlertDlg("",getString(R.string.error_show_content), null, null);
        }else {
            if (!libraryItem.getImage().contains("https://api.hanger-box.com/storage/items/large/")) {
                new GlideImageLoader(itemImage,
                        loadingProgress1).load("https://api.hanger-box.com/storage/items/large/" + libraryItem.getImage(), options);
            } else {
                new GlideImageLoader(itemImage,
                        loadingProgress1).load(libraryItem.getImage(), options);
            }
            if (library.getCodiImage1().equals("")
                    || library.getCodiImage1().equals("#")
                    || library.getCodiImage1().equals("null")
                    || library.getCodiImage1() == null) {
                codi1GetImgLayout.setVisibility(View.VISIBLE);
                codi1ImageLayout.setVisibility(View.GONE);
            } else {
                codi1GetImgLayout.setVisibility(View.GONE);
                codi1ImageLayout.setVisibility(View.VISIBLE);
                codi1Image.setImageBitmap(null);
                loadingProgress2.setVisibility(View.VISIBLE);
                new GlideImageLoader(codi1Image,
                        loadingProgress2).load(library.getCodiImage1(), options);
            }
            if (library.getCodiImage2().equals("")
                    || library.getCodiImage2().equals("#")
                    || library.getCodiImage2().equals("null")
                    || library.getCodiImage2() == null) {
                codi2GetImgLayout.setVisibility(View.VISIBLE);
                codi2ImageLayout.setVisibility(View.GONE);
                codi2CommentDesc.setVisibility(View.GONE);
                if(state.equals("edit")) {
                    codi2CommentExt.setVisibility(View.VISIBLE);
                } else {
                    codi2CommentExt.setVisibility(View.GONE);
                }
                codi2CommentTxt.setVisibility(View.GONE);
            } else {
                codi2GetImgLayout.setVisibility(View.GONE);
                codi2ImageLayout.setVisibility(View.VISIBLE);
                codi2Image.setImageBitmap(null);
                loadingProgress3.setVisibility(View.VISIBLE);
                new GlideImageLoader(codi2Image,
                        loadingProgress3).load(library.getCodiImage2(), options);
            }
            if (library.getTags().size() > 0) {
                tagList.clear();
                tagList.addAll(library.getTags());
            }
            commentExt.setText(libraryItem.getComment());
            codi1CommentExt.setText(library.getCodiComment1());
            codi2CommentExt.setText(library.getCodiComment2());
            if (state.equals("edit")) {
                affiliateTitleTxt.setText(getResources().getText(R.string.affiliate_url));
//            if (libraryItem.getShopUrl() == null || libraryItem.getShopUrl().equals("")) {
//                shopLinkLayout.setVisibility(View.GONE);
//            }else {
//                shopLinkLayout.setVisibility(View.VISIBLE);
//                shopLinkExt.setText(libraryItem.getShopUrl());
//            }
                affiliateExt.setText(libraryItem.getAffiliateUrl());
                commentExt.setText(libraryItem.getComment());
                tagGroup.removeAll();
                ArrayList<Tag> tags = new ArrayList<>();
                Tag tag;
                for (int i = 0; i < tagList.size(); i++) {
                    tag = new Tag("#" + tagList.get(i).getTag());
                    tag.radius = 0F;
                    tag.layoutColor = getResources().getColor(R.color.white);
                    tag.tagTextColor = getResources().getColor(R.color.link);
                    tag.tagTextSize = 15;
                    tag.deleteIndicatorSize = 15;
                    tag.deleteIndicatorColor = getResources().getColor(R.color.black);
                    tag.isDeletable = true;
                    tags.add(tag);
                }
                tagGroup.addTags(tags);
            } else if (state.equals("detail")) {
//            if (libraryItem.getShopUrl() == null || libraryItem.getShopUrl().equals("")) {
//                shopLinkLayout.setVisibility(View.GONE);
//            }else {
//                shopLinkLayout.setVisibility(View.VISIBLE);
//                String html = "<a href="+libraryItem.getShopUrl()+" style='color:blue; text-decoration:underline;'\n" +
//                        "                                target='_blank' rel='nofollow'>"+libraryItem.getShopUrl()+"</a>";
//                Spanned result = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
//                shopLinkTxt.setText(result);
//                shopLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
//            }
                affiliateTitleTxt.setText(getResources().getText(R.string.library_affiliate));

                if (libraryItem.getAffiliateUrl() == null || libraryItem.getAffiliateUrl().equals("")) {
                } else {
                    String html = libraryItem.getAffiliateUrl();
                    if (!libraryItem.getAffiliateUrl().contains("<a")) {
                        html = "<a href=" + libraryItem.getAffiliateUrl() + " target='_blank' rel='nofollow sponsored noopener' " +
                                "style='color:blue; word-wrap:break-word;'>" + libraryItem.getAffiliateUrl() + "</a>";
                    }
                    Spanned result = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
                    affiliateTxt.setText(result);
                    affiliateTxt.setMovementMethod(LinkMovementMethod.getInstance());
                }
                commentTxt.setText(libraryItem.getComment());
                if (library.getCodiComment1().equals("")) {
                    codi1CommentTxt.setVisibility(View.GONE);
                } else {
                    codi1CommentTxt.setText(library.getCodiComment1());
                }
                if (library.getCodiComment2().equals("")) {
                    codi2CommentTxt.setVisibility(View.GONE);
                } else {
                    codi2CommentTxt.setText(library.getCodiComment2());
                }
                showNumTxt.setText(getResources().getString(R.string.show_num_pre) + library.getShowNum() + getResources().getString(R.string.show_num));
                codi1GetImgLayout.setVisibility(View.GONE);
                codi2GetImgLayout.setVisibility(View.GONE);
                tagGroup.removeAll();
                ArrayList<Tag> tags = new ArrayList<>();
                Tag tag;
                for (int i = 0; i < tagList.size(); i++) {
                    tag = new Tag("#" + tagList.get(i).getTag());
                    tag.radius = 0F;
                    tag.layoutColor = getResources().getColor(R.color.white);
                    tag.tagTextColor = getResources().getColor(R.color.link);
                    tag.tagTextSize = 15;
                    tag.deleteIndicatorSize = 15;
                    tag.deleteIndicatorColor = getResources().getColor(R.color.black);
                    tags.add(tag);
                }
                tagGroup.addTags(tags);
            }
            brandExt.setText(libraryItem.getBrand());
            priceExt.setText(String.valueOf(libraryItem.getPrice()));
            int catId = Integer.parseInt(libraryItem.getCategoryId());
            if (categories.length > 0) {
                if (catId >= categories.length) catId = categories.length - 1;
                if (Common.lang.equals("ja")) {
                    categoryTxt.setText(categories[catId]);
                } else {
                    categoryTxt.setText(categories_en[catId]);
                }
            }
            currencyTxt.setText(libraryItem.getCurrency());
        }
    }

    private void checkLibrary() {
        if (codi1ImgBitmap == null) {
            cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_input_codi_image), null, null);
            return;
        }

        updateItem();
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
                .add("id", libraryItem.getId())
                .add("shop_url", "")
                .add("affiliate_url", affiliateExt.getText().toString())
                .add("comment", commentExt.getText().toString())
                .add("brand", brandExt.getText().toString())
                .add("category_id", String.valueOf(catId))
                .add("price", priceExt.getText().toString())
                .add("currency", currencyTxt.getText().toString())
                .add("userID", Common.me.getId())
                .add("lang", Common.lang)
                .build();

        if (itemImgBitmap != null) {
            final MediaType MEDIA_TYPE = MediaType.parse("image/png");
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id", libraryItem.getId())
                    .addFormDataPart("image", "cropped_image.jpeg", RequestBody.create(MEDIA_TYPE, itemImgBitmap))
                    .addFormDataPart("shop_url", "")
                    .addFormDataPart("affiliate_url", affiliateExt.getText().toString())
                    .addFormDataPart("comment", commentExt.getText().toString())
                    .addFormDataPart("brand", brandExt.getText().toString())
                    .addFormDataPart("category_id", String.valueOf(catId))
                    .addFormDataPart("price", priceExt.getText().toString())
                    .addFormDataPart("currency", currencyTxt.getText().toString())
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
                                        if (state.equals("new")) {
                                            saveLibrary();
                                        }else if (state.equals("edit")){
                                            updateLibrary(true);
                                        }else if (state.equals("detail")){
                                            updateLibrary(false);
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
        });
    }

    private void saveLibrary() {
        loadingLayout.setVisibility(View.VISIBLE);

        loading = true;
        final MediaType MEDIA_TYPE = MediaType.parse("image/png");
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        MultipartBody.Builder builder = bodyBuilder.setType(MultipartBody.FORM);
        builder.addFormDataPart("codi_image1", "codi1_image.png", RequestBody.create(MEDIA_TYPE, codi1ImgBitmap));
        builder.addFormDataPart("item_id", libraryItem.getId());
        builder.addFormDataPart("codi_comment1", codi1CommentExt.getText().toString());
        builder.addFormDataPart("codi_comment2", codi2CommentExt.getText().toString());
        builder.addFormDataPart("show_num", "0");
        builder.addFormDataPart("userID", Common.me.getId());
        builder.addFormDataPart("lang", Common.lang);

        for (int i=0; i<tagList.size(); i++) {
            builder.addFormDataPart("tags[]", tagList.get(i).getTag());
        }

        if (codi2ImgBitmap != null) {
            builder.addFormDataPart("codi_image2", "codi2_image.png", RequestBody.create(MEDIA_TYPE, codi2ImgBitmap));
        }

        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(Config.SERVER_URL + Config.CREATE_LIBRARY_URL)
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
                                        cm.showAlertDlg("", result.getString("message"), null, null);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        library = new LibraryModel(result.getJSONObject("data"));
                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("library", library);
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

    private void updateLibrary(final Boolean updateFlag) {
        if (updateFlag) {
            loadingLayout.setVisibility(View.VISIBLE);
        }else {
            showNumTxt.setText(getResources().getString(R.string.show_num_pre)+String.valueOf(library.getShowNum()+1)+getResources().getString(R.string.show_num));
        }
        loading = true;
        final MediaType MEDIA_TYPE = MediaType.parse("image/png");

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("id", library.getId());
        formBuilder.add("user_id", library.getUserId());
        if(libraryItem == null) {
            formBuilder.add("item_id", "0");
        }else {
            formBuilder.add("item_id", libraryItem.getId());
        }
        formBuilder.add("codi_image_1", library.getCodiImage1().replace("https://api.hanger-box.com/storage/library/large/", ""));
        formBuilder.add("codi_image_2", library.getCodiImage2().replace("https://api.hanger-box.com/storage/library/large/", ""));
        formBuilder.add("codi_comment1", codi1CommentExt.getText().toString());
        formBuilder.add("codi_comment2", codi2CommentExt.getText().toString());
        formBuilder.add("show_num", String.valueOf(library.getShowNum()+1));
        if (Common.me != null) formBuilder.add("userID", Common.me.getId());
        formBuilder.add("lang", Common.lang);

        for (int i=0; i<tagList.size(); i++) {
            formBuilder.add("tags[]", tagList.get(i).getTag());
        }
        RequestBody requestBody = formBuilder.build();
        if (codi1ImgBitmap != null || codi2ImgBitmap != null) {
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
            MultipartBody.Builder builder = bodyBuilder.setType(MultipartBody.FORM);
            builder.addFormDataPart("id", library.getId());
            builder.addFormDataPart("user_id", library.getUserId());
            if(libraryItem == null) {
                builder.addFormDataPart("item_id", "0");
            }else {
                builder.addFormDataPart("item_id", libraryItem.getId());
            }
            builder.addFormDataPart("codi_comment1", codi1CommentExt.getText().toString());
            builder.addFormDataPart("codi_comment2", codi2CommentExt.getText().toString());
            builder.addFormDataPart("show_num", String.valueOf(library.getShowNum()+1));
            if (Common.me != null) builder.addFormDataPart("userID", Common.me.getId());
            builder.addFormDataPart("lang", Common.lang);

            for (int i=0; i<tagList.size(); i++) {
                builder.addFormDataPart("tags[]", tagList.get(i).getTag());
            }

            if (codi1ImgBitmap != null) {
                builder.addFormDataPart("codi_image1", "codi1_image.png", RequestBody.create(MEDIA_TYPE, codi1ImgBitmap));
            }else {
                builder.addFormDataPart("codi_image_1", library.getCodiImage1().replace("https://api.hanger-box.com/storage/library/large/", ""));
            }
            if (codi2ImgBitmap != null) {
                builder.addFormDataPart("codi_image2", "codi2_image.png", RequestBody.create(MEDIA_TYPE, codi2ImgBitmap));
            }else {
                builder.addFormDataPart("codi_image_2", library.getCodiImage2().replace("https://api.hanger-box.com/storage/library/large/", ""));
            }
            requestBody = builder.build();
        }

        Request request = new Request.Builder()
                .url(Config.SERVER_URL + Config.UPDATE_LIBRARY_URL)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        if (Common.me != null) {
            request = new Request.Builder()
                    .url(Config.SERVER_URL + Config.UPDATE_LIBRARY_URL)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + LocalStorageManager.getObjectFromLocal("login_token"))
                    .post(requestBody)
                    .build();
        }
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
                                        cm.showAlertDlg("", result.getString("message"), null, null);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        library = new LibraryModel(result);
                                        if (updateFlag) {
                                            Intent returnIntent = new Intent();
                                            returnIntent.putExtra("library", library);
                                            returnIntent.putExtra("delete_flag", false);
                                            setResult(Activity.RESULT_OK,returnIntent);
                                            finish();
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
        });
    }

    private void showImageEditview(String type, String imgName) {
        loadingLayout.setVisibility(View.GONE);
        saveBtn.setVisibility(View.VISIBLE);
//        shopLinkLayout.setVisibility(View.GONE);

        imageName = imgName;
        imgType = type;
        if (type.equals("from_camera")) {
            camera_call();
        }else {
            gallery_call();
        }
    }

    private void deleteTags(com.hanger_box.models.Tag tag, final int tag_index) {
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.REMOVE_LIBRARY_TAG_URL;

        RequestBody requestBody = new FormBody.Builder()
                .add("tag_id", tag.getId())
                .add("library_id", library.getId())
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
                        if (tag_index == tagList.size()-1) {
                            deleteLibrary();
                        }
                    }
                });
            }
        });

    }

    private void deleteLibrary() {
        loadingLayout.setVisibility(View.VISIBLE);
        loading = true;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.DELETE_LIBRARY_URL + "/" + library.getId();

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
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("library", library);
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
        deleteLibraryComment();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        String sharerUrl;
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.back_btn:
//                Intent returnIntent = new Intent();
//                returnIntent.putExtra("library", library);
//                returnIntent.putExtra("delete_flag", false);
//                setResult(Activity.RESULT_OK,returnIntent);
                finish();
                break;
            case R.id.save_btn:

                View current = getCurrentFocus();
                InputMethodManager imm = (InputMethodManager) current.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(current.getWindowToken(), 0);

                if(state.equals("detail") && !Common.me.getId().equals(library.getUserId())) {
                    updateLibrary(false);
                    finish();
                }else {
                    if (library == null) {
                        checkLibrary();
                    } else {
                        updateItem();
                    }

                }
                break;
            case R.id.save_comment_icon:
                if(state.equals("detail") && !Common.me.getId().equals(library.getUserId())) {
                    postComment();
                }
                break;
            case R.id.edit_btn:
                state = "edit";
                changeSate();
                break;
            case R.id.delete_btn:
                if (state.equals("detail")) {
                    Common.cm.showAlertDlg(getString(R.string.alert_delete_library_title), getString(R.string.alert_delete_library_msg),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (tagList.size() > 0) {
                                        for (int i=0; i<tagList.size(); i++) {
//                            if (tagList.get(i).getId().equals(null))
//                                continue;
                                            deleteTags(tagList.get(i), i);
                                        }
                                    }else {
                                        deleteLibrary();
                                    }
                                }
                            }, null);
                }

                break;
            case R.id.library_item_image:
                if (state.equals("detail"))
                    return;
                DialogManager.showRadioDialog(currentActivity, null,
                        getResources().getStringArray(R.array.image_types), 0, null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    showImageEditview("from_camera", "cropped_image");
                                }else {
                                    showImageEditview("from_gallery", "cropped_image");
                                }
                            }
                        });
                break;
            case R.id.codi1_img:
                if (state.equals("detail")) {
                    if (fromAct.equals("profile")) {
                        finish();
                    }else {
                        if (Common.me.getId().equals(library.getUserId())) {
                            intent = new Intent( currentActivity, MainActivity.class );
                            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                            intent.putExtra("back", "my_page");
                            startActivity( intent );
                        }else {
                            intent = new Intent(currentActivity, UserProfileActivity.class);
                            intent.putExtra("user_info", library.getUser());
                            startActivity(intent);
                        }
                    }
                    return;
                }

                DialogManager.showRadioDialog(currentActivity, null,
                        getResources().getStringArray(R.array.image_types), 0, null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    showImageEditview("from_camera", "codi1_image");
                                }else {
                                    showImageEditview("from_gallery", "codi1_image");
                                }
                            }
                        });
                break;
            case R.id.codi2_img:
                if (state.equals("detail")) {
                    if (fromAct.equals("profile")) {
                        finish();
                    }else {
                        if (Common.me.getId().equals(library.getUserId())) {
                            intent = new Intent( currentActivity, MainActivity.class );
                            intent.putExtra("back", "my_page");
                            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                            startActivity( intent );
                        }else {
                            intent = new Intent(currentActivity, UserProfileActivity.class);
                            intent.putExtra("user_info", library.getUser());
                            startActivity(intent);
                        }
                    }
                    return;
                }
                DialogManager.showRadioDialog(currentActivity, null,
                        getResources().getStringArray(R.array.image_types), 0, null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    showImageEditview("from_camera", "codi2_image");
                                }else {
                                    showImageEditview("from_gallery", "codi2_image");
                                }
                            }
                        });
                break;
            case R.id.codi1_from_camera_layout:
                if (state.equals("detail"))
                    return;
                showImageEditview("from_camera", "codi1_image");
                break;
            case R.id.codi1_from_gallery_layout:
                if (state.equals("detail"))
                    return;
                showImageEditview("from_gallery", "codi1_image");
                break;
            case R.id.codi2_from_camera_layout:
                if (state.equals("detail"))
                    return;
                showImageEditview("from_camera", "codi2_image");
                break;
            case R.id.codi2_from_gallery_layout:
                if (state.equals("detail"))
                    return;
                showImageEditview("from_gallery", "codi2_image");
                break;
            case R.id.add_item_btn:
                intent = new Intent(getApplicationContext(), ItemSelectActivity.class);
                startActivityForResult(intent, ITEM_SELECT_ACTIVITY_ID);
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
            case R.id.tag_add_btn:
                if (!addTagExt.getText().toString().equals("")) {
                    HashMap tagMap = new HashMap();
                    tagMap.put("id", null);
                    tagMap.put("tag", addTagExt.getText().toString());

                    tagList.add(new com.hanger_box.models.Tag(tagMap));
                    Tag tag = new Tag("#" + addTagExt.getText().toString());
                    tag.radius = 0F;
                    tag.layoutColor = getResources().getColor(R.color.white);
                    tag.tagTextColor = getResources().getColor(R.color.link);
                    tag.tagTextSize = 15;
                    tag.deleteIndicatorSize = 15;
                    tag.deleteIndicatorColor = getResources().getColor(R.color.black);
                    tag.isDeletable = true;
                    tagGroup.addTag(tag);
                    addTagExt.setText("");
                }

                break;
            case R.id.share_btn:
                if (shareShown) {
                    shareShown = false;
                    shareBtns.animate()
                            .alpha(0)
                            .setDuration(100)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    shareBtns.setVisibility(View.GONE);
                                }
                            });
                }else {
                    shareShown = true;
                    shareBtns.setVisibility(View.VISIBLE);
                    shareBtns.setAlpha(0);
                    shareBtns.animate()
                            .alpha(1)
                            .setDuration(100)
                            .setListener(null);
                }
                break;
            case R.id.facebook_share:
                sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=https%3A%2F%2Fhanger-box.com%2Flibrary-details%2F" + library.getId();
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Uri.decode(sharerUrl)));
                startActivity(intent);
//                sharerUrl = libraryItem.getAffiliateUrl();
//                intent = new Intent(android.content.Intent.ACTION_SEND);
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareImgFile));
//                intent.putExtra(Intent.EXTRA_SUBJECT, "HANGER BOX");
//                intent.putExtra(Intent.EXTRA_TEXT   , getString(R.string.share_desc) + "\n" + sharerUrl);
//                intent.setPackage("com.facebook.katana");
//                startActivity(intent);
                break;
            case R.id.twitter_share:
                sharerUrl = "https://twitter.com/intent/tweet?url=https%3A%2F%2Fhanger-box.com%2Flibrary-details%2F" + library.getId()
                        + "&text=" + getString(R.string.share_desc);
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Uri.decode(sharerUrl)));
                startActivity(intent);
//                sharerUrl = libraryItem.getAffiliateUrl();
//                intent = new Intent(android.content.Intent.ACTION_SEND);
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareImgFile));
//                intent.putExtra(Intent.EXTRA_SUBJECT, "HANGER BOX");
//                intent.putExtra(Intent.EXTRA_TEXT   , getString(R.string.share_desc) + "\n" + sharerUrl);
//                intent.setPackage("com.twitter.android");
//                startActivity(intent);
                break;
            case R.id.instagram_share:
                sharerUrl = "https://hanger-box.com/library-details/" + library.getId();
                Intent instaIntent = new Intent(android.content.Intent.ACTION_SEND);
                instaIntent.setPackage("com.instagram.android");
                instaIntent.setType("image/*");
                instaIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                instaIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                instaIntent.addFlags(android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                instaIntent.putExtra(android.content.Intent.EXTRA_STREAM, getImageContentUri(this, shareImgFile));
                instaIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "HANGER BOX");

                StringBuilder sb = new StringBuilder();
                sb.append("<p><b>" + getString(R.string.share_desc) + "</b></p>");
                sb.append("<a href='" + sharerUrl + "'>" + sharerUrl + "</a>");

                instaIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(sb.toString()));
                startActivity(Intent.createChooser(instaIntent, "Share via Instagram"));
                break;
            case R.id.email_share:
                sharerUrl = "https://hanger-box.com/library-details/" + library.getId();
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("text/html");
                emailIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                emailIntent.addFlags(android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{""});
                emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(shareImgFile));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "HANGER BOX");

                StringBuilder sb1 = new StringBuilder();
                sb1.append("<p><b>" + getString(R.string.share_desc) + "</b></p>");
                sb1.append("<a href='" + sharerUrl + "'>" + sharerUrl + "</a>");

                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(sb1.toString()));
                startActivity(Intent.createChooser(emailIntent, "Sharing via Email"));
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
    protected void onDestroy() {
        super.onDestroy();

        File file = shareImgFile;
        if (file != null && file.exists()) {
            shareImgFile.delete();
        }

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
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case ITEM_SELECT_ACTIVITY_ID:
                if(resultCode == Activity.RESULT_OK){
                    libraryItem = (ItemModel) data.getSerializableExtra("item");
                    changeSate();
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_PERMISSIONS_RESULT:
                libraryRefresh();
//                ContextWrapper cw = new ContextWrapper(getApplicationContext());
//                File folder = cw.getDir("imageDir", Context.MODE_PRIVATE);
                /*
                Glide.with(this).asBitmap().load(library.getCodiImage1()).into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        shareImgFile = getLocalBitmapUri(resource, "share", AddLibraryActivity.this);
                    }
                    public void onLoadFailed(Drawable errorDrawable) {
                        Log.e(">>>", ">>>");
                    }
                });


                 */

                File folder = getExternalFilesDir(Environment.DIRECTORY_DCIM);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                String fName = "shared_image.png";
                shareImgFile = new File(folder, fName);
                if (shareImgFile.exists()) {
                    shareImgFile.delete();
                }
                try {
                    shareImgFile.createNewFile();
                    Glide.with(this)
                            .asBitmap()
                            .load(library.getCodiImage1())
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                                    try {
                                        FileOutputStream out = new FileOutputStream(shareImgFile);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                        out.flush();
                                        out.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                

                break;
            case CAMERA_PERMISSIONS_RESULT:
//                if (imgType.equals("from_camera")) {
//                    camera_call();
//                }else {
//                    gallery_call();
//                }
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
    private void loadComments() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        commentList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Libraries").child(library.getId()).child("Comments");

        Query query = reference.orderByChild("ptime");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelComment modelComment = dataSnapshot1.getValue(ModelComment.class);
                    commentList.add(modelComment);
                    adapterComment = new AdapterComment(getApplicationContext(), commentList);
                    recyclerView.setAdapter(adapterComment);

                    adapterComment.setOnItemClickListener(new AdapterComment.ClickListener() {
                            @Override
                            public void onItemLongClick(int position, View v) {
                                //long click test

                                if(dataSnapshot1.child("uid").getValue().toString().equals(Common.me.getId())) {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);
                                    alert.setTitle("");
                                    alert.setMessage(R.string.delete_comment);
                                    alert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ValueEventListener valueEventListener = new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    deleteComment(position);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            };
                                            Query query = reference.equalTo(Common.me.getId());
                                            query.addListenerForSingleValueEvent(valueEventListener);
                                        }
                                    });
                                    alert.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    alert.create();
                                    alert.show();
                                }
                            }

                            @Override
                            public void onItemClick(int position, View v) {
                                if(state.equals("detail") && Common.me.getId().equals(library.getUserId())) {
                                    LayoutInflater inflater = (LayoutInflater) currentActivity.getSystemService(currentActivity.LAYOUT_INFLATER_SERVICE);
                                    final View dialogLayout = inflater.inflate(R.layout.dialog_reply, null);

                                    AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);
                                    alert.setTitle("");
                                    alert.setIcon(R.drawable.balloon);
                                    alert.setView(dialogLayout);
                                    alert.setMessage(R.string.reply_comment);

                                    final EditText replycom = (EditText) dialogLayout.findViewById(R.id.replycom);

                                    alert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ValueEventListener valueEventListener = new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    replyComment(position, Common.me, replycom.getText().toString());

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            };
                                            Query query = reference.equalTo(Common.me.getId());
                                            query.addListenerForSingleValueEvent(valueEventListener);
                                        }
                                    });
                                    alert.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    alert.create();
                                    alert.show();
                                }
                            }
                        });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void postComment() {

        final String commentss = otherCommentExt.getText().toString().trim();

        if (TextUtils.isEmpty(commentss)) {
            cm.showAlertDlg("", getString(R.string.empty_comment), null, null);
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference datarf = FirebaseDatabase.getInstance().getReference("Libraries").child(library.getId()).child("Comments");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", Common.me.getId());
        hashMap.put("uavatar", Common.me.getAvatar());
        hashMap.put("comment", commentss);
        hashMap.put("ptime", timestamp);
        hashMap.put("repid", "");
        hashMap.put("repuavatar", "");
        hashMap.put("repcomment", "");
        hashMap.put("repptime", timestamp);
        hashMap.put("readstatus", "0");

        datarf.child(Common.me.getId()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updatecommetcount(Common.me.getId(), timestamp);
                otherCommentExt.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                cm.showAlertDlg("", getString(R.string.error_comment), null, null);
            }
        });
    }
    
    private void replyComment(int pos, UserModel uid, String replycom) {

        if (TextUtils.isEmpty(replycom)) {
            cm.showAlertDlg("", getString(R.string.empty_comment), null, null);
            return;
        }

        String timestamp = String.valueOf(System.currentTimeMillis());

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Libraries").child(library.getId()).child("Comments");

        Query query = reference.orderByChild("ptime");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int index = 0;
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    if(index == pos) {
                        String returnSellerID = child.child("uid").getValue().toString();
                        reference.child(returnSellerID).child("repid").setValue("" + uid.getId());
                        reference.child(returnSellerID).child("repuavatar").setValue("" + uid.getAvatar());
                        reference.child(returnSellerID).child("repcomment").setValue("" + replycom);
                        reference.child(returnSellerID).child("repptime").setValue("" + timestamp);
                        //reference.child(returnSellerID).child("readstatus").setValue("" + "0");
                    }
                    index ++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });        
    }

    private void updatecommetcount(String Id, String timestamp) {
        final String commentss = otherCommentExt.getText().toString().trim();

        if (TextUtils.isEmpty(commentss)) {
            cm.showAlertDlg("", getString(R.string.empty_comment), null, null);
            return;
        }
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Libraries").child(library.getId()).child("Comments");

        Query query = reference.orderByChild("ptime");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                reference.child(Id).child("comment").setValue("" + commentss);
                reference.child(Id).child("ptime").setValue("" + timestamp);
                reference.child(Id).child("readstatus").setValue("" + "0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteComment(int pos) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Libraries").child(library.getId()).child("Comments");

        Query query = reference.orderByChild("ptime");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int index = 0;
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    if(child.child("uid").getValue().toString().equals(Common.me.getId())) {
                        reference.child(Common.me.getId()).removeValue();
                        commentList.remove(pos);
                        adapterComment.notifyDataSetChanged();
                    }else if(index == pos && child.child("repid").getValue().toString().equals(Common.me.getId())) {
                        String returnSellerID = child.child("uid").getValue().toString();
                        reference.child(returnSellerID).child("repid").setValue("");
                        reference.child(returnSellerID).child("repuavatar").setValue("");
                        reference.child(returnSellerID).child("repcomment").setValue("");
                        reference.child(returnSellerID).child("readstatus").setValue("" + "1");
                    }
                    index ++;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void deleteLibraryComment() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Libraries").child(library.getId());

        Query query = reference.orderByChild("ptime");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reference.child("Comments").removeValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void readFinished() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Libraries").child(library.getId()).child("Comments");

        Query query = reference.orderByChild("ptime");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String returnSellerID = dataSnapshot1.child("uid").getValue().toString();
                    reference.child(returnSellerID).child("readstatus").setValue("" + "1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}