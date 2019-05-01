package net.nawaman.curry.compiler;

import java.io.File;
import java.io.IOException;

import net.nawaman.compiler.Code;
import net.nawaman.compiler.CodeFeeder;
import net.nawaman.compiler.CompilationException;
import net.nawaman.regparser.Util;

/** Code feeder out of a unit */
public class CFUnit extends CodeFeeder {
	
	final UnitDescription UD;
	final String          BasePath;
	final String[]        CNames;
	
	Code[] Codes = null;
	
	public CFUnit(UnitDescription pUD) {
		if(pUD == null) throw new NullPointerException();
		this.UD = pUD;
		
		this.BasePath = this.UD.Base + this.UD.Name + File.separator;
		
		this.CNames = new String[(this.UD.Files == null) ? 0 : this.UD.Files.length];
		
		for(int i = 0; i < this.CNames.length; i++) {
			if(this.UD.Files[i] == null)                                      continue;
			if(this.UD.Files[i].isDirectory() || !this.UD.Files[i].canRead()) continue;
			
			String CName = this.UD.Files[i].getAbsolutePath();
			if(CName == null) continue;
			CName = CName.substring(BasePath.length());
			
			this.CNames[i] = CName;
		}
	}

	/**{@inherDoc}*/ @Override
	public String getBase() {
		return this.UD.Base;
	}
	/**{@inherDoc}*/ @Override
	public String getFeederName() {
		return this.UD.Name;
	}
	/**{@inherDoc}*/ @Override
	public int getCodeCount()  {
		return (this.CNames == null)?0:this.CNames.length;
	}
	
	/**{@inherDoc}*/ @Override
	public String getCodeName(int pIndex) {
		if((pIndex < 0) || (pIndex >= this.getCodeCount())) return null;
		return this.CNames[pIndex];
	}
	/**{@inherDoc}*/ @Override
	public Code getCode(int pIndex)   {
		return this.getCode(this.CNames[pIndex]);
		
	}
	/**{@inherDoc}*/ @Override
	public Code getCode(String pName)   {
		if(pName == null) return null;
		
		// Find the index of the name
		int Index = -1;
		for(int i = this.CNames.length; --i >= 0; ) {
			if(!pName.equals(this.CNames[i])) continue;
			// Found it
			Index = i;
			break;
		}
		if(Index == -1) return null;
		
		// Load the code if needed and return it 
		if(this.Codes == null) this.Codes = new Code[this.CNames.length];
		Code Code = this.Codes[Index];
		if(Code != null) return Code;
		
		try {
			Code = new Code.Simple(pName, Util.loadTextFile(this.BasePath + pName));
			this.Codes[Index] = Code;
		} catch(IOException IOE) {
			throw new CompilationException("Error reading the source file: `" + this.BasePath + pName + "`.", IOE);
		}
		return Code;
	}
}