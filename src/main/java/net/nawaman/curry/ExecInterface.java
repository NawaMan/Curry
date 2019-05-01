package net.nawaman.curry;

import java.io.*;

import net.nawaman.util.*;

/** Interface for execution in the form of `<return> (<paramtype0>,<paramtype1>, ...)` */
public interface ExecInterface extends Serializable, Objectable {
	
	// Constants -----------------------------------------------------------------------------------
	
	/** Prefix of the parameter name for the auto-generated interface. So it goes `P0`,`P1`,`P2`, ... */
	static public final String AutoParamNamePrifix = "P";

	/** The score to indicate that the searching or comparison has found the exact match. */
	static public final int ExactMatch = MType.ExactMatch;
	/** The score to indicate that the searching or comparison has found nothing or not match. */
	static public final int NotMatch   = MType.NotMatch;

	/** The empty string array */
	static public final String[] EmptyStringArray = new String[0];
	
	// Predefine interfaces ------------------------------------------------------------------------

	/** The empty interface `any ()` */
	static public final ExecInterface EmptyInterface = new EInterface();
	
	// DataHolder --------------------------------------------------------------
	
	/** The default interface for setting a data holder value */
	static public final ExecInterface SetDHValueInterface      = Util.newInterface_SetDHValue(TKJava.TAny.getTypeRef(), AutoParamNamePrifix + 0);
	/** The default interface for getting a data holder value */
	static public final ExecInterface GetDHValueInterface      = Util.newProcedureInterface(TKJava.TAny.getTypeRef());
	/** The default interface for getting a data holder type */
	static public final ExecInterface GetDHTypeInterface       = Util.newProcedureInterface(TKJava.TType.getTypeRef());
	/** The default interface for checking if a data holder is readable */
	static public final ExecInterface IsDHReadableInterface    = Util.newProcedureInterface(TKJava.TBoolean.getTypeRef());
	/** The default interface for checking if a data holder is writable */
	static public final ExecInterface IsDHWritableInterface    = Util.newProcedureInterface(TKJava.TBoolean.getTypeRef());
	/** The default interface for checking if a data holder needs no type check */
	static public final ExecInterface IsDHNoTypeCheckInterface = Util.newProcedureInterface(TKJava.TBoolean.getTypeRef());
	/** The default interface for cloning a data holder */
	static public final ExecInterface CloneDHInterface         = Util.newProcedureInterface(TKJava.TDataHolder.getTypeRef());
	/** The default interface for getting a data holder MoreInfo */
	static public final ExecInterface ConfigDHInterface        = Util.newInterface_ConfigDH(AutoParamNamePrifix + 0, AutoParamNamePrifix + 1);
	/** The default interface for configuring a data holder */
	static public final ExecInterface GetDHMoreInfoInterface   = Util.newInterface_GetDHMoreInfo(AutoParamNamePrifix + 0);
	
	// Services ------------------------------------------------------------------------------------
	
	/** Returns the number of parameters */
	public int     getParamCount();
	/** Returns the parameter type ref at the index pPos */
	public TypeRef getParamTypeRef(int pPos);
	/** Returns the last parameter type ref of a VarArg Interface as a non-array TypeRef */
	public TypeRef getLastVarArgParamTypeRef_As_NonArray();
	/** Returns the parameter name at the index pPos */
	public String  getParamName(int pPos);
	/** Checks if the interface allow the open end parameter at the last parameter */
	public boolean isVarArgs();
	/** Returns the type ref of the return type */
	public TypeRef getReturnTypeRef();
	
	/** Checks equal without checking param names and return type. */
	public boolean isAllParameterTypeEquals(Object O);
	/** Checks equal without checking param names. */
	public boolean equivalents(Object O);
	
	/** Returns the hash code of the interface without the effect of the parameter names */
	public int hash_WithoutParamNamesReturnType();
	
	// Utilities -----------------------------------------------------------------------------------
	
	static public class Util {
		
		static public String[] newAutoParamNames(int PCount) {
			if(PCount < 0) return UString.EmptyStringArray;
			String[] PNs = new String[PCount];
			for(int i = PCount; --i >= 0; ) PNs[i] = ExecInterface.AutoParamNamePrifix + i;
			return PNs;
		}
		
		// Construction related --------------------------------------------------------------------
		
		/** Create a new ExecInterface */
		static public ExecInterface newInterface(
				TypeRef[] pParamTypes, String[] pParamNames,
				boolean   pIsVarArgs,
				TypeRef   pReturnType) {
			
			// The number of param types and names must be the same
			int NCount = (pParamNames == null)?0:pParamNames.length;
			int TCount = (pParamTypes == null)?0:pParamTypes.length;
			if(NCount != TCount) return null;
			
			// The each Param types (if any) must not be null or Void
			if(TCount != 0) {
				for(int i = TCount; --i >= 0; ) {
					if((pParamTypes[i] == null) || (TKJava.TVoid.getTypeRef().equals(pParamTypes[i])))
						return null;
				}
			}
			// The param names (if any) must not be null or empty and must not repeat
			if(NCount != 0) {
				for(int i = 0; i < pParamNames.length - 1; i++) {
					if((pParamNames[i] == null) || (pParamNames[i].length() == 0)) return null;
					for(int j = (i + 1); j < pParamNames.length; j++) {
						if(UString.equal(pParamNames[i], pParamNames[j])) return null;
					}
				}
				if((pParamNames[pParamNames.length - 1] == null) || (pParamNames[pParamNames.length - 1].length() == 0)) return null;
			}
			
			// If the return type is null, make it a void
			if(pReturnType == null) pReturnType = TKJava.TVoid.getTypeRef();
			
			// Create and assign
			return new EInterface(pParamTypes, pParamNames, pIsVarArgs, pReturnType);
		}
		
		/** Create a procedure interface */
		static public ExecInterface newProcedureInterface(TypeRef pReturnTypeRef) {
			return new EInterface(pReturnTypeRef);
		}
		
