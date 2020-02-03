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

		int dbVersion = -1;
		Connection conn = H2Helper.getConnection(JDBC_URL, USER, PASS);
		if (conn != null) {
			dbVersion = H2Helper.getVersion(conn);
			System.out.println("DB Version = " + dbVersion);
			H2Helper.releaseConnection(conn);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<!doctype html><html lang=\"ko\"><head><title>Dynamic Test</title>");
		sb.append("<body>Hello World!!<br/>");
		sb.append("DB Version : ").append(dbVersion);
		sb.append("</body></html>");

		return RouteResult.newRouteResult(Status.OK, NanoHTTPD.MIME_HTML, sb.toString());
	}

}
