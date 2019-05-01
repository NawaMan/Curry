package net.nawaman.curry.compiler;

import java.util.HashMap;

import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Documentation;
import net.nawaman.curry.ExecInterface;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.Location;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Variable;
import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;

public class Util_Element {
	
	// Default values --------------------------------------------------------------------------------------------------
	
	static public Accessibility DEFAULT_TYPE_ELEMENT_ACCESSIBILITY = Type.Public;
	
	// Entry names -----------------------------------------------------------------------------------------------------
	
	static public final String enNAME = "$Name";
	static public final String enKIND = "$Kind";

	static public final String enINTERFACE = "#Interface";
	
	static public final String prForInterface ="ForInterface";

	static public final String enDOCUMENT          = "#Documentation";
	static public final String dnDOCUMENT_FOR_PVAR = "Doc_PVar";
	static public final String dnDOCUMENT_FOR_FUNC = "Doc_Func";
	static public final String dnDOCUMENT_FOR_TYPE = "Doc_Type";
	
	static public final String enACCESS   = "#Access";
	static public final String enABSTRACT = "$Abstract";
	static public final String enDYNAMIC  = "$Dynamic";
	static public final String enSTATIC   = "$Static";
	static public final String enMOREDATA = "#MoreData";

	static public final String enDELEGATE            = "#Delegate";
	static public final String enDELEGATE_TARGETNAME = "$TargetName";
	
	static public final String enSTARTBODY = "$StartBody";
	static public final String enLANGNAME  = "$Lang";
	static public final String enCURRYBODY = "#Body";
	static public final String enLANGCODE  = "$Code";


	static public final String enTYPE         = "#Type";
	static public final String enDEFAULTVALUE = "#DefaultValue";
	
	static public final String enWRITEACCESS  = "#WriteAccess";
	static public final String enCONFIGACCESS = "#ConfigAccess";

	static public final String enNOTNULL    = "$NotNull";
	static public final String enUNWRITABLE = "$UnWritable";
	
	// Parse and Compile -----------------------------------------------------------------------------------------------

	static private HashMap<String, String> AttributePropertyNames = new HashMap<String, String>();
	static {
		AttributePropertyNames.put(enACCESS,       "read accessibility");
		AttributePropertyNames.put(enWRITEACCESS,  "write accessibility");
		AttributePropertyNames.put(enCONFIGACCESS, "config accessibility");
		AttributePropertyNames.put(enNOTNULL,      "not-null flag");
	}
	
