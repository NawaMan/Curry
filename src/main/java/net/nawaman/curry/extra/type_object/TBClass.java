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

import java.util.Vector;

import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Location;
import net.nawaman.curry.StackOwnerBuilderEncloseObject;
import net.nawaman.curry.TypeBuilder;

/**
 * TypeBuilder for Object
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class TBClass extends TypeBuilder {
	
	protected TBClass(TSClass pTSpec, Accessibility pAccess, Location pLocation,
			StackOwnerBuilderEncloseObject pEncloseObject) {
		super(pTSpec, pAccess, pLocation, pEncloseObject);
	}
	
	/** Add a static delegate */
	public boolean addStaticDelegatee(String pAName) {
		if(!this.isActive()) return false;
		
		// NOTE: Do not check name here since the delegated can be in super type or other delegate
		// The name must exist
		//if(!this.isAttrExist(pAName)) return false;
		
		@SuppressWarnings("unchecked")
		Vector<String> SDs = (Vector<String>)this.getTSpecDataAt(TSClass.Index_StaticDelegates);
		if(SDs == null) {
			SDs = new Vector<String>();
			this.setTSpecDataAt(TSClass.Index_StaticDelegates, SDs);
		}
		// Add into the list if not yet already there
		if(!SDs.contains(pAName)) SDs.add(pAName);
		return true;
	}
	
	/** Add a static delegated */
	public boolean addDynamicDelegatee(String pAName) {
		if(!this.isActive()) return false;
		
		// NOTE: Do not check name here since the delegated can be in super type or other delegate
		// The name must exist
		//if(!this.isAttrExist(pAName)) return false;
		
		@SuppressWarnings("unchecked")
		Vector<String> SDs = (Vector<String>)this.getTSpecDataAt(TSClass.Index_DynamicDelegates);
		if(SDs == null) {
			SDs = new Vector<String>();
			this.setTSpecDataAt(TSClass.Index_DynamicDelegates, SDs);
		}
		// Add into the list if not yet already there
		if(!SDs.contains(pAName)) SDs.add(pAName);
		return true;
	}

}
