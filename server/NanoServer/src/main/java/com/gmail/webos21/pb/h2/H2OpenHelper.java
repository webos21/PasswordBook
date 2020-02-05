package com.gmail.webos21.pb.h2;

import java.io.File;

import com.gmail.webos21.pb.db.Log;

public abstract class H2OpenHelper {
	private static final String TAG = H2OpenHelper.class.getSimpleName();

	private final String filePath;
	private final String user;
	private final String pass;

	private final int mNewVersion;
	private final int mMinimumSupportedVersion;

	private H2Database mDatabase;
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

	public H2Database getWritableDatabase() {
		synchronized (this) {
			return getDatabaseLocked(true);
		}
	}

	public H2Database getReadableDatabase() {
		synchronized (this) {
			return getDatabaseLocked(false);
		}
	}

	private H2Database getDatabaseLocked(boolean writable) {
		if (mDatabase != null) {
			if (!mDatabase.isOpen()) {
				// Darn! The user closed the database by calling mDatabase.close().
				mDatabase = null;
			} else if (!writable || !mDatabase.isReadOnly()) {
				// The database is already open for business.
				return mDatabase;
			}
		}

		if (mIsInitializing) {
			throw new IllegalStateException("getDatabase called recursively");
		}

		H2Database db = mDatabase;
		try {
			mIsInitializing = true;

			if (db != null) {
				if (writable && db.isReadOnly()) {
					db.reopenReadWrite();
				}
			} else if (filePath == null) {
				db = H2Database.createInMemory(user, pass);
			} else {
				try {
					db = H2Database.openDatabase(filePath, user, pass);
				} catch (Exception ex) {
					if (writable) {
						throw ex;
					}
					Log.e(TAG, "Couldn't open " + filePath + " for writing (will try read-only):", ex);
				}
			}

			onConfigure(db);

			final int version = db.getVersion();
			if (version != mNewVersion) {
				if (db.isReadOnly()) {
					throw new IllegalStateException("Can't upgrade read-only database from version " + db.getVersion()
							+ " to " + mNewVersion + ": " + filePath);
				}

				if (version > 0 && version < mMinimumSupportedVersion) {
					File databaseFile = new File(db.getFilePath());
					onBeforeDelete(db);
					db.close();
					if (H2Database.deleteDatabase(databaseFile)) {
						mIsInitializing = false;
						return getDatabaseLocked(writable);
					} else {
						throw new IllegalStateException(
								"Unable to delete obsolete database " + filePath + " with version " + version);
					}
				} else {
					db.beginTransaction();
					try {
						if (version == 0) {
							onCreate(db);
						} else {
							if (version > mNewVersion) {
								onDowngrade(db, version, mNewVersion);
							} else {
								onUpgrade(db, version, mNewVersion);
							}
						}
						db.setVersion(mNewVersion);
						db.setTransactionSuccessful();
					} finally {
						db.endTransaction();
					}
				}
			}

			onOpen(db);

			if (db.isReadOnly()) {
				Log.w(TAG, "Opened " + filePath + " in read-only mode");
			}

			mDatabase = db;
			return db;
		} finally {
			mIsInitializing = false;
			if (db != null && db != mDatabase) {
				db.close();
			}
		}
	}

	public synchronized void close() {
		if (mIsInitializing)
			throw new IllegalStateException("Closed during initialization");

		if (mDatabase != null && mDatabase.isOpen()) {
			mDatabase.close();
			mDatabase = null;
		}
	}

	public void onConfigure(H2Database db) {
	}

	public void onBeforeDelete(H2Database db) {
	}

	public abstract void onCreate(H2Database db);

	public abstract void onUpgrade(H2Database db, int oldVersion, int newVersion);

	public void onDowngrade(H2Database db, int oldVersion, int newVersion) {
		throw new IllegalStateException("Can't downgrade database from version " + oldVersion + " to " + newVersion);
	}

	public void onOpen(H2Database db) {
	}

}
