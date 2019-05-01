package net.nawaman.curry.compiler;

import java.util.Set;

import net.nawaman.compiler.Code;
import net.nawaman.compiler.TaskEntry;
import net.nawaman.compiler.TaskForCodeUsingRegParser;
import net.nawaman.curry.Accessibility;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Location;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeBuilder;
import net.nawaman.curry.TypeInfo;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.UnitBuilder;
import net.nawaman.curry.TLPackage.TRPackage;
import net.nawaman.curry.compiler.FileCompileResult.PackageElement;
import net.nawaman.curry.compiler.FileCompileResult.PackageFunction;
import net.nawaman.curry.compiler.FileCompileResult.PackageVariable;
import net.nawaman.curry.compiler.FileCompileResult.TypeConstructor;
import net.nawaman.curry.compiler.FileCompileResult.TypeElement;
import net.nawaman.curry.compiler.FileCompileResult.TypeField;
import net.nawaman.curry.compiler.FileCompileResult.TypeMethod;
import net.nawaman.curry.compiler.FileCompileResult.TypeSpecification;
import net.nawaman.curry.compiler.FileCompileResult.TypeWithElements;
import net.nawaman.curry.extra.type_object.TBClass;
import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;
import net.nawaman.task.TaskOptions;

/** Task for compile file */
abstract class FileCompileTasks_Code<FCResult extends FileCompileResult> extends TaskForCodeUsingRegParser {
	static final Class<?>[] IOCls = new Class<?>[] { ParseResult.class, UnitBuilder.class, String.class, String[].class };
	static final Object[]   IODVs = new Object[]   { null, null, null };

	/** Constructs a CompileTask */
	protected FileCompileTasks_Code(String pName, PTypeProvider pTProvider) {
		super(pName, IOCls, IODVs, IOCls);
		this.setTypeProvider(pTProvider);
	}

	abstract void doTask(PTypeProvider $TProvider, CompileProduct $CProduct,
			int FeederIndex, String FeederName, String CodeName,
			UnitBuilder UB, PackageBuilder PB, FCResult pFCResult);

