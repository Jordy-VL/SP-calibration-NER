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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import com.statnlp.learn.core.LBFGS.ExceptionWithIflag;

public class NetworkParam implements Serializable{
	
	private static final long serialVersionUID = -1216927656396018976L;
	
	//these parameters are used for discriminative training using LBFGS.
	protected transient double _kappa;
	protected transient LBFGSOptimizer _opt;
	
	protected HashMap<String, HashMap<String, Integer>> _featureIntMap = new HashMap<String, HashMap<String, Integer>>();
	protected String[][] _feature2rep;
	protected double[] _weights;
	protected double[] _counts;
	protected double _obj;
	protected int _size;
	protected int _fixedFeaturesSize;
	protected int _version;
	protected boolean _locked = false;
	
	public int countFeatures(){
		return this._size;
	}
	
	public int countFixedFeatures(){
		return this._fixedFeaturesSize;
	}
	
	public boolean isFixed(int feature){
		return feature < this._fixedFeaturesSize;
	}
	
	public void preview_count() throws IOException{
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(System.err, "UTF-8"));
		
		HashMap<String, Double> map = new HashMap<String, Double>();
		for(int k = 0; k<this._counts.length; k++){
			if(this._counts[k]!=0){
				String f1 = this._feature2rep[k][1];
				if(!map.containsKey(f1)){
					map.put(f1, 0.0);
				}
				map.put(f1, map.get(f1)+this._counts[k]);
			}
		}
		
