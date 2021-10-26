package net.nawaman.curry;

import java.io.*;
import java.util.*;

import net.nawaman.curry.Instructions_Array.*;
import net.nawaman.curry.Instructions_Context.*;
import net.nawaman.curry.Instructions_ControlFlow.*;
import net.nawaman.curry.Instructions_Core.*;
import net.nawaman.curry.Instructions_Executable.*;
import net.nawaman.curry.Instructions_ForSpeed.*;
import net.nawaman.curry.Instructions_Operations.*;
import net.nawaman.curry.compiler.EE_Language;
import net.nawaman.curry.extra.text.EE_Text;
import net.nawaman.curry.extra.type_enum.EE_Enum;
import net.nawaman.curry.extra.type_object.EE_Object;
import net.nawaman.curry.util.DataHolderFactory;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.Arrayable;
import net.nawaman.util.MightBeImmutable;
import net.nawaman.util.Objectable;
import net.nawaman.util.UArray;
import net.nawaman.util.UClass;
import net.nawaman.util.UNumber;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

/** Virtual Machine Engine Specification. */
abstract public class EngineSpec implements Objectable {
	
	static final HashMap<String,Class<? extends EngineExtension>> KnownExts;
	static final Vector<String>                                   KnownExtNames;
	static {
		KnownExtNames = new Vector<String>();
		KnownExtNames.add("Java");
		KnownExtNames.add("StackOwner");
		KnownExtNames.add("StackOwnerCustomizable");
		KnownExtNames.add("Unit");
		KnownExtNames.add("DefaultPackage");
		KnownExtNames.add("DataHolder");
		KnownExtNames.add("AdvanceLoop");
		KnownExtNames.add("Enum");
		KnownExtNames.add("Object");
		KnownExtNames.add("Language");
		KnownExtNames.add("Text");
		
		KnownExts = new HashMap<String,Class<? extends EngineExtension>>();
		KnownExts.put("Java",                   EngineExtensions.EE_Java.class);
		KnownExts.put("StackOwner",             EngineExtensions.EE_StackOwner.class);
		KnownExts.put("StackOwnerCustomizable", EngineExtensions.EE_StackOwnerCustomizable.class);
		KnownExts.put("Unit",                   EngineExtensions.EE_Unit.class);
		KnownExts.put("DefaultPackage",         EngineExtensions.EE_DefaultPackage.class);
		KnownExts.put("DataHolder",             EngineExtensions.EE_DataHolder.class);
		KnownExts.put("AdvanceLoop",            EngineExtensions.EE_AdvanceLoop.class);
		KnownExts.put("Enum",                   EE_Enum.class);
		KnownExts.put("Object",                 EE_Object.class);
		KnownExts.put("Language",               EE_Language.class);
		KnownExts.put("Text",                   EE_Text.class);
	}
	
	/** Construct an engine spec */
	public EngineSpec() {
		this.Signature = this.createSignature();
	}

	/** Creates a simple engine spec form the given name and array of extension */
	static public EngineSpec newSimpleEngineSpec(String pName) {
		return newSimpleEngineSpec(pName, KnownExtNames.toArray(new String[KnownExtNames.size()]));
	}

	/** Creates a simple engine spec form the given name and array of extension */
	static public EngineSpec newSimpleEngineSpec(String pName, EngineExtension ... pEExtensions) {
		final String            Name = pName;
		final EngineExtension[] Exts = (pEExtensions != null)?UArray.getLeanArray(pEExtensions).clone():new EngineExtension[0];
		return new EngineSpec() {
			@Override public    String            getEngineName() { return Name; }
			@Override protected EngineExtension[] getExtensions() { return Exts; }
		};
	}

	/**
	 * Creates a simple engine spec form the given name and array of extension classes. This classes must have a public
	 * default constructor.
	 **/
	@SuppressWarnings("unchecked")
    static public EngineSpec newSimpleEngineSpec(String pName, Class<? extends EngineExtension> ... pEExtClasses) {
		Vector<EngineExtension> VExts = new Vector<EngineExtension>();
		for(int i = 0; i < ((pEExtClasses == null)?0:pEExtClasses.length); i++) {
			Class<? extends EngineExtension> Cls = pEExtClasses[i];
			if(Cls == null) continue;
			
			try { VExts.add(Cls.getConstructor().newInstance()); }
			catch(Exception E) {
				throw new RuntimeException(
				            "Error while trying to creates an instance of the engine extension: `"+Cls.getClass()+"`", E);
			}
		}
		
		final String            Name = pName;
		final EngineExtension[] Exts = VExts.toArray(new EngineExtension[VExts.size()]);
		return new EngineSpec() {
			@Override public    String            getEngineName() { return Name; }
			@Override protected EngineExtension[] getExtensions() { return Exts; }
		};
	}

	/**
	 * Creates a simple engine spec form the given name and array of extension classes. This classes must have a public
	 * default constructor.
	 **/
	static public EngineSpec newSimpleEngineSpec(String pName, String ... pEExtNames) {
		Vector<EngineExtension> VExts = new Vector<EngineExtension>();
		for(int i = 0; i < ((pEExtNames == null) ? 0 : pEExtNames.length); i++) {
			String ExtName = pEExtNames[i];
			Class<? extends EngineExtension> Cls = KnownExts.get(ExtName);
			if(Cls == null) {
				Class<?> C = UClass.getClassByName(ExtName);
				if((C != null) && EngineExtension.class.isAssignableFrom(C))
					Cls = C.asSubclass(EngineExtension.class);
			}
			if(Cls == null) {
				System.out.println("Unknown engine extension: `"+ExtName+"`");
				continue;
			}
			
			try { VExts.add(Cls.getConstructor().newInstance()); }
			catch(Exception E) {
				throw new RuntimeException(
				            "Error while trying to creates an instance of the engine extension: `"+ExtName+"`", E);
			}
		}
		
		final String            Name = pName;
		final EngineExtension[] Exts = VExts.toArray(new EngineExtension[VExts.size()]);
		return new EngineSpec() {
			@Override public    String            getEngineName() { return Name; }
			@Override protected EngineExtension[] getExtensions() { return Exts; }
		};
	}
	
	// Engine -----------------------------------------------------------------
	             Engine Engine = null;
	/** Returns the current engine of this spec. */
	final public Engine getEngine() {
		return this.Engine;
	}
	
	// Name -------------------------------------------------------------------
	/** Returns the name of the engine. */
	abstract public String getEngineName();
	
	// Extension --------------------------------------------------------------
	/** Returns the array of engine extensions used in this engine. */
	abstract protected EngineExtension[] getExtensions();

	// Extension --------------------------------------------------------------
	/** Returns the array of engine extensions used in this engine. */
	final public EngineExtension[] getEngineExtensions() {
		return this.getExtensions().clone();
		}
	
	// Signature and Compatibility --------------------------------------------
	private Serializable Signature;
	/** Returns the signature of the engine. */
	final public Serializable getSignature() {
		return this.Signature;
	}
	
	/** Create the signature of this Spec */
	protected Serializable createSignature() {
		return new EngineSpec.Signature(this);
	}
	
	/** Checks if the given engine specification signature is compatible with this spec. */
	public boolean checkCompatibility(Serializable pEngineSpecSignature, StringBuffer pSB) {
		if(!(pEngineSpecSignature instanceof EngineSpec.Signature)) return false;
		return ((EngineSpec.Signature)this.getSignature())
					.checkCompatibility((EngineSpec.Signature)pEngineSpecSignature, pSB);
	}
	
	/** Returns the name of the engine context */
	public String getEngineContextName() {
		return "engine";
	}
	/** Returns the name of the global context */
	public String getGlobalContextName() {
		return "global";
	}
	
	/** Returns `true` if the Unit Manager will be created. */
	public boolean isCreateUnitManager() {
		return true;
	}
	/** Returns `true` if the default package will be created */
	public boolean isCreateDefaultPackage() {
		return true;
	}
	
	// Internal services ---------------------------------------------------------------------------
	/** Display engine message */
	protected void showMsg(String pMsg) {
		if((pMsg != null) && (pMsg.length() != 0)) System.out.println(pMsg);
	}
	/** Display engine error */
	protected void showErr(String pErr) {
		throw new CurryError(pErr);
	}
	
	// Messaging service ---------------------------------------------------------------------------
	
