package freemarker.ext.languageserver.services;

import java.util.List;

import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentItem;

import freemarker.ext.languageserver.model.IFMDocument;

public interface IFMLanguageService {
	
	IFMDocument parseFMDocument(TextDocumentItem document);

	List<? extends SymbolInformation> findDocumentSymbols(TextDocumentItem document, IFMDocument fmDocument);
}
