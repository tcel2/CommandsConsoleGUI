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

package com.github.commandsconsolegui.cmd;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.commandsconsolegui.cmd.VarIdValueOwnerData.IVarIdValueOwner;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.DebugI;
import com.github.commandsconsolegui.misc.DebugI.EDbgKey;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexHacks;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * All methods starting with "cmd" are directly accessible by user console commands.
 * Here are all base command related methods.
 * 
 * No instance for this, the user must create such instance to use anyplace needed,
 * so specialized methods will be recognized properly. Class name ends with I to 
 * indicate that!
 * Use {@link GlobalCommandsDelegatorI} to set and access such instance.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CommandsDelegator implements IReflexFillCfg, IHandleExceptions{
//	public static final T instance;
	
	
	/**
	 * TODO temporary variable used only during methods migration, commands class must not depend/know about state class.
	 */
//	public ConsoleGuiStateAbs csaTmp = null;
	
//	protected SimpleApplication	sapp;
	
	// not protected... development token... 
	public final String	TOKEN_CMD_NOT_WORKING_YET = "[NOTWORKINGYET]";
	
	/**
	 * Togglers:
	 * 
	 * Adding a toggler field on any class, 
	 * will automatically create the related console command!
	 */
//	public final BoolToggler	btgAcceptExternalExitRequests = new BoolToggler(this,false,strTogglerCodePrefix, 
//		"if a ");
	public final BoolTogglerCmdField	btgDbAutoBkp = new BoolTogglerCmdField(this,false,BoolTogglerCmdField.strTogglerCodePrefix, 
		"whenever a save happens, if the DB was modified, a backup will be created of the old file");
	public final BoolTogglerCmdField	btgShowWarn = new BoolTogglerCmdField(this,true);
	public final BoolTogglerCmdField	btgShowInfo = new BoolTogglerCmdField(this,true);
	public final BoolTogglerCmdField	btgShowException = new BoolTogglerCmdField(this,true);
	public final BoolTogglerCmdField	btgDumpToTerminal = new BoolTogglerCmdField(this,true,BoolTogglerCmdField.strTogglerCodePrefix,
		"The system terminal where the application is being run, will also receive "+CommandsDelegator.class.getSimpleName()+" output.");
	public final BoolTogglerCmdField	btgEngineStatsView = new BoolTogglerCmdField(this,false);
	public final BoolTogglerCmdField	btgEngineStatsFps = new BoolTogglerCmdField(this,false);
	public final BoolTogglerCmdField	btgShowMiliseconds=new BoolTogglerCmdField(this,false);
	public final BoolTogglerCmdField	btgFpsLimit=new BoolTogglerCmdField(this,false);
	public final BoolTogglerCmdField	btgConsoleCpuRest=new BoolTogglerCmdField(this,false,BoolTogglerCmdField.strTogglerCodePrefix,
		"Console update steps will be skipped if this is enabled.");
	public final BoolTogglerCmdField	btgAutoScroll=new BoolTogglerCmdField(this,true);
	public final BoolTogglerCmdField	btgUseFixedLineWrapModeForAllFonts=new BoolTogglerCmdField(this,false,BoolTogglerCmdField.strTogglerCodePrefix,
		"If enabled, this will use a fixed line wrap column even for non mono spaced fonts, "
		+"based on the width of the 'W' character. Otherwise it will dynamically guess the best "
		+"fitting string size.");
	
	/**
	 * Developer vars, keep together!
	 * Initialy true, the default init will disable them.
	 */
	public final BoolTogglerCmdField	btgShowDebugEntries=new BoolTogglerCmdField(this,true);
	public final BoolTogglerCmdField	btgShowDeveloperInfo=new BoolTogglerCmdField(this,true);
	public final BoolTogglerCmdField	btgShowDeveloperWarn=new BoolTogglerCmdField(this,true);
	public final BoolTogglerCmdField	btgShowExecQueuedInfo=new BoolTogglerCmdField(this,true);
	
	/**
	 * keep delayers together!
	 */
	protected TimedDelayVarField tdLetCpuRest = new TimedDelayVarField(this,0.1f,"updates will be skipped and only one update will be processed per delay, if this is active");
	protected TimedDelayVarField tdDumpQueuedSlowEntry = new TimedDelayVarField(this,1f/5f,"how many dump entries will be shown per second (from the slow queue)");
//	protected TimedDelayVarField tdSpareGpuFan = new TimedDelayVarField(this,1.0f/60f); // like 60 FPS
	
	/**
	 * used to hold a reference to the identified/typed user command
	 */
	protected BoolTogglerCmdField	btgReferenceMatched;
	
	/**
	 * user can type these below at console (the actual commands are prepared by reflex)
	 */
	public static final String strFinalCmdCodePrefix="CMD_";
//	public final StringField CMD_CLOSE_CONSOLE = new StringField(this,strFinalCmdCodePrefix);
//	public final StringField CMD_CONSOLE_HEIGHT = new StringField(this,strFinalCmdCodePrefix);
//	public final StringField CMD_CONSOLE_SCROLL_BOTTOM = new StringField(this,strFinalCmdCodePrefix);
//	public final StringField CMD_CONSOLE_STYLE = new StringField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_CONSOLE_SCROLL_BOTTOM = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_DB = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_ECHO = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_FIX_LINE_WRAP = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_FIX_VISIBLE_ROWS_AMOUNT = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_HELP = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_HISTORY = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_HK_TOGGLE = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_LINE_WRAP_AT = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_MESSAGE_REVIEW = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_VAR_SET = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_SLEEP = new StringCmdField(this,strFinalCmdCodePrefix);
	
	/**
	 * this char indicates something that users (non developers) 
	 * should not have direct access.
	 */
	public final Character	RESTRICTED_TOKEN	= '&';
	public final String strFinalFieldRestrictedCmdCodePrefix="RESTRICTED_CMD_";
	public final StringCmdField	RESTRICTED_CMD_SKIP_CURRENT_COMMAND	= new StringCmdField(this,strFinalFieldRestrictedCmdCodePrefix);
	public final StringCmdField	RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE	= new StringCmdField(this,strFinalFieldRestrictedCmdCodePrefix);
	public final StringCmdField	RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS	= new StringCmdField(this,strFinalFieldRestrictedCmdCodePrefix);
	public final StringCmdField	RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS	= new StringCmdField(this,strFinalFieldRestrictedCmdCodePrefix);
	
	/**
	 * more tokens
	 */
	protected Character	chCommandDelimiter = ';';
	protected Character	chAliasPrefix = '$';
	protected Character	chVariableExpandPrefix = chAliasPrefix;
	protected Character	chFilterToken = '~';
	protected	Character chAliasBlockedToken = '-';
	protected Character	chAliasAllowedToken = '+';
	protected	Character chVarDeleteToken = '-';
	protected	Character	chCommentPrefix='#';
	protected	Character	chCommandPrefix='/';
	protected char chCopyRangeIndicator=182;
	protected String strCopyRangeIndicator=""+chCopyRangeIndicator;
	
	/**
	 * etc
	 */
	protected String	strTypeCmd="Cmd";
	
	/** 0 is auto wrap, -1 will trunc big lines */
	protected IntLongVarField ilvConsoleMaxWidthInCharsForLineWrap = new IntLongVarField(this,0,null);
	protected IntLongVarField ilvCurrentFixedLineWrapAtColumn = new IntLongVarField(this,0,null);
	protected HashMap<IConsoleCommandListener,StackTraceElement[]> hmDebugListenerAddedStack = new HashMap<IConsoleCommandListener,StackTraceElement[]>();
	
	protected boolean	bAddEmptyLineAfterCommand = true;
	protected IConsoleUI	icui;
	protected boolean bStartupCmdQueueDone = false; 
	protected CharSequence	strReplaceTAB = "  ";
	protected int	iCopyFrom = -1;
	protected int	iCopyTo = -1;
	protected int	iCmdHistoryCurrentIndex = 0;
	public static final String	strValidCmdCharsRegex = "a-zA-Z0-9_"; // better not allow "-" as has other uses like negate number and commands functionalities
//	protected String	strStatsLast = "";
	protected boolean	bLastAliasCreatedSuccessfuly;
	protected float	fTPF;
	protected long	lNanoFrameTime;
	protected long	lNanoFpsLimiterTime;
	protected String	strFilePrefix = "Console"; //ConsoleStateAbs.class.getSimpleName();
	protected String	strFileTypeLog = "log";
	protected String	strFileTypeConfig = "cfg";
	protected String	strFileCmdHistory = strFilePrefix+"-CmdHist";
	protected String	strFileLastDump = strFilePrefix+"-LastDump";
	protected String	strFileInitConsCmds = strFilePrefix+"-Init";
	protected String	strFileSetup = strFilePrefix+"-Setup";
	protected String	strFileDatabase = strFilePrefix+"-DB";
	protected IntLongVarField ilvMaxCmdHistSize = new IntLongVarField(this,1000,null);
	protected IntLongVarField ilvMaxDumpEntriesAmount = new IntLongVarField(this,100000,"max dump area list size before older ones get removed");
	protected ArrayList<String>	astrCmdAndParams = new ArrayList<String>();
	protected ArrayList<ImportantMsgData>	astrImportantMsgBufferList = new ArrayList<ImportantMsgData>();
	protected ArrayList<String>	astrExecConsoleCmdsQueue = new ArrayList<String>();
	protected ArrayList<PreQueueCmdsBlockSubListData>	astrExecConsoleCmdsPreQueue = new ArrayList<PreQueueCmdsBlockSubListData>();
	protected String	strCmdLinePrepared = "";
	protected TreeMap<String,VarIdValueOwnerData> tmUserVariables = 
		new TreeMap<String, VarIdValueOwnerData>(String.CASE_INSENSITIVE_ORDER);
	protected TreeMap<String,VarIdValueOwnerData> tmRestrictedVariables =
		new TreeMap<String, VarIdValueOwnerData>(String.CASE_INSENSITIVE_ORDER);
	protected File	flCmdHist;
	protected File	flLastDump;
	protected File	flInit;
	protected File	flDB;
	protected File	flSetup;
	protected ArrayList<DumpEntryData> adeDumpEntryFastQueue = new ArrayList<DumpEntryData>();
	protected String	strInfoEntryPrefix			=". ";
	protected String	strWarnEntryPrefix			="?Warn: ";
	protected String	strErrorEntryPrefix			="!ERROR: ";
	protected String	strExceptionEntryPrefix	="!EXCEPTION: ";
	protected String	strDevWarnEntryPrefix="?DevWarn: ";
	protected String	strDevInfoEntryPrefix=". DevInfo: ";
	protected String	strSubEntryPrefix="\t";
	protected Boolean	bIfConditionExecCommands;
	protected ArrayList<ConditionalNestedData> aIfConditionNestedList = new ArrayList<ConditionalNestedData>();
	protected Boolean	bIfConditionIsValid;
	protected String	strCmdLineOriginal;
	protected ArrayList<String> astrCmdHistory = new ArrayList<String>();
//	protected ArrayList<String> astrCmdWithCmtValidList = new ArrayList<String>();
//	protected ArrayList<String> astrBaseCmdValidList = new ArrayList<String>();
	protected ArrayList<AliasData> aAliasList = new ArrayList<AliasData>();
	protected ArrayList<CommandData> acmdList = new ArrayList<CommandData>();
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		if(rfcv.getClass().isAssignableFrom(BoolTogglerCmdField.class)){
			if(BoolTogglerCmdField.strTogglerCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
				rfcfg.strCommandSuffix="Toggle";
			}
		}else
		if(rfcv.getClass().isAssignableFrom(StringCmdField.class)){
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
	protected CommandsDelegator setCommandDelimiter(Character chCommandDelimiter) {
		this.chCommandDelimiter = chCommandDelimiter;
		return this;
	}
	public Character getAliasPrefix() {
		return chAliasPrefix;
	}
	protected CommandsDelegator setAliasPrefix(Character chAliasPrefix) {
		this.chAliasPrefix = chAliasPrefix;
		return this;
	}
	public Character getVariableExpandPrefix() {
		return chVariableExpandPrefix;
	}
	protected CommandsDelegator setVariableExpandPrefix(Character chVariableExpandPrefix) {
		this.chVariableExpandPrefix = chVariableExpandPrefix;
		return this;
	}
	public Character getFilterToken() {
		return chFilterToken;
	}
	protected CommandsDelegator setFilterToken(Character chFilterToken) {
		this.chFilterToken = chFilterToken;
		return this;
	}
	public Character getAliasBlockedToken() {
		return chAliasBlockedToken;
	}
	protected CommandsDelegator setAliasBlockedToken(Character chAliasBlockedToken) {
		this.chAliasBlockedToken = chAliasBlockedToken;
		return this;
	}
	public Character getAliasAllowedToken() {
		return chAliasAllowedToken;
	}
	protected CommandsDelegator setAliasAllowedToken(Character chAliasAllowedToken) {
		this.chAliasAllowedToken = chAliasAllowedToken;
		return this;
	}
	public Character getVarDeleteToken() {
		return chVarDeleteToken;
	}
	public String getVarDeleteTokenStr() {
		return ""+chVarDeleteToken;
	}
	protected CommandsDelegator setVarDeleteToken(Character chVarDeleteToken) {
		this.chVarDeleteToken = chVarDeleteToken;
		return this;
	}
	public Character getCommentPrefix() {
		return chCommentPrefix;
	}
	protected CommandsDelegator setCommentPrefix(Character chCommentPrefix) {
		this.chCommentPrefix = chCommentPrefix;
		return this;
	}
	public Character getCommandPrefix() {
		return chCommandPrefix;
	}
	protected CommandsDelegator setCommandPrefix(Character chCommandPrefix) {
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
		//TODO sleep or wait for application to stop outside here?
		System.exit(0);
	}
	
	protected boolean checkCmdValidityBoolTogglers(){
		btgReferenceMatched=null;
		for(BoolTogglerCmdField btg : BoolTogglerCmdField.getListCopy()){
			if(checkCmdValidity(btg.getOwnerAsCmdListener(), btg.getCmdId(), "[bEnable] "+btg.getHelp(), true)){
				btgReferenceMatched = btg;
				break;
			}
		}
		return btgReferenceMatched!=null;
	}
	
	private class PseudoSelfListener implements IConsoleCommandListener{
		@Override
		public ECmdReturnStatus execConsoleCommand(CommandsDelegator ccRequester) {
			throw new NullPointerException("This method shall never be called!");
		}
	}
	protected PseudoSelfListener icclPseudo = new PseudoSelfListener();
	
	public String getListenerId(IConsoleCommandListener iccl){
		if(iccl.equals(icclPseudo)){
			return "ROOT";
		}
		
		return iccl.getClass().getSimpleName();
	}
	
	public IConsoleCommandListener getPseudoListener(){
		return icclPseudo;
	}
	
	public boolean checkCmdValidity(IConsoleCommandListener iccl, String strValidCmd){
		return checkCmdValidity(iccl, strValidCmd, null);
	}
	public boolean checkCmdValidity(IConsoleCommandListener iccl, StringCmdField strfValidCmd, String strComment){
		if(strComment==null){
			strComment = strfValidCmd.getHelpComment();
		}else{
			if(strfValidCmd.getHelpComment()!=null){
				strComment+="\n"+strfValidCmd.getHelpComment();
			}
		}
		return checkCmdValidity(iccl, strfValidCmd.toString(), strComment);
	}
	public boolean checkCmdValidity(IConsoleCommandListener iccl, String strValidCmd, String strComment){
		return checkCmdValidity(iccl, strValidCmd, strComment, false);
	}
	public boolean checkCmdValidity(IConsoleCommandListener iccl, String strValidCmd, String strComment, boolean bSkipSortCheck){
//		if(strCmdLinePrepared==null){
		if(bFillCommandList){
			if(strComment!=null){
				strValidCmd+=commentToAppend(strComment);
			}
			
			addCmdToValidList(iccl,strValidCmd,bSkipSortCheck);
			
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
	
	protected boolean cmdEcho() {
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

	public String	strDebugTest = ""; //no problem be public

	protected boolean	bConfigured;

	protected ArrayList<String>	astrBaseCmdCacheList = new ArrayList<String>();
	protected ArrayList<String>	astrBaseCmdCmtCacheList = new ArrayList<String>();

	private String	strLastTypedUserCommand;

	private ArrayList<Exception>	aExceptionList = new ArrayList<Exception>();

	protected String	strCurrentDay;

	private boolean	bFillCommandList;

	private boolean	bInitialized;


//	private ECmdReturnStatus	ecrsCurrentCommandReturnStatus;
	
	protected void assertConfigured(){
		if(bConfigured)return;
		throw new NullPointerException(CommandsDelegator.class.getName()+" was not configured!");
	}
	
	/**
	 * Any class can attach it's command interpreter.
	 * @param icc
	 */
	public void addConsoleCommandListener(IConsoleCommandListener icc){
		if(icc==null)throw new NullPointerException("invalid null commands listener.");
		if(aConsoleCommandListenerList.contains(icc)){
			NullPointerException ex = new NullPointerException("listener already added: "+icc.getClass().getName());
			Throwable tw = new Throwable("Listener added at:");
			tw.setStackTrace(hmDebugListenerAddedStack.get(icc));
			ex.initCause(tw);
			throw ex;
		}
		hmDebugListenerAddedStack.put(icc,Thread.currentThread().getStackTrace());
		aConsoleCommandListenerList.add(icc);
		
		//this will let it fill the commands list for this listener
		bFillCommandList=true;
		icc.execConsoleCommand(this); 
		bFillCommandList=false;
	}
	
	public static enum ECmdReturnStatus{
		FoundAndWorked,
		FoundAndFailedGracefully,
		FoundAndExceptionHappened,
		NotFound,
		Skip,
		;
	}
	
	/**
	 * This is the delegator, shall not be overriden.
	 * TODO set to final, make scripting commands a simple plugin...
	 * 
	 * TODO rename to execCmdFromConsoleRequestRoot()
	 * @return
	 */
	protected ECmdReturnStatus executePreparedCommandRoot(){
		if(RESTRICTED_CMD_SKIP_CURRENT_COMMAND.equals(strCmdLinePrepared)){
			return ECmdReturnStatus.Skip;
		}
		
		Boolean bCmdWorked = null; //must be filled with true or false to have been found!
		
		if(checkCmdValidityBoolTogglers()){
			bCmdWorked=toggle(btgReferenceMatched);
		}else
		if(checkCmdValidity(icclPseudo,"alias",getAliasHelp(),true)){
			bCmdWorked=cmdAlias();
		}else
		if(checkCmdValidity(icclPseudo,"clearCommandsHistory")){
			astrCmdHistory.clear();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,"clearDumpArea")){
			icui.getDumpEntries().clear();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,CMD_CONSOLE_SCROLL_BOTTOM,"")){
			icui.scrollToBottomRequest();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,CMD_DB,EDataBaseOperations.help())){
			bCmdWorked=cmdDb();
		}else
		if(checkCmdValidity(icclPseudo,CMD_ECHO," simply echo something")){
			bCmdWorked=cmdEcho();
		}else
		if(checkCmdValidity(icclPseudo,"editShowClipboad","--noNL")){
			String strParam1 = paramString(1);
			boolean bShowNL=true;
			if(strParam1!=null){
				if(strParam1.equals("--noNL")){
					bShowNL=false;
				}
			}
			showClipboard(bShowNL);
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,"editCopy","-d end lines with command delimiter instead of NL;")){
			bCmdWorked=icui.cmdEditCopyOrCut(false);
		}else
		if(checkCmdValidity(icclPseudo,"editCut","like copy, but cut :)")){
			bCmdWorked=icui.cmdEditCopyOrCut(true);
		}else
		if(checkCmdValidity(icclPseudo,"execBatchCmdsFromFile ","<strFileName>")){
			String strFile = paramString(1);
			if(strFile!=null){
				addCmdListOneByOneToQueue(MiscI.i().fileLoad(strFile),false,false);
//				astrExecConsoleCmdsQueue.addAll(Misc.i().fileLoad(strFile));
				bCmdWorked=true;
			}
		}else
		if(checkCmdValidity(icclPseudo,"exit","the application")){
			cmdExit();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,"fileShowData ","<ini|setup|CompleteFileName> show contents of file at dump area")){
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
					for(String str : MiscI.i().fileLoad(fl)){
						dumpSubEntry(str);
					}
				}else{
					dumpWarnEntry("File does not exist: "+fl.getAbsolutePath());
				}
			}
			
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,CMD_FIX_LINE_WRAP ,"in case words are overlapping")){
			icui.cmdLineWrapDisableDumpArea();
			bCmdWorked = true;
		}else
		if(checkCmdValidity(icclPseudo,CMD_FIX_VISIBLE_ROWS_AMOUNT,"[iAmount] in case it is not showing as many rows as it should")){
			icui.setVisibleRowsAdjustRequest(paramInt(1));
			if(!icui.isVisibleRowsAdjustRequested())icui.setVisibleRowsAdjustRequest(0);
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,CMD_HELP,"[strFilter] show (filtered) available commands")){
			cmdShowHelp(paramString(1));
			/**
			 * ALWAYS return TRUE here, to avoid infinite loop when improving some failed command help info!
			 */
			bCmdWorked=true; 
		}else
		if(checkCmdValidity(icclPseudo,CMD_HISTORY,"[strFilter] of issued commands (the filter results in sorted uniques)")){
			bCmdWorked=cmdShowHistory();
		}else