	/** Returns engine-initializing message. */
	public String getEngineInitializingMsg() {
		return "Initializing Engine '"+this.getEngineName()+"' ...";
	}
	/** Returns engine extension initialize success message. */
	public String getEngineExtRegistrationSuccessMsg(EngineExtension pEE) {
		return "";
	}
	/** Returns engine extension initialize fail message. */
	public String getEngineExtRegistrationFailMsg(EngineExtension pEE) {
		return "Fail to register an engine extension '"+pEE.getExtName()+"'.\n" +
		       "The engine '" + this.getEngineName() + "' fails to initialize.\n" + 
		       "Do not try to use this engine or you may experience unexpected behavior.";
	}
	/** Returns required engine-extension does not exist. */
	public String getRequiredEngineExtDoesNotExistMsg(EngineExtension pByEE, String pEEName) {
		return "The engine extension '"+pByEE.getExtName()+"' requires an engine extension '"+pEEName+
		       "' but it does not exist.\n"+
               "The engine '" + this.getEngineName() + "' fails to initialize.\n"+
		       "Do not try to use this engine or you may experience unexpected behavior.";
	}
	/** Returns engine-initialization success message. */
	public String getEngineExtInitializationSuccessMsg(EngineExtension pEE, int LongestNameLength) {
		return "Initialize engine extension '"+UString.ts(pEE.getExtName()+"'", LongestNameLength + 1)+" ... Success.";
	}
	/** Returns engine-initialization success message. */
	public String getEngineExtInitializationFailMsg(EngineExtension pEE, int LongestNameLength) {
		return "The engine extension '"+pEE.getExtName()+"' fails to initialize.\n"+
		       "The engine '"+this.getEngineName()+"' fails to initialize.\n"+
		       "Do not try to use this engine or you may experience unexpected behavior.";
	}
	/** Returns engine-initializing success message. */
	public String getEngineInitializationSuccessMsg() {
		return "Engine '"+this.getEngineName()+"' initialization success.";
	}
	

	/** Returns engine extension initialize success message for non vocal mode. */
	public String getEngineExtRegistrationSuccessMsg_NonVocal(EngineExtension pEE) {
		return "";
	}
	/** Returns engine-initialization success message for non Vocal mode. */
	public String getEngineExtInitializationSuccessMsg_NonVocal(EngineExtension pEE, int LongestNameLength) {
		return "";
	}
	/** Returns engine-initializing success message for non Vocal mode. */
	public String getEngineInitializationSuccessMsg_NonVocal() {
		return "";
	}
	
	/** Returns engine already initialize error message . */
	public String getEngineAlreadyInitializedMsg() {
		return "The engine "+this.getEngineName()+" has alrady been initialized.";
	}
	
	/** Returns null-extension registration fail error . */
	public String getNullExtRegErr() {
		return "Unable to register a null engine extension.";
	}
	/** Returns already-in-uses-extension registration error . */
	public String getExtAlreadyInUseErr(EngineExtension pEE) {
		return "The engine extension "+pEE.getExtName()+" is already in used by the engine "+pEE.getEngine().getName()+".";
	}
	
	/** Returns null-instruction registration fail error . */
	public String getNullInstRegErr(int hSearch) {
		return "The instruction with the name hash `"+hSearch+"` is missing.";
	}
	/** Returns already-in-uses-instruction registration error . */
	public String getInstAlreadyInUseErr(Instruction pInst) {
		return "The instruction `"+pInst.getName()+"` is already in used by the engine "+pInst.getEngine().getName()+".";
	}
	/** Returns already-in-uses-instruction registration error . */
	public String getInstAlreadyInUseErr(int pInstHash) {
		return "The instruction `"+pInstHash+"` is already in used.";
	}
	
	/** Returns null-DataHolderFactory registration fail error . */
	public String getNullDataHolderFactoryRegErr() {
		return "";
	}

	// Preparation of Default Engine Specification -------------------------------------------------

	/** Prepares the engine primitive types.<br/>This method must display error message if needed.**/
	protected boolean preparePrimitiveTypes() {
		// Primitive Types -------------------------------------------------------------------------
		return true;
	}
	
	protected boolean prepareLockedTypes() {
		// Locked Types ----------------------------------------------------------------------------
		//if(!this.Engine.registerPrimitiveType((TKJava.TJava)TKJava.TClass)) return false;
		return true;
	}
	
	/** Prepares the engine type factories.<br/>This method must display error message if needed.**/
	protected boolean prepareTypeKinds() {
		return true;
	}
	
	/** Prepares the engine type resolvers.<br/>This method must display error message if needed.**/
	protected boolean prepareTypeLoaders() {
		return true;
	}
	
	/** Prepares the engine DataHolder factories.<br/>This method must display error message if needed.**/
	protected boolean prepareDataHolderFactories() {
		return true;
	}
	
	/** Prepares the engine instructions.<br/>This method must display error message if needed. **/	
	protected boolean prepareInstructions() {
		return true;
	}
	
	/** Create an instruction that has previously registered but have not yet initialized */
	protected Instruction getNewInstruction(int hSearch) {
		return null;
	}

	/** Perform any actions needed to be done after the initialization and return error string if occurs. */
	protected String preparePredefinedTypes() {
		return this.doPreparePredefinedTypes();
	}

	/** Perform any actions needed to be done after the initialization and return error string if occurs. */
	protected String performPostInitializeAction() {
		return null;
	}
	
	// Engine Instruction --------------------------------------------------------------------------
	
	/** Returns if the instruction with the hash should be used in this engine */
	protected boolean useEngineInstruciont(int pInstHash) {
		return true;
	}
	
	/** Returns a new instance of an engine instruction - NOTE: Use this method to customize engine instruction */
	protected Instruction getNewEngineInstruction(int hSearch) {
		return this.getNewDefaultEngineInstruction(hSearch);
	}
	
	// Possible interaction to the engine -----------------------------------------------------------
	/** Register a type factory in to the engine and return true if success. */
	final protected boolean regTypeKind(TypeKind pTypeKind) {
		return this.Engine.getTypeManager().regTypeKind(null, pTypeKind);
	}
	
	/** Registes a primitive type. */
	final protected boolean regPrimitiveType(TKJava.TJava pType) {
		if(this.Engine.getTypeManager().isPrimitiveType(pType.getDataClass())) return true;
		return this.Engine.getTypeManager().regPrimitiveType(pType);
	}
	/** Registers a primitive type by the alias and the class. */
	final protected boolean regPrimitiveType(String pAlias, Class<?> pCls) {
		if((pAlias == null) || (pAlias.length() == 0)) pAlias = pCls.getCanonicalName();
		if(this.Engine.getTypeManager().isPrimitiveType(pCls)) return true;
		return this.Engine.getTypeManager().regPrimitiveType((TKJava.TJava)TKJava.Instance.getTypeByClass(this.getEngine(), pAlias, pCls));
	}
	
	/** Registers an instruction into the engine. */
	final protected boolean regInst(Instruction pInstruction) {
		if(pInstruction == null) return false;
		this.Engine.regInst(pInstruction, -1, null);
		return true;
	}
	/** Registers an instruction into the engine. */
	final protected boolean regInst(int pInstHash) {
		this.Engine.regInst(null, pInstHash, null);
		return true;
	}
	/** Registers a type resolver into the engine. */
	final protected boolean regTypeLoader(TypeLoader pTypeLoader) {
		if(pTypeLoader == null) return false;
		this.Engine.getTypeManager().regTypeLoader(null, pTypeLoader);
		return true;
	}
	/** Registers a DataHolder factory into the engine. */
	final protected boolean regDataHolderFactory(DataHolderFactory pDHF) {
		if(pDHF           == null) return false;
		if(pDHF.getName() == null) return false;
		if(this.Engine.getDataHolderManager().getDataHolderFactoryIndex(pDHF.getName()) != -1) return false;
		this.Engine.getDataHolderManager().regDataHolderFactory(pDHF);
		return true;
	}
	/** Register a temporary TypeSpec as a pre-define type. The type must be in "<PREDEFINEDTYPE_PACKAGENAME>" package */
	final protected String registerTempPredefineTypeSpec(String TName) {
		return this.Engine.getTypeManager().registerTempPredefineTypeSpec(TName);
	}
	/** Register a type spec as a pre-define type. The type must be in "curry" package */
	final protected String registerPredefineTypeSpec(TypeSpec TS) {
		return this.Engine.getTypeManager().registerPredefineTypeSpec(TS);
	}
	
	// Objectable -------------------------------------------------------------
	/** Returns the string representation of this action record. */
	@Override public String toString() {
		if(!(this.Signature instanceof EngineSpec.Signature)) return null;
		return ((EngineSpec.Signature)this.Signature).toString();
	}
	/** Returns the long string representation of this action record. */
	public String toDetail() {
		if(!(this.Signature instanceof EngineSpec.Signature)) return null;
		return ((EngineSpec.Signature)this.Signature).toDetail();
	}
	
	/** Checks if O is the same or consider to be the same with this engine spec. */
	public boolean is(Object O) { return this == O; }
	@Override public boolean equals(Object O) {
		if(!(O instanceof EngineSpec)) return false; return this.hash() == ((EngineSpec)O).hash();
	}
	/** Returns hash code of this action record. */
	@Override public int hash() { return ((EngineSpec.Signature)this.Signature).hash(); }
	
	/** Engine Specification Signature. */
	static public class Signature implements Serializable, Objectable {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		/** Construct a signature */
		public Signature(EngineSpec pEngineSpec) {
			this.EngineSpecName = pEngineSpec.getEngineName();
			
			this.EngineExtensionNames = new HashSet<String>();
			for(EngineExtension EE : pEngineSpec.getExtensions()) {
				this.EngineExtensionNames.add(EE.getExtName());
			}
		}
		
