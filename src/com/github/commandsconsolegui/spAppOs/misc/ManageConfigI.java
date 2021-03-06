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

package com.github.commandsconsolegui.spAppOs.misc;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.misc.Buffeds.BfdArrayList;
import com.github.commandsconsolegui.spAppOs.misc.ManageConfigI.IConfigure;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 */
public class ManageConfigI implements IManager<IConfigure>,IInstance{
	private static ManageConfigI instance = new ManageConfigI();
	public static ManageConfigI i(){return instance;}
	
	public void assertConfigured(IConfigure icfg){
		if(!icfg.isConfigured()){
			throw new PrerequisitesNotMetException("not configured", icfg);
		}
	}
	
	public ManageConfigI() {
		DelegateManagerI.i().addManager(this, IConfigure.class);
//		ManageSingleInstanceI.i().add(this);
	}
	
	public static interface IConfigure<T extends IConfigure<T>> {
		/**
		 * Each subclass can have the same name "CfgParm".<br>
		 * <br>
		 * Just reference the CfgParm of the superclass directly ex.:<br> 
		 * 	new ConditionalAppStateAbs.CfgParm()<br>
		 * <br>
		 * This is also very important when restarting (configuring a new and fresh robust instance)<br>
		 * where the current configuration will be just passed to the new instance!<br> 
		 */
		public static interface ICfgParm{}
		
		boolean isConfigured();
		T configure(ICfgParm icfg);
	}
	
	private BfdArrayList<IConfigure> a = new BfdArrayList<IConfigure>(){};
	
	@Override
	public boolean addHandled(IConfigure objNew) {
		return a.add(objNew);
	}

	@Override
	public BfdArrayList<IConfigure> getHandledListCopy() {
		return a.getCopy();
	}

	@Override public String getUniqueId() {return MiscI.i().prepareUniqueId(this);}

	@Override
	public boolean isInstanceReady() {
		return ManageConfigI.instance!=null;
	}

}