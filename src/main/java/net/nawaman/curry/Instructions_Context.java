package net.nawaman.curry;

import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.curry.Inst_AbstractVariable.VariableInfo;
import net.nawaman.util.UArray;

public class Instructions_Context {

	// Context Information -------------------------------------------------------------------------
	static public final class Inst_GetContextInfo extends Inst_AbstractSimple {
		static public final String Name = "getContextInfo";
		
		// Name
		static public final String StackName           = "StackName";
		static public final String StackIdentification = "StackIdentification";
		static public final String StackLocation       = "StackLocation";
		static public final String StackCode           = "StackCode";
		static public final String StackLineNumber     = "StackLineNumber";
		static public final String StackColumn         = "StackColumn";
		
		static public final String CurrentLineNumber     = "CurrentLineNumber";
		static public final String CurrentColumn         = "CurrentColumn";
		static public final String CurrentLocationString = "CurrentLocationString";
		static public final String CurrentStackTrace     = "CurrentStackTrace";
		static public final String CurrentDocumentation  = "CurrentDocumentation";
		
		static public final String StackOwner                = "StackOwner";
		static public final String StackOwner_As_Type        = "StackOwner_As_Type";
		static public final String StackOwner_As_CurrentType = "StackOwner_As_CurrentType";
		static public final String StackOwner_As_Package     = "StackOwner_As_Package";
		
		static public final String StackCaller            = "StackCaller";
		static public final String StackCaller_As_Type    = "StackCaller_As_Type";
		static public final String StackCaller_As_Package = "StackCaller_As_Package";

		static public final String DelegateSource = "DelegateSource";

		static public final String IsConstructor = "IsConstructor";
		
		// Hash
		static final int Hash_StackName           = StackName.hashCode();
		static final int Hash_StackIdentification = StackIdentification.hashCode();
		static final int Hash_StackLocation       = StackLocation.hashCode();
		static final int Hash_StackCode           = StackCode.hashCode();
		static final int Hash_StackLineNumber     = StackLineNumber.hashCode();
		static final int Hash_StackColumn         = StackColumn.hashCode();
		
		static final int Hash_CurrentLineNumber     = CurrentLineNumber.hashCode();
		static final int Hash_CurrentColumn         = CurrentColumn.hashCode();
		static final int Hash_CurrentLocationString = CurrentLocationString.hashCode();
		static final int Hash_CurrentStackTrace     = CurrentStackTrace.hashCode();
		static final int Hash_CurrentDocumentation  = CurrentDocumentation.hashCode();
		
		static final int Hash_StackOwner                = StackOwner.hashCode();
		static final int Hash_StackOwner_As_Type        = StackOwner_As_Type.hashCode();
		static final int Hash_StackOwner_As_CurrentType = StackOwner_As_CurrentType.hashCode();
		static final int Hash_StackOwner_As_Package     = StackOwner_As_Package.hashCode();
		
		static final int Hash_StackCaller            = StackCaller.hashCode();
		static final int Hash_StackCaller_As_Type    = StackCaller_As_Type.hashCode();
		static final int Hash_StackCaller_As_Package = StackCaller_As_Package.hashCode();
		
		static final int Hash_DelegateSource = DelegateSource.hashCode();
		
		static final int Hash_IsConstructor = IsConstructor.hashCode();
		
