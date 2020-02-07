package com.gmail.webos21.passwordbook.web;

import android.util.Log;

import com.gmail.webos21.crypto.Base64;
import com.gmail.webos21.nano.NanoHTTPD;
import com.gmail.webos21.nano.NanoHTTPD.IHTTPSession;
import com.gmail.webos21.nano.NanoHTTPD.Response.Status;
import com.gmail.webos21.nano.RouteResult;
import com.gmail.webos21.nano.UriHandler;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class LoginHandler implements UriHandler {

    private static final String TAG = LoginHandler.class.getSimpleName();

    private static String ALG_AES_KEY = "AES";

    private static String ALG_AES_CIPHER = "AES/CBC/PKCS5Padding";

    private static String PLAIN_PASSWORD = "test1234";

    private String validPassword;

    public LoginHandler() {
        byte[] keyBytes = "PasswordBook".getBytes();
        byte[] iv = "PasswordBook1234".getBytes();

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] aesKeyBytes = sha256.digest(keyBytes);

            SecretKeySpec secretKey = new SecretKeySpec(aesKeyBytes, ALG_AES_KEY);

            Cipher cipher = Cipher.getInstance(ALG_AES_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            cipher.update(PLAIN_PASSWORD.getBytes());
            byte[] encryptBytes = cipher.doFinal();

            validPassword = Base64.encodeToString(encryptBytes, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RouteResult process(Map<String, String> headers, IHTTPSession session, String uri,
                               Map<String, String> files) {
        Log.d(TAG, "\n=========================================\n");
        Log.d(TAG, "Response) " + session.getMethod() + " " + uri);

        switch (session.getMethod()) {
            case OPTIONS:
                return processSimple(Status.OK);
            case POST:
                return processPost(headers, session, uri, files);
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

    private RouteResult processSimple(NanoHTTPD.Response.IStatus status) {
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

    private RouteResult processPost(Map<String, String> headers, IHTTPSession session, String uri,
                                    Map<String, String> files) {
        StringBuilder sb = new StringBuilder();

        @SuppressWarnings("deprecation")
        Map<String, String> parms = session.getParms();
        String pbpwd = parms.get("pbpwd");

        if (pbpwd == null || pbpwd.length() == 0) {
            return processSimple(Status.BAD_REQUEST);
        }

        RouteResult rr = null;
        if (validPassword.equals(pbpwd)) {
            sb.append("{\n");
            sb.append("  \"result\": \"OK\",\n");
            sb.append("  \"auth\": {\n");
            sb.append("    \"ckey\": \"X-PB-AUTH\",\n");
            sb.append("    \"cval\": \"test\"\n");
            sb.append("  }\n");
            sb.append("}\n");

            rr = RouteResult.newRouteResult(Status.OK, "application/json", sb.toString());
            rr.addHeader("Set-Cookies", "X-PB-AUTH=test; Domain=localhost:3000");
        } else {
            sb.append("{\n");
            sb.append("  \"result\": \"FAIL\"\n");
            sb.append("}\n");
            rr = RouteResult.newRouteResult(Status.UNAUTHORIZED, "application/json", sb.toString());
            rr.addHeader("Set-Cookies", "X-PB-AUTH=;");
        }
        addCorsHeader(rr);

        RouteResult.print(rr);
        Log.d(TAG, sb.toString());

        return rr;
    }
}
