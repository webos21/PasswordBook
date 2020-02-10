package com.gmail.webos21.passwordbook.web;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StaticResourceExtractor extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "SRE";

    private Context context;
    private String fromDir;
    private String toDir;

    public StaticResourceExtractor(Context context, String fromDir, String toDir) {
        this.context = context;
        this.fromDir = fromDir;
        this.toDir = toDir;
    }

    private boolean copyAssetFolder(Context context, String srcName, String dstName) {
        try {
            Log.d(TAG, "COPY-DIR  : " + srcName + " => " + dstName);

            boolean result = true;
            String fileList[] = context.getAssets().list(srcName);
            if (fileList == null) {
                return false;
            }

            if (fileList.length == 0) {
                result = copyAssetFile(context, srcName, dstName);
            } else {
                File file = new File(dstName);
                result = file.mkdirs();
                for (String filename : fileList) {
                    result &= copyAssetFolder(context, srcName + File.separator + filename, dstName + File.separator + filename);
                }
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean copyAssetFile(Context context, String srcName, String dstName) {
        try {
            Log.d(TAG, "COPY-FILE : " + srcName + " => " + dstName);
            InputStream in = context.getAssets().open(srcName);
            File outFile = new File(dstName);
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[4096];
            int rb;
            while ((rb = in.read(buffer)) != -1) {
                out.write(buffer, 0, rb);
            }
            in.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            copyAssetFolder(context, fromDir, toDir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
