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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import misc.BoolToggler;
import misc.Debug;
import misc.IHandleExceptions;
import misc.Misc;
import misc.ReflexFill.IReflexFillCfg;
import misc.ReflexFill.IReflexFillCfgVariant;
import misc.ReflexFill.ReflexFillCfg;
import misc.StringField;
import misc.TimedDelay;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.jme3.app.SimpleApplication;

import console.gui.ConsoleGUILemurState;
import console.gui.ConsoleGuiStateAbs;
import console.gui.ConsoleGuiStateAbs.PreQueueCmdsBlockSubList;
import console.gui.LemurGuiExtraFunctionalitiesHK;

/**
 * All methods starting with "cmd" are directly accessible by user console commands.
 * Here are all base command related methods.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleCommands implements IReflexFillCfg, IHandleExceptions{
	/**
	 * TODO temporary variable used only during methods migration, commands class must not depend/know about state class.
	 */
	public ConsoleGuiStateAbs csaTmp = null;
	
	public SimpleApplication	sapp;
	
	// not public... development token... 
	public final String	TOKEN_CMD_NOT_WORKING_YET = "[NOTWORKINGYET]";
	
	/**
	 * Togglers:
	 * 
	 * Adding a toggler field on any class, 
	 * will automatically create the related console command!
	 */
	public static final String strTogglerCodePrefix="btg";
	public BoolToggler	btgDbAutoBkp = new BoolToggler(this,false,strTogglerCodePrefix, 
		"whenever a save happens, if the DB was modified, a backup will be created of the old file");
	public BoolToggler	btgShowWarn = new BoolToggler(this,true,strTogglerCodePrefix);
	public BoolToggler	btgShowInfo = new BoolToggler(this,true,strTogglerCodePrefix);
	public BoolToggler	btgShowException = new BoolToggler(this,true,strTogglerCodePrefix);
	public BoolToggler	btgEngineStatsView = new BoolToggler(this,false,strTogglerCodePrefix);
	public BoolToggler	btgEngineStatsFps = new BoolToggler(this,false,strTogglerCodePrefix);
	public BoolToggler	btgShowMiliseconds=new BoolToggler(this,false,strTogglerCodePrefix);
	public BoolToggler	btgFpsLimit=new BoolToggler(this,false,strTogglerCodePrefix);
	public BoolToggler	btgConsoleCpuRest=new BoolToggler(this,false,strTogglerCodePrefix,
		"Console update steps will be skipped if this is enabled.");
	public BoolToggler	btgAutoScroll=new BoolToggler(this,true,strTogglerCodePrefix);
	public BoolToggler	btgExecCommandsInBackground=new BoolToggler(this,false,strTogglerCodePrefix,
		"Will continue running console commands even if console is closed.");
	public BoolToggler	btgUseFixedLineWrapModeForAllFonts=new BoolToggler(this,false,strTogglerCodePrefix,
		"If enabled, this will use a fixed line wrap column even for non mono spaced fonts, "
		+"based on the width of the 'W' character. Otherwise it will dynamically guess the best "
		+"fitting string size.");
	
	/**
	 * Developer vars, keep together!
	 * Initialy true, the default init will disable them.
	 */
	public BoolToggler	btgShowDebugEntries=new BoolToggler(this,true,strTogglerCodePrefix);
	public BoolToggler	btgShowDeveloperInfo=new BoolToggler(this,true,strTogglerCodePrefix);
	public BoolToggler	btgShowDeveloperWarn=new BoolToggler(this,true,strTogglerCodePrefix);
	public BoolToggler	btgShowExecQueuedInfo=new BoolToggler(this,true,strTogglerCodePrefix);
	
	/**
	 * keep delayers together!
	 */
	public TimedDelay tdLetCpuRest = new TimedDelay(0.1f);
	public TimedDelay tdStatsRefresh = new TimedDelay(0.5f);
	public TimedDelay tdDumpQueuedEntry = new TimedDelay(1f/5f); // per second
	public TimedDelay tdSpareGpuFan = new TimedDelay(1.0f/60f); // like 60 FPS
	
	/**
	 * used to hold a reference to the identified/typed user command
	 */
	public BoolToggler	btgReferenceMatched;
	
	/**
	 * user can type these below at console (the actual commands are prepared by reflex)
	 */
	public static final String strFinalCmdCodePrefix="CMD_";
//	public final StringField CMD_CLOSE_CONSOLE = new StringField(this,strFinalCmdCodePrefix);
//	public final StringField CMD_CONSOLE_HEIGHT = new StringField(this,strFinalCmdCodePrefix);
//	public final StringField CMD_CONSOLE_SCROLL_BOTTOM = new StringField(this,strFinalCmdCodePrefix);
//	public final StringField CMD_CONSOLE_STYLE = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_DB = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_ECHO = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FIX_CURSOR = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FIX_LINE_WRAP = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FIX_VISIBLE_ROWS_AMOUNT = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_HELP = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_HISTORY = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_HK_TOGGLE = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_LINE_WRAP_AT = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_VAR_SET = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_SLEEP = new StringField(this,strFinalCmdCodePrefix);
	
	/**
	 * this char indicates something that users (non developers) 
	 * should not have direct access.
	 */
	public final Character	RESTRICTED_TOKEN	= '&';
	public final String strFinalFieldRestrictedCmdCodePrefix="RESTRICTED_CMD_";
	public final StringField	RESTRICTED_CMD_SKIP_CURRENT_COMMAND	= new StringField(this,strFinalFieldRestrictedCmdCodePrefix);
	public final StringField	RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE	= new StringField(this,strFinalFieldRestrictedCmdCodePrefix);
	public final StringField	RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS	= new StringField(this,strFinalFieldRestrictedCmdCodePrefix);
	public final StringField	RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS	= new StringField(this,strFinalFieldRestrictedCmdCodePrefix);
	
	/**
	 * more tokens
	 */
	public Character	chCommandDelimiter = ';';
	public Character	chAliasPrefix = '$';
	public Character	chVariableExpandPrefix = chAliasPrefix;
	public Character	chFilterToken = '~';
	public	Character chAliasBlockedToken = '-';
	public Character	chAliasAllowedToken = '+';
	public	Character chVarDeleteToken = '-';
	public	Character	chCommentPrefix='#';
	public	Character	chCommandPrefix='/';
	
	/**
	 * etc
	 */
	public String	strTypeCmd="Cmd";
	
	/** 0 is auto wrap, -1 will trunc big lines */
	public int iConsoleMaxWidthInCharsForLineWrap = 0;
	
	public boolean	bAddEmptyLineAfterCommand = true;
	public IConsoleUI	icui;
	public boolean bStartupCmdQueueDone = false; 
	public CharSequence	strReplaceTAB = "  ";
	public int	iCopyFrom = -1;
	public int	iCopyTo = -1;
	public int	iCmdHistoryCurrentIndex = 0;
	public String	strValidCmdCharsRegex = "a-zA-Z0-9_"; // better not allow "-" as has other uses like negate number and commands functionalities
//	public String	strStatsLast = "";
	public boolean	bLastAliasCreatedSuccessfuly;
	public float	fTPF;
	public long	lNanoFrameTime;
	public long	lNanoFpsLimiterTime;
	public String	strFilePrefix = "Console"; //ConsoleStateAbs.class.getSimpleName();
	public String	strFileTypeLog = "log";
	public String	strFileTypeConfig = "cfg";
	public String	strFileCmdHistory = strFilePrefix+"-CmdHist";
	public String	strFileLastDump = strFilePrefix+"-LastDump";
	public String	strFileInitConsCmds = strFilePrefix+"-Init";
	public String	strFileSetup = strFilePrefix+"-Setup";
	public String	strFileDatabase = strFilePrefix+"-DB";
	public int iMaxCmdHistSize = 1000;
	public int iMaxDumpEntriesAmount = 100000;
	public ArrayList<String>	astrCmdAndParams = new ArrayList<String>();
	public ArrayList<String>	astrExecConsoleCmdsQueue = new ArrayList<String>();
	public ArrayList<PreQueueCmdsBlockSubList>	astrExecConsoleCmdsPreQueue = new ArrayList<PreQueueCmdsBlockSubList>();
	public String	strCmdLinePrepared = "";
	public TreeMap<String,Object> tmUserVariables = 
		new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	public TreeMap<String,Object> tmRestrictedVariables =
		new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	public File	flCmdHist;
	public File	flLastDump;
	public File	flInit;
	public File	flDB;
	public File	flSetup;
	public ArrayList<DumpEntry> adeDumpEntryFastQueue = new ArrayList<DumpEntry>();
	public String	strInfoEntryPrefix			=". ";
	public String	strWarnEntryPrefix			="?Warn: ";
	public String	strErrorEntryPrefix			="!ERROR: ";
	public String	strExceptionEntryPrefix	="!EXCEPTION: ";
	public String	strDevWarnEntryPrefix="?DevWarn: ";
	public String	strDevInfoEntryPrefix=". DevInfo: ";
	public String	strSubEntryPrefix="\t";
	public Boolean	bIfConditionExecCommands;
	public ArrayList<ConditionalNested> aIfConditionNestedList = new ArrayList<ConditionalNested>();
	public Boolean	bIfConditionIsValid;
	public String	strCmdLineOriginal;
	public ArrayList<String> astrCmdHistory = new ArrayList<String>();
	public ArrayList<String> astrCmdWithCmtValidList = new ArrayList<String>();
	public ArrayList<String> astrBaseCmdValidList = new ArrayList<String>();
	public ArrayList<Alias> aAliasList = new ArrayList<Alias>();
	
