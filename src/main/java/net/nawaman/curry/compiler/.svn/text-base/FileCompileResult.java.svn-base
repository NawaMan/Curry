package net.nawaman.curry.compiler;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Documentation;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Location;
import net.nawaman.curry.Type;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

/** Result from the compilation of a file */
abstract public class FileCompileResult {
	
	public FileCompileResult(String pPackageName, String[] pImports, Object pSecretID) {
		this.PackageName = pPackageName;
		this.Imports     = (pImports == null)?UString.EmptyStringArray:pImports.clone();
		this.SecretID    = pSecretID;
	}
	
	private String PackageName;
	
	/** Returns the package name appear in the file */
	public String getPackageName() {
		return this.PackageName;
	}
	
	private String[] Imports;
	
	/** Returns a copy import list (in the format that CompileProduct Understand) */
	public String[] getImports() {
		return (this.Imports.length == 0)?this.Imports:this.Imports.clone();
	}
	
	/** Returns the number of imports appear in the code */
	public int getImportCount() {
		return this.Imports.length;
	}
	
	/** Returns the import at the index I (the import is in the format that CompileProduct Understand) */
	public String getImport(int I) {
		return ((I < 0) || (I >= this.Imports.length))?null:this.Imports[I];
	}
	
	private Object SecretID;

	/**
	 * Checks if the given ID match the secret ID (use it check if modification to FileCompileResult internal data is
	 *   allowed)
	 **/
	final protected boolean verifySecretID(Object ID) {
		if(this.SecretID == null) return true;
		return UObject.equal(this .SecretID, ID); 
	}
	
	// SubClasses ------------------------------------------------------------------------------------------------------
	
	// File -------------------------------------------------------------------------------------------------
	
	/** FileCompileResult that contains type */
	static public class TypeRelated extends FileCompileResult {
		
		/** Constructs a FileCompileResult for TypeRegistration */
		protected TypeRelated(String pPackageName, String[] pImports, Object pSecretID) {
			super(pPackageName, pImports, pSecretID);
		}
		
		private Vector<TypeSpecification> TypeRegistrationDatas = new Vector<TypeSpecification>();
		
		/** Add a type data for the registration */
		public boolean addTypeData(Object ID, String pTypeName, Documentation pDocument,
				Accessibility pAccessibility, boolean pCanHaveElements, Location pLocation, TypeSpecCreator pTSCreator) {
			if(!this.verifySecretID(ID)) return false;
			return this.addTypeData(ID, new TypeSpecification(pTypeName, pDocument, pAccessibility, pCanHaveElements,
					pLocation, pTSCreator));
		}

		/** Add a type data for the registration */
		public boolean addTypeData(Object ID, TypeSpecification pTypeData) {
			if((pTypeData == null) || !this.verifySecretID(ID)) return false;

			if(pTypeData.getTypeName()      == null) throw new NullPointerException("Missing type name.");
			if(pTypeData.getAccessibility() == null) throw new NullPointerException("Missing type accessibility.");
			if(pTypeData.getTSCreator()     == null) throw new NullPointerException("Missing type specification creator.");
			
			this.TypeRegistrationDatas.add(pTypeData);
			return false;
		}
		
		/** Returns the number of type datas is being registered with this result. */
		public int getTypeDataCount() {
			return this.TypeRegistrationDatas.size();
		}
		
		/** Returns the type registration data at the index */
		public TypeSpecification getTypeData(int I) {
			return ((I < 0) || (I > this.TypeRegistrationDatas.size()))?null:this.TypeRegistrationDatas.get(I);
		}

	}
	
	/** FileCompileResult for TypeRegistration */
	static public class TypeRegistration extends TypeRelated {
		/** Constructs a FileCompileResult for TypeRegistration */
		public TypeRegistration(String pPackageName, String[] pImports, Object pSecretID) {
			super(pPackageName, pImports, pSecretID);
		}
	}
	
