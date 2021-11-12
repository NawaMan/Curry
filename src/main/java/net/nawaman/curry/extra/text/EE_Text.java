package net.nawaman.curry.extra.text;

import java.util.Random;
import java.util.Vector;

import net.nawaman.curry.ActionRecord;
import net.nawaman.curry.Context;
import net.nawaman.curry.CurryError;
import net.nawaman.curry.DObject;
import net.nawaman.curry.DObjectStandalone;
import net.nawaman.curry.Engine;
import net.nawaman.curry.EngineExtension;
import net.nawaman.curry.Expression;
import net.nawaman.curry.ExternalContext;
import net.nawaman.curry.Inst_AbstractSimple;
import net.nawaman.curry.Inst_AbstractStack;
import net.nawaman.curry.Instruction;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.MType;
import net.nawaman.curry.MUnit;
import net.nawaman.curry.Package;
import net.nawaman.curry.SpecialResult;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.TLPrimitive;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.EngineExtensions.EE_DefaultPackage;
import net.nawaman.curry.Instructions_Core.Inst_Doc;
import net.nawaman.curry.Instructions_Core.Inst_ToString;
import net.nawaman.curry.Instructions_StackOwner.Inst_Invoke;
import net.nawaman.curry.Instructions_StackOwner.Inst_Invoke_ByParams;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.compiler.Util_Literal;
import net.nawaman.curry.extra.type_object.EE_Object;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.text.FixedLengthText;
import net.nawaman.text.StructureText;
import net.nawaman.text.Text;
import net.nawaman.text.AsText;
import net.nawaman.text.UText;
import net.nawaman.util.UArray;
import net.nawaman.util.UObject;

/** EngineExtension of Pattern */
public class EE_Text extends EngineExtension { 
		
	static public final String Name = "Text";
	
	static public final TypeRef TREF_Text   = new TLPrimitive.TRPrimitive(Text  .class.getCanonicalName());
	static public final TypeRef TREF_AsText = new TLPrimitive.TRPrimitive(AsText.class.getCanonicalName());
	
	private final TypeRef TREF_HasCharSequence = new TLPackage.TRPackage("pattern~>text", "HasCharSequence");
		
	/** Constructs an engine extension. */
	public EE_Text() {}
		
	/**{@inheritDoc}*/ @Override
	protected String getExtName() {
		return Name;
	}
	/**{@inheritDoc}*/ @Override
	protected String[] getRequiredExtensionNames() {
		return new String[] { EE_Object.Name };
	}
	/**{@inheritDoc}*/ @Override
	protected String initializeThis() {	
		// Create the unit manager
		
		// Register instruction
		this.regInst(-155601); // Inst_NewText
		this.regInst(-242241); // Inst_CreateText
		this.regInst(-179185); // Inst_EchoText
		this.regInst(-142977); // Inst_AddTabs

		return null;
	}
	/**{@inheritDoc}*/ @Override
	protected Instruction getNewInstruction(int hSearch) {
		Engine E = this.getEngine();
		
		switch(hSearch) {
			case -155601: return new Inst_NewText   (E);
			case -242241: return new Inst_CreateText(E);
			case -179185: return new Inst_EchoText  (E);
			case -142977: return new Inst_AddTabs   (E);
		}
		return null;
	}
	
	// Compiler --------------------------------------------------------------------------------------------------------
	
