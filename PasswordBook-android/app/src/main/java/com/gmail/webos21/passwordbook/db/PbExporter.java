package com.gmail.webos21.passwordbook.db;

import android.os.AsyncTask;
import android.util.Log;

import com.gmail.webos21.passwordbook.Consts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PbExporter extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "PbExporter";

    private PbDbInterface pdi;
    private File csvFile;

    private Runnable postRun;

    public PbExporter(PbDbInterface pdi, File csvFile, Runnable postRun) {
        this.pdi = pdi;
        this.csvFile = csvFile;
        this.postRun = postRun;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        BufferedWriter bwo = null;
        List<PbRow> pblist = pdi.findRows();
        StringBuffer sb = new StringBuffer();

        try {
            bwo = new BufferedWriter(new FileWriter(csvFile));
            for (PbRow pbrow : pblist) {
                String l = makeLine(pbrow, sb);
                if (Consts.DEBUG) {
                    Log.i(TAG, l);
                }
                bwo.write(l);
            }
            bwo.close();
            bwo = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bwo != null) {
                try {
                    bwo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bwo = null;
            }
        }

        sb = null;
        pdi = null;

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        postRun.run();
    }

    private String makeLine(PbRow pbRow, StringBuffer sb) {
        sb.delete(0, sb.length());

        sb.append(Long.toString(pbRow.getId())).append(',');
        sb.append(pbRow.getSiteUrl()).append(',');
        sb.append(pbRow.getSiteName()).append(',');
        sb.append(pbRow.getSiteType()).append(',');
        sb.append(pbRow.getMyId()).append(',');
        sb.append(pbRow.getMyPw()).append(',');
        sb.append(Consts.SDF_DATE.format(pbRow.getRegDate())).append(',');
        String memo = (pbRow.getMemo() == null || pbRow.getMemo().length() == 0) ? "null" : pbRow.getMemo();
        sb.append(memo);
        sb.append("\r\n");

        return sb.toString();
    }
}
