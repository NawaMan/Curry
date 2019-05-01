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

import net.nawaman.curry.TypeRef;
import net.nawaman.curry.util.MoreData;
import net.nawaman.curry.ParameterizedTypeInfo;

/**
 * TypeSpec of Class
 * 
 * @author Nawapunth Manusitthipol
 */
public class TSClass extends TSObject {

	// Constants ----------------------------------------------------------------------------------

	@SuppressWarnings("hiding")
	final static public int IndexCount = 15;
	
	final static public int Index_Constructors     = 12;	// Vector<ConstructorInfo>
	final static public int Index_StaticDelegates  = 13;	// Vector<String>
	final static public int Index_DynamicDelegates = 14;	// Vector<String>
	

	// Constructor and verification ---------------------------------------------------------------
	
	protected TSClass(TypeRef pTRef, String pKind, boolean pIsAbstract, boolean pIsFinal, TypeRef pSuperRef,
			TypeRef[] pInterfaces, ParameterizedTypeInfo pTPInfo, MoreData pMoreData, MoreData pExtraInfo) {
		super(pTRef, pKind, pIsAbstract, pIsFinal, pSuperRef, pInterfaces, pTPInfo, pMoreData, pExtraInfo);
	}

	// StackOwner ---------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected int getDataIndexCount() {
		return TSClass.IndexCount;
	}
	/**{@inheritDoc}*/ @Override
	final protected int getConstructorInfoIndex() {
		return Index_Constructors;
	}

	// Delegations --------------------------------------------------------------------------------

	/** Returns the number of static delegations */
	@SuppressWarnings("unchecked")
	final public int getStaticDelegationCount() {
		Vector<String> SDelegates = (Vector<String>)this.getData(Index_StaticDelegates);
		if(SDelegates == null) return 0;
		return  SDelegates.size();
	}
	/** Returns the name of the attribute for the static delegation at the index */
	@SuppressWarnings("unchecked")
	final public String getStaticDelegationAt(int I) {
		if(I < 0) return null;
		Vector<String> SDelegates = (Vector<String>)this.getData(Index_StaticDelegates);
		if(SDelegates == null) return null;
		if(I >= SDelegates.size()) return null;
		return SDelegates.get(I);
	}

	/** Returns the number of dynamic delegations */
	@SuppressWarnings("unchecked")
	final public int getDynamicDelegationCount() {
		Vector<String> DDelegates = (Vector<String>)this.getData(Index_DynamicDelegates);
		if(DDelegates == null) return 0;
		return  DDelegates.size();
	}
	/** Returns the name of the dynamic for the static delegation at the index */
	@SuppressWarnings("unchecked")
	final public String getDynamicDelegationAt(int I) {
		if(I < 0) return null;
		Vector<String> DDelegates = (Vector<String>)this.getData(Index_DynamicDelegates);
		if(DDelegates == null) return null;
		if(I >= DDelegates.size()) return null;
		return DDelegates.get(I);
	}

}
