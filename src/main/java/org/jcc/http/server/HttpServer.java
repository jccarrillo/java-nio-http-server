package org.jcc.http.server;

import java.io.IOException;

public interface HttpServer extends Runnable {

	public static final String KEY_SERVER_PORT = "org.jcc.http.server.SERVER_PORT";

	public final static String ADDRESS = "127.0.0.1";

	public final static long TIMEOUT = 10000;

	void init() throws IOException;

	void start() throws IOException;

	void stop() throws IOException;
}