package com.gmail.webos21.passwordbook.web;

import android.util.Log;

import com.gmail.webos21.crypto.Base64;
import com.gmail.webos21.nano.NanoHTTPD.IHTTPSession;
import com.gmail.webos21.nano.NanoHTTPD.Response.IStatus;
import com.gmail.webos21.nano.NanoHTTPD.Response.Status;
import com.gmail.webos21.nano.RouteResult;
import com.gmail.webos21.nano.UriHandler;
import com.gmail.webos21.passwordbook.Consts;
import com.gmail.webos21.passwordbook.db.PbDbInterface;
import com.gmail.webos21.passwordbook.db.PbDbManager;
import com.gmail.webos21.passwordbook.db.PbRow;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PbDataHandler implements UriHandler {
    private static final String TAG = PbDataHandler.class.getSimpleName();

    private PbDbInterface pdbi;

    public PbDataHandler() {
        PbDbManager pdb = PbDbManager.getInstance();
        pdbi = pdb.getPbDbInterface();
    }

    @Override
    public RouteResult process(Map<String, String> headers, IHTTPSession session, String uri,
                               Map<String, String> files) {
        Log.d(TAG, "\n\n=========================================\n");
        Log.d(TAG, "Response) " + session.getMethod() + " " + uri);

        switch (session.getMethod()) {
            case OPTIONS:
                return processSimple(Status.OK);
            case POST:
                return processPost(headers, session, uri, files);
            case GET:
                return processGet(headers, session, uri, files);
            case PUT:
                return processPut(headers, session, uri, files);
            case DELETE:
                return processDelete(headers, session, uri);
            default:
                return processSimple(Status.METHOD_NOT_ALLOWED);
        }
    }

    private void addCorsHeader(RouteResult rr) {
        rr.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        rr.addHeader("Access-Control-Allow-Credentials", "true");
        rr.addHeader("Access-Control-Allow-Headers", "origin,accept,content-type,authorization");
        rr.addHeader("Access-Control-Allow-Methods", "GET,DELETE,POST,PUT,HEAD,OPTIONS");
        rr.addHeader("Access-Control-Max-Age", "86400");
    }

    private RouteResult processSimple(IStatus status) {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"result\": ").append(status.getRequestStatus()).append(",\n");
        sb.append("  \"description\": \"").append(status).append("\"\n");
        sb.append("}\n");

        RouteResult rr = RouteResult.newRouteResult(status, "application/json", sb.toString());
        addCorsHeader(rr);

        RouteResult.print(rr);
        Log.d(TAG, sb.toString());

        return rr;
    }

    private RouteResult processDelete(Map<String, String> headers, IHTTPSession session, String uri) {
        Map<String, String> reqHeaders = session.getHeaders();
        String authVal = reqHeaders.get("authorization");
        if (authVal == null) {
            return processSimple(Status.UNAUTHORIZED);
        }
        String[] authArr = authVal.split(" ");
        if (authArr == null || authArr.length != 2) {
            return processSimple(Status.UNAUTHORIZED);
        }

        String auth = new String(Base64.decode(authArr[1], Base64.DEFAULT));
        Log.d(TAG, "auth = " + auth);
        if (!"username:password".equals(auth)) {
            return processSimple(Status.UNAUTHORIZED);
        }

        @SuppressWarnings("deprecation")
        Map<String, String> params = session.getParms();

        for (String key : params.keySet()) {
            Log.d(TAG, key + " = " + params.get(key));
        }

        String siteId = params.get("siteId");
        int deletedRows = pdbi.deleteRow(Long.parseLong(siteId));

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"result\": \"OK\",\n");
        sb.append("  \"deletedRows\": ").append(deletedRows);
        sb.append("}\n");

        RouteResult rr = RouteResult.newRouteResult(Status.OK, "application/json", sb.toString());
        addCorsHeader(rr);

        RouteResult.print(rr);
        Log.d(TAG, sb.toString());

        return rr;
    }

    private RouteResult processPut(Map<String, String> headers, IHTTPSession session, String uri,
                                   Map<String, String> files) {
        Map<String, String> reqHeaders = session.getHeaders();
        String authVal = reqHeaders.get("authorization");
        if (authVal == null) {
            return processSimple(Status.UNAUTHORIZED);
        }
        String[] authArr = authVal.split(" ");
        if (authArr == null || authArr.length != 2) {
            return processSimple(Status.UNAUTHORIZED);
        }

        String auth = new String(Base64.decode(authArr[1], Base64.DEFAULT));
        Log.d(TAG, "auth = " + auth);
        if (!"username:password".equals(auth)) {
            return processSimple(Status.UNAUTHORIZED);
        }

        @SuppressWarnings("deprecation")
        Map<String, String> params = session.getParms();

        for (String key : params.keySet()) {
            Log.d(TAG, key + " = " + params.get(key));
        }

        String siteId = params.get("siteId");
        String siteUrl = params.get("siteUrl");
        String siteName = params.get("siteName");
        String siteType = params.get("siteType");
        String myId = params.get("myId");
        String myPw = params.get("myPw");
        String regDate = params.get("regDate");
        String memo = params.get("memo");

        Date rd = null;
        try {
            rd = Consts.SDF_DATE.parse(regDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PbRow aRow = new PbRow(Long.parseLong(siteId), siteUrl, siteName, siteType, myId, myPw, rd.getTime(),
                System.currentTimeMillis(), memo);
        pdbi.updateRow(aRow);

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"result\": \"OK\",\n");
        sb.append("  \"data\": [\n");
        sb.append(aRow.toJson());
        sb.append("  ]\n");
        sb.append("}\n");

        RouteResult rr = RouteResult.newRouteResult(Status.OK, "application/json", sb.toString());
        addCorsHeader(rr);

        RouteResult.print(rr);
        Log.d(TAG, sb.toString());

        return rr;
    }

    private RouteResult processPost(Map<String, String> headers, IHTTPSession session, String uri,
                                    Map<String, String> files) {
        Map<String, String> reqHeaders = session.getHeaders();
        String authVal = reqHeaders.get("authorization");
        if (authVal == null) {
            return processSimple(Status.UNAUTHORIZED);
        }
        String[] authArr = authVal.split(" ");
        if (authArr == null || authArr.length != 2) {
            return processSimple(Status.UNAUTHORIZED);
        }

        String auth = new String(Base64.decode(authArr[1], Base64.DEFAULT));
        Log.d(TAG, "auth = " + auth);
        if (!"username:password".equals(auth)) {
            return processSimple(Status.UNAUTHORIZED);
        }

        @SuppressWarnings("deprecation")
        Map<String, String> params = session.getParms();

        for (String key : params.keySet()) {
            Log.d(TAG, key + " = " + params.get(key));
        }

        String siteUrl = params.get("siteUrl");
        String siteName = params.get("siteName");
        String siteType = params.get("siteType");
        String myId = params.get("myId");
        String myPw = params.get("myPw");
        String regDate = params.get("regDate");
        String memo = params.get("memo");

        Date rd = null;
        try {
            rd = Consts.SDF_DATE.parse(regDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PbRow aRow = new PbRow(null, siteUrl, siteName, siteType, myId, myPw, rd.getTime(), System.currentTimeMillis(),
                memo);
        pdbi.updateRow(aRow);

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"result\": \"OK\",\n");
        sb.append("  \"data\": [\n");
        sb.append(aRow.toJson());
        sb.append("  ]\n");
        sb.append("}\n");

        RouteResult rr = RouteResult.newRouteResult(Status.OK, "application/json", sb.toString());
        addCorsHeader(rr);

        RouteResult.print(rr);
        Log.d(TAG, sb.toString());

        return rr;
    }

    private RouteResult processGet(Map<String, String> headers, IHTTPSession session, String uri,
                                   Map<String, String> files) {
        Map<String, String> reqHeaders = session.getHeaders();
        String authVal = reqHeaders.get("authorization");
        if (authVal == null) {
            return processSimple(Status.UNAUTHORIZED);
        }
        String[] authArr = authVal.split(" ");
        if (authArr == null || authArr.length != 2) {
            return processSimple(Status.UNAUTHORIZED);
        }

        String auth = new String(Base64.decode(authArr[1], Base64.DEFAULT));
        Log.d(TAG, "auth = " + auth);
        if (!"username:password".equals(auth)) {
            return processSimple(Status.UNAUTHORIZED);
        }

        @SuppressWarnings("deprecation")
        String keyword = session.getParms().get("q");

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"result\": \"OK\",\n");
        sb.append("  \"data\": [\n");

        List<PbRow> rows = null;
        if (keyword != null && keyword.length() > 0) {
            rows = pdbi.findRows(keyword);
        } else {
            rows = pdbi.findRows();
        }

        int i = 0;
        for (PbRow r : rows) {
            if (i == 0) {
                sb.append(r.toJson()).append('\n');
            } else {
                sb.append(',').append(r.toJson()).append('\n');
            }
            i++;
        }

        sb.append("  ]\n");
        sb.append("}\n");

        RouteResult rr = RouteResult.newRouteResult(Status.OK, "application/json", sb.toString());
        addCorsHeader(rr);

        RouteResult.print(rr);
        Log.d(TAG, sb.toString());

        return rr;
    }

}
