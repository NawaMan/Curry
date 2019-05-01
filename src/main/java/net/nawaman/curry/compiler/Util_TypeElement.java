package net.nawaman.curry.compiler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Documentation;
import net.nawaman.curry.ExecInterface;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.Location;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Variable;
import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.util.UString;

public class Util_TypeElement {

	static public final String enCONSTRUCTOR = "#Constructor";
	static public final String enMETHOD      = "#Method";
	static public final String enFIELD       = "#Field";
	
	static public final String enSTATIC_DELEGATEE  = "$StaticDelegatee";
	static public final String enDYNAMIC_DELEGATEE = "$DynamicDelegatee";

	static public final String enFlag = "#Flag";
	
	/**
	 * Compile a set of TypeElements.
	 * 
	 * The elements' names must be "Constructor", "Method" and "Field".
	 * The rest will be ignored.
	 **/
	static public FileCompileResult.TypeElement<?>[] ParseCompileTypeElements(
			boolean AccpeptConstructor,
			boolean AccpetStaticField,  boolean AccpetAbstractField,  boolean AccpetField, 
			boolean AccpetStaticMethod, boolean AccpetAbstractMethod, boolean AccpetMethod,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// Structure Registration
		if(!$CProduct.getCompilationState().isStructuralRegistration()) return null;
		
		// Prepare the element list
		Vector<FileCompileResult.TypeElement<?>> Elements = new Vector<FileCompileResult.TypeElement<?>>();
			
		// Collect all the methods -------------------------------------------------------------------------------------
		ParseResult[] CPRs = $Result.subsOf(  enCONSTRUCTOR);
		Object[]      Cs   = $Result.valuesOf(enCONSTRUCTOR, $TPackage, $CProduct);
		
		for(int i = 0; i < ((Cs == null) ? 0 : Cs.length); i++) {
			Object C = Cs[i];
			if(C == null) continue;
			if(!(C instanceof FileCompileResult.TypeConstructor)) {
				$CProduct.reportError("Invalid return result for constructor. <TypeElements:53>", null, CPRs[i].getStartPosition());
				return null;
			}
			Elements.add((FileCompileResult.TypeElement<?>)C);
		}
			
		// Collect all the methods -------------------------------------------------------------------------------------
		ParseResult[] MPRs = $Result.subsOf(  enMETHOD);
		Object[]      Ms   = $Result.valuesOf(enMETHOD, $TPackage, $CProduct);
		
		for(int i = 0; i < ((Ms == null) ? 0 : Ms.length); i++) {
			Object M = Ms[i];
			if(M == null) continue;
			if(!(M instanceof FileCompileResult.TypeMethod)) {
				$CProduct.reportError("Invalid return result for method. <TypeElements:53>", null, MPRs[i].getStartPosition());
				return null;
			}
			Elements.add((FileCompileResult.TypeElement<?>)M);
		}
		
		// Collect all the attribute -----------------------------------------------------------------------------------
		ParseResult[] FPRs = $Result.subsOf(  enFIELD);
		Object[]      Fs   = $Result.valuesOf(enFIELD, $TPackage, $CProduct);
		
		for(int i = 0; i < ((Fs == null) ? 0 : Fs.length); i++) {
			Object F = Fs[i];
			if(F == null) continue;
			if(!(F instanceof FileCompileResult.TypeField)) {
				$CProduct.reportError("Invalid return result for field. <TypeElements:67>", null, FPRs[i].getStartPosition());
				return null;
			}
			Elements.add((FileCompileResult.TypeElement<?>)F);
		}
		
		return (Elements.size() == 0) ? null : Elements.toArray(new FileCompileResult.TypeElement[Elements.size()]);
	}

	static private HashMap<String, String> OperationPropertyNames = new HashMap<String, String>();
	static {
		OperationPropertyNames.put(Util_Element.enACCESS,   "accessibility");
		OperationPropertyNames.put(Util_Element.enDOCUMENT, "documentation");
		OperationPropertyNames.put(Util_Element.enABSTRACT, "abstract");
		OperationPropertyNames.put(Util_Element.enDYNAMIC,  "dynamic");
		OperationPropertyNames.put(Util_Element.enSTATIC,   "static");
		OperationPropertyNames.put(Util_Element.enMOREDATA, "more data");
	}

