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

package com.github.commandsconsolegui.misc;

import java.lang.management.ManagementFactory;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class DebugI implements IConsoleCommandListener{
//	private ConsoleCommands	cc;
	
	/**
	 * when enabled, these keys are used to perform debug tests
	 */
	public static enum EKey{
		StatsText,
		;
		boolean b;
	}
	
	private static DebugI instance = new DebugI();
	public static DebugI i(){return instance;}
//	public static void init(Debug dbg){
//		Debug.instance=dbg;
//	}

	private Boolean	bDebugMode;
	private boolean	bConfigured;
	
	public void configure(CommandsDelegatorI cc){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		//		if(Debug.instance==null)Debug.instance=this;
//		this.cc=cc;
		cc.addConsoleCommandListener(this);
		bConfigured=true;
	}
	
//	public void setConsoleCommand(ConsoleCommands cc){
//		this.cc=cc;
//	}
//	
	public boolean isKeyEnabled(EKey ek){
		return ek.b;
	}

	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI	cc) {
		boolean bCmdWorked=false;
		
		if(cc.checkCmdValidity(this,"debug","[optionToToggle] empty for a list")){
			String str = cc.paramString(1);
			if(str==null){
				for(EKey ek:EKey.values()){
					cc.dumpSubEntry(""+ek+" "+ek.b);
				}
			}else{
				try{
					EKey ek = EKey.valueOf(str);
					ek.b=!ek.b;
					bCmdWorked=true;
				}catch(IllegalArgumentException ex){
					cc.dumpExceptionEntry(ex);
				}
			}
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCmdWorked);
	}

	public boolean isInIDEdebugMode() {
		if(bDebugMode==null){
			bDebugMode=ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
				.indexOf("-agentlib:jdwp") > 0;
		}
		return bDebugMode;
	}
}