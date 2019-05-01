package net.nawaman.curry.extra.type_enum;

import net.nawaman.curry.*;

public class EE_Enum extends EngineExtension {
	
	static public final String Name = "Enum";
	
	@Override protected String getExtName() { return Name; }
	
	// Required Extension -----------------------------------------------------
	/**{@inheritDoc}*/ @Override
	protected String[] getRequiredExtensionNames() {
		return new String[] {};
	}
	
	/**{@inheritDoc}*/ @Override
	protected String initializeThis() {
		this.regTypeKind(new TKEnum(this.getEngine()));
		return null;
	}
}