		String      EngineSpecName       = null;
		Set<String> EngineExtensionNames = null;
		
		/** Checks if the name of the given engine-spec is compatible with this one */
		protected boolean checkEngineSpecNameEquals(EngineSpec.Signature pEngineSpecSignature, StringBuffer pSB) {
			if(!this.EngineSpecName.equals(pEngineSpecSignature.EngineSpecName)) {
				if(pSB != null) {
					pSB.append(
					    "The engine name is unmatched ('" + pEngineSpecSignature.EngineSpecName + "' is required " +
						"but \"" + this.EngineSpecName + "\" is found.");
				}
				return false;
			}
			return true;
		}
		
		/** Checks if all the reqiued extension in the given engine-spec's signature is available in this engine */
		protected boolean checkAllRequiredExtensionExist(EngineSpec.Signature pEngineSpecSignature, StringBuffer pSB) {
			// EngineExtension used in pEngineSpecSignature must also used in this engine
			for(String EEN : pEngineSpecSignature.EngineExtensionNames) {
				if(!this.EngineExtensionNames.contains(EEN)) {
					if(pSB != null) pSB.append("Uncompatible Engine Error: The engine " +
							"extension \"" + EEN + "\" is required.");
					return false;
				}
			}
			
			return true;
		}
		
		/** Checks if the signature is compatible with this signature. */
		public boolean checkCompatibility(EngineSpec.Signature pEngineSpecSignature, StringBuffer pSB) {
			if(!this.checkEngineSpecNameEquals     (pEngineSpecSignature, pSB)) return false;
			if(!this.checkAllRequiredExtensionExist(pEngineSpecSignature, pSB)) return false;
			return true;
		}
		
		// Representation ---------------------------------------------------------
		/** Returns the string representation of this action record. */
		@Override public String toString() {
			return "EngineSpec { Name: \"" + this.EngineSpecName + "\" } ";
		}
		/** Returns the long string representation of this action record. */
		public String toDetail() {
			StringBuffer SB = new StringBuffer();
			SB.append("EngineSpec { \n");
			
			SB.append("\tName: \""); SB.append(this.EngineSpecName); SB.append("\"\n");
			
			SB.append("\tEngine Extensions: [");
			int i = 0;
			for(String EEN : this.EngineExtensionNames) {
				if(i != 0) SB.append(",\n");
				SB.append("\t\t");
				SB.append(EEN);
			}
			SB.append("}");
			return SB.toString();
		}
		/** Returns hash code of this action record. */
		@Override public int hash() {
			return UObject.hash(this.EngineSpecName) + UObject.hash(EngineExtensionNames);
		}
		/** Checks if O is the same or consider to be the same with this signature. */
		public boolean is(Object O) {
			return this == O;
		}
		/**{@inheritDoc}*/ @Override
		public boolean equals(Object O) {
			if(!(O instanceof EngineSpec.Signature)) return false;
			return this.checkCompatibility((EngineSpec.Signature)O, null);
		}
	}
	
	// Engine Instructions ---------------------------------------------------------------------------------------------

