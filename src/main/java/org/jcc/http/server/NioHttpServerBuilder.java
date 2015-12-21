package org.jcc.http.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.Builder;

public class NioHttpServerBuilder implements Builder<HttpServer> {

	private final InetSocketAddress inetSocketAddress;

	public NioHttpServerBuilder(InetSocketAddress inetSocketAddress) {
		this.inetSocketAddress = inetSocketAddress;
	}

	@Override
	public HttpServer build() {
		return new NioHttpServer();
	}

	private class NioHttpServer extends AbstractHttpServer {

		private Selector selector;
		private ServerSocketChannel serverSocketChannel;
		private SelectionKeyCommandFactory selectionKeyCommandFactory;

		private NioHttpServer() {
			super(inetSocketAddress);
		}

		@Override
		public void init() throws IOException {
			super.init();
			this.serverSocketChannel = ServerSocketChannel.open();
			this.serverSocketChannel.configureBlocking(false);
			this.logger.info("NIO configured");
			ServerSocket serverSocket = this.serverSocketChannel.socket();
			InetSocketAddress inetSocketAddress = getInetSocketAddress();
			serverSocket.bind(inetSocketAddress, 100);
			this.logger.info("Binded to {}", inetSocketAddress);
			SelectorProvider selectorProvider = SelectorProvider.provider();
			this.selector = selectorProvider.openSelector();
			this.selectionKeyCommandFactory = new SelectionKeyCommandFactory(this.selector);
			this.logger.info("Selector created");
			this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.AbstractHttpServer#start()
		 */
		@Override
		public final void start() {
			this.logger.info("Now accepting connections...");
			try {
				// A run the server as long as the thread is not interrupted.
				Thread currentThread = Thread.currentThread();
				while (!currentThread.isInterrupted()) {
					// wait until connection or time out
					this.selector.select(TIMEOUT);
					// get actions
					Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
					handleSelectionKeys(selectedKeys);
				}
			} catch (IOException ex) {
				stop();
				throw new RuntimeException(ex);
			}
		}

		/**
		 * Handle received selection keys.
		 * 
		 * @param selectionKeys
		 *            the selection keys.
		 * @throws IOException
		 */
		private void handleSelectionKeys(Set<SelectionKey> selectionKeys) throws IOException {
			if (CollectionUtils.isEmpty(selectionKeys)) {
				// nothing happened
				return;
			}
			int size = selectionKeys.size();
			this.logger.info("Handling [{}] selection keys", size);
			// something happened
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();
				iterator.remove();
				this.logger.debug("Remaining [{}] selection keys", selectionKeys.size());
				// remove as to not process again.
				try {
					if (!selectionKey.isValid()) {
						continue;
					}
					SelectionKeyCommand selectionKeyCommand = this.selectionKeyCommandFactory
							.getSelectionKeyCommand(selectionKey);
					selectionKeyCommand.execute(selectionKey);
				} catch (Exception ex) {
					StringBuffer buffer = new StringBuffer("Error [");
					buffer.append(ex.getMessage());
					buffer.append("] on [");
					buffer.append(selectionKey.channel());
					buffer.append("] channel");
					this.logger.error(buffer.toString(), ex);
					SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
					socketChannel.close();
				}
			}
			this.logger.info("Handled [{}] selection keys", size);
		}

		/**
		 * Shutdown this server, preventing it from handling any more requests.
		 */
		@Override
		public final void stop() {
			try {
				selector.close();
				serverSocketChannel.close();
			} catch (IOException ex) {
			}
		}
	}
}