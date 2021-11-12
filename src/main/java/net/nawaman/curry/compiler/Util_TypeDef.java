package net.nawaman.curry.compiler;

import java.util.Arrays;
import java.util.Vector;

import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Documentation;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecInterface;
import net.nawaman.curry.Location;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKExecutable;
import net.nawaman.curry.TKInterface;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.extra.type_object.TKClass;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.util.UArray;

public class Util_TypeDef {
	
	static public final String enTYPEDEF   = "#TypeDef";
	static public final String enTYPE_NAME = "$TypeName";

	static public final String enARRAY_COMPONENTTYPE = "#ComponentType";
	static public final String enARRAY_DIMENSION     = "#Dimension";
	
	static public final String enPARAMETERIZEDINFO = "#ParameterizedInfo";
	
	static public final String enINHERITREF  = "#InheritTypeRef";
	static public final String enIMPLEMENTED = "#Implemented";

	static public final String enABSTRACT = "$Abstract";
	static public final String enFINAL    = "$Final";
	
	/** Parse and compile TypeDefinition at the state of TypeRgistration and TypeRefinition */
	static public FileCompileResult.TypeSpecification ParseCompileTypeDef_TypeSpec(
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		// Only Type Registration and Type Refinition is accept
		boolean isTRegistration = $CProduct.getCompilationState().isTypeRegistration();
		boolean isTRefinition   = $CProduct.getCompilationState().isTypeRefinition(); 
		if(!isTRegistration && !isTRefinition) return null;
		
		Object R = $Result.valueOf(enTYPEDEF, $TPackage, $CProduct);
		if(!(R instanceof TypeSpecCreator)) {
			$CProduct.reportError(
				"Invalid TypeDef result from `"+ $Result.nameOf(enTYPEDEF) +"` ("+R+") <Util_TypeDef:30>", null,
				$Result.startPositionOf(enTYPEDEF));
			return null;
		}
		
		// Get the type name
		String TName = $Result.textOf(enTYPE_NAME);
		if(TName == null) TName = $Result.subResultOf(enTYPEDEF).textOf(enTYPE_NAME);

		// Get the type accessibility
		Accessibility      Access = (Accessibility)$Result                 .valueOf(Util_Element.enACCESS, $TPackage, $CProduct);
		if(Access == null) Access = (Accessibility)$Result.subResultOf(enTYPEDEF).valueOf(Util_Element.enACCESS, $TPackage, $CProduct);
		if(Access == null) Access = net.nawaman.curry.Package.Public;
		
		TypeSpecCreator TSCreator = (TypeSpecCreator)R;
		Documentation   Document  = (Documentation)$CProduct.getCurrentCodeData(Util_Element.dnDOCUMENT_FOR_TYPE);
		Location        Location  = $CProduct.getCurrentLocation($Result.coordinateOf(0));
					
		return new FileCompileResult.TypeSpecification(TName, Document, Access, false, Location, TSCreator);
	}

	/** Parse and compile TypeDefinition at the state of StructuralRegistration */
	static public FileCompileResult.TypeWithElements ParseCompileTypeDef_TypeStructure(
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		// Only StructuralRegistration is accept
		if(!$CProduct.getCompilationState().isStructuralRegistration()) return null;

		// Compile structure
		boolean HasElement = false;
		Object  SecretID   = null;
		
		String TName = $Result.textOf(enTYPE_NAME);
		if((TName == null) && ($Result.subResultOf(enTYPEDEF) != null)) TName = $Result.subResultOf(enTYPEDEF).textOf(enTYPE_NAME);
		
		// Just in case
		TypeRef               TRef   = $CProduct.OwnerTypeRef;
		ParameterizedTypeInfo PTInfo = $CProduct.PTInfo;
		try {
			$CProduct.OwnerTypeRef = new TLPackage.TRPackage($CProduct.getOwnerPackageName(), TName);
			Type OT = $CProduct.getTypeAtCompileTime($CProduct.OwnerTypeRef, false);
			if(OT != null) {
				TypeSpec TS = OT.getTypeSpec();
				if(TS != null) $CProduct.PTInfo = TS.getParameterizedTypeInfo();
			}
		
			FileCompileResult.TypeWithElements TWE = new FileCompileResult.TypeWithElements(TName, SecretID);
				
			FileCompileResult.TypeElement<?>[] Elements = (FileCompileResult.TypeElement[])$Result.valueOf(enTYPEDEF, $TPackage, $CProduct);
			if((Elements != null) && (Elements.length != 0)) {
				for(int i = 0; i < Elements.length; i++) {
					FileCompileResult.TypeElement<?> Element = Elements[i];
					if(Element == null) continue;
						
					if(     Element instanceof FileCompileResult.TypeMethod)
						TWE.addMethod(SecretID, (FileCompileResult.TypeMethod)Element);
					
					else if(Element instanceof FileCompileResult.TypeField)
						TWE.addField(SecretID, (FileCompileResult.TypeField)Element);
					
					else if(Element instanceof FileCompileResult.TypeConstructor)
						TWE.addConstructor(SecretID, (FileCompileResult.TypeConstructor)Element);
					
					HasElement = true;
				}
			}
					
			return HasElement ? TWE : null;
		} finally {
			$CProduct.OwnerTypeRef = TRef;
			$CProduct.PTInfo       = PTInfo;
		}
	}

