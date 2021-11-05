package net.nawaman.curry.compiler;

import java.util.HashSet;

import net.nawaman.curry.CurryError;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TKVariant;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeKind;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.util.UClass;

public class Util_TypeRef {
	
	/** Compiles a TypeRef by its name */
	static public TypeRef CompileTypeRefByName(String $TName, ParseResult $Result, PTypePackage $TPackage,
			CompileProduct $CProduct) {
		TypeRef TR = $CProduct.getTypeRefFromString($TName);
		if(TR == null) {
			// If this is not in the type registration phrase, the type is unknown.
			if(!$CProduct.getCompilationState().isTypeRegistration())
				$CProduct.reportError("Unknown type: `"+$TName+"` <Util_TypeRef:24>", null, $Result.posOf(0));
			
			if($TName.contains("=>")) return null;
			
			// Assume to be a type in this package
			String PName = null; {
				PackageBuilder PB = $CProduct.getOwnerPackageBuilder();
				if(PB != null) PName = PB.getName();
			}
			if(PName == null) return null;

			TR = new TLPackage.TRPackage(PName, $TName);
		}
		
		return TR;
	}

	/** Compiles a TypeRef (for normal type, parametered type and array type)*/
	static public TypeRef CompileTypeRef(TypeRef $BaseTypeRef, TypeRef[][] $ParamTypeRefs, int[] $Dimensions,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		if($BaseTypeRef == null)
			return null;
		
		if(($ParamTypeRefs != null) && ($ParamTypeRefs.length != 0)) {
			for(int i = 0; i < $ParamTypeRefs.length; i++) {
				TypeRef[] ParamRefs = $ParamTypeRefs[i];
				
				if(!$BaseTypeRef.isLoaded()) {
					TypeSpec TSpec = null;
					try {
						Type T = $CProduct.getTypeAtCompileTime($BaseTypeRef);
						TSpec = T.getTypeSpec();
					} catch (Exception e) {}
					
					if(!$CProduct.getCompilationState().isTypeRegistration() && $CProduct.isCompileTimeCheckingFull()) {
						if(TSpec == null) {
							$CProduct.reportWarning(
								String.format(
									"Invalid type parameterization: Unable to retrive the type spec of '%s' <Util_TypeRef:59>",
									$BaseTypeRef
								),
								null,
								$Result.startPosition()
							);
							return null;
						}
						if(!TSpec.isParameterized()) {
							$CProduct.reportError(
								"Invalid type parameterization <Util_TypeRef:66>",
								null,
								$Result.startPosition());
							return null;
						}
					}
				}
				
				try { $BaseTypeRef = $BaseTypeRef.getParameteredTypeRef($CProduct.getEngine(), ParamRefs); }
				catch (CurryError CE) {
					String Msg = CE.getMessage();
					if(Msg.startsWith("Invalid type parameteization")) {
						$CProduct.reportError(Msg, CE, $Result.posOf(0));
						return null;
					}
				}
			}
		}
		
		if(($Dimensions != null) && ($Dimensions.length != 0)) {
			for(int i = $Dimensions.length; --i >= 0; )
				$BaseTypeRef = TKArray.newArrayTypeRef($BaseTypeRef, $Dimensions[i]);
		}
		
		return $BaseTypeRef;
	}
	
	/**
	 * Compile an Arbitary TypeRef by calling a method of the given TypeKind by name with the parameters.
	 * 
	 * The invoked method must return either a TypeSpec or a TypeRef.
	 **/
	static public TypeRef CompileArbitraryTypeRef(String $KindName, String $MethodName, Object[] $Params, 
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		TypeKind TK = (TKJava.KindName.equals($KindName))
		                   ? TKJava.Instance
		                   : $CProduct.getEngine().getTypeManager().getTypeKind($KindName);
		if(TK == null) {
			$CProduct.reportError("Unknown TypeKind '"+$KindName+"'<Util_TypeRef:233>", null, $Result.startPosition());
			return null;
		}
		
		Object O = null;
		try {
			O = UClass.invokeObjectMethod(TK, $MethodName, $Params);
		} catch (Exception E) {
			$CProduct.reportError("Invalid Arbitrary TypeKind Invocation <Util_TypeRef:254>", E,
					$Result.startPosition());
		}
		
		// Returns
		if(O instanceof TypeRef)  return ((TypeRef) O);
		if(O instanceof TypeSpec) return ((TypeSpec)O).getTypeRef();

		$CProduct.reportError("Invalid Arbitrary TypeKind Invocation return value '"+O+"' <Util_TypeRef:247>", null,
				$Result.startPosition());
		return null;
	}
	
