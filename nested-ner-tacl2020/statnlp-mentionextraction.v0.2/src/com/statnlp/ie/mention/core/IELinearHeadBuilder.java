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

import com.statnlp.ie.types.LabeledTextSpan;
import com.statnlp.ie.types.Mention;
import com.statnlp.ie.types.Segment;
import com.statnlp.ie.types.SemanticTag;
import com.statnlp.learn.core.NetworkException;
import com.statnlp.learn.core.NetworkIDMapper;
import com.statnlp.learn.core.TextSpan;

public class IELinearHeadBuilder {
	
	private int _N;
	private int _M; // number of possible tags at each position
	private SemanticTag[] _tags; //all possible tags, excluding start and end.
	private IELinearHeadFeatureManager _fm;
	private boolean _isLabeled = false;
	private boolean _built = false;
	private long[] _nodes;
	private int[][][] _children;
	private int[] _roots;
	private boolean _debug = false;
	
	public IELinearHeadBuilder(IELinearHeadFeatureManager fm, SemanticTag[] tags){
		this._fm = fm;
		this._tags = tags;
		this._N = IELinearHeadConfig._MAX_SENT_LENGTH;
		this._M = this._tags.length;
	}
	
	public IELinearHeadNetwork build(IELinearHeadInstance inst){
		
		this._isLabeled = inst.isLabeled();
		if(!this._isLabeled)
		{
//			if(!this._built){
//				this.build_unlabeled();
//				this._built = true;
//			}
//			
////			return this.build_labeled(inst);
//			IELinearNetwork network = new IELinearNetwork(inst, this._fm, this._nodes, this._children, this.getRootPositionForLen(inst.length())+1);
////			System.err.println(network.countNodes()+" nodes");
////			System.exit(1);
//			return network;
			return this.build_unlabeled(inst);
		} else {
			return this.build_labeled(inst);
		}
		
	}
	
	public IELinearHeadNetwork build_labeled(IELinearHeadInstance inst){
		IELinearHeadNetwork network = new IELinearHeadNetwork(inst, this._fm);
		this.build_labeled(network, inst);
		network.finalizeNetwork();
//		int size = 1 + (this._M * 2 + 3) * inst.length();
//		System.err.println("There are "+network.countNodes()+" nodes. maximum: "+size);
		return network;
	}
	
	public IELinearHeadNetwork build_unlabeled(IELinearHeadInstance inst){
		this._N = inst.length();
		IELinearHeadNetwork network = new IELinearHeadNetwork(inst, this._fm);
		this.build(network, -1);
		network.finalizeNetwork();
		return network;
	}
	
	public void build_unlabeled(){
		IELinearHeadNetwork network = new IELinearHeadNetwork(null, null);
		this.build(network, -1);
		network.finalizeNetwork();
		this._nodes = network.getAllNodes();
		this._children = network.getAllChildren();
		int size = 1 + (this._M * 2 + 3) * this._N;
		if(size!=this._nodes.length){
			throw new RuntimeException("The size is "+this._nodes.length+" which is different from expected "+size);
		}
		this._roots = new int[this._N];
		for(int k = 1; k<=this._N; k++){
			this._roots[k-1] = 1 + (this._M * 2 + 3) * k - 1;
		}
	}
	
	public int getRootPositionForLen(int len){
		return this._roots[len-1];
	}
	
	private SemanticTag[] getTags(int bIndex){
		return this._tags;
	}
	
