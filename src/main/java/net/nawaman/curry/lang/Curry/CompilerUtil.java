package net.nawaman.curry.lang.Curry;

import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Context;
import net.nawaman.curry.CurryError;
import net.nawaman.curry.Documentation;
import net.nawaman.curry.Engine;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Location;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Variable;
import net.nawaman.curry.Instructions_ControlFlow.Inst_NewThrowable;
import net.nawaman.curry.Instructions_Core.Inst_NewInstanceByTypeRefs;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.compiler.ElementResolver;
import net.nawaman.curry.compiler.Util_ElementResolver;
import net.nawaman.curry.compiler.FileCompileResult;
import net.nawaman.curry.compiler.StackOwnerAppender;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.result.Coordinate;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.util.UArray;
import net.nawaman.util.UClass;

/** Utilities for the compiler */
public class CompilerUtil {
	
	/** No inherit or instance */
	private CompilerUtil() {}
		
	
	/** Compile a new Statement */
	static public Expression compileNewStatement(TypeRef $TypeRef, Object[] $Params,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		Engine      $Engine = $CProduct.getEngine();
		MExecutable MT      = $Engine.getExecutableManager();
		
		boolean IsTypeValid = true;
		try { $Engine.getTypeManager().ensureTypeInitialized($TypeRef); }
		catch(Exception E) { IsTypeValid = false; }

		// See if the type is a curry error and the parameter is suitable to be a curry error (in the sense that we can use)
		if(IsTypeValid &&
		   CurryError.class.isAssignableFrom($TypeRef.getDataClass($Engine)) &&
		   (($Params == null) || ($Params.length <= 2)) ) {
			
			Class<?> CT = $TypeRef.getDataClass($Engine);
			
			int     IndexMessage = -1;
			int     IndexCause   = -1;
			boolean HasOthers    = false;
			
			if($Params != null) {
				if($Params.length >= 1) {
					TypeRef TR = $CProduct.getReturnTypeRefOf($Params[0]);
					if(     TKJava.TString.getTypeRef().equals(TR))                     IndexMessage = 0;
					else if(Throwable.class.isAssignableFrom(TR.getDataClass($Engine))) IndexCause   = 0;
					else HasOthers = true;
				}
				if(!HasOthers && ($Params.length >= 2)) {
					TypeRef TR = $CProduct.getReturnTypeRefOf($Params[1]);
					if(     TKJava.TString.getTypeRef().equals(TR))                     IndexMessage = 1;
					else if(Throwable.class.isAssignableFrom(TR.getDataClass($Engine))) IndexCause   = 1;
					else HasOthers = true;
				}
			}
			
			if(!HasOthers) {	// The parameter is suitable
				boolean IsGoodForNewThrowable = false;
				if((IndexMessage != -1) && (IndexCause != -1)) {	// The message and the cause are given
					Class<?>[] CParamClasses = new Class<?>[] { String.class, Context.class, Throwable.class };
					IsGoodForNewThrowable = (UClass.getConstructorByParamClasses(CT, CParamClasses) != null);
					
				} else {
					IsGoodForNewThrowable |= (UClass.getConstructorByParamClasses(CT, new Class<?>[] { String.class, Context.class }) != null);
					IsGoodForNewThrowable |= (UClass.getConstructorByParamClasses(CT, new Class<?>[] {               Context.class }) != null);
				}
				
				if(IsGoodForNewThrowable)	// The parameters are good for NewThrowable
					return MT.newExpr($Result.coordinateOf(0), Inst_NewThrowable.Name,
								(IndexMessage != -1)?$Params[IndexMessage]:null,
								CT,
								(IndexCause   != -1)?$Params[IndexCause  ]:null
							);
				
			}
		}
		
		// The type is not a throwable, so just go on normally ---------------------------------------------------------
		Object Type = $Engine.getExecutableManager().newType($Result.coordinateOf(0), $TypeRef);
		
		// Get the typerefs of the parameters
		TypeRef[] TRs = TypeRef.EmptyTypeRefArray;
		if($Params != null) {
			TRs = new TypeRef[$Params.length];
			for(int i = $Params.length; --i >= 0; ) TRs[i] = $CProduct.getReturnTypeRefOf($Params[i]);
		}
		
		// Check if the constructor exist
		if(!IsTypeValid || !$CProduct.isCompileTimeCheckingNone() && ($TypeRef.searchConstructor($Engine, TRs) == null)) {
			String ErrMsg = String.format(
			                    "Unable to find the constructor new %s(%s)",
			                    $TypeRef, UArray.toString(TRs, "", "", ",")); 
			$CProduct.reportError(ErrMsg, null, $Result.startPositionOf(0));
		}
		
		// Prepare the parameter, construct the expression and return it
		Object[] Os = new Object[(($Params == null)?0:$Params.length) + 2];
		Os[0] = Type;
		Os[1] = TRs;
		if($Params != null) System.arraycopy($Params, 0, Os, 2, $Params.length);

		// Create the expression
		Expression Expr = MT.newExpr($Result.coordinateOf(0), Inst_NewInstanceByTypeRefs.Name, (Object[])Os);
		if(!Expr.ensureParamCorrect($CProduct)) return null;
		return Expr;
	}
	