	/**
	 * Compile an Executable no-name TypeSpec.
	 * 
	 * @param $ReturnType      is the return types of the executable.
	 * @param $ParameterTypes  is an array of typeref for the paramter list
	 * @param $IsVarArgs       is a flag indecating that the last parameter is an VarArgs
	 * @param $DesiredKind     is the desired executable kind.
	 **/
	static public TypeSpecCreator ParseCompileExecutableTypeSpecCreator(ExecKind $DesiredKind, ExecInterface $EInterface,
			ParameterizedTypeInfo $TPInfo,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		TKExecutable TKE = (TKExecutable)$CProduct.getEngine().getTypeManager().getTypeKind(TKExecutable.KindName);
		if(TKE == null) {
			$CProduct.reportError(
					"Executable types are not supported by this engine <Util_TypeDef:27>.",
					null, $Result.startPosition());
			return null;
		}
		
		// Fragment must have no parameter
		if(($DesiredKind == ExecKind.Fragment) && ($EInterface.getParamCount() != 0)) {
			$CProduct.reportError(
					"A fragment must have no parameter <Util_TypeDef:35>.",
					null, $Result.startPosition());
			return null;
		} 
		
		return TKE.getTypeSpecCreator($DesiredKind, $EInterface, $TPInfo);
	}
	
	/** Parse and compile Array TypeDef */
	static public TypeSpecCreator ParseCompileArrayTypeSpecCreator(
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// Type Registration and Type Refinition
		boolean isTRegistration = $CProduct.getCompilationState().isTypeRegistration();
		boolean isTRefinition   = $CProduct.getCompilationState().isTypeRefinition();
		
		if(!isTRegistration && !isTRefinition) return null;
		
		// Get the engine
		Engine $Engine = $CProduct.getEngine();
		
		TKArray TKA = (TKArray)$Engine.getTypeManager().getTypeKind(TKArray.KindName);
		if(TKA == null) {
			$CProduct.reportError("Array type is not supported in this engine! <Util_TypeDef:127>", null, $Result.startPositionOf(0));
			return null;
		}
		
		int Dim = ($Result.textOf(enARRAY_DIMENSION) == null)?-1:(Integer)$Result.valueOf(enARRAY_DIMENSION, $TPackage, $CProduct);
		if(Dim < -1) {
			$CProduct.reportError("Invalid array type dimension ("+Dim+"). <Util_TypeDef:133>", null, $Result.startPositionOf(enARRAY_DIMENSION));
			return null;
		}
		
		TypeRef ComponentType = (TypeRef)$Result.valueOf(enARRAY_COMPONENTTYPE, $TPackage, $CProduct);
		return TKA.getTypeSpecCreator(ComponentType, Dim);
	}
	
	/** Parse and compile Interface TypeDef */
	static public FileCompileResult.TypeElement<?>[] ParseCompileInterfaceTypeSpecCreator_TypeStructure(
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		if(!$CProduct.getCompilationState().isStructuralRegistration()) return null;
		
		FileCompileResult.TypeElement<?>[] Ms =
			(FileCompileResult.TypeElement<?>[])
			UArray.convertArray(
				$Result.valuesOf(Util_TypeElement.enMETHOD, $TPackage, $CProduct),
				FileCompileResult.TypeElement[].class
		);
		
		FileCompileResult.TypeElement<?>[] Fs =
			(FileCompileResult.TypeElement<?>[])
			UArray.convertArray(
				$Result.valuesOf(Util_TypeElement.enFIELD, $TPackage, $CProduct),
				FileCompileResult.TypeElement[].class
		);
		
		Vector<FileCompileResult.TypeElement<?>> Es = new Vector<FileCompileResult.TypeElement<?>>();
		if(Ms != null) Es.addAll(Arrays.asList(Ms));
		if(Fs != null) Es.addAll(Arrays.asList(Fs));
		
		// Returns the type elements
		return Es.toArray(new FileCompileResult.TypeElement<?>[Es.size()]);
	}
	
