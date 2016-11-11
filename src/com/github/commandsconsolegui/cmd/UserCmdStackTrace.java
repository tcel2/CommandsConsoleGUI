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

package com.github.commandsconsolegui.cmd;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.commandsconsolegui.misc.ManageConfigI.IConfigure;

/**
 * User commands, like thru console, may contain invalid values.
 * Exceptions from it must never make the application exit...
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class UserCmdStackTrace implements IConfigure<UserCmdStackTrace>{
	private static UserCmdStackTrace instance = new UserCmdStackTrace();
	public static UserCmdStackTrace i(){return instance;}

	private boolean	bConfigured;
	private CfgParm	cfg;

	@Override
	public boolean isConfigured() {
		return bConfigured;
	}
	
	public static class CfgParm implements ICfgParm{
		ArrayList<Class<?>> aclassUserActionStackList = new ArrayList<Class<?>>();
		public CfgParm(Class<?>... aclassUserActionStack) {
			this.aclassUserActionStackList.addAll(Arrays.asList(aclassUserActionStack));
		}
	}
	
	@Override
	public UserCmdStackTrace configure(ICfgParm icfg) {
		this.cfg=(CfgParm)icfg;
		bConfigured=true;
		return this;
	}
	
	/**
	 * DevSelfNote: must not use {@link PrerequisitesNotMetException} as it calls this
	 * @return
	 */
	public boolean isUserActionStack(){
		for(StackTraceElement ste:Thread.currentThread().getStackTrace()){
			for(Class<?> cl:cfg.aclassUserActionStackList){
				if(
						ste.getClassName().equals(cl.getName())
						||
						ste.getClassName().startsWith(cl.getName()+"$")
				){
					return true;
				}
			}
		}
		
		return false;
	}
}