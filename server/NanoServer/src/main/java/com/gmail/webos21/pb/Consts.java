package com.gmail.webos21.pb;

import java.util.ArrayList;
import java.util.List;

public class Consts {

	public static final boolean DEBUG = true;

	public static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
	public static final String DEFAULT_ALLOWED_HEADERS = "origin,accept,content-type";
	public final static String ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME = "AccessControlAllowHeader";

	public static final String CROSS_ORIGIN = "*";

	public static final int MAX_AGE = 42 * 60 * 60;

	/**
	 * Default Index file names.
	 */
	@SuppressWarnings("serial")
	public static final List<String> INDEX_FILE_NAMES = new ArrayList<String>() {
		{
			add("index.html");
			add("index.htm");
		}
	};

}