	public void build_labeled(IELinearHeadNetwork network, IELinearHeadInstance inst){
		
		LabeledTextSpan span = (LabeledTextSpan)inst.getSpan();
		int N = span.length();
		
		long node_terminate = this.toNode_terminate();
		network.addNode(node_terminate);
		if(this._debug)
			System.err.println("TERMINATE:");

		SemanticTag[] tags = this._tags;
		
		ArrayList<Segment> segments = span.getAllSegments();
		
		for(int bIndex=-1; bIndex>=-N; bIndex--){
			long node_after_start = this.toNode_after_start(bIndex);
			long node_exact_start = this.toNode_exact_start(bIndex);
			network.addNode(node_after_start);
			network.addNode(node_exact_start);
			if(bIndex==-1){
				network.addEdge(node_after_start, new long[]{node_exact_start, node_terminate});
			} else {
				long node_after_start_next = this.toNode_after_start(bIndex+1);
				network.addEdge(node_after_start, new long[]{node_exact_start, node_after_start_next});
			}
			
			long[] nodes_exact_start_tags = new long[tags.length];
			for(int k = 0; k<tags.length; k++){
				SemanticTag tag = tags[k];
				
				long node_exact_start_tag = this.toNode_exact_start_tag(bIndex, tag);
				network.addNode(node_exact_start_tag);
				if(this._debug)
					System.err.println("EXACT_START_TAG:"+bIndex+"\t"+tag);
				
				nodes_exact_start_tags[k] = node_exact_start_tag;
				
				boolean found_seg = false;
				for(Segment segment : segments){
					ArrayList<Mention> mentions = span.getLabels_all(segment.getBIndex(), segment.getEIndex());
					for(Mention mention : mentions){
						if(N+bIndex!= mention.getSegment().getBIndex())
							continue;
						if(!mention.getTag().equals(tag))
							continue;
							
						found_seg = true;
						int eIndex = mention.getSegment().getEIndex()-N;
						int bIndex_head = mention.getHeadSegment().getBIndex()-N;
						int eIndex_head = mention.getHeadSegment().getEIndex()-N;
						
						long node_prev = node_exact_start_tag;
						long node;
						for(int index=bIndex; index<eIndex; index++){
							if(index<bIndex_head){
								node = this.toNode_before_head_tag(index, tag);
							} else if(index<eIndex_head){
								node = this.toNode_in_head_tag(index, tag);
							} else{
								node = this.toNode_after_head_tag(index, tag);
							}
							network.addNode(node);
							try{
								network.addEdge(node_prev, new long[]{node});
							} catch(NetworkException e){
							}
							node_prev = node;
						}
						try{
							network.addEdge(node_prev, new long[]{node_terminate});
						} catch(NetworkException e){
						}
					}
				}
				if(!found_seg){
					network.addEdge(node_exact_start_tag, new long[]{node_terminate});
				}
			}
			network.addEdge(node_exact_start, nodes_exact_start_tags);
		}
		
		long node_root = this.toNode_root(-N);
		long node_after_start = this.toNode_after_start(-N);
		
		network.addNode(node_root);
		network.addEdge(node_root, new long[]{node_after_start});
		
	}
	