	/**
	 * Compile a Variant TypeSpec.
	 *
	 * This method will screen for validity of the spec.
	 *
	 * @param $AsType          is the goven type of the variant type
	 * @param $AsType_NewType  is the type that i specified to be both AsType and NewType
	 * @param $NewTypes        are the types that are specified to be the type for new instance and default value
	 * @param $MemberTypes     are the rest of the member types
	 * @param $TPInfo          is the type parameterization for this type
	 * 
	 * Errors will be raise when:
	 * - $AsType and $AsType_NewType are given and they are not the same type.
	 * - There are more that one NewTypes
	 * Warnning will be raise when:
	 * - There are duplicate type name for the member types.
	 **/
	static public TypeSpecCreator CompileVariantTypeSpecCreator(TypeRef $AsType, TypeRef $AsType_NewType,
			TypeRef[] $NewTypes, TypeRef[] $MemberTypes, TypeRef[] $Interfaces, ParameterizedTypeInfo $TPInfo,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		TKVariant TKV = (TKVariant)$CProduct.getEngine().getTypeManager().getTypeKind(TKVariant.KindName);
		if(TKV == null) {
			$CProduct.reportError(
					"Variant types are not supported by this engine <Util_TypeRef:137>.",
					null, $Result.startPosition());
			return null;
		}
		
		// Multiple AsType ----------------------------------------------------------------------------------
		// It has only one
		if($AsType == null) $AsType = $AsType_NewType;
		// See if they are the same type
		else if(($AsType_NewType != null) && !$AsType.equals($AsType_NewType)) {
			$CProduct.reportError(
					"A variant type cannot have more than one `AsType` <Util_TypeRef:148>.",
					null, $Result.startPosition());
			return null;
		}

		// Multiple TypeForNew ------------------------------------------------------------------------------
		TypeRef $NewType = $AsType_NewType;
		
		if($NewTypes != null) {
			boolean Has_Multiple_TypeForNew = ($NewTypes.length > 1);
			
			if(!Has_Multiple_TypeForNew) {
				TypeRef Another_NewType = null;
				// Get the type
				if($NewTypes.length == 1) Another_NewType = $NewTypes[0];
				
				// So it is only NewType then,
				if($NewType == null) $NewType = Another_NewType;
				// Check if they are the same
				else if(($NewType != Another_NewType) && !$NewType.equals(Another_NewType)) {
					Has_Multiple_TypeForNew = true;
				}
			}
			if(Has_Multiple_TypeForNew) {
				$CProduct.reportError(
						"A variant type cannot have more than one `TypeForNew` <Util_TypeRef:173>.",
						null, $Result.startPosition());
				return null;
			}
		}

		// Compile the final no repeat member type
		HashSet<TypeRef> $MemberTypeRefs = new HashSet<TypeRef>();
		if($MemberTypes != null) {
			for(int i = 0; i < $MemberTypes.length; i++) {
				if($MemberTypes[i] == null) continue;
				$MemberTypeRefs.add($MemberTypes[i]);
			}
		}
		
		// The new type is a member type
		if($NewType != null) $MemberTypeRefs.add($NewType);
		
		// Checks one more time for the number of member type
		if($MemberTypeRefs.size() == 0) {
			$CProduct.reportError(
					"A variant type must have atleast one member type <Util_TypeRef:194>.",
					null, $Result.startPosition());
			return null;
		}
		
		$MemberTypes = $MemberTypeRefs.toArray(new TypeRef[$MemberTypeRefs.size()]);
		if($MemberTypeRefs.size() == 1) {
			if($AsType  == null) $AsType  = $MemberTypes[0];
			if($NewType == null) $NewType = $MemberTypes[0];
		}
		
		if(($AsType == null) || TKJava.TAny.getTypeRef().equals($AsType)) $AsType = TKJava.TAny.getTypeRef();
		else {
			// All Member Type must be assignable to the AsType
			for(TypeRef MTRef : $MemberTypeRefs) {
				if($AsType.canBeAssignedByInstanceOf($CProduct.getEngine(), MTRef)) continue;

				$CProduct.reportError(
						"Member type '"+MTRef+"' of a variant must all be assignable to its AsType <Util_TypeRef:211>.",
						null, $Result.startPosition());
				return null;
			}
		}
		
		return TKV.getTypeSpecCreator($AsType, $MemberTypes, $NewType, $Interfaces, $TPInfo);
	}
}
