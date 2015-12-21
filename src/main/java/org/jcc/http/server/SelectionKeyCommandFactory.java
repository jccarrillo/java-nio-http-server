package org.jcc.http.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class SelectionKeyCommandFactory {

	private final SelectionKeyCommand accept;
	private final SelectionKeyCommand read;
	private final SelectionKeyCommand write;

	public SelectionKeyCommandFactory(Selector selector) {
		this.accept = new AcceptSelectionKeyCommand(selector);
		this.read = new ReadSelectionKeyCommand();
		this.write = new WriteSelectionKeyCommand();
	}

	/**
	 * Returns a {@link SelectionKeyCommand} based on the {@link SelectionKey}.
	 * 
	 * @param selectionKey
	 * @return
	 * @throws InvalidSelectionKeyCommandException
	 *             if no command is found.
	 */
	public SelectionKeyCommand getSelectionKeyCommand(SelectionKey selectionKey)
			throws InvalidSelectionKeyCommandException {
		if (selectionKey.isAcceptable()) {
			return this.accept;
		} else if (selectionKey.isReadable()) {
			return this.read;
		} else if (selectionKey.isWritable()) {
			return this.write;
		}

		throw new InvalidSelectionKeyCommandException();
	}
}