	@SuppressWarnings("unchecked")
	/** Performs the task */ @Override
	public Object[] doTask(net.nawaman.compiler.CompileProduct pContext,
			TaskEntry pTE, TaskOptions pOptions, Object[] pIns) {
		
		int    FeederIndex = pContext.getCurrentFeederIndex();
		String FeederName  = pContext.getCurrentFeederName();
		String CodeName    = pContext.getCurrentCodeName();
		
		PTypeProvider  $TProvider = this.getTypeProvider();
		CompileProduct $CProduct  = (CompileProduct)pContext;
		
		// Ensure valid state
		if($CProduct.getCompilationState().isNormal()) {
			$CProduct.reportError(String.format("Invalid compilation state for file: Normal", FeederName, CodeName), null);
			return null;
		}
		
		// Checks if the code is valid (no parse error or any stop the compile process from going forward)
		if(Boolean.FALSE.equals(pContext.getCodeData(FeederIndex, CodeName, "IsValid"))) return null;
		
		// Get parse result from the input or create one if this is the first time parsing is run
		ParseResult $ThisResult = (ParseResult)pIns[0];
		ParseResult $Result     = ($ThisResult == null)?null:$ThisResult.subOf(0);
		// If the parse result is still null, report error and mark the code as invalid
		if($Result == null) {
			$CProduct.reportError(
					String.format("The file \'%s:%s\' does not contain a valid code", FeederName, CodeName), null);
			pContext.setCodeData(FeederIndex, CodeName, "IsValid", false);
			return null;
		}
		// There is error in the code, mark it as invalid (the parser should already report the error).
		if(!$Result.ensureNoError($TProvider, $CProduct)) {
			pContext.setCodeData(FeederIndex, CodeName, "IsValid", false);
			return null;
		}

		// Ensure UnitBuilder of this feeder exists.
		UnitBuilder UB = (UnitBuilder)pIns[1];
		if(UB == null) {
			$CProduct.reportError(String.format(
					"Internal Error: Missing UnitBuilder for \'%s:%s\' <FileCompileTask_File:553>",
					FeederName, CodeName), null, $Result.getStartPosition());
			return null;
		}
		
		String         PackageName = (String)  pIns[2];
		String[]       Imports     = (String[])pIns[3];
		FCResult       Result      = null;
		PackageBuilder PB          = null;
		
		if(!$CProduct.getCompilationState().isTypeRegistration()) {
			// Get the package builder or create a new one
			if((PB = UB.getPackageBuilder(PackageName)) == null)
				PB = UB.newPackageBuilder(PackageName);

			// Reset the context
			$CProduct.clearContext($CProduct.CCompiler.TheID);
			$CProduct.resetContextForFragment(
					$CProduct.CCompiler.TheID, TKJava.TAny.getTypeRef(), false, null, PackageName,
					null, null, null, true);
		}
		
		if((Imports != null) && (Imports.length != 0))
			$CProduct.addImport(Imports);
		
		if(!$CProduct.getCompilationState().isFullCompilation() && !$CProduct.getCompilationState().isTypeValidation()) {
		
			// Get the parser type to compile the parse result
			PType PTFile = $TProvider.getType(CLRegParser.ParserTypeName_File);
			if(PTFile == null) {
				$CProduct.reportError(String.format(
							"Unable to compile a file when the parser type '%s' does not exist",
							CLRegParser.ParserTypeName_File
						), null);
				pContext.setCodeData(FeederIndex, CodeName, "IsValid", false);
				return null;
			}
			
			// Do the compilation and ensure the right type of result
			Object RawResult = null;
			RawResult = PTFile.compile($Result, 0, null, $CProduct, $TProvider);
			
			// Empty one
			if((RawResult instanceof String) || ((RawResult != null) && (RawResult.toString().length() == 0)))
				return null;
				
			// Non empty one
			try { if((Result = ((FCResult)RawResult)) == null) return null; }
			catch (ClassCastException E) {}
			
			if(Result == null) {
				$CProduct.reportError(
					String.format(
						"Invalid compile result from '%s' at the '%s' state. Found: %s",
						CLRegParser.ParserTypeName_File, $CProduct.getCompilationState(),
						RawResult
					),
					null,
					$Result.getStartPosition());
				
				pContext.setCodeData(FeederIndex, CodeName, "IsValid", false);
				return null;
			}
			
			// Ensure the package is not null
			if(PackageName == null) {
				PackageName = Result.getPackageName();		
				if(PackageName == null) {
					$CProduct.reportError(String.format(
							"Internal Error: Missing Package definition for \'%s:%s\'",
							FeederName, CodeName),
						null);
					return null;
				}
			}
			
			// Extract the imports
			if(Imports == null) Imports = Result.getImports();
		}
		
		if($CProduct.getCompilationState().isTypeRegistration()) {
			// Get the package builder or create a new one
			if((PB = UB.getPackageBuilder(PackageName)) == null)
				PB = UB.newPackageBuilder(PackageName);

			// Reset the context
			$CProduct.clearContext($CProduct.CCompiler.TheID);
			$CProduct.resetContextForFragment(
					$CProduct.CCompiler.TheID, TKJava.TAny.getTypeRef(), false, null, PackageName,
					null, null, null, true);
		}
		
		// Do the task
		this.doTask($TProvider, $CProduct, FeederIndex, FeederName, CodeName, UB, PB, Result);
		
		return new Object[] { $ThisResult, UB, PackageName, Imports };
	}
	
	// SubClasses ------------------------------------------------------------------------------------------------------

	/** Task for types operations */
	static abstract class TypeRelated<FCRTypes extends FileCompileResult.TypeRelated>
	                          extends FileCompileTasks_Code<FCRTypes> {
		/** Constructs a CompileTask */
		protected TypeRelated(String pName, PTypeProvider pTPackage) {
			super(pName, pTPackage);
		}

		abstract void processNewType(CompileProduct $CProduct, PackageBuilder PB, Accessibility Access, TypeSpec TS,
				Location TL);

		/** Performs the task */ @Override
		void doTask(PTypeProvider $TPackage, CompileProduct $CProduct,
				int FeederIndex, String FeederName, String CodeName,
				UnitBuilder UB, PackageBuilder PB, FCRTypes pFCRTypes) {
			
			// No type is defined here.
			if(pFCRTypes.getTypeDataCount() == 0) return;
			
			for(int i = 0; i < pFCRTypes.getTypeDataCount(); i++) {
				TypeSpecification TData = pFCRTypes.getTypeData(i);
				if (TData == null) continue;

				String          TypeName  = TData.getTypeName();
				Accessibility   Access    = TData.getAccessibility();
				TypeSpecCreator TSCreator = TData.getTSCreator();
				TypeRef         TRef      = new TRPackage(PB.getName(), TypeName);

				TypeSpec TSpec = TSCreator.newTypeSpec(
				                     $CProduct.getEngine(),
				                     TRef,
				                     false,
				                     TData.getDocument());

				this.processNewType($CProduct, PB, Access, TSpec, TData.getLocation());
			}

			return;
		}
	}
	
