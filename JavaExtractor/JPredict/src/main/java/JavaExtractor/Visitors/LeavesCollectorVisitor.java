package JavaExtractor.Visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.TreeVisitor;

import JavaExtractor.Common.Common;
import JavaExtractor.FeaturesEntities.Property;

import com.google.common.base.CharMatcher;

public class LeavesCollectorVisitor extends TreeVisitor {
	ArrayList<Node> m_Leaves = new ArrayList<>(); 
	String javadoc_comments = "";
	private int currentId = 1;

	@Override
	public void process(Node node) {
		if (node.getComment() != null) {
		// if (node.getComment() != null && node.getComment() instanceof JavadocComment) {
			// System.out.println(node.getComment());
			// System.out.println(node.getComment() instanceof JavadocComment);
			if (node.getComment() instanceof JavadocComment) {
				javadoc_comments = node.getComment().toString();	
			}
		}
		if (node instanceof Comment) {
			return;
		}

		boolean isLeaf = false;
		boolean isGenericParent = isGenericParent(node);
		if (hasNoChildren(node) && isNotComment(node)) {
			if (!node.toString().isEmpty() && (!"null".equals(node.toString()) || (node instanceof NullLiteralExpr))) {
				m_Leaves.add(node);
				isLeaf = true;
			}
		}
		
		int childId = getChildId(node);
		node.setUserData(Common.ChildId, childId);
		Property property = new Property(node, isLeaf, isGenericParent, currentId++);
		node.setUserData(Common.PropertyKey, property);
	}

	private boolean isGenericParent(Node node) {
		return (node instanceof ClassOrInterfaceType) 
				&& ((ClassOrInterfaceType)node).getTypeArguments() != null 
				&& ((ClassOrInterfaceType)node).getTypeArguments().size() > 0;
	}

	private boolean hasNoChildren(Node node) {
		return node.getChildrenNodes().size() == 0;
	}

	private boolean isJavaDoc(Node node) {
		return node.getComment() instanceof JavadocComment;
	}
	
	private boolean isNotComment(Node node) {
		return !(node instanceof Comment) && !(node instanceof Statement);
	}
	
	public ArrayList<Node> getLeaves() {
		return m_Leaves;
	}

	public String getJavaDoc() {
		// return javadoc_comments;
		// TODO: stopwords
		if (!CharMatcher.ascii().matchesAllOf(javadoc_comments)) {
			return "";
		}

		String[] sentences = javadoc_comments.replace("*", "").replace("/", "").replace("\n", "").split("\\.");
		// System.out.println(javadoc_comments);
		if (sentences.length > 0 ) {
			sentences = sentences[0].split("@");
			return sentences[0].trim().replaceAll(" +", "|");
		}
		else {
			return "";
		}
	}
	
	private int getChildId(Node node) {
		Node parent = node.getParentNode();
		List<Node> parentsChildren = parent.getChildrenNodes();
		int childId = 0;
		for (Node child: parentsChildren) {
			if (child.getRange().equals(node.getRange())) {
				return childId;
			}
			childId++;
		}
		return childId;
	}
}
