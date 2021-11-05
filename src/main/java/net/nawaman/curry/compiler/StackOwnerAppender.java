package net.nawaman.curry.compiler;

import java.io.Serializable;

import net.nawaman.curry.*;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.result.ParseResult;

public interface StackOwnerAppender {
	
	public void append(CompileProduct $CProduct, StackOwnerBuilder SOB);
	
	// Implements ------------------------------------------------------------------------------------------------------
	
	static abstract public class AttrAppender implements StackOwnerAppender {
		public boolean isRepeat(CompileProduct $CProduct, StackOwnerBuilder SOB, String pAName, ParseResult $Result) {
			if(SOB.isAttrExist(pAName)) {
				$CProduct.reportError(
					String.format("The attribute `{%s}.{%s}` is already exist", SOB, pAName),
					null, $Result.startPosition());
				return true;
			}
			return false;
		}
		public boolean canBeStatic(CompileProduct $CProduct, StackOwnerBuilder SOB, String pAName, ParseResult $Result) {
			if(!(SOB instanceof TypeBuilder)) {
				$CProduct.reportError(
						String.format("Only type attribute can be static: `{%s}.{%s}` is not a type attribute", SOB, pAName),
						null, $Result.startPosition());
				return false;
			}
			return true;
		}
	}
	
	static abstract public class OperAppender implements StackOwnerAppender {
		public boolean isRepeat(CompileProduct $CProduct, StackOwnerBuilder SOB, ExecSignature pSignature,
				ParseResult $Result) {
			if(SOB.isOperExist(pSignature)) {
				$CProduct.reportError(
					String.format("The operation `{%s}.{%s}` is already exist", SOB, pSignature),
					null, $Result.startPosition());
				return true;
			}
			return false;
		}
		public boolean canBeStatic(CompileProduct $CProduct, StackOwnerBuilder SOB, ExecSignature pSignature,
				ParseResult $Result) {
			if(!(SOB instanceof TypeBuilder)) {
				$CProduct.reportError(
						String.format("Only type operation can be static: `{%s}.{%s}` is not a type operation", SOB, pSignature),
						null, $Result.startPosition());
				return false;
			}
			return true;
		}
	}
	
	static public class Util {
		
		static MoreData merge(MoreData pMoreData, Documentation pDoc) {
			MoreData MD = pMoreData;
			if(pDoc != null) {
				MoreData NewMD = new MoreData(Documentation.MIName_Documentation, pDoc);
				MD = (MD == null)?NewMD:MoreData.combineMoreData(MD, NewMD);
			}
			return MD;
		}
		
		// Constructor -------------------------------------------------------------------------------------------------
		
		static public StackOwnerAppender newConstructor(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility pPAccess, final ExecSignature pSignature, final MoreData pMoreData,
				final Object pTempData, final Documentation pDoc) {
			
			return new StackOwnerAppender() {
				public boolean isRepeat(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(((TypeBuilder)SOB).isConstructorExist(pSignature)) {
						$CProduct.reportError(
							String.format("The constructor `{%s}.{%s}` is already exist", SOB, pSignature),
							null, $Result.startPosition());
						return true;
					}
					return false;
				}
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(!(SOB instanceof TypeBuilder)) {
						$CProduct.reportError(
								String.format("Only type operation can be static: `{%s}.{%s}` is not a type operation", SOB, pSignature),
								null, $Result.startPosition());
						return;
					}
					if(this.isRepeat($CProduct, SOB)) return;
					
					TypeRef[] PTRefs = new TypeRef[pSignature.getParamCount()];
					String[]  PNames = new String [pSignature.getParamCount()];
					for(int i = pSignature.getParamCount(); --i >= 0; ) {
						PTRefs[i] = pSignature.getParamTypeRef(i);
						PNames[i] = pSignature.getParamName(i);
					}
					ExecSignature Signature = ExecSignature.newSignature("new", PTRefs, PNames, pSignature.isVarArgs(),
							TKJava.TVoid.getTypeRef(), pSignature.getLocation(), pSignature.getExtraData());
					
					Executable.Macro Macro = $CProduct.getEngine().getExecutableManager().newMacro(Signature, null);
					((TypeBuilder)SOB).addTempConstructor(pPAccess, pMoreData, Macro, pTempData);
				}
			};
		}

		
		// Elements ----------------------------------------------------------------------------------------------------
		
		// Dynamic ---------------------------------------------------------------------------------
		
		// Generic ------------------------------------------------------------
		
