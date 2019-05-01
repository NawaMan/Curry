package net.nawaman.curry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import net.nawaman.compiler.Code;
import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.compiler.UnitDescription;
import net.nawaman.curry.compiler.UnitDescription.UnitDescriptionFileData;
import net.nawaman.script.Executable;
import net.nawaman.script.Function;
import net.nawaman.script.ScriptEngine;
import net.nawaman.script.ScriptManager;
import net.nawaman.script.Signature;
import net.nawaman.script.Tools;
import net.nawaman.script.java.JavaEngine;
import net.nawaman.script.java.JavaFunction;
import net.nawaman.util.UString;

public class UnitBuilders {
	
	/** UnitBuilder Memory */
	static public final class UBMemory extends UnitBuilder {
		/** Construct a UnitBuilder Memory */
		public UBMemory(Engine pEngine, String pName, Object pSecretID) {
			super(pEngine, pName, pSecretID, null);
		}
		/** Construct a UnitBuilder Memory */
		public UBMemory(Engine pEngine, String pName, Object pSecretID, CodeFeeder pCodeFeeder) {
			super(pEngine, pName, pSecretID, pCodeFeeder);
		}
		// Save Unit -------------------------------------------------------------------------
		/** Save this being built unit */
		@Override protected Exception saveThis(Package[] Ps) {
			// Save to reset all typeref and others
			try { this.savePackages(Ps); } catch (IOException IOE) {}
			return null;
		}
	}
	
	/** UnitBuilder File */
	static public final class UBFile extends UnitBuilder {
		
		static public String getFileName(String pName) {
			return pName + "." + MUnit.UNIT_FILE_EXTENSION;
		}
		

		public UBFile(Engine pEngine, String pName, Object pSecretID) {
			this(pEngine, null, pName, pSecretID, null);
		}
		/** Construct a UnitBuilder Memory */
		public UBFile(Engine pEngine, String pName, Object pSecretID, CodeFeeder pCodeFeeder) {
			this(pEngine, null, pName, pSecretID, pCodeFeeder);
		}
		public UBFile(Engine pEngine, String BasePath, String pName, Object pSecretID) {
			this(pEngine, BasePath, pName, pSecretID, null);
		}
		/** Construct a UnitBuilder Memory */
		public UBFile(Engine pEngine, String BasePath, String pName, Object pSecretID, CodeFeeder pCodeFeeder) {
			super(pEngine, pName, pSecretID, pCodeFeeder);
			BasePath = (((BasePath == null) || (BasePath.length() == 0)) ? "" : BasePath + File.separator);
			this.FileName = BasePath + getFileName(pName);
		}
			
		       String    FileName = null;
		public String getFileName() {
			return this.FileName;
		}
		// Save directly to the UnitFactory
		@Override
		protected Exception saveThis(Package[] Ps) {

			String[] CodeNames = null;
			Code[]   Codes     = null;
			// Save the code
			if((this.CodeFeeder != null) && (this.CodeFeeder.getCodeCount() != 0)) {
				CodeNames = new String[this.CodeFeeder.getCodeCount()];
				Codes     = new Code[  this.CodeFeeder.getCodeCount()];
				for(int i = this.CodeFeeder.getCodeCount(); --i >= 0; ) {
					CodeNames[i] = this.CodeFeeder.getCodeName(i);
					Codes[i]     = new Code.Simple(CodeNames[i], this.CodeFeeder.getCode(CodeNames[i]).getSource());
				}
			}
			
			// Save it to the file
			ObjectOutputStream OOS   = null;
			DependencyInfo     DInfo = null;
			try {
				OOS = new ObjectOutputStream(new FileOutputStream(this.FileName));
				OOS.writeObject(MUnit.UNIT_FILE_SIGNATURE);
				OOS.writeObject(this.UM.TheEngine.getEngineSpec().getSignature());
				
				byte[] Bytes = this.savePackages(Ps);
				DInfo = UnitFactory.LoadPackagesDependencyInfo(Bytes);
				
				OOS.writeObject(Bytes);
				OOS.writeObject(CodeNames);
				OOS.writeObject(Codes);
			} catch(Exception E) {
				E.printStackTrace();
				return new CurryError("Error occurs while trying to save a unit.", E);
			} finally {
				try { if(OOS != null) OOS.close(); }
				catch(Exception E) { return new CurryError("Error occurs while trying to save a unit.", E);  }
			}
			
			// Save the DInfo to the Unit desciption file if exit
			String UDFName = this.getFileName();
			int    Index   = UDFName.lastIndexOf('/') + 1;
			UDFName = UDFName.substring(0, Index) + this.Name + File.separator + UnitDescription.UNIT_DESCRIPTION_FILE_NAME;
			File UDFile = new File(UDFName);
			if(UDFile.exists() && UDFile.canWrite()) {
				final DependencyInfo DI = DInfo;
				try {
					// Update the file
					UnitDescriptionFileData UDFD = UnitDescription.GetUnitDescriptionFileData(UDFile);
					
					String     Code = UDFD.Code;
					Executable Exec = new GetDependencyInfo(DI, UDFD.getFileSelectionPatterns());
					// Delete the file
					UDFile.delete();
					// Merge and save
					String TextToSave = Tools.MergeCodeAndCompiledExecutable(Code, Exec);
					UString.saveTextToFile(UDFile, TextToSave);
				} catch (IOException E) {
					System.err.println("Fail to save DependencyInfo into the Unit description file: " + UDFName);
				}
			}
			
			return null;
		}
		
