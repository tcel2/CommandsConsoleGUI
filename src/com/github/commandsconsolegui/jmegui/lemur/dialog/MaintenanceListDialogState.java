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

package com.github.commandsconsolegui.jmegui.lemur.dialog;

import java.util.ArrayList;

import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * This is like the inventory list.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class MaintenanceListDialogState<T extends Command<Button>> extends LemurBasicDialogStateAbs<T,MaintenanceListDialogState<T>> {
	public static class CfgParm<T> extends LemurBasicDialogStateAbs.CfgParm{
		private LemurDialogGUIStateAbs<T,?>	diagChoice;
		private LemurDialogGUIStateAbs<T,?>	diagQuestion;

		public CfgParm(
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Float fEntryHeightMultiplier,
				LemurDialogGUIStateAbs<T,?> diagChoice,
				LemurDialogGUIStateAbs<T,?> diagQuestion) {
			super(fDialogWidthPercentOfAppWindow,
					fDialogHeightPercentOfAppWindow, fInfoHeightPercentOfDialog,
					fEntryHeightMultiplier);
			this.setDiagChoice(diagChoice);
			this.setDiagQuestion(diagQuestion);
//			super.setUIId(MaintenanceListDialogStateAbs.class.getSimpleName());
		}

		public LemurDialogGUIStateAbs<T,?> getDiagChoice() {
			return diagChoice;
		}

		public void setDiagChoice(LemurDialogGUIStateAbs<T,?> diagChoice) {
			this.diagChoice = diagChoice;
		}

		public LemurDialogGUIStateAbs<T,?> getDiagQuestion() {
			return diagQuestion;
		}

		public void setDiagQuestion(LemurDialogGUIStateAbs<T,?> diagQuestion) {
			this.diagQuestion = diagQuestion;
		}
	}
	private CfgParm<T>	cfg;
	@Override
	public MaintenanceListDialogState<T> configure(ICfgParm icfg) {
		cfg = (CfgParm<T>)icfg;
		
		if(cfg.getDiagChoice()!=null)addModalDialog(cfg.getDiagChoice());
		if(cfg.getDiagQuestion()!=null)addModalDialog(cfg.getDiagQuestion());
		
		super.configure(cfg);
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	@Override
	protected boolean prepareTestData(){
		addEntryQuick(null);
		addEntryQuick(null);
		
		DialogListEntryData<T> dleS1 = addEntryQuick("section 1");
		addEntryQuick(null).setParent(dleS1);
		addEntryQuick(null).setParent(dleS1);
		addEntryQuick(null).setParent(dleS1);
		
		DialogListEntryData<T> dleS2 = addEntryQuick("section 2");
		addEntryQuick(null).setParent(dleS2);
		addEntryQuick(null).setParent(dleS2);
		DialogListEntryData<T> dleS21 = addEntryQuick("section 2.1").setParent(dleS2);
		addEntryQuick(null).setParent(dleS21);
		addEntryQuick(null).setParent(dleS21);
		addEntryQuick(null).setParent(dleS21);
		
		addEntryQuick("S2 child").setParent(dleS2); //ok, will be placed properly
		
		addEntryQuick("S1 child").setParent(dleS1); //out of order for test
		addEntryQuick("S21 child").setParent(dleS21); //out of order for test
		
		return true;
	}

	@Override
	public void applyResultsFromModalDialog() {
		BaseDialogStateAbs<T,?> diagModal = getChildDiagModalInfoCurrent().getDiagModal();
		T cmdRequestedAtThisDiag = getChildDiagModalInfoCurrent().getCmdAtParent();
		ArrayList<DialogListEntryData<T>> adledToApplyResultsList = getChildDiagModalInfoCurrent().getParentReferencedDledListCopy();
		
		boolean bChangesMade = false;
		for(DialogListEntryData<T> dledAtModal:diagModal.getDataSelectionListCopy()){
				if(cmdRequestedAtThisDiag.equals(cmdDel)){
					if(diagModal instanceof QuestionDialogState){
						QuestionDialogState<T> qds = (QuestionDialogState<T>)diagModal;
						if(qds.isYes(dledAtModal)){
							bChangesMade = deleteEntry(adledToApplyResultsList);
							
							/**
							 * !!! ATTENTION !!!
							 * There is already a sound for entries removal
							if(bChangesMade)AudioUII.i().play(EAudio.ReturnChosen);
							 */
						}else
						if(qds.isNo(dledAtModal)){
//						if(dataAtModal.equals(qds.dataNo)){
							AudioUII.i().play(EAudio.ReturnNothing);
						}
					}else{
						throw new PrerequisitesNotMetException("unexpected diag", diagModal, QuestionDialogState.class);
					}
				}else
				if(cmdRequestedAtThisDiag.equals(cmdCfg)){
					bChangesMade = modifyEntry(diagModal, dledAtModal, adledToApplyResultsList);
					if(bChangesMade)AudioUII.i().play(EAudio.ReturnChosen);
				}
		}
		
		if(bChangesMade){
			requestRefreshList();
		}
		
		super.applyResultsFromModalDialog();
	}
	
	/**
	 * 
	 * @param diagModal mainly to give more options when overriding this method
	 * @param dledAtModal
	 * @param adledToApplyResultsList
	 * @return
	 */
	protected boolean modifyEntry(BaseDialogStateAbs<T,?> diagModal, DialogListEntryData<T> dledAtModal, ArrayList<DialogListEntryData<T>> adledToApplyResultsList) {
		boolean bChangesMade=false;
		for(DialogListEntryData<T> dledToCfg:adledToApplyResultsList){
			dledToCfg.updateTextTo(dledAtModal.getTextValue());
			bChangesMade=true;
		}
		return bChangesMade;
	}

	protected boolean deleteEntry(ArrayList<DialogListEntryData<T>> adledToApplyResultsList) {
		boolean bChangesMade = false;
		for(DialogListEntryData<T> dledToApplyResults:adledToApplyResultsList){
			removeEntry(dledToApplyResults);
			bChangesMade=true;
		}
		
		return bChangesMade;
	}

	@Override
	protected void actionCustomAtEntry(DialogListEntryData<T> dledSelected) {
		if(cfg.getDiagChoice()!=null){
			super.actionCustomAtEntry(dledSelected);
			openModalDialog(cfg.getDiagChoice().getId(), dledSelected, (T)cmdCfg);
		}else{
			AudioUII.i().playOnUserAction(AudioUII.EAudio.Failure);
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("no choice dialog configured for "+this, dledSelected);
		}
	}

	public class CommandDel implements Command<Button>{
		@SuppressWarnings("unchecked")
		@Override
		public void execute(Button btn) {
			DialogListEntryData<T> dled = getDledFrom(btn);
			
			if(dled.isParent()){
//				CustomDialogGUIState.this.setDataToApplyModalChoice(data);
				if(cfg.getDiagQuestion()!=null){
					MaintenanceListDialogState.this.openModalDialog(cfg.getDiagQuestion().getId(), dled, (T)this);
					AudioUII.i().play(EAudio.Question);
				}else{
					AudioUII.i().playOnUserAction(AudioUII.EAudio.Failure);
					GlobalCommandsDelegatorI.i().dumpDevWarnEntry("no question dialog configured for "+this, btn, dled);
				}
			}else{
				MaintenanceListDialogState.this.removeEntry(dled);
			}
		}
	}
	CommandDel cmdDel = new CommandDel();
	
	@Override
	public DialogListEntryData<T> addEntryQuick(String strText) {
		DialogListEntryData<T> dled = super.addEntryQuick(strText);
		/**
		 * this order matters
		 */
		dled.addCustomButtonAction("Cfg",(T)cmdCfg);
		dled.addCustomButtonAction("X",(T)cmdDel);
		
		return dled;
	}

	public class CommandCfg implements Command<Button>{
		@Override
		public void execute(Button btn) {
//			DialogTestState.this.openModalDialog(EDiag.Cfg.toString(), getDataFrom(btn), (T)this);
			actionCustomAtEntry(getDledFrom(btn));
//			DialogTestState.this.actionSubmit();
		}
	}
	CommandCfg cmdCfg = new CommandCfg();
	
	@Override
	protected MaintenanceListDialogState<T> getThis() {
		return this;
	}

	@Override
	protected String getDefaultValueToUserModify() {
		return "(no default value)";
	}
	
}
