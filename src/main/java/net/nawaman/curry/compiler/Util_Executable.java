package net.nawaman.curry.compiler;

import java.util.HashSet;
import java.util.regex.Pattern;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.compiler.CodeFeeders;
import net.nawaman.compiler.CompilationOptions;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Instruction;
import net.nawaman.curry.Scope;
import net.nawaman.curry.TKExecutable;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.Instructions_Context.Inst_NewConstant;
import net.nawaman.curry.Instructions_Core.Inst_Group;
import net.nawaman.curry.Instructions_Executable.Inst_NewClosure;
import net.nawaman.curry.Instructions_Executable.Inst_ReCreate;
import net.nawaman.curry.Instructions_Executable.Inst_Run_Unsafe;
import net.nawaman.regparser.result.Coordinate;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;

public class Util_Executable {

	/** Contains information about frozen variable */
	static public class FrozenVariableCompiledResult {
		public FrozenVariableCompiledResult(String[] pFVNames, Scope pFVScope) {
			this.FVNames = pFVNames;
			this.FVScope = pFVScope;
		}
		public String[] FVNames = null;
		public Scope    FVScope = null;
	}
	
	/**
	 * Compile fronzen variable information
	 * 
	 * ParserResult elements:
	 * 	$FrozenVariable: an array of frozen variables' names.
	 * 
	 **/
	static public FrozenVariableCompiledResult CompileFrozenVariable(String[] $FVNames, ParseResult $Result,
			PTypePackage $TPackage, CompileProduct $CProduct) {
		
		HashSet<String> FVNameSet = new HashSet<String>();
		Scope           FVScope   = null;
		
		for(int i = ($FVNames == null)?0:$FVNames.length; --i >= 0; ) {
			String FName = $FVNames[i];
			
			if((FName == null) || !Pattern.matches("[a-zA-Z$_][a-zA-Z0-9$_]*", FName)) {
				$CProduct.reportError(
						"Invalid frozen variable name '"+FName+"' <Util_Executable:52>",
						null, $Result.startPositionsOf("$FrozenVariable")[i]
					);
			}
			
			// Checks if it is already there
			if(FVNameSet.contains(FName)) {
				$CProduct.reportError(
					"Repeat frozen variable name '"+FName+"' <Util_Executable:60>",
					null, $Result.startPositionsOf("$FrozenVariable")[i]
				);
				return null;
			}
			
			// Add to the set so we can check later
			FVNameSet.add(FName);
			
			if(!$CProduct.isVariableExist(FName)) {
				// The variable is unknown
				$CProduct.reportError(
					"Unknown variable '"+FName+"' for frozen variable <Util_Executable:72>",
					null, $Result.startPositionsOf("$FrozenVariable")[i]
				);
				return null;
			}

			// Try to get the type if possible
			TypeRef FTRef = $CProduct.getVariableTypeRef(FName);
			Type    FType = $CProduct.getTypeAtCompileTime(FTRef);

			// Default Type
			if(FType == null) {
				try { FType = $CProduct.getTypeAtCompileTime(FTRef); }
				catch(Throwable T) {}
				 
				if(FType == null) FType = TKJava.TAny;
			}
			
			// Creates the contant in the compile context.
			if(FVScope == null) FVScope = new Scope();
			FVScope.newConstant(FName, FType, null);
			
		}
		
		return new FrozenVariableCompiledResult($FVNames, FVScope);
	}

	/**
	 * Declare an compiled executable to be a local executable.
	 * 
	 * @param $Kind   a character representing the desired executable type. f,m,s,c = fragment, macro, sunroutine and
	 *                   closure, respectively.
	 */
	static Expression DeclareLocalExecutable(char $Kind, Expression $Expr, Executable $Exec,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		Engine       $Engine = $CProduct.getEngine();
		TKExecutable TKExec  = (TKExecutable)$Engine.getTypeManager().getTypeKind(TKExecutable.KindName);

		Executable.ExecKind EKind = $Exec.getKind();
		if($Kind == 'c') EKind = Executable.ExecKind.SubRoutine;
		
		ExecSignature Signature = $Exec.getSignature();
		
		StringBuffer SB = new StringBuffer();
		TypeRef TRef =  TKExec.getNoNameTypeRef(EKind, Signature, null, SB);
		
		if(SB.length() != 0) {
			$CProduct.reportError(
				String.format(
					"There is a problem creating executable type of %s %s. <Util_Executable:439>",
					EKind, $Exec.getSignature()),
				null, $Result.startPosition());
			return null;
		}

		Coordinate $LocationCR = $Result.coordinateOf(0);

		String ConstName  = Signature.getName();
		Object ConstType  = $Engine.getExecutableManager().newType($LocationCR, TRef);
		Object ConstValue = ($Expr == null) ? $Exec : $Expr;
		
		Instruction Inst = $Engine.getInstruction(Inst_NewConstant.Name);
		Inst.manipulateCompileContextBeforeSub(
				new Object[] { ConstName, ConstType, ConstValue },
				$CProduct, $Result.startPosition());
		
		$Expr = Inst.newExpression_Coordinate($LocationCR, ConstName, ConstType, ConstValue);

		// Ensure the parameter is correct
		if(!$Expr.ensureParamCorrect($CProduct) || !$Expr.manipulateCompileContextFinish($CProduct)) return null;
		
		return $Expr;
	}
	
