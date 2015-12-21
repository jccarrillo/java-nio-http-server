package org.jcc.http.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the {@link SelectionKey} to read, if its {@link SocketChannel} can
 * be accepted.
 */
public class AcceptSelectionKeyCommand implements SelectionKeyCommand {

	private Logger logger = LoggerFactory.getLogger(AcceptSelectionKeyCommand.class);
	private Selector selector;

	public AcceptSelectionKeyCommand(Selector selector) {
		this.selector = selector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jcc.http.server.SelectionKeyCommand#execute(java.nio.channels.
	 * SelectionKey)
	 */
	@Override
	public void execute(SelectionKey selectionKey) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		if (socketChannel == null) {
			this.logger.debug("Disregard selection key");
			return;
		}
		this.logger.debug("Accepting selection key");
		socketChannel.configureBlocking(false);
		socketChannel.register(this.selector, SelectionKey.OP_READ);
		this.logger.debug("Accepted selection key");
	}
}