	static public Object ParseCompileText(ParseResult $Result, PTypeProvider $TProvider, CompileProduct $CProduct) {
		MExecutable $ME = $CProduct.getEngine().getExecutableManager();
		
		// SimpleText --------------------------------------------------------------------------------------------------
		if($Result.textOf("$IsSimpleText") != null)
			return $ME.newExpr($Result.coordinateOf(0), Inst_NewText.Name, $Result.valueOf("#String", $TProvider, $CProduct));

		// SimpleExpressionString --------------------------------------------------------------------------------------
		if($Result.textOf("#StrExpr") != null)
			return $ME.newExpr(
					$Result.coordinateOf(0),
					Inst_NewText.Name,
					$ME.newExpr(
						$Result.coordinateOf(0),
						Inst_ToString.Name,
						$Result.valueOf("#StrExpr", $TProvider, $CProduct)
					)
				);
		
		// Long text ---------------------------------------------------------------------------------------------------

		// Manipulate Before
		Instruction CreateText = $CProduct.getEngine().getInstruction(Inst_CreateText.Name);
		CreateText.manipulateCompileContextStart($CProduct, $Result.startPositionOf(0));
		
		StringBuilder  LastStr = new StringBuilder();
		int            Count   = $Result.entryCount();
		Vector<Object> Objs    = new Vector<Object>();
		
		for(int i = 0; i < Count; i++) {
			String  Name   = $Result.nameOf(i);
			boolean IsExpr = false;
			if("$Chars[]".equals(Name)) {
				LastStr.append($Result.textOf(i));

			} else if("#EscapeChr".equals(Name)) {
				LastStr.append(Util_Literal.CompileCharEscape($Result.textOf(i), $Result, $TProvider, $CProduct));

			} else if("#EscapeStr".equals(Name)) {
				LastStr.append($Result.valueOf(i, $TProvider, $CProduct));

			} else if((IsExpr = "#Expr".equals(Name)) || "#Stms".equals(Name)) {
				// Save the non-added string
				if(LastStr.length() != 0) {
					Objs.add(LastStr.toString());
					LastStr = new StringBuilder();
				}
				
				Expression Expr;
				if(IsExpr)
					Expr = Expression.newExpr($Result.coordinateOf(i), Expression.toExpr($Result.valueOf(i, $TProvider, $CProduct)));
				else { 
					Expression[] Exprs = (Expression[])$Result.valueOf(i, $TProvider, $CProduct);
					if(Exprs == null) continue;
					
					Expression[] NewExprs = new Expression[Exprs.length + 1];
					System.arraycopy(Exprs, 0, NewExprs, 0, NewExprs.length - 1);
					Expr = Expression.newExpr($Result.coordinateOf(i), $ME.newGroup($Result.coordinateOf(i), NewExprs));
				}
				Objs.add(Expr);
			}
		}
		if(LastStr.length() != 0) Objs.add(LastStr.toString());
		
		Object[] Params = Objs.toArray(new Object[Objs.size()]);
		CreateText.manipulateCompileContextBeforeSub(Params, $CProduct, $Result.startPositionOf(0));
		
		// Creates the Expr
		Expression Expr = $ME.newExpr(
				$Result.coordinateOf(0),
				Inst_CreateText.Name,
				(Object[])Params
			);
		
		CreateText.manipulateCompileContextFinish(Expr, $CProduct);
		
		return Expr;
	}
	
	// Instructions ----------------------------------------------------------------------------------------------------
	
	/** The instruction to new Text object */
	static public final class Inst_NewText extends Inst_AbstractSimple {
		static public final String Name = "newText";
		
		Inst_NewText(Engine pEngine) {
			super(pEngine, Name + "($):"+Text.class.getCanonicalName());
		}
		
		Package     DefaultPackage = null;
		MExecutable $MExecutable   = null;
		EE_Text     $EEText        = null;
		
		/** Creates an appropriate fix length text */
		protected FixedLengthText newFixedLengthText(Context pContext, ActionRecord AR, CharSequence Str) {
			if(AR != null) {
				if(DefaultPackage == null) {
					MUnit UnitManager = this.getEngine().getUnitManager();
					DefaultPackage = UnitManager.getPackage(EE_DefaultPackage.DefaultPackageName);
				}
				if((AR.getActor() == DefaultPackage) && (AR.getLocationSnapshot() == null)) AR = null;
			}
			
			if(AR == null) return new FixedLengthText(    Str);
			else           return new FixedLengthText(AR, Str);
		}
		
		
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Engine $Engine = this.getEngine();
			if(this.$MExecutable == null) this.$MExecutable =          $Engine.getExecutableManager();
			if(this.$EEText      == null) this.$EEText      = (EE_Text)$Engine.getExtension(EE_Text.Name);
			
			Object O = pParams[0];
			if(!(O instanceof CharSequence)) {
				
				if(O instanceof AsText)
					O = ((AsText)O).asText();

				else if(!(O instanceof DObjectStandalone))
					O = this.getEngine().toString(O);
				
				else {
					O = ((DObjectStandalone)O).getAsDObject();

					if(((DObject)O).getAsNative() instanceof AsText)
						O = ((AsText)((DObject)O).getAsNative()).asText();
					
					else if(MType.CanTypeRefByAssignableBy(pContext, $Engine, this.$EEText.TREF_HasCharSequence, O))
						 O = this.executeAnExpression(pContext, this.$MExecutable.newExpr(Inst_Invoke.Name, O, "getCharSequence"));
					else O = (String)this.executeAnExpression(pContext, this.$MExecutable.newExpr(Inst_ToString.Name, O));
				}
			}
			return this.newFixedLengthText(pContext, ExternalContext.newActionRecord(pContext), (CharSequence)O);
		}
		