	/**
	 * FileCompileResult for TypeRefinition
	 * 
	 * Implementation note:
	 *   The process of TypeRefinition is to allow the type parser/compiler to verify the specification base one the 
	 *     newly available information about other types being compiled at the same time. Since types are process in 
	 *     order based on its appearance in Feeder and Code, a type may have not been registered before another type 
	 *     that refer to it (let say inherit) is being registered. Therefore, the client type may be unable to verify 
	 *     some of the constrains. But at the the second state (TypeRefinition), all types are registered with its rough
	 *     specification (include its type kind and type reference); thus, the client type can verify some of this
	 *     constrains early on.
	 *   With all the reason above, the result of the parsing and compilation in this state is identical to the first. 
	 *     The type compiler may returns null as no refinition to the type specification. It may even ignore this state 
	 *     all together if it does not care about the early verification.   
	 **/
	static public class TypeRefinition extends TypeRelated {
		/** Constructs a FileCompileResult for TypeRefinition */
		public TypeRefinition(String pPackageName, String[] pImports, Object pSecretID) {
			super(pPackageName, pImports, pSecretID);
		}
	}

	/** FileCompileResult for StructuralRegistration */
	static public class StructuralRegistration extends FileCompileResult {
		
		static public final PackageElement<?>[] EmptyPackageElements = new PackageElement<?>[0]; 

		private HashMap<Object, PackageElement<?>> PackageElements  = new HashMap<Object, PackageElement<?>>();
		private HashMap<Object, TypeWithElements>  TypeWithElements = new HashMap<Object, TypeWithElements>();
		
		private Object[] PackageElementKeys  = null;
		private Object[] TypeWithElementKeys = null;
		
		/** Constructs a FileCompileResult for StructuralRegistration */
		public StructuralRegistration(String pPackageName, String[] pImports, Object pSecretID) {
			super(pPackageName, pImports, pSecretID);
		}
		
		// Package Elements --------------------------------------------------------------------------------------------
		
		/** Returns a copy of elements in the package */
		public PackageElement<?>[] getPackageElements() {
			return (this.PackageElements.size() == 0)
					?EmptyPackageElements
					:this.PackageElements.values().toArray(EmptyPackageElements);
		}
		
		/** Returns the number of elements in the package */
		public int getPackageElementCount() {
			return this.PackageElements.size();
		}
		
		/** Returns the package element at the index I */
		public PackageElement<?> getPackageElement(int I) {
			int Count = this.PackageElements.size();
			if(this.PackageElementKeys == null)
				this.PackageElementKeys = this.PackageElements.keySet().toArray(new Object[Count]);
			
			return ((I < 0) || (I >= Count))?null:this.PackageElements.get(this.PackageElementKeys[I]);
		}
		
		/** Adds a package element to this package */
		final protected boolean addElement(Object pID, PackageElement<?> pPackageElement) {
			if(!this.verifySecretID(pID)) return false;
			
			if((pPackageElement == null) || this.PackageElements.containsKey(pPackageElement.getIdentity()))
				return false;
			this.PackageElements.put(pPackageElement.getIdentity(), pPackageElement);
			return true;
		}
		
		/** Adds a package variable to this package */
		public boolean addVariable(Object pID, PackageVariable pPackageVariable) {
			return this.addElement(pID, pPackageVariable);
		}
		
		/** Adds a package function to this package */
		public boolean addFunction(Object pID, PackageFunction pPackageFunction) {
			return this.addElement(pID, pPackageFunction);
		}

		// Type elements -----------------------------------------------------------------------------------------------
		
		/** Returns the number of elements in the package */
		public int getTypeWithElementsCount() {
			return this.TypeWithElements.size();
		}
		
		/** Returns the package element at the index I */
		public TypeWithElements getTypeWithElements(int I) {
			int Count = this.TypeWithElements.size();
			if(this.TypeWithElementKeys == null)
				this.TypeWithElementKeys = this.TypeWithElements.keySet().toArray(new Object[Count]);
			
			return ((I < 0) || (I >= Count))?null:this.TypeWithElements.get(this.TypeWithElementKeys[I]);
		}
		
		/** Adds a type with element in to this package */
		public boolean addTypeWithElements(Object pID, TypeWithElements pTypeWithElements) {
			if(!this.verifySecretID(pID)) return false;
			
			if((pTypeWithElements == null) || this.TypeWithElements.containsKey(pTypeWithElements.getTypeName()))
				return false;
			
			this.TypeWithElements.put(pTypeWithElements.getTypeName(), pTypeWithElements);
			return true;
		}
	}
	
