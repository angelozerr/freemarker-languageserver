package freemarker.ext.languageserver.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Position;

import freemarker.ext.languageserver.internal.parser.BadLocationException;

public class FMDocument extends Node {

	ListLineTracker lineTracker;

	public FMDocument(String text) {
		super(0, text.length(), new ArrayList<>(), null);
		lineTracker = new ListLineTracker();
		lineTracker.set(text);
	}

	public List<Node> getRoots() {
		return super.children;
	}

	public Position positionAt(int position) throws BadLocationException {
		int lineNumber = lineTracker.getLineNumberOfOffset(position);
		Line line = lineTracker.getLineInformation(lineNumber);
		return new Position(lineNumber, position - line.offset);
	}

	public int offsetAt(Position position) throws BadLocationException {
		int lineNumber = position.getLine();
		Line line = lineTracker.getLineInformation(lineNumber);
		return line.offset + position.getCharacter();
	}

}