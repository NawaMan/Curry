package net.nawaman.curry.extra.type_enum;

import java.util.*;

import net.nawaman.curry.*;
import net.nawaman.curry.TKVariant.TVariant;
import net.nawaman.curry.compiler.TypeSpecCreator;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UString;

final public class TKEnum extends TypeKind {
	
	// Constants ------------------------------------------------------------------------
	
	static final public String KindName = "Enum";
	
	// Constructor ---------------------------------------------------------------------------------
	
	TKEnum(Engine pEngine) {
		super(pEngine);
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		if(!(pTheType instanceof TEnum)) return false;
		return ((TSEnum)pTheType.getTypeSpec()).isFinal();
	}
	
	// Services ---------------------------------------------------------------
	
	public TypeSpec getTypeSpec(TypeRef pTypeRef, TypeRef pSuper, boolean IsFinal, EnumKind pEnumKind,
			TEMemberSpec[] pMemberSpecs, MoreData pExtraData, StringBuffer pSB) {
		return this.getTypeSpec(pTypeRef, pSuper, IsFinal, pEnumKind, pMemberSpecs, pExtraData, true, pSB);
	}
	
	public TypeSpec getTypeSpec(TypeRef pTypeRef, TypeRef pSuper, boolean IsFinal, EnumKind pEnumKind,
			TEMemberSpec[] pMemberSpecs, MoreData pExtraData, boolean IsToVerify, StringBuffer pSB) {
		//if(pTypeRef == null) return null; 
		if(pEnumKind == null) pEnumKind = EnumKind.Independent;
		
		if(IsToVerify) {
			String Error = ensureTypeSpecFormat(null, pTypeRef, pSuper, IsFinal, pEnumKind, pMemberSpecs);
			if(Error != null) {
				if(pSB != null) pSB.append(Error);
				return null;
			}
		}
		
		TSEnum ETE = new TSEnum(pTypeRef, pSuper, IsFinal, pEnumKind, pMemberSpecs, pExtraData);
		return ETE;
	}
	
	/** Creates a TypeSpec Creator for a Enum Type */
	public TypeSpecCreator getTypeSpecCreator(final TypeRef pSuper, final boolean IsFinal, final EnumKind pEnumKind,
			final TEMemberSpec[] pMemberSpecs, final MoreData pExtraData, final boolean IsToVerify) {
		return new TypeSpecCreator() {
			public TypeSpec newTypeSpec(Engine pEngine, TypeRef pTRef, boolean pIsVerify, Documentation pDocument) {
				StringBuffer SB = new StringBuffer();
				MoreData     MD = (pDocument == null)
				                      ? pExtraData
				                      : (pExtraData == null)
				                            ? null
				                            : MoreData.combineMoreData(
				                                  pExtraData,
				                                  Documentation.Util.NewMoreData(pDocument)
				                              );
				TypeSpec TS = getTypeSpec(pTRef, pSuper, IsFinal, pEnumKind, pMemberSpecs, MD, IsToVerify, SB);
				if(SB.length() != 0) {
					throw new IllegalArgumentException(
							String.format(
								"Unable to create type specification for a enum type '%s': %s",
								pTRef, SB
							)
						);
				}
				return TS;
			}
		}; 
	}
	