	/** Registers the structure */
	static public FileCompileResult.PackageVariable ParseCompilePackageVariable(
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {


		String Name = $Result.textOf(enNAME);
		
		// Quick validate ----------------------------------------------------------------------------------------------
		
		// Ensure no property is repeated
		if(Util_General.EnsureNoRepeat(
				$Result, $CProduct, true, null, " package variable " + Name, "Util_Element:86", AttributePropertyNames,
				enACCESS, enWRITEACCESS, enCONFIGACCESS, enNOTNULL)) return null;

		Documentation Document = (Documentation)$CProduct.getCurrentCodeData(dnDOCUMENT_FOR_PVAR);
		Accessibility RAccess  = (Accessibility)$Result.valueOf(enACCESS,       $TPackage, $CProduct);
		Accessibility WAccess  = (Accessibility)$Result.valueOf(enWRITEACCESS,  $TPackage, $CProduct);
		Accessibility CAccess  = (Accessibility)$Result.valueOf(enCONFIGACCESS, $TPackage, $CProduct);
		MoreData      MoreData = (MoreData)     $Result.valueOf(enMOREDATA,     $TPackage, $CProduct);
		
		boolean       NonNull  = ($Result.textOf(enNOTNULL)    != null);
		boolean       Writable = ($Result.textOf(enUNWRITABLE) == null);
		
		TypeRef       TRef     = (TypeRef)$Result.valueOf(enTYPE, $TPackage, $CProduct);
		ParseResult   DValue   =          $Result.subOf(  enDEFAULTVALUE);
		boolean       IsNull   = (DValue == null) || "null".equals($Result.textOf(enDEFAULTVALUE));

		if(DValue == null)                 { $CProduct.reportError("Package variable must have a default value <Util_Element:93>", null, $Result.posOf(enNAME));         return null; }
		if(!Writable && (WAccess != null)) { $CProduct.reportError("A constant is not writable <Util_Element:94>",                 null, $Result.posOf(enWRITEACCESS));  return null; }
		if(NonNull   && IsNull)            { $CProduct.reportError("The variable/constant must not be null <Util_Element:95>",     null, $Result.posOf(enDEFAULTVALUE)); return null; }

		int EIndex = -1;
		for(int i = $Result.count(); --i >= 0; ) { if(DValue == $Result.getSubOf(i)) { EIndex = i; break; } }
		ElementResolver Resolver = (DValue == null)?null:Util_ElementResolver.newAttrResolver(false, Name, $Result, EIndex, $TPackage, $CProduct);

		if             (RAccess == null)  RAccess = net.nawaman.curry.Package.Public;	// Default is public
		if(Writable && (WAccess == null)) WAccess = RAccess;	                        // Default is Read
		if(            (CAccess == null)) CAccess = net.nawaman.curry.Package.Package;	// Default is package !!!!!

		final DataHolderInfo DHI = new DataHolderInfo(TRef, null, Variable.FactoryName, true, Writable, true, true, null);
		final Location       Loc = new Location($CProduct.getCurrentFeederName(), $CProduct.getCurrentCodeName(), $Result.locationCROf(enNAME));
		StackOwnerAppender SOA = StackOwnerAppender.Util.newTempAttrDirect(
				$CProduct, $TPackage, $Result,
				RAccess, RAccess, CAccess,
				Name, NonNull, DHI, Loc, net.nawaman.curry.util.MoreData.newMoreDataFromArray(MoreData, true),
				Resolver, Document);

		return new FileCompileResult.PackageVariable(Name, SOA);
	}

	/** Registers the structure */
	static public FileCompileResult.PackageFunction ParseCompilePackageFunction(
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		// Ensure the right state of the compilation
		if(!$CProduct.getCompilationState().isStructuralRegistration()) return null;

		// StructuralRegistration ======================================================================================

		// Prepare some parameters -------------------------------------------------------------------------------------
		String  Language     =  $Result.textOf(enLANGNAME);
		boolean HasCurryBody = ($Result.textOf(enCURRYBODY) != null);
		boolean HasJavaBody  = ($Result.textOf(enLANGCODE)  != null);
		boolean HasBody      = HasCurryBody || HasJavaBody;

		// Unless it is for interface, the method needs to be one of these
		if(!HasBody) {
			$CProduct.reportError("Missing function body <CompilerUtil:1144>.", null, $Result.posOf(0));
			return null;
		}

		// Prepare the signature ---------------------------------------------------------------------------------------
		String Name = $Result.textOf(enNAME);
		char   Kind = Character.toLowerCase(Util_General.GetFirstCharOf($Result, Util_Element.enKIND, 's'));
		
		Location      Location  = new Location($CProduct.getCurrentFeederName(), $CProduct.getCurrentCodeName(), $Result.locationCROf(0));
		ExecInterface Interface = (ExecInterface)$Result.valueOf(enINTERFACE, $TPackage, $CProduct);
		ExecSignature Signature = ExecSignature.newSignature(Name, Interface, Location, null);

		Documentation Document = (Documentation)$CProduct.getCurrentCodeData(dnDOCUMENT_FOR_FUNC);
		Accessibility Access    = (Accessibility)$Result.valueOf(enACCESS, $TPackage, $CProduct);
		MoreData      MData     = MoreData.newMoreDataFromArray($Result.valuesOf(enMOREDATA, $TPackage, $CProduct), true);
		
		if(Access == null) Access = Accessibility.Public;
		
		MExecutable     MExec    = $CProduct.getEngine().getExecutableManager();
		ElementResolver Resolver = null;
		Executable      Exec     = null;
		ExecKind        EKind    = null;
		switch(Kind) {
			case ('f'):
				EKind = ExecKind.Fragment;
				Exec = MExec.newFragment(Name, Interface.getReturnTypeRef(), Location, null, null, null, null);
				break;
			case ('m'):
				EKind = ExecKind.Macro;
				Exec = MExec.newMacro(Signature, null, null, null);
				break;
			case ('s'):
				EKind = ExecKind.SubRoutine;
				Exec = MExec.newSubRoutine(Signature, null, null, null);
				break;
		}

		if(Language != null) {
			int EIndex = $Result.getLastIndexOfEntryName(enLANGCODE);
			// Create the resolver
			Resolver = Util_ElementResolver.newOperResolver(false, Signature, EKind, $Result, EIndex, Language, $TPackage, $CProduct);
				
		} else {
			int EIndex = $Result.getLastIndexOfEntryName(enCURRYBODY);
			// Create the resolver
			Resolver = Util_ElementResolver.newOperResolver(false, Signature, EKind, $Result, EIndex, null, $TPackage, $CProduct);
				
		}

		StackOwnerAppender SOA = StackOwnerAppender.Util.newTempOperDirect(false, $CProduct, $TPackage, $Result,
				Access, Exec, MData, Resolver, Document);
			           
		return new FileCompileResult.PackageFunction(Signature, SOA);
	}
}
