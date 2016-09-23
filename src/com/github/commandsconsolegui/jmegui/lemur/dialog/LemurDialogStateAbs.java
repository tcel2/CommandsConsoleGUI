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
import java.util.HashMap;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.DialogStateAbs;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.MiscLemurStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.MiscLemurStateI.BindKey;
import com.github.commandsconsolegui.jmegui.lemur.dialog.LemurDialogHelperI.DialogStyleElementId;
import com.github.commandsconsolegui.jmegui.lemur.extras.CellRendererDialogEntry;
import com.github.commandsconsolegui.jmegui.lemur.extras.CellRendererDialogEntry.CellDialogEntry;
import com.github.commandsconsolegui.jmegui.lemur.extras.CellRendererDialogEntry.CellDialogEntry.EUserData;
import com.github.commandsconsolegui.jmegui.lemur.extras.DialogMainContainer;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.WorkAroundI;
import com.github.commandsconsolegui.misc.WorkAroundI.BugFixBoolTogglerCmdField;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GridPanel;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BorderLayout.Position;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.list.SelectionModel;
import com.simsilica.lemur.style.ElementId;

/**
* 
* More info at {@link DialogStateAbs}
*	TODO implement docking dialogs, a small icon will be created at app window edges
* 
* TODO migrate from {@link DialogStateAbs} to here, everything that is not usable at {@link ConsoleLemurStateI}
* 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
*
*/
//public abstract class LemurDialogGUIStateAbs<T,CS extends LemurDialogGUIStateAbs.CompositeSavableLemur,R extends LemurDialogGUIStateAbs<T,CS,R>> extends BaseDialogStateAbs<T,CS,R> {//implements IWorkAroundBugFix{
public abstract class LemurDialogStateAbs<T,R extends LemurDialogStateAbs<T,R>> extends DialogStateAbs<T,R> {//implements IWorkAroundBugFix{
	private Label	lblTitle;
	private Label	lblTextInfo;
//	private ListBox<DialogListEntryData<T>>	lstbxEntriesToSelect;
	private VersionedList<DialogListEntryData<T>>	vlVisibleEntriesList = new VersionedList<DialogListEntryData<T>>();
	private int	iVisibleRows;
//	private Integer	iEntryHeightPixels; //TODO this is init is failing why? = 20; 
	private Vector3f	v3fEntryListSizeIni;
	private Container	cntrEntryCfg;
	private SelectionModel	selectionModel;
	private BoolTogglerCmdField btgAutoScroll = new BoolTogglerCmdField(this, true).setCallNothingOnChange();
//	private ButtonCommand	bc;
	private boolean	bRefreshScroll;
	private HashMap<String, LemurDialogStateAbs<T,?>> hmModals = new HashMap<String, LemurDialogStateAbs<T,?>>();
	private Long	lClickActionMilis;
//	private DialogListEntryData<T>	dataSelectRequested;
	private Label	lblSelectedEntryStatus;
	private ArrayList<BindKey>	abkList = new ArrayList<BindKey>();
	private KeyActionListener	actSimpleActions;
	private TimedDelayVarField tdListboxSelectorAreaBlinkFade = new TimedDelayVarField(1f,"");
	private CellRendererDialogEntry<T>	cr;
//	private StringVarField svfStyle = new StringVarField(this, null, null);
//	private String strStyle;
	private BoolTogglerCmdField btgEffectListEntries = new BoolTogglerCmdField(this, true);
//	private TimedDelayVarField tdEffectListEachEntry = new TimedDelayVarField(this, 0.05f, "");
//	private float fEffectListEntryDelay=0.05f;
	private FloatDoubleVarField fdvEffectListEntryDelay = new FloatDoubleVarField(this, 0.15f, "");
	
//	public abstract T getCmdDummy();
	
	@Override
	public DialogMainContainer getDialogMainContainer(){
		return (DialogMainContainer)super.getDialogMainContainer();
	}
	
	private FloatDoubleVarField fdvEntryHeightMultiplier = new FloatDoubleVarField(this,1f,"");
	
	public static class CfgParm extends DialogStateAbs.CfgParm{
		private Float fDialogHeightPercentOfAppWindow;
		private Float fDialogWidthPercentOfAppWindow;
		private Float fInfoHeightPercentOfDialog;
		
//		private Integer iEntryHeightPixels;
		private Float fEntryHeightMultiplier = 1.0f;
		
//		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI) {
//			super(strUIId, bIgnorePrefixAndSuffix, nodeGUI);
//		}
		/**
		 * 
		 * @param strUIId
		 * @param bIgnorePrefixAndSuffix
		 * @param fDialogHeightPercentOfAppWindow (if null will use default) the percentual height to cover the application screen/window
		 * @param fDialogWidthPercentOfAppWindow (if null will use default) the percentual width to cover the application screen/window
		 * @param fInfoHeightPercentOfDialog (if null will use default) the percentual height to show informational text, the list and input field will properly use the remaining space
		 */
		public CfgParm(String strUIId,
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow, Float fInfoHeightPercentOfDialog,
				Float fEntryHeightMultiplier)//, BaseDialogStateAbs<T> modalParent)
		{
			super(strUIId);//, nodeGUI);//, modalParent);
			
			this.fDialogHeightPercentOfAppWindow = fDialogHeightPercentOfAppWindow;
			this.fDialogWidthPercentOfAppWindow = fDialogWidthPercentOfAppWindow;
			this.fInfoHeightPercentOfDialog = fInfoHeightPercentOfDialog;
			if(fEntryHeightMultiplier!=null)this.fEntryHeightMultiplier = fEntryHeightMultiplier;
		}
	}
	private CfgParm	cfg;
	private boolean	bRunningEffectAtAllListEntries;
	private float	fMinScale = 0.01f;
	private boolean	bPreparedForListEntriesEffects;
	private Integer	iFinalEntryHeightPixels;
	private Quaternion	quaBkpMain;
	private Container	cntrCenterMain;
	private Button	btnResizeNorth;
	private Button	btnResizeSouth;
	private Button	btnResizeEast;
	private Button	btnResizeWest;
	private ArrayList<Button>	abtnBorderList = new ArrayList<Button>();
	private Vector3f	v3fDiagWindowSize;
	private Vector3f	v3fPosCentered;
	@Override
	public R configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;//this also validates if icfg is the CfgParam of this class
		
		fdvEntryHeightMultiplier.setObjectRawValue(cfg.fEntryHeightMultiplier);
//		DialogMouseCursorListenerI.i().configure(null);
		
		if(cfg.fDialogHeightPercentOfAppWindow==null){
			cfg.fDialogHeightPercentOfAppWindow=0.75f;
		}
		
		if(cfg.fDialogWidthPercentOfAppWindow==null){
			cfg.fDialogWidthPercentOfAppWindow=0.75f;
		}
		
