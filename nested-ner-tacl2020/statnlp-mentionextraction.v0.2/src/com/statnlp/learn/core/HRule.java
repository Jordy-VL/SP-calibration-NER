/** Statistical Natural Language Processing System
    Copyright (C) 2014-2015  Lu, Wei

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.statnlp.learn.core;

import java.io.Serializable;
import java.util.Arrays;

public class HRule implements Serializable{
	
	private static final long serialVersionUID = -689823193530675003L;

	private HPattern _LHS;
	private HPattern[] _RHS;
	private Orientation _orientation;
	
	public HRule(HPattern LHS, HPattern[] RHS, Orientation orientation){
		this._LHS = LHS;
		this._RHS = RHS;
		this._orientation = orientation;
	}
	
	public HPattern getLHS(){
		return this._LHS;
	}
	
	public HPattern[] getRHS(){
		return this._RHS;
	}
	
	public Orientation getOrientation(){
		return this._orientation;
	}
	
	public boolean isNormal(){
		return this._orientation.getForm().equals("N");
	}
	
	public boolean isInvert(){
		return this._orientation.getForm().equals("I");
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof HRule){
			HRule rule = (HRule)o;
			return this._LHS.equals(rule._LHS) &&
					Arrays.equals(this._RHS, rule._RHS) &&
					this._orientation.equals(rule._orientation);
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return (this._LHS.hashCode() + 7) ^ (Arrays.hashCode(this._RHS) + 7) ^ (this._orientation.hashCode() + 7);
	}
	
	@Override
	public String toString(){
		return this._orientation + " : " + this._LHS + " => " +Arrays.toString(this._RHS);
	}
	
}
