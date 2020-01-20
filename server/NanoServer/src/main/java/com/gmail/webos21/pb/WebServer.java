package com.gmail.webos21.pb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

	protected List<File> rootDirs;

	public WebServer(String ipaddr, int port, File wwwroot) throws IOException {
		super(ipaddr, port);

		rootDirs = new ArrayList<File>(Collections.singletonList(wwwroot));

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
		Map<String, String> header = session.getHeaders();
		Map<String, List<String>> parms = session.getParameters();
		String uri = session.getUri();

		if (Consts.DEBUG) {
			System.out.println(session.getMethod() + " '" + uri + "' ");

			Iterator<String> e = header.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
			}
			e = parms.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				System.out.println("  PRM: '" + value + "' = '" + parms.get(value).get(0) + "'");
			}
		}

		for (File homeDir : this.rootDirs) {
			// Make sure we won't die of an exception later
			if (!homeDir.isDirectory()) {
				return getInternalErrorResponse("given path is not a directory (" + homeDir + ").");
			}
		}
		return respond(Collections.unmodifiableMap(header), session, uri);
	}

	/**
	 * Serves file from homeDir and its' subdirectories (only). Uses only URI,
	 * ignores all headers and HTTP parameters.
	 */
	Response serveFile(String uri, Map<String, String> header, File file, String mime) {
		Response res;
		try {
			// Calculate etag
			String etag = Integer
					.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

			// Support (simple) skipping:
			long startFrom = 0;
			long endAt = -1;
			String range = header.get("range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					try {
						if (minus > 0) {
							startFrom = Long.parseLong(range.substring(0, minus));
							endAt = Long.parseLong(range.substring(minus + 1));
						}
					} catch (NumberFormatException ignored) {
					}
				}
			}

			// get if-range header. If present, it must match etag or else we
			// should ignore the range request
			String ifRange = header.get("if-range");
			boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

			String ifNoneMatch = header.get("if-none-match");
			boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null
					&& ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

			// Change return code and add Content-Range header when skipping is
			// requested
			long fileLen = file.length();

			if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
				// range request that matches current etag
				// and the startFrom of the range is satisfiable
				if (headerIfNoneMatchPresentAndMatching) {
					// range request that matches current etag
					// and the startFrom of the range is satisfiable
					// would return range from file
					// respond with not-modified
					res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
					res.addHeader("ETag", etag);
				} else {
					if (endAt < 0) {
						endAt = fileLen - 1;
					}
					long newLen = endAt - startFrom + 1;
					if (newLen < 0) {
						newLen = 0;
					}

					FileInputStream fis = new FileInputStream(file);
					fis.skip(startFrom);

					res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
					res.addHeader("Accept-Ranges", "bytes");
					res.addHeader("Content-Length", "" + newLen);
					res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
					res.addHeader("ETag", etag);
				}
			} else {

				if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
					// return the size of the file
					// 4xx responses are not trumped by if-none-match
					res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
					res.addHeader("Content-Range", "bytes */" + fileLen);
					res.addHeader("ETag", etag);
				} else if (range == null && headerIfNoneMatchPresentAndMatching) {
					// full-file-fetch request
					// would return entire file
					// respond with not-modified
					res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
					res.addHeader("ETag", etag);
				} else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
					// range request that doesn't match current etag
					// would return entire (different) file
					// respond with not-modified

					res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
					res.addHeader("ETag", etag);
				} else {
					// supply the file
					res = newFixedFileResponse(file, mime);
					res.addHeader("Content-Length", "" + fileLen);
					res.addHeader("ETag", etag);
				}
			}
		} catch (IOException ioe) {
			res = getForbiddenResponse("Reading file failed.");
		}

		return res;
	}

	private boolean canServeUri(String uri, File homeDir) {
		boolean canServeUri;
		File f = new File(homeDir, uri);
		canServeUri = f.exists();
		return canServeUri;
	}

	/**
	 * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
	 * instead of '+'.
	 */
	private String encodeUri(String uri) {
		String newUri = "";
		StringTokenizer st = new StringTokenizer(uri, "/ ", true);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if ("/".equals(tok)) {
				newUri += "/";
			} else if (" ".equals(tok)) {
				newUri += "%20";
			} else {
				try {
					newUri += URLEncoder.encode(tok, "UTF-8");
				} catch (UnsupportedEncodingException ignored) {
				}
			}
		}
		return newUri;
	}

	private String findIndexFileInDirectory(File directory) {
		for (String fileName : Consts.INDEX_FILE_NAMES) {
			File indexFile = new File(directory, fileName);
			if (indexFile.isFile()) {
				return fileName;
			}
		}
		return null;
	}

	private Response newFixedFileResponse(File file, String mime) throws FileNotFoundException {
		Response res;
		res = newFixedLengthResponse(Response.Status.OK, mime, new FileInputStream(file), (int) file.length());
		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

	protected Response getForbiddenResponse(String s) {
		return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
	}

	protected Response getInternalErrorResponse(String s) {
		return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERROR: " + s);
	}

	protected Response getNotFoundResponse() {
		return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
				"Error 404, file not found.");
	}

	protected Response addCORSHeaders(Map<String, String> queryHeaders, Response resp, String cors) {
		resp.addHeader("Access-Control-Allow-Origin", cors);
		resp.addHeader("Access-Control-Allow-Headers", calculateAllowHeaders(queryHeaders));
		resp.addHeader("Access-Control-Allow-Credentials", "true");
		resp.addHeader("Access-Control-Allow-Methods", Consts.ALLOWED_METHODS);
		resp.addHeader("Access-Control-Max-Age", "" + Consts.MAX_AGE);

		return resp;
	}

	protected String listDirectory(String uri, File f) {
		String heading = "Directory " + uri;
		StringBuilder msg = new StringBuilder("<html><head><title>" + heading + "</title><style><!--\n"
				+ "span.dirname { font-weight: bold; }\n" + "span.filesize { font-size: 75%; }\n" + "// -->\n"
				+ "</style>" + "</head><body><h1>" + heading + "</h1>");

		String up = null;
		if (uri.length() > 1) {
			String u = uri.substring(0, uri.length() - 1);
			int slash = u.lastIndexOf('/');
			if (slash >= 0 && slash < u.length()) {
				up = uri.substring(0, slash + 1);
			}
		}

		List<String> files = Arrays.asList(f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isFile();
			}
		}));
		Collections.sort(files);

		List<String> directories = Arrays.asList(f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		}));
		Collections.sort(directories);

		if (up != null || directories.size() + files.size() > 0) {
			msg.append("<ul>");
			if (up != null || directories.size() > 0) {
				msg.append("<section class=\"directories\">");
				if (up != null) {
					msg.append("<li><a rel=\"directory\" href=\"").append(up)
							.append("\"><span class=\"dirname\">..</span></a></li>");
				}
				for (String directory : directories) {
					String dir = directory + "/";
					msg.append("<li><a rel=\"directory\" href=\"").append(encodeUri(uri + dir))
							.append("\"><span class=\"dirname\">").append(dir).append("</span></a></li>");
				}
				msg.append("</section>");
			}
			if (files.size() > 0) {
				msg.append("<section class=\"files\">");
				for (String file : files) {
					msg.append("<li><a href=\"").append(encodeUri(uri + file)).append("\"><span class=\"filename\">")
							.append(file).append("</span></a>");
					File curFile = new File(f, file);
					long len = curFile.length();
					msg.append("&nbsp;<span class=\"filesize\">(");
					if (len < 1024) {
						msg.append(len).append(" bytes");
					} else if (len < 1024 * 1024) {
						msg.append(len / 1024).append(".").append(len % 1024 / 10 % 100).append(" KB");
					} else {
						msg.append(len / (1024 * 1024)).append(".").append(len % (1024 * 1024) / 10000 % 100)
								.append(" MB");
					}
					msg.append(")</span></li>");
				}
				msg.append("</section>");
			}
			msg.append("</ul>");
		}
		msg.append("</body></html>");

		return msg.toString();
	}

	private String calculateAllowHeaders(Map<String, String> queryHeaders) {
		// here we should use the given asked headers
		// but NanoHttpd uses a Map whereas it is possible for requester to send
		// several time the same header
		// let's just use default values for this version
		return System.getProperty(Consts.ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME, Consts.DEFAULT_ALLOWED_HEADERS);
	}

	private Response respond(Map<String, String> headers, IHTTPSession session, String uri) {
		// First let's handle CORS OPTION query
		Response r;
		if (Consts.CROSS_ORIGIN != null && Method.OPTIONS.equals(session.getMethod())) {
			r = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, null, 0);
		} else {
			r = defaultRespond(headers, session, uri);
		}

		if (Consts.CROSS_ORIGIN != null) {
			r = addCORSHeaders(headers, r, Consts.CROSS_ORIGIN);
		}
		return r;
	}

	private Response defaultRespond(Map<String, String> headers, IHTTPSession session, String uri) {
		// Remove URL arguments
		uri = uri.trim().replace(File.separatorChar, '/');
		if (uri.indexOf('?') >= 0) {
			uri = uri.substring(0, uri.indexOf('?'));
		}

		// Prohibit getting out of current directory
		if (uri.contains("../")) {
			return getForbiddenResponse("Won't serve ../ for security reasons.");
		}

		boolean canServeUri = false;
		File homeDir = null;
		for (int i = 0; !canServeUri && i < this.rootDirs.size(); i++) {
			homeDir = this.rootDirs.get(i);
			canServeUri = canServeUri(uri, homeDir);
		}
		if (!canServeUri) {
			return getNotFoundResponse();
		}

		// Browsers get confused without '/' after the directory, send a
		// redirect.
		File f = new File(homeDir, uri);
		if (f.isDirectory() && !uri.endsWith("/")) {
			uri += "/";
			Response res = newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML,
					"<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>");
			res.addHeader("Location", uri);
			return res;
		}

		if (f.isDirectory()) {
			// First look for index files (index.html, index.htm, etc) and if
			// none found, list the directory if readable.
			String indexFile = findIndexFileInDirectory(f);
			if (indexFile == null) {
				if (f.canRead()) {
					// No index file, list the directory if it is readable
					return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, listDirectory(uri, f));
				} else {
					return getForbiddenResponse("No directory listing.");
				}
			} else {
				return respond(headers, session, uri + indexFile);
			}
		}
		String mimeTypeForFile = getMimeTypeForFile(uri);
		Response response = serveFile(uri, headers, f, mimeTypeForFile);

		return response != null ? response : getNotFoundResponse();
	}

}
