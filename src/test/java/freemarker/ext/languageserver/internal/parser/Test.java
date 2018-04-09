package freemarker.ext.languageserver.internal.parser;

import freemarker.ext.languageserver.internal.parser.DefaultFMParser;
import freemarker.ext.languageserver.model.FMDocument;
import freemarker.ext.languageserver.model.IFMParser;

public class Test {

	public static void main(String[] args) {
		IFMParser parser = DefaultFMParser.getInstance();
		FMDocument document = parser.parse("<r><a>xx\nx</a><b>yyy</b></r>");
		System.err.println(document);
	}
}
