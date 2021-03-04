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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class HGrammarReader {
	
	public static HGrammar readGrammar(String filename) throws FileNotFoundException{
		
		Scanner scan = new Scanner(new File(filename));
		
		HGrammar g = new HGrammar();
		
		while(scan.hasNextLine()){
			String line = scan.nextLine().trim();
			if(line.indexOf("#")!=-1){
				line = line.substring(0, line.indexOf("#")).trim();
			}
			
			if(line.indexOf(".")!=-1){
				int index = line.indexOf(".");
				String p = line.substring(0, index).trim();
				HPattern.create(p.split("_"), g);
			}
			else if(line.startsWith("arity=")){
				line = line.trim();
				int arity = Integer.parseInt(line.substring(line.indexOf("=")+1).trim());
				line = scan.nextLine().trim();
				String[] pattern_strs = line.split("\\s");
				HPattern[] ps = new HPattern[pattern_strs.length];
				int k = 0;
				for(String pattern_str : pattern_strs){
					ps[k++] = HPattern.create(pattern_str.split("_"), g);
				}
				g.setPatternsByArity(arity, ps);
			}
			else if(line.indexOf("=")!=-1){
				int index = line.indexOf(":");
				String or = line.substring(0, index).trim();
				line = line.substring(index+1);
				
				String LHS = line.substring(0, line.indexOf("=")).trim();
				HPattern LHS_pattern = HPattern.create(LHS.split("_"), g);
				String RHSs = line.substring(line.indexOf("=")+2).trim();
				StringTokenizer st = new StringTokenizer(RHSs, "|");
				while(st.hasMoreTokens()){
					String token = st.nextToken().trim();
					String[] RHS = token.split("\\s");
					HPattern[] RHS_pattern = new HPattern[RHS.length];
					for(int k = 0; k<RHS.length; k++)
						RHS_pattern[k] = HPattern.create(RHS[k].split("_"), g);
					g.addRule(g.addOrientation(or), LHS_pattern, RHS_pattern);
				}
			}
		}
		
		return g;
	}
	
}
