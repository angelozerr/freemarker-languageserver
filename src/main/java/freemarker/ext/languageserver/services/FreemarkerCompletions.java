package freemarker.ext.languageserver.services;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;

import freemarker.ext.languageserver.internal.parser.BadLocationException;
import freemarker.ext.languageserver.internal.parser.FMScanner;
import freemarker.ext.languageserver.internal.parser.Scanner;
import freemarker.ext.languageserver.internal.parser.TokenType;
import freemarker.ext.languageserver.model.FMDocument;
import freemarker.ext.languageserver.model.Node;

public class FreemarkerCompletions {

	private ICompletionParticipant completionParticipants;

	public void setCompletionParticipants(ICompletionParticipant completionParticipants) {
		this.completionParticipants = completionParticipants;
	}

	public CompletionList doComplete(TextDocumentItem document, Position position, FMDocument fmDocument,
			CompletionConfiguration settings) {
		CompletionList result = new CompletionList();
		int offset;
		try {
			offset = fmDocument.offsetAt(position);
		} catch (BadLocationException e) {
			return null;
		}

		Node node = fmDocument.findNodeBefore(offset);
		if (node == null) {
			return result;
		}

		String text = document.getText();
		Scanner scanner = FMScanner.createScanner(text, node.start);
		String currentTag = "";
		String currentAttributeName;

		TokenType token = scanner.scan();
		while (token != TokenType.EOS && scanner.getTokenOffset() <= offset) {
			switch (token) {
			case StartTagOpen:
				if (scanner.getTokenEnd() == offset) {
					int endPos = scanNextForEndPos(offset, scanner, TokenType.StartTag);
					return collectTagSuggestions(offset, endPos);
				}
				break;
			case StartTag:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					return collectOpenTagSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd());
				}
				currentTag = scanner.getTokenText();
				break;
			case AttributeName:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					return collectAttributeNameSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd());
				}
				currentAttributeName = scanner.getTokenText();
				break;
			case DelimiterAssign:
				if (scanner.getTokenEnd() == offset) {
					int endPos = scanNextForEndPos(offset, scanner, TokenType.AttributeValue);
					return collectAttributeValueSuggestions(offset, endPos);
				}
				break;
			case AttributeValue:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					return collectAttributeValueSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd());
				}
				break;
			case Whitespace:
				if (offset <= scanner.getTokenEnd()) {
					switch (scanner.getScannerState()) {
					case AfterOpeningStartTag:
						int startPos = scanner.getTokenOffset();
						int endTagPos = scanNextForEndPos(offset, scanner, TokenType.StartTag);
						return collectTagSuggestions(startPos, endTagPos);
					case WithinTag:
					case AfterAttributeName:
						return collectAttributeNameSuggestions(scanner.getTokenEnd());
					case BeforeAttributeValue:
						return collectAttributeValueSuggestions(scanner.getTokenEnd());
					case AfterOpeningEndTag:
						return collectCloseTagSuggestions(scanner.getTokenOffset() - 1, false);
					case WithinContent:
						return collectInsideContent();
					}
				}
				break;
			case EndTagOpen:
				if (offset <= scanner.getTokenEnd()) {
					int afterOpenBracket = scanner.getTokenOffset() + 1;
					int endOffset = scanNextForEndPos(offset, scanner, TokenType.EndTag);
					return collectCloseTagSuggestions(afterOpenBracket, false, endOffset);
				}
				break;
			case EndTag:
				if (offset <= scanner.getTokenEnd()) {
					int start = scanner.getTokenOffset() - 1;
					while (start >= 0) {
						char ch = text.charAt(start);
						if (ch == '/') {
							return collectCloseTagSuggestions(start, false, scanner.getTokenEnd());
						} else if (!isWhiteSpace(ch)) {
							break;
						}
						start--;
					}
				}
				break;
			case StartTagClose:
				if (offset <= scanner.getTokenEnd()) {
					if (currentTag.length() > 0) {
						return collectAutoCloseTagSuggestion(scanner.getTokenEnd(), currentTag, fmDocument, result);
					}
				}
				break;
			case Content:
				if (offset <= scanner.getTokenEnd()) {
					return collectInsideContent();
				}
				break;
			default:
				if (offset <= scanner.getTokenEnd()) {
					return result;
				}
				break;
			}
			token = scanner.scan();
		}

		return result;
	}

	private boolean isWhiteSpace(char ch) {
		return ch == ' ';
	}

	private CompletionList collectAutoCloseTagSuggestion(int tagCloseEnd, String tag, FMDocument document, CompletionList result) {
		//if (!isEmptyElement(tag)) {
			Position pos;
			try {
				pos = document.positionAt(tagCloseEnd);
			} catch (BadLocationException e) {
				return result;
			}
			CompletionItem item = new CompletionItem();
			item.setLabel("</" + tag + ">");
			item.setKind(CompletionItemKind.Property);
			item.setFilterText("</" + tag + ">");
			item.setTextEdit(new TextEdit(new Range(pos, pos), "$0</" + tag + ">"));
			item.setInsertTextFormat(InsertTextFormat.Snippet);
		//}
		return result;
	}

	private boolean isEmptyElement(String tag) {
		// TODO Auto-generated method stub
		return false;
	}

	private CompletionList collectCloseTagSuggestions(int afterOpenBracket, boolean b, int endOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectInsideContent() {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectCloseTagSuggestions(int i, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectAttributeValueSuggestions(int tokenEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectAttributeNameSuggestions(int tokenEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectAttributeValueSuggestions(int offset, int endPos) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectAttributeNameSuggestions(int tokenOffset, int tokenEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectOpenTagSuggestions(int tokenOffset, int tokenEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectTagSuggestions(int offset, int endPos) {
		// TODO Auto-generated method stub
		return null;
	}

	private static int scanNextForEndPos(int offset, Scanner scanner, TokenType nextToken) {
		if (offset == scanner.getTokenEnd()) {
			TokenType token = scanner.scan();
			if (token == nextToken && scanner.getTokenOffset() == offset) {
				return scanner.getTokenEnd();
			}
		}
		return offset;
	}
}
