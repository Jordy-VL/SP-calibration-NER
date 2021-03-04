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
import java.util.Arrays;

public abstract class Network implements Serializable{
	
	private static final long serialVersionUID = -3630379919120581209L;
	
	//the instance
	protected transient Instance _inst;
	//the feature manager
	protected transient FeatureManager _fm;
	//whether we shall cache the features or not
	private transient boolean _cacheFeatures = false;
	//the cached features
	private transient FeatureArray[][] _featureCache;
	
	//at each index, store the node's inside score
	protected transient double[] _inside;
	//at each index, store the node's outside score
	protected transient double[] _outside;
	//at each index, store the score of the max tree
	protected transient double[] _max;
	//this stores the paths associated with the above tree
	protected transient int[][] _max_paths;
	
	public Network(Instance inst, FeatureManager fm){
		this._inst = inst;
		this._fm = fm;
	}
	
	public int countRemovedNodes(){
		int count = 0;
		for(int k = 0; k<this.countNodes(); k++)
			if(this.isRemoved(k))
				count++;
		return count;
	}
	
	public Instance getInstance(){
		return this._inst;
	}
	
	//get the inside score for the root node.
	public double getInside(){
		return this._inside[this._inside.length-1];
	}
	
	public double getMax(){
		return this._max[this._max.length-1];
	}

	public double getMax(int k){
		return this._max[k];
	}
	
	public int[] getPath(int k){
		return this._max_paths[k];
	}
	
	//train the network.
	public void train(){
		this._fm.setNetwork(this);
		this.inside();
		this.outside();
		this.update();
		this._fm.getParam().addObj(this.getInside());
	}
	
	protected void inside(){
//		System.err.println("BEFORE:"+Arrays.toString(NetworkIDMapper.toHybridNodeArray(this.getNode(this.countNodes()-1))));
		long time = System.currentTimeMillis();
		if(this._inside==null){
			this._inside = new double[this.countNodes()];
		} else {
			Arrays.fill(this._inside, 0.0);
		}
		for(int k=0; k<this.countNodes(); k++){
//			System.err.println(this.getNode(k));
			this.inside(k);
		}
		time = System.currentTimeMillis() - time;
//		long root = this.getRoot();
//		System.err.println(NetworkIDMapper.viewHybridNode_ie(root, 100, 100));
		if(this.getInside()==Double.NEGATIVE_INFINITY){
			throw new RuntimeException("Error! This instance has zero inside score!");
		}
//		System.err.println("INSIDE TIME:"+time+" ms\t"+Math.exp(this.getInside())+"\t"+this.countNodes()+Arrays.toString(NetworkIDMapper.toHybridNodeArray(this.getNode(this.countNodes()-1))));
	}
	
	protected void outside(){
//		long time = System.currentTimeMillis();
		if(this._outside==null){
			this._outside = new double[this.countNodes()];
//			for(int k=this.countNodes()-1; k>=0; k--){
//				this._outside[k] = Double.NEGATIVE_INFINITY;
//			}
			Arrays.fill(this._outside, Double.NEGATIVE_INFINITY);
		} else {
			Arrays.fill(this._outside, Double.NEGATIVE_INFINITY);
		}
		for(int k=this.countNodes()-1; k>=0; k--){
			this.outside(k);
		}
//		time = System.currentTimeMillis() - time;
//		System.err.println("OUTSIDE TIME:"+time+" ms");
	}
	
	protected void update(){
//		long time = System.currentTimeMillis();
		for(int k=0; k<this.countNodes(); k++)
			this.update(k);
//		time = System.currentTimeMillis() - time;
//		System.err.println("UPDATE TIME:"+time+" ms");
	}
	
	//just to gather the features.
	public void touch(){
		this._fm.setNetwork(this);
//		long time = System.currentTimeMillis();
		for(int k=0; k<this.countNodes(); k++)
			this.touch(k);
//		time = System.currentTimeMillis() - time;
//		System.err.println("TOUCH TIME:"+time+" ms");
	}
	