		/** Create a new ExecInterface of setting a dataholder value */
		static public ExecInterface newInterface_SetDHValue(TypeRef pTypeRef, String pParamName) {
			if(pParamName == null) return null;
			if(pTypeRef   == null) pTypeRef = TKJava.TAny.getTypeRef();
			return Util.newInterface(new TypeRef[] { pTypeRef }, new String[] { pParamName }, false, TKJava.TBoolean.getTypeRef());
		}
		/** Create a new ExecInterface of configuring a dataholder */
		static public ExecInterface newInterface_ConfigDH(String pNameName, String pParamName) {
			if(pNameName  == null) return null;
			if(pParamName == null) return null;
			return Util.newInterface(
					new TypeRef[] { TKJava.TString.getTypeRef(), TKArray.AnyArrayRef },
					new String[]  { pNameName,                   pParamName },
					false, TKJava.TAny.getTypeRef());
		}
		/** Create a new ExecInterface of getting a dataholder MoreInfo */
		static public ExecInterface newInterface_GetDHMoreInfo(String pNameName) {
			if(pNameName == null) return null;
			return Util.newInterface(new TypeRef[] { TKJava.TString.getTypeRef() }, new String[] { pNameName },
					                     false, TKJava.TAny.getTypeRef());
		}
		
		// Display related services ----------------------------------------------------------------
		
		/** Returns the display string of the interface with or without the name of the parameter */
		static public String getParametersToString(ExecInterface EI, boolean pWithParamName) {
			StringBuffer SB = new StringBuffer();
			SB.append("(");
			int PCount = EI.getParamCount();
			int LIndex = PCount - 1;
			for(int i = 0; i < PCount; i++) {
				if(i != 0) SB.append(", ");
				// Show parameter name
				if(pWithParamName) SB.append(EI.getParamName(i).toString()).append(":");
				
				if(i == LIndex) SB.append(EI.getLastVarArgParamTypeRef_As_NonArray().toString());
				else            SB.append(EI.getParamTypeRef(i).toString());
			}
			if(EI.isVarArgs()) SB.append(" ... ");
			SB.append(")");
			return SB.toString();
		}
		
		/** Returns the display string of the interface with or without the name of the parameter */
		static public String getParametersToDetail(ExecInterface EI, boolean pWithParamName) {
			StringBuffer SB = new StringBuffer();
			SB.append("(");
			int PCount = EI.getParamCount();
			int LIndex = PCount - 1;
			for(int i = 0; i < PCount; i++) {
				if(i != 0) SB.append(", ");
				// Show parameter name
				if(pWithParamName) SB.append(EI.getParamName(i).toString()).append(":");
				
				if(i == LIndex) SB.append(EI.getLastVarArgParamTypeRef_As_NonArray().toDetail());
				else            SB.append(EI.getParamTypeRef(i).toDetail());
			}
			if(EI.isVarArgs()) SB.append(" ... ");
			SB.append(")");
			return SB.toString();
		}
		
		// Extracting Parameters -------------------------------------------------------------------
		
		/** Returns the array of classes of the interface parameter */
		static Class<?>[] getParametersAsClasses(Engine pEngine, ExecInterface pEI) {
			Class<?>[] Cs = new Class<?>[pEI.getParamCount()];
			for(int i = pEI.getParamCount(); --i >= 0; ) {
				Cs[i] = pEngine.getTypeManager().getDataClassOf(pEI.getParamTypeRef(i));
			}
			return Cs;
		}
		/** Returns the array of classes from an array of type ref */
		static Class<?>[] getParametersAsClasses(Engine pEngine, TypeRef[] pTRs) {
			Class<?>[] Cs = new Class<?>[pTRs.length];
			for(int i = pTRs.length; --i >= 0; ) {
				Cs[i] = pEngine.getTypeManager().getDataClassOf(pTRs[i]);
			}
			return Cs;
		}
		
		/** Returns the array of classes from an array of types */
		static Class<?>[] getParametersAsClasses(Engine pEngine, Type[] pTs) {
			Class<?>[] Cs = new Class<?>[pTs.length];
			for(int i = pTs.length; --i >= 0; ) {
				Type T = pTs[i]; if(T == null) T = TKJava.TAny;
				Cs[i] = T.getDataClass();
			}
			return Cs;
		}
		
		// CanBeAssignedBy -------------------------------------------------------------------------
		
		// By Parameters -----------------------------------------------------------------
		
		/**
		 * Determines and returns the score of compatibility of using the parameter set pParams for
		 *     the execution of an executable with the interface EI.<br/>
		 * <br/>
		 * This method does not determine the compatibility of the return value. To check both
		 *     parameter interface and return type use `isCompatibleWith` method.
		 *     
		 * @return The score of incompatibility: 0 = ExactMatch and more value the less compatible and -1 means not compatible.
		 **/
		static public int canBeAssignedBy_ByParams(Engine pEngine, ExecInterface EI, Object[] pParams) {
			return canBeAssignedBy_All(pEngine, null, EI, Kind_Params, pParams, null);
		}
		/**
		 * Determines and returns the score of compatibility of using the parameter set pParams for
		 *     the execution of an executable with the interface EI.<br/>
		 * <br/>
		 * This method does not determine the compatibility of the return value. To check both
		 *     parameter interface and return type use `isCompatibleWith` method.
		 *     
		 * @return The score of incompatibility: 0 = ExactMatch and more value the less compatible and -1 means not compatible.
		 **/
		static public int canBeAssignedBy_ByParams(Engine pEngine, Context pContext, ExecInterface EI, Object[] pParams) {
			return canBeAssignedBy_All(pEngine, pContext, EI, Kind_Params, pParams, null);
		}
		
