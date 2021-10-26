package net.nawaman.curry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.nawaman.curry.JavaExecutable.JavaFragment_Simple;
import net.nawaman.curry.JavaExecutable.JavaSubRoutine_Simple;
import net.nawaman.util.UClass;
import net.nawaman.util.UObject;

public class NativeExecutable {

	// Field Read ------------------------------------------------------------------

	static class JavaFieldRead extends JavaFragment_Simple implements Executable.Fragment {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		JavaFieldRead(Engine pEngine, Field pField) {
			super(pEngine, ExecSignature.newSignature_ReadField(pEngine, pField), null, null);
			this.Field = pField;
			if(this.Field == null)
				throw new NullPointerException("Null field for a native " + (this.isStatic()?"class":"object")
						+ " field-read executable.");

			if(UClass.isMemberStatic(this.Field) != isStatic())
				throw new IllegalArgumentException("A native " + (this.isStatic()?"class":"object")
						+ " field read executable cannot be "+ (this.isStatic()?"non-":"") +"static.");

			if((this.getObject() != null) && this.Field.getDeclaringClass().isInstance(this.getObject()))
				throw new IllegalArgumentException("A native " + (this.isStatic()?"class":"object")
						+ " field read executable cannot be used with the giving object.");
		}
		final Field Field;
		boolean isStatic() { return true; }
		Object getObject() { return null; }
		// Clone -------------------------------------------------------------------
		@Override final public JavaFieldRead clone() { return this; }
		// Objectable --------------------------------------------------------------
		@Override protected int    getBodyHash() { return UObject.hash(this.getObject()) + UObject.hash(this.Field); } 
		@Override protected String getBodyStr(Engine pEngine) { return "return " + this.Field.getName() + ";"; }
		// Execution ---------------------------------------------------------------
		/** Executing this -  For external*/
		@Override protected Object run() { return null; } // Dummy
		@Override Object run(Context pContext, Object[] pParams) {
			try {
				return UClass.getFieldValue(this.Field, this.getObject());
			} catch(Throwable T) {
				Engine E = pContext.getEngine();
				throw new CurryError(
						"An error occur while reading " + this.Field.toString() + " of " + 
						((this.getObject() == null)
							? E.getDisplayObject(this.getObject())
							: E.getTypeManager().getTypeOfTheInstanceOf(this.Field.getDeclaringClass()).toString()
						) + ".",
						pContext, T);
			}
		}
	}
	static final class NativeClassFieldRead extends JavaFieldRead {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		NativeClassFieldRead(Engine pEngine, Field pField) { super(pEngine, pField); }
	}
	static final class JavaObjectFieldRead extends JavaFieldRead {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		JavaObjectFieldRead(Engine pEngine, Object pObj, Field pField) {
			super(pEngine, pField);
			this.Obj = pObj;
			if(this.Obj   == null) throw new NullPointerException("Null object for a native object field read executable.");
		}
		final Object Obj;
		@Override boolean isStatic()  { return false;    }
		@Override Object  getObject() { return this.Obj; }
	}

	// Field Write -----------------------------------------------------------------

	static class JavaFieldWrite extends JavaSubRoutine_Simple {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		JavaFieldWrite(Engine pEngine, Field pField) {
			super(pEngine, ExecSignature.newSignature_WriteField(pEngine, pField), null, null);
			this.Field = pField;
			if(this.Field == null) throw new NullPointerException("Null field for a native "+(this.isStatic()?"class":"object")+" field-write executable.");

			if(UClass.isMemberStatic(this.Field) != isStatic())
				throw new IllegalArgumentException("A native "+(this.isStatic()?"class":"object")+" field write executable cannot be "+ (this.isStatic()?"non-":"") +"static.");