	// Types ------------------------------------------------------------------------------------------------

	/** CompileResult for a type spec */
	static public final class TypeSpecification {
		
		private String          TypeName;
		private TypeSpecCreator TSCreator;
		private Documentation   Document;
		private Location        Location;
		private Accessibility   Accessibility;
		
		public TypeSpecification(String pTypeName, Documentation pDocument, Accessibility pAccessibility,
				boolean pCanHaveElements, Location pLocation, TypeSpecCreator pTSCreator) {
			if(pAccessibility instanceof Type.Access)
				throw new IllegalArgumentException("Type accessibibility is now permit in Package element ("+
						pAccessibility+" "+pTypeName+") <FileCompileResult.TypeSpecification:237>.");
			
			this.TypeName        = pTypeName;
			this.TSCreator       = pTSCreator;
			this.Document        = pDocument;
			this.Location        = pLocation;
			this.Accessibility   = pAccessibility;
		}
		
		/** Returns the type name  */
		public String getTypeName() {
			return this.TypeName;
		}
		
		/** Returns the type specification creator of this type */
		public TypeSpecCreator getTSCreator() {
			return this.TSCreator;
		}
		
		/** Returns the documentation */
		public Documentation getDocument() {
			return this.Document;
		}
		
		/** Returns the locations */
		public Location getLocation() {
			return this.Location;
		}
		
		/** Returns the accessibility */
		public Accessibility getAccessibility() {
			return this.Accessibility;
		}
		
		/**{@inheritDoc}*/ @Override public String toString() {
			return String.format(
					"TypeData { Name: %s; Document: %s; Access: %s; Location: %s; SpecCreator: %s }",
					this.getTypeName(), this.getDocument(), this.getAccessibility(), this.getLocation(),
					this.getTSCreator()
				);
		}
	}
	
	/** CompileResult of a type with elements */
	static public class TypeWithElements {
		
		static public final TypeElement<?>[] EmptyTypeElements = new TypeElement<?>[0]; 

		private Object                            SecretID;
		private String                            TypeName;
		private Hashtable<Object, TypeElement<?>> TypeElements;

		private Object[] TypeElementKeys = null;
		
		public TypeWithElements(String pTypeName, Object pSecretID) {
			this.TypeName     = pTypeName;
			this.TypeElements = new Hashtable<Object, TypeElement<?>>();	
			this.SecretID     = pSecretID;	
		}
		
		/** Returns the type name  */
		public String getTypeName() {
			return this.TypeName;
		}

		/**
		 * Checks if the given ID match the secret ID (use it check if modification to FileCompileResult internal data 
		 *   is allowed)
		 **/
		final protected boolean verifySecretID(Object ID) {
			if(this.SecretID == null) return true;
			return UObject.equal(this .SecretID, ID); 
		}
		
		/** Returns a copy of elements in the type */
		public TypeElement<?>[] getTypeElements() {
			return (this.TypeElements.size() == 0)
					?EmptyTypeElements
					:this.TypeElements.values().toArray(EmptyTypeElements);
		}
		
		/** Returns the number of elements in the type */
		public int getTypeElementCount() {
			return this.TypeElements.size();
		}
		
		/** Returns the type element at the index I */
		public TypeElement<?> getTypeElement(int I) {
			if(this.TypeElementKeys == null)
				this.TypeElementKeys = this.TypeElements.keySet().toArray(new Object[this.TypeElements.size()]);
			return ((I < 0) || (I >= this.TypeElements.size()))?null:this.TypeElements.get(this.TypeElementKeys[I]);
		}
		
		/** Adds a type element to this type */
		final protected boolean addElement(Object pID, TypeElement<?> pTypeElement) {
			if(!this.verifySecretID(pID)) return false;
			
			if((pTypeElement == null) || this.TypeElements.containsKey(pTypeElement.getIdentity())) return false;
			this.TypeElements.put(pTypeElement.getIdentity(), pTypeElement);
			return true;
		}
		
		/** Adds a type constructor to this type */
		public boolean addConstructor(Object pID, TypeConstructor pTypeConstructor) {
			return this.addElement(pID, pTypeConstructor);
		}
		