	/** Prepares the engine instructions.<br/>This method must display error message if needed. **/	
	protected boolean prepareEngineInstructions() {

		this.regInst(-153041); // Inst_RunOnce
		this.regInst(-747537); // Inst_getPrimitiveTypeByName
		this.regInst(-207009); // Inst_getTypeOf
		this.regInst(-375265); // Inst_getTypeOfClass
		
		// NewInstance
		this.regInst(-276657); // Inst_NewInstance
		this.regInst(-691857); // Inst_NewInstanceByTypeRefs
		this.regInst(-740977); // Inst_NewInstanceByInterface
		
		this.regInst(-335905); // Inst_GetEngineInfo
		this.regInst(-272417); // Inst_GetTypeInfo
		this.regInst(-351361); // Inst_GetTypeStatus
		
		// Instance Of ... type
		this.regInst(-554017); // Inst_InstanceOf => TKJava.TBoolean
		this.regInst(-422577); // Inst_InstanceOf => TKJava.TCharacter
		this.regInst(-508449); // Inst_InstanceOf => TKJava.TString
		this.regInst(-505265); // Inst_InstanceOf => TKJava.TNumber
		this.regInst(-426897); // Inst_InstanceOf => TKJava.TByte
		this.regInst(-386721); // Inst_InstanceOf => TKJava.TInteger
		this.regInst(-426321); // Inst_InstanceOf => TKJava.TLong
		this.regInst(-510017); // Inst_InstanceOf => TKJava.TDouble
		this.regInst(-786801); // Inst_InstanceOf => TKJava.TCharSequence
		
		// Is Kind of  ... type
		this.regInst(-407169); // Inst_IsKindOf TKJava.TNumber
		this.regInst(-665377); // Inst_IsKindOf TKJava.TCharSequence

		this.regInst(-186641); // Inst_Check => Inst_Check.CheckMethod.NEQs
		this.regInst(-180161); // Inst_Check => Inst_Check.CheckMethod.MT
		this.regInst(-342337); // Inst_Check => Inst_Check.CheckMethod.ME
		this.regInst(-180705); // Inst_Check => Inst_Check.CheckMethod.LT
		this.regInst(-343201); // Inst_Check => Inst_Check.CheckMethod.LE
		this.regInst(-102945); // Inst_Check => Inst_Check.CheckMethod.EQ
		this.regInst(-156225); // Inst_Check => Inst_Check.CheckMethod.NE
		
		// Operations ----------------------------------------------------------

		this.regInst(-213953); // Inst_TryNoNull
		this.regInst(-299713); // Inst_DoWhenNoNull
		this.regInst(-450129); // Inst_doWhenValidIndex
		this.regInst(-102513); // Inst_Which
		this.regInst(-54273);  // Inst_Abs
		this.regInst(-55377);  // Inst_Neg
		this.regInst(-125937); // Inst_IsNull
		this.regInst(-209089); // Inst_IsNotNull
		this.regInst(-126897); // Inst_IsZero
		this.regInst(-247553); // Inst_IsPositive
		this.regInst(-239777); // Inst_IsNegative
		this.regInst(-98129);  // Inst_IsOne
		this.regInst(-240081); // Inst_IsMinusOne 
		
		// Boolean ---------------------------------------------------
		this.regInst(-37089); // Inst_AND
		this.regInst(-27025); // Inst_OR
		this.regInst(-43921); // Inst_XOR
		this.regInst(-42321); // Inst_NOT

		// Number ---------------------------------------------------
		this.regInst( -83025); // Inst_Plus
		this.regInst(-189057); // Inst_Subtract
		this.regInst(-193217); // Inst_Multiply
		this.regInst(-125985); // Inst_Divide
		this.regInst(-161009); // Inst_Modulus
		
		// Number bitwise --------------------------------------------
		this.regInst(-53985); // Inst_And
		this.regInst(-37777); // Inst_Or
		this.regInst(-60817); // Inst_Xor
		this.regInst(-59217); // Inst_Not
			
		// Shift -----------------------------------------------------
		this.regInst(-210465); // Inst_ShiftLeft
		this.regInst(-244273); // Inst_ShiftRight
		this.regInst(-557649); // Inst_ShiftRightUnsigned
		
		// Number conversion -----------------------------------------
		this.regInst(-126241); // Inst_To => UNumber.NumberType.BYTE
		this.regInst(-156497); // Inst_To => UNumber.NumberType.SHORT
		this.regInst(-101009); // Inst_To => UNumber.NumberType.INT
		this.regInst(-125665); // Inst_To => UNumber.NumberType.LONG
		this.regInst(-151425); // Inst_To => UNumber.NumberType.FLOAT
		this.regInst(-179473); // Inst_To => UNumber.NumberType.DOUBLE
		this.regInst(-300689); // Inst_To => UNumber.NumberType.BIGINTEGER
		this.regInst(-293761); // Inst_To => UNumber.NumberType.BIGDECIMAL
		
		// String ----------------------------------------------------

		this.regInst(-128257); // Inst_Length
		this.regInst(-119121); // Inst_CharAt
		this.regInst(-126145); // Inst_Concat
		this.regInst(-129617); // Inst_Format
		this.regInst(-202833); // Inst_CharToInt
		this.regInst(-204097); // Inst_IntToChar
		
		// Array -----------------------------------------------------
		this.regInst( -151073); // Inst_IsArray
		this.regInst( -266337); // Inst_IsJavaArray
		this.regInst( -264097); // Inst_IsDataArray
		this.regInst( -204689); // Inst_IsArrayOf
		this.regInst( -367153); // Inst_IsKindOfArray
		this.regInst( -442081); // Inst_IsKindOfArrayOf
		this.regInst( -635073); // Inst_GetLengthArrayObject
		this.regInst( -547201); // Inst_GetLengthArrayType
		this.regInst(-1029729); // Inst_GetComponentTypeArrayObject
		this.regInst( -917697); // Inst_GetComponentTypeArrayType
		this.regInst( -497009); // Inst_GetArrayElementAt
		this.regInst( -502001); // Inst_SetArrayElementAt
		this.regInst( -308065); // Inst_GetArrayType
		this.regInst( -180929); // Inst_NewArray
		this.regInst( -415793); // Inst_NewArrayByClass
		
		this.regInst( -423009); // Inst_NewArrayLiteral
		this.regInst( -599825); // Inst_NewArrayLiteral => TKJava.TAny
		this.regInst( -801393); // Inst_NewArrayLiteral => TKJava.TBoolean
		this.regInst( -649121); // Inst_NewArrayLiteral => TKJava.TByte
		this.regInst( -600561); // Inst_NewArrayLiteral => TKJava.TInteger
		this.regInst( -648545); // Inst_NewArrayLiteral => TKJava.TLong
		this.regInst( -749009); // Inst_NewArrayLiteral => TKJava.TDouble
		this.regInst( -744257); // Inst_NewArrayLiteral => TKJava.TNumber
		this.regInst( -747441); // Inst_NewArrayLiteral => TKJava.TString
		this.regInst( -644801); // Inst_NewArrayLiteral => TKJava.TCharacter
		this.regInst( -645505); // Inst_NewArrayLiteral => TKJava.TType
		this.regInst( -794753); // Inst_NewArrayLiteral => TKJava.TTypeRef
		this.regInst( -690113); // Inst_NewArrayLiteral => TKJava.TClass
		this.regInst(-1086337); // Inst_NewArrayLiteral => TKJava.TSerializable
		this.regInst(-1076097); // Inst_NewArrayLiteral => TKJava.TCharSequence
		
		// Iterator -----------------------------------------------------------
		this.regInst(-427697); // Inst_ArrayToIterator
		this.regInst(-430881); // Inst_IteratorToArray
		
		// For Better speed ----------------------------------------------------
		
		// Iterator --------------------------------------------------
		this.regInst(-428913); // Inst_IteratorHasNext
		this.regInst(-319489); // Inst_IteratorNext
		this.regInst(-276625); // Inst_GetIterator
		
		// Console ---------------------------------------------------
		this.regInst( -82465); // Inst_Show
		this.regInst(-429537); // Inst_ToDisplayString
		this.regInst(-106881); // Inst_Print
		this.regInst(-161313); // Inst_PrintLn
		this.regInst(-311617); // Inst_PrintNewLine
		this.regInst(-132113); // Inst_PrintFormat
		
		this.regInst(-415281); // Inst_ReadConsoleLine
		this.regInst(-797105); // Inst_ReadConsolePasswordLine
		
		// Control Flow ---------------------------------------------
		// Exit 
		this.regInst(-83009); // Inst_Quit
		this.regInst(-81089); // Inst_Exit
		
		// Condition
		this.regInst( -34801); // Inst_If => true
		this.regInst(-133217); // Inst_If => false
		
		this.regInst(-132433); // Inst_Switch
		this.regInst(-127857); // Inst_Choose
		this.regInst( -77633); // Inst_Done
		
		// Loop
		this.regInst( -83649); // Inst_Stop
		this.regInst(-187441); // Inst_Continue
		
		this.regInst( -81233); // Inst_Loop
		this.regInst(-103617); // Inst_While   => false
		this.regInst(-128305); // Inst_Repeat  => false
		this.regInst( -57361); // Inst_For     => false
		this.regInst(-126577); // Inst_FromTo  => false
		this.regInst(-145329); // Inst_ForEach => false

		// Exception -------------------------------------------------
		this.regInst(-108081); // Inst_Throw
		this.regInst(-314577); // Inst_NewThrowable
		this.regInst(-211377); // Inst_TryOrElse
		this.regInst(-181297); // Inst_TryCatch
		this.regInst(-155521); // Inst_TryCast

		// Debug
		this.regInst(-588913); // Inst_SendDebuggerMessage
		this.regInst(-405569); // Inst_IsBeingDebugged
		
		// Context ---------------------------------------------------
		
		// Location
		this.regInst(-383505); // Inst_GetContextInfo
		this.regInst(-557201); // Inst_GetExternalContext
		// Variables
		this.regInst( -273441); // Inst_NewVariable
		this.regInst( -279649); // Inst_NewConstant
		this.regInst( -601665); // Inst_NewBorrowedVariable
		this.regInst( -607873); // Inst_NewBorrowedConstant
		this.regInst( -273425); // Inst_SetVarValue
		this.regInst( -269585); // Inst_GetVarValue
		this.regInst( -239361); // Inst_GetVarType
		this.regInst( -240001); // Inst_IsVarExist
		this.regInst( -413921); // Inst_IsLocalVarExist
		this.regInst( -244513); // Inst_IsConstant
		this.regInst(-1000337); // Inst_IsVariableDefaultDataHolder
		this.regInst(-1177505); // Inst_CheckVariableDataHolderFactory
		// Parent
		this.regInst(-506353); // Inst_SetParentVarValue
		this.regInst(-501361); // Inst_GetParentVarValue
		this.regInst(-462177); // Inst_IsParentVarExist
		this.regInst(-596753); // Inst_IsParentVarConstant
		// Parent By StackName
		this.regInst(-1071521); // Inst_SetParentVarValueByStackName
		this.regInst(-1064417); // Inst_GetParentVarValueByStackName
		this.regInst(-1010449); // Inst_IsParentVarExistByStackName
		this.regInst(-12008-201917); // Inst_IsParentVarConstantByStackName
		// Engine
		this.regInst(-500145); // Inst_SetEngineVarValue
		this.regInst(-495153); // Inst_GetEngineVarValue
		this.regInst(-455969); // Inst_IsEngineVarExist
		this.regInst(-589585); // Inst_IsEngineVarConstant(
		// Global
		this.regInst(-498513); // Inst_NewGlobalVariable
		this.regInst(-498689); // Inst_SetGlobalVarValue
		this.regInst(-493697); // Inst_GetGlobalVarValue
		this.regInst(-454513); // Inst_IsGlobalVarExist
		this.regInst(-587889); // Inst_IsGlobalVarConstant
		this.regInst(-654945); // Inst_ControlGlobalContext
		
		// Incremental Assign ----------------------------------------
		
		this.regInst(-250113); // Inst_IncAssignment
		
		// Executable ------------------------------------------------
		this.regInst( -60081); // Inst_Run
		this.regInst(-244289); // Inst_Run_Unsafe
		this.regInst(-153889); // Inst_RunSelf
		this.regInst( -77665); // Inst_Exec
		this.regInst(-233649); // Inst_Exec_Blind
		this.regInst(-176593); // Inst_ExecSelf
		this.regInst( -75505); // Inst_Call
		this.regInst(-230625); // Inst_Call_Blind
		this.regInst(-173857); // Inst_CallSelf
		this.regInst(-134241); // Inst_Return
		this.regInst(-174689); // Inst_ReCreate
		this.regInst(-245441); // Inst_NewClosure
		return true;
	}
	/** Create an instruction that has previously registered but have not yet initialized */
	protected Instruction getNewDefaultEngineInstruction(int hSearch) {
		Engine E = this.Engine;
		switch(hSearch) {
			case -153041: return new Inst_RunOnce(               E);
			case -747537: return new Inst_getPrimitiveTypeByName(E);
			case -207009: return new Inst_getTypeOf(             E);
			case -375265: return new Inst_getTypeOfClass(        E);
			
			case -335905: return new Inst_GetEngineInfo(E);
			case -272417: return new Inst_GetTypeInfo(  E);
			case -351361: return new Inst_GetTypeStatus(E);
			
			case -276657: return new Inst_NewInstance(           E);
			case -691857: return new Inst_NewInstanceByTypeRefs( E);
			case -740977: return new Inst_NewInstanceByInterface(E);
			
			case -554017: return new Inst_InstanceOf(E, TKJava.TBoolean);
			case -422577: return new Inst_InstanceOf(E, TKJava.TCharacter);
			case -508449: return new Inst_InstanceOf(E, TKJava.TString);
			case -505265: return new Inst_InstanceOf(E, TKJava.TNumber);
			case -426897: return new Inst_InstanceOf(E, TKJava.TByte);
			case -386721: return new Inst_InstanceOf(E, TKJava.TInteger);
			case -426321: return new Inst_InstanceOf(E, TKJava.TLong);
			case -510017: return new Inst_InstanceOf(E, TKJava.TDouble);
			case -786801: return new Inst_InstanceOf(E, TKJava.TCharSequence);
			
			case -407169: return new Inst_IsKindOf(E, TKJava.TNumber);
			case -665377: return new Inst_IsKindOf(E, TKJava.TCharSequence);
			
			case -186641: return new Inst_Check(E, Inst_Check.CheckMethod.NEQs);
			case -180161: return new Inst_Check(E, Inst_Check.CheckMethod.MT);
			case -342337: return new Inst_Check(E, Inst_Check.CheckMethod.ME);
			case -180705: return new Inst_Check(E, Inst_Check.CheckMethod.LT);
			case -343201: return new Inst_Check(E, Inst_Check.CheckMethod.LE);
			case -102945: return new Inst_Check(E, Inst_Check.CheckMethod.EQ);
			case -156225: return new Inst_Check(E, Inst_Check.CheckMethod.NE);
			
			// Operation -----------------------------------------------------------------

			case -213953: return new Inst_TryNoNull(       E);
			case -299713: return new Inst_DoWhenNoNull(    E);
			case -450129: return new Inst_DoWhenValidIndex(E);
			case -102513: return new Inst_Which(           E);
			case  -54273: return new Inst_Abs(             E);
			case  -55377: return new Inst_Neg(             E);
			case -125937: return new Inst_IsNull(          E);
			case -209089: return new Inst_IsNotNull(       E);
			case -126897: return new Inst_IsZero(          E);
			case -247553: return new Inst_IsPositive(      E);
			case -239777: return new Inst_IsNegative(      E);
			case  -98129: return new Inst_IsOne(           E);
			case -240081: return new Inst_IsMinusOne(      E);
			
			// Boolean -------------------------------------------------------------------
			case -37089: return new Inst_AND(E);
			case -27025: return new Inst_OR( E);
			case -43921: return new Inst_XOR(E);
			case -42321: return new Inst_NOT(E);
			
			// Number --------------------------------------------------------------------
			case  -83025: return new InstPlus(    E);
			case -189057: return new InstSubtract(E);
			case -193217: return new InstMultiply(E);
			case -125985: return new InstDivide(  E);
			case -161009: return new InstModulus( E);
			
			// Number bitwise --------------------------------------------
			case -53985: return new InstAnd(E);
			case -37777: return new InstOr( E);
			case -60817: return new InstXor(E);
			case -59217: return new InstNot(E);
			
			// Shift
			case -210465: return new InstShiftLeft(         E);
			case -244273: return new InstShiftRight(        E);
			case -557649: return new InstShiftRightUnsigned(E);
			
			// Number conversion -----------------------------------------
			case -126241: return new InstTo(E, UNumber.NumberType.BYTE,       "b");
			case -156497: return new InstTo(E, UNumber.NumberType.SHORT,      "s");
			case -101009: return new InstTo(E, UNumber.NumberType.INT,        "i");
			case -125665: return new InstTo(E, UNumber.NumberType.LONG,       "l");
			case -151425: return new InstTo(E, UNumber.NumberType.FLOAT,      "f");
			case -179473: return new InstTo(E, UNumber.NumberType.DOUBLE,     "d");
			case -300689: return new InstTo(E, UNumber.NumberType.BIGINTEGER, "java.math.BigInteger");
			case -293761: return new InstTo(E, UNumber.NumberType.BIGDECIMAL, "java.math.BigDecimal");	
			
			// String ----------------------------------------------------
			case -128257: return new InstLength(E);
			case -119121: return new InstCharAt(E);
			case -126145: return new InstConcat(E);
			case -129617: return new InstFormat(E);
			case -202833: return new InstCharToInt(E);
			case -204097: return new InstIntToChar(E);
	
			// Array -----------------------------------------------------
			case  -151073: return new Inst_IsArray(                    E);
			case  -266337: return new Inst_IsJavaArray(                E);
			case  -264097: return new Inst_IsDataArray(                E);
			case  -204689: return new Inst_IsArrayOf(                  E);
			case  -367153: return new Inst_IsKindOfArray(              E);
			case  -442081: return new Inst_IsKindOfArrayOf(            E);
			case  -635073: return new Inst_GetLengthArrayObject(       E);
			case  -547201: return new Inst_GetLengthArrayType(         E);
			case -1029729: return new Inst_GetComponentTypeArrayObject(E);
			case  -917697: return new Inst_GetComponentTypeArrayType(  E);
			case  -497009: return new Inst_GetArrayElementAt(          E);
			case  -502001: return new Inst_SetArrayElementAt(          E);
			case  -308065: return new Inst_GetArrayType(               E);
			case  -180929: return new Inst_NewArray(                   E);
			case  -415793: return new Inst_NewArrayByClass(            E);
			
			case  -423009: return new Inst_NewArrayLiteral(E, "~", null);
			case  -599825: return new Inst_NewArrayLiteral(E, "~", TKJava.TAny);
			case  -801393: return new Inst_NewArrayLiteral(E, "?", TKJava.TBoolean);
			case  -649121: return new Inst_NewArrayLiteral(E, "b", TKJava.TByte);
			case  -600561: return new Inst_NewArrayLiteral(E, "i", TKJava.TInteger);
			case  -648545: return new Inst_NewArrayLiteral(E, "l", TKJava.TLong);
			case  -749009: return new Inst_NewArrayLiteral(E, "d", TKJava.TDouble);
			case  -744257: return new Inst_NewArrayLiteral(E, "#", TKJava.TNumber);
			case  -747441: return new Inst_NewArrayLiteral(E, "$", TKJava.TString);
			case  -644801: return new Inst_NewArrayLiteral(E, "'", TKJava.TCharacter);
			case  -645505: return new Inst_NewArrayLiteral(E, "!", TKJava.TType);
			case  -690113: return new Inst_NewArrayLiteral(E, "@", TKJava.TClass);
			case -1086337: return new Inst_NewArrayLiteral(E, "P", TKJava.TSerializable);
			case -1076097: return new Inst_NewArrayLiteral(E, CharSequence.class.getCanonicalName(), TKJava.TCharSequence);

			case -794753: return new Inst_NewArrayLiteral(E, TypeRef   .class.getCanonicalName(), TKJava.TTypeRef);
			
			// Iterator -----------------------------------------------------------
			case -427697: return new Inst_ArrayToIterator(E);
			case -430881: return new Inst_IteratorToArray(E);
			
			// Iterator --------------------------------------------------
			case -428913: return new Inst_IteratorHasNext(E);
			case -319489: return new Inst_IteratorNext(   E);
			case -276625: return new Inst_GetIterator(    E);
			
			// Console ---------------------------------------------------
			case  -82465: return new InstShow(           E);
			case -429537: return new InstToDisplayString(E);
			case -106881: return new InstPrint(          E);
			case -161313: return new InstPrintLn(        E);
			case -311617: return new InstPrintNewLine(   E);
			case -132113: return new InstPrintFormat(    E);
			
			case -415281: return new Inst_ReadConsoleLine(        E);
			case -797105: return new Inst_ReadConsolePasswordLine(E);
			
			// Control Flow ---------------------------------------------
			// Exit 
			case -83009: return new Inst_Quit(E);
			case -81089: return new Inst_Exit(E);
			
			// Condition
			case  -34801: return new Inst_If(E,  true);
			case -133217: return new Inst_If(E, false);
			
			case -132433: return new Inst_Switch(E);
			case -127857: return new Inst_Choose(E);
			case  -77633: return new Inst_Done(  E);
			
			// Loop
			case  -83649: return new Inst_Stop(    E);
			case -187441: return new Inst_Continue(E);
			
			case  -81233: return new Inst_Loop(   E);
			case -103617: return new Inst_While(  E, false);
			case -128305: return new Inst_Repeat( E, false);
			case  -57361: return new Inst_For(    E, false);
			case -126577: return new Inst_FromTo( E, false);
			case -145329: return new Inst_ForEach(E, false);
	
			// Exception -------------------------------------------------
			case -108081: return new Inst_Throw(       E);
			case -314577: return new Inst_NewThrowable(E);
			case -211377: return new Inst_TryOrElse(   E);
			case -181297: return new Inst_TryCatch(    E);
			case -155521: return new Inst_TryCast(     E);
	
			// Debug
			case -588913: return new Inst_SendDebuggerMessage(E);
			case -405569: return new Inst_IsBeingDebugged(    E);
	
			// Context ---------------------------------------------------
			
			// Location
			case -383505: return new Inst_GetContextInfo(    E);
			case -557201: return new Inst_GetExternalContext(E);
			// Variables
			case  -273441: return new Inst_NewVariable(                   E);
			case  -279649: return new Inst_NewConstant(                   E);
			case  -601665: return new Inst_NewBorrowedVariable(           E);
			case  -607873: return new Inst_NewBorrowedConstant(           E);
			case  -273425: return new Inst_SetVarValue(                   E);
			case  -269585: return new Inst_GetVarValue(                   E);
			case  -239361: return new Inst_GetVarType(                    E);
			case  -240001: return new Inst_IsVarExist(                    E);
			case  -413921: return new Inst_IsLocalVarExist(               E);
			case  -244513: return new Inst_IsConstant(                    E);
			case -1000337: return new Inst_IsVariableDefaultDataHolder(   E);
			case -1177505: return new Inst_CheckVariableDataHolderFactory(E);
			// Parent
			case -506353: return new Inst_SetParentVarValue(  E);
			case -501361: return new Inst_GetParentVarValue(  E);
			case -462177: return new Inst_IsParentVarExist(   E);
			case -596753: return new Inst_IsParentVarConstant(E);
			// Parent By StackName
			case -1071521: return new Inst_SetParentVarValueByStackName(  E);
			case -1064417: return new Inst_GetParentVarValueByStackName(  E);
			case -1010449: return new Inst_IsParentVarExistByStackName(   E);
			case -12008-201917: return new Inst_IsParentVarConstantByStackName(E);
			// Engine
			case -500145: return new Inst_SetEngineVarValue(  E);
			case -495153: return new Inst_GetEngineVarValue(  E);
			case -455969: return new Inst_IsEngineVarExist(   E);
			case -589585: return new Inst_IsEngineVarConstant(E);
			// Global
			case -498513: return new Inst_NewGlobalVariable(   E);
			case -498689: return new Inst_SetGlobalVarValue(   E);
			case -493697: return new Inst_GetGlobalVarValue(   E);
			case -454513: return new Inst_IsGlobalVarExist(    E);
			case -587889: return new Inst_IsGlobalVarConstant( E);
			case -654945: return new Inst_ControlGlobalContext(E);
			
			// Incremental Assign ----------------------------------------
			
			case -250113: return new Inst_Assignment(E, Inst_Assignment.Name);
			     
			// Executable ------------------------------------------------
			case  -60081: return new Inst_Run(       E);
			case -244289: return new Inst_Run_Unsafe(E);
			case -153889: return new Inst_RunSelf(   E);
			case  -77665: return new Inst_Exec(      E, false);
			case -233649: return new Inst_Exec_Blind(E, false);
			case -176593: return new Inst_ExecSelf(  E, false);
			case  -75505: return new Inst_Call(      E, false);
			case -230625: return new Inst_Call_Blind(E, false);
			case -173857: return new Inst_CallSelf(  E, false);
			case -134241: return new Inst_Return(    E);
			case -174689: return new Inst_ReCreate(  E);
			case -245441: return new Inst_NewClosure(E);
		}
		return null;
	}
	

