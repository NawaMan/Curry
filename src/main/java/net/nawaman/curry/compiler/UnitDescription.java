package net.nawaman.curry.compiler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import net.nawaman.curry.DependencyInfo;
import net.nawaman.curry.MUnit;
import net.nawaman.javacompiler.JavaCompilerObjectInputStream;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.ParseResult;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.script.Executable;
import net.nawaman.script.Function;
import net.nawaman.script.ScriptEngine;
import net.nawaman.script.ScriptManager;
import net.nawaman.script.Tools;
import net.nawaman.util.UString;

public class UnitDescription {
	
	static public final String UNIT_DESCRIPTION_FILE_NAME = "unit.dsc";
	
	static public final UnitDescription[] EmptyUnitDescriptionArray = new UnitDescription[0];
	
	/** Data read from UnitDescription file */
	static public final class UnitDescriptionFileData {
		final public String  Code;
		final public boolean IsUpToDate;
		final public String  BASE64;
		final public String  CodeHash;
		
		final String[] Patterns;
		
		UnitDescriptionFileData(Object[] CompileResult) {
			this.Code       = (String)            CompileResult[0] ;
			this.IsUpToDate = Boolean.TRUE.equals(CompileResult[2]);
			this.BASE64     = (String)            CompileResult[3] ;
			this.CodeHash   = (String)            CompileResult[4] ;

			this.Patterns   = (CompileResult[1] == null) ? UString.EmptyStringArray : ((String[])CompileResult[1]).clone();
		}
		
		/** Returns the number of File Selection patterns */
		public int getFileSelectionPatternCount() {
			return this.Patterns.length;
		}
		/** Returns the file selection pattern at the index */
		public String getFileSelectionPattern(int I) {
			if((I < 0) || (I >= this.Patterns.length)) return null;
			return this.Patterns[I];
		}
		/** Returns the file selection patterns as an array*/
		public String[] getFileSelectionPatterns() {
			if(this.Patterns.length == 0) return this.Patterns;
			return this.Patterns.clone();
		}
	}
	
	/** Reads and returns unformation of a UnitDescription */
	static public UnitDescriptionFileData GetUnitDescriptionFileData(File UDFile) {
		if((UDFile == null) || !UDFile.isFile() || !UDFile.canRead()) return null;
		
		// Load the file and parse it
		String Text = null;
		try { Text = UString.loadTextFile(UDFile); } catch (IOException E) {
			System.err.println("Problem while reading a unit description file ("+UDFile+"): " + E);
			return null;
		}
		// Load the parser
		PTypePackage TPackage = PTypePackage.Use("UnitDesc");
		PType        PT       = TPackage.getType("UnitDescFile");
		ParseResult  Result   = PT.parse(Text);
		// Checks for parsing error
		if(!Result.ensureNoError(TPackage, new CompilationContext.Simple())) return null;
		// Compile it - [] { Code, Patterns, IsUpToDate, BASE64, CodeHash }
		// NOTE: Use array here to reduce un-nessessary dependency of the UnitDescFile parser to curry. 
		Object[] ExtractedData = (Object[])PT.compile(Result);
		if((ExtractedData == null) || (ExtractedData.length != 5)) return null;
		
		return new UnitDescriptionFileData(ExtractedData);
	}
	
	/** Returns the RegParsers of the given patterns */
	static public RegParser[] GetPatternRegParser(UnitDescriptionFileData UDFD) {
		if(UDFD == null) return null;
		return GetPatternRegParser(UDFD.Patterns);
	}
	
	/** Returns the RegParsers of the given patterns */
	static public RegParser[] GetPatternRegParser(String[] Patterns) {
		if(Patterns == null) return RegParser.EmptyRegParserArray;
		
		Vector<RegParser> Parsers = new Vector<RegParser>();
		for(int p = 0; p < Patterns.length; p++) {
			String Pattern = Patterns[p];
			if((Pattern == null) || (Pattern.length() == 0)) continue;
			
			RegParser Parser = null;
			try {
				Parser = RegParser.newRegParser(Patterns[p]);
			} catch (Exception PE) {}
			
			if(Parser == null) {
				System.err.println("Unable to compile the pattern (It will be skipped): " + Patterns[p]);
				continue;
			}
			
			Parsers.add(Parser);
		}
		
		return (Parsers.size() == 0)
		           ? RegParser.EmptyRegParserArray
		           : Parsers.toArray(new RegParser[Parsers.size()]);
	}
	
