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

package com.github.commandsconsolegui.spCmd.varfield;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public enum EType{
	Int,
	Long,
	Float,
	Double,
	String,
	Boolean,
	Var,
	;
	
	public static EType forClass(Class clValue) throws UnsupportedOperationException{ //@STATIC_OK
		EType e = null;
		if(clValue==Float.class		|| clValue==float.class		){e=EType.Float;}else
		if(clValue==Double.class	|| clValue==double.class	){e=EType.Double;}else
		if(clValue==Integer.class	|| clValue==int.class			){e=EType.Int;}else
		if(clValue==Long.class		|| clValue==long.class		){e=EType.Long;}else
		if(clValue==Boolean.class	|| clValue==boolean.class	){e=EType.Boolean;}else
		if(clValue==String.class														){e=EType.String;}else
		if(VarCmdFieldAbs.class.isAssignableFrom(clValue)		){e=EType.Var;}else
		{
			throw new UnsupportedOperationException("unsupported value class type "+clValue.getName());
		}
		
		return e;
	}
	
}
