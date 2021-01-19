package smallest_scope_extractor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class App {
	public static void main(String[] args) {
		String fileName = args[0];
		String indicesText = args[1];
		String stringArray[] = indicesText.split(",");

		int indices[] = new int[stringArray.length];
		for (int i = 0; i < stringArray.length; i++)
			indices[i] = Integer.parseInt(stringArray[i]);

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			ArrayList<String> sb = new ArrayList<String>();
			String line = br.readLine();
			while (line != null) {
				sb.add(line);
				line = br.readLine();
			}
			String[] lines = new String[sb.size()];

			for (int i = 0; i < sb.size(); i++) {
				lines[i] = sb.get(i);
			}

			String codeSnippet = String.join("\n", lines);
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7); // or newer version
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setSource(codeSnippet.toCharArray());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setCompilerOptions(options);
			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			Vector<ElemInfo> elems = null;
			int indicesLen = indices.length;
			// Print out in CSV format
			System.out.println("hunk_start,hunk_end,func_name,scope_start,scope_end");
			for (int index = 0; index < indicesLen; index += 2) {
				int startIndex = indices[index];
				int endIndex = indices[index + 1];
				elems = extractElements(cu, codeSnippet, lines, startIndex, endIndex);
				if (elems.size() > 0) {
					ElemInfo e = elems.get(elems.size() - 1);
					System.out.println(
							startIndex + "," + endIndex + "," + e.getName() + "," + e.getStart() + "," + e.getEnd());
				}
			}

		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

	private static Vector<ElemInfo> extractElements(CompilationUnit cu, String codeSnippet, String[] lines,
			int startIndex, int endIndex) {

		Vector<ElemInfo> elems = new Vector<ElemInfo>();

		int anonCnt[] = { 0 };
		int ifCnt[] = { 0 };
		int elseCnt[] = { 0 };
		int switchCnt[] = { 0 };
		int forCnt[] = { 0 };
		int whileCnt[] = { 0 };
		int doCnt[] = { 0 };
		int tryCnt[] = { 0 };
		int catchCnt[] = { 0 };
		int finallyCnt[] = { 0 };

		cu.accept(new ASTVisitor() {
			public boolean visit(AnonymousClassDeclaration node) {

				String name = "anon_" + String.valueOf(anonCnt[0]);
				int start = cu.getLineNumber(node.getStartPosition());

				int offSet = 0;

				int end = cu.getLineNumber(node.getStartPosition() + node.getLength() - offSet);

				while (end == -1) {
					offSet++;
					end = cu.getLineNumber(node.getStartPosition() + node.getLength() - offSet);
				}

				if (start > startIndex || end < endIndex)
					return false;

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				return true;
			}

			public boolean visit(TypeDeclaration node) {

				SimpleName name = node.getName();
				int start = cu.getLineNumber(node.getStartPosition());

				int offSet = 0;

				int end = cu.getLineNumber(node.getStartPosition() + node.getLength() - offSet);

				while (end == -1) {
					offSet++;
					end = cu.getLineNumber(node.getStartPosition() + node.getLength() - offSet);
				}

				if (start > startIndex || end < endIndex)
					return false;

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				return true;
			}

			//
			public boolean visit(EnumDeclaration node) {

				SimpleName name = node.getName();
				int start = cu.getLineNumber(node.getStartPosition());

				int offSet = 0;

				int end = cu.getLineNumber(node.getStartPosition() + node.getLength() - offSet);

				while (end == -1) {
					offSet++;
					end = cu.getLineNumber(node.getStartPosition() + node.getLength() - offSet);
				}

				if (start > startIndex || end < endIndex) {
					return false;
				}

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				return true;

			}

			public boolean visit(MethodDeclaration node) {

				SimpleName name = node.getName();
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());

				if (start > startIndex || end < endIndex) {
					return false;
				}

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				return true;
			}

			public boolean visit(SwitchStatement node) {
				String name = "switch_" + String.valueOf(switchCnt[0]);
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());

				if (start > startIndex || end < endIndex)
					return false;

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				switchCnt[0]++;

				return true;
			}

			public boolean visit(IfStatement node) {

				String name = "if_" + String.valueOf(ifCnt[0]);

				int start = cu.getLineNumber(node.getStartPosition());

				ASTNode thenBlock = node.getThenStatement();

				int endThen = cu.getLineNumber(thenBlock.getStartPosition() + thenBlock.getLength());

				boolean ifPass = true;

				if (start <= startIndex && endThen >= endIndex) {
					elems.addElement(new ElemInfo(name.toString(), start, endThen, node));
					ifCnt[0]++;
				} else
					ifPass = false;

				ASTNode elseBlock = node.getElseStatement();

				if (elseBlock != null) {

					boolean elsePass = true;

					String nameElse = "else_" + String.valueOf(elseCnt[0]);
					int startElse = cu.getLineNumber(elseBlock.getStartPosition());
					int endElse = cu.getLineNumber(elseBlock.getStartPosition() + elseBlock.getLength());

					for (int lineIndex = startElse - 1; lineIndex >= 0; lineIndex--) {

						if (lines[lineIndex].contains("else")) {
							startElse = lineIndex + 1;
							break;
						}
					}

					if (startElse <= startIndex && endElse >= endIndex) {
						elems.addElement(new ElemInfo(nameElse.toString(), startElse, endElse, node));
						elseCnt[0]++;
					} else
						elsePass = false;

					if (!ifPass && !elsePass)
						return false;

				} else if (!ifPass) {
					return false;
				}

				return true;
			}

			public boolean visit(ForStatement node) {
				String name = "for_" + String.valueOf(forCnt[0]);

				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());

				if (start > startIndex || end < endIndex)
					return false;

				forCnt[0]++;

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				return true;
			}

			public boolean visit(EnhancedForStatement node) {

				String name = "for_" + String.valueOf(forCnt[0]);

				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());

				if (start > startIndex || end < endIndex)
					return false;

				forCnt[0]++;

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				return true;

			}

			public boolean visit(WhileStatement node) {

				String name = "while_" + String.valueOf(whileCnt[0]);

				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());

				if (start > startIndex || end < endIndex)
					return false;

				whileCnt[0]++;

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				return true;
			}

			public boolean visit(DoStatement node) {

				String name = "do_" + String.valueOf(doCnt[0]);

				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());

				if (start > startIndex || end < endIndex)
					return false;

				doCnt[0]++;

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				return true;
			}

			public boolean visit(TryStatement node) {

				String name = "try_" + String.valueOf(tryCnt[0]);

				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());

				if (start > startIndex || end < endIndex)
					return false;

				tryCnt[0]++;

				elems.addElement(new ElemInfo(name.toString(), start, end, node));

				ASTNode finallyBlock = node.getFinally();

				if (finallyBlock != null) {
					String finallyName = "finally_" + String.valueOf(finallyCnt[0]);
					int startFinally = cu.getLineNumber(finallyBlock.getStartPosition());
					int endFinally = cu.getLineNumber(finallyBlock.getStartPosition() + finallyBlock.getLength());

					// Update start position for the previous try block
					for (int lineIndex = startFinally - 1; lineIndex >= 0; lineIndex--) {

						if (lines[lineIndex].contains("}")) {

							end = lineIndex + 1;

							if (start <= startIndex && end >= endIndex)
								elems.get(elems.size() - 1).setEnd(lineIndex + 1);
							else
								elems.remove(elems.size() - 1);

							break;
						}
					}

					for (int lineIndex = startFinally - 1; lineIndex >= 0; lineIndex--) {

						if (lines[lineIndex].contains("finally")) {
							startFinally = lineIndex + 1;
							break;
						}
					}

					if (startFinally <= startIndex && endFinally >= endIndex) {
						elems.addElement(new ElemInfo(finallyName.toString(), startFinally, endFinally, node));
						finallyCnt[0]++;
					}
				}

				return true;
			}

			public boolean visit(CatchClause node) {

				String name = "catch_" + String.valueOf(catchCnt[0]);

				catchCnt[0]++;

				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());

				boolean catchPass = true;

				if (start <= startIndex && end >= endIndex)
					elems.addElement(new ElemInfo(name.toString(), start, end, node));
				else
					catchPass = false;

				// Update start position for the previous try block
				for (int index = elems.size() - 1; index >= 0; index--) {
					ElemInfo curElem = elems.get(index);

					if (curElem.getName().contains("try")) {

						int endTryIndex = start;

						for (int lineIndex = start - 1; lineIndex >= 0; lineIndex--) {
							if (lines[lineIndex].contains("}")) {
								endTryIndex = lineIndex + 1;
								break;
							}
						}

						int startTryIndex = elems.get(index).getStart();

						if (startTryIndex > startIndex || endTryIndex < endIndex)
							elems.remove(index);
						else
							elems.get(index).setEnd(endTryIndex);

						break;
					}
				}

				if (!catchPass)
					return false;

				return true;
			}
		});

		return elems;
	}

}
