package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UArray;

/** Documentation for Type and other elements */
public interface Documentation extends Serializable {
	
	static public final String MIName_Documentation = "Documentation";
	
	/** Returns the document as plain text */
	public String getAsPlainText();
	
	// Sub-classes -----------------------------------------------------------------------------------------------------
	
	static public class Util {
		
		/** Creates a MoreData that hold a documentation text */
		static public MoreData NewMoreData(String pDocumentationText) {
			return NewMoreData(new Documentation.Simple(pDocumentationText));
		}
		
		/** Creates a MoreData that hold a documentation text */
		static public MoreData NewMoreData(Documentation pDocumentation) {
			return new MoreData(Documentation.MIName_Documentation, pDocumentation);
		}
		
		/** Returns the Documentation of the executable */
		static public Documentation getDocumentationOf(Executable Exec) {
			if(Exec == null) return null;
			return getDocumentationOf(Exec.getSignature());
		}
		/** Returns the Documentation of the executable signature */
		static public Documentation getDocumentationOf(ExecSignature Signature) {
			if(Signature == null) return null;
			return getDocumentationOf(Signature.getExtraData());
		}
		/** Returns the Documentation of the moredata */
		static public Documentation getDocumentationOf(MoreData MD) {
			if(MD == null) return null;
			Object O = MD.getData(MIName_Documentation);
			if(!(O instanceof Documentation)) return null;
			return (Documentation)O;
		}
		
	}
	
	/** A simple documentation */
	static public class Simple implements Documentation {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		// Constructors ------------------------------------------------------------------------------------------------
		
		public Simple(String pText) {
			this(null, null, pText);
		}
		public Simple(String pKind, String pText) {
			this(pKind, null, pText);
		}
		public Simple(String pKind, Serializable[] pParams, String pText) {
			this.Kind   = pKind;
			this.Params = (pParams == null)?null:pParams.clone();
			this.Text   = pText;
		}
		
		final String         Kind;
		final Serializable[] Params;
		final String         Text;
		
		/**{@inheritDoc}*/ @Override
		public String getAsPlainText() {
			return this.Text;
		}
		
		/**{@inheritDoc}*/ @Override
		public String toString() {
			return String.format(
					"<?%s%s%s[---\n%s\n---]?>",
					(this.Kind   == null)?"":String.format("@%s",  this.Kind),
					(this.Params == null)?"":String.format("(%s)", UArray.toString(this.Params, "", "", ",")),
					((this.Kind == null) && (this.Params == null))?"":":",
					this.Text);
		}
	} 
	
}
