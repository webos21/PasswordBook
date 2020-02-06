package com.gmail.webos21.pb.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import com.gmail.webos21.pb.Consts;
import com.gmail.webos21.pb.h2.H2Database;
import com.gmail.webos21.pb.h2.H2OpenHelper;

public class PbDbHelper extends H2OpenHelper implements PbDbInterface {

	private static final String TAG = "PbDbHelper";

	private static final String TB_PASSWORD_BOOK = "pwbook";

	private static final String CREATE_TB_PASSWORD_BOOK = /* Indent */"CREATE TABLE IF NOT EXISTS " + TB_PASSWORD_BOOK
			+ " (" +
			/* Indent */"	id               BIGINT  PRIMARY KEY  AUTO_INCREMENT, " +
			/* Indent */"	surl             VARCHAR(100), " +
			/* Indent */"	sname            VARCHAR(100), " +
			/* Indent */"	stype            VARCHAR(100), " +
			/* Indent */"	myid             VARCHAR(100), " +
			/* Indent */"	mypw             VARCHAR(100), " +
			/* Indent */"	reg_date         BIGINT, " +
			/* Indent */"	fix_date         BIGINT, " +
			/* Indent */"	memo             VARCHAR(4000) " +
			/* Indent */");";

	private static final String DROP_TB_PASSWORD_BOOK = /* Indent */"DROP TABLE IF EXISTS " + TB_PASSWORD_BOOK + ";";

	public PbDbHelper(String filePath, String user, String pass, int version) {
		super(filePath, user, pass, version);
	}

	@Override
	public void onCreate(H2Database db) {
		if (Consts.DB_DEBUG) {
			Log.d(TAG, "onCreate [" + getFilePath() + "]");
		}
		db.execSQL(CREATE_TB_PASSWORD_BOOK);
	}

	private void dropTables(H2Database db) {
		db.execSQL(DROP_TB_PASSWORD_BOOK);
	}

	@Override
	public void onUpgrade(H2Database db, int oldVersion, int newVersion) {
		if (Consts.DB_DEBUG) {
			Log.d(TAG, "onUpgrade [" + getFilePath() + "] oldVer = " + oldVersion + ", newVer = " + newVersion);
		}
		if (oldVersion != newVersion) {
			dropTables(db);
			onCreate(db);
		}
	}

