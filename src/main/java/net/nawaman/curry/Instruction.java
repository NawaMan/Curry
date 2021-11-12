package net.nawaman.curry;

import java.io.Serializable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.regparser.result.Coordinate;
import net.nawaman.curry.Instructions_Core.Inst_Doc;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.util.Objectable;
import net.nawaman.util.UClass;
import net.nawaman.util.UString;

abstract public class Instruction implements Objectable {
	
	// The engine will be used only for creating ISpec. It is not stored in the instruction until
	//    the instruction is actually registered.
	/** Constructs a new Instruction by using auto-generated spec. **/
	Instruction(Engine pEngine, String pISpecStr) {
		this(pEngine, pISpecStr, InstructionSpec.newISpec(pEngine, pISpecStr));
	}
	// The engine will be used only for creating ISpec. It is not stored in the instruction until
	//    the instruction is actually registered.
	/** Constructs a new Instruction by using explicitly specified spec. **/
	Instruction(Engine pEngine, InstructionSpec pISpec) {
		this(pEngine, null, pISpec);
	}
	// The engine will be used only for creating ISpec. It is not stored in the instruction until
	//    the instruction is actually registered.
	/** Constructs a new Instruction by using explicitly specified spec. **/
	Instruction(Engine pEngine, String pISpecStr, InstructionSpec pISpec) {
		this.ISpec = pISpec;
		if(this.ISpec == null) {
			throw new IllegalArgumentException(
			      "Internal Error: An expression is created without a proper instruction " +
			      "signature `"+pISpecStr+"` + (See " + this.getClass().getCanonicalName() + ")."
			   );
		}
	}
	
	// Signature --------------------------------------------------------------
	final public String getSignature() { return this.getSpecification().getName(); }
	
	// Specification ----------------------------------------------------------
	final        InstructionSpec ISpec;
	final public InstructionSpec getSpecification() { return this.ISpec; }
	
	final public String getName()     { return this.ISpec.getName(); }
	final public int    getNameHash() { return net.nawaman.curry.Engine.calculateHash(this.ISpec.getName()); }
	
	                Engine    Engine;
	final protected Engine getEngine() {
		return this.Engine;
	}

	/** Returns the context-free return type */
	public TypeRef getReturnTypeRef() {
		return this.ISpec.getReturnTypeRef();
	}
	
	// Services --------------------------------------------------------------------------------------------------------
	
	final protected Context newStack(Context pContext, String pName, Executable pInitiator) {
		return new Context(pContext, pName, pInitiator);
	}
	
	// Compile-time checking method ------------------------------------------------------------------------------------
	
