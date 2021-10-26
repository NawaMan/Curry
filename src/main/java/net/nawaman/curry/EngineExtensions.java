package net.nawaman.curry;

import net.nawaman.curry.Inst_Assignment.SourceProvider;
import net.nawaman.curry.Instructions_Initializer.*;
import net.nawaman.curry.Instructions_Java.*;
import net.nawaman.curry.Instructions_StackOwner.*;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.util.UString;

public class EngineExtensions {
	
	/** Extension that add advance loop support */
	static public class EE_AdvanceLoop extends EngineExtension {
		
		static public final String Name = "AdvanceLoop"; 
		
		public EE_AdvanceLoop() {}
		
		/**{@inheritDoc}*/ @Override
		protected String getExtName() {
			return Name;
		}
		/**{@inheritDoc}*/ @Override
		protected String[] getRequiredExtensionNames() {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected String initializeThis() {
			this.regInst(-340145); // Instructions_ControlFlow.Inst_While
			this.regInst(-378145); // Instructions_ControlFlow.Inst_Repeat
			this.regInst(-267009); // Instructions_ControlFlow.Inst_For
			this.regInst(-375137); // Instructions_ControlFlow.Inst_FromTo
			this.regInst(-402209); // Instructions_ControlFlow.Inst_ForEach
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected Instruction getNewInstruction(int hSearch) {
			Engine E = this.getEngine();
			switch(hSearch) {
				case -340145: return new Instructions_ControlFlow.Inst_While(  E, true);
				case -378145: return new Instructions_ControlFlow.Inst_Repeat( E, true);
				case -267009: return new Instructions_ControlFlow.Inst_For(    E, true);
				case -375137: return new Instructions_ControlFlow.Inst_FromTo( E, true);
				case -402209: return new Instructions_ControlFlow.Inst_ForEach(E, true);
			}
			return null;
		}
	}
	
	/** Extension that add a Java support */
	static public class EE_Java extends EngineExtension {
		
		static public final String Name = "Java";
		
		/** Constructs an engine extension. */
		public EE_Java() {}
		
		/**{@inheritDoc}*/ @Override
		protected String getExtName() {
			return Name;
		}
		/**{@inheritDoc}*/ @Override
		protected String[] getRequiredExtensionNames() {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected String initializeThis() {	
			this.regInst(-527217); // Inst_GetJavaClassByName
			this.regInst(-366993); // Inst_GetJavaClassOf
			
			this.regInst( -673505); // Inst_GetJavaMethodByParams
			this.regInst( -936193); // Inst_GetJavaMethodByParamClasss
			this.regInst( -738593); // Inst_InvokeJavaObjectMethod
			this.regInst( -689073); // Inst_InvokeJavaClassMethod
			this.regInst(-1195745); // Inst_InvokeJavaObjectMethodByMethod
			this.regInst(-1133809); // Inst_InvokeJavaClassMethodByMethod
			this.regInst( -295297); // Inst_GetJavaField
			
			this.regInst(-1172097); // Inst_SetJavaObjectFieldValueByField
			this.regInst(-1110625); // Inst_SetJavaClassFieldValueByField
			this.regInst( -775521); // Inst_SetJavaObjectFieldValue
			this.regInst( -724913); // Inst_SetJavaClassFieldValue
			this.regInst(-1164609); // Inst_GetJavaObjectFieldValueByField
			this.regInst(-1103329); // Inst_GetJavaClassFieldValueByField
			this.regInst( -769377); // Inst_GetJavaObjectFieldValue
			this.regInst( -718961); // Inst_GetJavaClassFieldValue
			
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected Instruction getNewInstruction(int hSearch) {
			Engine E = this.getEngine();
			switch(hSearch) {
				case  -527217: return new Inst_GetJavaClassByName(E);
				case  -366993: return new Inst_GetJavaClassOf(    E);
				
				case  -673505: return new Inst_GetJavaMethodByParams(         E);
				case  -936193: return new Inst_GetJavaMethodByParamClasss(    E);
				case -1195745: return new Inst_InvokeJavaObjectMethodByMethod(E);
				case -1133809: return new Inst_InvokeJavaClassMethodByMethod( E);
				case  -738593: return new Inst_InvokeJavaObjectMethod(        E);
				case  -689073: return new Inst_InvokeJavaClassMethod(         E);
				case  -295297: return new Inst_GetJavaField(                  E);
				
				case -1172097: return new Inst_SetJavaObjectFieldValueByField(E);
				case -1110625: return new Inst_SetJavaClassFieldValueByField( E);
				case  -775521: return new Inst_SetJavaObjectFieldValue(       E);
				case  -724913: return new Inst_SetJavaClassFieldValue(        E);
				case -1164609: return new Inst_GetJavaObjectFieldValueByField(E);
				case -1103329: return new Inst_GetJavaClassFieldValueByField( E);
				case  -769377: return new Inst_GetJavaObjectFieldValue(       E);
				case  -718961: return new Inst_GetJavaClassFieldValue(        E);
			}
			return null;
		}
	} 
	
	/** Extension that add a Unit support */
	static public class EE_Unit extends EngineExtension {
		
		static public final String Name = "Unit"; 
		
		/** Constructs an engine extension. */
		public EE_Unit() {}
		
		/**{@inheritDoc}*/ @Override
		protected String getExtName() {
			return Name;
		}
		/**{@inheritDoc}*/ @Override
		protected String[] getRequiredExtensionNames() {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected String initializeThis() {
			// Create the unit manager
			this.Engine.TheUnitManager = new MUnit(this.Engine);
			
			// Register Package TypeKind
			this.regTypeKind(new TKPackage(this.Engine));
			
			// Register Unit Type Loader
			this.regTypeLoader(new TLPackage(this.Engine));
			
			this.regInst(-378465); // Inst_GetUnitManager
			this.regInst(-503457); // Inst_GetCurrentPackage
			this.regInst(-233057); // Inst_GetPackage
			this.regInst(-346289); // Inst_EnsurePackage

			// Package StackOwner --------------------------------------------------
			this.regInst(-384497); // Inst_packageInvoke
			this.regInst(-741537); // Inst_packageInvoke_ByParams
			this.regInst(-731505); // Inst_packageInvoke_ByPTRefs
			this.regInst(-903729); // Inst_packageInvoke_ByInterface
			
			this.regInst(-642145); // Inst_packageSetAttrData
			this.regInst(-638113); // Inst_packageGetAttrData
			this.regInst(-593089); // Inst_packageGetAttrType
			this.regInst(-731793); // Inst_packageIsAttrReadable
			this.regInst(-741985); // Inst_packageIsAttrWritable
			this.regInst(-892305); // Inst_packageIsAttrNoTypeCheck
			this.regInst(-546529); // Inst_packageConfigAttr
			this.regInst(-786561); // Inst_packageGetAttrMoreInfo
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected Instruction getNewInstruction(int hSearch) {
			Engine E = this.getEngine();
			
			// Add Source Provider
			Inst_Assignment IA = (Inst_Assignment)E.getInstruction(Inst_Assignment.Name);
			IA.addSourceProvider(new SPPackageAttribute());
			
			switch(hSearch) {
				case -378465: return new Instructions_Package.Inst_GetUnitManager(   E);
				case -503457: return new Instructions_Package.Inst_GetCurrentPackage(E);
				case -233057: return new Instructions_Package.Inst_GetPackage(       E);
				case -346289: return new Instructions_Package.Inst_EnsurePackage(    E);
				
				// Package StackOwner --------------------------------------------------
				case -384497: return new Inst_packageInvoke(            E, false, false);
				case -741537: return new Inst_packageInvoke_ByParams(   E, false       );
				case -731505: return new Inst_packageInvoke_ByPTRefs(   E, false       );
				case -903729: return new Inst_packageInvoke_ByInterface(E, false       );
				
				case -642145: return new Inst_packageSetAttrValue(      E);
				case -638113: return new Inst_packageGetAttrValue(      E);
				case -593089: return new Inst_packageGetAttrType(      E);
				case -731793: return new Inst_packageIsAttrReadable(   E);
				case -741985: return new Inst_packageIsAttrWritable(   E);
				case -892305: return new Inst_packageIsAttrNoTypeCheck(E);
				case -546529: return new Inst_packageConfigAttr(       E);
				case -786561: return new Inst_packageGetAttrMoreInfo(  E);
			}
			return null;
		}
		
		/** Increment access to package attribute */
		static public class SPPackageAttribute extends SourceProvider {
			
			static final public String Name     = "PackageAttribute";
			static final public int    NameHash = UString.hash(Name);
			
			static final SPPackageAttribute Instance = new SPPackageAttribute();
			
			/**{@inheritDoc}*/ @Override protected String getTheName() {
				return Name;
			}
			/**{@inheritDoc}*/ @Override protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
				Object O0 = Ps[0];
				if(!(O0 instanceof String))
					throw new CurryError(
							"Invalid source provider parameter: String is needed at the 1st index. ("+O0+")", C);
				
				Package P = C.getStackOwnerAsPackage();
				if(P == null) throw new CurryError("The current stack does not own by a package", C);
				
				DataHolder.AccessKind DHAK = IsSet?DataHolder.AccessKind.Set:DataHolder.AccessKind.Get;
				return P.accessAttribute(C, null, DHAK, null, (String)O0, V, null, null);
			}
			
			/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return Inst_packageGetAttrValue.Name;    }
			/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return Inst_packageSetAttrValue.Name;    }
			/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return null; }	// TODO - May implement this
			/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return Inst_packageIsAttrWritable.Name; }
			/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return null; }
			/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return Inst_packageGetAttrType.Name;    }
			
