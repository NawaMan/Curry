package net.nawaman.curry;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import net.nawaman.util.UObject;

/** Information about dependency of a group of packages */
public class DependencyInfo implements Serializable {

	static final private long serialVersionUID = 315432132135132135L;
	
	public DependencyInfo(String[] pPackageNames, String[] pRequiredEngineExts, String[] pRequirePackageNames) {
		this((pPackageNames        == null) ? null : Arrays.asList((String[])pPackageNames),
			 (pRequiredEngineExts  == null) ? null : Arrays.asList((String[])pRequiredEngineExts),
			 (pRequirePackageNames == null) ? null : Arrays.asList((String[])pRequirePackageNames)
		);
	}
	
	public DependencyInfo(List<String> pPackageNames, List<String> pRequiredEngineExts,
			List<String> pRequirePackageNames) {
		if((pPackageNames == null) || (pPackageNames.size() == 0))
			throw new NullPointerException();
		
		// Ensure the package names are sorted
		pPackageNames = new Vector<String>(new HashSet<String>(pPackageNames));
		Collections.sort(pPackageNames);
		this.PackageNames = pPackageNames.toArray(new String[pPackageNames.size()]);
		
		// Ensure the required extension names are sorted
		if((pRequiredEngineExts != null) && (pRequiredEngineExts.size() != 0)) {
			pRequiredEngineExts = new Vector<String>(new HashSet<String>(pRequiredEngineExts));
			Collections.sort(pRequiredEngineExts);
			this.RequiredEngineExts = pRequiredEngineExts.toArray(new String[pRequiredEngineExts.size()]);
			
		} else
			this.RequiredEngineExts = null;

		// Ensure the required package names are sorted
		if((pRequirePackageNames != null) && (pRequirePackageNames.size() != 0)) {
			pRequirePackageNames = new Vector<String>(new HashSet<String>(pRequirePackageNames));
			// Ensure the required package name is sorted
			pRequirePackageNames.removeAll(pPackageNames);	// Required the local is not needed to be in there.
			Collections.sort(pRequirePackageNames);
			
			if(pRequirePackageNames.size() == 0)
				 this.RequiredPackageNames = null;
			else this.RequiredPackageNames = pRequirePackageNames.toArray(new String[pRequirePackageNames.size()]);

		} else this.RequiredPackageNames = null;
	}
	
	// All the data should be sorted
	final String[] PackageNames;
	final String[] RequiredEngineExts;
	final String[] RequiredPackageNames;
	
	/** Returns the number of package */
	public int getRequiredPackageCount() {
		return (this.RequiredPackageNames == null) ? 0 : this.RequiredPackageNames.length;
	}
	/** Returns the number of package */
	public String getRequiredPackage(int I) {
		if((I < 0) || (this.RequiredPackageNames == null) || (I >= this.RequiredPackageNames.length))
			return null;
		return  this.RequiredPackageNames[I];
	}
	
	/**{@inheritDoc}*/ @Override
	public int hashCode() {
		return UObject.hash(this.PackageNames) +
		       UObject.hash(this.RequiredEngineExts) +
		       UObject.hash(this.RequiredPackageNames);
	}
	
	/**{@inheritDoc}*/ @Override
	public boolean equals(Object O) {
		if(!(O instanceof DependencyInfo)) return false;
		return this.hashCode() == O.hashCode();
	}
}