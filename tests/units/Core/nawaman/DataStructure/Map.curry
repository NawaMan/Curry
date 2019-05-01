// @Curry:

@@:Package(nawaman~>DataStructure);

<?{ This is a function returns a new map }?>
@@:Sub newMap():java.util.Map {
	@:return(@:newInstance(java.util.HashMap.type));
};