		Iterator<String> keys = map.keySet().iterator();
		while(keys.hasNext()){
			String unigram = keys.next();
			double count = map.get(unigram);
			out.println(unigram+"\t"+count);
			out.flush();
		}
		out.close();
	}
	
	public void preview(){
		
		try{
			PrintWriter out = new PrintWriter(new OutputStreamWriter(System.err, "UTF-8"));

			int K = 10;
			Iterator<String> f0s = this._featureIntMap.keySet().iterator();
			while(f0s.hasNext()){
				String f0 = f0s.next();
				/*
				if(f0.startsWith("NGRAM")){
				} else {
					continue;
				}
				*/
				HashMap<String, Integer> f1map = this._featureIntMap.get(f0);
				ArrayList<Integer> top_ids = new ArrayList<Integer>();
				ArrayList<Double> top_scores = new ArrayList<Double>();
				Iterator<String> f1s = f1map.keySet().iterator();
				while(f1s.hasNext()){
					String f1 = f1s.next();
					int id = f1map.get(f1);
					double score = this.getWeight(id);
					if(Double.isNaN(score)){
						continue;
					}
					if(Double.isInfinite(score))
						continue;
					int index = Collections.binarySearch(top_scores, score);
					if(index<0)
						index = -1-index;
					top_scores.add(index, score);
					top_ids.add(index, id);
					if(top_scores.size()>K){
						top_scores.remove(0);
						top_ids.remove(0);
					}
				}
				out.println(f0);
				for(int k = top_scores.size()-1; k>=0; k--){
					out.println(k+":\t"+this.getFeatureRep(top_ids.get(k))[1]+"\t"+Math.exp(top_scores.get(k)));
				}
				out.println();
				out.flush();
			}
//			out.close();
		} catch(Exception e){
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public NetworkParam(){
//		this._featureIntMap;
		this._locked = false;
		this._version = -1;
		this._size = 0;
		this._fixedFeaturesSize = 0;
//		this._weights = new double[0];
		this._obj = Double.NEGATIVE_INFINITY;
		if(!NetworkConfig.TRAIN_MODE_IS_GENERATIVE){
			this._opt = new LBFGSOptimizer();
			this._kappa = NetworkConfig.L2_REGULARIZATION_CONSTANT;
		}
	}

	public String[] getFeatureRep(int feature){
		return this._feature2rep[feature];
	}
	
	public void addCount(int feature, double count){
		
		if(this.isFixed(feature)){
			return;
		}
		
//		System.err.println(feature+"\t"+count+"<<<");
//		try{
//			String s = Arrays.toString(this.getFeatureRep(feature));
//			if(s.contains("法郎")){
//				PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
//				out.println(s+"\t"+count);
//				out.flush();
//			}
//		} catch(Exception e){
//			throw new RuntimeException(e.getMessage());
//		}
		
//		if(NetworkConfig.DEBUG_MODE){
//			String f0 = this.getFeatureRep(feature)[0];
//			String f1 = this.getFeatureRep(feature)[1];
//			if(f0.startsWith("NGRAM")){
////				System.err.println(f0+"\t"+f1);
//				Debugger.addCount("NGRAM2", f1, count);
//				f0 = f0.substring(0, f0.lastIndexOf("+"));
//				f1 = f0.substring(f0.lastIndexOf("+")+1).trim();
//				Debugger.addCount("NGRAM1", f1, count);
//			} else if(f0.startsWith("PATTERN")){
//				Debugger.addCount("PATTERN", f0, count);
//			} else if(f0.startsWith("TRANSITION")){
////				System.err.println(f0+"\t"+f1+"\t"+count);
//			}
//		}
		
//		System.err.println("add count:"+count+"\t"+java.util.Arrays.toString(this.getFeatureRep(feature)));
		if(NetworkConfig.TRAIN_MODE_IS_GENERATIVE){
			this._counts[feature] += count;
		} else {
			//in this case, we store negative gradient.
			if(NetworkConfig.EXP_MODE == NetworkConfig.EXP_LOCAL)
				this._counts[feature] -= count;
			else
				this._counts[feature] += count;
		}
	}
	
	public void addObj(double obj){
//		System.err.println("obj="+obj);
		if(NetworkConfig.TRAIN_MODE_IS_GENERATIVE){
			this._obj += obj;
		} else {
			if(NetworkConfig.EXP_MODE == NetworkConfig.EXP_LOCAL)
				this._obj += obj;
			else 
				this._obj -= obj;
		}
	}
	
	public double getObj(){
		return this._obj;
	}
	
	private double getCount(int feature){
		return this._counts[feature];
	}
	
	public double getWeight(int feature){
		return this._weights[feature];
	}
	
	public void setWeight(int feature, double weight){
		if(this.isFixed(feature)) return;
		this._weights[feature] = weight;
	}
	
//	public void lockIt(IBMModel1<String, String> ibm1){
//		if(this.isLocked()) return;
//		this._weights = new double[this._size];
//		this._counts = new double[this._size];
//		
//		this._feature2rep = new String[this._size][];
//		Iterator<String> keys1 = this._featureIntMap.keySet().iterator();
//		while(keys1.hasNext()){
//			String s1 = keys1.next();
//			HashMap<String, Integer> submap = this._featureIntMap.get(s1);
//			Iterator<String> keys2 = submap.keySet().iterator();
//			while(keys2.hasNext()){
//				String s2 = keys2.next();
//				int id = submap.get(s2);
//				this._feature2rep[id] = new String[]{s1, s2};
//			}
//		}
//		
//		for(int k = 0 ; k<this._size; k++){
//			String[] reps = this.getFeatureRep(k);
//			this._weights[k] = Math.log(ibm1.getProb_ext(reps[0], reps[1])); //NetworkConfig.FEATURE_INIT_WEIGHT;//Math.random()*10-5;//NetworkConfig.FEATURE_INIT_WEIGHT;
////			System.err.println(reps[0]+"/"+reps[1]+"="+this._weights[k]);
//		}
//		this.resetCountsAndObj();
//		this._version = 0;
//		this._locked = true;
//	}
	
	public void unlockForNewFeaturesAndFixCurrentFeatures(){
		if(!this.isLocked()) 
			throw new RuntimeException("This param is not locked.");
		this.fixCurrentFeatures();
		this._locked = false;
	}
	
	public void fixCurrentFeatures(){
		this._fixedFeaturesSize = this._size;
	}
	
	public void lockIt(){
//		System.err.println("LOCKED???");
		if(this.isLocked()) return;
		double[] weights_new = new double[this._size];
		this._counts = new double[this._size];
		for(int k = 0; k<this._fixedFeaturesSize; k++){
			weights_new[k] = this._weights[k];
		}
		for(int k = this._fixedFeaturesSize ; k<this._size; k++){
			weights_new[k] = NetworkConfig.FEATURE_INIT_WEIGHT;//Math.random()*10-5;//NetworkConfig.FEATURE_INIT_WEIGHT;
		}
		this._weights = weights_new;
		this.resetCountsAndObj();
		
		this._feature2rep = new String[this._size][];
		Iterator<String> keys1 = this._featureIntMap.keySet().iterator();
		while(keys1.hasNext()){
			String s1 = keys1.next();
			HashMap<String, Integer> submap = this._featureIntMap.get(s1);
			Iterator<String> keys2 = submap.keySet().iterator();
			while(keys2.hasNext()){
				String s2 = keys2.next();
				int id = submap.get(s2);
				this._feature2rep[id] = new String[]{s1, s2};
			}
		}
		this._version = 0;
		this._opt = new LBFGSOptimizer();
		this._locked = true;
	}
	
	public int size(){
		return this._size;
	}
	
	public boolean isLocked(){
		return this._locked;
	}
	
	public int getVersion(){
		return this._version;
	}
	
	public int toFeature(String s1, String s2){
		//if it is locked, then we might return a dummy feature
		//if the feature does not appear to be present.
		if(this.isLocked()){
			if(!this._featureIntMap.containsKey(s1))
				return -1;
			else if(!this._featureIntMap.get(s1).containsKey(s2))
				return -1;
			else
				return this._featureIntMap.get(s1).get(s2);
		}
		
		if(!this._featureIntMap.containsKey(s1))
			this._featureIntMap.put(s1, new HashMap<String, Integer>());
		
		HashMap<String, Integer> subMap = this._featureIntMap.get(s1);
		if(!subMap.containsKey(s2))
			subMap.put(s2, this._size++);
		return subMap.get(s2);
	}
	
	public boolean update(){
		if(NetworkConfig.TRAIN_MODE_IS_GENERATIVE){
			return this.updateGenerative();
		} else {
			return this.updateDiscriminative();
		}
	}
	
	//it will always return false, for now.
	private boolean updateGenerative(){
		
		Iterator<String> s1s = this._featureIntMap.keySet().iterator();
		while(s1s.hasNext()){
			String s1 = s1s.next();
			HashMap<String, Integer> map = this._featureIntMap.get(s1);
			Iterator<String> s2s;
			double sum = 0.0;
			
			s2s = map.keySet().iterator();
			while(s2s.hasNext()){
				String s2 = s2s.next();
				int feature = map.get(s2);
				sum += this.getCount(feature);
			}
			
//			if(sum==0){
//				System.err.print('+');
//			}
			
			s2s = map.keySet().iterator();
			while(s2s.hasNext()){
				String s2 = s2s.next();
				int feature = map.get(s2);
				double value = sum!=0 ? this.getCount(feature)/sum : 1.0/map.size();
				this.setWeight(feature, Math.log(value));
			}
		}
		this.resetCountsAndObj();
		this._version ++;
		return false;
	}
	
	private double _old_obj = -1;
	
	public double getOldObj(){
		return this._old_obj;
	}
	
	//if the optimization seems to be done, it will return true.
	protected boolean updateDiscriminative(){
		
    	this._opt.setVariables(this._weights);
    	this._opt.setObjective(-this._obj);
    	this._opt.setGradients(this._counts);
    	
    	boolean done = false;
    	
    	try{
        	done = this._opt.optimize();
    	} catch(ExceptionWithIflag e){
    		throw new NetworkException("Exception with Iflag:"+e.getMessage());
    	}
    	this.resetCountsAndObj();
		
		this._version ++;
		return done;
	}
	
	protected void resetCountsAndObj(){
		this._old_obj = this._obj;
		for(int k = 0 ; k<this._size; k++){
			this._counts[k] = 0.0;
			//for regularization
			if(!NetworkConfig.TRAIN_MODE_IS_GENERATIVE && this._kappa > 0 && k>=this._fixedFeaturesSize)
				this._counts[k] += 2 * this._kappa * this._weights[k];
		}
		this._obj = 0.0;
		//for regularization
		if(!NetworkConfig.TRAIN_MODE_IS_GENERATIVE && this._kappa > 0)
			this._obj -= this._kappa * MathsVector.square(this._weights);
	}
	
	public boolean checkEqual(NetworkParam p){
		boolean v1 = Arrays.equals(this._weights, p._weights);
		boolean v2 = Arrays.deepEquals(this._feature2rep, p._feature2rep);
		return v1 && v2;
	}
//	
//	private void writeObject(ObjectOutputStream out)throws IOException{
//		out.writeInt(this._feature2rep.length);
//		for(int k = 0; k<this._feature2rep.length; k++){
//			String[] rep = this._feature2rep[k];
//			out.writeUTF(rep[0]);
//			out.writeUTF(rep[1]);
//		}
//		out.writeInt(this._weights.length);
//		for(int k = 0; k<this._weights.length; k++){
//			out.writeDouble(this._weights[k]);
//		}
//		out.writeInt(this._size);
//		out.writeInt(this._version);
//		out.writeBoolean(this._locked);
//	}
//	
//	private void readObject(ObjectInputStream in) throws IOException{
//		this._feature2rep = new String[in.readInt()][];
//		this._featureIntMap = new HashMap<String, HashMap<String, Integer>>();
//		for(int k = 0; k<this._feature2rep.length; k++){
//			String f1 = in.readUTF();
//			String f2 = in.readUTF();
//			this._feature2rep[k] = new String[]{f1, f2};
//			if(!this._featureIntMap.containsKey(f1))
//				this._featureIntMap.put(f1, new HashMap<String, Integer>());
//			this._featureIntMap.get(f1).put(f2, k);
//		}
//		this._weights = new double[in.readInt()];
//		for(int k = 0; k<this._weights.length; k++)
//			this._weights[k] = in.readDouble();
//		this._size = in.readInt();
//		this._version = in.readInt();
//		this._locked = in.readBoolean();
//	}
	
}

