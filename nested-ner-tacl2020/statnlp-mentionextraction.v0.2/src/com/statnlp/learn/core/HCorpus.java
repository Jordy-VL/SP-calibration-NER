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


public class HCorpus extends Corpus{
	
	private static final long serialVersionUID = -5434657898383141420L;
	
	@Override
	public void add(Instance inst){
		this._instances.add((HInstance)inst);
	}
	
	@Override
	public HInstance get(int k){
		return (HInstance)this._instances.get(k);
	}
	
}