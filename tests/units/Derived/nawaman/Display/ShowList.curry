// @Curry:

@@:Package(nawaman~>Display);

<?{ This is a function returns a new list }?>
@@:Sub ShowList(List:java.util.List):String {
	// @:println("I am here in nawaman~>Display=>ShowList");
	@:if(@:isNull(@:getVarValue(`List`))) {
		@:setVarValue(`List`, @:invokeByParams(@:getPackage("nawaman~>DataStructure"), "newList"));
	};
	@:show(`List: `, @:getVarValue(`List`), `\n`);
	@:return(``);
};