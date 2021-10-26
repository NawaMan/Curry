package net.nawaman.curry;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;


import net.nawaman.curry.Instructions_Core.*;
import net.nawaman.curry.Instructions_Package.Inst_GetPackage;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.curry.util.Objectable_Curry;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

abstract public class Expression implements Serializable, Objectable_Curry, Executable.Fragment, Executable.Curry {

	static final private long serialVersionUID = -68502068060821634L;
	
	// Constant ---------------------------------------------------------------
	
	/** An empty expression array */
	static final public Expression[] EmptyExpressionArray = new Expression[0];

	// CACHING for optimization -----------------------------------------------
	
	/** Predefine `null` data */  static final public Expression NULL  = new Expr_Data(null);
	
	/** Predefine  `true` data */ static final public Expression TRUE  = new Expr_Data( true);
	/** Predefine `false` data */ static final public Expression FALSE = new Expr_Data(false);
	
	/** Predefine  `0` data */ static final public Expression ZERO      = new Expr_Data( 0);
	/** Predefine  `1` data */ static final public Expression ONE       = new Expr_Data( 1);
	/** Predefine  `2` data */ static final public Expression TWO       = new Expr_Data( 2);
	/** Predefine  `3` data */ static final public Expression THREE     = new Expr_Data( 3);
	/** Predefine  `4` data */ static final public Expression FOUR      = new Expr_Data( 4);
	/** Predefine  `5` data */ static final public Expression FIVE      = new Expr_Data( 5);
	/** Predefine  `6` data */ static final public Expression SIX       = new Expr_Data( 6);
	/** Predefine  `7` data */ static final public Expression SEVEN     = new Expr_Data( 7);
	/** Predefine  `8` data */ static final public Expression EIGHT     = new Expr_Data( 8);
	/** Predefine  `9` data */ static final public Expression NINE      = new Expr_Data( 9);
	/** Predefine `10` data */ static final public Expression TEN       = new Expr_Data(10);
	/** Predefine `-1` data */ static final public Expression MINUS_ONE = new Expr_Data(-1);
	
	// Boolean Value
	/** Returns a no-location boolean data */
	static public Expression newData(boolean pValue) { return pValue?Expression.TRUE:Expression.FALSE; }
	
	// Int Value
	/** Returns a no-location integer data */
	static public Expression newData(int pValue) {
		switch(pValue) {
			case  0: return Expression.ZERO;
			case  1: return Expression.ONE;
			case  2: return Expression.TWO;
			case  3: return Expression.THREE;
			case  4: return Expression.FOUR;
			case  5: return Expression.FIVE;
			case  6: return Expression.SIX;
			case  7: return Expression.SEVEN;
			case  8: return Expression.EIGHT;
			case  9: return Expression.NINE;
			case 10: return Expression.TEN;
			case -1: return Expression.MINUS_ONE;
		}
		return new Expr_Data(pValue);
	}
	
	// To Expression -------------------------------------------------------------------------------
	/**
	 * Ensure the return to be an expression.
	 * 
	 * NOTE: The method will successfully return even if the value is not a serializable. But the result expression will
	 *           not be saved with the compiled code.
	 */
	static public Expression toExpr(Object pValue) {
		return toExpr(-1, -1, pValue);
	}

	/**
	 * Ensure the return to be an expression.
	 * 
	 * NOTE: The method will successfully return even if the value is not a serializable. But the result expression will
	 *           not be saved with the compiled code.
	 */
	static public Expression toExpr(int pCol, int pRow, Object pValue) {
		if(pValue instanceof Expression)                         return (Expression)pValue;
		if((pValue == null) || (pValue instanceof Serializable)) return newData(pCol, pRow, (Serializable)pValue);
		else                                                     return newNonSerializableData(pCol, pRow, pValue);
	}
	
	// New Expression ------------------------------------------------------------------------------
	/** Create a new no-location data of a serializable value (so it can be saved) */
	static public Expression newData(Serializable pValue) { return newData(-1, -1, pValue); }
	
	/** Create a new data with location of a serializable value (so it can be saved) */
	static public Expression newData(int pCol, int pRow, Serializable pValue) {
		return newNonSerializableData(pCol, pRow, pValue);
	}
	
	// No Care ------------------------------------------------------------------------------------- 
	
	/**
	 * Create a new no-location data of a value (don't care if the value is a serializable)
	 * 
	 * NOTE: The method will successfully return even if the value is not a serializable. But the result expression will
	 *           not be saved with the compiled code.
	 */
	static public Expression newDataNoCare(Object pValue) { return newData(-1, -1, pValue); }
	
	/**
	 * Create a new with-location data of a value (don't care if the value is a serializable)
	 * 
	 * NOTE: The method will successfully return even if the value is not a serializable. But the result expression will
	 *           not be saved with the compiled code.
	 */
	static public Expression newData(int pCol, int pRow, Object pValue) {
		if((pValue == null) || (pValue instanceof Serializable)) return newData(pCol, pRow, (Serializable)pValue);
		else                                                     return newNonSerializableData(pCol, pRow, pValue);
	}
	
	// Explicitly Non-Serializable -----------------------------------------------------------------
	
