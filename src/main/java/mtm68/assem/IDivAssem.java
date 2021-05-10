package mtm68.assem;

import java.util.List;

import mtm68.assem.ReplaceableReg.RegType;
import mtm68.assem.operand.RealReg;
import mtm68.assem.operand.Src;
import mtm68.util.ArrayUtils;

public class IDivAssem extends Assem {
	private Src src;

	public IDivAssem(Src src) {
		super();
		this.src = src;
	}
	
	public Src getSrc() {
		return src;
	}

	public void setSrc(Src src) {
		this.src = src;
	}

	@Override
	public String toString() {
		return "idiv " + src;
	}
	
	@Override
	public List<ReplaceableReg> getReplaceableRegs() {
		return ArrayUtils.concat(
				ReplaceableReg.fromSrc(src, this::setSrc),
				ArrayUtils.elems(
						ReplaceableReg.fromRealReg(RealReg.RDX, RegType.READ),
						ReplaceableReg.fromRealReg(RealReg.RDX, RegType.WRITE),
						ReplaceableReg.fromRealReg(RealReg.RAX, RegType.READ),
						ReplaceableReg.fromRealReg(RealReg.RAX, RegType.WRITE)
					)
			);
	}
}
