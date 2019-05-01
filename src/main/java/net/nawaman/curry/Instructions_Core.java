package net.nawaman.curry;

import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.Expression.Expr_Expr;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.util.UArray;
import net.nawaman.util.UClass;
import net.nawaman.util.UNumber;
import net.nawaman.util.UObject;

public class Instructions_Core {
	
	// Core ----------------------------------------------------------------------------------------

	// Engine Information --------------------------------------------------------------------------
	static public final class Inst_GetEngineInfo extends Inst_AbstractSimple {
		static public final String Name = "getEngineInfo";
		
		// Name
		static public final String EngineName           = "EngineName";
		static public final String EngineExtensionNames = "EngineExtensionNames";
		static public final String EngineSignature      = "EngineSignature";
		static public final String EngineContextName    = "EngineContextName";
		static public final String GlobalContextName    = "GlobalContextName";

		static public final String PrintInstructionInfo = "PrintInstructionInfo";
		
		// Hash
		static final int Hash_EngineName           = EngineName.hashCode();
		static final int Hash_EngineExtensionNames = EngineExtensionNames.hashCode();
		static final int Hash_EngineSignature      = EngineSignature.hashCode();
		static final int Hash_EngineContextName    = EngineContextName.hashCode();
		static final int Hash_GlobalContextName    = GlobalContextName.hashCode();
		
		static final int Hash_PrintInstructionInfo = PrintInstructionInfo.hashCode();
		
		Inst_GetEngineInfo(Engine pEngine) {
			super(pEngine, "=" + Name + "(+$):~");
		}
		
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			int InfoHash = ((String)pParams[0]).hashCode();
			if(InfoHash == Hash_EngineName)        return pContext.getEngine().getName();
			if(InfoHash == Hash_EngineSignature)   return pContext.getEngine().getSignature();
			if(InfoHash == Hash_EngineContextName) return pContext.getEngine().getEngineContextName();
			
			if(InfoHash == Hash_EngineExtensionNames) {
				return pContext.getEngine().ExtensionNames.toArray(new String[0]);
			}
			
