/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's Curry.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.curry.extra.type_object;

import net.nawaman.curry.EngineExtension;

/**
 * Engine Extension to support Object Types
 * @author Nawapunth Manusitthipol
 */
public class EE_Object extends EngineExtension {
	
    static public final String Name = "Object";
	
    @Override protected String getExtName() { return Name; }
	
    // Required Extension -----------------------------------------------------
    @Override protected String[] getRequiredExtensionNames() {
		return new String[] {};
    }
	
    @Override protected String initializeThis() {
		this.regTypeKind(new TKClass(this.getEngine()));
		return null;
    }

}
