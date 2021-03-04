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

import java.util.HashMap;

public abstract class NetworkBuilder {
	
	protected HInstance _inst;
	protected FeatureManager _fm;
	protected HGrammar _grammar;
	protected Network _network;

	protected PForest _src;
	protected PForest _tgt;
	
	//cache
	private HashMap<Integer, Network> _networks_cache;
	
	public NetworkBuilder(FeatureManager fm){
		this._fm = fm;
		this._grammar = fm.getHybridGrammar();
		this._networks_cache = new HashMap<Integer, Network>();
	}
	
	public HGrammar getHybridGrammar(){
		return this._grammar;
	}
	
	public FeatureManager getFeatureManager(){
		return this._fm;
	}
	
	public Network toNetwork(HInstance inst){
		if(this._networks_cache.containsKey(inst.getId())){
			return this._networks_cache.get(inst.getId());
		}
		this.set(inst);
		Network network = this.build();
		this._networks_cache.put(inst.getId(), network);
		return network;
	}
	
	private void set(HInstance inst){
		this._inst = inst;
		this._src = this._inst.getSrc();
		this._tgt = this._inst.getTgt();
		this._src.setSrc();
		this._tgt.setTgt();
	}
	
	protected abstract Network build();
	
	protected abstract long build(long src, long tgt, HPattern p);
	
}