	public void build_labeled_old(IELinearHeadNetwork network, IELinearHeadInstance inst){
		
		TextSpan span = inst.getSpan();
		
		int N = span.length();
		
		ArrayList<Segment> segments = null;
		
		if(this._isLabeled){
			segments = ((LabeledTextSpan)span).getAllSegments();
		}
		
		long node_terminate = this.toNode_terminate();
		network.addNode(node_terminate);
		if(this._debug)
			System.err.println("TERMINATE:");
		
		for(int bIndex = -1; bIndex>= -N; bIndex--){
			long node_after_start = this.toNode_after_start(bIndex);
			long node_exact_start = this.toNode_exact_start(bIndex);
			network.addNode(node_after_start);
			network.addNode(node_exact_start);
			if(bIndex==-1){
				network.addEdge(node_after_start, new long[]{node_exact_start, node_terminate});
			} else {
				long node_after_start_next = this.toNode_after_start(bIndex+1);
				network.addEdge(node_after_start, new long[]{node_exact_start, node_after_start_next});
			}
//			if(bIndex==-N)
			{
				long node_root = this.toNode_root(bIndex);
				network.addNode(node_root);
				if(this._debug)
					System.err.println("ROOT:"+bIndex);
				network.addEdge(node_root, new long[]{node_after_start});
			}
			
			long[] nodes_exact_start_tag = new long[this._tags.length];
			for(int k = 0; k<this._tags.length; k++){
				SemanticTag tag = this._tags[k];
				long node_exact_start_tag = this.toNode_exact_start_tag(bIndex, tag);
				network.addNode(node_exact_start_tag);
				nodes_exact_start_tag[k] = node_exact_start_tag;
				
				if(this._isLabeled){
					boolean should_start = false;
					boolean should_terminate = false;
					boolean should_connect_next = false;
					
					for(Segment segment : segments){
						ArrayList<Mention> mentions = ((LabeledTextSpan)span).getLabels_all(segment.getBIndex(), segment.getEIndex());
						
						boolean found_tag = false;
						for(Mention mention: mentions){
							if(mention.getTag().equals(tag)){
								found_tag = true;
								break;
							}
						}
						if(!found_tag){
							continue;
						}
						
						if(segment.getBIndex()-N==bIndex){
							int eIndex = segment.getEIndex();
							
							
						}
					}
					
//					//TODO
//					
//					long node_incomplete_start_tag = this.toNode_incomplete_start_tag(bIndex, tag);
//					
//					if(should_start){
//						network.addNode(node_incomplete_start_tag);
//						network.addEdge(node_exact_start_tag, new long[]{node_incomplete_start_tag});
//					} else {
//						network.addEdge(node_exact_start_tag, new long[]{node_terminate});
//					}
//					
//					if(should_terminate){
//						network.addNode(node_incomplete_start_tag);
//						long node_incomplete_start_tag_next = this.toNode_incomplete_start_tag(bIndex+1, tag);
//						if(should_connect_next){
//							network.addEdge(node_incomplete_start_tag, new long[]{node_terminate, node_incomplete_start_tag_next});
//						} else {
//							network.addEdge(node_incomplete_start_tag, new long[]{node_terminate});
//						}
//					}
//					else if(should_connect_next){
//						network.addNode(node_incomplete_start_tag);
//						long node_incomplete_start_tag_next = this.toNode_incomplete_start_tag(bIndex+1, tag);
//						network.addEdge(node_incomplete_start_tag, new long[]{node_incomplete_start_tag_next});
//					}
				} else {
					network.addEdge(node_exact_start_tag, new long[]{node_terminate});
					
					long node_in_head_tag = this.toNode_in_head_tag(bIndex, tag);
					network.addNode(node_in_head_tag);
					network.addEdge(node_exact_start_tag, new long[]{node_in_head_tag});
					network.addEdge(node_in_head_tag, new long[]{node_terminate});
					
					long node_after_head_tag = this.toNode_after_head_tag(bIndex, tag);
					network.addNode(node_after_head_tag);
					network.addEdge(node_after_head_tag, new long[]{node_terminate});
					
					if(bIndex<-1){
						long node_before_head_tag = this.toNode_before_head_tag(bIndex, tag);
						network.addNode(node_before_head_tag);
						network.addEdge(node_exact_start_tag, new long[]{node_before_head_tag});
						network.addEdge(node_exact_start_tag, new long[]{node_before_head_tag, node_in_head_tag});
						
						long node_before_head_tag_next = this.toNode_before_head_tag(bIndex+1, tag);
						long node_in_head_tag_next = this.toNode_in_head_tag(bIndex+1, tag);
						long node_after_head_tag_next = this.toNode_after_head_tag(bIndex+1, tag);
						
						network.addEdge(node_before_head_tag, new long[]{node_in_head_tag_next});
						if(network.contains(node_before_head_tag_next)){
							network.addEdge(node_before_head_tag, new long[]{node_before_head_tag_next});
							network.addEdge(node_before_head_tag, new long[]{node_before_head_tag_next, node_in_head_tag_next});
						}
						
						network.addEdge(node_in_head_tag, new long[]{node_in_head_tag_next});
						network.addEdge(node_in_head_tag, new long[]{node_after_head_tag_next});
						network.addEdge(node_in_head_tag, new long[]{node_in_head_tag_next, node_after_head_tag_next});
						
						network.addEdge(node_after_head_tag, new long[]{node_after_head_tag_next});
					}
				}
			}
			network.addEdge(node_exact_start, nodes_exact_start_tag);
		}
		
	}
	
