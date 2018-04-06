package freemarker.ext.languageserver.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentItem;

import freemarker.ext.languageserver.model.IFMDocument;
import freemarker.ext.languageserver.model.Node;
import freemarker.ext.languageserver.parser.FMParser;

public class FMLanguageService implements IFMLanguageService {

	@Override
	public IFMDocument parseFMDocument(TextDocumentItem document) {
		return FMParser.parse(document.getText());
	}

	@Override
	public List<SymbolInformation> findDocumentSymbols(TextDocumentItem document, IFMDocument fmDocument) {
		List<SymbolInformation> symbols = new ArrayList<>();
		fmDocument.getRoots().forEach(node -> {
			provideFileSymbolsInternal(document, node, "", symbols);
		});
		return symbols;
	}

	private void provideFileSymbolsInternal(TextDocumentItem document, Node node, String container,
			List<SymbolInformation> symbols) {
		String name = nodeToName(node);
		Position start = null;
		Position end = null;
		Range range = new Range(start, end);
		Location location = new Location(document.getUri(), range);
				
//				
//				Location.create(document.getUri(),
//				Range.create(document.positionAt(node.start), document.positionAt(node.end)));
		SymbolInformation symbol = new SymbolInformation(name, SymbolKind.Field, location, container);

		symbols.add(symbol);

		node.children.forEach(child -> {
			provideFileSymbolsInternal(document, child, name, symbols);
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
}
