package freemarker.ext.languageserver.internal.parser;

public interface Scanner {

	TokenType scan();

	TokenType getTokenType();

	int getTokenOffset();

	int getTokenLength();

	int getTokenEnd();

	String getTokenText();

	String getTokenError();

	ScannerState getScannerState();
}