			if(InfoHash == Hash_PrintInstructionInfo) {
				StringBuffer SB = new StringBuffer();
				Engine E = pContext.getEngine();
				int[] IHs = E.InstructionHashs;
				for(int i = 1; i < E.InstructionCount; i++) {
					SB.append("\n");
					SB.append(i);
					SB.append("#:");
					int hSearch = IHs[i];
					SB.append("Hash = ");
					SB.append(hSearch);
					SB.append("; Signature = ");
					SB.append(E.getInstruction(null, hSearch).getSpecification().toDetail());
				}
				return SB.toString();
			}
			return null;
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O instanceof String) {
				int InfoHash = ((String)O).hashCode();
				if(InfoHash == Hash_EngineName)           return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_EngineSignature)      return TKJava.TSerializable.getTypeRef();
				if(InfoHash == Hash_EngineContextName)    return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_EngineExtensionNames) return TKArray.StringArrayRef;
				if(InfoHash == Hash_PrintInstructionInfo) return TKJava.TString.getTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	

	static public final class Inst_GetEngine extends Inst_AbstractSimple {
		static public final String Name = "getEngine";
		
		Inst_GetEngine(Engine pEngine) {
			super(pEngine, "=" + Name + "():"+Engine.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			// Returns the data
			return pContext.getEngine();
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return TKJava.TEngine.getTypeRef();
		}
	}

	// Store a Data that will not be cache.
	// This is very useful storing Expression that is a data not to be execute as a parameter

	static public final class Inst_Data extends Inst_AbstractSimple {
		static public final String Name = "data";
		
		Inst_Data(Engine pEngine) {
			super(pEngine, Name + "(~):~");
		}
		/**{@inherDoc}*/ @Override
		public Expression newExpression() {
			return super.newExpression(new Object[]{ null });
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			// Returns the data
			return pParams[0];
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O instanceof Expression) return ((Expression)O).getReturnTypeRef(pCProduct);
			return pCProduct.getEngine().getTypeManager().getTypeOf(O).getTypeRef();
		}
	}
	static public final class Inst_Type extends Inst_AbstractSimple {
		static public final String Name = "type";
		
		Inst_Type(Engine pEngine) {
			super(pEngine, "=" + Name + "(+"+TypeRef.class.getCanonicalName()+"):!");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			TypeRef TR        = (TypeRef)pParams[0];
			MType   MT        = this.Engine.getTypeManager();
			boolean IsDynamic = TypeRef.isTypeRefDynamic(TR);
			
			// In the case of a Dynamic TypeRef, the type will be return by the TypeRef will be reset and
			//    ResultNoCache is used to avoid cache for functional
			if(IsDynamic) TR.setTheType(null);

			Type T  = TR.getTheType();
			if(T == null) {
				MT.ensureTypeValidated(pContext, TR, null);
				T  = TR.getTheType();
			}
			
			// In the case of a Dynamic TypeRef, the type will be return by the TypeRef will be reset and
			//    ResultNoCache is used to avoid cache for functional
			if(IsDynamic) {	
				TR.setTheType(null);
				return new SpecialResult.ResultNoCache(T);
			}
			// Returns the data
			MT.checkPermissionOfType(pContext, T);
			return T;
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O instanceof TypeRef) return new TypeTypeRef((TypeRef)O);
			return new TypeTypeRef(super.getReturnTypeRef(pExpr, pCProduct));
		}
	}

	static public final class Inst_Doc extends Inst_AbstractSimple {
		static public final String Name = "doc";
		
		ActionRecordHook ARHook;
		
		Inst_Doc(Engine pEngine) {
			super(pEngine, Name + "(net.nawaman.curry.Documentation,E):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			// Set the documentation
			Documentation Doc = (Documentation)pParams[0];
			if(Doc != null) pContext.setCurrentDocumentation(Doc);
			
			if(this.ARHook != null) {
				this.ARHook.ARecord = ExternalContext.newActionRecord(pContext);
				this.ARHook = null;
			}
			
			// Returns the data
			Object O = pParams[1];
			if(!(O instanceof Expression)) return O; 
			Object Result = this.executeAnExpression(pContext, (Expression)O);
			
			// Clear the documenation
			if(Doc != null) pContext.setCurrentDocumentation(null);
				
			return Result;
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(!(O instanceof Expr_Expr)) return TKJava.TAny.getTypeRef();
			return pCProduct.getReturnTypeRefOf(((Expr_Expr)O).getExpr());
		}
	}
	
	static public class Inst_Cast extends Inst_AbstractSimple {
		static public final String Name = "cast";
		
		// StackName, Object, Type to cast to, Variable name, Default value
		Inst_Cast(Engine pEngine, String          pISpecStr) { super(pEngine, pISpecStr);               }
		Inst_Cast(Engine pEngine, InstructionSpec pISpec)    { super(pEngine, pISpec);                  }
		Inst_Cast(Engine pEngine)                            { super(pEngine, "=" + Name + "(+!,~):~"); }
		
		// Execution --------------------------------------------------------------
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Type   T = (Type)pParams[0];
			Object O = pParams[1];
			Object V = O;
			boolean CanCast = ((O == null) || T.canBeAssignedBy(O) || ((O != null) && ((V = TKJava.tryToCastTo(O, T)) != null)));
			if(CanCast) return V;
			throw new ClassCastException("Curry Type: " + T.toString());
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pCProduct.getReturnTypeRefOf(pExpr.getParam(0));
			if(!(O instanceof TypeTypeRef)) return new TypeTypeRef(TKJava.TAny.getTypeRef());
			return ((TypeTypeRef)O).getTheRef();
		}
	}
	
	static public class Inst_CastOrElse extends Inst_Stack {
		@SuppressWarnings("hiding")
		static public final String Name = "castOrElse";
		
		// StackName, Object, Type to cast to, Variable name, Default value
		Inst_CastOrElse(Engine pEngine, String          pISpecStr) { super(pEngine, pISpecStr); }
		Inst_CastOrElse(Engine pEngine, InstructionSpec pISpec)    { super(pEngine, pISpec);    }
		Inst_CastOrElse(Engine pEngine)                            { super(pEngine, Name + "(+!,~){}:~"); }
		
		// Execution --------------------------------------------------------------
		/**@inherDoc()*/ @Override
		protected String getStackName(Object[] pParams) {
			return null;
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Type   T = (Type)pParams[0];
			Object O = pParams[1];
			Object V = O;
			boolean CanCast = ((O == null) || T.canBeAssignedBy(O) || ((O != null) && ((V = TKJava.tryToCastTo(O, T)) != null)));
			if(CanCast) return V;
			return super.run(pContext, pExpr, pParams);
		}
		/** Process exiting result. */
		@Override protected Object processResult(Context pContext, Expression pExpr, Object[] pParams, Object pResult) {
			Object Result = super.processResult(pContext, pExpr, pParams, pResult);
			Type   T = (Type)pParams[0];
			if(!T.canBeAssignedBy(Result)) {
				Result = TKJava.tryToCastTo(Result, T);
				if(Result != null) return Result;
				throw new ClassCastException("Curry Type: " + T.toString());
			}
			return Result;
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pCProduct.getReturnTypeRefOf(pExpr.getParam(0));
			if(!(O instanceof TypeTypeRef)) return new TypeTypeRef(TKJava.TAny.getTypeRef());
			return ((TypeTypeRef)O).getTheRef();
		}
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextBeforeSub(Object[] pParams,
				CompileProduct pCProduct, int pPosition) {
			pCProduct.newScope(null, null);
			return true;
		}
	}

	static public final class Inst_Group extends Inst_AbstractGroup {
		static public final String Name = "group";
		
		Inst_Group(Engine pEngine) {
			super(pEngine, Name + "(){}:~");
		}
	}
	
	// Execute just once
	static public final class Inst_RunOnce extends Inst_AbstractGroup {
		static public final String Name = "runOnce";
		
		Inst_RunOnce(Engine pEngine) {
			super(pEngine, Name + "(){}:~");
		}
		/**@inherDoc()*/ @Override
		protected Expression createExpression(int pCol, int pRow, Object[] pParameters, Expression[] pSubExpressions) {
			return this.newExprGroup_Functional(pCol, pRow, this, pSubExpressions);
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			//return super.run(new Context.ContextFragment(pContext, pExpr, null, pExpr), pExpr, pParams);
			return super.run(pContext, pExpr, pParams);
		}
	}
	
	static public class Inst_Stack extends Inst_AbstractStack {
		static public final String Name = "stack";

		Inst_Stack(Engine pEngine, String          pISpecStr) { super(pEngine, pISpecStr);        }
		Inst_Stack(Engine pEngine, InstructionSpec pISpec)    { super(pEngine, pISpec);           }
		Inst_Stack(Engine pEngine)                            { super(pEngine, Name + "($){}:~"); }
		
		/**@inherDoc()*/ @Override
		protected String getStackName(Object[] pParams) {
			return (String)((pParams == null)?null:pParams[0]);
		}
		/**@inherDoc()*/ @Override
		protected Object processResult(Context pContext, Expression pExpr, Object[] pParams, Object pResult) {
			// Trap break.
			if(pResult instanceof SpecialResult.ResultExit) {
				String SName = ((SpecialResult.ResultExit)pResult).Name;
				String EName = this.getStackName(pParams);
				if((SName == null) || (SName == EName) || SName.equals(EName))
					return new SpecialResult.ResultEnd(((SpecialResult.ResultExit)pResult).Result);
			}
			return pResult;
		}
	}
	
	// Type ----------------------------------------------------------------------------------------

	// Get the primitive type by alias or class name
	static public final class Inst_getPrimitiveTypeByName extends Inst_AbstractSimple {
		static public final String Name = "getPrimitiveTypeByName";
		
		Inst_getPrimitiveTypeByName(Engine pEngine) {
			super(pEngine, "=" + Name + "(+$):!");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			// Returns the data
			return TKJava.Instance.getTypeByClassName(pContext.getEngine(), (String)pParams[0], (String)pParams[0]);
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O instanceof String) {
				String PName = ((String)O);
				Type T = TKJava.Instance.getTypeByClassName(pCProduct.getEngine() , PName, PName);
				if(T != null) return new TypeTypeRef(T.getTypeRef());
			}
			return new TypeTypeRef(TKJava.TType.getTypeRef());
		}
	}
	
	// Returns the type of the given object, TAny will be return if the object is nul
	static public final class Inst_getTypeOf extends Inst_AbstractSimple {
		static public final String Name = "getTypeOf";
		
		Inst_getTypeOf(Engine pEngine) {
			super(pEngine, "=" + Name + "(~):!");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			if(pParams[0] == null) return TKJava.TAny;
			Type T = this.Engine.getTypeManager().getTypeOf(pContext, pParams[0]);
			if(T == null)
				return new SpecialResult.ResultError(
							new CurryError("Type Not Found Error: The type of an object "+
									this.Engine.getDisplayObject(pContext, pParams[0]) +
									" is unknown.",
									pContext));
			return T;
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O ==                         null) return TKJava.TVoid.getTypeRef();
			if(O instanceof Expression.Expr_Expr) return TKJava.TExpression.getTypeRef();
			if(!(O instanceof Expression)) {
				Type T = pCProduct.getEngine().getTypeManager().getTypeOf(O);
				if(T != null) return new TypeTypeRef(T.getTypeRef());
			}
			TypeRef TR = pCProduct.getReturnTypeRefOf(O);
			if(TR != null) return new TypeTypeRef(TR);
			
			return TLType.TypeRefOfType;
		}
	}
	
	// Returns the type of the given object, TAny will be return if the object is null
	static public final class Inst_getTypeOfClass extends Inst_AbstractSimple {
		static public final String Name = "getTypeOfClass";
		
		Inst_getTypeOfClass(Engine pEngine) {
			super(pEngine, "=" + Name + "(@):!");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			if(pParams[0] == null) return TKJava.TVoid;
			Type T = this.Engine.getTypeManager().getTypeOfTheInstanceOf(pContext, UClass.getCLASS((Class<?>)pParams[0]));
			if(T == null) {
				T = this.Engine.getTypeManager().getTypeOfTheInstanceOf(pContext, UClass.getTYPE((Class<?>)pParams[0]));
				if(T == null) {
					return new SpecialResult.ResultError(
						new CurryError(
							"Type Not Found Error: The type of a class " +
							"'"+ UObject.toDetail(pParams[0]) +"' is unknown.",
							pContext
						)
					);
				}
			}
			return T;
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O == null) return TKJava.TVoid.getTypeRef();
			if(!(O instanceof Class<?>)) {
				Type T = TKJava.Instance.getTypeByClass(pCProduct.getEngine(), null, (Class<?>)O);
				if(T != null) return new TypeTypeRef(T.getTypeRef());
			}
			return new TypeTypeRef(TKJava.TType.getTypeRef());
		}
	}
	
	//	 Check P[0].canBeAssignedBy(P[1]);
	static public final class Inst_InstanceOf extends Inst_AbstractSimple {
		Type Type = null;
		Inst_InstanceOf(Engine pEngine, Type pType) {
			super(pEngine, "=instanceOf"+
					(((pType == null)||(!(pType instanceof TKJava.TJava)))
						?"(+!,":"_"+ (((TKJava.TJava)pType).getAlias()+"("))+"~):?");
			this.Type = (pType instanceof TKJava.TJava)?pType:null;
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			if(this.Type == null) {
				if(pParams[1] == null) return false;
				return ((Type)pParams[0]).canBeAssignedBy(pContext, pParams[1]);
			} else {
				if(pParams[0] == null) return false;
				return this.Type.canBeAssignedBy(pContext, pParams[0]);
			}
		}
	}
	
	static public final class Inst_IsKindOf extends Inst_AbstractSimple {
		Type Type = null;
		Inst_IsKindOf(Engine pEngine, Type pType) {
			super(pEngine, "=isKindOf"+
					(((pType == null)||(!(pType instanceof TKJava.TJava)))
							?"(+!,"
							:"_"+ (((TKJava.TJava)pType).getAlias()+"("))+"!):?");
			this.Type = (pType instanceof TKJava.TJava)?pType:null;
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			if(this.Type == null) {
				if(pParams[1] == null) return false;
				return ((Type)pParams[0]).canBeAssignedByInstanceOf(((Type)pParams[1]));
			} else {
				if(pParams[0] == null) return false;
				return this.Type.canBeAssignedByInstanceOf((Type)pParams[0]);
			}
		}
	}
	
	static public final class Inst_GetTypeInfo extends Inst_AbstractSimple {
		static public final String Name = "getTypeInfo";
		
		// Name
		static public final String isAbstract   =   "isAbstract";
		static public final String DefaultValue = "DefaultValue";
		
		static public final String Description = "Description";
		static public final String ExtraInfo   =   "ExtraInfo";
		
		static public final String KindName      =      "KindName";
		static public final String EngineName    =    "EngineName";
		static public final String TypeRef       =       "TypeRef";
		static public final String DataClass     =     "DataClass";
		static public final String TypeClass     =     "TypeClass";
		static public final String DataClassName = "DataClassName";
		static public final String MoreData      =      "MoreData";
		static public final String TypeSpec      =      "TypeSpec";
		
		static public final String TypeAlias   = "TypeAlias";
		static public final String isPrimitive = "isPrimitive";
		static public final String isLocked    = "isLocked";
		
		// Hash
		static final int Hash_isAbstract   =   isAbstract.hashCode();
		static final int Hash_DefaultValue = DefaultValue.hashCode();
		
		static final int Hash_Description = Description.hashCode();
		static final int Hash_ExtraInfo   =   ExtraInfo.hashCode();
		
		static final int Hash_KindName      =      KindName.hashCode();
		static final int Hash_EngineName    =    EngineName.hashCode();
		static final int Hash_TypeRef       =       TypeRef.hashCode();
		static final int Hash_DataClass     =     DataClass.hashCode();
		static final int Hash_TypeClass     =     TypeClass.hashCode();
		static final int Hash_DataClassName = DataClassName.hashCode();
		static final int Hash_MoreData      =      MoreData.hashCode();
		static final int Hash_TypeSpec      =      TypeSpec.hashCode();
		
		static final int Hash_TypeAlias   =   TypeAlias.hashCode();
		static final int Hash_isPrimitive = isPrimitive.hashCode();
		static final int Hash_isLocked    =    isLocked.hashCode();
		
		Inst_GetTypeInfo(Engine pEngine) {
			super(pEngine, "=" + Name + "(+!,+$):~");
		}
		/**@inherDoc()*/ @Override protected Object run(Context pContext, Object[] pParams) {
			Type T        = (Type)pParams[0];
			int  InfoHash = ((String)pParams[1]).hashCode();
			if(InfoHash == Hash_isAbstract)   return T.isAbstract(pContext);
			if(InfoHash == Hash_DefaultValue) return T.getDefaultValue(pContext);
			
			if(InfoHash == Hash_Description) return T.getDescription(pContext.getEngine());
			if(InfoHash == Hash_ExtraInfo)   return T.getExtraInfo();
			
			if(InfoHash == Hash_KindName)   return T.getTypeKindName();
			if(InfoHash == Hash_EngineName) return (T instanceof TKJava.TJava)?"<<null>>":T.getEngine().getName();
			if(InfoHash == Hash_TypeRef)    return T.getTypeRef();
			if(InfoHash == Hash_DataClass)  return T.getDataClass();
			if(InfoHash == Hash_TypeClass)  return T.getClass();
			if(InfoHash == Hash_MoreData)   return UObject.toDetail(T.getMoreData());
			if(InfoHash == Hash_TypeSpec)   return T.getTypeSpec();
			
			if(InfoHash == Hash_TypeAlias) {
				if(!(T instanceof TKJava.TJava) || !((TKJava.TJava)T).isPrimitive()) return null;
				return ((TKJava.TJava)T).getAlias();
			}
			
			if(InfoHash == Hash_DataClassName) {
				Class<?> Cls = T.getDataClass();
				return (Cls == null)?null:Cls.getCanonicalName();
			}
			
			if(InfoHash == Hash_isPrimitive) {
				if(T instanceof TKJava.TJava) return ((TKJava.TJava)T).isPrimitive(); else return false;
			}
			
			if(InfoHash == Hash_isLocked) {
				if(T instanceof TKJava.TJava) return ((TKJava.TJava)T).isLocked(); else return false;
			}
			
			return null;
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(1);
			if(O instanceof String) {
				int InfoHash = ((String)O).hashCode();
				if(InfoHash == Hash_isAbstract) return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_DefaultValue) {
					O = pExpr.getParam(0);
					if(O instanceof Type) return ((Type)O).getTypeRef();
					if(O instanceof Expression) {
						Instruction I = pCProduct.getEngine().getInstruction(null, ((Expression)O).getInstructionNameHash());
						if(Inst_Type.Name.equals(I.getName()))
							return new TypeTypeRef(I.getReturnTypeRef((Expression)O, pCProduct));
					}
					return TKJava.TAny.getTypeRef();
				}
				if(InfoHash == Hash_Description)   return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_ExtraInfo)     return TKJava.TMoreData.getTypeRef();
				if(InfoHash == Hash_KindName)      return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_EngineName)    return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_TypeRef)       return TKJava.TTypeRef.getTypeRef();
				if(InfoHash == Hash_DataClass)     return TKJava.TClass.getTypeRef();
				if(InfoHash == Hash_MoreData)      return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_TypeSpec)      return TKJava.TTypeSpec.getTypeRef();
				if(InfoHash == Hash_DataClassName) return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_TypeAlias)     return TKJava.TString.getTypeRef();
				if(InfoHash == Hash_isPrimitive)   return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_isLocked)      return TKJava.TBoolean.getTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	
	static public final class Inst_GetTypeStatus extends Inst_AbstractSimple {
		static public final String Name = "getTypeStatus";
		
		// Name
		static public final String Status        =        "Status";
		static public final String isUnloaded    =    "isUnloaded";
		static public final String isLoaded      =      "isLoaded";
		static public final String isResolved    =    "isResolved";
		static public final String isInitialized = "isInitialized";

		// Hash
		static final int Hash_Status        =        Status.hashCode();
		static final int Hash_isUnloaded    =    isUnloaded.hashCode();
		static final int Hash_isLoaded      =      isLoaded.hashCode();
		static final int Hash_isResolved    =    isResolved.hashCode();
		static final int Hash_isInitialized = isInitialized.hashCode();
		
		Inst_GetTypeStatus(Engine pEngine) {
			super(pEngine, Name + "(+!,+$):~");
		}
		/**@inherDoc()*/ @Override protected Object run(Context pContext, Object[] pParams) {
			Type T        = (Type)pParams[0];
			int  InfoHash = ((String)pParams[1]).hashCode();
			
			if(InfoHash == Hash_Status)        return T.getStatus();
			if(InfoHash == Hash_isUnloaded)    return T.isUnloaded();
			if(InfoHash == Hash_isLoaded)      return T.isLoaded();
			if(InfoHash == Hash_isResolved)    return T.isResolved();
			if(InfoHash == Hash_isInitialized) return T.isInitialized();	
			return null;
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(1);
			if(O instanceof String) {
				int InfoHash = ((String)O).hashCode();
				if(InfoHash == Hash_Status)        return TKJava.Instance.getTypeByClass(pCProduct.getEngine(), null, TypeSpec.Status.class).getTypeRef();
				if(InfoHash == Hash_isUnloaded)    return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_isLoaded)      return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_isResolved)    return TKJava.TBoolean.getTypeRef();
				if(InfoHash == Hash_isInitialized) return TKJava.TBoolean.getTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	
	// Instance ------------------------------------------------------------------------------------
	
	// Create new instance of a type
	static public class Inst_NewInstance extends Inst_AbstractComplex {
		static public final String Name = "newInstance";
		
		Inst_NewInstance(Engine pEngine) {
			super(pEngine, Name + "(+!,~...):~");
		}
		protected Inst_NewInstance(Engine pEngine, String pISpecStr) {
			super(pEngine, pISpecStr);
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Type T = (Type)pParams[0];
			return T.newInstance(pContext, pExpr, null, UArray.getObjectArray(pParams[1]));
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			TypeRef TR = pCProduct.getReturnTypeRefOf(O);
			if(TR instanceof TypeTypeRef) return ((TypeTypeRef)TR).getTheRef();
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	
	// Create new instance of a type
	static public class Inst_NewInstanceByTypeRefs extends Inst_NewInstance {
		@SuppressWarnings("hiding")
		static public final String Name = "newInstanceByTypeRefs";
		
		Inst_NewInstanceByTypeRefs(Engine pEngine) {
			super(pEngine, Name + "(+!,+"+TypeRef.class.getCanonicalName()+"[],~...):~");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Type T = (Type)pParams[0];
			TypeRef[] PTRefs = (TypeRef[])UArray.getObjectArray(pParams[1]);
			Object[]  Params =            UArray.getObjectArray(pParams[2]);
			
			return T.newInstance(pContext, pExpr, PTRefs, Params);
		}
	}
	
	// Create new instance of a type
	static public class Inst_NewInstanceByInterface extends Inst_NewInstance {
		@SuppressWarnings("hiding")
		static public final String Name = "newInstanceByInterface";
		
		Inst_NewInstanceByInterface(Engine pEngine) {
			super(pEngine, Name + "(+!,+"+ExecInterface.class.getCanonicalName()+",~...):~");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Type T = (Type)pParams[0];
			ExecInterface PEI    = (ExecInterface)pParams[1];
			Object[]      Params = UArray.getObjectArray(pParams[2]);
			
			return T.newInstance(pContext, pExpr, PEI, Params);
		}
	}
	
	// Objectable ----------------------------------------------------------------------------------

	static public final class Inst_HashCode extends Inst_AbstractSimple {
		static public final String Name = "hashCode";
		
		Inst_HashCode(Engine pEngine) {
			super(pEngine, Name + "(~):i");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return (pParams[0] == null)?0:pParams[0].hashCode();
		}
	}
	static public final class Inst_Hash extends Inst_AbstractSimple {
		static public final String Name = "hash";
		
		Inst_Hash(Engine pEngine) {
			super(pEngine, Name + "(~):i");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return this.Engine.hash(pContext, pParams[0]);
		}
	}

	static public final class Inst_ToString extends Inst_AbstractSimple {
		static public final String Name = "toString";
		
		Inst_ToString(Engine pEngine) {
			super(pEngine, Name + "(~):$");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object O;
			if((O = pParams[0]) == null) return null;

			Object Obj = O;
			if(Obj instanceof DObjectStandalone) {
				return (String)((DObject)((DObjectStandalone)Obj).getAsDObject()).invoke(
					pContext,
					null,
					false,
					null,
					"toString",
					UObject.EmptyObjectArray
				);
			}
			
			return this.Engine.toString(pContext, O);
		}
	}

	static public final class Inst_ToDetail extends Inst_AbstractSimple {
		static public final String Name = "toDetail";
		
		Inst_ToDetail(Engine pEngine) {
			super(pEngine, Name + "(~):$");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object O;
			if((O = pParams[0]) == null) return null;

			Object Obj = O;
			if(Obj instanceof DObjectStandalone) {
				return (String)((DObject)((DObjectStandalone)Obj).getAsDObject()).invoke(
					pContext,
					null,
					false,
					null,
					"toDetail",
					UObject.EmptyObjectArray
				);
			}
			
			return this.Engine.toDetail(pContext, pParams[0]);
		}
	}
	
	static public final class Inst_Compare extends Inst_AbstractComplex {
		static public final String Name = "compare";
		Type ComparableType          = null;
		Type ComparableValueTypeType = null;
		
		Inst_Compare(Engine pEngine) {
			super(pEngine, Name + "(~,~):i");
		}
		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O1 = pParams[0];
			Object O2 = pParams[1];
			
			// Extract comparable if available
			MType MT = this.Engine.getTypeManager();

			if(this.ComparableType == null) {
				this.ComparableType          = MT.getTypeFromRefNoCheck(pContext, MT.getPrefineTypeRef("Comparable"));
				this.ComparableValueTypeType = MT.getTypeFromRefNoCheck(pContext, MT.getPrefineTypeRef("ComparableValueType"));
			}
			if((this.ComparableType != null) && (this.ComparableValueTypeType != null)) {
				boolean IsO1Comparable = (O1 instanceof DObjectStandalone) && this.ComparableType.canBeAssignedBy(pContext, O1);
				boolean IsO2Comparable = (O2 instanceof DObjectStandalone) && this.ComparableType.canBeAssignedBy(pContext, O2);
				if(IsO1Comparable || IsO2Comparable) {

					Type      Type1  = MT.getTypeFromRefNoCheck(pContext, MT.getTypeOf(pContext, O1).getTypeRef());
					TypeRef[] PRefs1 = TypeSpec.ExtractParameterFrom(pContext, this.Engine, Type1.getTypeRef(), this.ComparableType.getTypeRef());

					Type      Type2  = MT.getTypeFromRefNoCheck(pContext, MT.getTypeOf(pContext, O2).getTypeRef());
					TypeRef[] PRefs2 = TypeSpec.ExtractParameterFrom(pContext, this.Engine, Type2.getTypeRef(), this.ComparableType.getTypeRef());

					TypeRef ValueType = null;
					boolean IsSwapped = false;
					
					if(PRefs1 != null) {
						// See if O2 can be assigned to ComparableValue of Type1
						TypeRef ComparableValue1 = this.ComparableValueTypeType.getTypeSpec().getParameteredTypeRef(this.Engine, PRefs1);
						if(MType.CanTypeRefByAssignableBy(pContext, this.Engine, ComparableValue1, O2))
							ValueType = PRefs1[0];
					}
					if(PRefs2 != null) {
						// See if O1 can be assigned to ComparableValue of Type2
						TypeRef ComparableValue2 = this.ComparableValueTypeType.getTypeSpec().getParameteredTypeRef(this.Engine, PRefs2);
						if(MType.CanTypeRefByAssignableBy(pContext, this.Engine, ComparableValue2, O1)) {
							TypeRef ValueType2 = PRefs2[0];

							// see if ValueType2 is a larger type that ValueType
							if((ValueType == null) ||
							   MType.CanTypeRefByAssignableByInstanceOf(pContext, this.Engine, ValueType2, ValueType)) {
								
								ValueType = ValueType2;
									
								// Swap the object
								Object O = O1;
								O1 = O2;
								O2 = O;
								IsSwapped = true;
							}
						}
					}
					
					// Found the ValueType and O2 is an instance of it
					if(ValueType != null) {						
						ValueType = this.ComparableValueTypeType.getTypeSpec().getParameteredTypeRef(this.Engine, new TypeRef[] { ValueType });
						if(O1 instanceof DObjectStandalone) O1 = ((DObjectStandalone)O1).getAsDObject();
						
						DObject  DO = (DObject)O1;
						Object[] Ps = new Object[] { O2 };
							
						ExecSignature ES = DO.searchOperation(this.getEngine(), "compareTo", new TypeRef[] { ValueType });
						if(ES == null) {
							return new SpecialResult.ResultError(
									new CurryError(
										"Missing operation compareTo(...):int for" + DO + " <Instructions_Core:800>"
									)
								);
						}
						
						Object Result = DO.invokeDirect(pContext, pExpr, false, null, ES, Ps);
						if(Result instanceof SpecialResult) return Result;
						if(!(Result instanceof Integer)) {
							return new SpecialResult.ResultError(
								new CurryError(
									"Invalid return type for compareTo(...):int : " + Result +
									" <Instructions_Core:811>"
								)
							);
						}
						int    I      = ((Integer)Result).intValue();
						return IsSwapped ? -I : I;
					}
				}
			}
			
			return this.Engine.compares(pContext, O1, O2);
		}
	}
	
	static public final class Inst_Check extends Inst_AbstractCutShort {
		
		static public enum CheckMethod {
			MT, ME, LT, LE, EQ, NE, IS, EQs, NEQs;
			public String getSign() {
				switch(this) {
					case MT: return ">";
					case ME: return ">=";
					case LT: return "<";
					case LE: return "<=";
					case EQ: return "=#=";
					case NE: return "<>";
					
					case IS:   return "===";
					case EQs:  return "==";
					case NEQs: return "!=";
				}
				return "==";
			}
			public String getSignName() {
				switch(this) {
					case MT: return "moreThan";
					case ME: return "moreThanEqual";
					case LT: return "lessThan";
					case LE: return "lessThanEqual";
					case EQ: return "equal";
					case NE: return "inequal";
					
					case IS:   return "is";
					case EQs:  return "equals";
					case NEQs: return "inequals";
				}
				return "equal";
			}
		}
				
		CheckMethod Method = null;
		Inst_Check(Engine pEngine, CheckMethod pMethod) {
			// Non-Function for now as hash value may be changing (in case of immutable) 
			super(pEngine, pMethod.getSignName() + "(~...):?");
			this.Method = pMethod;
		}
		
		// Sign -------------------------------------------------------------------
		public String getSign() {
			return this.Method.getSign();
		}
		public String getSignName() {
			return this.Method.getSignName();
		}
		
		// Configure the process --------------------------------------------------------
		
		/**@inherDoc()*/ @Override
		protected boolean isSingle() {
			return false;
		}
		/**@inherDoc()*/ @Override
		protected Object getEarlyReturn(Context pContext, Object[] pParams) {
			return true;
		}
		/**@inherDoc()*/ @Override
		protected boolean processFirstValue(Context pContext, Object O) {
			return false;
		}
		/**@inherDoc()*/ @Override
		protected boolean processNonSingleValue(Context pContext, Object O1, Object O2) {
			
			if(this.Method == CheckMethod.IS)   return !this.Engine.is(    pContext, O1, O2);
			if(this.Method == CheckMethod.EQs)  return !this.Engine.equals(pContext, O1, O2);
			if(this.Method == CheckMethod.NEQs) return  this.Engine.equals(pContext, O1, O2);
			
			// Do the compare
			int CompareResult = ((O1 instanceof Number) && (O2 instanceof Number))
			                       ?UNumber.compare((Number)O1, (Number)O2)
			                       :pContext.getEngine().compares(pContext, O1, O2);
			
			switch(this.Method) {
				// Done if the opposite is found
				case MT: return (CompareResult <= 0);
				case ME: return (CompareResult <  0);
				case LT: return (CompareResult >= 0);
				case LE: return (CompareResult >  0);
				case EQ: return (CompareResult != 0);
				case NE: return (CompareResult == 0);
			}
			return true;
		}
		/**@inherDoc()*/ @Override
		protected Object getBreakReturn(Context pContext, Object[] pParams) {
			return false;
		}
		/**@inherDoc()*/ @Override
		protected Object getDoneReturn(Context pContext, Object[] pParams)  {
			return  true;
		}
	}
}