	// Separate this to help preventing developer to unintentionally create a non-serializable object for saving
	/** Create a new no-location data of a non-serializable value (immediate used only - not for saving) */
	static public Expression newNonSerializableData(Object pValue) {
		return newNonSerializableData(-1, -1, pValue);
	}
	/** Create a new no-location data of a non-serializable value (immediate used only - not for saving) */
	static public Expression newNonSerializableData(int pCol, int pRow, Object pValue) {
		int Coordinate = Location.getCoordinate(pCol, pRow);
		// Without location
		if(Coordinate == -1) {
			if(pValue == null) return Expression.NULL;
			else if(pValue instanceof Boolean) {
				return ((Boolean)pValue).booleanValue() ? Expression.TRUE : Expression.FALSE;
			}
			else if(pValue instanceof Integer) {
				return Expression.newData(((Integer)pValue).intValue());
			}
			return new Expr_Data(pValue);
		}
		// With location
		return new Expr_Data_Coordinate(Coordinate, pValue);
	}
	
	/** Create a new no-location data of a value that will not be replaced for run-time optimization */
	static public Expression newUnReplaceData(Engine pEngine, Object pValue) {
		return newUnReplaceData(-1, -1, pEngine, pValue);
	}
	/** Create a new data with location info of a value that will not be replaced for run-time optimization */
	static public Expression newUnReplaceData(int pCol, int pRow, Engine pEngine, Object pValue) {
		Instruction Inst = pEngine.getInstruction(Inst_Data.Name);
		if(Inst == null) throw new NullPointerException("Unknown  instruction '"+Inst_Data.Name+"'.");
		return Inst.newExpression_Coordinate(pCol, pRow, pValue);
	}
	
	/** Create a new no-location data of a type value */
	static public Expression newType(Engine pEngine, Type pType) {
		return newType(-1, -1, pEngine, pType);
	}
	/** Create a new data with location info of a type value */
	static public Expression newType(int pCol, int pRow, Engine pEngine, Type pType) {
		return newType(pCol, pRow, pEngine, pType.getTypeRef());
	}
	/** Create a new no-location data info of a type value */
	static public Expression newType(Engine pEngine, TypeRef pTypeRef) {
		return newType(-1, -1, pEngine, pTypeRef);
	}
	/** Create a new data with location info of a type value */
	static public Expression newType(int pCol, int pRow, Engine pEngine, TypeRef pTypeRef) {
		Instruction Inst = pEngine.getInstruction(Inst_Type.Name);
		if(Inst == null) throw new  NullPointerException("Unknown  instruction '"+Inst_Type.Name+"'.");
		return Inst.newExpression_Coordinate(pCol, pRow, pTypeRef);
	}
	/** Create a new no-location data info of an expression value */
	static public Expression newExpr(Expression pExpr) {
		return newExpr(-1, -1, pExpr);
	}
	/** Create a new data with location info of an expression value */
	static public Expression newExpr(int pCol, int pRow, Expression pExpr) {
		int Coordinate = Location.getCoordinate(pCol, pRow);
		if(Coordinate == -1) return new Expr_Expr(pExpr);
		return new Expr_Expr_Coordinate(Coordinate, pExpr);
	}
	/** Create a new data with location info of an expression value */
	static public Expression newExpr(int[] pCR, Expression pExpr) {
		if((pCR == null) || (pCR.length < 2)) return newExpr(-1, -1, pExpr);
		else                                  return newExpr((pCR[0] < -1)?-1:pCR[0], (pCR[1] < -1)?-1:pCR[1], pExpr);
	}
	
	/** Constructs a new Expression. */
	Expression() {}
	
	/** Returns an instruction hash of the expression or 0 if the expression is a data. */
	public int  getInstructionNameHash() { return 0; }
	/** Changes the hash to an index. */
	       void updateInstructionSingatureHash(int pIndex) {}
	/** Changes the index backup to hash */
		   void updateInstructionSingatureHashBack(Engine pEngine) {}
	
	public Instruction getInstruction(Engine pEngine) {
		return pEngine.getInstruction(null, this.getInstructionNameHash());
	}
	public String getInstructionName(Engine pEngine) {
		Instruction Inst = this.getInstruction(pEngine);
		return (Inst == null)?null:Inst.getName();
	}
	public boolean isInstruction(Engine pEngine, Instruction pInst) {
		Instruction Inst = this.getInstruction(pEngine);
		if(Inst  == pInst)                    return true;
		if((pInst == null) || (Inst == null)) return false;
		return pInst.getNameHash() == Inst.getNameHash();
	}
	public boolean isInstruction(Engine pEngine, String pInstName) {
		if(pInstName == null) return false;
		return this.isInstruction(pEngine, pEngine.getInstruction(pInstName));
	}
	
	/** Returns an instruction hash of the expression or 0 if the expression is a data. */
	static public int  getInstructionNameHash(Expression Expr) { return (Expr == null)?0:Expr.getInstructionNameHash(); }
	
	static public Instruction getInstruction(Expression Expr, Engine pEngine) {
		return (Expr == null)?null:Expr.getInstruction(pEngine);
	}
	static public String getInstructionName(Expression Expr, Engine pEngine) {
		return (Expr == null)?null:Expr.getInstructionName(pEngine);
	}
	static public boolean isInstruction(Expression Expr, Engine pEngine, Instruction pInst) {
		return (Expr == null)?(pInst == null):Expr.isInstruction(pEngine, pInst);
	}
	static public boolean isInstruction(Expression Expr, Engine pEngine, String pInstName) {
		return (Expr == null)?(pInstName == null):Expr.isInstruction(pEngine, pInstName);
	}
   	// Expression --------------------------------------------------------------
	/** Checks if this expression is functional */
	boolean isFunctional() { return false; }
	
