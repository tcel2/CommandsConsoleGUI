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

package com.github.commandsconsolegui.misc;

import java.util.ArrayList;

/**
 * This class is also a delegator. 
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class MsgI implements IMessageListener{
	private static MsgI instance = new MsgI();
	public static MsgI i(){return instance;}
	
	private boolean bDebug=false;
	
	private ArrayList<IMessageListener> aimsgList = new ArrayList<IMessageListener>();
	
	public void addListener(IMessageListener... aimsg){
		for(IMessageListener imsg:aimsg){
			if(!aimsgList.contains(imsg)){
				aimsgList.add(imsg);
			}
		}
	}
	
//	/**
//	 * 
//	 * @param str
//	 * @param bSuccess
//	 * @param objOwner use "this"
//	 */
//	public void dbg(String str, boolean bSuccess, Object objOwner){
//		if(!bDebug)return;
//		
//		if(!bSuccess){
//			int i,i2=0;
//			i=i2;i2=i;
//		}
//		
//		System.err.println(MsgI.class.getName()+":DBG: "
//			+new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()))+": "
//			+(objOwner!=null ? objOwner.getClass().getName()+": " : "")
//			+(bSuccess?"ok":"FAIL")+": "
//			+str);
//		
//		if(!bSuccess){
//			Thread.dumpStack();
//		}
//	}
	
	@Override
	public boolean warn(String str, Object... aobj){
		boolean bListened=false;
		for(IMessageListener imsg:aimsgList){
			if(imsg.warn(str, aobj)){
				bListened=true;
			}
		}
		
		if(!bListened){
			System.err.println(DebugI.i().joinMessageWithObjects(true,"WARN:"+str,aobj));
		}
		
		return true;
	}

	@Override
	public boolean info(String str, Object... aobj) {
		boolean bListened=false;
		for(IMessageListener imsg:aimsgList){
			if(imsg.info(str, aobj)){
				bListened=true;
			}
		}
		
		if(!bListened){
			System.out.println(DebugI.i().joinMessageWithObjects(true,"Info:"+str,aobj));
		}
		return true;
	}

	@Override
	public boolean debug(String str, Object... aobj) {
		if(!bDebug)return true; //"be quiet" skipper, less verbose mode like 
		
		boolean bListened=false;
		for(IMessageListener imsg:aimsgList){
			if(imsg.debug(str, aobj)){
				bListened=true;
			}
		}
		
		if(!bListened){
			System.err.println(DebugI.i().joinMessageWithObjects(true,"Debug:"+str,aobj));
		}
		
		return true;
	}
	
	/**
	 * @param strMsgOverride can be null
	 */
	@Override
	public boolean exception(String strMsgOverride, Exception ex, Object... aobj) {
		boolean bListened=false;
		for(IMessageListener imsg:aimsgList){
			if(imsg.exception(strMsgOverride, ex, aobj)){
				bListened=true;
			}
		}
		
		if(!bListened){
			if(strMsgOverride==null){
				strMsgOverride=ex.getMessage();
			}
			
			System.err.println(DebugI.i().joinMessageWithObjects(true,"EXCEPTION:"+strMsgOverride,aobj));
			ex.printStackTrace();
//			Thread.dumpStack();
		}
		return true;
	}

	public void setEnableDebugMessages(boolean b) {
		this.bDebug=b;
	}

	@Override
	public boolean devInfo(String str, Object... aobj) {
		boolean bListened=false;
		for(IMessageListener imsg:aimsgList){
			if(imsg.devInfo(str, aobj)){
				bListened=true;
			}
		}
		
		if(!bListened){
			System.out.println(DebugI.i().joinMessageWithObjects(true,"DevInfo:"+str,aobj));
		}
		return true;
	}

	@Override
	public boolean devWarn(String str, Object... aobj) {
		boolean bListened=false;
		for(IMessageListener imsg:aimsgList){
			if(imsg.devWarn(str, aobj)){
				bListened=true;
			}
		}
		
		if(!bListened){
			System.err.println(DebugI.i().joinMessageWithObjects(true,"DEVWARN:"+str,aobj));
		}
		
		return true;
	}

//	public boolean infoSystemTopOverride(String str) {
//		boolean bListened=false;
//		for(IMessageListener imsg:aimsgList){
//			if(imsg.infoSystemTopOverride(str)){
//				bListened=true;
//			}
//		}
//		
//		if(!bListened){
//			System.err.println(DebugI.i().joinMessageWithObjects(true,"SystemInfo:"+str));
//		}
//		
//		return true;
//	}
	
}
