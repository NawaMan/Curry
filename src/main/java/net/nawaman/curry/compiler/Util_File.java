package net.nawaman.curry.compiler;

import net.nawaman.curry.Documentation;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.TypeRef;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.util.UString;

public class Util_File {
	

	static public final String enFUNCTION = "#Funct";
	static public final String enVARIABLE = "#Var";
	static public final String enTYPE     = "#Type";
	static public final String enTYPEDEF  = "#TypeDef";
	static public final String enDOCUMENT = "#Documentation";
	
	static final java.util.Random Random = new java.util.Random();
	
	/** Report Internal compilation error */
	static public void ReportResultProblem(CompileProduct $CProduct, String PType, String CName, String Action, int Pos) {
		$CProduct.reportError(
			String.format(
				"Internal Compilation Error: %s compiler must return a %s when %s <File:12>.",
				PType, CName, Action
			),
			null, Pos);
	}
	
	/** Verify if the parse result of the file is match */
	static public Object VerifyIfFileParsingUnmatch(String $Text, ParseResult $Result, CompileProduct $CProduct) {
		if($Result != null) return null;
		// Unmatch	
		
		String CCodeName = $CProduct.getCurrentCodeName();
		
		// The code may be empty
		if(($Text == null) || ($Text.trim().length() == 0)) {
			if($CProduct.isCompileTimeCheckingFull()) {
				$CProduct.reportWarning(
					String.format("Empty code in '%s' <Util_File:36>.", CCodeName),
					null, 0
				);
			}
			return "";
		}
		
		$CProduct.reportError(
			String.format("Invalid the source code file '%s' <Util_File:46>.", CCodeName),
			null
		);
		return false;
	}
	
	/** Checks if package name is well-formed and return the */
	static public boolean VerifiedPackageName(CompileProduct $CProduct, String $PackageName, String CCodeName, int $PackagePos) {
		int    Index; if((Index = CCodeName.lastIndexOf('/')) == -1) Index = 0;
		String PName = CCodeName.substring(0, Index);
		
		// Ensure Package name is well-formed
		if(($PackageName != null) && PName.equals($PackageName.replaceAll("~>", "/")))
			return true;
		
		$CProduct.reportError(
			String.format(
				"Invalid package name '%s' in the code file '%s' <Util_File:37>.",
				$PackageName, CCodeName
			),
			null, $PackagePos
		);
		return false;
	}
	
	/** Extracts Imports string from imports parse result. */
	static public String[] ExtractImports(String $PackageName, ParseResult[] $Imports,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		if(($Imports == null) || ($Imports.length == 0)) return UString.EmptyStringArray;
		
		String[] Imports = new String[$Imports.length];
		// Extract the import
		for(int i = 0; i < $Imports.length; i++) {
			ParseResult Import = $Imports[i];
			// Check for error
			if(!Import.ensureNoError($TPackage, $CProduct)) continue;
		
			String[]     PNames = Import.textsOf("$PackageName");
			StringBuffer PName  = new StringBuffer();
			if(PNames != null) {
				// Join the package name
				for(int p = 0; p < PNames.length; p++)
					PName.append(PNames[p]);
				
				// Add wildcard
				String WildCard = Import.textOf("$IsWildCard");
				if(WildCard != null) PName.append(WildCard);
			}
			Imports[i] = PName.toString();
		}
		return Imports;
	}
	