	/** Returns the coordinate */
	public int getCoordinate() {
		return -1;
	}
	
	/** Returns the line number that this expression is */
	final public int getLineNumber() { return Location.getRow(this.getCoordinate()); }
	
	/** Returns the column that this expression is */
	final public int getColumn()     { return Location.getCol(this.getCoordinate()); }
	
	final public String getLocation(String pPrefix) {
		int R = this.getLineNumber();
		int C = this.getColumn();
		String Location = "";
		if((R != -1) || (C != -1)) Location = pPrefix + "("+((R == -1)?"-":""+R)+","+((C == -1)?"-":""+C)+")";
		return Location;
	}
	       
	/** Checks if this expression is a data */
	public boolean isData()  { return false; }
	/** Returns the data of the expression if it is a data or null if it is not */
	public Object  getData() { return  null; }
	       
	/** Checks if this expression contains another expression */
	public boolean    isExpr()  { return false; }
	/** Returns the expression contains by this expression */
	public Expression getExpr() { return  null; }
	       
   	/** Returns the number of parameters. */
   	public int    getParamCount()    { return    0; }
   	/** Returns the parameter associated with the position pPos. */
   	public Object getParam(int pPos) { return null; }

   	/** Returns the number of sub-expressions. */
   	public int        getSubExprCount()    { return    0; }
   	/** Returns the sub-expression associated with the position pPos. */
   	public Expression getSubExpr(int pPos) { return null; }
   	
   	// Cloning --------------------------------------------------------------------------
   	/** Make an exact clone of this expression */
   	abstract public Expression makeClone();
   	/** Make a clone of this expression but without line skipping */
   	abstract public Expression makeClone_WithoutCoordinate();
   	/** Make a clone of this expression but with one line skipping */
   	abstract public Expression makeClone_WithCoordinate();
	
   	// Utilities -------------------------------------------------------------------------
   	
	Object[] deepCloneParam(Object[] pParams) {
		if(pParams == null) return null;
		Object[] PRs = new Object[pParams.length];
		for(int i = pParams.length; --i >= 0; ){
			PRs[i] = !(pParams[i] instanceof Expression)
						?pParams[i]
						:((Expression)pParams[i]).makeClone();
		}
		return PRs;
	}
	Expression[] deepCloneSub(Expression[] pSubExpr) {
		if(pSubExpr == null) return null;
		Expression[] SEs = new Expression[pSubExpr.length];
		for(int i = pSubExpr.length; --i >= 0; ){
			SEs[i] = (pSubExpr[i] == null)?null:pSubExpr[i].makeClone();
		}
		return SEs;
	}

	/** Returns Context-Free return type */
	public TypeRef getReturnTypeRef(Engine pEngine) {
		if(pEngine == null) return TKJava.TAny.getTypeRef();
		Instruction I = pEngine.getInstruction(null, this.getInstructionNameHash());
		if(I == null) return TKJava.TAny.getTypeRef();
		return I.getReturnTypeRef();
	}
	/** Returns the context-awareness return type */
	public TypeRef getReturnTypeRef(CompileProduct pCProduct) {
		if((pCProduct == null) || (pCProduct.getEngine() == null)) return TKJava.TAny.getTypeRef();
		Instruction I = pCProduct.getEngine().getInstruction(null, this.getInstructionNameHash());
		if(I == null) return TKJava.TAny.getTypeRef();
		return I.getReturnTypeRef(this, pCProduct);
	}
	
	/**
	 * Checks if the type of the parameters of the expression are correct.
	 * The methods should be responsible for report warning and error.
	 * If the isDynamic flag is set, errors will be threat as warnings.
	 **/
	public boolean ensureParamCorrect(CompileProduct pCProduct) {
		if(pCProduct.isCompileTimeCheckingNone() || this.isData()) return true;
		Instruction Inst = pCProduct.getEngine().getInstruction(null, this.getInstructionNameHash());
		if(Inst == null) {
			pCProduct.reportFatalError("The instructor #"+this.getInstructionNameHash()+" is not found in this engine.", null);
			return false;
		}
		return Inst.ensureParamCorrect(this, pCProduct);
	}
	
	/**
	 * Manipulate the compilation context and return if the compilation should continue.
	 * 
	 * This method will be called before its parameters' manipulateCompileContextFinish is called
	 * 
	 * The methods should be responsible for report warning and error.
	 * If the isDynamic flag is set, errors will be threat as warnings.
	 **/
	public boolean manipulateCompileContextFinish(CompileProduct pCProduct) {
		Instruction Inst = pCProduct.getEngine().getInstruction(null, this.getInstructionNameHash());
		if(Inst == null) {
			pCProduct.reportFatalError("The instructor `#"+this.getInstructionNameHash()+"` is not found this the current engine.", null);
			return false;
		}
		return Inst.manipulateCompileContextFinish(this, pCProduct);
	}
   	
   	// Display -------------------------------------------------------------------------------------
   	
   	// Objectable --------------------------------------------------------------
   	
   	/** Returns the short string representation of the expression. */
   	@Override public String  toString()       { return this.toString(null); }
   	/** Returns the long string representation of the expression. */
   	public           String  toDetail()       { return this.toDetail(null); }
   	/** Checks if O is the same or consider to be the same object with this expression. */	
   	public           boolean is(Object O)     { return this == O;           }
   	/** Checks if O equals to this expression. */	
   	@Override public boolean equals(Object O) { return this == O;           }
   	/** Returns the integer representation of the expression. */
	@Override public int     hash()           { return this.hash(null);     }
   	/** Returns the integer representation of the expression. */
	@Override public int     hashCode()       { return super.hashCode();    }
   	
