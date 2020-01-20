package com.gmail.webos21.pb;

import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		try {
			File htdoc = new File("../FrontEnd/build/");
			System.out.println("htdoc = " + htdoc.getAbsolutePath());

			new App("0.0.0.0", 28080, htdoc);
			// new WebServer("0.0.0.0", 28080, htdoc);
		} catch (IOException ioe) {
			System.err.println("Couldn't start server:\n" + ioe);
		}
	}
}
