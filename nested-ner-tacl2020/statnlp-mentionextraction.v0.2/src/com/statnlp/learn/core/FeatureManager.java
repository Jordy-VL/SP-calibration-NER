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

public abstract class FeatureManager {
	
	protected NetworkParam _param;
	protected Network _network;
	protected HGrammar _g;

	public FeatureManager(HGrammar g, NetworkParam param){
		this._g = g;
		this._param = param;
	}
	
	public NetworkParam getParam(){
		return this._param;
	}
	
	public Network getNetwork(){
		return this._network;
	}
	
	public HGrammar getHybridGrammar(){
		return this._g;
	}
	
	public abstract void setNetwork(Network network);
	
	public abstract FeatureArray extract(int parent_k, int[] children_k);
	
}