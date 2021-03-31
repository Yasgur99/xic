package mtm68.ast.nodes;

import java.util.List;
import java.util.stream.Collectors;

import edu.cornell.cs.cs4120.ir.IRFuncDecl;
import edu.cornell.cs.cs4120.ir.IRSeq;
import edu.cornell.cs.cs4120.ir.IRStmt;
import edu.cornell.cs.cs4120.util.SExpPrinter;
import mtm68.ast.nodes.stmts.Block;
import mtm68.ast.nodes.stmts.Statement;
import mtm68.util.ArrayUtils;
import mtm68.visit.NodeToIRNodeConverter;
import mtm68.visit.TypeChecker;
import mtm68.visit.Visitor;

public class FunctionDefn extends Node {
	
	private FunctionDecl functionDecl;
	private Block body;
	
	private  IRFuncDecl irFuncDecl;

	public FunctionDefn(FunctionDecl fDecl, Block body) {
		this.functionDecl = fDecl;
		this.body = body;
	}

	public FunctionDecl getFunctionDecl() {
		return functionDecl;
	}
	
	public Block getBody() {
		return body;
	}

	public IRFuncDecl getIrFuncDecl() {
		return irFuncDecl;
	}

	public void setIrFuncDecl(IRFuncDecl irFuncDecl) {
		this.irFuncDecl = irFuncDecl;
	}

	@Override
	public String toString() {
		return "FunctionDefn [fDecl=" + functionDecl + ", body=" + body + "]";
	}

	@Override
	public void prettyPrint(SExpPrinter p) {
		p.startList();
		functionDecl.prettyPrint(p);
		body.prettyPrint(p);
		p.endList();
	}

	@Override
	public Node visitChildren(Visitor v) {
		FunctionDecl newFunctionDecl = functionDecl.accept(v);
		Block newBody = body.accept(v);

		if(newFunctionDecl!= functionDecl || newBody != body) {
			FunctionDefn defn = copy();
			defn.functionDecl = newFunctionDecl;
			defn.body = newBody;
			return defn;
		}
		return this;
	}

	@Override
	public Node typeCheck(TypeChecker tc) {
		tc.checkFunctionResult(this);
		return this;
	}

	@Override
	public Node convertToIR(NodeToIRNodeConverter cv) {
		List<IRStmt> args = functionDecl.getArgs()
										.stream()
										.map(Statement::getIrStmt)
										.collect(Collectors.toList());

		// Put the declaration of function args into the body of the stmt
		List<IRStmt> argsAndBody = ArrayUtils.append(args, body.getIrStmt());
		IRSeq seq = new IRSeq(argsAndBody);
				
		FunctionDefn copy = copy();
		IRFuncDecl ir = new IRFuncDecl(functionDecl.getId(), seq);
		copy.setIrFuncDecl(ir);
		return copy;
	}
}
