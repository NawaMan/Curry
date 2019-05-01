package net.nawaman.curry.compiler;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.Engine;
import net.nawaman.curry.UnitBuilder;
import net.nawaman.curry.UnitBuilders;

public interface UnitBuilderCreator {
	
	// Built-ins
	static public final UnitBuilderCreator Memory = new UBCMemory(); 
	static public final UnitBuilderCreator File   = new UBCFile();
	
	public UnitBuilder createUnitBuilder(Engine pEngine, Object pSecretID, CodeFeeder pCodeFeeder);
	
	
	// Simple implementation -------------------------------------------------------------------------------------------
	
	static class UBCMemory implements UnitBuilderCreator {
		public UnitBuilder createUnitBuilder(Engine pEngine, Object pSecretID, CodeFeeder pCodeFeeder) {
			return new UnitBuilders.UBMemory(pEngine, pCodeFeeder.getFeederName(), pSecretID, pCodeFeeder);
		}
	}
	
	static class UBCFile implements UnitBuilderCreator {
		public UnitBuilder createUnitBuilder(Engine pEngine, Object pSecretID, CodeFeeder pCodeFeeder) {
			return new UnitBuilders.UBFile(pEngine, pCodeFeeder.getBase(), pCodeFeeder.getFeederName(), pSecretID, pCodeFeeder);
		}
	}
	
}