			if((this.getObject() != null) && this.Field.getDeclaringClass().isInstance(this.getObject()))
				throw new IllegalArgumentException("A native object field read executable cannot be used with the giving object.");
		}
		final Field Field;
		boolean isStatic() { return true; }
		Object getObject() { return null; }
		// Clone -------------------------------------------------------------------
		@Override final public JavaFieldWrite clone() { return this; }
		// Objectable --------------------------------------------------------------
		@Override protected int getBodyHash() {
			return UObject.hash(this.getObject()) + UObject.hash(this.Field);
		} 
		@Override protected String getBodyStr(Engine pEngine) { return "{ " + this.Field.getName() + " = <Value>; }"; }
		// Execution ---------------------------------------------------------------
		/** Executing this -  For external*/
		@Override protected Object run(Object[] pParams) { return null; } // Dummy
		/** Executing this -  For internal to change */
		@Override protected Object run(Context pContext, Object[] pParams) {
			try {
				return UClass.setFieldValue(this.Field, this.getObject(), pParams[0]);
			} catch(Throwable T) {
				Engine E = pContext.getEngine();
				throw new CurryError(
						"An error occur while writing " + this.Field.toString() + " of " + 
						((this.getObject() == null)
							? E.getDisplayObject(this.getObject())
							: E.getTypeManager().getTypeOfTheInstanceOf(this.Field.getDeclaringClass()).toString()
						) + ".",
						pContext, T);
			}
		}
	}
	static final class JavaClassFieldWrite extends JavaFieldWrite {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaClassFieldWrite(Engine pEngine, Field pField) { super(pEngine, pField); }
	}
	static final class JavaObjectFieldWrite extends JavaFieldWrite {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaObjectFieldWrite(Engine pEngine, Object pObj, Field pField) {
			super(pEngine, pField);
			this.Obj = pObj;
			if(this.Obj   == null) throw new NullPointerException("Null object for a native object field read executable.");
		}
		final Object Obj;
		@Override boolean isStatic()  { return false;    }
		@Override Object  getObject() { return this.Obj; }
	}

	// Method Invoke ---------------------------------------------------------------

	static abstract class JavaMethodInvoke extends JavaSubRoutine_Simple implements Executable.SubRoutine {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaMethodInvoke(Method pMethod, ExecSignature pSignature) {
			super(pSignature);

			String TargetKind = this.getTagetKind();
			
			this.Method = pMethod;
			if(this.Method == null)
				throw new NullPointerException("Null method for a native "+TargetKind+" method-invokation executable.");

			if((this.getObject() != null) && this.Method.getDeclaringClass().isInstance(this.getObject()))
				throw new IllegalArgumentException(
						"A native "+TargetKind+" field read executable cannot be used with the giving object.");
		}
		JavaMethodInvoke(Engine pEngine, Method pMethod) {
			this(pMethod, ExecSignature.newSignature(pEngine, pMethod));
		}
		
		final private Method Method;
		
		String getTagetKind() {
			return this.isStatic()?"class":"object";
		}
		/** Checks if the method is static */
		final boolean isStatic() {
			return (this.Method == null) || Modifier.isStatic(this.Method.getModifiers());
		}
		/** Returns the object for the access*/
		Object getObject() {
			return null;
		}
		
		/** Returns the method that this executable holds */
		Method getMethod() {
			return this.Method;
		}
		
		// Clone ------------------------------------------------------------------- 
		@Override final public JavaMethodInvoke clone() { return this; }
		// Objectable --------------------------------------------------------------
		@Override protected int getBodyHash() {
			return UObject.hash(this.getObject()) + UObject.hash(this.Method);
		} 
		@Override protected String getBodyStr(Engine pEngine) { return this.Method.getName() + "(...);"; }
		// Execution ---------------------------------------------------------------
		/** Executing this -  For external*/
		@Override protected Object run(Object[] pParams) { return null; } // Dummy
		/** Executing this -  For internal to change */
		@Override Object run(Context pContext, Object[] pParams) {
			try {
				return UClass.invokeMethod(this.Method, this.getObject(), pParams);
			} catch(Throwable T) {
				Engine E = pContext.getEngine();
				throw new CurryError(
						"An error occur while invoking " + this.Method.toString() + " of " + 
						((this.getObject() == null)
							? E.getDisplayObject(this.getObject())
							: E.getTypeManager().getTypeOfTheInstanceOf(this.Method.getDeclaringClass()).toString()
						) + ".",
						pContext, T);
			}
		}
	}
	static final class JavaClassMethodInvoke extends JavaMethodInvoke {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaClassMethodInvoke(Engine pEngine, Method pMethod) {
			super(pEngine, pMethod);
		}
	}
	static final class JavaObjectMethodInvoke extends JavaMethodInvoke {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaObjectMethodInvoke(Engine pEngine, Object pObj, Method pMethod) {
			super(pEngine, pMethod);
			if(pObj == null) throw new NullPointerException("Null object for a native object method-invokation executable.");
			
