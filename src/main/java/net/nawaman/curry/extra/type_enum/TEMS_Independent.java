package net.nawaman.curry.extra.type_enum;

public final class TEMS_Independent extends TEMemberSpec {
	public TEMS_Independent(String pName) { super(pName); }
	@Override public boolean          isIndependent() { return true; }
	@Override public TEMS_Independent asIndependent() { return this; }
}