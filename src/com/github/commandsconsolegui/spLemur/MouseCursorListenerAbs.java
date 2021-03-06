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

package com.github.commandsconsolegui.spLemur;

import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spJme.ManageMouseCursorI;
import com.github.commandsconsolegui.spJme.ManageMouseCursorI.EMouseCursorButton;
import com.github.commandsconsolegui.spJme.MouseCursorButtonData;
import com.github.commandsconsolegui.spJme.MouseCursorButtonsControl;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI.EUserDataMiscJme;
import com.github.commandsconsolegui.spLemur.dialog.ManageLemurDialog.DummyEffect;
import com.github.commandsconsolegui.spLemur.extras.CellRendererDialogEntry.CellDialogEntry.EUserDataCellEntry;
import com.github.commandsconsolegui.spLemur.globals.GlobalManageDialogLemurI;
import com.github.commandsconsolegui.spLemur.misc.MiscLemurStateI;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.effect.AbstractEffect;
import com.simsilica.lemur.effect.Effect;
import com.simsilica.lemur.effect.EffectInfo;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorListener;
import com.simsilica.lemur.event.CursorMotionEvent;

/**
 * Click detection is based in time delay on this class.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public abstract class MouseCursorListenerAbs implements CursorListener {
//	private Button	btnLastHoverIn;
	
	private MouseCursorButtonsControl mcab;
	private boolean	bCancelNextMouseReleased;
//	private String	strPopupHelp;
	
	public MouseCursorListenerAbs() {
//		 mcab = MouseCursorCentralI.i().createButtonsInstance(this);
	}
	
	public MouseCursorButtonsControl mb(){
//		return mcab;
		return ManageMouseCursorI.i().getButtonsInstance();
	}
	
	@Override
	public void cursorButtonEvent(CursorButtonEvent eventButton, Spatial target, Spatial capture) {
//		DebugI.i().conditionalBreakpoint(this instanceof DialogMouseCursorListenerI);
		MouseCursorButtonData mcbd = mb().getMouseCursorDataFor(
			EMouseCursorButton.get(eventButton.getButtonIndex()));
		
		if(eventButton.isPressed()){
			mcbd.setPressed(eventButton, MiscLemurStateI.i().eventToV3f(eventButton));
			
    	if(clickBegin(mcbd, eventButton, target, capture)){
    		eventButton.setConsumed();
    	}
		}else{
//			if(mcbd.isPressed()){ // This check is NOT redundant. May happen just by calling: {@link MouseCursor#resetFixingAllButtons()}
				if(bCancelNextMouseReleased){
					mcbd.setReleasedAndGetDelay(eventButton);
					bCancelNextMouseReleased=false;
				}else{
					mcbd.setReleasedAndGetDelay(eventButton);
//					int iClickCount=mcbd.setReleasedAndRetrieveClickCount(eventButton, target, capture);
					
	//				if(MouseCursor.i().isClickDelay(getMouseCursor().getMouseCursorDataFor(emcb).setReleasedAndGetDelay())){
	//					MouseCursor.i().addClick(
	//	      		new MouseButtonClick(emcb, eventButton, target, capture));
	//					
	//					int iClickCount=MouseCursor.i().getMultiClickCountFor(emcb);
					
//					if(iClickCount>0){
						/**
						 * In this case, any displacement will be ignored.
						 * TODO could the minimal displacement it be used in some way?
						 */
		      	if(clickEnd(mcbd, eventButton, target, capture)){
		      		eventButton.setConsumed();
//		      		mcbd.getClicks().clearClicks();
		      	}
		      	
//		      	dragEnd(mcbd, eventButton, target, capture);
//					}
				}
				
      	dragEnd(mcbd, eventButton, target, capture);
//			}
		}
	}
		
	/**
	 * 
	 * @param event
	 * @param target
	 * @param capture
	 * @return if it is to be consumed
	 */
	public boolean clickEnd(MouseCursorButtonData buttonData, CursorButtonEvent eventButton, Spatial target,	Spatial capture){
		return false;
	}
//	public boolean clickEnd(MouseCursorButtonData button, CursorButtonEvent eventButton, Spatial target,	Spatial capture){
//		return false;
//	}
	
	/**
	 * this method is if you want to consume the event when the mouse cursor button 
	 * is pressed, this will also give access to {@link #clickEnd(EMouseCursorButton, CursorButtonEvent, Spatial, Spatial)}
	 * @param button
	 * @param eventButton
	 * @param target
	 * @param capture
	 * @return
	 */
	public boolean clickBegin(MouseCursorButtonData button, CursorButtonEvent eventButton, Spatial target,	Spatial capture){
		return false;
	}
	
	/**
	 * Hover in
	 */
	@Override
	public void cursorEntered(CursorMotionEvent event, Spatial target,Spatial capture) {
		String strDbg="";
		
		if(target!=null){
			strDbg+="target="+target.getName();
			String str=MiscJmeI.i().getPopupHelp(target);
			if(str!=null){
				MsgI.i().debug(EUserDataMiscJme.strPopupHelp+":"+str);
				MiscLemurStateI.i().setPopupHelpString(str);
			}
		}
		
		if(capture!=null){
			strDbg+="capture="+capture.getName();
		}
		
		if(!strDbg.isEmpty())MsgI.i().debug("HoverIn:"+strDbg);
	}
	
	/**
	 * Hover out
	 */
	@Override
	public void cursorExited(CursorMotionEvent event, Spatial target,Spatial capture) {
		MiscLemurStateI.i().clearPopupHelpString();
	}
	
	public boolean dragging(ArrayList<MouseCursorButtonData> aButtonList, CursorMotionEvent eventMotion, Spatial target,	Spatial capture){
		return false;
	}
	
	public boolean dragEnd(MouseCursorButtonData buttonData, CursorButtonEvent eventButton, Spatial target,	Spatial capture){
		return false;
	}
	
	@Override
	public void cursorMoved(CursorMotionEvent eventMotion, Spatial target, Spatial capture) {
		if(capture==null){
//			bCancelNextMouseReleased=true;
			return;
		}
		
		ArrayList<MouseCursorButtonData> aMouseCursorButtonsPressedList = new ArrayList<MouseCursorButtonData>();
		for(EMouseCursorButton e:EMouseCursorButton.values()){
			// Buttons pressed during drag
			MouseCursorButtonData mdata = mb().getMouseCursorDataFor(e);
			if(mdata.isPressed()){
				if(mdata.getPressedDistanceTo(MiscLemurStateI.i().eventToV3f(eventMotion)).length()>3){
					aMouseCursorButtonsPressedList.add(mdata);
				}
			}
		}
		
		if(aMouseCursorButtonsPressedList.size()>0){
			if(dragging(aMouseCursorButtonsPressedList, eventMotion, target, capture)){
				eventMotion.setConsumed();
				bCancelNextMouseReleased=true; //TODO explain why
			}
		}
		
	}

