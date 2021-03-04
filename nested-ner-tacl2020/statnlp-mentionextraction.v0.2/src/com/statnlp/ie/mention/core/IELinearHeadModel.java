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

import com.statnlp.ie.types.Mention;
import com.statnlp.ie.types.Segment;
import com.statnlp.ie.types.SemanticTag;
import com.statnlp.ie.types.UnlabeledTextSpan;
import com.statnlp.learn.core.NetworkConfig;
import com.statnlp.learn.core.NetworkIDMapper;

public class IELinearHeadModel {
	
	private transient ArrayList<IELinearHeadInstance> _labeledInstances;
	private transient IELinearHeadBuilder _builder;
	private IELinearHeadFeatureManager _fm;
	private SemanticTag[] _tags;
	private boolean _cacheFeatures = true;
	
	public IELinearHeadModel(IELinearHeadFeatureManager fm, SemanticTag[] tags){
		this._fm = fm;
		this._tags = tags;
		NetworkConfig.TRAIN_MODE_IS_GENERATIVE = false;
		this._builder = new IELinearHeadBuilder(fm, tags);
	}
	
	public void train(ArrayList<IELinearHeadInstance> labeledInstances){
		this._fm.disableCache();
		if(_cacheFeatures)
			this._fm.enableCache(labeledInstances.size(), this._tags.length);
		
		this._labeledInstances = labeledInstances;
		this.touch();
		this.train();
		this._fm.disableCache();
	}
	
	private IELinearHeadNetwork[] _networks_labeled;
	private IELinearHeadNetwork[] _networks_unlabeled;
	
	private void touch(){
		int num_instances = this._labeledInstances.size();
		IELinearHeadNetwork network1;
		IELinearHeadNetwork network2;
		for(int k = 0; k<num_instances; k++){
			if(k%1000==0 && k!=0){
				System.err.println(k+"...");
			}
			IELinearHeadInstance inst_labeled = this._labeledInstances.get(k);
			
//			System.err.println(inst_labeled.getId());
			if(!inst_labeled.isLabeled()){
				throw new RuntimeException("This instance is not labeled."+inst_labeled);
			}
			network1 = this.getNetwork(inst_labeled);
			network1.touch();
			
			IELinearHeadInstance inst_unlabeled = inst_labeled.removeLabels();
			if(inst_unlabeled.isLabeled()){
				throw new RuntimeException("This instance is labeled."+inst_unlabeled);
			}
			network2 = this.getNetwork(inst_unlabeled);
			network2.touch();
			
			if(!network2.contains(network1)){
				throw new RuntimeException("xx");
			}
		}
		System.err.println(this._fm.getParam().countFeatures()+" features");
		this._fm.getParam().lockIt();
	}
	
	private void train(){
		for(int it = 0; it<IELinearHeadConfig._MAX_LBFGS_ITRS; it++){
			if(this.train_oneIteration(it))
				return;
		}
	}
	
	private boolean _cacheNetworks = true;
	
	private IELinearHeadNetwork getNetwork(IELinearHeadInstance inst){
		
		if(this._cacheNetworks && this._networks_labeled == null){
//			System.err.println("xxx"+_labeledInstances.size());
			this._networks_labeled = new IELinearHeadNetwork[this._labeledInstances.size()];
			this._networks_unlabeled = new IELinearHeadNetwork[this._labeledInstances.size()];
		}
		
		if(this._cacheNetworks){
			int id = inst.getId();
			if(id>0){
				id--;
				if(this._networks_labeled[id]!=null){
					return this._networks_labeled[id];
				}
			} else {
				id = -id;
				id--;
				if(this._networks_unlabeled[id]!=null){
					return this._networks_unlabeled[id];
				}
			}
		}
		
		IELinearHeadNetwork network = this._builder.build(inst);
		
		if(this._cacheNetworks){
			int id = inst.getId();
			if(id>0){
				id--;
				this._networks_labeled[id] = network;
			} else {
				id = -id;
				id--;
				this._networks_unlabeled[id] = network;
			}
		}
		
		return network;
	}
	
