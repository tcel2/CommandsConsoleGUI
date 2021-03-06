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
package com.github.commandsconsolegui.spJme.misc;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.DebugData;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.EDbgStkOrigin;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.RunMode;
import com.github.commandsconsolegui.spJme.ManageMouseCursorI;
import com.github.commandsconsolegui.spJme.misc.ManageEffectsJmeStateI.IEffect;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <THIS>
 */
public abstract class EffectBaseAbs<THIS extends EffectBaseAbs> implements IEffect<THIS>{
	private DebugData dbg;
	private Vector3f	v3fFrom;
	private Vector3f	v3fTo;
	private Vector3f	v3fFollowFromDisplacement;
	private Vector3f	v3fFollowToDisplacement;
	private Spatial sptFollowFrom;
	private Spatial sptFollowTo;
	private boolean	bToMouse;
	private boolean	bDiscarded;
	private long	iHoldUntilMilis;
	private Vector3f	v3fHoldPreviousFrom=new Vector3f();
	private Vector3f	v3fHoldPreviousTo=new Vector3f();
	private int	iMaxHoldMilis = 1000;
	
	private ColorRGBA	colorRefDefault=ColorRGBA.White.clone();
	private ColorRGBA	colorRefBase;
	private Node	nodeParent;
	private Geometry	geom;
	private Object	objOwner;
	private boolean	bPlay = false;
	private boolean	bDiscardingByOwner=true;
	
	public EffectBaseAbs(Object objOwner){
		this();
		setOwner(objOwner);
	}
	
	public EffectBaseAbs() {
		if(RunMode.bValidateDevCode)dbg=ManageDebugDataI.i().setStack(dbg, EDbgStkOrigin.Constructed);
		
		this.geom = new Geometry("Geom:"+this.getClass().getSimpleName());
		Mesh mesh = new Mesh();
		mesh.setStreamed();
		this.geom.setMesh(mesh);
		
		DelegateManagerI.i().addHandled(this);
	}
	
	public Vector3f getLocationFrom() {
		Vector3f v3fTargetSpot = v3fFrom==null?null:v3fFrom.clone();
		if(sptFollowFrom!=null){
			//TODO if sptFollow is a node, add a node to it and apply displacement to let rotations etc apply
			v3fTargetSpot=sptFollowFrom.getWorldTranslation().add(v3fFollowFromDisplacement);
		}
		return v3fTargetSpot;
	}
	
	public Vector3f getLocationTo() {
		Vector3f v3fTargetSpot = v3fTo==null?null:v3fTo.clone();
		if(bToMouse){
			v3fTargetSpot=ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f();
			v3fTargetSpot.z=v3fTo.z;
		}else
		if(sptFollowTo!=null){
			//TODO if sptFollow is a node, add a node to it and apply displacement to let rotations etc apply
			v3fTargetSpot=sptFollowTo.getWorldTranslation().add(v3fFollowToDisplacement);
		}
		return v3fTargetSpot;
	}

	public Vector3f getV3fFrom() {
		return v3fFrom;
	}

	public void setV3fFrom(Vector3f v3fFrom) {
		this.v3fFrom = v3fFrom;
	}

	public Vector3f getV3fTo() {
		return v3fTo;
	}

	public void setV3fTo(Vector3f v3fTo) {
		this.v3fTo = v3fTo;
	}

	public Vector3f getV3fFollowFromDisplacement() {
		return v3fFollowFromDisplacement;
	}

	public void setV3fFollowFromDisplacement(Vector3f v3fFollowFromDisplacement) {
		this.v3fFollowFromDisplacement = v3fFollowFromDisplacement;
	}

	public Vector3f getV3fFollowToDisplacement() {
		return v3fFollowToDisplacement;
	}

	public void setV3fFollowToDisplacement(Vector3f v3fFollowToDisplacement) {
		this.v3fFollowToDisplacement = v3fFollowToDisplacement;
	}

	public Spatial getSptFollowFrom() {
		return sptFollowFrom;
	}

	public void setSptFollowFrom(Spatial sptFollowFrom) {
		this.sptFollowFrom = sptFollowFrom;
	}

	public Spatial getSptFollowTo() {
		return sptFollowTo;
	}

	public void setSptFollowTo(Spatial sptFollowTo) {
		this.sptFollowTo = sptFollowTo;
	}

	public boolean isbToMouse() {
		return bToMouse;
	}

	public void setbToMouse(boolean bToMouse) {
		this.bToMouse = bToMouse;
	}

	public void assertNotDiscarded(){
		if(bDiscarded){
			throw new PrerequisitesNotMetException("cant work with a discarded effect");
		}
	}
	
	@Override
	public THIS setFromTo(Vector3f v3fFrom, Vector3f v3fTo){
		assertNotDiscarded();
		this.v3fFrom=PrerequisitesNotMetException.assertNotNull(v3fFrom,this); 
		this.v3fTo=PrerequisitesNotMetException.assertNotNull(v3fTo,this);
		return getThis();
	}
	
	@Override
	public THIS setFollowToMouse(boolean b){
		assertNotDiscarded();
		this.bToMouse=b;
		return getThis();
	}
	