		Inst_GetContextInfo(Engine pEngine) {
			super(pEngine, Name + "(+$):~");
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			int InfoHash = ((String)pParams[0]).hashCode();
			if(InfoHash == Hash_StackName)           return pContext.getName();
			if(InfoHash == Hash_StackIdentification) return pContext.getStackIdentification();
			if(InfoHash == Hash_StackLocation)       return pContext.getStackLocation();
			
			if(InfoHash == Hash_StackCode) return pContext.getStackLocation().getSourceCode(this.getEngine());
			
			if(InfoHash == Hash_StackLineNumber) {
				Location L = pContext.getStackLocation();
				if(L == null) return 0;
				return L.getLineNumber();
			}
			if(InfoHash == Hash_StackColumn) {
				Location L = pContext.getStackLocation();
				if(L == null) return 0;
				return L.getColumn();
			}

			if(InfoHash == Hash_CurrentLineNumber) return pContext.getCurrentLineNumber();
			if(InfoHash == Hash_CurrentColumn)     return pContext.getCurrentColumn();
			
			if(InfoHash == Hash_CurrentLocationString) {
				LocationSnapshot L = pContext.getCurrentLocationSnapshot();
				if(L == null) return "<<- ROOT ->>";
				return L.toString();
			}
			if(InfoHash == Hash_CurrentStackTrace)
				return pContext.getLocationsToString();
			if(InfoHash == Hash_CurrentDocumentation)
				return pContext.getCurrentDocumentation();
			
			if(InfoHash == Hash_IsConstructor) return pContext.isConstructor();
			
			if(InfoHash == Hash_StackOwner)                return pContext.getStackOwner();
			if(InfoHash == Hash_StackOwner_As_Type)        return pContext.getStackOwnerAsType();
			if(InfoHash == Hash_StackOwner_As_CurrentType) return pContext.getStackOwnerAsCurrentType();
			if(InfoHash == Hash_StackOwner_As_Package)     return pContext.getStackOwnerAsPackage();
			
			if(InfoHash == Hash_StackCaller)            return pContext.getStackCaller();
			if(InfoHash == Hash_StackCaller_As_Type)    return pContext.getStackCallerAsType();
			if(InfoHash == Hash_StackCaller_As_Package) return pContext.getStackCallerAsPackage();
			
			if(InfoHash == Hash_DelegateSource) return pContext.getDelegateSource();
			
			return null;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			
			Object O = pExpr.getParam(0);
			if(O instanceof String) {
				int InfoHash = ((String)O).hashCode();
				
				if(InfoHash == Hash_StackName)             return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_StackIdentification)   return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_StackCode)             return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_StackLineNumber)       return TKJava.TInteger.getTypeRef();
				if(InfoHash == Hash_CurrentLineNumber)     return TKJava.TInteger.getTypeRef();
				if(InfoHash == Hash_CurrentLocationString) return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_CurrentStackTrace)     return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_IsConstructor)         return TKJava.TBoolean.getTypeRef();
				
				if(InfoHash == Hash_StackLocation) return TKJava.Instance.getTypeByClass(pCProduct.getEngine(), null, Location.class).getTypeRef();
				
				if(InfoHash == Hash_StackOwner) {
					if((pCProduct != null) && (pCProduct.getOwnerTypeRef() != null))
						 return pCProduct.getOwnerTypeRef();
					else return TKJava.TAny.getTypeRef();
				}
				
				if((InfoHash == Hash_StackOwner_As_Type) || (InfoHash == Hash_StackOwner_As_CurrentType)) {
					if(pCProduct != null)
					     return pCProduct.getOwnerTypeRef();
					else return TKJava.TVoid.getTypeRef();
				}
				
				if(InfoHash == Hash_StackOwner_As_Package)
					return TKPackage.newTypeTypeRef(pCProduct.getOwnerPackageBuilder().getName());

				if(InfoHash == Hash_StackCaller)            return TKJava.TAny.getTypeRef();
				if(InfoHash == Hash_StackCaller_As_Type)    return TKJava.TType.getTypeRef();
				if(InfoHash == Hash_StackCaller_As_Package) return TKJava.TPackage.getTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
		/**{@inherDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			if(!super.manipulateCompileContextFinish(pExpr, pCProduct)) return false;

			Object O = pExpr.getParam(1);
			if((O instanceof String) && (((String)O).hashCode() == Hash_StackOwner))
				return pCProduct.notifyAccessingElement(pExpr);
			
			return true;
		}
	}
	
	static public class Inst_GetExternalContext extends Inst_AbstractSimple {
		static public final String Name = "getExternalContext";
		
		Inst_GetExternalContext(Engine pEngine) {
			super(pEngine, Name + "():"+ExternalContext.class.getCanonicalName());
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return new ExternalContext(pContext);
		}
	}
	
	// Debugging -----------------------------------------------------------------------------------
	
	static public class Inst_IsBeingDebugged extends Inst_AbstractSimple {
		static public final String Name = "isBeingDebugged";
		
		Inst_IsBeingDebugged(Engine pEngine) {
			super(pEngine, Name + "():?");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.getExecutor().Debugger != null;
		}
	}
	
	static public class Inst_SendDebuggerMessage extends Inst_AbstractSimple {
		static public final String Name = "sendDebuggerMessage";
		
		Inst_SendDebuggerMessage(Engine pEngine) {
			super(pEngine, Name + "(~,~...):~");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Debugger D = pContext.getExecutor().Debugger;
			if(D == null) return null;			
			Object[] Params = UArray.getObjectArray(pParams[1]);
			return D.sendMessage(pContext, pParams[0], Params);
		}
	}
	
	// Variable related ----------------------------------------------------------------------------
	
	static abstract class Inst_NewLocal extends Inst_AbstractSimple {
		boolean IsConstant;
		Inst_NewLocal(Engine pEngine, String ISpecStr, boolean pIsConstant) {
			super(pEngine, ISpecStr);
			this.IsConstant = pIsConstant;
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Type   T = (Type)pParams[1];
			Object V = pParams[2];
			
			// Set default value for primitive array			
			if((V == null) && (T instanceof TKJava.TJava) &&
				(
					Number   .class.isAssignableFrom(((TKJava.TJava)T).getTheDataClass()) ||
					Character.class.isAssignableFrom(((TKJava.TJava)T).getTheDataClass()) ||
					Boolean  .class.isAssignableFrom(((TKJava.TJava)T).getTheDataClass())
			)) {
				if(     T == TKJava.TBoolean)   V =    false;
				else if(T == TKJava.TCharacter) V =      '0';
				else if(T == TKJava.TInteger)   V =        0;
				else if(T == TKJava.TDouble)    V =      0.0;
				else if(T == TKJava.TByte)      V =  (byte)0;
				else if(T == TKJava.TLong)      V =  (long)0;
				else if(T == TKJava.TFloat)     V =     0.0f;
				else if(T == TKJava.TShort)     V = (short)0;
			}
			
			if(!this.IsConstant) return pContext.newVariable(pContext.getEngine(), (String)pParams[0], T, V);
			else                 return pContext.newConstant(pContext.getEngine(), (String)pParams[0], T, V);
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(1);
			if(O instanceof Type) return ((Type)O).getTypeRef();
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
		/**@inherDoc()*/ @Override
		public boolean manipulateCompileContextBeforeSub(Object[] pParams, CompileProduct pCProduct, int pPosition) {
			Object O1 = pParams[0];
			Object O2 = pParams[1];
			O2 = pCProduct.getReturnTypeRefOf(O2);
			if(!(O1 instanceof String)) return true;

			TypeRef TR = TKJava.TAny.getTypeRef();
			if(O2 instanceof TypeTypeRef) TR = ((TypeTypeRef)O2).getTheRef();
				
			if(pCProduct.isLocalVariableExist((String)O1)) {
				pCProduct.reportError(
						String.format("The local variable `%s` is already exist. <newVariable:238>", (String)O1),
						null, pPosition);
				return false;
			}					

			if(!this.IsConstant) return pCProduct.newVariable((String)O1, TR);
			else                 return pCProduct.newConstant((String)O1, TR);
		}
		/**@inherDoc()*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			Object O1 = pExpr.getParam(0);
			if(!(O1 instanceof String)) return true;
			
			// The variable should already be there from this.manipulateCompileContextBeforeSub(...) so no need to
			//    create again.
			
			Object O2 = pExpr.getParam(1);
			O2 = pCProduct.getReturnTypeRefOf(O2);

			TypeRef TR = TKJava.TAny.getTypeRef();
			if(O2 instanceof TypeTypeRef) TR = ((TypeTypeRef)O2).getTheRef();
			
			Object Value = pExpr.getParam(2);
			
			if(Value != null) {
				MType   MT       = pCProduct.getEngine().getTypeManager();
				TypeRef TS       = TKJava.TString.getTypeRef();
				TypeRef TRef     = pCProduct.getReturnTypeRefOf(Value);
				// Everything can be convert to string, so it TR is string, TRef must be a string.
				Object MMResult = null;
				if(TR.equals(TRef))     MMResult = true;
				else if(!TS.equals(TR)) MMResult = Boolean.TRUE.equals(MT.mayTypeRefBeCastedTo(TR, TRef));
									
				Boolean MayMatch = (MMResult instanceof Boolean)?(Boolean)MMResult:null;
				if(!Boolean.TRUE.equals(MayMatch)) {
					return ReportCompileProblem(
							"newLocal:273", String.format("Imcompatible type `%s` (%s to %s)", (String)O1, TRef, TR),
							pExpr, pCProduct, (MayMatch == null), false);
				}
			}
			return true;
		}
	}
	
	
	static public class Inst_NewVariable extends Inst_NewLocal {
		static public final String Name = "newVariable";
		Inst_NewVariable(Engine pEngine) {
			super(pEngine, Name + "(+$,+!,~):~", false);
		}
	}
	
	static public class Inst_NewConstant extends Inst_NewLocal {
		static public final String Name = "newConstant";
		Inst_NewConstant(Engine pEngine) {
			super(pEngine, Name + "(+$,+!,~):~", true);
		}
	}
	
	/** A Variable that will be create only when it does not already exist. This is useful for Fragment and Macro */
	static public class Inst_NewBorrowedVariable extends Inst_NewLocal {
		static public final String Name = "newBorrowedVariable";
		
		Inst_NewBorrowedVariable(Engine pEngine) {
			super(pEngine, Name + "(+$,+!,~):~", false);
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.newBorrowedVariable((String)pParams[0], (Type)pParams[1], pParams[2]);
		}
	}
	
	/** A Variable that will be create only when it does not already exist. This is useful for Fragment and Macro */
	static public class Inst_NewBorrowedConstant extends Inst_NewLocal {
		static public final String Name = "newBorrowedConstant";
		
		Inst_NewBorrowedConstant(Engine pEngine) {
			super(pEngine, Name + "(+$,+!,~):~", true);
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.newBorrowedConstant((String)pParams[0], (Type)pParams[1], pParams[2]);
		}
	}
	
	/** Variable information of local variables */
	static public class LocalVar implements VariableInfo {
		
		static public final VariableInfo Instance = new LocalVar(); 
		
		/**{@inheritDoc}*/ @Override
		public String getVariableIdentity(Expression pExpr, CompileProduct pCProduct,
				boolean pIsWithCapitolThe) {
			return (pIsWithCapitolThe?"The ":"the ") +  String.format("local variable '%s'", pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkParameterTypes(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(0) instanceof String);
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkExistanceCompileTime(Expression pExpr, CompileProduct pCProduct) {
			String VName = (String)pExpr.getParam(0);
			if(VName == null) return false;
			return (pCProduct.isVariableExist(VName) ||
					pCProduct.isGlobalVariableExist(VName) ||
					pCProduct.isEngineVariableExist(VName));
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getVariableTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getVariableTypeRef((String)pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkConstantCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.isConstant((String)pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkNeedCompatibleTypeCompileTime(Expression pExpr,
				CompileProduct pCProduct) {
			// TODOSOON - True for the moment.
			return true;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getAssignedValueTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(1));
		}
		/**{@inheritDoc}*/ @Override
		public boolean isAssignedValueNull(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(1) == null);
		}
	}
	
	static public class Inst_SetVarValue extends Inst_AbstractVariable.Inst_SetVariableValue {
		static public final String Name = "setVarValue";
		
		Inst_SetVarValue(Engine pEngine) {
			super(pEngine, Name + "(+$,~):~", LocalVar.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.setVariableValue((String)pParams[0], pParams[1]);
		}
	}
	
	static public class Inst_GetVarValue extends Inst_AbstractVariable {
		static public final String Name = "getVarValue";
		
		Inst_GetVarValue(Engine pEngine) {
			super(pEngine, Name + "(+$):~", LocalVar.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getVariableValue((String)pParams[0]);
		}
	}
	
	static public class Inst_IsVarExist extends Inst_AbstractVariable.Inst_IsVariableExist {
		static public final String Name = "isVarExist";
		
		Inst_IsVarExist(Engine pEngine) {
			super(pEngine, Name + "(+$):?");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.isVariableExist((String)pParams[0]);
		}
	}
	static public class Inst_IsLocalVarExist extends Inst_AbstractVariable.Inst_IsVariableExist {
		static public final String Name = "isLocalVarExist";
		
		Inst_IsLocalVarExist(Engine pEngine) {
			super(pEngine, Name + "(+$):?");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.isLocalVariableExist((String)pParams[0]);
		}
	}
	
	static public class Inst_IsConstant extends Inst_AbstractVariable.Inst_IsVariableConstant {
		static public final String Name = "isConstant";
		
		Inst_IsConstant(Engine pEngine) {
			super(pEngine, Name + "(+$):?", LocalVar.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.isVariableConstant((String)pParams[0]);
		}
	}
	
	static public class Inst_IsVariableDefaultDataHolder extends Inst_AbstractVariable.Inst_IsVariableConstant {
		static public final String Name = "isVariableDefaultDataHolder";
		
		Inst_IsVariableDefaultDataHolder(Engine pEngine) {
			super(pEngine, Name + "(+$):?", LocalVar.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.isVariableDefaultDataHolder((String)pParams[0]);
		}
	}

	static public class Inst_CheckVariableDataHolderFactory extends Inst_AbstractVariable.Inst_IsVariableConstant {
		static public final String Name = "checkVariableDataHolderFactory";
		
		Inst_CheckVariableDataHolderFactory(Engine pEngine) {
			super(pEngine, Name + "(+$,+$):?", LocalVar.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.checkVariableDataHolderFactory((String)pParams[0], (String)pParams[1]);
		}
	}

	static public class Inst_GetVarType extends Inst_AbstractVariable.Inst_GetVariableType {
		static public final String Name = "getVarType";
		
		Inst_GetVarType(Engine pEngine) {
			super(pEngine, Name + "(+$):!", LocalVar.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.getType(pContext.getEngine(), (String)pParams[0]);
		}
	}
	
	// Parent variable -----------------------------------------------------------------------------

	/** Variable information for parent variable (using stack count) */
	static public class ParentVar_BC implements VariableInfo {
		
		static public final VariableInfo Instance = new ParentVar_BC(); 

		/**{@inheritDoc}*/ @Override
		public String getVariableIdentity(Expression pExpr, CompileProduct pCProduct,
				boolean pIsWithCapitolThe) {
			Object O0 = pExpr.getParam(0);
			Object O1 = pExpr.getParam(1);
			StringBuilder I = new StringBuilder();
			if((O0 instanceof Integer) && (O1 instanceof String)) {
				int    VCount = ((Integer)O0).intValue();
				String VName  = (String)O1;
				for(int i = VCount; --i >= 0; ) I.append("parent."); I.append(VName);
			}
			return (pIsWithCapitolThe?"The ":"the ") +  String.format("parent variable '%s'", I);
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkParameterTypes(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(0) instanceof Integer) && (pExpr.getParam(1) instanceof String);
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkExistanceCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.isParentVariableExist((Integer)pExpr.getParam(0), (String)pExpr.getParam(1));
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getVariableTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getParentVariableTypeRef((Integer)pExpr.getParam(0), (String)pExpr.getParam(1));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkConstantCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.isParentVariableConstant((Integer)pExpr.getParam(0), (String)pExpr.getParam(1));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkNeedCompatibleTypeCompileTime(Expression pExpr,
				CompileProduct pCProduct) {
			// TODOSOON - True for the moment.
			return true;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getAssignedValueTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(2));
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean isAssignedValueNull(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(2) == null);
		}
	}
	
	static public class Inst_SetParentVarValue extends Inst_AbstractVariable.Inst_SetVariableValue {
		static public final String Name = "setParentVarValue";
		
		Inst_SetParentVarValue(Engine pEngine) {
			super(pEngine, Name + "(+i,+$,~):~", ParentVar_BC.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.setParentVariableValue((Integer)pParams[0], (String)pParams[1], pParams[2]);
		}
	}
	
	static public class Inst_GetParentVarValue extends Inst_AbstractVariable {
		static public final String Name = "getParentVarValue";
		
		Inst_GetParentVarValue(Engine pEngine) {
			super(pEngine, Name + "(+i,+$):~", ParentVar_BC.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.getParentVariableValue((Integer)pParams[0], (String)pParams[1]);
		}
	}

	static public class Inst_IsParentVarExist extends Inst_AbstractVariable.Inst_IsVariableExist {
		static public final String Name = "isParentVarExist";
		
		Inst_IsParentVarExist(Engine pEngine) {
			super(pEngine, Name + "(+i,+$):?");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.isParentVariableExist((Integer)pParams[0], (String)pParams[1]);
		}
	}

	static public class Inst_IsParentVarConstant extends Inst_AbstractVariable.Inst_IsVariableConstant {
		static public final String Name = "isParentVarConstant";
		
		Inst_IsParentVarConstant(Engine pEngine) {
			super(pEngine, Name + "(+i,+$):?", ParentVar_BC.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.isParentVariableConstant((Integer)pParams[0], (String)pParams[1]);
		}
	}

	static public class Inst_GetParentVarType extends Inst_AbstractVariable.Inst_GetVariableType {
		static public final String Name = "getParentVarType";
		
		Inst_GetParentVarType(Engine pEngine) {
			super(pEngine, Name + "(+i,+$):!", ParentVar_BC.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.getParentVariableType((Integer)pParams[0], (String)pParams[1]);
		}
	}
		
	// Parent variable By Stack Name --------------------------------------------------------------

	/** Variable information for parent variable (using stack name) */
	static public class ParentVar_BN implements VariableInfo {
		
		static public final VariableInfo Instance = new ParentVar_BN(); 

		/**{@inheritDoc}*/ @Override
		public String getVariableIdentity(Expression pExpr, CompileProduct pCProduct,
				boolean pIsWithCapitolThe) {
			String SName = (String)pExpr.getParam(0);
			String VName = (String)pExpr.getParam(1);
			return (pIsWithCapitolThe?"The ":"the ") +  String.format("parent variable `%s.%s`", SName, VName);
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkParameterTypes(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(0) instanceof String) && (pExpr.getParam(1) instanceof String);
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkExistanceCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.isParentVariableExist((String)pExpr.getParam(0), (String)pExpr.getParam(1));
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getVariableTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getParentVariableTypeRef((String)pExpr.getParam(0), (String)pExpr.getParam(1));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkConstantCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.isParentVariableConstant((String)pExpr.getParam(0), (String)pExpr.getParam(1));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkNeedCompatibleTypeCompileTime(Expression pExpr,
				CompileProduct pCProduct) {
			// TODOSOON - True for the moment.
			return true;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getAssignedValueTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(2));
		}
		/**{@inheritDoc}*/ @Override
		public boolean isAssignedValueNull(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(2) == null);
		}
	}
	
	static public class Inst_SetParentVarValueByStackName extends Inst_AbstractVariable.Inst_SetVariableValue {
		static public final String Name = "setParentVarValueByStackName";
		
		Inst_SetParentVarValueByStackName(Engine pEngine) {
			super(pEngine, Name + "(+$,+$,~):~", ParentVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.setParentVariableValue((String)pParams[0], (String)pParams[1], pParams[2]);
		}
	}
	
	static public class Inst_GetParentVarValueByStackName extends Inst_AbstractVariable {
		static public final String Name = "getParentVarValueByStackName";
		
		Inst_GetParentVarValueByStackName(Engine pEngine) {
			super(pEngine, Name + "(+$,+$):~", ParentVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getParentVariableValue((String)pParams[0], (String)pParams[1]);
		}
	}

	static public class Inst_IsParentVarExistByStackName extends Inst_AbstractVariable.Inst_IsVariableExist {
		static public final String Name = "isParentVarExistByStackName";
		
		Inst_IsParentVarExistByStackName(Engine pEngine) {
			super(pEngine, Name + "(+$,+$):?");
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.isParentVariableExist((String)pParams[0], (String)pParams[1]);
		}
	}

	static public class Inst_IsParentVarConstantByStackName extends Inst_AbstractVariable.Inst_IsVariableConstant {
		static public final String Name = "isParentVarConstantByStackName";
		
		Inst_IsParentVarConstantByStackName(Engine pEngine) {
			super(pEngine, Name + "(+$,+$):?", ParentVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.isParentVariableConstant((String)pParams[0], (String)pParams[1]);
		}
	}

	static public class Inst_GetParentVarTypeByStackName extends Inst_AbstractVariable.Inst_GetVariableType {
		static public final String Name = "getParentVarTypeByStackName";
		
		Inst_GetParentVarTypeByStackName(Engine pEngine) {
			super(pEngine, Name + "(+$,+$):!", ParentVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getParentVariableType((String)pParams[0], (String)pParams[1]);
		}
	}
	
	// Engine Variable related ---------------------------------------------------------------------

	/** Variable information for engine variable */
	static public class EngineVar_BN implements VariableInfo {
		
		static public final VariableInfo Instance = new EngineVar_BN(); 

		/**{@inheritDoc}*/ @Override
		public String getVariableIdentity(Expression pExpr, CompileProduct pCProduct, boolean pIsWithCapitolThe) {
			String EName = (String)pExpr.getParam(0);
			return (pIsWithCapitolThe?"The ":"the ") +  String.format("engine variable `%s`", EName);
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkParameterTypes(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(0) instanceof String);
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkExistanceCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.isEngineVariableExist((String)pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getVariableTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getEngineVariableTypeRef((String)pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkConstantCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.isEngineVariableConstant((String)pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkNeedCompatibleTypeCompileTime(Expression pExpr, CompileProduct pCProduct) {
			// TODOSOON - True for the moment.
			return true;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getAssignedValueTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(1));
		}
		/**{@inheritDoc}*/ @Override
		public boolean isAssignedValueNull(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(1) == null);
		}
	}
	
	static public class Inst_SetEngineVarValue extends Inst_AbstractVariable.Inst_SetVariableValue {
		static public final String Name = "setEngineVarValue";
		
		Inst_SetEngineVarValue(Engine pEngine) {
			super(pEngine, Name + "(+$,~):~", EngineVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.setEngineVariableValue((String)pParams[0], pParams[1]);
		}
	}
	
	static public class Inst_GetEngineVarValue extends Inst_AbstractVariable {
		static public final String Name = "getEngineVarValue";
		
		Inst_GetEngineVarValue(Engine pEngine) {
			super(pEngine, Name + "(+$):~", EngineVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getEngineVariableValue((String)pParams[0]);
		}
	}

	static public class Inst_IsEngineVarExist extends Inst_AbstractVariable.Inst_IsVariableExist {
		static public final String Name = "isEngineVarExist";
		
		Inst_IsEngineVarExist(Engine pEngine) {
			super(pEngine, Name + "(+$):?");
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.isEngineVariableExist((String)pParams[0]);
		}
	}

	static public class Inst_IsEngineVarConstant extends Inst_AbstractVariable.Inst_IsVariableConstant {
		static public final String Name = "isEngineVarConstant";
		
		Inst_IsEngineVarConstant(Engine pEngine) {
			super(pEngine, Name + "(+$):?", EngineVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.isEngineVariableConstant((String)pParams[0]);
		}
	}

	static public class Inst_GetEngineVarType extends Inst_AbstractVariable.Inst_GetVariableType {
		static public final String Name = "getEngineVarType";
		
		Inst_GetEngineVarType(Engine pEngine) {
			super(pEngine, Name + "(+$):!", EngineVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getEngineVariableType((String)pParams[0]);
		}
	}
	
	// Global Variable related ---------------------------------------------------------------------
	
	static public class Inst_NewGlobalVariable extends Inst_AbstractSimple {
		static public final String Name = "newGlobalVariable";
		
		Inst_NewGlobalVariable(Engine pEngine) {
			super(pEngine, Name + "(+$,+!,~,+?):~");
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.newGlobalVariable(
					(String)pParams[0],
					(Type)pParams[1],
					pParams[2],
					((Boolean)pParams[3]).booleanValue());
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			if(pCProduct == null) return super.getReturnTypeRef(pExpr, pCProduct);
			Object VarName = pExpr.getParam(0);
			if(!(VarName instanceof String)) return super.getReturnTypeRef(pExpr, pCProduct);
			return pCProduct.getGlobalVariableTypeRef((String)VarName);
		}
		/**@inherDoc()*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			Object O0 = pExpr.getParam(0);
			Object O1 = pExpr.getParam(1);
			O1 = pCProduct.getReturnTypeRefOf(O1);
			TypeRef TR = TKJava.TAny.getTypeRef();
			if(O0 instanceof String) {
				if(O1 instanceof TypeTypeRef) TR = ((TypeTypeRef)O1).getTheRef();
				String VName      = (String)O0;
				Object IsConstant = pExpr.getParam(3);
				if(!(VName      instanceof String))  return super.manipulateCompileContextFinish(pExpr, pCProduct);
				if(!(IsConstant instanceof Boolean)) return super.manipulateCompileContextFinish(pExpr, pCProduct);
				
				if(!pCProduct.isGlobalVariableExist(VName))
					return ReportCompileProblem(
							"newGlobalVariable:755", String.format("The global variable `%s` is already exist.", VName),
							pExpr, pCProduct, false, false);
				else {
					Object Value = pExpr.getParam(2);
					
					if(Value != null) {
						TypeRef TRef = pCProduct.getReturnTypeRefOf(Value);
						Boolean MayMatch = pCProduct.getEngine().getTypeManager().mayTypeRefBeCastedTo(TR, TRef);
						if(!Boolean.TRUE.equals(MayMatch))
							return ReportCompileProblem(
									"newGlobalVariable:762",
									String.format("Imcompatible type `%s` (%s to %s)", VName, TRef, TR),
									pExpr, pCProduct, (MayMatch == null), false);
					}
						
					pCProduct.newGlobalVariable(VName, TR, Boolean.TRUE.equals(IsConstant));
				}
			}
			return true;
		}
	}

	/** Variable information for global variable */
	static public class GlobalVar_BN implements VariableInfo {
		
		static public final VariableInfo Instance = new GlobalVar_BN(); 

		/**{@inheritDoc}*/ @Override
		public String getVariableIdentity(Expression pExpr, CompileProduct pCProduct, boolean pIsWithCapitolThe) {
			String GName = (String)pExpr.getParam(0);
			return (pIsWithCapitolThe?"The ":"the ") +  String.format("global variable `%s`", GName);
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkParameterTypes(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(0) instanceof String);
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkExistanceCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.isGlobalVariableExist((String)pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getVariableTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getGlobalVariableTypeRef((String)pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkConstantCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.isGlobalVariableConstant((String)pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public boolean checkNeedCompatibleTypeCompileTime(Expression pExpr, CompileProduct pCProduct) {
			// TODOSOON - True for the moment.
			return true;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getAssignedValueTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(1));
		}
		/**{@inheritDoc}*/ @Override
		public boolean isAssignedValueNull(Expression pExpr, CompileProduct pCProduct) {
			return (pExpr.getParam(1) == null);
		}
	}
	
	static public class Inst_SetGlobalVarValue extends Inst_AbstractVariable.Inst_SetVariableValue {
		static public final String Name = "setGlobalVarValue";
		
		Inst_SetGlobalVarValue(Engine pEngine) {
			super(pEngine, Name + "(+$,~):~", GlobalVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.setEngineVariableValue((String)pParams[0], pParams[1]);
		}
	}
	
	static public class Inst_GetGlobalVarValue extends Inst_AbstractVariable {
		static public final String Name = "getGlobalVarValue";
		
		Inst_GetGlobalVarValue(Engine pEngine) {
			super(pEngine, Name + "(+$):~", GlobalVar_BN.Instance);
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.getGlobalVariableValue((String)pParams[0]);
		}
	}

	static public class Inst_IsGlobalVarExist extends Inst_AbstractVariable.Inst_IsVariableExist {
		static public final String Name = "isGlobalVarExist";
		
		Inst_IsGlobalVarExist(Engine pEngine) {
			super(pEngine, Name + "(+$):?");
		}
		/**{@inheritDoc}*/ @Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.isGlobalVariableExist((String)pParams[0]);
		}
	}

	static public class Inst_IsGlobalVarConstant extends Inst_AbstractVariable.Inst_IsVariableConstant {
		static public final String Name = "isGlobalVarConstant";
		
		Inst_IsGlobalVarConstant(Engine pEngine) {
			super(pEngine, Name + "(+$):?", GlobalVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override protected Object run(Context pContext, Object[] pParams) {
			return pContext.isGlobalVariableConstant((String)pParams[0]);
		}
	}

	static public class Inst_GetGlobalVarType extends Inst_AbstractVariable.Inst_GetVariableType {
		static public final String Name = "getGlobalVarType";
		
		Inst_GetGlobalVarType(Engine pEngine) {
			super(pEngine, Name + "(+$):!", GlobalVar_BN.Instance);
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getGlobalVariableType((String)pParams[0]);
		}
	}
	
	static public final class Inst_ControlGlobalContext extends Inst_AbstractSimple {
		static public final String Name = "controlGlobalContext";
		
		// Name
		static public final String IsGlobalContext          = "IsGlobalContext";
		static public final String IsNewGlobalVarEnabled    = "IsNewGlobalVarEnabled";
		static public final String EnableGlobalNewVar       = "EnableGlobalNewVar";
		static public final String DisableGlobalNewVar      = "DisableGlobalNewVar";
		static public final String EnableGlobalNewVarToAll  = "EnableGlobalNewVarToAll";
		static public final String DisableGlobalNewVarToAll = "DisableGlobalNewVarToAll";
		
		// Hash
		static final int Hash_IsGlobalContext          = IsGlobalContext.hashCode();
		static final int Hash_IsNewGlobalVarEnabled    = IsNewGlobalVarEnabled.hashCode();
		static final int Hash_EnableGlobalNewVar       = EnableGlobalNewVar.hashCode();
		static final int Hash_DisableGlobalNewVar      = DisableGlobalNewVar.hashCode();
		static final int Hash_EnableGlobalNewVarToAll  = EnableGlobalNewVarToAll.hashCode();
		static final int Hash_DisableGlobalNewVarToAll = DisableGlobalNewVarToAll.hashCode();
		
		Inst_ControlGlobalContext(Engine pEngine) {
			super(pEngine, Name + "(+$):~");
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			int InfoHash = ((String)pParams[0]).hashCode();
			if(InfoHash == Hash_IsGlobalContext)          return pContext.isGlobalStack();
			if(InfoHash == Hash_IsNewGlobalVarEnabled)    return pContext.isNewGlobalVariableEnabled();
			if(InfoHash == Hash_EnableGlobalNewVar)       return pContext.enableGlobalNewVariable();
			if(InfoHash == Hash_DisableGlobalNewVar)      return pContext.disableGlobalNewVariable();
			if(InfoHash == Hash_EnableGlobalNewVarToAll)  return pContext.enableGlobalNewVariableToAll();
			if(InfoHash == Hash_DisableGlobalNewVarToAll) return pContext.disableGlobalNewVariableToAll();
			
			return null;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O instanceof String) {
				int InfoHash = ((String)O).hashCode();
				if(InfoHash == Hash_IsGlobalContext)            return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_IsNewGlobalVarEnabled)    return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_EnableGlobalNewVar)       return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_DisableGlobalNewVar)      return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_EnableGlobalNewVarToAll)  return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_DisableGlobalNewVarToAll) return TKJava.TBoolean.getTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
}