	/** Context share kind */
	static public enum Share_Context_Kind {
		ShareFull,			// Share the context in full so all variable is accessible 
		ShareStackOwner,	// Share only StackOwner access and not the local variable
		ShareNone			// No share between the current context and the being compile executable
	}
	
	static public final String ENLanguage   = "$Lang";
	static public final String ENBody       = "#Body";
	static public final String ENCode       = "$Code";
	static public final String ENKind       = "$Kind";
	static public final String ENStatements = "#Statement";
	
	/**
	 * Compile an executable body.
	 * 
	 * $IsSeparateContext is a flag to indicate if the context of the compilation should be separated from the current
	 *    one. Local executable and executable elements should use the share one (which means that the context should be
	 *    prepared before this one is called).   
	 * 
	 * The variable $Result must have a first level elements with the following names:
	 * 0. $Kind -> The executable kind (used only for position and location)
	 * 1. $Lang -> Language Name.
	 * 2. #Body -> Body in the Default language.
	 * 3. $Code -> Body in the specified language.
	 * 
	 * @param $Kind   a character representing the desired executable type. g, f,m,s,c = group, fragment, macro,
	 *                   sunroutine and closure, respectively.
	 **/
	static public Object CompileExecutableBody(Share_Context_Kind $ShareKind, ExecSignature $Signature, char $Kind,
			boolean $IsToCreateConstant, String[] $FVNames, Scope $FVScope,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		CurryLanguage CL      = $CProduct.getCurryLanguage();
		Engine        $Engine = $CProduct.getEngine();
		TKExecutable  TKExec  = (TKExecutable)$Engine.getTypeManager().getTypeKind(TKExecutable.KindName);
		
		String     ELang   = ($Result == null) ? null                   : $Result.textOf(ENLanguage);
		Coordinate $LocCR  = ($Result == null) ? new Coordinate(-1, -1) : $Result.coordinateOf(0);
		int        ZeroPos = ($Result == null) ? 0                      : $Result.startPositionOf(ENKind);
		int        Offset  =    0;
		String     ECode   = null;
		ExecKind   EKind   = null;	
		
		// Prepare the executable kind
		switch($Kind) {
			case 'g':                              break;
			case 'f': EKind = ExecKind.Fragment;   break;
			case 'm': EKind = ExecKind.Macro;      break;
			case 'c':
			case 's': EKind = ExecKind.SubRoutine; break;
			default: {
				$CProduct.reportError("Invalid executable kind <Util_Executable:144>.", null, ZeroPos);
				return null;
			}
		}
		
		if($Kind == 'g') {
			$IsToCreateConstant = false;
			if($Signature == null)
				$Signature = ExecSignature.newProcedureSignature(Inst_Group.Name, TKJava.TAny.getTypeRef());
		}

		// Creates the constant
		if($IsToCreateConstant) {
			$CProduct.newConstant(
					// Name of the variable
					$Signature.getName(),
					// Type Ref of the
					TKExec.getNoNameTypeRef(
							EKind, $Signature, $Signature.getExtraData(),
							null)
				);
		}
	
		// Closure is always share
		if( $Kind == 'c') $ShareKind = Share_Context_Kind.ShareFull;
		// SubRoutine cannot share full 
		if(($Kind == 's') && ($ShareKind == Share_Context_Kind.ShareFull))
			$ShareKind = Share_Context_Kind.ShareStackOwner;
		
		// The Compile Product to be used 
		CompileProduct CP   = null;
		Executable     Exec = null;
		
		boolean HasNewMacroScopeCreated = false;
		try {

			CompileProductContainer CPContainer = new CompileProductContainer();
			CurryCompilationOptions Options     = null;

			// Share the whole context ---------------------------------------------------------------------------------
			if($ShareKind == Share_Context_Kind.ShareFull) {
				// Create scope for Macro and Closure
				if(($Kind == 'm') || ($Kind == 'c')) {
					CP = $CProduct;
					$CProduct.newMacroScope($Signature);
					HasNewMacroScopeCreated = true;
				}

				if((ELang == null) || CL.getName().equals(ELang)) {

					// Compile the body - Since it use the same $CProduct so all is shared
					Expression   Body  = null; 
					Expression[] $Body;
					
					if($Result.textOf("$StartClosure") == null) {
						// Regular
						$Body = (Expression[])$Result.valueOf(ENBody, $TPackage, $CProduct);
					} else {
						// In-line closure
						Object[] Objs = $Result.valuesOf(ENStatements, $TPackage, $CProduct);
						$Body = new Expression[(Objs == null) ? 0 : Objs.length];
						for(int i = 0; i < $Body.length; i++)
							$Body[i] = Expression.toExpr(Objs[i]);
					}
					
					// Warp the body
					if($Body != null) {
						if($Body.length == 1) {
							if($Body[0] != null)
								Body = $Body[0];
						} else  Body = $Engine.getExecutableManager().newGroup($Body);
					}
					
					if($Kind == 'g') {
						Exec = Body;
							
					} else if($Kind == 'f') {
						// Create the Fragment
						Exec = $Engine.getExecutableManager().newFragment(
									// Signature
									$Signature.getName(),
									$Signature.getReturnTypeRef(),
									$Signature.getLocation(),
									$Signature.getExtraData(),
									// Body
									Body,
									// Frozen Variables
									$FVNames,
									$FVScope
								);
						
					} else {
						// Create the Macro
						Exec = $Engine.getExecutableManager().newMacro(
									// Signature
									$Signature,
									// Body
									Body,
									// Frozen Variables
									$FVNames,
									$FVScope
								);
					}

					// Unknown error
					if(Exec == null) {
						$CProduct.reportError("Unable to compile an executable: Unknown Error <Util_Executable:255>.",
								null, ZeroPos);
						return null;
					}
				}
				
			// No context sharing or share only StackOwner -------------------------------------------------------------
			} else {
				// Share StackOwner ------------------------------------------------------------------------------------
				if($ShareKind == Share_Context_Kind.ShareStackOwner)
					 Options = $CProduct.createOptionsForShareStackOwner($CProduct.CCompiler.TheID);
				else Options = new CurryCompilationOptions();
				
				if((ELang == null) || CL.getName().equals(ELang)) {
					ECode  = ($Result == null) ? "" : $Result.textOf(ENBody);
					Offset = ($Result == null) ? 0  : $Result.startPositionOf( ENBody);
				} else  {
					ECode  = ($Result == null) ? "" : $Result.textOf(ENCode);
					Offset = ($Result == null) ? 0  : $Result.startPositionOf( ENCode);
				}
				
				Options.setCodeFeederName($CProduct.getCurrentFeederName());
				Options.setCodeName(      $CProduct.getCurrentCodeName());
				Options.setOffset(        Offset);
				Options.setEndPos(        Offset + ECode.length());
				Options.setTopScope(      $FVScope);
				Options.setFrozens(       $FVNames);
				Options.setImports(       $CProduct.getImports());
				Options.freeze();

				if((ELang == null) || CL.getName().equals(ELang)) {

					String OCode = $Result.originalText();
					String EName = $CProduct.getCurrentCodeName();
					if(EName == null) EName = "";
					EName += "::" + (($Kind == 'g') ? "group()" : $Signature.toString());
					
					switch($Kind) {
						case 'g': Exec = CL.compileExpression(EName,      OCode, Options, CPContainer); break;
						case 'f': Exec = CL.compileFragment(  EName,      OCode, Options, CPContainer); break;
						case 'c': 
						case 'm': Exec = CL.compileMacro(     $Signature, OCode, Options, CPContainer); break;
						case 's': Exec = CL.compileSubRoutine($Signature, OCode, Options, CPContainer); break;
					}
						
					// Append of the message from the compilation in to this product.
					CompileProduct CompiledCP = CPContainer.getCompileProduct();
					for(int i = 0; i < CompiledCP.getMessageCount(); i++)
						$CProduct.reportMessage(CompiledCP.getMessage(i));
					if(CompiledCP.hasErrMessage()) return null;

					// Unknown error
					if(Exec == null) {
						$CProduct.reportError("Unable to compile an executable: Unknown Error <Util_Executable:255>.",
								null, ZeroPos);
						return null;
					}
				}
			}
			
			if(Exec == null) {
			
				// Specified language --------------------------------------------------------------------------------------
				// CP and/or Options are alreasy set appropriatedly
				
				ExecutableCreator EC = CL.getExecutableCreator(ELang);
				if(EC == null) {
					$CProduct.reportError(
						String.format("Sub Language '%s' is not supported <Util_Executable:270>", ELang),
						null, $Result.startPositionOf(ENLanguage)
					);
					return null;
				}
	
				// Prepare the adjusted code ----------------------------------------------------------
				// Adjust the code so it can be compiled by the ExecutableCreator with the same test layout
				ECode  = $Result.textOf(ENCode);
				Offset = $Result.startPositionOf( ENCode);
	
				String OrgTxt = $Result.originalText();
				int    EndPos = Offset + ECode.length();
				StringBuilder SB = new StringBuilder();
				for(int i = 0; i < OrgTxt.length(); i++) {	
					char c = OrgTxt.charAt(i);
					if(Character.isWhitespace(c) || (i >= Offset) && (i < EndPos))
						 SB.append(c); 
					else SB.append(' ');
				}
				// Done
				String ACode = SB.toString();
				
				// Share the StackOwner
				if($ShareKind == Share_Context_Kind.ShareStackOwner) {
					// Creates the compile product from the curry compilation options
					CurryCompiler      CCompiler = $CProduct.getCurryCompiler();
					CompilationOptions Opts      = CCompiler.newCompilationOptions($Signature, Options);
					
					CodeFeeder CF = new CodeFeeder.CFCharSequence(($Kind == 'g') ? "group" : $Signature.getName(), ACode);
					CP = CCompiler.newCompileProduct(new CodeFeeders(CF), Opts);
				}
				
				if($Kind == 'g') EKind = ExecKind.Fragment;
				
				// Creates the executable directly
				Exec = EC.newExecutable(
							CP,			// No share
							null,		// This is compile time so no context
							$Engine,	// The Engine
							null,		// Language Parameter (not support yet)
							EKind,		// The executable kind
							$Signature,	// The signature
							$FVNames,	// The frozen variables' names
							$FVScope,	// The frozen variable scope
							ACode);		// The code

				
				if($Kind == 'g') {
					if(Exec.isCurry()) Exec = Expression.toExpr(Exec.asCurry().getBody());
					else {
						Instruction Inst;
						if((Inst = $Engine.getInstruction(Inst_Run_Unsafe.Name)) == null) {
							$CProduct.reportError(
									"To support, sub-language 'group' an instruction called '"+Inst_Run_Unsafe.Name+"'" +
									" is required <Util_Executable:403>", null,
									$Result.startPositionOf(ENLanguage));
							return null;
						}
						Exec = Inst.newExpression_Coordinate($LocCR, Exec);
					}
				}
			}
			
			Expression Expr = null;
			
			if(($FVNames != null) && ($FVNames.length != 0)) {
				Expr = $Engine.getExecutableManager().newExpr($LocCR, Inst_ReCreate.Name, Exec);
				if(!Expr.ensureParamCorrect($CProduct)) return null;
			}
			
			// Make it a closure if it is
			if($Kind == 'c') {
				Expr = $Engine.getExecutableManager().newExpr($LocCR, Inst_NewClosure.Name, (Expr == null) ? Exec : Expr);
				if(!Expr.ensureParamCorrect($CProduct)) return null;
			}

			// Declare it as a variable if it a local - this is for the run-time declaration
			if($IsToCreateConstant) {
				// Exit scope for Macro
				if(HasNewMacroScopeCreated) {
					$CProduct.exitScope();
					HasNewMacroScopeCreated = false;
				}
				
				// But first remove the old one
				$CProduct.getCurrentScope().removeVariable($Signature.getName());
				
				Expr = DeclareLocalExecutable($Kind, Expr, Exec, $Result, $TPackage, $CProduct);
			}
			
			if(($Kind == 'g') && (Exec instanceof Expression) && ((Expression)Exec).isData())
				return ((Expression)Exec).getData();

			return (Expr == null) ? Exec : Expr;
			
		} finally {
			// Exit scope for Macro
			if(HasNewMacroScopeCreated)
				$CProduct.exitScope();
		}
	}
}