	@Override
	public List<PbRow> findRows() {
		List<PbRow> aList = new ArrayList<PbRow>();

		try {
			H2Database db = getReadableDatabase();
			ResultSet rset = db.rawQuery("SELECT * FROM " + TB_PASSWORD_BOOK, null);
			if (rset == null || !rset.first()) {
				return aList;
			}

			do {
				PbRow aRow = new PbRow(/* id ------------- */rset.getLong(1), /* surl ----------- */rset.getString(2),
						/* sname ---------- */rset.getString(3), /* stype ---------- */rset.getString(4),
						/* myid ----------- */rset.getString(5), /* mypw ----------- */rset.getString(6),
						/* reg_date ------- */rset.getLong(7), /* fix_date ------- */rset.getLong(8),
						/* memo ----------- */rset.getString(9));
				aList.add(aRow);
			} while (rset.next());

			if (rset != null) {
				rset.close();
			}
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (Consts.DB_DEBUG) {
			debugDump(TB_PASSWORD_BOOK);
		}

		return aList;
	}

	@Override
	public List<PbRow> findRows(String keyString) {
		List<PbRow> aList = new ArrayList<PbRow>();

		try {
			H2Database db = getReadableDatabase();
			ResultSet rset = db.rawQuery(/* intent ---------- */ "SELECT * " +
			/* intent -------- */ " FROM " + TB_PASSWORD_BOOK + " " +
			/* intent -------- */ " WHERE (surl LIKE ?) OR " +
			/* intent -------- */ "        (sname LIKE ?) OR " +
			/* intent -------- */ "        (stype LIKE ?)",
					new String[] { "%" + keyString + "%", "%" + keyString + "%", "%" + keyString + "%" });
			if (rset == null || !rset.first()) {
				return aList;
			}

			do {
				PbRow aRow = new PbRow(/* id ------------- */rset.getLong(1), /* surl ----------- */rset.getString(2),
						/* sname ---------- */rset.getString(3), /* stype ---------- */rset.getString(4),
						/* myid ----------- */rset.getString(5), /* mypw ----------- */rset.getString(6),
						/* reg_date ------- */rset.getLong(7), /* fix_date ------- */rset.getLong(8),
						/* memo ----------- */rset.getString(9));
				aList.add(aRow);
			} while (rset.next());

			if (rset != null) {
				rset.close();
			}
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return aList;
	}

	@Override
	public PbRow getRow(Long id) {
		PbRow aRow = null;

		try {
			H2Database db = getReadableDatabase();
			ResultSet rset = db.rawQuery("SELECT * FROM " + TB_PASSWORD_BOOK + " WHERE id = " + id, null);
			if (rset == null || !rset.first()) {
				return null;
			}

			aRow = new PbRow(/* id ------------- */rset.getLong(1), /* surl ----------- */rset.getString(2),
					/* sname ---------- */rset.getString(3), /* stype ---------- */rset.getString(4),
					/* myid ----------- */rset.getString(5), /* mypw ----------- */rset.getString(6),
					/* reg_date ------- */rset.getLong(7), /* fix_date ------- */rset.getLong(8),
					/* memo ----------- */rset.getString(9));
			rset.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return aRow;
	}

	@Override
	public PbRow getRow(PbRow aRow) {
		return getRow(aRow.getId());
	}

	@Override
	public boolean updateRow(PbRow newRow) {
		try {
			H2Database db = getWritableDatabase();
			ResultSet rset = null;

			if (newRow.getId() != null) {
				rset = db.rawQuery("SELECT * FROM " + TB_PASSWORD_BOOK + " WHERE id = " + newRow.getId(), null);
				if (rset != null && rset.first()) {
					rset.close();

					ContentValues cv = new ContentValues();
					cv.put("surl", newRow.getSiteUrl());
					cv.put("sname", newRow.getSiteName());
					cv.put("stype", newRow.getSiteType());
					cv.put("myid", newRow.getMyId());
					cv.put("mypw", newRow.getMyPw());
					cv.put("reg_date", newRow.getRegDate().getTime());
					cv.put("fix_date", newRow.getFixDate().getTime());
					cv.put("memo", newRow.getMemo());
					db.update(TB_PASSWORD_BOOK, cv, " id = ? ", new String[] { Long.toString(newRow.getId()) });
				} else {
					ContentValues cv = new ContentValues();
					// cv.put("id", newRow.getId());
					cv.put("surl", newRow.getSiteUrl());
					cv.put("sname", newRow.getSiteName());
					cv.put("stype", newRow.getSiteType());
					cv.put("myid", newRow.getMyId());
					cv.put("mypw", newRow.getMyPw());
					cv.put("reg_date", newRow.getRegDate().getTime());
					cv.put("fix_date", newRow.getFixDate().getTime());
					cv.put("memo", newRow.getMemo());
					db.insert(TB_PASSWORD_BOOK, null, cv);
				}
			} else {
				ContentValues cv = new ContentValues();
				cv.put("surl", newRow.getSiteUrl());
				cv.put("sname", newRow.getSiteName());
				cv.put("stype", newRow.getSiteType());
				cv.put("myid", newRow.getMyId());
				cv.put("mypw", newRow.getMyPw());
				cv.put("reg_date", newRow.getRegDate().getTime());
				cv.put("fix_date", newRow.getFixDate().getTime());
				cv.put("memo", newRow.getMemo());
				db.insert(TB_PASSWORD_BOOK, null, cv);
			}

			db.close();
		} catch (Exception e) {

		}

		return true;
	}

	@Override
	public int deleteRow(Long id) {
		H2Database db = getWritableDatabase();
		int result = db.delete(TB_PASSWORD_BOOK, "id = " + id, null);
		db.close();

		return result;
	}

	@Override
	public int deleteRow(PbRow aRow) {
		return deleteRow(aRow.getId());
	}

	private void debugDump(String tableName) {
		try {
			H2Database db = getReadableDatabase();
			ResultSet rset = db.rawQuery("SELECT * FROM " + tableName, null);
			if (rset == null) {
				return;
			}

			ResultSetMetaData rmd = rset.getMetaData();

			int nCol = rmd.getColumnCount();
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i <= nCol; i++) {
				sb.append(rmd.getColumnName(i)).append('(').append(i).append(')').append('\t').append('|');
			}
			if (Consts.DB_DEBUG) {
				Log.d(TAG, sb.toString());
			}

			sb.delete(0, sb.length());

			rset.first();
			do {
				for (int c = 1; c <= nCol; c++) {
					sb.append(rset.getString(c)).append('\t').append('|');
				}
				sb.append('\n');
			} while (rset.next());

			if (Consts.DB_DEBUG) {
				Log.d(TAG, sb.toString());
			}

			rset.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
