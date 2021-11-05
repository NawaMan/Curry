package net.nawaman.curry.compiler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.nawaman.curry.Context;
import net.nawaman.curry.Engine;
import net.nawaman.curry.EngineExtensions;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Inst_Assignment;
import net.nawaman.curry.Instruction;
import net.nawaman.curry.Location;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.MType;
import net.nawaman.curry.Package;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.StackOwner;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TLPrimitive;
import net.nawaman.curry.TLType;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeInfo;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Inst_Assignment.DecAfter;
import net.nawaman.curry.Inst_Assignment.DecBefore;
import net.nawaman.curry.Inst_Assignment.IncAfter;
import net.nawaman.curry.Inst_Assignment.IncBefore;
import net.nawaman.curry.Inst_Assignment.OperatorProvider;
import net.nawaman.curry.Instructions_Array.Inst_GetArrayElementAt;
import net.nawaman.curry.Instructions_Array.Inst_GetLengthArrayObject;
import net.nawaman.curry.Instructions_Context.Inst_GetContextInfo;
import net.nawaman.curry.Instructions_Context.Inst_GetVarValue;
import net.nawaman.curry.Instructions_Context.Inst_NewConstant;
import net.nawaman.curry.Instructions_Core.Inst_Cast;
import net.nawaman.curry.Instructions_Core.Inst_CastOrElse;
import net.nawaman.curry.Instructions_Core.Inst_GetTypeInfo;
import net.nawaman.curry.Instructions_Core.Inst_ToString;
import net.nawaman.curry.Instructions_Core.Inst_getTypeOf;
import net.nawaman.curry.Instructions_DefaultPackage.Inst_GetDefaultPackage;
import net.nawaman.curry.Instructions_ForSpeed.Inst_DoWhenNoNull;
import net.nawaman.curry.Instructions_ForSpeed.Inst_DoWhenValidIndex;
import net.nawaman.curry.Instructions_ForSpeed.Inst_TryNoNull;
import net.nawaman.curry.Instructions_Java.Inst_GetJavaClassFieldValue;
import net.nawaman.curry.Instructions_Java.Inst_GetJavaClassFieldValueByField;
import net.nawaman.curry.Instructions_Java.Inst_GetJavaField;
import net.nawaman.curry.Instructions_Java.Inst_GetJavaMethodByParamClasss;
import net.nawaman.curry.Instructions_Java.Inst_GetJavaObjectFieldValue;
import net.nawaman.curry.Instructions_Java.Inst_GetJavaObjectFieldValueByField;
import net.nawaman.curry.Instructions_Java.Inst_InvokeJavaClassMethod;
import net.nawaman.curry.Instructions_Java.Inst_InvokeJavaClassMethodByMethod;
import net.nawaman.curry.Instructions_Java.Inst_InvokeJavaObjectMethod;
import net.nawaman.curry.Instructions_Java.Inst_InvokeJavaObjectMethodByMethod;
import net.nawaman.curry.Instructions_Operations.InstCharAt;
import net.nawaman.curry.Instructions_Operations.InstLength;
import net.nawaman.curry.Instructions_Package.Inst_GetCurrentPackage;
import net.nawaman.curry.Instructions_Package.Inst_GetPackage;
import net.nawaman.curry.Instructions_StackOwner.Inst_ConfigAttrAsType;
import net.nawaman.curry.Instructions_StackOwner.Inst_GetAttrMoreInfoAsType;
import net.nawaman.curry.Instructions_StackOwner.Inst_GetAttrValue;
import net.nawaman.curry.Instructions_StackOwner.Inst_GetAttrValueAsType;
import net.nawaman.curry.Instructions_StackOwner.Inst_Invoke;
import net.nawaman.curry.Instructions_StackOwner.Inst_InvokeAsType;
import net.nawaman.curry.Instructions_StackOwner.Inst_Invoke_ByParams;
import net.nawaman.curry.Instructions_StackOwner.Inst_packageGetAttrValue;
import net.nawaman.curry.Instructions_StackOwner.Inst_packageInvoke;
import net.nawaman.curry.Instructions_StackOwner.Inst_superInvoke;
import net.nawaman.curry.Instructions_StackOwner.Inst_thisGetAttrValue;
import net.nawaman.curry.Instructions_StackOwner.Inst_thisInvoke;
import net.nawaman.curry.Instructions_StackOwner.Inst_typeGetAttrValue;
import net.nawaman.curry.Instructions_StackOwner.Inst_typeInvoke;
import net.nawaman.curry.TKVariant.TVariant;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.compiler.CompileProduct.CompileTimeChecking;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.util.UArray;
import net.nawaman.util.UClass;
import net.nawaman.util.UObject;

public class Util_Term_Component {

	static boolean CheckParam(CompileProduct CP, Object O, TypeRef pPosibleType, String pOperator, String OperandStr, boolean IsSwap, int pPosition) {
		if(CP.isCompileTimeCheckingNone()) return true;
		
		TypeRef TRef    = CP.getReturnTypeRefOf(O);
		Boolean IsMatch = IsSwap
							? CP.getEngine().getTypeManager().mayTypeRefBeCastedTo(TRef, pPosibleType)
							: CP.getEngine().getTypeManager().mayTypeRefBeCastedTo(pPosibleType, TRef);
		if(Boolean.TRUE.equals(IsMatch)) return true;
		if(IsMatch == null)
			 CP.reportWarning(String.format("Invalid operand for '%s' (%s: %s)", pOperator, OperandStr, TRef), null, pPosition);
		else CP.reportError(  String.format("Invalid operand for '%s' (%s: %s)", pOperator, OperandStr, TRef), null, pPosition);
		return false;
	}
	
	static TypeRef TN = TKJava.TNumber .getTypeRef();

