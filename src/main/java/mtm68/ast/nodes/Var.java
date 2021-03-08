package mtm68.ast.nodes;

import java.util.Optional;

import edu.cornell.cs.cs4120.util.SExpPrinter;
import mtm68.ast.nodes.stmts.SingleAssignLHS;
import mtm68.ast.types.Type;

public class Var extends Expr implements SingleAssignLHS {
	
	private String id;
	
	public Var(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return id;
	}

	@Override
	public String getName() {
		return id;
	}

	@Override
	public Optional<Type> getType() {
		return Optional.empty();
	}

	@Override
	public void prettyPrint(SExpPrinter p) {
		p.printAtom(id);
	}

}
