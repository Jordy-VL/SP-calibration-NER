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

public class FeatureArray implements Serializable{
	
	private static final long serialVersionUID = 9170537017171193020L;
	
	private int _version;
	private double _score;
	private int[] _curr;
	private FeatureArray[] _next;
	private NetworkParam _param;
	
	public FeatureArray(NetworkParam param){
		this(new int[0], param);
	}
	
	public FeatureArray(int[] curr, NetworkParam param) {
		this._curr = curr;
		this._param = param;
		this._version = -1;
	}
	
	public FeatureArray(int[] curr, FeatureArray next){
		this._curr = curr;
		this._next = new FeatureArray[]{next};
		this._param = next._param;
		this._version = -1;
	}

	public FeatureArray(int[] curr, FeatureArray[] next){
		this._curr = curr;
		this._next = next;
		this._param = next[0]._param;//assume they share the same param.
		this._version = -1;
	}
	
	public int[] getCurrent(){
		return this._curr;
	}
	
	public void update(double count){
		int[] features = this.getCurrent();
		for(int feature : features){
			this._param.addCount(feature, count);
		}
		if(this.getNext()!=null){
			FeatureArray[] fas = this.getNext();
			for(FeatureArray fa : fas){
				fa.update(count);
			}
		}
	}
	
	public FeatureArray[] getNext(){
		return this._next;
	}
	
	public double getScore(){
		if(this._version == this._param.getVersion())
			return this._score;
		
		if(this.getNext()==null){
			this._score = this.computeScore(this.getCurrent());
			this._version = this._param.getVersion();
			return this._score;
		}
		
		this._score = this.computeScore(this.getCurrent());
		FeatureArray[] fas = this.getNext();
		for(FeatureArray fa : fas)
			this._score += fa.getScore();
		
		this._version = this._param.getVersion();
		return this._score;
	}
	
	private double computeScore(int[] fs){
		double score = 0.0;
		for(int f : fs){
			if(f!=-1)
				score += this._param.getWeight(f);
		}
		return score;
	}
	
}
