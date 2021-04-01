package mtm68.ir.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.cornell.cs.cs4120.ir.IRCJump;
import edu.cornell.cs.cs4120.ir.IRJump;
import edu.cornell.cs.cs4120.ir.IRLabel;
import edu.cornell.cs.cs4120.ir.IRName;
import edu.cornell.cs.cs4120.ir.IRStmt;
import mtm68.util.ArrayUtils;

public class CFGBuilder {
	
	private int nodeIdx;
	private int stmtIdx;
	private CFGNode currNode;
	private Map<Integer, CFGNode> nodeMap; // nodeIdx -> node
	private Map<Integer, CFGNode> stmtMap; // stmtIdx -> node
	private Map<String, Integer> labelMap; // label -> stmtIdx
	private Map<String, Set<CFGNode>> waitingNodes; // label -> Set<node>
	private CFGKind kind;
	
	public CFGBuilder() {
		nodeIdx = 0;
		stmtIdx = 0;
		nodeMap = new HashMap<>();
		stmtMap = new HashMap<>();
		labelMap = new HashMap<>();
		waitingNodes = new HashMap<>();
		kind = CFGKind.RET;  
	}
	
	public void visitStatement(IRStmt stmt) {
		if(isLabel(stmt)) {
			createCFGNode(stmt);
			storeLabelLoc(stmt);
		}

		if(kind == CFGKind.RET) {
			createCFGNode(stmt);
		} 
		
		if(isJump(stmt)) {
			currNode.addJumpStmt(stmt);
			addOutboundConnections(stmt);
			kind = CFGKind.JMP;
		}
		
		stmtIdx++;
	}
	
	public List<CFGNode> getNodes() {
		SortedSet<Integer> keys = new TreeSet<>(nodeMap.keySet());
		return keys.stream()
				.map(nodeMap::get)
				.collect(Collectors.toList());
	}
	
	private boolean isLabel(IRStmt stmt) {
		return stmt instanceof IRLabel;
	}
	
	private boolean isJump(IRStmt stmt) {
		return stmt instanceof IRCJump ||
				stmt instanceof IRJump;
	}
	private void storeLabelLoc(IRStmt stmt) {
		String label = ((IRLabel) stmt).name();
		labelMap.put(label, stmtIdx);
		resolveWaitingNodes(label);
	}
	
	private void createCFGNode(IRStmt stmt) {
		CFGNode node = new CFGNode(stmt, nodeIdx, stmtIdx);
		nodeMap.put(nodeIdx, node);
		stmtMap.put(stmtIdx, node);
		nodeIdx++;

		addInboundConnections(node);

		kind = CFGKind.LABEL;
		currNode = node;
	}
	
	private void addOutboundConnections(IRStmt stmt) {
		Set<String> toLabels = labelsFromJump(stmt);
		toLabels.forEach(l -> this.addOutboundConnection(currNode, l));
	}
	
	private void addOutboundConnection(CFGNode from, String label) {
		if(labelMap.containsKey(label)) {
			Integer stmtIdx = labelMap.get(label);
			CFGNode to = stmtMap.get(stmtIdx);
			
			link(from, to);
		} else {
			addToWaitingNodes(label);
		}
	}
	
	private void resolveWaitingNodes(String label) {
		if(!waitingNodes.containsKey(label)) return;
		
		Set<CFGNode> waiting = waitingNodes.get(label);
		waitingNodes.remove(label);
		
		waiting.forEach(n -> this.addOutboundConnection(n, label));
	}
	
	private void addToWaitingNodes(String label) {
		if(!waitingNodes.containsKey(label)) {
			waitingNodes.put(label, new HashSet<>());
		}
		
		waitingNodes.get(label).add(currNode);
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
		
		// Maybe print a warning?
		return new HashSet<>();
	}
	
	private void addInboundConnections(CFGNode node) {
		switch(kind) {
		case LABEL:
			link(currNode, node);
			break;
		case JMP:
		case RET:
		default:
			break;
		}
	}
	
	private void link(CFGNode from, CFGNode to) {
		CFGEdge edge = new CFGEdge(from, to);

		from.addOutgoing(edge);
		to.addIncoming(edge);
	}
	
	public static class CFGNode {
		private IRStmt stmt;
		private int nodeIdx;
		private int stmtIdx;

		private List<CFGEdge> in;
		private List<CFGEdge> out;
		
		private Optional<IRStmt> jumpStmt;

		public CFGNode(IRStmt stmt, int nodeIdx, int stmtIdx) {
			this.stmt = stmt;
			this.nodeIdx = nodeIdx;
			this.stmtIdx = stmtIdx;

			in = new ArrayList<>();
			out = new ArrayList<>();

			jumpStmt = Optional.empty();
		}
		
		public void addIncoming(CFGEdge inbound) {
			in.add(inbound);
		}

		public void addOutgoing(CFGEdge outbound) {
			out.add(outbound);
		}
		
		public IRStmt getStmt() {
			return stmt;
		}
		
		public int getNodeIdx() {
			return nodeIdx;
		}
		
		public int getStmtIdx() {
			return stmtIdx;
		}
		
		public List<CFGEdge> getIn() {
			return in;
		}
		
		public List<CFGEdge> getOut() {
			return out;
		}
		
		public void addJumpStmt(IRStmt stmt) {
			this.jumpStmt = Optional.of(stmt);
		}
		
		public Optional<IRStmt> getJumpStmt() {
			return jumpStmt;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(nodeIdx);
			builder.append(" - ");
			builder.append(stmt);
			builder.append('[');
			builder.append(stmtIdx);
			builder.append(']');
			builder.append(" In: ");
			builder.append(edgesToString(in, true));
			builder.append(", Out: ");
			builder.append(edgesToString(out, false));
			
			jumpStmt.ifPresent(stmt -> {
				builder.append(", Jump: "); 
				builder.append(stmt);
			});
			
			return builder.toString().replaceAll("[\n\r]", "");
		}
		
		private String edgesToString(List<CFGEdge> edges, boolean from) {
			String str = edges.stream()
					.map(e -> from ? e.getFrom() : e.getTo())
					.map(n -> n.getNodeIdx())
					.map(Object::toString)
					.collect(Collectors.joining(","));
			return "[" + str + "]";
		}
	}
	
	public static class CFGEdge {
		private CFGNode from;
		private CFGNode to;

		public CFGEdge(CFGNode from, CFGNode to) {
			this.from = from;
			this.to = to;
		}

		public CFGNode getFrom() {
			return from;
		}
		
		public CFGNode getTo() {
			return to;
		}
	}
	
	public static enum CFGKind {
		LABEL,
		RET,
		JMP
	}
}
