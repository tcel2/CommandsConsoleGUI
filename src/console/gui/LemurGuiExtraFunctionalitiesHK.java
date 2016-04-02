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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import misc.TimedDelay;

import com.jme3.material.MatParam;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.simsilica.lemur.DocumentModel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.focus.FocusManagerState;

/**
 * These are hacks to provide extra functionalities.
 * This class functionality may break, therefore it's usage is optional.
 * Proper coding suggestions (less prone to break) are most welcome/appreciated!
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class LemurGuiExtraFunctionalitiesHK {
	public ConsoleGuiStateAbs	cgs;
	
	/**
	 * keep "initialized" vars together!
	 */
	public boolean	bInitializedFixInvisibleCursorHK;
	public boolean	bInitializedBlinkingCursorHK;
	
	/**
	 * other vars
	 */
	public TextEntryComponent	tecInputFieldHK;
	public Geometry	geomCursorHK;
	public FocusManagerState	focusStateHK;
	public boolean bAllowHK = false;
	public boolean bBlinkingTextCursorHK = true;
	public TimedDelay tdTextCursorBlinkHK = new TimedDelay(1f);
	public DocumentModel	dmInputFieldHK;
	public boolean	bFixInvisibleTextInputCursorHK = false;

	private int	iMoveCaratTo;

	private String	strPrefixHK = "(HK) ";
	

	public LemurGuiExtraFunctionalitiesHK(ConsoleGuiStateAbs cgs){
		this.cgs=cgs;
	}
	
	/**
	 * a proper fix suggestion is appreciated!
	 * @return
	 */
	public boolean fixInvisibleTextInputCursorHK(){
		if(!bAllowHK)return false;			// too much spam...	dumpWarnEntry("this fix requires HK enabled");
		
		prepareInputFieldHK();
		tecInputFieldHK.setAlpha(1);
		
		boolean bOk=true;
		if(tecInputFieldHK!=null){
			cgs.cc.dumpInfoEntry(strPrefixHK+"cursor fix applied.");
		}else{
			cgs.cc.dumpWarnEntry(strPrefixHK+"cursor fix failed...");
			bOk=false;
		}
		
		/**
		 * if it fails, this here will prevent unnecessary retry attempts...
		 */
		bInitializedFixInvisibleCursorHK=true;
		
		return bOk;
	}
	
	public void updateFixInvisibleCursorHK() {
		if(!bAllowHK)return;
		
		if(!bFixInvisibleTextInputCursorHK)return;
		
		if(!bInitializedFixInvisibleCursorHK)fixInvisibleTextInputCursorHK();
	}

	public void updateHK() {
		updateBlinkInputFieldTextCursorHK();
		updateFixInvisibleCursorHK();
	}
	
	/**
	 * We shouldnt access private fields/methods as things may break.
	 * Any code depending on this must ALWAYS be optional!
	 * 
	 * @param clazzOfObjectFrom what superclass of the object from is to be used?
	 * @param objFrom
	 * @param strFieldName will break if changed..
	 * @return field value
	 */
	public Object reflexFieldHK(Class<?> clazzOfObjectFrom, Object objFrom, String strFieldName){
		if(!bAllowHK)return null;
		
		Object objFieldValue = null;
		try{
			Field field = clazzOfObjectFrom.getDeclaredField(strFieldName);
			field.setAccessible(true);
			objFieldValue = field.get(objFrom);
			field.setAccessible(false);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			cgs.cc.dumpExceptionEntry(e);
		}
		
		return objFieldValue;
	}
	public Object reflexFieldHK(Object objFrom, String strFieldName){
		if(!bAllowHK)return null;
		
		return reflexFieldHK(objFrom.getClass(), objFrom, strFieldName);
	}
	
	/**
	 * We shouldnt access private fields/methods as things may break.
	 * Any code depending on this must ALWAYS be optional!
	 * 
	 * @param objInvoker
	 * @param strMethodName
	 * @param aobjParams
	 */
	public Object reflexMethodCallHK(Object objInvoker, String strMethodName, Object... aobjParams) {
		if(!bAllowHK)return null;
		
		Object objReturn = null;
		try {
			Method m = objInvoker.getClass().getDeclaredMethod(strMethodName);
			boolean bWasAccessible=m.isAccessible();
			if(!bWasAccessible)m.setAccessible(true);
			objReturn = m.invoke(objInvoker);
			if(!bWasAccessible)m.setAccessible(false);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			cgs.cc.dumpExceptionEntry(e);
		}
		return objReturn;
	}
	
	public void prepareInputFieldHK(){
		if(!bAllowHK)return;
		tecInputFieldHK = ((TextEntryComponent)reflexFieldHK(cgs.tfInput, "text"));
		dmInputFieldHK = ((DocumentModel)reflexFieldHK(tecInputFieldHK, "model"));
	}
	
	public void prepareForBlinkingCursorHK(){
		if(!bAllowHK)return;
		
		prepareInputFieldHK();
		geomCursorHK = ((Geometry)reflexFieldHK(tecInputFieldHK, "cursor"));
		focusStateHK = ((FocusManagerState)reflexFieldHK(GuiGlobals.getInstance(), "focusState"));
		tdTextCursorBlinkHK.updateTime();
		
		if(geomCursorHK!=null && focusStateHK!=null){
			cgs.cc.dumpInfoEntry(strPrefixHK+"Text cursor can blink now!");
		}else{
			cgs.cc.dumpWarnEntry(strPrefixHK+"Unable to let Text cursor blink :(");
		}
		
		bInitializedBlinkingCursorHK=true;
	}
	public void updateBlinkInputFieldTextCursorHK() {
		if(!bAllowHK)return;
		
		if(!bBlinkingTextCursorHK)return;
		if(!bInitializedBlinkingCursorHK)prepareForBlinkingCursorHK();
		if(!cgs.tfInput.equals(focusStateHK.getFocus()))return;
		
		long lDelay = tdTextCursorBlinkHK.getCurrentDelay();
		
		boolean bUseCursorFade = false; //it actually is the same material used on the text, so will be a problem...
		if(bUseCursorFade){
		//		if(lDelay > lTextCursorBlinkDelay){
			MatParam param = geomCursorHK.getMaterial().getParam("Color");
			ColorRGBA color = (ColorRGBA)param.getValue();
//			color.a = (tdTextCursorBlink.lDelayLimit-lDelay)*fNanoToSeconds;
			color.a = tdTextCursorBlinkHK.getCurrentDelayPercentual();
			if(color.a<0)color.a=0;
			if(color.a>1)color.a=1;
			geomCursorHK.getMaterial().setColor("Color", color);
			
			if(lDelay > tdTextCursorBlinkHK.getNanoDelayLimit()){
				tdTextCursorBlinkHK.updateTime();
			}
		}else{
			if(lDelay > tdTextCursorBlinkHK.getNanoDelayLimit()){
				if(geomCursorHK.getCullHint().compareTo(CullHint.Never)==0){
					geomCursorHK.setCullHint(CullHint.Always);
				}else{
					geomCursorHK.setCullHint(CullHint.Inherit);
				}
				
				tdTextCursorBlinkHK.updateTime();
			}
		}
			
	}
	
	public Integer getInputFieldCaratPosition(){
		if(dmInputFieldHK!=null){ //insert at carat position
			return dmInputFieldHK.getCarat();
		}
		return null;
	}
	
	public String pasteAtCaratPositionHK(String strCurrent, String strPasted) {
		if(dmInputFieldHK!=null){ //insert at carat position
			int iCarat = getInputFieldCaratPosition();
			String strBefore = strCurrent.substring(0,iCarat);
			String strAfter = strCurrent.substring(iCarat);
			strCurrent = strBefore + strPasted;
			iMoveCaratTo = strCurrent.length();
			strCurrent += strAfter;
			return strCurrent;
		}
		
		return null;
	}

	public void positionCaratProperlyHK() {
		// position carat properly
		if(dmInputFieldHK!=null){
			dmInputFieldHK.home(true);
			for(int i=0;i<iMoveCaratTo;i++){
				dmInputFieldHK.right();
			}
			reflexMethodCallHK(tecInputFieldHK, "resetCursorPosition");
		}
	}
	
	public void cleanupHK() {
		bInitializedFixInvisibleCursorHK=false;
		bInitializedBlinkingCursorHK=false;
	}
	
	public void test(){
		tecInputFieldHK.getFont().getCharSet().getCharacter(32).getWidth();
	}

	public void setCaratPosition(int iPos) {
		iMoveCaratTo = iPos;
		positionCaratProperlyHK();
	}
}