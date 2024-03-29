/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2021 Nawapunth Manusitthipol. Implements with and for Java 11 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's Curry.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawa<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.curry;

import java.util.Hashtable;

import net.nawaman.curry.Instructions_Array.*;
import net.nawaman.curry.Instructions_Context.*;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.util.UArray;
import net.nawaman.util.UNumber;
import net.nawaman.util.UString;

/**
 * Abstract instruction that take are an assignment (including an incremental assignment) 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class Inst_Assignment extends Inst_AbstractSimple {
	
	static public final String Name = "assignment";
	
	static public final String ParamStrForIncAccess = "+i,+i";
	static public final int    IndexForSource    = 0;
	static public final int    IndexForOperation = 1;
	static public final int    IndexForValue     = 2;
	static public final int    IndexSourceParams = 3;
	
	final Hashtable<Integer, SourceProvider>   SourceProviders   = new Hashtable<Integer, Inst_Assignment.SourceProvider>();
	final Hashtable<Integer, OperatorProvider> OperatorProviders = new Hashtable<Integer, Inst_Assignment.OperatorProvider>();
	
	Inst_Assignment(Engine pEngine, String pName) {
		super(pEngine, pName + "("+ParamStrForIncAccess+",~,~...)");
	}
	
	@Override protected Object run(Context pContext, Object[] pParams) {
		SourceProvider   SP = this.getSourceProvider(  (Integer)pParams[IndexForSource]);
		OperatorProvider OP = this.getOperatorProvider((Integer)pParams[IndexForOperation]);
		Object Value = OP.getParam(pParams, pContext);
			
		// The incremental is done on the non-DataHolder (with set and get)
		Object OldO = SP.getValue(pContext, (Object[])pParams[IndexSourceParams]);
		if(OldO == null) OldO = 0;
		
		Object NewO = OP.process(pContext, OldO, Value);
		if(NewO instanceof SpecialResult.ResultError) return NewO;
		
		SP.setValue(pContext, (Object[])pParams[IndexSourceParams], NewO);
		return OP.isProcessBefore()?NewO:OldO;
	}
	
	/** Add a source provider and return if success */
	public boolean addSourceProvider(SourceProvider SP) {
		if(SP == null) return false;
		String SPName     = SP.getName();
		int    SPNameHash = UString.hash(SPName);
		if(this.getSourceProvider(SPNameHash) != null) return false;
		
		this.SourceProviders.put(SPNameHash, SP);
		return true;
	}
	/** Add a source provider and return if success */
	public boolean addOperatorProvider(OperatorProvider OP) {
		if(OP == null) return false;
		String OPName     = OP.getName();
		int    OPNameHash = UString.hash(OPName);
		if(this.getOperatorProvider(OPNameHash) != null) return false;
		
		this.OperatorProviders.put(OPNameHash, OP);
		return true;
	}
	
	/** Returns the source provider of this incremental access */
	SourceProvider getSourceProvider(int I, int Dummy) {
		SourceProvider SP = this.getSourceProvider(I);
		if(SP != null) return SP;
		// Add more here
		throw new RuntimeException("Unknown source provider index ("+I+").");
	}
	
	/** Returns the source provider of this incremental access */
	public SourceProvider getSourceProvider(int I) {
		// Those that are know at this time
		if(I == SPLocalVar        .NameHash) return SPLocalVar        .Instance;
		if(I == SPParentVarByCount.NameHash) return SPParentVarByCount.Instance;
		if(I == SPParentVarByName .NameHash) return SPParentVarByName .Instance;
		if(I == SPGlobalVar       .NameHash) return SPGlobalVar       .Instance;
		if(I == SPEngineVar       .NameHash) return SPEngineVar       .Instance;
		if(I == SPArrayElement    .NameHash) return SPArrayElement    .Instance;
		// Those that are added later
		if(this.SourceProviders != null) {
			SourceProvider SP = this.SourceProviders.get(I);
			if(SP != null) return SP;
		}
		return null;
	}

	/** Returns the parameter provider */
	OperatorProvider getOperatorProvider(int I, int Dummy) {
		OperatorProvider OP = this.getOperatorProvider(I);
		if(OP != null) return OP;
		// Add more here
		throw new RuntimeException("Unknown operator provider index ("+I+").");
	}
	/** Returns the parameter provider */
	public OperatorProvider getOperatorProvider(int I) {
		// Those that are know at this time
		if(I == IncBefore           .NameHash) return IncBefore           .Instance;
		if(I == DecBefore           .NameHash) return DecBefore           .Instance;
		if(I == IncAfter            .NameHash) return IncAfter            .Instance;
		if(I == DecAfter            .NameHash) return DecAfter            .Instance;
		if(I == AppendTo            .NameHash) return AppendTo            .Instance;
		if(I == AddTo               .NameHash) return AddTo               .Instance;
		if(I == SubtractFrom        .NameHash) return SubtractFrom        .Instance;
		if(I == MulTo               .NameHash) return MulTo               .Instance;
		if(I == DivTo               .NameHash) return DivTo               .Instance;
		if(I == ModulusOf           .NameHash) return ModulusOf           .Instance;
		if(I == ShiftLeftBy         .NameHash) return ShiftLeftBy         .Instance;
		if(I == ShiftRightBy        .NameHash) return ShiftRightBy        .Instance;
		if(I == ShiftRightUnsignedBy.NameHash) return ShiftRightUnsignedBy.Instance;
		if(I == bitwiseAndTo               .NameHash) return bitwiseAndTo               .Instance;
		if(I == bitwiseOrTo                .NameHash) return bitwiseOrTo                .Instance;
		if(I == bitwiseXorTo               .NameHash) return bitwiseXorTo               .Instance;
		if(I == ANDTo               .NameHash) return ANDTo               .Instance;
		if(I == ORTo                .NameHash) return ORTo                .Instance;
		if(I == XORTo               .NameHash) return XORTo               .Instance;
		// Those that are added later
		if(this.OperatorProviders != null) {
			OperatorProvider OP = this.OperatorProviders.get(I);
			if(OP != null) return OP;
		}
		return null;
	}
		
	// Return type --------------------------------------------------------------------------------
		
	/**{@inheritDoc}*/ @Override
	public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
		SourceProvider SP = this.getSourceProvider((Integer)pExpr.getParam(IndexForSource));
		if(SP == null) {
			pCProduct.reportError(
				"Unknown source provider '"+pExpr.getParam(IndexForSource)+"' <Inst_Assignment:158>",
				null, pExpr.getColumn(), pExpr.getLineNumber());
			return null;
		}
		TypeRef TRSource = SP.getReturnTypeRef(pExpr, pCProduct);
		if(TRSource != null) return TRSource;
		
		OperatorProvider OP = this.getOperatorProvider((Integer)pExpr.getParam(IndexForOperation));
		TypeRef TR = OP.getSourceType(pCProduct).getTypeRef();
		if(TR == null) TR = OP.getParamTypeRef(pExpr, pCProduct);
		return TR;
	}
	
	// Parameter check ----------------------------------------------------------------------------
		
	/**  Checks if the type of the parameters of the expression are correct. **/
	@Override public boolean ensureParamCorrect(Expression pExpr, CompileProduct pCProduct) {
		SourceProvider   SP = this.getSourceProvider(  (Integer)pExpr.getParam(IndexForSource));
		OperatorProvider OP = this.getOperatorProvider((Integer)pExpr.getParam(IndexForOperation));
		String ErrMsg = null;
		if(SP == null) ErrMsg = "Unable to find the source provider for the index "   + (Integer)pExpr.getParam(IndexForSource);
		if(OP == null) ErrMsg = "Unable to find the operator provider for the index " + (Integer)pExpr.getParam(IndexForOperation);
		
		if(ErrMsg != null) return ReportCompileProblem("Assignment:182", ErrMsg, pExpr, pCProduct, false, false);
		
		TypeRef TRSource = SP.getReturnTypeRef(pExpr, pCProduct);
		TypeRef TRParam  = pCProduct.getReturnTypeRefOf(pExpr.getParam( IndexForValue));
		
		if(TRSource == null) {
			ReportCompileProblem(
					"Assignment:189", "Unable to determine the type of the source object",
					pExpr, pCProduct, false, false);
			return pCProduct.isCompileTimeCheckingNone();
		}
		
		// Display more detail of the error
		if(!OP.ensureParamCorrect(pCProduct, pExpr, TRSource, TRParam))
			return ReportWrongParams("Assignment:196", pExpr, pCProduct, false, false);
		
		return SP.ensureWritable(pCProduct, pExpr);
	}
	
	// Get Set instruction of ---------------------------------------------------------------------
	
	/** Returns the name of the instruction for setting the value of the same assignable as the reading */
	public String getSourceSetInstructionName(String pInstName) {
		if(pInstName == null) return null;
		// ==
		if(pInstName == SPLocalVar        .Instance.getGetInstructionName()) return SPLocalVar        .Instance.getSetInstructionName();
		if(pInstName == SPParentVarByCount.Instance.getGetInstructionName()) return SPParentVarByCount.Instance.getSetInstructionName();
		if(pInstName == SPParentVarByName .Instance.getGetInstructionName()) return SPParentVarByName .Instance.getSetInstructionName();
		if(pInstName == SPGlobalVar       .Instance.getGetInstructionName()) return SPGlobalVar       .Instance.getSetInstructionName();
		if(pInstName == SPEngineVar       .Instance.getGetInstructionName()) return SPEngineVar       .Instance.getSetInstructionName();
		if(pInstName == SPArrayElement    .Instance.getGetInstructionName()) return SPArrayElement    .Instance.getSetInstructionName();
		
		// equals
		if(pInstName.equals(SPLocalVar        .Instance.getGetInstructionName())) return SPLocalVar        .Instance.getSetInstructionName();
		if(pInstName.equals(SPParentVarByCount.Instance.getGetInstructionName())) return SPParentVarByCount.Instance.getSetInstructionName();
		if(pInstName.equals(SPParentVarByName .Instance.getGetInstructionName())) return SPParentVarByName .Instance.getSetInstructionName();
		if(pInstName.equals(SPGlobalVar       .Instance.getGetInstructionName())) return SPGlobalVar       .Instance.getSetInstructionName();
		if(pInstName.equals(SPEngineVar       .Instance.getGetInstructionName())) return SPEngineVar       .Instance.getSetInstructionName();
		if(pInstName.equals(SPArrayElement    .Instance.getGetInstructionName())) return SPArrayElement    .Instance.getSetInstructionName();
		
		// Those that are added later
		if(this.SourceProviders != null) {
			for(SourceProvider SP : this.SourceProviders.values()) {
				if(pInstName ==     SP.getGetInstructionName())  return SP.getSetInstructionName();
				if(pInstName.equals(SP.getGetInstructionName())) return SP.getSetInstructionName();
			}
		}
		return null;
	}
		
	/** Returns hash of the source provide name that can be used as the parameter */
	public int getSourceHashOf(String pInstName) {
		if(pInstName == null) return -1;
		// ==
		if(pInstName == SPLocalVar        .Instance.getGetInstructionName()) return SPLocalVar        .NameHash;
		if(pInstName == SPParentVarByCount.Instance.getGetInstructionName()) return SPParentVarByCount.NameHash;
		if(pInstName == SPParentVarByName .Instance.getGetInstructionName()) return SPParentVarByName .NameHash;
		if(pInstName == SPGlobalVar       .Instance.getGetInstructionName()) return SPGlobalVar       .NameHash;
		if(pInstName == SPEngineVar       .Instance.getGetInstructionName()) return SPEngineVar       .NameHash;
		if(pInstName == SPArrayElement    .Instance.getGetInstructionName()) return SPArrayElement    .NameHash;
		
		// equals
		if(pInstName.equals(SPLocalVar        .Instance.getGetInstructionName())) return SPLocalVar        .NameHash;
		if(pInstName.equals(SPParentVarByCount.Instance.getGetInstructionName())) return SPParentVarByCount.NameHash;
		if(pInstName.equals(SPParentVarByName .Instance.getGetInstructionName())) return SPParentVarByName .NameHash;
		if(pInstName.equals(SPGlobalVar       .Instance.getGetInstructionName())) return SPGlobalVar       .NameHash;
		if(pInstName.equals(SPEngineVar       .Instance.getGetInstructionName())) return SPEngineVar       .NameHash;
		if(pInstName.equals(SPArrayElement    .Instance.getGetInstructionName())) return SPArrayElement    .NameHash;
		
		// Those that are added later
		if(this.SourceProviders != null) {
			for(int SPNameHash : this.SourceProviders.keySet()) {
				SourceProvider SP = this.SourceProviders.get(SPNameHash);
				if(pInstName ==     SP.getGetInstructionName())  return SPNameHash;
				if(pInstName.equals(SP.getGetInstructionName())) return SPNameHash;
			}
		}
		return -1;
	}
	/** Returns the parameter provider */
	public int getOperatorHashOf(String pName) {
		if(pName == null) return -1;
		int NameHash = UString.hash(pName);
		// Those that are know at this time
		if(NameHash == IncBefore           .NameHash) return IncBefore           .NameHash;
		if(NameHash == DecBefore           .NameHash) return DecBefore           .NameHash;
		if(NameHash == IncAfter            .NameHash) return IncAfter            .NameHash;
		if(NameHash == DecAfter            .NameHash) return DecAfter            .NameHash;
		if(NameHash == AppendTo            .NameHash) return AppendTo            .NameHash;
		if(NameHash == AddTo               .NameHash) return AddTo               .NameHash;
		if(NameHash == SubtractFrom        .NameHash) return SubtractFrom        .NameHash;
		if(NameHash == MulTo               .NameHash) return MulTo               .NameHash;
		if(NameHash == DivTo               .NameHash) return DivTo               .NameHash;
		if(NameHash == ModulusOf           .NameHash) return ModulusOf           .NameHash;
		if(NameHash == ShiftLeftBy         .NameHash) return ShiftLeftBy         .NameHash;
		if(NameHash == ShiftRightBy        .NameHash) return ShiftRightBy        .NameHash;
		if(NameHash == ShiftRightUnsignedBy.NameHash) return ShiftRightUnsignedBy.NameHash;
		if(NameHash == bitwiseAndTo               .NameHash) return bitwiseAndTo               .NameHash;
		if(NameHash == bitwiseOrTo                .NameHash) return bitwiseOrTo                .NameHash;
		if(NameHash == bitwiseXorTo               .NameHash) return bitwiseXorTo               .NameHash;
		if(NameHash == ANDTo               .NameHash) return ANDTo               .NameHash;
		if(NameHash == ORTo                .NameHash) return ORTo                .NameHash;
		if(NameHash == XORTo               .NameHash) return XORTo               .NameHash;
		// Those that are added later
		if(this.OperatorProviders != null) {
			OperatorProvider OP = this.OperatorProviders.get(NameHash);
			if(OP != null) return NameHash;
		}
		// Add more here
		throw new RuntimeException("Unknown operator provider name ("+pName+").");
	}
	
	// Utilities -------------------------------------------------------------------------------------------------------
	
	/** Create a new incremental assignment expression from a read expression */
	static public Expression newAssExpr(Engine pEngine, Expression pReadExpr, String pOperName, Object pValue) {
		if(pReadExpr == null) return null;
		
		Inst_Assignment IIA = (Inst_Assignment)pEngine.getInstruction(Inst_Assignment.Name);
		if(IIA == null) return null;
		
		String ReadInstName = pReadExpr.getInstructionName(pEngine);
		if(ReadInstName == null) return null;
		
		int SHash = IIA.getSourceHashOf(ReadInstName);
		int OHash = IIA.getOperatorHashOf(pOperName);
		
		Object[] Params = new Object[pReadExpr.getParamCount() + 3];
		Params[0] = SHash;
		Params[1] = OHash;
		Params[2] = pValue;
		for(int i = pReadExpr.getParamCount(); --i >= 0; ) Params[i + 3] = pReadExpr.getParam(i);
		
		return pEngine.getExecutableManager().newExpr(pReadExpr.getColumn(), pReadExpr.getLineNumber(), Inst_Assignment.Name, (Object[])Params);
	}

	static private final int IsExistKind = 0;
	static private final int GetTypeKind = 1;
	static private final int IsAssKind   = 2;
	static private final int IsConstKind = 3;
	
	/** Create a new incremental assignment expression from a read expression */
	static private Expression newExpr(int pKind, Engine pEngine, Expression pReadExpr, String pOperName) {
		if(pReadExpr == null) return null;
		
		Inst_Assignment IIA = (Inst_Assignment)pEngine.getInstruction(Inst_Assignment.Name);
		if(IIA == null) return null;
		
		String ReadInstName = pReadExpr.getInstructionName(pEngine);
		if(ReadInstName == null) return null;
		
		SourceProvider SP = IIA.getSourceProvider(IIA.getSourceHashOf(ReadInstName));
		if(SP == null) return null;
		
		String InstName = null;
		if(     pKind == IsExistKind) InstName = SP.getGetTypeInstructionName();
		else if(pKind == GetTypeKind) InstName = SP.getGetTypeInstructionName();
		else if(pKind == IsAssKind)   InstName = SP.getIsWritableInstructionName();
		else if(pKind == IsConstKind) InstName = SP.getIsConstantInstructionName();
		if(InstName == null) return null;
		
		Instruction Inst = pEngine.getInstruction(InstName);
		if(Inst == null) return null;
		// If the new expression need more parameter than the one we can get (from ReadExpr), don't know what to do 
		if(Inst.ISpec.getParameterCount() > pReadExpr.getParamCount()) return null;

		Object[] Ps = new Object[Inst.ISpec.getParameterCount()];
		for(int i = Ps.length; --i >= 0; ) Ps[i] = pReadExpr.getParam(i);
		
		return pEngine.getExecutableManager().newExpr(pReadExpr.getColumn(), pReadExpr.getLineNumber(), InstName, Ps);
	}
	
	/** Create a new incremental assignment expression from a read expression */
	static public Expression newSetExpr(Engine pEngine, Expression pReadExpr, Object pValue) {
		if(pReadExpr == null) return null;
		
		Inst_Assignment IIA = (Inst_Assignment)pEngine.getInstruction(Inst_Assignment.Name);
		if(IIA == null) return null;
		
		String ReadInstName = pReadExpr.getInstructionName(pEngine);
		if(ReadInstName == null) return null;
		
		SourceProvider SP = IIA.getSourceProvider(IIA.getSourceHashOf(ReadInstName));
		if(SP == null) return null;
		
		Object[] Params = new Object[1 + pReadExpr.getParamCount()];
		Params[Params.length - 1] = pValue;
		for(int i = pReadExpr.getParamCount(); --i >= 0; ) Params[i] = pReadExpr.getParam(i);
		
		return pEngine.getExecutableManager().newExpr(pReadExpr.getColumn(), pReadExpr.getLineNumber(), SP.getSetInstructionName(), Params);
	}
	
	/** Create a new incremental assignment expression from a read expression */
	static public Expression newIsExistExpr(Engine pEngine, Expression pReadExpr, String pOperName) {
		return newExpr(IsExistKind, pEngine, pReadExpr, pOperName);
	}
	/** Create a new incremental assignment expression from a read expression */
	static public Expression newIsAssignableExpr(Engine pEngine, Expression pReadExpr, String pOperName) {
		Expression         Expr = newExpr(IsAssKind,   pEngine, pReadExpr, pOperName);
		if(Expr == null) { Expr = newExpr(IsConstKind, pEngine, pReadExpr, pOperName);
			if(Expr != null) Expr = pEngine.getExecutableManager().newExpr("NOT", Expr);
		}
		return Expr;
	}
	/** Create a new incremental assignment expression from a read expression */
	static public Expression newIsConstantExpr(Engine pEngine, Expression pReadExpr, String pOperName) {
		Expression         Expr = newExpr(IsConstKind, pEngine, pReadExpr, pOperName);
		if(Expr == null) { Expr = newExpr(IsAssKind,   pEngine, pReadExpr, pOperName);
			if(Expr != null) Expr = pEngine.getExecutableManager().newExpr("NOT", Expr);
		}
		return Expr;
	}
	/** Create a new incremental assignment expression from a read expression */
	static public Expression newGetTypeExpr(Engine pEngine, Expression pReadExpr, String pOperName) {
		return newExpr(GetTypeKind, pEngine, pReadExpr, pOperName);
	}

	// Accessory and sub classes ---------------------------------------------------------------------------------------
	/** Provider for source */
	static abstract public class SourceProvider {
		String TheName = null;
		/** Returns the name of this source provider */
		final public String getName() {
			if(this.TheName == null) {
				this.TheName = this.getTheName();
				if(this.TheName == null) throw new RuntimeException("The name of the source provider cannot be null.");
			}
			return this.TheName;
		}
		
		/** Returns the name of this source provider */
		abstract protected String getTheName();
		
		// Set and Get -------------------------------------------------------------------------------------------------
		
		/** Process */ abstract protected Object process(Context pContext, Object[] pParams, boolean pIsSet, Object V);
		
		/** Set the value */ protected Object setValue(Context C, Object[] Ps, Object V) { return this.process(C, Ps, true,  V);    }
		/** Get the value */ protected Object getValue(Context C, Object[] Ps)           { return this.process(C, Ps, false, null); }
		
		// Other instruction name --------------------------------------------------------------------------------------
		
		/** Returns the name of the get instruction for getting the value of this source */
		abstract public String getGetInstructionName();
		/** Returns the name of the get instruction for getting the value of this source */
		abstract public String getSetInstructionName();

		/** Returns the name of the instruction for checking if the source exist */
		abstract public String getIsExistInstructionName();
		/** Returns the name of the instruction for checking if the source is assignable */
		abstract public String getIsWritableInstructionName();
		/** Returns the name of the instruction for checking if the source is a constant */
		abstract public String getIsConstantInstructionName();
		/** Returns the name of the instruction for getting the type of the source */
		abstract public String getGetTypeInstructionName();
		
		// Compile time methods ----------------------------------------------------------------------------------------
		
		/** Get the return type of this source */
		protected TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) { return null; }

		/**
		 * Ensures that the source referred by the expression exist.
		 * 
		 * The implementation of this method must report error and return false if the source is not writable
		 **/
		abstract protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr);
		/**
		 * Ensures that the expression (if it is a IncAssignment) is really writable.
		 * 
		 * The implementation of this method must report error and return false if the source is not writable
		 **/
		abstract protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr);
		
	}
	
	static abstract public class OperatorProvider {
		String TheName = null;
		/** Returns the name of this source provider */
		final public String getName() {
			if(this.TheName == null) {
				this.TheName = this.getTheName();
				if(this.TheName == null) throw new RuntimeException("The name of the source provider cannot be null.");
			}
			return this.TheName;
		}
		/** Returns the name of this source provider */
		abstract protected String getTheName();
		/** Checks if this processor required to be before or after */
		protected boolean isProcessBefore() { return true; }
		/** Returns the parameter of the operation from the array of parameters */
		protected Object  getParam(Object[] pParams, Context pContext) { return pParams[Inst_Assignment.IndexForValue]; }
		/** Process the value */
		abstract protected Object process(Context pContext, Object O1, Object O2);
		/** Returns the expect value of the source */
		protected Type getSourceType(CompileProduct pCProduct) {
			return TKJava.TNumber;
		}
		/** Get the param typeref */
		protected boolean isValidSourceTypeRef(CompileProduct pCProduct, TypeRef pSourceTypeRef) {
			try {
				Type T = pCProduct.getEngine().getTypeManager().getTypeFromRefNoCheck(null, pSourceTypeRef);
				return this.getSourceType(pCProduct).canBeAssignedByInstanceOf(T);
			} catch(Exception E) { return false; }
			
		}
		/** Get the param typeref - Returns null if the param is not of a valid parameter type */
		protected TypeRef getParamTypeRef(Expression pExpr, CompileProduct pCProduct) {
			TypeRef TR = pCProduct.getReturnTypeRefOf(pExpr.getParam(IndexForValue));
			if(TR == null) return this.getSourceType(pCProduct).getTypeRef();
			
			try { pCProduct.getEngine().getTypeManager().ensureTypeExist(null, TR); }
			catch(Exception E) { return this.getSourceType(pCProduct).getTypeRef(); }
			// Since the parameter type should (regularly) be the same type with the source
			if(this.isValidSourceTypeRef(pCProduct, TR)) return TR;
			return this.getSourceType(pCProduct).getTypeRef();
		}
		/**
		 * Checks if the type of the parameters of the expression are correct.
		 * Returns true or false if this is correct or not correct.
		 * This method does not need to report error or warning, the one that call it will do.
		 **/
		protected boolean ensureParamCorrect(CompileProduct pCProduct, Expression pExpr, TypeRef pSourceTypeRef,
				TypeRef pParamTypeRef) {
			// Both types must be equals
			if(pSourceTypeRef == pParamTypeRef) return true;
			if(pSourceTypeRef == null)          return false;
			
			if(pParamTypeRef == null) {
				if(pExpr.getParam(Inst_Assignment.IndexForValue) != null)
					return false;
			} else {
				MType MT = pCProduct.getEngine().getTypeManager();
				Type SourceType = this.getSourceType(pCProduct);
				if(Boolean.FALSE.equals(MT.mayTypeRefBeCastedTo(SourceType.getTypeRef(), pSourceTypeRef))) return false;
				if(Boolean.FALSE.equals(MT.mayTypeRefBeCastedTo(SourceType.getTypeRef(), pParamTypeRef ))) return false;
			}
			
			// And the source type should be valid
			return this.isValidSourceTypeRef(pCProduct, pSourceTypeRef);
		}
	}
	
	// Implementation of the configuration classes ---------------------------------------------------------------------
	
	// Sources ------------------------------------------------------------------------------------
	
	/** Increment access to local variable */
	static public class SPLocalVar extends SourceProvider {
		
		static final public String Name     = "LocalVariable";
		static final public int    NameHash = UString.hash(Name);
		
		static final SPLocalVar Instance = new SPLocalVar();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }

		/**{@inheritDoc}*/ @Override protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
			Object O = Ps[0]; if(!(O instanceof String)) throw new RuntimeException("Invalid source provider parameter: String is need at the 0th index. ("+O+")");
			
			String VName = (String)O;
			if(IsSet) return C.setVariableValue(VName, V);
			else      return C.getVariableValue(VName);
		}
		
		/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return Inst_GetVarValue.Name; }
		/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return Inst_SetVarValue.Name; }
		/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return Inst_IsVarExist.Name;  }
		/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return null;  }
		/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return Inst_IsConstant.Name;  }
		/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return Inst_GetVarType.Name;  }

		/**{@inheritDoc}*/ @Override protected TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(IndexSourceParams); if(!(O instanceof String)) return TKJava.TAny.getTypeRef();
			return pCProduct.getVariableTypeRef((String)O);
		}
		/**{@inheritDoc}*/ @Override protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O = pExpr.getParam(IndexSourceParams); if(!(O instanceof String)) return false;
			
			String VName = (String)O;

			if(!pCProduct.isVariableExist(VName) &&
			   !pCProduct.isGlobalVariableExist(VName) &&
			   !pCProduct.isEngineVariableExist(VName)) {
				ReportCompileProblem(
						"SPLocalVar:563", String.format("The variable `%s` does not exist", VName),
						pExpr, pCProduct, true, false);
				return false;
			}
			
			return true;
		}
		
		/**{@inheritDoc}*/ @Override protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O = pExpr.getParam(IndexSourceParams); if(!(O instanceof String)) return false;
			String VName = (String)O;

			if(!pCProduct.isVariableExist(VName) &&
			   !pCProduct.isGlobalVariableExist(VName) &&
			   !pCProduct.isEngineVariableExist(VName)) {
				ReportCompileProblem(
						"SPLocalVar:580", String.format("The variable `%s` does not exist", VName),
						pExpr, pCProduct, true, false);
				return pCProduct.isCompileTimeCheckingNone();
			}

			if(pCProduct.isConstant(VName)) 
				return ReportCompileProblem(
						"SPLocalVar:587", String.format("The variable `%s` is a constant", VName),
						pExpr, pCProduct, false, false);

			if(pExpr.getParam(IndexForValue) != null) {
				// Check the type compatibility
				TypeRef TR   = pCProduct.getVariableTypeRef(VName);
				TypeRef TRef = pCProduct.getReturnTypeRefOf(pExpr.getParam(IndexForValue));
				Boolean MayMatch = pCProduct.getEngine().getTypeManager().mayTypeRefBeCastedTo(TR, TRef);
				if(!Boolean.TRUE.equals(MayMatch))
					return ReportCompileProblem(
							"SPLocalVar:592", String.format("Imcompatible type `%s` (%s to %s)", VName, TRef, TR),
							pExpr, pCProduct, (MayMatch == null), false);
			}

			return true;
		}
	}
	
	/** Increment access to parent variable by count */
	static public class SPParentVarByCount extends SourceProvider {
		
		static final public String Name     = "ParentVarByCount";
		static final public int    NameHash = UString.hash(Name);
		
		static final SPParentVarByCount Instance = new SPParentVarByCount();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }

		/**{@inheritDoc}*/ @Override protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
			Object O0 = Ps[0]; if(!(O0 instanceof Integer)) throw new RuntimeException("Invalid source provider parameter: Integer is need at the 0th index. ("+O0+")");
			Object O1 = Ps[1]; if(!(O1 instanceof  String)) throw new RuntimeException("Invalid source provider parameter: String is need at the 1st index. ("+O1+")");

			int     I = (Integer)O0;
			String  N = (String)O1;
			if(IsSet) return C.setParentVariableValue(I, N, V);
			else      return C.getParentVariableValue(I, N);
		}
		
		/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return Inst_GetParentVarValue.Name; }
		/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return Inst_SetParentVarValue.Name; }
		/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return Inst_IsParentVarExist.Name;  }
		/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return null; }
		/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return Inst_IsParentVarConstant.Name; }
		/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return Inst_GetParentVarType.Name;    }
		
		/**{@inheritDoc}*/ @Override protected TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O0 = pExpr.getParam(IndexSourceParams);
			Object O1 = pExpr.getParam(IndexSourceParams + 1);
			if(!(O0 instanceof Integer) || !(O1 instanceof String)) return TKJava.TAny.getTypeRef();
			return pCProduct.getParentVariableTypeRef(((Integer)O0), (String)O1);
		}

		/**{@inheritDoc}*/ @Override protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O0 = pExpr.getParam(IndexSourceParams);
			Object O1 = pExpr.getParam(IndexSourceParams + 1);
			if(!(O0 instanceof Integer) || !(O1 instanceof String)) return false;
			int    VCount = ((Integer)O0).intValue();
			String VName  = (String)O1;

			if(!pCProduct.isParentVariableExist(VCount, VName)) {
				StringBuilder SB = new StringBuilder(); for(int i = VCount; --i >= 0; ) SB.append("parent.");
				ReportCompileProblem(
						"SPParentVarByCount:648", String.format("The parent variable `%s%s` does not exist", SB, VName),
						pExpr, pCProduct, true, false);
				return false;
			}
			
			return true;
		}

		/**{@inheritDoc}*/ @Override protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O0 = pExpr.getParam(IndexSourceParams);
			Object O1 = pExpr.getParam(IndexSourceParams + 1);
			if(!(O0 instanceof Integer) || !(O1 instanceof String)) return false;
			int    VCount = ((Integer)O0).intValue();
			String VName  = (String)O1;

			if(!pCProduct.isParentVariableExist(VCount, VName)) {
				StringBuilder SB = new StringBuilder();
				for(int i = VCount; --i >= 0; ) SB.append("parent.");
				ReportCompileProblem(
						"SPParentVarByCount:668", String.format("The parent variable `%s%s` does not exist", SB, VName),
						pExpr, pCProduct, true, false);
				return pCProduct.isCompileTimeCheckingNone();
			}

			if(pCProduct.isParentVariableConstant(VCount, VName)) {
				StringBuilder SB = new StringBuilder();
				for(int i = VCount; --i >= 0; ) SB.append("parent.");
				return ReportCompileProblem(
						"SPParentVarByCount:677", String.format("The parent variable `%s%s` is a constant", SB, VName),
						pExpr, pCProduct, false, false);
			}

			if(pExpr.getParam(IndexForValue) != null) {
				TypeRef TR   = pCProduct.getParentVariableTypeRef(VCount, VName);
				TypeRef TRef = pCProduct.getReturnTypeRefOf(pExpr.getParam(IndexForValue));
				Boolean MayMatch = pCProduct.getEngine().getTypeManager().mayTypeRefBeCastedTo(TR, TRef);
				if(!Boolean.TRUE.equals(MayMatch)) {
					StringBuilder SB = new StringBuilder();
					for(int i = VCount; --i >= 0; ) SB.append("parent.");
					return ReportCompileProblem(
							"SPParentVarByCount:592", String.format("Imcompatible type `%s%s` (%s to %s)", SB.toString(), VName, TRef, TR),
							pExpr, pCProduct, (MayMatch == null), false);
				}
			}
			
			return true;
		}
	}
	
	/** Increment access to parent variable by StackName */
	static public class SPParentVarByName extends SourceProvider {
		
		static final public String Name     = "ParentVarByName";
		static final public int    NameHash = UString.hash(Name);
		
		static final SPParentVarByName Instance = new SPParentVarByName();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }

		/**{@inheritDoc}*/ @Override protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
			Object O0 = Ps[0]; if(!(O0 instanceof String)) throw new RuntimeException("Invalid source provider parameter: String is need at the 3rd index. ("+O0+")");
			Object O1 = Ps[1]; if(!(O1 instanceof String)) throw new RuntimeException("Invalid source provider parameter: String is need at the 4rd index. ("+O1+")");
			
			String S = (String)O0;
			String N = (String)O1;
			if(IsSet) return C.setParentVariableValue(S, N, V);
			else      return C.getParentVariableValue(S, N);
		}
		
		/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return Inst_GetParentVarValueByStackName.Name; }
		/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return Inst_SetParentVarValueByStackName.Name; }
		/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return Inst_IsParentVarExistByStackName.Name;  }
		/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return null; }
		/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return Inst_IsParentVarConstantByStackName.Name; }
		/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return Inst_GetParentVarTypeByStackName.Name;    }
		
		/**{@inheritDoc}*/ @Override protected TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O0 = pExpr.getParam(IndexSourceParams);
			Object O1 = pExpr.getParam(IndexSourceParams + 1);
			if(!(O0 instanceof String) || !(O1 instanceof String)) return TKJava.TAny.getTypeRef();
			return pCProduct.getParentVariableTypeRef(((String)O0), (String)O1);
		}

		/**{@inheritDoc}*/ @Override protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O0 = pExpr.getParam(IndexSourceParams);
			Object O1 = pExpr.getParam(IndexSourceParams + 1);
			if(!(O0 instanceof String) || !(O1 instanceof String)) return false;
			String SName = (String)O0;
			String VName  =(String)O1;

			if(!pCProduct.isParentVariableExist(SName, VName)) {
				ReportCompileProblem(
						"SPParentVarByName:743", String.format("The parent variable `%s.%s` does not exist", SName, VName),
						pExpr, pCProduct, true, false);
				return false;
			}
			
			return true;
		}

		/**{@inheritDoc}*/ @Override protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O0 = pExpr.getParam(IndexSourceParams);
			Object O1 = pExpr.getParam(IndexSourceParams + 1);
			if(!(O0 instanceof String) || !(O1 instanceof String)) return false;
			String SName = (String)O0;
			String VName  =(String)O1;

			if(!pCProduct.isParentVariableExist(SName, VName)) {
				ReportCompileProblem(
						"SPParentVarByName:748", String.format("The parent variable `%s.%s` does not exist", SName, VName),
						pExpr, pCProduct, true, false);
				return pCProduct.isCompileTimeCheckingNone();
			}

			if(pCProduct.isParentVariableConstant(SName, VName)) {
				return ReportCompileProblem(
						"SPParentVarByName:755", String.format("The parent variable `%s.%s` is a constant", SName, VName),
						pExpr, pCProduct, false, false);
			}

			if(pExpr.getParam(IndexForValue) != null) {
				TypeRef TR   = pCProduct.getParentVariableTypeRef(SName, VName);
				TypeRef TRef = pCProduct.getReturnTypeRefOf(pExpr.getParam(IndexForValue));
				Boolean MayMatch = pCProduct.getEngine().getTypeManager().mayTypeRefBeCastedTo(TR, TRef);
				if(!Boolean.TRUE.equals(MayMatch)) {
					return ReportCompileProblem(
							"SPParentVarByName:777", String.format("Imcompatible type `%s.%s` (%s to %s)", SName, VName, TRef, TR),
							pExpr, pCProduct, (MayMatch == null), false);
				}
			}
			
			return true;
		}
	}
	
	/** Increment access to Global variable */
	static public class SPGlobalVar extends SourceProvider {
		
		static final public String Name     = "GlobalVariable";
		static final public int    NameHash = UString.hash(Name);
		
		static final SPGlobalVar Instance = new SPGlobalVar();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }

		/**{@inheritDoc}*/ @Override protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
			Object O0 = Ps[0]; if(!(O0 instanceof String)) throw new RuntimeException("Invalid source provider parameter: String is need at the 3rd index. ("+O0+")");
			if(IsSet) return C.setGlobalVariableValue((String)O0, V);
			else      return C.getGlobalVariableValue((String)O0);
		}

		/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return Inst_GetGlobalVarValue.Name; }
		/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return Inst_SetGlobalVarValue.Name; }
		/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return Inst_IsGlobalVarExist.Name;  }
		/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return null; }
		/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return Inst_IsGlobalVarConstant.Name; }
		/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return Inst_GetGlobalVarType.Name;    }
		
		/**{@inheritDoc}*/ @Override
		protected TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O0 = pExpr.getParam(IndexSourceParams);
			if(!(O0 instanceof String)) return TKJava.TAny.getTypeRef();
			return pCProduct.getGlobalVariableTypeRef((String)O0);
		}

		/**{@inheritDoc}*/ @Override
		protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O0 = pExpr.getParam(IndexSourceParams);
			if(!(O0 instanceof String)) return false;
			String VName = (String)O0;

			if(!pCProduct.isGlobalVariableExist(VName)) {
				ReportCompileProblem(
						"SPGlobalVar:801", String.format("The global variable `%s` does not exist", VName),
						pExpr, pCProduct, true, false);
				return false;
			}
			
			return true;
		}

		/**{@inheritDoc}*/ @Override
		protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O0 = pExpr.getParam(IndexSourceParams);
			if(!(O0 instanceof String)) return false;
			String VName = (String)O0;

			if(!pCProduct.isGlobalVariableExist(VName)) {
				ReportCompileProblem(
						"SPGlobalVar:817", String.format("The global variable `%s` does not exist", VName),
						pExpr, pCProduct, true, false);
				return pCProduct.isCompileTimeCheckingNone();
			}

			if(pCProduct.isGlobalVariableConstant(VName)) {
				return ReportCompileProblem(
						"SPGlobalVar:824", String.format("The global variable `%s` is a constant", VName),
						pExpr, pCProduct, false, false);
			}

			if(pExpr.getParam(IndexForValue) != null) {
				TypeRef TR   = pCProduct.getGlobalVariableTypeRef(VName);
				TypeRef TRef = pCProduct.getReturnTypeRefOf(pExpr.getParam(IndexForValue));
				Boolean MayMatch = pCProduct.getEngine().getTypeManager().mayTypeRefBeCastedTo(TR, TRef);
				if(!Boolean.TRUE.equals(MayMatch)) {
					return ReportCompileProblem(
							"SPGlobalVar:855", String.format("Imcompatible type `%s` (%s to %s)", VName, TRef, TR),
							pExpr, pCProduct, (MayMatch == null), false);
				}
			}
			
			return true;
		}
	}
	
	/** Increment access to Engine variable */
	static public class SPEngineVar extends SourceProvider {
		
		static final public String Name     = "EngineVariable";
		static final public int    NameHash = UString.hash(Name);
		
		static final SPEngineVar Instance = new SPEngineVar();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }

		/**{@inheritDoc}*/ @Override protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
			Object O0 = Ps[0]; if(!(O0 instanceof String)) throw new RuntimeException("Invalid source provider parameter: String is need at the 3rd index. ("+O0+")");
			if(IsSet) return C.setEngineVariableValue((String)O0, V);
			else      return C.getEngineVariableValue((String)O0);
		}

		/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return Inst_GetEngineVarValue.Name; }
		/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return Inst_SetEngineVarValue.Name; }
		/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return Inst_IsEngineVarExist.Name;  }
		/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return null; }
		/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return Inst_IsEngineVarConstant.Name; }
		/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return Inst_GetEngineVarType.Name;    }
		
		/**{@inheritDoc}*/ @Override
		protected TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O0 = pExpr.getParam(IndexSourceParams);
			if(!(O0 instanceof String)) return TKJava.TAny.getTypeRef();
			return pCProduct.getEngineVariableTypeRef((String)O0);
		}

		/**{@inheritDoc}*/ @Override
		protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O0 = pExpr.getParam(IndexSourceParams);
			if(!(O0 instanceof String)) return false;
			String VName = (String)O0;

			if(!pCProduct.isEngineVariableExist(VName)) {
				ReportCompileProblem(
						"SPEngineVar:853", String.format("The engine variable `%s` does not exist", VName),
						pExpr, pCProduct, true, false);
				return false;
			}
			
			return true;
		}

		/**{@inheritDoc}*/ @Override
		protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
			if(pExpr == null) return false;
			Object O0 = pExpr.getParam(IndexSourceParams);
			if(!(O0 instanceof String)) return false;
			
			String VName = (String)O0;

			if(!pCProduct.isEngineVariableExist(VName)) {
				ReportCompileProblem(
						"SPEngineVar:881", String.format("The engine variable `%s` does not exist", VName),
						pExpr, pCProduct, true, false);
				return pCProduct.isCompileTimeCheckingNone();
			}

			if(pCProduct.isEngineVariableConstant(VName)) {
				return ReportCompileProblem(
						"SPEngineVar:887", String.format("The engine variable `%s` is a constant", VName),
						pExpr, pCProduct, false, false);
			}

			if(pExpr.getParam(IndexForValue) != null) {
				TypeRef TR   = pCProduct.getGlobalVariableTypeRef(VName);
				TypeRef TRef = pCProduct.getReturnTypeRefOf(pExpr.getParam(IndexForValue));
				Boolean MayMatch = pCProduct.getEngine().getTypeManager().mayTypeRefBeCastedTo(TR, TRef);
				if(!Boolean.TRUE.equals(MayMatch)) {
					return ReportCompileProblem(
							"SPEngineVar:934", String.format("Imcompatible type `%s` (%s to %s)", VName, TRef, TR),
							pExpr, pCProduct, (MayMatch == null), false);
				}
			}
			
			return true;
		}
	}
	
	/** Increment access to array element */
	static public class SPArrayElement extends SourceProvider {
		
		static final public String Name     = "ArrayElement";
		static final public int    NameHash = UString.hash(Name);
		
		static final SPArrayElement Instance = new SPArrayElement();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		

		/**{@inheritDoc}*/ @Override protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
			Object O0 = Ps[0]; if(!UArray.isArrayInstance(O0)) throw new RuntimeException("Invalid source provider parameter: Array is need at the 1st index. ("+O0+")");
			Object O1 = Ps[1]; if(!(O1 instanceof Integer))    throw new RuntimeException("Invalid source provider parameter: Integer is need at the 2nd index. ("+O1+")");
			if(IsSet) return UArray.set(O0, (Integer)O1, V);
			else      return UArray.get(O0, (Integer)O1);
		}
		
		/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return Inst_GetArrayElementAt.Name; }
		/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return Inst_SetArrayElementAt.Name; }
		/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return null; }
		/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return null; }
		/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return null; }
		/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return Inst_GetComponentTypeArrayObject.Name; }
		
		/**{@inheritDoc}*/ @Override
		protected TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O0 = pExpr.getParam(IndexSourceParams);
			TypeRef TR = pCProduct.getReturnTypeRefOf(O0);
			if(TR == null) return TKJava.TAny.getTypeRef();
			
			try{ pCProduct.getEngine().getTypeManager().ensureTypeInitialized(TR); }
			catch(Exception E) { return TKJava.TAny.getTypeRef(); }
			
			Type T = TR.getTheType();
			if(T instanceof TKArray.TArray) return ((TKArray.TArray)T).getContainTypeRef();
			else                            return TKJava.TAny.getTypeRef();
		}
		/**{@inheritDoc}*/ @Override
		protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {

			if(pExpr.getParam(IndexForValue) != null) {
				TypeRef TR   = this.getReturnTypeRef(pExpr, pCProduct);
				TypeRef TRef = pCProduct.getReturnTypeRefOf(pExpr.getParam(IndexForValue));
				Boolean MayMatch = pCProduct.getEngine().getTypeManager().mayTypeRefBeCastedTo(TR, TRef);
				if(!Boolean.TRUE.equals(MayMatch)) {
					return ReportCompileProblem(
							"SPArrayElement:987", String.format("Imcompatible type (%s to %s)", TRef, TR),
							pExpr, pCProduct, (MayMatch == null), false);
				}
			}
			return true;
		}
		/**{@inheritDoc}*/ @Override
		protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
			return true;
		}
	}
	
	// Incremental Access -------------------------------------------------------------------------
	
	static public class IncBefore extends OperatorProvider {
		
		static final public String Name     = "IncBefore";
		static final public int    NameHash = UString.hash(Name);
		
		static final IncBefore Instance = new IncBefore();
		
		/**{@inheritDoc}*/ @Override protected String  getTheName() { return Name; }
		/**{@inheritDoc}*/ @Override protected Object  process(Context pContext, Object O1, Object O2) { return UNumber.plus((Number)O1, 1); }
		
		/**{@inheritDoc}*/ @Override protected Object  getParam(       Object[] pParams, Context pContext)         { return 1; }
		/**{@inheritDoc}*/ @Override protected TypeRef getParamTypeRef(Expression pExpr, CompileProduct pCProduct) { return TKJava.TNumber.getTypeRef(); }
	}
	static public class DecBefore extends OperatorProvider {
		
		static final public String Name     = "DecBefore";
		static final public int    NameHash = UString.hash(Name);
		
		static final DecBefore Instance = new DecBefore();
		
		/**{@inheritDoc}*/ @Override protected String  getTheName() { return Name; }
		/**{@inheritDoc}*/ @Override protected Object  process(Context pContext, Object O1, Object O2) { return UNumber.plus((Number)O1, -1); }
		
		/**{@inheritDoc}*/ @Override protected Object  getParam(       Object[] pParams, Context pContext)         { return -1; }
		/**{@inheritDoc}*/ @Override protected TypeRef getParamTypeRef(Expression pExpr, CompileProduct pCProduct) { return TKJava.TNumber.getTypeRef(); }
	}
	static public class IncAfter extends OperatorProvider {
		
		static final public String Name     = "IncAfter";
		static final public int    NameHash = UString.hash(Name);
		
		static final IncAfter Instance = new IncAfter();
		
		/**{@inheritDoc}*/ @Override protected String  getTheName()         { return Name; }
		/**{@inheritDoc}*/ @Override protected boolean isProcessBefore() { return false; }
		/**{@inheritDoc}*/ @Override protected Object  process(Context pContext, Object O1, Object O2) { return UNumber.plus((Number)O1, 1); }
		
		/**{@inheritDoc}*/ @Override protected Object  getParam(       Object[] pParams, Context pContext)         { return 1; }
		/**{@inheritDoc}*/ @Override protected TypeRef getParamTypeRef(Expression pExpr, CompileProduct pCProduct) { return TKJava.TNumber.getTypeRef(); }
	}
	static public class DecAfter extends OperatorProvider {
		
		static final public String Name     = "DecAfter";
		static final public int    NameHash = UString.hash(Name);
		
		static final DecAfter Instance = new DecAfter();
		
		/**{@inheritDoc}*/ @Override protected String  getTheName() { return Name; }
		/**{@inheritDoc}*/ @Override protected boolean isProcessBefore() { return false; }
		/**{@inheritDoc}*/ @Override protected Object  process(Context pContext, Object O1, Object O2) { return UNumber.plus((Number)O1, -1); }
		
		/**{@inheritDoc}*/ @Override protected Object  getParam(       Object[] pParams, Context pContext)         { return -1; }
		/**{@inheritDoc}*/ @Override protected TypeRef getParamTypeRef(Expression pExpr, CompileProduct pCProduct) { return TKJava.TNumber.getTypeRef(); }
	}
	
	static public class AppendTo extends OperatorProvider {
		
		static final public String Name     = "AppendTo";
		static final public int    NameHash = UString.hash(Name);
		
		static final AppendTo Instance = new AppendTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return pContext.getEngine().toString(pContext, O1) + pContext.getEngine().toString(pContext, O2);
		}
		
		/**{@inheritDoc}*/ @Override protected Type getSourceType(CompileProduct pCProduct) { return TKJava.TString; }
	}
	
	static public class AddTo extends OperatorProvider {
		
		static final public String Name     = "AddTo";
		static final public int    NameHash = UString.hash(Name);
		
		static final AddTo Instance = new AddTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.plus((Number)O1, (Number)O2);
		}
	}
	static public class SubtractFrom extends OperatorProvider {
		
		static final public String Name     = "SubtractFrom";
		static final public int    NameHash = UString.hash(Name);
		
		static final SubtractFrom Instance = new SubtractFrom();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.subtract((Number)O1, (Number)O2);
		}
	}
	static public class MulTo extends OperatorProvider {
		
		static final public String Name     = "MulTo";
		static final public int    NameHash = UString.hash(Name);
		
		static final MulTo Instance = new MulTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.multiply((Number)O1, (Number)O2);
		}
	}
	static public class DivTo extends OperatorProvider {
		
		static final public String Name     = "DivTo";
		static final public int    NameHash = UString.hash(Name);
		
		static final DivTo Instance = new DivTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.divide((Number)O1, (Number)O2);
		}
	}
	static public class ModulusOf extends OperatorProvider {
		
		static final public String Name     = "ModulusOf";
		static final public int    NameHash = UString.hash(Name);
		
		static final ModulusOf Instance = new ModulusOf();
		
		/**{@inheritDoc}*/ @Override
		protected String getTheName() {
			return Name;
		}
		
		/**{@inheritDoc}*/ @Override
		protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.modulus((Number)O1, (Number)O2);
		}
	}
	
	
	static abstract public class ShiftBy extends OperatorProvider {
		
		/**{@inheritDoc}*/ @Override
		protected TypeRef getParamTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(IndexForValue);
			if(O == null) return TKJava.TInteger.getTypeRef();
			TypeRef TR = pCProduct.getReturnTypeRefOf(O);
			
			// Since the parameter type should (regularly) be the same type with the source
			if(TKJava.TInteger.getTypeRef().equals(TR))
				return TR;
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected boolean ensureParamCorrect(CompileProduct pCProduct, Expression pExpr,
				TypeRef pSourceTypeRef, TypeRef pParamTypeRef) {
			// Both types must be equals
			if(pSourceTypeRef == null) return false;
			if(!this.isValidSourceTypeRef(pCProduct, pSourceTypeRef)) return false;
			
			if(pParamTypeRef  == null) {
				Object O = pExpr.getParam(IndexForValue);
				if(O != null) return false;
			} else {
				// And the param must be an int
				if(!TKJava.TInteger.getTypeRef().equals(pParamTypeRef))
					return false;

				// And the source must be a number
				if(!MType.CanTypeRefByAssignableByInstanceOf(null, pCProduct.getEngine(), TKJava.TNumber.getTypeRef(), pSourceTypeRef)) 
					return false;
			}
			
			return true;
		}
	}
	static public class ShiftLeftBy extends ShiftBy {
		
		static final public String Name     = "ShiftLeftBy";
		static final public int    NameHash = UString.hash(Name);
		
		static final ShiftLeftBy Instance = new ShiftLeftBy();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.shiftLeft((Number)O1, (Integer)O2);
		}
	}
	static public class ShiftRightBy extends ShiftBy {
		
		static final public String Name     = "ShiftRightBy";
		static final public int    NameHash = UString.hash(Name);
		
		static final ShiftRightBy Instance = new ShiftRightBy();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.shiftRight((Number)O1, (Integer)O2);
		}
	}
	static public class ShiftRightUnsignedBy extends ShiftBy {
		
		static final public String Name     = "ShiftRightUnsignedBy";
		static final public int    NameHash = UString.hash(Name);
		
		static final ShiftRightUnsignedBy Instance = new ShiftRightUnsignedBy();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.shiftRightUnsigned((Number)O1, (Integer)O2);
		}
	}
	
	// AND, OR, XOR ------------------------------------------------------------------------------------------------
	
	// Number ---------------------------------------------------------------------------------
	
	static public class bitwiseAndTo extends OperatorProvider {
		
		static final public String Name     = "andTo";
		static final public int    NameHash = UString.hash(Name);
		
		static final bitwiseAndTo Instance = new bitwiseAndTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.and((Number)O1, (Number)O2);
		}
	}
	static public class bitwiseOrTo extends OperatorProvider {
		
		static final public String Name     = "orTo";
		static final public int    NameHash = UString.hash(Name);
		
		static final bitwiseOrTo Instance = new bitwiseOrTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.or((Number)O1, (Number)O2);
		}
	}
	static public class bitwiseXorTo extends OperatorProvider {
		
		static final public String Name     = "xorTo";
		static final public int    NameHash = UString.hash(Name);
		
		static final bitwiseXorTo Instance = new bitwiseXorTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
		/**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
			return UNumber.xor((Number)O1, (Number)O2);
		}
	}
	
	// Boolean --------------------------------------------------------------------------------
	
	static public class ANDTo extends OperatorProvider {
		
		static final public String Name     = "ANDTo";
		static final public int    NameHash = UString.hash(Name);
		
	    static final ANDTo Instance = new ANDTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
	    /**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
	        return Boolean.TRUE.equals(O1) && Boolean.TRUE.equals(O2);
	    }
		
		/**{@inheritDoc}*/ @Override protected Type getSourceType(CompileProduct pCProduct) { return TKJava.TBoolean; }
	}
	static public class ORTo extends OperatorProvider {
		
		static final public String Name     = "ORTo";
		static final public int    NameHash = UString.hash(Name);
		
	    static final ORTo Instance = new ORTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
	    /**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
	        return Boolean.TRUE.equals(O1) || Boolean.TRUE.equals(O2);
	    }
		
		/**{@inheritDoc}*/ @Override protected Type getSourceType(CompileProduct pCProduct) { return TKJava.TBoolean; }
	}
	static public class XORTo extends OperatorProvider {
		
		static final public String Name     = "XORTo";
		static final public int    NameHash = UString.hash(Name);
		
	    static final XORTo Instance = new XORTo();
		
		/**{@inheritDoc}*/ @Override protected String getTheName() { return Name; }
		
	    /**{@inheritDoc}*/ @Override protected Object process(Context pContext, Object O1, Object O2) {
	        return Boolean.TRUE.equals(O1) != Boolean.TRUE.equals(O2);
	    }
		
		/**{@inheritDoc}*/ @Override protected Type getSourceType(CompileProduct pCProduct) { return TKJava.TBoolean; }
	}
}
