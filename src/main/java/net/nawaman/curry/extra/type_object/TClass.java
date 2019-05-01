/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's Curry.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.curry.extra.type_object;

import net.nawaman.curry.Type;

/**
 * Type of class
 * 
 * @author Nawapunth Manusitthipol
 */
public class TClass extends TObject {
	
	// Construction --------------------------------------------------------------------------------
	
	protected TClass(TKClass pTKind, TSClass pTSpec) {
		super(pTKind, pTSpec);
	}
	
	// Dynamic Delegation --------------------------------------------------------------------------
	
	// This two will be intialized and used by TKClass
	// NOTE: The 0th item will be tried first so let use the the first one the the class and the later until all.
	//          Next are the first one from the super and so on.
 	String[] DDlgNames   = null;
	Type[]   DDlgAsTypes = null;
	
	/** Returns the numner of dynamic delegatee */
	public int getDynamicDelegateeCount() {
		return (this.DDlgNames == null)?0:this.DDlgNames.length;
	}
	
	/** Returns the name of the dynamic delegatee at the position I */
	public String getDynamicDelegateeName(int I) {
		if((I < 0) || (I >= this.getDynamicDelegateeCount())) return null;
		return this.DDlgNames[I];
	}
	
	/**
	 * Returns Type used as AsType to get the object of the dynamic delegatee at the position I.
	 * 
	 * In this case, the type will be the type in which the dynamic delegation is declared.
	 */
	public Type getDynamicDelegateeAsType(int I) {
		if((I < 0) || (I >= this.getDynamicDelegateeCount())) return null;
		return this.DDlgAsTypes[I];
	}

}
