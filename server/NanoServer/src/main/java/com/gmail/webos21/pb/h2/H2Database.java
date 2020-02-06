package com.gmail.webos21.pb.h2;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.gmail.webos21.pb.db.ContentValues;

public final class H2Database {
	private final String filePath;
	private final String user;
	private final String pass;

	private Object mLock;
	private Connection mConn;
	private PreparedStatement mStmt;

	private boolean mOpened;
	private boolean mMemoryDb;

	public H2Database(String filePath, String user, String pass) {
		this.filePath = filePath;
		this.user = user;
		this.pass = pass;

		this.mLock = new Object();
	}

	public static H2Database openDatabase(String filePath, String user, String pass) {
		H2Database db = new H2Database(filePath, user, pass);
		db.open();
		return db;
	}

	public static H2Database createInMemory(String user, String pass) {
		return openDatabase(null, user, pass);
	}

	private void open() {
		synchronized (mLock) {
			String jdbcUrl = "jdbc:h2:";
			if (filePath == null) {
				mMemoryDb = true;
				jdbcUrl += "mem:";
			} else {
				jdbcUrl += filePath;
			}
			mConn = H2Helper.getConnection(jdbcUrl, user, pass);
			if (mConn != null) {
				mOpened = true;
			} else {
				mOpened = false;
			}
		}
	}

	public void close() {
		synchronized (mLock) {
			if (mOpened == true && mConn != null) {
				clearBeforeTransaction();
				H2Helper.releaseConnection(mConn);
				mOpened = false;
				mConn = null;
			}
		}
	}

	public void reopenReadWrite() {
		synchronized (mLock) {
			if (!mOpened) {
				throw new IllegalStateException("The database '" + filePath + "' is not open.");
			}

			if (!isReadOnlyLocked()) {
				return; // nothing to do
			}
		}
	}

	public static boolean deleteDatabase(File file) {
		if (file == null) {
			throw new IllegalArgumentException("file must not be null");
		}

		boolean deleted = false;
		deleted |= file.delete();

		return deleted;
	}

	public void beginTransaction() {
		synchronized (mLock) {
			H2Helper.beginTransaction(mConn);
		}
	}

	public void endTransaction() {
		synchronized (mLock) {
			H2Helper.endTransaction(mConn);
		}
	}

	public void setTransactionSuccessful() {
		synchronized (mLock) {
			H2Helper.setTransactionSuccessful(mConn);
		}
	}

	public boolean inTransaction() {
		synchronized (mLock) {
			return H2Helper.inTransaction(mConn);
		}
	}

	public String getFilePath() {
		synchronized (mLock) {
			return filePath;
		}
	}

	public String getUser() {
		synchronized (mLock) {
			return user;
		}
	}

	public int getVersion() {
		synchronized (mLock) {
			return H2Helper.getVersion(mConn);
		}
	}

	public void setVersion(int version) {
		synchronized (mLock) {
			H2Helper.dbUpdateDone(mConn, version);
		}
	}

	private void clearBeforeTransaction() {
		if (mStmt != null) {
			H2Helper.closeStatement(mStmt);
			mStmt = null;
		}
	}

	public ResultSet rawQuery(String sql, String[] selectionArgs) {
		synchronized (mLock) {
			if (!mOpened) {
				return null;
			}

			clearBeforeTransaction();

			mStmt = H2Helper.preparedStatement(mConn, sql);
			if (selectionArgs != null && selectionArgs.length > 0) {
				for (int i = 0; i < selectionArgs.length; i++) {
					try {
						mStmt.setString(i + 1, selectionArgs[i]);
					} catch (SQLException e) {
						e.printStackTrace();
						H2Helper.closeStatement(mStmt);
						mStmt = null;
						return null;
					}
				}
			}

			return H2Helper.executeQuery(mStmt);
		}
	}

	public long insert(String table, String nullColumnHack, ContentValues values) {
		synchronized (mLock) {
			long rows = -1;

			if (!mOpened) {
				return rows;
			}

			clearBeforeTransaction();

			StringBuilder sql = new StringBuilder();
			sql.append("INSERT");
			sql.append(" INTO ");
			sql.append(table);
			sql.append('(');

			Object[] bindArgs = null;
			int size = (values != null && !values.isEmpty()) ? values.size() : 0;
			if (size > 0) {
				bindArgs = new Object[size];
				int i = 0;
				for (String colName : values.keySet()) {
					sql.append((i > 0) ? "," : "");
					sql.append(colName);
					bindArgs[i++] = values.get(colName);
				}
				sql.append(')');
				sql.append(" VALUES (");
				for (i = 0; i < size; i++) {
					sql.append((i > 0) ? ",?" : "?");
				}
			} else {
				sql.append(nullColumnHack + ") VALUES (NULL");
			}
			sql.append(')');

			mStmt = H2Helper.preparedStatement(mConn, sql.toString());
			for (int i = 0; i < size; i++) {
				try {
					Object o = bindArgs[i];
					if (o instanceof Integer) {
						Integer t = (Integer) o;
						mStmt.setInt(i + 1, t);
					}
					if (o instanceof Long) {
						Long t = (Long) o;
						mStmt.setLong(i + 1, t);
					}
					if (o instanceof String) {
						String t = (String) o;
						mStmt.setString(i + 1, t);
					}
					if (o instanceof Float) {
						Float t = (Float) o;
						mStmt.setFloat(i + 1, t);
					}
					if (o instanceof Double) {
						Double t = (Double) o;
						mStmt.setDouble(i + 1, t);
					}
					if (o instanceof Timestamp) {
						Timestamp t = (Timestamp) o;
						mStmt.setTimestamp(i + 1, t);
					}
				} catch (SQLException e) {
					e.printStackTrace();
					H2Helper.closeStatement(mStmt);
					mStmt = null;
					return rows;
				}
			}

			if (mStmt != null) {
				rows = H2Helper.executeUpdate(mStmt);

				H2Helper.closeStatement(mStmt);
				mStmt = null;
			}

			return rows;
		}
	}