	/** Compile a new instance expression */
	static public Expression CompileTerm(Object Operand, String TName, String $OperandStr, String $Before, String $After,
			boolean[] IsDefaults, String[] $Prefixes, int[][]  $Locations, int $BeforePos, int $OperandPos, int $AfterPos,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// Get the engine
		Engine      $Engine = $CProduct.getEngine();
		MExecutable $ME     = $Engine.getExecutableManager();
		
		Expression Expr = null;
		
		if(($Before != null) || ($After != null)) {
			if(($Before != null) && ($After != null)) {
				$CProduct.reportError("The operation "+$Before+" and "+$After+" cannot be used at the same time", null, $BeforePos);
				return null;
			} else {
				// Get the source assignment
				if($Before == null) $Before = $After;
				Inst_Assignment InstAssign = (Inst_Assignment)$Engine.getInstruction(Inst_Assignment.Name);
	
				int SourceHash = (!(Operand instanceof Expression))?-1:InstAssign.getSourceHashOf(Expression.getInstructionName((Expression)Operand, $Engine));
				if(SourceHash == -1) {
					$CProduct.reportFatalError("Invalid assignment `"+$OperandStr+"`<Util_Term_Component:46>", null, $OperandPos);
					return null;
				}
	
				// Prepare the process
				boolean isInc        = ($Before.charAt(0) == '+');
				boolean isAfter      = ($After != null);
				int     OperNameHash =   -1;
				int     Value        =    1;
				if(     isInc  &&  isAfter) { OperNameHash = IncAfter .NameHash; Value =  1; }
				else if(isInc  && !isAfter) { OperNameHash = IncBefore.NameHash; Value =  1; }
				else if(!isInc &&  isAfter) { OperNameHash = DecAfter .NameHash; Value = -1; }
				else if(!isInc && !isAfter) { OperNameHash = DecBefore.NameHash; Value = -1; }
	
				if(!CheckParam($CProduct, Operand, TN, isInc?"++":"--", $OperandStr, true, $OperandPos))
					return null;
	
				OperatorProvider OP = InstAssign.getOperatorProvider(OperNameHash);
				if(OP == null) {
					$CProduct.reportFatalError("Unknown operator <Util_Term_Component:72>", null, $Result.posOf(0));
					return null;
				}
	
				// Create the Expression
				Expr = Inst_Assignment.newAssExpr($Engine, (Expression)Operand, OP.getName(), Value);
			}
		} else {
			// Find the index of the component
			for(int i = ($Prefixes == null) ? 0 : $Prefixes.length; --i >= 0; ) {
				String InstName = $Prefixes[i];
				switch(InstName.charAt(0)) {
					case '+': continue;
					case '-': InstName = "neg"; break;
					case '!': InstName = "NOT"; break;
				}
				Operand = $ME.newExpr($Locations[i], InstName, Operand);
				if(!((Expression)Operand).ensureParamCorrect($CProduct)) return null;
			}
		
			Expr = Expression.toExpr(Operand);
		}
		
		// Have ?? or ?$
		if(IsDefaults != null) {
			for(int i = 0; i < IsDefaults.length; i++) {
				if(IsDefaults[i]) {
					Expr = $ME.newExpr(
						$Result.locationCROf(0),
						Inst_TryNoNull.Name,
						Expr,
						$ME.newType($CProduct.getReturnTypeRefOf(Expr)));
				} else {
					Expr = $ME.newExpr(
						$Result.locationCROf(0),
						Inst_ToString.Name,
						Expr);
				}
			}
		}
		
		return Expr;
	}

	/** Set the compile-time checking */
	static void SetChecking(CompileProduct $CProduct, char Check) {
		switch(Check) {
			case '.': { $CProduct.setCompileTimeChecking(CompileTimeChecking.Full); break; }
			case '-': { $CProduct.setCompileTimeChecking(CompileTimeChecking.None); break; }
		}
	}
	/** Reset the compile-time checking setting */
	static void ResetCompileTimeChecking(CompileProduct $CProduct, CompileTimeChecking CheckingFlag) {
		$CProduct.setCompileTimeChecking(CheckingFlag);
	}
	/** Validate the expression parameters and Manipulate the compile context at finish point */
	static boolean CheckExpr(Expression Expr, CompileProduct $CProduct) {
		return Expr.ensureParamCorrect($CProduct) && Expr.manipulateCompileContextFinish($CProduct);
	}
	/** Prepare the parameter arrays */
	static void PrepareParams(Engine $Engine, CompileProduct $CProduct, boolean HasParams, boolean IsSOEE, boolean IsJavaEE, int PCount,
					Object[] Params, Class<?>[] PClss, TypeRef[] PTRefs) {
		if(!(HasParams && (PCount != 0) && (IsSOEE || IsJavaEE))) return;
		for(int i = PCount; --i >= 0; ) {
			TypeRef PTRef = $CProduct.getReturnTypeRefOf(Params[i]);
			// Class
			PClss[i] = (PTRef == null)? null : PTRef.getDataClass($Engine);
			// TypeRef
			while(true) {
				if(PTRef instanceof TLType.TypeTypeRef) PTRef = ((TLType.TypeTypeRef)PTRef).getTheRef();
				else if(PTRef == null)                { PTRef = null; break; }
				else break;
			}
			PTRefs[i] = PTRef;
		}
	}
	
	static public final String enACCESS            = "$Access";
	static public final String enNULL_AWARE_ACCESS = "$NullAwareAccess";
	static public final String enACCNAME           = "$AccName";
	static public final String enINDEX_BEGIN       = "$IndexBegin";
	static public final String enHAS_PARAMS        = "$HasParams";
	static public final String enOPERAND           = "#Operand";
	static public final String enINDEX             = "#Index";
	static public final String enNULL_AWARE_INDEX  = "$NullAwareIndex";
	static public final String enPARAMS            = "#Params";
	static public final String enCLOSURE           = "#Closure";
	
	/** Do Creator */
	static abstract class DoCreator {
		abstract Expression createDo(MExecutable $ME, int[] LocationCR, Object Host, Object P0, Object P1, Object P2, Object P3);
	}
	
	/** Do Creator */
	static class DCSimple extends DoCreator {
		DCSimple(String pInstName) { this.InstName = pInstName; }
		String InstName;
		@Override Expression createDo(MExecutable $ME, int[] LocationCR, Object Host, Object P0, Object P1, Object P2, Object P3) {
			return $ME.newExpr(LocationCR, InstName, Host);
		}
	}
	
	/** Do Creator */
	static class DCDynamic extends DoCreator {
		DCDynamic() {}
		@Override Expression createDo(MExecutable $ME, int[] LocationCR, Object Host, Object P0, Object P1, Object P2, Object P3) {
			if(((Integer)P0).intValue() == 0) return $ME.newExpr(LocationCR, (String)P1, Host);
			if(((Integer)P0).intValue() == 1) return $ME.newExpr(LocationCR, (String)P1, Host, P2);
			if(((Integer)P0).intValue() == 2) return $ME.newExpr(LocationCR, (String)P1, Host, P2, P3);
			throw new IllegalArgumentException();
		}
	}
	/** Do Creator */
	static class DCVarArgs extends DoCreator {
		DCVarArgs() {}
		@Override Expression createDo(MExecutable $ME, int[] LocationCR, Object Host, Object P0, Object P1, Object P2, Object P3) {
			// Replace with host
			int I = ((Integer)P2).intValue();
			if(I != -1) {
				P1 = ((Object[])P1).clone();
				((Object[])P1)[I] = Host;
			}
			
			return $ME.newExpr(LocationCR, (String)P0, (Object[])P1);
		}
	}
	
	static public final DoCreator dcDYNAMIC = new DCDynamic();
	static public final DoCreator dcVARARGS = new DCVarArgs();
	
	static public final DoCreator dcGETCLASS = new DoCreator() {
		@Override Expression createDo(MExecutable $ME, int[] LocationCR, Object Host, Object P0, Object P1, Object P2, Object P3) {
			return $ME.newExpr(LocationCR, Inst_GetTypeInfo.Name,
					$ME.newExpr(LocationCR, Inst_getTypeOf.Name, Host), Inst_GetTypeInfo.DataClass);
		}
	};
	