   	// Objectable with engine --------------------------------------------------
   	
   	/** Returns the short string representation of the expression. */
   	public String toString(Engine pEngine) {
   		if(this instanceof Expr_Expr) {
   			Expression E = ((Expression)this.getData());
   			return "Expression {"+((E == null)?"null":E.toString(pEngine))+"}";
   		}
   		if(this.isData()) {
   			if(pEngine == null) return UObject.toString(this.getData());
   			return pEngine.toString(this.getData());
   		}
   		String InstStr;
   		if(pEngine == null) InstStr = "Expression:" + this.getInstructionNameHash();
   		else {
   			Instruction I = pEngine.getInstruction(this, this.getInstructionNameHash());
   			if(I == null) return "Invalid Instruction";
   			if(I instanceof Instructions_Core.Inst_Data) return pEngine.getDisplayObject(this.getData());
   			if(I instanceof Instructions_Core.Inst_Type) {
   				Object P0 = this.getParam(0); 
   				if(P0 instanceof TypeRef) return UObject.toString(P0) + ".type";
   			}
   			InstStr = I.ISpec.getName();
   		}
   		return InstStr;
   	}
   	/** Returns the long string representation of the expression. */
   	public String toDetail(Engine pEngine) {
   		if(this instanceof Expr_Expr) {
   			Expression E = ((Expression)this.getData());
   			return "Expression {"+((E == null)?"null":E.toDetail(pEngine))+"}";
   		}
   		if(this.isData()) {
   			if(pEngine == null) return UObject.toDetail(this.getData());
   			return pEngine.toDetail(this.getData());
   		}
   		String InstStr;
   		if(pEngine == null) InstStr = "Expression:" + this.getInstructionNameHash();
   		else {
   			Instruction I = pEngine.getInstruction(this, this.getInstructionNameHash());
   			if(I == null) return "Invalid Instruction";
   			if(I instanceof Instructions_Core.Inst_Data) return pEngine.getDisplayObject(this.getData());
   			if(I instanceof Instructions_Core.Inst_Type) {
   				Object P0 = this.getParam(0); 
   				if(P0 instanceof TypeRef) return UObject.toString(P0) + ".type";
   			}
   			InstStr = I.ISpec.getName();
   		}
   		return "@:" + InstStr + Expression.getParamterString(pEngine, this);
   	}
   	
   	/** Returns the integer representation of the expression. */
   	public int hash(Engine pEngine) {
   		if(this.isData()) return UObject.hash(this.getData());
   		
   		Instruction Inst = (pEngine == null)?null:pEngine.getInstruction(this, 0);
   		int h = (pEngine == null)?this.getInstructionNameHash():UString.hash(Inst.getSignature());
   		for(int i = this.getParamCount(); --i >= 0;) {
   			h += ((i | 0x101) << 2)*((pEngine == null)?UObject.hash(this.getParam(i)):pEngine.hash(this.getParam(i)));
   		}
   		Expression E;
   		for(int i = this.getSubExprCount(); --i >= 0;) {
   			E = this.getSubExpr(i);
   			h += ((i | 0x101) << 2)*((E == null)?0:E.hash(pEngine));
   		}
   		return h;
   	}
   	
   	// Serializable --------------------------------------------------------------------------------
   	
   	private Object ensureSavableData(Engine $Engine, Object Data) {
   		if(Data instanceof Package) {
   			Object NewData = $Engine.getExecutableManager().newExpr(Inst_GetPackage.Name, ((Package)Data).getName());
   			return NewData;
   		}
   		if(Data instanceof Type) {
   			Object NewData = $Engine.getExecutableManager().newType(((Type)Data).getTypeRef());
   			return NewData;
   			
   		}
		
   		return Data;
		
		/*
		throw new CurryError(
				"For dependency checking: Packages or Types are not allowed to be saved (serialized) as part of an " +
				"expression.");
		*/
   	}

	static boolean ShowWarning = false;
   	
	/** Perform some operatin just before saving */
	private void writeObject(ObjectOutputStream out) throws IOException {
		if(!(out instanceof CurryOutputStream)) {
			if(!ShowWarning) {
				System.err.println(
					"Expression should be saved by `CurryOutputStream` or it is risk of having incorrect instruction " +
					"index which make it unusable.");
				ShowWarning = true;
			}
		} else {
			CurryOutputStream POS     = (CurryOutputStream)out;
			Engine            $Engine = POS.getEngine();
			
			// Reset the HashORIndex of this instruction back to `Hash` (if it has been set to Index during the
			//   compilation)
			this.updateInstructionSingatureHashBack($Engine);
			
			// Package and Type cannot be saved directly
			if(this.isData()) this.ensureSavableData($Engine, this.getData());
			
			for(int i = this.getParamCount(); --i >= 0; )
				((Expr_Simple)this).Params[i] = this.ensureSavableData($Engine, this.getParam(i));
			
			// Notify the CurryOutputStream that it is being written
			POS.notifyExpressionWritten(this);
		} 
		
		// Save the rest
		out.defaultWriteObject();
	}
   	
   	// Objectable with curry ---------------------------------------------------

