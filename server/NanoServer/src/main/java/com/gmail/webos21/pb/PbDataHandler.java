package com.gmail.webos21.pb;

import java.util.Iterator;
import java.util.Map;

import com.gmail.webos21.pb.web.NanoHTTPD.CookieHandler;
import com.gmail.webos21.pb.web.NanoHTTPD.IHTTPSession;
import com.gmail.webos21.pb.web.NanoHTTPD.Response.Status;
import com.gmail.webos21.pb.web.RouteResult;
import com.gmail.webos21.pb.web.UriHandler;

public class PbDataHandler implements UriHandler {

	public PbDataHandler() {

	}

	@Override
	public RouteResult process(Map<String, String> headers, IHTTPSession session, String uri,
			Map<String, String> files) {
		System.out.println("URI : " + uri);

		StringBuilder sb = new StringBuilder();

		CookieHandler cookies = session.getCookies();
		String auth = cookies.read("X-PB-AUTH");

		Iterator<String> ckeys = cookies.iterator();
		while (ckeys.hasNext()) {
			String key = ckeys.next();
			System.out.println(key + " = " + cookies.read(key));
		}

		RouteResult rr = null;
		if (auth == null) {
			sb.append("{\n");
			sb.append("  \"result\": \"OK\",\n");
			sb.append("  \"data\": [\n");

			sb.append("  ]\n");
			sb.append("}\n");

			rr = RouteResult.newRouteResult(Status.OK, "application/json", sb.toString());
		} else {
			sb.append("{\n");
			sb.append("  \"result\": \"FAIL\"\n");
			sb.append("}\n");
			rr = RouteResult.newRouteResult(Status.UNAUTHORIZED, "application/json", sb.toString());
		}

		rr.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		rr.addHeader("Access-Control-Allow-Credentials", "true");
		rr.addHeader("Access-Control-Allow-Methods", "GET,DELETE,POST,PUT,HEAD,OPTIONS");
		rr.addHeader("Access-Control-Max-Age", "86400");

		RouteResult.print(rr);
		System.out.println(sb.toString());

		return rr;
	}

}