//		if(checkCmdValidity(icclPseudo,CMD_HK_TOGGLE ,"[bEnable] allow hacks to provide workarounds")){
//			if(paramBooleanCheckForToggle(1)){
//				Boolean bEnable = paramBoolean(1);
//				icui.setHKenabled(bEnable);
//				bCmdEndedGracefully=true;
//			}
//		}else
		if(checkCmdValidity(icclPseudo,CMD_LINE_WRAP_AT,"[iMaxChars] 0 = wrap will be automatic")){
			Integer i = paramInt(1);
			if(i!=null && i>=0){ // a value was supplied
				ilvConsoleMaxWidthInCharsForLineWrap.setObjectValue(i);
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
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,CMD_MESSAGE_REVIEW,"[filter]|[index [stackLimit]] if filter is an index, and it has an exception, the complete exception will be dumped.")){
			String strFilter = paramString(1);
			Integer iIndex = paramInt(1,true);
			
			Integer iStackLimit = paramInt(2,true);
			
//			for(ImportantMsg imsg:astrImportantMsgBufferList){
			for(int i=0;i<astrImportantMsgBufferList.size();i++){
				if(iIndex!=null && iIndex.intValue()!=i)continue;
				
				ImportantMsgData imsg = astrImportantMsgBufferList.get(i);
				if(iIndex==null && strFilter!=null && !imsg.strMsg.toLowerCase().contains(strFilter.toLowerCase()))continue;
				
				dumpSubEntry(""+i+": "+imsg.strMsg);
				if(iIndex!=null && (imsg.ex!=null||imsg.aste!=null)){
//					dumpExceptionEntry(imsg,iStackLimit==null?0:iStackLimit,false);
					dumpExceptionEntry(imsg,iStackLimit==null?0:iStackLimit);
				}
			}
			dumpSubEntry("Total: "+astrImportantMsgBufferList.size());
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,"quit","the application")){
			cmdExit();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,"reset","will reset the console (restart it)")){
			cmdResetConsole();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,"showSetup","show restricted variables")){
			for(String str:MiscI.i().fileLoad(flSetup)){
				dumpSubEntry(str);
			}
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,CMD_SLEEP,"<fDelay> [singleCmd] will wait before executing next command in the command block; alternatively will wait before executing command in-line, but then it will not sleep the block it is in!")){
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
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,"statsEnable","[idToEnable [bEnable]] empty for a list. bEnable empty to toggle.")){
			bCmdWorked=true;
			String strId=paramString(1);
			Boolean bValue=paramBoolean(2);
			if(strId!=null){
				EStats e=null;
				try{e=EStats.valueOf(strId);}catch(IllegalArgumentException ex){
					bCmdWorked=false;
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
		if(checkCmdValidity(icclPseudo,"statsFieldToggle","[bEnable] toggle simple stats field visibility")){
			bCmdWorked=icui.statsFieldToggle();
		}else
		if(checkCmdValidity(icclPseudo,"statsShowAll","show all console stats")){
			dumpAllStats();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,"test","[...] temporary developer tests")){
			cmdTest();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(icclPseudo,"varAdd","<varId> <[-]value>")){
			bCmdWorked=cmdVarAdd(paramString(1),paramString(2),true,false);
		}else
		if(
				checkCmdValidity(icclPseudo,CMD_VAR_SET,
				"<[<varId> <value>] | [-varId]> "
					+"Can be boolean(true/false, and after set accepts 1/0), number(integer/floating) or string; "
					+"-varId will delete it; "
					+"Retrieve it's value with "+getVariableExpandPrefix()+"{varId}; "
					+"Restricted variables will have no effect; "
			)
		){
			bCmdWorked=cmdVarSet();
		}else
		if(checkCmdValidity(icclPseudo,"varSetCmp","<varIdBool> <value> <cmp> <value>")){
			bCmdWorked=cmdVarSetCmp();
		}else
		if(checkCmdValidity(icclPseudo,"varShow","[["+RESTRICTED_TOKEN+"]filter] list user or restricted variables.")){
			bCmdWorked=cmdVarShow();
		}else
		if(checkCmdValidity(icclPseudo,TOKEN_CMD_NOT_WORKING_YET+"zDisabledCommand"," just to show how to use it")){
			// keep this as reference
		}else
		{
			if(!bFillCommandList){ //when each listener is added, they already fill the list!
				for(IConsoleCommandListener icc:aConsoleCommandListenerList){
					ECmdReturnStatus ecrs = icc.execConsoleCommand(this);
					switch(ecrs){
						case NotFound:
						case Skip:
							continue; //try next cmd listener
						default:
							return ecrs;
					}
				}
			}
		}
		
		if(bCmdWorked==null)return ECmdReturnStatus.NotFound;
		
		/**
		 * exception will be captured out of here
		 */
		return cmdFoundReturnStatus(bCmdWorked);
	}
	
	public void cmdResetConsole() {
		icui.requestRestart();
//		icui.recreateConsoleGui();
	}

	protected ECmdReturnStatus cmdRawLineCheckAlias(){
		bLastAliasCreatedSuccessfuly=false;
		
		if(strCmdLineOriginal==null)return ECmdReturnStatus.NotFound;
		
		String strCmdLine = strCmdLineOriginal.trim();
		String strExecAliasPrefix = ""+getCommandPrefix()+getAliasPrefix();
		if(strCmdLine.startsWith(getCommandPrefix()+"alias ")){
			/**
			 * create
			 */
			AliasData alias = new AliasData(this);
			
			String[] astr = strCmdLine.split(" ");
			if(astr.length>=3){
				alias.strAliasId=astr[1];
				if(hasVar(alias.strAliasId)){
					dumpErrorEntry("Alias identifier '"+alias.strAliasId+"' conflicts with existing variable!");
					return ECmdReturnStatus.FoundAndFailedGracefully;
				}
				
				alias.strCmdLine=String.join(" ", Arrays.copyOfRange(astr, 2, astr.length));
				
				AliasData aliasFound=getAlias(alias.strAliasId);
				if(aliasFound!=null)aAliasList.remove(aliasFound);
				
				aAliasList.add(alias);
				MiscI.i().fileAppendLine(flDB, alias.toString());
				dumpSubEntry(alias.toString());
				
				bLastAliasCreatedSuccessfuly = true;
				return ECmdReturnStatus.FoundAndWorked;
			}
			
			/**
			 * parameters were missing...
			 */
			return ECmdReturnStatus.FoundAndFailedGracefully;
		}else
		if(strCmdLine.startsWith(strExecAliasPrefix)){
			/**
			 * execute
			 */
			String strAliasId=strCmdLine
				.split(" ")[0]
				.substring(strExecAliasPrefix.length())
				.toLowerCase();
			AliasData alias = getAlias(strAliasId);
			if(alias!=null){
				if(alias.bBlocked){
					dumpWarnEntry(alias.toString()); //will show the blocked status
					
					return ECmdReturnStatus.FoundAndFailedGracefully;
				}else{
					addCmdToQueue(alias.strCmdLine
						+commentToAppend("alias="+alias.strAliasId), true);
					return ECmdReturnStatus.FoundAndWorked;
				}
			}else{
				dumpWarnEntry("Alias not found: "+strAliasId);
				
				/**
				 * alias execution request/prefix was found, so it is not some other command...
				 */
				return ECmdReturnStatus.FoundAndFailedGracefully;
			}
		}
		
		return ECmdReturnStatus.NotFound;
	}

	/**
	 * provides line-wrap
	 * 
	 * @param bDumpToConsole if false, will only log to file
	 * @param strLineOriginal
	 */
	protected void dumpEntry(DumpEntryData de){
		dumpSave(de);
		
//		PrintStream output = System.out;
//		if(de.isStderr())output = System.err;
//		output.println("CONS: "+de.getLineOriginal());
		if(btgDumpToTerminal.b())de.sendToPrintStream("[CCUI]"+de.getLineOriginal().replace("\t","  ")); //remove tabs for better compatibility 
		
		if(!icui.isInitializationCompleted()){
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
			
			int iWrapAt = ilvConsoleMaxWidthInCharsForLineWrap.intValue();
//			if(iConsoleMaxWidthInCharsForLineWrap==null){
			if(iWrapAt==0){ //updateFontStuff();
				iWrapAt = icui.getLineWrapAt();
//				if(STYLE_CONSOLE.equals(strStyle)){ //TODO is faster?
//					iWrapAt = (int) (widthForDumpEntryField() / fWidestCharForCurrentStyleFont ); //'W' but any char will do for monospaced font
//				}
			}
			
			ilvCurrentFixedLineWrapAtColumn.setObjectValue(iWrapAt);
			
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

	protected void applyDumpEntryOrPutToSlowQueue(boolean bUseSlowQueue, String str) {
		if(bUseSlowQueue){
			icui.getDumpEntriesSlowedQueue().add(str);
		}else{
			icui.getDumpEntries().add(str);
		}
	}
	
	protected void updateDumpQueueEntry(){
		while(adeDumpEntryFastQueue.size()>0){
			dumpEntry(adeDumpEntryFastQueue.remove(0));
		}
			
		if(!tdDumpQueuedSlowEntry.isReady(true))return;
		
		if(icui.getDumpEntriesSlowedQueue().size()>0){
			icui.getDumpEntries().add(icui.getDumpEntriesSlowedQueue().remove(0));
			
			while(icui.getDumpEntries().size() > ilvMaxDumpEntriesAmount.getLong()){
				icui.getDumpEntries().remove(0);
			}
		}
	}
	
	public void dumpInfoEntry(String str){
		dumpEntry(false, btgShowInfo.get(), false, MiscI.i().getSimpleTime(btgShowMiliseconds.get())+strInfoEntryPrefix+str);
	}
	
	public void dumpWarnEntry(String str){
		String strType = "Warn";
		Exception ex = new Exception("(This is just a "+strType+" stacktrace) "+str);
		ex.setStackTrace(Thread.currentThread().getStackTrace());
		addImportantMsgToBuffer(strType,str,ex);
		dumpEntry(false, btgShowWarn.get(), false, MiscI.i().getSimpleTime(btgShowMiliseconds.get())+strWarnEntryPrefix+str);
	}
	
	private void addImportantMsgToBuffer(String strMsgType,String strMsgKey,Exception ex){
		addImportantMsgToBuffer(strMsgType, new ImportantMsgData(strMsgKey,ex,ex.getStackTrace()));
	}
//	private void addImportantMsgToBuffer(String strMsgType,String strMsgKey,StackTraceElement[] aste){
//		addImportantMsgToBuffer(strMsgType, new ImportantMsg(strMsgKey,null,aste));
//	}
	private void addImportantMsgToBuffer(String strMsgType,ImportantMsgData imsg){
		String str="["+strMsgType+"] "+imsg.strMsg;
		if(astrImportantMsgBufferList.contains(imsg)){ //that object overriden hashcode/equals is used at contains() 
			/**
			 * so, being re-added it will be refreshed and remain longer on the list
			 */
			astrImportantMsgBufferList.remove(imsg);
		}
		
		astrImportantMsgBufferList.add(imsg);
		
		if(astrImportantMsgBufferList.size()>1000)astrImportantMsgBufferList.remove(0);
	}
	
	public void dumpErrorEntry(String str){
		String strType = "ERROR";
		Exception ex = new Exception("(This is just a "+strType+" stacktrace) "+str);
		ex.setStackTrace(Thread.currentThread().getStackTrace());
		addImportantMsgToBuffer(strType,str,ex);
		dumpEntry(new DumpEntryData()
			.setPrintStream(System.err)
			.setDumpToConsole(btgShowWarn.get())
			.setLineOriginal(MiscI.i().getSimpleTime(btgShowMiliseconds.get())+strErrorEntryPrefix+str)
		);
//		dumpEntry(false, btgShowWarn.get(), false, Misc.i().getSimpleTime(btgShowMiliseconds.get())+strErrorEntryPrefix+str);
	}
	
	/**
	 * warnings that should not bother end users...
	 * @param str
	 */
	public void dumpDevWarnEntry(String str){
		String strType = "DevWarn";
		Exception ex = new Exception("(This is just a "+strType+" stacktrace) "+str);
		ex.setStackTrace(Thread.currentThread().getStackTrace());
		addImportantMsgToBuffer(strType,str,ex);
		dumpEntry(false, btgShowDeveloperWarn.get(), false, 
				MiscI.i().getSimpleTime(btgShowMiliseconds.get())+strDevWarnEntryPrefix+str);
	}
	
	public void dumpDebugEntry(String str){
		dumpEntry(new DumpEntryData()
			.setDumpToConsole(btgShowDebugEntries.get())
			.setLineOriginal("[DBG]"+str)
		);
	}
	
	public void dumpDevInfoEntry(String str){
		dumpEntry(new DumpEntryData()
			.setDumpToConsole(btgShowDeveloperInfo.get())
			.setLineOriginal(MiscI.i().getSimpleTime(btgShowMiliseconds.get())+strDevInfoEntryPrefix+str)
		);
//		dumpEntry(false, btgShowDeveloperInfo.get(), false, 
//			Misc.i().getSimpleTime(btgShowMiliseconds.get())+strDevInfoEntryPrefix+str);
	}
	
	protected void dumpExceptionEntry(ImportantMsgData imsg, Integer iShowStackElementsCount) {
		/**
		 * it is coming from the message buffer, so will not readd it...
		 */
		dumpExceptionEntry(imsg.ex, imsg.aste, iShowStackElementsCount, false);
	}
	public void dumpExceptionEntry(Exception ex){
		dumpExceptionEntry(ex, null, null, true);
	}
	/**
	 * 
	 * @param ex
	 * @param asteStackOverride if null will use the exception one
	 * @param iShowStackElementsCount if null, will show nothing. If 0, will show all.
	 * @param bAddToMsgBuffer
	 */
	protected void dumpExceptionEntry(Exception ex, StackTraceElement[] asteStackOverride, Integer iShowStackElementsCount, boolean bAddToMsgBuffer){
		String strTime="";
		PrintStream psStack = System.err;
		PrintStream psInfo = System.err;
		if(bAddToMsgBuffer){ //the exception is happening right now
			strTime=MiscI.i().getSimpleTime(btgShowMiliseconds.get());
			addImportantMsgToBuffer("Exception",ex.toString(),ex);
			ex.printStackTrace();
			psStack = null; //avoiding dup: already dumped to terminal, above
		}else{
			/**
			 * if it is not being added to buffer, means it is being reviewed by developer/user
			 * so will use stdout, as the exception is not happening right now.
			 */
			psInfo = psStack = System.out;
		}
		
//		dumpEntry(false, btgShowException.get(), false, 
//			strTime+strExceptionEntryPrefix+ex.toString());
		dumpEntry(new DumpEntryData()
			.setPrintStream(psInfo) //this is good to show the time at terminal
			.setApplyNewLineRequests(false)
			.setDumpToConsole(btgShowException.get())
			.setUseSlowQueue(false)
			.setLineOriginal(strTime+strExceptionEntryPrefix+ex.toString()));
		
		if(iShowStackElementsCount!=null){
			if(asteStackOverride==null)asteStackOverride=ex.getStackTrace();
			
			for(int i=0;i<asteStackOverride.length;i++){
				StackTraceElement ste = asteStackOverride[i]; 
				if(iShowStackElementsCount>0 && i>=iShowStackElementsCount)break;
				dumpEntry(new DumpEntryData()
					.setPrintStream(psStack) 
					.setApplyNewLineRequests(true)
					.setDumpToConsole(true)
					.setUseSlowQueue(false)
					.setLineOriginal(strSubEntryPrefix+ste.toString()));
			}
		}
	}
	
	/**
	 * a simple, usually indented, output
	 * @param str
	 */
	public void dumpSubEntry(String str){
		if(str.contains("\n")){
			String[] astr = str.split("\n");
			for(String strLine:astr){
				dumpEntry(strSubEntryPrefix+strLine);
			}
		}else{
			dumpEntry(strSubEntryPrefix+str);
		}
	}
	
	protected void addCmdToValidList(IConsoleCommandListener iccl, String strNew, boolean bSkipSortCheck){
		String strConflict=null;
		
		if(iccl==null)throw new PrerequisitesNotMetException("listener reference cannot be null");
			
//		if(!astrCmdWithCmtValidList.contains(strNew)){
		if(!strNew.startsWith(TOKEN_CMD_NOT_WORKING_YET)){
//			String strBaseCmdNew = extractCommandPart(strNew,0);
			String strBaseCmdNew = strNew.split(" ")[0];
			String strComment = "";
			if(strNew.length()>strBaseCmdNew.length()){
				strComment= strNew.substring(strBaseCmdNew.length());
			}
			
			/**
			 * conflict check
			 */
			for(CommandData cmd:acmdList){
//			for(String strBase:astrBaseCmdValidList){
				if(cmd.getBaseCmd().equalsIgnoreCase(strBaseCmdNew)){
					strConflict="";
					
					String strOwner = getListenerId(cmd.getOwner());
//					if(cmd.getOwner()!=null)strOwner = cmd.getOwner().getClass().getSimpleName();
					strConflict+="("+strOwner+":"+cmd.getBaseCmd()+")";
					
					strConflict+=" x ";
					
//					if(iccl.equals(icclSelfRoot)){
//						strOwner = getSelfRootListenerId();
//					}else{
//						strOwner = iccl.getClass().getSimpleName();
//					}
					
					strConflict+="("+strOwner+":"+strBaseCmdNew+")";
					
					break;
				}
			}
			
			if(strConflict==null){
				acmdList.add(new CommandData(iccl, strBaseCmdNew, strComment));
//				astrBaseCmdValidList.add(strBaseCmdNew);
//				astrCmdWithCmtValidList.add(strNew);
				
				/**
				 * coded sorting check (unnecessary actually), just useful for developers
				 * be more organized. 
				 */
				if(!bSkipSortCheck && acmdList.size()>0){
					String strLast = acmdList.get(acmdList.size()-1).getBaseCmd();
					if(strLast.compareToIgnoreCase(strBaseCmdNew)>0){
						dumpDevWarnEntry("sorting required, last '"+strLast+"' new '"+strBaseCmdNew+"'");
					}
				}
			}
		}
//		}
		
		if(strConflict!=null){
			dumpExceptionEntry(new NullPointerException("ConflictCmdId: "+strConflict));
		}
	}
	
	protected boolean isCommentedLine(){
		if(strCmdLinePrepared==null)return false;
		return strCmdLinePrepared.trim().startsWith(""+getCommentPrefix());
	}

	protected boolean cmdVarShow() {
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
		
//		setupVars(false); //this will refresh any pending variables
//		databaseSave(); //this will refresh any pending variables too
		
		ArrayList<String> astr = new ArrayList<String>();
//		if(strFilter!=null)strFilter=strFilter.substring(1);
		for(String strVarId : getVariablesIdentifiers(true)){
			if(isRestricted(strVarId) && !bRestrictedOnly)continue;
			if(!isRestricted(strVarId) && bRestrictedOnly)continue;
			
			/**
			 * empty filter will work too.
			 */
			if(strVarId.toLowerCase().contains(strFilter.toLowerCase())){
				astr.add(strVarId);
			}
		}
		Collections.sort(astr);
		for(String str:astr){
			varReport(str);
		}
		
		dumpSubEntry(getCommentPrefix()+"UserVarListHashCode="+tmUserVariables.hashCode());
		
		return true;
	}
	
	protected boolean varDelete(String strVarId){
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
	protected boolean cmdVarSet() {
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
	
	protected boolean isRestrictedAndDoesNotExist(String strVar){
		if(isRestricted(strVar)){
			// user can only set existing restricted vars
			if(!selectVarSource(strVar).containsKey(strVar)){
				dumpWarnEntry("Restricted var does not exist: "+strVar);
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean cmdVarSetCmp() {
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
				(MiscI.i().parseBoolean(strValueLeft) || MiscI.i().parseBoolean(strValueRight)), true);
		}else
		if(strCmp.equals("&&")){
			return varSet(strVarId, ""+
				(MiscI.i().parseBoolean(strValueLeft) && MiscI.i().parseBoolean(strValueRight)), true);
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

	protected boolean databaseSave(){
		ArrayList<String> astr = new ArrayList<>();
		
		ArrayList<String> astrVarList = getVariablesIdentifiers(false);
		for(String strVarId:astrVarList){
			astr.add(varReportPrepare(strVarId));
		}
		
		for(AliasData alias:aAliasList){
			astr.add(alias.toString());
		}
		
		flDB.delete();
		MiscI.i().fileAppendLine(flDB, getCommentPrefix()+" DO NOT MODIFY! auto generated. Set overrides at user init file!");
		MiscI.i().fileAppendListTS(flDB, astr);
		
		dumpInfoEntry("Database saved: "
			+astrVarList.size()+" vars, "
			+aAliasList.size()+" aliases, "
			+flDB.length()+" bytes,");
		
		setupRecreateFile();
		
		return true;
	}
	
	protected boolean cmdDatabase(EDataBaseOperations edbo){
		if(edbo==null)return false;
		
		switch(edbo){
			case load:
				/**
				 * prepend on the queue is important mainly at the initialization
				 */
				dumpInfoEntry("Loading Console Database:");
				addCmdListOneByOneToQueue(MiscI.i().fileLoad(flDB),true,false);
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
				for(String str:MiscI.i().fileLoad(flDB)){
					dumpSubEntry(str);
				}
				return true;
		}
		
		return false;
	}
	
	protected boolean hasChanged(ERestrictedSetupLoadableVars rv){
		String strValue = varGetValueString(""+RESTRICTED_TOKEN+rv);
		switch(rv){
			case userAliasListHashcode:
				return !(""+aAliasList.hashCode()).equals(strValue);
			case userVariableListHashcode:
				return !(""+tmUserVariables.hashCode()).equals(strValue);
		}
		
		return false;
	}
	
	protected boolean isDatabaseChanged(){
		if(hasChanged(ERestrictedSetupLoadableVars.userAliasListHashcode))return true;
		if(hasChanged(ERestrictedSetupLoadableVars.userVariableListHashcode))return true;
		
		return false;
	}
	
	protected boolean databaseBackup() {
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
	
	protected String getAliasHelp() {
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
	
	protected boolean cmdAlias() {
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
				for(AliasData alias:aAliasList){
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
	
	protected boolean cmdShowHistory() {
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

	protected boolean isStartupCommandsQueueDone(){
		return bStartupCmdQueueDone;
	}
	
	protected boolean cmdDb() {
		String strOpt = paramString(1);
		if(strOpt!=null){
			EDataBaseOperations edb = null;
			try {edb = EDataBaseOperations.valueOf(strOpt);}catch(IllegalArgumentException e){}
			return cmdDatabase(edb);
		}
		return false;
	}

	protected AliasData getAlias(String strAliasId){
		AliasData aliasFound=null;
		for(AliasData aliasCheck : aAliasList){
			if(aliasCheck.strAliasId.toLowerCase().equals(strAliasId.toLowerCase())){
				aliasFound = aliasCheck;
				break;
			}
		}
		return aliasFound;
	}
	
	protected boolean hasVar(String strVarId){
		return getVar(strVarId)!=null;
	}
	
	protected VarIdValueOwnerData getVar(String strVarId){
		return selectVarSource(strVarId).get(strVarId);
	}

	/**
	 * 
	 * @return false if toggle failed
	 */
	protected boolean toggle(BoolTogglerCmdField btg){
		if(paramBooleanCheckForToggle(1)){
			Boolean bEnable = paramBoolean(1);
			btg.set(bEnable==null ? !btg.get() : bEnable); //overrider
			varSet(btg,true);
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
		 *	KEEP THIS COMMENTED CODE AS A WARNING!
		return Boolean.parseBoolean(str);
		 */
		if(str.equals("0"))return false;
		if(str.equals("1"))return true;
		if(str.equalsIgnoreCase("false"))return false;
		if(str.equalsIgnoreCase("true"))return true;
		
		dumpExceptionEntry(new NumberFormatException("invalid string to boolean: "+str));
		
		return null;
	}
	public Integer paramInt(int iIndex){
		return paramInt(iIndex,false);
	}
	public Integer paramInt(int iIndex, boolean bNullOnParseFail){
		String str = paramString(iIndex);
		if(str==null)return null;
		try{return Integer.parseInt(str);}catch(NumberFormatException ex){
			if(!bNullOnParseFail)throw ex;
		};
		return null;
	}
	public Float paramFloat(int iIndex){
		return paramFloat(iIndex,false);
	}
	public Float paramFloat(int iIndex, boolean bNullOnParseFail){
		String str = paramString(iIndex);
		if(str==null)return null;
		try{return Float.parseFloat(str);}catch(NumberFormatException ex){
			if(!bNullOnParseFail)throw ex;
		};
		return null;
	}
	
	protected String prepareCmdAndParams(String strFullCmdLine){
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
			
			return prepareAndCleanMultiCommandsLine(strFullCmdLine);
		}
		
		return null;
	}
	
	protected String getPreparedCmdLine(){
		return strCmdLinePrepared;
	}
	
	/**
	 * Cleans from comments.
	 * Queues multi commands line, will return a command skipper in this case.
	 * 
	 * @param strFullCmdLine
	 * @return the prepared and cleaned single command line, or a skipper
	 */
	protected String prepareAndCleanMultiCommandsLine(String strFullCmdLine){
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
		
		astrCmdAndParams.clear(); //make sure it is emptied
		astrCmdAndParams.addAll(convertToCmdParamsList(strFullCmdLine));
		return String.join(" ",astrCmdAndParams);
	}
	
	public ArrayList<String> getPreparedCmdParamsListCopy(){
		return new ArrayList<String>(astrCmdAndParams);
	}
	
	/**
	 * Each param can be enclosed within double quotes (")
	 * @param strFullCmdLine
	 * @return
	 */
	protected ArrayList<String> convertToCmdParamsList(String strFullCmdLine){
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
	protected String applyVariablesValues(String strParam){
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

	protected boolean cmdVarAdd(String strVarId, String strValueAdd, boolean bSave, boolean bOverwrite){
		return cmdVarAdd(getVar(strVarId), new VarIdValueOwnerData(strVarId, strValueAdd, null, null, null), bSave, bOverwrite);
	}
	/**
	 * In case variable exists will be this method.
	 * @param strVarId
	 * @param strValueAdd
	 * @param bOverwrite
	 * @return
	 */
	protected boolean cmdVarAdd(VarIdValueOwnerData vivoExisting, VarIdValueOwnerData vivoAdd, boolean bSave, boolean bOverwrite){
		if(isRestrictedAndDoesNotExist(vivoExisting.strId))return false;
		
		Object objValueNew = null;
		VarIdValueOwnerData vivoCurrent = selectVarSource(vivoExisting.strId).get(vivoExisting.strId);
		
		if(vivoCurrent.objValue==null){
			dumpExceptionEntry(new NullPointerException("value is null for var "+vivoExisting.strId));
			return false;
		}
		
		String strValueAdd = ""+vivoAdd.objValue;
//		if(vivoCurrent.objValue!=null){
			if(Boolean.class.isAssignableFrom(vivoCurrent.objValue.getClass())){
				// boolean is always overwrite
				objValueNew = MiscI.i().parseBoolean(strValueAdd);
			}else
			if(Long.class.isAssignableFrom(vivoCurrent.objValue.getClass())){
				Long lValueCurrent = (Long)vivoCurrent.objValue;
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
			if(Double.class.isAssignableFrom(vivoCurrent.objValue.getClass())){
				Double dValueCurrent = (Double)vivoCurrent.objValue;
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
				/**
				 * simple String
				 */
				if(bOverwrite)vivoCurrent.objValue="";
				objValueNew = ""+vivoCurrent.objValue+strValueAdd;
			}
//		}else{
//			return varSet(vivo.strId, strValueAdd, true);
//		}
		
//		if(objValueNew==null)return false;
		
		vivoExisting.objValue=objValueNew;
		varApply(vivoExisting,bSave);
		return true;
	}
	
	protected boolean isRestricted(String strId){
		return strId.startsWith(""+RESTRICTED_TOKEN);
	}
	
	protected Object getVarValue(String strVarId){
		return selectVarSource(strVarId).get(strVarId).objValue;
	}
//	protected void setVarValue(String strVarId, VarIdValueOwner vivo){
//		selectVarSource(strVarId).put(strVarId,vivo);
//	}
	protected TreeMap<String, VarIdValueOwnerData> selectVarSource(String strVarId){
		if(isRestricted(strVarId)){
			return tmRestrictedVariables;
		}else{
			return tmUserVariables;
		}
	}
	
	protected File getVarFile(String strVarId){
		if(isRestricted(strVarId)){
			return flSetup;
		}else{
			return flDB;
		}
	}
	
	protected void varSaveSetupFile(){
		for(String strVarId : getVariablesIdentifiers(true)){
			if(isRestricted(strVarId))fileAppendVar(strVarId);
		}
	}
	
	protected void fileAppendVar(String strVarId){
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
		
		MiscI.i().fileAppendLine(getVarFile(strVarId), strCommentOut+varReportPrepare(strVarId)+strReadOnlyComment);
	}
	
	protected boolean varApply(VarIdValueOwnerData vivo, boolean bSave){
		/**
		 * creates the console variable
		 */
		selectVarSource(vivo.strId).put(vivo.strId, vivo);
		if(vivo.owner!=null)vivo.owner.setConsoleVarLink(vivo);
		
		/**
		 * save the console variable
		 */
		if(bSave)fileAppendVar(vivo.strId);
		
		if(isRestricted(vivo.strId) && btgShowDeveloperInfo.b()){
			varReport(vivo.strId);
		}
		
		return true;
	}
	
	public String varReportPrepare(String strVarId) {
		VarIdValueOwnerData vivo = selectVarSource(strVarId).get(strVarId);
		String str="";
		
		// as reusable command
		str+=getCommandPrefix();
		str+=CMD_VAR_SET.toString();
		str+=" ";
		str+=strVarId;
		str+=" ";
		if(vivo!=null){
			str+="\""+vivo.objValue+"\"";
			str+=" ";
		}
		
		// comments
		str+="#";
		// var type
		if(vivo!=null){
			str+=vivo.objValue.getClass().getSimpleName();
		}else{
			str+="(ValueNotSet)";
		}
		str+=" ";
		// scope
		str+=(isRestricted(strVarId)?"(Restricted)":"(User)");
		// dev info
		if(btgShowDeveloperInfo.b()){
			if(vivo.owner!=null && vivo.rfcfgClassHoldingTheOwner!=null){
				str+=" ";
				str+="["+vivo.rfcfgClassHoldingTheOwner.getClass().getName()+"]";
			}
		}
		str+=" ";
		str+=vivo.getHelp();
		
		return str;
	}
	
	public void varReport(String strVarId) {
		VarIdValueOwnerData vivo=selectVarSource(strVarId).get(strVarId);
		if(vivo!=null){
			dumpSubEntry(varReportPrepare(strVarId));
		}else{
			dumpSubEntry(strVarId+" is not set...");
		}
	}
	
	public boolean varSet(StringCmdField sfId, String strValue, boolean bSave) {
		return varSet(
			RESTRICTED_TOKEN+sfId.toString(),
			strValue,
			bSave);
	}
	
//	private boolean varRestoreTo(BoolTogglerCmd btgOwner){
//		VarIdValueOwner vivo = getVar(RESTRICTED_TOKEN+btgOwner.getCmdId());
//		if(vivo==null)return false;
//		btgOwner.setObjectValue(vivo.objValue);
//		dumpSubEntry(btgOwner.getReport());
//		return true;
//	}
//	
//	private boolean varRestoreTo(TimedDelayVar tdOwner){
//		VarIdValueOwner vivo = getVar(RESTRICTED_TOKEN+tdOwner.getVarId());
//		if(vivo==null)return false;
//		tdOwner.setObjectValue(vivo.objValue);
//		dumpSubEntry(tdOwner.getReport());
//		return true;
//	}
	
	private boolean varRestoreTo(IVarIdValueOwner owner){
		VarIdValueOwnerData vivo = getVar(RESTRICTED_TOKEN+owner.getVarId());
		if(vivo==null)return false;
		owner.setObjectValue(vivo.objValue);
		dumpSubEntry(owner.getReport());
		return true;
	}
	
//	public boolean varSet(BoolTogglerCmd btgOwner, boolean bSave) {
//		if(btgOwner.getOwner()==null)return false; //check if it is configured as a field should be
//		
//		return varSet(
//			new VarIdValueOwner(
//				RESTRICTED_TOKEN+btgOwner.getCmdId(),
//				""+btgOwner.getBoolean(),
//				btgOwner),
//			bSave);
//	}
//	/**
//	 * 
//	 * @param tdOwner only the ones configured as class field variables 
//	 * @param bSave
//	 * @return
//	 */
//	public boolean varSet(TimedDelayVar tdOwner, boolean bSave) {
//		if(tdOwner.getOwner()==null)return false; //check if it is configured as a field should be
//		
//		return varSet(
//			new VarIdValueOwner(
//				RESTRICTED_TOKEN+tdOwner.getVarId(),
//				Misc.i().fmtFloat(tdOwner.getDelayLimitSeconds(),3), //store with milis precision
//				tdOwner),
//			bSave);
//	}
	
//	public boolean varSet(FloatingDoubleVar fdvOwner, boolean bSave) {
//		return varSet(
//			new VarIdValueOwner(
//				RESTRICTED_TOKEN+fdvOwner.getVarId(),
//				Misc.i().fmtFloat(fdvOwner.getDouble(),3), //store with milis precision
//				fdvOwner),
//			bSave);
//	}
//	public boolean varSet(IntegerLongVar ilvOwner, boolean bSave) {
//		return varSet(
//			new VarIdValueOwner(
//				RESTRICTED_TOKEN+ilvOwner.getVarId(),
//				ilvOwner.getLong(),
//				ilvOwner),
//			bSave);
//	}
	
	public boolean varSet(IVarIdValueOwner owner, boolean bSave) {
		IReflexFillCfg rfcfg=null;
		if(owner instanceof IReflexFillCfgVariant){
			/**
			 * check if it is configured as a class field should be
			 */
			rfcfg = ((IReflexFillCfgVariant)owner).getOwner();
			if(rfcfg==null)return false;
		}
		
		return varSet(
			new VarIdValueOwnerData(
				RESTRICTED_TOKEN+owner.getVarId(),
				owner.getValueRaw(),
				owner,
				rfcfg,
				owner.getHelp()),
			bSave);
	}
	
	/**
	 * this will apply the value at the owner also (if it exists)
	 * @param strVarId
	 * @param strValue
	 * @param bSave
	 * @return
	 */
	public boolean varSet(String strVarId, String strValue, boolean bSave) {
		boolean bOk=varSet(new VarIdValueOwnerData(strVarId, strValue, null, null, null), bSave);
		
		if(bOk){
			VarIdValueOwnerData vivo = getVar(strVarId);
			if(vivo.owner instanceof IVarIdValueOwner){
				varRestoreTo(vivo.owner);
			}else
//				if(vivo.owner instanceof BoolTogglerCmd){
//					varRestoreTo((BoolTogglerCmd)vivo.owner);
//				}else
//					if(vivo.owner instanceof TimedDelayVar){
//						varRestoreTo((TimedDelayVar)vivo.owner);
//					}else
//						if(vivo.owner instanceof FloatingDoubleVar){
//							varRestoreTo((FloatingDoubleVar)vivo.owner);
//						}else
//							if(vivo.owner instanceof IntegerLongVar){
//								varRestoreTo((IntegerLongVar)vivo.owner);
//							}else
			if(vivo.owner!=null){
				throw new UnsupportedOperationException("Unsupported owner type: "+vivo.owner.getClass().getName());
			}
		}
		
		return bOk;
	}
	
	/**
	 * This is able to create restricted variables too.
	 * 
	 * @param strVarId
	 * @param strValue
	 * @return
	 */
	public boolean varSet(VarIdValueOwnerData vivo, boolean bSave) {
		if(getAlias(vivo.strId)!=null){
			dumpErrorEntry("Variable identifier '"+vivo.strId+"' conflicts with existing alias!");
			return false;
		}
		
		if(vivo.objValue==null)return false; //strValue=""; //just creates the var
		
		VarIdValueOwnerData vivoExisting = getVar(vivo.strId);
		if(vivoExisting!=null){
//			if(vivoExisting.owner instanceof BoolToggler){
//				int i=0;
//			}
			return cmdVarAdd(vivoExisting, vivo, bSave, true);
		}else{
			/**
			 * Priority:
			 * Double would parse a Long.
			 * Boolean would be accepted by String that accepts everything. 
			 */
			boolean bOk=false;
			
			String strValue = ""+vivo.objValue;
			if(!bOk)try{vivo.objValue=Long  .parseLong     (strValue);bOk=varApply(vivo,bSave);}catch(NumberFormatException e){}// accepted exception!
			if(!bOk)try{vivo.objValue=Double.parseDouble   (strValue);bOk=varApply(vivo,bSave);}catch(NumberFormatException e){}// accepted exception!
			if(!bOk)try{vivo.objValue=MiscI.i().parseBoolean(strValue);bOk=varApply(vivo,bSave);}catch(NumberFormatException e){}// accepted exception!
			if(!bOk){vivo.objValue=strValue;bOk=varApply(vivo,bSave);}
			
			return bOk;
		}
	}
	
	/**
	 * 
	 * @param strVarId
	 * @return "null" if not set
	 */
	protected String varGetValueString(String strVarId){
		Object obj = selectVarSource(strVarId).get(strVarId);
		if(obj==null)return "null";
		return ""+obj;
	}
//	protected Double varGetValueDouble(String strVarId){
//		Object obj = ahkVariables.get(strVarId);
//		if(obj==null)return null;
//		if(obj instanceof Double)return (Double)obj;
//		dumpExceptionEntry(new typeex);
//		return null;
//	}
	
	protected boolean aliasBlock(String strAliasId, boolean bBlock) {
		for(AliasData alias : aAliasList){
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
			dumpInfoEntry("Available Commands ("+acmdList.size()+"):");
		}else{
			dumpInfoEntry("Help for '"+strFilter+"':");
		}
		
		Collections.sort(acmdList, CommandData.comparator());
		for(CommandData cmd:acmdList){
//			if(strFilter!=null && !cmd.getBaseCmd().toLowerCase().contains(strFilter.toLowerCase()))continue;
			if(strFilter!=null && !cmd.asHelp().toLowerCase().contains(strFilter.toLowerCase()))continue;
			dumpSubEntry(getCommandPrefix()+cmd.asHelp());
		}
	}
	
	public ArrayList<String> getBaseCommandsWithComment() {
		if(astrBaseCmdCmtCacheList.size()!=acmdList.size()){
			astrBaseCmdCmtCacheList.clear();
			for(CommandData cmd:acmdList){
				astrBaseCmdCmtCacheList.add(cmd.getBaseCmd()+" "+cmd.getComment());
				Collections.sort(astrBaseCmdCmtCacheList);
			}
		}
		return astrBaseCmdCmtCacheList;
	}
	
	public ArrayList<String> getBaseCommands(){
		if(astrBaseCmdCacheList.size()!=acmdList.size()){
			astrBaseCmdCacheList.clear();
			for(CommandData cmd:acmdList){
				astrBaseCmdCacheList.add(cmd.getBaseCmd());
				Collections.sort(astrBaseCmdCacheList);
			}
		}
		return astrBaseCmdCacheList;
	}
	
	protected ECmdReturnStatus stillExecutingCommand(){
//		ECmdReturnStatus e = 
		return executePreparedCommandRoot();
//		if(e.compareTo(ECmdReturnStatus.NotFound)==0)return false;
//		return true;
	}
	
	public boolean isFound(ECmdReturnStatus ecrs){
		switch(ecrs){
			case FoundAndExceptionHappened:
			case FoundAndFailedGracefully:
			case FoundAndWorked:
				return true;
		}
		return false;
	}
	
	/**
	 * Command format: "commandIdentifier any comments as you wish"
	 * @param strFullCmdLineOriginal if null will populate the array of valid commands
	 * @return false if command execution failed
	 */
	protected ECmdReturnStatus executeCommand(final String strFullCmdLineOriginal){
		assertConfigured();
		
		ECmdReturnStatus ecrs = ECmdReturnStatus.NotFound;
		
		strCmdLineOriginal = strFullCmdLineOriginal;
		
//		boolean bCommandFound = false;
		
		try{
			if(!isFound(ecrs))ecrs=cmdRawLineCheckEndOfStartupCmdQueue();
			
			if(!isFound(ecrs))ecrs=cmdRawLineCheckAlias();
			
			if(!isFound(ecrs)){
				/**
				 * we will have a prepared line after below
				 */
				strCmdLinePrepared = prepareCmdAndParams(strCmdLineOriginal);
			}
			
			if(!isFound(ecrs))ecrs=stillExecutingCommand();
			
		}catch(Exception e){
			dumpExceptionEntry(e);
			
			ecrs=ECmdReturnStatus.FoundAndExceptionHappened;
		}
		
//		catch(NumberFormatException e){
//			// keep this one as "warning", as user may simply fix the typed value
//			dumpWarnEntry("NumberFormatException: "+e.getMessage());
//			e.printStackTrace();
//			bCmdFoundAndApplied=false;
//		}
		
		/**
		 * clear prepared line to mark end of command execution attempt
		 */
		clearPreparedCommandLine();
		
		return ecrs;
	}
	
	protected void clearPreparedCommandLine() {
		strCmdLinePrepared = null;
		astrCmdAndParams.clear();
	}

	public void dumpEntry(String strLineOriginal){
		dumpEntry(true, true, false, strLineOriginal);
	}
	
	public void dumpEntry(boolean bApplyNewLineRequests, boolean bDump, boolean bUseSlowQueue, String strLineOriginal){
		DumpEntryData de = new DumpEntryData()
			.setApplyNewLineRequests(bApplyNewLineRequests)
			.setDumpToConsole(bDump)
			.setUseSlowQueue(bUseSlowQueue)
			.setLineOriginal(strLineOriginal);
		
		dumpEntry(de);
	}
	
	public void update(float tpf) {
		if(!bConfigured)throw new NullPointerException("not configured yet");
		if(!bInitialized)throw new NullPointerException("not initialized yet");
		
		this.fTPF = tpf;
		if(tdLetCpuRest.isActive() && !tdLetCpuRest.isReady(true))return;
		
		if(!icui.isInitializationCompleted())return;
		
		updateNewDay();
		updateToggles();
		updateExecPreQueuedCmdsBlockDispatcher(); //before exec queue 
		updateExecConsoleCmdQueue(); // after pre queue
		updateDumpQueueEntry();
		updateHandleException();
	}
	
	protected void updateNewDay() {
		String str = MiscI.i().getSimpleDate();
		if(!str.equalsIgnoreCase(strCurrentDay) || DebugI.i().isKeyEnabled(EDbgKey.NewDayInfo)){
			strCurrentDay=str;
			dumpInfoEntry("Welcome to a new day "+strCurrentDay+"!");
			DebugI.i().disableKey(EDbgKey.NewDayInfo);
		}
	}

	protected void updateToggles() {
		if(btgEngineStatsView.checkChangedAndUpdate())icui.updateEngineStats();
		if(btgEngineStatsFps.checkChangedAndUpdate())icui.updateEngineStats();
//		if(btgFpsLimit.checkChangedAndUpdate())fpslState.setEnabled(btgFpsLimit.b());
		if(btgConsoleCpuRest.checkChangedAndUpdate())tdLetCpuRest.setActive(btgConsoleCpuRest.b());
//		if(btgPreQueue.checkChangedAndUpdate())bUsePreQueue=btgPreQueue.b();
	}

	protected void addCmdListOneByOneToQueue(ArrayList<String> astrCmdList, boolean bPrepend, boolean bShowExecIndex){
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
	
	protected void addCmdsBlockToPreQueue(ArrayList<String> astrCmdList, boolean bPrepend, boolean bShowExecIndex, String strBlockInfo){
		PreQueueCmdsBlockSubListData pqe = new PreQueueCmdsBlockSubListData();
		pqe.strBlockInfo=strBlockInfo;
		pqe.bPrepend=bPrepend;
		pqe.astrCmdList = new ArrayList<String>(astrCmdList);
		for(int i=0;i<pqe.astrCmdList.size();i++){
			pqe.astrCmdList.set(
				i, 
				pqe.astrCmdList.get(i)
					+commentToAppend(pqe.getUniqueInfo()));
		}
		if(bShowExecIndex){
			for(int i=0;i<pqe.astrCmdList.size();i++){
				pqe.astrCmdList.set(i, pqe.astrCmdList.get(i)+commentToAppend("ExecIndex="+i)
				);
			}
		}
		
		astrExecConsoleCmdsPreQueue.add(pqe);
		dumpDevInfoEntry("AddedCommandBlock"+commentToAppend(pqe.getUniqueInfo()));
	}
	
	protected boolean doesCmdQueueStillHasUId(String strUId){
		for(String strCmd:astrExecConsoleCmdsQueue){
			if(strCmd.contains(strUId))return true;
		}
		return false;
	}
	
	protected void updateExecPreQueuedCmdsBlockDispatcher(){
		for(PreQueueCmdsBlockSubListData pqe:astrExecConsoleCmdsPreQueue.toArray(new PreQueueCmdsBlockSubListData[0])){
			if(pqe.tdSleep!=null){
				if(pqe.tdSleep.isReady()){
					if(doesCmdQueueStillHasUId(commentToAppend(pqe.getUniqueInfo()))){
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
							+MiscI.i().fmtFloat(pqe.tdSleep.getDelayLimitSeconds())+"s: "
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
						pqe.tdSleep = new TimedDelayVarField(fDelay,null);
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
					astrCmdListFast.add(CMD_CONSOLE_SCROLL_BOTTOM.toString());
					addCmdListOneByOneToQueue(astrCmdListFast, pqe.bPrepend, false);
				}
				
			}
		}
	}
	public void addCmdToQueue(StringCmdField sfFullCmdLine){
		addCmdToQueue(sfFullCmdLine.toString());
	}
	public void addCmdToQueue(String strFullCmdLine){
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
	
	protected void updateExecConsoleCmdQueue() {
		if(astrExecConsoleCmdsQueue.size()>0){ // one per time! NO while here!!!!
			String str=astrExecConsoleCmdsQueue.remove(0);
			if(!str.trim().endsWith(""+getCommentPrefix())){
				if(btgShowExecQueuedInfo.get()){ // prevent messing user init cfg console log
					dumpInfoEntry("QueueExec: "+str);
				}
			}
			
			ECmdReturnStatus ecrs = executeCommand(str);
			if(ecrs.compareTo(ECmdReturnStatus.FoundAndWorked)!=0){
					dumpWarnEntry("QueueExecFail("+ecrs+"): "+str);
			}
		}
	}
	
	protected void showHelpForFailedCommand(String strFullCmdLine){
		if(validateBaseCommand(strFullCmdLine)){
//			addToExecConsoleCommandQueue(CMD_HELP+" "+extractCommandPart(strFullCmdLine,0));
			cmdShowHelp(extractCommandPart(strFullCmdLine,0));
		}else{
			dumpWarnEntry("Invalid command: "+strFullCmdLine);
		}
	}
	protected void cmdHistLoad() {
		astrCmdHistory.addAll(MiscI.i().fileLoad(flCmdHist));
	}
	
	protected void dumpSave(DumpEntryData de) {
//		if(de.isSavedToLogFile())return;
		MiscI.i().fileAppendLine(flLastDump,de.getLineOriginal());
	}
	/**
	 * These variables can be loaded from the setup file!
	 */
	enum ERestrictedSetupLoadableVars{
		userVariableListHashcode,
		userAliasListHashcode,
	}
	
	protected void setupRecreateFile(){
		dumpDevInfoEntry("Recreating restricted vars setup file:");
		
		flSetup.delete();
		
		/**
		 * comments for user
		 */
		MiscI.i().fileAppendLine(flSetup, getCommentPrefix()+" DO NOT EDIT!");
		MiscI.i().fileAppendLine(flSetup, getCommentPrefix()
			+" This file will be overwritten by the application!");
		MiscI.i().fileAppendLine(flSetup, getCommentPrefix()
			+" To set overrides use the user init config file.");
		MiscI.i().fileAppendLine(flSetup, getCommentPrefix()
			+" For command's values, the commands usage are required, the variable is just an info about their setup value.");
		MiscI.i().fileAppendLine(flSetup, getCommentPrefix()
			+" Some values will be read tho to provide restricted functionalities not accessible to users.");
		
		setupVars(true);
	}
	
	protected void setupVars(boolean bSave){
		varSet(""+RESTRICTED_TOKEN+ERestrictedSetupLoadableVars.userVariableListHashcode,
			""+tmUserVariables.hashCode(),
			false);
		
		varSet(""+RESTRICTED_TOKEN+ERestrictedSetupLoadableVars.userAliasListHashcode,
			""+aAliasList.hashCode(),
			false);
		
		for(BoolTogglerCmdField btg:BoolTogglerCmdField.getListCopy()){
			varSet(btg,false);
		}
		
		for(TimedDelayVarField td:TimedDelayVarField.getListCopy()){
			varSet(td,false);
		}
		
		for(FloatDoubleVarField fdv:FloatDoubleVarField.getListCopy()){
			varSet(fdv,false);
		}
		
		for(IntLongVarField ilv:IntLongVarField.getListCopy()){
			varSet(ilv,false);
		}
		
		for(StringVarField sv:StringVarField.getListCopy()){
			varSet(sv,false);
		}
		
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
	
	public boolean isCommandString(String str) {
		return str.trim().startsWith(getCommandPrefixStr());
	}
	
	protected void dumpAllStats(){
		icui.dumpAllStats();
		
		dumpSubEntry("Database User Variables Count = "+getVariablesIdentifiers(false).size());
		dumpSubEntry("Database User Aliases Count = "+aAliasList.size());
		
//		dumpSubEntry("Previous Second FPS  = "+lPreviousSecondFPS);
		
		for(BoolTogglerCmdField btg : BoolTogglerCmdField.getListCopy()){
			dumpSubEntry(btg.getReport());
		}
		
//		for(TimedDelayVar td : TimedDelayVar.getListCopy()){
//			dumpSubEntry(td.getReport());
//		}
		
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
		return getBaseCommands().contains(strCmdFullChk);
	}
	
	protected static enum EStats{
		CommandsHistory,
		ConsoleSliderControl,
		CopyFromTo,
		FunctionCreation(true),
		IfConditionalBlock(true),
		MouseCursorPosition,
		TimePerFrame,
		;
		
		private boolean b;
		public boolean b(){return b;};
		
		EStats(){}
		EStats(boolean b){this.b=b;}
	}
	
	/**
	 * this must be here because a console not using JME can show this info too.
	 * @return
	 */
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
						
		return strStatsLast;
	}
	
	protected void cmdHistSave(String strCmd) {
		MiscI.i().fileAppendLine(flCmdHist,strCmd);
	}
	
	/**
	 * configure must happen before initialization
	 * @param icui
	 * @param sapp
	 */
	public void configure(IConsoleUI icui){//, SimpleApplication sapp){
		if(bConfigured)throw new NullPointerException("already configured.");		// KEEP ON TOP
		
//		Init.i().initialize(sapp, this);
		TimedDelayVarField.configure(this);
		BoolTogglerCmdField.configure(this);
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
		
		DebugI.i().configure(this);
		MiscI.i().configure(this);
		ReflexHacks.i().configure(this, this);
//		SingleInstanceState.i().initialize(sapp, this);
		
//		CommandsBackgroundState.i().configure(sapp, icui, this);
		
		if(icui==null)throw new NullPointerException("invalid "+IConsoleUI.class.getName()+" instance");
		this.icui=icui;
//		this.sapp=sapp;
		
		bConfigured=true;
	}
	
	public void initialize(){
		if(!bConfigured)throw new NullPointerException("not configured yet");
		
		tdDumpQueuedSlowEntry.updateTime();
		
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
			addCmdListOneByOneToQueue(MiscI.i().fileLoad(flSetup), false, false);
		}
		
		// before user init file
		addCmdToQueue(btgShowExecQueuedInfo.getCmdIdAsCommand(false));
		addCmdToQueue(btgShowDebugEntries.getCmdIdAsCommand(false));
		addCmdToQueue(btgShowDeveloperWarn.getCmdIdAsCommand(false));
		addCmdToQueue(btgShowDeveloperInfo.getCmdIdAsCommand(false));
		
		// init DB
		flDB = new File(fileNamePrepareCfg(strFileDatabase,false));
		addCmdToQueue(CMD_DB+" "+EDataBaseOperations.load);
		addCmdToQueue(CMD_DB+" "+EDataBaseOperations.save
			+" "+commentToAppend("to shrink it"));
		
		// init user cfg
		flInit = new File(fileNamePrepareCfg(strFileInitConsCmds,false));
		if(flInit.exists()){
			addCmdListOneByOneToQueue(MiscI.i().fileLoad(flInit), false, false);
		}else{
			MiscI.i().fileAppendLine(flInit, getCommentPrefix()+" User console commands here will be executed at startup.");
		}
		
		// for debug mode, auto show messages
		if(DebugI.i().isInIDEdebugMode())addCmdToQueue(CMD_MESSAGE_REVIEW);
		
		// init valid cmd list
		bFillCommandList=true;
		executePreparedCommandRoot();
		bFillCommandList=false;
//		executeCommand(null); //to populate the array with available commands
		
		bInitialized=true;
	}
	
	enum ETest{
		fps,
		allchars,
		stats,
		exception
	}
	protected void cmdTest(){
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
				strDebugTest=paramString(2);
//				dumpDevInfoEntry("lblTxtSize="+csaTmp.lblStats.getText().length());
				break;
			case exception:
				test2();
				test3();
				break;
		}
		
	}
	protected void test3(){
		test2();
	}
	protected void test2(){
		dumpExceptionEntry(new NullPointerException("testEx"));
		dumpWarnEntry("testWarn");
	}

	public void showClipboard(){
		showClipboard(true);
	}
	public void showClipboard(boolean bShowNL){
		String strClipboard=MiscI.i().retrieveClipboardString();
	//	dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+" ( each line enclosed with \\"+strLineEncloseChar+" ):");
		dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+":");
		String[] astrLines = strClipboard.split("\n");
	//	for(String str:astr)dumpEntry(strLineEncloseChar+str+strLineEncloseChar);
	//	dumpEntry(""); // this empty line for clipboard content display is i
		String strFill="";for(int i=0;i<20;i++)strFill+=strCopyRangeIndicator;
		dumpEntry(strFill+" Clipboard BEGIN>>>");
		for(int i=0;i<astrLines.length;i++){
			String strLine=astrLines[i];
			if(bShowNL && i<(astrLines.length-1))strLine+="\\n";
			dumpEntry(false,true,false,strLine);
		}
		dumpEntry("<<<Clipboard END "+strFill);
		if(bAddEmptyLineAfterCommand)dumpEntry("");
	//	dumpEntry("");
		icui.scrollToBottomRequest();
	}

	protected String fileNamePrepare(String strFileBaseName, String strFileType, boolean bAddDateTime){
		return strFileBaseName
				+(bAddDateTime?"-"+MiscI.i().getDateTimeForFilename():"")
				+"."+strFileType;
	}
	protected String fileNamePrepareCfg(String strFileBaseName, boolean bAddDateTime){
		return fileNamePrepare(strFileBaseName, strFileTypeConfig, bAddDateTime);
	}
	protected String fileNamePrepareLog(String strFileBaseName, boolean bAddDateTime){
		return fileNamePrepare(strFileBaseName, strFileTypeLog, bAddDateTime);
	}

	protected ECmdReturnStatus cmdRawLineCheckEndOfStartupCmdQueue() {
		if(RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE.equals(strCmdLineOriginal)){
			bStartupCmdQueueDone=true;
			return ECmdReturnStatus.FoundAndWorked;
		}
		
		return ECmdReturnStatus.NotFound;
	}

	public void toggleLineCommentOrCommand() {
		String str = icui.getInputText();
		if(str.startsWith(""+getCommentPrefix())){
			str=str.substring(1);
		}else{
			str=getCommentPrefix()+str;
		}
		icui.setInputFieldText(str);
	}

	@Override
	synchronized public void handleExceptionThreaded(Exception e) {
		aExceptionList.add(e);
	}
	
	private void updateHandleException(){
		while(aExceptionList.size()>0){
			dumpExceptionEntry(aExceptionList.remove(0));
		}
	}
	
	public String convertNewLineToCmdDelimiter(String str){
		return str
			.replace(getCommandDelimiterStr()+"\n", getCommandDelimiterStr())
			.replace("\n", getCommandDelimiterStr());
	}
	
	public void updateCopyFrom(int iSelected, boolean bMultiLineMode){
		if(iSelected>=0){
			if(bMultiLineMode){
				if(iCopyTo==-1){
					iCopyFrom = iSelected;
					iCopyTo = iSelected;
				}else{
					updateCopyRangeCharIndicator(iCopyFrom,iCopyTo,false);
					iCopyFrom = iCopyTo;
					iCopyTo = iSelected;
					updateCopyRangeCharIndicator(iCopyFrom,iCopyTo,true);
				}
			}else{
				updateCopyRangeCharIndicator(iCopyFrom,iCopyTo,false);
				iCopyFrom = iSelected;
				iCopyTo = iSelected;
			}
		}
	}
	
	/**
	 * 
	 * @param iCpFrom
	 * @param iCpTo
	 * @param bApply if false will clear
	 */
	private void updateCopyRangeCharIndicator(int iCpFrom, int iCpTo, boolean bApply) {
	  if(iCpFrom>=0 && iCpTo>=0 && iCpFrom!=iCpTo){
	  	int iMin=Math.min(iCpFrom,iCpTo);
	  	int iMax=Math.max(iCpFrom,iCpTo);
	  	for(int i=iMin;i<=iMax;i++){
	  		if(bApply){
		  		icui.getDumpEntries().set(i,strCopyRangeIndicator+icui.getDumpEntries().get(i));
	  		}else{
		  		icui.getDumpEntries().set(i,icui.getDumpEntries().get(i)
		  			.replaceFirst("^["+strCopyRangeIndicator+"]",""));
	  		}
	  	}
	  }
	}

	public String editCopyOrCut(boolean bJustCollectText, boolean bCut, boolean bUseCommandDelimiterInsteadOfNewLine) {
	//	Integer iCopyTo = getDumpAreaSelectedIndex();
		String strTextToCopy = null;
		
		int iCopyToWork = iCopyTo;
		int iCopyFromWork = iCopyFrom;
		if(!bJustCollectText){
			updateCopyRangeCharIndicator(iCopyFrom, iCopyTo, false);
		}
		
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
				MiscI.i().putStringToClipboard(strTextToCopy);
				
				icui.clearDumpAreaSelection();
//				lstbxDumpArea.getSelectionModel().setSelection(-1); //clear selection
			}
		}else{
			/**
			 * nothing selected at dump area,
			 * use text input field as source
			 */
			String str = icui.getInputText();
			if(!str.trim().equals(""+chCommandPrefix))MiscI.i().putStringToClipboard(str);
		}
		
		if(!bJustCollectText){
			iCopyFrom=-1;
			iCopyTo=-1;
		}
		
		return strTextToCopy;
	}
	
	public void setCmdHistoryCurrentIndex(int i){
		this.iCmdHistoryCurrentIndex=i;
	}
	public void addCmdHistoryCurrentIndex(int i){
		this.iCmdHistoryCurrentIndex+=i;
	}
	public int getCmdHistoryCurrentIndex(){
		return iCmdHistoryCurrentIndex;
	}
	public void resetCmdHistoryCursor(){
		iCmdHistoryCurrentIndex = astrCmdHistory.size();
	}
	
	/**
	 * This method is only to be called when user types command in the console.
	 * 
	 * @param strCmd
	 * @return
	 */
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
			while(astrCmdHistory.size()>ilvMaxCmdHistSize.getLong()){
				astrCmdHistory.remove(0);
			}
		}
		
		resetCmdHistoryCursor();
		
		if(strType.equals(strTypeCmd)){
			strLastTypedUserCommand = strCmd;
			
			ECmdReturnStatus ecrs = executeCommand(strCmd);
			switch(ecrs){
				case FoundAndExceptionHappened:
				case FoundAndFailedGracefully:
					dumpWarnEntry(strType+": FAIL("+ecrs+"): "+strCmd);
					showHelpForFailedCommand(strCmd);
					break;
				case NotFound:
				case Skip:
					dumpWarnEntry(strType+": ("+ecrs+"): "+strCmd);
					break;
			}
			
			if(bAddEmptyLineAfterCommand ){
				dumpEntry("");
			}
		}
		
		icui.scrollToBottomRequest();
		
		return bIsCmd;
	}

	public int getCmdHistorySize() {
		return astrCmdHistory.size();
	}
	
	public String getCmdHistoryAtIndex(int i){
		if(getCmdHistorySize()==0)return null;
		if(i<0)return null;
		if(i>=getCmdHistorySize())return null;
		
		return astrCmdHistory.get(i);
	}

	public String getDevInfoEntryPrefix() {
		return strDevInfoEntryPrefix;
	}
	
	public int getCurrentFixedLineWrapAtColumn(){
		return ilvCurrentFixedLineWrapAtColumn.intValue();
	}
//	protected void setConsoleUI(IConsoleUI icui) {
//		this.icui=icui;
//	}

	public void repeatLastUserTypedCommand() {
		dumpInfoEntry("Repeating: "+strLastTypedUserCommand);
		if(strLastTypedUserCommand==null)return;
		addCmdToQueue(strLastTypedUserCommand, true);
	}

	public ECmdReturnStatus cmdFoundReturnStatus(boolean bCommandWorked) {
		return bCommandWorked?ECmdReturnStatus.FoundAndWorked:ECmdReturnStatus.FoundAndFailedGracefully;
	}

	public boolean isConfigured() {
		if(!bConfigured)MsgI.i().msgDbg("is cfg", false, this);
		return bConfigured;
	}

	public boolean isInitialized() {
		if(!bInitialized)MsgI.i().msgDbg("is ini", false, this);
		return bInitialized;
	}
}