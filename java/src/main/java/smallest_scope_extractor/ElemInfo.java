package smallest_scope_extractor;

import org.eclipse.jdt.core.dom.ASTNode;

public class ElemInfo {
	private String name;
	private ASTNode node;
	private int start;
	private int end;

	public ElemInfo(String name, int start, int end, ASTNode node) {
		this.name = name;
		this.start = start;
		this.end = end;
		this.node = node;
	}

	public String getName() {
		return name;
	}

	public ASTNode getNode() {
		return node;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String toString() {
		return this.name + "\t" + this.start + "\t" + this.end;
	}
}
