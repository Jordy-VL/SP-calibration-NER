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

public class BitextInstance extends Instance{
	
	private static final long serialVersionUID = -7141979683583209233L;
	
	protected Sentence _src;
	protected Sentence _tgt;
	
	public BitextInstance(int id, Sentence src, Sentence tgt){
		super(id);
		this._src = src;
		this._tgt = tgt;
	}
	
	public Sentence getSrc(){
		return this._src;
	}
	
	public Sentence getTgt(){
		return this._tgt;
	}
	
}