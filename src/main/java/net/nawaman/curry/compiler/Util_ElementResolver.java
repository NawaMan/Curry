package net.nawaman.curry.compiler;

import java.io.Serializable;

import net.nawaman.curry.AttributeInfo;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Instructions_Initializer;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.StackOwnerBuilder;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TypeBuilder;
import net.nawaman.curry.TypeRef;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;

public class Util_ElementResolver {
	
	static public final Object CompileResolvedHasProblem = new Serializable() {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		public @Override String toString() { return "Problem"; }
	};

	// Resolve a constructor -------------------------------------------------------------------------------------------
	static public ElementResolver newConstructorResolver(final ExecSignature  pSignature,
			final ParseResult   $ThisResult, final int            pEIndex,
			final ParserTypeProvider $TProvider,  final CompileProduct pCProduct) {
		
		String[] Ims = pCProduct.getImports();
		final String[] Imports = (Ims == null)?null:Ims.clone();
		
		return new ElementResolver() {
			public void resolveElement(CompileProduct $CProduct, StackOwnerBuilder SOB) {
				if($ThisResult == null) return;
				
				ParseResult $Result = $ThisResult.subResultOf(pEIndex);
				ParserType       Type    = $TProvider.type($ThisResult.typeNameOf(pEIndex));
				// Just ignored this part
				if((Type == null) || ($Result == null)) {
					$CProduct.reportError(
						"Unable to compile the body. <ConstructorResolver:38>",
						null, $Result.startPosition());
					return;
				}
				
				if(!(SOB instanceof TypeBuilder)) {
					$CProduct.reportError(
						String.format(
							"Only a type can have a constructor: `{%s}` is not a type. <ConstructorResolver:47>",
							SOB),
						null,
						$Result.startPosition()
					);
					return;
				}

				TypeRef OwnerTypeRef     = ((TypeBuilder)SOB).getTypeRef();
				String  OwnerPackageName = SOB.getPackageName();

				boolean DoesSuperHaveDefaultConstructor = false;
				net.nawaman.curry.Type ThisT     = $CProduct.getTypeAtCompileTime(OwnerTypeRef);
				TypeRef                SuperTRef = null;
				if((SuperTRef = ThisT.getSuperRef()) != null)
					DoesSuperHaveDefaultConstructor = (SuperTRef.searchConstructor($CProduct.getEngine(), TypeRef.EmptyTypeRefArray) != null);
				
				$CProduct.clearScope();
				$CProduct.resetContextForConstructor(
						$CProduct.CCompiler.TheID,
						pSignature,
						OwnerTypeRef,
						OwnerPackageName,
						DoesSuperHaveDefaultConstructor);
				if(Imports != null) $CProduct.addImport(Imports);
				
				ParameterizedTypeInfo PTInfo = ThisT.getTypeSpec().getParameterizedTypeInfo();
				if(PTInfo != null) $CProduct.useParameterizedTypeInfos(PTInfo);
				
				Object O = Type.compile($ThisResult, pEIndex, null, $CProduct, $TProvider);
				if((O != null) && !(O instanceof Serializable)) {
					$CProduct.reportError(
						String.format(
							"The constructor body must be a serializable. (``{%s}.{%s}``) <ConstructorResolver:70>",
							SOB, pSignature),
						null, $Result.startPosition());
					return;
				}
				
				MExecutable ME = $CProduct.getEngine().getExecutableManager();
				
				if(O instanceof Expression[])
					 O = ME.newGroup($ThisResult.coordinateOf(pEIndex), (Expression[])O);
				else O = Expression.toExpr(O);
				
				boolean IsToAddDefaultConstructorRevokation = $CProduct.isToAddDefaultConstructorRevocation();
				if(IsToAddDefaultConstructorRevokation) {
					try {
						if((SuperTRef != null) && !TKJava.TAny.getTypeRef().equals(SuperTRef)) {
							if(SuperTRef.searchConstructor($CProduct.getEngine(), TypeRef.EmptyTypeRefArray) == null) {
								$CProduct.reportError(
									"The super type `"+SuperTRef+"` does not have a default constructor <Util_ElementResolver:88>",
									null, $ThisResult.startPositionOf(pEIndex));
								return;
							}
							
							O = ME.newGroup($ThisResult.coordinateOf(pEIndex), 
								new Expression[] {
									ME.newExpr(
										$ThisResult.coordinateOf(pEIndex),
										Instructions_Initializer.Inst_InvokeSuperInitializer_ByPTRefs.Name,
										(Object)TypeRef.EmptyTypeRefArray),
									(Expression)O
								});
						}
					} catch (Exception E) {}
				}
				
				$CProduct.clearScope();
				
				final Expression Body = Expression.toExpr(O);

				TypeRef[] PTRefs = new TypeRef[pSignature.getParamCount()];
				String[]  PNames = new String [pSignature.getParamCount()];
				for(int i = pSignature.getParamCount(); --i >= 0; ) {
					PTRefs[i] = pSignature.getParamTypeRef(i);
					PNames[i] = pSignature.getParamName(i);
				}
				ExecSignature Signature = ExecSignature.newSignature("new", PTRefs, PNames, pSignature.isVarArgs(),
						TKJava.TVoid.getTypeRef(), pSignature.getLocation(), pSignature.getExtraData());
				
				((TypeBuilder)SOB).resolveTempCons(Signature, $CProduct.CCompiler.TheID, Body);
			}
		};
	}
	
