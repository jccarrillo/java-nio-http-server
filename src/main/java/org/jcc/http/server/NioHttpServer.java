package org.jcc.http.server;

import java.net.InetSocketAddress;

public class NioHttpServer {

	public static void main(String args[]) {
		// setup the socket we're listening for connections on.
		InetSocketAddress inetSocketAddress = new InetSocketAddress(8400);
		HttpServer httpServer = new NioHttpServerBuilder(inetSocketAddress).build();
		Thread thread = new Thread(httpServer);
		thread.start();
	}
}