/* 
	Copyright (c) 2016, AquariusPower <https://github.com/AquariusPower>
	
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

package com.github.commandsconsolegui.globals;

import com.github.commandsconsolegui.misc.CheckInitAndCleanupI;

/**
 * "centralizing" objects to easy coding.
 * @author AquariusPower <https://github.com/AquariusPower>
 * 
 * @param <T>
 */
public class Global<T> {
	T obj;
	
	protected void setAssertingNotAlreadySet(T objNew){
//		if(this.obj!=null){
		this.obj = CheckInitAndCleanupI.i().assertGlobalIsNull(this.obj, objNew);
//			this.obj = InitTraceI.i().getAssertValidatingIsNull(this.obj, obj);
//			throw new NullPointerException("already set: "+obj);
//		}
//		this.obj=obj;
	}
	
	public T get(){
		if(obj==null)throw new NullPointerException("global not set yet...");
		return obj;
	}
	
	public T set(T obj){
		setAssertingNotAlreadySet(obj);
		return this.obj; //easy chain
	} 
}