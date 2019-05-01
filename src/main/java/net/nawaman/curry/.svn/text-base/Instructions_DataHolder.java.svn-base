package net.nawaman.curry;

import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.DataHolderFactory;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.util.UArray;

public class Instructions_DataHolder {

	static public class Inst_NewDH extends Inst_AbstractSimple {
		static public final String Name = "newDH";
		
		Inst_NewDH(Engine pEngine) {
			super(
				pEngine,
				String.format(
					"%s(+%s,~):%s",
					Name,
					DataHolderInfo.class.getCanonicalName(),
					DataHolder.class.getCanonicalName()
				)
			);
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Engine            $Engine = this.getEngine();
			DataHolderInfo    DHI     = (DataHolderInfo)pParams[0];
			DataHolderFactory DHF     = $Engine.getDataHolderManager().getDataHolderFactory(DHI.getDHFactoryName());
			
			Object  D = pParams[1];
			Type    T = $Engine.getTypeManager().getTypeFromRef(pContext, DHI.getTypeRef());
			
			if(DHI.isSet())
				 return DHF.newDataHolder(pContext, $Engine, T, D, true, DHI.isWritable(), null, DHI);
			else return DHF.newDataHolder(pContext, $Engine, T,    true, DHI.isWritable(), null, DHI);
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O instanceof DataHolderInfo) {
				DataHolderInfo DHI  = (DataHolderInfo)O;
				TKDataHolder   TKDH = (TKDataHolder)pCProduct.getEngine().getTypeManager().getTypeKind(TKDataHolder.KindName);
				return TKDH.getNoNameTypeRef(DHI.getTypeRef(), true, DHI.isWritable(), null);
			}
			return TKJava.TDataHolder.getTypeRef();
		}
	}

	static public class Inst_GetDHValue extends Inst_AbstractSimple {
		static public final String Name = "getDHValue";
		
		protected Inst_GetDHValue(Engine pEngine, String pInstStr) {
		    super(pEngine, pInstStr);
		}
		Inst_GetDHValue(Engine pEngine) {
			super(pEngine, Name + "(+D):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			DataHolder DH = (DataHolder)pParams[0];
			return this.getEngine().getDataHolderManager().getDHData(pContext, null, DH);
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			TypeRef TR = pCProduct.getReturnTypeRefOf(O);
			if(TR == null) return super.getReturnTypeRef(pExpr, pCProduct);
			
			try {
				Type T = pCProduct.getTypeAtCompileTime(TR, false);
				if(TKJava.TDataHolder.equals(T))             return TKJava.TAny.getTypeRef();
				if(!(T instanceof TKDataHolder.TDataHolder)) return null;
				return ((TKDataHolder.TDataHolder)T).getDataTypeRef();
			} catch(Exception E) {}
			
			return null;
		}
	}

	static public class Inst_SetDHValue extends Inst_AbstractSimple {
		static public final String Name = "setDHValue";
		
		Inst_SetDHValue(Engine pEngine) {
			super(pEngine, Name + "(+D,~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			DataHolder DH = (DataHolder)pParams[0];
			return this.getEngine().getDataHolderManager().setDHData(pContext, null, DH, pParams[1]);
		}
		/**{@inherDoc}*/ @Override
		public boolean ensureParamCorrect(Expression pExpr, CompileProduct CP) {
			Object O = pExpr.getParam(0);
			TypeRef TR = CP.getReturnTypeRefOf(O);
			if(TR == null) return true;

			TypeRef ValueTR = CP.getReturnTypeRefOf(pExpr.getParam(1));
			
			try {
				Type T = CP.getTypeAtCompileTime(TR, false);	
				if(!(T instanceof TKDataHolder.TDataHolder)) return false;
				
				Type VT = CP.getTypeAtCompileTime(ValueTR, false);
				return MType.CanTypeRefByAssignableByInstanceOf(
						null, CP.getEngine(),
						TR, (VT == null) ? ValueTR : VT.getTypeRef());
			} catch(Exception E) {
				return false;
			}
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(1));
		}
	}

	static public class Inst_IsDHReadable extends Inst_AbstractSimple {
		static public final String Name = "isDHReadable";
		
		Inst_IsDHReadable(Engine pEngine) {
			super(pEngine, Name + "(+D):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return this.getEngine().getDataHolderManager().isDHReadable(pContext, null, (DataHolder)pParams[0]);
		}
	}

	static public class Inst_IsDHWritable extends Inst_AbstractSimple {
		static public final String Name = "isDHWritable";
		
		Inst_IsDHWritable(Engine pEngine) {
			super(pEngine, Name + "(+D):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return this.getEngine().getDataHolderManager().isDHWritable(pContext, null, (DataHolder)pParams[0]);
		}
	}

	static public class Inst_GetDHType extends Inst_AbstractSimple {
		static public final String Name = "getDHType";
		
		Inst_GetDHType(Engine pEngine) {
			super(pEngine, Name + "(+D):!");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return this.getEngine().getDataHolderManager().getDHType(pContext, null, (DataHolder)pParams[0]);
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			TypeRef TR = pCProduct.getReturnTypeRefOf(O);
			if(TR == null) return super.getReturnTypeRef(pExpr, pCProduct);
			
			try {
				Type T = pCProduct.getTypeAtCompileTime(TR, false);				
				if(!(T instanceof TKDataHolder.TDataHolder)) return null;
				return new TypeTypeRef(((TKDataHolder.TDataHolder)T).getDataTypeRef());
			} catch(Exception E) {
				return null;
			}
		}
	}

	static public class Inst_GetDHMoreInfo extends Inst_AbstractSimple {
		static public final String Name = "getDHMoreInfo";
		
		Inst_GetDHMoreInfo(Engine pEngine) {
			super(pEngine, Name + "(+D,+$):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return this.getEngine().getDataHolderManager().getDHMoreInfo(pContext, (DataHolder)pParams[0], pParams[1].toString());
		}
	}

	static public class Inst_ConfigDH extends Inst_AbstractSimple {
		static public final String Name = "configDH";
		
		Inst_ConfigDH(Engine pEngine) {
			super(pEngine, Name + "(+D,+$,~...):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return this.getEngine().getDataHolderManager().configDH(
					pContext,
					(DataHolder)pParams[0],
					pParams[1].toString(),
					UArray.getObjectArray(pParams[2]));
		}
	}

	static public class Inst_IsDHNoTypeCheck extends Inst_AbstractSimple {
		static public final String Name = "isDHNoTypeCheck";
		
		Inst_IsDHNoTypeCheck(Engine pEngine) {
			super(pEngine, Name + "(+D):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return this.getEngine().getDataHolderManager().isDHNoTypeCheck(pContext, null, (DataHolder)pParams[0]);
		}
	}

	static public class Inst_AddDataHolderAsLocalVariable extends Inst_AbstractSimple {
		static public final String Name = "addDataHolderAsLocalVariable";
		
		Inst_AddDataHolderAsLocalVariable(Engine pEngine) {
			super(pEngine, Name + "(+$,+D):D");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			String     N  = (String)    pParams[0];
			DataHolder DH = (DataHolder)pParams[1];
			return pContext.addDataHolder(N, DH);
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			TypeRef TRef = pCProduct.getReturnTypeRefOf(pExpr.getParam(1));
			return (TKDataHolder.isDataHolder(pCProduct.getEngine(), TRef))?TRef:null;
		}
		
		/**@inherDoc()*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(!(O instanceof String)) return true;

			if(pCProduct.isLocalVariableExist((String)O)) {
				return ReportCompileProblem(
						"addDataHolderAsLocalVariable:136", String.format("The local variable `%s` is already exist.", (String)O),
						pExpr, pCProduct, false, false);
			}
			
			Engine  $Engine = pCProduct.getEngine();
			TypeRef TRef    = pCProduct.getReturnTypeRefOf(pExpr.getParam(1)) ;
			
			if(TKJava.TDataHolder.getTypeRef().equals(TRef))                TRef = TKJava.TAny.getTypeRef();
			else if(TKDataHolder.isDataHolder(pCProduct.getEngine(), TRef)) TRef = TKDataHolder.getDataHolderDataTypeRef($Engine, TRef);
			else {
				return ReportCompileProblem(
					"addDataHolderAsLocalVariable:240",
					String.format("Invalid Type for `%s`: DataHolder Type is required.", (String)O),
					pExpr, pCProduct, false, false);
			}
			
			pCProduct.newVariable((String)O, TRef);
			return true;
		}
	}
}