		/** {@inheritDoc} */ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return EE_Text.TREF_Text;
		}
	}
	
	/** The instruction to create Text object */
	static public final class Inst_CreateText extends Inst_AbstractStack {
		static public final String Name = "createText";
		
		Inst_CreateText(Engine pEngine) {
			super(pEngine, Name + "(~...):"+Text.class.getCanonicalName());
			Random R = new Random();
			for(int i = 0; i < 100; i++) R.nextInt();
			this.TextVarName = Math.abs(R.nextInt()) + "_Text";
			this.TextType    = pEngine.getExecutableManager().getTypeManager().getTypeOfTheInstanceOf(Text.class);
		}
		
		String  TextVarName;
		Type    TextType;
		
		Package     DefaultPackage = null;
		MExecutable $MExecutable   = null;
		EE_Text     $EEText        = null;
		
		/** Creates an appropriate fix length text */
		protected FixedLengthText newFixedLengthText(Context pContext, ActionRecord AR, CharSequence Str) {
			if(AR != null) {
				if(DefaultPackage == null) {
					MUnit UnitManager = this.getEngine().getUnitManager();
					DefaultPackage = UnitManager.getPackage(EE_DefaultPackage.DefaultPackageName);
				}
				if((AR.getActor() == DefaultPackage) && (AR.getLocationSnapshot() == null)) AR = null;
			}
			
			if(AR == null) return new FixedLengthText(    Str);
			else           return new FixedLengthText(AR, Str);
		}
		
		
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Expression Expr, Object[] pParams) {
			Engine $Engine = this.getEngine();
			if(this.$MExecutable == null) this.$MExecutable =          $Engine.getExecutableManager();
			if(this.$EEText      == null) this.$EEText      = (EE_Text)$Engine.getExtension(EE_Text.Name);
			
			// Returns the data
			ActionRecord AR = ExternalContext.newActionRecord(pContext);
			Object[]     Os = UArray.getObjectArray(pParams[0]);
			
			// See if one of this is
			StringBuilder LastStr = new StringBuilder();
			StructureText Result  = new StructureText(AR);

			pContext = this.newStack(pContext, null, Expr);
			ExternalContext.newConstant(pContext, this.TextVarName, this.TextType, Result);
			
			for(Object O : Os) {
				if(O == null) continue;

				
				if(O instanceof SpecialResult)
					return O;
				
				
				if(O instanceof AsText)
					O = ((AsText)O).asText();

				else if(O instanceof DObjectStandalone) {
					O = ((DObjectStandalone)O).getAsDObject();

					if(((DObject)O).getAsNative() instanceof AsText)
						O = ((AsText)((DObject)O).getAsNative()).asText();
					
					else if(MType.CanTypeRefByAssignableBy(pContext, $Engine, this.$EEText.TREF_HasCharSequence, O))
						 O = this.executeAnExpression(pContext, this.$MExecutable.newExpr(Inst_Invoke.Name, O, "getCharSequence"));
				}
				
				if(!(O instanceof Expression) && !(O instanceof Text)) 
					LastStr.append(UObject.toString(O));
					
				else {
					if(LastStr.length() != 0) {
						Result.append(this.newFixedLengthText(pContext, null, LastStr.toString()));
						LastStr = new StringBuilder();
					}
					
					Object R = null;
					
					ActionRecord LocalAR = null;
					
					if(O instanceof AsText)
						O = ((AsText)O).asText();
 
					if(O instanceof Text) R =  (Text)O;
					else {
						Expression Ex    = (Expression)O;
						boolean    IsDoc = Ex.isInstruction($Engine, Inst_Doc.Name);
						
						ActionRecordHook ARHook = IsDoc ? new ActionRecordHook() : null;
						// Not a doc
						R = this.executeAnExpression(pContext, Ex, ARHook);
						
						if(R instanceof SpecialResult)
							return R;
						
						if(IsDoc) LocalAR = ARHook.ARecord;
						else      LocalAR = ExternalContext.newActionRecord(pContext);
					}
					
					if(R instanceof AsText)
						R = ((AsText)O).asText();

					else if(R instanceof DObjectStandalone) {
						R = ((DObjectStandalone)R).getAsDObject();

						if(((DObject)R).getAsNative() instanceof AsText)
							R = ((AsText)((DObject)R).getAsNative()).asText();
						
						else if(MType.CanTypeRefByAssignableBy(pContext, $Engine, this.$EEText.TREF_HasCharSequence, R))
							 R = this.executeAnExpression(pContext, this.$MExecutable.newExpr(Inst_Invoke_ByParams.Name, R, "getCharSequence"));
					}
					
					if(!(R instanceof Text)) {
						if(R == null) continue;
						
						R = this.newFixedLengthText(
								pContext,
								LocalAR,
								(!(R instanceof DObjectStandalone))
									? this.getEngine().toString(R)
									: (String)this.executeAnExpression(
										pContext,
										this.$MExecutable.newExpr(
											Inst_ToString.Name,
											((DObjectStandalone)R).getAsDObject()
										)
									)
								);
					}
					
					Result.append((Text)R);
				}
			}
			
			if(Result.getStructureCount() == 0)
				return this.newFixedLengthText(pContext, AR, LastStr.toString());

			if(LastStr.length() != 0)
				Result.append(this.newFixedLengthText(pContext, null, LastStr.toString()));
			
			return Result;
		}
		
		/**{@inherDoc}*/ @Override
		public boolean manipulateCompileContextStart(CompileProduct pCProduct, int pPosition) {
			pCProduct.newScope(null, TKJava.TVoid.getTypeRef());
			pCProduct.newConstant(this.TextVarName, this.TextType.getTypeRef());
			return true;
		}
		
		/**{@inherDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			pCProduct.exitScope();
			return true;
		}
		
		/** {@inheritDoc} */ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return EE_Text.TREF_Text;
		}
	}
	
	/** The instruction to create Text object */
	static public final class Inst_EchoText extends Inst_AbstractSimple {
		static public final String Name = "echoText";
		
		Inst_EchoText(Engine pEngine) {
			super(pEngine, Name + "(~):~");
		}
		
		String          TextVarName  = null;
		Inst_CreateText CreateText   = null;
		MExecutable     $MExecutable = null;
		EE_Text         $EEText      = null;
		
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Engine $Engine = this.getEngine();
			
			if(this.TextVarName == null) {
				this.CreateText   = (Inst_CreateText)this.getEngine().getInstruction(Inst_CreateText.Name);
				this.TextVarName  = this.CreateText.TextVarName;
				this.$MExecutable = this.getEngine().getExecutableManager();
				this.$EEText      = (EE_Text)$Engine.getExtension(EE_Text.Name);
			}
			
			Object Obj = null;
			try { Obj = ExternalContext.getVarValue(pContext, this.TextVarName); }
			catch (Exception E) {}
			
			if(!(Obj instanceof Text)) {
				throw new CurryError(
					"Echo is called out side a text stack: Unable to find a Text object to echo into. <EE_Pattern:354>",
					pContext
				);
			}
			
			StructureText SText = (StructureText)Obj;
			
			Object ToBeEcho = pParams[0];
			if(ToBeEcho == null)
				return null;

			
			if(ToBeEcho instanceof AsText)
				ToBeEcho = ((AsText)ToBeEcho).asText();

			else if(ToBeEcho instanceof DObjectStandalone) {
				
				ToBeEcho = ((DObjectStandalone)ToBeEcho).getAsDObject();

				if(((DObject)ToBeEcho).getAsNative() instanceof AsText)
					ToBeEcho = ((AsText)((DObject)ToBeEcho).getAsNative()).asText();
				
				else if(MType.CanTypeRefByAssignableBy(pContext, $Engine, this.$EEText.TREF_HasCharSequence, ToBeEcho))
					ToBeEcho = this.executeAnExpression(pContext, this.$MExecutable.newExpr(Inst_Invoke_ByParams.Name, ToBeEcho, "getCharSequence"));
			}
			
			if(ToBeEcho instanceof Text)
				 SText.append((Text)ToBeEcho);
			else {
				ToBeEcho = 
					(!(ToBeEcho instanceof DObjectStandalone))
					? this.getEngine().toString(ToBeEcho)
					: (String)this.executeAnExpression(
						pContext,
						this.$MExecutable.newExpr(
							Inst_ToString.Name,
							((DObjectStandalone)ToBeEcho).getAsDObject()
						)
					);
					
				SText.append(
					this.CreateText.newFixedLengthText(
						pContext,
						null, // ExternalContext.newActionRecord(pContext),
						ToBeEcho.toString()
					)
				);
			}
			
			return ToBeEcho;
		}
		
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			TypeRef ReturnRef = pCProduct.getReturnTypeRefOf(pExpr.getParam(0));
			
			if(!pCProduct.isCompileTimeCheckingFull()) return ReturnRef;
			
			if(this.TextVarName == null) {
				this.CreateText   = (Inst_CreateText)this.getEngine().getInstruction(Inst_CreateText.Name);
				this.TextVarName  = this.CreateText.TextVarName;
				this.$MExecutable = this.getEngine().getExecutableManager();
			}
			
			// If the var does not exist or the type is not a text, report a warning
			if(!pCProduct.isVariableExist(this.TextVarName) &&
			   !this.CreateText.TextType.getTypeRef().equals(pCProduct.getVariableTypeRef(this.TextVarName)))
				pCProduct.reportWarning("Echo must be under a Text stack.", null, pExpr.getColumn(), pExpr.getLineNumber());
			
			return ReturnRef;
		}
		
		/**{@inherDoc}*/ @Override
		public boolean manipulateCompileContextStart(CompileProduct pCProduct, int pPosition) {
			if(!pCProduct.isCompileTimeCheckingFull()) return true;
			
			if(this.TextVarName == null) {
				this.CreateText   = (Inst_CreateText)this.getEngine().getInstruction(Inst_CreateText.Name);
				this.TextVarName  = this.CreateText.TextVarName;
				this.$MExecutable = this.getEngine().getExecutableManager();
			}
			
			// If the var does not exist or the type is not a text, report a warning
			if(!pCProduct.isVariableExist(this.TextVarName) &&
			   !this.CreateText.TextType.getTypeRef().equals(pCProduct.getVariableTypeRef(this.TextVarName)))
				pCProduct.reportWarning("Echo must be under a Text stack.", null, pPosition);
			
			return true;
		}
	}
	
	/** The instruction to add Tabs to Text */
	static public final class Inst_AddTabs extends Inst_AbstractSimple {
		static public final String Name = "addTabs";
		
		Inst_AddTabs(Engine pEngine) {
			super(pEngine, Name + "(~,+i):"+CharSequence.class.getCanonicalName());
		}
		
		Package     DefaultPackage = null;
		MExecutable $MExecutable   = null;
		EE_Text     $EEText        = null;
		
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Engine $Engine = this.getEngine();
			if(this.$MExecutable == null) this.$MExecutable =          $Engine.getExecutableManager();
			if(this.$EEText      == null) this.$EEText      = (EE_Text)$Engine.getExtension(EE_Text.Name);
			
			Object O = pParams[0];
			if(O == null) return null;
			
			if(!(O instanceof CharSequence)) {
				if(O instanceof AsText)
					O = ((AsText)O).asText();

				else if(!(O instanceof DObjectStandalone))
					O = this.getEngine().toString(O);
				
				else {
					O = ((DObjectStandalone)O).getAsDObject();

					if(((DObject)O).getAsNative() instanceof AsText)
						O = ((AsText)((DObject)O).getAsNative()).asText();
					
					else if(MType.CanTypeRefByAssignableBy(pContext, $Engine, this.$EEText.TREF_HasCharSequence, O))
						 O = this.executeAnExpression(pContext, this.$MExecutable.newExpr(Inst_Invoke.Name, O, "getCharSequence"));
					else O = (String)this.executeAnExpression(pContext, this.$MExecutable.newExpr(Inst_ToString.Name, O));
				}
			}
			
			CharSequence CS = (CharSequence)O;
			ActionRecord AR = ExternalContext.newActionRecord(pContext);
			
			return UText.AddTabs(CS, (Integer)pParams[1], true, AR);
		}
		
		/** {@inheritDoc} */ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return TKJava.TCharSequence.getTypeRef();
		}
	}
}
