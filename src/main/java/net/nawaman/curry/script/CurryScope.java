package net.nawaman.curry.script;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

public class CurryScope implements net.nawaman.script.Scope {
	
	public CurryScope(net.nawaman.curry.Engine pTheEngine, net.nawaman.curry.Scope pTheScope) {
		this.TheEngine = pTheEngine;
		this.TheScope  = pTheScope;
	}
	
	net.nawaman.curry.Engine TheEngine = null;
	net.nawaman.curry.Scope  TheScope  = null;
	
	/**
	 * Returns a variable and constant names
	 * 
	 * This method is just to satisfy net.nawaman.script.Scope. Its implementation is heavy so it is not recommend that
	 *     this method to be used in the job where performance is needed.
	 * The reason for this is that Curry Scope may have parent. Listing all the variable means to includes all the
	 *     variables in the parent and their parents.
	 **/
	public Set<String> getVariableNames() {
		return net.nawaman.curry.Scope.getVariableNamesOf(this.TheScope);
	}
	
	/**
	 * Returns the variable count
	 * 
	 * This method is just to satisfy net.nawaman.script.Scope. Its implementation is heavy so it is not recommend that
	 *     this method to be used in the job where performance is needed.
	 * The reason for this is that Curry Scope may have parent. Listing all the variable means to includes all the
	 *     variables in the parent and their parents.
	 * NOTE: This method use Scope.StandAlone.getVariableNames(); therefore, if you need to also get variable names
	 *       user the one above and ask its for the size instead of calling this method.
	 **/
	public int getVarCount() {
		Set<String> S = net.nawaman.curry.Scope.getVariableNamesOf(this.TheScope);
		return (S == null)?0:S.size();
	}
	
	/**{@inheritDoc}*/@Override
	public Object getValue(String pName) {
		return this.TheScope.getValue(this.TheEngine, pName);
	}
	/**{@inheritDoc}*/@Override
	public Object setValue(String pName, Object pValue) {
		return this.TheScope.setValue(this.TheEngine, pName, pValue);
	}
	
	/**{@inheritDoc}*/@Override
	public boolean newVariable(String pName, Class<?> pType, Object pValue) {
		if(this.isExist(pName)) return false;
		try { this.TheScope.newVariable(pName, this.TheEngine.getTypeManager().getTypeOfTheInstanceOf(pType), pValue); return true; }
		catch (Exception E) { return false; }
	}
	/**{@inheritDoc}*/@Override
	public boolean newConstant(String pName, Class<?> pType, Object pValue) {
		if(this.isExist(pName)) return false;
		try { this.TheScope.newConstant(pName, this.TheEngine.getTypeManager().getTypeOfTheInstanceOf(pType), pValue); return true; }
		catch (Exception E) { return false; }
	}
	
	/**{@inheritDoc}*/@Override
	public boolean removeVariable(String pName) {
		return this.TheScope.removeVariable(pName);
	}
	
	/**{@inheritDoc}*/@Override
	public Class<?> getTypeOf(String pName) {
		return this.TheScope.getType(this.TheEngine, pName).getTypeInfo().getDataClass();
	}
	/**{@inheritDoc}*/@Override
	public boolean isExist(String pName) {
		return this.TheScope.isVariableExist(pName);
	}
	/**{@inheritDoc}*/@Override
	public boolean isWritable(String pName) {
		return !this.TheScope.isVariableConstant(pName);
	}
	
	/** Checks if this scope support constant declaration */
	public boolean isConstantSupport() { return true; }
	
	Writer Out = new OutputStreamWriter(System.out);
	Writer Err = new OutputStreamWriter(System.err);
	Reader In  = new InputStreamReader( System.in);
    
	/**{@inheritDoc}*/ @Override
	public void setWriter(Writer pWriter) {
		this.Err = (pWriter != null)?pWriter:Simple.DErr;
	}
    /**{@inheritDoc}*/ @Override
    public void setErrorWriter(Writer pWriter) {
    	this.Err = (pWriter != null)?pWriter:Simple.DErr;
    }
    /**{@inheritDoc}*/ @Override
    public void setReader(Reader pReader) {
    	this.In  = (pReader != null)?pReader:Simple.DIn;
    }

    /**{@inheritDoc}*/ @Override
    public Writer getWriter() {
    	return Out;
    }
    /**{@inheritDoc}*/ @Override
    public Writer getErrorWriter() {
    	return Err;
    }
    /**{@inheritDoc}*/ @Override
    public Reader getReader() {
    	return In;
    }
    
}