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

package com.github.commandsconsolegui.jmegui.extras;

import java.util.HashMap;

import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.jme3.scene.Node;

/**
 * A console command will be automatically created based on the configured {@link #strUIId}.<br>
 * See it at {@link #BaseUIStateAbs(String)}.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class InteractionDialogStateAbs <V> extends BaseDialogStateAbs{
	protected Node cntrNorth;
	protected String	strLastFilter = "";
	protected HashMap<String,V> hmKeyValue = new HashMap<String,V>();
	protected String	strLastSelectedKey;
	
	public abstract void clearSelection();
	
	@Override
	protected boolean enableOrUndo() {
		updateTextInfo();
		updateList();
		updateInputField();
		
		return super.enableOrUndo();
	}
	
	public static class CfgParm extends BaseDialogStateAbs.CfgParm{
		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI) {
			super(strUIId, bIgnorePrefixAndSuffix, nodeGUI, 
					false /** Dialogs must be initially disabled because they are enabled on user demand. */
				);
		}
	}
//	@Override
//	public BaseDialogStateAbs configure(ICfgParm icfg) {
//		return super.configure(icfg);
//	}
	
	/**
	 * when dialog is enabled,
	 * default is to fill with the last filter
	 */
	protected abstract void updateInputField();

	protected abstract Integer getSelectedIndex();
	
	protected abstract String getSelectedKey();
	
	protected V getSelectedValue() {
		return hmKeyValue.get(getSelectedKey());
	}
	
	/**
	 * 
	 */
	protected abstract void updateList();
	
	protected abstract void updateTextInfo();
	
	@Override
	protected boolean updateOrUndo(float tpf) {
		String str = getSelectedKey();
		if(str!=null)strLastSelectedKey=str;
		
		updateTextInfo();
		
		//requestFocus(intputText); //keep focus at input as it shall have all listeners.
		
//		setMouseCursorKeepUngrabbed(isEnabled());
		return super.updateOrUndo(tpf);
	}
	
	/**
	 * What will be shown at each entry on the list.
	 * Default is to return the default string about the object.
	 * 
	 * @param val
	 * @return
	 */
	public String formatEntryKey(V val){
		return val.toString();
	}
	
	
	/**
	 * what happens when pressing ENTER keys
	 * default is to update the list filter
	 */
	@Override
	protected void actionSubmit(){
		strLastFilter=getInputText();
		updateList();
	}

}
