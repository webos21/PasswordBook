package com.gmail.webos21.pb;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.gmail.webos21.pb.web.NanoHTTPD.IHTTPSession;
import com.gmail.webos21.pb.web.NanoHTTPD.Response.Status;
import com.gmail.webos21.pb.web.RouteResult;
import com.gmail.webos21.pb.web.UriHandler;

public class LoginHandler implements UriHandler {

	public static int DEF_AES_BYTELEN = 32;

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

			Encoder base64enc = Base64.getEncoder();
			validPassword = base64enc.encodeToString(encryptBytes);

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
		System.out.println("URI : " + uri);

		StringBuilder sb = new StringBuilder();

		@SuppressWarnings("deprecation")
		Map<String, String> parms = session.getParms();
		String pbpwd = parms.get("pbpwd");

		System.out.println("validPassword = " + validPassword);
		System.out.println("pbpwd         = " + pbpwd);

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

		rr.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		rr.addHeader("Access-Control-Allow-Credentials", "true");
		rr.addHeader("Access-Control-Allow-Headers", "true");
		rr.addHeader("Access-Control-Allow-Methods", "GET,DELETE,POST,PUT,HEAD,OPTIONS");
		rr.addHeader("Access-Control-Max-Age", "86400");

		RouteResult.print(rr);
		System.out.println(sb.toString());

		return rr;
	}

}
