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

package console;

import java.util.Comparator;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class Command {
	String strBaseCmd;
	String strComment;
	IConsoleCommandListener icclOwner;
	
	
	
	public String getBaseCmd() {
		return strBaseCmd;
	}
	public void setBaseCmd(String strBaseCmd) {
		this.strBaseCmd = strBaseCmd;
	}
	public String getComment() {
		return strComment;
	}
	public void setComment(String strComment) {
		this.strComment = strComment;
	}
	public IConsoleCommandListener getOwner() {
		return icclOwner;
	}
	public void setOwner(IConsoleCommandListener icclOwner) {
		this.icclOwner = icclOwner;
	}
	
	public Command(IConsoleCommandListener icclOwner, String strBaseCmd, String strComment) {
		super();
		this.icclOwner = icclOwner;
		this.strBaseCmd = strBaseCmd;
		this.strComment = strComment;
	}
	
	public static CommandComparator comparator(){
		return cmp;
	}
	
	private static CommandComparator cmp = new CommandComparator();
	private static class CommandComparator implements Comparator<Command>{
		@Override
		public int compare(Command c1, Command c2){
			return c1.strBaseCmd.compareToIgnoreCase(c2.strBaseCmd);
		}
	}
	public String asHelp() {
		return strBaseCmd+" "
			+strComment+" "
			+"("+(icclOwner!=null ? icclOwner.getClass().getSimpleName() : "Root")+")";
	}
	
}