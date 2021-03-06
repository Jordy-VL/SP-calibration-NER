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

import java.util.ArrayList;
import java.util.Iterator;

import com.statnlp.learn.core.AttributedWord;
import com.statnlp.learn.core.FeatureArray;
import com.statnlp.learn.core.FeatureManager;
import com.statnlp.learn.core.HGrammar;
import com.statnlp.learn.core.Network;
import com.statnlp.learn.core.NetworkIDMapper;
import com.statnlp.learn.core.NetworkParam;
import com.statnlp.learn.core.TextSpan;

public class IELinearFeatureManager extends FeatureManager{
	
	private IELinearNetwork _network;
	private IELinearInstance _inst;
	private TextSpan _span;
	private final FeatureArray _EMPTY_ = new FeatureArray(this._param);
	
	public IELinearFeatureManager(HGrammar g, NetworkParam param) {
		super(g, param);
//		int f1 = this._param.toFeature("MENTION","MENTION");
//		int f2 = this._param.toFeature("ONE_WORD", "ONE_WORD");
		//dirty hack
//		this._param.lockIt();
//		this._param.setWeight(f, +100.0);
//		this._param.unlockForNewFeaturesAndFixCurrentFeatures();
	}
	
	public int[][][][][] _cache;
	
	public void enableCache(int num_insts, int num_tags){
		if(num_insts>1)
			System.err.println("cache enabled.");
		this._cache = new int[num_insts][num_tags][FEATURE_TYPE.values().length][][];
	}
	
	public void disableCache(){
//		System.err.println("cache disabled.");
		this._cache = null;
	}
	
	private enum FEATURE_TYPE {START_MENTION, END_MENTION, WITHIN_MENTION};
	
	private FeatureArray extract(FEATURE_TYPE ft, int word_id, int tag_id, FeatureArray fa){
		int ft_id = ft.ordinal();
		if(this._cache != null){
			int inst_id = this._inst.getId();
			if(inst_id<0){
				inst_id = -inst_id;
			}
			if(this._cache[inst_id-1][tag_id-1][ft_id]==null){
				this._cache[inst_id-1][tag_id-1][ft_id] = new int[this._span.length()][];
			}
			
			if(this._cache[inst_id-1][tag_id-1][ft_id][word_id]!=null){
				int[] fs = this._cache[inst_id-1][tag_id-1][ft_id][word_id];
				return new FeatureArray(fs, fa);
			}
		}
		
		int[] fs = this.extract_helper(ft, word_id, tag_id);
		
		if(this._cache!=null){
			int inst_id = this._inst.getId();
			if(inst_id<0){
				inst_id = -inst_id;
			}
			this._cache[inst_id-1][tag_id-1][ft_id][word_id] = fs;
		}
		
		fa  = new FeatureArray(fs, fa);
		return fa;
	}
	
	private int[] extract_helper(FEATURE_TYPE ft, int word_id, int tag_id){
		TextSpan span = this._inst.getSpan();
//		System.err.println(span.toLine());
		AttributedWord word = span.getWord(word_id);
		Iterator<String> atts = word.getAttributes().iterator();
		
		int num_atts = 0;
		for(String att :word.getAttributes()){
			num_atts += word.getAttribute(att).size();
		}
		
		int size = num_atts;
		
		if(ft == FEATURE_TYPE.START_MENTION){
			size = size+1;
		}
		
		int[] f = new int[size];
		int k = 0;
		while(atts.hasNext()){
			String att = atts.next();
			ArrayList<String> vals = word.getAttribute(att);
			
//			System.err.println(att+"\t"+word.getAttribute(att));
			for(String val : vals){
//				if(att.equals("POS") || att.equals("curr"))
				{
					f[k++] = this._param.toFeature(ft.name()+":"+tag_id+":"+att, val);
//					f[k++] = this._param.toFeature(ft.name()+":"+":"+att, val);
				}
			}
		}
		
//		System.exit(1);
		
		if(ft == FEATURE_TYPE.START_MENTION){
			f[k++] = this._param.toFeature("MENTION","MENTION");
		}
		
		if(k!=size)
			throw new RuntimeException("Wrong.."+k+"!="+size);
		
		return f;
	}
	