	public void max(){
		this._fm.setNetwork(this);
//		long time = System.currentTimeMillis();
		this._max = new double[this.countNodes()];
		this._max_paths = new int[this.countNodes()][];
		for(int k=0; k<this.countNodes(); k++){
			this.max(k);
//			System.err.println("max["+k+"]"+this._max[k]);
		}
//		time = System.currentTimeMillis() - time;
//		System.err.println("MAX TIME:"+time+" ms");
	}
	
	protected FeatureArray extractFeatures(int k, int[][] childrenList_k, int children_k_index){
		if(this._cacheFeatures){
			if(this._featureCache[k][children_k_index]!=null){
				return this._featureCache[k][children_k_index];
			}
		}
		int[] children_k = childrenList_k[children_k_index];
		FeatureArray fa = this._fm.extract(k, children_k);
		if(this._cacheFeatures){
			this._featureCache[k][children_k_index] = fa;
		}
		return fa;
	}
	
	protected void inside(int k){
		this.inside_new(k);
//		this.inside_old(k);
	}

	protected void inside_new(int k){
		if(this.isRemoved(k)){
			this._inside[k] = Double.NEGATIVE_INFINITY;
			return;
		}
		
		double inside = 0.0;
		int[][] childrenList_k = this.getChildren(k);
		
		if(_cacheFeatures){
			if(this._featureCache == null){
				this._featureCache = new FeatureArray[this.countNodes()][];
			}
			this._featureCache[k] = new FeatureArray[childrenList_k.length];
		}
		if(childrenList_k.length==0){
			childrenList_k = new int[1][0];
		}
		
		{
			int children_k_index = 0;
			int[] children_k = childrenList_k[children_k_index];

			boolean ignoreflag = false;
			for(int child_k : children_k)
				if(this.isRemoved(child_k))
					ignoreflag = true;
			if(ignoreflag){
				inside = Double.NEGATIVE_INFINITY;
			} else {
				FeatureArray fa = this.extractFeatures(k, childrenList_k, children_k_index);//this._fm.extract(k, children_k);
				double score = fa.getScore();
				for(int child_k : children_k)
					score += this._inside[child_k];
				inside = score;
			}
		}
		
		for(int children_k_index = 1; children_k_index < childrenList_k.length; children_k_index++){
			int[] children_k = childrenList_k[children_k_index];

			boolean ignoreflag = false;
			for(int child_k : children_k)
				if(this.isRemoved(child_k))
					ignoreflag = true;
			if(ignoreflag)
				continue;
			
			FeatureArray fa = this.extractFeatures(k, childrenList_k, children_k_index);//this._fm.extract(k, children_k);
			double score = fa.getScore();
			for(int child_k : children_k)
				score += this._inside[child_k];
			
			double v1 = inside;
			double v2 = score;
			if(v1>v2){
//				if(Math.log1p(Math.exp(score-inside))==0){
//					System.err.println("xxxxx1");
//				}
				inside = Math.log1p(Math.exp(score-inside))+inside;
			} else {
//				if(Math.log1p(Math.exp(inside-score))==0){
//					System.err.println("xxxxx2");
//				}
				inside = Math.log1p(Math.exp(inside-score))+score;
			}
		}
		
		this._inside[k] = inside;

//		if(k==this.countNodes()-1){
//			System.err.println("v="+this._inside[k]+"\t"+childrenList_k.length);
//		}
		
		if(this._inside[k]==Double.NEGATIVE_INFINITY)
			this.remove(k);
	}
	