	private boolean train_oneIteration(int it){
		
		long bTime = System.currentTimeMillis();
		int num_instances = this._labeledInstances.size();
		IELinearHeadNetwork network;
		for(int k = 0; k<num_instances; k++){
			IELinearHeadInstance inst_labeled = this._labeledInstances.get(k);
			
			NetworkConfig.EXP_MODE = NetworkConfig.EXP_LOCAL;
//			network = this._builder.build(inst_labeled);
			network = this.getNetwork(inst_labeled);
			network.train();
			double inside_num = network.getInside();
//			if((it+1)%100==0)
//				System.err.println("numerator="+Math.exp(inside_num)+"\t"+inside_num);
//			System.err.println(network.toString());
//			System.exit(1);
			
			NetworkConfig.EXP_MODE = NetworkConfig.EXP_GLOBAL;
			IELinearHeadInstance inst_unlabeled = inst_labeled.removeLabels();
//			network = this._builder.build(inst_unlabeled);
			network = this.getNetwork(inst_unlabeled);
			network.train();
			double inside_denom = network.getInside();
//			if((it+1)%100==0)
//				System.err.println("denominator="+Math.exp(inside_denom)+"\t"+inside_denom);
//			System.err.println(network.toString_ie(inst_unlabeled._span.length()));
//			System.exit(1);
			double prob = Math.exp(inside_num-inside_denom);
//			if((it+1)%100==0)
//				System.err.println("prob="+prob);
			if(prob>1.0){
				throw new RuntimeException("The prob exceeds 1:\t"+prob);
			}
		}
		double obj = this._fm.getParam().getObj();
		long eTime = System.currentTimeMillis();
		System.out.printf("Iteration %-4d   Obj=%-15.9f    %6.3f secs.\n", it, obj, (eTime-bTime)/1000.0);
//		this._fm.getParam().preview();
		return this._fm.getParam().update();
	}

	public void max(IELinearHeadInstance inst){
		
		this._cacheNetworks = false;
		
		this._fm.disableCache();
		this._fm.enableCache(1, this._tags.length);
		inst.setId(1);

		UnlabeledTextSpan span = (UnlabeledTextSpan)inst.getSpan();
		
//		IELinearHeadNetwork network = this._builder.build(inst);
		IELinearHeadNetwork network = this.getNetwork(inst);
		network.max();
		
		int root_k = network.countNodes()-1;
		this.decode(network, root_k, span);
		
	}
	
	private void decode(IELinearHeadNetwork network, int node_k, UnlabeledTextSpan span){
		
		int N = span.length();
		
		long node = network.get(node_k);
		int[] array = NetworkIDMapper.toHybridNodeArray(node);
		int hybridType = array[4];
		if(hybridType == IELinearHeadConfig.NODE_TYPE.EXACT_START_TAG.ordinal())
		{
			int[] nodes_child_k = network.getPath(node_k);
			
//			if(nodes_child_k.length!=1){
//				throw new RuntimeException("This is strange..."+nodes_child_k.length);
//			}
			
			int node_child_k = nodes_child_k[0];
			long node_child = network.get(node_child_k);
			int[] array_child = NetworkIDMapper.toHybridNodeArray(node_child);
			int hybridType_child = array_child[4];
			if(hybridType_child == IELinearHeadConfig.NODE_TYPE.TERMINATE.ordinal()){
				//DO NOTHING..
			}
			
			else {
				int start_index = N - array[0];
				int tag_id = array[3]-1;
				SemanticTag tag = this._tags[tag_id-1];
				if(tag_id!=tag.getId()){
					throw new RuntimeException(tag.getId()+"!="+tag_id);
				}
				
//				System.err.println("HERE.");
				
				ArrayList<Segment[]> boundaries = this.findBoundaries(N, network, node_k);
				for(Segment[] segs : boundaries){
					Segment seg_mention = segs[0];
					if(seg_mention.getBIndex()!=start_index){
						throw new RuntimeException(seg_mention.getBIndex()+"!="+start_index);
					}
					Segment seg_head = segs[1];
					span.label_predict(seg_mention.getBIndex(), seg_mention.getEIndex(), new Mention(seg_mention.getBIndex(), seg_mention.getEIndex(), seg_head.getBIndex(), seg_head.getEIndex(), tag));
				}
			}
			
//			else{
//				throw new RuntimeException("This is strange..."+hybridType_child);
//			}
			
		}
		
		int[] nodes_child_k = network.getPath(node_k);
		
		for(int node_child_k : nodes_child_k){
			this.decode(network, node_child_k, span);
		}
		
	}
	
