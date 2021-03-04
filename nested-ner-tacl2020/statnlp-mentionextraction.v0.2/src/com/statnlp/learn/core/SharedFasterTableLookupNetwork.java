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

import com.statnlp.learn.core.FeatureManager;
import com.statnlp.learn.core.TableLookupNetwork;

public class SharedFasterTableLookupNetwork extends TableLookupNetwork{
	
	private int _num_nodes;
	
	public SharedFasterTableLookupNetwork(Instance inst, FeatureManager fm, long[] nodes, int[][][] children, int num_nodes) {
		super(inst, fm, nodes, children);
		this._num_nodes = num_nodes;
	}
	
	//disable the following...
	public SharedFasterTableLookupNetwork(Instance inst, FeatureManager fm) {
		super(inst, fm);
		throw new RuntimeException("not allowed.");
	}
	
	@Override
	public int countNodes(){
		return this._num_nodes;
	}

	//remove the node k from the network.
	@Override
	public void remove(int k){
		//DO NOTHING..
	}
	
	//check if the node k is removed from the network.
	@Override
	public boolean isRemoved(int k){
		return false;
	}
	
}