	static public final DoCreator dcCHARAT = new DoCreator() {
		@Override Expression createDo(MExecutable ME, int[] LocationCR, Object Host, Object P0, Object P1, Object P2, Object P3) {
			if(Boolean.TRUE.equals(P0)) return ME.newExpr(LocationCR, InstCharAt.Name, Host,                                 P1);
			else                        return ME.newExpr(LocationCR, InstCharAt.Name, Host, ME.newExpr(LocationCR, "toInt", P1));
		}
	};

	/** Create Null aware access */
	static public final Expression CreateNullAwareAccess(CompileProduct $CProduct, MExecutable $ME,
			boolean IsAccessNA, DoCreator DC, int[] Location, Object Operand) {
		return CreateNullAwareAccess($CProduct, $ME, IsAccessNA, DC, Location, Operand, null, null, null, null);
	}
	/** Create Null aware access */
	static public final Expression CreateNullAwareAccess(CompileProduct $CProduct, MExecutable $ME,
			boolean IsAccessNA, DoCreator DC, int[] Location, Object Operand, Object P0) {
		return CreateNullAwareAccess($CProduct, $ME, IsAccessNA, DC, Location, Operand, P0, null, null, null);
	}
	/** Create Null aware access */
	static public final Expression CreateNullAwareAccess(CompileProduct $CProduct, MExecutable $ME,
			boolean IsAccessNA, DoCreator DC, int[] Location, Object Operand, Object P0, Object P1) {
		return CreateNullAwareAccess($CProduct, $ME, IsAccessNA, DC, Location, Operand, P0, P1, null, null);
	}
	/** Create Null aware access */
	static public final Expression CreateNullAwareAccess(CompileProduct $CProduct, MExecutable $ME,
			boolean IsAccessNA, DoCreator DC, int[] Location, Object Operand, Object P0, Object P1, Object P2) {
		return CreateNullAwareAccess($CProduct, $ME, IsAccessNA, DC, Location, Operand, P0, P1, P2, null);
	}
	/** Create Null aware access */
	static public final Expression CreateNullAwareAccess(CompileProduct $CProduct, MExecutable $ME,
			boolean IsAccessNA, DoCreator DC, int[] Location, Object Operand, Object P0, Object P1, Object P2, Object P3) {
		// No access NA
		if(!IsAccessNA) return DC.createDo($ME, Location, Operand, P0, P1, P2, P3);
		
		Object     Host = Operand;
		Expression Expr;
		
		boolean IsVar = ((Operand instanceof Expression) &&
			                 ((Expression)Operand).isInstruction($CProduct.getEngine(), Inst_GetVarValue.Name));

		String  VName = null;
		TypeRef OTR   = null; 
		
		if(!IsVar) {
			while($CProduct.isVariableExist(VName = ("" + $CProduct.getTempVarNumber())));
			Host = $ME.newExpr(Location, Inst_GetVarValue.Name, VName);
			
			OTR = $CProduct.getReturnTypeRefOf(Operand);
			$CProduct.newConstant(VName, ((OTR == null) ? TKJava.TAny.getTypeRef() : OTR));
		}
			
		Expression Do = DC.createDo($ME, Location, Host, P0, P1, P2, P3);
		TypeRef    TR = $CProduct.getReturnTypeRefOf(Do);
		Expr = $ME.newExpr(Location, Inst_DoWhenNoNull.Name,
				Host,
				Expression.newExpr(Do),
				$ME.newType(Location, TR)
			);
			
		// Wrap it
		if(!IsVar) {
			Expression VarType = $ME.newType(Location, ((OTR == null) ? TKJava.TAny.getTypeRef() : OTR));
			Expression VarExpr = $ME.newExpr(Location, Inst_NewConstant.Name, VName, VarType, Operand);
			
			Expr = $ME.newGroup(Location, VarExpr, Expr);
		}

		return Expr;
	}

	/** Create Null aware access */
	static public final Expression CreateNullAwareIndex(CompileProduct $CProduct, MExecutable $ME,
			boolean IsAccessNA, int[] Location, Object Operand, Object Index) {
		// No access NA
		if(!IsAccessNA) return $ME.newExpr(Location, Inst_GetArrayElementAt.Name, Operand, Index);
		
		Object     Host = Operand;
		Expression Expr;
		
		boolean IsVar = ((Operand instanceof Expression) &&
			                 ((Expression)Operand).isInstruction($CProduct.getEngine(), Inst_GetVarValue.Name));

		String  VName = null;
		TypeRef OTR   = null; 
		
		if(!IsVar) {
			while($CProduct.isVariableExist(VName = ("" + $CProduct.getTempVarNumber())));
			Host = $ME.newExpr(Location, Inst_GetVarValue.Name, VName);
			
			OTR = $CProduct.getReturnTypeRefOf(Operand);
			$CProduct.newConstant(VName, ((OTR == null) ? TKJava.TAny.getTypeRef() : OTR));
		}
			
		Expression Do = $ME.newExpr(Location, Inst_GetArrayElementAt.Name, Host, Index);
		TypeRef    TR = $CProduct.getReturnTypeRefOf(Do);
		Expr = $ME.newExpr(Location, Inst_DoWhenValidIndex.Name,
				Host, Index,
				Expression.newExpr(Do),
				$ME.newType(Location, TR)
			);
			
		// Wrap it
		if(!IsVar) {
			Expr = $ME.newGroup(
				Location,
				$ME.newExpr(
					Location,
					Inst_NewConstant.Name, VName,
					$ME.newType(Location, OTR),
					Operand),
				Expr
			);
		}

		return Expr;
	}

	/** RegPaser for parsing instruction */
	static RegParser InstPattern = RegParser.newRegParser("($Pre:~(^get)*~)get($Mid:~(^Value)*~)Value($Post:~.*~)");
	
