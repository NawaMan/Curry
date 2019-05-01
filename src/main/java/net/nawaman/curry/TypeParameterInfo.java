package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.curry.TLParameter.TRParameter;

/** Information about parameter of a type */
public final class TypeParameterInfo implements Serializable {
	
	static private final long serialVersionUID = 2605955555076264950L;
	
	static public final TypeParameterInfo[] EmptyParamterTypeInfoArray =  new TypeParameterInfo[0];
	
	final private String  Name;
	final private TypeRef TypeRef;

	public TypeParameterInfo(String pName) {
		this(pName, TKJava.TAny.getTypeRef());
	}
	public TypeParameterInfo(String pName, TypeRef pTypeRef) {
		if(pName    == null) throw new NullPointerException("Name of a ParameterTypeInfo cannot be null.");
		if(pTypeRef == null)
			throw new NullPointerException("TypeRef of a ParameterTypeInfo cannot be null.");
		this.Name    = pName;
		this.TypeRef = pTypeRef;
	} 
	
	/** Returns the name of the type-parameter information */ 
	public String getName() {
		return this.Name;
	}
	/** Returns the base type reference of the parameter */ 
	public TypeRef getTypeRef() {
		return this.TypeRef;
	}
	/**{@inheritDoc}*/ @Override
	public String toString() {
		return String.format("%s:%s", this.getName(), this.getTypeRef());
	}
	
	// The collection for ParameterTypeInfo ----------------------------------------------------------------------------
	
	/** A collection of parameter type information */
    static class TypeParameterInfos implements Serializable {
		
        private static final long serialVersionUID = -7293015866011883991L;
        
        TypeParameterInfo[] PTInfos = null;
		
		TypeParameterInfos(TypeParameterInfo ... pPTInfos) {
			if(pPTInfos != null) {
				for(int i = pPTInfos.length; --i >= 0; ) {
					String Name = pPTInfos[i].getName();
					for(int j = i - 1; --j >= 0; )
						if(Name.equals(pPTInfos[j].getName()))
							throw new IllegalArgumentException("Parameter type cannot have a same name ("+Name+").");
				}
			} else pPTInfos = TypeParameterInfo.EmptyParamterTypeInfoArray;
			
			this.PTInfos = pPTInfos;
		}

		/** Returns the ParameterTypeInfo found this TypeParameterizationInfo at the index I */
		TypeParameterInfo getParameterTypeInfo(int I) {
			return ((I < 0)||(I >= this.PTInfos.length))?null:this.PTInfos[I];
		}
		/** Returns the ParameterTypeInfo found this TypeParameterizationInfo at the index I */
		TypeParameterInfo getParameterTypeInfo(String PName) {
			if(PName == null) return null;
			for(int i = this.PTInfos.length; --i >= 0; ) {
				if(!(PName.equals(this.PTInfos[i].Name))) continue;
				return this.PTInfos[i];
			}
			return null;
		}
		
		/** Returns the number of parameter type found this TypeParameterizationInfo */
		public int getParameterTypeCount() {
			return this.PTInfos.length;
		}
        /** Returns the array of Parameter types */
        public TypeRef[] getParameterTypeRefs() {
            final TypeRef[] aPTRefs = new TypeRef[this.PTInfos.length];
            for(int i = this.PTInfos.length; --i >= 0; )
                aPTRefs[i] = this.getParameterTypeRef(i);
            return aPTRefs;
        }
		/** Returns the name of the parameter type found this TypeParameterizationInfo at the index I */
		public String getParameterTypeName(int I) {
			TypeParameterInfo TPInfo = this.getParameterTypeInfo(I);
			return (TPInfo == null)?null:TPInfo.getName();
		}
		/** Returns the base type of the parameter type found this TypeParameterizationInfo at the index I */
		public TypeRef getParameterTypeRef(int I) {
			TypeParameterInfo TPInfo = this.getParameterTypeInfo(I);
			return (TPInfo == null)?null:TPInfo.getTypeRef();
		}
		/** Returns the index of the parameter with the name */
		public int getParameterIndex(String PName) {
			if(PName == null) return -1;
			
			if(this.PTInfos == null) return -1;
			for(int i = this.PTInfos.length; --i >= 0; ) {
				TypeParameterInfo TPInfo = this.PTInfos[i];
				if(TPInfo == null) continue;
				if(PName.equals(TPInfo.getName())) return i; 
			}
			return -1;
		}
		/** Returns the base type of the parameter type found this TypeParameterizationInfo at the index I */
		public TypeRef getParameterTypeRef(String PName) {
			TypeParameterInfo TPInfo = this.getParameterTypeInfo(PName);
			if(TPInfo != null) return TPInfo.getTypeRef();
			
			for(int i = this.PTInfos.length; --i >= 0; ) {
				if(!(PName.equals(this.PTInfos[i].Name))) {
					if((this.PTInfos[i].TypeRef instanceof TRParameter) &&
					   (PName.equals(((TRParameter)this.PTInfos[i].TypeRef).ParameterName))) {
						return this.PTInfos[i].TypeRef;
					}
					continue;
				}
			}
			return null;
		}
		
		/** Checks if this type has a parameter type by the given name */
		public boolean containParameterTypeRef(String PName) {
			if(PName == null) return false;
			for(int i = this.PTInfos.length; --i >= 0; ) {
				if(PName.equals(this.PTInfos[i].Name)) return true;
				
				if((this.PTInfos[i].TypeRef instanceof TRParameter) &&
				   (PName.equals(((TRParameter)this.PTInfos[i].TypeRef).ParameterName))) {
					return true;
				}
			}
			return false;
		}
	}
}