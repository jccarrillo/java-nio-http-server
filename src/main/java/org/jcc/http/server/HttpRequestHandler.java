package org.jcc.http.server;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface HttpRequestHandler {

	/**
	 * Try to read a line.
	 */
	String line() throws IOException;

	/**
	 * Get more data from the stream.
	 */
	void read(ReadableByteChannel readableByteChannel) throws IOException;

	void sendResponse(WritableByteChannel writableByteChannel, HttpResponse response);

	StringBuilder getReadLines();
}