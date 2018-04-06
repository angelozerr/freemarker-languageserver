/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package freemarker.ext.languageserver;

import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import freemarker.core.ParseException;
import freemarker.ext.languageserver.commons.LanguageModelCache;
import freemarker.ext.languageserver.commons.TextDocuments;
import freemarker.ext.languageserver.model.IFMDocument;
import freemarker.ext.languageserver.services.FMLanguageService;
import freemarker.ext.languageserver.services.IFMLanguageService;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Freemarker text document service.
 *
 */
public class FreemarkerTextDocumentService implements TextDocumentService {

	private final FreemarkerLanguageServer fmLanguageServer;
	private final TextDocuments documents;
	private final IFMLanguageService languageService;
	private Configuration fmConfiguration;
	private LanguageModelCache<IFMDocument> fmDocuments;

	public FreemarkerTextDocumentService(FreemarkerLanguageServer fmLanguageServer) {
		this.fmLanguageServer = fmLanguageServer;
		this.languageService = new FMLanguageService();
		this.documents = new TextDocuments();
		this.fmDocuments = new LanguageModelCache<IFMDocument>(10, 60,
				document -> languageService.parseFMDocument(document));
	}

	private IFMDocument getFMDocument(TextDocumentItem document) {
		return fmDocuments.get(document);
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
			TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		return null;
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
		return computeAsync((monitor) -> {
			TextDocumentItem document = documents.get(params.getTextDocument().getUri());
			IFMDocument fmDocument = getFMDocument(document);
			return languageService.findDocumentSymbols(document, fmDocument);
		});
	}

	@Override
	public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		return null;
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		return null;
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return null;
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		documents.onDidOpenTextDocument(params);
		triggerValidation(params.getTextDocument());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		documents.onDidChangeTextDocument(params);
		TextDocumentItem document = documents.get(params.getTextDocument().getUri());
		if (document != null) {
			triggerValidation(document);
		}
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		documents.onDidCloseTextDocument(params);
		fmDocuments.onDocumentRemoved(params.getTextDocument().getUri());
		TextDocumentIdentifier document = params.getTextDocument();
		String uri = document.getUri();
		fmLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));

	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	private void triggerValidation(TextDocumentItem document) {
		String uri = document.getUri();
		List<Diagnostic> diagnostics = validateFMDocument(uri, document.getText());
		fmLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
	}

	private List<Diagnostic> validateFMDocument(String xmlDocumentUri, String xmlDocumentContent) {
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

		if (fmConfiguration == null) {
			fmConfiguration = new Configuration(Configuration.getVersion());
			fmConfiguration.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
			fmConfiguration.setTabSize(1);
		}

		try {
			@SuppressWarnings("unused")
			Template dummy = new Template(xmlDocumentUri, xmlDocumentContent, fmConfiguration);
		} catch (ParseException e) {
			Position start = new Position(e.getLineNumber() - 1, e.getColumnNumber());
			Position end = new Position(e.getEndLineNumber() - 1, e.getEndColumnNumber());
			Diagnostic diagnostic = new Diagnostic(new Range(start, end), e.getEditorMessage());
			diagnostics.add(diagnostic);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return diagnostics;
	}

	public void validateOpenDocuments() {

	}
}
