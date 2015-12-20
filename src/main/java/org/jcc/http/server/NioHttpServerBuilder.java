package org.jcc.http.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
					LinkedList<SelectionKey> keys = new LinkedList<>(selectedKeys);
					handleSelectionKeys(keys);
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
		private void handleSelectionKeys(LinkedList<SelectionKey> selectionKeys) throws IOException {
			if (CollectionUtils.isEmpty(selectionKeys)) {
				// nothing happened
				return;
			}
			int size = selectionKeys.size();
			this.logger.info("Handling [{}] selection keys", size);
			// something happened
			while (!selectionKeys.isEmpty()) {
				SelectionKey selectionKey = selectionKeys.pop();
				this.logger.info("Handling [{}] selection key", selectionKey.interestOps());
				this.logger.debug("Channel: {}", selectionKey.channel());
				this.logger.debug("Remaining [{}] selection keys", selectionKeys.size());
				// remove as to not process again.
				try {
					if (!selectionKey.isValid()) {
						continue;
					}
					// valid selection key
					if (selectionKey.isAcceptable()) {
						doAccept(selectionKey);
					} else if (selectionKey.isReadable()) {
						doRead(selectionKey);
					} else if (selectionKey.isWritable()) {
						doWrite(selectionKey);
					}
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
		 * Accept the client, if valid.
		 * 
		 * @param selectionKey
		 * @throws IOException
		 * @throws ClosedChannelException
		 */
		private void doAccept(SelectionKey selectionKey) throws IOException, ClosedChannelException {
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
			// accept
			SocketChannel socketChannel = serverSocketChannel.accept();
			if (socketChannel == null) {
				this.logger.debug("Disregard client");
				return;
			}
			this.logger.debug("Accepting client");
			// non blocking
			socketChannel.configureBlocking(false);
			// read from the client
			socketChannel.register(selector, SelectionKey.OP_READ);
			this.logger.debug("Accepted client");
		}

		/**
		 * Reads from the client.
		 * 
		 * @param selectionKey
		 *            the client.
		 * @throws IOException
		 * @throws ClosedChannelException
		 */
		private void doRead(SelectionKey selectionKey) throws IOException {
			boolean readable = selectionKey.isReadable();
			if (!readable) {
				this.logger.debug("Not readable");
				return;
			}
			this.logger.debug("Reading connection");
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
				if (!StringUtils.isEmpty(line)) {
					continue;
				}
				// write the next round
				selectionKey.interestOps(SelectionKey.OP_WRITE);
			}
			this.logger.debug("Read connection");
		}

		/**
		 * Writes to the client.
		 * 
		 * @param selectionKey
		 *            the client.
		 * @throws IOException
		 */
		private void doWrite(SelectionKey selectionKey) throws IOException {
			this.logger.debug("Writing to client");
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
			this.logger.debug("Wrote to client");
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