	/**
	 * Returns the context-awareness return type (returns null if unsure (in case that it depends on parameter and the
	 * param is unsure in its return type))
	 **/
	public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
		return this.ISpec.getReturnTypeRef();
	}
	
	/**
	 * Checks if the type of the parameters of the expression are correct.
	 * The methods should be responsible for report warning and error.
	 **/
	public boolean ensureParamCorrect(Expression E, CompileProduct CP) {
		return this.ensureParamCorrect(E, CP, false);
	}
	
	/**
	 * Checks if the type of the parameters of the expression are correct.
	 * 
	 * The methods should be responsible for report warning and error.
	 **/
	public boolean ensureParamCorrect(Expression E, CompileProduct CP, boolean pIsIgnoreReturnTypeCheck) {
		if(E.isData()) return true;
		
		int ICount = this.ISpec.getParameterCount();
		int LastI  = (ICount - 1);
		if(this.ISpec.isVarArgs()) {
			// Less parameter than needed
			if((E.getParamCount() < LastI) && !ReportWrongParams(null, E, CP, false, false)) return false;
			
			// If isVarArgs and there is no last parameter
			if((E.getParamCount() < ICount) &&
			   !this.ISpec.canParameterBeNull(LastI) &&
			   !ReportWrongParams(null, E, CP, false, false))
				return false;
		
		// If not VarArgs, the parameter must be equals.
		} else if((E.getParamCount() != ICount) && !ReportWrongParams(null, E, CP, false, false)) return false;
		
		for(int i = E.getParamCount(); --i >= 0; ) {
			
			Object O = E.getParam(i);
			if(this instanceof Inst_AbstractCutShort) {
				Object SO = E.getSubExpr(i);
				if(SO != null) O = SO;
			}
			if(O == null) {
				if(!this.ISpec.canParameterBeNull(i) && !ReportWrongParams(null, E, CP, false, false)) return false;
				continue;
			}
			TypeRef TR = CP.getReturnTypeRefOf(O);
			if(TR == null) TR = TKJava.TAny.getTypeRef();
			else {
				Type TTR = CP.getTypeAtCompileTime(TR);
				if(TTR == null) {
					// For detecting an error
					System.out.println("What da heck??? " + TR);
					TR  = CP.getReturnTypeRefOf(O);
					TTR = CP.getTypeAtCompileTime(TR);
				}
				TR = TTR.getTypeRef();
			}			
			Type T = null;
			if(i >= LastI) T = this.ISpec.getParameterType(LastI);
			else           T = this.ISpec.getParameterType(i);
			
			if(!MType.CanTypeRefByAssignableByInstanceOf(null, CP.getEngine(), T.getTypeRef(), TR)) {
				boolean IsTempTypeRef = (TR instanceof TypeTypeRef);
				if(!IsTempTypeRef || (T != TKJava.TType))
					if(!ReportWrongParams(null, E, CP, false, false)) return false;
			}
		}
		
		if(!pIsIgnoreReturnTypeCheck) {
			TypeRef TR = CP.getReturnTypeRefOf(E);
			// Unable to find the return type, the parameter is very likely to be invalid
			if(TR == null) {
				TR = CP.getReturnTypeRefOf(E);
				return ReportWrongParams(null, E, CP, false, true);
			}
		}
		return true;
	}
	
	final protected String getCompileTimeParametersList(Expression pExpr, CompileProduct pCProduct) {
		StringBuffer SB = new StringBuffer();
		SB.append("(");
		for(int i = 0; i < pExpr.getParamCount(); i++) {
			if(i != 0) SB.append(", ");
			SB.append(pCProduct.getReturnTypeRefOf(pExpr.getParam(i)));
		}
		SB.append(")");
		return SB.toString();
	}
	
	final protected String getCompileTimeInvalidParameters(Expression pExpr, CompileProduct pCProduct) {
		return "Invalid expression parameters `" + this.getCompileTimeParametersList(pExpr, pCProduct) +
				"` cannot be assigned to " + this.toDetail() + " ("+pExpr.toDetail(pCProduct.getEngine())+")";
	}

	/**
	 * Report a compile-time wrong parameter of the given expression E
	 */
	final static public boolean ReportWrongParams(String pSource, Expression E, CompileProduct CP, boolean pIsIgnoreable,
			boolean pIsFatal) {
		if(E == null) return true;
		Instruction Inst = E.getInstruction(CP.getEngine());
		return ReportCompileProblem(pSource, Inst.getCompileTimeInvalidParameters(E, CP), E, CP, pIsIgnoreable, pIsFatal);
	}
	
	/**
	 * Report a compile problem
	 * 
	 * @param pIsIgnoreable indicates if the problem should be ignored if the compile time checking is none
	 * @param psIsFatal     indicates if the problem is fatal.
	 */
	final static public boolean ReportCompileProblem(String pSource, String pMessage, Expression E,
			CompileProduct CP, boolean pIsIgnoreable, boolean pIsFatal) {
		if(CP.isCompileTimeCheckingNone() && pIsIgnoreable) return true;
		if(pSource != null) pMessage += " <"+pSource+">";
		if(     CP.isCompileTimeCheckingNone() || pIsIgnoreable) CP.reportWarning(   pMessage, null, E.getColumn(), E.getLineNumber());
		else if(!pIsFatal)                                       CP.reportError(     pMessage, null, E.getColumn(), E.getLineNumber());
		else {                                                   CP.reportFatalError(pMessage, null, E.getColumn(), E.getLineNumber()); return false; }
		return true;
	}
	
	/**
	 * Manipulate the compilation context and return if the compilation should continue.
	 * 
	 * This method will be called before its parameters' manipulateCompileContextStart is called
	 * 
	 * The methods should be responsible for report warning and error.
	 * If the isDynamic flag is set, errors will be threat as warnings.
	 **/
	public boolean manipulateCompileContextStart(CompileProduct pCProduct, int pPosition) {
		return true;
	}
	
	/**
	 * Manipulate the compilation context and return if the compilation should continue.
	 * 
	 * This method will be called before its sub expression' manipulateCompileContextStart is called
	 * 
	 * The methods should be responsible for report warning and error.
	 * If the isDynamic flag is set, errors will be threat as warnings.
	 **/
	public boolean manipulateCompileContextBeforeSub(Object[] pParams, CompileProduct pCProduct, int pPosition) {
		return true;
	}
	
	/**
	 * Manipulate the compilation context and return if the compilation should continue.
	 * 
	 * This method will be called before its parameters' manipulateCompileContextFinish is called
	 * 
	 * The methods should be responsible for report warning and error.
	 * If the isDynamic flag is set, errors will be threat as warnings.
	 **/
	public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
		return true;
	}
	
	// Expression Generation --------------------------------------------------
	boolean checkParams(Object[] pParameters) {
		boolean IsMatch = true;
		
		if((pParameters == null) && (this.ISpec.getParameterCount() == 1) && this.ISpec.isVarArgs()) return true;
		
		if((pParameters == null) && (this.ISpec.getParameterCount() != 0)) IsMatch = false;
			
		if(!IsMatch || !((pParameters == null) && (this.ISpec.getParameterCount() == 0))) {
			int PLength = (pParameters == null)?0:pParameters.length;
			if(!this.ISpec.IsVarArgs) {
				if(PLength != this.ISpec.getParameterCount()) IsMatch = false;
			} else {
				if(PLength < (this.ISpec.getParameterCount() - 1)) IsMatch = false;
			}
		}
		
		if(!IsMatch) {
			throw new IllegalArgumentException("Unmatch number of parameters: " +
				    Executor.getParamErrMsg_ParamListOnly(this.Engine, null, this, pParameters));
		}
		return true;
	}
	
	boolean checkSubExpression(int PCount, Expression[] pSubExpressions) {
		// Default
		if(pSubExpressions == null) pSubExpressions = Expression.EmptyExpressionArray;
		
		int Min = this.getMinimumNumberOfSub(PCount);
		if((Min >= 0) && (pSubExpressions.length < Min))
			throw new IllegalArgumentException("The number of Sub expressions is lower than the minimum limit ("+this.getName()+")");
		int Max = this.getMaximumNumberOfSub(PCount);
		if((Max >= 0) && (pSubExpressions.length > Max))
			throw new IllegalArgumentException("The number of Sub expressions is more than the minimum limit ("+this.getName()+")");
		
		return true;
	}
	
	protected int getMinimumNumberOfSub(int pParameterCount) { return -1; }
	protected int getMaximumNumberOfSub(int pParameterCount) { return -1; }
	
	protected Expression createExpression(int pCol, int pRow, Object[] pParameters, Expression[] pSubExpressions) { return null; }
	
	// External use --------------------------------------------------------------------------------
	
	public Expression newExpression() { return this.newExprSubs(null, null); }
	
	public Expression newExpression(Object ... pParams) {
		return this.newExprSubs(pParams, null);
	}
	public Expression newExpression_Coordinate(int pCol, int pRow, Object ... pParams) {
		return this.newExprSubs_Coordinate(pCol, pRow, pParams, null);
	}
	public Expression newExpression_Coordinate(int[] pCR, Object ... pParams) {
		int pCol = ((pCR == null) || (pCR.length < 2))?-1:pCR[0];
		int pRow = ((pCR == null) || (pCR.length < 2))?-1:pCR[1];
		return this.newExpression_Coordinate(pCol, pRow, pParams);
	}
	public Expression newExpression_Coordinate(Coordinate coordinate, Object ... pParams) {
		int pCol = (coordinate == null) ? -1 : coordinate.col();
		int pRow = (coordinate == null) ? -1 : coordinate.row();
		return this.newExpression_Coordinate(pCol, pRow, pParams);
	}
	
	public Expression newExprSubs(Object[] pParameters, Expression[] pSubExpressions) {
		return this.newExprSubs_Coordinate(-1, -1, pParameters, pSubExpressions);
	}
	public Expression newExprSubs_Coordinate(Coordinate pCR, Object[] pParameters, Expression[] pSubExpressions) {
		int pCol = Coordinate.colOf(pCR);
		int pRow = Coordinate.rowOf(pCR);
		return this.newExprSubs_Coordinate(pCol, pRow, pParameters, pSubExpressions);
	}
	public Expression newExprSubs_Coordinate(int[] pCR, Object[] pParameters, Expression[] pSubExpressions) {
		int pCol = ((pCR == null) || (pCR.length < 2))?-1:pCR[0];
		int pRow = ((pCR == null) || (pCR.length < 2))?-1:pCR[1];
		return this.newExprSubs_Coordinate(pCol, pRow, pParameters, pSubExpressions);
	}
	public Expression newExprSubs_Coordinate(int pCol, int pRow, Object[] pParameters, Expression[] pSubExpressions) {
		if(!this.checkParams(pParameters))                                                        return null;
		if(!this.checkSubExpression((pParameters == null)?0:pParameters.length, pSubExpressions)) return null;
		return this.createExpression(pCol, pRow, pParameters, pSubExpressions);
	}

	// Execution ---------------------------------------------------------------
	
	// Default implementation
	abstract protected Object run(Context pContext, Expression pExpr, Object[] pParams);
	
	// Service ---------------------------------------------------------------------------
	// Expression -----------------------------------------------------------------------

	final protected Expression newExprData(int pCol, int pRow, Serializable pData) {
		return Expression.newData(pCol, pRow, pData);
	}
	final protected Expression newExprExpr(int pCol, int pRow, Expression pData) {
		return Expression.newExpr(pCol, pRow, pData);
	}
	final protected Expression newNonSerializableData(int pCol, int pRow, Object pData) {
		return Expression.newNonSerializableData(pCol, pRow, pData);
	}
	final protected Expression newUnReplaceData(int pCol, int pRow, Engine pEngine, Object pData) {
		return Expression.newUnReplaceData(pCol, pRow, pEngine, pData);
	}
	
	final protected Expression newExprSimple(int pCol, int pRow, Instruction pInstruction, Object[] pParams) {
		if(pInstruction.ISpec.IsFunctional) {
			// Check if this is really functional
			boolean IsFunctional = true;
			if(pParams != null) {
				for(int i = pParams.length; --i >= 0; ) {
					// Non expression is functional
					if(!(pParams[i] instanceof Expression)) continue;
					
					Expression P = ((Expression)pParams[i]);
					
					// Function Subs are functional
					if(P.isFunctional()) continue;
					
					// The rest are not
					IsFunctional = false;
					break;
				}
			}
			if(IsFunctional) {
				int Coordinate = Location.getCoordinate(pCol, pRow);
				if(Coordinate == -1) return new Expression.Expr_Simple_Functional(                       pInstruction, pParams);
				else                 return new Expression.Expr_Simple_Functional_Coordinate(Coordinate, pInstruction, pParams);
			}
		}

		int Coordinate = Location.getCoordinate(pCol, pRow);
		if(Coordinate == -1) return new Expression.Expr_Simple(                       pInstruction, pParams);
		else                 return new Expression.Expr_Simple_Coordinate(Coordinate, pInstruction, pParams);
	}
	final protected Expression newExprGroup(int pCol, int pRow, Instruction pInstruction,
			Expression[] pSubExprs) {
		int Coordinate = Location.getCoordinate(pCol, pRow);
		if(Coordinate == -1) return new Expression.Expr_Group(                       pInstruction, pSubExprs);
		else                 return new Expression.Expr_Group_Coordinate(Coordinate, pInstruction, pSubExprs);
	}
	final protected Expression newExprGroup_Functional(int pCol, int pRow, Instruction pInstruction,
			Expression[] pSubExprs) {
		int Coordinate = Location.getCoordinate(pCol, pRow);
		if(Coordinate == -1) return new Expression.Expr_Group_Functional(                       pInstruction, pSubExprs);
		else                 return new Expression.Expr_Group_Functional_Coordinate(Coordinate, pInstruction, pSubExprs);
	}
	final protected Expression newExprComplex(int pCol, int pRow, Instruction pInstruction,
			Object[] pParamExprs, Expression[] pSubExprs) {
		int Coordinate = Location.getCoordinate(pCol, pRow);
		if(Coordinate == -1) return new Expression.Expr_Complex(                       pInstruction, pParamExprs, pSubExprs);
		else                 return new Expression.Expr_Complex_Coordinate(Coordinate, pInstruction, pParamExprs, pSubExprs);
	}
	final protected Expression newExprComplex_Functional(int pCol, int pRow, Instruction pInstruction,
			Object[] pParamExprs, Expression[] pSubExprs) {
		int Coordinate = Location.getCoordinate(pCol, pRow);
		if(Coordinate == -1) return new Expression.Expr_Complex_Functional(                       pInstruction, pParamExprs, pSubExprs);
		else                 return new Expression.Expr_Complex_Functional_Coordinate(Coordinate, pInstruction, pParamExprs, pSubExprs);
	}
	
	// Utilities --------------------------------------------------------------
	
	/** Returns the parameter error */
	final protected SpecialResult reportParameterError(Engine pEngine, Context pContext, String pNote, Object[] pEParams) {
		return new SpecialResult.ResultError(new CurryError(Executor.getParamErrMsg(pEngine, pContext, this, pEParams) + ((pNote==null)?"":("("+pNote+")")), pContext));
	}

	/** Returns the string representation of the parameter set */
	static protected String getParamterString(Engine pEngine, Object[] pParams) {
		StringBuffer SB = new StringBuffer();
		SB.append("(");
		if(pParams != null) {
			for(int i = 0; i < pParams.length; i++) {
				if(i != 0) SB.append(", ");
				Object D = pParams[i];
				SB.append(pEngine.getDisplayObject(D));
			}
		}
		SB.append(")");
		return SB.toString();
	}
	
	/** Execute an expression with the given context */
	final protected Object executeAnExpression(Context pContext, Expression pExpr) {
		if(pExpr == null)  return null;
		if(pExpr.isData()) return pExpr.getData();
		return pContext.getExecutor().execInternal(pContext, pExpr);
	}
	
	final protected class ActionRecordHook {
		public ActionRecordHook() {}
		public ActionRecord ARecord;
	}
	
	/** Execute an expression with the given context */
	final protected Object executeAnExpression(Context pContext, Expression pExpr, ActionRecordHook ARHook) {
		if(pExpr.isInstruction(this.getEngine(), Inst_Doc.Name)) {
			Inst_Doc IDoc = (Inst_Doc)this.getEngine().getInstruction(Inst_Doc.Name);
			IDoc.ARHook = ARHook;
		}
			
		return this.executeAnExpression(pContext, pExpr);
	}
	
	/** Returns the location snapshot of the context */
	final protected LocationSnapshot[] getContextLocationSnapshots(Context pContext) {
		return Context.getLocationsOf(pContext);
	}
	
	/** Checks if the given object R is a SpecialResult (that must not be passed outside the scope of Curry) */
	final protected boolean isSpecialResult(Object R) { return (R instanceof SpecialResult); }

	/** Get the owner of the given context and ensure that it is not null */
	final protected Object getStackOwnerAsObject_NoNull(Context pContext) {
		Object O = pContext.getStackOwner();
		if(O == null) throw new CurryError("The corrent context does not owned by any object.", pContext);
		return O;
	}
	/** Get the owner of the given context as a package */
	final protected Object getStackOwnerAsPackage_NoNull(Context pContext) {
		Object O = pContext.getStackOwnerAsType();
		if(!(O instanceof Package)) 
			throw new CurryError("The corrent context does not owned by any object that can be seen as a package.",
					pContext);
		return O;
	}
	
	// Objectable ----------------------------------------------------------------------------------
	
	/** Returns the short string representation of the object. */
	@Override public String toString() { return "Instruction:"+ this.ISpec.toString(); }

	/** Returns the long string representation of the object. */
	public String toDetail() { return "Instruction: " + (this.ISpec.isFunctional()?"=":"") + this.getName() + this.ISpec.toDetail_DataOnly(); }

	/** Checks if O is the same or consider to be the same object with this object. */	
	public boolean is(Object O) { return this == O; }

	/** Checks if O equals to this object. */	
	@Override public boolean equals(Object O) {
		if(!(O instanceof Instruction)) return false;
		return this.ISpec.equals(((Instruction)O).ISpec);
	}

	/** Returns the integer representation of the object. */
	@Override public int hash() { return UString.hash(this.ISpec.Name); }
	
	/** Returns the hashCode of the object */
	@Override public int hashCode() { return super.hashCode(); }

	// Instruction spec ------------------------------------------------------------------------------------------------
	
	static public class InstructionSpec implements Objectable {

		// Mapping

		// ~ = Any
		// ^ = Void
		// ? = Boolean
		// $ = String
		// ' = Character
		// # = Number
		// b = Byte
		// s = Short
		// i = Int
		// l = Long
		// f = Float
		// d = Double
		// ! = Type
		// E = nawa.curry.Expr_Expr
		// @ = Class
		// P = java.io.Serializable (Sessional)
		// I = nawa.curry.Iterator
		// D = nawa.curry.DataHolder
		// S = nawa.curry.util.DataHolderStructure
		// A = nawa.curry.util.DataHolderArray
		// F = nawa.curry.util.FieldHolder
		// M = nawa.curry.util.MoreData

		// [] = Array (i[] = array of int)
		// + = Cannot be Null (must be in the front like +$ or +*?)
		// = = functional
		// / = variant
		static Type getTypeFromSymbol(Engine pEngine, String pSymbol) {
			if((pSymbol == null) || (pSymbol.length() == 0)) return null;

			boolean IsArray     = false;
			int     ArrayLength = -1;
			if(pSymbol.startsWith("+")) pSymbol = pSymbol.substring(1);
			if(pSymbol.matches("^[^\\[]*\\[[0-9]*\\]$")) {
				IsArray = true;
				String ALStr = pSymbol.substring(pSymbol.indexOf("[") + 1, pSymbol.indexOf("]"));
				ArrayLength = (ALStr.length() == 0)?-1:Integer.parseInt(ALStr);
				pSymbol = pSymbol.substring(0, pSymbol.indexOf("["));
			}

			Type T = null;
			if(pSymbol.length() ==1 ) {
				char C = pSymbol.charAt(0);
				switch(C) {
					case  '~': T = TKJava.TAny;           break;
					case  '^': T = TKJava.TVoid;          break;

					case  '?': T = TKJava.TBoolean;       break;
					case  '$': T = TKJava.TString;        break;
					case '\'': T = TKJava.TCharacter;     break;

					case  'b': T = TKJava.TByte;          break;
					case  's': T = TKJava.TShort;         break;
					case  'i': T = TKJava.TInteger;       break;
					case  'l': T = TKJava.TLong;          break;
					case  'f': T = TKJava.TFloat;         break;
					case  'd': T = TKJava.TDouble;        break;
					case  '#': T = TKJava.TNumber;        break;

					case  '!': T = TKJava.TType;          break;
					case  'E': T = TKJava.TExpression;    break;
					case  '@': T = TKJava.TClass;         break;
					case  'P': T = TKJava.TSerializable;  break;
					case  'I': T = TKJava.TIterator;      break;
					case  'D': T = TKJava.TDataHolder;    break;
					case  'M': T = TKJava.TMoreData;      break;
				}
			}
			if(T == null){
				Class<?> Cls = UClass.getClassByName(pSymbol, MClassPaths.getClassLoaderOf(pEngine));

				if(Cls == null) return null;
				T = TKJava.Instance.getTypeByClass(pEngine, null, Cls);
			}

			if(IsArray) {
				TKArray TK = (TKArray)pEngine.getTypeManager().getTypeKind(TKArray.KindName);
				T = TK.getType(pEngine, null, TK.getTypeSpec(null, T, ArrayLength));
			}
			return T;
		}

		/** Checks the parameter symbol if the parameter can be null */
		static boolean getCanParamBeNullFromSymbol(String pSymbol) {
			return !(pSymbol.startsWith("+"));
		}
		
		static final Pattern InstSpecPattern = Pattern.compile("^([~a-zA-Z0-9$_]*)(\\([^\\)]*\\))?(\\{\\})?(:.+)?$");

		/** Creates a new instruction specification from string */
		static public InstructionSpec newISpec(Engine pEngine, String pStr) {
			if(pStr == null) return null;
			boolean Is_Functional = pStr.startsWith("=");
			if(Is_Functional) pStr = pStr.substring(1);

			Matcher M = InstSpecPattern.matcher(pStr);

			if (!M.find()) return null;

			String          Name           = M.group(1);
			Vector<Type>    Params         = null;
			Vector<Boolean> PCBNs          = null;
			boolean         IsVarArgs      = false;
			boolean         CanHaveSubExpr = (M.group(3) != null);
			String          ReturnType     = M.group(4);

			String RawPR = M.group(2); if("()".equals(RawPR)) RawPR = null;

			if(RawPR != null) {
				RawPR = RawPR.substring(1, RawPR.length() - 1);
				Params = new Vector<Type>();
				PCBNs  = new Vector<Boolean>();
				String[] Ss = RawPR.split(",");
				for(int j = 0; j < Ss.length; j++) {
					String S = Ss[j].trim();
					// Check for Unlimited
					if((j == (Ss.length - 1)) && (S.endsWith("..."))) {
						S = S.substring(0, (S.length() - 3));
						IsVarArgs = true;
					}
					Type T = InstructionSpec.getTypeFromSymbol(pEngine, S);
					if(T == null) return null;
					Params.add(T);
					PCBNs.add(InstructionSpec.getCanParamBeNullFromSymbol(S));
				}
			}

			boolean[] NewPCBNs = null;
			if(PCBNs != null) {
				// Shrink PCBNs
				for(int i = (PCBNs.size() - 1); i >= 0; i--) {
					if(!PCBNs.get(i).booleanValue()) break;
					PCBNs.remove(PCBNs.size() - 1);
				}
				if(PCBNs.size() == 0) PCBNs = null;

				// Change to Array of boolean
				if(PCBNs != null) {
					NewPCBNs = new boolean[PCBNs.size()];
					for(int i = 0; i < PCBNs.size(); i++) NewPCBNs[i] = PCBNs.get(i).booleanValue();
				}
			}

			TypeRef RT = TKJava.TAny.getTypeRef();
			if(ReturnType != null) RT = InstructionSpec.getTypeFromSymbol(pEngine, ReturnType.substring(1)).getTypeRef();

			return new InstructionSpec(Name,
						  Is_Functional,
						  (Params == null)?Type.EmptyTypeArray:Params.toArray(new Type[0]),
						  NewPCBNs,
						  IsVarArgs,
						  CanHaveSubExpr,
						  RT);
		}

		InstructionSpec(String pName, boolean pIs_Functional, Type[] pParams, boolean[] pPCBNs,
				boolean pIsVarArgs, boolean pCanHaveSubExpr, TypeRef pReturnTypeRef) {
			this.Name = pName;

			this.IsFunctional = pIs_Functional;

			this.Params = (pParams == null)?Type.EmptyTypeArray:pParams;
			this.PCBNs  = pPCBNs;

			this.IsVarArgs      = pIsVarArgs;
			this.CanHaveSubExpr = pCanHaveSubExpr;

			this.ReturnTypeRef = (pReturnTypeRef == null) ? TKJava.TAny.getTypeRef() : pReturnTypeRef;
			
			boolean HasParamAsExpr = false;
			if(this.Params != null) {
				for(int i = this.Params.length; --i >= 0; ) {
					if(this.Params[i] == TKJava.TExpression) {
						HasParamAsExpr = true;
						break;
					}
				}
			}
			this.HasParamAsExpression = HasParamAsExpr;
		}

		final        String Name;
		final public String getName() { return this.Name; }

		final        boolean IsFunctional;
		final public boolean isFunctional() { return this.IsFunctional; }
		
		final        boolean HasParamAsExpression;
		final public boolean hasParamAsExpression() { return this.HasParamAsExpression; }

		// Parameter ----------------------------------------------------
		final        boolean IsVarArgs;
		final public boolean isVarArgs() { return this.IsVarArgs; }

		final Type[]    Params;
		final boolean[] PCBNs;
		
		public int getParameterCount() { return (this.Params == null)?0:this.Params.length; }

		final TypeRef ReturnTypeRef;
		/** Returns the context-unawareness return type */
		final public TypeRef getReturnTypeRef() {
			return this.ReturnTypeRef;
		}

		// These already include the unlimited parameter ---------------------------

		public Type getParameterType(int pInd) {
			if(pInd < 0)                         return null;
			if(pInd >= this.getParameterCount()) return null;
			if(this.Params == null)                        return null;
			if((pInd < 0) || (pInd >= this.Params.length)) return null;
			return this.Params[pInd];
		}
		public boolean canParameterBeNull(int pInd) {
			if(pInd < 0) return true;
			if(this.PCBNs == null)        return true;
			if(pInd >= this.PCBNs.length) return true;
			return this.PCBNs[pInd];
		}
		public boolean canBeAssignedBy(int pInd, Object pValue) {
			if(pInd < 0) return false;
			if(pInd >= this.getParameterCount()) return false;
			Type T = this.getParameterType(pInd);
			if(T      == null) return false;
			if(pValue == null) this.canParameterBeNull(pInd);
			return T.canBeAssignedBy(pValue);
		}
		// For internal use -------------------------------------------
		boolean checkCanBeAssignedBy(Context pContext, int pInd, Object pValue) {
			if((pValue == null) && (this.PCBNs != null) && (pInd < this.PCBNs.length) && !this.PCBNs[pInd])
				return false;
			
			Type T = this.Params[pInd];
			if(T == null) return false;
			return T.canBeAssignedBy(pContext, pValue);
		}

		// Sub Expr ---------------------------------------------------------------
			   boolean CanHaveSubExpr = false;
		public boolean canHaveSubExpr() { return this.CanHaveSubExpr; }
		// Representation ---------------------------------------------------------
		/** Returns a long string representation of this InstructionSpec */
		String toDetail_DataOnly() {	// ---------------------------------------
			StringBuffer SB = new StringBuffer();
			// Parameters -----------------------------------------------
			SB.append("(");
			if(this.getParameterCount() != 0) {
				for(int i = 0; i < this.getParameterCount(); i++) {
					if(i != 0) SB.append(",");
					SB.append(this.canParameterBeNull(i)?"":"+");
					SB.append(this.getParameterType(i).toString());
				}
			}
			if(this.IsVarArgs) SB.append("...");
			SB.append(")");
			// Sub Expression -------------------------------------------
			if(this.canHaveSubExpr()) SB.append("{}");
			return SB.toString();
		}
		/** Returns a short string representation of this InstructionSpec */
		@Override public String toString() {	// ---------------------------------------
			return "InstSpec " + this.getName() + ":" + this.getReturnTypeRef().toString();
		}
		/** Returns a long string representation of this InstructionSpec */
		@Override public String toDetail() {	// ---------------------------------------
			StringBuffer SB = new StringBuffer();
			SB.append("InstSpec: ");
			if(this.isFunctional()) SB.append("="); else SB.append(" ");
			SB.append(this.getName());
			SB.append(this.toDetail_DataOnly());
			SB.append(":").append(this.getReturnTypeRef().toString());
			return SB.toString();
		}

		/** Checks if this object and O are exactly the same object */
		@Override public boolean is(Object O) { return this == O; }

		/** Returns an integer representation of this obejct */
		@Override public int hash() { return this.hashCode(); }
	}

}