		/**
		 * Determines and returns the score of compatibility of using the parameter set pParams for
		 *     the execution of an executable with the interface EI. At the same time, create an
		 *     regarding adjusted parameter to be used with it.<br/>
		 * <br/>
		 * This method does not determine the compatibility of the return value. To check both
		 *     parameter interface and return type use `isCompatibleWith` method.
		 *     
		 * @return The score of incompatibility: 0 = ExactMatch and more value the less compatible and -1 means not compatible.
		 * @return Unless AdjParams not an Object[1], The adjusted value will be returned as the value of AdjParams[0].
		 **/
		static public int canBeAssignedBy_ByParams(Engine pEngine, ExecInterface EI, Object[] pParams, Object[][] AdjParams) {
			return canBeAssignedBy_All(pEngine, null, EI, Kind_Params, pParams, AdjParams);
		}
		/**
		 * Determines and returns the score of compatibility of using the parameter set pParams for
		 *     the execution of an executable with the interface EI. At the same time, create an
		 *     regarding adjusted parameter to be used with it.<br/>
		 * <br/>
		 * This method does not determine the compatibility of the return value. To check both
		 *     parameter interface and return type use `isCompatibleWith` method.
		 *     
		 * @return The score of incompatibility: 0 = ExactMatch and more value the less compatible and -1 means not compatible.
		 * @return Unless AdjParams not an Object[1], The adjusted value will be returned as the value of AdjParams[0].
		 **/
		// For internal use only
		static public int canBeAssignedBy_ByParams(Engine pEngine, Context pContext, ExecInterface EI, Object[] pParams, Object[][] AdjParams) {
			return canBeAssignedBy_All(pEngine, pContext, EI, Kind_Params, pParams, AdjParams);
		}
		
		// By Parameter TypeRefs ---------------------------------------------------------

		/**
		 * Determines and returns the score of compatibility of using the parameter set of type-refs
		 *     pPTRefs for the execution of an executable with the interface EI.<br/> 
		 * <br/>
		 * This method does not determine the compatibility of the return value. To check both
		 *     parameter interface and return type use `isCompatibleWith` method.
		 *     
		 * @return The score of incompatibility: 0 = ExactMatch and more value the less compatible and -1 means not compatible.
		 **/
		static public int canBeAssignedBy_ByPTRefs(Engine pEngine, ExecInterface EI, TypeRef[] pPTRefs) {
			return canBeAssignedBy_All(pEngine, null, EI, Kind_TRefs, pPTRefs, null);
		}
		/**
		 * Determines and returns the score of compatibility of using the parameter set of type-refs
		 *     pPTRefs for the execution of an executable with the interface EI.<br/> 
		 * <br/>
		 * This method does not determine the compatibility of the return value. To check both
		 *     parameter interface and return type use `isCompatibleWith` method.
		 *     
		 * @return The score of incompatibility: 0 = ExactMatch and more value the less compatible and -1 means not compatible.
		 **/
		// For Internal use only
		static public int canBeAssignedBy_ByPTRefs(Engine pEngine, Context pContext, ExecInterface EI, TypeRef[] pPTRefs) {
			return canBeAssignedBy_All(pEngine, pContext, EI, Kind_TRefs, pPTRefs, null);
		}
		
		// By interface ------------------------------------------------------------------
		
		/**
		 * Determines and returns the score of compatibility of using the parameter set that exactly
		 *     compatible with the interface pEI for the execution of an executable with the
		 *     interface EI.<br/> 
		 * <br/>
		 * This method does not determine the compatibility of the return value. To check both
		 *     parameter interface and return type use `isCompatibleWith` method.
		 * <br/>
		 * This method does not determine the exact-match between two ExecInterfaces. It obeys type
		 *     theory by using co-variant operation for parameter types and contra-variant for return
		 *     types comparison. In case of you want an exact match comparison between two
		 *     ExecInterfaces consider using `equivalents`.<br />
		 * <br />
		 * This method also relax in the comparison involving `IsVarArgs`. For example: 
		 *     `int (int ...)` can be assigned by `int (int, int, int)`
		 **/
		static public int canBeAssignedBy_ByInterface(Engine pEngine, ExecInterface EI, ExecInterface pEI) {
			return canBeAssignedBy_All(pEngine, null, EI, Kind_EI, pEI, null);
		}
		/**
		 * Determines and returns the score of compatibility of using the parameter set that exactly
		 *     compatible with the interface pEI for the executaion of an executable with the
		 *     interface EI.<br/> 
		 * <br/>
		 * This method does not determine the compatibility of the return value. To check both
		 *     parameter interface and return type use `isCompatibleWith` method.
		 * <br/>
		 * This method does not determine the exact-match between two ExecInterfaces. It obeys type
		 *     theory by using co-variant operation for parameter types and contravariant for return
		 *     types comparison. In case of you want an exact match comparison between two
		 *     ExecInterfaces consider using `equivalents`.<br />
		 * <br />
		 * This method also relax in the comparison involving `IsVarArgs`. For example: 
		 *     `int (int ...)` can be assigned by `int (int, int, int)`
		 **/
		// For Internal use only
		static public int canBeAssignedBy_ByInterface(Engine pEngine, Context pContext, ExecInterface EI, ExecInterface pEI) {
			return canBeAssignedBy_All(pEngine, pContext, EI, Kind_EI, pEI, null);
		}
		
		// isCompatibleWith ------------------------------------------------------------------------
		
		// By TypeRefs -------------------------------------------------------------------
		
		/**
		 * Determines and returns the score of compatibility of using the parameter set and return
		 *     type references of types pPTRefs for the execution of an executable with the
		 *     interface EI.
		 *          
		 * @return The score of incompatibility: 0 = ExactMatch and more value the less compatible and -1 means not compatible.
		 **/
		static public int isCompatibleWith(Engine pEngine, ExecInterface EI, TypeRef pReturnTypeRef, TypeRef[] pPTRefs) {
			return isCompatibleWith(pEngine, (Context)null, EI, pReturnTypeRef, pPTRefs);
		}
		/**
		 * Determines and returns the score of compatibility of using the parameter set and return
		 *     type references of types pPTRefs for the execution of an executable with the
		 *     interface EI.
		 *          
		 * @return The score of incompatibility: 0 = ExactMatch and more value the less compatible and -1 means not compatible.
		 **/
		static public int isCompatibleWith(Engine pEngine, Context pContext, ExecInterface EI, TypeRef pReturnTypeRef, TypeRef[] pPTRefs) {
			// Checks the parameter
			int Score = canBeAssignedBy_ByPTRefs(pEngine, pContext, EI, pPTRefs);
			if(Score == NotMatch) return NotMatch;
			// Checks the return type
			int S = MType.compareTypes(pEngine, EI.getReturnTypeRef(), pReturnTypeRef);
			if(S == NotMatch) return NotMatch;
			return Score + S;
		}
		
