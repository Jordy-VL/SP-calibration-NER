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

public class HNode implements Serializable{
	
	private static final long serialVersionUID = -1080521940943276633L;
	
	private long _srcForestNode;
	private long _tgtForestNode;
	private HPattern _p;
	
	public HNode(long srcForestNode, long tgtForestNode, HPattern p){
		this._srcForestNode = srcForestNode;
		this._tgtForestNode = tgtForestNode;
		this._p = p;
	}
	
	public long getSrcForestNode(){
		return this._srcForestNode;
	}
	
	public long getTgtForestNode(){
		return this._tgtForestNode;
	}
	
	public HPattern getHybridPattern(){
		return this._p;
	}
	
	public long toHybridNodeId(){
		return NetworkIDMapper.toHybridNodeID(_srcForestNode, _tgtForestNode, this._p.getId());
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof HNode){
			HNode node = (HNode)o;
			return this._srcForestNode == node._srcForestNode 
					&& this._tgtForestNode == node._tgtForestNode
					&& this._p.equals(node._p);
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return (Long.valueOf(this._srcForestNode).hashCode() + 7) 
				^ (Long.valueOf(this._tgtForestNode).hashCode() + 7)
				^ (this._p.hashCode() + 7);
	}
	
	public void view(PForest src, PForest tgt){
		System.err.println();
		System.err.println("--------------------");
		src.viewNode(this._srcForestNode);
		tgt.viewNode(this._tgtForestNode);
		System.err.println(this._p);
		System.err.println("--------------------");
		System.err.println();
	}
	
}