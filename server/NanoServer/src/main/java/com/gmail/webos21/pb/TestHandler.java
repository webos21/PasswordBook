package com.gmail.webos21.pb;

import java.sql.Connection;
import java.util.Map;

import com.gmail.webos21.pb.h2.H2Helper;
import com.gmail.webos21.pb.web.NanoHTTPD;
import com.gmail.webos21.pb.web.NanoHTTPD.IHTTPSession;
import com.gmail.webos21.pb.web.NanoHTTPD.Response.Status;
import com.gmail.webos21.pb.web.RouteResult;
import com.gmail.webos21.pb.web.UriHandler;

public class TestHandler implements UriHandler {

	private static final String JDBC_URL = "jdbc:h2:~/test";
	private static final String USER = "sa";
	private static final String PASS = "sa";

	public TestHandler() {

	}

	@Override
	public RouteResult process(Map<String, String> headers, IHTTPSession session, String uri,
			Map<String, String> files) {
		System.out.println("URI : " + uri);

		StringBuilder sb = new StringBuilder();
		sb.append("<!doctype html><html lang=\"ko\"><head><title>Dynamic Test</title>");
		sb.append("<body>Hello World!!</body></html>");

		Connection conn = H2Helper.getConnection(JDBC_URL, USER, PASS);
		if (conn != null) {
			boolean isUpdate = H2Helper.checkDbUpdate(conn, 1);
			System.out.println("DB Update = " + isUpdate);
			H2Helper.releaseConnection(conn);
		}

		return RouteResult.newRouteResult(Status.OK, NanoHTTPD.MIME_HTML, sb.toString());
	}

}