	/** Compile a new instance expression */
	static public Object ParseCompileComponent(ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		// Get the engine
		final Engine      $Engine = $CProduct.getEngine();
		final MExecutable $ME     = $Engine.getExecutableManager();

		// Often used values
		final String  Access      =  $Result.textOf(enACCESS);
		final String  AccName     =  $Result.textOf(enACCNAME);
		final boolean HasIndexed  = ($Result.textOf(enINDEX_BEGIN)       != null);
		final boolean HasAccName  = (AccName != null);
		      boolean HasParams   = ($Result.textOf(enHAS_PARAMS)        != null);
		final boolean IsAccessNA  = ($Result.textOf(enNULL_AWARE_ACCESS) != null);
		final int     Position    =  $Result.posOf(0);
		
		EE_Language EEL = (EE_Language)$Engine.getExtension(EE_Language.Name);
		boolean IsAccess_this_AsVar = (EEL != null) && EEL.isStackOwnerVariableShouldBeTreatedAsVariable; 

		// Determine the compile-time checking of this instruction and preserve the old value
		CompileTimeChecking CheckingFlag = $CProduct.getCompileTimeChecking();
		char    Check        = (Access != null) ? Access.charAt(0) : $CProduct.isCompileTimeCheckingFull() ? '.' : '-';		
		boolean IsCheckFull  = (Check == '.');
		boolean IsDHExtra    = false; 
		boolean IsToAsType   = true;
		
		// Access to DataHolder config or more info
		if("->>".equals(Access)) {
			Check       = '.';
            IsCheckFull = true;
            IsDHExtra   = true;
            IsToAsType  = false; 
		}

		// If there is no special feature of Component, just return the value it is just an atom
		if(!(HasIndexed || HasAccName || HasParams))
			return $Result.valueOf(enOPERAND, $TPackage, $CProduct);

		Expression Expr = null;

		try {
			// Set the compile time checking -------------------------------------------------------------------------------
			SetChecking($CProduct, Check);

			boolean IsGetExpr = false;
			GetExpr: while(!IsGetExpr) {
				IsGetExpr = true;

				if(HasIndexed) { // Process indexes
					final String[]  NAIndexes = $Result.textsOf(      enNULL_AWARE_INDEX); 
					final Object[]  Indexes   = $Result.valuesOf(     enINDEX, $TPackage, $CProduct);
					final int[][]   Locations = $Result.locationCRsOf(enINDEX);

					// Loop all the index from the first to the last
					for(int i = 0; i < Indexes.length; i++) {
						// Create access to array element expression
						Object Operand = (Expr == null) ? $Result.valueOf(enOPERAND, $TPackage, $CProduct) : Expr;
						
						Expr = CreateNullAwareIndex($CProduct, $ME, (NAIndexes[i].length() != 0), Locations[i], Operand, Indexes[i]);
						if((i != (Indexes.length - 1)) && !CheckExpr(Expr, $CProduct)) return (Expr = null);
					}

					break GetExpr;
				}

				// Get the location
				final int[] Location = $Result.locationCROf(enACCESS);

				// Operand
				String  $Operand = $Result.textOf(enOPERAND); if($Operand == null) $Operand = "";
				char    $Prefix  = $Operand.charAt(0);
				if(($Prefix == '@') || ($Prefix == '#')) $Operand = $Operand.substring(1); else $Prefix  = '@';

				// Flag about the available packaget that often use	----------------------------------------------
				boolean IsSOEE   = ($Engine.getExtension(EngineExtensions.EE_StackOwner.Name)             != null);
				boolean IsSOCEE  = ($Engine.getExtension(EngineExtensions.EE_StackOwnerCustomizable.Name) != null);
				boolean IsJavaEE = ($Engine.getExtension(EngineExtensions.EE_Java.Name)                   != null);

				// Prepare the parameter information -------------------------------------------------------------
				Object[] Params = HasParams ? (Object[])$Result.valueOf(enPARAMS, $TPackage, $CProduct) : UObject.EmptyObjectArray;
				// Add the closure
				if($Result.textOf(enCLOSURE) != null) {
					// Compile the closure
					Object Closure = $Result.valueOf(enCLOSURE, $TPackage, $CProduct);
					// Creates parameter array that include the closure
					Object[] NewParams = new Object[(Params == null) ? 0 : Params.length + 1];
					if(Params != null) System.arraycopy(Params, 0, NewParams, 0, NewParams.length - 1);
					NewParams[NewParams.length - 1] = Closure;
					
					// Replace it
					Params = NewParams;
				}
				
				
				int        PCount = Params.length;
				Class<?>[] PClss  = new Class<?>[PCount];
				TypeRef[]  PTRefs = new TypeRef [PCount];
				PrepareParams($Engine, $CProduct, HasParams, IsSOEE, IsJavaEE, PCount, Params, PClss, PTRefs);

				// Access --------------------------------------------------------------------------------------------------
				boolean IsGetAccExpr = false;
				GetAccExpr: while(HasAccName && !IsGetAccExpr) {
					IsGetAccExpr = true;

					// Predefined Suffix =========================================================================

					if(!IsDHExtra && (PCount == 0)) {
						// Array and String length ---------------------------------------------------------------
						if("length".equals(AccName)) {
							Object  Operand = $Result.valueOf(enOPERAND, $TPackage, $CProduct);
							TypeRef TRef    = $CProduct.getReturnTypeRefOf(Operand);

							// Array .length -----------------------------------------------------------
							if(!HasParams && MType.CanTypeRefByAssignableByInstanceOf(null, $Engine, TKArray.AnyArrayRef, TRef))
								Expr = CreateNullAwareAccess($CProduct, $ME, IsAccessNA, dcDYNAMIC, Location, Operand, 0, Inst_GetLengthArrayObject.Name);

							// String .length() --------------------------------------------------------
							if(HasParams && (PCount == 0) && TKJava.TString.getTypeRef().equals(TRef))
								Expr = CreateNullAwareAccess($CProduct, $ME, IsAccessNA, dcDYNAMIC, Location, Operand, 0, InstLength.Name);

						} else if(HasParams) {

							// .toString(), .toDetail() -------------------------------------------
							if("toString".equals(AccName) || "toDetail".equals(AccName)) {
								Object Operand = $Result.valueOf(enOPERAND, $TPackage, $CProduct);
								Expr = CreateNullAwareAccess($CProduct, $ME, IsAccessNA, dcDYNAMIC, Location, Operand, 0, AccName);
							// .hashCode(), .hash() -----------------------------------------------
							} else if("hashCode".equals(AccName) || "hash"    .equals(AccName)) {
								Object Operand = $Result.valueOf(enOPERAND, $TPackage, $CProduct);
								// NOTE: these are method that is null-proof so we do not need to create doWhenNoNull
								// ------------------------------------------vvvvv
								Expr = CreateNullAwareAccess($CProduct, $ME, false, dcDYNAMIC, Location, Operand, 0, AccName);
							// .getType() ---------------------------------------------------------
							} else if("getType".equals(AccName)) {
								Object Operand = $Result.valueOf(enOPERAND, $TPackage, $CProduct);
								Expr = CreateNullAwareAccess($CProduct, $ME, IsAccessNA, dcDYNAMIC, Location, Operand, 0, Inst_getTypeOf.Name);
							// .getClass() --------------------------------------------------------
							} else if("getClass".equals(AccName))
								Expr = CreateNullAwareAccess($CProduct, $ME, IsAccessNA, dcGETCLASS, Location,
										$Result.valueOf(enOPERAND, $TPackage, $CProduct));
						}

					} else if(!IsDHExtra && (PCount == 1)) {

						// String .charAt  -----------------------------------------------------------------------
						if("charAt".equals(AccName)) {
							final Object Operand = $Result.valueOf(enOPERAND, $TPackage, $CProduct);
							TypeRef TRef = $CProduct.getReturnTypeRefOf(Operand);
							if(MType.CanTypeRefByAssignableByInstanceOf(null, $Engine, TKJava.TString.getTypeRef(), TRef)) {
								TypeRef PTRef = PTRefs[0];
								// Integer
								if(MType.CanTypeRefByAssignableByInstanceOf(null, $Engine, TKJava.TInteger.getTypeRef(), PTRef))
									Expr = CreateNullAwareAccess($CProduct, $ME, IsAccessNA, dcCHARAT, Location, Operand, false, Params[0]);

								// The rest of the integer number
								else if(MType.CanTypeRefByAssignableByInstanceOf(null, $Engine, TKJava.TByte .getTypeRef(), PTRef) ||
										MType.CanTypeRefByAssignableByInstanceOf(null, $Engine, TKJava.TShort.getTypeRef(), PTRef) ||
										MType.CanTypeRefByAssignableByInstanceOf(null, $Engine, TKJava.TLong .getTypeRef(), PTRef))
									Expr = CreateNullAwareAccess($CProduct, $ME, IsAccessNA, dcCHARAT, Location, Operand, true, Params[0]);
									
							}

						// Is, Equals, Compares ------------------------------------------------------------------
						} else if("is".equals(AccName) || "equals".equals(AccName) || "compares".equals(AccName)) {
							Object Operand = $Result.valueOf(enOPERAND, $TPackage, $CProduct);
							// NOTE: these are method that is null-proof so we do not need to create doWhenNoNull
							// ------------------------------------------vvvvv
							Expr = CreateNullAwareAccess($CProduct, $ME, false, dcDYNAMIC, Location, Operand, 1, AccName, Params[0]);
						}
					}

					if(Expr != null)
						break GetExpr;

					// From this point JavaEE or StackOwner is required
					if(!IsSOEE && !IsJavaEE) {
						$CProduct.reportFatalError(
							"In order to use access or invocation either or both StackOwner or/and Java engine extension "+
							"must be part of the engine <Component:538>",
							null, $Result.posOf(0)
						);
						return null;
					}

					// Variables to be used in the formation of Access and Invocation Expression -----------------
					Object  Operand    = null;
					TypeRef TRef       = null;
					Package ThePackage = null;
					boolean IsSelected = false;
					boolean IsThis     = false;
					boolean IsSuper    = false;
					boolean IsType     = false;
					boolean IsTypeRef  = false;
					boolean IsPackage  = false;
					boolean IsStatic   = false;
					boolean IsAsType   = false;

					if((Context.StackOwner_VarName.equals($Operand) || "this".equals($Operand)) && IsSOCEE) {
						// This ----------------------------------------------------------------------------
						// StackOwner customizable
						if(!$CProduct.isOwnerObject() && ($Prefix == '@')) {
							$CProduct.reportError(
								"The current context is not under an object <Component:541>.",
								null, Position
							);
							return null;
						}
						
						TRef = $CProduct.getOwnerTypeRef();
						if(TRef == null) TRef = TKJava.TAny.getTypeRef();

						if(IsAccess_this_AsVar)
							 Operand = $ME.newExpr(Location, Inst_GetVarValue.Name, $Operand);
						else IsThis  = true;
						IsSelected   = true;
						
					} else if(("$Super$".equals($Operand) || "super".equals($Operand)) && IsSOCEE) {
						// StackOwner customizable
						if(!$CProduct.isOwnerObject() && ($Prefix == '@')) {
							$CProduct.reportError(
								"The current context is not under an object <Component:551>.",
								null, Position
							);
							return null;
						}

						TypeRef TR = $CProduct.getOwnerTypeRef();
						if(!TypeInfo.isTypeHasSuper($Engine, TR) && ($Prefix == '@')) {
							// TODO - This is a hack
							if(!TypeInfo.isTypeHasSuper($Engine, TR) && ($Prefix == '@')) {
								$CProduct.reportError(
									"The current context is not under an object of a type with super <Component:561>.",
									null, Position
								);
								return null;
							}
						}

						IsSuper    = true;
						IsSelected = true;
						TRef       = TypeInfo.getSuperRefOf($Engine, TR);

					} else if(Context.StackOwnerAsType_VarName.equals($Operand) && IsSOCEE) {
						// StackOwner customizable
						TRef = $CProduct.getOwnerTypeRef();
						if((TRef == null) && ($Prefix == '@')) {
							$CProduct.reportError(
								String.format("Unavailable type variable '%s' <Component:569>",$Operand),
								null, $Result.posOf(enOPERAND)
							);
							return null;
						}

						if(IsAccess_this_AsVar)
							Operand = $ME.newExpr(Location, Inst_GetVarValue.Name, $Operand);
						else {
							IsType   = true;
							IsStatic = true;

							int[] OPERANDCR = $Result.locationCROf(enOPERAND);
							Operand = (TRef != null)
							               ? $ME.newType(OPERANDCR, TRef)
							               : $ME.newExpr(OPERANDCR, Inst_GetContextInfo.Name, "StackOwner_As_Type");
						}
						
						IsSelected = true;

					} else if(Context.StackOwnerAsPackage_VarName.equals($Operand)) {
						if(IsSOCEE) {
							PackageBuilder PB = $CProduct.getOwnerPackageBuilder();
							if(PB == null) {
								$CProduct.reportError(
									String.format("Unavailable package variable '%s' <Component:583>", $Operand),
									null, $Result.posOf(enOPERAND)
								);
								return null;
							}
							ThePackage = PB.getPackage();
							IsPackage  = true;
							IsSelected = true;

						} else if(IsSOEE) {
							// Process Package as if it is a variable
							Instruction Inst = null;
							// If the engine does not support current package, try default package
							if(Inst == null) Inst = $Engine.getInstruction(Inst_GetCurrentPackage.Name);
							if(Inst == null) Inst = $Engine.getInstruction(Inst_GetDefaultPackage.Name);
							if(Inst == null) {
								$CProduct.reportError(
									String.format("Unavailable package variable '%s' <Component:613>.", $Operand),
									null, $Result.posOf(enOPERAND)
								);
								return null;
							}

							PackageBuilder PB = null;
							Operand    = Inst.newExpression_Coordinate(Location);
							ThePackage = (Inst == $Engine.getInstruction(Inst_GetCurrentPackage.Name))
										? (((PB = $CProduct.getOwnerPackageBuilder()) == null)?null:PB.getPackage())
										: $Engine.getDefaultPackage();
							IsPackage  = true;
							IsSelected = true;
						}
					}

					if(!IsSelected) {
						try {
							Object Saved = null;

							try {
								$CProduct.toStartTry();
								ResetCompileTimeChecking($CProduct, CheckingFlag);	// Restore the compile-time checking setting TEMPORARILY
								PType       PT = $TPackage.getType("TypeRef");
								ParseResult PR = PT.match($Operand, 0, $TPackage);
								Operand        = null;
								if(PR != null) { 
									try {
										Operand = PT.compile(PR, null, $CProduct, $TPackage);
										Type T  = $CProduct.getTypeAtCompileTime((TypeRef)Operand);
										Operand = T.getTypeRef();
									} catch(Exception E) {
										Operand = null;
									}
								}
							} finally { SetChecking($CProduct, Check); }			// Set back

							if(Operand instanceof TypeRef) {		// So it is a TypeRef
								TRef     = (TypeRef)Operand;
								Operand  = $ME.newType(Location, TRef);
								IsTypeRef = true;
								IsStatic  = true;
								
							} else {	// Not found try as typeref
								Saved = $CProduct.toReStartTry();
								try { Operand = $Result.valueOf(enOPERAND, $TPackage, $CProduct); }
								catch (Exception E) {}
							}

							// Not a Typeref, just go back to the first try
							if(!IsTypeRef && !IsPackage) {
								$CProduct.toRestoreTry(Saved);
								TRef    = $CProduct.getReturnTypeRefOf(Operand);
								// If void, set to any
								if(TRef == null) TRef = TKJava.TAny.getTypeRef();
								// If package, set to null
								else if(MType.CanTypeRefByAssignableByInstanceOf(null, $Engine, TKJava.TPackage.getTypeRef(), TRef))
										TRef = null;
							}

						} finally {
							$CProduct.toStopTry(true);
						}
					}

					// Do we need AsType
					if(!IsStatic && !IsTypeRef && !IsPackage && (Operand instanceof Expression)) {
						Expression OperExpr = (Expression)Operand;
						// Cast or Variant
						if(OperExpr.isInstruction($Engine, Inst_Cast.Name)) {
							Operand = OperExpr.getParam(1);
							IsAsType = IsToAsType;
							
						} else if(OperExpr.isInstruction($Engine, Inst_CastOrElse.Name))
							IsAsType = IsToAsType;
						else {
							try {
								Type T = $CProduct.getTypeAtCompileTime(TRef);
								IsAsType = IsToAsType && (T instanceof TVariant);
							} catch (Exception E) {}
						}

						// Use this as Var if there is AsType involved
						if(IsAsType && IsThis) {
							Operand = $ME.newExpr(Location, Inst_GetVarValue.Name, "this");
							IsThis  = false;
							IsAccess_this_AsVar = true;
						}
							
					}
					
					if(TKJava.TPackage.getTypeRef().equals(TRef)) {
						ThePackage = null;
						TRef       = null;
					}

					// At this point, all the required parameters are here and we are ready to process

					StackOwner SO    = null;
					Class<?>   Cls   = null;

					String    ErrMsg = null;
					Exception Excp   = null;
					boolean   ErrPos = true;

					// Prefer Java reflection when the type is Java type but not an interface, compile check in full and Java is available
					boolean IsPreferJava = IsJavaEE && IsCheckFull && (TRef != null) && (TRef instanceof TLPrimitive.TRPrimitive) && !IsAsType;
					if(IsPreferJava) {
						Type T = $CProduct.getTypeAtCompileTime(TRef);
						IsPreferJava = !((T instanceof TKJava.TJava) && T.getTypeInfo().getDataClass().isInterface());
					}

					// Look for the identify ---------------------------------------------------------------------
					if(TRef == null) {
						if(!IsSOEE) ErrMsg = "Internal error!!! <Component:365>.";

						else {
							Object Op = (ThePackage != null)?ThePackage:Operand;
							if(Op instanceof Expression) { // Attempt at best to attract the object itself (especially in the case of Package)
								Expression  Ex   = (Expression)Op;
								Instruction Inst = $Engine.getInstruction(Inst_GetPackage.Name);
								if((Inst != null) && Ex.isInstruction($Engine, Inst) && (Ex.getParam(0) instanceof String))
									Op = $Engine.getUnitManager().getRawPackage((String)Ex.getParam(0));
							}

							if(Op instanceof StackOwner) SO = (StackOwner)Op;
							if(SO == null) {
								ErrMsg = String.format(
										"Unknown object kind for '%s' (Don't know what to do) <Component:708>.",
										$Operand
									);
							}
						}

					} else if(IsSOEE && !IsPreferJava) {
						boolean IsResolved = false;
						Type T = null;
						try {
							T    = $CProduct.getTypeAtCompileTime(TRef, false);
							TRef = T.getTypeRef();
							IsResolved = T.getTypeInfo().isResolved(); 
						} catch(Exception E) {
							/* Excp = E; */
							//System.err.println("TRef: " + TRef);
						}
						if(!IsResolved) {
							Location L   = null;
							String   CCN = $CProduct.getCurrentCodeName();
							String   LCN = null;
							T = $CProduct.getTypeAtCompileTime(TRef);
							if((T != null) && ((L = T.getTypeInfo().getLocation($Engine)) != null) && (CCN != null) &&
							    ((LCN = L.getCodeName()) != null) &&
								!CCN.startsWith(LCN + "::")) {
								// If the code name of the location is the same, report error position
								ErrPos = false;
							} else {
								ErrMsg = String.format("Unable to resolve type '%s' <Component:737>", TRef);
								TRef   = TKJava.TAny.getTypeRef();
							}
						}

					} else if(IsJavaEE || IsPreferJava) {
						Cls = TRef.getDataClass($Engine);
						if(Cls == null) {
							ErrMsg = String.format("Unable to get class from the type '%s' <Component:744>", TRef);
							Cls    = Object.class;
						}

					} else ErrMsg = "Internal error!!! <Component:748>)";

					// Ensure the identify -----------------------------------------------------------------------
					if(IsCheckFull && (ErrMsg != null)) {
						$CProduct.reportError(ErrMsg, Excp, ErrPos ? $Result.posOf(enOPERAND) : -1);
						return null;
					}

					// Clear the error message
					ErrMsg = null;
					
					// In case this is an access to DataHolder config/moreinfo
					if(IsDHExtra) {
						boolean     isConvention = (Operand instanceof Expression);
						String      OperInstName = ((Expression)Operand).getInstructionName($Engine);
						ParseResult PResult      = null;
						isConvention &= (PResult = InstPattern.parse(OperInstName)) != null;
						
						if(isConvention) {
							Expression  OperExpr = (Expression)Operand;
							String      Pre      = PResult.textOf(0);
							String      Mid      = PResult.textOf(2);
							String      Post     = PResult.textOf(4);
							
							if(IsAsType) {
								// Instructions with AsType - Only getAttrValueAsType
								if(OperInstName.contains("AsType")) {
									// Any, AsType ... Other params   +  AccName, This param
									if(HasParams) {	// Config
										Object[] ConfigParams = new Object[4 + PCount];
										ConfigParams[0] = OperExpr.getParam(0);	// Operand 
										ConfigParams[1] = OperExpr.getParam(1);	// The as type
										ConfigParams[2] = OperExpr.getParam(2);	// Attribute name
										ConfigParams[3] = AccName;								// Access name of config
										System.arraycopy(Params, 0, ConfigParams, 4, PCount);	// The rest of the params
										Expr = $ME.newExpr(Location, Inst_ConfigAttrAsType.Name, ConfigParams);
										
									} else { // GetMoreInfo
										Expr = $ME.newExpr(Location, Inst_GetAttrMoreInfoAsType.Name,
											OperExpr.getParam(0),
											OperExpr.getParam(1),
											OperExpr.getParam(2),
											AccName
										);
									}
									
									break GetAccExpr;
								}
								
								Object NewOper;
								int    StartIndex = 0;
								
								if(OperInstName.startsWith("this_"))
									 NewOper = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwner_VarName);
								else if(OperInstName.startsWith("type_"))
									 NewOper = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwnerAsCurrentType_VarName);
								else if(OperInstName.startsWith("package_"))
									 NewOper = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwnerAsPackage_VarName);
								else {
									NewOper    = OperExpr.getParam(0);
									StartIndex = 1;
								}
							
								if(HasParams) {	// Config: 3 for Oper, AsType, and AccessName
									Object[] ConfigParams = new Object[OperExpr.getParamCount() + 3 + PCount - StartIndex];
									int      Index        = 0;
									
									ConfigParams[Index++] = NewOper;
									ConfigParams[Index++] = $ME.newType(Location, TRef);
									
									for(int i = StartIndex; i < OperExpr.getParamCount(); i++)
										ConfigParams[Index++] = OperExpr.getParam(i);
									
									ConfigParams[Index++] = AccName;
									
									System.arraycopy(Params, 0, ConfigParams, Index, PCount);
									Expr = $ME.newExpr(Location, Inst_ConfigAttrAsType.Name, ConfigParams);
									
								} else { // GetMoreInfo
									Expr = $ME.newExpr(Location, Inst_GetAttrMoreInfoAsType.Name, Operand, TRef, AccName);
								}
								
							} else if(HasParams) {	// Config
								int      OPCount      = OperExpr.getParamCount();
								Object[] ConfigParams = new Object[OPCount + PCount + 1];
								// Copy the operation parameters
								for(int i = OPCount; --i >= 0; ) ConfigParams[i] = OperExpr.getParam(i);
								// Set the access name
								ConfigParams[OPCount] = AccName;
								// Set the config parameter
								System.arraycopy(Params, 0, ConfigParams, OPCount + 1, Params.length);
	
								// Create the expression
								Expr = $ME.newExpr($Result.locationCROf(enACCESS), Pre + "config" + Mid + Post, ConfigParams);
								
							} else {	// GetMoreInfo
								int      OPCount           = OperExpr.getParamCount();
								Object[] GetMoreInfoParams = new Object[OPCount + 1];
								// Copy the operation parameters
								for(int i = OPCount; --i >= 0; ) GetMoreInfoParams[i] = OperExpr.getParam(i);
								// Set the access name
								GetMoreInfoParams[OPCount] = AccName;
	
								// Create the expression
								Expr = $ME.newExpr($Result.locationCROf(enACCESS), Pre + "get" + Mid + "MoreInfo" + Post, GetMoreInfoParams);
							}
						}
						
						break GetAccExpr;
					}

					// Clear the error message
					ErrMsg = null;

					// The search result
					ExecSignature ES = null;
					Method        M  = null;

					// Look for the Signature if need
					if(HasParams) {
						if(IsSOEE && !IsPreferJava) {
							if(TRef == null) {	// Without type
								ES = SO.getSOInfo().searchOperation(AccName, PTRefs);
								if(ES == null) ErrMsg = "Unknown operation '"+SO;

							} else {			// With type
								ES = IsStatic
										?TRef.searchTypeOperation(  $Engine, AccName, PTRefs)
										:TRef.searchObjectOperation($Engine, AccName, PTRefs);
								if(ES == null) {
									ErrMsg = String.format(
										"Unknown%s method '%s%s <Component:735>",
										(IsStatic ?" static":""),
										TRef, "%s"
									);
								}
							}

						} else {						
							M = UClass.getMethodByParamClasses(Cls, AccName, IsStatic, PClss);
							if(M == null) {
								if(TRef != null) {	// With type (only typed object can be native)
									ES = IsStatic
											?TRef.searchTypeOperation(  $Engine, AccName, PTRefs)
											:TRef.searchObjectOperation($Engine, AccName, PTRefs);
								}
								
								if(ES == null) {
									ErrMsg = String.format(
										"Unknown%s method '%s%s <Component:754>",
										(IsStatic ?" static":""),
										Cls.getName(), "%s"
									);
								}
							}
						}
						
						// If the type is native and the ES has BaseOnType TypeRefs, invoke it by params
						// This is done by making ES == null so it will be called by default
						if((ErrMsg == null) && (ES != null) && TKJava.KindName.equals(TRef.getTypeKindName($Engine))) {
							if(ES.getReturnTypeRef() instanceof TRBasedOnType) ES = null;
							else {
								for(int i = 0; i < ES.getParamCount(); i++) {
									if(!(ES.getParamTypeRef(i) instanceof TRBasedOnType)) continue;
									
									ES = null;
									break;
								}
							}
						}

						// Report error
						if(IsCheckFull && (ErrMsg != null)) {
							$CProduct.reportError(
									String.format(
										ErrMsg,
										$Result.textOf(enACCESS) + AccName + UArray.toString(PTRefs, "(", ")", ",")
									),
									null, $Result.posOf(enACCESS));
							return null;
						}
						ErrMsg = null;

						if(IsCheckFull) {	// Call as compile time reference
							if(ES != null) {
								if(IsSOCEE) {
									String InstName    = null;
									Object OprdToCheck = null;
									if(     IsThis)    { InstName = Inst_thisInvoke   .Name; OprdToCheck = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwner_VarName);          } 
									else if(IsSuper)   { InstName = Inst_superInvoke  .Name; OprdToCheck = null; }
									else if(IsType)    { InstName = Inst_typeInvoke   .Name; OprdToCheck = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwnerAsType_VarName);    }
									else if(IsPackage) { InstName = Inst_packageInvoke.Name; OprdToCheck = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwnerAsPackage_VarName); }
									
									if(IsAccess_this_AsVar && (OprdToCheck instanceof Expression)) {
										Object[] TheParams = new Object[2 + PCount];
										TheParams[0] = OprdToCheck;
										TheParams[1] = ES;
										if(PCount != 0) System.arraycopy(Params, 0, TheParams, 2, PCount);
										
										Expr = CreateNullAwareAccess(
												$CProduct, $ME, IsAccessNA, dcVARARGS, Location, OprdToCheck,
												((Expression)OprdToCheck).getInstructionName($Engine), TheParams, -1);
										
									} else if(InstName != null) {
										Object[] TheParams = new Object[1 + PCount];
										TheParams[0] = ES;
										if(PCount != 0) System.arraycopy(Params, 0, TheParams, 1, PCount);
										if((OprdToCheck == null) || !IsAccessNA) Expr = $ME.newExpr(Location, InstName, TheParams);
										else {
											Expr = CreateNullAwareAccess(
													$CProduct, $ME, IsAccessNA, dcVARARGS, Location, OprdToCheck,
													InstName, TheParams, -1);
										} 
									}
								}
								if(Expr == null) {
									int PCountAsType = IsAsType ? 1 : 0;
									
									Object[] TheParams = new Object[2 + PCount + PCountAsType];
									TheParams[0               ] = Operand;
									TheParams[1 + PCountAsType] = ES;
									if(IsAsType) TheParams[1]   = $ME.newType(Location, TRef);
									
									if(PCount != 0) System.arraycopy(Params, 0, TheParams, 2 + PCountAsType, PCount);
									Expr = CreateNullAwareAccess(
										$CProduct, $ME, IsAccessNA, dcVARARGS, Location, Operand,
										IsAsType ? Inst_InvokeAsType.Name : Inst_Invoke.Name, TheParams, 0
									);
								}

							} else if(M != null) {
								Expr = $ME.newExpr(Location, Inst_GetJavaMethodByParamClasss.Name, Cls, AccName, IsStatic, PClss);
								
								Object[] TheParams = new Object[(IsStatic?1:2) + PCount];
								TheParams[0] = Expr;
								if(!IsStatic) TheParams[1] = Operand;
								if(PCount != 0) System.arraycopy(Params, 0, TheParams, (IsStatic?1:2), PCount);
								Expr = CreateNullAwareAccess(
										$CProduct, $ME, IsAccessNA, dcVARARGS, Location, Operand,
										(IsStatic
										    ? Inst_InvokeJavaClassMethodByMethod .Name
										    : Inst_InvokeJavaObjectMethodByMethod.Name
										),
										TheParams, (IsStatic?-1:1)
									);
							}

