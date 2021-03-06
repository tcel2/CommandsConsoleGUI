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

package com.github.commandsconsolegui.spJme.cmd;

import java.lang.reflect.Field;

import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spJme.globals.console.GlobalJmeConsoleUII;

/**
 * This is not a thread.
 * This is a state, at the main thread, that will keep the console running
 * while it is closed.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class CommandsBackgroundStateI extends CmdConditionalStateAbs<CommandsBackgroundStateI> {
	private static CommandsBackgroundStateI instance = new CommandsBackgroundStateI();
	public static CommandsBackgroundStateI i(){return instance;}
	
	private final BoolTogglerCmdField	btgExecCommandsInBackground=new BoolTogglerCmdField(this, true,
		"Will continue running console commands even if console is closed.").setCallNothingOnChange();
	
//	private IConsoleUI	icui;
	
	public CommandsBackgroundStateI() {
		setPrefixCmdWithIdToo(true);
	}
	
	public static class CfgParm extends CmdConditionalStateAbs.CfgParm{
		public CfgParm() {
			super(null);
		}
	}
	@Override
	public CommandsBackgroundStateI configure(ICfgParm icfg){
		@SuppressWarnings("unused")
		CfgParm cfg = (CfgParm)icfg;
//		if(cfg.icui==null)throw new NullPointerException("invalid instance for "+IConsoleUI.class.getName());
//		this.icui = cfg.icui;
//		super.configure(new CmdConditionalStateAbs.CfgParm(CommandsBackgroundStateI.class.getSimpleName()));
		super.configure(icfg);
		
//		return storeCfgAndReturnSelf(cfg);
		return getThis();
	}
	
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!btgExecCommandsInBackground.b())return true; //this is an OK state, no failure!
		
		if(GlobalJmeConsoleUII.i().isEnabled())return true; //will be foreground execution, this is an OK state, no failure!
		
		/**
		 * This way, being controlled by JME state update, the commands will happen in the same 
		 * rate they would with the console foreground state.
		 */
		cd().update(tpf);
		
		return super.updateAttempt(tpf);
	}
	
	@Override
	protected boolean initCheckPrerequisites() {
		if(!cd().isInitialized())return false;
		return super.initCheckPrerequisites();
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=CommandsBackgroundStateI.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=CommandsBackgroundStateI.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}

	@Override
	protected CommandsBackgroundStateI getThis() {
		return this;
	}
}
