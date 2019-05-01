// @Curry:

@@:Package(nawaman~>DataStructure);

<?{ This is a function returns a new list }?>
@@:Sub newList():java.util.List {
	//@:printf("Here: %S\n", @:newInstance(java.util.Vector.type));
	@:return(@:newInstance(java.util.Vector.type));
};

@@:TypeDef public interface List <ValueType:any> extends java.util.List {
	@@:Method public Sub set(I:int, Value:ValueType):ValueType;
	@@:Method public Sub get(I:int):ValueType;
	@@:Method public Sub length():int;
};