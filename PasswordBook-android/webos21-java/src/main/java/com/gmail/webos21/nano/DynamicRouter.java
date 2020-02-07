package com.gmail.webos21.nano;

import java.util.HashMap;
import java.util.Map;

import com.gmail.webos21.nano.NanoHTTPD.IHTTPSession;

public class DynamicRouter {

	private HashMap<String, Class<? extends UriHandler>> dynamicMap;

	public DynamicRouter() {
		dynamicMap = new HashMap<String, Class<? extends UriHandler>>();
	}

	public RouteResult route(Map<String, String> headers, IHTTPSession session, String uri, Map<String, String> files) {
		RouteResult res = null;

		Class<? extends UriHandler> clazz = dynamicMap.get(uri);
		if (clazz != null) {
			try {
				UriHandler h = clazz.newInstance();
				res = h.process(headers, session, uri, files);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return res;
	}

	public void addDynamicPage(String uri, Class<? extends UriHandler> clazz) {
		dynamicMap.put(uri, clazz);
	}

}
