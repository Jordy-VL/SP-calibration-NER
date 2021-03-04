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
import java.util.ArrayList;


//this is a packed forest.

public abstract class PForest implements Serializable{
	
	private static final long serialVersionUID = -7565558121030500764L;
	
	protected int _height;
	protected boolean _isSrc = false;
	
	public PForest(int height){
		this._height = height;
	}
	
	public void setSrc(){
		this._isSrc = true;
	}

	public void setTgt(){
		this._isSrc = false;
	}
	
	public boolean isSrc(){
		return this._isSrc;
	}
	
	public boolean isTgt(){
		return !this._isSrc;
	}
	
	public long getRoot(){
		return this.getNode(this.getHeight(), 0);
	}
	
	public int getHeight(){
		return this._height;
	}
	
	public long getNode(int height, int width){
		return NetworkIDMapper.toForestNodeID(new int[]{height, width});
	}
	
	public abstract ArrayList<long[]> getChildren(long parent, HRule rule);
	
	public abstract ArrayList<long[]> getChildren(long parent);
	
	public abstract void viewNode(long node);
	
}