//	Command<Button> cmdbtnHoverOver = new Command<Button>(){
//		@Override
//		public void execute(Button source) {
//			if( !LemurFocusHelperStateI.i().isDialogFocusedFor(source) )return;
//			
//			AudioUII.i().playOnUserAction(EAudio.HoverOverActivators);
//			
//			Panel pnlApplyEffect = source;
//			CellDialogEntry<?> cell = (CellDialogEntry<?>)source.getUserData(EUserData.classCellRef.s());
//			if(cell!=null){
////				GlobalCommandsDelegatorI.i().dumpDevWarnEntry("activator has no cell?", source, Cell.class.getName());
////			}else{
////				source=cell;
//				pnlApplyEffect = cell;
//			}
//			
//			MiscLemurStateI.i().setOverrideBackgroundColorNegatingCurrent(pnlApplyEffect);
//			
//			source.setUserData(EUserData.bHoverOverIsWorking.s(),true);
//			
//			btnLastHoverIn = source;
//		}
//	};
//	Command<Button> cmdbtnHoverOut = new Command<Button>(){
//		@Override
//		public void execute(Button source) {
//			if( !LemurFocusHelperStateI.i().isDialogFocusedFor(source) )return;
//			
//			Panel pnlApplyEffect = source;
//			CellDialogEntry<?> cell = (CellDialogEntry<?>)source.getUserData(EUserData.classCellRef.s());
//			if(cell!=null){
//				pnlApplyEffect = cell;
////				GlobalCommandsDelegatorI.i().dumpDevWarnEntry("activator has no cell?", source, Cell.class.getName());
////			}else{
//			}
//			
//			MiscLemurStateI.i().resetOverrideBackgroundColor(pnlApplyEffect);
//			
//			if(btnLastHoverIn!=null){
//				if(btnLastHoverIn==source){
//					btnLastHoverIn=null;
//				}else{
//					//TODO also reset all like in app lost focus etc?
//					GlobalCommandsDelegatorI.i().dumpDevWarnEntry("inconsistency, why is not last hover in?", btnLastHoverIn, source, this);
//				}
//			}
//		}
//	};
	
//	public void clearLastButtonHoverIn(){
//		if(btnLastHoverIn!=null)cmdbtnHoverOut.execute(btnLastHoverIn);
//	}
	
	public void addMouseCursorHighlightEffects(Button btn){
		efDummy = GlobalManageDialogLemurI.i().setupSimpleEffect(btn, Button.EFFECT_ACTIVATE, efHighLightBkg, efDummy);
		btn.addEffect(Button.EFFECT_DEACTIVATE,efDummy);
	}
	
	DummyEffect efDummy;// = new LemurDialogHelperI.DummyEffect(LemurDialogHelperI.i().getEffectHighLightBkg().getChannel());
	
	private Effect<Button> efHighLightBkg = new AbstractEffect<Button>("ChannelHighLight") {
		@Override
		public Animation create(final Button target, final EffectInfo existing) {
			final GuiComponent gcBgChk = target.getBackground();
			if(!QuadBackgroundComponent.class.isInstance(gcBgChk)){
				MsgI.i().devWarn("background type not supported for this effect", gcBgChk, target, existing, this);
				return null;
			}
			
			return new Animation() {
				QuadBackgroundComponent gcBg = (QuadBackgroundComponent)gcBgChk;
				ColorRGBA colorBkp = gcBg.getColor();
				boolean bApplied=false;
				@Override	public void cancel() {
					gcBg.setColor(colorBkp);
				}
				@Override	public boolean animate(double tpf) {
					if(!bApplied){
	//					if(existing!=null && existing.getAnimation()==this)return true;
						gcBg.setColor(MiscJmeI.i().neglightColor(colorBkp));
						target.setUserData(EUserDataCellEntry.bHoverOverIsWorking.s(),true);
						bApplied=true;
					}
					return true;
				}
			};
		}
	};
	
//	/**
//	 * this is just to let actual effects to end themselves with their cancel()
//	 */
//	Effect efDummy = new AbstractEffect("HighLight") {
//		@Override
//		public Animation create(Object target, EffectInfo existing) {
//			return new Animation() {
//				@Override	public void cancel() {}
//				@Override	public boolean animate(double tpf) {return true;}
//			};
//		}
//	};

}
