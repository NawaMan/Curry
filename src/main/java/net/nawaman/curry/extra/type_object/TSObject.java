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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

import net.nawaman.curry.Context;
import net.nawaman.curry.Engine;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.Util;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.util.MoreData;
import net.nawaman.curry.ParameterizedTypeInfo;

/**
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class TSObject extends TypeSpec {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	// Constants ----------------------------------------------------------------------------------

	final static public int IndexCount = 12;
	// DATA
	final static public int Index_Kind       = 0;	// String
	final static public int Index_IsAbstract = 1;	// Boolean
	final static public int Index_IsFinal    = 2;	// Boolean
	final static public int Index_Super      = 3;	// TypeRef		: NOTE: the type must be in Required type and same kind with this type
	final static public int Index_Interfaces = 4;	// TypeRef[]	: NOTE: all of these types must be in Required type
	
	final static public int Index_TypeAttrs   = 5;	// Vector<AttributeInfo>
	final static public int Index_TypeOpers   = 6;	// Vector<OperationInfo>
	final static public int Index_ObjectAttrs = 7;	// Vector<AttributeInfo>
	final static public int Index_ObjectOpers = 8;	// Vector<OperationInfo>

	final static public int Index_Parameterization = 9;	// MoreData	: NOTE: all types in here must be in the used types
	
	final static public int Index_MoreData  = 10;	// MoreData
	final static public int Index_ExtraInfo = 11;	// MoreData

	// Constructor and verification ---------------------------------------------------------------
	
	protected TSObject(TypeRef pTRef, String pKind, boolean pIsAbstract, boolean pIsFinal, TypeRef pSuperRef,
			TypeRef[] pInterfaceRefs, ParameterizedTypeInfo pTPInfo, MoreData pMoreData, MoreData pExtraInfo) {
		super(pTRef);
		
		// Datas -----------------------------------------------------------------------------------
		int            TDCount  = this.getDataIndexCount();
		Serializable[] TheDatas = (TDCount < 0)?null:new Serializable[TDCount];
		
		TheDatas[Index_Kind]       = pKind;
		TheDatas[Index_IsAbstract] = pIsAbstract;
		TheDatas[Index_IsFinal]    = pIsFinal;
		TheDatas[Index_Super]      = pSuperRef;
		TheDatas[Index_Interfaces] = (pInterfaceRefs == null)?null:pInterfaceRefs.clone();

		TheDatas[Index_Parameterization] = pTPInfo;
		
		TheDatas[Index_MoreData]  = pMoreData;
		TheDatas[Index_ExtraInfo] = pExtraInfo;
		
		this.Datas = TheDatas;
		
		// Required Types --------------------------------------------------------------------------
		Vector<TypeRef> RTs = new Vector<TypeRef>();
		if(pSuperRef != null)      RTs.add(pSuperRef);
		if(pInterfaceRefs != null) RTs.addAll(Arrays.asList((TypeRef[])TheDatas[Index_Interfaces]));
		RTs.toArray(this.RequiredTypes = new TypeRef[RTs.size()]);
		
		// Used Types -----------------------------------------------------------------------------
		if((pTPInfo != null) && (pTPInfo.getParameterTypeCount() != 0)) { 
			Vector<TypeRef> UTs = new Vector<TypeRef>();
			for(int i = pTPInfo.getParameterTypeCount(); --i >= 0; )
				UTs.add(pTPInfo.getParameterTypeRef(i));
			UTs.toArray(this.UsedTypes = new TypeRef[UTs.size()]);
		}
		
		// The rest of the data should be added or set via TypeBuilder ----------------------------
	}
	
	// TypeSpec -----------------------------------------------------------------------------------
	
	/** Returns the dimension of Data that will be prepared in the constructor */
	protected int getDataIndexCount() {
		return IndexCount;
	}
	
	/**{@inheritDoc}*/ @Override
	protected void resolveParameteredTypeSpec(Context pContext, Engine pEngine) {
		super.resolveParameteredTypeSpec(pContext, pEngine);

		boolean IsThisAParameterized = this.isParameterized();
		boolean IsThisAParametered   = this.isParametered();
		
		TypeRef SuperRef = (TypeRef)this.Datas[Index_Super];
		if(SuperRef instanceof TRBasedOnType) {
			this.Datas[Index_Super] = this.newBaseOnTypeTypeRef(pEngine, (TRBasedOnType)SuperRef);
		
			if((SuperRef instanceof TRBasedOnType) && !IsThisAParameterized)
				this.Datas[Index_Super] = (SuperRef = ((TRBasedOnType)SuperRef).flatType(pEngine, null, null));
		}

		TypeRef[] Interfaces = (TypeRef[])this.Datas[Index_Interfaces];
		if(Interfaces != null) {
			Interfaces = IsThisAParametered ? (TypeRef[])Interfaces : (TypeRef[])Interfaces.clone();
			for(int i = 0; i < Interfaces.length; i++) {
				TypeRef IRef = Interfaces[i];
				if(!(IRef instanceof TRBasedOnType)) continue;
				Interfaces[i] = (IRef = this.newBaseOnTypeTypeRef(pEngine, (TRBasedOnType)IRef));

				if((IRef instanceof TRBasedOnType) && !IsThisAParameterized)
					Interfaces[i] = (IRef = ((TRBasedOnType)IRef).flatType(pEngine, null, null));
			}
		}
	}
	
	// Classification -----------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public String  getKindName() {
		return (String)this.getData(Index_Kind);
	}
	
	/** Checks if the type is abstract (cannot create new instance) */
	final public boolean isAbstract() {
		return Boolean.TRUE.equals(this.getData(Index_IsAbstract));
	}
	/** Checks if the type is final (not derivable) */
	final public boolean isFinal() {
		return Boolean.TRUE.equals(this.getData(Index_IsFinal));
	}
	
	/**{@inheritDoc}*/ @Override
	protected int getSuperIndex() {
		return Index_Super;
	}
	
	// Interface ----------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected int getInterfaceIndex() {
		return Index_Interfaces;
	}
	
	// Parameterization ------------------------------------------------------------------------------------------------	

	/**{@inheritDoc}*/ @Override
	protected int getParameterizationInfoIndex() {
		return Index_Parameterization;
	}
	
	/**{@inheritDoc}*/ @Override
	protected void resetTypeSpecForParameterization() {}
	
	// MoreData and ExtraInfo ---------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override final protected int getMoreDataIndex()  { return Index_MoreData; }
	/**{@inheritDoc}*/ @Override final protected int getExtraInfoIndex() { return Index_ExtraInfo; }

	// StackOwner ---------------------------------------------------------------------------------
	/** Returns the index in Datas of the Data AttributeInfo */
	@Override final protected int getDataAttributeInfoIndex() { return Index_ObjectAttrs; }
	/** Returns the index in Datas of the Data OperationInfo */
	@Override final protected int getDataOperationInfoIndex() { return Index_ObjectOpers; }
	/** Returns the index in Datas of the Type AttributeInfo */
	@Override final protected int getTypeAttributeInfoIndex() { return Index_TypeAttrs; }
	/** Returns the index in Datas of the Type OperationInfo */
	@Override final protected int getTypeOperationInfoIndex() { return Index_TypeOpers; }
	
	// For compilation only --------------------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected void resetTypeSpecForCompilation() {
		Util.ResetTypeRefs(this.getSuperRef());
		Util.ResetTypeRefs(this.getInterfaces());
	}
	
	// Type Information ------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected String getToString() {
		return this.getKindName() + ":=:"+ this.hashCode();
	}

	/**{@inheritDoc}*/ @Override
	protected String getDescriptionDetail(Engine pEngine) {
		TypeRef  SuperRef = this.getSuperRef();
		
		StringBuffer SB = new StringBuffer();
		SB.append(this.getKindName()).append(":=:").append(this.hashCode());
		
		if(SuperRef != null) SB.append(" extends ").append(SuperRef.toString());
		
		StringBuffer ISB = new StringBuffer();
		for(int i = 0; i < this.getInterfaceCount(); i++) {
			if(this.getInterfaceRefAt(i) == null) continue;
			if(ISB.length() != 0) ISB.append(", ");
			ISB.append(this.getInterfaceRefAt(i));
		}
		if(ISB.length() != 0) SB.append(" implements ").append(ISB);
		
		return SB.toString();
	}
}
