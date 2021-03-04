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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public abstract class TableLookupNetwork extends Network{
	
	private static final long serialVersionUID = -7250820762892368213L;
	
	//temporary data structures used when constructing the network
	private transient HashSet<Long> _nodes_tmp;
	private transient HashMap<Long, ArrayList<long[]>> _children_tmp;
	
	//at each index, store the node's ID
	protected long[] _nodes;
	//at each index, store the node's list of children's indices (with respect to _nodes)
	protected int[][][] _children;
	
	//the constructor
	public TableLookupNetwork(Instance inst, FeatureManager fm){
		super(inst, fm);
		this._nodes_tmp = new HashSet<Long>();
		this._children_tmp = new HashMap<Long, ArrayList<long[]>>();
	}
	
	//the constructor
	public TableLookupNetwork(Instance inst, FeatureManager fm, long[] nodes, int[][][] children){
		super(inst, fm);
		this._nodes = nodes;
		this._children = children;
	}
	
	@Override
	protected long getNode(int k){
		return this._nodes[k];
	}
	
	@Override
	protected int[][] getChildren(int k){
		return this._children[k];
	}
	
	public int countTmpNodes_tmp(){
		return this._nodes_tmp.size();
	}
	
	public long[] getAllNodes(){
		return this._nodes;
	}
	
	public int[][][] getAllChildren(){
		return this._children;
	}
	
	@Override
	public int countNodes() {
		return this._nodes.length;
	}
	
	//remove the node k from the network.
	public void remove(int k){
		if(true){
//			throw new RuntimeException("x"+NetworkIDMapper.viewHybridNode2(this._nodes[k]));
		}
		this._nodes[k] = -1;
		if (this._inside!=null)
		this._inside[k] = Double.NEGATIVE_INFINITY;
		if (this._outside!=null)
		this._outside[k] = Double.NEGATIVE_INFINITY;
	}
	
	//check if the node k is removed from the network.
	public boolean isRemoved(int k){
		return this._nodes[k] == -1;
	}
	
	public boolean contains(long node){
		return this._nodes_tmp.contains(node);
	}
	
	//add one cell to the network.
	public boolean addNode(long node){
		if(this._nodes_tmp.contains(node))
			return false;
//			throw new NetworkException("The node is already added:"+node);
		this._nodes_tmp.add(node);
		return true;
	}
	
	public void finalizeNetwork(){
//		System.err.println(this._nodes_tmp.size()+"<<<");
		Iterator<Long> node_ids = this._nodes_tmp.iterator();
		ArrayList<Long> values = new ArrayList<Long>();
		while(node_ids.hasNext()){
			values.add(node_ids.next());
		}
		this._nodes = new long[this._nodes_tmp.size()];
		HashMap<Long, Integer> nodesValue2IdMap = new HashMap<Long, Integer>();
		Collections.sort(values);
		for(int k = 0 ; k<values.size(); k++){
			this._nodes[k] = values.get(k);
			nodesValue2IdMap.put(this._nodes[k], k);
		}
		
		this._nodes_tmp = null;
		this._children = new int[this._nodes.length][][];
		
		Iterator<Long> parents = this._children_tmp.keySet().iterator();
		while(parents.hasNext()){
			long parent = parents.next();
			int parent_index = nodesValue2IdMap.get(parent);
			ArrayList<long[]> childrens = this._children_tmp.get(parent);
			if(childrens==null){
				this._children[parent_index] = new int[1][0];
			} else {
				this._children[parent_index] = new int[childrens.size()][];
				for(int k = 0 ; k <this._children[parent_index].length; k++){
					long[] children = childrens.get(k);
					int[] children_index = new int[children.length];
					for(int m = 0; m<children.length; m++){
						children_index[m] = nodesValue2IdMap.get(children[m]);
					}
					this._children[parent_index][k] = children_index;
				}
			}
		}
		for(int k = 0 ; k<this._children.length; k++){
			if(this._children[k]==null){
				this._children[k] = new int[1][0];
			}
		}
		this._children_tmp = null;
	}
	
	private void checkLinkValidity(long parent, long[] children){
		/**/
		for(long child : children){
			if(child >= parent){
				System.err.println(Arrays.toString(NetworkIDMapper.toHybridNodeArray(parent)));
				System.err.println(Arrays.toString(NetworkIDMapper.toHybridNodeArray(children[0])));
				System.err.println();
				throw new NetworkException("This link seems to be invalid:"+parent+"\t"+Arrays.toString(children));
			}
		}
		/**/
		
		this.checkNodeValidity(parent);
		for(long child : children){
			this.checkNodeValidity(child);
		}
	}
	
	private void checkNodeValidity(long node){
		if(!this._nodes_tmp.contains(node)){
			throw new NetworkException("This node seems to be invalid:"+Arrays.toString(NetworkIDMapper.toHybridNodeArray(node)));
		}
	}

	//add the links. only do this after the cells are added.
	public void addEdge(long parent, long[] children){
		this.checkLinkValidity(parent, children);
		if(!this._children_tmp.containsKey(parent)){
			this._children_tmp.put(parent, new ArrayList<long[]>());
		}
		ArrayList<long[]> existing_children = this._children_tmp.get(parent);
		for(int k = 0; k<existing_children.size(); k++){
			if(Arrays.equals(existing_children.get(k), children)){
				throw new NetworkException("This children is already added. Add again???");
			}
		}
		existing_children.add(children);
	}
	
	@Override
	public boolean isRoot(int k){
		return this.countNodes()-1 == k;
	}
	
	@Override
	public boolean isLeaf(int k){
		int[][] v= this._children[k];
		if(v.length==0) return false;
		if(v[0].length==0) return true;
		return false;
	}
	
	public long get(int k){
		return this._nodes[k];
	}
	
	public int countInValidNodes(){
		int count = 0;
		for(int k = 0; k<this._nodes.length; k++){
			if(this._inside[k]==Double.NEGATIVE_INFINITY || this._outside[k]==Double.NEGATIVE_INFINITY){
				count++;
			}
		}
		return count;
	}

//	public String toString_ie(int N){
//		StringBuilder sb = new StringBuilder();
//		sb.append("nodes:");
//		sb.append('[');
//		sb.append('\n');
//		for(int k = 0; k<this._nodes.length; k++){
//			sb.append(NetworkIDMapper.viewHybridNode_ie(this._nodes[k], N));
//			sb.append('\n');
//		}
//		sb.append(']');
//		sb.append('\n');
//		sb.append("links:");
//		sb.append('[');
//		sb.append('\n');
//		for(int k = 0; k<this._children.length; k++){
//			sb.append('<');
//			long parent = this._nodes[k];
////			sb.append(Arrays.toString(NetworkIDMapper.toHybridNodeArray(parent)));
//			sb.append(NetworkIDMapper.viewHybridNode_ie(parent, N));
//			int[][] childrenList = this._children[k];
//			for(int i = 0; i<childrenList.length; i++){
//				sb.append('\n');
//				sb.append('\t');
//				sb.append('(');
//				int[] children = childrenList[i];
//				for(int j = 0; j<children.length; j++){
//					sb.append('\n');
////					sb.append('\t'+Arrays.toString(NetworkIDMapper.toHybridNodeArray(this._nodes[children[j]])));
//					sb.append('\t'+NetworkIDMapper.viewHybridNode_ie(this._nodes[children[j]], N));
//				}
//				sb.append('\n');
//				sb.append('\t');
//				sb.append(')');
//			}
//			sb.append('>');
//			sb.append('\n');
//		}
//		sb.append(']');
//		sb.append('\n');
//		
//		return sb.toString();
//	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("nodes:");
		sb.append('[');
		sb.append('\n');
		for(int k = 0; k<this._nodes.length; k++){
			sb.append(Arrays.toString(NetworkIDMapper.toHybridNodeArray(this._nodes[k])));
			sb.append('\n');
		}
		sb.append(']');
		sb.append('\n');
		sb.append("links:");
		sb.append('[');
		sb.append('\n');
		for(int k = 0; k<this._children.length; k++){
			sb.append('<');
			long parent = this._nodes[k];
			sb.append(Arrays.toString(NetworkIDMapper.toHybridNodeArray(parent)));
			int[][] childrenList = this._children[k];
			for(int i = 0; i<childrenList.length; i++){
				sb.append('\n');
				sb.append('\t');
				sb.append('(');
				int[] children = childrenList[i];
				for(int j = 0; j<children.length; j++){
					sb.append('\n');
					sb.append('\t'+Arrays.toString(NetworkIDMapper.toHybridNodeArray(this._nodes[children[j]])));
				}
				sb.append('\n');
				sb.append('\t');
				sb.append(')');
			}
			sb.append('>');
			sb.append('\n');
		}
		sb.append(']');
		sb.append('\n');
		
		return sb.toString();
	}

	//for serialization.
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeInt(this._nodes.length);
		for(int k = 0; k<this._nodes.length; k++){
			out.writeLong(this._nodes[k]);
			out.writeInt(this._children[k].length);
			for(int i = 0; i<this._children[k].length; i++){
				out.writeInt(this._children[k][i].length);
				for(int j = 0; j<this._children[k][i].length; j++){
					out.writeInt(this._children[k][i][j]);
				}
			}
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException{
		this._nodes = new long[in.readInt()];
		this._children = new int[this._nodes.length][][];
		for(int k = 0; k<this._nodes.length; k++){
			this._nodes[k] = in.readLong();
			this._children[k] = new int[in.readInt()][];
			for(int i = 0; i<this._children[k].length; i++){
				this._children[k][i] = new int[in.readInt()];
				for(int j = 0; j<this._children[k][i].length; j++){
					this._children[k][i][j] = in.readInt();
				}
			}
		}
	}
	
}
