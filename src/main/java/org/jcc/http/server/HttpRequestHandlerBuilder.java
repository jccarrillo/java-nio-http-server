package org.jcc.http.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.builder.Builder;

public class HttpRequestHandlerBuilder implements Builder<HttpRequestHandler> {

	public HttpRequestHandlerBuilder() {
	}

	@Override
	public HttpRequestHandler build() {
		return new HttpRequestHandlerImpl();
	}

	private class HttpRequestHandlerImpl implements HttpRequestHandler {

		private Charset charset = Charset.forName("UTF-8");
		private CharsetEncoder encoder = charset.newEncoder();
		private final ByteBuffer buffer = ByteBuffer.allocate(2048);
		private final StringBuilder readLines = new StringBuilder();
		private int mark = 0;

		private HttpRequestHandlerImpl() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpSession#readLine()
		 */
		@Override
		public String line() throws IOException {
			StringBuilder sb = new StringBuilder();
			int l = -1;
			while (buffer.hasRemaining()) {
				char c = (char) buffer.get();
				sb.append(c);
				if (c == '\n' && l == '\r') {
					// mark our position
					mark = buffer.position();
					// append to the total
					readLines.append(sb);
					// return with no line separators
					return sb.substring(0, sb.length() - 2);
				}
				l = c;
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpRequestHandler#read(java.nio.channels.
		 * ReadableByteChannel)
		 */
		@Override
		public void read(ReadableByteChannel readableByteChannel) throws IOException {
			int capacity = buffer.capacity();
			buffer.limit(capacity);
			int read = readableByteChannel.read(buffer);
			if (read == -1) {
				throw new IOException("End of stream");
			}
			buffer.flip();
			buffer.position(mark);
		}

		private void writeLine(WritableByteChannel writableByteChannel, String line) throws IOException {
			CharBuffer charBuffer = CharBuffer.wrap(line + "\r\n");
			ByteBuffer byteBuffer = encoder.encode(charBuffer);

			writableByteChannel.write(byteBuffer);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jcc.http.server.HttpRequestHandler#sendResponse(java.nio.channels
		 * .WritableByteChannel, org.jcc.http.server.HttpResponse)
		 */
		@Override
		public void sendResponse(WritableByteChannel writableByteChannel, HttpResponse httpResponse) {
			httpResponse.addDefaultHeaders();
			try {
				String version = httpResponse.getVersion();
				int responseCode = httpResponse.getResponseCode();
				String responseReason = httpResponse.getResponseReason();
				writeLine(writableByteChannel, version + " " + responseCode + " " + responseReason);
				Map<String, String> headers = httpResponse.getHeaders();
				Set<Entry<String, String>> entrySet = headers.entrySet();
				for (Entry<String, String> entry : entrySet) {
					String key = entry.getKey();
					String value = entry.getValue();
					writeLine(writableByteChannel, key + ": " + value);
				}
				writeLine(writableByteChannel, "");
				byte[] content = httpResponse.getContent();
				ByteBuffer byteBuffer = ByteBuffer.wrap(content);
				writableByteChannel.write(byteBuffer);
			} catch (IOException ex) {
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jcc.http.server.HttpSession#getReadLines()
		 */
		@Override
		public StringBuilder getReadLines() {
			return this.readLines;
		}
	}
}