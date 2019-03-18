package com.gmail.webos21.passwordbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gmail.webos21.passwordbook.Consts;

import java.util.ArrayList;
import java.util.List;

public class PbDbHelper extends SQLiteOpenHelper implements PbDbInterface {

    private static final String TAG = "PbDbHelper";

    private static final String TB_PASSWORD_BOOK = "pwbook";

    private static final String CREATE_TB_PASSWORD_BOOK =
            /* Indent */"CREATE TABLE IF NOT EXISTS " + TB_PASSWORD_BOOK + " (" +
            /* Indent */"	id               INTEGER  PRIMARY KEY  AUTOINCREMENT, " +
            /* Indent */"	surl             VARCHAR(100), " +
            /* Indent */"	sname            VARCHAR(100), " +
            /* Indent */"	stype            VARCHAR(100), " +
            /* Indent */"	myid             VARCHAR(100), " +
            /* Indent */"	mypw             VARCHAR(100), " +
            /* Indent */"	reg_date         INTEGER, " +
            /* Indent */"	memo             VARCHAR(4000) " +
            /* Indent */");";

    private static final String DROP_TB_PASSWORD_BOOK =
            /* Indent */"DROP TABLE IF EXISTS " + TB_PASSWORD_BOOK + ";";

    public PbDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                      int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (Consts.DB_DEBUG) {
            Log.d(TAG, "onCreate [" + db.getPath() + "]");
        }
        db.execSQL(CREATE_TB_PASSWORD_BOOK);
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL(DROP_TB_PASSWORD_BOOK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (Consts.DB_DEBUG) {
            Log.d(TAG, "onUpgrade [" + db.getPath() + "] oldVer = "
                    + oldVersion + ", newVer = " + newVersion);
        }
        if (oldVersion != newVersion) {
            onCreate(db);
        }
    }

    @Override
    public List<PbRow> findRows() {
        List<PbRow> aList = new ArrayList<PbRow>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor rset = db.rawQuery("SELECT * FROM "
                + TB_PASSWORD_BOOK, null);
        if (rset == null || rset.getCount() == 0) {
            return aList;
        }

        rset.moveToFirst();
        do {
            PbRow aRow = new PbRow(
                    /* id ------------- */rset.getLong(0),
                    /* surl ----------- */rset.getString(1),
                    /* sname ---------- */rset.getString(2),
                    /* stype ---------- */rset.getString(3),
                    /* myid ----------- */rset.getString(4),
                    /* mypw ----------- */rset.getString(5),
                    /* reg_date ------- */rset.getLong(6),
                    /* memo ----------- */rset.getString(7));
            aList.add(aRow);
        } while (rset.moveToNext());

        if (rset != null) {
            rset.close();
        }
        db.close();

        if (Consts.DB_DEBUG) {
            debugDump(TB_PASSWORD_BOOK);
        }

        return aList;
    }

    @Override
    public List<PbRow> findRows(String keyString) {
        List<PbRow> aList = new ArrayList<PbRow>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor rset = db.rawQuery(
                /* intent ---------- */ "SELECT * " +
                        /* intent -------- */ " FROM " + TB_PASSWORD_BOOK + " " +
                        /* intent -------- */ " WHERE (surl LIKE ?) OR " +
                        /* intent -------- */ "        (sname LIKE ?) OR " +
                        /* intent -------- */ "        (stype LIKE ?)"
                , new String[]{"%" + keyString + "%", "%" + keyString + "%", "%" + keyString + "%"});
        if (rset == null || rset.getCount() == 0) {
            return aList;
        }

        rset.moveToFirst();
        do {
            PbRow aRow = new PbRow(
                    /* id ------------- */rset.getLong(0),
                    /* surl ----------- */rset.getString(1),
                    /* sname ---------- */rset.getString(2),
                    /* stype ---------- */rset.getString(3),
                    /* myid ----------- */rset.getString(4),
                    /* mypw ----------- */rset.getString(5),
                    /* reg_date ------- */rset.getLong(6),
                    /* memo ----------- */rset.getString(7));
            aList.add(aRow);
        } while (rset.moveToNext());

        if (rset != null) {
            rset.close();
        }
        db.close();

        return aList;
    }

    @Override
    public PbRow getRow(Long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor rset = db.rawQuery("SELECT * FROM " + TB_PASSWORD_BOOK
                + " WHERE id = " + id, null);
        if (rset == null || rset.getCount() == 0) {
            return null;
        }
        rset.moveToFirst();
        PbRow aRow = new PbRow(
                /* id ------------- */rset.getLong(0),
                /* surl ----------- */rset.getString(1),
                /* sname ---------- */rset.getString(2),
                /* stype ---------- */rset.getString(3),
                /* myid ----------- */rset.getString(4),
                /* mypw ----------- */rset.getString(5),
                /* reg_date ------- */rset.getLong(6),
                /* memo ----------- */rset.getString(7));
        rset.close();
        db.close();
        return aRow;
    }

    @Override
    public PbRow getRow(PbRow aRow) {
        return getRow(aRow.getId());
    }

    @Override
    public boolean updateRow(PbRow newRow) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor rset = db.rawQuery("SELECT * FROM " + TB_PASSWORD_BOOK
                + " WHERE id = " + newRow.getId(), null);
        if (rset == null || rset.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put("id", newRow.getId());
            cv.put("surl", newRow.getSiteUrl());
            cv.put("sname", newRow.getSiteName());
            cv.put("stype", newRow.getSiteType());
            cv.put("myid", newRow.getMyId());
            cv.put("mypw", newRow.getMyPw());
            cv.put("reg_date", newRow.getRegDate().getTime());
            cv.put("memo", newRow.getMemo());
            db.insert(TB_PASSWORD_BOOK, null, cv);
        } else {
            ContentValues cv = new ContentValues();
            cv.put("surl", newRow.getSiteUrl());
            cv.put("sname", newRow.getSiteName());
            cv.put("stype", newRow.getSiteType());
            cv.put("myid", newRow.getMyId());
            cv.put("mypw", newRow.getMyPw());
            cv.put("reg_date", newRow.getRegDate().getTime());
            cv.put("memo", newRow.getMemo());
            db.update(TB_PASSWORD_BOOK, cv, " id = ? ",
                    new String[]{Long.toString(newRow.getId())});
        }

        if (rset != null) {
            rset.close();
        }
        db.close();

        return true;
    }

    @Override
    public int deleteRow(Long id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TB_PASSWORD_BOOK, "id = " + id, null);
        db.close();

        return result;
    }

    @Override
    public int deleteRow(PbRow aRow) {
        return deleteRow(aRow.getId());
    }

    private void debugDump(String tableName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor rset = db.rawQuery("SELECT * FROM " + tableName, null);
        if (rset == null) {
            return;
        }

        int nCol = rset.getColumnCount();
        int nRow = rset.getCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nCol; i++) {
            sb.append(rset.getColumnName(i)).append('(').append(i)
                    .append(')').append('\t').append('|').append('\t');
        }
        if (Consts.DB_DEBUG) {
            Log.d(TAG, sb.toString());
        }

        sb.delete(0, sb.length());

        rset.moveToFirst();
        for (int r = 0; r < nRow; r++) {
            for (int c = 0; c < nCol; c++) {
                sb.append(rset.getString(c)).append('\t');
            }
            sb.append('\n');
            rset.moveToNext();
        }
        if (Consts.DB_DEBUG) {
            Log.d(TAG, sb.toString());
        }

        rset.close();
        db.close();
    }
}
