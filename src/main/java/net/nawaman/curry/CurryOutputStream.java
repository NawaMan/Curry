package net.nawaman.curry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import net.nawaman.curry.Instructions_Package.Inst_GetPackage;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLPackage.TRPackage;
import net.nawaman.javacompiler.JavaCompilerObjectOutputStream;

public class CurryOutputStream extends JavaCompilerObjectOutputStream {
	
	final Engine         Engine;
	final Vector<String> PackageNames        = new Vector<String>();
	final Vector<String> EngineExtNames      = new Vector<String>();
	final Vector<String> RequirePackageNames = new Vector<String>();
	
	/** Creates a new CurryOutputStream */
	static public CurryOutputStream newCOS(Engine pEngine, OutputStream pOS) throws IOException {
		return new CurryOutputStream(pEngine, new ByteArrayOutputStream(), pOS);
	}
	
	/** Constructs an CurryOutputStream */
	protected CurryOutputStream(Engine pEngine, ByteArrayOutputStream pBAOS, OutputStream pOS) throws IOException {
		super(pBAOS, pOS);
		
		if(pEngine == null) throw new NullPointerException();
		this.Engine = pEngine;
	}
	
	/** Returns the engine of this Package */
	public Engine getEngine() {
		return this.Engine;
	}
    
	// TODOLATER - Later we can use this as a way to optimize the code by reducing some of the computation in advance
	// TODOLATER - Detect EngineExtension for TypeKind, TypeLoader and DataHolderFactory
			
	/** Expression will use this method to notify POS that it is being written */
	void notifyPackageWritten(Package Pkg) {
		String PName = Pkg.getName();
		
		if(this.PackageNames.contains(PName))
			throw new CurryError("Internal Error: A package with the same name is already added: " + PName);
		
		// Add the required package to the list if not already there or not in the local package 
		if(!this.PackageNames.contains(PName))
			this.PackageNames.add(PName);
	}
			
	/** Expression will use this method to notify POS that it is being written */
	void notifyExpressionWritten(Expression Expr) {
		if(Expr.isData()) return;
		
		Engine E = this.getEngine();
		Instruction Inst = Expr.getInstruction(E);
		if(Inst == null) throw new CurryError("Instruction is not found in this engine: " + Expr.getInstructionNameHash());
		
		// Registed the required Extension name
		EngineExtension EExt = E.getOwnerOfInstruction(Inst.getNameHash());
		if((EExt != null) && !this.EngineExtNames.contains(EExt.getExtName()))
			this.EngineExtNames.add(EExt.getExtName()); 
		
		// Use a package
		if(Inst.getName().equals(Inst_GetPackage.Name)) {
			Object O = Expr.getParam(0);
			// Add the required package to the list if not already there or not in the local package 
			if((O instanceof String) && !this.PackageNames.contains(O) && !this.RequirePackageNames.contains(O))
				this.RequirePackageNames.add((String)O);
		}
	}
	/** TypeRef will use this method to notify POS that it is being written */
	void notifyTypeRefWritten(TypeRef TRef) {
		MType MT = this.getEngine().getTypeManager();
		
		if(TRef instanceof TRPackage) {
			String PName = ((TRPackage)TRef).getPackageName();
			// Add the required package to the list if not already there or not in the local package 
			if((PName != null) && !this.PackageNames.contains(PName) && !this.RequirePackageNames.contains(PName))
				this.RequirePackageNames.add(PName);
		}

		// Save the Extension from the TypeLoader
		try {
			TypeLoader TL = MT.getTypeLoader(TRef.getRefKindName());
			if(TL == null) throw new CurryError("TypeLoader is not found in this engine: " + TRef.getRefKindName());
			
			EngineExtension EExt = MT.getOwnerOfTypeLoader(TL.getKindName());
			if((EExt != null) && !this.EngineExtNames.contains(EExt.getExtName()))
				this.EngineExtNames.add(EExt.getExtName());
		} catch(Exception E) {}
		
		// Save the Extension from the TypeKind
		try {
			MT.ensureTypeValidated(null, TRef, (TRef instanceof TRBasedOnType) ? ((TRBasedOnType)TRef).getBaseTypeRef() : null);
			
			Type     T  = TRef.getTheType();
			TypeKind TK = T.getTypeKind();
			if(TK == null)
				throw new CurryError("TypeKind is not found in this engine: " + TRef.getRefKindName());
			
			EngineExtension EExt = MT.getOwnerOfTypeKind(TK.getKindName());
			if((EExt != null) && !this.EngineExtNames.contains(EExt.getExtName()))
				this.EngineExtNames.add(EExt.getExtName());
		} catch(Exception E) {}
	}
	
	public DependencyInfo getPackagesDependencyInfo() {
		return new DependencyInfo(
				this.PackageNames,
				this.EngineExtNames,
				this.RequirePackageNames);
	}
}