		if(cfg.fInfoHeightPercentOfDialog==null){
			cfg.fInfoHeightPercentOfDialog=0.25f;
		}
		
//		if(isCompositeSavableSet())setCompositeSavable(new LemurDialogCS(this));
		
		super.configure(cfg);
		
		return storeCfgAndReturnSelf(cfg);
	}
	
	public void selectAndChoseOption(DialogListEntryData<T> data){
		if(!isOptionSelectionMode())throw new PrerequisitesNotMetException("not option mode");
		if(data==null)throw new PrerequisitesNotMetException("invalid null data");
		
		selectEntry(data);
		requestActionSubmit();
	}

	public boolean isMyChild(Spatial spt){
		return getDialogMainContainer().hasChild(spt); //this is actually recursive!!!
	}
	
	private float sizePercOrPixels(float fSizeBase, float fPercOrPixels){
		if(Float.compare(fPercOrPixels, 1.0f)<=0){ //percent
			fSizeBase *= fPercOrPixels;
		}else{ // >1.0f is in pixels
			fSizeBase = fPercOrPixels;
		}
		
		return fSizeBase;
	}
	
	@Override
	protected boolean initAttempt() {
		if(!isCompositeSavableSet())setCompositeSavable(new LemurDialogCS(this));
		if(!super.initAttempt())return false;
		
//		setRetryDelayFor(300L, EDelayMode.Update.s()); //mainly useful when resizing
		
		return true;
	}
	
	/**
	 * The input field will not require height, will be small on south edge.
	 * @param fDialogPerc the percentual width/height to cover the application screen/window 
	 * @param fInfoPerc the percentual height to show informational text, the list and input field will properly use the remaining space
	 */
	@Override
	protected boolean initGUI(){
		if(!super.initGUI())return false;
//		if(getStyle()==null){
//			setStyle(ConsoleLemurStateI.i().STYLE_CONSOLE);
//		}
		
		//main top container
//		setContainerMain(new ContainerMain(new BorderLayout(), getDiagStyle()).setDiagOwner(this));
		setDialogMainContainer(new DialogMainContainer(new BorderLayout(), getDiagStyle()));
		getDialogMainContainer().setName(getId()+"_Dialog");
		
		Vector3f v3fAppWindowSize = MiscJmeI.i().getAppWindowSize();
		v3fDiagWindowSize = new Vector3f(v3fAppWindowSize);
		v3fDiagWindowSize.y = sizePercOrPixels(v3fDiagWindowSize.y,cfg.fDialogHeightPercentOfAppWindow);
		v3fDiagWindowSize.x = sizePercOrPixels(v3fDiagWindowSize.x,cfg.fDialogWidthPercentOfAppWindow);
		
		v3fPosCentered = new Vector3f(
			(v3fAppWindowSize.x-v3fDiagWindowSize.x)/2f,
			(v3fAppWindowSize.y-v3fDiagWindowSize.y)/2f+v3fDiagWindowSize.y,
			0
		);
		
		cfg.setIniPos(v3fPosCentered.clone());
		cfg.setIniSize(v3fDiagWindowSize.clone());
		
		MiscLemurStateI.i().setSizeSafely(getDialogMainContainer(), v3fDiagWindowSize);
		getDialogMainContainer().setLocalTranslation(v3fPosCentered);
		
		// resizing borders
//		CallQueueI.i().addCall(new CallableX() {
//			@Override
//			public Boolean call() {
				reinitBorders(true);
//				return true;
//			}
//		});
//		if(false){
			v3fDiagWindowSize.x -= btnResizeEast.getSize().x + btnResizeWest.getSize().x;
			v3fDiagWindowSize.y -= btnResizeNorth.getSize().y + btnResizeSouth.getSize().y;
//		}
		
		// main center container
		cntrCenterMain = new Container(new BorderLayout(), getDiagStyle());
		MiscJmeI.i().setUserDataPSH(cntrCenterMain, this);
		quaBkpMain = cntrCenterMain.getLocalRotation().clone();
		cntrCenterMain.setName(getId()+"_CenterMain");
		getDialogMainContainer().addChild(cntrCenterMain, BorderLayout.Position.Center);
		
		// impossible layout indicator
//		Label lbl = new Label("[X] impossible layout",getDiagStyle());
//		lbl.setFontSize(0.5f);
		getDialogMainContainer().setImpossibleLayoutIndicatorAndCenterMain(
			null, //			lbl,
			cntrCenterMain,
			this);
		
		///////////////////////// NORTH (title + info/help)
		setContainerNorth(new Container(new BorderLayout(), getDiagStyle()));
		getNorthContainer().setName(getId()+"_NorthContainer");
		Vector3f v3fNorthSize = v3fDiagWindowSize.clone();
		/**
		 * TODO info height should be automatic. Or Info should be a list with vertical scroll bar, and constrainted to >= 1 lines.
		 */
		float fInfoHeightPixels = sizePercOrPixels(v3fDiagWindowSize.y, cfg.fInfoHeightPercentOfDialog);
		v3fNorthSize.y = fInfoHeightPixels;
		MiscLemurStateI.i().setSizeSafely(getNorthContainer(), v3fNorthSize);
		
		//title 
//		Container cntrTitleBox = new Container(new BorderLayout(), getStyle());
//		cntrTitleBox.setName(getId()+"_TitleBox");
//		cntrTitleBox.addChild(lblTitle, BorderLayout.Position.Center);
		
		lblTitle = new Label(getTitle(),getDiagStyle());
		lblTitle.setName(getId()+"_Title");
		ColorRGBA cLightGreen = new ColorRGBA(0.35f,1f,0.35f,1f);
		lblTitle.setColor(cLightGreen); //TODO make it custom
		getNorthContainer().addChild(lblTitle, BorderLayout.Position.North);
		
//		CursorEventControl.addListenersToSpatial(lblTitle, DialogMouseCursorListenerI.i());
		
		// simple info
		lblTextInfo = new Label("",getDiagStyle());
		lblTextInfo.setName(getId()+"_TxtInfo");
		MiscLemurStateI.i().lineWrapDisableFor(lblTextInfo);
		getNorthContainer().addChild(lblTextInfo, BorderLayout.Position.Center);
		
		cntrCenterMain.addChild(getNorthContainer(), BorderLayout.Position.North);
		
		//////////////////////////// CENTER (list)
		// list
		v3fEntryListSizeIni = v3fDiagWindowSize.clone();
//		float fListPerc = 1.0f - cfg.fInfoHeightPercentOfDialog;
//		v3fEntryListSize.y *= fListPerc;
		v3fEntryListSizeIni.y -= fInfoHeightPixels;
		setMainList(new ListBox<DialogListEntryData<T>>(
			new VersionedList<DialogListEntryData<T>>(), 
			getCellRenderer(), 
			getDiagStyle()));
		selectionModel = getMainList().getSelectionModel();
		getMainList().setName(getId()+"_EntriesList");
		getMainList().setSize(v3fEntryListSizeIni); //not preferred, so the input field can fit properly
		//TODO multi was not implemented yet... lstbxVoucherListBox.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		cntrCenterMain.addChild(getMainList(), BorderLayout.Position.Center);
		
//		vlstrEntriesList.add("(Empty list)");
		getMainList().setModel((VersionedList<DialogListEntryData<T>>)vlVisibleEntriesList);
		
//		LemurMiscHelpersStateI.i().bugFix(null, LemurMiscHelpersStateI.i().btgBugFixListBoxSelectorArea, getListEntries());
		
//		/**
//		 * TODO entry height should be automatic... may be each entry could have its own height.
//		 */
//		iEntryHeightPixels = cfg.iEntryHeightPixels;
		
		//////////////////////////////// SOUTH (typing/config)
		setCntrSouth(new Container(new BorderLayout(), getDiagStyle()));
		getSouthContainer().setName(getId()+"_SouthContainer");
		
//		// configure an entry from the list
//		cntrEntryCfg = new Container(new BorderLayout(), getStyle());
//		cntrEntryCfg.setName(getId()+"_EntryConfig");
//		getSouthContainer().addChild(cntrEntryCfg, Bor)
		
		// status line, about the currently selected entry on the list
		lblSelectedEntryStatus = new Label("Selected Entry Status",getDiagStyle());
		MiscLemurStateI.i().lineWrapDisableFor(lblSelectedEntryStatus);
		getSouthContainer().addChild(lblSelectedEntryStatus, BorderLayout.Position.North);
		
		// mainly used as a list filter
		setInputField(new TextField("",getDiagStyle()));
		getInputField().setName(getId()+"_InputField");
		LemurFocusHelperStateI.i().addFocusChangeListener(getInputField());
		getSouthContainer().addChild(getInputField(),BorderLayout.Position.South);
		
		cntrCenterMain.addChild(getSouthContainer(), BorderLayout.Position.South);
		
		// finalize
		LemurFocusHelperStateI.i().prepareDialogToBeFocused(this);
		CursorEventControl.addListenersToSpatial(getDialogMainContainer(), DialogMouseCursorListenerI.i());
		
		getNodeGUI().attachChild(getDialogMainContainer());
		
		return true;
	}
	
	private void reinitBorders(boolean bAppyBorderSize) {
//		abtnResizeBorderList.clear();
		
		btnResizeNorth = prepareResizeBorder(btnResizeNorth, BorderLayout.Position.North);
		btnResizeSouth = prepareResizeBorder(btnResizeSouth, BorderLayout.Position.South);
		btnResizeEast = prepareResizeBorder(btnResizeEast, BorderLayout.Position.East);
		btnResizeWest = prepareResizeBorder(btnResizeWest, BorderLayout.Position.West);
		
		if(bAppyBorderSize){
			CallQueueI.i().addCall(new CallableX(this) {
				@Override
				public Boolean call() {
					if(getCompositeSavable(LemurDialogCS.class)==null)return false;
					getCompositeSavable(LemurDialogCS.class).ilvBorderThickness.callerAssignedQueueNow();
					return true;
				}
			});
		}
//		setBordersSize(ilvBorderSize.getInt());
	}

	BoolTogglerCmdField btgResizeBordersNorthAndSouthAlsoAffectEastBorder = new BoolTogglerCmdField(this,true);
	BoolTogglerCmdField btgResizeBordersWestAndEastAlsoAffectSouthBorder = new BoolTogglerCmdField(this,true);
	
	@Override
	public void move(Spatial sptDraggedElement, Vector3f v3fDisplacement) {
		Vector3f v3fSizeAdd = new Vector3f();
		Vector3f v3fPosAdd = new Vector3f();
		
		// will be resize
		boolean bWorkOnEastBorder = false;
		boolean bWorkOnSouthBorder = false;
		if(sptDraggedElement==btnResizeNorth){
			v3fSizeAdd.y += v3fDisplacement.y;
			v3fPosAdd.y += v3fDisplacement.y;
			if(btgResizeBordersNorthAndSouthAlsoAffectEastBorder.b())bWorkOnEastBorder=true;
		}else
		if(sptDraggedElement==btnResizeSouth){
			bWorkOnSouthBorder = true;
			if(btgResizeBordersNorthAndSouthAlsoAffectEastBorder.b())bWorkOnEastBorder=true;
		}else
		if(sptDraggedElement==btnResizeWest){
			v3fSizeAdd.x += -v3fDisplacement.x;
			v3fPosAdd.x += v3fDisplacement.x;
			if(btgResizeBordersWestAndEastAlsoAffectSouthBorder.b())bWorkOnSouthBorder=true;
		}else
		if(sptDraggedElement==btnResizeEast){
			bWorkOnEastBorder=true;
			if(btgResizeBordersWestAndEastAlsoAffectSouthBorder.b())bWorkOnSouthBorder=true;
		}else{
			/**
			 * simple move
			 */
			super.move(sptDraggedElement, v3fDisplacement);
			return;
		}
		
		if(bWorkOnEastBorder)v3fSizeAdd.x += v3fDisplacement.x;
		if(bWorkOnSouthBorder)v3fSizeAdd.y += -v3fDisplacement.y;
		
		Vector3f v3fSizeNew = getDialogMainContainer().getPreferredSize().add(v3fSizeAdd);
		if(MiscLemurStateI.i().setSizeSafely(getDialogMainContainer(), v3fSizeNew, true)!=null){
			Vector3f v3fPosNew = getDialogMainContainer().getLocalTranslation().add(v3fPosAdd);
			getDialogMainContainer().setLocalTranslation(v3fPosNew);
		}
		
		requestRefreshUpdateList();
	}
	
	private Button prepareResizeBorder(final Button btnExisting, final Position edge) {
		if(btnExisting!=null){
			if(!btgBugFixReinitBordersByRecreating.b()){
				CursorEventControl.removeListenersFromSpatial(btnExisting, DialogMouseCursorListenerI.i());
				CursorEventControl.addListenersToSpatial(btnExisting, DialogMouseCursorListenerI.i());
				/**
				 * this does not work very well...
				 */
				Boolean b=btnExisting.getUserData(EUserData.bHoverOverIsWorking.s());
				if(b!=null && b){
					return btnExisting;
				}
			}
			
			abtnBorderList.remove(btnExisting);
		}
			
//			CursorEventControl.removeListenersFromSpatial(btnExisting, DialogMouseCursorListenerI.i());
////			getDialogMainContainer().addChild(new Panel(), edge);
//			getDialogMainContainer().removeChild(btnExisting);
//			CallQueueI.i().addCall(new CallableX(this,100) {
//				@Override
//				public Boolean call() {
//					getDialogMainContainer().addChild(btnExisting, edge);
//					CursorEventControl.addListenersToSpatial(btnExisting, DialogMouseCursorListenerI.i());
//					return true;
//				}
//			}.updateTimeMilisNow());
//			return btnExisting;
//		}else{
			Button btnBorder=new Button("", new ElementId(DialogStyleElementId.buttonResizeBorder.s()), getDiagStyle());
			//TRICK(seems not necessary?): btnBorder.setFontSize(0.1f); //this trick will let us set it with any dot size!
			
			MiscJmeI.i().setUserDataPSH(btnBorder, this); //if a border is clicked, the bugfixer that recreates it will make the old border object have no parent. Its parentest would have a reference to the dialog, but now it is alone, so such reference must be on it.
			
			btnBorder.setName("Dialog_ResizeBorder_"+edge.toString());
			
			CursorEventControl.addListenersToSpatial(btnBorder, DialogMouseCursorListenerI.i());
			DialogMouseCursorListenerI.i().addDefaultCommands(btnBorder);
			
			abtnBorderList.add(btnBorder);
			
			getDialogMainContainer().addChild(btnBorder, edge); //this actually replaces the current at that border
			
			return btnBorder;
//		}
	}
	
	StringCmdField scfFixReinitDialogBorders = new StringCmdField(this,null,"it may require a few tries actually...")
		.setCallerAssigned(new CallableX(this,1000) {
			@Override
			public Boolean call() {
				if(isEnabled()){
					reinitBorders(false);
					return true;
				}
				
				GlobalCommandsDelegatorI.i().dumpWarnEntry("not enabled: "+LemurDialogStateAbs.this.getId(), this, LemurDialogStateAbs.this);
				return false;
			}
		});
	private boolean	bReinitBordersAfterThicknessChange;
	