		// By Interface ------------------------------------------------------------------
		
		/**
		 * Determines and returns the score of compatibility of using the parameter set and return
		 *     type that exactly compatible with the interface pEI for the executaion of an
		 *     executable with the interface EI.
		 **/
		static int isCompatibleWith(Engine pEngine, ExecInterface EI, ExecInterface pEI) {
			return isCompatibleWith(pEngine, (Context)null, EI, pEI);
		}
		/**
		 * Determines and returns the score of compatibility of using the parameter set and return
		 *     type that exactly compatible with the interface pEI for the executaion of an
		 *     executable with the interface EI.
		 **/
		static public int isCompatibleWith(Engine pEngine, Context pContext, ExecInterface EI, ExecInterface pEI) {
			// Checks the parameter
			if(pEI == null)         return NotMatch;
			if(EI.equivalents(pEI)) return ExactMatch;
			int Score = canBeAssignedBy_ByInterface(pEngine, pContext, EI, pEI);
			if(Score == NotMatch) return NotMatch;
			// Checks the return type
			int S = MType.compareTypes(pEngine, EI.getReturnTypeRef(), pEI.getReturnTypeRef());
			if(S == NotMatch) return NotMatch;
			return Score + S;
		}
		
		/**
		 * Creates the adjusted parameters from the parameter set pParams to be used in the
		 *     execution of an executable with the interface EI.
		 * @param  pEngine        is the engine
		 * @param  pContext       is the current context
		 * @param  EI             is the exec-interface in question
		 * @param  ParamTypeTypes is the given parameters
		 * @return The score of incompatibility: 0 = ExactMatch and more value the less compatible and -1 means not compatible.
		 **/
		static Object[] getAdjustedValue(Engine pEngine, Context pContext, ExecInterface EI, Object[] pParams) {
			Object[][] AParams = new Object[1][];
			int Score = canBeAssignedBy_All(pEngine, pContext, EI, Kind_Params, pParams, AParams);
			return (Score == NotMatch)?null:AParams[0];
		}
		
		/** Returns 'toString' of this interface as if it is a signature */
		static public String toString(ExecInterface EI, String pName) {
			if(pName == null) pName = "";
			StringBuffer SB = new StringBuffer();
			SB.append(pName);
			SB.append(ExecInterface.Util.getParametersToString(EI, false));
			SB.append(":");
			SB.append(EI.getReturnTypeRef().toString());
			return SB.toString();
		}
		
		// Search ------------------------------------------------------------------------

		// Search Signatures ---------------------------------------------------
		