	public int delete(String table, String whereClause, String[] whereArgs) {
		synchronized (mLock) {
			int rows = -1;

			if (!mOpened) {
				return rows;
			}

			clearBeforeTransaction();

			String sql = "DELETE FROM " + table;
			if (whereClause != null && whereClause.length() > 0) {
				sql += " WHERE " + whereClause;
			}

			mStmt = H2Helper.preparedStatement(mConn, sql);
			if (whereArgs != null) {
				for (int i = 0; i < whereArgs.length; i++) {
					try {
						mStmt.setString(i + 1, whereArgs[i]);
					} catch (SQLException e) {
						e.printStackTrace();
						H2Helper.closeStatement(mStmt);
						mStmt = null;
						return rows;
					}
				}
			}

			if (mStmt != null) {
				rows = H2Helper.executeUpdate(mStmt);

				H2Helper.closeStatement(mStmt);
				mStmt = null;
			}

			return rows;
		}
	}

	public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		synchronized (mLock) {
			int rows = -1;

			if (!mOpened) {
				return rows;
			}

			clearBeforeTransaction();

			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE ");
			sql.append(table);
			sql.append(" SET ");

			int setValuesSize = values.size();
			int bindArgsSize = (whereArgs == null) ? setValuesSize : (setValuesSize + whereArgs.length);
			Object[] bindArgs = new Object[bindArgsSize];
			int i = 0;
			for (String colName : values.keySet()) {
				sql.append((i > 0) ? "," : "");
				sql.append(colName);
				bindArgs[i++] = values.get(colName);
				sql.append("=?");
			}
			if (whereArgs != null) {
				for (i = setValuesSize; i < bindArgsSize; i++) {
					bindArgs[i] = whereArgs[i - setValuesSize];
				}
			}
			if (whereClause != null && whereClause.length() > 0) {
				sql.append(" WHERE ");
				sql.append(whereClause);
			}

			mStmt = H2Helper.preparedStatement(mConn, sql.toString());
			for (i = 0; i < bindArgsSize; i++) {
				try {
					Object o = bindArgs[i];
					if (o instanceof Integer) {
						Integer t = (Integer) o;
						mStmt.setInt(i + 1, t);
					}
					if (o instanceof Long) {
						Long t = (Long) o;
						mStmt.setLong(i + 1, t);
					}
					if (o instanceof String) {
						String t = (String) o;
						mStmt.setString(i + 1, t);
					}
					if (o instanceof Float) {
						Float t = (Float) o;
						mStmt.setFloat(i + 1, t);
					}
					if (o instanceof Double) {
						Double t = (Double) o;
						mStmt.setDouble(i + 1, t);
					}
					if (o instanceof Timestamp) {
						Timestamp t = (Timestamp) o;
						mStmt.setTimestamp(i + 1, t);
					}
				} catch (SQLException e) {
					e.printStackTrace();
					H2Helper.closeStatement(mStmt);
					mStmt = null;
					return rows;
				}
			}

			if (mStmt != null) {
				rows = H2Helper.executeUpdate(mStmt);

				H2Helper.closeStatement(mStmt);
				mStmt = null;
			}

			return rows;
		}
	}

	public void execSQL(String sql) {
		executeSql(sql, null);
	}

	public void execSQL(String sql, Object[] bindArgs) {
		if (bindArgs == null) {
			throw new IllegalArgumentException("Empty bindArgs");
		}
		executeSql(sql, bindArgs);
	}

	private int executeSql(String sql, Object[] bindArgs) {
		synchronized (mLock) {
			int rows = -1;

			if (!mOpened) {
				return rows;
			}

			clearBeforeTransaction();

			mStmt = H2Helper.preparedStatement(mConn, sql);
			if (bindArgs != null && bindArgs.length > 0) {
				for (int i = 0; i < bindArgs.length; i++) {
					try {
						Object o = bindArgs[i];
						if (o instanceof Integer) {
							Integer t = (Integer) o;
							mStmt.setInt(i + 1, t);
						}
						if (o instanceof Long) {
							Long t = (Long) o;
							mStmt.setLong(i + 1, t);
						}
						if (o instanceof String) {
							String t = (String) o;
							mStmt.setString(i + 1, t);
						}
						if (o instanceof Float) {
							Float t = (Float) o;
							mStmt.setFloat(i + 1, t);
						}
						if (o instanceof Double) {
							Double t = (Double) o;
							mStmt.setDouble(i + 1, t);
						}
						if (o instanceof Timestamp) {
							Timestamp t = (Timestamp) o;
							mStmt.setTimestamp(i + 1, t);
						}
					} catch (SQLException e) {
						e.printStackTrace();
						H2Helper.closeStatement(mStmt);
						mStmt = null;
						return rows;
					}
				}
			}

			if (mStmt != null) {
				rows = H2Helper.executeUpdate(mStmt);

				H2Helper.closeStatement(mStmt);
				mStmt = null;
			}

			return rows;
		}
	}

	public boolean isReadOnly() {
		synchronized (mLock) {
			return isReadOnlyLocked();
		}
	}

	private boolean isReadOnlyLocked() {
		return H2Helper.isReadOnly(mConn);
	}

	public boolean isInMemoryDatabase() {
		synchronized (mLock) {
			return mMemoryDb;
		}
	}

	public boolean isOpen() {
		synchronized (mLock) {
			return mOpened;
		}
	}

	public boolean needUpgrade(int newVersion) {
		return newVersion > getVersion();
	}

}
