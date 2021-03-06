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

package commandsconsoleguitests;

import com.github.commandsconsolegui.spCmd.ScriptingCommandsDelegator;
import com.github.commandsconsolegui.spJme.extras.FpsLimiterStateI;
import com.github.commandsconsolegui.spJme.globals.GlobalAppRefI;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class CommandsTest extends ScriptingCommandsDelegator{ //use ConsoleCommands to prevent scripts usage
//	public final BoolTogglerCmdField	btgFpsLimit=new BoolTogglerCmdField(this,false);

	public CommandsTest(){
		super();
		setAllowUserCmdOS(true);
	}
	
	@Override
	public String prepareStatsFieldText() {
		String strStatsLast = super.prepareStatsFieldText();
		
		if(EStats.MouseCursorPosition.isShow()){
			strStatsLast+=
				"xy"
					+(int)GlobalAppRefI.i().getInputManager().getCursorPosition().x
					+","
					+(int)GlobalAppRefI.i().getInputManager().getCursorPosition().y
					+";";
		}
		
		if(EStats.TimePerFrame.isShow()){
			strStatsLast+=FpsLimiterStateI.i().getSimpleStatsReport(getTPF())+";";
		}
		
		return strStatsLast; 
	}
	
//	@Override
//	public void cmdExit() {
//		GlobalAppRefI.i().stop();
//		super.cmdExit();
//	}
	
}