		/** A function to returns the dependency information */
		static class GetDependencyInfo extends JavaFunction {
		    
            private static final long serialVersionUID = -8948351627766245620L;
            
            static Signature SIGNATURE = new Signature.Simple("getUnitDescriptionInfo", Object[].class);
			
			static class GDIBody implements Function.Simple.Body, Serializable {
			    
                private static final long serialVersionUID = -4426743944376901566L;
                
                final DependencyInfo DInfo;
				final String[]       Patterns;
				GDIBody(DependencyInfo pDInfo, String[] pPatterns) {
					this.DInfo    = pDInfo;
					this.Patterns = ((pPatterns == null) || (pPatterns.length == 0)) ? pPatterns : pPatterns.clone();
				}
				public @Override String       getEngineName() { return JavaEngine.Name; }
				public @Override ScriptEngine getEngine()     { return ScriptManager.Instance.getDefaultEngineOf(this.getEngineName()); }
				public @Override String       getCode()       { return null;      }
				public @Override Signature    getSignature()  { return SIGNATURE; }
				public @Override Object       run(Function.Simple pFunction, Object[] pParams) {
					String[] Ps = ((this.Patterns == null) || (this.Patterns.length == 0)) ? this.Patterns : this.Patterns.clone();
					return new Object[] { this.DInfo, Ps };
				}
			}
			
			public GetDependencyInfo(final DependencyInfo DInfo, String[] pPatterns) {
				super(UString.EmptyStringArray, new GDIBody(DInfo, pPatterns), null);
			}
		} 
	}
	
	/** UnitBuilder Folder * /
	static public final class UBFolder extends UnitBuilder {
		public UBFolder(Engine pEngine, String pBasePath, String pFolderName) {
			super(pEngine, pFolderName);
			this.BasePath   = pBasePath;
			this.FolderName = pFolderName;
		}
		       String    BasePath = null;
		public String getBasePath() { return this.BasePath; }
		       String    FolderName = null;
		public String getFolderName() { return this.FolderName; }
		// Save directly to the UnitFactory
		@Override protected Exception saveThis(Package[] Ps) {	
			
			// Create the PackageArrayASpec.SubArtifactSpecs
			for(Package P : Ps) {
				if(P == null) continue;
				
				// Save it to the file
				FileOutputStream FOS = null;
				try {
					String DName = ((this.BasePath==null)?"":(this.BasePath + java.io.File.separator)) + this.FolderName;
					String FName = DName + java.io.File.separator + P.getName() + "." + UnitManager.PackageFileExtension;
					
					File F = null;
					F = new File(DName); F.mkdirs();
					F = new File(FName); F.createNewFile();
					
					FOS = new FileOutputStream(F);
					ObjectOutputStream OOS = new UUnitBuilder.PackageOutputStream(this.UM.getEngine(), FOS);
					OOS.writeObject(UnitManager.PackageFileSignature);
					OOS.writeObject(this.UM.Engine.getEngineSpec().getSignature());
					OOS.writeObject(P);
				} catch(Exception E) {
					return new CurryError("An error occurs while trying to save a package: " + P.getName() +".", E);
				} finally {
					try { if(FOS != null) FOS.close(); }
					catch(Exception E) {
						return new CurryError("An error occurs while trying to save a unit: "+this.getName()+".", E);
					}
				}
			}
			
			if((this.CodeFeeder != null) || (this.CodeFeeder.getCodeCount() != 0)) {
				for(int i = this.CodeFeeder.getCodeCount(); --i >= 0; ) {
					String CodeName = this.CodeFeeder.getCodeName(i);
					Code   Code     = new Code(CodeName, this.CodeFeeder.getCode(CodeName));

					// Save it to the file
					FileOutputStream FOS = null;
					try {
						String DName = ((this.BasePath==null)?"":(this.BasePath + java.io.File.separator)) + this.FolderName;
						String FName = DName + java.io.File.separator + CodeName + "." + UnitManager.CodeFileExtension;
						
						File F = null;
						F = new File(DName); F.mkdirs();
						F = new File(FName); F.createNewFile();
						
						FOS = new FileOutputStream(F);
						ObjectOutputStream OOS = new UnitBuilder.PackageOutputStream(this.UM.getEngine(), FOS);
						OOS.writeObject(CodeName);
						OOS.writeObject(Code);
					} catch(Exception E) {
						return new CurryError("An error occurs while trying to save unit code: " + CodeName +".", E);
					} finally {
						try { if(FOS != null) FOS.close(); }
						catch(Exception E) {
							return new CurryError("An error occurs while trying to save a unit: "+this.getName()+".", E);
						}
					}
				}
			}
			
			return null;
		}	
	}*/
}