	protected void inside_old(int k){
		if(this.isRemoved(k)){
			this._inside[k] = Double.NEGATIVE_INFINITY;
			return;
		}
		
		double inside = 0.0;
		int[][] childrenList_k = this.getChildren(k);
		
		if(_cacheFeatures){
			if(this._featureCache == null){
				this._featureCache = new FeatureArray[this.countNodes()][];
			}
			this._featureCache[k] = new FeatureArray[childrenList_k.length];
		}
		if(childrenList_k.length==0){
			childrenList_k = new int[1][0];
		}
		
		double[] subInsides = new double[childrenList_k.length];
		double subInside_max = 0;
		
		for(int children_k_index = 0; children_k_index < childrenList_k.length; children_k_index++){
			int[] children_k = childrenList_k[children_k_index];

			boolean ignoreflag = false;
			for(int child_k : children_k)
				if(this.isRemoved(child_k))
					ignoreflag = true;
			if(ignoreflag)
				continue;
			
			FeatureArray fa = this.extractFeatures(k, childrenList_k, children_k_index);//this._fm.extract(k, children_k);
			double score = fa.getScore();
			for(int child_k : children_k)
				score += this._inside[child_k];
			
			subInsides[children_k_index] = score;
			if(score > subInside_max)
				subInside_max = score;
		}
		
		for(int children_k_index = 0; children_k_index < childrenList_k.length; children_k_index++){
			inside += Math.exp(subInsides[children_k_index]-subInside_max);
//			System.err.println(children_k_index+"..."+(subInsides[children_k_index]-subInside_max));
		}
		
		this._inside[k] = Math.log(inside)+subInside_max;
		
		if(k==this.countNodes()-1){
			System.err.println("v="+this._inside[k]+"\t"+childrenList_k.length);
		}
		
		if(this._inside[k]==Double.NEGATIVE_INFINITY)
			this.remove(k);
	}
	
	protected void outside(int k){
		if(this.isRemoved(k)){
			this._outside[k] = Double.NEGATIVE_INFINITY;
			return;
		}
		else
			this._outside[k] = this.isRoot(k) ? 0.0 : this._outside[k];//0.0 : Math.log(this._outside[k]);
//		
//		if(this._outside[k]!=Double.NEGATIVE_INFINITY){
//			System.err.println(k+"="+this._outside[k]);
//		}
//		
		if(this._inside[k]==Double.NEGATIVE_INFINITY)
			this._outside[k] = Double.NEGATIVE_INFINITY;
		
		int[][] childrenList_k = this.getChildren(k);
		for(int children_k_index = 0; children_k_index< childrenList_k.length; children_k_index++){
			int[] children_k = childrenList_k[children_k_index];
			
			boolean ignoreflag = false;
			for(int child_k : children_k)
				if(this.isRemoved(child_k)){
					ignoreflag = true; break;
				}
			if(ignoreflag)
				continue;
			
			FeatureArray fa = this.extractFeatures(k, childrenList_k, children_k_index);//this._fm.extract(k, children_k);
			double score = fa.getScore();
			score += this._outside[k];
			for(int child_k : children_k)
				score += this._inside[child_k];
			
			if(score == Double.NEGATIVE_INFINITY)
				continue;
			
			for(int child_k : children_k){
				double v1 = this._outside[child_k];
				double v2 = score - this._inside[child_k];
				if(v1>v2){
					this._outside[child_k] = v1 + Math.log1p(Math.exp(v2-v1));
				} else {
					this._outside[child_k] = v2 + Math.log1p(Math.exp(v1-v2));
				}
//				
//				if(this._outside[k]!=Double.NEGATIVE_INFINITY){
//					System.err.println("child "+child_k+"="+this._outside[child_k]);
//				}

			}
		}
//		
//		if(k==7407){
//			System.err.println(this._outside[k]);
//			System.exit(1);
//		}
//		
		if(this._outside[k]==Double.NEGATIVE_INFINITY){
//			System.err.println("ROOT   :"+NetworkIDMapper.viewHybridNode(this.getNode(this.countNodes()-1)));
//			System.err.println("Network:"+NetworkIDMapper.viewHybridNode(this.getNode(k)));
//			System.err.println(this._inside[k]);
			this.remove(k);
		}
	}
	