//	/**
//	 * instance
//	 */
//	public static ConsoleCommands instance;
//	public static ConsoleCommands i(){return instance;}
//	public ConsoleCommands(){
//		instance=this;
//	}
	
	public ConsoleCommands(){ //IConsoleUI icg) {
//		this();
//		this.icui=icg;
		
		new Debug(this);
		Misc.i().setExceptionHandler(this);
//		addConsoleCommandListener(Debug.i());
//		Debug.i().setConsoleCommand(this);
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		if(rfcv.getClass().isAssignableFrom(BoolToggler.class)){
			if(strTogglerCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
				rfcfg.strCommandSuffix="Toggle";
			}
		}else
		if(rfcv.getClass().isAssignableFrom(StringField.class)){
			if(strFinalCmdCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
			}else
			if(strFinalFieldRestrictedCmdCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
				rfcfg.strCommandPrefix=""+RESTRICTED_TOKEN;
			}
		}
		
		return rfcfg;
	}
	
	public Character getCommandDelimiter() {
		return chCommandDelimiter;
	}
	public String getCommandDelimiterStr() {
		return ""+chCommandDelimiter;
	}
	public ConsoleCommands setCommandDelimiter(Character chCommandDelimiter) {
		this.chCommandDelimiter = chCommandDelimiter;
		return this;
	}
	public Character getAliasPrefix() {
		return chAliasPrefix;
	}
	public ConsoleCommands setAliasPrefix(Character chAliasPrefix) {
		this.chAliasPrefix = chAliasPrefix;
		return this;
	}
	public Character getVariableExpandPrefix() {
		return chVariableExpandPrefix;
	}
	public ConsoleCommands setVariableExpandPrefix(Character chVariableExpandPrefix) {
		this.chVariableExpandPrefix = chVariableExpandPrefix;
		return this;
	}
	public Character getFilterToken() {
		return chFilterToken;
	}
	public ConsoleCommands setFilterToken(Character chFilterToken) {
		this.chFilterToken = chFilterToken;
		return this;
	}
	public Character getAliasBlockedToken() {
		return chAliasBlockedToken;
	}
	public ConsoleCommands setAliasBlockedToken(Character chAliasBlockedToken) {
		this.chAliasBlockedToken = chAliasBlockedToken;
		return this;
	}
	public Character getAliasAllowedToken() {
		return chAliasAllowedToken;
	}
	public ConsoleCommands setAliasAllowedToken(Character chAliasAllowedToken) {
		this.chAliasAllowedToken = chAliasAllowedToken;
		return this;
	}
	public Character getVarDeleteToken() {
		return chVarDeleteToken;
	}
	public String getVarDeleteTokenStr() {
		return ""+chVarDeleteToken;
	}
	public ConsoleCommands setVarDeleteToken(Character chVarDeleteToken) {
		this.chVarDeleteToken = chVarDeleteToken;
		return this;
	}
	public Character getCommentPrefix() {
		return chCommentPrefix;
	}
	public ConsoleCommands setCommentPrefix(Character chCommentPrefix) {
		this.chCommentPrefix = chCommentPrefix;
		return this;
	}
	public Character getCommandPrefix() {
		return chCommandPrefix;
	}
	public ConsoleCommands setCommandPrefix(Character chCommandPrefix) {
		this.chCommandPrefix = chCommandPrefix;
		return this;
	}
	
	public String commentToAppend(String strText){
		strText=strText.trim();
		if(strText.startsWith(getCommentPrefixStr())){
			strText=strText.substring(1);
		}
		return " "+getCommentPrefix()+strText;
	}
	public String getCommentPrefixStr() {
		return ""+chCommentPrefix;
	}
	public String getCommandPrefixStr() {
		return ""+chCommandPrefix;
	}
	
	public void cmdExit(){
		sapp.stop();
		System.exit(0);
	}
	
	public boolean checkCmdValidityBoolTogglers(){
		btgReferenceMatched=null;
		for(BoolToggler btg : BoolToggler.getBoolTogglerListCopy()){
			if(checkCmdValidity(btg.getCmdId(), "[bEnable] "+btg.getHelp(), true)){
				btgReferenceMatched = btg;
				break;
			}
		}
		return btgReferenceMatched!=null;
	}
	public boolean checkCmdValidity(String strValidCmd){
		return checkCmdValidity(strValidCmd, null);
	}
	public boolean checkCmdValidity(StringField strfValidCmd, String strComment){
		return checkCmdValidity(strfValidCmd.toString(), strComment);
	}
	public boolean checkCmdValidity(String strValidCmd, String strComment){
		return checkCmdValidity(strValidCmd, strComment, false);
	}
	public boolean checkCmdValidity(String strValidCmd, String strComment, boolean bSkipSortCheck){
		if(strCmdLinePrepared==null){
			if(strComment!=null){
				strValidCmd+=commentToAppend(strComment);
			}
			
			addCmdToValidList(strValidCmd,bSkipSortCheck);
			
			return false;
		}
		
		if(RESTRICTED_CMD_SKIP_CURRENT_COMMAND.equals(strCmdLinePrepared))return false;
		if(isCommentedLine())return false;
		if(strCmdLinePrepared.trim().isEmpty())return false;
		
//		String strCheck = strPreparedCmdLine;
//		strCheck = strCheck.trim().split(" ")[0];
		strValidCmd = strValidCmd.trim().split(" ")[0];
		
//		return strCheck.equalsIgnoreCase(strValidCmd);
		return paramString(0).equalsIgnoreCase(strValidCmd);
	}
	
	public boolean cmdEcho() {
		String strToEcho="";
		String strPart="";
		int iParam=1;
		while(strPart!=null){
			strToEcho+=strPart;
			strToEcho+=" ";
			strPart = paramString(iParam++);
		}
		strToEcho=strToEcho.trim();
		
		dumpEntry(strToEcho);
		
		return true;
	}

	ArrayList<IConsoleCommandListener> aConsoleCommandListenerList = new ArrayList<IConsoleCommandListener>();

	public String	strTest = "";

	private boolean	bInitialized;
	
	private void assertInitialized(){
		if(bInitialized)return;
		throw new NullPointerException(ConsoleCommands.class.getName()+" was not initialized!");
	}
	
	/**
	 * Any class can attach it's command interpreter.
	 * @param icc
	 */
	public void addConsoleCommandListener(IConsoleCommandListener icc){
		aConsoleCommandListenerList.add(icc);
	}
	
	public boolean executePreparedCommandRoot(){
		if(RESTRICTED_CMD_SKIP_CURRENT_COMMAND.equals(strCmdLinePrepared))return true;
		
		/**
		 * means the command didnt have any problem, didnt fail, requiring a warning message
		 */
		boolean bCmdEndedGracefully = false;
		
		if(checkCmdValidityBoolTogglers()){
			bCmdEndedGracefully=toggle(btgReferenceMatched);
		}else
		if(checkCmdValidity("alias",getAliasHelp(),true)){
			bCmdEndedGracefully=cmdAlias();
		}else
		if(checkCmdValidity("clearCommandsHistory")){
			astrCmdHistory.clear();
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity("clearDumpArea")){
			icui.getDumpEntries().clear();
			bCmdEndedGracefully=true;
		}else
//		if(checkCmdValidity(CMD_CLOSE_CONSOLE,"like the bound key to do it")){
//			csaTmp.setEnabled(false);
//			bCommandWorked=true;
//		}else
//		if(checkCmdValidity(CMD_CONSOLE_HEIGHT,"[fPercent] of the application window")){
//			Float f = paramFloat(1);
//			csaTmp.modifyConsoleHeight(f);
//			bCommandWorked=true;
//		}else
//		if(checkCmdValidity(CMD_CONSOLE_SCROLL_BOTTOM,"")){
//			csaTmp.scrollToBottomRequest();
//			bCommandWorked=true;
//		}else
//		if(checkCmdValidity(CMD_CONSOLE_STYLE,"[strStyleName] changes the style of the console on the fly, empty for a list")){
//			String strStyle = paramString(1);
//			if(strStyle==null)strStyle="";
//			bCommandWorked=csaTmp.cmdStyleApply(strStyle);
//		}else
		if(checkCmdValidity(CMD_DB,EDataBaseOperations.help())){
			bCmdEndedGracefully=cmdDb();
		}else
//		if(checkCmdValidity("dumpFind","<text> finds, select and scroll to it at dump area")){
//			bCommandWorkedProperly=cmdFind();
//		}else
//		if(checkCmdValidity("dumpFindNext","<text> finds, select and scroll to it at dump area")){
//			bCommandWorkedProperly=cmdFind();
//		}else
//		if(checkCmdValidity("dumpFindPrevious","<text> finds, select and scroll to it at dump area")){
//			bCommandWorkedProperly=cmdFind();
//		}else
		if(checkCmdValidity(CMD_ECHO," simply echo something")){
			bCmdEndedGracefully=cmdEcho();
		}else
		if(checkCmdValidity("editShowClipboad","--noNL")){
			String strParam1 = paramString(1);
			boolean bShowNL=true;
			if(strParam1!=null){
				if(strParam1.equals("--noNL")){
					bShowNL=false;
				}
			}
			showClipboard(bShowNL);
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity("editCopy","-d end lines with command delimiter instead of NL;")){
			bCmdEndedGracefully=csaTmp.cmdEditCopyOrCut(false);
		}else
		if(checkCmdValidity("editCut","like copy, but cut :)")){
			bCmdEndedGracefully=csaTmp.cmdEditCopyOrCut(true);
		}else
//		if(checkCmdValidity(CMD_ELSE,"conditinal block")){
//			bCommandWorked=cmdElse();
//		}else
//		if(checkCmdValidity(CMD_ELSE_IF,"<[!]<true|false>> conditional block")){
//			bCommandWorked=cmdElseIf();
//		}else
		if(checkCmdValidity("execBatchCmdsFromFile ","<strFileName>")){
			String strFile = paramString(1);
			if(strFile!=null){
				addCmdListOneByOneToQueue(Misc.i().fileLoad(strFile),false,false);
//				astrExecConsoleCmdsQueue.addAll(Misc.i().fileLoad(strFile));
				bCmdEndedGracefully=true;
			}
		}else
		if(checkCmdValidity("exit","the application")){
			cmdExit();
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity("fileShowData ","<ini|setup|CompleteFileName> show contents of file at dump area")){
			String strOpt = paramString(1);
			
			if(strOpt!=null){
				File fl = null;
				switch(strOpt){
					case "ini":
						dumpInfoEntry("Init file data: ");
						fl = flInit;
						break;
					case "setup":
						dumpInfoEntry("Setup file data: ");
						fl = flSetup;
						break;
					default:
						fl = new File(strOpt);
				}
				
				if(fl.exists()){
					for(String str : Misc.i().fileLoad(fl)){
						dumpSubEntry(str);
					}
				}else{
					dumpWarnEntry("File does not exist: "+fl.getAbsolutePath());
				}
			}
			
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity(CMD_FIX_CURSOR ,"in case cursor is invisible")){
			if(csaTmp.efHK==null){
				dumpWarnEntry("requires command: "+CMD_HK_TOGGLE);
			}else{
				dumpInfoEntry("requesting: "+CMD_FIX_CURSOR);
				csaTmp.efHK.bFixInvisibleTextInputCursorHK=true;
			}
			bCmdEndedGracefully = true;
		}else
		if(checkCmdValidity(CMD_FIX_LINE_WRAP ,"in case words are overlapping")){
			csaTmp.cmdLineWrapDisableDumpArea();
			bCmdEndedGracefully = true;
		}else
		if(checkCmdValidity(CMD_FIX_VISIBLE_ROWS_AMOUNT,"[iAmount] in case it is not showing as many rows as it should")){
			csaTmp.iVisibleRowsAdjustRequest = paramInt(1);
			if(csaTmp.iVisibleRowsAdjustRequest==null)csaTmp.iVisibleRowsAdjustRequest=0;
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity(CMD_HELP,"[strFilter] show (filtered) available commands")){
			cmdShowHelp(paramString(1));
			/**
			 * ALWAYS return TRUE here, to avoid infinite loop when improving some failed command help info!
			 */
			bCmdEndedGracefully=true; 
		}else
		if(checkCmdValidity(CMD_HISTORY,"[strFilter] of issued commands (the filter results in sorted uniques)")){
			bCmdEndedGracefully=cmdShowHistory();
		}else
		if(checkCmdValidity(CMD_HK_TOGGLE ,"[bEnable] allow hacks to provide workarounds")){
			if(paramBooleanCheckForToggle(1)){
				Boolean bEnable = paramBoolean(1);
				if(csaTmp.efHK==null && (bEnable==null || bEnable)){
					csaTmp.efHK=new LemurGuiExtraFunctionalitiesHK(csaTmp);
				}
				
				if(csaTmp.efHK!=null){
					csaTmp.efHK.bAllowHK = bEnable==null ? !csaTmp.efHK.bAllowHK : bEnable; //override
					if(csaTmp.efHK.bAllowHK){
						dumpWarnEntry("Hacks enabled!");
					}else{
						dumpWarnEntry("Hacks may not be completely disabled/cleaned!");
					}
				}
				
				bCmdEndedGracefully=true;
			}
		}else
//		if(checkCmdValidity(CMD_LINE_WRAP_AT,"[iMaxChars] -1 = will trunc big lines, 0 = wrap will be automatic")){
		if(checkCmdValidity(CMD_LINE_WRAP_AT,"[iMaxChars] 0 = wrap will be automatic")){
			Integer i = paramInt(1);
			if(i!=null && i>=0){ // a value was supplied
				iConsoleMaxWidthInCharsForLineWrap = i;
//				if(i==-1){
//					/**
//					 * prefered using null instead of -1 that is for the user type a valid integer
//					 */
//					iConsoleMaxWidthInCharsForLineWrap=null;
//				}else{
//					iConsoleMaxWidthInCharsForLineWrap=i;
//				}
			}
//			csaTmp.updateFontStuff();
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity("quit","the application")){
			cmdExit();
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity("showBinds","")){
			dumpInfoEntry("Key bindings: ");
			dumpSubEntry("Ctrl+C - copy");
			dumpSubEntry("Ctrl+X - cut");
			dumpSubEntry("Ctrl+V - paste");
			dumpSubEntry("Shift+Ctrl+V - show clipboard");
			dumpSubEntry("Shift+Click - marks dump area CopyTo selection marker for copy/cut");
			dumpSubEntry("Ctrl+Del - clear input field");
			dumpSubEntry("TAB - autocomplete (starting with)");
			dumpSubEntry("Ctrl+TAB - autocomplete (contains)");
			dumpSubEntry("Ctrl+/ - toggle input field comment");
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity("showSetup","show restricted variables")){
			for(String str:Misc.i().fileLoad(flSetup)){
				dumpSubEntry(str);
			}
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity(CMD_SLEEP,"<fDelay> [singleCmd] will wait before executing next command in the command block; alternatively will wait before executing command in-line, but then it will not sleep the block it is in!")){
			Float fSleep = paramFloat(1);
			String strCmds = paramStringConcatenateAllFrom(2);
			
			if(strCmds!=null){
				/**
				 * creates mini block
				 */
				ArrayList<String> astrCmdList = new ArrayList<String>();
				astrCmdList.add(CMD_SLEEP+" "+fSleep);
				astrCmdList.add(strCmds);
				addCmdsBlockToPreQueue(astrCmdList, false, false, "in-line sleep commands");
			}else{
				/**
				 * This mode is only used on the pre-queue, 
				 * here it is ignored.
				 */
				dumpWarnEntry(CMD_SLEEP+" without cmd, only works on command blocks like functions");
			}
			bCmdEndedGracefully=true;
		}else
//		if(checkCmdValidity("showDump","<filter> show matching entries from dump log file")){
//			String strFilter = paramString(1);
//			if(strFilter!=null){
//				for(String str:Misc.i().fileLoad(flLastDump)){
//					if(str.toLowerCase().contains(strFilter)){
//						dumpEntry(false, true, false, str);
//					}
//				}
//				bCommandWorked=true;
//			}
//		}else
		if(checkCmdValidity("statsEnable","[idToEnable [bEnable]] empty for a list. bEnable empty to toggle.")){
			bCmdEndedGracefully=true;
			String strId=paramString(1);
			Boolean bValue=paramBoolean(2);
			if(strId!=null){
				EStats e=null;
				try{e=EStats.valueOf(strId);}catch(IllegalArgumentException ex){
					bCmdEndedGracefully=false;
					dumpWarnEntry("Invalid option: "+strId+" "+bValue);
				}
				
				if(e!=null){
					e.b=bValue!=null?bValue:!e.b;
				}
			}else{
				for(EStats e:EStats.values()){
					dumpSubEntry(e.toString()+" "+e.b);
				}
			}
		}else
		if(checkCmdValidity("statsFieldToggle","[bEnable] toggle simple stats field visibility")){
			bCmdEndedGracefully=csaTmp.statsFieldToggle();
		}else
		if(checkCmdValidity("statsShowAll","show all console stats")){
			dumpAllStats();
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity("test","[...] temporary developer tests")){
			cmdTest();
			if(csaTmp.efHK!=null)csaTmp.efHK.test();
			bCmdEndedGracefully=true;
		}else
		if(checkCmdValidity("varAdd","<varId> <[-]value>")){
			bCmdEndedGracefully=cmdVarAdd(paramString(1),paramString(2),true,false);
		}else
//		if(checkCmdValidity(CMD_VAR_SET,"[<varId> <value>] | [-varId] | ["+getFilterToken()+"filter] - can be a number or a string, retrieve it's value with: ${varId}")){
		if(
			checkCmdValidity(CMD_VAR_SET,
				"<[<varId> <value>] | [-varId]> "
					+"Can be boolean(true/false, and after set accepts 1/0), number(integer/floating) or string; "
					+"-varId will delete it; "
					+"Retrieve it's value with "+getVariableExpandPrefix()+"{varId}; "
					+"Restricted variables will have no effect; "
			)
		){
			bCmdEndedGracefully=cmdVarSet();
		}else
		if(checkCmdValidity("varSetCmp","<varIdBool> <value> <cmp> <value>")){
			bCmdEndedGracefully=cmdVarSetCmp();
		}else
		if(checkCmdValidity("varShow","[["+RESTRICTED_TOKEN+"]filter] list user or restricted variables.")){
			bCmdEndedGracefully=cmdVarShow();
		}else
		if(checkCmdValidity(TOKEN_CMD_NOT_WORKING_YET+"zDisabledCommand"," just to show how to use it")){
			// keep this as reference
		}else
		{
			for(IConsoleCommandListener icc:aConsoleCommandListenerList){
				bCmdEndedGracefully = icc.executePreparedCommand();
				if(bCmdEndedGracefully)break;
			}
//			if(strCmdLinePrepared!=null){
//				if(SPECIAL_CMD_MULTI_COMMAND_LINE_OK.equals(strCmdLinePrepared)){
//					bOk=true;
//				}
//			}
		}
		
		return bCmdEndedGracefully;
	}
	
	public boolean cmdRawLineCheckAlias(){
		bLastAliasCreatedSuccessfuly=false;
		
		if(strCmdLineOriginal==null)return false;
		
		String strCmdLine = strCmdLineOriginal.trim();
		String strExecAliasPrefix = ""+getCommandPrefix()+getAliasPrefix();
		if(strCmdLine.startsWith(getCommandPrefix()+"alias ")){
			/**
			 * create
			 */
			Alias alias = new Alias(this);
			
			String[] astr = strCmdLine.split(" ");
			if(astr.length>=3){
				alias.strAliasId=astr[1];
				if(hasVar(alias.strAliasId)){
					dumpErrorEntry("Alias identifier '"+alias.strAliasId+"' conflicts with existing variable!");
					return false;
				}
				
				alias.strCmdLine=String.join(" ", Arrays.copyOfRange(astr, 2, astr.length));
				
				Alias aliasFound=getAlias(alias.strAliasId);
				if(aliasFound!=null)aAliasList.remove(aliasFound);
				
				aAliasList.add(alias);
				Misc.i().fileAppendLine(flDB, alias.toString());
				dumpSubEntry(alias.toString());
				
				bLastAliasCreatedSuccessfuly = true;
			}
			
			return bLastAliasCreatedSuccessfuly;
		}else
		if(strCmdLine.startsWith(strExecAliasPrefix)){
			/**
			 * execute
			 */
			String strAliasId=strCmdLine
				.split(" ")[0]
				.substring(strExecAliasPrefix.length())
				.toLowerCase();
			Alias alias = getAlias(strAliasId);
			if(alias!=null){
				if(!alias.bBlocked){
					addCmdToQueue(alias.strCmdLine
						+commentToAppend("alias="+alias.strAliasId), true);
					return true;
				}else{
					dumpWarnEntry(alias.toString());
				}
			}else{
				dumpWarnEntry("Alias not found: "+strAliasId);
			}
			
//			for(Alias alias:aAliasList){
//				if(!alias.strAliasId.toLowerCase().equals(strAliasId))continue;
//				
//				if(!alias.bBlocked){
//					addCmdToQueue(alias.strCmdLine
//						+commentToAppend("alias="+alias.strAliasId), true);
//					return true;
//				}else{
//					dumpWarnEntry(alias.toString());
//				}
//			}
		}
			
//		return bLastAliasCreatedSuccessfuly;
		return false;
	}

	/**
	 * provides line-wrap
	 * 
	 * @param bDumpToConsole if false, will only log to file
	 * @param strLineOriginal
	 */
	public void dumpEntry(DumpEntry de){
		dumpSave(de);
		
		if(!csaTmp.isInitialized()){
			adeDumpEntryFastQueue.add(de);
			return;
		}
		
		if(!de.bDumpToConsole)return;
		
		ArrayList<String> astrDumpLineList = new ArrayList<String>();
		if(de.strLineOriginal.isEmpty()){
			astrDumpLineList.add(de.strLineOriginal);
		}else{
			de.setLineBaking(de.strLineOriginal.replace("\t", strReplaceTAB));
			de.setLineBaking(de.getLineBaking().replace("\r", "")); //removes carriage return
			
			if(de.bApplyNewLineRequests){
				de.setLineBaking(de.getLineBaking().replace("\\n","\n")); //converts newline request into newline char
			}else{
				de.setLineBaking(de.getLineBaking().replace("\n","\\n")); //disables any newline char without losing it
			}
			
//			/**
//			 * will put the line as it is,
//			 * the UI will handle the line being truncated.
//			 */
//			if(!de.bApplyNewLineRequests){
//				if(iConsoleMaxWidthInCharsForLineWrap<0){
//					applyDumpEntryOrPutToSlowQueue(de.bUseSlowQueue, de.strLineOriginal);
//					return;
//				}
//			}
			
			int iWrapAt = iConsoleMaxWidthInCharsForLineWrap;
//			if(iConsoleMaxWidthInCharsForLineWrap==null){
			if(iConsoleMaxWidthInCharsForLineWrap==0){
				iWrapAt = icui.getLineWrapAt();
//				if(STYLE_CONSOLE.equals(strStyle)){ //TODO is faster?
//					iWrapAt = (int) (widthForDumpEntryField() / fWidestCharForCurrentStyleFont ); //'W' but any char will do for monospaced font
//				}
			}
			
			//TODO use \n to create a new line properly
			if(iWrapAt>0){ //fixed chars wrap
				/**
				 * fills each line till the wrap column or \n
				 */
				String strLineToDump="";
				boolean bDumpAdd=false;
				for (int i=0;i<de.getLineBaking().length();i++){
					char ch = de.getLineBaking().charAt(i);
					strLineToDump+=ch;
					if(ch=='\n'){
						bDumpAdd=true;
					}else
					if(strLineToDump.length()==iWrapAt){
						bDumpAdd=true;
					}else
					if(i==(de.getLineBaking().length()-1)){
						bDumpAdd=true;
					}
					
					if(bDumpAdd){
						astrDumpLineList.add(strLineToDump);
						strLineToDump="";
						bDumpAdd=false;
					}
				}
				
			}else{ 
				astrDumpLineList.addAll(icui.wrapLineDynamically(de));
			}
		}
		
		/**
		 * ADD LINE WRAP INDICATOR
		 */
		for(int i=0;i<astrDumpLineList.size();i++){
			String strPart = astrDumpLineList.get(i);
			if(i<(astrDumpLineList.size()-1)){
				if(strPart.endsWith("\n")){
					strPart=strPart.substring(0, strPart.length()-1)+"\\n"; // new line indicator
				}else{
					strPart+="\\"; // line wrap indicator
				}
			}
			
			applyDumpEntryOrPutToSlowQueue(de.bUseSlowQueue, strPart);
		}
		
	}

	public void applyDumpEntryOrPutToSlowQueue(boolean bUseSlowQueue, String str) {
		if(bUseSlowQueue){
			icui.getDumpEntriesSlowedQueue().add(str);
		}else{
			icui.getDumpEntries().add(str);
		}
	}
	
	public void updateDumpQueueEntry(){
		while(adeDumpEntryFastQueue.size()>0){
			dumpEntry(adeDumpEntryFastQueue.remove(0));
		}
			
		if(!tdDumpQueuedEntry.isReady(true))return;
		
		if(icui.getDumpEntriesSlowedQueue().size()>0){
			icui.getDumpEntries().add(icui.getDumpEntriesSlowedQueue().remove(0));
			
			while(icui.getDumpEntries().size() > iMaxDumpEntriesAmount){
				icui.getDumpEntries().remove(0);
			}
		}
	}
	
	public void dumpInfoEntry(String str){
		dumpEntry(false, btgShowInfo.get(), false, Misc.i().getSimpleTime(btgShowMiliseconds.get())+strInfoEntryPrefix+str);
	}
	
	public void dumpWarnEntry(String str){
		dumpEntry(false, btgShowWarn.get(), false, Misc.i().getSimpleTime(btgShowMiliseconds.get())+strWarnEntryPrefix+str);
	}
	
	public void dumpErrorEntry(String str){
		dumpEntry(new DumpEntry()
			.setDumpToConsole(btgShowWarn.get())
			.setLineOriginal(Misc.i().getSimpleTime(btgShowMiliseconds.get())+strErrorEntryPrefix+str)
		);
//		dumpEntry(false, btgShowWarn.get(), false, Misc.i().getSimpleTime(btgShowMiliseconds.get())+strErrorEntryPrefix+str);
	}
	
	/**
	 * warnings that should not bother end users...
	 * @param str
	 */
	public void dumpDevWarnEntry(String str){
		dumpEntry(false, btgShowDeveloperWarn.get(), false, 
				Misc.i().getSimpleTime(btgShowMiliseconds.get())+strDevWarnEntryPrefix+str);
	}
	
	public void dumpDebugEntry(String str){
		dumpEntry(new DumpEntry()
			.setDumpToConsole(btgShowDebugEntries.get())
			.setLineOriginal("[DBG]"+str)
		);
	}
	
	public void dumpDevInfoEntry(String str){
		dumpEntry(new DumpEntry()
			.setDumpToConsole(btgShowDeveloperInfo.get())
			.setLineOriginal(Misc.i().getSimpleTime(btgShowMiliseconds.get())+strDevInfoEntryPrefix+str)
		);
//		dumpEntry(false, btgShowDeveloperInfo.get(), false, 
//			Misc.i().getSimpleTime(btgShowMiliseconds.get())+strDevInfoEntryPrefix+str);
	}
	
	public void dumpExceptionEntry(Exception e){
		dumpEntry(false, btgShowException.get(), false, 
			Misc.i().getSimpleTime(btgShowMiliseconds.get())+strExceptionEntryPrefix+e.toString());
		e.printStackTrace();
	}
	
	/**
	 * a simple, usually indented, output
	 * @param str
	 */
	public void dumpSubEntry(String str){
		dumpEntry(strSubEntryPrefix+str);
	}
	
	public void addCmdToValidList(String strNew, boolean bSkipSortCheck){
		String strConflict=null;
		
//		if(!astrCmdWithCmtValidList.contains(strNew)){
		if(!strNew.startsWith(TOKEN_CMD_NOT_WORKING_YET)){
			String strBaseCmdNew = extractCommandPart(strNew,0);
			
			/**
			 * conflict check
			 */
			for(String strBase:astrBaseCmdValidList){
				if(strBase.equalsIgnoreCase(strBaseCmdNew)){
					strConflict=strBaseCmdNew;
					break;
				}
			}
			
			if(strConflict==null){
				astrBaseCmdValidList.add(strBaseCmdNew);
				astrCmdWithCmtValidList.add(strNew);
				
				/**
				 * coded sorting check (unnecessary actually), just useful for developers
				 * be more organized. 
				 */
				if(!bSkipSortCheck && astrBaseCmdValidList.size()>0){
					String strLast = astrBaseCmdValidList.get(astrBaseCmdValidList.size()-1);
					if(strLast.compareToIgnoreCase(strBaseCmdNew)>0){
						dumpDevWarnEntry("sorting required, last '"+strLast+"' new '"+strBaseCmdNew+"'");
					}
				}
			}
		}
//		}
		
		if(strConflict!=null){
			dumpExceptionEntry(
					new NullPointerException("Conflicting commands identifiers for: "+strConflict));
		}
	}
	
	public boolean isCommentedLine(){
		if(strCmdLinePrepared==null)return false;
		return strCmdLinePrepared.trim().startsWith(""+getCommentPrefix());
	}

	public boolean cmdVarShow() {
		String strFilter = paramString(1);
		if(strFilter==null)strFilter="";
		strFilter=strFilter.trim();
		
		/**
		 * LIST all, user and restricted
		 */
		dumpInfoEntry("Variables list:");
		boolean bRestrictedOnly=false;
		if(strFilter.startsWith(""+RESTRICTED_TOKEN)){
			bRestrictedOnly=true;
			strFilter=strFilter.substring(1);
		}
//		if(strFilter!=null)strFilter=strFilter.substring(1);
		for(String strVarId : getVariablesIdentifiers(true)){
			if(isRestricted(strVarId) && !bRestrictedOnly)continue;
			if(!isRestricted(strVarId) && bRestrictedOnly)continue;
			
			if(strVarId.toLowerCase().contains(strFilter.toLowerCase()))varReport(strVarId);
		}
		dumpSubEntry(getCommentPrefix()+"UserVarListHashCode="+tmUserVariables.hashCode());
		
		return true;
	}
	
	public boolean varDelete(String strVarId){
		/**
		 * DELETE/UNSET only user variables
		 */
		boolean bCmdWorkDone=tmUserVariables.remove(strVarId)!=null;
		if(bCmdWorkDone){
			dumpInfoEntry("Var '"+strVarId+"' deleted.");
		}else{
			dumpWarnEntry("Var '"+strVarId+"' not found.");
		}
		return bCmdWorkDone;
	}
	
	/**
	 * When creating variables, this method can only create custom user ones.
	 * @return
	 */
	public boolean cmdVarSet() {
		String strVarId = paramString(1);
		String strValue = paramString(2);
		
		if(strVarId==null)return false;
		
		boolean bCmdWorkDone = false;
		if(strVarId.trim().startsWith(getVarDeleteTokenStr())){
			bCmdWorkDone=varDelete(strVarId.trim().substring(1));
		}else{
			/**
			 * SET user or restricted variable
			 */
			if(isRestrictedAndDoesNotExist(strVarId))return false;
			bCmdWorkDone=varSet(strVarId,strValue,true);
		}
		
		return bCmdWorkDone;
	}
	
	public boolean isRestrictedAndDoesNotExist(String strVar){
		if(isRestricted(strVar)){
			// user can only set existing restricted vars
			if(!selectVarSource(strVar).containsKey(strVar)){
				dumpWarnEntry("Restricted var does not exist: "+strVar);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean cmdVarSetCmp() {
		String strVarId = paramString(1);
		if(isRestrictedAndDoesNotExist(strVarId))return false;
		
		String strValueLeft = paramString(2);
		String strCmp = paramString(3);
		String strValueRight = paramString(4);
		
		if(strCmp.equals("==")){
			return varSet(strVarId, ""+strValueLeft.equals(strValueRight), true);
		}else
		if(strCmp.equals("!=")){
			return varSet(strVarId, ""+(!strValueLeft.equals(strValueRight)), true);
		}else
		if(strCmp.equals("||")){
			return varSet(strVarId, ""+
				(Misc.i().parseBoolean(strValueLeft) || Misc.i().parseBoolean(strValueRight)), true);
		}else
		if(strCmp.equals("&&")){
			return varSet(strVarId, ""+
				(Misc.i().parseBoolean(strValueLeft) && Misc.i().parseBoolean(strValueRight)), true);
		}else
		if(strCmp.equals(">")){
			return varSet(strVarId, ""+
				(Double.parseDouble(strValueLeft) > Double.parseDouble(strValueRight)), true);
		}else
		if(strCmp.equals(">=")){
			return varSet(strVarId, ""+
				(Double.parseDouble(strValueLeft) >= Double.parseDouble(strValueRight)), true);
		}else
		if(strCmp.equals("<")){
			return varSet(strVarId, ""+
				(Double.parseDouble(strValueLeft) < Double.parseDouble(strValueRight)), true);
		}else
		if(strCmp.equals("<=")){
			return varSet(strVarId, ""+
					(Double.parseDouble(strValueLeft) <= Double.parseDouble(strValueRight)), true);
		}else{
			dumpWarnEntry("Invalid comparator: "+strCmp);
		}
		
		return false;
	}

	public boolean databaseSave(){
		ArrayList<String> astr = new ArrayList<>();
		
		ArrayList<String> astrVarList = getVariablesIdentifiers(false);
		for(String strVarId:astrVarList){
			astr.add(varReportPrepare(strVarId));
		}
		
		for(Alias alias:aAliasList){
			astr.add(alias.toString());
		}
		
		flDB.delete();
		Misc.i().fileAppendLine(flDB, getCommentPrefix()+" DO NOT MODIFY! auto generated. Set overrides at user init file!");
		Misc.i().fileAppendList(flDB, astr);
		
		dumpInfoEntry("Database saved: "
			+astrVarList.size()+" vars, "
			+aAliasList.size()+" aliases, "
			+flDB.length()+" bytes,");
		
		setupRecreateFile();
		
		return true;
	}
	
	public boolean cmdDatabase(EDataBaseOperations edbo){
		if(edbo==null)return false;
		
		switch(edbo){
			case load:
				/**
				 * prepend on the queue is important mainly at the initialization
				 */
				dumpInfoEntry("Loading Console Database:");
				addCmdListOneByOneToQueue(Misc.i().fileLoad(flDB),true,false);
				return true;
			case backup:
				return databaseBackup();
			case save:
				if(btgDbAutoBkp.b()){
					if(isDatabaseChanged()){
						databaseBackup();
					}
				}
				
				return databaseSave();
			case show:
				for(String str:Misc.i().fileLoad(flDB)){
					dumpSubEntry(str);
				}
				return true;
		}
		
		return false;
	}
	
	public boolean hasChanged(ERestrictedSetupLoadableVars rv){
		String strValue = varGetValueString(""+RESTRICTED_TOKEN+rv);
		switch(rv){
			case userAliasListHashcode:
				return !(""+aAliasList.hashCode()).equals(strValue);
			case userVariableListHashcode:
				return !(""+tmUserVariables.hashCode()).equals(strValue);
		}
		
		return false;
	}
	
	public boolean isDatabaseChanged(){
		if(hasChanged(ERestrictedSetupLoadableVars.userAliasListHashcode))return true;
		if(hasChanged(ERestrictedSetupLoadableVars.userVariableListHashcode))return true;
		
		return false;
	}
	
	public boolean databaseBackup() {
		try {
			File fl = new File(fileNamePrepareCfg(strFileDatabase,true));
			Files.copy(flDB, fl);
			dumpSubEntry("Backup made: "+fl.getAbsolutePath()+"; "+fl.length()+" bytes");
		} catch (IOException ex) {
			dumpExceptionEntry(ex);
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public String getAliasHelp() {
		return "[<identifier> <commands>] | [<+|->identifier] | ["+getFilterToken()+"filter]\n"
			+"\t\tCreates an alias to run a in-line commands block (each separated by '"+getCommandDelimiter()+"')\n"
			+"\t\tWithout params, will list all aliases\n"
			+"\t\t"+getFilterToken()+"filter - will filter (contains) the alias list\n"
			+"\t\t-identifier - will block that alias execution\n"
			+"\t\t+identifier - will un-block that alias execution\n"
			+"\t\tObs.: to execute an alias, "
				+"prefix the identifier with '"+getAliasPrefix()+"', "
				+"ex.: "+getCommandPrefix()+getAliasPrefix() +"tst123";
	}
	
	public boolean cmdAlias() {
		boolean bOk=false;
		String strAliasId = paramString(1);
		if(strAliasId!=null && strAliasId.startsWith(""+getAliasAllowedToken())){
			bOk=aliasBlock(strAliasId.substring(1),false);
		}else
		if(strAliasId!=null && strAliasId.startsWith(""+getAliasBlockedToken())){
			bOk=aliasBlock(strAliasId.substring(1),true);
		}else{
			String strFilter=null;
			if(strAliasId!=null && strAliasId.startsWith(""+getFilterToken())){
				if(strAliasId.length()>1)strFilter = strAliasId.substring(1);
				strAliasId=null;
			}
			
			if(strAliasId==null){
				/**
				 * will list all aliases (or filtered)
				 */
				for(Alias alias:aAliasList){
					if(strFilter!=null && !alias.toString().toLowerCase().contains(strFilter.toLowerCase()))continue;
					dumpSubEntry(alias.toString());
				}
				dumpSubEntry(commentToAppend("AliasListHashCode="+aAliasList.hashCode()));
				bOk=true;
			}else{
				bOk=bLastAliasCreatedSuccessfuly;
			}
		}
		
		return bOk;
	}
	
	public boolean cmdShowHistory() {
		String strFilter = paramString(1);
		ArrayList<String> astrToDump = new ArrayList<String>();
		if(strFilter!=null){
			for(String str:astrCmdHistory){
				if(!str.toLowerCase().contains(strFilter.toLowerCase()))continue;
				str=str.trim(); // to prevent fail of unique check by spaces presence
				if(!astrToDump.contains(str))astrToDump.add(str);
				Collections.sort(astrToDump);
			}
		}else{
			astrToDump.addAll(astrCmdHistory);
		}
		
		for(String str:astrToDump){
//			dumpSubEntry(str);
			dumpEntry(false, true, false, str);
		}
		
		return true;
	}

	public boolean isStartupCommandsQueueDone(){
		return bStartupCmdQueueDone;
	}
	
	public boolean cmdDb() {
		String strOpt = paramString(1);
		if(strOpt!=null){
			EDataBaseOperations edb = null;
			try {edb = EDataBaseOperations.valueOf(strOpt);}catch(IllegalArgumentException e){}
			return cmdDatabase(edb);
		}
		return false;
	}

	public Alias getAlias(String strAliasId){
		Alias aliasFound=null;
		for(Alias aliasCheck : aAliasList){
			if(aliasCheck.strAliasId.toLowerCase().equals(strAliasId.toLowerCase())){
				aliasFound = aliasCheck;
				break;
			}
		}
		return aliasFound;
	}
	
	public boolean hasVar(String strVarId){
		return selectVarSource(strVarId).get(strVarId)!=null;
	}

	/**
	 * 
	 * @return false if toggle failed
	 */
	public boolean toggle(BoolToggler btg){
		if(paramBooleanCheckForToggle(1)){
			Boolean bEnable = paramBoolean(1);
			btg.set(bEnable==null ? !btg.get() : bEnable); //override
			varSet(btg, ""+btg.getBoolean(), true);
			dumpInfoEntry("Toggle, setting "+paramString(0)+" to "+btg.get());
			return true;
		}
		return false;
	}
	
	public Boolean paramBooleanCheckForToggle(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return true; //if there was no param, will work like toggle
		
		Boolean b = paramBoolean(iIndex);
		if(b==null)return false; //if there was a param but it is invalid, will prevent toggle
		
		return true; // if reach here, will not be toggle, will be a set override
	}
	public Boolean paramBoolean(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return null;
		/**
		 *	Not using java default method because it is permissive towards "false", ex.:
		 *	if user type "tre" instead of "true", it will result in `false`.
		 *	But false may be an undesired option.
		 *	Instead, user will be warned of the wrong typed value "tre".
		return Boolean.parseBoolean(str);
		 */
		if(str.equals("0"))return false;
		if(str.equals("1"))return true;
		if(str.equalsIgnoreCase("false"))return false;
		if(str.equalsIgnoreCase("true"))return true;
		
		dumpWarnEntry("invalid boolean value: "+str);
		
		return null;
	}
	public Integer paramInt(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return null;
		return Integer.parseInt(str);
	}
	public Float paramFloat(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return null;
		return Float.parseFloat(str);
	}
	
	public String prepareCmdAndParams(String strFullCmdLine){
		if(strFullCmdLine!=null){
			strFullCmdLine = strFullCmdLine.trim();
			
			if(strFullCmdLine.isEmpty())return null; //dummy line
			
			// a comment shall not create any warning based on false return value...
			if(strFullCmdLine.startsWith(""+getCommentPrefix()))return null; //comment is a "dummy command"
			
			// now it is possibly a command
			
			strFullCmdLine = strFullCmdLine.trim();
			if(strFullCmdLine.startsWith(getCommandPrefixStr())){
				strFullCmdLine = strFullCmdLine.substring(1); //cmd prefix 1 char
			}
			
			if(strFullCmdLine.endsWith(getCommentPrefixStr())){
				strFullCmdLine=strFullCmdLine.substring(0,strFullCmdLine.length()-1); //-1 getCommentPrefix()Char
			}
			
			return convertLineToCmdAndParams(strFullCmdLine);
		}
		
		return null;
	}
	
	public String getPreparedCmdLine(){
		return strCmdLinePrepared;
	}

	public String convertLineToCmdAndParams(String strFullCmdLine){
		/**
		 * remove comment
		 */
		int iCommentAt = strFullCmdLine.indexOf(getCommentPrefix());
		String strComment = "";
		if(iCommentAt>=0){
			strComment=strFullCmdLine.substring(iCommentAt);
			strFullCmdLine=strFullCmdLine.substring(0,iCommentAt);
		}
		
		/**
		 * queue multicommands line
		 */
		if(strFullCmdLine.contains(""+getCommandDelimiter())){
			ArrayList<String> astrMulti = new ArrayList<String>();
			astrMulti.addAll(Arrays.asList(strFullCmdLine.split(""+getCommandDelimiter())));
			for(int i=0;i<astrMulti.size();i++){
				/**
				 * replace by propagating the existing comment to each part that will be executed
				 */
				astrMulti.set(i, astrMulti.get(i).trim()
					+(strComment.isEmpty()?"":commentToAppend(strComment))
					+commentToAppend("SplitCmdLine")
				);
//				astrMulti.set(i, astrMulti.get(i).trim()+" "+getCommentPrefix()+"SplitCmdLine "+strComment);
			}
			addCmdListOneByOneToQueue(astrMulti,true,true);
			return RESTRICTED_CMD_SKIP_CURRENT_COMMAND.toString();
		}
		
		astrCmdAndParams.clear();
		astrCmdAndParams.addAll(convertToCmdParamsList(strFullCmdLine));
		return String.join(" ",astrCmdAndParams);
	}
	
	/**
	 * Each param can be enclosed within double quotes (")
	 * @param strFullCmdLine
	 * @return
	 */
	public ArrayList<String> convertToCmdParamsList(String strFullCmdLine){
		ArrayList<String> astrCmdParams = new ArrayList<String>();
//		astrCmdAndParams.clear();
		
		/**
		 * Prepare parameters, separated by blanks, that can be enclosed in double quotes.
		 * Param 0 is the actual command
		 */
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(strFullCmdLine);
		while (m.find()){
			String str=m.group(1);
			if(str!=null){
				if(str.trim().startsWith(""+getCommentPrefix()))break; //ignore comments
				str=str.trim();
				str=str.replace("\"", ""); //remove all quotes on the string TODO could be only 1st and last? mmm... too much trouble...
//				astrCmdAndParams.add(str);
				astrCmdParams.add(str);
			}
		}
		
//		return strFullCmdLine;
		return astrCmdParams;
	}
	
	/**
	 * 
	 * @return the first "word" in the command line, is the command
	 */
	public String paramCommand(){
		return paramString(0);
	}
	
	/**
	 * 
	 * @param iIndex 0 is the command, >=1 are parameters
	 * @return
	 */
	public String paramString(int iIndex){
		if(iIndex<astrCmdAndParams.size()){
			String str=astrCmdAndParams.get(iIndex);
			str = applyVariablesValues(str);
			return str;
		}
		return null;
	}
	public String paramStringConcatenateAllFrom(int iStartIndex){
		String str=null;
		while(iStartIndex<astrCmdAndParams.size()){
			if(str!=null){
				str+=" ";
			}else{
				str="";
			}
			
			str+=astrCmdAndParams.get(iStartIndex++);
		}
		
		if(str!=null){
			str = applyVariablesValues(str);
		}
		
		return str;
	}
	
	/**
	 * 
	 * @param bAll if false, will bring only user variables
	 * @return
	 */
	public ArrayList<String> getVariablesIdentifiers(boolean bAll){
		ArrayList<String> astr = Lists.newArrayList(tmUserVariables.keySet().iterator());
		if(bAll)astr.addAll(Lists.newArrayList(tmRestrictedVariables.keySet().iterator()));
		Collections.sort(astr);
		return astr;
	}
	
	/**
	 * this method must be used just before a command is going to be executed,
	 * so variables have time to be updated by other commands etc.
	 * @param strParam
	 * @return
	 */
	public String applyVariablesValues(String strParam){
		// fast skip
		if(!strParam.contains(getVariableExpandPrefix()+"{"))return strParam;
		
		for(String strVarId : getVariablesIdentifiers(true)){
			String strToReplace=getVariableExpandPrefix()+"{"+strVarId+"}";
			if(strParam.toLowerCase().contains(strToReplace.toLowerCase())){
//				strParam=strParam.replace(strToReplace, ""+getVarHT(strVarId).get(strVarId));
				strParam=strParam.replaceAll(
					"(?i)"+Pattern.quote(strToReplace), 
					""+getVarValue(strVarId));
				
				// nothing remaining to be done
				if(!strParam.contains(getVariableExpandPrefix()+"{"))break;
			}
		}
		return strParam;
	}

	/**
	 * In case variable exists will be this method.
	 * @param strVarId
	 * @param strValueAdd
	 * @param bOverwrite
	 * @return
	 */
	public boolean cmdVarAdd(String strVarId, String strValueAdd, boolean bSave, boolean bOverwrite){
		if(isRestrictedAndDoesNotExist(strVarId))return false;
		
		Object objValueNew = null;
		Object objValueCurrent = selectVarSource(strVarId).get(strVarId);
		
		if(objValueCurrent==null){
			dumpExceptionEntry(new NullPointerException("value is null for var "+strVarId));
			return false;
		}
			
//		if(objValueCurrent!=null){
			if(Boolean.class.isAssignableFrom(objValueCurrent.getClass())){
				// boolean is always overwrite
				objValueNew = Misc.i().parseBoolean(strValueAdd);
			}else
			if(Long.class.isAssignableFrom(objValueCurrent.getClass())){
				Long lValueCurrent = (Long)objValueCurrent;
				Long lValueAdd=null;
				try{lValueAdd = Long.parseLong(strValueAdd);}catch(NumberFormatException e){}// accepted exception!
				if(lValueAdd!=null){
					if(bOverwrite)lValueCurrent=0L;
					lValueCurrent+=lValueAdd;
					objValueNew = lValueCurrent;
				}else{
					dumpWarnEntry("Add value should be: "+Long.class.getSimpleName());
				}
			}else
			if(Double.class.isAssignableFrom(objValueCurrent.getClass())){
				Double dValueCurrent = (Double)objValueCurrent;
				Double dValueAdd=null;
				try{dValueAdd = Double.parseDouble(strValueAdd);}catch(NumberFormatException e){}// accepted exception!
				if(dValueAdd!=null){
					if(bOverwrite)dValueCurrent=0.0;
					dValueCurrent+=dValueAdd;
					objValueNew = dValueCurrent;
				}else{
					dumpWarnEntry("Add value should be: "+Double.class.getSimpleName());
				}
			}else{
				if(bOverwrite)objValueCurrent="";
				objValueNew = ""+objValueCurrent+strValueAdd;
			}
//		}else{
//			return varSet(strVarId, strValueAdd, true);
//		}
		
//		if(objValueNew==null)return false;
		
		varApply(strVarId,objValueNew,bSave);
		return true;
	}
	
	public boolean isRestricted(String strId){
		return strId.startsWith(""+RESTRICTED_TOKEN);
	}
	
	public Object getVarValue(String strVarId){
		return selectVarSource(strVarId).get(strVarId);
	}
	public void setVarValue(String strVarId, Object objValue){
		selectVarSource(strVarId).put(strVarId,objValue);
	}
	public TreeMap<String, Object> selectVarSource(String strVarId){
		if(isRestricted(strVarId)){
			return tmRestrictedVariables;
		}else{
			return tmUserVariables;
		}
	}
	
	public File getVarFile(String strVarId){
		if(isRestricted(strVarId)){
			return flSetup;
		}else{
			return flDB;
		}
	}
	
	public void varSaveSetupFile(){
		for(String strVarId : getVariablesIdentifiers(true)){
			if(isRestricted(strVarId))fileAppendVar(strVarId);
		}
	}
	
	public void fileAppendVar(String strVarId){
		String strCommentOut="";
		String strReadOnlyComment="";
		if(isRestricted(strVarId)){
			try{ERestrictedSetupLoadableVars.valueOf(strVarId.substring(1));}catch(IllegalArgumentException e){
				/**
				 * comment non loadable restricted variables, like the ones set by commands
				 */
				strCommentOut=getCommentPrefixStr();
				strReadOnlyComment="(ReadOnly)";
			}
		}
		
		Misc.i().fileAppendLine(getVarFile(strVarId), strCommentOut+varReportPrepare(strVarId)+strReadOnlyComment);
	}
	
	public boolean varApply(String strVarId, Object objValue, boolean bSave){
		selectVarSource(strVarId).put(strVarId,objValue);
		if(bSave)fileAppendVar(strVarId);
		
		if(isRestricted(strVarId) && btgShowDeveloperInfo.b()){
			varReport(strVarId);
		}
		
		return true;
	}
	
	public String varReportPrepare(String strVarId) {
		Object objValue = selectVarSource(strVarId).get(strVarId);
		String str="";
		
		str+=getCommandPrefix();
		str+=CMD_VAR_SET.toString();
		str+=" ";
		str+=strVarId;
		str+=" ";
		if(objValue!=null){
			str+="\""+objValue+"\"";
			str+=" ";
		}
		str+="#";
		if(objValue!=null){
			str+=objValue.getClass().getSimpleName();
		}else{
			str+="(ValueNotSet)";
		}
		str+=" ";
		str+=(isRestricted(strVarId)?"(Restricted)":"(User)");
		
		return str;
	}
	
	public void varReport(String strVarId) {
		Object objValue=selectVarSource(strVarId).get(strVarId);
		if(objValue!=null){
			dumpSubEntry(varReportPrepare(strVarId));
		}else{
			dumpSubEntry(strVarId+" is not set...");
		}
	}
	
	public boolean varSet(StringField sfId, String strValue, boolean bSave) {
		return varSet(RESTRICTED_TOKEN+sfId.toString(), strValue, bSave);
	}
	public boolean varSet(BoolToggler btg, String strValue, boolean bSave) {
		return varSet(RESTRICTED_TOKEN+btg.getCmdId(), strValue, bSave);
	}
	
	/**
	 * This is able to create restricted variables too.
	 * 
	 * @param strVarId
	 * @param strValue
	 * @return
	 */
	public boolean varSet(String strVarId, String strValue, boolean bSave) {
		if(getAlias(strVarId)!=null){
			dumpErrorEntry("Variable identifier '"+strVarId+"' conflicts with existing alias!");
			return false;
		}
		
		if(strValue==null)return false; //strValue=""; //just creates the var
		
		if(hasVar(strVarId)){
			return cmdVarAdd(strVarId, strValue, bSave, true);
		}
		
		boolean bOk=false;
		
		/**
		 * Priority:
		 * Double would parse a Long.
		 * Boolean would be accepted by String that accepts everything. 
		 */
		if(!bOk)try{bOk=varApply(strVarId, Long  .parseLong     (strValue),bSave);}catch(NumberFormatException e){}// accepted exception!
		if(!bOk)try{bOk=varApply(strVarId, Double.parseDouble   (strValue),bSave);}catch(NumberFormatException e){}// accepted exception!
		if(!bOk)try{bOk=varApply(strVarId, Misc.i().parseBoolean(strValue),bSave);}catch(NumberFormatException e){}// accepted exception!
		if(!bOk)bOk=varApply(strVarId,strValue,bSave);
		
		return bOk;
	}
	
	/**
	 * 
	 * @param strVarId
	 * @return "null" if not set
	 */
	public String varGetValueString(String strVarId){
		Object obj = selectVarSource(strVarId).get(strVarId);
		if(obj==null)return "null";
		return ""+obj;
	}
//	public Double varGetValueDouble(String strVarId){
//		Object obj = ahkVariables.get(strVarId);
//		if(obj==null)return null;
//		if(obj instanceof Double)return (Double)obj;
//		dumpExceptionEntry(new typeex);
//		return null;
//	}
	
	public boolean aliasBlock(String strAliasId, boolean bBlock) {
		for(Alias alias : aAliasList){
			if(alias.strAliasId.toLowerCase().equals(strAliasId.toLowerCase())){
				dumpInfoEntry((bBlock?"Blocking":"Unblocking")+" alias "+alias.strAliasId);
				alias.bBlocked=bBlock;
				return true;
			}
		}
		return false;
	}
	public void cmdShowHelp(String strFilter) {
		if(strFilter==null){
			dumpInfoEntry("Available Commands:");
		}else{
			dumpInfoEntry("Help for '"+strFilter+"':");
		}
		
		Collections.sort(astrCmdWithCmtValidList,String.CASE_INSENSITIVE_ORDER);
		for(String str:astrCmdWithCmtValidList){
			if(strFilter!=null && !str.toLowerCase().contains(strFilter.toLowerCase()))continue;
			dumpSubEntry(getCommandPrefix()+str);
		}
	}
	
	public boolean stillExecutingCommand(){
		return executePreparedCommandRoot();
	}
	
	/**
	 * Command format: "commandIdentifier any comments as you wish"
	 * @param strFullCmdLineOriginal if null will populate the array of valid commands
	 * @return false if command execution failed
	 */
	public boolean executeCommand(final String strFullCmdLineOriginal){
		assertInitialized();
		
		strCmdLineOriginal = strFullCmdLineOriginal;
		
		boolean bCmdFoundAndApplied = false;
		try{
			if(!bCmdFoundAndApplied)bCmdFoundAndApplied=cmdRawLineCheckEndOfStartupCmdQueue();
			
			if(!bCmdFoundAndApplied)bCmdFoundAndApplied=cmdRawLineCheckAlias();
			
//			if(!bOk)bOk=cmdRawLineCheckIfElse();
			
//			if(!bOk){
//				if(SPECIAL_CMD_SKIP_CURRENT_COMMAND.equals(strCmdLinePrepared)){
//					bOk=true;
//				}
//			}
			
			
			if(!bCmdFoundAndApplied){
				/**
				 * we will have a prepared line after below
				 */
				strCmdLinePrepared = prepareCmdAndParams(strCmdLineOriginal);
			}
			
			if(!bCmdFoundAndApplied)bCmdFoundAndApplied=stillExecutingCommand();
			
//			if(!bCmdWorkDone){
//				if(bFuncCmdLineRunning){
//					if(checkFuncExecEnd()){
//						bFuncCmdLineRunning=false;
//						bFuncCmdLineSkipTilEnd=false;
//						bCmdWorkDone=true;
//					}
//				}
//			}
//			
//			if(!bCmdWorkDone){
//				if(checkFuncExecStart()){
//					bFuncCmdLineRunning=true;
//					bCmdWorkDone=true;
//				}else
//				if(strPrepareFunctionBlockForId!=null){
//					if(!bCmdWorkDone)bCmdWorkDone = functionEndCheck(strCmdLineOriginal); //before feed
//					if(!bCmdWorkDone)bCmdWorkDone = functionFeed(strCmdLineOriginal);
//				}else
//				if(bIfConditionExecCommands!=null && !bIfConditionExecCommands){
//					/**
//					 * These are capable of stopping the skipping.
//					 */
//					if(CMD_ELSE_IF.equals(paramString(0))){
//						if(!bCmdWorkDone)bCmdWorkDone = cmdElseIf();
//					}else
//					if(CMD_ELSE.equals(paramString(0))){
//						if(!bCmdWorkDone)bCmdWorkDone = cmdElse();
//					}else
//					if(CMD_IF_END.equals(paramString(0))){
//						if(!bCmdWorkDone)bCmdWorkDone = cmdIfEnd();
//					}else{
//						/**
//						 * The if condition resulted in false, therefore commands must be skipped.
//						 */
//						dumpInfoEntry("ConditionalSkip: "+strCmdLinePrepared);
//						if(!bCmdWorkDone)bCmdWorkDone = true;
//					}
//				}
//			}
//			
//			if(!bCmdWorkDone){
//				if(bFuncCmdLineRunning && bFuncCmdLineSkipTilEnd){
//					dumpWarnEntry("SkippingRemainingFunctionCmds: "+strCmdLinePrepared);
//					bCmdWorkDone = true; //this just means that the skip worked
//				}
//			}
//			
//			if(!bCmdWorkDone){
//				/**
//				 * normal commands execution
//				 */
//				bCmdWorkDone = executePreparedCommand();
//			}
//			
//			if(!bCmdWorkDone){
//				if(bFuncCmdLineRunning){
//					// a command may fail inside a function, only that first one will generate error message 
//					bFuncCmdLineSkipTilEnd=true;
//				}
//			}
		}catch(NumberFormatException e){
			// keep this one as "warning", as user may simply fix the typed value
			dumpWarnEntry("NumberFormatException: "+e.getMessage());
			e.printStackTrace();
			bCmdFoundAndApplied=false;
		}
		
		return bCmdFoundAndApplied;
	}
	
	public void dumpEntry(String strLineOriginal){
		dumpEntry(true, true, false, strLineOriginal);
	}
	
	public void dumpEntry(boolean bApplyNewLineRequests, boolean bDump, boolean bUseSlowQueue, String strLineOriginal){
		DumpEntry de = new DumpEntry()
			.setApplyNewLineRequests(bApplyNewLineRequests)
			.setDumpToConsole(bDump)
			.setUseSlowQueue(bUseSlowQueue)
			.setLineOriginal(strLineOriginal);
		
		dumpEntry(de);
	}
	public void update(float tpf) {
		this.fTPF = tpf;
		if(tdLetCpuRest.isActive() && !tdLetCpuRest.isReady(true))return;
		
		updateToggles();
		updateExecPreQueuedCmdsBlockDispatcher(); //before exec queue 
		updateExecConsoleCmdQueue(); // after pre queue
		updateDumpQueueEntry();
	}
	public void updateToggles() {
		if(btgEngineStatsView.checkChangedAndUpdate())csaTmp.updateEngineStats();
		if(btgEngineStatsFps.checkChangedAndUpdate())csaTmp.updateEngineStats();
//		if(btgFpsLimit.checkChangedAndUpdate())fpslState.setEnabled(btgFpsLimit.b());
		if(btgConsoleCpuRest.checkChangedAndUpdate())tdLetCpuRest.setActive(btgConsoleCpuRest.b());
//		if(btgPreQueue.checkChangedAndUpdate())bUsePreQueue=btgPreQueue.b();
	}

	public void addCmdListOneByOneToQueue(ArrayList<String> astrCmdList, boolean bPrepend, boolean bShowExecIndex){
		ArrayList<String> astrCmdListCopy = new ArrayList<String>(astrCmdList);
		
		if(bShowExecIndex){
			for(int i=0;i<astrCmdListCopy.size();i++){
				astrCmdListCopy.set(i, astrCmdListCopy.get(i)+commentToAppend("ExecIndex="+i));
			}
		}
		
		if(bPrepend){
			Collections.reverse(astrCmdListCopy);
		}
		for(String strCmd:astrCmdListCopy){
			addCmdToQueue(strCmd, bPrepend);
		}
	}
	
	public void addCmdsBlockToPreQueue(ArrayList<String> astrCmdList, boolean bPrepend, boolean bShowExecIndex, String strBlockInfo){
		PreQueueCmdsBlockSubList pqe = new PreQueueCmdsBlockSubList();
		pqe.strBlockInfo=strBlockInfo;
		pqe.bPrepend=bPrepend;
		pqe.astrCmdList = new ArrayList<String>(astrCmdList);
		for(int i=0;i<pqe.astrCmdList.size();i++){
			pqe.astrCmdList.set(i, pqe.astrCmdList.get(i)+pqe.getUniqueInfo());
		}
		if(bShowExecIndex){
			for(int i=0;i<pqe.astrCmdList.size();i++){
				pqe.astrCmdList.set(i, pqe.astrCmdList.get(i)+commentToAppend("ExecIndex="+i)
				);
			}
		}
		
		astrExecConsoleCmdsPreQueue.add(pqe);
		dumpDevInfoEntry("AddedCommandBlock"+pqe.getUniqueInfo());
	}
	
	public boolean doesCmdQueueStillHasUId(String strUId){
		for(String strCmd:astrExecConsoleCmdsQueue){
			if(strCmd.contains(strUId))return true;
		}
		return false;
	}
	
	public void updateExecPreQueuedCmdsBlockDispatcher(){
		for(PreQueueCmdsBlockSubList pqe:astrExecConsoleCmdsPreQueue.toArray(new PreQueueCmdsBlockSubList[0])){
			if(pqe.tdSleep!=null){
				if(pqe.tdSleep.isReady()){
					if(doesCmdQueueStillHasUId(pqe.getUniqueInfo())){
						/**
						 * will wait all commands of this same pre queue list
						 * to complete, before continuing, in case the delay was too short.
						 */
						continue;
					}else{
						pqe.tdSleep=null;
					}
				}else{
					if(!pqe.bInfoSleepBegin){
						dumpDevInfoEntry("Sleeping for "
							+Misc.i().fmtFloat(pqe.tdSleep.getDelayLimit())+"s: "
							+pqe.getUniqueInfo()
							+commentToAppend(pqe.getUniqueInfo()));
						pqe.bInfoSleepBegin =true;
					}
					continue;
				}
			}
			
			if(pqe.astrCmdList.size()==0 || pqe.bForceFailBlockExecution){
				astrExecConsoleCmdsPreQueue.remove(pqe);
			}else{
				ArrayList<String> astrCmdListFast = new ArrayList<String>();
				
				while(pqe.astrCmdList.size()>0){
					String strCmd = pqe.astrCmdList.remove(0);
					String strCmdBase = extractCommandPart(strCmd, 0);
					if(strCmdBase==null)continue;
					
					if(CMD_SLEEP.equals(strCmdBase)){
						String strParam1 = extractCommandPart(strCmd, 1);
						strParam1=applyVariablesValues(strParam1);
						Float fDelay=null;
						try{fDelay = Float.parseFloat(strParam1);}catch(NumberFormatException ex){
							dumpExceptionEntry(ex);
							pqe.bForceFailBlockExecution=true;
							break;
						}
						pqe.tdSleep = new TimedDelay(fDelay);
						pqe.tdSleep.updateTime();
//						dumpDevInfoEntry(strCmd);
						break;
					}else{
						astrCmdListFast.add(strCmd);
					}
				}
				
				if(pqe.bForceFailBlockExecution)continue;
				
				if(astrCmdListFast.size()>0){
//					if(pqe.bPrepend){
//						Collections.reverse(astrCmdListFast);
//					}
//					for(String str:astrCmdListFast){
//						addCmdToQueue(str, pqe.bPrepend);
//					}
					astrCmdListFast.add(csaTmp.CMD_CONSOLE_SCROLL_BOTTOM.toString());
					addCmdListOneByOneToQueue(astrCmdListFast, pqe.bPrepend, false);
				}
				
			}
		}
	}
	public void addExecConsoleCommandToQueue(StringField sfFullCmdLine){
		addExecConsoleCommandToQueue(sfFullCmdLine.toString());
	}
	public void addExecConsoleCommandToQueue(String strFullCmdLine){
		addCmdToQueue(strFullCmdLine,false);
	}
	public void addCmdToQueue(String strFullCmdLine, boolean bPrepend){
		strFullCmdLine=strFullCmdLine.trim();
		
		if(strFullCmdLine.startsWith(""+getCommentPrefix()))return;
		if(strFullCmdLine.isEmpty())return;
		if(strFullCmdLine.equals(""+getCommandPrefix()))return;
		
		if(!strFullCmdLine.startsWith(""+RESTRICTED_TOKEN)){
			if(!strFullCmdLine.startsWith(""+getCommandPrefix())){
				strFullCmdLine=getCommandPrefix()+strFullCmdLine;
			}
		}
		
		dumpDevInfoEntry("CmdQueued: "+strFullCmdLine+(bPrepend?" #Prepended":""));
		
		if(bPrepend){
			astrExecConsoleCmdsQueue.add(0,strFullCmdLine);
		}else{
			astrExecConsoleCmdsQueue.add(strFullCmdLine);
		}
	}
	
	public void updateExecConsoleCmdQueue() {
		if(astrExecConsoleCmdsQueue.size()>0){ // one per time! NO while here!!!!
			String str=astrExecConsoleCmdsQueue.remove(0);
			if(!str.trim().endsWith(""+getCommentPrefix())){
				if(btgShowExecQueuedInfo.get()){ // prevent messing user init cfg console log
					dumpInfoEntry("QueueExec: "+str);
				}
			}
			if(!executeCommand(str)){
				dumpWarnEntry("QueueExecFail: "+str);
			}
		}
	}
	
	public void showHelpForFailedCommand(String strFullCmdLine){
		if(validateBaseCommand(strFullCmdLine)){
//			addToExecConsoleCommandQueue(CMD_HELP+" "+extractCommandPart(strFullCmdLine,0));
			cmdShowHelp(extractCommandPart(strFullCmdLine,0));
		}else{
			dumpWarnEntry("Invalid command: "+strFullCmdLine);
		}
	}
	public void cmdHistLoad() {
		astrCmdHistory.addAll(Misc.i().fileLoad(flCmdHist));
	}
	
	public void dumpSave(DumpEntry de) {
//		if(de.isSavedToLogFile())return;
		Misc.i().fileAppendLine(flLastDump,de.getLineOriginal());
	}
	/**
	 * These variables can be loaded from the setup file!
	 */
	enum ERestrictedSetupLoadableVars{
		userVariableListHashcode,
		userAliasListHashcode,
	}
	
	public void setupRecreateFile(){
		flSetup.delete();
		
		Misc.i().fileAppendLine(flSetup, getCommentPrefix()+" DO NOT EDIT!");
		Misc.i().fileAppendLine(flSetup, getCommentPrefix()
			+" This file will be overwritten by the application!");
		Misc.i().fileAppendLine(flSetup, getCommentPrefix()
			+" To set overrides use the user init config file.");
		Misc.i().fileAppendLine(flSetup, getCommentPrefix()
			+" For command's values, the commands usage are required, the variable is just an info about their setup value.");
		Misc.i().fileAppendLine(flSetup, getCommentPrefix()
			+" Some values will be read tho to provide restricted functionalities not accessible to users.");
		
		setupVars(true);
	}
	
	public void setupVars(boolean bSave){
		varSet(""+RESTRICTED_TOKEN+ERestrictedSetupLoadableVars.userVariableListHashcode,
			""+tmUserVariables.hashCode(),
			false);
		
		varSet(""+RESTRICTED_TOKEN+ERestrictedSetupLoadableVars.userAliasListHashcode,
			""+aAliasList.hashCode(),
			false);
		
		if(bSave)varSaveSetupFile();
	}
	/**
	 * 
	 * @param strCmdFull
	 * @param iPart 0 is base command, 1.. are params
	 * @return
	 */
	public String extractCommandPart(String strCmdFull, int iPart){
		if(strCmdFull.startsWith(""+getCommandPrefix())){
			strCmdFull=strCmdFull.substring(1); //1 getCommandPrefix()Char
		}
		
//		String[] astr = strCmdFull.split("[^$"+strValidCmdCharsRegex+"]");
//		if(astr.length>iPart){
//			return astr[iPart];
//		}
		ArrayList<String> astr = convertToCmdParamsList(strCmdFull);
		if(iPart>=0 && astr.size()>iPart){
			return astr.get(iPart);
		}
		
		return null;
	}
	public boolean isValidIdentifierCmdVarAliasFuncString(String strCmdPart) {
		if(strCmdPart==null)return false;
		return strCmdPart.matches("["+strValidCmdCharsRegex+"]*");
	}
	public void dumpAllStats(){
		icui.dumpAllStats();
		
		dumpSubEntry("Database User Variables Count = "+getVariablesIdentifiers(false).size());
		dumpSubEntry("Database User Aliases Count = "+aAliasList.size());
		
//		dumpSubEntry("Previous Second FPS  = "+lPreviousSecondFPS);
		
		for(BoolToggler bh : BoolToggler.getBoolTogglerListCopy()){
			dumpSubEntry(bh.getCmdId()+" = "+bh.get());
		}
		
		dumpSubEntry("User Dump File = "+flLastDump.getAbsolutePath());
		dumpSubEntry("User Commands History File = "+flCmdHist.getAbsolutePath());
		dumpSubEntry("User Database File = "+flDB.getAbsolutePath());
		dumpSubEntry("User Config File = "+flInit.getAbsolutePath());
	}
	
	/**
	 * Validates if the first extracted word is a valid command.
	 * 
	 * @param strCmdFullChk can be the full command line here
	 * @return
	 */
	public boolean validateBaseCommand(String strCmdFullChk){
		strCmdFullChk = extractCommandPart(strCmdFullChk,0);
//		if(strCmdFullChk.startsWith(strCommandPrefixChar)){
//			strCmdFullChk=strCmdFullChk.substring(strCommandPrefixChar.length());
//		}
		return astrBaseCmdValidList.contains(strCmdFullChk);
	}
	
	public static enum EStats{
		CommandsHistory,
		ConsoleSliderControl,
		CopyFromTo,
		FunctionCreation(true),
		IfConditionalBlock(true),
		MousePosition,
		TimePerFrame,
		;
		
		public boolean b;
		
		EStats(){}
		EStats(boolean b){this.b=b;}
	}
	
	public String prepareStatsFieldText(){
		String strStatsLast = "";
		
		if(EStats.CopyFromTo.b){
			strStatsLast+=
					// user important
					"Cp"+iCopyFrom
						+">"+iCopyTo //getDumpAreaSelectedIndex()
						+";";
		}
						
		if(EStats.CommandsHistory.b){
			strStatsLast+=
					"Hs"+iCmdHistoryCurrentIndex+"/"+(astrCmdHistory.size()-1)
						+";";
		}
					
		if(EStats.IfConditionalBlock.b && aIfConditionNestedList.size()>0){
			strStatsLast+=
					"If"+aIfConditionNestedList.size()
						+";";
		}
					
//		if(EStats.FunctionCreation.b && strPrepareFunctionBlockForId!=null){
//			strStatsLast+=
//					"F="+strPrepareFunctionBlockForId
//						+";";
//		}
			
					/**
					 * KEEP HERE AS REFERENCE!
					 * IMPORTANT, DO NOT USE
					 * clipboard reading is too heavy...
					+"Cpbd='"+retrieveClipboardString()+"', "
					 */
						
					// less important (mainly for debug)
		if(EStats.ConsoleSliderControl.b){
			strStatsLast+=icui.getDumpAreaSliderStatInfo();
		}
					
//		if(EStats.TimePerFrame.b){
//			strStatsLast+=
//					"Tpf"+(fpslState.isEnabled() ? (int)(fTPF*1000.0f) : Misc.i().fmtFloat(fTPF,6)+"s")
//						+(fpslState.isEnabled()?
//							"="+fpslState.getFrameDelayByCpuUsageMilis()+"+"+fpslState.getThreadSleepTimeMilis()+"ms"
//							:"")
//						+";";
//		}
						
		if(EStats.MousePosition.b){
			strStatsLast+=
					"xy"
						+(int)sapp.getInputManager().getCursorPosition().x
						+","
						+(int)sapp.getInputManager().getCursorPosition().y
						+";";
		}
		
		return strStatsLast;
	}
	
	public void cmdHistSave(String strCmd) {
		Misc.i().fileAppendLine(flCmdHist,strCmd);
	}

	public void initialize(IConsoleUI icui, SimpleApplication sapp){
		this.icui=icui;
		this.sapp=sapp;
		
		tdStatsRefresh.updateTime();
		tdDumpQueuedEntry.updateTime();
		
		// init dump file, MUST BE THE FIRST!
		flLastDump = new File(fileNamePrepareLog(strFileLastDump,false));
		flLastDump.delete(); //each run will have a new file
		
		// init cmd history
		flCmdHist = new File(fileNamePrepareLog(strFileCmdHistory,false));
		cmdHistLoad();
		
		// restricted vars setup
		setupVars(false);
		flSetup = new File(fileNamePrepareCfg(strFileSetup,false));
		if(flSetup.exists()){
			addCmdListOneByOneToQueue(Misc.i().fileLoad(flSetup), false, false);
		}
		
		// before user init file
		addExecConsoleCommandToQueue(btgShowExecQueuedInfo.getCmdIdAsCommand(false));
		addExecConsoleCommandToQueue(btgShowDebugEntries.getCmdIdAsCommand(false));
		addExecConsoleCommandToQueue(btgShowDeveloperWarn.getCmdIdAsCommand(false));
		addExecConsoleCommandToQueue(btgShowDeveloperInfo.getCmdIdAsCommand(false));
		
		// init user cfg
		flInit = new File(fileNamePrepareCfg(strFileInitConsCmds,false));
		if(flInit.exists()){
			addCmdListOneByOneToQueue(Misc.i().fileLoad(flInit), false, false);
		}else{
			Misc.i().fileAppendLine(flInit, getCommentPrefix()+" User console commands here will be executed at startup.");
		}
		
		// init DB
		flDB = new File(fileNamePrepareCfg(strFileDatabase,false));
		
		bInitialized=true;
		// init valid cmd list
		executeCommand(null); //to populate the array with available commands
	}
	
	enum ETest{
		fps,
		allchars,
		stats,
	}
	public void cmdTest(){
		dumpInfoEntry("testing...");
		String strOption = paramString(1);
		
		if(strOption==null){
			dumpSubEntry(Arrays.toString(ETest.values()));
			return;
		}
		
		ETest et = ETest.valueOf(strOption.toLowerCase());
		switch(et){
			case fps:
//			sapp.setSettings(settings);
				break;
			case allchars:
				for(char ch=0;ch<256;ch++){
					dumpSubEntry(""+(int)ch+"='"+Character.toString(ch)+"'");
				}
				break;
			case stats:
				strTest=paramString(2);
				dumpDevInfoEntry("lblTxtSize="+csaTmp.lblStats.getText().length());
				break;
		}
		
	}	

	public void showClipboard(){
		showClipboard(true);
	}
	public void showClipboard(boolean bShowNL){
		String strClipboard=Misc.i().retrieveClipboardString();
	//	dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+" ( each line enclosed with \\"+strLineEncloseChar+" ):");
		dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+":");
		String[] astr = strClipboard.split("\n");
	//	for(String str:astr)dumpEntry(strLineEncloseChar+str+strLineEncloseChar);
	//	dumpEntry(""); // this empty line for clipboard content display is i
		dumpEntry(">>> Clipboard BEGIN");
		for(int i=0;i<astr.length;i++){
			String str=astr[i];
			if(bShowNL && i<(astr.length-1))str+="\\n";
			dumpEntry(false,true,false,str);
		}
		dumpEntry("<<< Clipboard END");
		if(bAddEmptyLineAfterCommand)dumpEntry("");
	//	dumpEntry("");
		icui.scrollToBottomRequest();
	}

	public String fileNamePrepare(String strFileBaseName, String strFileType, boolean bAddDateTime){
		return strFileBaseName
				+(bAddDateTime?"-"+Misc.i().getDateTimeForFilename():"")
				+"."+strFileType;
	}
	public String fileNamePrepareCfg(String strFileBaseName, boolean bAddDateTime){
		return fileNamePrepare(strFileBaseName, strFileTypeConfig, bAddDateTime);
	}
	public String fileNamePrepareLog(String strFileBaseName, boolean bAddDateTime){
		return fileNamePrepare(strFileBaseName, strFileTypeLog, bAddDateTime);
	}

	public boolean cmdRawLineCheckEndOfStartupCmdQueue() {
		if(RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE.equals(strCmdLineOriginal)){
			bStartupCmdQueueDone=true;
			return true;
		}
		return false;
	}

	public void toggleLineCommentOrCommand() {
		String str = icui.getInputText();
		if(str.startsWith(""+getCommentPrefix())){
			str=str.substring(1);
		}else{
			str=getCommentPrefix()+str;
		}
		icui.setInputField(str);
	}

	@Override
	public void handleException(Exception e) {
		dumpExceptionEntry(e);
	}
	
	public String convertNewLineToCmdDelimiter(String str){
		return str
			.replace(getCommandDelimiterStr()+"\n", getCommandDelimiterStr())
			.replace("\n", getCommandDelimiterStr());
	}

	public String editCopyOrCut(boolean bJustCollectText, boolean bCut, boolean bUseCommandDelimiterInsteadOfNewLine) {
	//	Integer iCopyTo = getDumpAreaSelectedIndex();
		String strTextToCopy = null;
		
		int iCopyToWork = iCopyTo;
		int iCopyFromWork = iCopyFrom;
		
	//	String strNL="\n";
	//	if(bUseCommandDelimiterInsteadOfNewLine){
	////		str=str.replace("\n", getCommandDelimiterStr());
	//		strNL=getCommandDelimiterStr();
	//	}
		
		if(iCopyToWork>=0){
			
			if(iCopyFromWork==-1)iCopyFromWork=iCopyToWork;
			
			// wrap mode overrides this behavior
			boolean bMultiLineMode = iCopyFromWork!=iCopyToWork;
			
			if(iCopyFromWork>iCopyToWork){
				int iCopyFromBkp=iCopyFromWork;
				iCopyFromWork=iCopyToWork;
				iCopyToWork=iCopyFromBkp;
			}
			
			strTextToCopy="";
			while(true){ //multi-line copy
				if(iCopyFromWork>=icui.getDumpEntries().size())break;
				
				String strEntry =	bCut ? icui.getDumpEntries().remove(iCopyFromWork) :
					icui.getDumpEntries().get(iCopyFromWork);
	//			strEntry=strEntry.replace("\\n","\n"); //translate in-between newline requests into newline
				
				boolean bJoinWithNext = false;
				if(strEntry.endsWith("\\")){
					bJoinWithNext=true;
					
					/**
					 * removes trailing linewrap indicator
					 */
					strEntry = strEntry.substring(0, strEntry.length()-1); 
				}else
				if(strEntry.endsWith("\\n")){
					bJoinWithNext=true;
					strEntry = strEntry.substring(0, strEntry.length()-2);
					strEntry+="\n";
				}
				
				strTextToCopy+=strEntry;
				
				if(!bJoinWithNext)strTextToCopy+="\n";
				
				if(bMultiLineMode){
					/**
					 * this overrides wrap mode, as user may not want other lines
					 * as he/she used a multi-line mode.
					 */
					if((iCopyFromWork-iCopyToWork)==0)break;
					
					if(bCut){iCopyToWork--;}else{iCopyFromWork++;};
				}else{ // single line mode
					if(bJoinWithNext){
						if(bCut){iCopyToWork--;}else{iCopyFromWork++;};
					}else{
						break;
					}
				}
			}
			
			if(bUseCommandDelimiterInsteadOfNewLine){
				strTextToCopy=convertNewLineToCmdDelimiter(strTextToCopy);
			}
			
			if(!bJustCollectText){
				Misc.i().putStringToClipboard(strTextToCopy);
				
				icui.clearDumpAreaSelection();
//				lstbxDumpArea.getSelectionModel().setSelection(-1); //clear selection
			}
		}
		
		if(!bJustCollectText){
			iCopyFrom=-1;
			iCopyTo=-1;
		}
		
		return strTextToCopy;
	}
	
	public void resetCmdHistoryCursor(){
		iCmdHistoryCurrentIndex = astrCmdHistory.size();
	}

	public boolean actionSubmitCommand(final String strCmd){
		if(strCmd.isEmpty() || strCmd.trim().equals(""+getCommandPrefix())){
			icui.clearInputTextField(); 
			return false;
		}
		
		String strType=strTypeCmd;
		boolean bIsCmd=true;
		boolean bShowInfo=true;
		if(strCmd.trim().startsWith(""+getCommentPrefix())){
			strType="Cmt";
			bIsCmd=false;
		}else
		if(!strCmd.trim().startsWith(""+getCommandPrefix())){
			strType="Inv";
			bIsCmd=false;
		}
		
		if(bIsCmd){
			if(strCmd.trim().endsWith(""+getCommentPrefix())){
				bShowInfo=false;
			}
		}
		
//		String strTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+": ";
		if(bShowInfo)dumpInfoEntry(strType+": "+strCmd);
		
		icui.clearInputTextField(); 
		
		// history
		boolean bAdd=true;
		if(!astrCmdHistory.isEmpty()){
			if(astrCmdHistory.get(astrCmdHistory.size()-1).equals(strCmd)){
				bAdd=false; //prevent sequential dups
			}
		}
		
		if(bAdd){
			astrCmdHistory.add(strCmd);
			
			cmdHistSave(strCmd);
			while(astrCmdHistory.size()>iMaxCmdHistSize){
				astrCmdHistory.remove(0);
			}
		}
		
		resetCmdHistoryCursor();
		
		if(strType.equals(strTypeCmd)){
			if(!executeCommand(strCmd)){
				dumpWarnEntry(strType+": FAIL: "+strCmd);
				showHelpForFailedCommand(strCmd);
			}
			
			if(bAddEmptyLineAfterCommand ){
				dumpEntry("");
			}
		}
		
		icui.scrollToBottomRequest();
		
		return bIsCmd;
	}

//	public void setConsoleUI(IConsoleUI icui) {
//		this.icui=icui;
//	}
}