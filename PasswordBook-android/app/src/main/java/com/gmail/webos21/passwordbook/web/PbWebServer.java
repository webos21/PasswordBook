package com.gmail.webos21.passwordbook.web;

import com.gmail.webos21.nano.DynamicRouter;
import com.gmail.webos21.nano.NanoHTTPD;
import com.gmail.webos21.nano.RouteResult;
import com.gmail.webos21.nano.StaticRouter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PbWebServer extends NanoHTTPD {

    private static final boolean DEBUG = true;

    private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";

    private StaticRouter staticRouter;
    private DynamicRouter dynamicRouter;

    private UiLog logger;

    private Map<String, Boolean> whiteMap;
    private HttpNewClientListener cl;

    public PbWebServer(String ipaddr, int port, File wwwroot) throws IOException {
        super(ipaddr, port);

        staticRouter = new StaticRouter(wwwroot);
        dynamicRouter = new DynamicRouter();

        dynamicRouter.addDynamicPage("/login.do", LoginHandler.class);
        dynamicRouter.addDynamicPage("/pwdata.do", PbDataHandler.class);

        mimeTypes().put("xhtml", "application/xhtml+xml");
        mimeTypes().put("opf", "application/oebps-package+xml");
        mimeTypes().put("ncx", "application/xml");
        mimeTypes().put("epub", "application/epub+zip");
        mimeTypes().put("otf", "application/x-font-otf");
        mimeTypes().put("ttf", "application/x-font-ttf");
        mimeTypes().put("js", "application/javascript");
        mimeTypes().put("svg", "image/svg+xml");

        whiteMap = new HashMap<String, Boolean>();
    }

    public void setUiLog(UiLog logger) {
        this.logger = logger;
    }

    public void setClientListener(HttpNewClientListener cl) {
        this.cl = cl;
    }

    public void addWhiteList(String ip) {
        whiteMap.put(ip, true);
    }

    private void log(String msg) {
        if (DEBUG && logger != null) {
            logger.log(msg);
        }
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

        String rIp = session.getRemoteIpAddress();
        Boolean p = whiteMap.get(rIp);
        if (p == null || p == false) {
            if (cl != null) {
                cl.onNewClientRequest(rIp);
            }
            return newFixedLengthResponse(Response.Status.UNAUTHORIZED, NanoHTTPD.MIME_PLAINTEXT, "Granting is processing on App!!");
        }

        if (DEBUG) {
            log("\n\n" + session.getMethod() + " '" + uri + "' ");

            Iterator<String> e = header.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                log("  HDR: '" + value + "' = '" + header.get(value) + "'");
            }
            e = parms.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                log("  PRM: '" + value + "' = '" + parms.get(value) + "'");
            }
        }

        RouteResult res;

        res = dynamicRouter.route(header, session, uri, files);
        if (res == null) {
            StringBuilder sb = new StringBuilder();
            res = staticRouter.route(header, session, uri);
            RouteResult.print(res, sb);
            log(sb.toString());
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
