package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.curry.Executable.SubRoutine;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;

public class Instructions_DefaultPackage {

	// Instructions ----------------------------------------------------------------------------

	static public class Inst_GetDefaultPackage extends Inst_AbstractSimple {
		static public final String Name = "getDefaultPackage";
		
		Inst_GetDefaultPackage(Engine pEngine) {
			super(pEngine, "=" + Name + "():"+ Package.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getEngine().getDefaultPackage();
		}
	}
	static public class Inst_GetDefaultPackageBuilder extends Inst_AbstractSimple {
		static public final String Name = "getDefaultPackageBuilder";
		
		Inst_GetDefaultPackageBuilder(Engine pEngine) {
			super(pEngine, "=" + Name + "():"+DefaultPackageBuilder.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getEngine().getDefaultPackageBuilder();
		}
	}
	static abstract public class Inst_DefaultPackageAccess extends Inst_AbstractSimple {
		Inst_DefaultPackageAccess(Engine pEngine, String pInstSpecStr) {
			super(pEngine, pInstSpecStr);
		}
		
		abstract Object doAccess(Context pContext, DefaultPackageBuilder DPB, Object[] pParams);
		
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			DefaultPackageBuilder DPB = this.Engine.DefaultPackageBuilder;
			if(DPB == null) {
				if(this.Engine.getExtension(EngineExtensions.EE_DefaultPackage.Name) == null)
					return new SpecialResult.ResultError(new CurryError("Default Package Extension is not enabled.", pContext));
				// DefaultPackage is enable but the default package builder is null
				return new SpecialResult.ResultError(new CurryError("Internal Error: The default package builder is not found.", pContext));
			}
			return this.doAccess(pContext, DPB, pParams);
		}
	}
	static public class Inst_AddDefaultPackageVariable extends Inst_DefaultPackageAccess {
		static public final String Name = "addDefaultPackageVariable";
		
		Inst_AddDefaultPackageVariable(Engine pEngine) {
			super(pEngine,
				Name + "("+
					Accessibility.class.getCanonicalName()+","+
					Accessibility.class.getCanonicalName()+","+
					Accessibility.class.getCanonicalName()+","+
					"+$,+?,+!,P,+?,M,"+
					Location.class.getCanonicalName()+",M"+
				"):?"
			);
		}
		/**{@inherDoc}*/ @Override
		Object doAccess(Context pContext, DefaultPackageBuilder DPB, Object[] pParams) {
			return DPB.addPVar(
					(Accessibility)pParams[ 0],
					(Accessibility)pParams[ 1],
					(Accessibility)pParams[ 2],
					(String)       pParams[ 3],
					(Boolean)      pParams[ 4],
					(Type)         pParams[ 5],
					(Serializable) pParams[ 6],
					(Boolean)      pParams[ 7],
					(MoreData)     pParams[ 8],
					(Location)     pParams[ 9],
					(MoreData)     pParams[10]);
		}
	}
	static public class Inst_AddDefaultPackageDataHolder extends Inst_DefaultPackageAccess {
		static public final String Name = "addDefaultPackageDataHolder";
		
		Inst_AddDefaultPackageDataHolder(Engine pEngine) {
			super(pEngine,
				Name + "("+
					Accessibility.class.getCanonicalName()+","+
					Accessibility.class.getCanonicalName()+","+
					Accessibility.class.getCanonicalName()+","+
					"+$,+?,+"+
					DataHolderInfo.class.getCanonicalName()+","+
					Location.class.getCanonicalName()+",M"+
				"):?"
			);
		}
		/**{@inherDoc}*/ @Override
		Object doAccess(Context pContext, DefaultPackageBuilder DPB, Object[] pParams) {
			return DPB.addPDataHolder(
					(Accessibility)pParams[0],
					(Accessibility)pParams[1],
					(Accessibility)pParams[2],
					(String)        pParams[3],
					(Boolean)       pParams[4],
					(DataHolderInfo)pParams[5],
					(Location)      pParams[6],
					(MoreData)      pParams[7]);
		}
	}
	static public class Inst_BindDefaultPackageDataHolder extends Inst_DefaultPackageAccess {
		static public final String Name = "bindDefaultPackageDataHolder";
		
		Inst_BindDefaultPackageDataHolder(Engine pEngine) {
			super(pEngine,
				Name + "("+
					Accessibility.class.getCanonicalName()+","+
					Accessibility.class.getCanonicalName()+","+
					Accessibility.class.getCanonicalName()+","+
					"+$,+?,+"+
					DataHolder.class.getCanonicalName()+ ","+
					Location.class.getCanonicalName()+",M"+
				"):?"
			);
		}
		/**{@inherDoc}*/ @Override
		Object doAccess(Context pContext, DefaultPackageBuilder DPB, Object[] pParams) {
			return DPB.bindPDataHolder(
					(Accessibility)pParams[0],
					(Accessibility)pParams[1],
					(Accessibility)pParams[2],
					(String)       pParams[3],
					(Boolean)      pParams[4],
					(DataHolder)   pParams[5],
					(Location)     pParams[6],
					(MoreData)     pParams[7]);
		}
	}
	static public class Inst_AddDefaultPackageFunction extends Inst_DefaultPackageAccess {
		static public final String Name = "addDefaultPackageFunction";
		
		Inst_AddDefaultPackageFunction(Engine pEngine) {
			super(pEngine, Name + "("+Accessibility.class.getCanonicalName()+",+"
					+ SubRoutine.class.getCanonicalName()+",M):?");
		}
		/**{@inherDoc}*/ @Override
		Object doAccess(Context pContext, DefaultPackageBuilder DPB, Object[] pParams) {
			return DPB.addFunction((Accessibility)pParams[0], (SubRoutine)pParams[1], (MoreData)pParams[2]);
		}
	}
	static public class Inst_AddDefaultPackageType extends Inst_DefaultPackageAccess {
		static public final String Name = "addDefaultPackageType";
		
		Inst_AddDefaultPackageType(Engine pEngine) {
			super(pEngine, Name + "("+Accessibility.class.getCanonicalName()+",+$,+"
					+TypeSpec.class.getCanonicalName()+"):?" );
		}
		/**{@inherDoc}*/ @Override
		Object doAccess(Context pContext, DefaultPackageBuilder DPB, Object[] pParams) {
			// TODO - May think of add location
			return DPB.addType((Accessibility)pParams[0], (String)pParams[1], (TypeSpec)pParams[2], null);
		}
	}
}