	/** Parse and compile Interface TypeDef */
	static public TypeSpecCreator ParseCompileInterfaceTypeSpecCreator(boolean IsDuck,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		TypeRef TargetTypeRef = (TypeRef)$Result.valueOf( enINHERITREF,  $TPackage, $CProduct);
		return ParseCompileInterfaceTypeSpecCreator(IsDuck, TargetTypeRef, $Result, $TPackage, $CProduct);
	}
	
	/** Parse and compile Interface TypeDef */
	static public TypeSpecCreator ParseCompileInterfaceTypeSpecCreator(boolean IsDuck, TypeRef   TargetTypeRef, 
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// Type Registration and Type Refinition
		boolean isTRegistration = $CProduct.getCompilationState().isTypeRegistration();
		boolean isTRefinition   = $CProduct.getCompilationState().isTypeRefinition();
		
		if(!isTRegistration && !isTRefinition) return null;

		// Get the engine
		Engine $Engine = $CProduct.getEngine();
		
		TKInterface TKI = (TKInterface)$Engine.getTypeManager().getTypeKind(TKInterface.KindName);
		if(TKI == null) {
			$CProduct.reportError("Interface type is not support", null, $Result.startPositionOf(0));
			return null;
		}
			
		try {
			ParameterizedTypeInfo PTInfo = (ParameterizedTypeInfo)$Result.valueOf(enPARAMETERIZEDINFO, $TPackage, $CProduct);
			$CProduct.useParameterizedTypeInfos(PTInfo);
		
			int Count = $CProduct.getErrorMessageCount();
		
			// Prepare data
			TypeRef[] InterfaceTRefs = (TypeRef[])UArray.convertArray($Result.valuesOf(enIMPLEMENTED, $TPackage, $CProduct), TypeRef[].class);
			
			// There is some error
			if(Count != $CProduct.getErrorMessageCount()) return null;
		
			return TKI.getTypeSpecCreator(PTInfo, InterfaceTRefs, TargetTypeRef, !IsDuck);
					
		} finally {
			$CProduct.clearParameterizedTypeInfos();
		}
	}
	
	/** Compile a Class TypeSpec. */
	static public TypeSpecCreator ParseCompileClassTypeSpecCreator(
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// Type Registration and Type Refinition
		boolean isTRegistration = $CProduct.getCompilationState().isTypeRegistration();
		boolean isTRefinition   = $CProduct.getCompilationState().isTypeRefinition();
		
		if(!isTRegistration && !isTRefinition) return null;

		// Get the engine
		Engine $Engine = $CProduct.getEngine();
		
		TKClass TKC = (TKClass)$Engine.getTypeManager().getTypeKind(TKClass.KindName);
		if(TKC == null) {
			$CProduct.reportError("Class type is not support", null, $Result.startPositionOf(0));
			return null;
		}
		
		boolean IsAbstract = $Result.textOf(enABSTRACT) != null;
		boolean IsFinal    = $Result.textOf(enFINAL)    != null;
			
		try {
			ParameterizedTypeInfo PTInfo = (ParameterizedTypeInfo)$Result.valueOf(enPARAMETERIZEDINFO, $TPackage, $CProduct);
			$CProduct.useParameterizedTypeInfos(PTInfo);
		
			int Count = $CProduct.getErrorMessageCount();
		
			// Prepare data
			TypeRef   SuperTypeRef   = (TypeRef)                      $Result.valueOf( enINHERITREF,  $TPackage, $CProduct);
			TypeRef[] InterfaceTRefs = (TypeRef[])UArray.convertArray($Result.valuesOf(enIMPLEMENTED, $TPackage, $CProduct), TypeRef[].class);
			
			// There is some error
			if(Count != $CProduct.getErrorMessageCount()) return null;
		
			return TKC.getTypeSpecCreator(IsAbstract, IsFinal, SuperTypeRef, InterfaceTRefs, PTInfo, null, null);
					
		} finally {
			$CProduct.clearParameterizedTypeInfos();
		}
	}
}
