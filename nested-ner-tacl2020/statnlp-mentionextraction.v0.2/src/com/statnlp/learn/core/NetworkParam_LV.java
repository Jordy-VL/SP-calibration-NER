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

import com.statnlp.learn.core.LBFGS.ExceptionWithIflag;

public class NetworkParam_LV extends NetworkParam{
	
	private static final long serialVersionUID = -9009360142466925960L;
	
	private double _log_factor=0.0;
	
	public void setFactorAndSwitchToOptimizeLikelihood(double log_factor){
		this._log_factor = log_factor;
		System.err.println("factor now is set to:"+this._log_factor);
	}
	
	@Override
	//if the optimization seems to be done, it will return true.
	protected boolean updateDiscriminative(){
		
		if(this._log_factor==0.0){
			System.err.println("factor1="+this._log_factor);
			return super.updateDiscriminative();
		}
		
//		if(this.getOldObj()>-20){
//			this._log_factor = -this.getOldObj();
//		}
		
		System.err.println("factor2="+this._log_factor);
		
    	this._opt.setVariables(this._weights);
    	this._opt.setObjective(-Math.exp(this._obj + this._log_factor));
    	System.err.println("c*Likelihood="+(Math.exp(this._obj + this._log_factor)));
    	for(int k = 0; k<this._counts.length; k++)
    		this._counts[k] *= Math.exp(this._obj+this._log_factor);
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
	
}

