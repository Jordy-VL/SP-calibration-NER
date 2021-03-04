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

import com.statnlp.ie.types.IEConfig;
import com.statnlp.ie.types.Mention;
import com.statnlp.ie.types.SemanticTag;
import com.statnlp.ie.types.SemanticTagList;
import com.statnlp.ie.types.UnlabeledTextSpan;
import com.statnlp.learn.core.NetworkConfig;
import com.statnlp.learn.core.NetworkIDMapper;

public class IELinearModel_CONLL2003 {
	
	private transient ArrayList<IELinearInstance> _labeledInstances;
	private transient IELinearBuilder_CONLL2003 _builder;
	private IELinearFeatureManager_CONLL2003 _fm;
	private SemanticTag[] _tags;
	private boolean _cacheFeatures = true;
	
	public IELinearModel_CONLL2003(IELinearFeatureManager_CONLL2003 fm, SemanticTag[] tags){
		this._fm = fm;
		this._tags = tags;
		NetworkConfig.TRAIN_MODE_IS_GENERATIVE = false;
		this._builder = new IELinearBuilder_CONLL2003(fm, tags);
	}
	
	public void train(ArrayList<IELinearInstance> labeledInstances, int max_num_iterations){
		this._fm.disableCache();
		if(_cacheFeatures)
			this._fm.enableCache(labeledInstances.size(), this._tags.length);
		
		this._labeledInstances = labeledInstances;
		this.touch();
		this.train(max_num_iterations);
		
		//dirty trick..
		int f1 = this._fm.getParam().toFeature("MENTION","MENTION");
		double weight1 = this._fm.getParam().getWeight(f1);
//		System.err.println("mention penalty="+weight1);
//		int f2 = this._fm.getParam().toFeature("ONE_WORD", "ONE_WORD");
//		double weight2 = this._fm.getParam().getWeight(f2);
//		System.err.println("word penalty="+weight2);
		
		this._fm.disableCache();
	}
	
	private IELinearNetwork_CONLL2003[] _networks_labeled;
	private IELinearNetwork_CONLL2003[] _networks_unlabeled;
	
	private void touch(){
		int num_instances = this._labeledInstances.size();
		IELinearNetwork_CONLL2003 network1;
		IELinearNetwork_CONLL2003 network2;
		for(int k = 0; k<num_instances; k++){
			if(k%1000==0 && k!=0){
				System.err.println(k+"...");
			}
			IELinearInstance inst_labeled = this._labeledInstances.get(k);
			
//			System.err.println(inst_labeled.getId());
			if(!inst_labeled.isLabeled()){
				throw new RuntimeException("This instance is not labeled."+inst_labeled);
			}
			network1 = this.getNetwork(inst_labeled);
			network1.touch();
			
			IELinearInstance inst_unlabeled = inst_labeled.removeLabels();
			if(inst_unlabeled.isLabeled()){
				throw new RuntimeException("This instance is labeled."+inst_unlabeled);
			}
			network2 = this.getNetwork(inst_unlabeled);
			network2.touch();
			
//			if(!network2.contains(network1)){
//				throw new RuntimeException("xx");
//			}
		}
		System.err.println(this._fm.getParam().countFeatures()+" features");
		this._fm.getParam().lockIt();
	}
	
	private void train(int max_num_iterations){
		for(int it = 0; it<max_num_iterations; it++){
			if(this.train_oneIteration(it))
				break;
		}
	}
	
	private boolean _cacheNetworks = true;
	
