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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class HGrammar implements Serializable{
	
	private static final long serialVersionUID = 2087144805307723950L;
	
	private HashMap<HPattern, ArrayList<HRule>> _hp2rules;
	private ArrayList<HPattern> _allHybridPatterns;
	private ArrayList<Pattern> _allPatterns;
	private ArrayList<Orientation> _ors;
	private HPattern[][] _psByArity;
	
	protected HPattern _ZZ;
	protected HPattern _XX;
	protected HPattern _YY;
	protected HPattern _XY;
	protected HPattern _YX;
	
	public HGrammar(){
		this._hp2rules = new HashMap<HPattern, ArrayList<HRule>>();
		this._allHybridPatterns = new ArrayList<HPattern>();
		this._allPatterns = new ArrayList<Pattern>();
		this._ors = new ArrayList<Orientation>();
		this._psByArity = new HPattern[3][];
	}
	
	public void setPatternsByArity(int arity, HPattern[] ps){
		this._psByArity[arity] = ps;
	}
	
	public HPattern[] getPatternsByArity(int arity){
		return this._psByArity[arity];
	}
	
	public HPattern getHybridPatternAt(int index){
		return this._allHybridPatterns.get(index);
	}
	
	public int countHybridPatterns(){
		return this._allHybridPatterns.size();
	}
	
	public boolean isTerminal(HPattern p){
		if(p.isXX() || p.isYY() || p.isZZ() || p.isXY() || p.isYX())
			return false;
		
		if(this._hp2rules.containsKey(p)){
			if(this._hp2rules.get(p).size()>=1)
				return false;
		}
		return true;
	}

	public HPattern getZZ(){
		return this._ZZ;
	}
	
	public HPattern getXX(){
		return this._XX;
	}
	
	public HPattern getYY(){
		return this._YY;
	}
	
	public HPattern getXY(){
		return this._XY;
	}
	
	public HPattern getYX(){
		return this._YX;
	}
	
	public Orientation addOrientation(String form){
		Orientation p = new Orientation(form);
		int index = this._ors.indexOf(p);
		if(index==-1){
			this._ors.add(p);
		} else {
			p = this._ors.get(index);
		}
		return p;
	}
	
	public Pattern addPattern(String form){
		Pattern p = new Pattern(form);
		int index = this._allPatterns.indexOf(p);
		if(index==-1){
			this._allPatterns.add(p);
		} else {
			p = this._allPatterns.get(index);
		}
		return p;
	}
	
	public HPattern getHybridPatternById(int id){
		return this._allHybridPatterns.get(id);
	}
	
	public HPattern addHybridPattern(String[] form){
		Pattern[] pts = new Pattern[form.length];
		for(int k = 0; k<pts.length; k++)
			pts[k] = this.addPattern(form[k]);
		
		HPattern result = new HPattern(pts);
		int index = this._allHybridPatterns.indexOf(result);
		if(index<0){
			result.setId(this._allHybridPatterns.size());
			this._allHybridPatterns.add(result);
			result._g = this;
			this._hp2rules.put(result, new ArrayList<HRule>());
		} else {
			result = this._allHybridPatterns.get(index);
		}
		if(form.length < 2){
			throw new NetworkException("HybridGrammar Reader: The forms should have at least two entires:"+Arrays.toString(form));
		}
		
		if(result.isZZ())
			this._ZZ = result;
		else if(result.isXX())
			this._XX = result;
		else if(result.isYY())
			this._YY = result;
		else if(result.isXY())
			this._XY = result;
		else if(result.isYX())
			this._YX = result;
		
		return result;
	}
	
	public void addRule(Orientation or, HPattern LHS, HPattern[] RHS){
		if(!this._hp2rules.containsKey(LHS))
			this._hp2rules.put(LHS, new ArrayList<HRule>());
		HRule rule = new HRule(LHS, RHS, or);
		this._hp2rules.get(LHS).add(rule);
	}
	
	public ArrayList<HRule> getRules(HPattern LHS){
		return this._hp2rules.get(LHS);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(HPattern hp : this._allHybridPatterns){
			sb.append(hp);
			sb.append(':');
			ArrayList<HRule> ruleList = this._hp2rules.get(hp);
			int i = 0;
			for(HRule rule : ruleList){
				if(i++!= 0){
					sb.append(' ');
					sb.append('|');
					sb.append(' ');
				}
				sb.append(rule.toString());
			}
			sb.append('\n');
		}
		sb.append('\n');
		
		sb.append("All Patterns:"+this._allPatterns.size());
		sb.append('\n');
		for(Pattern p : this._allPatterns){
			sb.append(p);
			sb.append('\n');
		}
		sb.append('\n');
		
		sb.append("XYZ:");
		sb.append('\n');
		sb.append("ZZ="+this._ZZ);
		sb.append('\n');
		sb.append("XX="+this._XX);
		sb.append('\n');
		sb.append("YY="+this._YY);
		sb.append('\n');
		sb.append("XY="+this._XY);
		sb.append('\n');
		sb.append("YX="+this._YX);
		sb.append('\n');

		return sb.toString();
	}
	
}
