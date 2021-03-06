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

import com.statnlp.ie.types.LabeledTextSpan;
import com.statnlp.learn.core.Instance;
import com.statnlp.learn.core.TextSpan;

public abstract class IELinearHeadInstance extends Instance{
	
	private static final long serialVersionUID = -1879109898850269290L;

	protected TextSpan _span;

	public IELinearHeadInstance(int id, TextSpan span) {
		super(id);
		this._span = span;
	}
	
	public TextSpan getSpan(){
		return this._span;
	}

	public boolean isLabeled(){
		return this._span instanceof LabeledTextSpan;
	}
	
	public int length(){
		return this._span.length();
	}
	
	public abstract IELinearHeadInstance removeLabels();
	
}