			this.Obj = pObj;
		}
		
		final Object Obj;
		
		/**{@inheritDoc}*/ @Override
		Object getObject() {
			return this.Obj;
		}
	}

	// Method Invoke With NoParam ---------------------------------------------------

	static class JavaMethodInvoke_NoParam extends JavaFragment_Simple {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaMethodInvoke_NoParam(Engine pEngine, Method pMethod) {
			super(pEngine, ExecSignature.newSignature(pEngine, pMethod), null, null);
			this.Method = pMethod;
			if(this.Method == null) throw new NullPointerException("Null method for a native "+(this.isStatic()?"class":"object")+" method-invokation executable.");

			if(UClass.isMemberStatic(this.Method) != isStatic())
				throw new IllegalArgumentException("A native "+(this.isStatic()?"class":"object")+" method executable cannot be "+ (this.isStatic()?"non-":"") +"static.");

			if((this.getObject() != null) && this.Method.getDeclaringClass().isInstance(this.getObject()))
				throw new IllegalArgumentException("A native "+(this.isStatic()?"class":"object")+" field read executable cannot be used with the giving object.");
		}
		Method Method;
		boolean isStatic()  { return true; }
		Object  getObject() { return null; }
		// Clone -------------------------------------------------------------------
		@Override final public JavaMethodInvoke_NoParam clone() { return this; }
		// Objectable --------------------------------------------------------------
		@Override protected int getBodyHash() {
			return UObject.hash(this.getObject()) + UObject.hash(this.Method);
		} 
		@Override protected String getBodyStr(Engine pEngine) { return this.Method.getName() + "();"; }
		// Execution ---------------------------------------------------------------
		/** Executing this -  For external*/
		@Override protected Object run() { return null; } // Dummy
		/** Executing this -  For internal to change */
		@Override Object run(Context pContext, Object[] pParams) {
			try {
				return UClass.invokeMethod(this.Method, this.getObject(), null);	// No param
			} catch(Throwable T) {
				Engine E = pContext.getEngine();
				throw new CurryError(
						"An error occur while invoking " + this.Method.toString() + " of " + 
						((this.getObject() == null)
							? E.getDisplayObject(this.getObject())
							: E.getTypeManager().getTypeOfTheInstanceOf(this.Method.getDeclaringClass()).toString()
						) + ".",
						pContext, T);
			}
		}
	}
	static final class JavaClassMethodInvoke_NoParam extends JavaMethodInvoke_NoParam {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaClassMethodInvoke_NoParam(Engine pEngine, Method pMethod) { super(pEngine, pMethod); }
	}
	static final class JavaObjectMethodInvoke_NoParam extends JavaMethodInvoke_NoParam {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaObjectMethodInvoke_NoParam(Engine pEngine, Object pObj, Method pMethod) {
			super(pEngine, pMethod);
			this.Obj = pObj;
			if(this.Obj   == null) throw new NullPointerException("Null object for a native object method-invokation executable.");
		}
		Object Obj;
		@Override boolean isStatic()  { return false;    }
		@Override Object  getObject() { return this.Obj; }
	}

	// Constructor Invoke ----------------------------------------------------------

	static final class JavaConstructorInvoke extends JavaSubRoutine_Simple {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaConstructorInvoke(Engine pEngine, Constructor<?> pConstructor) {
			super(pEngine, ExecSignature.newSignature(pEngine, pConstructor), null, null);
			this.Constructor = pConstructor;
			if(this.Constructor == null) throw new NullPointerException("Null method for a native "+(this.isStatic()?"class":"object")+" method-invokation executable.");

