package net.nawaman.curry.compiler;

import net.nawaman.curry.Context;
import net.nawaman.curry.EngineExtension;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Inst_AbstractSimple;
import net.nawaman.curry.Instruction;
import net.nawaman.curry.Engine;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;

public class EE_Language extends EngineExtension {
	
	static public final String Name = "Language";
	
	/** Constructs an engine extension. */
	public EE_Language() {}
	

	protected boolean isStackOwnerVariableShouldBeTreatedAsVariable = false;
	
	/**{@inheritDoc}*/ @Override
	protected String getExtName() {
		return Name;
	}
	/**{@inheritDoc}*/ @Override
	protected String[] getRequiredExtensionNames() {
		return null;
	}
	/**{@inheritDoc}*/ @Override
	protected String initializeThis() {	
		this.regInst(-540593); // Inst_GetDefaultLanguage
		return null;
	}
	/**{@inheritDoc}*/ @Override
	protected Instruction getNewInstruction(int hSearch) {
		Engine E = this.getEngine();
		switch(hSearch) {
			case  -540593: return new Inst_GetDefaultLanguage(E);
		}
		return null;
	}

	
	CurryLanguage Language = null;
	
	/** Returns the default engine of the Engine */
	final public CurryLanguage getDefaultLanguage() {
		if(this.Language == null) this.Language = CurryLanguage.Util.GetDefaultCurryLanguage();
		return this.Language;
	}

	/** Returns the default engine of the Engine */
	final public boolean setDefaultLanguage(CurryLanguage pDefaultLanguage) {
		if(this.Language != null) return false;
		this.Language = pDefaultLanguage;
		return true;
	}
	
	static public class Inst_GetDefaultLanguage extends Inst_AbstractSimple {
		@SuppressWarnings("hiding")
		static public final String Name = "getDefaultLanguage";
		
		Inst_GetDefaultLanguage(Engine pEngine) {
			super(pEngine, "=" + Name + "():" + CurryLanguage.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return ((EE_Language)this.getEngine().getExtension(EE_Language.Name)).getDefaultLanguage();
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			CurryLanguage CL = ((EE_Language)this.getEngine().getExtension(EE_Language.Name)).getDefaultLanguage();
			if(CL == null) return super.getReturnTypeRef();
			
			Type T = this.getEngine().getTypeManager().getTypeOfTheInstanceOf(CL.getClass());
			if(T == null) return super.getReturnTypeRef();
			
			return T.getTypeRef();
		}
	}
}