							if(Expr != null) {
								HasParams = false;
								break GetExpr;
							} else {
								// TODO - Report as not found
							}
						}

						// Not found so nake it as dynamic access
						Object[] TheParams = new Object[2 + PCount];
						TheParams[0] = (IsSOEE?Operand:(IsStatic?Cls:Operand));
						TheParams[1] = AccName;
						if(PCount != 0) System.arraycopy(Params, 0, TheParams, 2, PCount);

						String InstName = IsSOEE
						                      ? Inst_Invoke_ByParams.Name
						                      : IsStatic
						                            ? Inst_InvokeJavaClassMethod .Name
						                            : Inst_InvokeJavaObjectMethod.Name;
						
						Expr = CreateNullAwareAccess(
							$CProduct, $ME, IsAccessNA, dcVARARGS, Location, (IsSOEE?Operand:(IsStatic?Cls:Operand)),
							InstName, TheParams, 0
						);

					} else {
						TypeRef AT = null;
						Field   F  = null;

						if(IsSOEE && !IsPreferJava) {
							if(TRef == null) {
								AT = SO.getSOInfo().searchAttribute($Engine, false, null, AccName);
								if(AT == null) {
									ErrMsg = String.format(
										"Unknown attribute '%s' <Component:922>.",
										SO + Access + AccName
									);
								}

							} else {	// With type
								AT = IsStatic
										?TRef.searchTypeAttribute(  $Engine, AccName)
										:TRef.searchObjectAttribute($Engine, AccName);
								if((AT == null) && !HasParams) {
									ErrMsg = IsStatic
									    ? String.format("Unknown static field '%s' <Component:935>.", TRef + Access + AccName)
									    : String.format("Unknown " +   "field '%s' <Component:936>.", TRef + Access + AccName);
								}
							}

						} else {
							F = UClass.getField(Cls, AccName, IsStatic);
							if((F == null) && !HasParams) {
								ErrMsg = IsStatic
							    ? String.format("Unknown static field '%s' <Component:944>.", Cls.getName() + Access + AccName)
							    : String.format("Unknown " +   "field '%s' <Component:945>.", Cls.getName() + Access + AccName);
							}

						}

						// Report error
						if(IsCheckFull && (ErrMsg != null)) {
							$CProduct.reportError(ErrMsg, null, Position);
							return null;
						}
						ErrMsg = null;

						if(IsCheckFull) {	// Access as static
							if(AT != null) {
								if(IsSOCEE) {
									String InstName    = null;
									Object OprdToCheck = null;
									// TODO - Do something with Super getAttrValue
									if(     IsThis)    { InstName = Inst_thisGetAttrValue   .Name; OprdToCheck = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwner_VarName);          } 
									else if(IsSuper)   { InstName = null;                          OprdToCheck = null; }
									else if(IsType)    { InstName = Inst_typeGetAttrValue   .Name; OprdToCheck = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwnerAsType_VarName);    }
									else if(IsPackage) { InstName = Inst_packageGetAttrValue.Name; OprdToCheck = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwnerAsPackage_VarName); }

									if(IsAccess_this_AsVar && (OprdToCheck instanceof Expression)) {
										if((OprdToCheck == null) || !IsAccessNA) Expr = $ME.newExpr(Location, InstName, AccName);
										else {
											Expr = CreateNullAwareAccess(
												$CProduct, $ME, IsAccessNA, dcVARARGS, Location, OprdToCheck,
												InstName, new Object[] { OprdToCheck, AccName }, -1
											);
										} 
										
									} else if(InstName != null) {
										if((OprdToCheck == null) || !IsAccessNA) Expr = $ME.newExpr(Location, InstName, AccName);
										else {
											Expr = CreateNullAwareAccess(
												$CProduct, $ME, IsAccessNA, dcVARARGS, Location, OprdToCheck,
												InstName, new Object[] { AccName }, -1
											);
										} 
									}
								}

								if(Expr == null) {
									if(IsAsType) {
										Expr = CreateNullAwareAccess(
											$CProduct, $ME, IsAccessNA, dcDYNAMIC, Location, Operand,
											2, Inst_GetAttrValueAsType.Name, $ME.newType(Location, TRef), AccName
										);
									} else {
										Expr = CreateNullAwareAccess(
											$CProduct, $ME, IsAccessNA, dcDYNAMIC, Location, Operand,
											1, Inst_GetAttrValue.Name, AccName
										);
									}
								}

							} else if(F != null) {
								String InstName;
								Expr = $ME.newExpr(Location, Inst_GetJavaField.Name, Cls, AccName, IsStatic);
								
								if(IsStatic) {
									InstName = Inst_GetJavaClassFieldValueByField.Name;
									Expr     = $ME.newExpr(Location, InstName, Expr);
								} else {
									InstName = Inst_GetJavaObjectFieldValueByField.Name;
									Expr     = CreateNullAwareAccess(
									               $CProduct, $ME, IsAccessNA, dcDYNAMIC, Location, Operand,
									               1, InstName, Expr
									           );
								}
							}

							if(Expr != null) { HasParams = false; break GetExpr; }
						}

						// Not found so dynamically run as access
						Object Target;
						String InstName;
						if(IsSOEE)        { Target = Operand; InstName = Inst_Invoke_ByParams.Name;         }
						else if(IsStatic) { Target = Cls;     InstName = Inst_GetJavaClassFieldValue.Name;  }
						else              { Target = Operand; InstName = Inst_GetJavaObjectFieldValue.Name; }

						//Expr = $ME.newExpr(Location, InstName, Target, AccName);
						Expr = CreateNullAwareAccess($CProduct, $ME, IsAccessNA, dcDYNAMIC, Location, Target, 1, InstName, AccName);
					}

					// Clear it
					HasParams = false;
					break GetAccExpr;
				}
			}

		} finally {
			ResetCompileTimeChecking($CProduct, CheckingFlag);
		}

		// TODO - Should reconsider if this is appropriate return
		return (Expr == null) ? $Result.textOf(enOPERAND) : Expr;
	}
}