			/**{@inheritDoc}*/ @Override protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
				Object O0 = pExpr.getParam(Inst_Assignment.IndexSourceParams);
				if(!(O0 instanceof String)) return false;

				PackageBuilder PB = pCProduct.getOwnerPackageBuilder();
				if(PB == null) return false;
				
				Package P = PB.getPackage();
				return P.searchAttribute(pCProduct.getEngine(), false, null, (String)O0) != null;
			}
			/**{@inheritDoc}*/ @Override protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
				Object O0 = pExpr.getParam(0); if(!(O0 instanceof String)) return false;
				return (Boolean.TRUE.equals(pCProduct.getEngine().execute(this.getIsWritableInstructionName(), O0)));
			}
		}
	}
	
	/** Extension that add a Default Package support */
	static public class EE_DefaultPackage extends EngineExtension {
		
		static public final String Name               = "DefaultPackage"; 
		static public final String DefaultUnitName    = "default";
		static public final String DefaultPackageName = "default";
		
		final EE_DefaultPackage This;
		
		/** Constructs an engine extension. */
		public EE_DefaultPackage() {
			This = this;
		}
		
		/**{@inheritDoc}*/ @Override
		protected String getExtName() {
			return Name;
		}
		/**{@inheritDoc}*/ @Override
		protected String[] getRequiredExtensionNames() {
			return new String[] { EE_Unit.Name };
		}
		/**{@inheritDoc}*/ @Override
		protected String initializeThis() {
			// Create the default package
			UnitFactories.UFMemory UFM = new UnitFactories.UFMemory(
                    // Engine    Unit Name
					this.Engine, DefaultUnitName,
					// Packages
					new Package[] {
						// The Package -> Package Name, Engine Signature
						new Package(DefaultPackageName, this.Engine.getSignature())
					},
					null);
			// Assign it to the engine
			this.Engine.getUnitManager().registerUnitFactory(UFM);
			this.Engine.DefaultPackage        = this.Engine.getUnitManager().getPackage(null, DefaultPackageName);
			this.Engine.DefaultPackageBuilder = new DefaultPackageBuilder.Simple(this);
			
			this.regInst( -493009); // Inst_GetDefaultPackage
			this.regInst( -831041); // Inst_GetDefaultPackageBuilder
				
			this.regInst( -873553); // Inst_AddDefaultPackageVariable
			this.regInst( -979185); // Inst_AddDefaultPackageDataHolder
			this.regInst(-1047409); // Inst_BindDefaultPackageDataHolder
			this.regInst( -879281); // Inst_AddDefaultPackageFunction
			this.regInst( -668705); // Inst_AddDefaultPackageType				
			return null;
		}

		/**{@inheritDoc}*/ @Override protected Instruction getNewInstruction(int hSearch) {
			Engine E = this.getEngine();
			switch(hSearch) {
				case -493009: return new Instructions_DefaultPackage.Inst_GetDefaultPackage(       E);
				case -831041: return new Instructions_DefaultPackage.Inst_GetDefaultPackageBuilder(E);
				
				case  -873553: return new Instructions_DefaultPackage.Inst_AddDefaultPackageVariable(   E);
				case  -979185: return new Instructions_DefaultPackage.Inst_AddDefaultPackageDataHolder( E);
				case -1047409: return new Instructions_DefaultPackage.Inst_BindDefaultPackageDataHolder(E);
				case  -879281: return new Instructions_DefaultPackage.Inst_AddDefaultPackageFunction(   E);
				case  -668705: return new Instructions_DefaultPackage.Inst_AddDefaultPackageType(       E);
			}
			return null;
		}
	}
	
	/** Extension that add stack owner support */
	static public class EE_StackOwner extends EngineExtension {
		
		static public final String Name = "StackOwner";
		
		/** Constructs an engine extension. */
		public EE_StackOwner() {}
		
		/**{@inheritDoc}*/ @Override
		protected String getExtName() {
			return Name;
		}
		/**{@inheritDoc}*/ @Override
		protected String[] getRequiredExtensionNames() {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected String initializeThis() {
			// Assignable SourceProvider for StackOwner Attribute
			Inst_Assignment IA = (Inst_Assignment)this.getEngine().getInstruction(Inst_Assignment.Name);
			IA.addSourceProvider(new SPSOAttribute());
			IA.addSourceProvider(new SPSOAttributeAsType());
			
			// StackOwner ------------------------------------------------
			this.regInst(-130689); // Inst_Invoke
			this.regInst(-383921); // Inst_Invoke_ByParams
			this.regInst(-373889); // Inst_Invoke_ByPTRefs
			this.regInst(-507185); // Inst_Invoke_ByInterface
			
			this.regInst(-310481); // Inst_SetAttrData
			this.regInst(-306449); // Inst_GetAttrData
			this.regInst(-274401); // Inst_GetAttrType
			this.regInst(-374177); // Inst_IsAttrReadable
			this.regInst(-384369); // Inst_IsAttrWritable
			this.regInst(-495761); // Inst_IsAttrNoTypeCheck
			this.regInst(-240817); // Inst_ConfigAttr
			this.regInst(-415969); // Inst_GetAttrMoreInfo
			
			this.regInst(-311217); // Inst_InvokeAsType
			this.regInst(-640993); // Inst_InvokeAsType_ByParams
			this.regInst(-630961); // Inst_InvokeAsType_ByPTRefs
			this.regInst(-792961); // Inst_InvokeAsType_ByInterface
			
			this.regInst(-548609); // Inst_SetAttrDataAsType
			this.regInst(-543425); // Inst_GetAttrDataAsType
			this.regInst(-502641); // Inst_GetAttrTypeAsType
			this.regInst(-627953); // Inst_IsAttrReadableAsType
			this.regInst(-642177); // Inst_IsAttrWritableAsType
			this.regInst(-778433); // Inst_IsAttrNoTypeCheckAsType
			this.regInst(-458689); // Inst_ConfigAttrAsType
			this.regInst(-680785); // Inst_GetAttrMoreInfoAsType
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected Instruction getNewInstruction(int hSearch) {
			Engine E = this.getEngine();
			switch(hSearch) {
				case -130689: return new Inst_Invoke(            E, false, false);
				case -383921: return new Inst_Invoke_ByParams(   E, false       );
				case -373889: return new Inst_Invoke_ByPTRefs(   E, false       );
				case -507185: return new Inst_Invoke_ByInterface(E, false       );
				
				case -310481: return new Inst_SetAttrValue(      E);
				case -306449: return new Inst_GetAttrValue(      E);
				case -274401: return new Inst_GetAttrType(      E);
				case -374177: return new Inst_IsAttrReadable(   E);
				case -384369: return new Inst_IsAttrWritable(   E);
				case -495761: return new Inst_IsAttrNoTypeCheck(E);
				case -240817: return new Inst_ConfigAttr(       E);
				case -415969: return new Inst_GetAttrMoreInfo(  E);
				
				case -311217: return new Inst_InvokeAsType(            E, false, false);
				case -640993: return new Inst_InvokeAsType_ByParams(   E, false       );
				case -630961: return new Inst_InvokeAsType_ByPTRefs(   E, false       );
				case -792961: return new Inst_InvokeAsType_ByInterface(E, false       );
				
				case -548609: return new Inst_SetAttrDataAsType(      E);
				case -543425: return new Inst_GetAttrValueAsType(      E);
				case -502641: return new Inst_GetAttrTypeAsType(      E);
				case -627953: return new Inst_IsAttrReadableAsType(   E);
				case -642177: return new Inst_IsAttrWritableAsType(   E);
				case -778433: return new Inst_IsAttrNoTypeCheckAsType(E);
				case -458689: return new Inst_ConfigAttrAsType(       E);
				case -680785: return new Inst_GetAttrMoreInfoAsType(  E);
			}
			return null;
		}
		
		/** Increment access to StackOwner's attribute */
		static public class SPSOAttribute extends SourceProvider {
			
			static final public String Name     = "SOAttribute";
			static final public int    NameHash = UString.hash(Name);
			
			static final SPSOAttribute Instance = new SPSOAttribute();
			
			/**{@inheritDoc}*/ @Override
			protected String getTheName() {
				return Name;
			}
			
			/**{@inheritDoc}*/ @Override
			protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
				Object O0 = Ps[0];
				Object O1 = Ps[1];
				if(!(O1 instanceof String))
					throw new CurryError(
							"Invalid source provider parameter: String is needed at the 2nd index. ("+O1+")", C);
				
				if(O0 instanceof StackOwner) {
					DataHolder.AccessKind DHAK = IsSet?DataHolder.AccessKind.Set:DataHolder.AccessKind.Get;
					return ((StackOwner)O0).accessAttribute(C, null, DHAK, null, (String)O1, V, null, null);
				} else {
					if(IsSet) return C.getEngine().execute(this.getSetInstructionName(), O0, O1, V);
					else      return C.getEngine().execute(this.getGetInstructionName(), O0, O1);
				}
			}
			
			/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return Inst_GetAttrValue.Name;    }
			/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return Inst_SetAttrValue.Name;    }
			/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return null; }	// TODO - May implement this
			/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return Inst_IsAttrWritable.Name; }
			/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return null; }
			/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return Inst_GetAttrType.Name;    }
			
			/**{@inheritDoc}*/ @Override protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
				Object O0 = pExpr.getParam(Inst_Assignment.IndexSourceParams);
				Object O1 = pExpr.getParam(Inst_Assignment.IndexSourceParams + 1);
				if(!(O1 instanceof String)) return false;
				O0 = pCProduct.getReturnTypeRefOf(O0);
				
				boolean IsStatic = false;
				if(O0 instanceof TypeTypeRef) {
					IsStatic = true;
					O0 = ((TypeTypeRef)O0).getTheRef();
				}
				TypeRef TRef = (TypeRef)O0;
				
				if(IsStatic) return TRef.searchTypeAttribute(  pCProduct.getEngine(), (String)O1) != null;
				else         return TRef.searchObjectAttribute(pCProduct.getEngine(), (String)O1) != null;
			}
			/**{@inheritDoc}*/ @Override protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
				Object O1 = pExpr.getParam(1); if(!(O1 instanceof String)) return false;
				Object O0 = pExpr.getParam(0); O0 = pCProduct.getReturnTypeRefOf(O0);
				return (Boolean.TRUE.equals(pCProduct.getEngine().execute(this.getIsWritableInstructionName(), O0, O1)));
			}
		}
		
		/** Increment access to StackOwner's attribute with AsType */
		static public class SPSOAttributeAsType extends SourceProvider {
		    
			static final public String Name     = "SOAttributeAsType";
			static final public int    NameHash = UString.hash(Name);
			
			static final SPSOAttribute Instance = new SPSOAttribute();
			
			/**{@inheritDoc}*/ @Override
			protected String getTheName() {
				return Name;
			}
			
			/**{@inheritDoc}*/ @Override
			protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
				Object O0 = Ps[0];
				Object O1 = Ps[1]; 
				Object O2 = Ps[2]; 
				if(!(O1 instanceof   Type))
					throw new CurryError(
							"Invalid source provider parameter: Type is needed at the 2nd index. ("+O1+")", C);
				if(!(O2 instanceof String))
					throw new CurryError(
							"Invalid source provider parameter: String is needed at the 3rd index. ("+O2+")", C);
				
				if(O0 instanceof StackOwner) {
					DataHolder.AccessKind DHAK = IsSet?DataHolder.AccessKind.Set:DataHolder.AccessKind.Get;
					return ((StackOwner)O0).accessAttribute(C, null, DHAK, null, (String)O2, V, null, null);
				} else {
					if(IsSet) return C.getEngine().execute(this.getSetInstructionName(), O0, O1, O2, V);
					else      return C.getEngine().execute(this.getGetInstructionName(), O0, O1, O2);
				}
			}
			
			/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return Inst_GetAttrValueAsType.Name;    }
			/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return Inst_SetAttrDataAsType.Name;    }
			/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return null; }	// TODO - May implement this
			/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return Inst_IsAttrWritableAsType.Name; }
			/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return null; }
			/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return Inst_GetAttrTypeAsType.Name;    }
			
			/**{@inheritDoc}*/ @Override
			protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
				Object O0 = pExpr.getParam(Inst_Assignment.IndexSourceParams);
				Object O1 = pExpr.getParam(Inst_Assignment.IndexSourceParams + 1);
				Object O2 = pExpr.getParam(Inst_Assignment.IndexSourceParams + 2);  if(!(O2 instanceof String)) return false;
				
				O1 = pCProduct.getReturnTypeRefOf(O1);
				O0 = pCProduct.getReturnTypeRefOf(O0);
				
				boolean IsStatic = false;
				if(O0 instanceof TypeTypeRef) {
					IsStatic = true;
					O0 = ((TypeTypeRef)O0).getTheRef();
				}
				TypeRef TRef0 = (TypeRef)O0;
				
				TypeRef TRef1 = null;
				if(O1 == null) TRef1 = TRef0;
				else {
					if(!(O1 instanceof TypeTypeRef)) return false;
					TRef1 = ((TypeTypeRef)O1).getTheRef();
					if(!TRef1.canBeAssignedByInstanceOf(pCProduct.getEngine(), TRef0)) return false;
				} 
				
				if(IsStatic) return TRef1.searchTypeAttribute(  pCProduct.getEngine(), (String)O2) != null;
				else         return TRef1.searchObjectAttribute(pCProduct.getEngine(), (String)O2) != null;
			}
			/**{@inheritDoc}*/ @Override
			protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
				Object O0 = pExpr.getParam(Inst_Assignment.IndexSourceParams);
				Object O1 = pExpr.getParam(Inst_Assignment.IndexSourceParams + 1);
				Object O2 = pExpr.getParam(Inst_Assignment.IndexSourceParams + 2);
				
				if(!(O2 instanceof String)) return false;
				O1 = pCProduct.getReturnTypeRefOf(O1);
				O0 = pCProduct.getReturnTypeRefOf(O0);
				return (Boolean.TRUE.equals(pCProduct.getEngine().execute(this.getIsWritableInstructionName(), O0, O1, O2)));
			}
		}
	}
	
	/** Extension that add customizable stack owner support */
	static public class EE_StackOwnerCustomizable extends EngineExtension {
		
		static public final String Name = "StackOwnerCustomizable";
		
		/** Constructs an engine extension. */
		public EE_StackOwnerCustomizable() {}
		
		/**{@inheritDoc}*/ @Override
		protected String getExtName() {
			return Name;
		}
		/**{@inheritDoc}*/ @Override
		protected String[] getRequiredExtensionNames() {
			return new String[] { EE_StackOwner.Name };
		}
		/**{@inheritDoc}*/ @Override
		protected String initializeThis() {
			// Add assignable SourceProvider for StackOwner Customizable
			Inst_Assignment IA = (Inst_Assignment)this.getEngine().getInstruction(Inst_Assignment.Name);
			IA.addSourceProvider(new SPThisAttribute());
			IA.addSourceProvider(new SPTypeAttribute());
			
			// this StackOwner -----------------------------------------------------
			this.regInst(-285265); // Inst_thisInvoke
			this.regInst(-606977); // Inst_thisInvoke_ByParams
			this.regInst(-596945); // Inst_thisInvoke_ByPTRefs
			this.regInst(-755921); // Inst_thisInvoke_ByInterface
			
			this.regInst(-516417); // Inst_thisSetAttrData
			this.regInst(-512385); // Inst_thisGetAttrData
			this.regInst(-471777); // Inst_thisGetAttrType
			this.regInst(-597233); // Inst_thisIsAttrReadable
			this.regInst(-607425); // Inst_thisIsAttrWritable
			this.regInst(-744497); // Inst_thisIsAttrNoTypeCheck
			this.regInst(-429633); // Inst_thisConfigAttr
			this.regInst(-647585); // Inst_thisGetAttrMoreInfo
				
			// super ---------------------------------------------------------------
			this.regInst(-325233); // Inst_thisInvoke
			this.regInst(-662177); // Inst_thisInvoke_ByParams
			this.regInst(-652145); // Inst_thisInvoke_ByPTRefs
			this.regInst(-816833); // Inst_thisInvoke_ByInterface
			
			// type StackOwner -----------------------------------------------------
			this.regInst(-288641); // Inst_typeInvoke
			this.regInst(-611633); // Inst_typeInvoke_ByParams
			this.regInst(-601601); // Inst_typeInvoke_ByPTRefs
			this.regInst(-761057); // Inst_typeInvoke_ByInterface
			
			this.regInst(-520753); // Inst_typeSetAttrData
			this.regInst(-516721); // Inst_typeGetAttrData
			this.regInst(-475953); // Inst_typeGetAttrType
			this.regInst(-601889); // Inst_typeIsAttrReadable
			this.regInst(-612081); // Inst_typeIsAttrWritable
			this.regInst(-749633); // Inst_typeIsAttrNoTypeCheck
			this.regInst(-433649); // Inst_typeConfigAttr
			this.regInst(-652401); // Inst_typeGetAttrMoreInfo

			// StackOwner Initialization -------------------------------------------
			this.regInst( -866689); // Inst_InvokeInitializer_ByParams
			this.regInst( -807201); // Inst_InvokeInitializer_ByPTRefs
			this.regInst(-1040449); // Inst_InvokeInitializer_ByInterface

				// StackOwner Super Initialization -------------------------------------------
			this.regInst(-931409);  // Inst_InvokeSuperInitializer_ByParams
			this.regInst(-870017);  // Inst_InvokeSuperInitializer_ByPTRefs
			this.regInst(-1110881); // Inst_InvokeSuperInitializer_ByInterface
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected Instruction getNewInstruction(int hSearch) {
			Engine E = this.getEngine();
			switch(hSearch) {				
				// this StackOwner -----------------------------------------------------
				case -285265: return new Inst_thisInvoke(            E, false, false);
				case -606977: return new Inst_thisInvoke_ByParams(   E, false       );
				case -596945: return new Inst_thisInvoke_ByPTRefs(   E, false       );
				case -755921: return new Inst_thisInvoke_ByInterface(E, false       );
				
				case -516417: return new Inst_thisSetAttrValue(      E);
				case -512385: return new Inst_thisGetAttrValue(      E);
				case -471777: return new Inst_thisGetAttrType(      E);
				case -597233: return new Inst_thisIsAttrReadable(   E);
				case -607425: return new Inst_thisIsAttrWritable(   E);
				case -744497: return new Inst_thisIsAttrNoTypeCheck(E);
				case -429633: return new Inst_thisConfigAttr(       E);
				case -647585: return new Inst_thisGetAttrMoreInfo(  E);
				
				// Super ---------------------------------------------------------------
				case -325233: return new Inst_superInvoke(            E, false, false);
				case -662177: return new Inst_superInvoke_ByParams(   E, false       );
				case -652145: return new Inst_superInvoke_ByPTRefs(   E, false       );
				case -816833: return new Inst_superInvoke_ByInterface(E, false       );
				
				// Type StackOwner -----------------------------------------------------
				case -288641: return new Inst_typeInvoke(            E, false, false);
				case -611633: return new Inst_typeInvoke_ByParams(   E, false       );
				case -601601: return new Inst_typeInvoke_ByPTRefs(   E, false       );
				case -761057: return new Inst_typeInvoke_ByInterface(E, false       );
				
				case -520753: return new Inst_typeSetAttrValue(      E);
				case -516721: return new Inst_typeGetAttrValue(      E);
				case -475953: return new Inst_typeGetAttrType(      E);
				case -601889: return new Inst_typeIsAttrReadable(   E);
				case -612081: return new Inst_typeIsAttrWritable(   E);
				case -749633: return new Inst_typeIsAttrNoTypeCheck(E);
				case -433649: return new Inst_typeConfigAttr(       E);
				case -652401: return new Inst_typeGetAttrMoreInfo(  E);
				
				// StackOwner Initialization -------------------------------------------
				case  -866689: return new Inst_InvokeThisInitializer_ByParams(   E);
				case  -807201: return new Inst_InvokeThisInitializer_ByPTRefs(   E);
				case -1040449: return new Inst_InvokeThisInitializer_ByInterface(E);

				// StackOwner Super Initialization -------------------------------------------
				case  -931409: return new Inst_InvokeSuperInitializer_ByParams(   E);
				case  -870017: return new Inst_InvokeSuperInitializer_ByPTRefs(   E);
				case -1110881: return new Inst_InvokeSuperInitializer_ByInterface(E);
			}
			return null;
		}
		
		/** Increment access to this attribute */
		static public class SPThisAttribute extends SourceProvider {
			
			static final public String Name     = "ThisAttribute";
			static final public int    NameHash = UString.hash(Name);
			
			static final SPThisAttribute Instance = new SPThisAttribute();
			
			/**{@inheritDoc}*/ @Override
			protected String getTheName() {
				return Name;
			}
			
			/**{@inheritDoc}*/ @Override
			protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
				Object O0 = Ps[0];
				if(!(O0 instanceof String))
					throw new CurryError("Invalid source provider parameter: String is needed at the 1st index. ("+O0+")", C);
				
				Type   T = C.getStackOwnerAsType();
				Object O = C.getStackOwner();
				if((T == null) || (O == null)) throw new CurryError("The current stack does not own by an object", C);
				
				if(O instanceof StackOwner) {
					DataHolder.AccessKind DHAK = IsSet?DataHolder.AccessKind.Set:DataHolder.AccessKind.Get;
					return ((StackOwner)O).accessAttribute(C, null, DHAK, null, (String)O0, V, null, null);
				} else {
					if(IsSet) return C.getEngine().execute(this.getSetInstructionName(), O, O0, V);
					else      return C.getEngine().execute(this.getGetInstructionName(), O, O0);
				}
			}
			
			/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return "this_getAttrValue";   }
			/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return "this_setAttrValue";   }
			/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return null; }	// TODO - May implement this
			/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return "this_isAttrWritable"; }
			/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return null; }
			/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return "this_getAttrType"; }
			
			/**{@inheritDoc}*/ @Override
			protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
				Object O0 = pExpr.getParam(Inst_Assignment.IndexSourceParams);
				if(!(O0 instanceof String)) return false;

				TypeRef TRef = pCProduct.getOwnerTypeRef();
				if(!pCProduct.isOwnerObject() || (TRef == null)) return false;
				return TRef.searchObjectAttribute(pCProduct.getEngine(), (String)O0) != null;
			}
			/**{@inheritDoc}*/ @Override
			protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
				Object O0 = pExpr.getParam(0); if(!(O0 instanceof String)) return false;
				return (Boolean.TRUE.equals(pCProduct.getEngine().execute(this.getIsWritableInstructionName(), O0)));
			}
		}
		
		/** Increment access to Type's attribute  */
		static public class SPTypeAttribute extends SourceProvider {
			
			static final public String Name     = "TypeAttribute";
			static final public int    NameHash = UString.hash(Name);
			
			static final SPTypeAttribute Instance = new SPTypeAttribute();
			
			/**{@inheritDoc}*/ @Override
			protected String getTheName() {
				return Name;
			}
			
			/**{@inheritDoc}*/ @Override
			protected Object process(Context C, Object[] Ps, boolean IsSet, Object V) {
				Object O0 = Ps[0];
				if(!(O0 instanceof String))
					throw new CurryError("Invalid source provider parameter: String is needed at the 1st index. ("+O0+")", C);
				
				Type   T = C.getStackOwnerAsType();
				if(T == null) throw new CurryError("The current stack does not own by an object or a type", C);
				
				DataHolder.AccessKind DHAK = IsSet?DataHolder.AccessKind.Set:DataHolder.AccessKind.Get;
				return T.accessAttribute(C, null, DHAK, null, (String)O0, V, null, null);
			}
			
			/**{@inheritDoc}*/ @Override public String getGetInstructionName()        { return "type_getAttrValue";   }
			/**{@inheritDoc}*/ @Override public String getSetInstructionName()        { return "type_setAttrValue";   }
			/**{@inheritDoc}*/ @Override public String getIsExistInstructionName()    { return null; }	// TODO - May implement this
			/**{@inheritDoc}*/ @Override public String getIsWritableInstructionName() { return "type_isAttrWritable"; }
			/**{@inheritDoc}*/ @Override public String getIsConstantInstructionName() { return null; }
			/**{@inheritDoc}*/ @Override public String getGetTypeInstructionName()    { return "type_getAttrType"; }
			
			/**{@inheritDoc}*/ @Override
			protected boolean ensureExist(CompileProduct pCProduct, Expression pExpr) {
				Object O0 = pExpr.getParam(Inst_Assignment.IndexSourceParams);
				if(!(O0 instanceof String)) return false;

				TypeRef TRef = pCProduct.getOwnerTypeRef();
				if(TRef == null) return false;
				return TRef.searchTypeAttribute(pCProduct.getEngine(), (String)O0) != null;
			}
			/**{@inheritDoc}*/ @Override
			protected boolean ensureWritable(CompileProduct pCProduct, Expression pExpr) {
				Object O0 = pExpr.getParam(Inst_Assignment.IndexSourceParams);
				if(!(O0 instanceof String)) return false;
				return (Boolean.TRUE.equals(pCProduct.getEngine().execute(this.getIsWritableInstructionName(), O0)));
			}
		}
	}
	
	/** Extension that add DataHolder support */
	static public class EE_DataHolder extends EngineExtension {
		
		static public final String Name = "DataHolder";
		
		/** Constructs an engine extension. */
		public EE_DataHolder() {}
		
		/**{@inheritDoc}*/ @Override
		protected String getExtName() {
			return Name;
		}
		/**{@inheritDoc}*/ @Override
		protected String[] getRequiredExtensionNames() {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected String initializeThis() {
			// Register DataHolder TypeKind
			this.regTypeKind(new TKDataHolder(this.getEngine()));
			// Register Instruction
			this.regInst(  -91985); // Instructions_DataHolder.Inst_NewDH
			this.regInst( -223569); // Instructions_DataHolder.Inst_GetDHValue
			this.regInst( -227217); // Instructions_DataHolder.Inst_SetDHValue
			this.regInst( -281489); // Instructions_DataHolder.Inst_IsDHReadable
			this.regInst( -291681); // Instructions_DataHolder.Inst_IsDHWritable
			this.regInst( -195857); // Instructions_DataHolder.Inst_GetDHType
			this.regInst( -32008-20191); // Instructions_DataHolder.Inst_GetDHMoreInfo
			this.regInst( -169697); // Instructions_DataHolder.Inst_ConfigDH
			this.regInst( -390065); // Instructions_DataHolder.Inst_IsDHNoTypeCheck
			this.regInst(-1028865); // Instructions_DataHolder.Inst_AddDataHolderAsLocalVariable
			return null;
		}
		/**{@inheritDoc}*/ @Override
		protected Instruction getNewInstruction(int hSearch) {
			Engine E = this.getEngine();
			switch(hSearch) {
				case   -91985: return new Instructions_DataHolder.Inst_NewDH                       (E);
				case  -223569: return new Instructions_DataHolder.Inst_GetDHValue                  (E);
				case  -227217: return new Instructions_DataHolder.Inst_SetDHValue                  (E);
				case  -281489: return new Instructions_DataHolder.Inst_IsDHReadable                (E);
				case  -291681: return new Instructions_DataHolder.Inst_IsDHWritable                (E);
				case  -195857: return new Instructions_DataHolder.Inst_GetDHType                   (E);
				case  -32008-20191: return new Instructions_DataHolder.Inst_GetDHMoreInfo               (E);
				case  -169697: return new Instructions_DataHolder.Inst_ConfigDH                    (E);
				case  -390065: return new Instructions_DataHolder.Inst_IsDHNoTypeCheck             (E);
				case -1028865: return new Instructions_DataHolder.Inst_AddDataHolderAsLocalVariable(E);
			}
			return null;
		}
	}
}