	/** Registers the types */
	static public FileCompileResult.TypeRegistration RegisterTypes(String $PackageName,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// Type Registration and Type Refinition
		boolean isTRegistration = $CProduct.getCompilationState().isTypeRegistration();
		boolean isTRefinition   = $CProduct.getCompilationState().isTypeRefinition();
		if(!isTRegistration && !isTRefinition) {
			$CProduct.reportError(
				"Internal Compilation Error: Invalid compilation state: `TypeRegistration` or `Typedefinition` expected. " +
				"<Util_File:173>.",
				null);
			return null;
		}
		
		String ID = "ID"+Random.nextInt();
		
		// Extract the imports
		String[] $Imports = Util_File.ExtractImports($PackageName, $Result.subResultsOf("#Import"), $Result, $TPackage, $CProduct);			
		FileCompileResult.TypeRegistration TReg = new FileCompileResult.TypeRegistration($PackageName, $Imports, ID);

		// Add the imports
		if($Imports != null) $CProduct.addImport($Imports);

		Documentation Doc = null;
		for(int i = 0; i < $Result.entryCount(); i++) {
			String EntryName = $Result.nameOf(i);

			if(enTYPE.equals(EntryName)) {
				TypeRef OldOwnerTypeRef = $CProduct.OwnerTypeRef;
				
				try {
					// Get the type name
					String TName;
					if((TName = $Result.textOf(Util_TypeDef.enTYPE_NAME)) == null) {
						ParseResult PR;
						if((PR = $Result.subResultOf(i)) != null) {
							TName = PR.textOf(Util_TypeDef.enTYPE_NAME);
							if((TName == null) && ((PR = PR.subResultOf(enTYPEDEF)) != null))
								TName = PR.textOf(Util_TypeDef.enTYPE_NAME);
						}
					}
					
					String PName;
					if(((PName = $CProduct.getOwnerPackageName()) != null) && (TName != null))
						$CProduct.OwnerTypeRef = new TLPackage.TRPackage(PName, TName);
					
					// Pass on the Document
					$CProduct.setCurrentCodeData(Util_Element.dnDOCUMENT_FOR_TYPE, Doc);
					
					Object Type = $Result.valueOf(i, $TPackage, $CProduct);
						
					if(!(Type instanceof FileCompileResult.TypeSpecification)) {
						if(Type == null) continue;
						ReportResultProblem($CProduct, "Type", "TypeSpecification", "registering/refining type", $Result.startPositionOf(i));
						return null;
					}
						
					TReg.addTypeData(ID, (FileCompileResult.TypeSpecification)Type);
					
				} finally {
					// Release the document
					Doc = null;
					$CProduct.setCurrentCodeData(Util_Element.dnDOCUMENT_FOR_TYPE, Doc);
					$CProduct.OwnerTypeRef = OldOwnerTypeRef;
				}
				
			} else if("#Import".equals(EntryName)) {
				// This will add the import into the import list 
				$Result.valueOf(i, $TPackage, $CProduct);
				
			} else if(Util_Element.enDOCUMENT.equals(EntryName)) {
				Doc = (Documentation)$Result.valueOf(i, $TPackage, $CProduct);
				
			} else if(EntryName  != null)
				Doc = null;
		}
		
		return TReg;
	}

