package com.example.seyoung.findtaste.views;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by seyoung on 2017-10-20.
 */

public class FileLib {
    public static final String TAG = FileLib.class.getSimpleName();
    private volatile static FileLib instance;

    public static FileLib getInstance() {
        if (instance == null) {
            synchronized (FileLib.class) {
                if (instance == null) {
                    instance = new FileLib();
                }
            }
        }
        return instance;
    }

    /**
     * 파일을 저장할 수 있는 디렉토리 객체를 반환한다.
     * @param context 컨텍스트 객체
     * @return 파일 객체
     */
    private File getFileDir(Context context) {
        String state = Environment.getExternalStorageState();
        File filesDir;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            filesDir = context.getExternalFilesDir(null);
        } else {
            filesDir = context.getFilesDir();
        }

        return filesDir;
    }


    public File getImageFile(Context context, String name) {
        return new File(FileLib.getInstance().getFileDir(context), name + ".jpg");
    }
}