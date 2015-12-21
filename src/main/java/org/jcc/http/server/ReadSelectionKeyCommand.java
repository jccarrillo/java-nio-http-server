package org.jcc.http.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the {@link SelectionKey}. When done reading, the {@link SelectionKey}
 * is set to Write.
 */
public class ReadSelectionKeyCommand implements SelectionKeyCommand {

	private Logger logger = LoggerFactory.getLogger(ReadSelectionKeyCommand.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jcc.http.server.SelectionKeyCommand#execute(java.nio.channels.
	 * SelectionKey)
	 */
	@Override
	public void execute(SelectionKey selectionKey) throws IOException {
		boolean readable = selectionKey.isReadable();
		if (!readable) {
			this.logger.debug("Not readable");
			return;
		}
		this.logger.debug("Reading selection key");
		// get the httpRequestHandler
		HttpRequestHandler httpRequestHandler = (HttpRequestHandler) selectionKey.attachment();
		this.logger.debug("Handler [{}]", httpRequestHandler);
		// create it if it doesn't exist
		if (httpRequestHandler == null) {
			this.logger.debug("Creating Handler");
			httpRequestHandler = new HttpRequestHandlerBuilder().build();
			selectionKey.attach(httpRequestHandler);
		}
		httpRequestHandler = (HttpRequestHandler) selectionKey.attachment();
		this.logger.debug("Handler [{}]", httpRequestHandler);
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		// get more data
		try {
			httpRequestHandler.read(socketChannel);
		} catch (IOException e) {
			// nothing to read
			socketChannel.close();
			selectionKey.cancel();
		}
		// decode the message
		String line;
		while ((line = httpRequestHandler.line()) != null) {
			// check if we have got everything
			if (StringUtils.isNotEmpty(line)) {
				continue;
			}
			// write the next round
			selectionKey.interestOps(SelectionKey.OP_WRITE);
		}
		this.logger.debug("Read selection key");
	}
}