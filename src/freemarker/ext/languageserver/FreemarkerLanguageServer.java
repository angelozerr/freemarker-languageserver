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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Freemarker language server.
 *
 */
public class FreemarkerLanguageServer implements LanguageServer {

	/**
	 * Exit code returned when Freemarker Language Server is forced to exit.
	 */
	private static final int FORCED_EXIT_CODE = 1;

	private final FreemarkerTextDocumentService fmTextDocumentService;
	private final FreemarkerWorkspaceService fmWorkspaceService;
	private LanguageClient languageClient;

	public FreemarkerLanguageServer() {
		fmTextDocumentService = new FreemarkerTextDocumentService(this);
		fmWorkspaceService = new FreemarkerWorkspaceService(this);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
		InitializeResult result = new InitializeResult(capabilities);
		return CompletableFuture.completedFuture(result);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return computeAsync((cc) -> {
			return new Object();
		});
	}

	@Override
	public void exit() {
		Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			logInfo("Forcing exit after 1 min.");
			System.exit(FORCED_EXIT_CODE);
		}, 1, TimeUnit.MINUTES);
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return fmTextDocumentService;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		return fmWorkspaceService;
	}

	public void setClient(LanguageClient languageClient) {
		this.languageClient = languageClient;
	}

	public LanguageClient getLanguageClient() {
		return languageClient;
	}

	private void logInfo(String message) {
		System.out.println(message);
	}
}