		/** Serach in the signature pool using the name and the params **/
		static public int searchSignatureByParams(Engine pEngine, Context pContext,
				            ExecSignature[] pESPools, String pName, Object[] pParams,
				            boolean pIgnoreName) {
			if((pESPools == null) || (pESPools.length == 0)) return NotMatch;
			int Min = NotMatch;
			int Ind = NotMatch;
			for(int i = pESPools.length; --i >= 0; ) {
				ExecSignature AnES = pESPools[i];
				if(AnES == null)                                          continue;
				if(!pIgnoreName && !UObject.equal(AnES.getName(), pName)) continue;
				int S = canBeAssignedBy_ByParams(pEngine, pContext, AnES, pParams);
				if(S == NotMatch) continue;
				if(S == ExactMatch) return i;
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
				}
			}
			return Ind;
		}
		/** Serach in the interface pool using the name and the params **/
		static public int searchSignatureByParams(Engine pEngine, Context pContext,
				            ExecSignature[] pESPools, String pName, Object[] pParams,
				            Object[][] pAdjParams, boolean pIgnoreName) {
			if((pESPools == null) || (pESPools.length == 0)) return NotMatch;
			if((pAdjParams != null) && (pAdjParams.length != 1)) pAdjParams = null;
			int Min = NotMatch;
			int Ind = NotMatch;
			for(int i = pESPools.length; --i >= 0; ) {
				ExecSignature AnES = pESPools[i];
				if(AnES == null)                                          continue;
				if(!pIgnoreName && !UObject.equal(AnES.getName(), pName)) continue;
				Object[][] AdjParams = (pAdjParams == null)?null:new Object[1][];
				int S = canBeAssignedBy_ByParams(pEngine, pContext, AnES, pParams, AdjParams);
				if(S == NotMatch) continue;
				if(S == ExactMatch) return i;
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
					if(pAdjParams != null) pAdjParams[0] = AdjParams[0];
				}
			}
			return Ind;
		}
		/** Serach in the interface pool using the name and the param types **/
		static public int searchSignatureByTRefs(Engine pEngine, Context pContext, ExecSignature[] pESPools,
				String pName, TypeRef[] pPTRefs, boolean pIgnoreName) {
			if((pESPools == null) || (pESPools.length == 0)) return NotMatch;
			int Min = NotMatch;
			int Ind = NotMatch;
			for(int i = pESPools.length; --i >= 0; ) {
				ExecSignature AnES = pESPools[i];
				if(AnES == null)                                          continue;
				if(!pIgnoreName && !UObject.equal(AnES.getName(), pName)) continue;
				int S = canBeAssignedBy_ByPTRefs(pEngine, pContext, AnES, pPTRefs);
				if(S == NotMatch) continue;
				if(S == ExactMatch) return i;
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
				}
			}
			return Ind;
		}
		/** Serach in the interface pool using the name and the param types **/
		static public int searchSignatureByInterface(Engine pEngine, Context pContext,
				            ExecSignature[] pESPools, String pName, ExecInterface EI, boolean pIgnoreName) {
			if(pName == null) return NotMatch;
			if(EI    == null) return NotMatch;
			if((pESPools == null) || (pESPools.length == 0)) return NotMatch;
			
			int Min = NotMatch;
			int Ind = NotMatch;
			for(int i = pESPools.length; --i >= 0; ) {
				ExecSignature AnES = pESPools[i];
				if(AnES == null)                                          continue;
				if(!pIgnoreName && !UObject.equal(AnES.getName(), pName)) continue;
				int S = canBeAssignedBy_ByInterface(pEngine, pContext, AnES, EI);
				if(S == NotMatch) continue;
				if(S == ExactMatch) return i;
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
				}
			}
			return Ind;
		}
		/** Serach in the interface pool using the name and the param types **/
		static public int searchSignatureBySignature(Engine pEngine, Context pContext,
				             ExecSignature[] pESPools, ExecSignature ES, boolean pIgnoreName) {
			if(ES == null) return NotMatch;
			if((pESPools == null) || (pESPools.length == 0)) return NotMatch;
			String Name = ES.getName();
			
			int Min = NotMatch;
			int Ind = NotMatch;
			for(int i = pESPools.length; --i >= 0; ) {
				ExecSignature AnES = pESPools[i];
				if(AnES == null)                                         continue;
				if(!pIgnoreName && !UObject.equal(AnES.getName(), Name)) continue;
				int S = canBeAssignedBy_ByInterface(pEngine, pContext, AnES, ES);
				if(S == NotMatch) continue;
				if(S == ExactMatch) return i;
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
				}
			}
			return Ind;
		}
		
		// Search Executables --------------------------------------------------

		/** Serach in the interface pool using the name and the params **/
		static public int searchExecutableByParams(Engine pEngine, Context pContext,
				             HasSignature[] pEPools, String pName, Object[] pParams,
				             boolean pIgnoreName) {
			if((pEPools == null) || (pEPools.length == 0)) return NotMatch;
			int Min = NotMatch;
			int Ind = NotMatch;
			for(int i = pEPools.length; --i >= 0; ) {
				HasSignature E = pEPools[i];
				if(E == null)                                                         continue;
				if(!pIgnoreName && !UObject.equal(E.getSignature().getName(), pName)) continue;
				ExecSignature AnES = E.getSignature();
				int S = canBeAssignedBy_ByParams(pEngine, pContext, AnES, pParams);
				if(S == NotMatch) continue;
				if(S == ExactMatch) return i;
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
				}
			}
			return Ind;
		}
		/** Serach in the interface pool using the name and the params **/
		static public int searchExecutableByParams(Engine pEngine, Context pContext,
				             HasSignature[] pEPools, String pName, Object[] pParams,
				             Object[][] pAdjParams, boolean pIgnoreName) {
			if((pEPools == null) || (pEPools.length == 0)) return NotMatch;
			if((pAdjParams != null) && (pAdjParams.length != 1)) pAdjParams = null;
			int Min = NotMatch;
			int Ind = NotMatch;
			Object[][] AdjParams = (pAdjParams == null)?null:new Object[1][];
			for(int i = pEPools.length; --i >= 0; ) {
				HasSignature E = pEPools[i];
				if(E == null)                                                         continue;
				if(!pIgnoreName && !UObject.equal(E.getSignature().getName(), pName)) continue;
				ExecSignature AnES = E.getSignature();
				if(AdjParams != null) AdjParams[0] = null; else AdjParams = new Object[1][];
				int S = canBeAssignedBy_ByParams(pEngine, pContext, AnES, pParams, AdjParams);
				if(S == NotMatch) continue;
				if(S == ExactMatch) {
					if(pAdjParams != null) pAdjParams[0] = AdjParams[0];
					return i;
				}
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
					if(pAdjParams != null) pAdjParams[0] = AdjParams[0];
				}
			}
			return Ind;
		}
		/** Search in the interface pool using the name and the param types **/
		static public int searchExecutableByTRefs(Engine pEngine, Context pContext,
				            HasSignature[] pEPools, String pName,
				            TypeRef[] pPTRefs, boolean pIgnoreName) {
			if((pEPools == null) || (pEPools.length == 0)) return NotMatch;
			int Min = NotMatch;
			int Ind = NotMatch;
			for(int i = pEPools.length; --i >= 0; ) {
				HasSignature E = pEPools[i];
				if(E == null)                                                         continue;
				if(!pIgnoreName && !UObject.equal(E.getSignature().getName(), pName)) continue;
				ExecSignature AnES = E.getSignature();
				int S = canBeAssignedBy_ByPTRefs(pEngine, pContext, AnES, pPTRefs);
				if(S == NotMatch) continue;
				if(S == ExactMatch) return i;
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
				}
			}
			return Ind;
		}
		/** Serach in the interface pool using the name and the param types **/
		static public int searchExecutableByInterface(Engine pEngine, Context pContext,
				            HasSignature[] pEPools, String pName, ExecInterface EI,
				            boolean pIgnoreName) {
			if(pName == null) return NotMatch;
			if(EI    == null) return NotMatch;
			if((pEPools == null) || (pEPools.length == 0)) return NotMatch;
			
			int Min = NotMatch;
			int Ind = NotMatch;
			for(int i = pEPools.length; --i >= 0; ) {
				HasSignature E = pEPools[i];
				if(E == null)                                                         continue;
				if(!pIgnoreName && !UObject.equal(E.getSignature().getName(), pName)) continue;
				ExecSignature AnES = E.getSignature();
				int S = canBeAssignedBy_ByInterface(pEngine, pContext, AnES, EI);
				if(S == NotMatch) continue;
				if(S == ExactMatch) return i;
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
				}
			}
			return Ind;
		}
		/** Serach in the interface pool using the name and the param types **/
		static public int searchExecutableBySignature(Engine pEngine, Context pContext, HasSignature[] pEPools,
				ExecSignature ES, boolean pIgnoreName) {
			if(ES == null) return NotMatch;
			if((pEPools == null) || (pEPools.length == 0)) return NotMatch;
			String Name = ES.getName();
			
			int Min = NotMatch;
			int Ind = NotMatch;
			for(int i = pEPools.length; --i >= 0; ) {
				HasSignature E = pEPools[i];
				if(E == null)                                                         continue;
				if(!pIgnoreName && !UObject.equal(E.getSignature().getName(), Name)) continue;
				ExecSignature AnES = E.getSignature();
				int S = canBeAssignedBy_ByInterface(pEngine, pContext, AnES, ES);
				if(S == NotMatch) continue;
				if(S == ExactMatch) return i;
				if((Min == NotMatch) || (S < Min)) {
					Min = S;
					Ind = i;
				}
			}
			return Ind;
		}
		
		// The implementation for `canBeAssignedBy` ------------------------------------------------
		
		private static final int Kind_Params = 0; // Param0 = Params:Object[];      Param1 = AdjustedParamed:Object[1][]
		private static final int Kind_TRefs  = 2; // Param0 = PTRefs:TypeRef[];     Param1 = <nothing>;
		private static final int Kind_EI     = 3; // Param0 =     EI:ExecInterface; Param1 = <nothing>;

		private static int canBeAssignedBy_All(Engine pEngine, Context pContext, ExecInterface EI,
				             int pKind, Object Param0, Object Param1) {
			// Early check
			if(EI == null) return NotMatch;
			
			final int     Kind        = pKind;
			      int     PCount      =    -1;
			      boolean IsAdjusting = false;
			      boolean IsPVarArgs  = false;
			      
			// Comparing Data
			Object[]      RawParams = null;
			Object[]      Params    = null;
			Object[]      AdjParams = null;
			TypeRef[]     PTypeRefs = null;
			ExecInterface PEI       = null;
			      
			// Prepare the data
			switch(Kind) {
				case Kind_Params:{
					RawParams = (Param0 != null)?(Object[])  Param0:UObject.EmptyObjectArray;
					AdjParams =                  (Object[][])Param1;
					if((AdjParams != null) && (AdjParams.length == 1)) {
						IsAdjusting  = true;
						AdjParams[0] = null;
					}
					PCount = RawParams.length;
					break;
				}
				case Kind_TRefs: { PTypeRefs = (Param0 != null)?(TypeRef[])Param0:TypeRef.EmptyTypeRefArray; PCount = PTypeRefs.length; break; }
				case Kind_EI: {
					PEI = (ExecInterface)Param0;
					if(EI == PEI)           return ExactMatch;
					if(EI.equivalents(PEI)) return ExactMatch;
					
					if(PEI == null) throw new NullPointerException();
					PCount     = PEI.getParamCount();
					IsPVarArgs = PEI.isVarArgs();
					break;
				}
				default: throw new IllegalArgumentException("pKind must be with in [0..3].");
			}
			
			// Early catch
			if((EI.getParamCount() == 0)) {
				if(PCount == 0) {
					if(IsAdjusting) AdjParams[0] = UObject.EmptyObjectArray;
					return ExactMatch;
				}
				return NotMatch;
			}
			
			// Ensure param count
			boolean IsVarArgs = EI.isVarArgs();
			boolean IsProcessAsNormal  = false;
			boolean IsProcessAsVarArgs = false;
			if(IsVarArgs && IsPVarArgs) {
				// The only case to be here is they both are ExecInterface and isVarArgs()
				// Only Kind_EI can be here
				// The case of: int (String, int ...) && int (String, int ...)      is Ok
				// The case of: int (String, int ...) && int (String, int, int ...) is Ok
				// The case of: int (String, int, int ...) && int (String, int ...) is not Ok
				// So the count of the param cannot be more than the Interface
				if(EI.getParamCount() > PCount) return ExecInterface.NotMatch;
				IsProcessAsVarArgs = true;
			} else if(IsVarArgs || IsPVarArgs) {
				if(IsVarArgs) {
					// EI isVarArgs and the Param set is not
					// Only this: int (String, int ...) && int (String) is Ok
					if((EI.getParamCount() - 1) > PCount) return ExecInterface.NotMatch;
					IsProcessAsVarArgs = true;
				} else {
					// EI is not VarArgs but the Param set is
					// Only Kind_EI can be here
					return ExecInterface.NotMatch;
				}
			} else { // Both are not isVarArgs so param count must match
				// Only this: int (String, int) && int (String, int) is Ok
				if(EI.getParamCount() != PCount) return ExecInterface.NotMatch;
				IsProcessAsNormal = true;
			}
			// More investigate into the type of the parameters are needed.

			int Score = ExactMatch;
			if(IsAdjusting) Params = new Object[EI.getParamCount()];
			
			// Process last Parameter has 
			if(IsProcessAsVarArgs) {
				int      ECount     = EI.getParamCount();
				Object[] LastParams = null;
				boolean  IsTail     = false;

				// If the count are equal, there are can be two cases of `sole value` and `array value`
				if(ECount == PCount) {
					// Get type of EI
					TypeRef  ETR = EI.getParamTypeRef(ECount - 1);
					TypeSpec ETS = TypeRef.getTypeSpecOf(pEngine, pContext, ETR);
					// Get type of the param
					TypeRef PTR = null;
					switch(Kind) {
						case Kind_Params: PTR = pEngine.getTypeManager().getTypeOfNoCheck(pContext, RawParams[PCount - 1]).getTypeRef(); break;
						case Kind_TRefs:  PTR = PTypeRefs[PCount - 1];                                                                   break;
						case Kind_EI:     PTR = PEI.getParamTypeRef(PCount - 1);                                                         break;
					}
					TypeSpec PTS = TypeRef.getTypeSpecOf(pEngine, pContext, PTR);
					
					 // If the param type is array
					if(PTS instanceof TKArray.TSArray) {
						// Check the component of the parameter to the interface
						// int (String, int ...) by ("Test", int[] { 1, 2, 3 })
						int S = MType.compareTypes(pEngine, ((TKArray.TSArray)ETS).getContainTypeRef(), ((TKArray.TSArray)PTS).getContainTypeRef());
						//int S = MType.compareTypes(ETR.getTheType(), ((TKArray.TArray)PTR.getTheType()).getContainType());
						if(S == NotMatch) IsTail = true;	// The type of array may not fit but the data inside may fit
						else {
							// Match direcly
							Score += S;
							// So the last param fit
							if(IsAdjusting) LastParams = (Object[])RawParams[PCount - 1];				
						}
					} else {
						// A single value
						//Type PT = pEngine.getTypeOf(pContext, RawParams[PCount - 1]);
						//pEngine.ensureTypeInitialized(pContext, PT);
						int S = MType.compareTypes(pEngine, ((TKArray.TSArray)ETS).getContainTypeRef(), PTR);
						//int S = MType.compareTypes(ETR.getTheType(), PTR.getTheType());
						if(S == NotMatch) return NotMatch;
						Score += S;
						// Wrap the value with an array
						if(IsAdjusting) LastParams = new Object[] { RawParams[PCount - 1] };
					}
				} else IsTail = true;	// Not equal, so it has a tail
				
				if(IsTail) {
					// Get the type of the last parameter of EI
					TypeRef  ETR = EI.getParamTypeRef(ECount - 1);
					TypeSpec ETS = TypeRef.getTypeSpecOf(pEngine, pContext, ETR);
					ETR = ((TKArray.TSArray)ETS).getContainTypeRef();
					int LS = 0;
					if(IsAdjusting) LastParams = new Object[PCount - ECount + 1];	// Prepare last param array
					// Run for all tail values
					for(int i = PCount; --i >= (ECount - 1); ) {
						TypeRef PTR = null;
						int     S   = NotMatch; 
						switch(Kind) {
							case Kind_Params:
								Object P = RawParams[i];
								if(P == null) S = ExactMatch;
								else {
									PTR = pEngine.getTypeManager().getTypeOfNoCheck(pContext, P).getTypeRef();
									S   = MType.compareTypes(pEngine, ETR, PTR);
								}
								break;
							case Kind_TRefs:
								PTR = PTypeRefs[i];
								S   = MType.compareTypes(pEngine, ETR, PTR);
								break;
							case Kind_EI:
								PTR = PEI.getParamTypeRef(i);
								S   = MType.compareTypes(pEngine, ETR, PTR);
								break;
						}
						
						if(S == NotMatch) return NotMatch;
						LS += S;
						if(IsAdjusting) LastParams[i - (ECount - 1)] = RawParams[i];
					}
					if((PCount - ECount + 1) != 0)
						Score += LS/(PCount - ECount + 1);
				}
				// Assign the last params.
				if(IsAdjusting) Params[ECount - 1] = LastParams;
			}
			// This for NoProcess. For Process as normal it will be overwriten by the real value.
			else {
				if(IsAdjusting) Params[EI.getParamCount() - 1] = null;	 
			}
			
			// Check params before the last (if it is a normal process, include the last)
			for(int i = (EI.getParamCount() - (IsProcessAsNormal?0:1)); --i >= 0;) {
				// Get type of EI
				TypeRef ETR = EI.getParamTypeRef(i);
				// Get type of the param
				TypeRef PTR = null;
				int     S   = NotMatch; 
				switch(Kind) {
					case Kind_Params:
						Object P = RawParams[i];
						if(P == null) S = ExactMatch;
						else {
							PTR = pEngine.getTypeManager().getTypeOfNoCheck(pContext, P).getTypeRef();
							S = MType.compareTypes(pEngine, ETR, PTR);
							// TODO - This is a quick solution
							if((S == -1) && MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, ETR, PTR)) S = 10;
						}
						break;
					case Kind_TRefs:
						PTR = PTypeRefs[i];
						S = MType.compareTypes(pEngine, ETR, PTR);
						// TODO - This is a quick solution
						if((S == -1) && MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, ETR, PTR)) S = 10;
						break;
					case Kind_EI:
						PTR = PEI.getParamTypeRef(i);
						S = MType.compareTypes(pEngine, ETR, PTR);
						// TODO - This is a quick solution
						if((S == -1) && MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, ETR, PTR)) S = 10;
						break;
				}
				
				if(S == NotMatch) return NotMatch;
				// Keep score
				Score += S;
				// Adjust the param
				if(IsAdjusting) Params[i] = RawParams[i];
			}

			if(IsAdjusting) AdjParams[0] = Params;
			return Score;
		} 
	}
	
	// Default Implementation ----------------------------------------------------------------------

	/** Default implementation of EInterface */
	static class EInterface implements ExecInterface {
		
		static private final long serialVersionUID = -1821968809684972658L;
		
		/** Constructs an EInterface */
		protected EInterface() {
			this(null);
		}
		
		/** Constructs an EInterface */
		protected EInterface(TypeRef pReturnType) {
			this(null, null, false, pReturnType);
		}
		
		/** Constructs an EInterface */
		protected EInterface(TypeRef[] ParamTypes, String[]  ParamNames, boolean pIsVarArgs, TypeRef pReturnType) {
			
			// Prepare types
			if(     ParamTypes        == null) ParamTypes = TypeRef.EmptyTypeRefArray;
			else if(ParamTypes.length !=    0) ParamTypes = ParamTypes.clone();

			// Prepare names
			if(     ParamNames        == null) ParamNames = UString.EmptyStringArray;
			else if(ParamNames.length !=    0) ParamNames = ParamNames.clone();
			
			// Prepare the return type
			if(pReturnType == null) pReturnType = TKJava.TAny.getTypeRef();
			
			this.ParamTypes = ParamTypes;
			this.ParamNames = ParamNames;
			this.IsVarArgs  = pIsVarArgs;
			this.ReturnType = pReturnType;
		}
		
		final TypeRef[] ParamTypes;
		final String[]  ParamNames;
		final boolean   IsVarArgs;
		final TypeRef   ReturnType;
		
		// Informative ------------------------------------------------------------
		
		/** Returns the number of parameters */
		public int getParamCount() {
			return (this.ParamTypes == null)?0:this.ParamTypes.length;
		}
		/** Returns the parameter type ref at the index pPos */
		public TypeRef getParamTypeRef(int pPos) {
			if((pPos < 0) || (pPos >= this.getParamCount())) return null;
			
			TypeRef PRef = this.ParamTypes[pPos];
			if(this.IsVarArgs && (pPos == (this.getParamCount() - 1)))
				return TKArray.newArrayTypeRef(PRef);
			
			return this.ParamTypes[pPos];
		}
		/** Returns the last parameter type ref of a VarArg Interface as a non-array TypeRef */
		public TypeRef getLastVarArgParamTypeRef_As_NonArray() {
			int PCount = this.getParamCount();
			if(PCount == 0) return null;
			return this.ParamTypes[PCount - 1];
		}
		/** Returns the parameter name at the index pPos */
		public String getParamName(int pPos) {
			if((pPos < 0) || (pPos >= this.getParamCount())) return null;
			return this.ParamNames[pPos];
		}
		/** Checks if the inteface allow the open end parameter at the last parameter */
		public boolean isVarArgs()        { return this.IsVarArgs;  }
		/** Returns the type ref of the return type */
		public TypeRef getReturnTypeRef() {
			return this.ReturnType;
		}
		
		// Objectable -------------------------------------------------------------
		/** Returns the short string representation of the object. */
		public boolean is(Object O) { return (this == O); }
		/** Checks Equal with out param name and return type. */
		public boolean isAllParameterTypeEquals(Object O) {
			if(this == O) return true;
			if(O == null) return false;
			if(!(O instanceof ExecInterface)) return false;
			ExecInterface EI = (ExecInterface)O;
			if(this.getParamCount() != EI.getParamCount()) return false;
			if(this.IsVarArgs       != EI.isVarArgs())     return false;
			
			if(this.ParamNames != null) {
				for(int i = this.ParamNames.length; --i >= 0; ) {
					if(!this.ParamTypes[i].equals(EI.getParamTypeRef(i))) return false;
				}
			}
			return true;
		}
		/** Checks Equal with out param name. */
		@Override public boolean equivalents(Object O) {
			if(this == O) return true;
			if(O == null) return false;
			if(!(O instanceof ExecInterface)) return false;
			ExecInterface EI = (ExecInterface)O;
			if(this.getParamCount() != EI.getParamCount()) return false;
			if(this.IsVarArgs       != EI.isVarArgs())     return false;
			
			if(!UObject.equal(this.ReturnType, EI.getReturnTypeRef())) return false;
			
			if(this.ParamNames != null) {
				for(int i = this.ParamNames.length; --i >= 0; ) {
					if(!this.ParamTypes[i].equals(EI.getParamTypeRef(i))) return false;
				}
			}
			return true;
		}
		/** Checks if the given object equals to this einterface exactly (including parameter name) */
		@Override public boolean equals(Object O) {
			if(this == O) return true;
			if(O == null) return false;
			if(!(O instanceof ExecInterface)) return false;
			ExecInterface EI = (ExecInterface)O;
			if(this.getParamCount() != EI.getParamCount()) return false;
			if(this.IsVarArgs       != EI.isVarArgs())     return false;
			
			if(!UObject.equal(this.ReturnType, EI.getReturnTypeRef())) return false;
			if(this.ParamNames != null) {
				for(int i = this.ParamNames.length; --i >= 0; ) {
					if(!this.ParamNames[i].equals(EI.getParamName(   i))) return false;
					if(!this.ParamTypes[i].equals(EI.getParamTypeRef(i))) return false;
				}
			}
			return true;
		}
		int hash = -1;
		/** Calculates and returns the hash value of the exec-inteface. */ @Override
		public int hash() {
			if(hash == -1) this.hash = this.hashCode() + UObject.hash(this.ParamNames);
			return this.hash;
		}

		static final int IsVarArgsHash = "isVarArgs".hashCode();
		
		static int WeightConstant = 4;
		
		int hash_WOPNs = -1;
		/** Calculates and returns the hash value of the exec-inteface. */
		public int hash_WithoutParamNamesReturnType() {
			if(hash_WOPNs == -1) {
				int h = 0;
				h += this.getParamCount();
				for(int i = 0; i < this.getParamCount(); i++)
					h += (WeightConstant + i + 1)*UObject.hash(this.getParamTypeRef(i));
				if(this.isVarArgs())
					h += WeightConstant*WeightConstant*IsVarArgsHash;
				
				this.hash_WOPNs = h;
			}
			return this.hash_WOPNs;
		}
		/** Calculates and returns the hash value of the exec-inteface. */
		@Override
		public int hashCode() {
			return this.hash_WithoutParamNamesReturnType() + UObject.hash(this.ReturnType);
		}
		/** Calculates and returns the hash value of the exec-inteface the Java way. */
		public int hashCode_Java() { return super.hashCode(); }
		/** Checks if O equals to this object. */
		@Override public String toString() {
			StringBuffer SB = new StringBuffer();
			SB.append(ExecInterface.Util.getParametersToString(this, false));
			SB.append(":");
			SB.append(this.ReturnType.toString());
			return SB.toString();
		}
		/** Returns the integer representation of the object. */
		public String toDetail() {
			StringBuffer SB = new StringBuffer();
			SB.append(ExecInterface.Util.getParametersToString(this, true));
			SB.append(":");
			SB.append(this.ReturnType.toString());
			return SB.toString();
		}
	}
}