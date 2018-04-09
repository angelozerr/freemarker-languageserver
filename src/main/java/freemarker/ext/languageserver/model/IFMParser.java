package freemarker.ext.languageserver.model;

import freemarker.ext.languageserver.internal.parser.DefaultFMParser;

public interface IFMParser {

	public static IFMParser getDefault() {
		return DefaultFMParser.getInstance();
	}
	
	FMDocument parse(String text);
}
