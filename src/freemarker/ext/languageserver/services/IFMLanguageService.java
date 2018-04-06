package freemarker.ext.languageserver.services;

import java.util.List;

import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentItem;

import freemarker.ext.languageserver.model.FMDocument;

public interface IFMLanguageService {

	List<? extends SymbolInformation> findDocumentSymbols(TextDocumentItem document, FMDocument fmDocument);
}
