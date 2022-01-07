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

package net.nawaman.curry;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nawaman.curry.util.DataHolder;
import net.nawaman.util.UObject;

/**
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class ScopePrivate {

	/** Constructs a scope */
	ScopePrivate() { this(null, null); }

	/** Constructs a scope with a delegated parent */
	ScopePrivate(ScopePrivate pParent) {
		this(null, pParent);
	}

	/** Constructs a scope with a delegated parent and scope name */
	ScopePrivate(String pSName, ScopePrivate pParent) {
		this.Parent = pParent;
		this.ScopeName = pSName;
	}

	ScopePrivate Parent;
	
	final String ScopeName;
	
	final String getName() {
		return this.ScopeName;
	}
	ScopePrivate getParent() {
		return this.Parent;
	}
	
	boolean isNewVarAllowed() {
		return true;
	}
		
	// DataHolders -------------------------------------------------------------
	
	Hashtable<String, DataHolder> DataHolders;
	
	DataHolder getDataHolder(String pDHName) {
		DataHolder DH = this.getLocalDataHolder(pDHName);
		return (DH != null)?DH:this.getParentDataHolder(pDHName);
	}
	DataHolder getLocalDataHolder(String pDHName) {
		if(this.DataHolders == null) return null;
		DataHolder DH = this.DataHolders.get(pDHName);
		if(DH != null) return DH;
		if(this.ScopeName != null)
			DH = this.DataHolders.get(this.ScopeName +"." + pDHName);
		return DH;
	}
	
	DataHolder getParentDataHolder(String pDHName) {
		// Get from the parent
		if(this.getParent() != null) {
			DataHolder DH = this.getParent().getDataHolder(pDHName);
			if(DH != null) return DH;
		}
		return null;
	}
	
	DataHolder onDelegateToParentForSet(String pVarName, DataHolder DH) { return DH; }
	
	Object setValue(Engine pEngine, String pVarName, Object pNewValue) {
		Context    C  = (this instanceof Context) ? (Context)this : null;
		DataHolder DH = this.getLocalDataHolder(pVarName);
		if(DH == null) {
			DH = this.getParentDataHolder(pVarName);
			if(DH == null) throw new CurryError("Local variable \'"+pVarName+"\' does not exist.", C);
			
			DH = this.onDelegateToParentForSet(pVarName, DH);
		}
		return pEngine.getDataHolderManager().setDHData(C, pVarName, DH, pNewValue);
	}
	
	Object getValue(Engine pEngine, String pDHName) {
		Context    C  = (this instanceof Context) ? (Context)this : null;
		DataHolder DH = this.getDataHolder(pDHName);
		
		if(DH == null)
			throw new CurryError("Local variable does not exist ("+pDHName+").", C);
		
		return pEngine.getDataHolderManager().getDHData(C, pDHName, DH);
	}
	
	Type getType(Engine pEngine, String pDHName) {
		Context    C  = (this instanceof Context) ? (Context)this : null;
		DataHolder DH = this.getDataHolder(pDHName);
		if(DH == null) throw new CurryError("Local variable does not exist ("+pDHName+").", C);
		return pEngine.getDataHolderManager().getDHType(C, pDHName, DH);
	}
	
	private Object newVariable(Engine pEngine, String pVName, Type pType, Object pDefaultValue, boolean IsConstant,
			boolean IgnoreExist) {
		
		Context C = (this instanceof Context) ? (Context)this : null;
		if((pEngine == null) && (C != null)) pEngine = C.getEngine();
		
		if(!this.isNewVarAllowed())
			throw new CurryError("A new variable cannot be created in this scope/context ("+pVName+").", C);
		
		DataHolder DH = this.getLocalDataHolder(pVName);
		if(DH != null) {
			if(IgnoreExist) return this.getValue(pEngine, pVName);
			DH = this.getLocalDataHolder(pVName);
			
			// TODO - This is a Hack - Some temporary variable cause problem when used in delay computation.
			Matcher M = Pattern.compile("^[0-9]+$").matcher(pVName);
			if(M.find()) this.removeVariable(pVName);
			else throw new CurryError("Variable already exist ("+pVName+").", (this instanceof Context) ? (Context)this : null);
		}
		
		if((pDefaultValue != null) && !pType.canBeAssignedBy(pDefaultValue)) {
			Object O = TKJava.tryToCastTo(pDefaultValue, pType);
			if(O == null)
				throw new CurryError("Incompatible default value '"+UObject.toDetail(pDefaultValue)+"' ("+pVName+").", C);
			pDefaultValue = O;
		}
		
		if(pEngine != null) DH = pEngine.getDataHolderManager().newDH(C, pEngine, Variable.FactoryName, pType, pDefaultValue, true, !IsConstant, null);
		else                DH = Variable.getFactory().newDataHolder(C, pEngine, pType, pDefaultValue, true, !IsConstant, null, null);
		if(this.DataHolders == null) this.DataHolders = new Hashtable<String, DataHolder>();
		//if(this.DataHolders == null) this.DataHolders = new SmallMap<String, DataHolder>(String.class, DataHolder.class);
		this.DataHolders.put(pVName, DH);
		if(this.ScopeName != null)
			DH = this.DataHolders.get(this.ScopeName +"." + pVName);
		return pDefaultValue;
	}
	
	Object newVariable(Engine pEngine, String pVName, Type pType, Object pDefaultValue) {
		return this.newVariable(pEngine, pVName, pType, pDefaultValue, false, false);
	}
	Object newConstant(Engine pEngine, String pVName, Type pType, Object pDefaultValue) {
		return this.newVariable(pEngine, pVName, pType, pDefaultValue, true, false);
	}
	
	Object newVariable(Engine pEngine, String pVName, Type pType, Object pDefaultValue, boolean IgnoreExist) {
		return this.newVariable(pEngine, pVName, pType, pDefaultValue, false, IgnoreExist);
	}
	Object newConstant(Engine pEngine, String pVName, Type pType, Object pDefaultValue, boolean IgnoreExist) {
		return this.newVariable(pEngine, pVName, pType, pDefaultValue, true, IgnoreExist);
	}
	
	Object addDataHolder(String pDHName, DataHolder pDH) {
		Context C = (this instanceof Context) ? (Context)this : null;
		
		if(!this.isNewVarAllowed())
			throw new CurryError("A new variable cannot be added in this scope/context ("+pDHName+").", C);
		
		DataHolder DH = this.getLocalDataHolder(pDHName);
		if(DH != null) throw new CurryError("Variable already exist ("+pDHName+").", C);

		if(this.DataHolders == null) this.DataHolders = new Hashtable<String, DataHolder>();
		this.DataHolders.put(pDHName, pDH);
		if(this.ScopeName != null)
			DH = this.DataHolders.get(this.ScopeName +"." + pDHName);
		return true;
	}
		
	boolean removeVariable(String pVarName) {
		Context C = (this instanceof Context) ? (Context)this : null;
		
		if(pVarName         == null) return false;
		if(this.DataHolders == null) return false;
		
		if((this.ScopeName != null) && pVarName.startsWith(this.ScopeName + "."))
			pVarName = pVarName.substring(this.ScopeName.length());

		String FullName = (this.ScopeName != null)?(this.ScopeName + "." + pVarName):null;
		
		if(!this.isLocalVariableExist(pVarName) && !this.isLocalVariableExist(FullName))
			throw new CurryError("Local variable does not exist ("+pVarName+ ((FullName != null)?(" or "+FullName):"")+").", C);
		
		if(this.DataHolders.containsKey(pVarName)) this.DataHolders.remove(pVarName);
		if((FullName != null) && this.DataHolders.containsKey(FullName)) this.DataHolders.remove(FullName);
		return true;
	}

	boolean isVariableExist(String pDHName) {
		return (this.getDataHolder(pDHName) != null)
				|| ((this.ScopeName != null) && (this.getDataHolder(this.ScopeName + "." +pDHName) != null));
	}
	boolean isLocalVariableExist(String pDHName) {
		return (this.getLocalDataHolder(pDHName) != null)
				|| ((this.ScopeName != null) && (this.getLocalDataHolder(this.ScopeName + "." +pDHName) != null));
	}
	
	boolean isVariableConstant(String pDHName) {
		DataHolder DH = this.getDataHolder(pDHName);
		if(DH == null) return false;
		return !DH.isWritable();
	}
	
}