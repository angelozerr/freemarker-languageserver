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

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Launcher to start Freemarker language server in stdio
 *
 */
public class FreemarkerServerStdioLauncher {

	public static void main(String[] args) {
		FreemarkerLanguageServer server = new FreemarkerLanguageServer();
		Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
		server.setClient(launcher.getRemoteProxy());
		launcher.startListening();
	}
}
