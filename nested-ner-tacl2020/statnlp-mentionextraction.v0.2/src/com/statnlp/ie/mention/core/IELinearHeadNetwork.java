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
package com.statnlp.ie.mention.core;

import java.util.Arrays;

import com.statnlp.learn.core.NetworkIDMapper;
import com.statnlp.learn.core.TableLookupNetwork;

public class IELinearHeadNetwork extends TableLookupNetwork{

	private int _numNodes;
	
	public IELinearHeadNetwork(IELinearHeadInstance inst, IELinearHeadFeatureManager fm) {
		super(inst, fm);
	}
	
	public IELinearHeadNetwork(IELinearHeadInstance inst, IELinearHeadFeatureManager fm, long[] nodes, int[][][] children, int numNodes) {
		super(inst, fm, nodes, children);
		this._numNodes = numNodes;
	}
	
	public void finalizeNetwork(){
		super.finalizeNetwork();
		this._numNodes = super.countNodes();
	}
	
	@Override
	public int countNodes(){
		return this._numNodes;
	}

	//remove the node k from the network.
//	@Override
//	public void remove(int k){
//		//DO NOTHING..
//	}
	
	//check if the node k is removed from the network.
//	@Override
//	public boolean isRemoved(int k){
//		return false;
//	}
	
	private long[] toNodes(int[] ks){
		long[] nodes = new long[ks.length];
		for(int i = 0; i<nodes.length; i++){
			nodes[i] = this.get(ks[i]);
		}
		return nodes;
	}
	
	public boolean contains(IELinearHeadNetwork network){
//		if (true)
//		return true;
		
		if(this.countNodes() < network.countNodes()){
			return false;
		}
		int start = 0;
		for(int j = 0;j<network.countNodes(); j++){
			long node1 = network.get(j);
			int[][] children1 = network.getChildren(j);
			boolean found = false;
			for(int k = start; k<this.countNodes() ; k++){
				long node2 = this.get(k);
				int[][] children2 = this.getChildren(k);
				if(node1==node2){
					
					for(int[] child1 : children1){
						long[] child1_nodes = network.toNodes(child1);
						boolean child_found = false;
						for(int[] child2 : children2){
							long[] child2_nodes = this.toNodes(child2);
							if(Arrays.equals(child1_nodes, child2_nodes)){
								child_found = true;
							}
						}
						if(!child_found){
							System.err.println(Arrays.toString(child1_nodes)+"\t"+children1.length);
							System.err.println(Arrays.toString(this.toNodes(children2[0]))+"\t"+children2.length);
//							throw new RuntimeException(NetworkIDMapper.viewHybridNode_ie(node1, 100, 100));
							return false;
						}
					}
					
					
					found = true;
					start = k;
					break;
				}
			}
			if(!found)
				return false;
		}
		return true;
	}
}