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

package console.gui;

import misc.ReflexFill.IReflexFillCfg;
import misc.ReflexFill.IReflexFillCfgVariant;
import misc.ReflexFill.ReflexFillCfg;
import misc.ReflexFill;
import misc.ReflexHacks;
import misc.StringField;
import misc.TimedDelay;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapText;
import com.jme3.material.MatParam;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.simsilica.lemur.DocumentModel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.focus.FocusManagerState;

import console.ConsoleCommands;
import console.IConsoleCommandListener;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class LemurGuiMisc implements AppState, IConsoleCommandListener, IReflexFillCfg{
	public final StringField CMD_FIX_INVISIBLE_TEXT_CURSOR = new StringField(this, ConsoleCommands.strFinalCmdCodePrefix);
	
	private static LemurGuiMisc instance = new LemurGuiMisc(); 
	public static LemurGuiMisc i(){return instance;}

	private SimpleApplication	sapp;
	
//	public void initialize(SimpleApplication sapp){
//		this.sapp = sapp;
//	}
	
	protected TimedDelay tdTextCursorBlink = new TimedDelay(1f);
	private boolean	bBlinkingTextCursor = true;
	private FocusManagerState	focusState;
	private TextField	tfToBlinkCursor;

	private boolean	bInitialized;

	private boolean	bEnabled = true;

	private ConsoleCommands	cc;

	private boolean	bFixInvisibleTextInputCursor;

//	private int	iMoveCaratTo;
	
	public Geometry getTextCursorFrom(TextField tf){
		return (Geometry) getBitmapTextFrom(tf).getChild("cursor");
//				
//		Geometry geomCursor = null;
//		for(Spatial spt:tf.getChildren()){
//			if(spt instanceof BitmapText){
//				geomCursor = (Geometry)((BitmapText)spt).getChild("cursor"); //as set at TextEntryComponent()
//				break;
//			}
//		}
//		
//		return geomCursor;
	}
	
	public BitmapText getBitmapTextFrom(TextField tf){
		for(Spatial spt:tf.getChildren()){
			if(spt instanceof BitmapText)return (BitmapText)spt;
		}
		
		return null;
	}
	
	public void setTextFieldInputToBlinkCursor(TextField tf){
		this.tfToBlinkCursor = tf;
	}
	
	public FocusManagerState getFocusManagerState(){
		if(focusState==null)focusState = sapp.getStateManager().getState(FocusManagerState.class);
		return focusState;
	}
	
	private void fixInvisibleCursor(TextField tf){
		if(!bFixInvisibleTextInputCursor)return;
		BitmapText bmt = getBitmapTextFrom(tf);
		
		/**
		 * The BitmapText base alpha is set to an invalid value -1.
		 * That value seems to be used as a marker/indicator of "invalidity?".
		 * But the problem is, it is used as a normal value and never verified/validadted 
		 * towards its invalidity of -1.
		 * Wouldnt have been better if it was used 'null' as indicator of invalidity?
		 * 
		 * This flow will fix that base alpha to fully visible.
		 */
		bmt.setAlpha(1f); // alpha need to be fixed, it was -1; -1 is an invalid value used as a merker/indicator, woulndt be better it be a null marker?
		
		/**
		 * This flow will apply the base alpha of BitmapText to the text cursor.
		 */
		tf.setColor(tf.getColor());
	}
	/**
	 * see {@link TextEntryComponent#resetCursorColor()}
	 */
	private void fixInvisibleCursor(Geometry geomCursor){
		if(!bFixInvisibleTextInputCursor)return;
//	getBitmapTextFrom(tf).setAlpha(1f); //this is a fix to let text cursor be visible.
		geomCursor.getMaterial().setColor("Color",ColorRGBA.White.clone());
	}
	
	private void updateBlinkInputFieldTextCursor(TextField tf) {
		if(!bBlinkingTextCursor)return;
		if(!tf.equals(getFocusManagerState().getFocus()))return;
		
//		tdTextCursorBlink.updateTime();
		
		String strCursorHotLink="CursorHotLink";
		Geometry geomCursor = tf.getUserData(strCursorHotLink);
		if(geomCursor==null){
			geomCursor = getTextCursorFrom(tf);
			tf.setUserData(strCursorHotLink, geomCursor);
//		fixInvisibleCursor(geomCursor);
		}
		
		long lDelay = tdTextCursorBlink.getCurrentDelay();
		
		boolean bUseCursorFade = false; //it actually is the same material used on the text, so will be a problem...
		if(bUseCursorFade){
		//		if(lDelay > lTextCursorBlinkDelay){
			MatParam param = geomCursor.getMaterial().getParam("Color");
			ColorRGBA color = (ColorRGBA)param.getValue();
//			color.a = (tdTextCursorBlink.lDelayLimit-lDelay)*fNanoToSeconds;
			color.a = tdTextCursorBlink.getCurrentDelayPercentual();
			if(color.a<0)color.a=0;
			if(color.a>1)color.a=1;
			geomCursor.getMaterial().setColor("Color", color);
			
			if(lDelay > tdTextCursorBlink.getNanoDelayLimit()){
				tdTextCursorBlink.updateTime();
			}
		}else{
			if(lDelay > tdTextCursorBlink.getNanoDelayLimit()){
				if(geomCursor.getCullHint().compareTo(CullHint.Always)!=0){
					geomCursor.setCullHint(CullHint.Always);
				}else{
					fixInvisibleCursor(tf);
//					fixInvisibleCursor(geomCursor);
					geomCursor.setCullHint(CullHint.Inherit);
				}
				
				tdTextCursorBlink.updateTime();
			}
		}
			
	}
	
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		this.sapp = (SimpleApplication) app;
		tdTextCursorBlink.updateTime();
		bInitialized=true;
		
		cc.addConsoleCommandListener(this);
		ReflexFill.assertReflexFillFieldsForOwner(this);
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
		return bEnabled ;
	}

	@Override
	public void stateAttached(AppStateManager stateManager) {
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
	}

	@Override
	public void update(float tpf) {
		if(isEnabled()){
			if(tfToBlinkCursor!=null)updateBlinkInputFieldTextCursor(tfToBlinkCursor);
		}
	}

	@Override
	public void render(RenderManager rm) {
	}

	@Override
	public void postRender() {
	}

	@Override
	public void cleanup() {
	}

	public void requestFocus(Spatial spt) {
		if(spt instanceof TextField){
			setTextFieldInputToBlinkCursor((TextField) spt);
		}
		
		GuiGlobals.getInstance().requestFocus(spt);
	}

	@Override
	public boolean executePreparedCommand(ConsoleCommands	cc) {
		boolean bCmdEndedGracefully = false;
		
		if(cc.checkCmdValidity(this,CMD_FIX_INVISIBLE_TEXT_CURSOR ,"in case text cursor is invisible")){
			cc.dumpInfoEntry("requesting: "+CMD_FIX_INVISIBLE_TEXT_CURSOR);
			bFixInvisibleTextInputCursor=true;
			bCmdEndedGracefully = true;
		}else
		{}
		
		return bCmdEndedGracefully;
	}

	public void setConsoleCommands(ConsoleCommands cc) {
		this.cc=cc;
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return cc.getReflexFillCfg(rfcv);
	}
	
	private String strCaratNewPosition = "CaratNewPosition";
	/**
	 * Carat new position will be stored at {@link TextField#}
	 * 
	 * @param tf
	 * @param strCurrent
	 * @param strPasted
	 * @return
	 */
	public String prepareStringToPasteAtCaratPosition(TextField tf, String strCurrent, String strPasted) {
		int iCarat = tf.getDocumentModel().getCarat();
		String strBefore = strCurrent.substring(0,iCarat);
		String strAfter = strCurrent.substring(iCarat);
		
		strCurrent = strBefore + strPasted;
		tf.setUserData(strCaratNewPosition, strCurrent.length());
		strCurrent += strAfter;
		
		return strCurrent;
	}
	
	public void pasteAtCaratPosition(TextField tf, String strCurrent, String strPasted) {
		tf.setText(prepareStringToPasteAtCaratPosition(tf, strCurrent, strPasted));
		positionCaratProperly(tf);//, (int) tf.getUserData(strCaratNewPosition));
	}
	
	/**
	 * 
	 * @param tf must have had the new carat set by {@link #prepareStringToPasteAtCaratPosition(TextField, String, String)} 
	 */
	public void positionCaratProperly(TextField tf) {
		int iMoveCaratTo = (int) tf.getUserData(strCaratNewPosition);
		setCaratPosition(tf, iMoveCaratTo);
	}
	
	/**
	 * To show the cursor at the new carat position, 
	 * this required method: {@link TextEntryComponent#resetCursorPosition}
	 * must be reached in some way...
	 * 
	 * @param tf
	 * @param iMoveCaratTo
	 */
	public void setCaratPosition(TextField tf, int iMoveCaratTo) {
		// position carat properly
		DocumentModel dm = tf.getDocumentModel();
		dm.home(true);
		for(int i=0;i<iMoveCaratTo;i++){
			dm.right();
		}
		
		resetCursorPosition(tf);
	}
	
	/**
	 * This updates the displayed text cursor position.
	 * 
	 * This below is actually a trick, 
	 * because this flow will finally call the required method.
	 *  
	 * @param tf
	 */
	public void resetCursorPosition(TextField tf){
		tf.setFontSize(tf.getFontSize()); //resetCursorPositionHK(tf);
	}	
	@Deprecated
	public void resetCursorPositionHK(TextField tf){
		TextEntryComponent tec = ((TextEntryComponent)ReflexHacks.i().getFieldValueHK(tf, "text"));
		ReflexHacks.i().callMethodHK(tec,"resetCursorPosition");
	}
}