	/** Registers the structure */
	static public FileCompileResult.StructuralRegistration RegisterFileStructure(String $PackageName,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		if(!$CProduct.getCompilationState().isStructuralRegistration()) {
			$CProduct.reportError(
					"Internal Compilation Error: Invalid compilation state: `StruictureRegistration` expected. <File:1133>.",
					null);
			return null;
		}
			
		String ID = "ID"+Random.nextInt();
		FileCompileResult.StructuralRegistration SReg = new FileCompileResult.StructuralRegistration($PackageName, null, ID);
		
		// Just in case
		String OldPackageName = $CProduct.OwnerPackageName;
		try {
			$CProduct.OwnerPackageName = $PackageName;
		
			Documentation Doc = null;
			for(int i = 0; i < $Result.entryCount(); i++) {
				String EntryName = $Result.nameOf(i);
				if(enFUNCTION.equals(EntryName)) {
					TypeRef OldOwnerTypeRef = $CProduct.OwnerTypeRef;	
					try {
						// Set the OwnerTypeRef
						$CProduct.OwnerTypeRef = null;
						
						// Pass on the Document
						$CProduct.setCurrentCodeData(Util_Element.dnDOCUMENT_FOR_FUNC, Doc);
	
						// Compile the function
						Object Funct = $Result.valueOf(i, $TPackage, $CProduct);
						if(!(Funct instanceof FileCompileResult.PackageFunction)) {
							if(Funct == null) continue;
		
							ReportResultProblem($CProduct, "Package Function", "PackageFunction", "registering package function",
									$Result.startPositionOf(i));
							return null;
						}
						
						// Register
						SReg.addFunction(ID, (FileCompileResult.PackageFunction)Funct);
						
					} finally {
						// Release the document
						Doc = null;
						$CProduct.setCurrentCodeData(Util_Element.dnDOCUMENT_FOR_FUNC, Doc);
						$CProduct.OwnerTypeRef = OldOwnerTypeRef;
					}
						
				} else if(enVARIABLE.equals(EntryName)) {
					TypeRef OldOwnerTypeRef = $CProduct.OwnerTypeRef;
					try {
						// Set the OwnerTypeRef
						$CProduct.OwnerTypeRef = null;
						
						// Pass on the Document
						$CProduct.setCurrentCodeData(Util_Element.dnDOCUMENT_FOR_PVAR, Doc);
						
						// Compile the variable
						Object Var = $Result.valueOf(i, $TPackage, $CProduct);
						
						if(!(Var instanceof FileCompileResult.PackageVariable)) {
							if(Var == null) continue;
							ReportResultProblem($CProduct, "Package Variable", "PackageVariable", "registering package variable", $Result.startPositionOf(i));
							return null;
						}
	
						// Register
						SReg.addVariable(ID, (FileCompileResult.PackageVariable)Var);
						
					} finally {
						// Release the document
						Doc = null;
						$CProduct.setCurrentCodeData(Util_Element.dnDOCUMENT_FOR_PVAR, Doc);
						$CProduct.OwnerTypeRef = OldOwnerTypeRef;
					}
					
				} else if(enTYPE.equals(EntryName)) {
					TypeRef OldOwnerTypeRef = $CProduct.OwnerTypeRef;
					try {
						// Get the type name
						String TName;
						if((TName = $Result.textOf(Util_TypeDef.enTYPE_NAME)) == null) {
							ParseResult PR;
							if((PR = $Result.subResultOf(i)) != null) {
								TName = PR.textOf(Util_TypeDef.enTYPE_NAME);
								if((TName == null) && ((PR = PR.subResultOf(enTYPEDEF)) != null))
									TName = PR.textOf(Util_TypeDef.enTYPE_NAME);
							}
						}
						
						String PName;
						if(((PName = $CProduct.getOwnerPackageName()) != null) && (TName != null))
							$CProduct.OwnerTypeRef = new TLPackage.TRPackage(PName, TName);
						
						// Pass on the Document
						$CProduct.setCurrentCodeData(Util_Element.dnDOCUMENT_FOR_TYPE, Doc);
						
						// Compile the variable
						Object Type = $Result.valueOf(i, $TPackage, $CProduct);
						
						if(!(Type instanceof FileCompileResult.TypeWithElements)) {
							// Complex Type
							if(Type == null) continue;
							ReportResultProblem($CProduct, "Type", "TypeWithElements", "registering type structure", $Result.startPositionOf(i));
							return null;
						}
		
						// Register
						SReg.addTypeWithElements(ID, (FileCompileResult.TypeWithElements)Type);
						
					} finally {
						// Release the document
						Doc = null;
						$CProduct.setCurrentCodeData(Util_Element.dnDOCUMENT_FOR_TYPE, Doc);
						$CProduct.OwnerTypeRef = OldOwnerTypeRef;
					}
					
				} else if(Util_Element.enDOCUMENT.equals(EntryName)) {
					Doc = (Documentation)$Result.valueOf(i, $TPackage, $CProduct);
					
				} else if(EntryName != null) {
					Doc = null;
				}
					
			}
				
			return SReg;
			
		} finally {
			$CProduct.OwnerPackageName = OldPackageName;
		}
	}
}
