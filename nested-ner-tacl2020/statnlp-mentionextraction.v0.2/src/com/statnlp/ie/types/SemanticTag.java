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
package com.statnlp.ie.types;

import com.statnlp.learn.core.Tag;

public abstract class SemanticTag extends Tag implements Comparable<SemanticTag>{
	
	private static final long serialVersionUID = -409478314050611880L;
	
	protected int _id;
	
	public SemanticTag(String name){
		super(name);
	}
	
	public int getId(){
		return this._id;
	}
	
	public void setId(int id){
		this._id = id;
	}
	
	@Override
	public int hashCode(){
		return this._name.hashCode() + 7;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof SemanticTag){
			SemanticTag tag = (SemanticTag)o;
			return this._name.equals(tag._name);
		}
		return false;
	}
	
}