	/** Task for registering types */
	static public class TypeRegistration extends TypeRelated<FileCompileResult.TypeRegistration> {
		/** Constructs a CompileTask */
		protected TypeRegistration(PTypeProvider pTPackage) {
			super("TypeRegistration", pTPackage);
		}

		@Override
		void processNewType(CompileProduct $CProduct, PackageBuilder PB, Accessibility Access, TypeSpec TS, Location TL) {
			String TName = ((TRPackage) TS.getTypeRef()).getTypeName();
			String Err = null;
			if (!PB.isActive())        Err = "Internal Error: The package builder become in active ";
			if (PB.isTypeExist(TName)) Err = "Internal Error: The type is already exist ";
			if (Err != null) {
				$CProduct.reportError(
					Err + String.format("(%s=>%s)", PB.getName(), TName),
					null, TL.getColumn(), TL.getLineNumber());
				return;
			}

			PB.newTypeBuilder(Access, TS, TL);
		}
	}	

	/** Task for constructing the file structure */
	static public class StructureRegistration extends FileCompileTasks_Code<FileCompileResult.StructuralRegistration> {
		/** Constructs a CompileTask */
		protected StructureRegistration(PTypeProvider pTPackage) {
			super("Structure", pTPackage);
		}

		/** Performs the task */ @Override
		void doTask(PTypeProvider $TPackage, CompileProduct $CProduct, int FeederIndex, String FeederName,
				String CodeName, UnitBuilder UB, PackageBuilder PB, FileCompileResult.StructuralRegistration Result) {

			boolean isPBActive = PB.isActive();
			if(!isPBActive) {
				$CProduct.reportError(
					String.format(
						"Internal error: The package builder (%s=>%s) is no longer active.",
						FeederName, PB.getName()),
					null);
			}
			
			// Register all package elements
			int PECount = Result.getPackageElementCount();
			for(int i = 0; i < PECount; i++) {
				PackageElement<?>  PE  = Result.getPackageElement(i);
				StackOwnerAppender SOA = null;
				
				if (PE == null) continue;
				else if(PE instanceof PackageVariable) {
					String VName = ((PackageVariable)PE).getName();
					if(PB.isAttrExist(VName)) {
						$CProduct.reportError(
							String.format(
								"Internal error: The package variable (%s=>%s) is already exist.",
								PB.getName(), VName),
							null);
						continue;
					}
				} else if(PE instanceof PackageFunction) {
					ExecSignature FSignature = ((PackageFunction)PE).getSignature();
					if(PB.isOperExist(FSignature)) {
						$CProduct.reportError(
							String.format(
								"Internal error: The package variable (%s=>%s) is already exist.",
								PB.getName(), FSignature),
							null);
						continue;
					}
				} else {
				//else if (!(PE instanceof PackageVariable) && !(PE instanceof PackageFunction)) {
					$CProduct.reportError(
						String.format(
							"Internal error: Unknown Package element kind: %s.",
							PE),
						null);
						
				}
				
				// Get the appender
				SOA = PE.getStackOwnerAppender();
				if(SOA == null) {
					$CProduct.reportError(String.format(
							"Compilation Error: Missing the StackOwnerAppender for %s=>%s.",
							PB.getName(), PE.getIdentity()),
						null);
					continue;
				}
				
				// Append
				SOA.append($CProduct, PB);
			}
			
			// Register all the elements in types
			int TCount = Result.getTypeWithElementsCount();
			for(int i = 0; i < TCount; i++) {
				TypeWithElements TWE = Result.getTypeWithElements(i);
				if(TWE == null) continue;

				TypeBuilder TB = PB.getTypeBuilder(TWE.getTypeName());
				if(TB == null) {
					$CProduct.reportError(String.format(
							"Internal Error: Missing TypeBuilder for \'%s:%s:%s=>%s\' <FileCompileTask_Structure:842>",
							FeederName, CodeName, PB.getName(), TWE.getTypeName()), null);
					continue;
				}

				boolean isTBActive = TB.isActive();
				TB.getSpec().resetForCompilation();

				int TECount = TWE.getTypeElementCount();
				for (int e = 0; e < TECount; e++) {
					TypeElement<?> TE = TWE.getTypeElement(e);
					if(TE == null) continue;
					
					if(!isTBActive) {
						$CProduct.reportError(
							String.format(
								"Internal error: The type builder (%s=>%s) is no longer active.",
								PB.getName(), TB.getName()),
							null);
						break;
					}
					
					StackOwnerAppender SOA = null;
					
					if(TE instanceof TypeField) {
						TypeField TF       = (TypeField)TE;
						String    FName    = TF.getName();
						boolean   IsStatic = TF.isStatic();
						
						if((IsStatic && TB.isStaticAttrExist(FName)) || (!IsStatic && TB.isAttrExist(FName))) {
							$CProduct.reportError(
								String.format(
									"Internal error: The field (%s=>%s.%s) is already exist.",
									PB.getName(), TB.getName(), FName),
								null);
							continue;
						}
						
					} else if(TE instanceof TypeMethod) {
						TypeMethod    TM         = (TypeMethod)TE;
						ExecSignature MSignature = TM.getSignature();
						boolean       IsStatic   = TM.isStatic();

						if((IsStatic && TB.isStaticOperExist(MSignature)) || (!IsStatic && TB.isOperExist(MSignature))) {
							$CProduct.reportError(
								String.format(
									"Internal error: The method (%s=>%s.%s) is already exist.",
									PB.getName(), TB.getName(), MSignature),
								null);
							continue;
						}
						
					} else if(TE instanceof TypeConstructor) {
						TypeConstructor TC         = (TypeConstructor)TE;
						ExecSignature   CSignature = TC.getSignature();

						if(TB.isConstructorExist(CSignature)) {
							$CProduct.reportError(
								String.format(
									"The constructor (%s=>%s.%s) is already exist <CurryLanguage:969>",
									PB.getName(), TB.getName(), CSignature),
								null);
							continue;
						}
						
					} else {
						$CProduct.reportError(
								String.format(
									"Internal error: Unknown Type element kind: %s.",
									TE),
								null);
						continue;
					}
					
					// Get the appender
					SOA = TE.getStackOwnerAppender();
					if(SOA == null) {
						$CProduct.reportError(String.format(
								"Compilation Error: Missing the StackOwnerAppender for %s=>%s.%s.",
								PB.getName(), TB.getName() , TE.getIdentity()),
							null);
						continue;
					}
					
					// Append
					SOA.append($CProduct, TB);
					
					// Consider if this is a Static and/or Dynamic Delegatee
					if((TE instanceof TypeField) && (TB instanceof TBClass)) {
						TypeField TF = (TypeField)TE;
						String    FN = TF.getName();
						
						// Set the Static Delegatee and Dynamic Delegatee
						MoreData MD = TB.getTempAttrMoreData(FN);
						if(MD != null) {
							if(Boolean.TRUE.equals(MD.getData(Util_TypeElement.enSTATIC_DELEGATEE)))
								((TBClass)TB).addStaticDelegatee(TF.getName());
							if(Boolean.TRUE.equals(MD.getData(Util_TypeElement.enDYNAMIC_DELEGATEE)))
								((TBClass)TB).addDynamicDelegatee(TF.getName());
						}
					}
				}
			}
			
			return;
		}
	}
	
