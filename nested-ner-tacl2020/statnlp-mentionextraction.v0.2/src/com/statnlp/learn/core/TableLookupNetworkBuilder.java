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

import java.util.ArrayList;

public abstract class TableLookupNetworkBuilder extends NetworkBuilder{
	
	public TableLookupNetworkBuilder(FeatureManager fm) {
		super(fm);
	}
	
	@Override
	protected abstract TableLookupNetwork build();
	
	@Override
	protected long build(long srcForestNode, long tgtForestNode, HPattern p) {
		long r = this.build_node(new HNode(srcForestNode, tgtForestNode, p));
		return r;
	}
	
	private long build_node(HNode currentHybridNode){
		long currentHybridNodeId = currentHybridNode.toHybridNodeId();
		
		if(this._network.contains(currentHybridNodeId))
			return currentHybridNodeId;
		
		boolean isValidHybridNode = false;
		
		long srcForestNode = currentHybridNode.getSrcForestNode();
		long tgtForestNode = currentHybridNode.getTgtForestNode();
		HPattern hp = currentHybridNode.getHybridPattern();
		ArrayList<long[]> tgtChildrenList = this._tgt.getChildren(tgtForestNode);
		
		ArrayList<HRule> ruleList;

		if(hp.isXX()){
			for(long[] tgtChildren : tgtChildrenList){
				long tgtChildForestNode = tgtChildren[0];
				long childHybridNode = build(srcForestNode, tgtChildForestNode, this._grammar.getZZ());
				if(childHybridNode!=-1){
					isValidHybridNode = true;
					((TableLookupNetwork)this._network).addNode(currentHybridNodeId);
					((TableLookupNetwork)this._network).addEdge(currentHybridNodeId, new long[]{childHybridNode});
				}
			}
		}
		
		if(hp.isYY()){
			for(long[] tgtChildren : tgtChildrenList){
				long tgtChildForestNode = tgtChildren[1];
				long childHybridNode = build(srcForestNode, tgtChildForestNode, this._grammar.getZZ());
				if(childHybridNode!=-1){
					isValidHybridNode = true;
					((TableLookupNetwork)this._network).addNode(currentHybridNodeId);
					((TableLookupNetwork)this._network).addEdge(currentHybridNodeId, new long[]{childHybridNode});
				}
			}
		}

		if(hp.isXY()){
			for(long[] tgtChildren : tgtChildrenList){
				long tgtChildForestNode = tgtChildren[1];
				long childHybridNode = build(srcForestNode, tgtChildForestNode, this._grammar.getZZ());
				if(childHybridNode!=-1){
					isValidHybridNode = true;
					((TableLookupNetwork)this._network).addNode(currentHybridNodeId);
					((TableLookupNetwork)this._network).addEdge(currentHybridNodeId, new long[]{childHybridNode});
				}
			}
		}

		if(hp.isYX()){
			for(long[] tgtChildren : tgtChildrenList){
				long tgtChildForestNode = tgtChildren[0];
				long childHybridNode = build(srcForestNode, tgtChildForestNode, this._grammar.getZZ());
				if(childHybridNode!=-1){
					isValidHybridNode = true;
					((TableLookupNetwork)this._network).addNode(currentHybridNodeId);
					((TableLookupNetwork)this._network).addEdge(currentHybridNodeId, new long[]{childHybridNode});
				}
			}
		}
		ruleList = this._grammar.getRules(hp);
		
		for(HRule rule : ruleList){
			ArrayList<HNode[]> childHybridNodesList = this.build_rule(currentHybridNode, rule);
			
			for(HNode[] childHybridNodes : childHybridNodesList){
				
				boolean foundValidChildren = true;
				long[] childHybridNodesId = new long[childHybridNodes.length];
				for(int k = 0; k<childHybridNodes.length; k++){
					
					long node = this.build_node(childHybridNodes[k]);
					if(node==-1){
						foundValidChildren = false;
						break;
					} else {
						childHybridNodesId[k] = node;
					}
				}
				if(childHybridNodes.length == 0){
					foundValidChildren = false;
				}
				if(foundValidChildren){
					isValidHybridNode = true;
					((TableLookupNetwork)this._network).addNode(currentHybridNodeId);
					((TableLookupNetwork)this._network).addEdge(currentHybridNodeId, childHybridNodesId);
				}
			}
		}
		
		if(hp.isTerminal()){
			if(this._src.getChildren(srcForestNode)==null || this._src.getChildren(srcForestNode).size() == 0){
				isValidHybridNode = true;
				((TableLookupNetwork)this._network).addNode(currentHybridNodeId);
			}
		}
		
		if(isValidHybridNode){
//			System.err.println("OK:"+((TableLookupNetwork)this._network).countTmpNodes_tmp());
//			currentHybridNode.view(this._src, this._tgt);
			return currentHybridNodeId;
		}
		
		return -1;
	}
	
	private ArrayList<HNode[]> build_rule(HNode currentNode, HRule rule){
		
		ArrayList<HNode[]> children = new ArrayList<HNode[]>();
		
		ArrayList<long[]> srcChildrenList = this._src.getChildren(currentNode.getSrcForestNode(), rule);
		ArrayList<long[]> tgtChildrenList = this._tgt.getChildren(currentNode.getTgtForestNode(), rule);
		
		int arity = rule.getRHS().length;
		
		for(long[] srcChildren : srcChildrenList){
			for(long[] tgtChildren: tgtChildrenList){
				HNode childNodes[] = new HNode[arity];
				for(int k = 0; k<arity; k++){
					childNodes[k] = new HNode(srcChildren[k], tgtChildren[k], rule.getRHS()[k]);
				}
				children.add(childNodes);
			}
		}
		
		return children;
	}
	
}



