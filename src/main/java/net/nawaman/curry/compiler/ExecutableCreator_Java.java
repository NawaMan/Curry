package net.nawaman.curry.compiler;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Random;

import net.nawaman.curry.Context;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecInterface;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.ExternalContext;
import net.nawaman.curry.Package;
import net.nawaman.curry.Scope;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.JavaExecutable.JavaSubRoutine_Simple;
import net.nawaman.javacompiler.JavaCompiler;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.util.UObject;

/**
 * Create an executable from string of JavaCode
 * 
 * Implementation Note: This class works in a similar way to Java Script Engine in SimpleScript in a sense that java
 *     code is transformed into Java Class reflecting different kind of executable. This class wrap the execution with
 *     Curry executable that targets to be running by the curry engine rather than the script one.
 *     
 *     The reason this is re-create was that at first the environment of Curry are more information rich so some of the
 *     requirements (found in Curry) were not available in Script. However, some of those functionalities are later
 *     ported into Script as it seems to be beneficial. Both implementations turn out to be very similar in concept so
 *     they may be later combined for the better maintainability.
 * 
 \* @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class ExecutableCreator_Java implements ExecutableCreator {
	
	static  public final String EC_NAME = "Java";
	
	public String getName() { return EC_NAME; }
	
	@SuppressWarnings("unchecked")
	public Executable newExecutable(CompileProduct pCProduct, ExternalContext EC, Engine pEngine, String pParam,
			ExecKind pKind, ExecSignature pSignature, String[] pFVNames, Scope pFScope, String pCode) {

		if(pKind == null) pKind = ExecKind.SubRoutine;

		// Class Name -----------------------------------------------------------------------------
		String ClassName = pSignature.getName() + "_" + Math.abs(Random.nextInt());
				
		int[][] SPos  = new int[1][];
		String  ACode = ExecutableCreator_Java.applyTemplate(
		                     pCProduct, pEngine, ClassName, pKind, pSignature, pFScope, pFVNames, pCode, SPos);
		
		JavaCompiler JC = pEngine.getClassPaths().getJavaCompiler();
		JC.addCode(ClassName+".java", "", ACode);
				
		String Err = JC.compile();
		if(Err != null) {
			if(Err.contains("missing return statement")) {
				Err = null;
				ACode = ExecutableCreator_Java.applyTemplate(
						pCProduct, pEngine, ClassName, pKind, pSignature, pFScope, pFVNames, pCode + "\n\treturn null;\n",
						SPos);
				JC.addCode(ClassName+".java", "", ACode);
				Err = JC.compile();
			}
			if(Err != null) throw new RuntimeException("An error creating java "+pKind.toString().toLowerCase()+": Compile Error!\n" + Err);
		}

		try {
			Class<Executable>       Cls  = (Class<Executable>)JC.getClassByName(ClassName.toString());
			Constructor<Executable> Cnst = Cls.getConstructor(Engine.class, ExecSignature.class, String[].class, Scope.class);
			return Cnst.newInstance(pEngine, pSignature, pFVNames, pFScope);
		} catch(Exception E) {
			throw new RuntimeException("An error creating java "+pKind.toString().toLowerCase()+".", E);
		}
	}
	
	static final String EN  = Engine       .class.getCanonicalName();
	static final String ESN = ExecSignature.class.getCanonicalName();
	static final String SN  = Scope        .class.getCanonicalName();
	
	static final String getJavaExecutablePrefix() {
		String JSRName = JavaSubRoutine_Simple.class.getCanonicalName();
		return JSRName.substring(0, JSRName.lastIndexOf('.'));
	}
	
	static final String Template =
		"%s" +	// Imports
		"final public class %s extends " + getJavaExecutablePrefix() + ".Java%s_Complex {" +	// ClassName, Kind
		"" +
		"	public %s("+EN+" pEngine, "+ESN+" pSignature, String[] pFVNames, "+SN+" pFrozenScope) {" +	// ClassName
		"		super(pEngine, %s, pFVNames, pFrozenScope);"	+	// Call Super
		"	}" +
		"%s" +	// Other elements
		"	protected Object run(" + Context.class.getCanonicalName() + " pContext%s) {" +	// Parameter Array
		"		" + ExternalContext.class.getCanonicalName() + " $EC = null;" +
		"		try {" +
		"			$EC = new " + ExternalContext.class.getCanonicalName() + "(pContext);" +
		"%s" +	// Parameter Validation
		"			Object Return = this.run($EC%s);" +	// Parameter Assignment
		"%s" +	// Return Validation
		"			return Return;" +
		"		} finally { $EC.detach(); }" +
		"	}" +
		"	public %s clone() { return new %s(null, this.getSignature(), (this.FVNames == null)?null:this.FVNames.clone(), null); }"+
		"	private %s run(" + ExternalContext.class.getCanonicalName() + " $EC%s) {" +	// Return Type, Parameter List
		"%s" +	// Frozen Variable Recreated
		"%s" +	// Body
		"	}" +
		"}";
	
	static final Random Random = new Random();
	
	static private PTypeProvider PTProvider = null;
	
	static public int[] getSection(String pCode) {
		if(PTProvider == null) {
			try { PTProvider = PTypePackage.UseWithException("JavaSectionExtractor"); }
			catch(Exception E) {
				throw new RuntimeException(
						"Missing parser file 'JavaSectionExtractor.tpt' or it contains an invalid data <ExecutableCreator:60>.",
						E);
			}
		}
		PType PT = PTProvider.getType("JavaSections");
		if(PT == null) {
			throw new RuntimeException(
					"Invalid parser data for 'JavaSectionExtractor.tpt' (no 'JavaSections' type) <ExecutableCreator:68>.");
		}
		Object O = PT.compile(pCode);
		if((O instanceof int[]) && (((int[])O).length == 2)) return (int[])O;
		return null;
	}
	
	static public Object[] validateParameters(Engine pEngine, ExecSignature pSignature, Object[] pParams) {
		if((pSignature.getParamCount() == 0) && ((pParams == null) || (pParams.length == 0))) return UObject.EmptyObjectArray;
		Object[][] AdjParams = pSignature.isVarArgs()?new Object[1][]:null;
		
		if(ExecSignature.Util.canBeAssignedBy_ByParams(pEngine, pSignature, pParams, AdjParams) == ExecInterface.NotMatch)
			throw new RuntimeException(
					String.format("Incompatible parameter `%s` for `%s` <ExecutableCreator_Java:82>.",
					Arrays.toString(pParams),
					pSignature
				));
		return pSignature.isVarArgs()?AdjParams[0]:pParams;
	}
	
	static public void validateReturn(Engine pEngine, ExecSignature pSignature, Object pReturn) {
		if(TKJava.TAny.getTypeRef().equals(pSignature.getReturnTypeRef()))                       return;
		if(TKJava.TVoid.getTypeRef().equals(pSignature.getReturnTypeRef()) && (pReturn == null)) return;
		
		if(!pSignature.getReturnTypeRef().canBeAssignedBy(pEngine, pReturn)) 
			throw new RuntimeException(
					String.format("Invalid return type `%s` for `%s` <ExecutableCreator_Java:97>.",
					pReturn,
					pSignature
				));
		
		return;
	}
	static public String applyTemplate(CompileProduct pCProduct, Engine pEngine, String ClassName, ExecKind pKind,
			ExecSignature pSignature, Scope pFScope, String[] pFVNames, String pCode) {
		return applyTemplate(pCProduct, pEngine, ClassName, pKind, pSignature, pFScope, pFVNames, pCode, null);
	}
		
	// pSectionPos is a return value of the section positions
	static public String applyTemplate(CompileProduct pCProduct, Engine pEngine, String ClassName, ExecKind pKind,
			ExecSignature pSignature, Scope pFScope, String[] pFVNames, String pCode, int[][] pSectionPos) {
		
		if(pKind == null) pKind = ExecKind.SubRoutine;
		if(pCode == null) pCode = "\nreturn null;\n";
		
		String Kind = pKind.toString();
		
		// Super ----------------------------------------------------------------------------------
		String CallSuper = "";
		if(pKind.isFragment())
			 CallSuper = "%1$s.getName(), %1$s.getReturnTypeRef(), %1$s.getLocation(), %1$s.getExtraData()";
		else CallSuper = "%1$s";
		CallSuper = String.format(CallSuper, "pSignature");
		
		// Sections -------------------------------------------------------------------------------
		StringBuilder Imports      = new StringBuilder();
		StringBuilder OtherElemens = new StringBuilder();
		StringBuilder Body         = new StringBuilder();

		int[] SectionPos = getSection(pCode);
		if(SectionPos == null) return null;
		if((pSectionPos != null) && (pSectionPos.length != 0)) pSectionPos[0] = SectionPos;
		
		Imports     .append(pCode.substring(0,             SectionPos[0]));
		OtherElemens.append(pCode.substring(SectionPos[0], SectionPos[1]));
		Body        .append(pCode.substring(SectionPos[1]               ));

		// Return ---------------------------------------------------------------------------------
		String ReturnValidation = String.format(
				"			%s.validateReturn($EC.getEngine(), this.getSignature(), Return);",
				ExecutableCreator_Java.class.getCanonicalName()); 
		
		String ReturnType = "Object"; {
			Class<?> C = pSignature.getReturnTypeRef().getDataClass(pEngine);
			if(C != null) ReturnType = C.getCanonicalName();
		}

		// Parameters -----------------------------------------------------------------------------
		String ParamVarName    = "pParams";
		String AParamVarName   = "$AParams";
		String ParamArrays     = pKind.isFragment()?"":(", Object ... " + ParamVarName);
		String ParamValidation = "";
		String ParamAssignment = "";
		String ParamList       = "";
		if(!pKind.isFragment()) {
			ParamValidation = String.format(
					"			Object[] %s = %s.validateParameters($EC.getEngine(), this.getSignature(), %s);",
					AParamVarName,
					ExecutableCreator_Java.class.getCanonicalName(),
					ParamVarName);
			
			StringBuilder PAssg = new StringBuilder();
			StringBuilder PList = new StringBuilder();
			for(int i = 0; i < pSignature.getParamCount(); i++) {
				TypeRef  TRef  = pSignature.getParamTypeRef(i);
				Class<?> Cls   = (TRef == null)?null:TRef.getDataClass(pEngine);
				String   CName = (Cls == null)?"Object":Cls.getCanonicalName();
				if(CName.startsWith("java.lang.")) CName = CName.substring("java.lang.".length());
				
				PAssg.append(", ");
				PAssg.append("(").append(CName).append(")");
				PAssg.append(AParamVarName).append("[").append(i).append("]");

				PList.append(", ");
				PList.append(CName);
				PList.append(" ");
				PList.append(pSignature.getParamName(i));
			}
			ParamAssignment = PAssg.toString();
			ParamList       = PList.toString();
		}

		// Frozen parameters ----------------------------------------------------------------------
		
		StringBuilder FrozenVariables = new StringBuilder();
		if((pFScope != null) && (pFVNames != null) && (pFVNames.length != 0)) {
			for(int i = 0; i < pFVNames.length; i++) {
				String FVName = pFVNames[i];
				if(FVName == null) continue;
				else {
					PType PT = PTProvider.getType("Identifier");
					if((PT != null) && (PT.match(FVName) == null)) continue;
					if(Context.isStackOwnerVariableNames(FVName))  continue;
				}
				
				Type     Type  = pFScope.getType(pEngine, FVName);
				Class<?> Cls   = (Type == null)?null:Type.getTypeInfo().getDataClass();
				String   CName = (Cls  == null)?null:Cls.getCanonicalName();
				if(CName == null)                       CName = "Object";
				else if(CName.startsWith("java.lang.")) CName = CName.substring("java.lang.".length());
				FrozenVariables.append(String.format(
					"final %s %s = (%s)(this.getFrozenScope().getValue($EC.getEngine(), \"%s\"));",
					CName, FVName, CName, FVName
				));
			}
		}
		
		// Add the variable from the Context ---------------------------------------------------------------------------
		StringBuilder StackOwnerVars = new StringBuilder();
		Class<?> Cls = Object.class;
		
		// StackOwner - $This$
		if(pCProduct != null) {
			if(pCProduct.isOwnerObject() && (pCProduct.getOwnerTypeRef() != null))
				Cls = pCProduct.getOwnerTypeRef().getDataClass(pEngine);
		}
		StackOwnerVars.append(String.format(
			"final %1$s %2$s = (%1$s)$EC.getStackOwner();",
			Cls.getCanonicalName(), Context.StackOwner_VarName
		));
		// StackOwner_AsType - $Type$			
		StackOwnerVars.append(String.format(
			"final %1$s %2$s = (%1$s)$EC.getStackOwnerAsType();",
			Type.class.getCanonicalName(), Context.StackOwnerAsType_VarName
		));
		// StackOwner_AsType - $Package$			
		StackOwnerVars.append(String.format(
			"final %1$s %2$s = (%1$s)$EC.getStackOwnerAsPackage();",
			Package.class.getCanonicalName(), Context.StackOwnerAsPackage_VarName
		));
		
		// Append to FrozenVariables as they are in the same area
		FrozenVariables.append(StackOwnerVars);
		
		// Compose them together ---------------------------------------------------------------------------------------
		
		String Str = String.format(ExecutableCreator_Java.Template,
					Imports,
					ClassName,
					Kind,
					ClassName,
					CallSuper,
					OtherElemens,
					ParamArrays,
					ParamValidation,
					ParamAssignment,
					ReturnValidation,
					ClassName,
					ClassName,
					ReturnType,
					ParamList,
					FrozenVariables,
					Body
				);
		return Str;
	}
}
