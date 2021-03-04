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

public class MathsVector implements Serializable{
	
	private static final long serialVersionUID = -6493241066565944244L;
	
	private double[] _v;
	private double _L1 = -1;
	private double _L2 = -1;

	public MathsVector(double[] v){
		this._v = v;
	}
	
	public static double square(double[] x){
		double v = 0.0;
		for(int k = 0; k<x.length; k++)
			v += x[k] * x[k];
		return v;
	}
	
	public MathsVector(int size){
		this._v = new double[size];
	}
	
	public int size(){
		return this._v.length;
	}
	
	public double dotProd(MathsVector u){
		assert u.size() == this.size();
		double r = 0.0;
		for(int k = 0; k<u.size(); k++)
			r += this._v[k]*u._v[k];
		return r;
	}

	public void add(MathsVector u){
		assert this.size() == u.size();
		for(int k = 0; k<u.size(); k++)
			this._v[k] += u._v[k];
	}
	
	public double square(){
		double v = L2_norm();
		return v * v;
	}
	
	public double L2_norm(){
		if(this._L2 >=0)
			return this._L2;
		this._L2 = 0.0;
		for(double u : this._v)
			this._L2 += u * u;
		this._L2 = Math.sqrt(this._L2);
		return this._L2;
	}
	
	public double L1_norm(){
		if(this._L1 >=0)
			return this._L1;
		this._L1 = 0.0;
		for(double u : this._v)
			this._L1 += Math.abs(u);
		return this._L1;
	}
	
	@Override
	public String toString(){
		return Arrays.toString(this._v);
	}
	
}