	/** Load a UnitDescription */
	static public UnitDescription Load(File UnitFolder) {
		File UDFile = GetUnitDescFileFromUnitFolder(UnitFolder);
		if(UDFile == null) return null;
		
		try {
			UnitDescriptionFileData UDFD = GetUnitDescriptionFileData(UDFile);
			if(UDFD == null) return null;
			
			String Base = GetFileAbsolutePath(UnitFolder.getParentFile().getAbsolutePath()) + File.separator;
			String Name = UnitFolder.getName();
			
			// Check if up-to-date -------------------------------------------------------------------------------------
			// 0. It must be compiled
			boolean IsUpTodate = UDFD.IsUpToDate;
			if(IsUpTodate) {
				File   UnitPath = new File(Base + Name);
				// 1. <unti>.cuf must exist
				// 2. unit.dsc & <unitpath> must have the same modified time.
				File Unit  = new File(Base + Name + "." + MUnit.UNIT_FILE_EXTENSION);
				File UFile = new File(Base + Name + File.separator + UNIT_DESCRIPTION_FILE_NAME);
				IsUpTodate = (Unit.exists() && UFile.exists() && UFile.canRead() && (UFile.lastModified() == UnitPath.lastModified()));
			}

			// Get the DependencyInfo and the hash of the code
			String         BASE64   = (String)UDFD.BASE64;
			String         CodeHash = (String)UDFD.CodeHash;
			DependencyInfo DInfo    = null;
			RegParser[]    RParsers = RegParser.EmptyRegParserArray;
			
			if(IsUpTodate && (BASE64 != null)) {
				
				Executable Exec = null;
				
				ByteArrayInputStream BAIS  = null;
				ObjectInputStream    OIS   = null;
				ByteArrayInputStream EBAIS = null;
				ObjectInputStream    EOIS  = null;
				
				try {
					BAIS = new ByteArrayInputStream(Tools.Base64Decode(BASE64));
					OIS  = new ObjectInputStream(BAIS);
					
					String EngineName  = OIS.readUTF();
					String EngineParam = OIS.readUTF();
					
					byte[] Bs = (byte[])OIS.readObject();
					EBAIS = new ByteArrayInputStream(Bs);
					
					ScriptEngine SE = ScriptManager.GetEngineFromCode(String.format("// @%s(%s):", EngineName, EngineParam));
					EOIS = SE.newExecutableObjectInputStream(EBAIS);
					if(EOIS == null) EOIS = JavaCompilerObjectInputStream.NewJavaCompilerObjectInputStream(EBAIS);
					
					// Extract the object
					Exec            = (Executable)EOIS.readObject();
					String TextHash = (String)    EOIS.readObject();

					// Check the hash using the hash value inside the compiled code
					IsUpTodate = TextHash.trim().equals(CodeHash);	
				} catch (Exception E) {
					IsUpTodate = false;
				} finally {
					// Close the stream
					if(EBAIS != null) try { EBAIS.close(); } catch (Exception E) { }
					if(EOIS  != null) try { EOIS .close(); } catch (Exception E) { }
					if(OIS   != null) try { OIS  .close(); } catch (Exception E) { }
					if(BAIS  != null) try { BAIS .close(); } catch (Exception E) { }
				}
				
				// Get the function
				Object O   = null;
				IsUpTodate = (Exec instanceof Function);
				if(IsUpTodate) {
					try {
						Object O0 = null;
						Object O1 = null;
						IsUpTodate = ((O = ((Function)Exec).run()) instanceof Object[]);
						IsUpTodate = IsUpTodate && ((O0 = ((Object[])O)[0]) instanceof DependencyInfo);
						IsUpTodate = IsUpTodate && ((O1 = ((Object[])O)[1]) instanceof String[]);
						
						// Get the dependency info and pattern form the file
						if(IsUpTodate) {
							DInfo    = (DependencyInfo)O0;
							RParsers = GetPatternRegParser((String[])O1);
						}
					} catch (Exception E) {}
				}
			}

			// If not update, gets from the extracted data
			if(!IsUpTodate) RParsers = GetPatternRegParser(UDFD);
			
			// Create the Description
			return new UnitDescription(Base, Name, DInfo, IsUpTodate, RParsers);
		} catch (Exception E) {
			System.err.println("Problem while reading a unit description file ("+UDFile+"): " + E);
			throw (E instanceof RuntimeException) ? (RuntimeException)E : new RuntimeException(E);
			//return null;
		}
	}

	/** Scan the given Folder for UnitFolders */
	static public UnitDescription[] Discover(File Folder) {
		if((Folder == null) || !Folder.isDirectory() || !Folder.canRead())
			return UnitDescription.EmptyUnitDescriptionArray;
		
		File[] Files = Folder.listFiles();
		if((Files == null) || (Files.length == 0))
			return UnitDescription.EmptyUnitDescriptionArray;

		Vector<UnitDescription> UDs = new Vector<UnitDescription>();
		for(int i = 0; i < Files.length; i++) {
			File File = Files[i];
			if(File.isFile()) continue;
			
			UnitDescription UD = Load(File);
			if(UD == null) continue;
			
			UDs.add(UD);
		}
			
		// Returns the UnitDesciption
		return UDs.toArray(new UnitDescription[UDs.size()]);
	}
	
