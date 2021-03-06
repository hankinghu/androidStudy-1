package example.album;

import android.Manifest;
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent;
import android.net.Uri
import android.os.Build;
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.dmcbig.mediapicker.PickerActivity;
import com.dmcbig.mediapicker.PickerConfig;
import com.dmcbig.mediapicker.entity.Media;
import com.style.app.FileDirConfig;
import com.style.base.BaseRecyclerViewAdapter
import com.style.base.BaseTitleBarActivity;
import com.style.dialog.SelAvatarDialog;
import com.style.framework.R;
import com.style.framework.databinding.ActivitySelectLocalPictureBinding;
import com.style.utils.DeviceInfoUtil;
import com.style.utils.SystemShareUtil;
import com.style.view.diviver.GridFullDividerDecoration;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;

import example.viewPager.ImageScanActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by xiajun on 2016/10/8.
 */
public class SelectLocalPictureActivity : BaseTitleBarActivity() {

    lateinit var bd: ActivitySelectLocalPictureBinding;
    private lateinit var TAG_ADD: Media;

    private lateinit var adapter: DynamicPublishImageAdapter;
    private lateinit var paths: ArrayList<Media>;
    protected var photoFile: File? = null;
    private var dialog: SelAvatarDialog? = null;

    private var haveImg = false;

    override fun getLayoutResId(): Int {
        return R.layout.activity_select_local_picture;
    }

    override fun initData() {
        bd = getBinding();
        setToolbarTitle("本地图片选择");
        paths = ArrayList();
        TAG_ADD = Media();
        TAG_ADD.name = "addTag";
        paths.add(TAG_ADD);
        adapter = DynamicPublishImageAdapter(getContext(), paths);
        var gridLayoutManager = GridLayoutManager(getContext(), 4);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        bd.recyclerView.setLayoutManager(gridLayoutManager);
        bd.recyclerView.addItemDecoration(GridFullDividerDecoration(getContext(), 4));
        bd.recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(object : BaseRecyclerViewAdapter.OnItemClickListener<Media> {
            override fun onItemClick(position: Int, data: Media) {
                var count = adapter.getItemCount();
                if (position == count - 1) {
                    showSelPicPopupWindow();
                } else {
                    var cacheList = ArrayList<Media>();
                    if (count > 1) {
                        for (i in 0 until count - 1) {
                            cacheList.add(paths.get(i));
                        }
                    }
                    var intent = Intent(getContext(), ImageScanActivity::class.java);
                    intent.putExtra("list", cacheList); // (Optional)
                    startActivityForResult(intent, PickerConfig.CODE_TAKE_ALBUM);
                }
            }
        });

        adapter.setOnDeleteClickListener(object : DynamicPublishImageAdapter.OnDeleteClickListener {
            override fun onItemClickDelete(position: Int) {
                paths.removeAt(position);
                adapter.notifyDataSetChanged();
                setHaveDynamic();
            }
        })

        bd.btnShareText.setOnClickListener({ v ->
            SystemShareUtil.shareText(getContext(), "来自系统分享");
        });
        bd.btnShareImage.setOnClickListener { v ->
            SystemShareUtil.shareImage(getContext(), paths.get(0).path);
        }
    }

    fun selAvatar(v: View) {
        showSelPicPopupWindow();
    }

    val CODE_TAKE_CAMERA = 997// 拍照

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK || resultCode == PickerConfig.RESULT_CODE) {
            when (requestCode) {
                PickerConfig.CODE_TAKE_ALBUM ->
                    if (data != null) {
                        var newPaths: ArrayList<Media> = data.getParcelableArrayListExtra(PickerConfig.EXTRA_RESULT);
                        paths.clear();
                        paths.addAll(newPaths);
                        paths.add(TAG_ADD);
                        adapter.notifyDataSetChanged();
                    }
                CODE_TAKE_CAMERA ->
                    if (photoFile!!.exists()) {
                        DeviceInfoUtil.notifyUpdateGallary(this, photoFile);// 通知系统更新相册
                        var filePath = photoFile?.getAbsolutePath();// 获取相片的保存路径
                        var size = paths.size;
                        if (size >= 10) {
                            showToast("最多上传9张图片");
                        } else {
                            var location = 0;
                            if (size >= 1)
                                location = size - 1;
                            val media = Media();
                            media.path = filePath;
                            media.size = photoFile!!.length();
                            paths.add(location, media);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        showToast(R.string.file_does_not_exist);
                    }
            }
            setHaveDynamic();
        }
    }

    fun setHaveDynamic() {
        var number = adapter.getItemCount();
        haveImg = number > 1;
    }

    fun showSelPicPopupWindow() {
        if (dialog == null) {
            dialog = SelAvatarDialog(getContext());
            dialog?.setOnItemClickListener(object : SelAvatarDialog.OnItemClickListener {
                override fun OnClickCamera() {
                    initPermission();
                }

                override fun OnClickPhoto() {
                    selectPhotos();
                }

                override fun OnClickCancel() {
                }
            });
        }
        dialog?.show();
    }

    fun selectPhotos() {
        var newCount = adapter.getItemCount();
        var cacheList = ArrayList<Media>();
        if (newCount > 1) {
            for (i in 0 until newCount - 1) {
                cacheList.add(paths.get(i));
            }
        }

        var intent = Intent(getContext(), PickerActivity::class.java);
        intent.putExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE);//default image and video (Optional)
        var maxSize = 188743680L;//long long long
        intent.putExtra(PickerConfig.MAX_SELECT_SIZE, maxSize); //default 180MB (Optional)
        intent.putExtra(PickerConfig.MAX_SELECT_COUNT, 40);  //default 40 (Optional)
        intent.putExtra(PickerConfig.DEFAULT_SELECTED_LIST, cacheList); // (Optional)
        this.startActivityForResult(intent, PickerConfig.CODE_TAKE_ALBUM);
    }

    @SuppressLint("CheckResult")
    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var rxPermissions = RxPermissions(this);
            //拍照之前首先需要读写SD卡权限
            rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ grated ->
                        if (grated) {
                            takePhoto();
                        } else {
                            //onError("请开启相机权限");
                        }
                    }, { throwable ->
                        throwable.printStackTrace();
                    });
        } else {
            takePhoto();
        }
    }

    private fun takePhoto() {
        photoFile = File(FileDirConfig.DIR_APP_IMAGE_CAMERA, "${System.currentTimeMillis()}.jpg")
        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, FileDirConfig.FILE_PROVIDER_AUTHORITY, photoFile!!)
        } else {
            uri = Uri.fromFile(photoFile)
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, CODE_TAKE_CAMERA)
    }
}