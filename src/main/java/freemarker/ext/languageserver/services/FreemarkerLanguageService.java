package freemarker.ext.languageserver.services;

import java.util.List;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentItem;

import freemarker.ext.languageserver.model.FMDocument;

public class FreemarkerLanguageService {

	private FreemarkerCompletions completions;

	public FreemarkerLanguageService() {
		completions = new FreemarkerCompletions();
	}

	public List<SymbolInformation> findDocumentSymbols(TextDocumentItem document, FMDocument fmDocument) {
		return FreemarkerSymbolsProvider.findDocumentSymbols(document, fmDocument);
	}

	public List<DocumentHighlight> findDocumentHighlights(TextDocumentItem document, Position position,
			FMDocument fmDocument) {
		return FreemarkerHighlighting.findDocumentHighlights(document, position, fmDocument);
	}

	public CompletionList doComplete(TextDocumentItem document, Position position, FMDocument fmDocument,
			CompletionConfiguration settings) {
		return completions.doComplete(document, position, fmDocument, settings);
	}

	public void setCompletionParticipants(ICompletionParticipant completionParticipants) {
		this.completions.setCompletionParticipants(completionParticipants);
	}
}
