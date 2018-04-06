package freemarker.ext.languageserver.model;

import java.util.List;

public interface IFMDocument {

	List<Node> getRoots();

	Node findNodeBefore(int offset);

	Node findNodeAt(int offset);
}