	/** Perform any actions needed to be done after the initialization and return error string if occurs. */
	protected String doPreparePredefinedTypes() {
		Engine      E   = this.getEngine();
		MType       MT  = E.getTypeManager();
		TKInterface TKI = (TKInterface)this.Engine.getTypeManager().getTypeKind(TKInterface.KindName);
		TKVariant   TKV = (TKVariant)  this.Engine.getTypeManager().getTypeKind(TKVariant  .KindName);
		
		String   TName;
		Location Location = new Location("net.nawaman.curry.EngineSpec");
		
		// Iterator<T:any>
		TName = "Iterator"; {
			TypeRef TR;
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName),
					new TypeRef[] { TKJava.TIterator.getTypeRef() },
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new MoreData(Documentation.MIName_Documentation, "Objects that can be immutable"),
					new ExecSignature[] {
						// hasNext():boolean
						ExecSignature.newSignature(
							"hasNext", TKJava.TBoolean.getTypeRef(), Location,
							Documentation.Util.NewMoreData("Returns true if the iteration has more elements.")
						),
						// next():T
						ExecSignature.newSignature(
							"next", new TLParameter.TRParameter(TR, "T"), Location,
							Documentation.Util.NewMoreData("Returns the next element in the iteration.")
						),
						// remove():void
						ExecSignature.newSignature(
							"remove", TKJava.TVoid.getTypeRef(), Location,
							Documentation.Util.NewMoreData("Removes from the underlying collection the last element returned by the iterator (optional operation).")
						)
					}
				)
			);
		}
		
		// Iterable<T>
		TName = "Iterable"; {
			TypeRef TR;
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName),
					new TypeRef[] { TKJava.TIterable.getTypeRef() },
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new MoreData(Documentation.MIName_Documentation, "Objects that can be immutable"),
					new ExecSignature[] {
						// itertor():Iterator<T>
						ExecSignature.newSignature(
							"iterator",
							new TLParametered.TRParametered(
								E,
								MT.getPrefineTypeRef("Iterator"),
								new TLParameter.TRParameter(TR, "T")
							),
							Location,
							Documentation.Util.NewMoreData("Returns true if the iteration has more elements.")
						)
					}
				)
			);
		}
		
		// Variant:<any||:T:|curry=>Comparable<T>>
		TName = "ComparableValueType"; {
			TypeRef TRef = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);
			TypeRef TR_T = new TLParameter.TRParameter(TRef, "T");
			this.registerPredefineTypeSpec(
				TKV.getTypeSpec(
					TRef,
					TKJava.TAny.getTypeRef(),
					new TypeRef[] {
						TR_T,
						new TLParametered.TRParametered(
							new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, "Comparable"),
							TR_T
						),
					},
					TR_T,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new Documentation.Simple("Type of the value for compare."),
					null
				)
			);
		}
		
		// Duck:<{ compare(O1:ComparableValueType<T>,O2:ComparableValueType<T>):int }><T:any>;
		TName = "Comparator"; {
			TypeRef TR = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR,
					new TypeRef[] { TKJava.TComparator.getTypeRef() },
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new MoreData(Documentation.MIName_Documentation, "Comparator for data"),
					new ExecSignature[] {
						// compare(O1:ComparableValueType<T>,O2:ComparableValueType<T>):int
						ExecSignature.newSignature(
							"compare",
							new TypeRef[] {
								new TLParametered.TRParametered(
									E,
									MT.getPrefineTypeRef("ComparableValueType"),
									new TLParameter.TRParameter(TR, "T")
								),
								new TLParametered.TRParametered(
									E,
									MT.getPrefineTypeRef("ComparableValueType"),
									new TLParameter.TRParameter(TR, "T")
								)
							},
							new String[] {
								"O1",
								"O2"
							},
							false,
							TKJava.TInteger.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Compares its two arguments for order.")
						)
					}
				)
			);
		}
		
		// Duck:<{ compareTo(O:ComparableValueType<T>):int }><T:any>;
		TName = "Comparable"; {
			TypeRef TR = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR,
					new TypeRef[] { TKJava.TComparable.getTypeRef() },
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new MoreData(Documentation.MIName_Documentation, "Comparable data"),
					new ExecSignature[] {
						// compareTo(O:ComparableValueType<T>):int
						ExecSignature.newSignature(
							"compareTo",
							new TypeRef[] {
								new TLParametered.TRParametered(
									E,
									MT.getPrefineTypeRef("ComparableValueType"),
									new TLParameter.TRParameter(TR, "T")
								),
							},
							new String[] { "O" },
							false,
							TKJava.TInteger.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Compares its two arguments for order.")
						)
					}
				)
			);
		}
		
		
		// Often used ones --------------------------------------------------------------
		
		// Java ---------------------------------------------------------------
		TypeRef TR_MightBeImmutable    = new TLPrimitive.TRPrimitive(MightBeImmutable.class.getCanonicalName());
		TypeRef TR_Arrayable_Native    = new TLPrimitive.TRPrimitive(Arrayable       .class.getCanonicalName());
		TypeRef TR_Collection_Native   = new TLPrimitive.TRPrimitive(Collection      .class.getCanonicalName());
		TypeRef TR_ListIterator_Native = new TLPrimitive.TRPrimitive(ListIterator    .class.getCanonicalName());
		TypeRef TR_MapEntry_Native     = new TLPrimitive.TRPrimitive(Map.Entry       .class.getCanonicalName());
		TypeRef TR_List_Native         = new TLPrimitive.TRPrimitive(List            .class.getCanonicalName());
		TypeRef TR_Set_Native          = new TLPrimitive.TRPrimitive(Set             .class.getCanonicalName());
		TypeRef TR_Map_Native          = new TLPrimitive.TRPrimitive(Map             .class.getCanonicalName());
		
		// Curry --------------------------------------------------------------
		TypeRef TR_Arrayable;
		TypeRef TR_Collection;
		TypeRef TR_ListIterator;
		TypeRef TR_MapEntry;
		TypeRef TR_List;
		TypeRef TR_Set;
		TypeRef TR_Map;
		
		// Arrayable<T:any>
		TName = "Arrayable"; {
			TR_Arrayable = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);
			TypeRef TR_T = new TLParameter.TRParameter(TR_Arrayable.clone(), "T");
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR_Arrayable,
					new TypeRef[] {
						// Iterable<T>
						new TLParametered.TRParametered(
							E,
							MT.getPrefineTypeRef("Iterable"),
							TR_T.clone()
						),
						// net.nawaman.util.MightBeImmutable
						TR_MightBeImmutable,
						// net.nawaman.util.Arrayable
						TR_Arrayable_Native
					},
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new MoreData(Documentation.MIName_Documentation, "Objects that can be accessed as an array"),
					new ExecSignature[] {
						// set(Index:int, Value:T):T
						ExecSignature.newSignature(
							"set",
							new TypeRef[] { TKJava.TInteger.getTypeRef(), TR_T.clone() },
							new String[]  { "Index",                      "Value"      },
							false,
							TR_T.clone(),
							Location, Documentation.Util.NewMoreData("Changes the element value.")
						),
						// get(Index:int):T
						ExecSignature.newSignature(
							"get",
							new TypeRef[] { TKJava.TInteger.getTypeRef() },
							new String[]  { "Index"                      },
							false,
							TR_T.clone(),
							Location, Documentation.Util.NewMoreData("Returns the element value.")
						),
						// length():int
						ExecSignature.newSignature(
							"length",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TInteger.getTypeRef(),
							Location, Documentation.Util.NewMoreData("Returns the length of the array.")
						)
					}
				)
			);
		}
		
		// Collection<T:any>
		TName = "Collection"; {
			TR_Collection  = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);		// Collection<?>
			TypeRef TR_T   = new TLParameter  .TRParameter  (TR_Collection.clone(), "T");			// T
			TypeRef TRColT = new TLParametered.TRParametered(TR_Collection.clone(), TR_T.clone());	// Collection<T>
			TypeRef TRArrT = TKArray.newArrayTypeRef(TR_T.clone());									// T[]
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR_Collection,
					new TypeRef[] {
						// Iterable<T>
						new TLParametered.TRParametered(
							E,
							MT.getPrefineTypeRef("Iterable"),
							TR_T.clone()
						),
						// java.util.Collection
						TR_Collection_Native
					},
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new MoreData(Documentation.MIName_Documentation, "Collections of Data"),
					new ExecSignature[] {
						// add(Value:T):boolean
						ExecSignature.newSignature(
							"add",
							new TypeRef[] { TR_T.clone() },
							new String[]  { "Value"      },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Adds element into the collection.")
						),
						// addAll(Col:Collection<T>):boolean
						ExecSignature.newSignature(
							"addAll",
							new TypeRef[] { TRColT.clone() },
							new String[]  { "Col"          },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Adds all elements of the given collection into the list.")
						),
						// clear():void
						ExecSignature.newSignature(
							"clear",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TVoid.getTypeRef(),
							Location, Documentation.Util.NewMoreData("Empty the collection.")
						),
						// contains(Value:T):boolean
						ExecSignature.newSignature(
							"contains",
							new TypeRef[] { TR_T.clone() },
							new String[]  { "Value"      },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location, Documentation.Util.NewMoreData("Checks if the collection contains the element.")
						),
						// containsAll(Col:Collection<T>):boolean
						ExecSignature.newSignature(
							"containsAll",
							new TypeRef[] { TRColT.clone() },
							new String[]  { "Col"          },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Checks if the collection contains all elements of the given collection.")
						),
						// isEmpty():boolean
						ExecSignature.newSignature(
							"containsAll",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Checks if the collection is empty.")
						),
						// size():int
						ExecSignature.newSignature(
							"size",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TInteger.getTypeRef(),
							Location, Documentation.Util.NewMoreData("Returns the size of the collection.")
						),
						// isEmpty():boolean
						ExecSignature.newSignature(
							"isEmpty",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TBoolean.getTypeRef(),
							Location, Documentation.Util.NewMoreData("Checks if the list is empty.")
						),
						// remove(Value:T):boolean
						ExecSignature.newSignature(
							"remove",
							new TypeRef[] { TR_T.clone() },
							new String[]  { "Value"      },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Removes a single instance of the specified element from this collection, if it is present."
							)
						),
						// removeAll(Col:Collection<T>):boolean
						ExecSignature.newSignature(
							"removeAll",
							new TypeRef[] { TRColT.clone() },
							new String[]  { "Col"          },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Removes all this collection's elements that are also contained in the specified collection."
							)
						),
						// removeAll(Col:Collection<T>):boolean
						ExecSignature.newSignature(
							"retainAll",
							new TypeRef[] { TRColT.clone() },
							new String[]  { "Col"          },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Removes all this collection's elements that are not contained in the specified collection."
							)
						),
						// toArray():T[]
						ExecSignature.newSignature(
							"toArray",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TRArrT.clone(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns the number of elements in this collection."
							)
						),
						// toArray(Values:T[]):T[]
						ExecSignature.newSignature(
							"toArray",
							new TypeRef[] { TRArrT.clone()   },
							new String[]  { "Values" },
							false,
							TRArrT.clone(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns an array containing all of the elements in this collection."
							)
						)
					}
				)
			);
		}
		
		// ListIterator<T:any>
		TName = "ListIterator"; {
			TR_ListIterator = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);	// ListIterator<?>
			TypeRef TR_T    = new TLParameter.TRParameter(TR_ListIterator.clone(), "T");		// T
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR_ListIterator,
					new TypeRef[] {
						// Iterator<T>
						new TLParametered.TRParametered(
							E,
							MT.getPrefineTypeRef("Iterator"),
							TR_T.clone()
						),
						// java.util.ListIterator
						TR_ListIterator_Native
					},
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new MoreData(Documentation.MIName_Documentation, "Iterator of a list of data"),
					new ExecSignature[] {
						// add(Value:T):void
						ExecSignature.newSignature(
							"add",
							new TypeRef[] { TR_T.clone() },
							new String[]  { "Value"      },
							false,
							TKJava.TVoid.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Add an element at the current postion.")
						),
						// set(Value:T):void
						ExecSignature.newSignature(
							"set",
							new TypeRef[] { TR_T.clone() },
							new String[]  { "Value"      },
							false,
							TKJava.TVoid.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Changes the element value at the current postion.")
						),
						// hasNext():boolean
						ExecSignature.newSignature(
							"hasNext", TKJava.TBoolean.getTypeRef(), Location,
							Documentation.Util.NewMoreData("Returns true if the iteration has next elements.")
						),
						// next():T
						ExecSignature.newSignature(
							"next", TR_T.clone(), Location,
							Documentation.Util.NewMoreData("Returns the next element in the iteration.")
						),
						// hasPrevious():boolean
						ExecSignature.newSignature(
							"hasPrevious", TKJava.TBoolean.getTypeRef(), Location,
							Documentation.Util.NewMoreData("Returns true if the iteration has previous elements.")
						),
						// previous():T
						ExecSignature.newSignature(
							"previous", TR_T.clone(), Location,
							Documentation.Util.NewMoreData("Returns the next element in the iteration.")
						),
						// nextIndex():int
						ExecSignature.newSignature(
							"nextIndex",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TInteger.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Returns the next index in the iteration.")
						),
						// previousIndex():int
						ExecSignature.newSignature(
							"previousIndex",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TInteger.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Returns the previous index in the iteration.")
						),
						// remove():boolean
						ExecSignature.newSignature(
							"remove",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Removes from the underlying collection the last element returned by the iterator " +
								"(optional operation)"
							)
						)
					}
				)
			);
		}
		
		// MapEntry<T:any>
		TName = "MapEntry"; {
			TR_MapEntry = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);
			TypeRef TR_K = new TLParameter.TRParameter(TR_MapEntry.clone(), "K");
			TypeRef TR_V = new TLParameter.TRParameter(TR_MapEntry.clone(), "V");
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR_MapEntry,
					new TypeRef[] {
						// java.util.Map.Entry
						TR_MapEntry_Native
					},
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("K"), new TypeParameterInfo("V")),
					new MoreData(Documentation.MIName_Documentation, "A map entry (key-value pair)."),
					new ExecSignature[] {
						// getKey():K
						ExecSignature.newSignature(
							"getKey",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TR_K.clone(),
							Location,
							Documentation.Util.NewMoreData("Returns the key corresponding to this entry.")
						),
						// setValue(Value:V):V
						ExecSignature.newSignature(
							"setValue",
							new TypeRef[] { TR_V.clone() },
							new String[]  { "Value"      },
							false,
							TR_V.clone(),
							Location,
							Documentation.Util.NewMoreData(
								"Replaces the value corresponding to this entry with the specified value (optional operation)."
							)
						),
						// getValue():V
						ExecSignature.newSignature(
							"getValue",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TR_V.clone(),
							Location,
							Documentation.Util.NewMoreData("Returns the value corresponding to this entry.")
						),
					}
				)
			);
		}
		
		// List<T:any>
		TName = "List"; {
			TR_List        = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);		// List<?>
			TypeRef TR_T   = new TLParameter.TRParameter(TR_List.clone(), "T");						// T
			TypeRef TRColT = new TLParametered.TRParametered(TR_Collection.clone(), TR_T.clone());	// Collection<T>
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR_List,
					new TypeRef[] {
						// Collection<T>
						new TLParametered.TRParametered(
							E,
							MT.getPrefineTypeRef("Collection"),
							TR_T.clone()
						),
						// java.util.List
						TR_List_Native
					},
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new MoreData(Documentation.MIName_Documentation, "Linear collection of Data."),
					new ExecSignature[] {
						// add(Index:int,Value:T):boolean
						ExecSignature.newSignature(
							"add",
							new TypeRef[] { TKJava.TInteger.getTypeRef(), TR_T.clone() },
							new String[]  { "Index",                      "Value"      },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Adds element into the list at the index.")
						),
						// addAll(Index:int,Col:Collection<T>):boolean
						ExecSignature.newSignature(
							"addAll",
							new TypeRef[] { TKJava.TInteger.getTypeRef(), TRColT.clone() },
							new String[]  { "Index",                      "Col"          },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData("Adds all elements of the given collection into the list.")
						),
						// set(Index:int, Value:T):T
						ExecSignature.newSignature(
							"set",
							new TypeRef[] { TKJava.TInteger.getTypeRef(), TR_T.clone() },
							new String[]  { "Index",                      "Value"      },
							false,
							TR_T.clone(),
							Location, Documentation.Util.NewMoreData("Changes the element value.")
						),
						// get(Index:int):T
						ExecSignature.newSignature(
							"get",
							new TypeRef[] { TKJava.TInteger.getTypeRef() },
							new String[]  { "Index"                      },
							false,
							TR_T.clone(),
							Location, Documentation.Util.NewMoreData("Returns the element value.")
						),
						// indexOf(Value:T):int
						ExecSignature.newSignature(
							"indexOf",
							new TypeRef[] { TR_T.clone() },
							new String[]  { "Value"      },
							false,
							TKJava.TInteger.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns the index in this list of the first occurrence of the specified element, or " +
								"-1 if this list does not contain this element."
							)
						),
						// lastIndexOf(Value:T):int
						ExecSignature.newSignature(
							"lastIndexOf",
							new TypeRef[] { TR_T.clone() },
							new String[]  { "Value"      },
							false,
							TKJava.TInteger.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns the index in this list of the last occurrence of the specified element, or " +
								"-1 if this list does not contain this element."
							)
						),
						// listItertor():ListIterator<T>
						ExecSignature.newSignature(
							"listItertor",
							new TLParametered.TRParametered(
								E,
								MT.getPrefineTypeRef("ListIterator"),
								TR_T.clone()
							),
							Location,
							Documentation.Util.NewMoreData(
								"Returns a list iterator of the elements in this list (in proper sequence)."
							)
						),
						// listItertor(Index:int):ListIterator<T>
						ExecSignature.newSignature(
							"listItertor",
							new TypeRef[] { TKJava.TInteger.getTypeRef() },
							new String[]  { "Index"                      },
							false,
							new TLParametered.TRParametered(
								E,
								MT.getPrefineTypeRef("ListIterator"),
								TR_T.clone()
							),
							Location,
							Documentation.Util.NewMoreData(
								"Returns a list iterator of the elements in this list (in proper sequence), starting " +
								"at the specified position in this list."
							)
						),
						// remove(Index:int):void
						ExecSignature.newSignature(
							"remove",
							new TypeRef[] { TKJava.TInteger.getTypeRef() },
							new String[]  { "Index"                      },
							false,
							TKJava.TVoid.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Removes the element at the specified position in this list (optional operation)."
							)
						),
						// removeAt(Index:int):void
						ExecSignature.newSignature(
							"removeAt",
							new TypeRef[] { TKJava.TInteger.getTypeRef() },
							new String[]  { "Index"                      },
							false,
							TKJava.TVoid.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Removes the element at the specified position in this list (optional operation)."
							)
						),
						// subList(FromIndex:int, ToIndex:int):List<T>
						ExecSignature.newSignature(
							"subList",
							new TypeRef[] { TKJava.TInteger.getTypeRef(), TKJava.TInteger.getTypeRef() },
							new String[]  { "FromIndex"                 , "ToIndex"                    },
							false,
							new TLParametered.TRParametered(
								TR_Collection.clone(),
								TR_T.clone()
							),
							Location,
							Documentation.Util.NewMoreData(
								"Returns a view of the portion of this list between the specified FromIndex, " +
								"inclusive, and ToIndex, exclusive."
							)
						)
					}
				)
			);
		}
		
		// Set<T:any>
		TName = "Set"; {
			TR_Set       = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);	// Set<?>
			TypeRef TR_T = new TLParameter.TRParameter(TR_Set.clone(), "T");					// T
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR_Set,
					new TypeRef[] {
						// Collection<T>
						new TLParametered.TRParametered(
							E,
							MT.getPrefineTypeRef("Collection"),
							TR_T.clone()
						),
						// java.util.Set
						TR_Set_Native
					},
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("T")),
					new MoreData(Documentation.MIName_Documentation, "Linear collection of un-repeated Data."),
					new ExecSignature[] {
						// All of the method of set are similar to those in Collection
					}
				)
			);
		}
		
		// Map<K:any,V:any>
		TName = "Map"; {
			TR_Map        = new TLPackage.TRPackage(MType.PREDEFINEDTYPE_PACKAGENAME, TName);	// Map<?>
			TypeRef TR_K  = new TLParameter.TRParameter(TR_Map.clone(), "KeyType");				// KeyType
			TypeRef TR_V  = new TLParameter.TRParameter(TR_Map.clone(), "ValueType");			// ValueType
			TypeRef TR_E  = new TLParametered.TRParametered(TR_MapEntry.clone(), TR_K.clone(), TR_V.clone());
			TypeRef TR_SK = new TLParametered.TRParametered(TR_Set.clone(),        TR_K.clone());
			TypeRef TR_SE = new TLParametered.TRParametered(TR_Set.clone(),        TR_E.clone());
			TypeRef TR_M  = new TLParametered.TRParametered(TR_Map.clone(),        TR_K.clone(), TR_V.clone());
			TypeRef TR_CV = new TLParametered.TRParametered(TR_Collection.clone(), TR_V.clone());
			this.registerPredefineTypeSpec(
				TKI.newInterfaceTypeSpec(
					TR_Map,
					new TypeRef[] {
						// java.util.Map
						TR_Map_Native
					},
					null,
					new ParameterizedTypeInfo(new TypeParameterInfo("KeyType"), new TypeParameterInfo("ValueType")),
					new MoreData(
						Documentation.MIName_Documentation,
						"An object that maps keys to values. A map cannot contain duplicate keys; each key can map to" +
						" at most one value.."
					),
					new ExecSignature[] {
						// clear():void
						ExecSignature.newSignature(
							"clear",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TVoid.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Removes all mappings from this map (optional operation)."
							)
						),
						// containsKey(Key:KeyType):boolean
						ExecSignature.newSignature(
							"containsKey",
							new TypeRef[] { TR_K.clone() },
							new String[]  { "Key"        },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns true if this map contains a mapping for the specified key."
							)
						),
						// containsValue(Value:ValueType):boolean
						ExecSignature.newSignature(
							"containsValue",
							new TypeRef[] { TR_V.clone() },
							new String[]  { "Value"      },
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns true if this map maps one or more keys to the specified value."
							)
						),
						// entrySet():Set<MapEntry<KeyType, ValueType>>
						ExecSignature.newSignature(
							"entrySet",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TR_SE.clone(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns a set view of the mappings contained in this map."
							)
						),
						// get(Key:KeyType):ValueType
						ExecSignature.newSignature(
							"get",
							new TypeRef[] { TR_K.clone() },
							new String[]  { "Key"        },
							false,
							TR_V.clone(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns the value to which this map maps the specified key."
							)
						),
						// clear():void
						ExecSignature.newSignature(
							"isEmpty",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TBoolean.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns true if this map contains no key-value mappings.."
							)
						),
						// keySet():Set<KeyType>
						ExecSignature.newSignature(
							"keySet",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TR_SK.clone(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns a set view of the keys contained in this map."
							)
						),
						// put(Key:KeyType, Value:ValueType):ValueType
						ExecSignature.newSignature(
							"put",
							new TypeRef[] { TR_K.clone(), TR_V.clone() },
							new String[]  { "Key"       , "Value" },
							false,
							TR_V.clone(),
							Location,
							Documentation.Util.NewMoreData(
								"Associates the specified value with the specified key in this map (optional operation)."
							)
						),
						// putAll(M:Map<KeyType, ValueType>):void
						ExecSignature.newSignature(
							"putAll",
							new TypeRef[] { TR_M.clone() },
							new String[]  { "M"          },
							false,
							TKJava.TVoid.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Copies all of the mappings from the specified map to this map (optional operation)."
							)
						),
						// remove(Key:KeyType):ValueType
						ExecSignature.newSignature(
							"remove",
							new TypeRef[] { TR_K.clone() },
							new String[]  { "Key"        },
							false,
							TR_V.clone(),
							Location,
							Documentation.Util.NewMoreData(
								"Removes the mapping for this key from this map if it is present (optional operation)."
							)
						),
						// size():int
						ExecSignature.newSignature(
							"size",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TKJava.TInteger.getTypeRef(),
							Location,
							Documentation.Util.NewMoreData(
								"Removes all mappings from this map (optional operation)."
							)
						),
						// size():int
						ExecSignature.newSignature(
							"values",
							TypeRef.EmptyTypeRefArray,
							UString.EmptyStringArray,
							false,
							TR_CV.clone(),
							Location,
							Documentation.Util.NewMoreData(
								"Returns a collection view of the values contained in this map."
							)
						)						
					}
				)
			);
		}

		return null;
	}
}