//	IntLongVarField ilvBorderThickness = new IntLongVarField(this, 3, "").setMinMax(1L, 20L)
//		.setCallerAssigned(new CallableX(this) {
//			@Override
//			public Boolean call() {
//				setBordersThickness(ilvBorderThickness.getInt());
//				return true;
//			}
//		});
//	private boolean	bAllowUpdateLogicalState;
	
	public static class LemurDialogCS extends DialogCS<LemurDialogStateAbs> {
		public LemurDialogCS() {super();}//required by savable
		public LemurDialogCS(LemurDialogStateAbs owner) {super(owner);}
		
		/**
		 * This console variable will be saved at console cfg file and also with the dialog JME savable. 
		 */
		private IntLongVarField ilvBorderThickness;
		
		@Override
		protected void initialize(){
			super.initialize();
			
			IReflexFillCfg irfcfgOwner = isThisInstanceALoadedTmp() ? null : this;
			ilvBorderThickness = new IntLongVarField(irfcfgOwner, 3, "")
				.setMinMax(1L, 20L)
				.setCallerAssigned(new CallableX(this,100) {
					@Override
					public Boolean call() {
						LemurDialogStateAbs diag = LemurDialogCS.this.getOwner();
						if(diag==null)return false; //to retry
						
						diag.setBordersThickness(ilvBorderThickness.getInt());
						return true;
					}
				});
			
			if(!isThisInstanceALoadedTmp()){
				ilvBorderThickness.callerAssignedQueueNow();
//				getOwner().setBordersThickness(ilvBorderThickness.getInt()); //apply default initially
			}
		}
		
	}
	
	public void setBordersThickness(int iPixels){
		for(Button btn:abtnBorderList){
			MiscLemurStateI.i().setSizeSafely(btn, iPixels, iPixels, true);
		}
//		CallQueueI.i().addCall(callerReinitBordersAfterThicknessChange.updateTimeMilisNow());
		bReinitBordersAfterThicknessChange=true;
	}

	@Override
	protected ListBox<DialogListEntryData<T>> getMainList() {
		return (ListBox<DialogListEntryData<T>>)super.getMainList();
	}
	
	protected CellRendererDialogEntry<T> getCellRenderer(){
		if(cr==null)cr = new CellRendererDialogEntry<T>(getDiagStyle(),this);// bOptionSelectionMode),
		return cr;
	}
	
