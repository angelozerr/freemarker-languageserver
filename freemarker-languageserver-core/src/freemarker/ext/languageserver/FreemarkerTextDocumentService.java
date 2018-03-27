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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Freemarker text document service.
 *
 */
public class FreemarkerTextDocumentService implements TextDocumentService {

	private final FreemarkerLanguageServer fmLanguageServer;
	private final Map<String, String> openFMDocuments;
	private Configuration fmConfiguration;

	public FreemarkerTextDocumentService(FreemarkerLanguageServer fmLanguageServer) {
		this.fmLanguageServer = fmLanguageServer;
		this.openFMDocuments = new HashMap<>();
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
		return null;
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
		TextDocumentItem textDocument = params.getTextDocument();
		String uri = textDocument.getUri();
		List<Diagnostic> diagnostics = validateFMDocument(uri, textDocument.getText());
		openFMDocuments.put(uri, textDocument.getText());
		fmLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = params.getTextDocument();
		String uri = versionedTextDocumentIdentifier.getUri();
		Iterator<TextDocumentContentChangeEvent> textDocumentContentChangeEventIterator = params.getContentChanges()
				.iterator();
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		while (textDocumentContentChangeEventIterator.hasNext()) {
			TextDocumentContentChangeEvent textDocumentContentChangeEvent = textDocumentContentChangeEventIterator
					.next();
			String text = textDocumentContentChangeEvent.getText();
			openFMDocuments.put(uri, text);
			List<Diagnostic> currentDiagnostics = validateFMDocument(uri, text);
			diagnostics.addAll(currentDiagnostics);
		}
		fmLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		String uri = params.getTextDocument().getUri();
		fmLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
		openFMDocuments.remove(uri);
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

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
