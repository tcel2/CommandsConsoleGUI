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

package com.github.commandsconsolegui.jmegui.lemur.console;

import com.github.commandsconsolegui.cmd.varfield.NumberVarFieldAbs;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.dialog.ChoiceDialogState;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <T>
 */
public class ChoiceVarDialogState<T extends Command<Button>> extends ChoiceDialogState<T>{
	public static class CfgParm extends ChoiceDialogState.CfgParm{
		public CfgParm(Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Float fEntryHeightMultiplier) {
			super(fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
					fInfoHeightPercentOfDialog, fEntryHeightMultiplier);
		}
	}

	private VarCmdFieldAbs	vcf;
	private DialogListEntryData<T>	dledAtParent;
	
//	@Override
//	public boolean doItAllProperly(CompositeControl cc, float tpf) {
////		if(isTryingToEnable()){
////			int i=2;int i2=i;
////		}
//		return super.doItAllProperly(cc, tpf);
//	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected boolean enableAttempt() {
		if(!super.enableAttempt())return false;
		
		dledAtParent = getParentReferencedDledListCopy().get(0);
		Object objUser = dledAtParent.getUserObj();
		if(objUser instanceof VarCmdFieldAbs){
			vcf = (VarCmdFieldAbs)objUser;
		}else{
			throw new PrerequisitesNotMetException("user object is not "+VarCmdFieldAbs.class, objUser, dledAtParent);
		}
		
		btgSortListEntries.setObjectRawValue(false);
		
		return true;
	}
	
//	@Override
//	protected void enableSuccess() {
//		super.enableSuccess();
////		setInputText(getUserEnterCustomValueToken()+vcf.getValueAsString(10));
//		setInputText(getUserEnterCustomValueToken()+vcf.getValueRaw());
//	}
	
	@Override
	protected String getTextInfo() {
		String str="";
		
		str+="Help("+vcf.getClass().getSimpleName()+"):\n";
		str+="\t"+(vcf.getHelp()==null ? "(no help)" : vcf.getHelp())+"\n";
		
		str+="Help("+ConsoleVarsDialogStateI.class.getSimpleName()+"):\n";
		str+="\tList and manage all console variables for all class listeners.\n";
		
		str+=super.getTextInfo();
		
		return str;
	}
	
	@Override
	protected void actionSubmit() { //just to help on debug...
		super.actionSubmit();
	}
	
	private class CmdApplyValueAtInput implements Command<Button>{
		@Override
		public void execute(Button source) {
			ChoiceVarDialogState.this.setInputTextAsUserTypedValue(
				ChoiceVarDialogState.this.getDledFrom(source).getVisibleText());
		}
	}
	CmdApplyValueAtInput cavai = new CmdApplyValueAtInput();
	private DialogListEntryData<T>	dledRawValue;
	private boolean	bListIsFilled;
	private DialogListEntryData<T>	dledVals;
	private DialogListEntryData<T>	dledInfo;
	
	@Override
	protected void updateList() {
		if(!bListIsFilled){
//			clearList();
			
			DialogListEntryData<T> dledNew = null;
			
			if(dledInfo==null){
				dledInfo = new DialogListEntryData<T>(this);
				dledInfo.setText("Info:", vcf);
				dledInfo.setTreeExpanded(true);
			}
			addEntry(dledInfo);
			
			{
				dledNew = new DialogListEntryData<T>(this);
				dledNew.setText(vcf.getUniqueVarId(), vcf);
				dledNew.setParent(dledInfo);
				addEntry(dledNew).addCustomButtonAction("UniqueId",getCmdDummy());
				
				dledNew = new DialogListEntryData<T>(this);
				dledNew.setText(vcf.getSimpleId(), vcf);
				dledNew.setParent(dledInfo);
				addEntry(dledNew).addCustomButtonAction("SimpleId",getCmdDummy());
					
				dledNew = new DialogListEntryData<T>(this);
				dledNew.setText(vcf.getHelp(), vcf);
				dledNew.setParent(dledInfo);
				addEntry(dledNew).addCustomButtonAction("Help",getCmdDummy());
			}
			
			if(dledVals==null){
				dledVals = new DialogListEntryData<T>(this);
				dledVals.setText("Value:", vcf);
				dledVals.setTreeExpanded(true);
			}
			addEntry(dledVals);
			
			{
				if(vcf instanceof NumberVarFieldAbs){
					NumberVarFieldAbs v = (NumberVarFieldAbs)vcf;
					
					if(v.getMin()!=null){
						dledNew = new DialogListEntryData<T>(this);
						dledNew.setParent(dledVals);
						addEntry(dledNew.setText(v.getMin(), vcf)).addCustomButtonAction("MinValue->",(T)cavai);
					}
					
					if(v.getMax()!=null){
						dledNew = new DialogListEntryData<T>(this);
						dledNew.setParent(dledVals);
						addEntry(dledNew.setText(v.getMax(), vcf)).addCustomButtonAction("MaxValue->",(T)cavai);
					}
				}
				
				dledNew = new DialogListEntryData<T>(this);
				dledNew.setText(vcf.getRawValueDefault(), vcf);
				dledNew.setAddVisibleQuotes(vcf instanceof StringVarField);
				dledNew.setParent(dledVals);
				addEntry(dledNew).addCustomButtonAction("DefaultValueRaw->",(T)cavai);
				
				dledRawValue = new DialogListEntryData<T>(this);
				dledRawValue.setText(vcf.getRawValue(), vcf);
				dledRawValue.setAddVisibleQuotes(vcf instanceof StringVarField);
				dledRawValue.setParent(dledVals);
				addEntry(dledRawValue).addCustomButtonAction("ValueRaw->",(T)cavai);
				
				dledNew = new DialogListEntryData<T>(this);
				dledNew.setText(vcf.getValueAsString(3), vcf);
				dledNew.setAddVisibleQuotes(vcf instanceof StringVarField);
				dledNew.setParent(dledVals);
				addEntry(dledNew).addCustomButtonAction("Value->",(T)cavai);
			}
			
//			addEntry(
//					new DialogListEntryData<T>(this).setText(vcf.getValueAsString(3), vcf)
//						.setAddVisibleQuotes(vcf instanceof StringVarField)
//				).addCustomButtonAction("CustomTypedReturnValue",(T)cavai);
			
			bListIsFilled=true;
		}
		
		super.updateList();
	}
	
	@Override
	protected void clearList() {
		super.clearList();
		dledInfo.clearChildren();
		dledVals.clearChildren();
		bListIsFilled=false;
	}
	
	@Override
	protected boolean disableAttempt() {
		if(!super.disableAttempt())return false;
		vcf=null; 
		clearList(); //from a previous enable
		setInputText("");
		return true;
	}
	
//	@Override
//	protected void actionSubmit() {
//		super.actionSubmit();
//	}
	@Override
	protected String getDefaultValueToUserModify() {
//		return ""+vcf.getRawValue();
		return dledRawValue.getVisibleText();
	}
	
}