//	@Override
//	public void requestFocus(Spatial spt) {
//		LemurFocusHelperStateI.i().requestFocus(spt);
//	}
	
	@Override
	protected boolean enableAttempt() {
		if(!super.enableAttempt())return false;
		
		LemurFocusHelperStateI.i().requestFocus(getInputField());
		
		bPreparedForListEntriesEffects=false;
//		prepareEffectListEntries(false);
		
		return true;
	}
	
	@Override
	protected boolean disableAttempt() {
		if(!super.disableAttempt())return false;
		
//		if(getInputField().equals(LemurFocusHelperStateI.i().getFocused())){
			LemurFocusHelperStateI.i().removeFocusableFromList(getInputField());
//		}
			
			// this is to prepare for the next enable
//			prepareEffectListEntries(false);
			
//		bPreparedForListEntriesEffects=false;
		
		return true;
	}
	
//	@Override
//	protected void enableSuccess() {
//		super.enableSuccess();
//		updateInputField();
//	}
	
	@Override
	protected TextField getInputField(){
		return (TextField)super.getInputField();
	}
	
	@Override
	public DialogListEntryData<T> getSelectedEntryData() {
		Integer iSel = selectionModel.getSelection();
		if(iSel==null)return null;
		return	vlVisibleEntriesList.get(iSel);
	}
	
	private void updateFinalEntryHeightPixels(){
		this.iFinalEntryHeightPixels = (int)FastMath.ceil(
			getEntryHeightPixels() * fdvEntryHeightMultiplier.f());
	}
	
	private GridModel<Panel> getListBoxGridPanelModel(){
		return getMainList().getGridPanel().getModel();
	}
	
	protected Integer getEntryHeightPixels(){
		// query for an entry from the list
		//if(vlVisibleEntriesList.size()==0)return null;
		
		// create a new cell
		GridModel<Panel> gm = getListBoxGridPanelModel();
		Panel pnl = gm.getCell(0, 0, null);
		float fHeight = pnl.getPreferredSize().getY();
		// a simple value would be: MiscJmeI.i().retrieveBitmapTextFor(new Button("W")).getLineHeight()
		
		return (int)FastMath.ceil(fHeight);
	}
	
	/**
	 * override ex. to avoid reseting
	 */
	private void resetList(){
		vlVisibleEntriesList.clear();
		clearSelection();
//		selectionModel.setSelection(-1);
	}
	
	///**
	//* call {@link #updateList(ArrayList)} from the method overriding this
	//*/
	
	private void addWithParents(DialogListEntryData<T> dled){
		if(vlVisibleEntriesList.contains(dled))return;
		
		vlVisibleEntriesList.add(dled);
		
		DialogListEntryData<T> dledParent = dled.getParent();
		while(dledParent!=null){
			if(!vlVisibleEntriesList.contains(dledParent)){
				vlVisibleEntriesList.add(vlVisibleEntriesList.indexOf(dled),dledParent);
			}
			dled=dledParent;
			dledParent=dledParent.getParent();
		}
	}
	
	@Override
	protected void updateList(){
//		updateList(adleCompleteEntriesList);
		DialogListEntryData<T> dledLastSelectedBkp = getLastSelected(); 
		
		resetList();
		
		prepareTree();
		
		for(DialogListEntryData<T> dled:getCompleteEntriesListCopy()){
			if(!getLastFilter().isEmpty()){
				if(dled.getVisibleText().toLowerCase().contains(getLastFilter())){
					addWithParents(dled);
//					vlVisibleEntriesList.add(dled);
				}
			}else{
				if(dled.getParent()==null){
					vlVisibleEntriesList.add(dled); //root entries
				}else{
					if(checkAllParentTreesExpanded(dled)){
						vlVisibleEntriesList.add(dled);
					}
				}
			}
		}
		
		updateSelected(dledLastSelectedBkp);
		
		/**
		 * update visible rows
		 * 
		 * if there are too many rows, they will be shrinked...
		 * so this grants they have a good height.
		 * 
		 * TODO sum each individual top entries height? considering they could have diff heights of course
		 */
		if(getListBoxGridPanelModel().getRowCount()>0){
			updateFinalEntryHeightPixels();
			
//			iVisibleRows = (int) (v3fEntryListSizeIni.y/getFinalEntryHeightPixels());
			iVisibleRows = (int) (getMainList().getSize().y/getFinalEntryHeightPixels());
			getMainList().setVisibleItems(iVisibleRows);
			if(vlVisibleEntriesList.size()>0){
				if(getSelectedEntryData()==null){
					selectRelativeEntry(0);
				}
			}
		}
	}
	