	@Override
	public void setNetwork(Network network) {
		this._network = (IELinearNetwork)network;
		this._inst = (IELinearInstance)this._network.getInstance();
		this._span = this._inst.getSpan();
	}
	
	@Override
	public FeatureArray extract(int parent_k, int[] children_k) {
		
		long node_parent = this._network.get(parent_k);
		int[] node_parent_arr = NetworkIDMapper.toHybridNodeArray(node_parent);
		int node_parent_type = node_parent_arr[4];
		
		if(node_parent_type == IELinearConfig.NODE_TYPE.ROOT.ordinal()){
			return _EMPTY_;
		}
		if(node_parent_type == IELinearConfig.NODE_TYPE.TERMINATE.ordinal()){
			return _EMPTY_;
		}
		if(node_parent_type == IELinearConfig.NODE_TYPE.AFTER_START.ordinal()){
			return _EMPTY_;
		}
		if(node_parent_type == IELinearConfig.NODE_TYPE.EXACT_START.ordinal()){
			return _EMPTY_;
		}
		
		if(node_parent_type == IELinearConfig.NODE_TYPE.EXACT_START_TAG.ordinal()){
			long node_child = this._network.get(children_k[0]);
			int[] node_child_arr = NetworkIDMapper.toHybridNodeArray(node_child);
			int node_child_type = node_child_arr[4];
			
			if(node_child_type == IELinearConfig.NODE_TYPE.TERMINATE.ordinal()){
				return _EMPTY_;
//				int bIndex = - node_parent_arr[0];
//				int tag_id = node_parent_arr[3]-1;
//				TextSpan span = this._inst.getSpan();
//				int word_id = span.length() + bIndex;
//				
//				FeatureArray fa = this._EMPTY_;
//				fa = this.extract(FEATURE_TYPE.OUT_MENTION, word_id, tag_id, fa);
//				
//				return fa;
			}
			
			if(node_child_type == IELinearConfig.NODE_TYPE.INCOMPLETE_START_TAG.ordinal()){
				int bIndex = - node_child_arr[0];
				int tag_id = node_child_arr[3]-1;
				TextSpan span = this._inst.getSpan();
				int word_id = span.length() + bIndex;
				
				FeatureArray fa = this._EMPTY_;
				fa = this.extract(FEATURE_TYPE.START_MENTION, word_id, tag_id, fa);
				
				return fa;
			}
			throw new RuntimeException("This should not happen.");
		}
		
		if(node_parent_type == IELinearConfig.NODE_TYPE.INCOMPLETE_START_TAG.ordinal()){
			int bIndex = - node_parent_arr[0];
			int tag_id = node_parent_arr[3]-1;
			TextSpan span = this._inst.getSpan();
			int word_id = span.length() + bIndex;

			FeatureArray fa = this._EMPTY_;
			fa = this.extract(FEATURE_TYPE.WITHIN_MENTION, word_id, tag_id, fa);
			
			for(int i = 0; i<children_k.length; i++){
				long node_child = this._network.get(children_k[i]);
				int[] node_child_arr = NetworkIDMapper.toHybridNodeArray(node_child);
				int node_child_type = node_child_arr[4];
				
				if(node_child_type == IELinearConfig.NODE_TYPE.TERMINATE.ordinal()){
					fa = this.extract(FEATURE_TYPE.END_MENTION, word_id, tag_id, fa);
				}
				
				if(node_child_type == IELinearConfig.NODE_TYPE.INCOMPLETE_START_TAG.ordinal()){
//					fa = this.extract(FEATURE_TYPE.WITHIN_MENTION, word_id, tag_id, fa);
				}
			}
			return fa;
		}
		
		throw new RuntimeException("This should not happen.");
		
//		long node = this._network.get(parent_k);
//		long[] children_nodes = new long[children_k.length];
//		for(int k = 0; k<children_nodes.length; k++){
//			children_nodes[k] = this._network.get(children_k[k]);
//		}
//		
//		int id = this._inst.getId();
//		if(id<0)
//			id = -id;
////		
////		//TODO
//		int f = this._param.toFeature("x"+node, "y"+java.util.Arrays.toString(children_nodes)+":z"+id);
////		int f = this._param.toFeature("x", "y");
//		return new FeatureArray(new int[]{f}, this._param);
		
	}
	
}