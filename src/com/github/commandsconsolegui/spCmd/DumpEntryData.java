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

package com.github.commandsconsolegui.spCmd;

import java.io.PrintStream;
import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.globals.GlobalOSAppI;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.misc.DebugI;

/**
 * dump strings will always be logged to file even if disabled.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 */
public class DumpEntryData{
	/**
	 * Beware, better do NOT change these defaults,
	 * as many usages of DumpEntry may depend on it.
	 * Maybe extend this class to have other defaults.
	 */
	private boolean bApplyNewLineRequests = false; //this is a special behavior, disabled by default
	private boolean bDumpToConsole = true;
	private boolean bUseSlowQueue = false;
//private String strLineOriginal = null;
	private String strLineBaking = null;
	private PrintStream	ps = System.out;
	private Object[]	aobjCustom;
	private String	strType;
	private String	strKey = null;
	private Throwable	ex;
	private boolean	bImportant;
	private long	lMillis;
	private boolean	bShowTime = true;
	private boolean	bShowDumpObjects;
	private long lKeyOcurrenceTimes=1;
	private ImportantMsgData	imsg;
	
	public DumpEntryData() {
		lMillis = System.currentTimeMillis(); //can be realtime as this is just information
	}
	
	public boolean isApplyNewLineRequests() {
		return bApplyNewLineRequests;
	}
	public DumpEntryData setApplyNewLineRequests(boolean bApplyNewLineRequests) {
		this.bApplyNewLineRequests = bApplyNewLineRequests;
		return this;
	}
	public boolean isDump() {
		return bDumpToConsole;
	}
	public DumpEntryData setDumpToConsole(boolean bDump) {
		this.bDumpToConsole = bDump;
		return this;
	}
	public boolean isUseSlowQueue() {
		return bUseSlowQueue;
	}
	public DumpEntryData setUseSlowQueue(boolean bUseQueue) {
		this.bUseSlowQueue = bUseQueue;
		return this;
	}
//	public String getLineOriginal() {
//		return strLineOriginal;
//	}
	
	public String getLineFinal() {
		return getLineFinal(bShowDumpObjects);
	}
	public String getLineFinal(boolean bShowObjects) {
		String str = "";
		
		if(isShowTime()){
			str+=MiscI.i().getSimpleTime(lMillis, GlobalCommandsDelegatorI.i().btgShowMiliseconds.get());
		}
		
		str+=strKey;
		if(lKeyOcurrenceTimes>1){
			str+=" (x"+lKeyOcurrenceTimes+")";
		}
		
		str=MiscI.i().joinMessageWithObjects(str, bShowObjects ? aobjCustom : null);
		
		return str;
	}
	
	/**
	 * The message as key is important to help on avoiding duplicates.
	 * So try to make them unique when it will be useful.
	 * @param strLineOriginal
	 * @return
	 */
	public DumpEntryData setKey(String strLineOriginal) {
		this.strKey = strLineOriginal;
		return this;
	}
	public String getLineBaking() {
		return strLineBaking;
	}
	public void setLineBaking(String strLineBaking) {
		this.strLineBaking = strLineBaking;
	}
	/**
	 * 
	 * @param ps set to null to skip terminal printing, can be set to System.err too.
	 * @return
	 */
	public DumpEntryData setPrintStream(PrintStream ps) {
		this.ps=ps;
		return this;
	}
	public void sendToPrintStream(){
		String strOutput=("["+GlobalOSAppI.i().getCmdConsLibSimpleId()+"]"+getLineFinal(true).replace("\t",MiscI.i().getTabAsSpaces())); //remove tabs for better compatibility (mainly with eclipse IDE source link)
		if(this.ps!=null)this.ps.println(strOutput);
	}
	public DumpEntryData setDumpObjects(Object[] aobj) {
		this.aobjCustom=aobj;
		return this;
	}
	public DumpEntryData setImportant(String strType, String strKey, Throwable ex) {
		this.strType=strType;
		this.strKey=strKey;
		this.ex=ex;
		this.bImportant=true;
		return this;
	}
	public boolean isImportant() {
		return bImportant;
	}
	public String getType() {
		return strType;
	}
	public String getKey() {
		return strKey;
	}
	public Throwable getException() {
		return ex;
	}

	public DumpEntryData setShowTime(boolean bShowTime) {
		this.bShowTime=bShowTime;
		return this;
	}
	
	public boolean isShowTime(){
		return bShowTime;
	}

	public Object[] getCustomObjects() {
		return aobjCustom;
	}

	public boolean isDumpToConsole() {
		return bDumpToConsole;
	}

	public DumpEntryData setShowDumpObjects(boolean b) {
		this.bShowDumpObjects=b;
		return this;
	}
	
	public boolean isShowDumpObjects(){
		return bShowDumpObjects;
	}

	public long getKeyOcurrenceTimes() {
		return lKeyOcurrenceTimes;
	}

	public void incKeyOcurrenceTimes() {
		this.lKeyOcurrenceTimes++;
	}

	public ImportantMsgData getImportantMessageLink() {
		return imsg;
	}

	public void setImportantMessageLink(ImportantMsgData imsg) {
		this.imsg=imsg;
	}
}