		/** Adds a type field to this type */
		public boolean addField(Object pID, TypeField pTypeField) {
			return this.addElement(pID, pTypeField);
		}
		
		/** Adds a type method to this type */
		public boolean addMethod(Object pID, TypeMethod pTypeMethod) {
			return this.addElement(pID, pTypeMethod);
		}
	}
	
	// Elements ---------------------------------------------------------------------------------------------

	/** Abstract class for all compile result of Package and Type elements */
	static abstract public class Element<IdentityType> {
		
		private StackOwnerAppender SOAppender;
		private IdentityType       Identity;

		protected Element(IdentityType pIdentity, StackOwnerAppender pSOAppender) {
			this.Identity      = pIdentity;
			this.SOAppender    = pSOAppender;
		}
		
		/** Returns the Identity of the element */
		public IdentityType getIdentity() {
			return this.Identity;
		}
		
		/** Returns the StackOwner appender for appending this element */
		public StackOwnerAppender getStackOwnerAppender() {
			return this.SOAppender;
		}
		
		/**{@inheritDoc}*/ @Override
		public String toString() {
			return this.Identity.toString();
		}

	}
	
	/** Abstract class for all compile result of package elements */
	static abstract public class PackageElement<IdentityType> extends Element<IdentityType> {
		protected PackageElement(IdentityType pIdentity, StackOwnerAppender pSOAppender) {
			super(pIdentity, pSOAppender);
		}
	}
	
	/** Abstract class for all compile result of type elements */
	static abstract public class TypeElement<IdentityType> extends Element<IdentityType> {
		
		private boolean IsStatic;
		private boolean IsAbstract;

		protected TypeElement(IdentityType pIdentity, boolean pIsStatic, boolean pIsAbstract,
				StackOwnerAppender pSOAppender) {
			super(pIdentity, pSOAppender);
			this.IsStatic   = pIsStatic;
			this.IsAbstract = pIsAbstract;
		}
		
		/** Checks if the field is static */
		public boolean isStatic() {
			return this.IsStatic;
		}
		
		/** Checks if the field is abstract */
		public boolean isAbstract() {
			return this.IsAbstract;
		}

	}
	
	/** CompileResult for Package Variable */
	static public class PackageVariable extends PackageElement<String> {
		
		public PackageVariable(String pVarName, StackOwnerAppender pSOAppender) {
			super(pVarName, pSOAppender);
		}
		
		/** Returns the name of the variable */
		public String getName() {
			return this.getIdentity();
		}

	}

	/** CompileResult for Package Function */
	static public class PackageFunction extends PackageElement<ExecSignature> {
		
		public PackageFunction(ExecSignature pSignature, StackOwnerAppender pSOAppender) {
			super(pSignature, pSOAppender);
		}
		
		/** Returns the signature of the function */
		public ExecSignature getSignature() {
			return this.getIdentity();
		}

	}
	
	/** CompileResult for Type Field */
	static public class TypeField extends TypeElement<String> {
		
		public TypeField(String pVarName, boolean pIsStatic, boolean pIsAbstract, StackOwnerAppender pSOAppender) {
			super(pVarName, pIsStatic, pIsAbstract, pSOAppender);
		}
		
		/** Returns the name of the field */
		public String getName() {
			return this.getIdentity();
		}

	}

	/** CompileResult for Type Method */
	static public class TypeMethod extends TypeElement<ExecSignature> {
		
		public TypeMethod(ExecSignature pSignature, boolean pIsStatic, boolean pIsAbstract,
				StackOwnerAppender pSOAppender) {
			super(pSignature, pIsStatic, pIsAbstract, pSOAppender);
		}
		
		/** Returns the signature of the method */
		public ExecSignature getSignature() {
			return this.getIdentity();
		}

	}

	/** CompileResult for Type Constructor */
	static public class TypeConstructor extends TypeElement<ExecSignature> {
		
		public TypeConstructor(ExecSignature pSignature, StackOwnerAppender pSOAppender) {
			super(pSignature, false, false, pSOAppender);
		}
		
		/** Returns the interface of the constructor */
		public ExecSignature getSignature() {
			return this.getIdentity();
		}

	}

}