	protected void update(int k){
		if(this.isRemoved(k))
			return;
		
		int[][] childrenList_k = this.getChildren(k);
		
		for(int children_k_index = 0; children_k_index<childrenList_k.length; children_k_index++){
			int[] children_k = childrenList_k[children_k_index];
			
			boolean ignoreflag = false;
			for(int child_k : children_k)
				if(this.isRemoved(child_k)){
					ignoreflag = true; break;
				}
			if(ignoreflag)
				continue;
			
			FeatureArray fa = this.extractFeatures(k, childrenList_k, children_k_index);//this._fm.extract(k, children_k);
			double score = fa.getScore();
			score += this._outside[k];
			for(int child_k : children_k)
				score += this._inside[child_k];
			double count = Math.exp(score-this.getInside());
			if(this._inst.getWeight()!=1.0)
				count *= this._inst.getWeight();
			fa.update(count);
		}
	}
	
	protected void touch(int k){
		if(this.isRemoved(k))
			return;
		
		int[][] childrenList_k = this.getChildren(k);
		for(int[] children_k : childrenList_k)
			this._fm.extract(k, children_k);
	}
	
	protected void max(int k){
		if(this.isRemoved(k)){
			this._max[k] = Double.NEGATIVE_INFINITY;
			return;
		}
		
		int[][] childrenList_k = this.getChildren(k);
		this._max[k] = Double.NEGATIVE_INFINITY;
		for(int[] children_k : childrenList_k){
			boolean ignoreflag = false;
			for(int child_k : children_k)
				if(this.isRemoved(child_k)){
					ignoreflag = true; break;
				}
			if(ignoreflag)
				continue;
			
			FeatureArray fa = this._fm.extract(k, children_k);
			double max = fa.getScore();
//			System.err.println("max="+max);
			for(int child_k : children_k)
				max += this._max[child_k];
			if(max >= this._max[k]){
				this._max[k] = max;
				this._max_paths[k] = children_k;
			}
		}
//		if(k==10){
//			System.exit(1);
//		}
	}
	
	//count the total number of nodes.
	public abstract int countNodes();
	
	//get the node with index k.
	protected abstract long getNode(int k);
	
	//get the children for node with index k.
	protected abstract int[][] getChildren(int k);
	
	//check whether the node with index k is removed.
	protected abstract boolean isRemoved(int k);
	
	//remove the node with index k.
	protected abstract void remove(int k);
	
	//check whether the node with index k is the root of the network.
	public abstract boolean isRoot(int k);
	
	//check whether the node with index k is a leaf of the network.
	public abstract boolean isLeaf(int k);
	
	public abstract boolean contains(long node);
	
	public long getRoot(){
		return this.getNode(this.countNodes()-1);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<this.countNodes(); i++)
			sb.append(Arrays.toString(NetworkIDMapper.toHybridNodeArray(this.getNode(i))));
		return sb.toString();
	}
	
	public void viewNetwork(HInstance inst, HGrammar g){
		this.viewNode(this.countNodes()-1, inst, g, 0);
	}
	
	private void viewNode(int t, HInstance inst, HGrammar g, int indent){
		PForest src = inst.getSrc();
		PForest tgt = inst.getTgt();
		long node = this.getNode(t);
		int[] node_arr = NetworkIDMapper.toHybridNodeArray(node);
		for(int k = 0; k<indent; k++)
			System.err.print('\t');
		src.viewNode(NetworkIDMapper.toForestNodeID(new int[]{node_arr[0], node_arr[1]}));
		for(int k = 0; k<indent; k++)
			System.err.print('\t');
		tgt.viewNode(NetworkIDMapper.toForestNodeID(new int[]{node_arr[2], node_arr[3]}));
		for(int k = 0; k<indent; k++)
			System.err.print('\t');
//		System.err.println(g.getHybridPatternById(node_arr[4]));
		System.err.println();
		int[][] childrenList = this.getChildren(t);
		for(int[] children : childrenList){
			for(int k = 0; k<indent; k++)
				System.err.print('\t');
			System.err.println("{");
			for(int child : children){
				this.viewNode(child, inst, g, indent+1);
			}
			for(int k = 0; k<indent; k++)
				System.err.print('\t');
			System.err.println("}");
		}
	}

}