	@Override
	public THIS setFollowFromTarget(Spatial spt, Vector3f v3fDisplacement){
		assertNotDiscarded();
		sptFollowFrom=spt;
		v3fFollowFromDisplacement = v3fDisplacement==null?new Vector3f():v3fDisplacement;
		return getThis();
	}
	
	@Override
	public THIS setFollowToTarget(Spatial spt, Vector3f v3fDisplacement){
		if(RunMode.bValidateDevCode)dbg=ManageDebugDataI.i().setStack(dbg);
		assertNotDiscarded();
		sptFollowTo=spt;
		v3fFollowToDisplacement = v3fDisplacement==null?new Vector3f():v3fDisplacement;
		return getThis();
	}
	
	@Override
	public THIS setColor(ColorRGBA colorRef){
		this.colorRefBase = colorRef!=null ? colorRef : colorRefDefault;
		this.geom.setMaterial(MiscJmeI.i().retrieveMaterialUnshadedColor(colorRef));
		return getThis();
	}
	@Override
	public THIS setNodeParent(Node node){
		this.nodeParent=node;
		return getThis();
	}
	
	@Override
	public Object getOwner() {
		assertNotDiscarded();
		return objOwner;
	}
	
	@Override
	public THIS setPlay(boolean b) {
		assertNotDiscarded();
		this.bPlay=b;
		if(!this.bPlay){
			if(geom!=null)geom.removeFromParent();
		}
		return getThis();
	}
	
	@Override
	public THIS setAsDiscarded() {
		if(RunMode.bValidateDevCode)dbg=ManageDebugDataI.i().setStack(dbg, EDbgStkOrigin.Discarded);
		setPlay(false);
		if(geom!=null)geom.removeFromParent();
		bDiscarded=true;
		return getThis();
	}

	@Override
	public void assertConfigIsValid() {
		assertNotDiscarded();
		if(!isPlaying())return; //not redundant when called directly after adding the effect
		
		if(colorRefBase==null){
			setColor(colorRefDefault);
		}
		
		if(v3fFrom==null && sptFollowFrom==null){
			throw new PrerequisitesNotMetException("playing and 'from' not set",this);
		}
		
		if(v3fTo==null && sptFollowTo==null && !bToMouse){
			throw new PrerequisitesNotMetException("playing and 'to' not set",this);
		}
		
		if(nodeParent==null){
			if (objOwner instanceof ILinkedSpatial) {
				ILinkedSpatial ils = (ILinkedSpatial) objOwner;
				// the top node
				nodeParent=(Node)MiscJmeI.i().getParentestFrom(ils.getLinkedSpatial(),false); 
			}else{
				if (objOwner instanceof Spatial) {
					Spatial spt = (Spatial) objOwner;
					nodeParent=(Node)MiscJmeI.i().getParentestFrom(spt,false);
				}
			}
			
			PrerequisitesNotMetException.assertNotNull(nodeParent, "parent", objOwner, this);
		}
	}
	
	@Override
	public boolean isPlaying() {
		return bPlay;
	}

	@Override
	public THIS setSkipDiscardingByOwner() {
		bDiscardingByOwner=false;
		return getThis();
	}
	
	@Override
	public boolean isDiscardingByOwner() {
		return bDiscardingByOwner;
	}
	
	@Override
	public THIS setOwner(Object objOwner) {
		this.objOwner=objOwner;
		return getThis();
	}

	@Override
	public THIS clone() {
		try {
			return (THIS)this.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new PrerequisitesNotMetException(e);
		}
	}

	public long getiHoldUntilMilis() {
		return iHoldUntilMilis;
	}

	public void setiHoldUntilMilis(long iHoldUntilMilis) {
		this.iHoldUntilMilis = iHoldUntilMilis;
	}

	public Vector3f getV3fHoldPreviousFrom() {
		return v3fHoldPreviousFrom;
	}

	public void setV3fHoldPreviousFrom(Vector3f v3fHoldPreviousFrom) {
		this.v3fHoldPreviousFrom = v3fHoldPreviousFrom;
	}

	public Vector3f getV3fHoldPreviousTo() {
		return v3fHoldPreviousTo;
	}

	public void setV3fHoldPreviousTo(Vector3f v3fHoldPreviousTo) {
		this.v3fHoldPreviousTo = v3fHoldPreviousTo;
	}

	public int getiMaxHoldMilis() {
		return iMaxHoldMilis;
	}

	public void setiMaxHoldMilis(int iMaxHoldMilis) {
		this.iMaxHoldMilis = iMaxHoldMilis;
	}

	public boolean isbPlay() {
		return bPlay;
	}

	public void setbPlay(boolean bPlay) {
		this.bPlay = bPlay;
	}

	public Node getNodeParent() {
		return nodeParent;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	@Override
	public void play(ManageEffectsJmeStateI.CompositeControl cc, float tpf) {
		cc.assertSelfNotNull();
		assertNotDiscarded();
		if(!isbPlay())return;
		
		if(!getNodeParent().hasChild(getGeom()))getNodeParent().attachChild(this.getGeom());
		
		playWork();
		
		getGeom().setLocalTranslation(getLocationFrom());
	}

	protected abstract void playWork();
}
