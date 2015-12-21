package org.jcc.http.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes the {@link SelectionKey}. When done reading, the {@link SelectionKey}
 * is set to Read.
 */
public class WriteSelectionKeyCommand implements SelectionKeyCommand {

	private Logger logger = LoggerFactory.getLogger(WriteSelectionKeyCommand.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jcc.http.server.SelectionKeyCommand#execute(java.nio.channels.
	 * SelectionKey)
	 */
	@Override
	public void execute(SelectionKey selectionKey) throws IOException {
		this.logger.debug("Writing to selection key");
		// get the httpRequestHandler
		HttpRequestHandler httpRequestHandler = (HttpRequestHandler) selectionKey.attachment();
		if (httpRequestHandler == null) {
			this.logger.debug("Not writing");
			throw new IOException("Can't write");
		}
		this.logger.debug("Handler [{}]", httpRequestHandler);
		StringBuilder stringBuilder = httpRequestHandler.getReadLines();
		String raw = stringBuilder.toString();
		this.logger.debug("Received:\n{}", raw);
		// build request
		HttpRequest request = new HttpRequestBuilder(raw).build();
		HttpResponse httpResponse = new HttpResponseBuilder().build();
		httpResponse.setContent("Hello World!".getBytes());
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		httpRequestHandler.sendResponse(socketChannel, httpResponse);
		// read the next round.
		selectionKey.interestOps(SelectionKey.OP_READ);
		this.logger.debug("Wrote to selection key");
	}
}