	public void build(IELinearHeadNetwork network, int bIndex){
		
		long node_terminate = this.toNode_terminate();
		
		if(bIndex == -1){
			network.addNode(node_terminate);
			if(this._debug)
				System.err.println("TERMINATE:");
		}
		
		long node_after_start = this.toNode_after_start(bIndex);
		network.addNode(node_after_start);
		if(_debug){
			System.err.println("AFTER_START:"+bIndex);
		}
		
		long node_exact_start = this.toNode_exact_start(bIndex);
		network.addNode(node_exact_start);
		if(_debug){
			System.err.println("EXACT_START:"+bIndex);
		}
		
		if(bIndex == -1){
			network.addEdge(node_after_start, new long[]{node_exact_start, node_terminate});
		} else {
			long node_after_start_next = this.toNode_after_start(bIndex+1);
			network.addEdge(node_after_start, new long[]{node_exact_start, node_after_start_next});
		}
		//FINISH AS
		
		SemanticTag[] tags = this.getTags(bIndex);
		
		long[] nodes_exact_start_tags = new long[tags.length];
		
		for(int k = 0; k<tags.length; k++){
			SemanticTag tag = tags[k];
			
			long node_exact_start_tag = this.toNode_exact_start_tag(bIndex, tag);
			network.addNode(node_exact_start_tag);
			if(this._debug)
				System.err.println("EXACT_START_TAG:"+bIndex+"\t"+tag);
			
			nodes_exact_start_tags[k] = node_exact_start_tag;
			
			long node_in_head_tag = this.toNode_in_head_tag(bIndex, tag);
			network.addNode(node_in_head_tag);
			network.addEdge(node_exact_start_tag, new long[]{node_in_head_tag});
			network.addEdge(node_in_head_tag, new long[]{node_terminate});
			
			long node_after_head_tag = this.toNode_after_head_tag(bIndex, tag);
			network.addNode(node_after_head_tag);
			network.addEdge(node_after_head_tag, new long[]{node_terminate});
			
			if(bIndex<-1){
				long node_before_head_tag = this.toNode_before_head_tag(bIndex, tag);
				network.addNode(node_before_head_tag);
				network.addEdge(node_exact_start_tag, new long[]{node_before_head_tag});
				network.addEdge(node_exact_start_tag, new long[]{node_before_head_tag, node_in_head_tag});
				
				long node_before_head_tag_next = this.toNode_before_head_tag(bIndex+1, tag);
				long node_in_head_tag_next = this.toNode_in_head_tag(bIndex+1, tag);
				long node_after_head_tag_next = this.toNode_after_head_tag(bIndex+1, tag);
				
				network.addEdge(node_before_head_tag, new long[]{node_in_head_tag_next});
				if(network.contains(node_before_head_tag_next)){
					network.addEdge(node_before_head_tag, new long[]{node_before_head_tag_next});
					network.addEdge(node_before_head_tag, new long[]{node_before_head_tag_next, node_in_head_tag_next});
				}
				
				network.addEdge(node_in_head_tag, new long[]{node_in_head_tag_next});
				network.addEdge(node_in_head_tag, new long[]{node_after_head_tag_next});
				network.addEdge(node_in_head_tag, new long[]{node_in_head_tag_next, node_after_head_tag_next});
				
				network.addEdge(node_after_head_tag, new long[]{node_after_head_tag_next});
			}
			//FINISH IST
			
			network.addEdge(node_exact_start_tag, new long[]{node_terminate});
			//FINISH EST
		}
		
//		System.err.println("size="+nodes_exact_start_tags.length);
		network.addEdge(node_exact_start, nodes_exact_start_tags);
		//FINISH ES
		
		long node_root = this.toNode_root(bIndex);
		network.addNode(node_root);
		if(this._debug){
			System.err.println("ROOT:"+bIndex);
		}
		network.addEdge(node_root, new long[]{node_after_start});
		//FINISH ROOT
		
		if(bIndex!=-this._N)
			this.build(network, bIndex-1);
		
	}
	