		static public StackOwnerAppender newAttrDynamic(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final TypeRef pTRef, final MoreData pMoreData, final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					if(!pIsStatic) SOB.addAttrDynamic(pPARead, pPAWrite, pPAConfig, pAName, pTRef, merge(pMoreData, pDoc));
					else {
						if(!this.canBeStatic($CProduct, SOB, pAName, $Result)) return;
						((TypeBuilder)SOB).addStaticAttrDynamic(pPARead, pPAWrite, pPAConfig, pAName, pTRef, merge(pMoreData, pDoc));
					} 
				}
			};
		}
		
		static public StackOwnerAppender newOperDynamic(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility pPAccess, final ExecSignature pSignature, final MoreData pMoreData,
				final Documentation pDoc) {
			
			return new OperAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pSignature, $Result)) return;
					if(!pIsStatic) SOB.addOperDynamic(pPAccess, pSignature, merge(pMoreData, pDoc));
					else {
						if(!this.canBeStatic($CProduct, SOB, pSignature, $Result)) return;
						((TypeBuilder)SOB).addStaticOperDynamic(pPAccess, pSignature,  merge(pMoreData, pDoc));
					}
				}
			};
		}
		
		// Non-Static ---------------------------------------------------------
		
		static public StackOwnerAppender newAttrDynamic(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final TypeRef pTRef, final MoreData pMoreData, final Documentation pDoc) {
			return newAttrDynamic(false, pCProduct, $TPackage, $Result, pPARead, pPAWrite, pPAConfig, pAName, pTRef, pMoreData, pDoc);
		}
		
		static public StackOwnerAppender newOperDynamic(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility pPAccess, final ExecSignature pSignature, final MoreData pMoreData,
				final Documentation pDoc) {
			return newOperDynamic(false, pCProduct, $TPackage, $Result, pPAccess, pSignature, pMoreData, pDoc); 
		}
		
		// Static -------------------------------------------------------------
		
		static public StackOwnerAppender newStaticAttrDynamic(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final TypeRef pTRef, final MoreData pMoreData, final Documentation pDoc) {
			return newAttrDynamic(true, pCProduct, $TPackage, $Result, pPARead, pPAWrite, pPAConfig, pAName, pTRef, pMoreData, pDoc);
		}
		
		static public StackOwnerAppender newStaticOperDynamic(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility pPAccess, final ExecSignature pSignature, final MoreData pMoreData,
				final Documentation pDoc) {
			return newOperDynamic(true, pCProduct, $TPackage, $Result, pPAccess, pSignature, pMoreData, pDoc); 
		}
		
		// Delegate --------------------------------------------------------------------------------
		
		// Generic ------------------------------------------------------------
		
		static public StackOwnerAppender newAttrDlgAttr(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final String pTName, final MoreData pMoreData,
				final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					if(!pIsStatic) SOB.addAttrDlgAttr(pPARead, pPAWrite, pPAConfig, pAName, pTName, merge(pMoreData, pDoc));
					else {
						if(!this.canBeStatic($CProduct, SOB, pAName, $Result)) return;
						((TypeBuilder)SOB).addStaticAttrDlgAttr(pPARead, pPAWrite, pPAConfig, pAName, pTName, merge(pMoreData, pDoc));
					}
				}
			};
		}
		
		static public StackOwnerAppender newOperDlgAttr(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPAccess, final ExecSignature pSignature, final String pTName, final MoreData pMoreData,
				final Documentation pDoc) {
			
			return new OperAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pSignature, $Result)) return;
					if(!pIsStatic) SOB.addOperDlgAttr(pPAccess, pSignature, pTName, merge(pMoreData, pDoc));
					else {
						if(!this.canBeStatic($CProduct, SOB, pSignature, $Result)) return;
						((TypeBuilder)SOB).addStaticOperDlgAttr(pPAccess, pSignature, pTName, merge(pMoreData, pDoc));
					}
				}
			};
		}
		
		// Non-Static ---------------------------------------------------------
		
		static public StackOwnerAppender newAttrDlgAttr(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final String pTName, final MoreData pMoreData,
				final Documentation pDoc) {			
			return newAttrDlgAttr(false, pCProduct, $TPackage, $Result, pPARead, pPAWrite, pPAConfig, pAName, pTName, pMoreData, pDoc);
		}
		
		static public StackOwnerAppender newOperDlgAttr(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPAccess, final ExecSignature pSignature, final String pTName, final MoreData pMoreData,
				final Documentation pDoc) {
			return newOperDlgAttr(false, pCProduct, $TPackage, $Result, pPAccess, pSignature, pTName, pMoreData, pDoc);
		}
		
		// Static -------------------------------------------------------------
		
		static public StackOwnerAppender newStaticAttrDlgAttr(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final String pTName, final MoreData pMoreData, final Documentation pDoc) {
			return newAttrDlgAttr(true, pCProduct, $TPackage, $Result, pPARead, pPAWrite, pPAConfig, pAName, pTName, pMoreData, pDoc);
		}
		
		static public StackOwnerAppender newStaticOperDlgAttr(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPAccess, final ExecSignature pSignature, final String pTName, final MoreData pMoreData,
				final Documentation pDoc) {
			return newOperDlgAttr(true, pCProduct, $TPackage, $Result, pPAccess, pSignature, pTName, pMoreData, pDoc);
		}
		
		// Direct ----------------------------------------------------------------------------------
		
		// Non-Temp ---------------------------------------------------------------------
		
		// Generic ------------------------------------------------------------

		static public StackOwnerAppender newAttrConst(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final Serializable pValue, final boolean IsValueExpr,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					if(!pIsStatic) SOB.addAttrConst(pAName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation, merge(pMoreData, pDoc));
					else {
						if(!this.canBeStatic($CProduct, SOB, pAName, $Result)) return;
						((TypeBuilder)SOB).addStaticAttrConst(pAName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation, merge(pMoreData, pDoc));
					}
				}
			};
		}

		static public StackOwnerAppender newAttrDirect(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final TypeRef pTypeRef, final Serializable pValue,
				final boolean IsValueExpr, final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData,
				final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					if(!pIsStatic) SOB.addAttrDirect(pAName, pIsNotNull, pTypeRef, pValue, IsValueExpr, pMoreInfo, pLocation, merge(pMoreData, pDoc));
					else {
						if(!this.canBeStatic($CProduct, SOB, pAName, $Result)) return;
						((TypeBuilder)SOB).addStaticAttrDirect(pAName, pIsNotNull, pTypeRef, pValue, IsValueExpr, pMoreInfo, pLocation, merge(pMoreData, pDoc));
					}
				}
			};
		}

		static public StackOwnerAppender newAttrDirect(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final boolean pIsNotNull, final DataHolderInfo pDHI, final Location pLocation,
				final MoreData pMoreData, final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					if(!pIsStatic) SOB.addAttrDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, merge(pMoreData, pDoc));
					else {
						if(!this.canBeStatic($CProduct, SOB, pAName, $Result)) return;
						((TypeBuilder)SOB).addStaticAttrDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, merge(pMoreData, pDoc));
					}
				}
			};
		}

		static public StackOwnerAppender newOperDirect(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPAccess, final Executable pExec, final MoreData pMoreData, final Documentation pDoc) {
			
			return new OperAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pExec.getSignature(), $Result)) return;
					if(!pIsStatic) SOB.addOperDirect(pPAccess, pExec, merge(pMoreData, pDoc));
					else {
						if(!this.canBeStatic($CProduct, SOB, pExec.getSignature(), $Result)) return;
						((TypeBuilder)SOB).addStaticOperDirect(pPAccess, pExec, merge(pMoreData, pDoc));
					}
				}
			};
		}
		
		// Non-Static ---------------------------------------------------------

		static public StackOwnerAppender newAttrConst(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final Serializable pValue, final boolean IsValueExpr,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Documentation pDoc) {			
			return newAttrConst(false, pCProduct, $TPackage, $Result, pAName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, pDoc);
		}

		static public StackOwnerAppender newAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final TypeRef pTypeRef, final Serializable pValue,
				final boolean IsValueExpr, final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData,
				final Documentation pDoc) {			
			return newAttrDirect(false, pCProduct, $TPackage, $Result, pAName, pIsNotNull, pTypeRef, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, pDoc);
		}

		static public StackOwnerAppender newAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final boolean pIsNotNull, final DataHolderInfo pDHI, final Location pLocation,
				final MoreData pMoreData, final Documentation pDoc) {			
			return newAttrDirect(false, pCProduct, $TPackage, $Result, pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, pMoreData, pDoc);
		}

		static public StackOwnerAppender newOperDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPAccess, final Executable pExec, final MoreData pMoreData, final Documentation pDoc) {
			return newOperDirect(false, pCProduct, $TPackage, $Result, pPAccess, pExec, pMoreData, pDoc);
		}
		
		// Static -------------------------------------------------------------

		static public StackOwnerAppender newStaticAttrConst(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final Serializable pValue, final boolean IsValueExpr,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Documentation pDoc) {			
			return newAttrConst(true, pCProduct, $TPackage, $Result, pAName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, pDoc);
		}

		static public StackOwnerAppender newStaticAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final TypeRef pTypeRef, final Serializable pValue,
				final boolean IsValueExpr, final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData,
				final Documentation pDoc) {			
			return newAttrDirect(true, pCProduct, $TPackage, $Result, pAName, pIsNotNull, pTypeRef, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, pDoc);
		}

		static public StackOwnerAppender newStaticAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final boolean pIsNotNull, final DataHolderInfo pDHI, final Location pLocation,
				final MoreData pMoreData, final Documentation pDoc) {			
			return newAttrDirect(true, pCProduct, $TPackage, $Result, pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, pMoreData, pDoc);
		}

		static public StackOwnerAppender newStaticOperDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPAccess, final Executable pExec, final MoreData pMoreData, final Documentation pDoc) {
			return newOperDirect(true, pCProduct, $TPackage, $Result, pPAccess, pExec, pMoreData, pDoc);
		}
		
		// Abstract ----------------------------------------------------------------------

		static public StackOwnerAppender newAbstractAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final TypeRef pTypeRef, final MoreData pMoreInfo,
				final Location pLocation, final MoreData pMoreData, final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					SOB.addAbstractAttrDirect(pAName, pIsNotNull, pTypeRef, pMoreInfo, pLocation, merge(pMoreData, pDoc));
				}
			};
		}

		static public StackOwnerAppender newAbstractAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility Access, final String pAName, final boolean pIsNotNull, final TypeRef pTypeRef,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					SOB.addAbstractAttrDirect(Access, pAName, pIsNotNull, pTypeRef, pMoreInfo, pLocation, merge(pMoreData, pDoc));
				}
			};
		}

		static public StackOwnerAppender newAbstractAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final boolean pIsNotNull, final DataHolderInfo pDHI, final Location pLocation,
				final MoreData pMoreData, final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					SOB.addAbstractAttrDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, merge(pMoreData, pDoc));
				}
			};
		}

		static public StackOwnerAppender newAbstractOperDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPAccess, final ExecSignature pSignature, final Executable.ExecKind pKind,
				final MoreData pMoreData, final Documentation pDoc) {
			
			return new OperAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pSignature, $Result)) return;
					SOB.addAbstractOperDirect(pPAccess, pSignature, pKind, merge(pMoreData, pDoc));
				}
			};
		}
		
		// Temp -----------------------------------------------------------------------------------
		
		// Generic ------------------------------------------------------------

		static public StackOwnerAppender newTempAttrConst(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result,
				final Accessibility Access, final String pAName, final boolean pIsNotNull, final boolean IsValueExpr,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Object pTempData,
				final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					if(!pIsStatic)
						SOB.addTempAttrConst(pAName, pIsNotNull, IsValueExpr, pMoreInfo, pLocation,
							merge(pMoreData, pDoc), pTempData);
					else {
						if(!this.canBeStatic($CProduct, SOB, pAName, $Result)) return;
						((TypeBuilder)SOB).addTempStaticAttrConst(Access, pAName, pIsNotNull, IsValueExpr, pMoreInfo,
								pLocation,
								merge(pMoreData, pDoc), pTempData);
					}
				}
			};
		}

		static public StackOwnerAppender newTempAttrDirect(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final TypeRef pTypeRef, final boolean IsValueExpr,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Object pTempData,
				final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					if(!pIsStatic) SOB.addTempAttrDirect(pAName, pIsNotNull, pTypeRef, IsValueExpr, pMoreInfo, pLocation, merge(pMoreData, pDoc), pTempData);
					else {
						if(!this.canBeStatic($CProduct, SOB, pAName, $Result)) return;
						((TypeBuilder)SOB).addTempStaticAttrDirect(pAName, pIsNotNull, pTypeRef, IsValueExpr, pMoreInfo, pLocation, merge(pMoreData, pDoc), pTempData);
					}
				}
			};
		}

		static public StackOwnerAppender newTempAttrDirect(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final boolean pIsNotNull, final DataHolderInfo pDHI, final Location pLocation,
				final MoreData pMoreData, final Object pTempData, final Documentation pDoc) {
			
			return new AttrAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pAName, $Result)) return;
					if(!pIsStatic) SOB.addTempAttrDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, merge(pMoreData, pDoc), pTempData);
					else {
						if(!this.canBeStatic($CProduct, SOB, pAName, $Result)) return;
						((TypeBuilder)SOB).addTempStaticAttrDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, merge(pMoreData, pDoc), pTempData);
					}
				}
			};
		}

		static public StackOwnerAppender newTempOperDirect(final boolean pIsStatic,
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pAccess, final Executable pExec, final MoreData pMoreData, final Object pTempData,
				final Documentation pDoc) {
			
			return new OperAppender() {
				public void append(CompileProduct $CProduct, StackOwnerBuilder SOB) {
					if(this.isRepeat($CProduct, SOB, pExec.getSignature(), $Result)) return;
					
					if(pTempData == null) {
						if(!pIsStatic) SOB.addOperDirect(pAccess, pExec, merge(pMoreData, pDoc));
						else {
							if(!this.canBeStatic($CProduct, SOB, pExec.getSignature(), $Result)) return;
							((TypeBuilder)SOB).addStaticOperDirect(pAccess, pExec, merge(pMoreData, pDoc));
						}
					} else {
						if(!pIsStatic) SOB.addTempOperDirect(pAccess, pExec, merge(pMoreData, pDoc), pTempData);
						else {
							if(!this.canBeStatic($CProduct, SOB, pExec.getSignature(), $Result)) return;
							((TypeBuilder)SOB).addTempStaticOperDirect(pAccess, pExec, merge(pMoreData, pDoc), pTempData);
						}
					}
				}
			};
		}
		
		// Non-Static ---------------------------------------------------------

		static public StackOwnerAppender newTempAttrConst(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pAccess, final String pAName, final boolean pIsNotNull, final boolean IsValueExpr,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Object pTempData,
				final Documentation pDoc) {
			return newTempAttrConst(false, pCProduct, $TPackage, $Result, pAccess, pAName, pIsNotNull, IsValueExpr,
					pMoreInfo, pLocation, pMoreData, pTempData, pDoc);
		}

		static public StackOwnerAppender newTempAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final TypeRef pTypeRef, final boolean IsValueExpr,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Object pTempData,
				final Documentation pDoc) {
			return newTempAttrDirect(false, pCProduct, $TPackage, $Result, pAName, pIsNotNull, pTypeRef, IsValueExpr,
					pMoreInfo, pLocation, pMoreData, pTempData, pDoc);
		}

		static public StackOwnerAppender newTempAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final boolean pIsNotNull, final DataHolderInfo pDHI, final Location pLocation,
				final MoreData pMoreData, final Object pTempData, final Documentation pDoc) {
			return newTempAttrDirect(false, pCProduct, $TPackage, $Result, pPARead, pPAWrite, pPAConfig, pAName, 
					pIsNotNull, pDHI, pLocation, pMoreData, pTempData, pDoc);
		}

		static public StackOwnerAppender newTempOperDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPAccess, final Executable pExec, final MoreData pMoreData, final Object pTempData,
				final Documentation pDoc) {
			return newTempOperDirect(false, pCProduct, $TPackage, $Result, pPAccess, pExec, pMoreData, pTempData, pDoc);
		}
		
		// Static -------------------------------------------------------------

		static public StackOwnerAppender newTempStaticAttrConst(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pAccess, final String pAName, final boolean pIsNotNull, final boolean IsValueExpr,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Object pTempData,
				final Documentation pDoc) {
			return newTempAttrConst(true, pCProduct, $TPackage, $Result, pAccess, pAName, pIsNotNull, IsValueExpr,
					pMoreInfo, pLocation, pMoreData, pTempData, pDoc);
		}

		static public StackOwnerAppender newTempStaticAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final String pAName, final boolean pIsNotNull, final TypeRef pTypeRef, final boolean IsValueExpr,
				final MoreData pMoreInfo, final Location pLocation, final MoreData pMoreData, final Object pTempData,
				final Documentation pDoc) {
			return newTempAttrDirect(true, pCProduct, $TPackage, $Result, pAName, pIsNotNull, pTypeRef, IsValueExpr,
					pMoreInfo, pLocation, pMoreData, pTempData, pDoc);
		}

		static public StackOwnerAppender newTempStaticAttrDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPARead, final Accessibility pPAWrite, final Accessibility pPAConfig,
				final String pAName, final boolean pIsNotNull, final DataHolderInfo pDHI, final Location pLocation,
				final MoreData pMoreData, final Object pTempData, final Documentation pDoc) {
			return newTempAttrDirect(true, pCProduct, $TPackage, $Result, pPARead, pPAWrite, pPAConfig, pAName, 
					pIsNotNull, pDHI, pLocation, pMoreData, pTempData, pDoc);
		}

		static public StackOwnerAppender newTempStaticOperDirect(
				final CompileProduct pCProduct, final PTypeProvider $TPackage, final ParseResult $Result, 
				final Accessibility pPAccess, final Executable pExec, final MoreData pMoreData, final Object pTempData,
				final Documentation pDoc) {
			return newTempOperDirect(true, pCProduct, $TPackage, $Result, pPAccess, pExec, pMoreData, pTempData, pDoc);
		}
	}
}