	// Resolve a direct attribute ----------------------------------------------------------------------------------
	
	/** Compile an attribute default value */
	static public Object CompileAttrDValue(
			final boolean        IsStatic,    final String            Name,
			final ParseResult    $ThisResult, final int               EIndex,
			final String[]       Imports,     final ParserTypeProvider     $TProvider,
			final CompileProduct $CProduct,   final StackOwnerBuilder SOB) {
		
		if($ThisResult == null) return CompileResolvedHasProblem;

		TypeRef OwnerTypeRef     = (SOB instanceof TypeBuilder)?((TypeBuilder)SOB).getTypeRef():null;
		String  OwnerPackageName = SOB.getPackageName();
		boolean IsOwnerObject    = (OwnerTypeRef != null) && !IsStatic;
		
		$CProduct.clearScope();
		$CProduct.resetContextForFragment(
				$CProduct.CCompiler.TheID,
				TKJava.TAny.getTypeRef(),
				IsOwnerObject,
				OwnerTypeRef,
				OwnerPackageName,
				null, null, null, false);
		if(Imports != null) $CProduct.addImport(Imports);
		
		ParseResult $Result = $ThisResult.subResultOf(EIndex);
		ParserType       Type    = $TProvider.type($ThisResult.typeNameOf(EIndex));
		// Just ignored this part
		if((Type == null) || ($Result == null)) {
			$CProduct.reportError(
				String.format(
					"Unable to compile the default value (`{%s}.{%s}`). <AttributeResolver:120>",
					SOB, Name),
				null, $Result.startPosition());
			return CompileResolvedHasProblem;
		}
		
		Object O = Type.compile($ThisResult, EIndex, null, $CProduct, $TProvider);
		if((O != null) && !(O instanceof Serializable)) {
			$CProduct.reportError(
				String.format(
					"The default value must be a serializable (`{%s}.{%s}`). <AttributeResolver:130>",
					SOB, Name),
				null, $Result.startPosition());
			return CompileResolvedHasProblem;
		}
		
		$CProduct.clearScope();
			
		return (Serializable)O;
	} 
	
	static abstract public class AttrResolver implements ElementResolver {

		abstract public boolean      isStatic();
		abstract public String       getName();
		abstract public int          getDefaultValuePosition();
		abstract public Serializable getDefaultValue(CompileProduct $CProduct, StackOwnerBuilder SOB);
		
