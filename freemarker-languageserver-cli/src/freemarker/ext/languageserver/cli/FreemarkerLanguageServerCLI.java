package freemarker.ext.languageserver.cli;

import freemarker.ext.languageserver.FreemarkerLanguageServerLauncher;

public class FreemarkerLanguageServerCLI {
	
	public static void main(String[] args) {
		if (args.length != 0) {
			System.err.print("No command line options are supported at the moment...");
		}
		FreemarkerLanguageServerLauncher.launch(System.in, System.out);
	}
	
}
