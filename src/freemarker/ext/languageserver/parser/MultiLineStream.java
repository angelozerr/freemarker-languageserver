package freemarker.ext.languageserver.parser;

import static freemarker.ext.languageserver.parser.Constants._CAR;
import static freemarker.ext.languageserver.parser.Constants._LFD;
import static freemarker.ext.languageserver.parser.Constants._NWL;
import static freemarker.ext.languageserver.parser.Constants._TAB;
import static freemarker.ext.languageserver.parser.Constants._WSP;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MultiLineStream {

	private String source;
	private int len;
	private int position;

	public MultiLineStream(String source, int position) {
		this.source = source;
		this.len = source.length();
		this.position = position;
	}

	public boolean eos() {
		return this.len <= this.position;
	}

	public String getSource() {
		return this.source;
	}

	public int pos() {
		return this.position;
	}

	public void goBackTo(int pos) {
		this.position = pos;
	}

	public void goBack(int n) {
		this.position -= n;
	}

	public void advance(int n) {
		this.position += n;
	}

	public void goToEnd() {
		this.position = this.source.length();
	}

	public int nextChar() {
		// return this.source.codePointAt(this.position++) || 0;
		int next = this.source.codePointAt(this.position++);
		return next >= 0 ? next : 0;
	}

	public int peekChar() {
		return peekChar(0);
	}

	public int peekChar(int n) {
		int c = this.source.codePointAt(this.position + n);
		return c >= 0 ? c : 0;
	}

	public boolean advanceIfChar(int ch) {
		if (ch == this.source.codePointAt(this.position)) {
			this.position++;
			return true;
		}
		return false;
	}

	public boolean advanceIfChars(int... ch) {
		int i;
		if (this.position + ch.length > this.source.length()) {
			return false;
		}
		for (i = 0; i < ch.length; i++) {
			if (this.source.codePointAt(this.position + i) != ch[i]) {
				return false;
			}
		}
		this.advance(i);
		return true;
	}

	public String advanceIfRegExp(Pattern regex) {
		String str = this.source.substring(this.position);
		Matcher match = regex.matcher(str);
		if (match.find()) {
			String s = match.group(0);
			this.position = this.position + match.start() + s.length();
			return s;
		}
		return "";
	}

	public String advanceUntilRegExp(Pattern regex) {
		String str = this.source.substring(this.position);
		/*
		 * TODO let match = str.match(regex); if (match) { this.position = this.position
		 * + match.index!; return match[0]; } else { this.goToEnd(); }
		 */
		return "";
	}

	public boolean advanceUntilChar(int ch) {
		while (this.position < this.source.length()) {
			if (this.source.codePointAt(this.position) == ch) {
				return true;
			}
			this.advance(1);
		}
		return false;
	}

	public boolean advanceUntilChars(int... ch) {
		while (this.position + ch.length <= this.source.length()) {
			int i = 0;
			for (; i < ch.length && this.source.codePointAt(this.position + i) == ch[i]; i++) {
			}
			if (i == ch.length) {
				return true;
			}
			this.advance(1);
		}
		this.goToEnd();
		return false;
	}

	public boolean skipWhitespace() {
		int n = this.advanceWhileChar(ch -> {
			return ch == _WSP || ch == _TAB || ch == _NWL || ch == _LFD || ch == _CAR;
		});
		return n > 0;
	}

	public int advanceWhileChar(Predicate<Integer> condition) {
		int posNow = this.position;
		while (this.position < this.len && condition.test(this.source.codePointAt(this.position))) {
			this.position++;
		}
		return this.position - posNow;
	}
}