   	/** Checks if this Objectable_Curry can be used as a normal objectable */
   	public boolean isAlsoNormalObjectable() { return false; }

   	/** Returns an expression that can be used to determine the short string representation of the expression. */
   	@Override public Expression getExpr_toString(Engine pEngine) {
   		return Expression.newData(this.toString(pEngine));
   	}
   	/** Returns an expression that can be used to determine the short string representation of the expression. */
   	@Override public Expression getExpr_toDetail(Engine pEngine) {
   		return Expression.newData(this.toDetail(pEngine));
   	}

   	/** Returns an expression that can be used to determine the short string representation of the expression. */
   	@Override public Expression getExpr_is(Engine pEngine, Object O) {
   		return Expression.newData(this == O);
   	}
   	/** Checks if O equals to this expression. */
   	@Override public Expression getExpr_equals(Engine pEngine, Object O) {
   		return Expression.newData(this == O);
   	}
   	/** Returns the integer representation of the expression. */
   	@Override public Expression getExpr_hash(Engine pEngine) {
   		return Expression.newData(this.hash(pEngine));
   	}
   	
   	// Internal Services ----------------------------------------------------------------
   	/** Returns the string represetation of the parameter and sub expression of an expression. */
   	static String getParamterString(Engine pEngine, Expression pExpr) {
   		StringBuffer SB = new StringBuffer();
   		SB.append("(");
   		for(int i = 0; i < pExpr.getParamCount(); i++) {
   			if(i != 0) SB.append(", ");
   			Object O = pExpr.getParam(i);
   			if(     O ==               null) SB.append("null");
   			else if(O instanceof Expression) SB.append(((Expression)O).toDetail(pEngine));
   			else if(O instanceof Executable) SB.append((pEngine == null)?UObject.toString(O):pEngine.toDetail(O));
   			else                             SB.append((pEngine == null)?UObject.toString(O):pEngine.getDisplayObject(O));
   		}
   		SB.append(")");
   		if(pExpr.getSubExprCount() != 0) {
   			SB.append("{");
   			for(int i = 0; i < pExpr.getSubExprCount(); i++) {
   				if(i != 0) SB.append("; ");
   				Expression Expr = pExpr.getSubExpr(i);
   				if(Expr == null) SB.append("null");
   				else             SB.append(Expr.toDetail(pEngine));
   			}
   			SB.append("}");
   		}
   		return SB.toString();
   	}
   	
   	// To satisfy Executable.Fragment --------------------------------------------------------------
   	
   	static final ExecSignature ExpressionSignature = ExecSignature.newEmptySignature("Expression", null, null);
   	
   	/**{@inheritDoc}*/ @Override
   	final public ExecSignature getSignature() {
		return  ExpressionSignature;
	}
	/**{@inheritDoc}*/ @Override
	final public Location getLocation() {
		return null;
	}
	
	// Kind --------------------------------------------------------------------
	
   	/**{@inheritDoc}*/ @Override
   	final public Executable.ExecKind getKind() {
		return ExecKind.Fragment;
	}
	
   	/**{@inheritDoc}*/ @Override
   	final public boolean isFragment()   { return  true; }
   	/**{@inheritDoc}*/ @Override
   	final public boolean isMacro()      { return false; }
   	/**{@inheritDoc}*/ @Override
   	final public boolean isSubRoutine() { return false; }
	
	// Cast --------------------------------------------------------------------
	
   	/**{@inheritDoc}*/ @Override
   	final public Fragment   asFragment()   { return this; }
   	/**{@inheritDoc}*/ @Override
   	final public Macro      asMacro()      { return null; }
   	/**{@inheritDoc}*/ @Override
   	final public SubRoutine asSubRoutine() { return null; }
	
	// Curry -------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	final public boolean isCurry() { return true; }
	/**{@inheritDoc}*/ @Override
	final public Curry asCurry()   { return this; }
	/**{@inheritDoc}*/ @Override
	final public Serializable getBody() { return this; }
	
	// Java --------------------------------------------------------------------
	
	/** Checks if the executable is a Java executable */
	final public boolean        isJava() { return false; }
	/** Returns the executable as a Java if it is or null if it is not */
	final public JavaExecutable asJava() { return null; }

	// Frozen variables and Recreation -----------------------------------------
	
	/** Returns all the names of variables */
	final public String[] getFrozenVariableNames() {
		return null;
	}
	/** Returns the number of the frozen variables */
	final public int getFrozenVariableCount() {
		return 0;
	} 
	/** Returns the name of the frozen variable at the index I */
	final public String getFrozenVariableName(int I) {
		return null;
	}
	/** Returns the type of the frozen variable at the index I */
	final public TypeRef getFrozenVariableTypeRef(Engine pEngine, int I) {
		return null;
	}

	/** Recreate the executable based on the newly given frozen scope */
	final public Executable reCreate(Engine pEngine, Scope pFrozenScope) {
		return this;
	}
	
	// Display -----------------------------------------------------------------
	
	// public String toString(Engine pEngine); // Already have          
	// public String toDetail(Engine pEngine); // Already have
	
