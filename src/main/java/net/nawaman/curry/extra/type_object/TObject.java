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

package net.nawaman.curry.extra.type_object;

import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;

/**
 * Super type of all curry object
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class TObject extends Type {
	
	// Construction --------------------------------------------------------------------------------
	
	protected TObject(TKObject pTKind, TSObject pTSpec) {
		super(pTKind, pTSpec);
	}
	
	// Classification -----------------------------------------------------------------------------
	
	/** Returns the kind name of this type. */
	final public String  getKindName()   { return ((TSObject)this.getTypeSpec()).getKindName(); }
	/** Returns the spec of this type. */
	final public TSObject getTSObject()  { return ((TSObject)this.getTypeSpec());               }
	/** Checks if the type is final (not derivable) */
	final public boolean isFinal()       { return ((TSObject)this.getTypeSpec()).isFinal();     }
	
	/** Returns the type of the super type */
	final public Type getSuper() {
		TypeRef TRS = ((TSObject)this.getTypeSpec()).getSuperRef();
		if(TRS == null) return TKJava.TAny;
		this.getEngine().getTypeManager().ensureTypeInitialized(TRS);
		return this.getTypeFromRef(TRS);
	}
	
}
