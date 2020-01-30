package com.gmail.webos21.pb;

import java.util.Map;

import com.gmail.webos21.pb.web.NanoHTTPD.IHTTPSession;
import com.gmail.webos21.pb.web.NanoHTTPD.Response.Status;
import com.gmail.webos21.pb.web.RouteResult;
import com.gmail.webos21.pb.web.UriHandler;

public class LoginHandler implements UriHandler {

	public LoginHandler() {

	}

	@Override
	public RouteResult process(Map<String, String> headers, IHTTPSession session, String uri,
			Map<String, String> files) {
		System.out.println("URI : " + uri);

		StringBuilder sb = new StringBuilder();

		@SuppressWarnings("deprecation")
		Map<String, String> parms = session.getParms();
		String pbpwd = parms.get("pbpwd");

		System.out.println("pbpwd = " + pbpwd);

		RouteResult rr = null;
		if ("test".equals(pbpwd)) {
			sb.append("{\n");
			sb.append("  \"result\": \"OK\"\n");
			sb.append("}\n");
			 rr = RouteResult.newRouteResult(Status.OK, "application/json", sb.toString());
		} else {
			sb.append("{\n");
			sb.append("  \"result\": \"FAIL\"\n");
			sb.append("}\n");
			 rr = RouteResult.newRouteResult(Status.UNAUTHORIZED, "application/json", sb.toString());
		}

		rr.addHeader("Access-Control-Allow-Origin", "*");
		rr.addHeader("Access-Control-Allow-Credentials", "true");
		rr.addHeader("Access-Control-Allow-Methods", "GET,DELETE,POST,PUT,HEAD,OPTIONS");
		rr.addHeader("Access-Control-Max-Age", "86400");

		return rr;
	}

}
