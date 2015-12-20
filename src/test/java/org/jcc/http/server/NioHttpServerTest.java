package org.jcc.http.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class NioHttpServerTest {

	public static void main(String args[]) throws Exception {
		long startTime = System.currentTimeMillis();
		InputStream inputStream = null;
		URL url = new URL("http://localhost:8400/");
		URLConnection connection = url.openConnection();
		inputStream = connection.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			System.out.println(line);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime) + "ms");
	}
}