	final public String         Base;
	final public String         Name;
	final public DependencyInfo DInfo;
	
	final   RegParser[] Filters;
	private Boolean     IsUpTodate;
	
	UnitDescription(String pBase, String pName, DependencyInfo pDInfo, boolean pIsUpToDate, RegParser[] pFilters) {
		this.Base       = GetFileAbsolutePath((new File(pBase)).getAbsolutePath()) + File.separator;
		this.Name       = pName;
		this.IsUpTodate = pIsUpToDate ? null : pIsUpToDate;
		this.DInfo      = pDInfo;
		this.Filters    = pFilters.clone();
	}
	
	// The result --------------------------------------------------------------------------------------------------
	
	String[] Packages = null;
	File[]   Files    = null;
	
	/** Returns the number of found packages in this unit */
	public String[] getPackages() {
		if(this.Packages == null) this.process();
		return this.Packages.clone();
	}
	
	/** Returns all the code files to be compiled */
	public File[] getFiles() {
		if(this.Files == null) this.process();
		return this.Files.clone();
	}
	
	/** Checks if this UnitDescription is up-to-date */
	public boolean isUpToDate() {
		if(this.IsUpTodate == null) {
			this.process();
			if(this.IsUpTodate == null) this.IsUpTodate = true;
		}
		return this.IsUpTodate;
	}
	
	/** Change this UnitDecription to be not up-to-date. */
	void toNotUpToDate() {
		this.IsUpTodate = false;
	}
	
	/**{@inheritDoc}*/ @Override
	public String toString() {
		return "UNIT: " + this.Base + "::" +this.Name;
	}
	
	// Utility methods ---------------------------------------------------------------------------------------------
	
	/** Checks and add a file into the list - using the filter given in the unit.dsc file */
	void addFile(String FN, File UnitFolder, File Folder, File File, HashMap<String, File> AllPackages,
			HashMap<String, File> AllFiles) {
		
		for(int i = 0; i < this.Filters.length; i++) {
			RegParser Filter = this.Filters[i];
			if(Filter           == null) continue;
			if(Filter.match(FN) == null) continue;
			
			int    Idx = FN.length() - File.getName().length() - 1;
			String DN  = FN.substring(0, (Idx < 0) ? 0 : Idx);
			
			AllFiles   .put(FN, File);
			AllPackages.put(DN.replaceAll("/", "~>"), Folder);
			if(UnitFolder.lastModified() < File.lastModified()) this.IsUpTodate = false;
			break;
		}
	}
	
	/** Checks if the given folder contains a source fole (so it will become a package) */
	void discover(String BasePath, File UnitFolder, File Folder, HashMap<String, File> AllPackages,
			HashMap<String, File> AllFiles) {
		File[] Fs = Folder.listFiles();
		
		for(int i = 0; i < Fs.length; i++) {
			File File = Fs[i];
			
			if(File.isFile()) {
				String FN = File.getAbsolutePath().substring(BasePath.length() + 1);
				this.addFile(FN, UnitFolder, Folder, File, AllPackages, AllFiles);
				continue;
			}

			if(UnitFolder.lastModified() < File.lastModified()) this.IsUpTodate = false;
			this.discover(BasePath, UnitFolder, File, AllPackages, AllFiles);
		}
	}
	
	/** Starts process the discovery */
	void process() {			
		File                  UnitPath    = new File(this.Base + this.Name);
		HashMap<String, File> AllFiles    = new HashMap<String, File>();
		HashMap<String, File> AllPackages = new HashMap<String, File>();
		
		// Discovering ---------------------------------------------------------------------------------------------
		this.discover(UnitPath.getAbsolutePath(), UnitPath, UnitPath, AllPackages, AllFiles);
		
		Vector<String> Ps = new Vector<String>(AllPackages.keySet());
		Vector<File>   Fs = new Vector<File>  (AllFiles   .values());
		Collections.sort(Ps);
		Collections.sort(Fs);
		
		this.Packages = Ps.toArray(new String[Ps.size()]);
		this.Files    = Fs.toArray(new File[  Fs.size()]);
	}
	
	/** Returns the UnitDescriptionFile form the Unit folder or null if the folder is not a unit folder*/
	static public File GetUnitDescFileFromUnitFolder(File Folder) {
		if((Folder == null) || Folder.isFile() || !Folder.canRead()) return null;
		
		File File = new File(Folder.getAbsolutePath() + java.io.File.separator + UNIT_DESCRIPTION_FILE_NAME);
		if((File.exists() && File.canRead())) return File;
		return null;
	}
	
	/** Returns the file abosolute path (eliminate '/./' and a like) */
	static String GetFileAbsolutePath(String FN) {
		return FN.replaceFirst("/[^/]*/\\.\\./", "/").replaceFirst("/\\./", "/").replaceFirst("//", "/");
	}
}
