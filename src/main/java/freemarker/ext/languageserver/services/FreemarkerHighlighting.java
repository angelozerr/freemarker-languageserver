package freemarker.ext.languageserver.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;

import freemarker.ext.languageserver.internal.parser.BadLocationException;
import freemarker.ext.languageserver.internal.parser.FMScanner;
import freemarker.ext.languageserver.internal.parser.Scanner;
import freemarker.ext.languageserver.internal.parser.TokenType;
import freemarker.ext.languageserver.model.FMDocument;
import freemarker.ext.languageserver.model.Node;

public class FreemarkerHighlighting {

	public static List<DocumentHighlight> findDocumentHighlights(TextDocumentItem document, Position position,
			FMDocument fmDocument) {
		int offset = -1;
		try {
			offset = fmDocument.offsetAt(position);
		} catch (BadLocationException e) {
			return Collections.emptyList();
		}
		Node node = fmDocument.findNodeAt(offset);
		if (node.tag == null) {
			return Collections.emptyList();
		}
		List<DocumentHighlight> result = new ArrayList<>();
		Range startTagRange = getTagNameRange(TokenType.StartTag, document, node.start, fmDocument);
		Range endTagRange = node.endTagStart != null
				? getTagNameRange(TokenType.EndTag, document, node.endTagStart, fmDocument)
				: null;
		if (startTagRange != null && covers(startTagRange, position)
				|| endTagRange != null && covers(endTagRange, position)) {
			if (startTagRange != null) {
				result.add(new DocumentHighlight(startTagRange, DocumentHighlightKind.Read));
			}
			if (endTagRange != null) {
				result.add(new DocumentHighlight(endTagRange, DocumentHighlightKind.Read));
			}
		}
		return result;
	}

	private static boolean isBeforeOrEqual(Position pos1, Position pos2) {
		return pos1.getLine() < pos2.getLine()
				|| (pos1.getLine() == pos2.getLine() && pos1.getCharacter() <= pos2.getCharacter());
	}

	private static boolean covers(Range range, Position position) {
		return isBeforeOrEqual(range.getStart(), position) && isBeforeOrEqual(position, range.getEnd());
	}

	private static Range getTagNameRange(TokenType tokenType, TextDocumentItem document, int startOffset,
			FMDocument fmDocument) {
		Scanner scanner = FMScanner.createScanner(document.getText(), startOffset);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS && token != tokenType) {
			token = scanner.scan();
		}
		if (token != TokenType.EOS) {
			try {
				return new Range(fmDocument.positionAt(scanner.getTokenOffset()),
						fmDocument.positionAt(scanner.getTokenEnd()));
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
