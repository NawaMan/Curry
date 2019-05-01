package net.nawaman.curry.test;

import net.nawaman.curry.EngineExtensions.EE_AdvanceLoop;
import net.nawaman.curry.EngineExtensions.EE_DataHolder;
import net.nawaman.curry.EngineExtensions.EE_DefaultPackage;
import net.nawaman.curry.EngineExtensions.EE_Java;
import net.nawaman.curry.EngineExtensions.EE_StackOwner;
import net.nawaman.curry.EngineExtensions.EE_StackOwnerCustomizable;
import net.nawaman.curry.EngineExtensions.EE_Unit;
import net.nawaman.curry.compiler.EE_Language;
import net.nawaman.curry.extra.type_enum.EE_Enum;
import net.nawaman.curry.extra.type_object.EE_Object;
import net.nawaman.curry.test.AllTests.TestCase;
import net.nawaman.util.UObject;

public class Test_01_Extensions_Instructions extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		// NOTE: It is suggested that every time a new instruction is added, list of the instructions must be determine.
		//   This is because instruction specification is verify at load time and instructions are load in lazy manner (
		//   for better performance).  

		this.printSection("Known Instructions, its signature and its hash");
		
		// All known extensions ------------------------------------------------------------------------------------
		this.assertValue(
			"[" +
				"{ Name: Java }, " +
				"{ Name: StackOwner }, " +
				"{ Name: StackOwnerCustomizable }, " +
				"{ Name: Unit }, " +
				"{ Name: DefaultPackage; DefaultUnitName: default; DefaultPackageName: default }, " +
				"{ Name: DataHolder }, " +
				"{ Name: AdvanceLoop }, " +
				"{ Name: Enum }, " +
				"{ Name: Object }, " +
				"{ Name: Language }" +
			"]",
			UObject.toDetail(AllTests.getEngine().getEngineSpec().getEngineExtensions())
		);

		// All Code Instructions -----------------------------------------------------------------------------------
		this.assertValue(
			" Owner =>     Hash: Specification\n"+
			"Engine =>   -27025: InstSpec: =OR(boolean...):boolean\n"+
			"Engine =>   -34801: InstSpec:  if(boolean){}:boolean\n"+
			"Engine =>   -36881: InstSpec:  is(any...):boolean\n"+
			"Engine =>   -37089: InstSpec: =AND(boolean...):boolean\n"+
			"Engine =>   -37777: InstSpec: =or(any...):any\n"+
			"Engine =>   -42321: InstSpec: =NOT(boolean):boolean\n"+
			"Engine =>   -43921: InstSpec: =XOR(boolean,boolean):boolean\n"+
			"Engine =>   -53985: InstSpec: =and(any...):any\n"+
			"Engine =>   -54273: InstSpec: =abs(Number):Number\n"+
			"Engine =>   -54577: InstSpec:  doc(net.nawaman.curry.Documentation,Expression):any\n"+
			"Engine =>   -55377: InstSpec: =neg(Number):Number\n"+
			"Engine =>   -57361: InstSpec:  for(String,Expression,Expression,Expression){}:boolean\n"+
			"Engine =>   -59217: InstSpec: =not(any):any\n"+
			"Engine =>   -60081: InstSpec:  run(any):any\n"+
			"Engine =>   -60817: InstSpec: =xor(any...):any\n"+
			"Engine =>   -75361: InstSpec:  data(any):any\n"+
			"Engine =>   -75505: InstSpec:  call(any,any...):any\n"+
			"Engine =>   -77137: InstSpec:  hash(any):int\n"+
			"Engine =>   -77633: InstSpec:  done(String,any):any\n"+
			"Engine =>   -77665: InstSpec:  exec(any,any...):any\n"+
			"Engine =>   -78017: InstSpec: =cast(+Type,any):any\n"+
			"Engine =>   -81089: InstSpec:  exit(String,any):any\n"+
			"Engine =>   -81233: InstSpec:  loop(String,Expression,Expression,Expression,Expression,Expression){}:any\n"+
			"Engine =>   -82465: InstSpec:  show(+String,any,+String):any\n"+
			"Engine =>   -83009: InstSpec:  quit(any):any\n"+
			"Engine =>   -83025: InstSpec: =plus(Number...):Number\n"+
			"Engine =>   -83233: InstSpec: =type(+TypeRef):Type\n"+
			"Engine =>   -83649: InstSpec:  stop(String,any):any\n"+
			"Engine =>   -98129: InstSpec: =isOne(Number):boolean\n"+
			"Engine =>  -101009: InstSpec: =toInt(Number):int\n"+
			"Engine =>  -102513: InstSpec:  which(boolean,Expression,Expression):any\n"+
			"Engine =>  -102945: InstSpec:  equal(any...):boolean\n"+
			"Engine =>  -103057: InstSpec:  stack(String){}:any\n"+
			"Engine =>  -103617: InstSpec:  while(String,Expression){}:boolean\n"+
			"Engine =>  -106609: InstSpec:  group(){}:any\n"+
			"Engine =>  -106881: InstSpec:  print(any):String\n"+
			"Engine =>  -108081: InstSpec:  throw(+Throwable):void\n"+
			"Engine =>  -119121: InstSpec: =charAt(+String,+int):char\n"+
			"Engine =>  -125665: InstSpec: =toLong(Number):long\n"+
			"Engine =>  -125937: InstSpec: =isNull(any):boolean\n"+
			"Engine =>  -125985: InstSpec: =divide(Number,Number):Number\n"+
			"Engine =>  -126145: InstSpec:  concat(any...):String\n"+
			"Engine =>  -126241: InstSpec: =toByte(Number):byte\n"+
			"Engine =>  -126577: InstSpec:  fromTo(String,+String,+Type,+Number,+Number,+Number){}:boolean\n"+
			"Engine =>  -126897: InstSpec: =isZero(Number):boolean\n"+
			"Engine =>  -127857: InstSpec:  choose(String,any,+CaseEntry[],Expression):any\n"+
			"Engine =>  -128257: InstSpec: =length(String):int\n"+
			"Engine =>  -128305: InstSpec:  repeat(String,Expression){}:boolean\n"+
			"Engine =>  -129617: InstSpec:  format(+String,any...):String\n"+
			"Engine =>  -129921: InstSpec:  equals(any...):boolean\n"+
			"Engine =>  -132113: InstSpec:  printf(+String,any...):String\n"+
			"Engine =>  -132433: InstSpec:  switch(String,any,+CaseEntry[],Expression):any\n"+
			"Engine =>  -133217: InstSpec:  unless(boolean){}:boolean\n"+
			"Engine =>  -134241: InstSpec:  return(any):void\n"+
			"Engine =>  -145329: InstSpec:  forEach(String,+String,+Type,any){}:boolean\n"+
			"Engine =>  -151073: InstSpec: =isArray(any):boolean\n"+
			"Engine =>  -151425: InstSpec: =toFloat(Number):float\n"+
			"Engine =>  -153041: InstSpec:  runOnce(){}:any\n"+
			"Engine =>  -153889: InstSpec:  runSelf():any\n"+
			"Engine =>  -154545: InstSpec:  compare(any,any):int\n"+
			"Engine =>  -155521: InstSpec:  tryCast(String,+String,+Type,any,Expression,Expression){}:any\n"+
			"Engine =>  -156225: InstSpec:  inequal(any...):boolean\n"+
			"Engine =>  -156497: InstSpec: =toShort(Number):short\n"+
			"Engine =>  -161009: InstSpec: =modulus(Number,Number):Number\n"+
			"Engine =>  -161313: InstSpec:  println(any):String\n"+
			"Engine =>  -171825: InstSpec: =isKindOf(+Type,Type):boolean\n"+
			"Engine =>  -173025: InstSpec:  hashCode(any):int\n"+
			"Engine =>  -173857: InstSpec:  callSelf(any...):any\n"+
			"Engine =>  -174689: InstSpec:  reCreate(Executable):any\n"+
			"Engine =>  -176593: InstSpec:  execSelf(any...):any\n"+
			"Engine =>  -177425: InstSpec:  toDetail(any):String\n"+
			"Engine =>  -179473: InstSpec: =toDouble(Number):double\n"+
			"Engine =>  -180161: InstSpec:  moreThan(any...):boolean\n"+
			"Engine =>  -180705: InstSpec:  lessThan(any...):boolean\n"+
			"Engine =>  -180929: InstSpec:  newArray(+Type,+int):any[]\n"+
			"Engine =>  -181297: InstSpec:  tryCatch(String,CatchEntry[],Expression){}:any\n"+
			"Engine =>  -185585: InstSpec:  toString(any):String\n"+
			"Engine =>  -186641: InstSpec:  inequals(any...):boolean\n"+
			"Engine =>  -187441: InstSpec:  continue(String,any):any\n"+
			"Engine =>  -189057: InstSpec: =subtract(Number,Number):Number\n"+
			"Engine =>  -193217: InstSpec: =multiply(Number...):Number\n"+
			"Engine =>  -202833: InstSpec: =charToInt(char):int\n"+
			"Engine =>  -204097: InstSpec: =intToChar(int):char\n"+
			"Engine =>  -204689: InstSpec: =isArrayOf(Type,any):boolean\n"+
			"Engine =>  -205137: InstSpec: =getEngine():Engine\n"+
			"Engine =>  -207009: InstSpec: =getTypeOf(any):Type\n"+
			"Engine =>  -209089: InstSpec: =isNotNull(any):boolean\n"+
			"Engine =>  -210465: InstSpec: =shiftLeft(Number,Number):Number\n"+
			"Engine =>  -211377: InstSpec:  tryOrElse(Expression,Expression):any\n"+
			"Engine =>  -213953: InstSpec: =tryNoNull(any,Type):any\n"+
			"Engine =>  -230625: InstSpec:  call_Blind(any,any...):any\n"+
			"Engine =>  -233649: InstSpec:  exec_Blind(any,any...):any\n"+
			"Engine =>  -234993: InstSpec:  castOrElse(+Type,any){}:any\n"+
			"Engine =>  -239361: InstSpec:  getVarType(+String):Type\n"+
			"Engine =>  -239777: InstSpec: =isNegative(Number):boolean\n"+
			"Engine =>  -240001: InstSpec:  isVarExist(+String):boolean\n"+
			"Engine =>  -240081: InstSpec: =isMinusOne(Number):boolean\n"+
			"Engine =>  -242705: InstSpec: =instanceOf(+Type,any):boolean\n"+
			"Engine =>  -244273: InstSpec: =shiftRight(Number,Number):Number\n"+
			"Engine =>  -244289: InstSpec:  run_Unsafe(any):any\n"+
			"Engine =>  -244513: InstSpec:  isConstant(+String):boolean\n"+
			"Engine =>  -245441: InstSpec:  newClosure(Executable):SubRoutine\n"+
			"Engine =>  -247553: InstSpec: =isPositive(Number):boolean\n"+
			"Engine =>  -250113: InstSpec:  assignment(+int,+int,any,any...):any\n"+
			"Engine =>  -264097: InstSpec: =isDataArray(any):boolean\n"+
			"Engine =>  -266337: InstSpec: =isJavaArray(any):boolean\n"+
			"Engine =>  -269585: InstSpec:  getVarValue(+String):any\n"+
			"Engine =>  -272417: InstSpec: =getTypeInfo(+Type,+String):any\n"+
			"Engine =>  -273425: InstSpec:  setVarValue(+String,any):any\n"+
			"Engine =>  -273441: InstSpec:  newVariable(+String,+Type,any):any\n"+
			"Engine =>  -276625: InstSpec:  getIterator(Iterable):Iterator\n"+
			"Engine =>  -276657: InstSpec:  newInstance(+Type,any...):any\n"+
			"Engine =>  -279649: InstSpec:  newConstant(+String,+Type,any):any\n"+
			"Engine =>  -293761: InstSpec: =toBigDecimal(Number):BigDecimal\n"+
			"Engine =>  -299713: InstSpec:  doWhenNoNull(any,Expression,Type):any\n"+
			"Engine =>  -300689: InstSpec: =toBigInteger(Number):BigInteger\n"+
			"Engine =>  -308065: InstSpec: =getArrayType(+Type,+int):Type\n"+
			"Engine =>  -311617: InstSpec:  printNewLine():String\n"+
			"Engine =>  -314577: InstSpec:  newThrowable(+String,Class,Throwable):Throwable\n"+
			"Engine =>  -319489: InstSpec:  iteratorNext(Iterator):any\n"+
			"Engine =>  -335905: InstSpec: =getEngineInfo(+String):any\n"+
			"Engine =>  -342337: InstSpec:  moreThanEqual(any...):boolean\n"+
			"Engine =>  -343201: InstSpec:  lessThanEqual(any...):boolean\n"+
			"Engine =>  -351361: InstSpec:  getTypeStatus(+Type,+String):any\n"+
			"Engine =>  -367153: InstSpec: =isKindOf_Array(Type):boolean\n"+
			"Engine =>  -375265: InstSpec: =getTypeOfClass(Class):Type\n"+
			"Engine =>  -383505: InstSpec:  getContextInfo(+String):any\n"+
			"Engine =>  -386721: InstSpec: =instanceOf_int(any):boolean\n"+
			"Engine =>  -405569: InstSpec:  isBeingDebugged():boolean\n"+
			"Engine =>  -407169: InstSpec: =isKindOf_Number(Type):boolean\n"+
			"Engine =>  -413921: InstSpec:  isLocalVarExist(+String):boolean\n"+
			"Engine =>  -415281: InstSpec:  readConsoleLine():String\n"+
			"Engine =>  -415793: InstSpec:  newArrayByClass(+Class,+int):any[]\n"+
			"Engine =>  -422577: InstSpec: =instanceOf_char(any):boolean\n"+
			"Engine =>  -423009: InstSpec:  newArrayLiteral(+Type,any...):any[]\n"+
			"Engine =>  -426321: InstSpec: =instanceOf_long(any):boolean\n"+
			"Engine =>  -426897: InstSpec: =instanceOf_byte(any):boolean\n"+
			"Engine =>  -427697: InstSpec:  arrayToIterator(any[]):Iterator\n"+
			"Engine =>  -428913: InstSpec:  iteratorHasNext(Iterator):boolean\n"+
			"Engine =>  -429537: InstSpec:  toDisplayString(any):String\n"+
			"Engine =>  -430881: InstSpec:  iteratorToArray(Iterator):any[]\n"+
			"Engine =>  -442081: InstSpec: =isKindOf_ArrayOf(Type,Type):boolean\n"+
			"Engine =>  -450129: InstSpec:  doWhenValidIndex(any[],int,Expression,Type):any\n"+
			"Engine =>  -454513: InstSpec:  isGlobalVarExist(+String):boolean\n"+
			"Engine =>  -455969: InstSpec:  isEngineVarExist(+String):boolean\n"+
			"Engine =>  -462177: InstSpec:  isParentVarExist(+int,+String):boolean\n"+
			"Engine =>  -493697: InstSpec:  getGlobalVarValue(+String):any\n"+
			"Engine =>  -495153: InstSpec:  getEngineVarValue(+String):any\n"+
			"Engine =>  -497009: InstSpec:  getArrayElementAt(+any[],+int):any\n"+
			"Engine =>  -498513: InstSpec:  newGlobalVariable(+String,+Type,any,+boolean):any\n"+
			"Engine =>  -498689: InstSpec:  setGlobalVarValue(+String,any):any\n"+
			"Engine =>  -500145: InstSpec:  setEngineVarValue(+String,any):any\n"+
			"Engine =>  -501361: InstSpec:  getParentVarValue(+int,+String):any\n"+
			"Engine =>  -502001: InstSpec:  setArrayElementAt(+any[],+int,any):any\n"+
			"Engine =>  -505265: InstSpec: =instanceOf_Number(any):boolean\n"+
			"Engine =>  -506353: InstSpec:  setParentVarValue(+int,+String,any):any\n"+
			"Engine =>  -508449: InstSpec: =instanceOf_String(any):boolean\n"+
			"Engine =>  -510017: InstSpec: =instanceOf_double(any):boolean\n"+
			"Engine =>  -547201: InstSpec: =getLengthArrayType(+net.nawaman.curry.TKArray.TArray):int\n"+
			"Engine =>  -554017: InstSpec: =instanceOf_boolean(any):boolean\n"+
			"Engine =>  -557201: InstSpec:  getExternalContext():net.nawaman.curry.ExternalContext\n"+
			"Engine =>  -557649: InstSpec: =shiftRightUnsigned(Number,Number):Number\n"+
			"Engine =>  -587889: InstSpec:  isGlobalVarConstant(+String):boolean\n"+
			"Engine =>  -588913: InstSpec:  sendDebuggerMessage(any,any...):any\n"+
			"Engine =>  -589585: InstSpec:  isEngineVarConstant(+String):boolean\n"+
			"Engine =>  -596753: InstSpec:  isParentVarConstant(+int,+String):boolean\n"+
			"Engine =>  -599825: InstSpec:  newArrayLiteral_any(any...):any[]\n"+
			"Engine =>  -600561: InstSpec:  newArrayLiteral_int(int...):int[]\n"+
			"Engine =>  -601665: InstSpec:  newBorrowedVariable(+String,+Type,any):any\n"+
			"Engine =>  -607873: InstSpec:  newBorrowedConstant(+String,+Type,any):any\n"+
			"Engine =>  -635073: InstSpec:  getLengthArrayObject(+any[]):int\n"+
			"Engine =>  -644801: InstSpec:  newArrayLiteral_char(char...):char[]\n"+
			"Engine =>  -645505: InstSpec:  newArrayLiteral_Type(Type...):Type[]\n"+
			"Engine =>  -648545: InstSpec:  newArrayLiteral_long(long...):long[]\n"+
			"Engine =>  -649121: InstSpec:  newArrayLiteral_byte(byte...):byte[]\n"+
			"Engine =>  -654945: InstSpec:  controlGlobalContext(+String):any\n"+
			"Engine =>  -665377: InstSpec: =isKindOf_CharSequence(Type):boolean\n"+
			"Engine =>  -690113: InstSpec:  newArrayLiteral_Class(Class...):Class[]\n"+
			"Engine =>  -691857: InstSpec:  newInstanceByTypeRefs(+Type,+TypeRef[],any...):any\n"+
			"Engine =>  -740977: InstSpec:  newInstanceByInterface(+Type,+net.nawaman.curry.ExecInterface,any...):any\n"+
			"Engine =>  -744257: InstSpec:  newArrayLiteral_Number(Number...):Number[]\n"+
			"Engine =>  -747441: InstSpec:  newArrayLiteral_String(String...):String[]\n"+
			"Engine =>  -747537: InstSpec: =getPrimitiveTypeByName(+String):Type\n"+
			"Engine =>  -749009: InstSpec:  newArrayLiteral_double(double...):double[]\n"+
			"Engine =>  -786801: InstSpec: =instanceOf_CharSequence(any):boolean\n"+
			"Engine =>  -794753: InstSpec:  newArrayLiteral_TypeRef(TypeRef...):TypeRef[]\n"+
			"Engine =>  -797105: InstSpec:  readConsolePasswordLine():String\n"+
			"Engine =>  -801393: InstSpec:  newArrayLiteral_boolean(boolean...):boolean[]\n"+
			"Engine =>  -917697: InstSpec: =getComponentTypeArrayType(+net.nawaman.curry.TKArray.TArray):Type\n"+
			"Engine => -1000337: InstSpec:  isVariableDefaultDataHolder(+String):boolean\n"+
			"Engine => -1010449: InstSpec:  isParentVarExistByStackName(+String,+String):boolean\n"+
			"Engine => -1029729: InstSpec: =getComponentTypeArrayObject(+any[]):Type\n"+
			"Engine => -1064417: InstSpec:  getParentVarValueByStackName(+String,+String):any\n"+
			"Engine => -1071521: InstSpec:  setParentVarValueByStackName(+String,+String,any):any\n"+
			"Engine => -1076097: InstSpec:  newArrayLiteral_CharSequence(CharSequence...):CharSequence[]\n"+
			"Engine => -1086337: InstSpec:  newArrayLiteral_Serializable(Serializable...):Serializable[]\n"+
			"Engine => -1177505: InstSpec:  checkVariableDataHolderFactory(+String,+String):boolean\n"+
			"Engine => -12008-201917: InstSpec:  isParentVarConstantByStackName(+String,+String):boolean\n",
			AllTests.getEngine().getInstructionsAsString(null, false)
		);
		
		// All instruction of Java Extension
		this.assertValue(
			"Owner =>     Hash: Specification\n"+
			"Java =>  -295297: InstSpec: =getJavaField(+Class,+String,+boolean):java.lang.reflect.Field\n"+
			"Java =>  -366993: InstSpec: =getJavaClassOf(+any):Class\n"+
			"Java =>  -527217: InstSpec: =getJavaClassByName(+String):Class\n"+
			"Java =>  -673505: InstSpec: =getJavaMethodByParams(+Class,+String,+boolean,any...):java.lang.reflect.Method\n"+
			"Java =>  -689073: InstSpec:  invokeJavaClassMethod(+Class,+String,any...):any\n"+
			"Java =>  -718961: InstSpec:  getJavaClassFieldValue(+Class,+String):any\n"+
			"Java =>  -724913: InstSpec:  setJavaClassFieldValue(+Class,+String,any):any\n"+
			"Java =>  -738593: InstSpec:  invokeJavaObjectMethod(+any,+String,any...):any\n"+
			"Java =>  -769377: InstSpec:  getJavaObjectFieldValue(+any,+String):any\n"+
			"Java =>  -775521: InstSpec:  setJavaObjectFieldValue(+any,+String,any):any\n"+
			"Java =>  -936193: InstSpec: =getJavaMethodByParamClasss(+Class,+String,+boolean,Class...):java.lang.reflect.Method\n"+
			"Java => -1103329: InstSpec:  getJavaClassFieldValueByField(+java.lang.reflect.Field):any\n"+
			"Java => -1110625: InstSpec:  setJavaClassFieldValueByField(+java.lang.reflect.Field,any):any\n"+
			"Java => -1133809: InstSpec:  invokeJavaClassMethodByMethod(+java.lang.reflect.Method,any...):any\n"+
			"Java => -1164609: InstSpec:  getJavaObjectFieldValueByField(+java.lang.reflect.Field,+any):any\n"+
			"Java => -1172097: InstSpec:  setJavaObjectFieldValueByField(+java.lang.reflect.Field,+any,any):any\n"+
			"Java => -1195745: InstSpec:  invokeJavaObjectMethodByMethod(+java.lang.reflect.Method,+any,any...):any\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_Java.Name))
		);
		
		// All instructions from StackOwner extension
		this.assertValue(
			"     Owner =>     Hash: Specification\n"+
		    "StackOwner =>  -130689: InstSpec:  invoke(any,+ExecSignature,any...):any\n"+
		    "StackOwner =>  -240817: InstSpec:  configAttr(any,+String,+String,any...):any\n"+
		    "StackOwner =>  -274401: InstSpec:  getAttrType(any,+String):any\n"+
		    "StackOwner =>  -306449: InstSpec:  getAttrValue(any,+String):any\n"+
		    "StackOwner =>  -310481: InstSpec:  setAttrValue(any,+String,any):any\n"+
		    "StackOwner =>  -311217: InstSpec:  invokeAsType(any,Type,+ExecSignature,any...):any\n"+
		    "StackOwner =>  -373889: InstSpec:  invokeByPTRefs(any,+String,+TypeRef[],any...):any\n"+
		    "StackOwner =>  -374177: InstSpec:  isAttrReadable(any,+String):any\n"+
		    "StackOwner =>  -383921: InstSpec:  invokeByParams(any,+String,any...):any\n"+
		    "StackOwner =>  -384369: InstSpec:  isAttrWritable(any,+String):any\n"+
		    "StackOwner =>  -415969: InstSpec:  getAttrMoreInfo(any,+String,+String):any\n"+
		    "StackOwner =>  -458689: InstSpec:  configAttrAsType(any,Type,+String,+String,any...):any\n"+
		    "StackOwner =>  -495761: InstSpec:  isAttrNoTypeCheck(any,+String):any\n"+
		    "StackOwner =>  -502641: InstSpec:  getAttrTypeAsType(any,Type,+String):any\n"+
		    "StackOwner =>  -507185: InstSpec:  invokeByInterface(any,+String,+net.nawaman.curry.ExecInterface,any...):any\n"+
		    "StackOwner =>  -543425: InstSpec:  getAttrValueAsType(any,Type,+String):any\n"+
		    "StackOwner =>  -548609: InstSpec:  setAttrValueAsType(any,Type,+String,any):any\n"+
		    "StackOwner =>  -627953: InstSpec:  isAttrReadableAsType(any,Type,+String):any\n"+
		    "StackOwner =>  -630961: InstSpec:  invokeAsTypeByPTRefs(any,Type,+String,+TypeRef[],any...):any\n"+
		    "StackOwner =>  -640993: InstSpec:  invokeAsTypeByParams(any,Type,+String,any...):any\n"+
		    "StackOwner =>  -642177: InstSpec:  isAttrWritableAsType(any,Type,+String):any\n"+
		    "StackOwner =>  -680785: InstSpec:  getAttrMoreInfoAsType(any,Type,+String,+String):any\n"+
		    "StackOwner =>  -778433: InstSpec:  isAttrNoTypeCheckAsType(any,Type,+String):any\n"+
		    "StackOwner =>  -792961: InstSpec:  invokeAsTypeByInterface(any,Type,+String,+net.nawaman.curry.ExecInterface,any...):any\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_StackOwner.Name))
		);

		// All instructions from StackOwnerCustomizable extension
		this.assertValue(
            "                 Owner =>     Hash: Specification\n"+
            "StackOwnerCustomizable =>  -285265: InstSpec:  this_invoke(+ExecSignature,any...):any\n"+
            "StackOwnerCustomizable =>  -288641: InstSpec:  type_invoke(+ExecSignature,any...):any\n"+
            "StackOwnerCustomizable =>  -325233: InstSpec:  super_invoke(+ExecSignature,any...):any\n"+
            "StackOwnerCustomizable =>  -429633: InstSpec:  this_configAttr(+String,+String,any...):any\n"+
            "StackOwnerCustomizable =>  -433649: InstSpec:  type_configAttr(+String,+String,any...):any\n"+
            "StackOwnerCustomizable =>  -471777: InstSpec:  this_getAttrType(+String):any\n"+
            "StackOwnerCustomizable =>  -475953: InstSpec:  type_getAttrType(+String):any\n"+
            "StackOwnerCustomizable =>  -512385: InstSpec:  this_getAttrValue(+String):any\n"+
            "StackOwnerCustomizable =>  -516417: InstSpec:  this_setAttrValue(+String,any):any\n"+
            "StackOwnerCustomizable =>  -516721: InstSpec:  type_getAttrValue(+String):any\n"+
            "StackOwnerCustomizable =>  -520753: InstSpec:  type_setAttrValue(+String,any):any\n"+
            "StackOwnerCustomizable =>  -596945: InstSpec:  this_invokeByPTRefs(+String,+TypeRef[],any...):any\n"+
            "StackOwnerCustomizable =>  -597233: InstSpec:  this_isAttrReadable(+String):any\n"+
            "StackOwnerCustomizable =>  -601601: InstSpec:  type_invokeByPTRefs(+String,+TypeRef[],any...):any\n"+
            "StackOwnerCustomizable =>  -601889: InstSpec:  type_isAttrReadable(+String):any\n"+
            "StackOwnerCustomizable =>  -606977: InstSpec:  this_invokeByParams(+String,any...):any\n"+
            "StackOwnerCustomizable =>  -607425: InstSpec:  this_isAttrWritable(+String):any\n"+
            "StackOwnerCustomizable =>  -611633: InstSpec:  type_invokeByParams(+String,any...):any\n"+
            "StackOwnerCustomizable =>  -612081: InstSpec:  type_isAttrWritable(+String):any\n"+
            "StackOwnerCustomizable =>  -647585: InstSpec:  this_getAttrMoreInfo(+String,+String):any\n"+
            "StackOwnerCustomizable =>  -652145: InstSpec:  super_invokeByPTRefs(+String,+TypeRef[],any...):any\n"+
            "StackOwnerCustomizable =>  -652401: InstSpec:  type_getAttrMoreInfo(+String,+String):any\n"+
            "StackOwnerCustomizable =>  -662177: InstSpec:  super_invokeByParams(+String,any...):any\n"+
            "StackOwnerCustomizable =>  -744497: InstSpec:  this_isAttrNoTypeCheck(+String):any\n"+
            "StackOwnerCustomizable =>  -749633: InstSpec:  type_isAttrNoTypeCheck(+String):any\n"+
            "StackOwnerCustomizable =>  -755921: InstSpec:  this_invokeByInterface(+String,+net.nawaman.curry.ExecInterface,any...):any\n"+
            "StackOwnerCustomizable =>  -761057: InstSpec:  type_invokeByInterface(+String,+net.nawaman.curry.ExecInterface,any...):any\n"+
            "StackOwnerCustomizable =>  -807201: InstSpec:  this_initialize_ByTRefs(TypeRef[],any...):void\n"+
            "StackOwnerCustomizable =>  -816833: InstSpec:  super_invokeByInterface(+String,+net.nawaman.curry.ExecInterface,any...):any\n"+
            "StackOwnerCustomizable =>  -866689: InstSpec:  this_initialize_ByParams(any...):void\n"+
            "StackOwnerCustomizable =>  -870017: InstSpec:  super_initialize_ByTRefs(TypeRef[],any...):void\n"+
            "StackOwnerCustomizable =>  -931409: InstSpec:  super_initialize_ByParams(any...):void\n"+
            "StackOwnerCustomizable => -1040449: InstSpec:  this_initialize_ByInterface(+net.nawaman.curry.ExecInterface,any...):void\n"+
            "StackOwnerCustomizable => -1110881: InstSpec:  super_initialize_ByInterface(+net.nawaman.curry.ExecInterface,any...):void\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_StackOwnerCustomizable.Name))
		);

		// All instructions from Unit extension
		this.assertValue(
			"Owner =>     Hash: Specification\n"+
			"Unit =>  -233057: InstSpec: =getPackage(+String):Package\n"+
			"Unit =>  -346289: InstSpec: =ensurePackage(+String):Package\n"+
			"Unit =>  -378465: InstSpec: =getUnitManager():net.nawaman.curry.MUnit\n"+
			"Unit =>  -384497: InstSpec:  package_invoke(+ExecSignature,any...):any\n"+
			"Unit =>  -503457: InstSpec: =getCurrentPackage():Package\n"+
			"Unit =>  -546529: InstSpec:  package_configAttr(+String,+String,any...):any\n"+
			"Unit =>  -593089: InstSpec:  package_getAttrType(+String):any\n"+
			"Unit =>  -638113: InstSpec:  package_getAttrValue(+String):any\n"+
			"Unit =>  -642145: InstSpec:  package_setAttrValue(+String,any):any\n"+
			"Unit =>  -731505: InstSpec:  package_invokeByPTRefs(+String,+TypeRef[],any...):any\n"+
			"Unit =>  -731793: InstSpec:  package_isAttrReadable(+String):any\n"+
			"Unit =>  -741537: InstSpec:  package_invokeByParams(+String,any...):any\n"+
			"Unit =>  -741985: InstSpec:  package_isAttrWritable(+String):any\n"+
			"Unit =>  -786561: InstSpec:  package_getAttrMoreInfo(+String,+String):any\n"+
			"Unit =>  -892305: InstSpec:  package_isAttrNoTypeCheck(+String):any\n"+
			"Unit =>  -903729: InstSpec:  package_invokeByInterface(+String,+net.nawaman.curry.ExecInterface,any...):any\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_Unit.Name))
		);

		// All instructions from DefaultPackage extension
		this.assertValue(
			"         Owner =>     Hash: Specification\n"+
			"DefaultPackage =>  -493009: InstSpec: =getDefaultPackage():Package\n"+
			"DefaultPackage =>  -668705: InstSpec:  addDefaultPackageType(net.nawaman.curry.Accessibility,+String,+TypeSpec):boolean\n"+
			"DefaultPackage =>  -831041: InstSpec: =getDefaultPackageBuilder():net.nawaman.curry.DefaultPackageBuilder\n"+
			"DefaultPackage =>  -873553: InstSpec:  addDefaultPackageVariable(net.nawaman.curry.Accessibility,net.nawaman.curry.Accessibility,net.nawaman.curry.Accessibility,+String,+boolean,+Type,Serializable,+boolean,MoreData,net.nawaman.curry.Location,MoreData):boolean\n"+
			"DefaultPackage =>  -879281: InstSpec:  addDefaultPackageFunction(net.nawaman.curry.Accessibility,+SubRoutine,MoreData):boolean\n"+
			"DefaultPackage =>  -979185: InstSpec:  addDefaultPackageDataHolder(net.nawaman.curry.Accessibility,net.nawaman.curry.Accessibility,net.nawaman.curry.Accessibility,+String,+boolean,+net.nawaman.curry.util.DataHolderInfo,net.nawaman.curry.Location,MoreData):boolean\n"+
			"DefaultPackage => -1047409: InstSpec:  bindDefaultPackageDataHolder(net.nawaman.curry.Accessibility,net.nawaman.curry.Accessibility,net.nawaman.curry.Accessibility,+String,+boolean,+DataHolder,net.nawaman.curry.Location,MoreData):boolean\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_DefaultPackage.Name))
		);

		// All instructions from DataHolder extension
		this.assertValue(
			"     Owner =>     Hash: Specification\n"+
			"DataHolder =>   -91985: InstSpec:  newDH(+net.nawaman.curry.util.DataHolderInfo,any):DataHolder\n"+
			"DataHolder =>  -169697: InstSpec:  configDH(+DataHolder,+String,any...):any\n"+
			"DataHolder =>  -195857: InstSpec:  getDHType(+DataHolder):Type\n"+
			"DataHolder =>  -223569: InstSpec:  getDHValue(+DataHolder):any\n"+
			"DataHolder =>  -227217: InstSpec:  setDHValue(+DataHolder,any):any\n"+
			"DataHolder =>  -281489: InstSpec:  isDHReadable(+DataHolder):boolean\n"+
			"DataHolder =>  -291681: InstSpec:  isDHWritable(+DataHolder):boolean\n"+
			"DataHolder =>  -32008-20191: InstSpec:  getDHMoreInfo(+DataHolder,+String):any\n"+
			"DataHolder =>  -390065: InstSpec:  isDHNoTypeCheck(+DataHolder):boolean\n"+
			"DataHolder => -1028865: InstSpec:  addDataHolderAsLocalVariable(+String,+DataHolder):DataHolder\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_DataHolder.Name))
		);

		// All instructions from AdvanceLoop extension
		this.assertValue(
			"      Owner =>     Hash: Specification\n"+
			"AdvanceLoop =>  -267009: InstSpec:  for_Advance(String,Expression,Expression,Expression,Expression,Expression){}:any\n"+
			"AdvanceLoop =>  -340145: InstSpec:  while_Advance(String,Expression,Expression,Expression,Expression){}:any\n"+
			"AdvanceLoop =>  -375137: InstSpec:  fromTo_Advance(String,+String,+Type,+Number,+Number,+Number,Expression,Expression,Expression){}:any\n"+
			"AdvanceLoop =>  -378145: InstSpec:  repeat_Advance(String,Expression,Expression,Expression,Expression){}:any\n"+
			"AdvanceLoop =>  -402209: InstSpec:  forEach_Advance(String,+String,+Type,any,Expression,Expression,Expression){}:any\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_AdvanceLoop.Name))
		);

		// All instructions from Enum extension
		this.assertValue(
			"Owner =>     Hash: Specification\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_Enum.Name))
		);

		// All instructions from Object extension
		this.assertValue(
			" Owner =>     Hash: Specification\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_Object.Name))
		);

		// All instructions from Object extension
		this.assertValue(
			"   Owner =>     Hash: Specification\n" +
			"Language =>  -540593: InstSpec: =getDefaultLanguage():net.nawaman.curry.compiler.CurryLanguage\n",
			AllTests.getEngine().getInstructionsAsString(AllTests.getEngine().getExtension(EE_Language.Name))
		);
	}
}