	/** Registers the structure */
	static public FileCompileResult.TypeMethod ParseCompileTypeMethod(String $Param,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		return ParseCompileTypeMethod($Param, Util_Element.prForInterface.equals($Param), $Result, $TPackage, $CProduct);
	}

	/** Registers the structure */
	static public FileCompileResult.TypeMethod ParseCompileTypeMethod(String $Param, boolean IsInterface, 
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		// Ensure the right state of the compilation
		if(!$CProduct.getCompilationState().isStructuralRegistration()) return null;

		// StructuralRegistration
		
		String Name = $Result.textOf(Util_Element.enNAME);

		// Quick validate ----------------------------------------------------------------------------------------------
		
		// Ensure no property is repeated
		if(Util_General.EnsureNoRepeat(
				$Result, $CProduct, true, null, " method " + Name, "Util_TypeElement:84", OperationPropertyNames,
				Util_Element.enACCESS, Util_Element.enDOCUMENT, Util_Element.enABSTRACT, Util_Element.enDYNAMIC,
				Util_Element.enSTATIC, Util_Element.enMOREDATA)) return null;

		// Prepare some parameters -------------------------------------------------------------------------------------
		boolean IsAbstract   = ($Result.textOf(Util_Element.enABSTRACT)  != null);
		boolean IsStatic     = ($Result.textOf(Util_Element.enSTATIC)    != null);
		boolean IsDynamic    = ($Result.textOf(Util_Element.enDYNAMIC)   != null);
		boolean IsDelegate   = ($Result.textOf(Util_Element.enDELEGATE)  != null);
		boolean HasCurryBody = ($Result.textOf(Util_Element.enCURRYBODY) != null);
		boolean HasJavaBody  = ($Result.textOf(Util_Element.enLANGCODE)  != null);
		boolean HasBody      = HasCurryBody || HasJavaBody;
		
		if(IsAbstract && IsStatic) {
			$CProduct.reportError("Static method cannot be abstrct <Util_TypeElement:99>.", null, $Result.posOf(Util_Element.enSTATIC));
			return null;
		}
		
		// Abstract method cannot be dynamic, delegate or have a body
		if(IsAbstract) {
			if(HasBody)    { $CProduct.reportError("Abstract method cannot have the body "+"<Util_TypeElement:105>.", null, $Result.posOf(Util_Element.enSTARTBODY)); return null; }
			if(IsDynamic)  { $CProduct.reportError("Abstract method cannot be dynamic "   +"<Util_TypeElement:106>.", null, $Result.posOf(Util_Element.enDYNAMIC));   return null; }
			if(IsDelegate) { $CProduct.reportError("Abstract method cannot be delegate "  +"<Util_TypeElement:107>.", null, $Result.posOf(Util_Element.enDELEGATE));  return null; }
		}

		// Cannot be more than one of these kind
		if((IsDynamic && HasBody) || (IsDelegate && HasBody) || (IsDynamic && IsDelegate)) {
			$CProduct.reportError("Method with body cannot be dynamic or delegate <Util_Element:105>.", null, $Result.posOf(0));
			return null;
		}

		// Unless it is for interface, the method needs to be one of these
		if(!IsAbstract && !IsDynamic && !IsDelegate && !HasBody && !IsInterface) {
			$CProduct.reportError("Missing method body <Util_Element:111>.", null, $Result.posOf(0));
			return null;
		}

		// The method is abstract, if it is for Interface
		if(IsInterface && !HasBody) IsAbstract = true;

		// Prepare the signature -------------------------------------------------------------------------------------------
		char   Kind     = Character.toLowerCase(Util_General.GetFirstCharOf($Result, Util_Element.enKIND, 's'));
		String Language = $Result.textOf(Util_Element.enLANGNAME);

		Accessibility Access   = (Accessibility)$Result.valueOf(Util_Element.enACCESS,   $TPackage, $CProduct);
		Documentation Document = (Documentation)$Result.valueOf(Util_Element.enDOCUMENT, $TPackage, $CProduct);
		
		// Default Access
		if(Access == null) Access = Util_Element.DEFAULT_TYPE_ELEMENT_ACCESSIBILITY;
		
		Location      Location  = Util_Curry.GetLocationOf($Result, $CProduct, "$Start");
		ExecInterface Interface = (ExecInterface)$Result.valueOf(Util_Element.enINTERFACE, $TPackage, $CProduct);
		ExecSignature Signature = ExecSignature.newSignature(Name, Interface, Location, null);
		MoreData      MData     = MoreData.newMoreDataFromArray($Result.valuesOf(Util_Element.enMOREDATA, $TPackage, $CProduct), true);

		if($Result.textOf(enFlag) != null) {
			Object[] Flags = $Result.valuesOf(enFlag, $TPackage, $CProduct);
			for(Object Each : Flags) {
				Object[] Flag = (Object[])Each;
				if(MData == null) MData = new MoreData();
				MData.setData((String)Flag[0], (Serializable)Flag[1]);
			}
		}
		
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
		
		StackOwnerAppender SOA      = null;
		if(IsAbstract) {
			SOA = StackOwnerAppender.Util.newAbstractOperDirect($CProduct, $TPackage, $Result,
					Access, Signature, EKind, MData, Document);
			
		} else if(IsDynamic) {
			SOA = StackOwnerAppender.Util.newOperDynamic(IsStatic, $CProduct, $TPackage, $Result,
					Access, Signature, MData, Document);
			
		} else if(IsDelegate) {
			SOA = StackOwnerAppender.Util.newOperDlgAttr(IsStatic, $CProduct, $TPackage, $Result, 
					Access, Signature, $Result.textOf(Util_Element.enDELEGATE_TARGETNAME), MData, Document);
			
		} else if(HasBody) {		
			if(Language != null) {
				int EIndex = $Result.getLastIndexOfEntryName(Util_Element.enLANGCODE);
				// Create the resolver
				Resolver = Util_ElementResolver.newOperResolver(IsStatic, Signature, EKind, $Result, EIndex, Language, $TPackage, $CProduct);
				
			} else {
				int EIndex = $Result.getLastIndexOfEntryName(Util_Element.enCURRYBODY);
				// Create the resolver
				Resolver = Util_ElementResolver.newOperResolver(IsStatic, Signature, EKind, $Result, EIndex, null, $TPackage, $CProduct);
				
			}

			SOA = StackOwnerAppender.Util.newTempOperDirect(IsStatic, $CProduct, $TPackage, $Result,
					Access, Exec, MData, Resolver, Document);
			
		} else {
			$CProduct.reportError(
					"Internal Error: An impossible statuc for TypeMethod compilation is found. Please report this to " +
					"the developer of Curry. <Util_TypeElement:191>", null, $Result.posOf(0));
			return null;
		}
        
		return new FileCompileResult.TypeMethod(Signature, IsStatic, IsAbstract, SOA);
	}
	
