package net.nawaman.curry.util;

import java.io.Serializable;

import net.nawaman.curry.Engine;
import net.nawaman.curry.Expression;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TypeRef;
import net.nawaman.util.Objectable;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

public class DataHolderInfo implements Serializable, Objectable, Cloneable {
	
	static private final long serialVersionUID = -4896111669757785674L;
	
	static final int Ind_IsReadable   = 0;
	static final int Ind_IsWritable   = 1;
	static final int Ind_IsSet        = 2;
	static final int Ind_IsExpression = 3;
	
	static public final TypeRef NO_CHANGE_TYPEREF = null;
	
	public DataHolderInfo(TypeRef pTypeRef, Serializable pIValue, String pDHFactoryName, boolean pIsReadable,
			boolean pIsWritable, boolean pIsSet, boolean pIsExpression, MoreData pMoreInfo) {
		this.TypeRef       = pTypeRef;
		this.IValue        = pIValue;
		this.DHFactoryName = pDHFactoryName;
		this.MoreInfo      = pMoreInfo;
		
		this.Flags[Ind_IsReadable]   = pIsReadable;
		this.Flags[Ind_IsWritable]   = pIsWritable;
		this.Flags[Ind_IsSet]        = pIsSet;
		this.Flags[Ind_IsExpression] = pIsExpression && (pIValue instanceof Expression);
		
		if(this.MoreInfo != null) this.MoreInfo.toFreeze();
	}
	
	TypeRef      TypeRef       = null;
	Serializable IValue        = null;
	String       DHFactoryName = null;
	MoreData     MoreInfo      = null;
	
	private boolean[] Flags = new boolean[4];


	final protected TypeRef getTypeRefRAW() {
		return this.TypeRef;
	}
	public TypeRef getTypeRef() {
		if(this.TypeRef == null) return TKJava.TAny.getTypeRef();
		return this.TypeRef;
	}
	public boolean hasTypeRef() {
		return this.TypeRef != null;
	}
	
	public Serializable getIValue()        { return this.IValue;        }
	public String       getDHFactoryName() { return this.DHFactoryName; }
	public MoreData     getMoreInfo()      { return (this.MoreInfo == null)?MoreData.Empty:this.MoreInfo; }
	
	final public boolean isReadable()   { return this.Flags[Ind_IsReadable];   }
	final public boolean isWritable()   { return this.Flags[Ind_IsWritable];   }
	final public boolean isSet()        { return this.Flags[Ind_IsSet];        }
	final public boolean isExpression() { return this.Flags[Ind_IsExpression]; }
	
	final protected void setReadable(boolean V)   { this.Flags[Ind_IsReadable]   = V; }
	final protected void setWritable(boolean V)   { this.Flags[Ind_IsWritable]   = V; }
	final protected void setSet(boolean V)        { this.Flags[Ind_IsSet]        = V; }
	final protected void setExpression(boolean V) { this.Flags[Ind_IsExpression] = V; }
	
	// Cloneable --------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public DataHolderInfo clone() {
		return new DataHolderInfo(
			this.TypeRef.clone(),
			this.IValue,
			this.DHFactoryName,
			this.isReadable(),
			this.isWritable(),
			this.isSet(),
			this.isExpression(),
			(this.MoreInfo == null) ? null : this.MoreInfo.clone()
		);
	}
	
	// Resolve ----------------------------------------------------------------
	
	/** Create a duplication of this DataHolderInfo with a new Default Value */
	public DataHolderInfo resolve(Engine $Engine, TypeRef DValueTypeRef, Serializable DValue) {
		TypeRef TRef       = this.getTypeRefRAW();
		boolean IsWritable = this.isWritable();
		
		if((TRef == null) && !IsWritable)
			TRef = DValueTypeRef;
		
		return new DataHolderInfo(
			TRef,
			DValue,
			this.getDHFactoryName(),
			this.isReadable(),
			IsWritable,
			true,
			(DValue instanceof Expression),
			this.getMoreInfo()
		);
	}
	
	// Objectable -------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	public String toString() {
		StringBuffer SB = new StringBuffer();
		
		SB.append(" { ");
		boolean R = this.isReadable();
		boolean W = this.isWritable();
		SB.append(( R && !W)? "readonly":"");
		SB.append((!R &&  W)?"writeonly":"");
		SB.append((!R && !W)? "abstract":"");
		SB.append(" ");
		SB.append(this.DHFactoryName);
		SB.append(" ");
		SB.append(UObject.toString(this.TypeRef));
		SB.append(" } ");
		
		return SB.toString();
	}
	/**{@inheritDoc}*/ @Override
	public String toDetail() {
		StringBuffer SB = new StringBuffer();
		
		SB.append(" { ");
		boolean R = this.isReadable();
		boolean W = this.isWritable();
		SB.append(( R && !W)? "readonly":"");
		SB.append((!R &&  W)?"writeonly":"");
		SB.append((!R && !W)? "abstract":"");
		SB.append(" ");
		SB.append(this.DHFactoryName);
		SB.append(" ");
		SB.append(UObject.toString(this.TypeRef));
		SB.append(" = ");
		SB.append(this.isExpression()?"":"(");
		SB.append(UObject.toString(this.IValue));
		SB.append(this.isExpression()?"":")");
		SB.append(";");
		
		SB.append(this.isSet()?" set at initial;":"");

		SB.append((this.MoreInfo != null)?(" "+ UObject.toString(this.MoreInfo) +";"):"");
		
		SB.append(" } ");
		
		return SB.toString();
	}
	
	/**{@inheritDoc}*/ @Override
	public boolean is(Object O) {
		return this == O;
	}
	/**{@inheritDoc}*/ @Override
	public boolean equals(Object O) {
		if(!(O instanceof DataHolderInfo)) return false;
		DataHolderInfo DHI = (DataHolderInfo)O;
		if(!UString.equal(this.DHFactoryName, DHI.DHFactoryName)) return false;
		if(!UObject.equal(this.TypeRef,       DHI.TypeRef))       return false;
		if(!UObject.equal(this.IValue,        DHI.IValue))        return false;
		if(!UObject.equal(this.MoreInfo,      DHI.MoreInfo))      return false;
		if(this.isReadable()   != DHI.isReadable())               return false;
		if(this.isWritable()   != DHI.isWritable())               return false;
		if(this.isSet()        != DHI.isSet())                    return false;
		if(this.isExpression() != DHI.isExpression())             return false;
		return true;
	}
	/**{@inheritDoc}*/ @Override
	public int hash() {
		return this.hashCode();
	}
}