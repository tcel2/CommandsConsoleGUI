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

package com.github.commandsconsolegui.jme;

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jme.GlobalAppRefI;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.font.BitmapFont;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * Identical dialog behaviors must go here.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public abstract class BaseDialogHelper implements IReflexFillCfg{
	public static final class CompositeControl extends CompositeControlAbs<BaseDialogHelper>{
		private CompositeControl(BaseDialogHelper casm){super(casm);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
	public final String STYLE_CONSOLE="console";
	private String strDefaultFont = "DroidSansMono";
	private StringVarField	svfUserFontOption = new StringVarField(this, strDefaultFont, null);
	private int iDefaultFontSize = 12;
	private IntLongVarField ilvFontSize = new IntLongVarField(this, iDefaultFontSize,null);
	private BitmapFont	font;
	private BitmapFont	fontConsoleDefault;
	private String	strConsoleDefaultFontName = "Console";
	private BitmapFont	fontConsoleExtraDefault;
	
	protected abstract String getTextFromField(Spatial spt);
	protected abstract Vector3f getSizeFrom(Spatial spt);
	protected abstract void setTextAt(Spatial spt,String str);
	
	public void prepareStyle() {
		fontConsoleDefault = GlobalAppRefI.i().getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		fontConsoleExtraDefault = GlobalAppRefI.i().getAssetManager().loadFont("Interface/Fonts/DroidSansMono.fnt");
		
		String strFontName=svfUserFontOption.getValueAsString();
		
		font=null;
		try{
			if(font==null){ //system font object
				font = MiscJmeI.i().fontFromTTF(strFontName,ilvFontSize.intValue());
			}
			
			if(font==null){ //custom font file
				font = MiscJmeI.i().fontFromTTFFile(strFontName,ilvFontSize.intValue());
			}
			
			if(font==null){ //bundled fonts
				strFontName=strFontName.replace(" ","");
				String strFile = "Interface/Fonts/"+strFontName+".fnt";
				font = GlobalAppRefI.i().getAssetManager().loadFont(strFile);
				if(font!=null)GlobalCommandsDelegatorI.i().dumpInfoEntry("Bundled font: "+strFile);
			}
		}catch(AssetNotFoundException ex){
			GlobalCommandsDelegatorI.i().dumpExceptionEntry(ex);
			font = fontConsoleDefault; //fontConsoleExtraDefault
			svfUserFontOption.setObjectRawValue(strConsoleDefaultFontName );
		}
	}
	
	protected BitmapFont getFont() {
		return font;
	}
	protected void setFont(BitmapFont font) {
		this.font = font;
	}
	public void setDefault() {
		svfUserFontOption.setObjectRawValue(strDefaultFont);
		ilvFontSize.setObjectRawValue(iDefaultFontSize);
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}
	
	private ArrayList<DialogStateAbs> adiagList = new ArrayList<DialogStateAbs>();

//	private boolean	bRequestSaveDialog;
	public void addDialog(DialogStateAbs diag){
		adiagList.add(diag);
	}
	public <T extends DialogStateAbs> ArrayList<T> getDialogListCopy(Class<T> clFilter) {
		if(clFilter==null)clFilter=(Class<T>)DialogStateAbs.class;
		ArrayList<T> adiag = new ArrayList<T>();
		for(DialogStateAbs diag:adiagList){
			if (clFilter.isInstance(diag)) {
				adiag.add((T)diag);
			}
		}
		return adiag;
	}
//	public <T extends BaseDialogStateAbs> ArrayList<DialogSavable> getDialogsSavableObjectListCopy(Class<T> clFilter){
//		ArrayList<DialogSavable> asv = new ArrayList<DialogSavable>();
//		for(BaseDialogStateAbs diag:getDialogListCopy(clFilter)){
//			asv.add(diag.getSavable(ccSelf));
//		}
//		return asv;
//	}
	
//	protected boolean isDialogSaveRequestedAndReset(BaseDialogStateAbs diag){
//		return diag.isRequestSaveDialogAndReset(ccSelf);
//	}
	
//	public void requestSaveDialog(BaseDialogStateAbs diag){
//		//TODO log diag?
//		this.bRequestSaveDialog=true;
//	}
//	
//	public boolean isDialogSaveRequested(){
//		return this.bRequestSaveDialog;
//	}
//	
//	protected void resetDialogSaveRequest(){
//		this.bRequestSaveDialog=false;
//	}
	
	public void update(float tpf){
	}
}