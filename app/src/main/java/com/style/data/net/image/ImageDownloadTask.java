package com.style.data.net.image;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.style.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 多线程下载文件
 * Created by xiajun on 2017/2/12.
 */
public class ImageDownloadTask extends Thread {
    private static final int DOWN_FAILED = 1;
    private static final int DOWN_START = 2;
    private static final int DOWN_PROGRESS = 3;
    private static final int DOWN_COMPLETE = 4;

    private static final String TAG = ImageDownloadTask.class.getSimpleName();
    private ImageCallback fileDownCallback;
    private boolean canCallback = true;//是否需要执行回调,默认true

    private String downloadUrl;// 下载链接地址
    private final String dir;// 文件保存跟目录
    private String fileName;// 文件名
    private int fileSize;//文件总大小
    private int progress = 0;//文件下载大小
    private int percent = 0;//文件下载百分比
    private String error;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_START:
                    if (isCallbackEnable()) {
                        fileDownCallback.start(fileSize);
                    }
                    break;
                case DOWN_PROGRESS:
                    if (isCallbackEnable()) {
                        fileDownCallback.inProgress(progress, fileSize, percent);
                    }
                    break;
                case DOWN_COMPLETE:
                    if (isCallbackEnable()) {
                        fileDownCallback.complete(dir, fileName);
                    }
                    break;
                case DOWN_FAILED:
                    if (isCallbackEnable()) {
                        fileDownCallback.failed(error);
                    }
                    break;
            }
        }
    };

    public ImageDownloadTask(String downloadUrl, String dir, String fileName, ImageCallback fileDownCallback) {
        this.downloadUrl = downloadUrl;
        this.dir = dir;
        this.fileName = fileName;
        this.fileDownCallback = fileDownCallback;
    }

    @Override
    public void run() {
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            URL url = new URL(downloadUrl);
            Log.e(TAG, "download file http path:" + downloadUrl);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Charset", "UTF-8");
            // 读取下载文件总大小
            fileSize = conn.getContentLength();
            if (fileSize <= 0) {
                sendMsg2Failed("读取文件失败");
            } else {
                long block = fileSize / 100;// 百分之一是多少byte
                is = conn.getInputStream();
                if (is == null) {
                    sendMsg2Failed("文件获取失败");
                } else {
                    File file = new File(dir, fileName);
                    if (!FileUtil.isNewFileCanWrite(file)) {
                        sendMsg2Failed("文件创建失败");
                    } else {
                        sendMsg2Start();
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int byteCount;
                        while ((byteCount = is.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, byteCount);
                            fileOutputStream.flush();
                            progress = progress + byteCount;
                            percent = (int) (progress / block);// 进度百分比
                            sendMsg2Progress();
                        }
                        sendMsg2Completed();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendMsg2Failed("下载失败");
        } finally {
            try {
                if (is != null)
                    is.close();
                if (fileOutputStream != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendMsg2Failed(String error) {
        this.error = error;
        Log.e(TAG, error);
        sendMsg(DOWN_FAILED);
        deleteCacheFile(dir, fileName);
    }

    protected void sendMsg2Start() {
        sendMsg(DOWN_START);
    }

    protected void sendMsg2Progress() {
        sendMsg(DOWN_PROGRESS);
    }

    protected void sendMsg2Completed() {
        sendMsg(DOWN_COMPLETE);
    }

    private void sendMsg(int code) {
        Message msg = mHandler.obtainMessage(code);
        mHandler.sendMessage(msg);
    }

    private void deleteCacheFile(String dir, String fileName) {
        FileUtil.delete(dir, fileName);
    }


    public void setCanCallback(boolean canCallback) {
        this.canCallback = canCallback;
    }

    public boolean isCallbackEnable() {
        if (this.canCallback && this.fileDownCallback != null)
            return true;
        return false;
    }

    public void setCallback(ImageCallback callback) {
        this.fileDownCallback = callback;
    }
}