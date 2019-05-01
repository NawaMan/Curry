package net.nawaman.curry.util;

import java.io.IOException;
import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nawaman.curry.Engine;

/** Adaptor for formating object to string (without Engine operation) */
public class FormattableAdaptor implements Formattable {
	
	/** Format kind */
	static enum ObjectFormatKind {
		ToString,
		ToDetail,
		ToDisplay;
		
		/** Do the formating */
		public String format(Engine pEngine, Object O) {
			if(this == ToDetail)  return pEngine.toDetail(O);
			if(this == ToDisplay) return pEngine.getDisplayObject(O);
			return pEngine.toString(O);
		}
	};

	public FormattableAdaptor(Engine pEngine, Object pObj) {
		this(pEngine, pObj, false);
	}
	
	public FormattableAdaptor(Engine pEngine, Object pObj, boolean pIsDetail) {
		this.Engine   = pEngine;
		this.Obj      = pObj;
		this.IsDetail = pIsDetail;
	}
	
	Object  Obj;
	Engine  Engine;
	boolean IsDetail;
	
	public void formatTo(Formatter formatter, int flags, int width, int precision) {
		if(this.Obj instanceof Formattable) {
			((Formattable)this.Obj).formatTo(formatter, flags, width, precision);
			return;
		}
		String S = this.IsDetail?this.Engine.toDetail(this.Obj):this.Engine.toString(this.Obj);
		if((precision >= 0) && (precision < S.length())) S = S.substring(0, precision);

		StringBuffer SB = new StringBuffer();
		if(width != -1) { while(SB.length() < (width - S.length())) { SB.append(" "); } }
		
		if((FormattableFlags.LEFT_JUSTIFY & flags) != 0) S = S + SB.toString();
		else                                             S = SB.toString() + S;
		
		if((FormattableFlags.UPPERCASE & flags) != 0) S = S.toUpperCase();
		
		try {
			formatter.out().append(S);
			formatter.flush();
		} catch(IOException IOE) {}
		
		return;
	}
	
	// Performing the format -------------------------------------------------------------------------------------------

	static private boolean doCheckText(String s) {
		@SuppressWarnings("unused")
		int idx;
		// If there are any '%' in the given string, we got a bad format specifier.
		if ((idx = s.indexOf('%')) != -1) return false;
		return true;
	}
	
	// %[argument_index$][flags][width][.precision][t]conversion
	private static final String  formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
	private static       Pattern fsPattern       = Pattern.compile(formatSpecifier);

	/** perform the format */
	static public String doFormat(Engine pEngine, String pPattern, Object[] pVars) {
		if(pPattern          == null) return null;
		if(pPattern.length() ==    0) return   "";
		
		Matcher m = fsPattern.matcher(pPattern);
		int     p = 0;
		int     i = 0;
		while (i < pPattern.length()) {
			if (m.find(i)) {
				// Make sure we didn't miss any invalid format specifiers
				if(!doCheckText(pPattern.substring(i, m.start()))) break;
				
				// Replace if needed.
				char c = m.group(m.groupCount()).charAt(0);
				switch(c) {
					case 's': pVars[p] = new FormattableAdaptor(pEngine, pVars[p], false);
					case 'S': pVars[p] = new FormattableAdaptor(pEngine, pVars[p], true);
				}
			
				// Assume previous characters were fixed text
				p++;
				i = m.end();
			} else break;
		}
		
		// Search for %s
		return String.format(pPattern, (Object[])pVars);
	} 

}