//	/**
//	 * basic functionality
//	 * 
//	 * @param aValueList
//	 */
//	private void updateList(ArrayList<DialogListEntryData<T>> adle){
//	}
	
	/**
	 * for entry visibility
	 * @param dled
	 * @return
	 */
	private boolean checkAllParentTreesExpanded(DialogListEntryData<T> dled){
		DialogListEntryData<T> dledParent = dled.getParent();
		while(dledParent!=null){
			if(!dledParent.isTreeExpanded())return false;
			dledParent = dledParent.getParent();
		}
		return true;
	}
	
	/**
	 * 
	 * @return -1 if none
	 */
	public int getSelectedIndex(){
		return vlVisibleEntriesList.indexOf(getLastSelected());
	}
	
	private void autoScroll(){
		if(!btgAutoScroll.b())return;
		
		Integer iSelected = getSelectedIndex();
		if(iSelected!=null){ //TODO this is buggy...
			int iTopEntryIndex = getTopEntryIndex();
			int iBottomItemIndex = getBottomEntryIndex();
			Integer iScrollTo=null;
			
			if(iSelected>=iBottomItemIndex){
				iScrollTo=iSelected-iBottomItemIndex+iTopEntryIndex;
			}else
			if(iSelected<=iTopEntryIndex){
				iScrollTo=iSelected-1;
			}
			
			if(iScrollTo!=null){
				scrollTo(iScrollTo);
			}
		}
		
		bRefreshScroll=false;
	}
	
	protected ArrayList<CellDialogEntry<T>> getVisibleCellEntries(){
		ArrayList<CellDialogEntry<T>> acell = new ArrayList<CellDialogEntry<T>>();
		
		GridPanel gp = getMainList().getGridPanel();
		for(int iC=0;iC<gp.getVisibleColumns();iC++){
			for(int iR=0;iR<gp.getVisibleRows();iR++){
				Panel pnl = gp.getCell(gp.getRow()+iR, gp.getColumn()+iC);
				if (pnl instanceof CellDialogEntry) {
					CellDialogEntry<T> cell = (CellDialogEntry<T>) pnl;
					acell.add(cell);
				}
			}
		}
		
		return acell;
	}
	
	protected boolean simpleUpdateVisibleCells(float tpf){
		for(CellDialogEntry<T> cell:getVisibleCellEntries()){
			cell.simpleUpdateThisCell(tpf);
		}
		return true;
	}
	
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		
		if(!simpleUpdateVisibleCells(tpf))return false;
		
		if(bRefreshScroll)autoScroll();
		
//		updateSelectEntryRequested();
		
		if(!getInputText().startsWith(getUserEnterCustomValueToken())){
			if(!getInputText().equalsIgnoreCase(getLastFilter())){
	//			setLastFilter(getInputText());
				applyListKeyFilter();
				requestRefreshUpdateList();	//updateList();				
			}
		}
		
		updateEffectListEntries(isTryingToEnable());
//		if(isTryingToEnable()){
//			if(isEffectsDone()){ // play list effect after main one completes
////				if(btgEffectListEntries.b()){
////					if(!tdEffectListEachEntry.isActive()){
////						if(!bEffectListAllEntriesCompleted){
////							tdEffectListEachEntry.setActive(true);
////						}
////					}
////				}
//		
////				if(tdEffectListEachEntry.isActive()){ //dont use btgEffectListEntries.b() as it may be disabled during the grow effect
//				if(bRunningEffectAtAllListEntries){
//					updateEffectListEntries(isTryingToEnable());
//				}
//			}
//		}
		
		MiscLemurStateI.i().updateBlinkListBoxSelector(getMainList());//,true);
		
