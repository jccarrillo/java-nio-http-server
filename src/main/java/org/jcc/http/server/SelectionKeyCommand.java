package org.jcc.http.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface SelectionKeyCommand {

	/**
	 * Executes a {@link SelectionKey}.
	 * 
	 * @param selectionKey
	 *            the selection key to execute.
	 * @throws IOException
	 */
	void execute(SelectionKey selectionKey) throws IOException;
}