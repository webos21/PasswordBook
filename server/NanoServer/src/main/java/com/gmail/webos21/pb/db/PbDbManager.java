package com.gmail.webos21.pb.db;

import com.gmail.webos21.pb.Consts;

public class PbDbManager {

	private static volatile PbDbManager instance;

	private PbDbHelper dbHelper;

	private PbDbManager() {
	}

	public static PbDbManager getInstance() {
		if (instance != null) {
			return instance;
		}
		synchronized (PbDbManager.class) {
			if (instance != null) {
				return instance;
			}
			instance = new PbDbManager();
		}
		return instance;
	}

	public void init() {
		this.dbHelper = new PbDbHelper(Consts.DB_FILE, Consts.DB_USER, Consts.DB_PASS, Consts.DB_VERSION);
	}

	public void destroy() {
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
		if (instance != null) {
			instance = null;
		}
	}

	public PbDbInterface getPbDbInterface() {
		return this.dbHelper;
	}
}
