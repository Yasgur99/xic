package mtm68.ast.nodes.stmts;

import java.util.List;
import java.util.Optional;

import edu.cornell.cs.cs4120.util.SExpPrinter;
import mtm68.ast.nodes.Node;
import mtm68.visit.TypeChecker;
import mtm68.visit.Visitor;

public class Block extends Statement {
	
	private List<Statement> stmts;
	private Optional<Return> returnStmt;

	public Block(List<Statement> stmts) {
		this.stmts = stmts;
		this.returnStmt = Optional.empty();
	}

	public Block(List<Statement> stmts, Return returnStmt) {
		this.stmts = stmts;
		this.returnStmt = Optional.of(returnStmt);
	}

	public Block(List<Statement> stmts, Optional<Return> returnStmt) {
		this.stmts = stmts;
		this.returnStmt = returnStmt;
	}

	@Override
	public String toString() {
		return "Block [stmts=" + stmts + ", returnStmt=" + returnStmt + "]";
	}

	@Override
	public void prettyPrint(SExpPrinter p) {
		p.startUnifiedList();
		for(Statement stmt : stmts) {
			stmt.prettyPrint(p);
		}
		
		if(returnStmt.isPresent()) returnStmt.get().prettyPrint(p);
		p.endList();
	}
	
	public List<Statement> getStmts() {
		return stmts;
	}

	public Optional<Return> getReturnStmt() {
		return returnStmt;
	}

	@Override
	public Node visitChildren(Visitor v) {
		List<Statement> stmts = visitChild(this.stmts, v);
		Optional<Return> returnStmt = visitChild(this.returnStmt, v);

		// TODO: check copy
		return new Block(stmts, returnStmt);
	}

	@Override
	public Node typeCheck(TypeChecker tc) {
		// TODO Auto-generated method stub
		return null;
	}
}
