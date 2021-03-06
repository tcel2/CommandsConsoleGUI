/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
	
	All rights reserved.

	Redistribution and use in source and binary forms, with or without modification, are permitted 
	provided that the following conditions are met:

	1.	Redistributions of source code must retain the above copyright notice, this list of conditions 
		and the following disclaimer.

	2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
		and the following disclaimer in the documentation and/or other materials provided with the distribution.
	
	3.	Neither the name of the copyright holder nor the names of its contributors may be used to endorse 
		or promote products derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
	PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
	LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
	INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN 
	IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.github.commandsconsolegui.spAppOs.misc;

import java.lang.reflect.Field;

/**
 * The class implementing it is agreeing to properly allow set and get of field values,
 * therefore this is an expected and safe access/behavior. 
 * So, the class implementing get and set, will ALWAYS have access to field's value management!
 * 
 * All classes from superest to concrete int the inheritance MUST implement these methods.
 * If some subclass misses such implementation, the superest will simply fail to access the Field object.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public interface IReflexFieldSafeAccess {
	/**
	 * //Use this at superest implementor:<br>
	 * //if(fld.getDeclaringClass()!=CURRENT_SUB_CLASS.class)return super.getFieldValue(fld); //For subclasses uncomment this line<br>
	 * return fld.get(Modifier.isStatic(fld.getModifiers()) ? null : this);<br>
	 * //or if statics are not allowed/expected, use just:<br>
	 * return fld.get(this);<br>
	 */
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException;
	
	/**
	 * //Use this at superest implementor:<br>
	 * //if(fld.getDeclaringClass()!=CURRENT_SUB_CLASS.class){super.setFieldValue(fld,value);return;} //For subclasses uncomment this line<br>
	 * fld.set(Modifier.isStatic(fld.getModifiers()) ? null : this, value);<br>
	 * //or if statics are not allowed/expected, use just:<br>
	 * fld.set(this,value);<br>
	 */
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException;
}
