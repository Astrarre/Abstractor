package io.github.intransientmc.abstracter.util;

import java.util.function.Supplier;

import org.objectweb.asm.MethodVisitor;

public class MethodProposal {
	private final Supplier<MethodVisitor> visitorMaker;
	private final String desc;
	private MethodVisitor visitor;

	public MethodProposal(Supplier<MethodVisitor> visitor, String desc) {
		this.visitorMaker = visitor;
		this.desc = desc;
	}

	public MethodVisitor makeVisitor() {
		if(this.visitor != null)
			return this.visitor;
		return this.visitor = this.visitorMaker.get();
	}

	public String getDesc() {
		return this.desc;
	}
}
