package com.gmail.webos21.pb.h2;

import java.io.File;
import java.sql.Connection;

import com.gmail.webos21.pb.db.Log;

public abstract class H2OpenHelper {
	private static final String TAG = H2OpenHelper.class.getSimpleName();

	private final String filePath;
	private final String user;
	private final String pass;

	private final int mNewVersion;
	private final int mMinimumSupportedVersion;

	private Connection mConn;
	private boolean mIsInitializing;

	public H2OpenHelper(String filePath, String user, String pass, int version) {
		this(filePath, user, pass, version, 0);
	}

	public H2OpenHelper(String filePath, String user, String pass, int version, int minimumSupportedVersion) {
		if (version < 1) {
			throw new IllegalArgumentException("Version must be >= 1, was " + version);
		}

		this.filePath = filePath;
		this.user = user;
		this.pass = pass;

		this.mNewVersion = version;
		this.mMinimumSupportedVersion = Math.max(0, minimumSupportedVersion);
	}

	public String getFilePath() {
		return filePath;
	}

	public String getUser() {
		return user;
	}

	public Connection getWritableDatabase() {
		synchronized (this) {
			return getDatabaseLocked(true);
		}
	}

	public Connection getReadableDatabase() {
		synchronized (this) {
			return getDatabaseLocked(false);
		}
	}

	private Connection getDatabaseLocked(boolean writable) {
		if (mConn != null) {
			if (!H2Helper.isValid(mConn)) {
				// Darn! The user closed the database by calling mDatabase.close().
				mConn = null;
			} else if (!writable || !H2Helper.isReadOnly(mConn)) {
				// The database is already open for business.
				return mConn;
			}
		}

		if (mIsInitializing) {
			throw new IllegalStateException("getDatabase called recursively");
		}

		Connection conn = mConn;
		try {
			mIsInitializing = true;

			if (conn != null) {
				if (writable && H2Helper.isReadOnly(conn)) {
					H2Helper.releaseConnection(conn);
					conn = H2Helper.getConnection("jdbc:h2:" + filePath, user, pass);
				}
			} else if (filePath == null) {
				conn = H2Helper.getConnection("jdbc:h2:mem:", user, pass);
			} else {
				conn = H2Helper.getConnection("jdbc:h2:" + filePath, user, pass);
				if (conn == null) {
					Log.e(TAG, "Couldn't open " + filePath + " for writing (will try read-only)");
				}
			}

			onConfigure(conn);

			final int version = H2Helper.getVersion(conn);
			if (version != mNewVersion) {
				if (H2Helper.isReadOnly(conn)) {
					throw new IllegalStateException("Can't upgrade read-only database from version " + version + " to "
							+ mNewVersion + ": " + filePath);
				}

				if (version > 0 && version < mMinimumSupportedVersion) {
					File databaseFile = new File(filePath);
					onBeforeDelete(conn);
					H2Helper.releaseConnection(conn);

					if (databaseFile.delete()) {
						mIsInitializing = false;
						return getDatabaseLocked(writable);
					} else {
						throw new IllegalStateException(
								"Unable to delete obsolete database " + filePath + " with version " + version);
					}
				} else {
					if (version == 0) {
						onCreate(conn);
					} else {
						if (version > mNewVersion) {
							onDowngrade(conn, version, mNewVersion);
						} else {
							onUpgrade(conn, version, mNewVersion);
						}
					}
					H2Helper.dbUpdateDone(conn, mNewVersion);
				}
			}

			onOpen(conn);

			if (H2Helper.isReadOnly(conn)) {
				Log.w(TAG, "Opened " + filePath + " in read-only mode");
			}

			return conn;
		} finally {
			mIsInitializing = false;
			if (conn != null && conn != mConn) {
				H2Helper.releaseConnection(conn);
				conn = null;
			}
		}
	}

	public synchronized void close() {
		if (mIsInitializing)
			throw new IllegalStateException("Closed during initialization");

		if (mConn != null && H2Helper.isValid(mConn)) {
			H2Helper.releaseConnection(mConn);
			mConn = null;
		}
	}

	public void onConfigure(Connection conn) {
	}

	public void onBeforeDelete(Connection conn) {
	}

	public abstract void onCreate(Connection conn);

	public abstract void onUpgrade(Connection conn, int oldVersion, int newVersion);

	public void onDowngrade(Connection conn, int oldVersion, int newVersion) {
		throw new IllegalStateException("Can't downgrade database from version " + oldVersion + " to " + newVersion);
	}

	public void onOpen(Connection conn) {
	}

}