	/** Registers the structure */
	static public FileCompileResult.TypeField compileTypeField(
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// StructuralRegistration
		
		String     VarName = $Result.textOf("$VarName");
		int        VarPos  = $Result.startPosition();
		Coordinate VarLoc  = $Result.coordinateOf(0);
		
		String   PName;
		String[] Strs;
		if( (((PName = "Access")       != null) && ((Strs = $Result.textsOf("#Access"))       != null) && (Strs.length > 1)) ||
		    (((PName = "WriteAccess")  != null) && ((Strs = $Result.textsOf("#WriteAccess"))  != null) && (Strs.length > 1)) ||
		    (((PName = "ConfigAccess") != null) && ((Strs = $Result.textsOf("#ConfigAccess")) != null) && (Strs.length > 1)) ||
		    (((PName = "Document")     != null) && ((Strs = $Result.textsOf("#Document"))     != null) && (Strs.length > 1)) ||
		    (((PName = "NotNull")      != null) && ((Strs = $Result.textsOf("$NotNull"))      != null) && (Strs.length > 1)) ||
		    (((PName = "Dynamic")      != null) && ((Strs = $Result.textsOf("$Dynamic"))      != null) && (Strs.length > 1)) ||
		    (((PName = "Static")       != null) && ((Strs = $Result.textsOf("$IsStatic"))     != null) && (Strs.length > 1))
		  ) {
		  $CProduct.reportError("Multiple type-field property `"+PName+"` of `"+VarName+"` <CompilerUtil:1098>", null,
				  $Result.startPositionsOf(PName)[1]);
		  return null;
		}
		
		// Validation ------------------------------------------------------------------------------------------------------
		
		Documentation Doc          = (Documentation)$Result.valueOf("#Document",     $TPackage, $CProduct);
		Accessibility Read         = (Accessibility)$Result.valueOf("#Access",       $TPackage, $CProduct);
		Accessibility Write        = (Accessibility)$Result.valueOf("#WriteAccess",  $TPackage, $CProduct);
		Accessibility Config       = (Accessibility)$Result.valueOf("#ConfigAccess", $TPackage, $CProduct);
		boolean       NonNull      = ($Result.textOf("$NonNull") != null);
		boolean       Writable     = ($Result.textOf("$UnWritable") == null);
		TypeRef       TRef         = (TypeRef)$Result.valueOf("#TypeRef", $TPackage, $CProduct);
		String        Name         = $Result.textOf( "$VarName");
		ParseResult   DValue       = $Result.subResultOf(  "#Value");
		boolean       IsNull       = (DValue == null) || "null".equals($Result.textOf("#Value"));
		String        Flag         = ($Result.textOf("#Abstract") != null)?"Abstract":($Result.textOf("#Dynamic") != null)?"Dynamic":null;
		boolean       IsStatic     = ($Result.textOf("$IsStatic") != null);
		boolean       IsAbstract   = ($Result.textOf("#Abstract") != null);
		MoreData      MD           = MoreData.newMoreDataFromArray($Result.valueOf("#MoreData", $TPackage, $CProduct), true);
		
		if(NonNull && IsNull) {
			$CProduct.reportError("The variable/constant must not be null ("+Name+") <CompilerUtil:1128>", null, VarPos);
			return null;
		}
		
		if(Write  == null) Write  = Read;
		if(Config == null) Config = Type.Private;
		
		if(Flag == null) {	// Direct or Delegate
			if($Result.textOf("$Delegate") == null) {	// Direct
				if(DValue == null) {
					$CProduct.reportError("Direct field must have a default value ("+Name+") <CompilerUtil:1137>", null, VarPos);
					return null;
				}
				
				int EIndex = -1;
				for(int i = $Result.entryCount(); --i >= 0; ) { if(DValue == $Result.subResultOf(i)) { EIndex = i; break; } }
				ElementResolver Resolver = null;
				if(DValue != null) Resolver = Util_ElementResolver.newAttrResolver(IsStatic, Name, $Result, EIndex, $TPackage, $CProduct);

				final DataHolderInfo DHI = new DataHolderInfo(TRef, null, Variable.FactoryName, true, Writable, true, true, null);
				final Location       Loc = new Location($CProduct.getCurrentFeederName(), $CProduct.getCurrentCodeName(), VarLoc);
				
				return new FileCompileResult.TypeField(
						VarName,
						IsStatic,
						IsAbstract,
						StackOwnerAppender.Util.newTempAttrDirect(
							IsStatic,
							$CProduct, $TPackage, $Result,
							Read, Write, Config,
							Name, NonNull, DHI, Loc, MD,
							Resolver, Doc));
						
			} else {	// Delegate
				String  TargetName = $Result.textOf("$TargetName");
				return new FileCompileResult.TypeField(
						VarName,
						IsStatic,
						IsAbstract,
						StackOwnerAppender.Util.newAttrDlgAttr(
							IsStatic,
							$CProduct, $TPackage, $Result,
							Read, Write, Config,
							Name, TargetName, MD, Doc));
			}
		} else if(Flag.equals("Dynamic")) {	// Dynamic
			if((DValue != null) || ($Result.textOf("$Delegate") != null)) {
				$CProduct.reportError("Dynamic fields cannot have default value or delegation target ("+Name+") <CompilerUtil:1198>",
						null, VarPos);
				return null;
			}
			if(IsStatic)  {
				$CProduct.reportError("Dynamic fields cannot be static ("+Name+") <CompilerUtil:1180>", null, VarPos);
				return null;
			}
			return new FileCompileResult.TypeField(
					VarName,
					IsStatic,
					IsAbstract,
					StackOwnerAppender.Util.newAttrDynamic(
						IsStatic,
						$CProduct, $TPackage, $Result,
						Read, Write, Config,
						Name, TRef, MD, Doc));
		} else {
			$CProduct.reportError("Unknown type field flag <TField:48>", null, VarPos);
			return null;
		}
	}
}