	/** Task for refining type specification */
	static public class TypeRefinition extends TypeRelated<FileCompileResult.TypeRegistration> {
		/** Constructs a CompileTask */
		protected TypeRefinition(PTypeProvider pTPackage) {
			super("TypeRefinition", pTPackage);
		}

		@Override
		void processNewType(CompileProduct $CProduct, PackageBuilder PB, Accessibility Access, TypeSpec TS, Location TL) {
			String TName = ((TRPackage) TS.getTypeRef()).getTypeName();
			String Err = null;
			if (!PB.isActive())                           Err = "Internal Error: The package builder become inactive";
			if (!PB.isValidID($CProduct.CCompiler.TheID)) Err = "Internal Error: The secret ID is invalid";
			if (!PB.isTypeExist(TName))                   Err = "Internal Error: The type is not exist";
			if (Err != null) {
				$CProduct.reportError(
						Err + String.format("(%s=>%s)", PB.getName(), TName),
						null, TL.getColumn(), TL.getLineNumber());
				return;
			}

			PB.replaceTypeSpec($CProduct.CCompiler.TheID, Access, TS, TL);
		}
	}
	
	/**
	 * Task for compiling a fileTName
	 * 
	 * The compilation is done by executing ElementResolver assigned to each elements during the registration.
	 **/
	static public class FullCompilation  extends FileCompileTasks_Code<FileCompileResult> {
		/** Constructs a CompileTask */
		protected FullCompilation(PTypeProvider pTPackage) {
			super("FullComilation", pTPackage);
		}

