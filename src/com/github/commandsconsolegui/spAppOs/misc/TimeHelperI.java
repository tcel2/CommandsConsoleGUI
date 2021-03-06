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

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class TimeHelperI {
	private static TimeHelperI instance = new TimeHelperI();
	public static TimeHelperI i(){return instance;}
	
	private long lNanoOneSecond  = 1000000000L;
	private long lMilisToNano = 1000000L;
	private double dNanoToSeconds = 1.0/lNanoOneSecond;
	
	public long secondsToNano(double dSeconds){
		return (long) (dSeconds*lNanoOneSecond);
	}
	public long secondsToNano(float fSeconds){
		return (long) (fSeconds*lNanoOneSecond);
	}
	public long nanoToMilis(long lTimeNano){
		return lTimeNano/lMilisToNano;
	}
	public long milisToNano(long lTimeMilis){
		return lTimeMilis*lMilisToNano;
	}
	public double nanoToSeconds(long lNano) {
		return lNano*dNanoToSeconds;
	}
	
//	public double convertDelayNanoToSeconds(long lDelayNano){
//		return (double)((double)lDelayNano/(double)lNano); //TODO is that?
//	}
}