	// Clonable ----------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	final public Executable clone() {
		return this.makeClone();
	}

	// Lock --------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
		return null;
	}

	// Sub classes -----------------------------------------------------------------------------------------------------

	// Difference expression types ---------------------------------------------------------------------
	// Difference expression types are defined explicitly and individually instead of using composition
	//    patterns or other patterns because expression is the very likely to be the most populated
	//    objects in the whole VM and it has properties in may aspects (i.e., data, funntional, 
	//    parameters, sub-expressions and line skipping).  

	// Expression with no location information -----------------------------------------------

	/** Abstract Data expression  */
	static abstract class Expr_AbstractData extends Expression {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_AbstractData(Object pData) {
			super();
			this.TheData = pData;
		}
		// Expression --------------------------------------------------------------
		final Object TheData;
		@Override public boolean isData()  { return true;      }
		@Override public Object  getData() { return this.TheData; }
		/** Returns the context-awareness return type */
		@Override public TypeRef getReturnTypeRef(CompileProduct pCProduct) {
			if(this.TheData instanceof TypeRef) return new TypeTypeRef((TypeRef)this.TheData);
			return pCProduct.getEngine().getTypeManager().getTypeOf(this.TheData).getTypeRef();
		}
	}

	/** Regular Data expression - Functional */
	static class Expr_Data extends Expr_AbstractData {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Data(Object pData) { super(pData); }
		// Expression --------------------------------------------------------------
		@Override boolean isFunctional() { return true; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone() {
			return this.makeClone_WithoutCoordinate();
		}
		@Override public Expression makeClone_WithoutCoordinate() {
			// Some of the value are cache
			if(this.TheData == null)            return Expression.NULL;
			if(this.TheData instanceof Boolean) return ((Boolean)this.TheData).booleanValue()?Expression.TRUE:Expression.FALSE;
			if(this.TheData instanceof Integer) return Expression.newData(((Integer)this.TheData).intValue());
			return new Expr_Data(this.getData());
		}
		@Override public Expression makeClone_WithCoordinate() {
			if(this.getCoordinate() == -1) return this.makeClone_WithoutCoordinate();
			return new Expr_Data_Coordinate(this.getCoordinate(), this.getData());
		}
	}
	/** Data expression for expression value - Non-functional */
	static class Expr_Expr extends Expr_AbstractData {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Expr(Expression pData) { super(pData); }
		@Override public boolean    isExpr()  { return true;                     }
		@Override public Expression getExpr() { return (Expression)this.TheData; }
		// Expression --------------------------------------------------------------
		@Override boolean isFunctional() { return false; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone() {
			return this.makeClone_WithoutCoordinate();
		}
		@Override public Expression makeClone_WithoutCoordinate() {
			return new Expr_Expr((Expression)this.getData());
		}
		@Override public Expression makeClone_WithCoordinate() {
			if(this.getCoordinate() == -1) return this.makeClone_WithoutCoordinate();
			return new Expr_Expr_Coordinate(this.getCoordinate(), (Expression)this.getData());
		}
	}

	/** Non-Data expression  */
	static abstract class Expr_NonData extends Expression {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_NonData(int pIHash) {
			if(pIHash == 0) throw new CurryError("Interna Error!!! An expression is created without a proper instruction signature.(Expression.java#303)");
			this.InstHash = pIHash;
		}
		Expr_NonData(Instruction pInst) {
			if(pInst == null) throw new CurryError("Interna Error!!! An expression is created without a proper instruction signature.(Expression.java#313)");
			this.InstHash = Engine.calculateHash(pInst.getSignature());
		}
		// Expression --------------------------------------------------------------
		int InstHash = 0;
		@Override public int  getInstructionNameHash()                   { return this.InstHash;   }
		@Override        void updateInstructionSingatureHash(int pIndex) { this.InstHash = pIndex; }
		@Override        void updateInstructionSingatureHashBack(Engine pEngine) {
			if(this.InstHash < 0) return;
			Instruction Inst = pEngine.getInstruction(null, this.InstHash);
			this.InstHash = Inst.getNameHash();
		}
	}

	//Have Only Sub
	/** Group expression or expression with no parameter - Non-Functional */
	static class Expr_Group extends Expr_NonData {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Group(int         pIHash, Expression[] pSubExprs) { super(pIHash); this.SubExprs = pSubExprs; }
		Expr_Group(Instruction pInst,  Expression[] pSubExprs) { super(pInst); this.SubExprs = pSubExprs;  }
		// Expression --------------------------------------------------------------
		final Expression[] SubExprs;
		@Override final public int getSubExprCount() {
			return (this.SubExprs == null)?0:this.SubExprs.length;
		}
		@Override final public Expression getSubExpr(int pPos) {
			if((pPos < 0) || (pPos >= this.SubExprs.length)) return null;
			return this.SubExprs[pPos];
		}
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone() {
			return this.makeClone_WithoutCoordinate();
		}
		@Override public Expression makeClone_WithoutCoordinate() {
			return new Expr_Group(this.InstHash, this.deepCloneSub(this.SubExprs));
		}
		@Override public Expression makeClone_WithCoordinate() {
			if(this.getCoordinate() == -1) return this.makeClone_WithoutCoordinate();
			return new Expr_Group_Coordinate(this.getCoordinate(), this.InstHash, this.deepCloneSub(this.SubExprs));
		}
	}
	/** Group expression or expression with no parameter - Functional */
	static class Expr_Group_Functional extends Expr_Group {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Group_Functional(int         pIHash, Expression[] pSubExprs) { super(pIHash, pSubExprs); }
		Expr_Group_Functional(Instruction pInst,  Expression[] pSubExprs) { super(pInst, pSubExprs);  }
		// Expression --------------------------------------------------------------
		@Override boolean isFunctional() { return true; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone() {
			return this.makeClone_WithoutCoordinate();
		}
		@Override public Expression makeClone_WithoutCoordinate() {
			return new Expr_Group_Functional(this.InstHash, this.deepCloneSub(this.SubExprs));
		}
		@Override public Expression makeClone_WithCoordinate() {
			if(this.getCoordinate() == -1) return this.makeClone_WithoutCoordinate();
			return new Expr_Group_Functional_Coordinate(this.getCoordinate(), this.InstHash, this.deepCloneSub(this.SubExprs));
		}
	}

	/** Simple expression or expression with parameters but no sub expressions - Non-Functional */
	static class Expr_Simple extends Expr_NonData {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Simple(int         pIHash, Object[] pParams) { super(pIHash); this.Params = pParams; }
		Expr_Simple(Instruction pInst,  Object[] pParams) { super(pInst);  this.Params = pParams; }
		// Expression --------------------------------------------------------------
		Object[] Params;
		/** Returns the number of parameters. */
		@Override public int    getParamCount()    { return (this.Params == null)?0:this.Params.length;}
		/** Returns the parameter associated with the position pPos. */
		@Override public Object getParam(int pPos) {
			if(this.Params == null)                        return null;
			if((pPos < 0) || (pPos >= this.Params.length)) return null;
			return this.Params[pPos];
		}
		// Clone --------------------------------------------------------------------
		@Override public Expression makeClone() {
			return this.makeClone_WithoutCoordinate();
		}
		@Override public Expression makeClone_WithoutCoordinate() {
			return new Expr_Simple(this.InstHash, this.deepCloneParam(this.Params));
		}
		@Override public Expression makeClone_WithCoordinate() {
			if(this.getCoordinate() == -1) return this.makeClone_WithoutCoordinate();
			return new Expr_Simple_Coordinate(this.getCoordinate(), this.InstHash, this.deepCloneParam(this.Params));
		}
	}
	/** Simple expression or expression with parameters but no sub expressions - Functional */
	static class Expr_Simple_Functional extends Expr_Simple {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Simple_Functional(int pIHash, Object[] pParams)        { super(pIHash, pParams); }
		Expr_Simple_Functional(Instruction pInst, Object[] pParams) { super(pInst, pParams);  }
		// Expression --------------------------------------------------------------
		@Override boolean isFunctional() { return true; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone() {
			return this.makeClone_WithoutCoordinate();
		}
		@Override public Expression makeClone_WithoutCoordinate() {
			return new Expr_Simple_Functional(this.InstHash, this.deepCloneParam(this.Params));
		}
		@Override public Expression makeClone_WithCoordinate() {
			if(this.getCoordinate() == -1) return this.makeClone_WithoutCoordinate();
			return new Expr_Simple_Functional_Coordinate(this.getCoordinate(), this.InstHash, this.deepCloneParam(this.Params));
		}
	}

	//Have Param and Sub
	/** Comple expression or expression with parameters and sub expressions - Non-Functional */
	static class Expr_Complex extends Expr_Simple {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Complex(int         pIHash, Object[] pParamExprs, Expression[] pSubExprs) { super(pIHash, pParamExprs); this.SubExprs = pSubExprs; }
		Expr_Complex(Instruction pInst,  Object[] pParamExprs, Expression[] pSubExprs) { super(pInst, pParamExprs);  this.SubExprs = pSubExprs; }
		// Expression --------------------------------------------------------------
		Expression[] SubExprs = null;
		@Override public int getSubExprCount() {
			return (this.SubExprs == null)?0:this.SubExprs.length;
		}
		@Override public Expression getSubExpr(int pPos) {
			if((pPos < 0) || (pPos >= this.SubExprs.length)) return null;
			return this.SubExprs[pPos];
		}
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone() {
			return this.makeClone_WithoutCoordinate();
		}
		@Override public Expression makeClone_WithoutCoordinate() {
			return new Expr_Complex(this.InstHash, this.deepCloneParam(this.Params), this.deepCloneSub(this.SubExprs));
		}
		@Override public Expression makeClone_WithCoordinate() {
			if(this.getCoordinate() == -1) return this.makeClone_WithoutCoordinate();
			return new Expr_Complex_Coordinate(this.getCoordinate(), this.InstHash, this.deepCloneParam(this.Params), this.deepCloneSub(this.SubExprs));
		}
	}
	/** Comple expression or expression with parameters and sub expressions - Non-Functional */
	static class Expr_Complex_Functional extends Expr_Complex {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Complex_Functional(int pIHash, Object[] pParamExprs, Expression[] pSubExprs)        { super(pIHash, pParamExprs, pSubExprs); }
		Expr_Complex_Functional(Instruction pInst, Object[] pParamExprs, Expression[] pSubExprs) { super(pInst, pParamExprs, pSubExprs);  }
		// Expression --------------------------------------------------------------
		@Override boolean isFunctional() { return true; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone() {
			return this.makeClone_WithoutCoordinate();
		}
		@Override public Expression makeClone_WithoutCoordinate() {
			return new Expr_Complex_Functional(this.InstHash, this.deepCloneParam(this.Params), this.deepCloneSub(this.SubExprs));
		}
		@Override public Expression makeClone_WithCoordinate() {
			if(this.getCoordinate() == -1) return this.makeClone_WithoutCoordinate();
			return new Expr_Complex_Functional_Coordinate(this.getCoordinate(), this.InstHash, this.deepCloneParam(this.Params), this.deepCloneSub(this.SubExprs));
		}
	}

	// Expression with one-skip-line location information ------------------------------------

	/** Regular Data expression - Functional */
	static class Expr_Data_Coordinate extends Expr_Data {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Data_Coordinate(int pCoordinate, Object pData) {
			super(pData); this.Coordinate = pCoordinate;
		}
		// Expression --------------------------------------------------------------
		final int Coordinate;
		@Override public int        getCoordinate() { return this.Coordinate; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone()     { return this.makeClone_WithCoordinate(); }
	}
	/** Data expression for expression value - Non-functional */
	static class Expr_Expr_Coordinate extends Expr_Expr {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Expr_Coordinate(int pCoordinate, Expression pData) { super(pData); this.Coordinate = pCoordinate; }
		// Expression --------------------------------------------------------------
		final int Coordinate;
		@Override public int        getCoordinate() { return this.Coordinate; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone()     { return this.makeClone_WithCoordinate(); }
	}

	/** Group expression or expression with no parameter - Non-Functional */
	static class Expr_Group_Coordinate extends Expr_Group {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Group_Coordinate(int pCoordinate, int         pIHash, Expression[] pSubExprs) { super(pIHash, pSubExprs); this.Coordinate = pCoordinate; }
		Expr_Group_Coordinate(int pCoordinate, Instruction pInst,  Expression[] pSubExprs) { super(pInst, pSubExprs);  this.Coordinate = pCoordinate; }
		// Expression --------------------------------------------------------------
		final int Coordinate;
		@Override public int        getCoordinate() { return this.Coordinate; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone()     { return this.makeClone_WithCoordinate(); }
	}
	/** Group expression or expression with no parameter - Functional */
	static class Expr_Group_Functional_Coordinate extends Expr_Group_Functional {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Group_Functional_Coordinate(int pCoordinate, int         pIHash, Expression[] pSubExprs) { super(pIHash, pSubExprs); this.Coordinate = pCoordinate; }
		Expr_Group_Functional_Coordinate(int pCoordinate, Instruction pInst,  Expression[] pSubExprs) { super(pInst, pSubExprs);  this.Coordinate = pCoordinate; }
		// Expression --------------------------------------------------------------
		final int Coordinate;
		@Override public int        getCoordinate() { return this.Coordinate; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone()     { return this.makeClone_WithCoordinate(); }
	}

	/** Simple expression or expression with parameters but no sub expressions - Non-Functional */
	static class Expr_Simple_Coordinate extends Expr_Simple {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Simple_Coordinate(int pCoordinate, int         pIHash, Object[] pParams) { super(pIHash, pParams); this.Coordinate = pCoordinate; }
		Expr_Simple_Coordinate(int pCoordinate, Instruction pInst,  Object[] pParams) { super(pInst, pParams);  this.Coordinate = pCoordinate; }
		// Expression --------------------------------------------------------------
		final int Coordinate;
		@Override public int getCoordinate() { return this.Coordinate; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone() { return this.makeClone_WithCoordinate(); }
	}
	/** Simple expression or expression with parameters but no sub expressions - Functional */
	static class Expr_Simple_Functional_Coordinate extends Expr_Simple_Functional {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Simple_Functional_Coordinate(int pCoordinate, int         pIHash, Object[] pParams) { super(pIHash, pParams); this.Coordinate = pCoordinate; }
		Expr_Simple_Functional_Coordinate(int pCoordinate, Instruction pInst,  Object[] pParams) { super(pInst, pParams);  this.Coordinate = pCoordinate; }
		// Expression --------------------------------------------------------------
		final int Coordinate;
		@Override public int        getCoordinate() { return this.Coordinate; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone()     { return this.makeClone_WithCoordinate(); }
	}

	/** Comple expression or expression with parameters and sub expressions - Non-Functional */
	static class Expr_Complex_Coordinate extends Expr_Complex {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Complex_Coordinate(int pCoordinate, int         pIHash, Object[] pParamExprs, Expression[] pSubExprs) { super(pIHash, pParamExprs, pSubExprs); this.Coordinate = pCoordinate; }
		Expr_Complex_Coordinate(int pCoordinate, Instruction pInst,  Object[] pParamExprs, Expression[] pSubExprs) { super(pInst, pParamExprs, pSubExprs);  this.Coordinate = pCoordinate; }
		// Expression --------------------------------------------------------------
		final int Coordinate;
		@Override public int        getCoordinate() { return this.Coordinate; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone()     { return this.makeClone_WithCoordinate(); }
	}
	/** Comple expression or expression with parameters and sub expressions - Functional */
	static class Expr_Complex_Functional_Coordinate extends Expr_Complex_Functional {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Expr_Complex_Functional_Coordinate(int pCoordinate, int         pIHash, Object[] pParamExprs, Expression[] pSubExprs) { super(pIHash, pParamExprs, pSubExprs); this.Coordinate = pCoordinate; }
		Expr_Complex_Functional_Coordinate(int pCoordinate, Instruction pInst,  Object[] pParamExprs, Expression[] pSubExprs) { super(pInst, pParamExprs, pSubExprs);  this.Coordinate = pCoordinate; }
		// Expression --------------------------------------------------------------
		final int Coordinate;
		@Override public int        getCoordinate() { return this.Coordinate; }
		// Clone -------------------------------------------------------------------
		@Override public Expression makeClone()     { return this.makeClone_WithCoordinate(); }
	}
	
}
