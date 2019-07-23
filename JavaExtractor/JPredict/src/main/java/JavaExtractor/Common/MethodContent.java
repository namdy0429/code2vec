package JavaExtractor.Common;

import java.util.ArrayList;
import com.github.javaparser.ast.Node;

public class MethodContent {
	private ArrayList<Node> leaves;
	private String name;
	private long length;
	private String comment;

	public MethodContent(ArrayList<Node> leaves, String name, long length, String comment) {
		this.leaves = leaves;
		this.name = name;
		this.length = length;
		this.comment = comment;
	}

	public ArrayList<Node> getLeaves() {
		return leaves;
	}

	public String getName() {
		return name;
	}

	public long getLength() {
		return length;
	}

	public String getJavaDoc() {
		return comment;
	}

}
