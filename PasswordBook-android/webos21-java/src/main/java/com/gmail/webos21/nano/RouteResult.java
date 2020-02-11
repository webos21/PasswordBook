package com.gmail.webos21.nano;

import com.gmail.webos21.nano.NanoHTTPD.ContentType;
import com.gmail.webos21.nano.NanoHTTPD.Response.IStatus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RouteResult {

    private final Map<String, String> header = new HashMap<String, String>();
    private IStatus status;
    private String mimeType;
    private InputStream data;
    private long contentLength;

    public RouteResult(IStatus status, String mimeType, InputStream data, long totalBytes) {
        this.status = status;
        this.mimeType = mimeType;
        this.data = data;
        this.contentLength = totalBytes;
    }

    public static RouteResult newRouteResult(IStatus status, String mimeType, InputStream data, long totalBytes) {
        return new RouteResult(status, mimeType, data, totalBytes);
    }

    public static RouteResult newRouteResult(IStatus status, String mimeType, String txt) {
        ContentType contentType = new ContentType(mimeType);
        if (txt == null) {
            return newRouteResult(status, mimeType, new ByteArrayInputStream(new byte[0]), 0);
        } else {
            byte[] bytes;
            try {
                CharsetEncoder newEncoder = Charset.forName(contentType.getEncoding()).newEncoder();
                if (!newEncoder.canEncode(txt)) {
                    contentType = contentType.tryUTF8();
                }
                bytes = txt.getBytes(contentType.getEncoding());
            } catch (UnsupportedEncodingException e) {
                System.out.println("ERROR : encoding problem, responding nothing");
                e.printStackTrace();
                bytes = new byte[0];
            }
            return newRouteResult(status, contentType.getContentTypeHeader(), new ByteArrayInputStream(bytes),
                    bytes.length);
        }
    }

    public static void print(RouteResult r) {
        StringBuilder sb = new StringBuilder();
        RouteResult.print(r, sb);
        System.out.println(sb.toString());
    }

    public static void print(RouteResult r, StringBuilder sb) {
        if (r == null) {
            sb.append("RouteResult = null");
            return;
        }

        sb.append("=================================\n");
        sb.append("* Status   : ").append(r.getStatus().getDescription()).append('\n');
        sb.append("* MimeType : ").append(r.getMimeType()).append('\n');
        sb.append("---------------------------------\n");
        sb.append("* Headers\n");
        Map<String, String> headers = r.getHeaders();
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            sb.append("  ").append(key).append(": ").append(headers.get(key)).append('\n');
        }
        sb.append("---------------------------------\n");
        sb.append("* BodyLen  : ").append(r.getContentLength()).append('\n');
        sb.append("=================================\n");
    }

    public IStatus getStatus() {
        return status;
    }

    public void setStatus(IStatus status) {
        this.status = status;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public void addHeader(String name, String value) {
        this.header.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return header;
    }

}
