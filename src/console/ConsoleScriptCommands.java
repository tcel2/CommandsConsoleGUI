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

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import misc.Misc;
import misc.StringField;

import com.google.common.collect.Lists;

import console.ConsoleCommands.EStats;

/**
 *	This class holds all commands that allows users to create
 *	scripts that will run in the console.
 *	
 *	For single player games seems ok.
 *	For multiplayer, me be interesting to disable it, just use {@link #ConsoleCommands()}
 *	or restrict what commands will be allowed inside the scripting ones.
 *
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleScriptCommands extends ConsoleCommands{
	public final StringField CMD_FUNCTION = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FUNCTION_CALL = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FUNCTION_END = new StringField(this,strFinalCmdCodePrefix);
	/**
	 * conditional user coding
	 */
	public final StringField CMD_IF = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_ELSE_IF = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_ELSE = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_IF_END = new StringField(this,strFinalCmdCodePrefix);

	public String	strPrepareFunctionBlockForId;
	public boolean	bFuncCmdLineRunning;
	public boolean	bFuncCmdLineSkipTilEnd;
	public TreeMap<String,ArrayList<String>> tmFunctions = 
		new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER);
	
//	public ConsoleScriptCommands(IConsoleUI icg) {
//		super(icg);
//	}
	public boolean checkFuncExecEnd() {
		if(strCmdLineOriginal==null)return false;
		return strCmdLineOriginal.startsWith(RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS.toString());
	}
	public boolean checkFuncExecStart() {
		if(strCmdLineOriginal==null)return false;
		return strCmdLineOriginal.startsWith(RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS.toString());
	}
	public boolean cmdFunctionCall() {
		String strFunctionId = paramString(1);
		
		/**
		 * put cmds block at queue
		 */
		ArrayList<String> astrFuncBlock = tmFunctions.get(strFunctionId);
		if(astrFuncBlock==null)return false;
		
		astrFuncBlock.removeAll(Collections.singleton(null));
		if(astrFuncBlock!=null && astrFuncBlock.size()>0){
			dumpInfoEntry("Running function: "+strFunctionId+" "+getCommentPrefix()+"totLines="+astrFuncBlock.size());
			
			/**
			 * params var ids
			 */
			ArrayList<String> astrFuncParams = new ArrayList<String>();
			int i=2;
			while(true){
				String strParamValue = paramString(i);
				if(strParamValue==null)break;
				String strParamId=strFunctionId+"_"+(i-1);
				astrFuncParams.add(strParamId);
				if(hasVar(strParamId)){
					dumpWarnEntry("RmConflictingVar: "+varReportPrepare(strParamId));
					varDelete(strParamId);
				}
				varSet(strParamId, strParamValue, false);
				i++;
			}
			
			ArrayList<String> astrFuncBlockToExec = new ArrayList<String>(astrFuncBlock);
			/**
			 * prepend section (is inverted order)
			 */
//			for(String strUnsetVar:astrFuncParams){
//				if(hasVar(strUnsetVar)){
//					/**
//					 * Tries to inform user about the inconsistency.
//					 */
//					dumpWarnEntry("Conflicting func param var will be removed: "+strUnsetVar);
//				}
//				astrFuncBlockToExec.add(0,getCommandPrefix()+
//						CMD_VAR_SET.toString()+" "+getVarDeleteToken()+strUnsetVar);
//			}
			astrFuncBlockToExec.add(0,RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS+" "+strFunctionId);
			
			/**
			 * append section
			 */
			for(String strUnsetVar:astrFuncParams){
				astrFuncBlockToExec.add(getCommandPrefix()+
					CMD_VAR_SET.toString()+" "+getVarDeleteToken()+strUnsetVar);
			}
			astrFuncBlockToExec.add(RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS+" "+strFunctionId);
			addCmdsBlockToPreQueue(astrFuncBlockToExec, true, true, "Func:"+strFunctionId);
		}
		
		return true;
	}
	public boolean cmdFunctionBegin() {
		String strFunctionId = paramString(1);
		
		if(!isValidIdentifierCmdVarAliasFuncString(strFunctionId))return false;
		
		tmFunctions.put(strFunctionId, new ArrayList<String>());
		dumpInfoEntry("Function creation begins for: "+strFunctionId);
		
		strPrepareFunctionBlockForId=strFunctionId;
		
		return true;
	}
	public boolean functionFeed(String strCmdLine){
		ArrayList<String> astr = tmFunctions.get(strPrepareFunctionBlockForId);
		astr.add(strCmdLine);
		dumpDevInfoEntry("Function line added: "+strCmdLine+" "+getCommentPrefix()+"tot="+astr.size());
		return true;
	}
	public boolean functionEndCheck(String strCmdLine) {
		if(CMD_FUNCTION_END.equals(extractCommandPart(strCmdLine,0))){
			return cmdFunctionEnd();
		}
//		String strCmdCheck=""+getCommandPrefix()+CMD_FUNCTION_END.toString();
//		strCmdCheck=strCmdCheck.toLowerCase();
//		if(strCmdCheck.equals(strCmdLine.trim().toLowerCase())){
//			return cmdFunctionEnd();
//		}
		return false;
	}
	public boolean cmdFunctionEnd() {
		if(strPrepareFunctionBlockForId==null){
			dumpExceptionEntry(new NullPointerException("no function being prepared..."));
			return false;
		}
		
		dumpInfoEntry("Function creation ends for: "+strPrepareFunctionBlockForId);
		strPrepareFunctionBlockForId=null;
		
		return true;
	}
	
	@Override
	public boolean executePreparedCommandRoot() {
		boolean bCommandWorked = super.executePreparedCommandRoot();
		
		if(!bCommandWorked){
			if(checkCmdValidity(CMD_FUNCTION,"<id> begins a function block")){
				bCommandWorked=cmdFunctionBegin();
			}else
			if(checkCmdValidity(CMD_FUNCTION_CALL,"<id> [parameters...] retrieve parameters values with ex.: ${id_1} ${id_2} ...")){
				bCommandWorked=cmdFunctionCall();
			}else
			if(checkCmdValidity(CMD_FUNCTION_END,"ends a function block")){
				bCommandWorked=cmdFunctionEnd();
			}else
			if(checkCmdValidity("functionList","[filter]")){
				String strFilter = paramString(1);
				ArrayList<String> astr = Lists.newArrayList(tmFunctions.keySet().iterator());
				for(String str:astr){
					if(strFilter!=null && !str.toLowerCase().contains(strFilter.toLowerCase()))continue;
					dumpSubEntry(str);
				}
				bCommandWorked=true;
			}else
			if(checkCmdValidity("functionShow","<functionId>")){
				String strFuncId = paramString(1);
				if(strFuncId!=null){
					ArrayList<String> astr = tmFunctions.get(strFuncId);
					if(astr!=null){
						dumpSubEntry(getCommandPrefixStr()+CMD_FUNCTION+" "+strFuncId+getCommandDelimiter());
						for(String str:astr){
							str=strSubEntryPrefix+strSubEntryPrefix+str+getCommandDelimiter();
//								dumpSubEntry("\t"+str+getCommandDelimiter());
							dumpEntry(false, true, false, str);
						}
						dumpSubEntry(getCommandPrefixStr()+CMD_FUNCTION_END+getCommandDelimiter());
						bCommandWorked=true;
					}
				}
			}else
			if(checkCmdValidity(CMD_ELSE,"conditinal block")){
				bCommandWorked=cmdElse();
			}else
			if(checkCmdValidity(CMD_ELSE_IF,"<[!]<true|false>> conditional block")){
				bCommandWorked=cmdElseIf();
			}else
			if(checkCmdValidity(CMD_IF,"<[!]<true|false>> [cmd|alias] if cmd|alias is not present, this will be a multiline block start!")){
				bCommandWorked=cmdIf();
			}else
			if(checkCmdValidity(CMD_IF_END,"ends conditional block")){
				bCommandWorked=cmdIfEnd();
			}else
			{}
		}
		
		return bCommandWorked;
	}
	
	@Override
	public boolean stillExecutingCommand() {
		boolean bCmdWorkDone=false;
		
		if(!bCmdWorkDone){
			if(bFuncCmdLineRunning){
				if(checkFuncExecEnd()){
					bFuncCmdLineRunning=false;
					bFuncCmdLineSkipTilEnd=false;
					bCmdWorkDone=true;
				}
			}
		}
		
		if(!bCmdWorkDone){
			if(checkFuncExecStart()){
				bFuncCmdLineRunning=true;
				bCmdWorkDone=true;
			}else
			if(strPrepareFunctionBlockForId!=null){
				if(!bCmdWorkDone)bCmdWorkDone = functionEndCheck(strCmdLineOriginal); //before feed
				if(!bCmdWorkDone)bCmdWorkDone = functionFeed(strCmdLineOriginal);
			}else
			if(bIfConditionExecCommands!=null && !bIfConditionExecCommands){
				/**
				 * These are capable of stopping the skipping.
				 */
				if(CMD_ELSE_IF.equals(paramString(0))){
					if(!bCmdWorkDone)bCmdWorkDone = cmdElseIf();
				}else
				if(CMD_ELSE.equals(paramString(0))){
					if(!bCmdWorkDone)bCmdWorkDone = cmdElse();
				}else
				if(CMD_IF_END.equals(paramString(0))){
					if(!bCmdWorkDone)bCmdWorkDone = cmdIfEnd();
				}else{
					/**
					 * The if condition resulted in false, therefore commands must be skipped.
					 */
					dumpInfoEntry("ConditionalSkip: "+strCmdLinePrepared);
					if(!bCmdWorkDone)bCmdWorkDone = true;
				}
			}
		}
		
		if(!bCmdWorkDone){
			if(bFuncCmdLineRunning && bFuncCmdLineSkipTilEnd){
				dumpWarnEntry("SkippingRemainingFunctionCmds: "+strCmdLinePrepared);
				bCmdWorkDone = true; //this just means that the skip worked
			}
		}
		
		if(!bCmdWorkDone){
			/**
			 * normal commands execution
			 */
			bCmdWorkDone = super.stillExecutingCommand();
//			bCmdWorkDone = executePreparedCommand();
		}
		
		if(!bCmdWorkDone){
			if(bFuncCmdLineRunning){
				// a command may fail inside a function, only that first one will generate error message 
				bFuncCmdLineSkipTilEnd=true;
			}
		}
		
		return bCmdWorkDone;
	}
	
	@Override
	public String prepareStatsFieldText() {
		String strStatsLast = super.prepareStatsFieldText();
		
		if(EStats.FunctionCreation.b && strPrepareFunctionBlockForId!=null){
			strStatsLast+=
					"F="+strPrepareFunctionBlockForId
						+";";
		}
		
		return strStatsLast;
	}

	public boolean cmdIf() {
		return cmdIf(false);
	}
	public boolean cmdIf(boolean bSkipNesting) {
		bIfConditionIsValid=false;
		
		String strCondition = paramString(1);
		
		boolean bNegate = false;
		if(strCondition.startsWith("!")){
			strCondition=strCondition.substring(1);
			bNegate=true;
		}
		
		Boolean bCondition = null;
		try{bCondition = Misc.i().parseBoolean(strCondition);}catch(NumberFormatException e){};//accepted exception
		
		if(bNegate)bCondition=!bCondition;
		
		if(bCondition==null){
			dumpWarnEntry("Invalid condition: "+strCondition);
			return false;
		}
		
		String strCmds = paramStringConcatenateAllFrom(2);
		if(strCmds==null)strCmds="";
		strCmds.trim();
		if(strCmds.isEmpty() || strCmds.startsWith(getCommentPrefixStr())){
			if(bSkipNesting){
				ConditionalNested cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
				cn.bCondition = bCondition;
			}else{
				aIfConditionNestedList.add(new ConditionalNested(bCondition));
			}
			
			bIfConditionExecCommands=bCondition;
		}else{
			if(!bSkipNesting){
				if(bCondition){
					addCmdToQueue(strCmds,true);
				}
			}
		}
		
		return true;
	}
	
	public boolean cmdElse(){
//		bIfConditionExecCommands=!aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		ConditionalNested cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		bIfConditionExecCommands = !cn.bCondition;
		cn.bIfEndIsRequired = true;
		
		return true;
	}
	
	public boolean cmdElseIf(){
		ConditionalNested cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		if(cn.bIfEndIsRequired){
			dumpExceptionEntry(new NullPointerException("command "+CMD_ELSE_IF.toString()
				+" is missplaced, ignoring"));
			bIfConditionExecCommands=false; //will also skip this block commands
			return false;
		}
		
		boolean bConditionSuccessAlready = cn.bCondition;
		
		if(bConditionSuccessAlready){
			/**
			 * if one of the conditions was successful, will skip all the remaining ones
			 */
			bIfConditionExecCommands=false;
		}else{
			return cmdIf(true);
		}
		
		return true;
	}
	
	public boolean cmdIfEnd(){
		if(aIfConditionNestedList.size()>0){
			aIfConditionNestedList.remove(aIfConditionNestedList.size()-1);
			
			if(aIfConditionNestedList.size()==0){
				bIfConditionExecCommands=null;
//				bIfEndIsRequired = false;
			}else{
				ConditionalNested cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
				bIfConditionExecCommands = cn.bCondition;
			}
		}else{
			dumpExceptionEntry(new NullPointerException("pointless condition ending..."));
			return false;
		}
		
		return true;
	}
}