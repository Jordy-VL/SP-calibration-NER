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
package com.statnlp.ie.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com.statnlp.learn.core.TextSpan;

public class UnlabeledTextSpan extends TextSpan{
	
	private static final long serialVersionUID = -9108224176079369166L;
	
	private HashMap<Segment, ArrayList<Mention>> _corr_map;
	private HashMap<Segment, ArrayList<Mention>> _pred_map;

	public String viewCorrect(){
		StringBuilder sb = new StringBuilder();
		Iterator<Segment> segments = this._corr_map.keySet().iterator();
		while(segments.hasNext()){
			Segment segment = segments.next();
			ArrayList<Mention> mentions = this._corr_map.get(segment);
			sb.append(mentions.toString());
		}
		return sb.toString();
	}

	public String viewPredict(){
		StringBuilder sb = new StringBuilder();
		Iterator<Segment> segments = this._pred_map.keySet().iterator();
		while(segments.hasNext()){
			Segment segment = segments.next();
			ArrayList<Mention> mentions = this._pred_map.get(segment);
			sb.append(mentions.toString());
		}
		return sb.toString();
	}
	
	public String printPredictions(){
		StringBuilder sb = new StringBuilder();
		
		for(int k = 0; k<this.length(); k++){
			if(k!=0)
				sb.append(' ');
			sb.append(this.getWord(k).getName());
		}
		sb.append('\n');
		for(int k = 0; k<this.length(); k++){
			if(k!=0)
				sb.append(' ');
			sb.append(this.getWord(k).getAttribute("POS").get(0));
		}
		sb.append('\n');
		Iterator<Segment> segments = this._pred_map.keySet().iterator();
		boolean start = true;
		while(segments.hasNext()){
			Segment segment = segments.next();
			ArrayList<Mention> mentions = this._pred_map.get(segment);
			for(Mention mention : mentions){
				if(start){
					start = !start;
				} else {
					sb.append('|');
				}
				sb.append(segment.getBIndex()+","+segment.getEIndex()+" "+mention.getTag().getName());
			}
		}
		sb.append('\n');
		
		return sb.toString();
	}
	
	public UnlabeledTextSpan(LabeledTextSpan lspan) {
		super(lspan._words, lspan._bIndex, lspan._eIndex);
		this._corr_map = new HashMap<Segment, ArrayList<Mention>>();
		Iterator<Segment> segs;
		segs = lspan._map_supported.keySet().iterator();
		while(segs.hasNext()){
			Segment seg = segs.next();
			ArrayList<Mention> tags = lspan._map_supported.get(seg);
			this._corr_map.put(seg, tags);
		}
		segs = lspan._map_not_supported.keySet().iterator();
		while(segs.hasNext()){
			Segment seg = segs.next();
			ArrayList<Mention> tags = lspan._map_not_supported.get(seg);
			if(this._corr_map.containsKey(seg)){
				throw new RuntimeException("Weird...");
			}
			this._corr_map.put(seg, tags);
		}
		this._pred_map = new HashMap<Segment, ArrayList<Mention>>();
	}
	
	public boolean label_predict(int bIndex, int eIndex, Mention cat){
		Segment seg = new Segment(bIndex, eIndex);
		if(!this._pred_map.containsKey(seg))
			this._pred_map.put(seg, new ArrayList<Mention>());
		this._pred_map.get(seg).add(cat);
		
		if(!this._corr_map.containsKey(seg)){
			return false;
		}
		if(!this._corr_map.get(seg).contains(cat)){
			return false;
		}
		return true;
	}
	

//	public double countSupported(){
//		double count = 0;
//		Iterator<Segment> segments = this._corr_map.keySet().iterator();
//		while(segments.hasNext()){
//			Segment segment = segments.next();
//			if(this.isStartSegment(segment) || this.isFinishSegment(segment))
//				continue;
//			if(segment.length()>IELinearSemiConfig._MAX_MENTION_LEN)
//				continue;
//			
//			count += this._corr_map.get(segment).size();
//		}
//		return count;
//	}
	
	public double countExpected(){
		double count = 0;
		Iterator<Segment> segments = this._corr_map.keySet().iterator();
		while(segments.hasNext()){
			Segment segment = segments.next();
			if(this.isStartSegment(segment) || this.isFinishSegment(segment))
				continue;
			
			count += this._corr_map.get(segment).size();
		}
		return count;
	}
	
	public double countPredicted(){
		double count = 0;
		Iterator<Segment> segments = this._pred_map.keySet().iterator();
		while(segments.hasNext()){
			Segment segment = segments.next();
			if(this.isStartSegment(segment) || this.isFinishSegment(segment))
				continue;
			
			count += this._pred_map.get(segment).size();
		}
		return count;
	}
	

	public double countCorrect_span(){
		double count = 0;
		Iterator<Segment> segments = this._pred_map.keySet().iterator();
		while(segments.hasNext()){
			Segment segment = segments.next();
			if(this.isStartSegment(segment) || this.isFinishSegment(segment))
				continue;
			
			ArrayList<Mention> mentions_pred = this._pred_map.get(segment);
			if(this._corr_map.containsKey(segment)){
				ArrayList<Mention> mentions_corr = this._corr_map.get(segment);
				for(int k = 0; k<mentions_pred.size(); k++){
					Mention mention_pred = mentions_pred.get(k);
					for(int j = 0; j<mentions_corr.size(); j++){
						Mention mention_corr = mentions_corr.get(j);
						if(mention_pred.spanMatches(mention_corr)){
							count ++;
							break;
						}
					}
				}
			}
		}
		return count;
	}
	
	public double countCorrect(){
		double count = 0;
		Iterator<Segment> segments = this._pred_map.keySet().iterator();
		while(segments.hasNext()){
			Segment segment = segments.next();
			if(this.isStartSegment(segment) || this.isFinishSegment(segment))
				continue;
			
			ArrayList<Mention> mentions_pred = this._pred_map.get(segment);
			if(this._corr_map.containsKey(segment)){
				ArrayList<Mention> mentions_corr = this._corr_map.get(segment);
				for(int k = 0; k<mentions_pred.size(); k++){
					Mention mention_pred = mentions_pred.get(k);
					for(int j = 0; j<mentions_corr.size(); j++){
						Mention mention_corr = mentions_corr.get(j);
						if(mention_pred.equals(mention_corr)){
							count ++;
						}
					}
				}
			}
		}
		return count;
	}

	private boolean isStartSegment(Segment segment){
		return segment.getBIndex() == -1 && segment.getEIndex() == 0;
	}
	
	private boolean isFinishSegment(Segment segment){
		return segment.getBIndex() == this.length() && segment.getEIndex() == this.length()+1;
	}
	
}