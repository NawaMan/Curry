package net.nawaman.curry;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import net.nawaman.curry.TKInterface.TSInterface;
import net.nawaman.curry.TKJava.TSJava;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLPackage.TRPackage;
import net.nawaman.curry.TLPackage.TRPackage_Internal;
import net.nawaman.curry.TLParameter.TRParameter;
import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.TLPrimitive.TRPrimitive;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.util.UClass;

public class MType extends EnginePart {
	
	static public final String PREDEFINEDTYPE_PACKAGENAME = "curry";
	
	/** Constructs a Type Manager */
	protected MType(Engine pEngine) {
		super(pEngine);
	}
	
	/**{@inheritDoc}*/ @Override
	public MType getTypeManager() {
		return this;
	}
	
	// For Engine Initialization ------------------------------------------------------------------
	
	/** Prepares the engine primitive types.<br/>This method must display error message if needed.**/
	boolean preparePrimitiveTypes() {
		
		// Primitive Types -------------------------------------------------------------------------
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TAny))       return false;
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TVoid))      return false;
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TBoolean))   return false;
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TCharacter)) return false;
		
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TString)) return false;
		
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TByte))    return false;
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TShort))   return false;
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TInteger)) return false;
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TLong))    return false;
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TFloat))   return false;
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TDouble))  return false;
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TNumber))  return false;
		
		return true;
	}
	
	/** Sets some types to be a locked one as it cannot be used in Curry */
	boolean prepareLockedTypes() {
		/*
		if(!this.regPrimitiveType((TKJava.TJava)TKJava.TEngine))         return false;
		//if(!this.registerPrimitiveType((TKJava.TJava)TKJava.TExecutor))     return false;
		//if(!this.registerPrimitiveType((TKJava.TJava)TKJava.TScopePrivate)) return false;
		//if(!this.registerPrimitiveType((TKJava.TJava)TKJava.TExpression))   return false;
		
		if(!this.regLockedType((TKJava.TJava)TKJava.TType))         return false;
		if(!this.regLockedType((TKJava.TJava)TKJava.TTypeSpec))     return false;
		if(!this.regLockedType((TKJava.TJava)TKJava.TTypeKind))     return false;
		if(!this.regLockedType((TKJava.TJava)TKJava.TTypeRef))      return false;
		if(!this.regLockedType((TKJava.TJava)TKJava.TTypeLoader))   return false;
		
		if(!this.regLockedType((TKJava.TJava)TKJava.TContext))      return false;
		if(!this.regLockedType((TKJava.TJava)TKJava.TScopePrivate)) return false;
		*/
		return true;
	}
	
	/** Prepares the engine type factories.<br/>This method must display error message if needed.**/
	boolean prepareTypeKinds() {
		if(!this.regTypeKind(null, TKJava.Instance))                  return false;
		if(!this.regTypeKind(null, new TKType(      this.TheEngine))) return false;
		if(!this.regTypeKind(null, new TKArray(     this.TheEngine))) return false;
		if(!this.regTypeKind(null, new TKVariant(   this.TheEngine))) return false;
		if(!this.regTypeKind(null, new TKInterface( this.TheEngine))) return false;
		if(!this.regTypeKind(null, new TKExecutable(this.TheEngine))) return false;
		return true;
	}
	
	/** Prepares the engine type resolvers.<br/>This method must display error message if needed.**/
	boolean prepareTypeLoaders() {
		if(!this.regTypeLoader(null, new TLPrimitive(  this.TheEngine))) return false;
		if(!this.regTypeLoader(null, new TLNoName(     this.TheEngine))) return false;
		if(!this.regTypeLoader(null, new TLCurrent(    this.TheEngine))) return false;
		if(!this.regTypeLoader(null, new TLParametered(this.TheEngine))) return false;
		if(!this.regTypeLoader(null, new TLParameter(  this.TheEngine))) return false;
		return true;
	}
	
	// Type Loaders ---------------------------------------------------------
	
	/** Container of type loader */
	private HashMap<String, TypeLoader>      TypeLoaders      = new HashMap<String, TypeLoader>();
	private HashMap<String, EngineExtension> TypeLoaderOwners = new HashMap<String, EngineExtension>();
	
	/** Registers a type kind pEK in to this Engine. */
	boolean regTypeLoader(EngineExtension EExt, TypeLoader pTL) {
		if(this.TheEngine.IsInitialized) return false;
		if(pTL == null)        return false;
		if(this.TypeLoaders.get(pTL.getKindName()) != null) return false;
		this.TypeLoaders     .put(pTL.getKindName(), pTL);
		this.TypeLoaderOwners.put(pTL.getKindName(), EExt);
		return true;
	}
	
	/** Returns the type loader by the name */
	TypeLoader getTypeLoader(String TLName) {
		return this.TypeLoaders.get(TLName);
	}

	/**
	 * Returns the owner of the type loader with the name or null if the loader is owned by the code engine or the
	 *    engine spec
	 **/
	EngineExtension getOwnerOfTypeLoader(String TLName) {
		return this.TypeLoaderOwners.get(TLName);
	}
	
	// Type Kinds --------------------------------------------------------------
	
	/** Container of type kinds. */
	private HashMap<String, TypeKind>        TypeKinds      = new HashMap<String, TypeKind>();
	private HashMap<String, EngineExtension> TypeKindOwners = new HashMap<String, EngineExtension>();
	
	/** Registers a type kind pTK in to this Engine. */
	boolean regTypeKind(EngineExtension EExt, TypeKind pTK) {
		if(this.TheEngine.IsInitialized) return false;
		if(pTK == null)        return false;
		if(this.TypeKinds.get(pTK.getKindName()) != null) return false;
		this.TypeKinds     .put(pTK.getKindName(), pTK);
		this.TypeKindOwners.put(pTK.getKindName(), EExt);
		return true;
	}

	/** Returns the type kind named pKindName */
	public TypeKind getTypeKind(String pKindName) {
		return this.TypeKinds.get(pKindName);
	}
	/** Returns the names of all the type kind */
	public String[] getTypeKindNames() {
		return this.TypeKinds.keySet().toArray(new String[this.TypeKinds.size()]);
	}
	/**
	 * Returns the owner of the type kind with the name or null if the loader is owned by the code engine or the engine
	 *     spec
	 **/
	public EngineExtension getOwnerOfTypeKind(String pKindName) {
		return this.TypeKindOwners.get(pKindName);
	}

	// Primitive Types -----------------------------------------------
	
	// Primitive types must be universal because it directly associated with Java Class.
	// All native type must be registered by engine extensions
	Vector<TKJava.TJava> PrimitiveTypes   = new Vector<TKJava.TJava>();
	Vector<Class<?>>     PrimitiveClasses = new Vector<Class<?>>();
	boolean regPrimitiveType(TKJava.TJava pType) {
		if(this.TheEngine.IsInitialized) return false;
		if(pType == null)      return false;
		this.PrimitiveTypes  .add(pType);
		this.PrimitiveClasses.add(pType.getDataClass());
		((TSJava)pType.getTypeSpec()).setToPrimitive();
		return true;
	}
	boolean regLockedType(TKJava.TJava pType) {
		if(this.TheEngine.IsInitialized) return false;
		if(pType == null)      return false;
		this.regPrimitiveType(pType);	// Locked must also be a primitive.
		((TSJava)pType.getTypeSpec()).setToLocked();
		return true;
	}
	/** Checks if the type of the class pClass is primitive */
	public boolean isPrimitiveType(Class<?> pClass) {
		if(pClass == null) return false;
		return this.PrimitiveClasses.contains(pClass);
	}
	/** Checks if the type of the class pClass is locked */
	public boolean isSubClassOfLocked(Class<?> pClass) {
		if(pClass == null) return true;
		for(TKJava.TJava T : this.PrimitiveTypes) {
			if(T == TKJava.TAny)  continue;
			if(T == TKJava.TVoid) continue;
			if(T.getDataClass().isAssignableFrom(pClass))
				return ((TSJava)T.getTypeSpec()).isLocked();
		}
		return false;
	}
	/** Checks if the type of object pObj is locked */
	public boolean isInstanceOfLocked(Object pObj) {
		if(pObj == null) return false;
		for(TKJava.TJava T : this.PrimitiveTypes) {
			if(T == TKJava.TAny)  continue;
			if(T == TKJava.TVoid) continue;
			if(T.getDataClass().isInstance(pObj))
				return ((TSJava)T.getTypeSpec()).isLocked();
		}
		return false;
	}

	// Pre-define TypeRef ----------------------------------------------------------------------------------------------
	
	HashMap<String, TypeSpec> PredefinedTypeSpecs = new HashMap<String, TypeSpec>();
	
	/** Register a temporary TypeSpec as a pre-define type. The type must be in "<PREDEFINEDTYPE_PACKAGENAME>" package */
	final String registerTempPredefineTypeSpec(String TName) {
		if(TName == null) return null;
		
		if(this.PredefinedTypeSpecs.containsKey(TName))
			return "Invalid type reference: pre-defined TypeRef already exists. ("+TName+") <MType:217>";
		
		this.PredefinedTypeSpecs.put(TName, null);
		return null;
	}
	
	/** Register a type spec as a pre-define type. The type must be in "<PREDEFINEDTYPE_PACKAGENAME>" package */
	final String registerPredefineTypeSpec(TypeSpec TS) {
		if(TS == null) return null;
		
		TypeRef TRef = TS.getTypeRef();
		if(!(TRef instanceof TRPackage))
			return "Invalid type reference: pre-defined TypeRef must be a package type spec ("+TS.getTypeRef()+") <MType:803>";
		
		TRPackage TPRef = (TRPackage)TRef;
		if(!PREDEFINEDTYPE_PACKAGENAME.equals(TPRef.getPackageName()))
			return "Invalid type reference: pre-defined TypeRef must be in the `curry` package ("+TS.getTypeRef()+") <MType:807>";
		
		String TName = TPRef.getTypeName();
		if(this.PredefinedTypeSpecs.containsKey(TName) &&
		  (this.PredefinedTypeSpecs.get(TName) != TS)  &&
		  (this.PredefinedTypeSpecs.get(TName) != null))
			return "Invalid type reference: pre-defined TypeRef already exists. ("+TS.getTypeRef()+") <MType:239>";
		
		Type T = TRef.getTheType();
		if((T != null) && (T.getEngine() != null) && (T.getEngine() != this.getEngine())) {
			TypeSpec NewTS = null;
			
			// Use the cloned one as it can be sure not the be changed
			try { NewTS = (TypeSpec)TS.clone(); }
			catch (CloneNotSupportedException E) {}
			if((NewTS == null) || (NewTS == TS))
				throw new CurryError(
					String.format(
						"Pre-define type spec of '%s' already belong to other engine and it fails to produce a clone <MType:817>.",
						TRef
					)
				);
			
			TS = NewTS;
			TS.resetForCompilation();
		}

		this.PredefinedTypeSpecs.put(TName, TS);
		TS.Ref = new TRPackage_Internal(
					TS,
					PREDEFINEDTYPE_PACKAGENAME,
					TName,
					Accessibility.Public,
					null);
		return null;
	}
	
	/** Returns the TypeRef of the pre-define type associated with the given type name. */
	final public TypeRef getPrefineTypeRef(String TName) {
		TypeSpec TS = this.PredefinedTypeSpecs.get(TName);
		return (TS == null) ? null : TS.getTypeRef();
	}

	/** Returns the TypeSpec of the pre-define type associated with the given type name. */
	final TypeSpec getPrefineTypeSpec(String TName) {
		return this.PredefinedTypeSpecs.get(TName);
	}
	
	//==================================================================================================================
	//== Load, Resolve, Validate and Initialized =======================================================================
	//==================================================================================================================
	
	// Load Types ------------------------------------------------------------------------------------------------------

	/**
	 * Resolves an type from the type reference. <br />
	 * Types in this package can override this method and load type directly. Otherwise, another
	 *    loadType method should be used.
	 **/
	/* Must be called from TypeManager only */
	private Exception performLoadType(Context pContext, TypeLoader TL, TypeRef /*NoNull*/ pTRef, boolean pIsNo_IsLoaded) {
		Engine   $Engine = this.getEngine();
		TypeSpec TS      = null;
		
		// Do the load (get the spec)
		Object LoadSpecResult = TL.loadTypeSpec(pContext, pTRef);
		if(!(LoadSpecResult instanceof TypeSpec)) {
			if(LoadSpecResult instanceof Throwable) { // An Error
				return new CurryError(
					String.format("Type Loading Error: Type resolution fail (%s).", pTRef.toString()),
					pContext,
					(Exception)LoadSpecResult
				);
			}
			
			// Wrong Type of result
			return new CurryError(
				String.format(
					"Type Loading Error: Invalid type '%s' loading result by %s. A TypeSpec is expected but '%s' was returned.",
					pTRef, TL.getKindName(), $Engine.toString(pContext, LoadSpecResult)
				),
				pContext
			);
		}
		TS = (TypeSpec)LoadSpecResult;

		// Construct the type
		// Change the status - The type is now fully loaded
		if(!TS.isLoaded() || !TS.Ref.isLoaded()) {			
			// Get the TypeKind
			TypeKind TK = $Engine.getTypeManager().getTypeKind(TS.getKindName());
			if(TK == null) {
				// No such type kind
				return new CurryError(
					"Type Loading Error: Unknown type kind '"+ TS.getKindName() + "' ("+pTRef.toString()+")",
					pContext
				); 
			}
			
			// Construct the type
			Object Result = TK.getType($Engine, pContext, TS);
			if(!(Result instanceof Type)) {	// Wrong type of result
				if(Result instanceof Throwable) {		// An Error
					return new CurryError(
							"Type Resolution Error: Type resolution fail.",
							pContext, (Exception)Result
						);
				}
				
				// This is a hack
				if("Type:<any>:Type".equals(TS.toString()))
					Result = TK.getType($Engine, pContext, TS);
				
				return new CurryError(
					"Type Loading Error: Invalid type loading result by "+ TL.getKindName() +". A TypeSpec is " +
					"expected but " + $Engine.toString(pContext, Result) + " was returned.",
					pContext, (Exception)Result
				);
			}
			
			Type T = (Type)Result;
			// Set the type to the type ref
			pTRef .setTheType(T);
			TS.Ref.setTheType(T);

			// Change the type status
			if(!TS.isLoaded())
				TS.TypeStatus = TypeSpec.Status.Loaded;
			
		} else {
			// Set the type to the type ref
			pTRef.setTheType(TS.Ref.getTheType());
			
		}
		// Success
		return null;
	}
	
	// Resolve Types ---------------------------------------------------------------------------------------------------
	
	/* Types thats are currently been resolving. */
	private HashMap<String, Type> BeingResolvedTypes_Map = new HashMap<String, Type>();
	private HashSet<Type>         BeingResolvedTypes_Set = new HashSet<Type>();
		
	// Resolve type (resolve the parameters)
	// This method can be called to ensure that the type exists
	
	/** Resolve a type referred by the type ref pRef. */
	// pRef must not be null
	private Exception performResolveType(Context pContext, TypeRef pRef) {
		
		// If not dynamic, it may already be resolved
		boolean IsDynamic = TypeRef.isTypeRefDynamic(pRef);
		if(!IsDynamic) {
			Type    pRef_Type     = pRef.getTheType();
			boolean IsRefResolved = pRef_Type != null;

			// Already resolved
			if(IsRefResolved && pRef_Type.isResolved())
				return null;

			// For TypeTypeRef (TypeRef of a Type of Type), do its target first
			if(pRef instanceof TypeTypeRef) {
				TypeRef TTRef = ((TypeTypeRef)pRef).getTheRef();
				
				// Resolve the "TheType"
				Exception ValidationProblem = this.performResolveType(pContext, TTRef);
				if(ValidationProblem != null)
					return ValidationProblem;
				
				TKType TKT = (TKType)this.getTypeKind(TKType.KindName);
				// Get the spec of the Type of Type
				TypeSpec TheTSpec = TKT.getTypeSpecOfTypeOf(TTRef);
				// Assigned it to the pRef
				Type T = TheTSpec.getTypeRef().getTheType();
				pRef.setTheType(T);
				
				if(T.isResolved())
					return null;
			}
		}
		
		// The one being resolved -----------------------------------------------------------------
		
		Type Type = null;
		try { Type = this.BeingResolvedTypes_Map.get(pRef.toString()); } catch (Exception E) {}
		if(Type != null) {
			pRef.setTheType(Type);
			return null;
		}
		
		// Often used
		Engine $Engine = this.getEngine();
		
		// Create the default context
		if(pContext == null)
			pContext = $Engine.newRootContext();

		Type    pRef_Type   = pRef.getTheType();
		boolean IsRefLoaded = !IsDynamic && (pRef_Type != null);
		
		// If not at all loaded, load it
		if(!IsRefLoaded || !pRef_Type.isLoaded()) {
			
			// At the type loader
			String KindName = pRef.getRefKindName();
			TypeLoader TL = this.TypeLoaders.get(KindName);
			if(TL == null) {
				return new CurryError(
					String.format(
						"Type Loading Error: No such type loader '%s' (%s)",
						KindName, pRef
					),
					pContext
				);
			}
			
			// Load the type
			Exception TypeLoadingProblem = this.performLoadType(pContext, TL, pRef, false);
			if(TypeLoadingProblem != null) return TypeLoadingProblem;

			pRef_Type = pRef.getTheType();
			
			// Ensure that the type is really been loaded.
			if((pRef_Type == null) || !pRef_Type.isLoaded()) {
				return new CurryError(
					String.format(
						"Type Loading Error: There is an unknown error preventing the type from being loaded and resolved '%s'.",
						pRef
					),
					pContext
				);
			}
			
			// Set the loaded type into the the Ref of the spec
			pRef_Type.getTypeSpec().Ref.setTheType(pRef_Type);
		}
		
		// If already resolved, DONE!!!
		if(pRef_Type.isResolved()) return null;

		
		// ----------------------------------------------------------------------------------------
		// Resolve the type -----------------------------------------------------------------------
		// ----------------------------------------------------------------------------------------
		
		Type   T         = pRef_Type;
		String TRefToStr = null;
		
		// Is it being resolved?
		try { TRefToStr = T.getTypeRef().toString(); } catch (Exception E) {}
		if(TRefToStr != null)
		{      if(this.BeingResolvedTypes_Map.containsKey(TRefToStr)) return null; }
		else { if(this.BeingResolvedTypes_Set.contains(T))            return null; }
				
		TypeSpec TSpec = null;
		try {
			// Marked as being resolved
			if(TRefToStr != null)
			{      this.BeingResolvedTypes_Map.put(TRefToStr, T); }
			else { this.BeingResolvedTypes_Set.contains(T);       }

			TSpec = T.getTypeSpec();

			// Resolve dynamic type-ref (Super and/or interface may be BOT)
			if(!(TSpec.getTypeRef() instanceof TRParametered))	// Already resolved
				TSpec.resolveParameters(pContext, $Engine);

			// Resolve all requried types.
			TypeRef[] TRefs = TSpec.RequiredTypes;
			if((TRefs != null) && (TRefs.length != 0)) {
				for(int i = TRefs.length; --i >= 0;) {
					TypeRef TRef = TRefs[i];
					if(TRef == null) continue;
					
					// Ensure the type exist and not recursively required this type
					Exception Excp = this.ensureRequiredType(pContext, TRef);
					if(Excp == null) continue;
						
					return new CurryError(
						String.format(
							"Type Resolution Error: Untable to resolve a type '%s' because its required type '%s' " +
							"cannot be resolved.",
							T, TRef
						),
						pContext, Excp 
					);
				}	
			}

			// Validate the TypeSpec - Primitive checking for non-type related TypeSpec validation
			Exception TypeSpecValidateProblem = T.getTypeKind().doValidateTypeSpec(pContext, TSpec);
			if(TypeSpecValidateProblem != null) return TypeSpecValidateProblem;
			
			// Change the status
			TSpec.TypeStatus = TypeSpec.Status.Resolved;
			
		} finally {
			// Mask was not being resolved.
			if(TRefToStr != null)
			{      this.BeingResolvedTypes_Map.remove(TRefToStr); }
			else { this.BeingResolvedTypes_Set.remove(T);         }
			
			if(TSpec != null) {
				HashSet<TypeRef> CompatibleRefs = this.SpecToBeAssignable.get(TSpec.Ref);
				if(CompatibleRefs != null) {
					// Checks the compatibility (those that were assumed to be compatilble (to avoid recursive looping))
					if(TSpec.isResolved()) {
						// Checks type compatible after resolve here
						for(TypeRef CRef : CompatibleRefs) {
							if(CRef.canBeAssignedByInstanceOf($Engine, TSpec.Ref)) continue;
							
							// Not compatible, the type is no-longer consider valid
							TSpec.Ref = null;
							TSpec.TypeStatus = TypeSpec.Status.Unloaded;
							break;
						}
					}

					// Remove the record
					this.SpecToBeAssignable.remove(TSpec.Ref);
				}
			}
		}
		
		// Success
		return null;
	}
	
	// This will only be called by performedResolveType(...) in order to see if required types are valid (exists and no
	//   recusively required) 
	
	/** Ensure that a required type exists and in the right state */
	private Exception ensureRequiredType(Context pContext, TypeRef pTRef) {
		if(pTRef == null) return null;
		
		Type T = pTRef.getTheType();
		if(T != null) {
			// Already resolved
			if (T.isResolved())
				return null;
/*
			// The type is loaded but not yet resolve so it is likely to be the one being resolved
			return new CurryError(
				String.format(
					"Type Resolution Error: Recursive required type detected (%s).",
					pTRef
				),
				pContext
			);
*/
		}
		
		// Resolve the "TheType"
		Exception ValidationProblem = this.performResolveType(pContext, pTRef);
		if(ValidationProblem != null)
			return ValidationProblem;
		
		return null;
	}
	
	// A collection to remember that the type specs must be compatible - so that MType can check them after the Spec is
	//   resolved.
	private HashMap<TypeRef, HashSet<TypeRef>> SpecToBeAssignable = new HashMap<TypeRef, HashSet<TypeRef>>();
	
	// Check the Base type replacing is likely to happen during type-spec parameter resolution (in type resolution) but
	//   at the time the type spec is not ready for such check. This method allows 
	/** Checks if the NewBase can replace OldBase a base of a BOT types */
	boolean canReplaceBaseOfBOT(TRBasedOnType pBOT, TypeRef pNewBase) {
		TypeRef OldBase = pBOT.getBaseTypeRef();
		
		// 1. If the old base is null, return true
		if(OldBase == null)
			return true;
		
		// 2. If the TypeRef seem assignable
		if(Boolean.TRUE.equals(MightTypeRefByAssignableByInstanceOf(OldBase, pNewBase)))
			return true;
		
		// 3. If the new TypeRef is not resolved, something must be wrong here
		boolean IsTypeExist = true;
		try { this.ensureTypeExist(null, pNewBase); }
		catch (Exception E) { IsTypeExist = false; }
		if(!IsTypeExist || !pNewBase.isLoaded()) {
			throw new CurryError(
				String.format(
					"Fail to resolve a BasedOnType type '%s' as the new base type '%s' has not yet been loaded. <MType:479>",
					pBOT, pNewBase
				)
			);
		}
		
		// 4. The type is loaded but not yet resolved, add the type checking to be done later
		if(!pNewBase.getTheType().isResolved()) {
			TypeSpec         NewBaseTS     = pNewBase.getTheType().getTypeSpec();
			HashSet<TypeRef> TS_ToAssignTo = this.SpecToBeAssignable.get(NewBaseTS.Ref);	// Use the main Ref
			if(TS_ToAssignTo == null) TS_ToAssignTo =  new HashSet<TypeRef>();
			
			// Add them to be processed later.
			TS_ToAssignTo.add(OldBase);
			
			// Assume it is true
			return true;
		} 
		
		// 5. If the BOT is a parameter, see if we can get the parameter form the new base
		if(pBOT instanceof TRParameter) {
			TypeSpec TS = pNewBase.getTypeSpecWithoutEngine();
			TypeParameterInfo.TypeParameterInfos TPIs = TS.getTypeParameterInfos();
			if(TPIs.containParameterTypeRef(((TRParameter)pBOT).getParameterName())) return true;
		}
		
		// 6. Check if the new base is compatible with the old one
		// If needed, canBeAssignedByInstanceOf may ensure the OldBase is validated or even initailized.
		// That is left to the method 'canBeAssignedByInstanceOf' to decide.
		return OldBase.canBeAssignedByInstanceOf(this.getEngine(), pNewBase);
	}
	
	// Validate Types --------------------------------------------------------------------------------------------------
	
	/* Types thats are currently been Validated. */
	private HashSet<Type> BeingValidatedTypes = new HashSet<Type>();
	
	// Validate Types
	// This method is to be called to validate that the type is in the right condition
	// This should be called before constructors/attributes/operations are prepared
	
	/** Link a type referred by the type ref pRef. */
	// pType must not be null
	private Exception performValidateType(Context pContext, Type pType, TypeRef pParameterBaseTypeToIgnore) {
		// Already resolved and not dynamic
		if(pType.isValidated())
			return null;
		
		// Is it being Validated?
		if(this.BeingValidatedTypes.contains(pType)) return null;
		
		try {
			// Often used
			Engine $Engine = this.getEngine();
			
			// Validate the Type
			if(pContext == null) pContext = $Engine.newRootContext();
			
			// Marked as being resolved
			this.BeingValidatedTypes.add(pType);

			TypeSpec TSpec = pType.getTypeSpec();

			// Linked all requried types and resolved all used types.
			for(int l = 2; --l >= 0; ) {
				TypeRef[] TRefs = (l == 1) ? TSpec.RequiredTypes : TSpec.UsedTypes;
				if((TRefs == null) || (TRefs.length == 0)) continue;

				for(int i = TRefs.length; --i >= 0;) {
					TypeRef TRef = TRefs[i];
					if(TRef == null) continue;
					
					if(l == 1) {
						// Initialize the requireds
						Exception Excp = this.initializeType(pContext, TRef, pParameterBaseTypeToIgnore);
						if(Excp == null) continue;
						
						return new CurryError(
							String.format(
								"Type Resolution Error: Untable to resolve a type '%s' because its required type '%s'" +
								" cannot be initialized.",
								pType, TRef
							),
							pContext, Excp 
						);
						
					} else {
						// Resolve the useds
						Exception Excp = this.performResolveType(pContext, TRef);
						if(Excp == null) continue;
						
						return new CurryError(
							String.format(
								"Type Resolution Error: Untable to resolve a type '%s' " + "because its used type " +
								"'%s' cannot be resolved.",
								pType, TRef
							),
							pContext, Excp
						);
					}
				}	
			}
			
			Exception ValidationProblem = pType.getTypeKind().doValidateType(pContext, pType);
			if(ValidationProblem != null)
				return new CurryError("Type Validation Error: Type validation fail.", pContext, ValidationProblem);
			
			// Change the status
			TSpec.TypeStatus = TypeSpec.Status.Validated;
			
		} finally {
			// Mask was not being resolved.
			this.BeingValidatedTypes.remove(pType);
			
		}
		
		// Success
		return null;
	}
	
	// Initialize Types ------------------------------------------------------------------------------------------------

	/** Types that are currently been initialized. */
	HashSet<Type> BeingInitializedTypes                    = new HashSet<Type>();
	/** Types that've just been initialized. */
	HashSet<Type> JustInitializedTypes                     = new HashSet<Type>();
	/** Types that are currently been initialized. */
	HashSet<Type> BeingValidateTypePostInitializationTypes = new HashSet<Type>();

	/** Add a Type to a set to mark that it is being initialized */
	private void addToBeingInitializedTypes(Type T) {
		if(T instanceof TKJava. TJava) return;
		// TODO - Not sure if this is needed or correct
		/*
		if(T instanceof TKArray.TArray) {
			this.addToBeingInitializedTypes(((TKArray.TArray)T).getContainType());
			return;
		}
		*/
		this.BeingInitializedTypes.add(T);
	}

	// Initialize - is to ensure that the type is fully ready, a new instance can be created
	
	/**
	 * Initializes the type
	 * @return	Exception	Any error that occurs or null if not any.
	 **/
	private Exception performInitializeType(Context pContext, Type /*NoNull*/ pType, TypeRef pParameterBaseTypeToIgnore) {
		Type T = pType;
		// Already Initialized
		if(T.isInitialized())
			return null;
		
		// Is it being resolved?
		if(this.BeingInitializedTypes.contains(T))
			return null;
		
		try {
			// Mask as being resolved.
			this.addToBeingInitializedTypes(T);
			
			// Often used
			Engine $Engine = this.getEngine();
			
			// Validate the Type
			if(pContext == null) pContext = $Engine.newRootContext();
			
			// The type is not ready validated, do the validation.
			Exception ValidateProblem = this.performValidateType(pContext, T, pParameterBaseTypeToIgnore);
			if(ValidateProblem != null)
				return ValidateProblem;
			
			// Do initialization
			TypeKind  TK                = T.getTypeKind();
			Exception InitializeProblem = TK.initializeType(pContext, T);
			if(InitializeProblem != null)
				return InitializeProblem;
			
			// Execute all initialize expression.
			Expression[] Exprs = TK.getTypeInitializeExpressions(pContext, T);
			for(int i = 0; i < ((Exprs == null) ? 0 : Exprs.length); i++)
				pContext.getExecutor().execInternal(pContext, Exprs[i]);
			
			// Initialize the type elements
			T.initializeElements(pContext, this.TheEngine);
			
			// The type is now initialized.
			T.TSpec.TypeStatus = TypeSpec.Status.Initialized;
			
			if(!this.JustInitializedTypes.contains(T))
				this.JustInitializedTypes.add(T);		
			
		} finally {
			// Mask as not being resolved.
			this.BeingInitializedTypes.remove(T);
			
			// Do post initialization validation
			if(this.JustInitializedTypes.size() != 0) {
				try {
					for(Type TheT : this.JustInitializedTypes) {
						if(TheT == null) continue;
						if(this.BeingValidateTypePostInitializationTypes.contains(TheT)) continue;
						Exception Exc = null;
						try {
							this.BeingValidateTypePostInitializationTypes.add(TheT);
							                Exc = TheT.getTypeKind().doValidateTypeInterfaceImplementation(pContext, TheT);
							if(Exc == null) Exc = TheT.getTypeKind().doValidateTypePostInitialization     (pContext, TheT);
						} catch(Exception E) {
							Exc = E;
						} finally{
							this.BeingValidateTypePostInitializationTypes.remove(TheT);
						}
						if(Exc == null) continue;
							
						// Found a problem
						for(Type TheT2 : this.JustInitializedTypes) {
							// Mark all type in this set to be unusable
							TheT2.TSpec.TypeStatus = TypeSpec.Status.Unloaded;
							TheT2.TSpec.Ref.setTheType(null);
						}
							
						return Exc;
					}
				} finally {
					this.JustInitializedTypes.clear();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Validates an type from the type ref, this method ensure first that the type has been validated.
	 * @return	Exception	Any error that occurs or null if not any.
	 **/
	private Exception validateType(Context pContext, TypeRef pRef, TypeRef pParameterBaseTypeToIgnore) {
		if(pRef == null) return null;

		// Ensure Resolved
		Exception ResolveProblem = this.performResolveType(pContext, pRef);
		if(ResolveProblem != null)
			return ResolveProblem;

		// Validate
		Exception ValidateProblem = this.performValidateType(pContext, pRef.getTheType(), pParameterBaseTypeToIgnore);
		if(ValidateProblem != null)
			return ValidateProblem;
		
		return null;
	}
	
	/**
	 * Initializes an type from the type ref, this method ensure first that the type has been resolved.
	 * @return	Exception	Any error that occurs or null if not any.
	 **/
	private Exception initializeType(Context pContext, TypeRef pRef, TypeRef pParameterBaseTypeToIgnore) {
		if(pRef == null) return null;
		
		// Ensure validate
		Exception ValidateProblem = this.validateType(pContext, pRef, pParameterBaseTypeToIgnore);
		if(ValidateProblem != null)
			return ValidateProblem;
			
		Type T = pRef.getTheType();
		if(!T.isInitialized())
			return this.performInitializeType(pContext, T, pParameterBaseTypeToIgnore);
		// Already Initialized, so done
		return null;
	}

	// External Services ----------------------------------------------------------------

	// Resolve ----------------------------------------------------------------
	// Ensure that the type exist

	/** Ensure that the type referred by pTypeRef exist (resolved) */
	void ensureTypeExist(Context pContext, TypeRef pRef) {
		if(pRef == null) return;
		
		Exception ResolveProblem = this.performResolveType(pContext, pRef);
		if(ResolveProblem != null) {
			// An exception has been thrown
			throw new CurryError(
					String.format("There is an error loading a type (%s). <MType:865>", pRef),
					pContext, ResolveProblem
				);
		}
		
		Type T = pRef.getTheType();
		if((T == null) || !T.isResolved()) {
			if((T == null) || !T.isLoaded() || 
				(
					!this.BeingResolvedTypes_Map.containsKey(T.getTypeRef().toString()) &&
					!this.BeingResolvedTypes_Set.contains(   T)
				)
			) {
				// No error was report but the type is still not ready
				throw new CurryError(
						String.format("There is an unknown error loading a type (%s). <MType:874>", pRef),
						pContext
					);
			}
		}
	}

	// Validate ---------------------------------------------------------------
	// Ensure that the type can be used with this engine
	
	/** Ensure that the type referred by pTypeRef is valided (ready to be used for checking compatibility) */
	public void ensureTypeValidated(TypeRef pRef) {
		this.ensureTypeValidated(null, pRef, null);
	}
	/** Ensure that the type referred by pTypeRef is valided (ready to be used for checking compatibility) */
	void ensureTypeValidated(Context pContext, TypeRef pRef, TypeRef pParameterBaseTypeToIgnore) {
		if(pRef == null) return;
		
		// Ensure it exists first
		this.ensureTypeExist(pContext, pRef);
		
		Type T = pRef.getTheType();
		if(T == null) return;
		
		// Ensure it is validated
		this.ensureTypeValidated(pContext, T, pParameterBaseTypeToIgnore);
	}
	
	/** Ensure that the type referred by pTypeRef is valided (ready to be used for checking compatibility) */
	public void ensureTypeValidated(Type pType) {
		this.ensureTypeValidated(null, pType, null);
	}
	/** Ensure that the type referred by pTypeRef is valid (ready to be used for checking compatibility) */
	void ensureTypeValidated(Context pContext, Type pType, TypeRef pParameterBaseTypeToIgnore) {
		if(pType == null)
		    return;
		
		Exception ValidateProblem = this.performValidateType(pContext, pType, pParameterBaseTypeToIgnore);
		if(ValidateProblem != null) {
			// An exception has been thrown
			throw new CurryError(
					String.format("There is an error loading a type (%s). <MType:865>", pType),
					pContext, ValidateProblem
				);
		}
		
		if(!pType.isResolved()) {
			// No error was report but the type is still not ready
			throw new CurryError(
					String.format("There is an unknown error loading a type (%s). <MType:874>", pType),
					pContext
				);
		}
	}
	
	// Initialize ----------------------------------------------------
	// Ensure the type is ready to create an instance
	
	/** Prefix for type initialization error */
	static public final String TypeInitializationErrorPrefix = "Type Initialization Error: ";
	
	/** Ensure that the type is initialized */
	public void ensureTypeInitialized(Type pType)   {
		this.ensureTypeInitialized(null, pType.getTypeRef(), null);
	}
	/** Ensure that the type referred by pRef is initialized */
	public void ensureTypeInitialized(TypeRef pRef) {
		this.ensureTypeInitialized(null, pRef, null);
	}
	/** Ensure that the type referred by pRef is initialized */
	public void ensureTypeInitialized(TypeRef pRef, TypeRef pParameterBaseTypeToIgnore) {
		this.ensureTypeInitialized(null, pRef, pParameterBaseTypeToIgnore);
	}

	/** Ensure that the type referred by pRef is initialized */
	void ensureTypeInitialized(Context pContext, Type pType) {
		this.ensureTypeInitialized(pContext, pType.getTypeRef(), null);
	}
	/** Ensure that the type referred by pRef is initialized */
	void ensureTypeInitialized(Context pContext, TypeRef pRef) {
		this.ensureTypeInitialized(pContext, pRef, null);
	}
	/** Ensure that the type referred by pRef is initialized */
	void ensureTypeInitialized(Context pContext, TypeRef pRef, TypeRef pParameterBaseTypeToIgnore) {
		Type T = null;
		if((pRef == null) || (pRef.isLoaded() && (T = pRef.getTheType()).isInitialized())) return;
		if((T != null) && this.BeingInitializedTypes.contains(T))                          return;
		
		Exception InitializeProblem = this.initializeType(pContext, pRef, pParameterBaseTypeToIgnore);
		if(InitializeProblem != null) {
			// An exception has been thrown
			throw new CurryError(
				String.format(
					"Type Initialization Error: There is an error initializing a type (%s) <MType:904>.",
					pRef
				),
				pContext, InitializeProblem
			);
		}		
		
		if(!pRef.isLoaded() || !pRef.getTheType().isInitialized()) {
			// No error was report but the type is still not ready
			throw new CurryError(
				String.format(
					"Type Initialization Error: There is an unknown error initializing a type (%s).<MType:912>",
					pRef
				),
				pContext
			);
		}
	}
	
	// Get Type -------------------------------------------------------------------------

	// An error should be throw when not accessible
	/** Checks if the given context have sufficient permission to access to the Type  */
	void checkPermissionOfType(Context pContext, Type T) {
		if(!(T.getTypeRef() instanceof TLPackage.TRPackage_Internal)) return;
		TLPackage.TRPackage_Internal TRPI = (TLPackage.TRPackage_Internal)T.getTypeRef();
		if(TRPI.PAccess.isAllowed(pContext, T, null)) return;
		
		// Report as error
		throw new CurryError("Access Permission Error: The current context does not have permission to access '"
				+T.toString()+"'.", pContext);
	}
	
	// From TypeRef -----------------------------------------------------------
	
	/** Returns the type from the ref */
	public Type getTypeFromRef(TypeRef TR) {
		return this.getTypeFromRef(null, TR);
	}

	/** Returns the type from the ref with permission checking */
	Type getTypeFromRef(Context pContext, TypeRef TR) {
		Type T = this.getTypeFromRefNoCheck(pContext, TR);
		this.checkPermissionOfType(pContext, T);
		return TR.getTheType();
	}
	
	/** Returns the type from the ref without permission checking */
	Type getTypeFromRefNoCheck(Context pContext, TypeRef TR) {
		if(TR == null) return TKJava.TVoid;
		
		Type T = TR.getTheType();
		if((T != null) && T.isInitialized()) 
			return TR.getTheType();
		
		this.ensureTypeExist(pContext, TR);
		return TR.getTheType();
	}
	
	// From Object ------------------------------------------------------------
	
	/** Returns the type of the object pObj */
	public Type getTypeOf(Object pObj) {
		return this.getTypeOf(null, pObj);
	}

	/** Returns the type of the object pObj */	
	Type getTypeOf(Context pContext, Object pObj) {
		Type T = this.getTypeOfNoCheck(pContext, pObj);
		this.checkPermissionOfType(pContext, T);
		return T;
	}

	/** Returns the type of the object pObj without permission checking */	
	Type getTypeOfNoCheck(Context pContext, Object pObj) {
		Type T = this.getTypeOf_Raw(pContext, pObj);
		this.ensureTypeExist(pContext, T.getTypeRef());
		return T;
	}

	/** Actually search for an appropriate type of the given obejct */
	private Type getTypeOf_Raw(Context pContext, Object pObj) {
		if(pObj instanceof TypedData) return ((TypedData)pObj).getTheType();
		
		if(pObj instanceof Type) {
			TKType TKT = ((TKType)this.getTypeKind(TKType.KindName));
			return TKT.getTypeSpecOfTypeOf(((Type)pObj).getTypeRef()).getTypeRef().getTheType();
		}
		
		// Unable to extract
		if(pObj == null) return TKJava.TAny;
		// Check the native types first
		for(Type T : this.PrimitiveTypes) {
			if(T == null) continue;
			if(T == TKJava.TAny)  continue;
			if(T == TKJava.TVoid) continue;
			
			Class<?> Cls = T   .getDataClass();
			Class<?> C   = pObj.getClass();
			if(C.isPrimitive()) {	// This is a Java primitive not script primitive
				if(     C == Boolean  .TYPE) C = Boolean.class;
				else if(C == Character.TYPE) C = Character.class;
				else if(C == Byte     .TYPE) C = Byte.class;
				else if(C == Short    .TYPE) C = Short.class;
				else if(C == Integer  .TYPE) C = Integer.class;
				else if(C == Long     .TYPE) C = Long.class;
				else if(C == Float    .TYPE) C = Float.class;
				else if(C == Double   .TYPE) C = Double.class;
			}
			if(Cls == C) return T;
		}
		
		// From this point on, a valid context is needed
		if(pContext == null) pContext = this.TheEngine.newRootContext();
		
		// Check each kind - Start from non-virtual
		Collection<TypeKind> EKs = this.TypeKinds.values();
		for(TypeKind TK : EKs) {
			if(TK == TKJava.Instance)  continue;
			if(TK.isVirtual(pContext)) continue;
			
			Type T = TK.getTypeOf(pContext, pObj);
			if(T != null) return T;
		}
		// Follow by the virtual one
		for(TypeKind TK : EKs) {
			if(TK == TKJava.Instance)  continue;
			if(!TK.isVirtual(pContext)) continue;
			
			Type T = TK.getTypeOf(pContext, pObj);
			if(T != null) return T;
		}
		return TKJava.Instance.getTypeOf(pContext, pObj);
	}
	
	// From Class -------------------------------------------------------------

	/** Returns the type of the object in the class pCls */
	public Type getTypeOfTheInstanceOf(Class<?> pCls) {
		return this.getTypeOfTheInstanceOf(null, pCls);
	}

	/** Returns the type of the object in the class pCls */	
	Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		Type T = this.getTypeOfTheInstanceOfNoCheck(pContext, pCls);
		this.checkPermissionOfType(pContext, T);
		return T;
	}

	/** Returns the type of the object in the class pCls without checking its permission */
	Type getTypeOfTheInstanceOfNoCheck(Context pContext, Class<?> pCls) {
		Type T = this.getTypeOfTheInstanceOf_Raw(pContext, pCls);
		this.ensureTypeExist(pContext, T.getTypeRef());
		return T;
	}

	/** Actually search for an appropriate type associating with the given class */
	private Type getTypeOfTheInstanceOf_Raw(Context pContext, Class<?> pCls) {
		// Unable to extract
		if((pCls == null) || (pCls == void.class)) return TKJava.TVoid;
		
		// Check the native types first
		Class<?> C = pCls;
		if(C.isPrimitive()) {	// This is a Java primitive not script primitive
			if(     C == Boolean  .TYPE) C = Boolean.class;
			else if(C == Character.TYPE) C = Character.class;
			else if(C == Byte     .TYPE) C = Byte.class;
			else if(C == Short    .TYPE) C = Short.class;
			else if(C == Integer  .TYPE) C = Integer.class;
			else if(C == Long     .TYPE) C = Long.class;
			else if(C == Float    .TYPE) C = Float.class;
			else if(C == Double   .TYPE) C = Double.class;
		}
		for(int i = this.PrimitiveTypes.size(); --i >= 0;) {
			Type T = this.PrimitiveTypes.get(i);
			if(T == TKJava.TAny)  continue;
			if(T == TKJava.TVoid) continue;
			
			if(this.PrimitiveClasses.get(i) == C)
				return T;
		}
		
		if(pContext == null) pContext = this.TheEngine.newRootContext();
		// Check each kind
		Collection<TypeKind> EKs = this.TypeKinds.values();
		for(TypeKind TK : EKs) {
			if(TK == TKJava.Instance) continue;
			
			Type T = TK.getTypeOfTheInstanceOf(pContext, pCls);
			if(T != null) return T;
		}
		return TKJava.Instance.getTypeOfTheInstanceOf(pContext, pCls);
	}
	
	// Annonymous array -----------------------------------------------------------------

	/** Returns a type of an anonymous array type of the given type and length */
	public Type getAnnonymousArrayTypeOf(Type pContainType, int pLength) {
		if(pContainType == null) pContainType = TKJava.TAny;
		return ((TKArray)this.getTypeKind(TKArray.KindName)).newArrayType(null, pContainType.getTypeRef(), pLength);
	}
	/** Returns a type of an anonymous array type of the given type */
	public Type getAnnonymousArrayTypeOf(Type pContainType) {
		if(pContainType == null) pContainType = TKJava.TAny;
		return ((TKArray)this.getTypeKind(TKArray.KindName)).newArrayType(null, pContainType.getTypeRef(), -1);
	}
	/** Returns a type of an anonymous array type of the given type and length */
	public Type getAnnonymousArrayTypeOf(TypeRef pContainTypeRef, int pLength) {
		if(pContainTypeRef == null) pContainTypeRef = TKJava.TAny.getTypeRef();
		this.ensureTypeInitialized(pContainTypeRef);
		return ((TKArray)this.getTypeKind(TKArray.KindName)).newArrayType(null, pContainTypeRef, pLength);
	}
	/** Returns a type of an anonymous array type of the given type */
	public Type getAnnonymousArrayTypeOf(TypeRef pContainTypeRef) {
		if(pContainTypeRef == null) pContainTypeRef = TKJava.TAny.getTypeRef();
		this.ensureTypeInitialized(pContainTypeRef);
		return ((TKArray)this.getTypeKind(TKArray.KindName)).newArrayType(null, pContainTypeRef, -1);
	}
	
	// Get type from string (Search TypeRef) ---------------------------------------------------------------------------
	
	/** Returns the TypeRef of a Curry Type */
	final protected TypeRef doGetCurryTypeRef(String PName, String TName) {
		// Find in the unit first.
		MUnit UManager = this.TheEngine.getUnitManager();
		if(UManager == null) return null;
		
		if(MType.PREDEFINEDTYPE_PACKAGENAME.equals(PName)) {
			TypeRef TR = UManager.getTypeManager().getPrefineTypeRef(TName);
			if(TR != null) return TR;
		}
		
		// Get from the already loaded package
		Package P = UManager.getPackage(PName);
		if(P != null) {
			TypeSpec TS = P.getTypeSpec(TName);
			if(TS != null) return TS.getTypeRef();
		}
		// Get from the being-build package
		PackageBuilder PB = UManager.getPackageBuilder(null, PName);
		if(PB != null) {
			TypeBuilder TB = PB.getTypeBuilder(TName);
			if(TB != null) return TB.getTypeRef();
		}
		
		return null;
	}

	/** Returns the TypeRef of a Java Type */
	final protected TypeRef doGetJavaTypeRef(String TFullName) {	
		// Find it as a whole for Java class
		try {
			Engine $Engine = this.TheEngine;
			// Load the class with the engine current class loader
			Class<?> Cls = UClass.getClassByName_WithException(
			                   TFullName,
			                   $Engine.getClassPaths().getJavaCompiler().getCurrentClassLoader());
			
			// Create the type from the class
			Type TheType = TKJava.Instance.getTypeByClass($Engine, null, Cls);
			if(TheType != null) return TheType.getTypeRef();
				
		} catch(ClassNotFoundException E) { /* Do nothing */ }		
		
		return null;
	}

	/** Returns the TypeRef of a Java Type */
	final protected TypeRef doGetJavaTypeRef(String PName, String TName) {
		return this.doGetJavaTypeRef(PName + "." + TName);
	}
	
	/** Returns the TypeRef of a Curry Type */
	final public TypeRef getCurryTypeRef(String PName, String TName) {
		if(PName == null) return null;
		if(TName == null) return null;
		
		// Pre-Process
		PName = PName.trim();
		if(PName.length() == 0) return this.doGetTypeRef(TName);
		
		// Pre-Process
		TName = TName.trim();
		// Early return
		if(TName.length() == 0) return null;
		
		return this.doGetCurryTypeRef(PName, TName);
	}

	/** Returns the TypeRef of a Java Type */
	final public TypeRef getJavaTypeRef(String PName, String TName) {
		if(PName == null) return null;
		if(TName == null) return null;
		
		// Pre-Process
		PName = PName.trim();
		if(PName.length() == 0) return this.doGetTypeRef(TName);
		
		// Pre-Process
		TName = TName.trim();
		// Early return
		if(TName.length() == 0) return null;
		
		return this.doGetJavaTypeRef(PName, TName);
	}
	
	/**
	 * Returns TypeRef of a type in the  associated with the given type Name.
	 * 
	 * This method will search in primitive class, classes in java.lang and commonly used class (see TKJava). Then find
	 *      in the default package.
	 **/
	final protected TypeRef doGetTypeRef(String TName) {
		// Early return
		if((TName == null) || (TName.length() == 0)) return null;
		
		// Pre-Process
		TName = TName.trim();
		// Early return
		if(TName.length() == 0) return null;
		
		switch(TName.charAt(0)) {
			case 'a': if("any"    .equals(TName)) return TKJava.TAny      .getTypeRef(); break;
			case 'b': if("bool"   .equals(TName)) return TKJava.TBoolean  .getTypeRef();
	                  if("boolean".equals(TName)) return TKJava.TBoolean  .getTypeRef();
	                  if("byte"   .equals(TName)) return TKJava.TByte     .getTypeRef(); break;
			case 'c': if("char"   .equals(TName)) return TKJava.TCharacter.getTypeRef(); break;
			case 'f': if("float"  .equals(TName)) return TKJava.TFloat    .getTypeRef(); break;
			case 'i': if("int"    .equals(TName)) return TKJava.TInteger  .getTypeRef(); break;
			case 'l': if("long"   .equals(TName)) return TKJava.TLong     .getTypeRef(); break;
			case 'n': if("number" .equals(TName)) return TKJava.TNumber   .getTypeRef(); break;
			case 's': if("short"  .equals(TName)) return TKJava.TShort    .getTypeRef(); break;
			case 'v': if("void"   .equals(TName)) return TKJava.TVoid     .getTypeRef(); break;
			
			
			case 'A': if("Any"              .equals(TName)) return TKJava.TAny              .getTypeRef();
			          if("Appendable"       .equals(TName)) return TKJava.TAppendable       .getTypeRef(); break;
			case 'B': if("Boolean"          .equals(TName)) return TKJava.TBoolean          .getTypeRef();
			          if("BigInteger"       .equals(TName)) return TKJava.TBigInteger       .getTypeRef();
			          if("BigDecimal"       .equals(TName)) return TKJava.TBigDecimal       .getTypeRef();
			          if("Byte"             .equals(TName)) return TKJava.TByte             .getTypeRef(); break; 
			case 'C': if("Character"        .equals(TName)) return TKJava.TCharacter        .getTypeRef();
			          if("CharSequence"     .equals(TName)) return TKJava.TCharSequence     .getTypeRef();
			          if("Class"            .equals(TName)) return TKJava.TClass            .getTypeRef();
			          if("ClassLoader"      .equals(TName)) return TKJava.TClassLoader      .getTypeRef();
			          if("Cloneable"        .equals(TName)) return TKJava.TCloneable        .getTypeRef();
			          if("Comparable"       .equals(TName)) return TKJava.TComparable       .getTypeRef(); break;
			case 'E': if("Engine"           .equals(TName)) return TKJava.TEngine           .getTypeRef();
			          if("Enum"             .equals(TName)) return TKJava.TEnum             .getTypeRef();
			          if("Error"            .equals(TName)) return TKJava.TError            .getTypeRef();
			          if("Exception"        .equals(TName)) return TKJava.TException        .getTypeRef(); break;
			case 'F': if("Float"            .equals(TName)) return TKJava.TFloat            .getTypeRef(); break;
			case 'I': if("Integer"          .equals(TName)) return TKJava.TInteger          .getTypeRef(); break;
			case 'L': if("Long"             .equals(TName)) return TKJava.TLong             .getTypeRef(); break;
			case 'M': if("Math"             .equals(TName)) return TKJava.TMath             .getTypeRef(); break;
			case 'N': if("Number"           .equals(TName)) return TKJava.TNumber           .getTypeRef(); break;
			case 'O': if("Object"           .equals(TName)) return TKJava.TAny              .getTypeRef(); break;
			case 'P': if("Package"          .equals(TName)) return TKJava.TPackage          .getTypeRef();
			          if("Process"          .equals(TName)) return TKJava.TProcess          .getTypeRef();
			          if("ProcessBuilder"   .equals(TName)) return TKJava.TProcessBuilder   .getTypeRef(); break;
			case 'R': if("Readable"         .equals(TName)) return TKJava.TReadable         .getTypeRef();
			          if("Runnable"         .equals(TName)) return TKJava.TRunnable         .getTypeRef();
			          if("Runtime"          .equals(TName)) return TKJava.TRuntime          .getTypeRef();
			          if("RuntimePermission".equals(TName)) return TKJava.TRuntimePermission.getTypeRef(); break;
			case 'S': if("Serializable"     .equals(TName)) return TKJava.TSerializable     .getTypeRef();
			          if("Short"            .equals(TName)) return TKJava.TShort            .getTypeRef();
			          if("StackTraceElement".equals(TName)) return TKJava.TStackTraceElement.getTypeRef();
			          if("StrictMath"       .equals(TName)) return TKJava.TStrictMath       .getTypeRef();
			          if("String"           .equals(TName)) return TKJava.TString           .getTypeRef();
			          if("StringBuffer"     .equals(TName)) return TKJava.TStringBuffer     .getTypeRef();
			          if("StringBuilder"    .equals(TName)) return TKJava.TStringBuilder    .getTypeRef();
			          if("System"           .equals(TName)) return TKJava.TSystem           .getTypeRef();
			          if("SecurityManager"  .equals(TName)) return TKJava.TSecurityManager  .getTypeRef(); break;
			case 'T': if("Thread"           .equals(TName)) return TKJava.TThread           .getTypeRef();
			          if("ThreadGroup"      .equals(TName)) return TKJava.TThreadGroup      .getTypeRef();
			          if("ThreadLocal"      .equals(TName)) return TKJava.TThreadLocal      .getTypeRef();
			          if("Throwable"        .equals(TName)) return TKJava.TThrowable        .getTypeRef();
			          if("Type"             .equals(TName)) return TKJava.TType             .getTypeRef();
			          if("TypeRef"          .equals(TName)) return TKJava.TTypeRef          .getTypeRef(); break;
			case 'V': if("Void"             .equals(TName)) return TKJava.TVoid             .getTypeRef(); break;
		}

		TypeRef TRef = null;
		if((TRef = this.doGetCurryTypeRef(EngineExtensions.EE_DefaultPackage.DefaultPackageName, TName)) != null) return TRef;
		if((TRef = this.doGetJavaTypeRef ("java.lang",                                           TName)) != null) return TRef;
		
		return null;
	}
	
    /** Gets Java Type by the class */
    public TypeRef getTypeRefByClass(Engine pEngine, Class<?> pCls) {
        final Type aType = TKJava.Instance.getTypeByClass(pEngine, null, pCls);
        if (aType == null)
            return null;
        return aType.getTypeRef();
    }
	
	/**
	 * Returns TypeRef of a type associated with the given type Name and package name.
	 * 
	 * If the package name is null, null will be returned.
	 * If the package name is empty, the search will be to the default package name or often used class (see TKJava)
	 **/
	final public TypeRef searchTypeRef(String PName, String TName) {
		if(PName == null) return null;
		if(TName == null) return null;
		
		// Pre-Process
		PName = PName.trim();
		if(PName.length() == 0) return this.doGetTypeRef(TName);
		
		// Pre-Process
		TName = TName.trim();
		// Early return
		if(TName.length() == 0) return null;

		TypeRef TRef = null;
		if((TRef = this.doGetCurryTypeRef(PName, TName)) != null) return TRef;
		if((TRef = this.doGetJavaTypeRef (PName, TName)) != null) return TRef;
		// not found
		return null;
	}
	
	/** Looks for and Returns the TypeRef of the type whose name associated with pTypeFullName. */
	public TypeRef searchTypeRef(String pSearchName) {
		if((pSearchName == null) || ((pSearchName = pSearchName.trim()).length() == 0)) return null;
		
		int     Index;
		TypeRef TRef;
		String  PName = "";
		String  TName = "";
		if((Index = pSearchName.lastIndexOf(".")) != -1) {
			PName = pSearchName.substring(0, Index);
			TName = pSearchName.substring(Index + 1);

			if((TRef = this.doGetJavaTypeRef(PName, TName)) != null)
				return TRef;
				
		} else if((Index = pSearchName.lastIndexOf("=>")) != -1) {
			PName = pSearchName.substring(0, Index);
			TName = pSearchName.substring(Index + 2);
			
			if((TRef = this.doGetCurryTypeRef(PName, TName)) != null)
				return TRef;
			
		} else return this.doGetTypeRef(TName);
		
		// Find the Curry way
		if(!PName.contains(".") && (TRef = this.doGetCurryTypeRef(PName, TName)) != null)
			return TRef;

		// Find the Java way
		if((TRef = this.doGetJavaTypeRef(PName, TName)) != null)
			return TRef;

		if((TRef = this.doGetJavaTypeRef(pSearchName)) != null)
			return TRef;

		return null;
	}
	
	/**
	 * Looks for and Returns the TypeRef of the type whose name associated with pTypeFullName.
	 * 
	 * @param	pImports			is the list of import strings. The import strings may ends with '.' if the search 
	 *                                 should look in side it.
	 * @param	pLocalPackageName	is the name of the local Package (so the type may omit the package name)
	 * @param	pLocalTypeName		is the name of the local Type (so the type may parameterized if the local type)
	 **/
	public TypeRef searchTypeRef(List<String> pImports, String pLocalPackageName, TypeRef pLocalTRef, String pSearchName) {
		if((pSearchName == null) || ((pSearchName = pSearchName.trim()).length() == 0)) return null;
		
		int DotIndex;
		// No type separater in the name
		if(((DotIndex = pSearchName.indexOf('.')) == -1) && (pSearchName.indexOf('=') == -1)) {
			
			// Try to find in the Parameterization Type
			if(pLocalTRef != null) {
				// Try to resolve to get the parameter type
				if(!pLocalTRef.isLoaded()) {
					try { this.ensureTypeExist(null, pLocalTRef); }
					catch (Exception e) {}
				}
				if(pLocalTRef.isLoaded()) {
					// If the type is parameterized, find the parameter name
					TypeSpec TS = pLocalTRef.getTheType().getTypeSpec();
					if(TS.isParameterized() && TS.getParameterizedTypeInfo().containParameterTypeRef(pSearchName)) {
						// So it is a parameter Type
						return new TRParameter(pLocalTRef, pSearchName); 
					}
				}
			}

			TypeRef TRef;
			
			// Try to see if the type name is in the import value
			for(int i = 0; i < ((pImports == null)?0:pImports.size()); i++) {
				String Import = pImports.get(i);
				if(Import == null) continue;
				
				// The one that ends with '.' is the one with wild card
				boolean EndsWithDot;
				if((EndsWithDot = Import.endsWith(".")) || Import.endsWith("=>")) {
					String PName = Import.substring(0, Import.length() - (EndsWithDot?1:2));

					// Find the Curry way
					if(!PName.contains(".") && (TRef = this.doGetCurryTypeRef(PName, pSearchName)) != null)
						return TRef;

					// Find the Java way
					if((TRef = this.doGetJavaTypeRef(PName, pSearchName)) != null)
						return TRef;
					
				} else {	// No wild card, find the exact match after the last separater
				
					int    Index;
					String TName = "";
					String PName = "";
					if(     (Index = Import.lastIndexOf('.'))  != -1) { TName = Import.substring(Index + 1); PName = Import.substring(0, Index); }
					else if((Index = Import.lastIndexOf("=>")) != -1) { TName = Import.substring(Index + 2); PName = Import.substring(0, Index); }
					
					if(TName.equals(pSearchName)) {	// The type name match
						// Find the Curry way
						if(!PName.contains(".") && (TRef = this.doGetCurryTypeRef(PName, pSearchName)) != null)
							return TRef;

						// Find the Java way
						if((TRef = this.doGetJavaTypeRef(PName, pSearchName)) != null)
							return TRef;
					}
				}
			}
			
			// Try to find the type with the name in the current package
			if((pLocalPackageName != null) && (TRef = this.doGetCurryTypeRef(pLocalPackageName, pSearchName)) != null)
				return TRef;

			// Try to find the type with the name in the default package
			if((TRef = this.doGetCurryTypeRef(EngineExtensions.EE_DefaultPackage.DefaultPackageName, pSearchName)) != null)
				return TRef;
			
			// Search Java type or often used one
			if((TRef = this.doGetTypeRef(pSearchName)) != null)
				return TRef;
			
			if((TRef = this.getPrefineTypeRef(pSearchName)) != null)
				return TRef;
		}
		
		String[][] IStrss     = null;
		Boolean[]  IsWildCard = null;
		// Try to see if the type name is in the import value
		for(int i = 0; i < ((pImports == null)?0:pImports.size()); i++) {
			String Import = pImports.get(i);
			if(Import == null) continue;
			
			// Exact match
			if(Import.equals(pSearchName)) {
				TypeRef TRef = this.searchTypeRef(pSearchName);
				if(TRef != null) return TRef;
			}
			
			if(IsWildCard    == null) IsWildCard    = new Boolean[pImports.size()];
			if(IsWildCard[i] == null) IsWildCard[i] = (Import.endsWith(".") || Import.contains("=>"));
			
			// Overlap
			if((DotIndex != -1) && Boolean.FALSE.equals(IsWildCard[i])) {
				if(IStrss    == null) IStrss    = new String[pImports.size()][];
				if(IStrss[i] == null) IStrss[i] = Import.split("\\.");
				
				String[] IStrs = IStrss[i];
				String[] NStrs = pSearchName.split("\\.");
				// Overlap
				if(IStrs[IStrs.length - 1].equals(NStrs[0])) {
					// Combine and search as a hole
					String TypeFullName = Import + pSearchName.substring(DotIndex);
					TypeRef TRef = this.searchTypeRef(TypeFullName);
					if(TRef != null) return TRef;
				}
			}
			
			// Wild card
			if(Boolean.TRUE.equals(IsWildCard[i])) {
				// Combine and search as a hole
				TypeRef TRef = this.searchTypeRef(Import + pSearchName);
				if(TRef != null) return TRef;
			}
		}
		
		return this.searchTypeRef(pSearchName);
	}
	
	// Information -----------------------------------------------------------------------------------------------------
	
	/** Returns the name of the type kind that this type ref is referring */
	public String getTypeKindNameOf(TypeRef pRef) {
		TypeSpec TS = TypeRef.getTypeSpecOf(this.TheEngine, null, pRef);
		if(TS == null) return null;
		return TS.getKindName();
	}
	
	/** Returns the DataClass of the given TypeRef */
	public Class<?> getDataClassOf(TypeRef pRef) {
		if(pRef == null) return null;
		if(!pRef.isLoaded()) this.ensureTypeInitialized(pRef);
		return pRef.getTheType().getDataClass();
	}
	
	/** Returns the DataClass of the given TypeRef */
	Class<?> getDataClassOf(Context pContext, TypeRef pRef) {
		if(pRef == null) return null;
		if(pRef.isLoaded()) return pRef.getTheType().getDataClass();
		String TKName = this.getTypeKindNameOf(pRef);
		if(TKName == null) return null;
		TypeKind TK = this.getTypeKind(TKName);
		if(TK == null) return null;
		return TK.getTypeDataClass(pContext, TypeRef.getTypeSpecOf(this.TheEngine, pContext, pRef));
	}
	
	/** Returns the TypeClass of the given TypeRef */
	public Class<? extends Type> getTypeClassOf(TypeRef pRef) {
		if(pRef == null) return null;
		this.ensureTypeExist(null, pRef);
		return pRef.getTheType().getClass();
	}

	/** Returns the TypeClass of the given TypeRef */
	public Class<? extends Type> getTypeClassOf(Context pContext, TypeRef pRef) {
		if(pRef == null) return null;
		this.ensureTypeExist(pContext, pRef);
		return pRef.getTheType().getClass();
	}
	
	// Type Compatibility checking -------------------------------------------------------------------------------------
	
	// Check general compatibility (equals, null, Any, Void), Java type, and primitive type-parameter checking
	/**
	 * Checks if tht type ByRef can be assigned into a variable of the type TheRef -  This method will only check using
	 * TypeRef and nothing else.
	 **/
	static final public Boolean MightTypeRefByAssignableByInstanceOf(TypeRef TheRef, TypeRef ByRef) {
		return MightTypeRefByAssignableByInstanceOf(null, null, TheRef, ByRef);
	}
	
	// Check general compatibility (equals, null, Any, Void), Java type, and primitive type-parameter checking
	/**
	 * Checks if tht type ByRef can be assigned into a variable of the type TheRef -  This method will only check using
	 * TypeRef and nothing else.
	 **/
	static final public Boolean MightTypeRefByAssignableByInstanceOf(Context pContext, Engine pEngine, TypeRef TheRef, TypeRef ByRef) {
		// Early returns
		if(TheRef == ByRef)                          return  true;
		if((TheRef == null) || (ByRef == null))      return false;
		if(TheRef.equals(ByRef))                     return  true;
		if(TheRef.equals(TKJava.TVoid.getTypeRef())) return false;
		if(TheRef.equals(TKJava.TAny .getTypeRef())) return !ByRef.equals(TKJava.TVoid.getTypeRef());
		
		// Check from the Cache
		TypeSpec ToRef_Spec = ByRef.getTypeSpecWithoutEngine();
		if(ToRef_Spec != null) {
			Boolean IsCompatible = ToRef_Spec.checkCanBeAssignedTo_InCache(TheRef);
			if(IsCompatible != null) return IsCompatible;
		}

		// Perform the checking
		Boolean IsCompatible = DoMightTypeRefByAssignableByInstanceOf(pContext, pEngine, TheRef, ByRef);
		if(IsCompatible == null) return null;
		
		// Save the result to the cache
		ToRef_Spec = ByRef.getTypeSpecWithoutEngine();
		if(ToRef_Spec != null) ToRef_Spec.addToCanBeAssignedToCache(TheRef, IsCompatible.booleanValue());
		
		return IsCompatible;
	}
	
	// Check general compatibility (equals, null, Any, Void), Java type, and primitive type-parameter checking
	/**
	 * Checks if tht type ByRef can be assigned into a variable of the type TheRef -  This method will only check using
	 * TypeRef and nothing else.
	 **/
	static final Boolean DoMightTypeRefByAssignableByInstanceOf(TypeRef TheRef, TypeRef ByRef) {
		return DoMightTypeRefByAssignableByInstanceOf(null, null, TheRef, ByRef);
	}
	
	// Check general compatibility (equals, null, Any, Void), Java type, and primitive type-parameter checking
	/**
	 * Checks if tht type ByRef can be assigned into a variable of the type TheRef -  This method will only check using
	 * TypeRef and nothing else.
	 **/
	static final Boolean DoMightTypeRefByAssignableByInstanceOf(Context pContext, Engine pEngine, TypeRef TheRef, TypeRef ByRef) {
		// In case of Java types, we can check right away
		Type TheType = TheRef.getTheType();
		Type ByType  = ByRef .getTheType();
		if((TheType instanceof TKJava.TJava) && (ByType instanceof TKJava.TJava)) {
			int SA = -1;
			if     (TheType == TKJava.TBigDecimal) SA = 10;
			else if(TheType == TKJava.TBigInteger) SA =  9;
			else if(TheType == TKJava.TDouble)     SA =  8;
			else if(TheType == TKJava.TLong)       SA =  7;
			else if(TheType == TKJava.TFloat)      SA =  6;
			else if(TheType == TKJava.TInteger)    SA =  5;
			else if(TheType == TKJava.TShort)      SA =  4;
			else if(TheType == TKJava.TByte)       SA =  3;

			int SB = -1;
			if     (ByType == TKJava.TBigDecimal) SB = 10;
			else if(ByType == TKJava.TBigInteger) SB =  9;
			else if(ByType == TKJava.TDouble)     SB =  8;
			else if(ByType == TKJava.TLong)       SB =  7;
			else if(ByType == TKJava.TFloat)      SB =  6;
			else if(ByType == TKJava.TInteger)    SB =  5;
			else if(ByType == TKJava.TShort)      SB =  4;
			else if(ByType == TKJava.TByte)       SB =  3;
			
			if((SA != -1) && (SB != -1)) {
				int Diff = SA - SB;
				return (Diff >= 0);
			}
			
			return TheType.getDataClass().isAssignableFrom(ByType.getDataClass());
		}
		
		// Parameted types - If ByRef is a parametered of TheRef
		if(ByRef instanceof TRParametered) {
			
			// but TheRef is not a parametered - it must be a parameterized to be valid
			if(!(TheRef instanceof TRParametered)) {

				TypeRef ByRef_Target = ((TRParametered)ByRef).getTargetTypeRef();
				
				Boolean IsCompatibleWithTarget = MightTypeRefByAssignableByInstanceOf(pContext, pEngine, TheRef, ByRef_Target);
				
				// False or null
				if(!Boolean.TRUE.equals(IsCompatibleWithTarget))
					return IsCompatibleWithTarget;
				
				// Determine the value to be returned if the checking that follow fails
				Boolean ValueIfFail = TheRef.equals(ByRef_Target) ? true : null;
				
				// See if the ref is parameterized
				TypeSpec TheRef_Spec = TheRef.getTypeSpecWithoutEngine();
				if(TheRef_Spec == null) return ValueIfFail;	// We do not know at the moment
				
				ParameterizedTypeInfo PTI = TheRef_Spec.getParameterizedTypeInfo();
				if(PTI == null) return ValueIfFail;	// The ref type is not a parameterized one

				TypeRef[] ByRef__Ps = ((TRParametered)ByRef).ParamTypeRefs;
				int       PCount    = PTI.getParameterTypeCount();
				// The number of the parameters are not the same
				if(PCount != ByRef__Ps.length) return ValueIfFail;

				// Checks if the parameters are compatible
				boolean IsSure = true;
				for(int i = 0; i < PCount; i++) {
					Boolean B = MightTypeRefByAssignableByInstanceOf(PTI.getParameterTypeRef(i), ByRef__Ps[i]);
					if     (B == null)               IsSure = false;
					else if(Boolean.FALSE.equals(B)) return ValueIfFail;
				}
				if(IsSure) return true;
				
				return ValueIfFail;
			}


			// They both are parameters -----------------------------------------------------------
			
			TypeRef TheRef_Target = ((TRParametered)TheRef).getTargetTypeRef();
			TypeRef ByRef_Target  = ((TRParametered)ByRef).getTargetTypeRef();
			Boolean IsComoparibleWithTarget = MightTypeRefByAssignableByInstanceOf(TheRef_Target, ByRef_Target);
			
			// False or null
			if(!Boolean.TRUE.equals(IsComoparibleWithTarget)) {
				if(IsComoparibleWithTarget == null) {
					TypeSpec TheSpec_Target = TheRef_Target.getTypeSpecWithoutEngine();
					TypeSpec BySpec_Target  = ByRef_Target .getTypeSpecWithoutEngine();
					IsComoparibleWithTarget = DoMightTypeSpecByAssignableByInstanceOf(pContext, pEngine, TheSpec_Target, BySpec_Target);
				}
				
				return IsComoparibleWithTarget;
			}
			
			// Checks the parameters
			
			TypeRef[] TheRef_Ps = ((TRParametered)TheRef).ParamTypeRefs;
			TypeRef[] ByRef__Ps = ((TRParametered)ByRef) .ParamTypeRefs;
			
			// Ensure the number of parameters
			if(((TheRef_Ps == null) || (ByRef__Ps == null)) && (TheRef_Ps != ByRef__Ps)) return false;
			if(TheRef_Ps.length != ByRef__Ps.length)                                     return false;

			// Checks if the parameters are compatible
			int     PCount = TheRef_Ps.length;
			boolean IsSure = true;
			for(int i = 0; i < PCount; i++) {
				Boolean B = MightTypeRefByAssignableByInstanceOf(TheRef_Ps[i], ByRef__Ps[i]);
				if     (B == null)               IsSure = false;
				else if(Boolean.FALSE.equals(B)) return false;
			}
			if(IsSure) return true;
		}
		
		return null;
	}  

	/** Checks if tht type BySpec can be assigned into a variable of the type BySpec. */
	static final public Boolean MightTypeSpecByAssignableByInstanceOf(Context pContext, Engine pEngine,
			TypeSpec TheSpec, TypeSpec BySpec) {
		
		// Early returns
		if(TheSpec == BySpec)                     return  true;
		if((TheSpec == null) || (BySpec == null)) return false;
		
		TypeRef TheRef = TheSpec.getTypeRef();
		TypeRef ByRef  = BySpec .getTypeRef();
		
		// Early returns
		if(TheRef == ByRef)                          return  true;
		if((TheRef == null) || (ByRef == null))      return false;
		if(TheRef.equals(ByRef))                     return  true;
		if(TheRef.equals(TKJava.TVoid.getTypeRef())) return false;
		if(TheRef.equals(TKJava.TAny .getTypeRef())) return !ByRef.equals(TKJava.TVoid.getTypeRef());

		// Check from the Cache
		TypeSpec ToRef_Spec = ByRef.getTypeSpecWithoutEngine();
		if(ToRef_Spec != null) {
			Boolean IsCompatible = ToRef_Spec.checkCanBeAssignedTo_InCache(TheRef);
			if(IsCompatible != null) return IsCompatible;
		}
		
		// Perform the checkingTheSpec
		Boolean IsCompatible = DoMightTypeSpecByAssignableByInstanceOf(pContext, pEngine, TheSpec, BySpec);
		if(IsCompatible == null) return null;
		
		// Save the result to the cache
		ToRef_Spec = ByRef.getTypeSpecWithoutEngine();
		if(ToRef_Spec != null) ToRef_Spec.addToCanBeAssignedToCache(TheRef, IsCompatible.booleanValue());
		
		return IsCompatible;
		
	}

	/** Checks if tht type BySpec can be assigned into a variable of the type BySpec. */
	static final Boolean DoMightTypeSpecByAssignableByInstanceOf(Context pContext, Engine pEngine,
			TypeSpec TheSpec, TypeSpec BySpec) {
		
		TypeRef TheSpec_TRef = TheSpec.getTypeRef();
		TypeRef BySpec_TRef  = BySpec .getTypeRef();

		// Check the type ref only    v--- Use Do as the early return should already been checked
		Boolean IsTypeRefCompatible = DoMightTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec_TRef, BySpec_TRef);
		if(IsTypeRefCompatible != null) return IsTypeRefCompatible;
		
		// The Super of ByType match with ThisRef -----------------------------
		TypeRef BySpec_SuperRef = BySpec.getSuperRef();
		if((BySpec_SuperRef != null) && !TKJava.TAny.getTypeRef().equals(BySpec_SuperRef)) {
			Boolean B = MayTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec_TRef, BySpec_SuperRef);
			if(Boolean.TRUE.equals(B)) return true;
		}
				
		// Get the Spec -------------------------------------------------------------
		if(TheSpec instanceof TSInterface) {
			// The Target Type must match -----------------------------------------------
			TypeRef TheSpec_TargetRef = ((TSInterface)TheSpec).getTargetRef();
			if((TheSpec_TargetRef != null) && !TKJava.TAny.getTypeRef().equals(TheSpec_TargetRef)) {
				Boolean B = MayTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec_TargetRef, BySpec_TRef);
				if(Boolean.FALSE.equals(B)) return false;
			}
		} else if(!(TheSpec instanceof TSJava))
			return null;
		else if (TheSpec.getTypeRef().getTheType() == null)
			return null;
		else if (!TheSpec.getTypeRef().getTheType().getDataClass().isInterface())
			return null;
		

		// If any interface of BySpec can may be assigned to TheSpec --------------------
		int ICount = BySpec.getInterfaceCount();
		for(int i = 0; i < ICount; i++) {
			TypeRef BySpec_InterfaceRef = BySpec.getInterfaceRefAt(i);
			if(BySpec_InterfaceRef == null) continue;
			
			// If the type can be assigned by one of the ByType's interface, It can
			Boolean B = MayTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec_TRef, BySpec_InterfaceRef);
			if(Boolean.TRUE.equals(B)) return true;
			
			if(B == null) {
				B = CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec_TRef, BySpec_InterfaceRef);
				if(Boolean.TRUE.equals(B)) return true;
			}
		}
		
		return null;
	}
	
	/**
	 * Checks if tht type ByRef can be assigned into a variable of the type TheRef - This method will try to determine
	 * the compatibility as much as it can.
	 **/
	static final public Boolean MayTypeRefByAssignableByInstanceOf(Context pContext, Engine pEngine,
			TypeRef TheRef, TypeRef ByRef) {
		// Early returns
		if(TheRef == ByRef)                          return  true;
		if((TheRef == null) || (ByRef == null))      return false;
		if(TheRef.equals(ByRef))                     return  true;
		if(TheRef.equals(TKJava.TVoid.getTypeRef())) return false;
		if(TheRef.equals(TKJava.TAny .getTypeRef())) return !ByRef.equals(TKJava.TVoid.getTypeRef());

		// Check from the Cache
		TypeSpec ToRef_Spec = ByRef.getTypeSpecWithoutEngine();
		if(ToRef_Spec != null) {
			Boolean IsCompatible = ToRef_Spec.checkCanBeAssignedTo_InCache(TheRef);
			if(IsCompatible != null) return IsCompatible;
		}
		
		// Perform the checking
		Boolean IsCompatible = DoMayTypeRefByAssignableByInstanceOf(pContext, pEngine, TheRef, ByRef);
		if(IsCompatible == null) return null;
		
		// Save the result to the cache
		ToRef_Spec = ByRef.getTypeSpecWithoutEngine();
		if(ToRef_Spec != null) ToRef_Spec.addToCanBeAssignedToCache(TheRef, IsCompatible.booleanValue());
		
		return IsCompatible;
	}
	
	/**
	 * Checks if tht type ByRef can be assigned into a variable of the type TheRef - This method will try to determine
	 * the compatibility as much as it can.
	 **/
	static final Boolean DoMayTypeRefByAssignableByInstanceOf(Context pContext, Engine pEngine,
			TypeRef TheRef, TypeRef ByRef) {
		
		TypeSpec TheSpec = TheRef.getTypeSpecWithoutEngine();
		TypeSpec BySpec  = ByRef .getTypeSpecWithoutEngine();
		
		// See if the spec cannot be compared
		if((TheSpec == null) || (BySpec == null)) {
			// Check the type ref only  v--- Use Do as the early return should already been checked
			Boolean IsTypeRefCompable = DoMightTypeRefByAssignableByInstanceOf(pContext, pEngine, TheRef, ByRef);
			if(IsTypeRefCompable != null) return IsTypeRefCompable;
			
		} else {			
			Boolean IsTypeRefCompable = MightTypeSpecByAssignableByInstanceOf(pContext, pEngine, TheSpec, BySpec);
			if(IsTypeRefCompable != null) return IsTypeRefCompable;
			
			if(TheSpec.isValidated() && BySpec.isValidated()) {
				// The types can be fully compared

				Type     TheSpec_Type = TheSpec.getTypeRef().getTheType();
				TypeKind TheSpec_Kind = TheSpec_Type.getTypeKind();
				
				return TheSpec_Kind.performCheckIfTypeCanBeAssignedByTypeWith(pContext, pEngine, TheSpec, BySpec);
				
			} else if(TheSpec.isResolved() && BySpec.isResolved()) {
				// The types can be compare but there are possiblity of unknown data
				// Check the spec (it will check the TypeRef (might) delegatedly)
				Boolean IsTypeSpecCompable = DoMightTypeSpecByAssignableByInstanceOf(pContext, pEngine, TheSpec, BySpec);
				if(IsTypeSpecCompable != null) return IsTypeSpecCompable;
				
			}
		}
		
		return null;
	} 

	/**
	 * Checks if tht type ByRef can be assigned into a variable of the type TheRef - This method will do what every it
	 *   take to find the solution.
	 **/
	static final public boolean CanTypeRefByAssignableByInstanceOf(Context pContext, Engine pEngine,
			TypeRef TheRef, TypeRef ByRef) {
		
		// Check the type ref only    
		Boolean IsTypeRefCompatible = MayTypeRefByAssignableByInstanceOf(pContext, pEngine, TheRef, ByRef);
		if(IsTypeRefCompatible != null) return IsTypeRefCompatible;
		
		Engine $Engine = pEngine;
		if(($Engine == null) && (pContext != null)) $Engine = pContext.getEngine();
		
		MType MT = $Engine.getTypeManager();
		MT.ensureTypeExist(pContext, TheRef);
		MT.ensureTypeExist(pContext, ByRef);

		TypeSpec TheRef_Spec = TheRef.getTypeSpec($Engine);
		TypeSpec ByRef_Spec  = ByRef .getTypeSpec($Engine);

		Boolean IsTypeSpecCompatible = MightTypeSpecByAssignableByInstanceOf(pContext, pEngine, TheRef_Spec, ByRef_Spec);
		if(IsTypeSpecCompatible != null) return IsTypeSpecCompatible;
		
		TypeKind TK = TheRef.getTheType().getTypeKind();

		boolean IsCompatible = TK.performCheckIfTypeCanBeAssignedByTypeWith(pContext, pEngine, TheRef_Spec, ByRef_Spec);

		// Save the result to the cache
		TypeSpec ToRef_Spec = ByRef_Spec;
		if(ToRef_Spec != null) ToRef_Spec.addToCanBeAssignedToCache(TheRef, IsCompatible);
		
		return IsCompatible;
	}  

	/**
	 * Checks if tht type ByRef can be assigned into a variable of the type TheRef - This method will do what every it
	 *   take to find the solution.
	 **/
	static final public boolean CanTypeRefByAssignableBy(Context pContext, Engine pEngine, TypeRef TheRef, Object ByObj) {
		if(TheRef == null)                           return false;
		if(TheRef.equals(TKJava.TAny .getTypeRef())) return  true;
		if(TheRef.equals(TKJava.TVoid.getTypeRef())) return false;
		if(ByObj  == null)                           return  true;
		
		Engine $Engine = pEngine;
		if(pEngine == null) {
			
			// Attempt at all cost to get the Engine
			if($Engine == null) {
				if(pContext != null) $Engine = pContext.getEngine();
				
				if($Engine == null) {
					TypeSpec TheRef_Spec = TheRef.getTypeSpecWithoutEngine();
					if(TheRef_Spec != null) {
						Type TheSpec_Type = TheRef_Spec.getTypeRef().getTheType();
						if(TheSpec_Type != null) $Engine = TheSpec_Type.getEngine();	
					}
				}
			}
		}
		
		// Resolve the TRef type
		TypeSpec TheRef_Spec = TheRef.getTypeSpecWithoutEngine();
		Type     TheRef_Type = null;
		if(TheRef_Spec != null) TheRef_Type = TheRef_Spec.getTypeRef().getTheType();
		if(TheRef_Type == null) {
			if(     ($Engine == null) && (TheRef instanceof TRPrimitive))
				 TheRef_Type = TKJava.Instance.getTypeByClassName(null, ((TRPrimitive)TheRef).getAlias(), ((TRPrimitive)TheRef).getClassCanonicalName());
			else if(($Engine == null) && (TheRef_Spec instanceof TSJava))
				 TheRef_Type = TKJava.Instance.getTypeByClassName(null, ((TSJava)TheRef_Spec).getAlias(), ((TSJava)TheRef_Spec).getClassName());
			else TheRef_Type = $Engine.getTypeManager().getTypeFromRefNoCheck(pContext, TheRef);
		}
		return TheRef_Type.getTypeKind().checkIfTypeCanBeAssignedBy(pContext, $Engine, TheRef_Type.getTypeSpec(), ByObj);
	}
	

	/** Checks if variable of the first given type ref can be assigned by the given object */
	public Boolean canTypeRefBeAssignableBy(TypeRef TheRef, Object TheObject) {
		return this.canTypeRefBeAssignableBy(null, TheRef, TheObject);
	}
	/** Checks if variable of the first given type ref can be assigned by an instance of the second given type ref */
	public boolean canTypeRefBeAssignableByInstanceOf(TypeRef TheRef, TypeRef ByRef) {
		return this.canTypeRefBeAssignableByInstanceOf(null, TheRef, ByRef);
	}
	/** Checks if variable of the first given type ref can be assigned by the given object */
	Boolean canTypeRefBeAssignableBy(Context pContext, TypeRef TheRef, Object ByObj) {
		if(TheRef == null) return false;
		if(ByObj  == null) return  true;
		return CanTypeRefByAssignableBy(pContext, this.getEngine(), TheRef, ByObj);
	}
	/** Checks if variable of the first given type ref can be assigned by an instance of the second given type ref */
	boolean canTypeRefBeAssignableByInstanceOf(Context pContext, TypeRef TheRef, TypeRef ByRef) {
		if(TheRef == null) return false;
		if(ByRef  == null) return false;
		return CanTypeRefByAssignableByInstanceOf(pContext, this.getEngine(), TheRef, ByRef);
	}
	
	// May be casted --------------------------------------------------------------------

	/**
	 * Checks if an instance of TRef may be casted to the type of TheTypeRef
	 * This method return null if it is unsure.
	 * This method should be used only for during the compilation especially for casting.
	 */
	public Boolean mayTypeRefBeCastedTo(TypeRef pToRef, TypeRef FromRef) {
		return this.mayTypeRefBeCastedTo(null, pToRef, FromRef);
	}
	/**
	 * Checks if an instance of TRef may be casted to the type of TheTypeRef
	 * This method return null if it is unsure.
	 * This method should be used only for during the compilation especially for casting.
	 */
	Boolean mayTypeRefBeCastedTo(Context pContext, TypeRef ToRef, TypeRef FromRef) {
		if(ToRef   == null)       return null;
		if(FromRef == null)       return true;
		if(ToRef   == FromRef)    return true;
		if(ToRef.equals(FromRef)) return true;

		// Resolve the TypeRefs
		try{ this.ensureTypeExist(pContext, ToRef);   } catch(Exception E) { return null; }
		try{ this.ensureTypeExist(pContext, FromRef); } catch(Exception E) { return null; }

		Type ToType   = ToRef  .getTheType();
		Type FromType = FromRef.getTheType();
			
		// All numbers and characters are interchangable
		if((ToType instanceof TKJava.TJava) && (FromType instanceof TKJava.TJava)) {
			Class<?> ToClass   = ((TKJava.TJava)ToType  ).getDataClass();
			Class<?> FromClass = ((TKJava.TJava)FromType).getDataClass();
			
			// Number and character are assignable
			if(  (Number.class.isAssignableFrom(ToClass)   || Character.class.isAssignableFrom(ToClass  ))
			  && (Number.class.isAssignableFrom(FromClass) || Character.class.isAssignableFrom(FromClass)))
				return true;	
		}
		
		if(this.canTypeRefBeAssignableByInstanceOf(pContext, ToRef, FromRef))
			return true;
		
		// Both are Java, the only possibility is that ToType is interface
		if((ToType instanceof TKJava.TJava) && (FromType instanceof TKJava.TJava))
			return ToType.getDataClass().isInterface();
		
		// If they both are interface, see if the target is compatible 
		if(TKInterface.isTypeInterface(ToType) && TKInterface.isTypeInterface(FromType)) {
			TypeRef ToType_TargetRef   = TKInterface.getInterfaceTarget(ToType);
			TypeRef FromType_TargetRef = TKInterface.getInterfaceTarget(FromType);
			return MayTypeRefByAssignableByInstanceOf(pContext, this.getEngine(), ToType_TargetRef, FromType_TargetRef)
			          ? true
			          : null;
		}
		
		// If the target is virtual, everything is possible except that it is a current interface targeting the type
		if(ToType.getTypeKind().isVirtual(null)) return true;
		
		return null;
	}
	
	// Compare Types --------------------------------------------------------------------

	/** The score to indicate that the searching or comparison has found the exact match. */
	static public final int ExactMatch =  0;
	/** The score to indicate that the searching or comparison has found nothing or not match. */
	static public final int NotMatch   = -1;
	
	/**
	 * Determine the score of the compatibility of type A and B.<br/>
	 * The score can be calculated as:
	 *   &nbsp;&nbsp; -1 (SearchResult.NotMatch) when A has no relationship with B <br/>
	 *   &nbsp;&nbsp;  0 (SearchResult.ExactMatch) when A is B or B is null and A is any<br/>
	 *   &nbsp;&nbsp;  1 when an instance of B can be assigned to the a variable of type A except A is Object.
	 *   &nbsp;&nbsp;  2 when B is an equalvalent array form of A (both are array)<br/>
	 *   &nbsp;&nbsp;  3 when B is null and A is primitive but not any<br/>
	 *   &nbsp;&nbsp;  4 when B is null and A is not any and not a primitive<br/>
	 *   &nbsp;&nbsp; 10 when A is Object.
	 *   &nbsp;&nbsp;  Other positive value when B is equivalent to A in deeper relationship<br/>
	 *   &nbsp;&nbsp;  If A is a variant, the result will be 5 + the variant member of A
	 **/
	static public int compareTypes(Engine pEngine, TypeRef A, TypeRef B) {
		if(A == B) return ExactMatch;
		
		if(    (A == null) || TKJava.TVoid.getTypeRef().equals(A)) {
			if((B == null) || TKJava.TVoid.getTypeRef().equals(B)) return ExactMatch;
			return NotMatch;
		}
		
		if(B == null) {
			if(TKJava.TAny.getTypeRef().equals(A))  return ExactMatch;
			
			if(A instanceof TRPrimitive) {
				Type TA = A.getTheType();
				
				if(TA == null) {
					// Try to load the type
					if(pEngine != null) {
						try { pEngine.getTypeManager().ensureTypeExist(null, A); TA = A.getTheType(); }
						catch (Exception Excp) {}
					}
					
					if(TA == null) {
						// Check the type ref only
						if(TKJava.TDouble .getTypeRef().equals(TA)) return 3;
						if(TKJava.TFloat  .getTypeRef().equals(TA)) return 3;
						if(TKJava.TLong   .getTypeRef().equals(TA)) return 3;
						if(TKJava.TInteger.getTypeRef().equals(TA)) return 3;
						if(TKJava.TShort  .getTypeRef().equals(TA)) return 3;
						if(TKJava.TByte   .getTypeRef().equals(TA)) return 3;
					}
				}
				
				// Primitive
				if(TA != null) {
					Class<?> DC = A.getTheType().getDataClass();
					Class<?> TC = A.getTheType().getTypeClass();	
					if((DC != null) && (DC.isPrimitive() || ((pEngine != null) && pEngine.getTypeManager().isPrimitiveType(DC)))) return 3;
					if((TC != null) && (TC.isPrimitive() || ((pEngine != null) && pEngine.getTypeManager().isPrimitiveType(TC)))) return 3;
				}
			}
			
			return 4;
		}
		
		if(A.equals(B))                         return ExactMatch;
		if(TKJava.TAny .getTypeRef().equals(A)) return 10;
		if(TKJava.TVoid.getTypeRef().equals(B)) return NotMatch;
		
		TypeSpec TSA = TypeRef.getTypeSpecOf(pEngine, A);
		TypeSpec TSB = TypeRef.getTypeSpecOf(pEngine, B);
		if(TSA == TSB)                     return ExactMatch;
		if((TSA == null) || (TSB == null)) return NotMatch;
		
		// Try to get the Engine
		Engine E = pEngine;
		if((E == null) && TSA.getTypeRef().isLoaded()) {
			E = TSA.getTypeRef().getTheType().getEngine();
			
			if((E == null) && TSB.getTypeRef().isLoaded())
				E = TSB.getTypeRef().getTheType().getEngine();
			
			pEngine = E;
		}

		// Java and Array -------------------------------------------------------------------------
		
		// If there are java types
		if((TSA instanceof TKJava.TSJava) && (TSB instanceof TKJava.TSJava)) {
			TKJava.TJava TA = (TKJava.TJava)(((TKJava.TSJava)TSA).getTypeRef().getTheType());
			TKJava.TJava TB = (TKJava.TJava)(((TKJava.TSJava)TSB).getTypeRef().getTheType());
			if(TA.canBeAssignedByInstanceOf(TB)) {
				if(!TKJava.TAny.getTypeRef().equals(A)) {
					if(TSA instanceof TKVariant.TSVariant) return ((TKVariant.TSVariant)TSA).getMemberCount() + 5;
					return 1; 
				}
				return 10;
			}
			
			int SA = -1;
			if     (TA == TKJava.TBigDecimal) SA = 10;
			else if(TA == TKJava.TBigInteger) SA =  9;
			else if(TA == TKJava.TDouble)     SA =  8;
			else if(TA == TKJava.TLong)       SA =  7;
			else if(TA == TKJava.TFloat)      SA =  6;
			else if(TA == TKJava.TInteger)    SA =  5;
			else if(TA == TKJava.TShort)      SA =  4;
			else if(TA == TKJava.TByte)       SA =  3;

			int SB = -1;
			if     (TB == TKJava.TBigDecimal) SB = 10;
			else if(TB == TKJava.TBigInteger) SB =  9;
			else if(TB == TKJava.TDouble)     SB =  8;
			else if(TB == TKJava.TLong)       SB =  7;
			else if(TB == TKJava.TFloat)      SB =  6;
			else if(TB == TKJava.TInteger)    SB =  5;
			else if(TB == TKJava.TShort)      SB =  4;
			else if(TB == TKJava.TByte)       SB =  3;
			
			if((SA != -1) && (SB != -1)) {
				int Diff = SA - SB;
				return (Diff < 0) ? NotMatch : Diff;
			}
		}
		
		// Array ----------------------------------------------------------------------------------
		
		// If there are array
		if((TSA instanceof TKArray.TSArray) && (TSB instanceof TKArray.TSArray)) {
			int ExtraScore = Math.abs((((TKArray.TSArray)TSA).getLength() - ((TKArray.TSArray)TSB).getLength()));
			A = ((TKArray.TSArray)TSA).getContainTypeRef();
			B = ((TKArray.TSArray)TSB).getContainTypeRef();
			int Score = compareTypes(pEngine, A, B);
			if(Score >= 0) return Score + 2 + ExtraScore;
		}
		
		// It needs the TypeKind to check it ------------------------------------------------------
		
		// Parameterized and Parameters -----------------------------------------------------------
				
		// Parameted types - If ByRef is a parametered of TheRef
		if(B instanceof TRParametered) {
			
			// but TheRef is not a parametered - it must be a parameterized to be valid
			if(!(A instanceof TRParametered)) {
				
				TypeRef ByRef_Target  = ((TRParametered)B).getTargetTypeRef();
				int     ScoreOfTarget = compareTypes(E, A, ByRef_Target);
				
				// False
				if(ScoreOfTarget < 0) return NotMatch;
				
				// See if the target is parameterized
				Type     ByRef_Type = ByRef_Target.getTheType();
				TypeSpec ByRef_Spec = (ByRef_Type == null)
				            ? ByRef_Target.getTypeSpec(pEngine)
				            : ByRef_Type  .getTypeSpec();
				
				ParameterizedTypeInfo PTI = ByRef_Spec.getParameterizedTypeInfo();
				if(PTI == null) return NotMatch;	// The target type is not a parameterized one

				TypeRef[] ByRef__Ps = ((TRParametered)B).ParamTypeRefs;
				int       PCount    = PTI.getParameterTypeCount();
				// The number of the parameters are not the same
				if(PCount != ByRef__Ps.length) return NotMatch;

				// Checks if the parameters are compatible
				int ParamsScope = 0;
				for(int i = 0; i < PCount; i++) {
					int ScoreOfParam = compareTypes(E, PTI.getParameterTypeRef(i), ByRef__Ps[i]);

					// False
					if(ScoreOfParam < 0) return NotMatch;
					ParamsScope += ScoreOfParam;
				}
				
				return ScoreOfTarget + 2 + ParamsScope;
			}	
			
			// They both are parametered ----------------------------------------------------------
			
			TypeRef ByRef_Target  = ((TRParametered)B).getTargetTypeRef();
			int     ScoreOfTarget = compareTypes(E, A, ByRef_Target);
			
			// Checks the parameters
			
			TypeRef[] TheRef_Ps = ((TRParametered)A).ParamTypeRefs;
			TypeRef[] ByRef__Ps = ((TRParametered)B).ParamTypeRefs;
			
			// Ensure the number of parameters
			if(((TheRef_Ps == null) || (ByRef__Ps == null)) && (TheRef_Ps != ByRef__Ps)) return NotMatch;
			if(TheRef_Ps.length != ByRef__Ps.length)                                     return NotMatch;

			// Checks if the parameters are compatible
			int PCount      = TheRef_Ps.length;
			int ParamsScope = 0;
			for(int i = 0; i < PCount; i++) {
				int ScoreOfParam = compareTypes(E, TheRef_Ps[i], ByRef__Ps[i]);

				// False
				if(ScoreOfParam < 0) return NotMatch;
				ParamsScope += ScoreOfParam;
			}
			
			return ScoreOfTarget + 2 + ParamsScope;
		}
		
		// If the types are assignable
		if(CanTypeRefByAssignableByInstanceOf(null, E, TSA.getTypeRef(), TSB.getTypeRef())) {
			if(TSA instanceof TKVariant.TSVariant) return ((TKVariant.TSVariant)TSA).getMemberCount() + 5;
			return 1;
		}
		
		return NotMatch;
		
	}
	
	// Type Check --------------------------------------------------------------

	/** Get the closest assessor that are share between the two */
	public TypeRef getClosestSharedAncessorOf(TypeRef TRefA, TypeRef TRefB) {
		TypeRef TR = this.findClosestSharedAncessorOf(null, TRefA, TRefB);
		if(TR == null) return TKJava.TAny.getTypeRef();
		return TR;
	}
	/** Get the closest assessor that are share between the two */
	TypeRef getClosestSharedAncessorOf(Context pContext, TypeRef TRefA, TypeRef TRefB) {
		TypeRef TR = this.findClosestSharedAncessorOf(null, TRefA, TRefB);
		if(TR == null) return TKJava.TAny.getTypeRef();
		return TR;
	}
	/** Get the closest assessor that are share between the two */
	private TypeRef findClosestSharedAncessorOf(Context pContext, TypeRef TRefA, TypeRef TRefB) {
		// Null
		if((TRefA == null) || (TRefB == null)) return null;
		
		// Same or equal TypeRef
		if((TRefA == TRefB) || TRefA.equals(TRefB)) return TRefA;
		
		try{ this.ensureTypeExist(pContext, TRefA); } catch(Exception E) { return TKJava.TAny.getTypeRef(); }
		try{ this.ensureTypeExist(pContext, TRefB); } catch(Exception E) { return TKJava.TAny.getTypeRef(); }
		
		Type TA = TRefA.getTheType();
		Type TB = TRefB.getTheType();
		
		// Same type
		if(TA == TB) return TRefA;
		
		// Void
		Type Void = TKJava.TVoid;
		if((TA == Void) || TA.equals(Void)) return Void.getTypeRef();
		if((TB == Void) || TB.equals(Void)) return Void.getTypeRef();

		// Any
		Type    Any   = TKJava.TAny;
		TypeRef AnyTR = TKJava.TAny.getTypeRef();
		if((TA == Any) || TA.equals(Any)) return AnyTR;
		if((TB == Any) || TB.equals(Any)) return AnyTR;
		
		// Assignable
		if(TA.canBeAssignedByInstanceOf(TB)) return TRefA;
		if(TB.canBeAssignedByInstanceOf(TA)) return TRefB;
		
		// Compatible numbers
		if(TKJava.TNumber.getTypeRef().canBeAssignedByInstanceOf(this.TheEngine, TRefA) &&
		   TKJava.TNumber.getTypeRef().canBeAssignedByInstanceOf(this.TheEngine, TRefB)) {
			if(TRefA == TKJava.TNumber.getTypeRef()) return TRefB;
			if(TRefB == TKJava.TNumber.getTypeRef()) return TRefA;

			if((TRefA == TKJava.TDouble .getTypeRef()) || (TRefB == TKJava.TDouble .getTypeRef())) return TKJava.TDouble.getTypeRef();
			if((TRefA == TKJava.TFloat  .getTypeRef()) || (TRefB == TKJava.TFloat  .getTypeRef())) return TKJava.TFloat .getTypeRef();
			if((TRefA == TKJava.TLong   .getTypeRef()) || (TRefB == TKJava.TLong   .getTypeRef())) return TKJava.TLong  .getTypeRef();
			if((TRefA == TKJava.TInteger.getTypeRef()) || (TRefB == TKJava.TInteger.getTypeRef())) return TKJava.TInteger.getTypeRef();
			if((TRefA == TKJava.TShort  .getTypeRef()) || (TRefB == TKJava.TShort  .getTypeRef())) return TKJava.TShort  .getTypeRef();
			if((TRefA == TKJava.TByte   .getTypeRef()) || (TRefB == TKJava.TByte   .getTypeRef())) return TKJava.TByte   .getTypeRef();			
		}
		
		// Super
		TypeRef TR_A = TA.getSuperRef();
		TR_A         =  this.findClosestSharedAncessorOf(pContext, TR_A, TRefB);
		if((TR_A != null) && !TR_A.equals(AnyTR)) return TR_A;

		TypeRef TR_B = TB.getSuperRef();
		TR_B        =  this.findClosestSharedAncessorOf(pContext, TR_B, TRefA);
		if((TR_B != null) && !TR_B.equals(AnyTR)) return TR_B;
		
		// Both are interfeace - See the target
		boolean IsAInterface = TKInterface.isTypeInterface(TA);
		boolean IsBInterface = TKInterface.isTypeInterface(TB);
		if(IsAInterface && IsBInterface) {
			TypeRef TR = this.findClosestSharedAncessorOf(
					pContext, 
					TKInterface.getInterfaceTarget(TA),
					TKInterface.getInterfaceTarget(TB));
			if((TR != null) && !TR.equals(AnyTR)) return TR;
		}
		
		return null;
	}
}