	String ensureTypeSpecFormat(Context pContext, TypeSpec pTypeSpec) {
		if(pTypeSpec == null)               return "Null TypeSpec.";
		if(!(pTypeSpec instanceof TSEnum)) return "TypeSpec is mal-form (TS_Enum is required).";
		TSEnum TSE = (TSEnum)pTypeSpec; 
		
		return this.ensureTypeSpecFormat(pContext, TSE.getTypeRef(), TSE.getSuperRef(), TSE.isFinal(),
				TSE.getEnumKind(), TSE.getMemberSpecs());
	}
	String ensureTypeSpecFormat(Context pContext, TypeRef pTypeRef, TypeRef pSuper,
			boolean IsFinal, EnumKind pEnumKind, TEMemberSpec[] pMemberSpecs) {

		// Prepare members
		Map<String, TEMemberSpec> TMembers = new Hashtable<String, TEMemberSpec>();
		for(TEMemberSpec TEMS : pMemberSpecs) {
			if(TEMS == null)
				return "Mal-form TypeSpec: At least of the enum member spec is null (Enum type "+pTypeRef+").";
			// Locally repeat name
			if(TMembers.containsKey(TEMS.Name))
				return "Mal-form TypeSpec: Repeat enum member '"+TEMS.Name+"' (Enum type "+pTypeRef+").";
			
			TMembers.put(TEMS.Name, TEMS);
		}
		
		// Check super
		if(pSuper != null) {
			// Must not be an independent
			if(pEnumKind == EnumKind.Independent)
				return "Mal-form TypeSpec: An independent enum type must not have a super (Enum type "+pTypeRef+").";
			
			// Cannot inherit from itself
			if(pTypeRef.equals(pSuper))
				return "Mal-form TypeSpec: An enum type must not inherit or super from itself (Enum type "+pTypeRef+").";
			
			TypeSpec TSSuper = this.getTypeSpec(pContext, pSuper);
			// Super of Enum must be an Enum
			if(!KindName.equals(TSSuper.getKindName()) || !(TSSuper instanceof TSEnum))
				return "Mal-form TypeSpec: Super type "+pSuper.toString()+" of an enum type must be an enum type (Enum type "+pTypeRef+").";

			// Super must not be final
			if((pEnumKind != EnumKind.Grouping) && ((TSEnum)TSSuper).isFinal())
				return "Mal-form TypeSpec: Super type "+pSuper.toString()+" of an enum type must not be final (Enum type "+pTypeRef+").";

			Map<String, TEMemberSpec> SMembers = new Hashtable<String, TEMemberSpec>();
			for(TEMemberSpec TEMS : ((TSEnum)TSSuper).getMemberSpecs())
				SMembers.put(TEMS.Name, TEMS);
			
			if((pEnumKind == EnumKind.Expanding) || (pEnumKind == EnumKind.Emulating)) {
				// Check members for Expanding and Emulating

				// Current borrowed member must be in super
				// Current derive member must either local or from super
				for(TEMemberSpec TEMS : TMembers.values()) {
					if(TEMS instanceof TEMS_Borrowing) {
						// Expandind cannot have borrowing
						if(pEnumKind == EnumKind.Expanding)
							return "Mal-form TypeSpec: Only deriving members are allowed in an expanding enum type ('"+TEMS.Name+"' in "+pTypeRef+").";
						
						if(!SMembers.containsKey(TEMS.Name))
							return "Mal-form TypeSpec: An enum member '"+TEMS.Name+"' borrows from a non-exist enum member '"+TEMS.Name+"' (Enum type "+pTypeRef+").";
						continue;
					}
					if(!(TEMS instanceof TEMS_Deriving))
						return "Mal-form TypeSpec: Only deriving members are allowed in an expanding enum type ('"+TEMS.Name+"' in "+pTypeRef+").";
					
					// Note: No Need to check repeat type name here because constructor of DerivingSpec manage it.
					
					String DN = ((TEMS_Deriving)TEMS).getDerivedName();
					
					// Derive from super
					if(SMembers.containsKey(DN)) continue;
					
					// Derive locally
					if(!TMembers.containsKey(DN))
						return "Mal-form TypeSpec: An enum member '"+TEMS.Name+"' derives from a non-exist enum member '"+DN+"' (Enum type "+pTypeRef+").";
					
					Set<String> Ns = new HashSet<String>();
					// Ensure that the deriving is not recursive
					Ns.add(TEMS.Name);
					TEMemberSpec TEMS_New = TMembers.get(DN);
					while(TEMS_New instanceof TEMS_Deriving) {
						if(Ns.contains(TEMS_New.Name))
							return "Mal-form TypeSpec: Recursive deriving of an enum member '"+TEMS.Name+"' (Enum type "+pTypeRef+").";
						
						// Is it not local
						if(!TMembers.containsValue(TEMS_New.Name)) {
							// Is found in super. If so, done!
							if(SMembers.containsValue(TEMS_New.Name)) break;
							// Not found anywhere
							else return "Mal-form TypeSpec: An enum member '"+TEMS.Name+"' derives from a non-exist enum member '"+DN+"' (Enum type "+pTypeRef+").";
						}
						
						TEMS_New = TMembers.get(TEMS_New.Name);
					}
				}
				
			} else if(pEnumKind == EnumKind.Grouping) {
				// Check members for Grouping

				// All members of super must derive or borrow from this one.
				// Grouped member must not repeat
				Set<String> UseSMembers =  new HashSet<String>();
				for(TEMemberSpec TEMS : TMembers.values()) {
					// Check borrowed
					if(TEMS instanceof TEMS_Borrowing) {
						if(!SMembers.containsKey(TEMS.Name))
							return "Mal-form TypeSpec: An enum member '"+TEMS.Name+"' borrows from a non-exist enum member '"+TEMS.Name+"' (Enum type "+pTypeRef+").";
						// Remove the member out of the super so that we can check if it has used.
						SMembers.remove(TEMS.Name);
						UseSMembers.add(TEMS.Name);
						continue;
					}
					// Check deriving - Deriving must be local only
					if(TEMS instanceof Abstract_TEMS_Deriving) {
						String DN = ((Abstract_TEMS_Deriving)TEMS).getDerivedName();
						if(!TMembers.containsKey(DN)) {
							// Is non-local
							if(SMembers.containsKey(DN) || UseSMembers.contains(DN))
								return "Mal-form TypeSpec: Deriving member '"+TEMS.Name+"' of a grouping enum type must derive locally (Enum type "+pTypeRef+").";
							// Non exist
							return "Mal-form TypeSpec: An enum member '"+TEMS.Name+"' derives from a non-exist enum member '"+DN+"' (Enum type "+pTypeRef+").";
						}
						
						Set<String> Ns = new HashSet<String>();
						// Ensure that the deriving is not recursive
						Ns.add(TEMS.Name);
						TEMemberSpec TEMS_New = TMembers.get(DN);
						while(TEMS_New instanceof TEMS_Deriving) {
							if(Ns.contains(TEMS_New.Name))
								return "Mal-form TypeSpec: Recursive deriving of an enum member '"+TEMS.Name+"' (Enum type "+pTypeRef+").";
							TEMS_New = TMembers.get(TEMS_New.Name);
						}
						if(TEMS instanceof TEMS_Deriving) continue;
					}
					// Independent
					if(TEMS instanceof TEMS_Independent) {
						// The name must not repeat in its super 
						if(SMembers.containsKey(TEMS.Name) || UseSMembers.contains(TEMS.Name))
							return "Mal-form TypeSpec: Independent member '"+TEMS.Name+"' of a grouping enum type must not have the same name with any member of its super (Enum type "+pTypeRef+").";
						continue;
					}
					// If it's not group, error
					if(!(TEMS instanceof TEMS_Grouping))
						return "Mal-form TypeSpec: Invalid enum member kind '"+TEMS.getClass().getCanonicalName()+"' ('"+TEMS.Name+"' in "+pTypeRef+").";
					
					// Here is for group member
					
					// All group members must exist in super
					for(String GN : ((TEMS_Grouping)TEMS).GroupedNames) {
						if(!SMembers.containsKey(GN)) {
							// and ... it must not been used before
							if(UseSMembers.contains(GN))
								return "Mal-form TypeSpec: The grouped member '"+GN+"' is included more that once ('"+TEMS.Name+"' in "+pTypeRef+").";
							return "Mal-form TypeSpec: The grouped member '"+GN+"' must be in the super ('"+TEMS.Name+"' in "+pTypeRef+").";
						}
						// Members of a group cannot be a local deriving
						TEMemberSpec TEMS_Sub = SMembers.get(GN);
						if(TEMS_Sub instanceof Abstract_TEMS_Deriving) {
							String DN = ((Abstract_TEMS_Deriving)TEMS_Sub).getDerivedName();
							if((DN == null) && (SMembers.containsKey(DN) || UseSMembers.add(DN)))
								return "Mal-form TypeSpec: The member '"+GN+"' of a group '"+TEMS.getName()+"' must not be a local deriving member ('"+TEMS.Name+"' in "+pTypeRef+").";
						}
						
						// Remove the member out of the super so that we can check if it has used.
						SMembers.remove(GN);
						UseSMembers.add(GN);
					}
				}
				
			} else {
				// Should not be here
				throw new RuntimeException("Internal Error: Invalid enum type kind (TKEnum.java#184)");
			}
		} else {
			// Non-independent must have a super
			if(pEnumKind != EnumKind.Independent)
				return "Mal-form TypeSpec: A non-independent enum type must have a super (Enum type "+pTypeRef+").";
			
			// All members must be independent or local derive
			for(TEMemberSpec TEMS : TMembers.values()) {
				if(TEMS instanceof TEMS_Independent) continue;
				
				if(!(TEMS instanceof TEMS_Deriving))
					return "Mal-form TypeSpec: Only independent and local-deriving members are allowed in an independent enum type ('"+TEMS.Name+"' in "+pTypeRef+").";
					
				String DN = ((TEMS_Deriving)TEMS).getDerivedName();
				if(!TMembers.containsKey(DN))
					return "Mal-form TypeSpec: An enum member '"+TEMS.Name+"' derives from a non-exist enum member '"+DN+"' (Enum type "+pTypeRef+").";
				
				Set<String> Ns = new HashSet<String>();
				// Ensure that the deriving is not recursive
				Ns.add(TEMS.Name);
				TEMemberSpec TEMS_New = TMembers.get(DN);
				while(TEMS_New instanceof TEMS_Deriving) {
					if(Ns.contains(TEMS_New.Name))
						return "Mal-form TypeSpec: Recursive deriving of an enum member '"+TEMS.Name+"' (Enum type "+pTypeRef+").";
					TEMS_New = TMembers.get(TEMS_New.Name);
				}
			}
		}
		return null;
	}
	