	private long toNode_after_start(int bIndex){
		int srcHeight = - bIndex;
		int srcWidth = 0;
		int tgtHeight = srcHeight;
		int tgtWidth = this._tags.length + 3;
		int hybridType = IELinearHeadConfig.NODE_TYPE.AFTER_START.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_exact_start(int bIndex){
		int srcHeight = - bIndex;
		int srcWidth = 0;
		int tgtHeight = srcHeight;
		int tgtWidth = this._tags.length + 3;
		int hybridType = IELinearHeadConfig.NODE_TYPE.EXACT_START.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_exact_start_tag(int bIndex, SemanticTag tag){
		int srcHeight = - bIndex;
		int srcWidth = 0;
		int tgtHeight = srcHeight;
		int tgtWidth = tag.getId() + 1;
		int hybridType = IELinearHeadConfig.NODE_TYPE.EXACT_START_TAG.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_before_head_tag_numerator(int bIndex, SemanticTag tag, int len){
		int srcHeight = - bIndex;
		int srcWidth = srcHeight-len;
		int tgtHeight = srcHeight;
		int tgtWidth = tag.getId() + 1;
		int hybridType = IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_BEFORE_HEAD.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_in_head_tag_numerator(int bIndex, SemanticTag tag, int len){
		int srcHeight = - bIndex;
		int srcWidth = srcHeight-len;
		int tgtHeight = srcHeight;
		int tgtWidth = tag.getId() + 1;
		int hybridType = IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_WITHIN_HEAD.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_after_head_tag_numerator(int bIndex, SemanticTag tag, int len){
		int srcHeight = - bIndex;
		int srcWidth = srcHeight-len;
		int tgtHeight = srcHeight;
		int tgtWidth = tag.getId() + 1;
		int hybridType = IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_AFTER_HEAD.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_before_head_tag(int bIndex, SemanticTag tag){
		int srcHeight = - bIndex;
		int srcWidth = 0;
		int tgtHeight = srcHeight;
		int tgtWidth = tag.getId() + 1;
		int hybridType = IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_BEFORE_HEAD.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_in_head_tag(int bIndex, SemanticTag tag){
		int srcHeight = - bIndex;
		int srcWidth = 0;
		int tgtHeight = srcHeight;
		int tgtWidth = tag.getId() + 1;
		int hybridType = IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_WITHIN_HEAD.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_after_head_tag(int bIndex, SemanticTag tag){
		int srcHeight = - bIndex;
		int srcWidth = 0;
		int tgtHeight = srcHeight;
		int tgtWidth = tag.getId() + 1;
		int hybridType = IELinearHeadConfig.NODE_TYPE.INCOMPLETE_START_TAG_AFTER_HEAD.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_root(int bIndex){
		int srcHeight = - bIndex;
		int srcWidth = 0;
		int tgtHeight = srcHeight;
		int tgtWidth = this._tags.length + 4;
		int hybridType = IELinearHeadConfig.NODE_TYPE.ROOT.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
	private long toNode_terminate(){
		int srcHeight = 0;
		int srcWidth = 0;
		int tgtHeight = 0;
		int tgtWidth = 0;
		int hybridType = IELinearHeadConfig.NODE_TYPE.TERMINATE.ordinal();
		return NetworkIDMapper.toHybridNodeID(new int[]{srcHeight, srcWidth, tgtHeight, tgtWidth, hybridType});
	}
	
}