//		bAllowUpdateLogicalState=MiscLemurHelpersStateI.i().validatePanelUpdate(getContainerMain());
		if(bReinitBordersAfterThicknessChange){
			reinitBorders(false); //false to avoid call queue recursion
			bReinitBordersAfterThicknessChange=false;
		}
		
		return true;
	}
	
	/**
	 * default is the class name, will look like the dialog title
	 */
	@Override
	protected void updateTextInfo(){
//		lblTextInfo.setText("DIALOG for "+this.getClass().getSimpleName());
		lblTextInfo.setText(getTextInfo());
		
		MiscLemurStateI.i().fixBitmapTextLimitsFor(lblTextInfo);
	}
	
	@Override
	public R setTitle(String str) {
		super.setTitle(str);
		lblTitle.setText(str);
		return getThis();
	}
	
	/**
	 * 
	 * @return max-1 (if total 1, max index 0)
	 */
	private int getMaxIndex(){
//		return getListEntries().getVisibleItems()
		return vlVisibleEntriesList.size()-1;
//			+( ((int)getListEntries().getSlider().getModel().getMaximum()) -1);
	}
	
	private int getTopEntryIndex(){
		int iVisibleItems = getMainList().getVisibleItems();
		int iTotEntries = vlVisibleEntriesList.size();
		if(iVisibleItems>iTotEntries){
			return 0; //is not overflowing the max visible items amount
		}
		
		int iSliderInvertedIndex=(int)getMainList().getSlider().getModel().getValue();
		int iTopEntryIndex = (int)(iTotEntries -iSliderInvertedIndex -iVisibleItems);
		
		return iTopEntryIndex;
	}
	
	private int getBottomEntryIndex(){
		return getTopEntryIndex()+iVisibleRows-1;
	}
	
	private void scrollTo(int iIndex){
		//getListEntries().getSlider().getModel().getValue();
//		getListEntries().getSlider().getModel().setValue(getMaxIndex()-iIndex);
		getMainList().getSlider().getModel().setValue(
			vlVisibleEntriesList.size()-getMainList().getVisibleItems()-iIndex);
		
	}
	
	@Override
	protected boolean initKeyMappings(){
		actSimpleActions = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				boolean bControl = key.hasModifier(KeyAction.CONTROL_DOWN); //0x1
	//		boolean bShift = key.hasModifier(0x01);
				
				switch(key.getKeyCode()){
					case KeyInput.KEY_ESCAPE:
						setEnabledRequest(false);
						break;
					case KeyInput.KEY_UP:
							selectRelativeEntry(-1);
						break;
					case KeyInput.KEY_DOWN:
							selectRelativeEntry(1);
						break;
					case KeyInput.KEY_PGUP:
						selectRelativeEntry(-getMainList().getVisibleItems());
						break;
					case KeyInput.KEY_PGDN:
						selectRelativeEntry(getMainList().getVisibleItems());
						break;
					case KeyInput.KEY_HOME:
						selectRelativeEntry(-vlVisibleEntriesList.size()); //uses underflow protection
//						if(bControl)selectEntry(vlEntriesList.get(0));
						break;
					case KeyInput.KEY_END:
						selectRelativeEntry(vlVisibleEntriesList.size()); //uses overflow protection
//						if(bControl)selectEntry(vlEntriesList.get(vlEntriesList.size()-1));
						break;
					case KeyInput.KEY_DELETE:
						if(bControl){
							if(isInputToUserEnterCustomValueMode() && getInputText().isEmpty()){
								applyDefaultValueToUserModify();
							}else{
								getInputField().setText("");
							}
						}
						break;
					case KeyInput.KEY_NUMPADENTER:
					case KeyInput.KEY_RETURN:
						actionSubmit();
						break;
					case KeyInput.KEY_V: 
						if(bControl){
							MiscLemurStateI.i().insertTextAtCaratPosition(getInputField(),
								MiscI.i().retrieveClipboardString(true));
//							getInputField().setText(getInputField().getText()
//								+MiscI.i().retrieveClipboardString(true));
						}
						break;
				}
			}

		};
		
		bindKey("Navigate list to first entry", KeyInput.KEY_HOME, KeyAction.CONTROL_DOWN);
		bindKey("Navigate list to last entry", KeyInput.KEY_END,KeyAction.CONTROL_DOWN);
		bindKey("Navigate list to previous page", KeyInput.KEY_PGUP);
		bindKey("Navigate list to next page", KeyInput.KEY_PGDN);
		bindKey("Navigate list to previous entry", KeyInput.KEY_UP);
		bindKey("Navigate list to next entry", KeyInput.KEY_DOWN);
		
		bindKey("Clear the input field text", KeyInput.KEY_DELETE,KeyAction.CONTROL_DOWN);
		
		bindKey("Accept entry choice (on choice dialogs only)", KeyInput.KEY_RETURN);
		bindKey("Accept entry choice (on choice dialogs only)", KeyInput.KEY_NUMPADENTER);
		
		bindKey("Close dialog", KeyInput.KEY_ESCAPE);
		
		return true;
	}
	
	private BindKey bindKey(String strActionPerformedHelp, int iKeyCode, int... aiKeyModifiers){
		BindKey bk = MiscLemurStateI.i().bindKey(getInputField(), actSimpleActions,
			strActionPerformedHelp, iKeyCode, aiKeyModifiers);
		abkList.add(bk);
		return bk;
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,"showDialogKeyBinds"+getId(),null,"")){
//			cc.dumpSubEntry("ESC - close");
//			cc.dumpSubEntry("Up/Down/PgUp/PgDn/Ctrl+Home|End - nav. list entry");
//			cc.dumpSubEntry("Enter - accept selected choice at config dialog");
			cc.dumpSubEntry("DoubleClick - open config/accept choice at config dialog");
			
			if(abkList.size()==0){
				cc.dumpWarnEntry("open the dialog first to let keys be bound");
			}else{
				for(BindKey bk:abkList){
					cc.dumpSubEntry(bk.getHelp());
				}
			}
			
			bCommandWorked = true;
		}else
		{
			return super.execConsoleCommand(cc);
//			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
	
//	@Override
//	public String getInputText() {
//		return getInputField().getText();
//	}
	
//	@Override
//	protected R setInputText(String str) {
//		getInputField().setText(str);
//		return getThis();
//	}
	
	public R addModalDialog(LemurDialogStateAbs<T,?> diagModal){
		diagModal.setDiagParent(this);
		hmModals.put(diagModal.getId(),diagModal);
		return getThis();
	}
	
//	public DiagModalInfo<T> getDiagModalCurrent(){
//		return dmi;
//	}
	
//	public void openModalDialog(String strDialogId, DialogListEntryData<T> dataToAssignModalTo, T cmd){
	public void openModalDialog(String strDialogId, DialogListEntryData<T> dledToAssignModalTo, T cmd){
		LemurDialogStateAbs<T,?> diagModalCurrent = hmModals.get(strDialogId);
		if(diagModalCurrent!=null){
			setDiagModalInfoCurrent(new DiagModalInfo(diagModalCurrent,cmd,dledToAssignModalTo));
			diagModalCurrent.requestEnable();
		}else{
			throw new PrerequisitesNotMetException("no dialog set for id: "+strDialogId);
		}
	}
	
	public void applyResultsFromModalDialog(){
		if(getChildDiagModalInfoCurrent()==null)throw new PrerequisitesNotMetException("no modal active");
		
		getChildDiagModalInfoCurrent().getDiagModal().resetChoice();
//		dmi.getDiagModal().adataSelectedEntriesList.clear();
		setDiagModalInfoCurrent(null);
	}
	
	public boolean isListBoxEntry(Spatial spt){
		if(getSelectedIndex()>=0){
			boolean bForce=true;
			if(bForce){
				return true;
			}else{
				if(spt instanceof Panel){ //TODO the capture is actually the dialog container, can it be the listbox entry Panel?
					//TODO check if it is correcly at the ListBox
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void clearSelection() {
		selectionModel.setSelection(-1); //clear selection
//		setSelectedEntryIndex(-1);
	}
	public void setSelectedEntryIndex(int i){
		if(i<0)i=0;
		if(i>getMaxIndex())i=getMaxIndex();
		selectionModel.setSelection(i);
//		dleLastSelected = vlEntriesList.get(i);
		
		DialogListEntryData<T> dled = vlVisibleEntriesList.get(i);
		DialogListEntryData<T> dledParent = dled.getParent();
		lblSelectedEntryStatus.setText(""
			+"i="+i+", "
			+"uid="+dled.getUId()+", "
			+"puid="+(dledParent==null?"(ROOT)":dledParent.getUId())+", "
			+"'"+dled.getTextValue()+"'"
		);
		
//		LemurMiscHelpersStateI.i().bugFix(null, LemurMiscHelpersStateI.i().btgBugFixListBoxSelectorArea, getListEntries());
		MiscLemurStateI.i().listboxSelectorAsUnderline(getMainList());
	}
	public void selectEntry(DialogListEntryData<T> dledSelectRequested) {
//		this.dataSelectRequested = data;
//	}
//	
//	public void updateSelectEntryRequested() {
//		if(dataSelectRequested==null)return;
//		
		int i=vlVisibleEntriesList.indexOf(dledSelectRequested);
		if(i>=0){
			setSelectedEntryIndex(i);
//			selectionModel.setSelection(i);
//			dataSelectRequested=null;
			cd().dumpDebugEntry(getId()+",SelectIndex="+i+","+dledSelectRequested.toString());
		}else{
			throw new PrerequisitesNotMetException("data not present on the list", dledSelectRequested, getMainList());
		}
	}
	
	/**
	 * 
	 * @param iAddIndex (down), can be negative (up)
	 * @return
	 */
	public Integer selectRelativeEntry(int iAddIndex){
		Integer iSel = selectionModel.getSelection();
		
		int iMaxIndex=getMaxIndex();
		if(iMaxIndex<0)return null;
		
		if(iSel==null)iSel=0;
		
		iSel+=iAddIndex;
		
		if(iSel<0)iSel=0;
		if(iSel>iMaxIndex){
			iSel=iMaxIndex;
		}
//		
//			if(iMax>0){
//				iSel=0;
//			}else{
//				iSel=-1;
//			}
//		}
		
		setSelectedEntryIndex(iSel);
//		selectionModel.setSelection(iSel);
		bRefreshScroll=true;
		
		DialogMouseCursorListenerI.i().clearLastButtonHoverIn();
		
//		iSel = selectionModel.getSelection();
		cd().dumpDebugEntry(getId()+":"
			+"SelectedEntry="+iSel+","
			+"SliderValue="+MiscI.i().fmtFloat(getMainList().getSlider().getModel().getValue()));
		return iSel;
//		return iSel==null?-1:iSel;
	}

	public abstract boolean execTextDoubleClickActionFor(DialogListEntryData<T> dled);

	public abstract boolean execActionFor(EMouseCursorButton e, Spatial capture);

	protected void updateSelected(DialogListEntryData<T> dledPreviouslySelected){
		if(dledPreviouslySelected==null)return;
		
		int i = vlVisibleEntriesList.indexOf(dledPreviouslySelected);
		if(i>=0){
			setSelectedEntryIndex(i);//selectionModel.setSelection(i);
		}else{
			updateSelected(getAbove(dledPreviouslySelected), dledPreviouslySelected.getParent());
		}
	}
	@Override
	protected void updateSelected(final DialogListEntryData<T> dledAbove, final DialogListEntryData<T> dledParentTmp){
		/**
		 * need to wait it actually get selected
		 */
		CallQueueI.i().addCall(new CallableX(this) {
			@Override
			public Boolean call() {
				DialogListEntryData<T> dledParent = dledParentTmp;
				
				if(vlVisibleEntriesList.contains(dledAbove)){
					if(dledAbove.equals(dledParent)){
						if(!dledParent.isTreeExpanded()){ //was collapsed
							selectEntry(dledParent);
							return true;
						}
					}
					
					if(getSelectedEntryData().equals(dledAbove)){
						/**
						 * select the below one
						 * no problem if it was at the end of the list
						 */
						selectRelativeEntry(+1);
						return true;
					}
					
					selectEntry(dledAbove); //prepare to retry
					
					return false; //will retry
				}else{ //use parent
					while(true){
						if(dledParent==null)break;
						
						if(vlVisibleEntriesList.contains(dledParent)){
							/**
							 * useful when collapsing a tree branch
							 */
							selectEntry(dledParent);
							break;
						}
						
						dledParent = dledParent.getParent();
					}
					
					return true;
				}
			}
		});
	}

//	public ArrayList<DialogListEntryData<T>> getListCopy() {
//		return new ArrayList<DialogListEntryData<T>>(adleCompleteEntriesList);
//	}
	
	/**
	 * Lemur must have a chance to configure everything before we play with it.
	 * So this must happen at update and not at enable.
	 */
	private void prepareEffectListEntries(boolean bEnabling) {
//	private void prepareEffectListEntries(boolean bApplyNow) {
//		bRunningEffectAtAllListEntries=true;
		
		for(CellDialogEntry<T> cell:getVisibleCellEntries()){
			MiscLemurStateI.i().setScaleXY(cell, fMinScale, 1f);
		}
//		GridPanel gp = getMainList().getGridPanel();
//		for(int iC=0;iC<gp.getVisibleColumns();iC++){
//			for(int iR=0;iR<gp.getVisibleRows();iR++){
//				Panel pnl = gp.getCell(iR, iC);
//				if(pnl!=null)MiscLemurHelpersStateI.i().setScaleXY(pnl, fMinScale, 1f);
//			}
//		}
		
		if(bEnabling){
			bPreparedForListEntriesEffects=true;
			bRunningEffectAtAllListEntries=true;
		}
	}
	protected void updateEffectListEntries(boolean bGrow) {
		if(!btgEffectListEntries.b())return;
		if(!isTryingToEnable())return; // only actually interesting during enable
		if(!bPreparedForListEntriesEffects)prepareEffectListEntries(true);
		if(!isDialogEffectsDone())return; // play list effect after main one completes
//		if(!bPreparedForListEntriesEffects)prepareEffectListEntries(true);
		if(!bRunningEffectAtAllListEntries)return;
		
		GridPanel gp = getMainList().getGridPanel();
		
		int iTotal = gp.getVisibleColumns() * gp.getVisibleRows();
		int iCount = 0;
		
//		int iMaxConcurrent = 3;
//		int iCountConcurrent = 0;
		
		float fLastCalculatedEntryScale = 0f;
		for(CellDialogEntry<T> cell:getVisibleCellEntries()){
//		for(int iC=0;iC<gp.getVisibleColumns();iC++){
//			for(int iR=0;iR<gp.getVisibleRows();iR++){
//				Panel pnl = gp.getCell(iR, iC);
				iCount++;
				
				if(cell!=null){
					if(Float.compare(cell.getLocalScale().x,1f)==0){
						continue;
					}
					
					fLastCalculatedEntryScale=updateEffectOnEntry(cell,bGrow);
					if(fLastCalculatedEntryScale<0.33f){
						break;
					}
//					iCountConcurrent++;
					
//					fScale-=0.33f;
//					if(fScale<0f)fScale=fMinScale;
					
//					if(iCountConcurrent==iMaxConcurrent)break; //to update one entry step per frame
//				}
			}
		}
		
		if(iCount==iTotal && Float.compare(fLastCalculatedEntryScale,1f)==0){ //the last entry scale must be 1f
			bRunningEffectAtAllListEntries=false; // ended
//			tdEffectListEachEntry.setActive(false);
		}
		
//		return bRunningEffectAtAllListEntries;
	}
	
	private float updateEffectOnEntry(Spatial spt, boolean bGrow) {
		if(Float.compare(spt.getLocalScale().x,fMinScale)==0){
			AudioUII.i().play(EAudio.DisplayEntryEffect);
		}
		
//		MiscJmeI.i().user
//		TimedDelayVarField td = (TimedDelayVarField)spt.getUserData("TimedDelayEffect");
		TimedDelayVarField td = MiscJmeI.i().retrieveUserDataTimedDelay(
			spt, "tdListEntryEffect", fdvEffectListEntryDelay.f());
		
		float fScale = 1f;
		float fPerc = td.getCurrentDelayPercentual(false);
		if(Float.compare(fPerc,1f)==0){
			td.updateTime(); //prepare for next entry
		}
		fScale = fPerc;
		
		if(!bGrow)fScale=1f-fScale; //shrink
		
		MiscLemurStateI.i().setScaleXY(spt, fScale, 1f);
		
		return fScale;
	}

	public Integer getFinalEntryHeightPixels() {
		return iFinalEntryHeightPixels;
	}
	
	@Override
	protected <N extends Node> void lineWrapDisableForChildrenOf(N node) {
		ListBox<String> lstbx = (ListBox<String>)node;
		MiscLemurStateI.i().lineWrapDisableForListboxEntries(lstbx);
	}
	
	public T getCmdDummy() {
		return (T) MiscLemurStateI.i().getCmdDummy();
	}

	@Override
	public void focusGained() {
		requestRefreshUpdateList();
//		if(quaBkpMain!=null){
//			getContainerMain().setLocalRotation(quaBkpMain);
//		}
//		bugFix(null, null, btgBugFixAutoReinitBorderOnFocusGained);
		WorkAroundI.i().bugFix(btgBugFixAutoReinitBorder);
		changeResizeBorderColor(ColorRGBA.Cyan);
	}
	
//	CallableX callerReinitBordersAfterThicknessChange = new CallableX(this,1000) {
//		@Override
//		public Boolean call() {
//			reinitBorders(true);
//			return true;
//		}
//	};
	private void changeResizeBorderColor(ColorRGBA c){
		for(Button btn:abtnBorderList){
			MiscLemurStateI.i().setOverrideBackgroundColor(btn, c);
		}
	}
	
	@Override
	public void focusLost() {
		changeResizeBorderColor(ColorRGBA.Blue);
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(0,1,1), new Vector3f(1,1,0));
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(1,0,1), new Vector3f(0,1,0));
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(0,1f,1f), new Vector3f(1,0,0));
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(0,1,1), new Vector3f(1,0,0));
	}
	
	@Override
	protected void restoreDefaultPositionSize() {
		super.restoreDefaultPositionSize();
		
		if(getDialogMainContainer()!=null){
			setPositionSize(cfg.getIniPos(), cfg.getIniSize());
//			getDialogMainContainer().setLocalTranslation(cfg.getIniPos());
//			getDialogMainContainer().setPreferredSize(cfg.getIniSize());
		}
	}
	
	@Override
	protected void setPositionSize(Vector3f v3fPos, Vector3f v3fSize) {
		MiscLemurStateI.i().setPositionSafely(getDialogMainContainer(), v3fPos);
//		v3fPos.setZ(getDialogMainContainer().getLocalTranslation().getZ());
//		getDialogMainContainer().setLocalTranslation(v3fPos);
		
//		getDialogMainContainer().setPreferredSize(v3fSize);
		MiscLemurStateI.i().setSizeSafely(getDialogMainContainer(), v3fSize, true);
		
		requestRefreshUpdateList();
	}

//	public ArrayList<LemurDialogGUIStateAbs<T,?>> getParentsDialogList(LemurFocusHelperStateI.CompositeControl cc) {
//	cc.assertSelfNotNull();
	/**
	 * 
	 * @return a new list of parent's dialogs
	 */
	public ArrayList<LemurDialogStateAbs<T,?>> getParentsDialogList() {
		ArrayList<LemurDialogStateAbs<T,?>> adiag = new ArrayList<LemurDialogStateAbs<T,?>>();
		
		LemurDialogStateAbs<T,?> diag = (LemurDialogStateAbs<T,?>)super.getParentDialog();
		while(diag!=null){
			adiag.add(diag);
			diag = (LemurDialogStateAbs<T,?>)diag.getParentDialog();
		}
		
		return adiag;
	}
	
	BugFixBoolTogglerCmdField btgBugFixReinitBordersByRecreating = new BugFixBoolTogglerCmdField(this,false)
		.setHelp("this can cause minor problems too concerning mouse cursor clicks on borders, but it is more granted to make the borders work better overall");
	
	BugFixBoolTogglerCmdField btgBugFixAutoReinitBorder = new BugFixBoolTogglerCmdField(this,false)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				if(!isEnabled())return true;//this is just a successful skipper
//				if(!isEnabled()){
//					this.setQuietOnFail(true);
//					return false;
//				}
				
//				this.setQuietOnFail(false);
				reinitBorders(false);
				return true;
			}
		});
//	@Override
//	public <BFR> BFR bugFix(Class<BFR> clReturnType,
//			BFR objRetIfBugFixBoolDisabled, BoolTogglerCmdField btgBugFixId,
//			Object... aobjCustomParams
//	) {
//		if(!btgBugFixId.b())return objRetIfBugFixBoolDisabled;
//		
//		boolean bFixed = false;
//		Object objRet = null;
//		
//		if(btgBugFixAutoReinitBorderOnFocusGained.isEqualToAndEnabled(btgBugFixId)){
////			Spatial spt = MiscI.i().getParamFromArray(Spatial.class, aobjCustomParams, 0);
////			Float fZ = MiscI.i().getParamFromArray(Float.class, aobjCustomParams, 1);
//			
//			reinitBorders();
//			
//			bFixed=true;
//		}
//		
//		return MiscI.i().bugFixRet(clReturnType,bFixed, objRet, aobjCustomParams);
//	}
}