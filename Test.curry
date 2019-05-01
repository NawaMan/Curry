// @Curry: { main }

@:println("Hello World!!!");
@:println();

// The parameter count
@:println("$Args.length: " + $Args?.length);

// The each parameter
foreach(String $Arg : $Args)
	@:printf("$Args[%s]: %s\n", $Count$, $Args?[$Count$]);
@:println();


/*
@@:Group @@Java:{
	import net.nawaman.script.*;
	
	ScriptManager.Usepaths.registerUsepath("tests");
}:Java:;

// This is to ensure that nawaman~>Display is sure to be loaded (or successfully compiled if that is needed) before use.
@:println(@:getPackage("nawaman~>Display"));

@@:Group {
	@:println("Curry: Hello World!!!");
	@:println("Curry: Hi    World!!!");
	
	@@:Group @@Java:{
		System.out.println("Java: Hello World!!!");
		System.out.println("Java: Hi    World!!!");
		System.out.println("Java: Hey   World!!!");
	}:Java:;
	
	@:printf("Here: %s\n", @:getPackage("nawaman~>Display"));
	@:invokeByParams(@:getPackage("nawaman~>Display"), "ShowList", null);
	@:println("Here");
		
	@:return(null);
};

*/