		public void resolveElement(CompileProduct $CProduct, StackOwnerBuilder SOB) {
			final Serializable DValue    = this.getDefaultValue($CProduct, SOB);	
			if(DValue == CompileResolvedHasProblem) return;
			
			final boolean      IsStatic  = this.isStatic();
			final String       Name      = this.getName();
			final int          DValuePos = this.getDefaultValuePosition();
			final TypeRef      DVTRef    = $CProduct.getReturnTypeRefOf(DValue);
			
			boolean IsError = false;
			if(!IsStatic) {
				AttributeInfo AI = SOB.getTempAttrAttributeInfo(Name);
				if((DValue != null) && !AI.hasTypeRef() &&
				   !AI.getDeclaredTypeRef().canBeAssignedByInstanceOf(
						   $CProduct.getEngine(),
						   $CProduct.getReturnTypeRefOf(DValue)))
					 IsError = true;
				else IsError = !SOB.resolveTempAttr(Name, $CProduct.CCompiler.TheID, DVTRef, DValue);
			}
			else {
				if(!(SOB instanceof TypeBuilder)) {
					$CProduct.reportError(
						String.format(
							"Only type attribute can be static: `%s.%s` is not a type operation. <AttributeResolver:144>",
							SOB, Name
						), null, DValuePos);
					return;
				}
				AttributeInfo AI = ((TypeBuilder)SOB).getStaticTempAttrAttributeInfo(Name);
				if((DValue != null) && !AI.hasTypeRef() &&
				   !AI.getDeclaredTypeRef().canBeAssignedByInstanceOf(
						   $CProduct.getEngine(),
						   $CProduct.getReturnTypeRefOf(DValue)))
					 IsError = true;
				else IsError = !((TypeBuilder)SOB).resolveStaticTempAttr(Name, $CProduct.CCompiler.TheID, DVTRef, DValue);
			}
			
			if(IsError) {
				$CProduct.reportError(
					String.format(
						"There is a problem with the element default value: `%s.%s` <AttributeResolver:201>",
						SOB, Name
					), null, DValuePos);
				return;
			}
		}
	}
	
	
	static public ElementResolver newAttrResolver(
			final boolean       IsStatic,    final String   Name,
			final ParseResult   $ThisResult, final int      EIndex,
			final ParserTypeProvider $TProvider,  CompileProduct CProduct) {
		
		String[] Ims = CProduct.getImports();
		final String[] Imports = (Ims == null)?null:Ims.clone();
		
		return new AttrResolver() {

			/**{@inheritDoc}*/ @Override
			public boolean isStatic() {
				return IsStatic;
			}
			
			/**{@inheritDoc}*/ @Override
			public String getName() {
				return Name;
			}
			
			/**{@inheritDoc}*/ @Override
			public int getDefaultValuePosition() {
				return $ThisResult.subResultOf(EIndex).startPosition();
			}
			/**{@inheritDoc}*/ @Override
			public Serializable getDefaultValue(CompileProduct $CProduct, StackOwnerBuilder SOB) {
				return (Serializable)CompileAttrDValue(IsStatic, Name, $ThisResult, EIndex, Imports, $TProvider, $CProduct, SOB);
			}
		};
	}
	static public ElementResolver newAttrResolver_Value(final boolean IsStatic, final String Name, final int ValuePos,
			final Serializable DValue) {
		
		return new AttrResolver() {
			/**{@inheritDoc}*/ @Override
			public boolean isStatic() {
				return IsStatic;
			}
			/**{@inheritDoc}*/ @Override
			public String getName() {
				return Name;
			}
			/**{@inheritDoc}*/ @Override
			public int getDefaultValuePosition() {
				return ValuePos;
			}
			
			/**{@inheritDoc}*/ @Override
			public Serializable getDefaultValue(CompileProduct $CProduct, StackOwnerBuilder SOB) {
				return DValue;
			}
		};
	}

	// Resolve a direct operation ----------------------------------------------------------------------------------
	