	static private HashMap<String, String> AttributePropertyNames = new HashMap<String, String>();
	static {
		AttributePropertyNames.put(Util_Element.enACCESS,       "read accessibility"  );
		AttributePropertyNames.put(Util_Element.enWRITEACCESS,  "write accessibility" );
		AttributePropertyNames.put(Util_Element.enCONFIGACCESS, "config accessibility");
		AttributePropertyNames.put(Util_Element.enNOTNULL,      "not-null flag"       );
		AttributePropertyNames.put(enSTATIC_DELEGATEE,          "static delegatee"    );
		AttributePropertyNames.put(enDYNAMIC_DELEGATEE,         "dynamic delegatee"   );
	}
	
	/** Registers the structure */
	static public FileCompileResult.TypeField ParseCompileTypeAttribute(
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		TypeRef TRef = (TypeRef)$Result.valueOf(Util_Element.enTYPE, $TPackage, $CProduct);
		return ParseCompileTypeAttribute(TRef, $Result, $TPackage, $CProduct);
	}
	
	/** Registers the structure */
	static public FileCompileResult.TypeField ParseCompileTypeAttribute(TypeRef TRef, 
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// Structure Registration
		if(!$CProduct.getCompilationState().isStructuralRegistration()) return null;

		String Name = $Result.textOf(Util_Element.enNAME);
		
		// Quick validate ----------------------------------------------------------------------------------------------
		
		// Ensure no property is repeated
		if(Util_General.EnsureNoRepeat(
				$Result, $CProduct, true, null, " type field " + Name, "Util_TypeElement:241",
				AttributePropertyNames,
				Util_Element.enACCESS,
				Util_Element.enWRITEACCESS,
				Util_Element.enCONFIGACCESS,
				Util_Element.enNOTNULL,
				Util_Element.enSTATIC))
			return null;
		
		if(Util_General.EnsureNoRepeat(
				$Result, $CProduct, true, null, " type field " + Name, "Util_TypeElement:250",
				AttributePropertyNames,
				Util_Element.enDOCUMENT,
				enSTATIC_DELEGATEE,
				enDYNAMIC_DELEGATEE))
			return null;

		Documentation Document = (Documentation)$Result.valueOf(Util_Element.enDOCUMENT,     $TPackage, $CProduct);
		Accessibility RAccess  = (Accessibility)$Result.valueOf(Util_Element.enACCESS,       $TPackage, $CProduct);
		Accessibility WAccess  = (Accessibility)$Result.valueOf(Util_Element.enWRITEACCESS,  $TPackage, $CProduct);
		Accessibility CAccess  = (Accessibility)$Result.valueOf(Util_Element.enCONFIGACCESS, $TPackage, $CProduct);
		MoreData      MD       = (MoreData)     $Result.valueOf(Util_Element.enMOREDATA,     $TPackage, $CProduct);

		if($Result.textOf(enFlag) != null) {
			Object[][] Flags = (Object[][])$Result.valuesOf(enFlag, $TPackage, $CProduct);
			for(Object[] Flag : Flags) MD.setData((String)Flag[0], (Serializable)Flags[1]);
		}
		
		boolean       IsStatic = ($Result.textOf(Util_Element.enSTATIC)     != null);
		boolean       NonNull  = ($Result.textOf(Util_Element.enNOTNULL)    != null);
		boolean       Writable = ($Result.textOf(Util_Element.enUNWRITABLE) == null);
		
		ParseResult   DValue =          $Result.subOf(  Util_Element.enDEFAULTVALUE);
		boolean       IsNull = (DValue == null) || "null".equals($Result.textOf(Util_Element.enDEFAULTVALUE));

		boolean IsSDlg = ($Result.textOf(enSTATIC_DELEGATEE)  != null);
		boolean IsDDlg = ($Result.textOf(enDYNAMIC_DELEGATEE) != null);
		
		if(!Writable && (WAccess != null)) {
			$CProduct.reportError("A constant is not writable <Util_TypeElement:238>", null,
					$Result.posOf(Util_Element.enWRITEACCESS));
			return null;
		}
		if(IsSDlg || IsDDlg) {
			if(IsSDlg) NonNull = true;
			if(IsStatic) {
				$CProduct.reportError("A static field cannot must not be a delegatee <Util_TypeElement:293>", null,
						$Result.posOf(Util_Element.enSTATIC));
				return null;
			}
			if(IsSDlg && IsNull) {
				$CProduct.reportError("A static-delegatee field is automatically a \"NonNull\" field and it cannot null <Util_TypeElement:298>", null,
						$Result.posOf(enSTATIC_DELEGATEE));
				return null;
			}
			
			if(MD == null) MD = new MoreData();
			
			MoreData theMD = MD;
			if(MD.isFrozen()) theMD = new MoreData();
			if(IsSDlg) theMD.setData(enSTATIC_DELEGATEE,  true);
			if(IsDDlg) theMD.setData(enDYNAMIC_DELEGATEE, true);
			if(MD.isFrozen()) theMD = MoreData.combineMoreData(theMD, MD);
			MD = theMD;
		}
		if(NonNull && IsNull) {
			$CProduct.reportError("The field must not be null <Util_TypeElement:284>", null,
					(DValue == null)?$Result.posOf(Util_Element.enNAME):$Result.posOf(Util_Element.enDEFAULTVALUE));
			return null;
		}
		
		int EIndex = -1;
		for(int i = $Result.count(); --i >= 0; ) {
			if(DValue != $Result.getSubOf(i)) continue;
			EIndex = i;
			break;
		}
		ElementResolver Resolver = (DValue == null) ? null :
		                    Util_ElementResolver.newAttrResolver(IsStatic, Name, $Result, EIndex, $TPackage, $CProduct);

		if(             RAccess == null ) RAccess = net.nawaman.curry.Package.Public;	// Default is public
		if(Writable && (WAccess == null)) WAccess = RAccess;	                        // Default is Read
		if(            (CAccess == null)) CAccess = net.nawaman.curry.Package.Package;	// Default is package !!!!!

		final DataHolderInfo DHI = new DataHolderInfo(TRef, null, Variable.FactoryName, true, Writable, true, true, null);
		final Location       Loc = Util_Curry.GetLocationOf($Result, $CProduct, "$Start");
		
		StackOwnerAppender SOA;
		
		if(IsStatic)
			 SOA = StackOwnerAppender.Util.newTempStaticAttrDirect(
				$CProduct, $TPackage, $Result, RAccess, WAccess, CAccess, Name, NonNull, DHI, Loc, MD, Resolver, Document);
		else SOA = StackOwnerAppender.Util.newTempAttrDirect(
				$CProduct, $TPackage, $Result, RAccess, WAccess, CAccess, Name, NonNull, DHI, Loc, MD, Resolver, Document);

		return new FileCompileResult.TypeField(Name, IsStatic, false, SOA);
	}