			if(UClass.isMemberStatic(this.Constructor) != isStatic())
				throw new IllegalArgumentException("A native "+(this.isStatic()?"class":"object")+" method executable cannot be "+ (this.isStatic()?"non-":"") +"static.");

			if((this.getObject() != null) && this.Constructor.getDeclaringClass().isInstance(this.getObject()))
				throw new IllegalArgumentException("A native "+(this.isStatic()?"class":"object")+" field read executable cannot be used with the giving object.");
		}
		Constructor<?> Constructor;
		boolean isStatic() { return true; }
		Object getObject() { return null; }
		// Clone -------------------------------------------------------------------
		@Override final public JavaConstructorInvoke clone() { return this; }
		// Objectable --------------------------------------------------------------
		@Override protected int getBodyHash() {
			return UObject.hash(this.getObject()) + UObject.hash(this.Constructor);
		} 
		@Override protected String getBodyStr(Engine pEngine) { return "new "+ UClass.getClassShortName(this.Constructor.getDeclaringClass()) + "(...); }"; }
		// Execution ---------------------------------------------------------------
		/** Executing this -  For external*/
		@Override protected Object run(Object[] pParams) { return null; } // Dummy
		/** Executing this -  For internal to change */
		@Override Object run(Context pContext, Object[] pParams) {
			try {
				return UClass.newInstance(this.Constructor, pParams);
			} catch(Throwable T) {
				MType TM = pContext.getEngine().getTypeManager();
				throw new CurryError(
						"An error occur while create a new instance of " + 
						TM.getTypeOfTheInstanceOf(this.Constructor.getDeclaringClass()).toString() + ".",
						pContext, T);
			}
		}
	}

	// Constructor Invoke With NoParams --------------------------------------------

	static final class JavaConstructorInvoke_NoParam extends JavaFragment_Simple {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		JavaConstructorInvoke_NoParam(Engine pEngine, Constructor<?> pConstructor) {
			super(pEngine, ExecSignature.newSignature(pEngine, pConstructor), null, null);
			this.Constructor = pConstructor;
			if(this.Constructor == null) throw new NullPointerException("Null method for a native "+(this.isStatic()?"class":"object")+" method-invokation executable.");

			if(UClass.isMemberStatic(this.Constructor) != isStatic())
				throw new IllegalArgumentException("A native "+(this.isStatic()?"class":"object")+" method executable cannot be "+ (this.isStatic()?"non-":"") +"static.");

			if((this.getObject() != null) && this.Constructor.getDeclaringClass().isInstance(this.getObject()))
				throw new IllegalArgumentException("A native "+(this.isStatic()?"class":"object")+" field read executable cannot be used with the giving object.");
		}
		Constructor<?> Constructor;
		boolean isStatic() { return true; }
		Object getObject() { return null; }
		// Clone -------------------------------------------------------------------
		@Override final public JavaConstructorInvoke_NoParam clone() { return this; }
		// Objectable --------------------------------------------------------------
		@Override protected int getBodyHash() {
			return UObject.hash(this.getObject()) + UObject.hash(this.Constructor);
		} 
		@Override protected String getBodyStr(Engine pEngine) { return "new "+ UClass.getClassShortName(this.Constructor.getDeclaringClass()) + "(); }"; }
		// Execution ---------------------------------------------------------------
		/** Executing this -  For external*/
		@Override protected Object run() { return null; } // Dummy
		/** Executing this -  For internal to change */
		@Override Object run(Context pContext, Object[] pParams) {
			try {
				return UClass.newInstance(this.Constructor, null);
			} catch(Throwable T) {
				MType TM = pContext.getEngine().getTypeManager();
				throw new CurryError(
						"An error occur while create a new instance of " + 
						TM.getTypeOfTheInstanceOf(this.Constructor.getDeclaringClass()).toString() + ".",
						pContext, T);
			}
		}
	}
}
