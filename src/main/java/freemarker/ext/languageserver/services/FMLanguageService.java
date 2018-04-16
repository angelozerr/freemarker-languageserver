package freemarker.ext.languageserver.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentItem;

import freemarker.ext.languageserver.internal.parser.BadLocationException;
import freemarker.ext.languageserver.internal.parser.FMScanner;
import freemarker.ext.languageserver.internal.parser.Scanner;
import freemarker.ext.languageserver.internal.parser.TokenType;
import freemarker.ext.languageserver.model.FMDocument;
import freemarker.ext.languageserver.model.Node;

public class FMLanguageService implements IFMLanguageService {

	@Override
	public List<SymbolInformation> findDocumentSymbols(TextDocumentItem document, FMDocument fmDocument) {
		List<SymbolInformation> symbols = new ArrayList<>();
		fmDocument.getRoots().forEach(node -> {
			try {
				provideFileSymbolsInternal(document, fmDocument, node, "", symbols);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return symbols;
	}

	private void provideFileSymbolsInternal(TextDocumentItem document, FMDocument fmDocument, Node node,
			String container, List<SymbolInformation> symbols) throws BadLocationException {
		String name = nodeToName(node);
		Position start = fmDocument.positionAt(node.start);
		Position end = fmDocument.positionAt(node.end);
		Range range = new Range(start, end);
		Location location = new Location(document.getUri(), range);
		SymbolInformation symbol = new SymbolInformation(name, SymbolKind.Field, location, container);

		symbols.add(symbol);

		node.children.forEach(child -> {
			try {
				provideFileSymbolsInternal(document, fmDocument, child, name, symbols);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

	}

	private static String nodeToName(Node node) {
		String name = node.tag;

		if (node.attributes != null) {
			String id = node.attributes.get("id");
			String classes = node.attributes.get("class");

//			if (id) {
//				name += `#${id.replace(/[\"\']/g, '')}`;
//			}
//
//			if (classes) {
//				name += classes.replace(/[\"\']/g, '').split(/\s+/).map(className => `.${className}`).join('');
//			}
		}

		return name != null ? name : "?";
	}

	@Override
	public List<DocumentHighlight> findDocumentHighlights(TextDocumentItem document, Position position,
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