		/** Performs the task */ @Override
		void doTask(PTypeProvider $TPackage, CompileProduct $CProduct, int FeederIndex, String FeederName,
				String CodeName, UnitBuilder UB, PackageBuilder PB, FileCompileResult Result) {
				
			// Resolve all Package Variable
			{
				Set<String> TANames = PB.getTempAttrNames();
				if(TANames != null) {
					for(String TAName : TANames) {
						Object O = PB.getTempAttrTempData(TAName);
						if(O == null) continue;
						if(!(O instanceof ElementResolver)) {
							Location L = PB.getTempAttrLocation(TAName);
							int      P = -1;
							if(L != null) {
								Code C = (Code)$CProduct.getCodeData(FeederIndex, CodeName, "Code");
								if(C != null) P = C.getNearestValidPositionOf(L.getColumn(), L.getLineNumber());
							}
							$CProduct.reportError(
								String.format(
									"Internal error package variable %s=>%s's temporary data must be a ElementResolder.",
									PB.getName(), TAName),
								null, P);
							continue;
						}

						// Only get the one that is in this code
						if(!CodeName.equals(PB.getTempAttrLocation(TAName).getCodeName())) continue;
						
						ElementResolver AR = (ElementResolver)O;
						AR.resolveElement($CProduct, PB);
					}
				}
			}

			// Resolve all Package Operation
			{
				Set<ExecSignature> TOSignatures = PB.getTempOperSignatures();
				if(TOSignatures != null) {
					for(ExecSignature TOSignature : TOSignatures) {
						Object O = PB.getTempOperTempData(TOSignature);
						if(O == null) continue;
						if(!(O instanceof ElementResolver)) {
							Location L = PB.getTempOperLocation(TOSignature);
							int      P = -1;
							if(L != null) {
								Code C = (Code)$CProduct.getCodeData(FeederIndex, CodeName, "Code");
								if(C != null) P = C.getNearestValidPositionOf(L.getColumn(), L.getLineNumber());
							}
							$CProduct.reportError(
								String.format(
									"Internal error package operation %s=>%s's temporary data must be a ElementResolder.",
									PB.getName(), TOSignature),
								null, P);
							continue;
						}
						
						// Only get the one that is in this code
						if(!CodeName.equals(PB.getTempOperLocation(TOSignature).getCodeName())) continue;
						
						ElementResolver AR = (ElementResolver)O;
						AR.resolveElement($CProduct, PB);
					}
				}
			}
			
			// Resolve elements of all Type with elements
			Set<String> TNames = PB.getTypeBuilderNames();
			if(TNames != null) {
				for(String TName : TNames) {
					TypeBuilder TB = PB.getTypeBuilder(TName);
					if(TB == null) continue;

					// Only get the one that is in this code
					TypeSpec TS = TB.getSpec();
					if(!CodeName.equals(TS.getLocation($CProduct.getEngine()).getCodeName())) continue;
					
					try { TS.resetForCompilation(); } catch (Exception E) {}

					// Resolve all Type Variable
					Set<String> TANames = null;
					for(int i = 0; i < 2; i++) {
						TANames = (i == 0)?TB.getTempAttrNames():TB.getStaticTempAttrNames();
						if(TANames == null) continue;
						
						for(String TAName : TANames) {
							Object O = (i == 0)?TB.getTempAttrTempData(TAName):TB.getStaticTempAttrTempData(TAName);
							if(O == null) continue;
							if(!(O instanceof ElementResolver)) {
								Location L = (i == 0)?TB.getTempAttrLocation(TAName):TB.getStaticTempAttrLocation(TAName);
								int      P = -1;
								if(L != null) {
									Code C = (Code)$CProduct.getCodeData(FeederIndex, CodeName, "Code");
									if(C != null) P = C.getNearestValidPositionOf(L.getColumn(), L.getLineNumber());
								}
								$CProduct.reportError(
									String.format(
										"Internal error package variable %s=>%s.%s's temporary data must be a ElementResolder.",
										PB.getName(), TB.getName(), TAName),
									null, P);
								continue;
							}
								
							ElementResolver AR = (ElementResolver)O;
							AR.resolveElement($CProduct, TB);
						}
					}

					// Resolve all Type Operation
					Set<ExecSignature> TOSigns = null;
					for(int i = 0; i < 2; i++) {
						TOSigns = (i == 0)?TB.getTempOperSignatures():TB.getStaticTempOperSignatures();
						
						if(TOSigns == null) continue;
						for(ExecSignature TOSignature : TOSigns) {
							Object O = (i == 0)?TB.getTempOperTempData(TOSignature):TB.getStaticTempOperTempData(TOSignature);
							if(O == null) continue;
							if(!(O instanceof ElementResolver)) {
								Location L = (i == 0)?TB.getTempOperLocation(TOSignature):TB.getStaticTempOperLocation(TOSignature);
								int      P = -1;
								if(L != null) {
									Code C = (Code)$CProduct.getCodeData(FeederIndex, CodeName, "Code");
									if(C != null) P = C.getNearestValidPositionOf(L.getColumn(), L.getLineNumber());
								}
								$CProduct.reportError(
									String.format(
										"Internal error type operation %s=>%s.%s's temporary data must be a ElementResolver.",
										PB.getName(), TB.getName(), TOSignature),
									null, P);
								continue;
							}
							
							ElementResolver AR = (ElementResolver)O;
							AR.resolveElement($CProduct, TB);
						}
					}

					// Resolve all Type Constructor
					Set<ExecSignature> TCSigns = TB.getTempConsSignatures();
					if(TCSigns == null) continue;
					for(ExecSignature TCSignature : TCSigns) {
						Object O = TB.getTempConsTempData(TCSignature);
						if(O == null) continue;
						if(!(O instanceof ElementResolver)) {
							Location L = TB.getTempConstLocation(TCSignature);
							int      P = -1;
							if(L != null) {
								Code C = (Code)$CProduct.getCodeData(FeederIndex, CodeName, "Code");
								if(C != null) P = C.getNearestValidPositionOf(L.getColumn(), L.getLineNumber());
							}
							$CProduct.reportError(
								String.format(
									"Internal error type constructor %s=>%s.%s's temporary data must be a ElementResolver.",
									PB.getName(), TB.getName(), TCSignature),
								null, P);
							continue;
						}
							
						ElementResolver AR = (ElementResolver)O;
						AR.resolveElement($CProduct, TB);
					}
				}
			}
			return;
		}
	}

