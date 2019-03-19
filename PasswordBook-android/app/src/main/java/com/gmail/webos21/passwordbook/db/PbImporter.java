package com.gmail.webos21.passwordbook.db;

import android.os.AsyncTask;
import android.util.Log;

import com.gmail.webos21.passwordbook.Consts;
import com.gmail.webos21.passwordbook.crypt.PbCryptHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class PbImporter extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "PbImporter";

    private PbDbInterface pdi;
    private File csvFile;
    private byte[] pkBytes;

    private Runnable postRun;

    public PbImporter(PbDbInterface pdi, File csvFile, byte[] pkBytes, Runnable postRun) {
        this.pdi = pdi;
        this.csvFile = csvFile;
        this.pkBytes = pkBytes;
        this.postRun = postRun;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        BufferedReader bri = null;
        try {
            bri = new BufferedReader(new FileReader(csvFile));
            String s;

            while ((s = bri.readLine()) != null) {
                if (Consts.DEBUG) {
                    Log.i(TAG, "[FileRead] " + s);
                }
                processLine(pdi, s, pkBytes);
            }

            bri.close();
            bri = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bri != null) {
                try {
                    bri.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bri = null;
            }
        }
        pdi = null;

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        postRun.run();
    }

    private void processLine(PbDbInterface pdi, String s, byte[] encKey) {
        String[] strArr = s.split(",");

        Long id = null;
        String surl = null;
        Date regdate = null;
        Date fixdate = null;
        String stype = null;
        String myid = null;
        String mypw = null;
        String sname = null;
        String memo = null;

        if (strArr.length == 7) {
            surl = strArr[0];
            if ("null".equals(strArr[1])) {
                regdate = new Date(0);
            } else {
                try {
                    regdate = Consts.SDF_DATE.parse(strArr[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            fixdate = new Date();
            stype = strArr[2];
            myid = PbCryptHelper.encData(strArr[3], encKey);
            mypw = PbCryptHelper.encData(strArr[4], encKey);
            sname = strArr[5];
            memo = ("null".equals(strArr[6]) ? "" : strArr[6]);
        } else if (strArr.length == 8) {
            id = Long.parseLong(strArr[0]);
            surl = strArr[1];
            sname = strArr[2];
            stype = strArr[3];
            myid = PbCryptHelper.encData(strArr[4], encKey);
            mypw = PbCryptHelper.encData(strArr[5], encKey);
            if ("null".equals(strArr[6])) {
                regdate = new Date(0);
            } else {
                try {
                    regdate = Consts.SDF_DATE.parse(strArr[6]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            fixdate = new Date();
            memo = ((strArr[7] == null || strArr[7].length() == 0 || "null".equals(strArr[7])) ? "" : strArr[7]);
        } else if (strArr.length == 9) {
            id = Long.parseLong(strArr[0]);
            surl = strArr[1];
            sname = strArr[2];
            stype = strArr[3];
            myid = PbCryptHelper.encData(strArr[4], encKey);
            mypw = PbCryptHelper.encData(strArr[5], encKey);
            if ("null".equals(strArr[6])) {
                regdate = new Date(0);
            } else {
                try {
                    regdate = Consts.SDF_DATE.parse(strArr[6]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if ("null".equals(strArr[7])) {
                fixdate = new Date();
            } else {
                try {
                    fixdate = Consts.SDF_DATE.parse(strArr[7]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            memo = ((strArr[8] == null || strArr[8].length() == 0 || "null".equals(strArr[8])) ? "" : strArr[8]);
        } else {
            return;
        }

        PbRow pbrow = new PbRow(id, surl, sname, stype, myid, mypw, regdate.getTime(), fixdate.getTime(), memo);
        pdi.updateRow(pbrow);
    }
}
