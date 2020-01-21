package com.gmail.webos21.pb;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.gmail.webos21.pb.web.DynamicRouter;
import com.gmail.webos21.pb.web.NanoHTTPD;
import com.gmail.webos21.pb.web.RouteResult;
import com.gmail.webos21.pb.web.StaticRouter;

public class WebServer extends NanoHTTPD {

	private static final boolean DEBUG = true;

	private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";

	private StaticRouter staticRouter;
	private DynamicRouter dynamicRouter;

	public WebServer(String ipaddr, int port, File wwwroot) throws IOException {
		super(ipaddr, port);

		staticRouter = new StaticRouter(wwwroot);
		dynamicRouter = new DynamicRouter();

		dynamicRouter.addDynamicPage("/test.do", TestHandler.class);

		mimeTypes().put("xhtml", "application/xhtml+xml");
		mimeTypes().put("opf", "application/oebps-package+xml");
		mimeTypes().put("ncx", "application/xml");
		mimeTypes().put("epub", "application/epub+zip");
		mimeTypes().put("otf", "application/x-font-otf");
		mimeTypes().put("ttf", "application/x-font-ttf");
		mimeTypes().put("js", "application/javascript");
		mimeTypes().put("svg", "image/svg+xml");

		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

		System.out.println("\nRunning! Point your browsers to http://" + ipaddr + ":" + port + "/ \n");
	}

	@Override
	public Response serve(IHTTPSession session) {
		Map<String, String> files = new HashMap<String, String>();
		Method method = session.getMethod();
		if (Method.PUT.equals(method) || Method.POST.equals(method)) {
			try {
				session.parseBody(files);
			} catch (IOException ioe) {
				return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
						"SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			} catch (ResponseException re) {
				return newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re.getMessage());
			}
		}

		@SuppressWarnings("deprecation")
		Map<String, String> parms = session.getParms();
		parms.put(QUERY_STRING_PARAMETER, session.getQueryParameterString());

		Map<String, String> header = session.getHeaders();
		String uri = session.getUri();

		if (DEBUG) {
			System.out.println(session.getMethod() + " '" + uri + "' ");

			Iterator<String> e = header.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
			}
			e = parms.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				System.out.println("  PRM: '" + value + "' = '" + parms.get(value) + "'");
			}
		}

		RouteResult res;

		res = dynamicRouter.route(header, session, uri, files);
		if (res == null) {
			res = staticRouter.route(header, session, uri);
		}

		return routeToResponse(res);
	}

	private Response routeToResponse(RouteResult res) {
		Response response = newFixedLengthResponse(res.getStatus(), res.getMimeType(), res.getData(),
				res.getContentLength());
		Map<String, String> headers = res.getHeaders();
		Set<String> hKey = headers.keySet();
		for (String hk : hKey) {
			response.addHeader(hk, headers.get(hk));
		}
		return response;
	}

}
