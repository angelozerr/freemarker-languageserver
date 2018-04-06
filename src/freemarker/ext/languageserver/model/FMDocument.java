package freemarker.ext.languageserver.model;

import java.util.ArrayList;
import java.util.List;

public class FMDocument extends Node {

		public FMDocument(String text) {
			super(0, text.length(), new ArrayList<>(), null);
		}

		public List<Node> getRoots() {
			return super.children;
		}
		
	}