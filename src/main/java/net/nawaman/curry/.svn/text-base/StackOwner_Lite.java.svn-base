package net.nawaman.curry;

import net.nawaman.curry.util.DataHolder;

/** A StackOwner that contain the attribute but not all the element information */
abstract public class StackOwner_Lite extends StackOwner {
	
	protected StackOwner_Lite() {}
	
	// Attribute Info --------------------------------------------------------------------
	
	/** Returns the number of attribute info this StackOwner contains */
	abstract protected int getAttrInfoCount();
	
	/** Returns the attribute info at the index */
	abstract protected AttributeInfo getAttrInfoAt(int pIndex);
	
	/** Returns the maximum value of DataHolder index (or the maximum number of Direct AttrInfo) */
	abstract protected int getMaxDHIndex();
	
	// Attributes ------------------------------------------------------------------------

	/** Attributes **/
	transient DataHolder[] Attrs = null;

	/** Initialize all the elements */
	protected void initializeAttributes(Context pContext, Engine pEngine) {
		this.initializeAllDH(pContext, pEngine);
	}
	
	/**{@inheritDoc}*/ @Override
	protected DataHolder getDHAt(Context pContext, DataHolder.AccessKind DHAK, AttributeInfo.AIDirect AI) {
		if(AI == null)      return null;
		if(AI.isAbstract()) return null;
		if((AI.DHIndex < 0) || (AI.DHIndex >= this.getMaxDHIndex())) return null;
		this.ensureDHSpace(pContext);
		DataHolder DH = this.Attrs[AI.DHIndex];
		if(DH == null) { this.initializeDH(pContext, null, DHAK, AI); DH = this.Attrs[AI.DHIndex]; }
		if(DH == null) return null;	// Not Found (กันเหนียว :p)
		if(!this.isAttrAllowed(pContext, AI, DHAK))
			throw new CurryError("Invalid access to the package variable ("+AI.getName()+").", pContext); 
		return DH;
	}

	// TODO - This is a Hack to allow Pattern to access to its port
	/** Get the DH at the index */
	final protected DataHolder getDHAt_RAW(int Index) {
		if((Index < 0) || (Index >= this.getMaxDHIndex())) return null;
		if(Index >= this.Attrs.length)                     return null;
		return this.Attrs[Index];
	}
	
	/** Ensure that there is enough space for Attributes */
	final protected void ensureDHSpace(Context pContext) {
		this.doEnsureDHSpace(pContext);
	}
	
	/** Ensure that there is enough space for Attributes */
	protected void doEnsureDHSpace(Context pContext) {
		if(this.Attrs == null) {
			// Ensure that the attribute array is created
			this.Attrs = new DataHolder[this.getMaxDHIndex()];
			
		} else if(this.Attrs.length < this.getMaxDHIndex()) {
			DataHolder[] NewAttrs = new DataHolder[this.getMaxDHIndex()];
			System.arraycopy(this.Attrs, 0, NewAttrs, 0, this.Attrs.length);
			this.Attrs = NewAttrs;
		}
	}
	
	/** Initialize all the DataHolder of this StackOwner */
	protected void initializeDH(Context pContext, Engine pEngine, DataHolder.AccessKind DHAK, AttributeInfo.AIDirect AI) {
		if(AI == null) return;
		this.ensureDHSpace(pContext);
		DataHolder DH = this.Attrs[AI.DHIndex];
		if(DH != null) return;
		// Ask Engine to create a DataHolder
		Exception Cause = null;
		if((pEngine == null) && (pContext != null)) pEngine = pContext.getEngine();
		if(pEngine == null) Cause = new NullPointerException("Null Engine.");
		else {
			if(pContext == null) pContext = new Context.ContextStackOwner(pEngine.newRootContext(), "Initializing", false, null, this, null);
			
			try                { DH = pEngine.getDataHolderManager().newDH(pContext, AI.getDHInfo()); }
			catch(Exception T) { Cause = T;                                                           }
		}
		
		if((DH == null) || (Cause != null))
			throw new CurryError("There is a problem initializing the attribute ("+AI.Name+").", pContext, Cause);
		this.Attrs[AI.DHIndex] = DH;
		// Enforce 'isNotNull'
		if(this.isEnforceNotNull() && AI.isNotNull() && (pContext.getEngine().getDataHolderManager().getDHData(pContext, AI.Name, DH) == null))
			this.throwAttributeMustNotBeNull(pContext, DHAK, AI.getOwnerAsType(), AI.Name);
	}
	/** Initialize all the DataHolder of this StackOwner */
	final protected void initializeAllDH(Context pContext, Engine pEngine) {
		this.ensureDHSpace(pContext);
		for(int i = 0; i < this.getAttrInfoCount(); i++) {
			if(!(this.getAttrInfoAt(i) instanceof AttributeInfo.AIDirect)) continue;
			AttributeInfo.AIDirect AID = ((AttributeInfo.AIDirect)this.getAttrInfoAt(i));
			int DHIndex = AID.DHIndex;
			DataHolder DH = this.Attrs[DHIndex];
			if(DH != null) continue;
			this.initializeDH(pContext, pEngine, DataHolder.AccessKind.Get, AID);
		}
	}
	
	// Others ------------------------------------------------------------------
      	
	/**{@inheritDoc}*/ @Override
	protected boolean toEnforceNotNull(Context pContext) {
		if(!this.isEnforceNotNull()) return true;
		this.ensureDHSpace(pContext);
		for(int i = 0; i < this.getAttrInfoCount(); i++) {
			if(!(this.getAttrInfoAt(i) instanceof AttributeInfo.AIDirect)) continue;
			AttributeInfo.AIDirect AID = ((AttributeInfo.AIDirect)this.getAttrInfoAt(i));
			int DHIndex = AID.DHIndex;
			DataHolder DH = this.Attrs[DHIndex];
			if(DH == null) this.initializeDH(pContext, null, DataHolder.AccessKind.Get, AID);
			else {
				// Just Getting the value will already check is the null is enforce.
				this.getAttrData(pContext, null, null, AID.getName(), null); 
			}
		}
		return true;
	}
	
	// Utilities ---------------------------------------------------------------
	
	/** Returns the data-holder by the index */
	final protected DataHolder getDHByIndex(Context pContext, DataHolder.AccessKind DHAK, int Index) {
		AttributeInfo AI = this.getAttrInfoAt(Index);
		if((AI == null) || !AI.getRKind().isDirect()) return null;
		return this.getDHAt(pContext, DHAK, (AttributeInfo.AIDirect)AI);
	}
}
