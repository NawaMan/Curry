package net.nawaman.curry.script;

import java.util.Vector;

import net.nawaman.compiler.CodeFeeders;
import net.nawaman.compiler.CompilerMessage;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.script.Problem;
import net.nawaman.script.ProblemContainer;

public class CurryProblem implements Problem {

	static class CurryProblemContainer extends ProblemContainer {
		void            setProblems(Vector<Problem> pProblems) { super.Problems = pProblems; }          
		Vector<Problem> getProblems()                          { return super.Problems; }
	} 

	static void SetProblemContainer(CurryProblemContainer pCPContianer, CompileProduct pCProduct) {
		if((pCPContianer == null) || (pCProduct == null) || (pCProduct.getMessageCount() == 0)) return;
		if(pCPContianer.getProblems() == null) pCPContianer.setProblems(new Vector<Problem>());
		for(int i = 0; i < pCProduct.getMessageCount(); i++)
			pCPContianer.getProblems().add(new CurryProblem(pCProduct, i));
	}

	CurryProblem(CompileProduct pCProduct, int pPIndex) {
		this.CProduct = pCProduct;
		this.PIndex   = pPIndex;
	}

	CompileProduct CProduct = null;
	int            PIndex   = -1;

	/**{@inheritDoc}*/@Override
	public String getCodeName() {
		CompilerMessage CMsg = this.CProduct.getMessage(this.PIndex);
		if(CMsg == null) return null;
		
		return CMsg.getCodeName();
	}
	/**{@inheritDoc}*/@Override
	public String getCode() {
		CompilerMessage CMsg = this.CProduct.getMessage(this.PIndex);
		if(CMsg == null) return null;
		
		this.CProduct.getCodeData(CMsg.getCodeFeederIndex(), CMsg.getCodeName(), CodeFeeders.DataName_SourceCode);
		return null;
	}

	/**{@inheritDoc}*/@Override
	public Problem.Kind getKind() {
		CompilerMessage CMsg = this.CProduct.getMessage(this.PIndex);
		if(CMsg == null) return null;
		
		if(CMsg.isMessage())    return Problem.Kind.Note;
		if(CMsg.isFatalError()) return Problem.Kind.Error;
		if(CMsg.isError())      return Problem.Kind.Error;
		if(CMsg.isWarning())    return Problem.Kind.Warning;
		return Problem.Kind.Note;
	}

	/**  Returns a localized message. */
	public String getMessage() {
		CompilerMessage CMsg = this.CProduct.getMessage(this.PIndex);
		if(CMsg == null) return null;
		
		return CompileProduct.RetrieveMessage(CMsg.getMessage());
	}

	/**{@inheritDoc}*/@Override
	public int getStartPosition() {
		CompilerMessage CMsg = this.CProduct.getMessage(this.PIndex);
		if(CMsg == null) return Problem.NoPosition;
		return CMsg.getPosition();
	}
	/**{@inheritDoc}*/@Override
	public int getEndPosition() {
		return this.getStartPosition();
	}
	/**{@inheritDoc}*/@Override 
	public int getLineNumber() {
		CompilerMessage CMsg = this.CProduct.getMessage(this.PIndex);
		if(CMsg == null) return Problem.NoPosition;
		return CMsg.getLineNumber();
	}
	/**{@inheritDoc}*/@Override
	public int getColumnNumber() {
		CompilerMessage CMsg = this.CProduct.getMessage(this.PIndex);
		if(CMsg == null) return Problem.NoPosition;
		return CMsg.getColumnNumber();
	}

}