	/** Registers the structure */
	static public FileCompileResult.TypeConstructor ParseCompileTypeConstructor(String $Param,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		// Ensure the right state of the compilation
		if(!$CProduct.getCompilationState().isStructuralRegistration()) return null;

		// StructuralRegistration

		// Quick validate ----------------------------------------------------------------------------------------------

		// Prepare the signature ---------------------------------------------------------------------------------------
		String Language = $Result.textOf(Util_Element.enLANGNAME);
		if(Language != null) {
			$CProduct.reportError("Constructor must be in Curry language. <Util_TypeElement:375>", null,
					$Result.posOf(Util_Element.enLANGNAME));
			return null;
		}

		Accessibility Access   = (Accessibility)$Result.valueOf(Util_Element.enACCESS,   $TPackage, $CProduct);
		Documentation Document = (Documentation)$Result.valueOf(Util_Element.enDOCUMENT, $TPackage, $CProduct);
		
		// Prepare the Interface parameters
		ExecInterface Interface = (ExecInterface)$Result.valueOf(Util_Element.enINTERFACE, $TPackage, $CProduct);
		int PCount = Interface.getParamCount();
		TypeRef[] ITRefs = (PCount == 0) ? TypeRef.EmptyTypeRefArray : new TypeRef[PCount];
		String[]  INames = (PCount == 0) ? UString.EmptyStringArray  : new String[ PCount];
		for(int i = PCount; --i >= 0; ) {
			ITRefs[i] = Interface.getParamTypeRef(i);
			INames[i] = Interface.getParamName(   i);
		}
		
		// Default Access
		if(Access == null) Access = Util_Element.DEFAULT_TYPE_ELEMENT_ACCESSIBILITY;
		
		Location      Location  = Util_Curry   .GetLocationOf(  $Result, $CProduct, "$Start");
		ExecSignature Signature = ExecSignature.newSignature(   "new", ITRefs, INames, Interface.isVarArgs(), TKJava.TVoid.getTypeRef(), Location, null);
		MoreData      MData     = MoreData.newMoreDataFromArray($Result.valuesOf(Util_Element.enMOREDATA, $TPackage, $CProduct), true);
		
		int             EIndex   = $Result.getLastIndexOfEntryName(Util_Element.enCURRYBODY);
		ElementResolver Resolver = Util_ElementResolver.newConstructorResolver(Signature, $Result, EIndex, $TPackage, $CProduct);
		
		StackOwnerAppender SOA = StackOwnerAppender.Util.newConstructor($CProduct, $TPackage, $Result, Access, Signature, MData, Resolver, Document);
        
		return new FileCompileResult.TypeConstructor(Signature, SOA);
	}
}