	/** Task for validating type */
	static public class TypeValidation extends FileCompileTasks_Code<FileCompileResult.StructuralRegistration> {
		/** Constructs a CompileTask */
		protected TypeValidation(PTypeProvider pTPackage) {
			super("Validation", pTPackage);
		}

		/** Performs the task */ @Override
		void doTask(PTypeProvider $TPackage, CompileProduct $CProduct, int FeederIndex, String FeederName,
				String CodeName, UnitBuilder UB, PackageBuilder PB, FileCompileResult.StructuralRegistration Result) {
			Set<String> TBNames;
			if((PB == null) || ((TBNames = PB.getTypeBuilderNames()) == null)) return;

			// Try initialize the type so that type checking can be done
			for(String TName : TBNames) {
				try {
					TypeBuilder TB = PB.getTypeBuilder(TName);
					TypeSpec    TS = TB.getSpec();

					// Only get the one that is in this code
					if(!CodeName.equals(TS.getLocation($CProduct.getEngine()).getCodeName())) continue;
					
					TypeRef TR = TS.getTypeRef();
					
					// This cause the type to be initialized and the validation (same when loaded normally) is performed.
					Type T = $CProduct.getTypeAtCompileTime(TR, true);
					if(T == null) continue;
					
					TypeInfo TI = T.getTypeInfo();
					TI.getConstructorInfos();
					TI.getOperationInfos();
					TI.getAttributeInfos();
					TI.getObjectOperationInfos();
					TI.getObjectAttributeInfos();
				} catch (Exception E) {}
			}
		}
		
	}
}