	private IELinearNetwork_CONLL2003 getNetwork(IELinearInstance inst){
		
		if(this._cacheNetworks && this._networks_labeled == null){
//			System.err.println("xxx"+_labeledInstances.size());
			this._networks_labeled = new IELinearNetwork_CONLL2003[this._labeledInstances.size()];
			this._networks_unlabeled = new IELinearNetwork_CONLL2003[this._labeledInstances.size()];
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
		
		IELinearNetwork_CONLL2003 network = this._builder.build(inst);
		
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
		IELinearNetwork_CONLL2003 network;
		for(int k = 0; k<num_instances; k++){
			IELinearInstance inst_labeled = this._labeledInstances.get(k);
			
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
			IELinearInstance inst_unlabeled = inst_labeled.removeLabels();
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
		System.out.println("Iteration "+it+"\t Obj="+obj+"\t"+(eTime-bTime)/1000.0+" secs.");
//		this._fm.getParam().preview();
		return this._fm.getParam().update();
	}

	public void max(IELinearInstance inst){
		
		this._cacheNetworks = false;
		
		this._fm.disableCache();
		this._fm.enableCache(1, this._tags.length);
		inst.setId(1);
		
		UnlabeledTextSpan span = (UnlabeledTextSpan)inst.getSpan();
		
//		IELinearNetwork network = this._builder.build(inst);
		IELinearNetwork_CONLL2003 network = this.getNetwork(inst);
		network.max();
		
		int root_k = network.countNodes()-1;
		this.decode(network, root_k, span);
		
	}
	
	private void decode(IELinearNetwork_CONLL2003 network, int node_k, UnlabeledTextSpan span){
		
		//TODO
		
//		throw new RuntimeException("Decoder not yet implemented.");

//		/*
		int N = span.length();
		
		long node = network.get(node_k);
		int[] array = NetworkIDMapper.toHybridNodeArray(node);
		int hybridType = array[4];
		if(hybridType == IELinearConfig.NODE_TYPE.EXACT_START_TAG.ordinal())
		{
			int[] nodes_child_k = network.getPath(node_k);
			
			if(nodes_child_k.length!=1){
				throw new RuntimeException("This is strange..."+nodes_child_k.length);
			}
			
			int node_child_k = nodes_child_k[0];
			long node_child = network.get(node_child_k);
			int[] array_child = NetworkIDMapper.toHybridNodeArray(node_child);
			int hybridType_child = array_child[4];
			if(hybridType_child == IELinearConfig.NODE_TYPE.TERMINATE.ordinal()){
				//DO NOTHING..
			}
			
			else if(hybridType_child == IELinearConfig.NODE_TYPE.INCOMPLETE_START_TAG.ordinal()){
				int start_index = N - array[0];
				int tag_id = array[3]-1;
				SemanticTag tag = this._tags[tag_id-1];
				if(tag_id!=tag.getId()){
					throw new RuntimeException(tag.getId()+"!="+tag_id);
				}
				
				ArrayList<Integer> end_indices = this.findBoundaries(N, network, node_child_k);
				for(int end_index : end_indices){
//					System.err.println(start_index+","+end_index+"\t"+tag);
					span.label_predict(start_index, end_index, new Mention(start_index, end_index, start_index, end_index, tag));
				}
			}
			
			else{
				throw new RuntimeException("This is strange..."+hybridType_child);
			}
			
		}
		
		int[] nodes_child_k = network.getPath(node_k);
		
		for(int node_child_k : nodes_child_k){
			this.decode(network, node_child_k, span);
		}
		
//			*/
	}
	
	private ArrayList<Integer> findBoundaries(int N, IELinearNetwork_CONLL2003 network, int node_k){
		ArrayList<Integer> results = new ArrayList<Integer>();
		this.findBoundariesHelper(N, results, network, node_k);
		return results;
	}
	
	private void findBoundariesHelper(int N, ArrayList<Integer> results, IELinearNetwork_CONLL2003 network, int node_k){
		long node = network.get(node_k);
		int[] array = NetworkIDMapper.toHybridNodeArray(node);
		int hybridType = array[4];
//		System.err.println("<<<"+hybridType+"\t"+IELinearConfig.NODE_TYPE.INCOMPLETE_START_TAG.ordinal()+"\t"+array[0]);
		int[] nodes_child_k = network.getPath(node_k);
		
		for(int node_child_k : nodes_child_k){
			long node_child = network.get(node_child_k);
			int[] array_child = NetworkIDMapper.toHybridNodeArray(node_child);
			int hybridType_child = array_child[4];
			if(hybridType_child == IELinearConfig.NODE_TYPE.TERMINATE.ordinal()){
				int end_index = N - array[0] + 1;
//				System.err.println(">>>\t"+hybridType+"\t"+IELinearConfig.NODE_TYPE.INCOMPLETE_START_TAG.ordinal()+"\t"+array[0]+"\t"+end_index+"\t"+N);
				results.add(end_index);
			} else {
				this.findBoundariesHelper(N, results, network, node_child_k);
			}
		}
	}
	
}
