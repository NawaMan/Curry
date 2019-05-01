package net.nawaman.curry.extra.type_enum;
import net.nawaman.curry.Engine;
import net.nawaman.curry.EngineExtension;
import net.nawaman.curry.EngineExtensions;
import net.nawaman.curry.EngineSpec;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Instructions_Core;
import net.nawaman.curry.Package;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;

public class Test {
	
	static public void main(String ... pArgs) {
		Test_Package.main(pArgs);
		//Test_StackOwner.main(pArgs);
	}
	
	static public class Test_Package {
		static public void main(String ... pArgs) {
			EngineSpec ES = new EngineSpec() {
				@Override public String getEngineName() { return "Test"; }
				@Override protected EngineExtension[] getExtensions() {
					return new EngineExtension[] {
							new EngineExtensions.EE_AdvanceLoop(),
							new EngineExtensions.EE_Unit(),
							new EngineExtensions.EE_DefaultPackage(),
							new EE_Enum()
						};
				}
			};
			
			Engine E = net.nawaman.curry.Engine.newEngine(ES, true);
			
			System.out.println();
			System.out.println("Enum1");
			String TE1Name = "Enum1";
			TypeRef TRE1 = new TLPackage.TRPackage(EngineExtensions.EE_DefaultPackage.DefaultPackageName, TE1Name);
			
			System.out.println(E.getDefaultPackageBuilder().addType(Package.Access.Public,
					TE1Name,
					((TKEnum)E.getTypeManager().getTypeKind(TKEnum.KindName)).getTypeSpec(TRE1,
						null, false,
						EnumKind.Independent,
						new TEMemberSpec[] {
							new TEMS_Independent("E_1"),
							new TEMS_Independent("E_2"),
							new TEMS_Independent("E_3"),
							new TEMS_Independent("E_4"),
							new TEMS_Deriving("E_5", "E_2")
						},
						null, null
					),
					null
				));
			
			System.out.println(TRE1);
			E.getTypeManager().ensureTypeInitialized(TRE1);
			System.out.println(E.getTypeManager().getTypeFromRef(TRE1).getTypeRef().toDetail());
			System.out.println(E.getTypeManager().getTypeFromRef(TRE1).getTypeInfo().getDescription());
			DEnum E_1 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE1)).getMember("E_1");
			DEnum E_2 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE1)).getMember("E_2");
			DEnum E_5 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE1)).getMember("E_5");
			System.out.println("E_1.equals(E_5): "+ E_1.equals(E_5));
			System.out.println("E_2.equals(E_5): "+ E_2.equals(E_5));
			System.out.println("E_5.equals(E_1): "+ E_5.equals(E_1));
			System.out.println("E_5.equals(E_2): "+ E_5.equals(E_2));
			System.out.println("E_1.is(    E_5): "+ E_1.is(    E_5));
			System.out.println("E_2.is(    E_5): "+ E_2.is(    E_5));
			System.out.println("E_5.is(    E_1): "+ E_5.is(    E_1));
			System.out.println("E_5.is(    E_2): "+ E_5.is(    E_2));
	
			System.out.println();
			System.out.println("Enum2");
			String  TE2Name = "Enum2";
			TypeRef TRE2 = new TLPackage.TRPackage(EngineExtensions.EE_DefaultPackage.DefaultPackageName, TE2Name);
			System.out.println(E.getDefaultPackageBuilder().addType(Package.Access.Public,
					TE2Name,
					((TKEnum)E.getTypeManager().getTypeKind(TKEnum.KindName)).getTypeSpec(TRE2,
						TRE1, false,
						EnumKind.Emulating,
						new TEMemberSpec[] {
							new TEMS_Borrowing("E_1"),
							new TEMS_Borrowing("E_2"),
							new TEMS_Deriving( "E_N5","E_5")
						},
						null, null
					),
					null
				));
			
			System.out.println(TRE2);
			E.getTypeManager().ensureTypeInitialized(TRE2);
			System.out.println(E.getTypeManager().getTypeFromRef(TRE2).getTypeRef().toDetail());
			System.out.println(E.getTypeManager().getTypeFromRef(TRE2).getTypeInfo().getDescription());
			DEnum _E_1 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE2)).getMember("E_1");
			DEnum E_N5 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE2)).getMember("E_N5");
			System.out.println(" E_1 .is(    _E_1) : " +  E_1 .is(    _E_1));
			System.out.println("_E_1 .is(     E_1) : " + _E_1 .is(     E_1));
			System.out.println(" E_1 .equals(_E_1) : " +  E_1 .equals(_E_1));
			System.out.println("_E_1 .equals( E_1) : " + _E_1 .equals( E_1));
			System.out.println("_E_1 .equals( E_N5): " + _E_1 .equals( E_N5));
			System.out.println(" E_2 .equals( E_N5): " +  E_2 .equals( E_N5));
			System.out.println(" E_N5.equals( E_2) : " +  E_N5.equals( E_2));
			System.out.println(" E_5 .equals( E_N5): " +  E_5 .equals( E_N5));
			System.out.println(" E_N5.equals( E_5) : " +  E_N5.equals( E_5));
			
			System.out.println();
			System.out.println("Enum3");
			String  TE3Name = "Enum3";
			TypeRef TRE3 = new TLPackage.TRPackage(EngineExtensions.EE_DefaultPackage.DefaultPackageName, TE3Name);
			System.out.println(E.getDefaultPackageBuilder().addType(Package.Access.Public,
					TE3Name,
					((TKEnum)E.getTypeManager().getTypeKind(TKEnum.KindName)).getTypeSpec(TRE3,
						TRE1, false,
						EnumKind.Grouping,
						new TEMemberSpec[] {
							new TEMS_Borrowing(  "E_1"),
							new TEMS_Borrowing(  "E_2"),
							new TEMS_Independent("E_6"),
							new TEMS_Independent("E_7"),
							new TEMS_Independent("E_8"),
							new TEMS_Grouping(   "E_S5", "E_7", new String[] { "E_3", "E_5" })
						},
						null, null
					),
					null
				));
			
			System.out.println(TRE3);
			E.getTypeManager().ensureTypeInitialized(TRE3);
			System.out.println(E.getTypeManager().getTypeFromRef(TRE3).getTypeRef().toDetail());
			System.out.println(E.getTypeManager().getTypeFromRef(TRE3).getTypeInfo().getDescription());
			DEnum __E_1 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE3)).getMember("E_1");
			DEnum E_7   = ((TEnum)E.getTypeManager().getTypeFromRef(TRE3)).getMember("E_7");
			DEnum E_S5  = ((TEnum)E.getTypeManager().getTypeFromRef(TRE3)).getMember("E_S5");
			System.out.println(" _E_1 .equals(__E_1) : " +  _E_1 .equals(__E_1));
			System.out.println(" _E_1 .is(    __E_1) : " +  _E_1 .is(    __E_1));
			System.out.println("  E_1 .is(    __E_1) : " +   E_1 .is(    __E_1));
			System.out.println("__E_1 .equals(  E_S5): " + __E_1 .equals(  E_S5));
			System.out.println("  E_7 .equals(  E_5) : " +   E_7 .equals(  E_5));
			System.out.println("  E_S5.equals(  E_5) : " +   E_S5.equals(  E_5));
			System.out.println("  E_S5.equals(  E_N5): " +   E_S5.equals(  E_N5));
			System.out.println(" _E_1 .is(    __E_1) : " +  _E_1 .is(    __E_1));
			System.out.println("  E_S5.is(      E_N5): " +   E_S5.is(      E_N5));
		}
	}

	static class Test_StackOwner {
		static public void main(String ... pArgs) {
			EngineSpec ES = new EngineSpec() {
				@Override public String getEngineName() { return "Test"; }
				@Override protected EngineExtension[] getExtensions() {
					return new EngineExtension[] {
							new EngineExtensions.EE_AdvanceLoop(),
							new EngineExtensions.EE_Unit(),
							new EngineExtensions.EE_DefaultPackage(),
							new EE_Enum()
						};
				}
			};
			
			Engine     E = net.nawaman.curry.Engine.newEngine(ES, true);
			//Expression Expr;
			
			Type T = E.getTypeManager().getTypeOfTheInstanceOf(System.class);
			System.out.println(T);
			System.out.println(T.getTypeInfo().isTypeFieldExist("out"));
			System.out.println(T.getAttrData("out"));	// From the DataClass
			System.out.println(T.getAttrData("Public"));	// From the type's class
			
			System.out.println(E.execute("invokeByParams", T.getAttrData("out"), "println", "Hello World!"));
			System.out.println(E.execute("invokeByParams", T.getAttrData("out"), "println", 5));
			System.out.println(E.execute("invokeByParams", T.getAttrData("out"), "printf", "Number = %d\n", 5));

			System.out.println(E.execute("invokeByParams", T, "getProperties"));
			System.out.println(E.execute("invokeByParams", T, "getTheDataClass"));
			
			System.out.println();
			System.out.println("Enum1");
			String TE1Name = "Enum1";
			TypeRef TRE1 = new TLPackage.TRPackage(EngineExtensions.EE_DefaultPackage.DefaultPackageName, TE1Name);
			System.out.println(E.getDefaultPackageBuilder().addType(Package.Access.Public,
					TE1Name,
					((TKEnum)E.getTypeManager().getTypeKind(TKEnum.KindName)).getTypeSpec(TRE1,
						null, false,
						EnumKind.Independent,
						new TEMemberSpec[] {
							new TEMS_Independent("E_1"),
							new TEMS_Independent("E_2"),
							new TEMS_Independent("E_3"),
							new TEMS_Independent("E_4"),
							new TEMS_Deriving("E_5", "E_2")
						},
						null, null
					),
					null
				));
			
			System.out.println(TRE1);
			E.getTypeManager().ensureTypeInitialized(TRE1);
			System.out.println(E.getTypeManager().getTypeFromRef(TRE1).getTypeRef().toDetail());
			System.out.println(E.getTypeManager().getTypeFromRef(TRE1).getTypeInfo().getDescription());
			DEnum E_1 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE1)).getMember("E_1");
			DEnum E_2 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE1)).getMember("E_2");
			DEnum E_5 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE1)).getMember("E_5");
			System.out.println(E_1);
			System.out.println(E_2);
			System.out.println(E_5);
			System.out.println(E_1.equals(E_5));
			System.out.println(E_2.equals(E_5));
			
			System.out.println();
			System.out.println("Enum2");
			String  TE2Name = "Enum2";
			TypeRef TRE2 = new TLPackage.TRPackage(EngineExtensions.EE_DefaultPackage.DefaultPackageName, TE2Name);
			System.out.println(E.getDefaultPackageBuilder().addType(Package.Access.Public,
					TE2Name,
					((TKEnum)E.getTypeManager().getTypeKind(TKEnum.KindName)).getTypeSpec(TRE2,
						TRE1, false,
						EnumKind.Emulating,
						new TEMemberSpec[] {
							new TEMS_Borrowing("E_1"),
							new TEMS_Borrowing("E_2"),
							new TEMS_Deriving("E_N5","E_5")
						},
						null, null
					),
					null
				));
			
			System.out.println(TRE2);
			E.getTypeManager().ensureTypeInitialized(TRE2);
			System.out.println(E.getTypeManager().getTypeFromRef(TRE2).getTypeRef().toDetail());
			System.out.println(E.getTypeManager().getTypeFromRef(TRE2).getTypeInfo().getDescription());
			DEnum _E_1 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE2)).getMember("E_1");
			DEnum E_N5 = ((TEnum)E.getTypeManager().getTypeFromRef(TRE2)).getMember("E_N5");
			System.out.println(E_1.is(_E_1));
			System.out.println(_E_1.equals(E_1));
			System.out.println(_E_1.equals(E_N5));
			System.out.println(E_2.equals(E_N5));
			System.out.println(E_5.equals(E_N5));
			
			System.out.println();
			System.out.println("TEnumCount --------------------------------");
			TEnum TE1 = (TEnum)E.getTypeManager().getTypeFromRef(TRE1);
			TEnum TE2 = (TEnum)E.getTypeManager().getTypeFromRef(TRE2);
			System.out.println(TE1.getAttrData("TEnumCount"));
			System.out.println(TE2.getAttrData("TEnumCount"));
			
			System.out.println();
			System.out.println("TNumF1 --------------------------------");
			System.out.println(TE1.getAttrData("TNumF1"));
			System.out.println(TE2.getAttrData("TNumF1"));

			System.out.println(TE1.setAttrData("TNumF1", "TNumF1's New Value"));
			System.out.println(TE2.setAttrData("TNumF1", "TNumF2's New Value"));
			System.out.println(TE1.getAttrData("TNumF1"));
			System.out.println(TE2.getAttrData("TNumF1"));
			
			System.out.println();
			System.out.println("F --------------------------------");

			System.out.println(E_1.getAttrData("F"));
			System.out.println(E_5.getAttrData("F"));
			System.out.println(E_1.setAttrData("F", 1));
			System.out.println(E_5.setAttrData("F", 5));
			System.out.println(E_1.getAttrData("F"));
			System.out.println(E_5.getAttrData("F"));

			System.out.println();
			System.out.println("factorialStatic --------------------------------");
			
			System.out.println(E.execute("invokeByParams", TE1, "factorialStatic"));
			System.out.println(E.execute("invokeByParams", TE2, "factorialStatic"));

			System.out.println();
			System.out.println("factorialNonStatic --------------------------------");
			
			System.out.println(E.execute("invokeByParams", E_1, "factorialNonStatic"));
			System.out.println(E.execute("invokeByParams", E_5, "factorialNonStatic"));

			System.out.println();
			System.out.println("Delegate --------------------------------");
			System.out.println(E.execute("invokeByParams", E_5, "toLowerCase"));
			System.out.println(E.execute("invokeByParams", E_5, "toUpperCase"));
			//System.out.println(UByte.bc2hex((byte[])E.execute("invokeByParams", E_5, "getBytes")));
			System.out.println(E.execute("invokeByParams", E_5, "split", "e"));

			System.out.println();
			
			Executable.SubRoutine SubR = E.getExecutableManager().newSubRoutine(
					ExecSignature.newSignature("factorial", new TypeRef[] { TKJava.TInteger.getTypeRef() }, new String[] { "I" }, false , TKJava.TInteger.getTypeRef(), null, null),
					E.getInstruction(Instructions_Core.Inst_Stack.Name).newExprSubs(new Object[] { "Here" },
						new Expression[] {
							E.getInstruction("if").newExprSubs(
								new Object[]     { E.getInstruction("lessThanEqual").newExpression(E.getInstruction("getVarValue").newExpression("I"), 1) },
								new Expression[] { E.getInstruction("return").newExpression(1) }
							),
							E.getInstruction("return").newExpression(
								E.getInstruction("multiply").newExpression(
									E.getInstruction("getVarValue").newExpression("I"),
									E.getInstruction("callSelf").newExpression(
										E.getInstruction("substract").newExpression(
											E.getInstruction("getVarValue").newExpression("I"),
											1
										)
									)
								)
							)
						}
					)
				);

			System.out.println(E.getTypeManager().getTypeOf(SubR));
		}
	}
}