	// ----------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return KindName;
	}

	/**{@inhertDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return false;
	}
	
	// It is very important to remember that Required Types in pTypeInfo may not be resolved
	//     and initialized. Therefore, only use them as TypeRefs
	/**{@inheritDoc}*/ @Override
	protected Type getType(Engine pEngine, Context pContext, TypeSpec pTypeSpec) {
		String Err = this.ensureTypeSpecFormat(pContext, pTypeSpec);
		if(Err != null) {
			throw ExternalContext.newCurryError(pContext,
					"Type Creation Error: " +
					"The following error occur while trying to create a type " +
					pTypeSpec.getTypeRef().toString() + ": " + Err + ".(TKEnum.java#363)",
					null);
		}
		EnumKind EK = ((TSEnum)pTypeSpec).getEnumKind();
		TypeRef STR = ((TSEnum)pTypeSpec).getSuperRef();
		TEnum Super = (STR != null)?(TEnum)ExternalContext.getTypeFromRef(pContext, STR):null;
		if(EK == EnumKind.Independent) return new TEnum.TE_Independent(this, (TSEnum)pTypeSpec);
		if(EK == EnumKind.Expanding)   return new TEnum.TE_Expanding(  this, (TSEnum)pTypeSpec, Super);
		if(EK == EnumKind.Emulating)   return new TEnum.TE_Emulating(  this, (TSEnum)pTypeSpec, Super);
		if(EK == EnumKind.Grouping)    return new TEnum.TE_Grouping(   this, (TSEnum)pTypeSpec, Super);
		
		return null;
	}

	// Get Type -------------------------------------------------------------------------
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOf(Context pContext, Object pObj) {
		if(pObj instanceof DEnum) return ((DEnum)pObj).getTheType();
		return null;
	}
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) { return null; }
	
	// Typing --------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TVariant.class;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getDefaultType(Context pContext) {
		return null;
	}

	// Information and functionality -------------------------------------------
	
	// Return the class of the data object, this is used in Array and Collection
	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pTS) {
		return DEnum.class;
	}
	
	// Check if the data object is a valid data of this type.
	// This method will be called only after the data class is checked
	//     so there is no need to check for the data class again.
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec, Object pByObject) {
		if(pByObject == null) return  true;
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine,
				pTheTypeSpec.getTypeRef(), ((DEnum)pByObject).getTheType().getTypeRef());
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec, TypeSpec BySpec) {
		
		Engine $Engine = this.getEngine();
		MType  MT      = $Engine.getTypeManager();
		
		// We can do this because the only types enum requires are its super
		if(!TheSpec.isInitialized() || !BySpec.isInitialized()) {
			if(pContext != null) {
				ExternalContext.ensureTypeInitialized(pContext, TheSpec.getTypeRef());
				ExternalContext.ensureTypeInitialized(pContext, BySpec.getTypeRef());
			} else {
				MT.ensureTypeInitialized(TheSpec.getTypeRef());
				MT.ensureTypeInitialized(BySpec.getTypeRef());
			}
		}
		TEnum TheEnum = (TEnum)this.getTypeFromRef(pContext, TheSpec.getTypeRef());
		TEnum ByEnum  = (TEnum)this.getTypeFromRef(pContext, BySpec.getTypeRef());
		
		// Prepare inherit path
		TheEnum.getInheritPath();
		ByEnum.getInheritPath();

		List<TEnum> A_TEs = TheEnum.InheritPath.getSource();
		List<TEnum> B_TEs = ByEnum.InheritPath.getSource();
		
		if(TheEnum.isGrouping() == ByEnum.isGrouping()) {
			// If so, they may share the same path
			
			// Path of A must be shorter and be contained in B
			// So they are not in the same path, so they will never equal.
			return B_TEs.contains(A_TEs.get(A_TEs.size() - 1));
			
		} else {
			if(!TheEnum.isGrouping()) return false;

			// Is TE_A is group and join the non-group TE_B
			if(B_TEs.contains(A_TEs.get(A_TEs.size() - 1))) return true;
			// Is TE_A is group and joined by the non-group TE_B
			if(A_TEs.contains(B_TEs.get(0)))                return true;
			
			return false;
		}
	}
	
	// Initialization ------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected boolean isNeedInitialization() {
		return false;
	}

	/**{@inheritDoc}*/ @Override
	protected boolean isTypeAbstract(Context pContext, Type pTheType) {
		return false;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Object getTypeDefaultValue(Context pContext, Type pTheType) {
		return (pTheType instanceof TEnum)?((TEnum)pTheType).getMemberByIndex(0):null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Object createNewTypeInstance(Context pContext, Executable pInitiator,
			Type pTheType, Object pSearchKey, Object[] pParams) {
		// Returns null before all the instances of this type are create at the type initialization
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected ConstructorInfo[] getConstructorInfos(Context pContext, Engine pEngine, Type pTheType) {
		return null;
	}
	
	static TypeRef[] IntParamsTypes = new TypeRef[] { TKJava.TInteger.getTypeRef() };
	static String[]  IntParamsNames = new String[]  { "I" };

	static TypeRef[] StringParamsTypes = new TypeRef[] { TKJava.TString.getTypeRef() };
	static String[]  StringParamsNames = new String[]  { "S" };
	
	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindFields(Context pContext, Engine pEngine, Type pTheType,
			boolean pIsStatic, Vector<AttributeInfo> AIs) {
		if(!pIsStatic || !(pTheType instanceof TEnum)) return;
		
		TEnum   TE = ((TEnum)pTheType);
		TypeRef TR = TE.getTypeRef();
		Accessibility Public = Accessibility.Public;
		
		for(int i = 0; i < TE.getMemberCount(); i++) {
			DEnum DE = TE.getMemberByIndex(i);
			this.addTypeAttributeToAttributeList(pContext, pEngine, AIs,
				this.doType_newAIDirect(pTheType, Public, Public, Public, DE.getName(), false, 
					new DataHolderInfo(TR, DE, Variable.FactoryName, true, false, true, false, null ),
					null, null
				)
			);
		}
	}
	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {
		if(!pIsStatic || !(pTheType instanceof TEnum)) return;

		Engine        E      = this.getEngine();
		MExecutable   ME     = E.getExecutableManager();
		TEnum         TE     = ((TEnum)pTheType);
		TypeRef       TR     = TE.getTypeRef();
		Accessibility Public = Accessibility.Public;
		ExecSignature ES;
		
		// Get MemberCount --------------------------------------------------------------
		ES = ExecSignature.newSignature(
			"getMemberCount",
			TypeRef.EmptyTypeRefArray,
			UString.EmptyStringArray,
			TKJava.TInteger.getTypeRef()
		);
		this.addTypeOperationToOperationList(pContext, pEngine, OIs,
			this.doType_newOIDirect(
				pTheType,
				Public,
				ME.newSubRoutine(ES, TE.getMemberCount()),
				null
			)
		);
		
		final TEnum TheTE = TE;
		
		// Get MemberAt -----------------------------------------------------------------
		ES = ExecSignature.newSignature("getMemberAt", IntParamsTypes, IntParamsNames, TR );
		this.addTypeOperationToOperationList(pContext, pEngine, OIs,
			this.doType_newOIDirect(
				pTheType,
				Public,
				new JavaExecutable.JavaSubRoutine_Complex(ES) {
					/**{@inheritDoc}*/ @Override
					protected Object run(Context $Context, Object[] pParams) {
						if(pParams[0] == null) return null;
						return TheTE.getMemberByIndex(((Integer)pParams[0]).intValue());
					}
				},
				null
			)
		);
		
		// Get Member -----------------------------------------------------------------
		ES = ExecSignature.newSignature("getMember", StringParamsTypes, StringParamsNames, TR );
		this.addTypeOperationToOperationList(pContext, pEngine, OIs,
			this.doType_newOIDirect(
				pTheType,
				Public,
				new JavaExecutable.JavaSubRoutine_Complex(ES) {
					/**{@inheritDoc}*/ @Override
					protected Object run(Context $Context, Object[] pParams) {
						if(pParams[0] == null) return null;
						return TheTE.getMember(pParams[0].toString());
					}
				},
				null
			)
		);
	}
	
	/**{@inheritDoc}*/ @Override
	protected Exception doValidateType(Context pContext, Type pThisType) {
		if(pThisType ==  null)            return new NullPointerException();
		if(!(pThisType instanceof TEnum)) return new IllegalArgumentException("Wrong type.(TKEnum.java#348)");
		
		TSEnum TS = ((TSEnum)pThisType.getTypeSpec());
		String Err = this.ensureTypeSpecFormat(pContext, TS);
		if(Err != null) return ExternalContext.newCurryError(pContext, Err, null);
		return null;
	}
}
