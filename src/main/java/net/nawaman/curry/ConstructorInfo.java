/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's Curry.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.curry;

import java.io.Serializable;
import java.lang.reflect.*;

import net.nawaman.curry.OperationInfo.OIDirect;
import net.nawaman.curry.OperationInfo.SimpleOperation;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLParameter.TRParameter;
import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UObject;

/**
 * Information about constructor
 * 
 * ConstructorInfo has no used during the runtime. Every new instance is created by the TypeKind and ten passed on to
 * the initializer. ConstructorInfo is only used in the compile time and type loading when 
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class ConstructorInfo implements Accessible, Serializable, HasSignature {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	static public final ConstructorInfo[] EmptyConstructorInfos = new ConstructorInfo[0];
	
	/** The name (int its signature) of all Constructor */
	static public final String ConstructorSignatureName = "new";
	
	/** Signature of a default constructor */
	static public final ExecSignature DefaultConstructorSignature =
		ExecSignature.newProcedureSignature(ConstructorInfo.ConstructorSignatureName, TKJava.TVoid.getTypeRef());
	

	/** Error Message for Current Type in Constructor */
	static final String ErrMsg_NoCurrentTypeInConstructor = "Current type is not allowed in constructor (%s).";

	/** Error Message for Current Type in Constructor (use for CI.resolve) */
	static private final String ErrMsg_NoCurrentTypeInConstructor_CI =
	                        ErrMsg_NoCurrentTypeInConstructor + "<ConstructorInfo:151>";

	/** Error Message for Current Type in Constructor (use for CIMacro) */
	static private final String ErrMsg_NoCurrentTypeInConstructor_CIMacro =
	                        ErrMsg_NoCurrentTypeInConstructor + "<ConstructorInfo:116>";

	ConstructorInfo(Accessibility pAccess, TypeRef pOwnerAsTypeRef) {
		this.Access = (pAccess == null) ? Accessibility.Public : pAccess;
		this.OwnerAsTypeRef = pOwnerAsTypeRef;
	}
	
	final Accessibility Access;
	
	Executable.Macro DclExec        = null;
	Executable.Macro Exec           = null;
	TypeRef          OwnerAsTypeRef = null;
	
	/** Returns the Accessibility of the operation */
	final public Accessibility getAccessibility() {
		return this.Access;
	}
	/** Returns the signature of this construction */
	final public ExecSignature getSignature() {
		if(this.Exec != null) return this.Exec.getSignature();
		return this.DclExec.getSignature();
	}
	/** Returns the location of this construction */
	final public Location getLocation() {
		ExecSignature ES = this.getSignature();
		if(ES == null) return null;
		return ES.getLocation();
	}
	/** Returns the signature of this operation */
	public ExecSignature getDeclaredSignature() {
		return this.DclExec.getSignature();
	}
	/** Returns the type that own this operation (and that operation access will be done under it) */
	final public TypeRef getOwnerAsTypeRef() {
		return this.OwnerAsTypeRef;
	}
	/** Returns the MoreData of this type */
	abstract public MoreData getMoreData();
	
	// Owner -----------------------------------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public StackOwner getOwner() {
		return this.OwnerAsTypeRef.getTheType();
	}
	/**{@inheritDoc}*/ @Override
	public Type getOwnerAsType() {
		return this.OwnerAsTypeRef.getTheType();
	}
	
	// Display ---------------------------------------------------------------------------------------------------------
		
	/**{@inheritDoc}*/ @Override
	final public String toString() {
		ExecSignature ES = this.getSignature();
		return (ES == null)?"null":ES.toString();
	}
	
	/** String representation of the ConstructorInfo */
	public String toString(Engine pEngine) {
		if(this.Exec    != null) return this.Exec   .getSignature().toString();
		if(this.DclExec != null) return this.DclExec.getSignature().toString();
		return "new():void";
	}
	/** String representation of the ConstructorInfo */
	public String toDetail(Engine pEngine) {
		if(this.Exec    != null) return this.Exec   .getSignature().toDetail();
		if(this.DclExec != null) return this.DclExec.getSignature().toDetail();
		return "new():void";
	}
		
	// Sub classes -----------------------------------------------------------------------------------------------------
	
	/** Initialize with Macro */
	static final class CIMacro extends ConstructorInfo implements Executable.Macro {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		CIMacro(Accessibility pAccess, TypeRef pOwnerAsTypeRef, Executable.Macro pDclExec) {
			super(pAccess, pOwnerAsTypeRef);
			this.DclExec  = (Executable.Macro)pDclExec.clone();
			this.Exec     = null;
			this.MoreData = null;
		}
		CIMacro(Engine pEngine, Accessibility pAccess, TypeRef pOwnerTR, Executable.Macro pMacro, MoreData pMoreData) { 
			super(pAccess, pOwnerTR);
			
			this.DclExec = pMacro;
			if(this.DclExec == null) {
				if(pOwnerTR == null) throw new NullPointerException();
				this.DclExec = new CurryExecutable.CurryMacro(pEngine,
					ExecSignature.newProcedureSignature(ConstructorInfo.ConstructorSignatureName, TKJava.TVoid.getTypeRef(), null, null),
					null, null, null
				);
			}

			if(!ConstructorInfo.ConstructorSignatureName.equals(this.DclExec.getSignature().getName())) 
				throw new IllegalArgumentException("Invalid initializer signature name: '"+
						ConstructorInfo.ConstructorSignatureName+"' is expected but '"+ this.DclExec.getSignature().getName()+
						"' is found.");

			if(!TKJava.TVoid.getTypeRef().equals(this.DclExec.getSignature().getReturnTypeRef()))
				throw new IllegalArgumentException("Invalid initializer return type: 'void' is expected but '"+
						this.DclExec.getSignature().getReturnTypeRef()+"' is found.");

			ExecSignature ES = pMacro.getSignature();
			for(int i = ES.getParamCount(); --i >= 0; ) {
				TypeRef PTRef = ES.getParamTypeRef(i);
				if(!(PTRef instanceof TRBasedOnType) ||
						(PTRef instanceof TRParameter)  ||
						(PTRef instanceof TRParametered)) continue;
				// Report error when found a BasedOnType in the parameter
				throw new CurryError(String.format(ErrMsg_NoCurrentTypeInConstructor_CIMacro, pMacro.getSignature()));
			}
		
			this.MoreData = pMoreData;
		}
		
		final MoreData MoreData;
		
		/**{@inheritDoc}*/ @Override
		final public MoreData getMoreData() {
			return this.MoreData;
		}
		
		// To Satisfy Executable.Macro ---------------------------------------------------------------------------------
		
		// Kind --------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public Executable.ExecKind getKind() {
			return Executable.ExecKind.Macro;
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean isFragment() {
			return false;
		}
		/**{@inheritDoc}*/ @Override
		public boolean isMacro() {
			return true;
		}
		/**{@inheritDoc}*/ @Override
		public boolean isSubRoutine() {
			return false;
		}
		
		// Cast --------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public Fragment asFragment() {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		public Macro asMacro() {
			return this;
		}
		/**{@inheritDoc}*/ @Override
		public SubRoutine asSubRoutine() {
			return null;
		}
		
		// Curry -------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public boolean isCurry() {
			return this.Exec.isCurry();
		}
		/**{@inheritDoc}*/ @Override
		public Curry asCurry() {
			return this.Exec.asCurry();
		}

		/**{@inheritDoc}*/ @Override
		public boolean isJava() {
			return this.Exec.isJava();
		}
		/**{@inheritDoc}*/ @Override
		public JavaExecutable asJava() {
			return this.Exec.asJava();
		}
		
		// Frozen variables and Recreation ----------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public String[] getFrozenVariableNames() {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		public int getFrozenVariableCount() {
			return 0;
		}
		/**{@inheritDoc}*/ @Override
		public String getFrozenVariableName(int I) {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getFrozenVariableTypeRef(Engine pEngine, int I) {
			return null;
		}
		
		/**{@inheritDoc}*/ @Override
		public OIDirect reCreate(Engine pEngine, Scope pFrozenScope) {
			throw new CurryError("Constructor cannot be recreated.");
		}

		
		/**{@inheritDoc}*/ @Override
		public int hash() {
			return (this.Exec != null) ? this.Exec.hash() : this.DclExec.hash();
		}
		// Cloneable ---------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public CIMacro clone() {
			return new CIMacro(this.Access, this.getOwnerAsTypeRef(), this.DclExec);
		}
		// Lock --------------------------------------------------------------------
		/** This method will help limiting the implementation of this interface to be within this package. */
		final public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) { return null; }
		// Objectable --------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public String  toDetail()       { return (this.Exec != null)?this.Exec.toDetail():this.DclExec.toDetail(); }
		/**{@inheritDoc}*/ @Override
		public boolean is(Object O)     { return this == O; }
		/**{@inheritDoc}*/ @Override
		public int     hashCode()       { return (this.Exec != null)?this.Exec.hashCode():this.DclExec.hashCode(); }
		/**{@inheritDoc}*/ @Override
		public boolean equals(Object O) {
			if(!(O instanceof CIMacro)) return false;
			CIMacro TheO = (CIMacro)O;
			if((this.Exec == null) || (TheO.Exec == null)) return UObject.equal(this.DclExec, TheO.DclExec);
			return UObject.equal(this.Exec, TheO.Exec);
		}
	}
	
	/** Initialize with Native Constructor */
	static final class CINative extends ConstructorInfo {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		CINative(Engine pEngine, Accessibility pAccess, Constructor<?> pConstructor, MoreData pMoreData) { 
			super(pAccess, pEngine.getTypeManager().getTypeOfTheInstanceOf(pConstructor.getDeclaringClass()).getTypeRef());
			
			this.DclExec = new CurryExecutable.CurryMacro(pEngine, 
					ExecSignature.newSignature(pEngine, ConstructorSignatureName, pConstructor),
					null, null, null
				);
		}
		
		/**{@inheritDoc}*/ @Override
		public MoreData getMoreData() {
			return null;
		}
	}

	
	/** Initialize with Root Constructor */
	static final class CIRoot extends ConstructorInfo {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		CIRoot(Engine pEngine, Accessibility pAccess, TypeRef pOwnerAsTypeRef) {
			super(pAccess, pOwnerAsTypeRef);
			this.DclExec = new CurryExecutable.CurryMacro(pEngine, DefaultConstructorSignature, null, null, null);
		}
		/**{@inheritDoc}*/ @Override
		public MoreData getMoreData() {
			return null;
		}
	}

	// NOTE: Since constructor is not inheritable, the NewType is only for Parameterization (no Current).
	/** Resolve the Signature of this Constructor */
	boolean resolve(Engine pEngine, Type pNewType, boolean IsNeedToCheck) {
		if(this.Exec    != null) return true;
		if(this.DclExec == null) return true;
		
		if(IsNeedToCheck) {
			// Save the DclSignature for a while
			ExecSignature DclSignature = this.DclExec.getSignature();
			// Update the signature
			TypeRef       NewTypeRef   = pNewType.getTypeRef();
			ExecSignature NewSignature = SimpleOperation.ChangeBaseTypeSignature(
			                     pEngine,
			                     NewTypeRef,
			                     NewTypeRef,
			                     DclSignature,
			                     ErrMsg_NoCurrentTypeInConstructor_CI,
			                     TKJava.TVoid.getTypeRef()
			);
			
			// Update the declared
			Executable.Macro EC = (Executable.Macro)this.DclExec.clone();
			// Change the Signature
			if(      EC instanceof JavaExecutable)    ((JavaExecutable)   EC).Signature = NewSignature;
			else if (EC instanceof CurryExecutable)   ((CurryExecutable)  EC).Signature = NewSignature;
			else if (EC instanceof WrapperExecutable) ((WrapperExecutable)EC).Signature = NewSignature;
			//else if (EC instanceof OperationInfo)	    // OperationInfo cannot be used in OperationInfo 
			this.DclExec = EC;
				
			// Update the flatten
			NewSignature = SimpleOperation.flatSignature(
			                     pEngine,
			                     this.getOwnerAsTypeRef(),
			                     NewSignature);
			// Update the declared
			EC = (Executable.Macro)this.DclExec.clone();
			// Change the Signature
			if(      EC instanceof JavaExecutable)    ((JavaExecutable)   EC).Signature = NewSignature;
			else if (EC instanceof CurryExecutable)   ((CurryExecutable)  EC).Signature = NewSignature;
			else if (EC instanceof WrapperExecutable) ((WrapperExecutable)EC).Signature = NewSignature;
			//else if (EC instanceof OperationInfo)	    // OperationInfo cannot be used in OperationInfo 
			this.Exec = EC;
			
		} else
			// No resolve needed
			this.Exec = this.DclExec;
		
		return true;
	}
}