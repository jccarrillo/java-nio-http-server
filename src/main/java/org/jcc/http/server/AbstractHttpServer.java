package org.jcc.http.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpServer implements HttpServer {

	protected Logger logger = LoggerFactory.getLogger(HttpServer.class);

	private InetSocketAddress inetSocketAddress;

	/**
	 * Create a new server and immediately binds it.
	 *
	 * @param address
	 *            the address to bind on
	 */
	public AbstractHttpServer(InetSocketAddress address) {
		this.inetSocketAddress = address;
	}

	public InetSocketAddress getInetSocketAddress() {
		return inetSocketAddress;
	}

	public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
		this.inetSocketAddress = inetSocketAddress;
	}

	@Override
	public void init() throws IOException {
		this.logger.debug("Simple name: {}", getClass().getSimpleName());
	}

	@Override
	public void start() throws IOException {

	}

	@Override
	public void stop() throws IOException {

	}

	@Override
	public final void run() {
		try {
			this.logger.info("Initializing");
			init();
			this.logger.info("Initialized");
			this.logger.info("Starting [{}]", this.inetSocketAddress);
			start();
		} catch (IOException ex) {
			// call it quits
			try {
				stop();
			} catch (IOException e) {
				throw new RuntimeException(ex);
			}

			throw new RuntimeException(ex);
		}
		this.logger.info("Stopped [{}]", this.inetSocketAddress);
	}
}