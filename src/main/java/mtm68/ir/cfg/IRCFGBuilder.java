package mtm68.ir.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import edu.cornell.cs.cs4120.ir.IRCJump;
import edu.cornell.cs.cs4120.ir.IRJump;
import edu.cornell.cs.cs4120.ir.IRLabel;
import edu.cornell.cs.cs4120.ir.IRName;
import edu.cornell.cs.cs4120.ir.IRReturn;
import edu.cornell.cs.cs4120.ir.IRStmt;
import mtm68.assem.cfg.Graph;
import mtm68.assem.cfg.Graph.Node;
import mtm68.util.ArrayUtils;
import polyglot.util.InternalCompilerError;

public class IRCFGBuilder<T> {
	private Graph<IRData<T>> graph;
	private	Node prev;
	private Node curr;
	private boolean prevWasLabel = false;
	private boolean prevWasJump = false;
	private boolean prevWasRet = false;
	private String lastLabel;
	
	private Map<String, Node> locationMap;
	private Map<String, List<Node>> waitingJumps;
	private Map<Integer, Node> stmtIdxToNode;
	private List<IRStmt> originalStmts;
	
	public IRCFGBuilder() {
		graph = new Graph<>();
		locationMap = new HashMap<>();
		waitingJumps = new HashMap<>();
		stmtIdxToNode = new HashMap<>();
	}
	
	public List<IRStmt> convertBackToIR() {
		List<IRStmt> rebuilt = ArrayUtils.empty();
		int i = 0;
		for(IRStmt stmt : originalStmts) {
			if (stmt instanceof IRLabel || stmt instanceof IRJump || stmt instanceof IRCJump) {
				rebuilt.add(stmt);
				continue;
			} 
			
			Node n = stmtIdxToNode.get(i);
			i++;
			IRData<T> data = graph.getDataForNode(n);
			IRStmt newStmt = data.getIR();
			rebuilt.add(newStmt);
		}
		return rebuilt;
	}

	public Graph<IRData<T>> buildIRCFG(List<IRStmt> stmts, Supplier<T> flowDataConstructor) {
		this.originalStmts = stmts;
		int i = 0;
		for(IRStmt stmt : stmts) {
					
			if(isLabel(stmt)) {
				handleLabel((IRLabel)stmt);
				continue;
			}

			if(!(stmt instanceof IRJump)) {
				IRData<T> data = new IRData<>(stmt, flowDataConstructor.get());
				curr = graph.createNode(data);
				stmtIdxToNode.put(i++, curr);
				
				if(prev != null && !prevWasJump && !prevWasRet){
					graph.addEdge(prev, curr);
				}
			}
			
			if(isJump(stmt)) {
				handleJump(stmt);
				if(stmt instanceof IRJump) continue;
			}
			
			if(prevWasLabel) {
				locationMap.put(lastLabel, curr);
				resolveWaitingJumps(lastLabel);
				prevWasLabel = false;
			} 
			
			prev = curr;
			if(!isJump(stmt)) prevWasJump = false;
			prevWasRet = graph.getDataForNode(prev).getIR() instanceof IRReturn;
		}
		
		if(waitingJumps.size() != 0) throw new InternalCompilerError("Still have jumps that need resolving: " + waitingJumps);
		
		return graph;
	}
	
	private void handleJump(IRStmt jump) {
		prevWasJump = jump instanceof IRJump || !isFallThroughCJump(jump);
		
		Set<String> toLabels = labelsFromJump(jump);
		for(String label : toLabels) {
			if(locationMap.containsKey(label)) {
				Node jumpTarget = locationMap.get(label);
				graph.addEdge(curr, jumpTarget); 
			} else {
				addToWaitingJumps(label, curr);
			}
		}
	}
	
	private void addToWaitingJumps(String label, Node node) {
		if(!waitingJumps.containsKey(label)) {
			waitingJumps.put(label, ArrayUtils.empty());
		}
		
		waitingJumps.get(label).add(node);
	}
	
	private void resolveWaitingJumps(String label) {
		Node jumpTo = locationMap.get(label);
		
		if(jumpTo == null) throw new InternalCompilerError("Resolving jump nodes but found null for location");
		
		if(waitingJumps.containsKey(label)) {
			waitingJumps.get(label).forEach(n -> graph.addEdge(n, jumpTo));
			waitingJumps.remove(label);
		}
	}
	
	private Set<String> labelsFromJump(IRStmt stmt) {
		if(stmt instanceof IRJump) {
			IRJump jump = (IRJump) stmt;
			IRName name = (IRName) jump.target();
			return ArrayUtils.newHashSet(name.name());
		} else if (stmt instanceof IRCJump) {
			IRCJump cjump = (IRCJump) stmt;

			Set<String> set = ArrayUtils.newHashSet(cjump.trueLabel());
			if(cjump.falseLabel() != null) set.add(cjump.falseLabel());

			return set; 
		}
		
		return new HashSet<>();
	}

	private void handleLabel(IRLabel label) {
		lastLabel = label.name();
		prevWasLabel = true;
	}
	
	private boolean isFallThroughCJump(IRStmt stmt) {
		if(!(stmt instanceof IRCJump)) return false;
		
		IRCJump jump = (IRCJump)stmt;
		
		return !jump.hasFalseLabel();
	}
	
	private boolean isJump(IRStmt stmt) {
		return stmt instanceof IRJump || stmt instanceof IRCJump;
	}

	private boolean isLabel(IRStmt stmt) {
		return stmt instanceof IRLabel;
	}
	
	public static class IRData<T> {

		private IRStmt ir;
		private T flowData;
		private int stmtIdx;

		public IRData(IRStmt ir, T flowData) {
			this(ir, flowData, -1);
		}
		
		public IRData(IRStmt ir, T flowData, int stmtIdx) {
			this.ir= ir;
			this.flowData = flowData;
			this.stmtIdx = stmtIdx;
		}

		public IRStmt getIR() {
			return ir;
		}

		public void setIR(IRStmt ir) {
			this.ir = ir;
		}

		public T getFlowData() {
			return flowData;
		}

		public void setFlowData(T flowData) {
			this.flowData = flowData;
		}

		public int getStmtIdx() {
			return stmtIdx;
		}

		public void setStmtIdx(int stmtIdx) {
			this.stmtIdx = stmtIdx;
		}

		@Override
		public String toString() {
			return ir.toString();
		}
	}
}
