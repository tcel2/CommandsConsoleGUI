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

package com.github.commandsconsolegui.jmegui.lemur.console.test;

import com.github.commandsconsolegui.cmd.ScriptingCommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.extras.FpsLimiterStateI;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.ReflexFillI;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CustomCommandsI extends ScriptingCommandsDelegatorI{ //use ConsoleCommands to prevent scripts usage
	public CustomCommandsI(){
		super();
		
//		ConsoleGUILemurStateI.i().configureSimple(KeyInput.KEY_F10);
//		FpsLimiterStateI.i().configure();
//		UngrabMouseStateI.i().configureSimple(null,true);
		
		/**
		 *  This allows test3 at endUserCustomMethod() to work.
		 */
		ReflexFillI.i().setUseDefaultCfgIfMissing(true);
	}
	
//	@Override
//	public ECmdReturnStatus executePreparedCommandRoot() {
//		boolean bCommandWorked = false;
//		
//		if(checkCmdValidity(null,"fpsLimit","[iMaxFps]")){
//			Integer iMaxFps = paramInt(1);
//			if(iMaxFps!=null){
//				FpsLimiterStateI.i().setMaxFps(iMaxFps);
//				bCommandWorked=true;
//			}
//			dumpSubEntry("FpsLimit = "+FpsLimiterStateI.i().getFpsLimit());
//		}else
//		{
//			return super.executePreparedCommandRoot();
//		}
//		
//		return cmdFoundReturnStatus(bCommandWorked);
//	}
	
	@Override
	public void updateToggles() {
		if(btgFpsLimit.checkChangedAndUpdate())FpsLimiterStateI.i().setEnabledRequest(btgFpsLimit.b());
		super.updateToggles();
	}
	
	@Override
	public String prepareStatsFieldText() {
		String strStatsLast = super.prepareStatsFieldText();
		
		if(EStats.MousePosition.b()){
			strStatsLast+=
					"xy"
						+(int)GlobalSappRefI.i().get().getInputManager().getCursorPosition().x
						+","
						+(int)GlobalSappRefI.i().get().getInputManager().getCursorPosition().y
						+";";
		}
		
		if(EStats.TimePerFrame.b()){
			strStatsLast+=
					"Tpf"+(FpsLimiterStateI.i().isEnabled() ? (int)(fTPF*1000.0f) : MiscI.i().fmtFloat(fTPF,6)+"s")
						+(FpsLimiterStateI.i().isEnabled()?
							"="+FpsLimiterStateI.i().getFrameDelayByCpuUsageMilis()+"+"+FpsLimiterStateI.i().getThreadSleepTimeMilis()+"ms"
							:"")
						+";";
		}
		
		return strStatsLast; 
	}
	
	@Override
	public void cmdExit() {
		GlobalSappRefI.i().get().stop();
		super.cmdExit();
	}
	
//	@Override
//	public void configure(IConsoleUI icui) {
//		super.configure(icui);
//		
////		CommandsBackgroundState.i().configure(GlobalSappRefI.i().get(), icui, this);
////		MiscJmeI.i().configure(GlobalSappRefI.i().get(), this);
//	}
}