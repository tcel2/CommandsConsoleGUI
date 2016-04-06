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

package jmestates;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import misc.BoolTogglerCmd;
import misc.Debug;
import misc.Misc;
import misc.ReflexFill.IReflexFillCfg;
import misc.ReflexFill.IReflexFillCfgVariant;
import misc.ReflexFill.ReflexFillCfg;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;

import console.ConsoleCommands;

/**
 * Locks have a short timeout.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class SingleInstanceState implements IReflexFillCfg, AppState{
	public final BoolTogglerCmd	btgSingleInstaceMode = new BoolTogglerCmd(this,true,BoolTogglerCmd.strTogglerCodePrefix,
		"better keep this enabled, other instances may conflict during files access.");
	private boolean bDevModeExitIfThereIsANewerInstance = true; //true if in debug mode
//	public final BoolToggler	btgSingleInstaceOverrideOlder = new BoolToggler(this,false,ConsoleCommands.strTogglerCodePrefix,
//		"If true, any older instance will exit and this will keep running."
//		+"If false, the oldest instance will keep running and this one will exit.");
	String strPrefix=SingleInstanceState.class.getSimpleName()+"-";
	String strSuffix=".lock";
	String strId;
	private File	flSelfLock;
	private SimpleApplication	sapp;
	private ConsoleCommands	cc;
	private BasicFileAttributes	attrSelfLock;
	private boolean	bInitialized;
	private boolean	bEnabled = true;
	private FilenameFilter	fnf;
	private File	flFolder;
	private long lLockUpdateTargetDelayMilis=3000;
	private long	lSelfLockCreationTime;
	private String	strExitReasonOtherInstance = "";
	public int	lCheckCountsTD;
	private boolean	bExitApplicationTD;
	public long	lCheckTotalDelay;
	
	private static SingleInstanceState instance = new SingleInstanceState();
	public static SingleInstanceState i(){return instance;}
	
	private File[] getAllLocksTD(){
		return flFolder.listFiles(fnf);
	}
	
//	/**
//	 * This will happen if newer instances are to exit promptly
//	 * allowing only the older instance to remain running.
//	 * 
//	 * So, broken locks will be cleaned.
//	 */
//	private void clearBrokenLocks(){
//		if(bDevModeExitIfThereIsANewerInstance)return;
//			
//		for(File fl:getAllLocks()){
//			if(cmpSelfWith(fl))continue;
//			output("Cleaning lock: "+fl.getName());
//			fl.delete();
//		}
//	}
	
	/**
	 * Clear locks that have not been updated lately.
	 */
	private void clearOldLocksTD(){
		for(File fl:getAllLocksTD()){
			if(cmpSelfWithTD(fl))continue;
			
			BasicFileAttributes attr = Misc.i().fileAttributesTS(fl);
			if(attr==null)continue;
			
			long lTimeLimit = System.currentTimeMillis() - (lLockUpdateTargetDelayMilis*2);
			if(attr.lastModifiedTime().toMillis() < lTimeLimit){
				outputTD("Cleaning old lock: "+fl.getName());
				fl.delete();
			}
		}
	}
	
	private boolean cmpSelfWithTD(File fl){
//		System.out.println(">>>"+flSelfLock.getName()()+">>>"+(fl.getAbsolutePath()));
		return flSelfLock.getName().equalsIgnoreCase(fl.getName());
	}
	
	private boolean checkExitTD(){
		if(!btgSingleInstaceMode.b())return false;
		
		bExitApplicationTD=false;
		String strReport="";
		strReport+="-----------------SimultaneousLocks--------------------\n";
		strReport+="ThisLock:  "+flSelfLock.getName()+" "+getSelfMode(true)+"\n";
		int iSimultaneousLocksCount=0;
		for(File flOtherLock:getAllLocksTD()){
			if(cmpSelfWithTD(flOtherLock))continue;
			
			Long lOtherCreationTime = getCreationTimeOfTD(flOtherLock);
			if(lOtherCreationTime==null)continue;
			
			ERunMode ermOther = getLockRunModeOfTD(flOtherLock);
			strReport+="OtherLock: "+flOtherLock.getName()+" "+getMode(ermOther,true)+"\n";
			
			boolean  bOtherIsNewer = lSelfLockCreationTime < lOtherCreationTime;
			
			if(bDevModeExitIfThereIsANewerInstance && bOtherIsNewer){
				applyExitReason("NEWER"+getMode(ermOther,true));
			}else
			if(!bDevModeExitIfThereIsANewerInstance && !bOtherIsNewer){
				/**
				 * exit if there is an older instance
				 */
				applyExitReason("OLDER"+getMode(ermOther,true));
			}
			
			/**
			 * the priority is to keep the release mode instance running
			 * despite.. it may never happen at the end user machine...
			 */
			if(bExitApplicationTD){
				if(!bSelfIsDebugMode){ // self is release mode
					if(ermOther.compareTo(ERunMode.Debug)==0){
						/**
						 * will ignore exit request if the other is in debug mode.
						 */
						bExitApplicationTD=false;
					}
				}
			}else{
				/**
				 * If the other instance is in release mode,
				 * this debug mode instance will exit.
				 * 
				 * To test, start a release mode, and AFTER that, start a debug mode one.
				 */
				if(bSelfIsDebugMode){
					if(ermOther.compareTo(ERunMode.Release)==0){
						applyExitReason(ermOther.toString());
					}
				}
			}
			
			iSimultaneousLocksCount++;
		}
		
		if(bExitApplicationTD){
			outputTD(strReport);
		}else{
			if(iSimultaneousLocksCount>0){
				outputTD(strReport+"This instance will continue running.");
				clearOldLocksTD();
			}
		}
		
		lCheckCountsTD++;
		
		return bExitApplicationTD;
	}
	
	private void applyExitReason(String str){
		strExitReasonOtherInstance=str;
		bExitApplicationTD=true;
	}
	
	class ThreadChecker implements Runnable{
		@Override
		public void run() {
			long lStartMilis = System.currentTimeMillis();
			
			long lIncStep=50;
			long lLockUpdateFastInitDelayMilis=lIncStep;
			while(threadMain==null || threadMain.isAlive()){ //null means not configured yet
				try {
					if(!flSelfLock.exists()){
						outputTD("Lock was deleted, recreating: "+flSelfLock.getName());
						createSelfLockFileTD();
					}
					
					if(checkExitTD()){
						outputTD("Other "+strExitReasonOtherInstance+" instance is running, exiting this...");
						flSelfLock.delete();
						break;
					}
					
					/**
					 * This will also update the file creation time...
					 */
					flSelfLock.setLastModified(System.currentTimeMillis());
					
					/**
					 * sleep after to help on avoiding allocating resources.
					 */
					Thread.sleep(lLockUpdateFastInitDelayMilis);
					lCheckTotalDelay+=lLockUpdateFastInitDelayMilis;
					
					if(lLockUpdateFastInitDelayMilis<lLockUpdateTargetDelayMilis){
						lLockUpdateFastInitDelayMilis+=lIncStep;
//						outputTD("update delay milis "+lLockUpdateFastInitDelayMilis);
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
					flSelfLock.delete();
				}
			}
			
			long lDelayMilis = System.currentTimeMillis()-lStartMilis;
			
			if(threadMain!=null && !threadMain.isAlive()){
				outputTD("Main thread ended.");
			}
			
			outputTD("Checked times: "+lCheckCountsTD);
			outputTD("Checked total delay (milis): "+lCheckTotalDelay);
			outputTD("Lasted for "+Misc.i().fmtFloat(lDelayMilis/1000f,3)+"s");
			
			System.exit(0);
		}

	}
	
//	String strDebugMode="DebugMode";
//	String strReleaseMode="ReleaseMode";
	private boolean	bConfigured;
	private String	strErrorMissingValue="ERROR_MISSING_VALUE";
	private Boolean	bSelfIsDebugMode;
	private Thread	threadMain;
	private Thread	threadChecker;
	private int	lWaitCount;
	private boolean	bAllowCfgOutOfMainMethod = false;
	
	private Long getCreationTimeOfTD(File fl){
		ArrayList<String> astr = Misc.i().fileLoad(fl);
		Long l = null;
		if(astr.size()>0){
			// line 1
			try{l = Long.parseLong(astr.get(0));}catch(NumberFormatException ex){};
		}
		return l;
	}
	
	private enum ERunMode{
		Debug,
		Release,
		Undefined,
		;
	}
	
	/**
	 * LINE 2
	 * @param fl
	 * @return
	 */
	private ERunMode getLockRunModeOfTD(File fl){
		ArrayList<String> astr = Misc.i().fileLoad(fl);
		try{return ERunMode.valueOf(astr.get(1));}catch(IllegalArgumentException|IndexOutOfBoundsException e){}
		return ERunMode.Undefined;
	}
	
	/**
	 * 
	 * @param fl
	 * @return if not found, returns a missing value indicator string.
	 */
	@Deprecated
	private String getLockModeOfTD(File fl){
		ArrayList<String> astr = Misc.i().fileLoad(fl);
		if(astr.size()>0)return astr.get(1); // line 2
		return strErrorMissingValue;
	}
	
	private String getSelfMode(boolean bReportMode){
		return getMode((bSelfIsDebugMode?ERunMode.Debug:ERunMode.Release), bReportMode);
	}
	private String getMode(ERunMode erm, boolean bReportMode){
		return ""
			+(bReportMode?"(":"")
			+erm
			+(bReportMode?")":"")
			;
	}
	
	private void createSelfLockFileTD() {
		try{
			flSelfLock.createNewFile();
			
			ArrayList<String> astr = new ArrayList<String>();
			// line 1
			astr.add(""+lSelfLockCreationTime);
			// line 2
			astr.add(getSelfMode(false));
			
			Misc.i().fileAppendListTS(flSelfLock, astr);
			
			attrSelfLock = Misc.i().fileAttributesTS(flSelfLock);
			
			outputTD("Created lock: "+flSelfLock.getName()+" "+getSelfMode(true));
			
			flSelfLock.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
			throw new NullPointerException("unable to create lock file "+flSelfLock.getAbsolutePath());
		}
	}
	
//	private String otherPrefixInfo(){
//		String strOther="Other ";
//		if(bDevModeExitIfThereIsANewerInstance){
//			strOther+="NEWER";
//		}else{
//			strOther+="OLDER";
//		}
//		if(Debug.i().isInIDEdebugMode())strOther+="(DebugMode)";
//		return strOther+" instance";
//	}
	
	public void configureBeforeInitializing(SimpleApplication sapp){
		configureBeforeInitializing(sapp,false);
	}
	
	/**
	 * 
	 * @param sapp
	 * @param bAllowCfgOutOfMainMethod use this to skip the resources allocation preventer code checker {@link #assertFlowAtMainMethodThread()}
	 */
	public void configureBeforeInitializing(SimpleApplication sapp, boolean bAllowCfgOutOfMainMethod){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		
		this.sapp=sapp;
		this.bAllowCfgOutOfMainMethod=bAllowCfgOutOfMainMethod;
//		this.cc=cc;
//		this.threadMain=threadMain;
		
		bSelfIsDebugMode = Debug.i().isInIDEdebugMode();
		
//		if(Debug.i().isInIDEdebugMode())strPrefix="DebugMode-"+strPrefix;
		
		lSelfLockCreationTime = System.currentTimeMillis();
		strId=strPrefix+Misc.i().getDateTimeForFilename(lSelfLockCreationTime,true)+strSuffix;
		flSelfLock = new File(strId);
		
		flFolder = new File("./");
		fnf = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.startsWith(strPrefix) && name.endsWith(strSuffix))return true;
				return false;
			}
		};
		
		bDevModeExitIfThereIsANewerInstance = bSelfIsDebugMode;
		
		if(bSelfIsDebugMode){
			outputTD("This instance is in DEBUG mode. ");
		}
		
		if(!sapp.getStateManager().attach(this)){
			throw new NullPointerException("already attached state "+this.getClass().getName());
		}
		
		/**
		 * creating the new thread here will make the application ends faster if it can.
		 */
		clearOldLocksTD();
		createSelfLockFileTD();
		threadChecker = new Thread(new ThreadChecker());
		threadChecker.start();
		
		/**
		 * this will sleep the main thread (the thread configuring this class)
		 */
		threadSleepWaitSingleInstanceFastCheck();
		
		bConfigured=true;
	}
	
	private void assertFlowAtMainMethodThread(){
		StackTraceElement[] ast = Thread.currentThread().getStackTrace();
		boolean bIsFromMainMethod=false;
		for(StackTraceElement ste:ast){
			if(ste.getMethodName()=="main"){
				bIsFromMainMethod=true;
				break;
			}
		}
		
		if(!bIsFromMainMethod){
			outputTD(
				"The flow that reaches this method must be called at 'main()'. " 
				+"This must be called before the main window shows up, what will allocate resources."
				+"Alternatively, skip it by allowing configuration out of 'main()' method."
			);
			Thread.dumpStack();
			System.exit(1);
		}
	}
	
	/**
	 * This helps on avoiding allocating too much resources.
	 */
	private void threadSleepWaitSingleInstanceFastCheck(){
		if(!bAllowCfgOutOfMainMethod)assertFlowAtMainMethodThread();
		
		try {
			long lWaitDelayMilis=100;
			long lMaxDelayToWaitForChecksMilis=1000;
			while(true){
				if(bExitApplicationTD){
					/**
					 * if the application is exiting, keep sleeping.
					 */
					Thread.sleep(lWaitDelayMilis);
				}else{
//					if(lCheckCountsTD==0){
					if(lCheckTotalDelay<lMaxDelayToWaitForChecksMilis){
						Thread.sleep(lWaitDelayMilis);
					}else{
						/**
						 * initial check allowed this instance of the application
						 * to continue running 
						 */
						break;
					}
				}
				lWaitCount++;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		outputTD("Waited times: "+lWaitCount);
	}
	
	private void outputTD(String str){
		System.err.println(""
			+"["+SingleInstanceState.class.getSimpleName()+"]"
			+Misc.i().getSimpleTime(true)
			+": "
			+str.replace("\n", "\n\t"));
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return cc.getReflexFillCfg(rfcv);
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		bInitialized=true;
	}

	@Override
	public boolean isInitialized() {
		return bInitialized;
	}

	@Override
	public void setEnabled(boolean active) {
		this.bEnabled=active;
	}

	@Override
	public boolean isEnabled() {
		return bEnabled;
	}

	@Override
	public void stateAttached(AppStateManager stateManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(float tpf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(RenderManager rm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postRender() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanup() {
		flSelfLock.delete();
	}

	public void configureBeforeInitializing(ConsoleCommands cc,Thread threadMain) {
		this.cc=cc;
		this.threadMain=threadMain;
	}
	
}