	static public ElementResolver newOperResolver(
			final boolean       pIsStatic,   final ExecSignature  pSignature, final Executable.ExecKind pKind, 
			final ParseResult   $ThisResult, final int            pEIndex,    final String              pLangName, 
			final ParserTypeProvider $TProvider,  final CompileProduct pCProduct) {
		
		String[] Ims = pCProduct.getImports();
		final String[] Imports = (Ims == null)?null:Ims.clone();
		
		return new ElementResolver() {
			public void resolveElement(CompileProduct $CProduct, StackOwnerBuilder SOB) {
				if($ThisResult == null) return;

				TypeRef OwnerTypeRef     = (SOB instanceof TypeBuilder)?((TypeBuilder)SOB).getTypeRef():null;
				String  OwnerPackageName = SOB.getPackageName();
				boolean IsOwnerObject    = (OwnerTypeRef != null) && !pIsStatic;
				Object  CompiledResult   = null;
				
				try {
					$CProduct.clearScope();
				
					Executable.ExecKind Kind = pKind;
					if(Kind == null) Kind = Executable.ExecKind.SubRoutine;
					switch(Kind) {
						case Fragment:
							$CProduct.resetContextForFragment(
									$CProduct.CCompiler.TheID,
									TKJava.TAny.getTypeRef(),
									IsOwnerObject,
									OwnerTypeRef,
									OwnerPackageName,
									null, null, null, false);
							break;
						
						case Macro:
							$CProduct.resetContextForMacro(
									$CProduct.CCompiler.TheID,
									pSignature,
									IsOwnerObject,
									OwnerTypeRef,
									OwnerPackageName,
									null, null, null, false);
							break;
						
						case SubRoutine:
							$CProduct.resetContextForSubRoutine(
									$CProduct.CCompiler.TheID,
									pSignature,
									IsOwnerObject,
									OwnerTypeRef,
									OwnerPackageName,
									null, null, null);
							break;
					}
					if(Imports != null) $CProduct.addImport(Imports);

					// Perform the compilation				
					CompiledResult = Util_Executable.CompileExecutableBody(
							Util_Executable.Share_Context_Kind.ShareFull,
							pSignature,
							Kind.toString().toLowerCase().charAt(0),
							false, null, null, $ThisResult, (PTypePackage)$TProvider, $CProduct);
				} finally {
					$CProduct.clearScope();
				}
				
				if(pLangName != null) {

					if(!(CompiledResult instanceof Executable)) CompiledResult = Expression.toExpr(CompiledResult);
					Executable Exec = (Executable)CompiledResult;
					
					if(!pIsStatic) SOB.resolveTempOper(pSignature, $CProduct.CCompiler.TheID, Exec);
					else {
						if(!(SOB instanceof TypeBuilder)) {
							$CProduct.reportError(
								String.format(
									"Only type operation can be static: (`{%s}.{%s}`)",
									SOB, pSignature
								), null, $ThisResult.startPositionOf(pEIndex));
							return;
						}
						((TypeBuilder)SOB).resolveStaticTempOper(pSignature, $CProduct.CCompiler.TheID, Exec);
					}
					
					return;
				}
				
				if(CompiledResult instanceof Executable) {
					Executable Exec = (Executable)CompiledResult;
					if(Exec.isCurry()) CompiledResult = Exec.asCurry().getBody();
				}
				final Expression Body = Expression.toExpr(CompiledResult);
				if(!pIsStatic)
					SOB.resolveTempOper(pSignature, $CProduct.CCompiler.TheID, Body);
				else {
					if(!(SOB instanceof TypeBuilder)) {
						$CProduct.reportError(
							String.format(
								"Only type operation can be static: (`{%s}.{%s}`)",
								SOB, pSignature
							), null, $ThisResult.startPositionOf(pEIndex));
						return;
					}
					((TypeBuilder)SOB).resolveStaticTempOper(pSignature, $CProduct.CCompiler.TheID, Body);
				}
				
				return;
			}
		};
	}

}