	private ArrayList<Segment[]> findBoundaries(int N, IELinearHeadNetwork network, int node_k){
		ArrayList<Segment[]> results = new ArrayList<Segment[]>();
		this.findBoundariesHelper(N, results, network, node_k, new ArrayList<String>());
		return results;
	}
	
	@SuppressWarnings("unchecked")
	private void findBoundariesHelper(int N, ArrayList<Segment[]> results, IELinearHeadNetwork network, 
			int node_k, ArrayList<String> curr_states){
		long node = network.get(node_k);
		int[] array = NetworkIDMapper.toHybridNodeArray(node);
		int hybridType = array[4];
		int[] nodes_child_k = network.getPath(node_k);
		
		ArrayList<String> next_states = (ArrayList<String>)curr_states.clone();
		
		if(hybridType == IELinearHeadConfig.NODE_TYPE.EXACT_START_TAG.ordinal()){
			int end_index = N - array[0];
			String next_state = ""+end_index;
//			System.err.println("==>"+next_state);
			next_states.add(next_state);
		}
		
		for(int node_child_k : nodes_child_k){
			long node_child = network.get(node_child_k);
			int[] array_child = NetworkIDMapper.toHybridNodeArray(node_child);
			int hybridType_child = array_child[4];
			
			if(hybridType == IELinearHeadConfig.NODE_TYPE.EXACT_START_TAG.ordinal()
					&& hybridType_child == IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_WITHIN_HEAD.ordinal()){
				int end_index = N - array[0];
				ArrayList<String> next_states_new = new ArrayList<String>();
				for(String next_state : next_states){
					next_states_new.add(next_state+"|"+end_index);
//					System.err.println("==>"+next_state+"|"+end_index);
				}
				next_states = next_states_new;
			}
			
			if(hybridType == IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_BEFORE_HEAD.ordinal()
					&& hybridType_child == IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_WITHIN_HEAD.ordinal()){
				int end_index = N - array[0] + 1;
				ArrayList<String> next_states_new = new ArrayList<String>();
				for(String next_state : next_states){
					next_states_new.add(next_state+"|"+end_index);
//					System.err.println("==>"+next_state+"|"+end_index);
				}
				next_states = next_states_new;
			}
			
			if((hybridType == IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_WITHIN_HEAD.ordinal()
					&& hybridType_child == IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_AFTER_HEAD.ordinal())
					|| (hybridType == IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_WITHIN_HEAD.ordinal()
							&& hybridType_child == IELinearHeadConfig.NODE_TYPE.TERMINATE.ordinal())){
				int end_index = N - array[0] + 1;
				ArrayList<String> next_states_new = new ArrayList<String>();
				for(String next_state : next_states){
					next_states_new.add(next_state+"|"+end_index);
//					System.err.println("==>"+next_state+"|"+end_index);
				}
				next_states = next_states_new;
			}
			
			if(hybridType_child == IELinearHeadConfig.NODE_TYPE.TERMINATE.ordinal()){
				int end_index = N - array[0] + 1;
				ArrayList<String> next_states_new = new ArrayList<String>();
				for(String next_state : next_states){
					next_states_new.add(next_state+"|"+end_index);
				}
				next_states = next_states_new;
//				System.err.println("YES"+next_states.size());
				Segment segs[] = new Segment[2];
				for(String next_state : next_states){
//					System.err.println("["+next_state+"]\t"+hybridType);
					String[] s = next_state.split("\\|");
//					System.err.println(s[0]);
//					System.err.println(s[1]);
//					System.err.println(s[2]);
//					System.err.println(s[3]);
					int bIndex = Integer.parseInt(s[0]);
					int eIndex = Integer.parseInt(s[3]);
					int bIndex_head = Integer.parseInt(s[1]);
					int eIndex_head = Integer.parseInt(s[2]);
					segs[0] = new Segment(bIndex, eIndex);
					segs[1] = new Segment(bIndex_head, eIndex_head);
				}
				results.add(segs);
			} else {
				this.findBoundariesHelper(N, results, network, node_child_k, next_states);
			}
		}
	}
	
}
