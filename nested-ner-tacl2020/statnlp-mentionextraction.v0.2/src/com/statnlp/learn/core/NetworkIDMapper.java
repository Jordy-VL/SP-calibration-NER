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

//import com.statnlp.ie.flatsemi.FlatSemiConfig;
//import com.statnlp.ie.flatsemi.zeroth.FlatSemiZerothConfig;
//import com.statnlp.ie.linear.IELinearConfig;
//import com.statnlp.ie.linear.semi.IELinearSemiConfig;

public class NetworkIDMapper implements Serializable{
	
	private static final long serialVersionUID = -7101734566617861789L;

	public static final int ID_SRC_FOREST_HEIGHT 	= 0;
	public static final int ID_SRC_FOREST_WIDTH 	= 1;
	public static final int ID_TGT_FOREST_HEIGHT 	= 2;
	public static final int ID_TGT_FOREST_WIDTH 	= 3;
	public static final int ID_HYBRID_PATTERN 		= 4;
	
	public static int[] _CAPACITY_FOREST = new int[]{NetworkConfig._FOREST_MAX_HEIGHT+1, NetworkConfig._FOREST_MAX_WIDTH+1};
	
	//for both sides, the max height and max width are both 10000, and the max depth is 100
	public static int[] _CAPACITY_NETWORK = new int[]{_CAPACITY_FOREST[0],_CAPACITY_FOREST[1],
													  _CAPACITY_FOREST[0],_CAPACITY_FOREST[1],
													  NetworkConfig._NETWORK_MAX_DEPTH};
//	public static int[] _RESULT = new int[_CAPACITY_NETWORK.length];


	public static String viewHybridNode2(long node){
		int[] array = toHybridNodeArray(node);
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(array[0]);
		sb.append(',');
		sb.append(array[1]);
		sb.append(']');
		sb.append('X');
		sb.append('[');
		sb.append(array[2]);
		sb.append(',');
		sb.append(array[3]);
		sb.append(']');
		sb.append(array[4]);
		return sb.toString();
	}
	
	public static String viewHybridNode(long node){
		int[] array = toHybridNodeArray(node);
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(array[1]);
		sb.append(',');
		sb.append(array[0]+array[1]);
		sb.append(']');
		sb.append('X');
		sb.append('[');
		sb.append(array[2]);
		sb.append(',');
		sb.append(array[3]);
		sb.append(']');
		sb.append(array[4]);
		return sb.toString();
	}
	
	public static String viewHybridNode_bitext(long node){
		int[] array = toHybridNodeArray(node);
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(array[1]);
		sb.append(',');
		sb.append(array[0]+array[1]);
		sb.append(']');
		sb.append('X');
		sb.append('[');
		sb.append(array[3]);
		sb.append(',');
		sb.append(array[2]+array[3]);
		sb.append(']');
		sb.append(array[4]);
		return sb.toString();
	}

//	public static String viewHybridNode_semiie(long node, int N, int M){
//		int[] array = toHybridNodeArray(node);
//		StringBuilder sb = new StringBuilder();
//		sb.append('[');
//		sb.append(array[0]);
//		sb.append(',');
////		sb.append(array[4] ==0 || array[4] ==1 ? N + 2-array[1] : "--");
////		sb.append(',');
//		sb.append(array[3] == 0 ? "--" 
//				: array[3] == M + 3 ? "???"
//				: array[3] == M + 4 ? "???R"
//				: array[3]-1);
//		sb.append(']');
//		
//		String name = "xxx";
//		for(IELinearSemiConfig.NODE_TYPE t : IELinearSemiConfig.NODE_TYPE.values()){
//			if(t.ordinal() == array[4]){
//				name = t.name();
//				break;
//			}
//		}
//		
//		sb.append(name);
//		return sb.toString();
//	}
	
//	public static String viewHybridNode_ie(long node, int N, int M){
//		int[] array = toHybridNodeArray(node);
//		StringBuilder sb = new StringBuilder();
//		sb.append('[');
//		sb.append(array[0]);
//		sb.append(',');
////		sb.append(array[4] ==0 || array[4] ==1 ? N + 2-array[1] : "--");
////		sb.append(',');
//		sb.append(array[3] == 0 ? "--" 
//				: array[3] == M + 3 ? "??EXACT_START"
//				: array[3] == M + 4 ? "??ROOT"
//				: array[3]-1);
//		sb.append(']');
//		sb.append(array[4] == IELinearConfig.NODE_TYPE.ROOT.ordinal() ? IELinearConfig.NODE_TYPE.ROOT.name()
//				: array[4] == IELinearConfig.NODE_TYPE.AFTER_START.ordinal() ? IELinearConfig.NODE_TYPE.AFTER_START.name() 
//				: array[4] == IELinearConfig.NODE_TYPE.EXACT_START.ordinal() ? IELinearConfig.NODE_TYPE.EXACT_START.name() 
//				: array[4] == IELinearConfig.NODE_TYPE.EXACT_START_TAG.ordinal() ? IELinearConfig.NODE_TYPE.EXACT_START_TAG.name() 
//				: array[4] == IELinearConfig.NODE_TYPE.TERMINATE.ordinal() ? IELinearConfig.NODE_TYPE.TERMINATE.name()
//				: "XXXX");
//		return sb.toString();
//	}
//	
//	public static String viewHybridNode_semie_zeroth(long node, int N, int M){
//		int[] array = toHybridNodeArray(node);
//		StringBuilder sb = new StringBuilder();
//		sb.append('[');
//		sb.append(array[0]);
//		sb.append(',');
////		sb.append(array[4] ==0 || array[4] ==1 ? N + 2-array[1] : "--");
////		sb.append(',');
//		sb.append(array[3] == 0 ? "--" 
//				: array[3] == M + 3 ? "??EXACT_START"
//				: array[3] == M + 4 ? "??ROOT"
//				: array[3]-1);
//		sb.append(']');
//		sb.append(array[4] == FlatSemiZerothConfig.NODE_TYPE.ROOT.ordinal() ? FlatSemiZerothConfig.NODE_TYPE.ROOT.name()
//				: array[4] == FlatSemiZerothConfig.NODE_TYPE.BEFORE.ordinal() ? FlatSemiZerothConfig.NODE_TYPE.BEFORE.name() 
//				: array[4] == FlatSemiZerothConfig.NODE_TYPE.EXACT.ordinal() ? FlatSemiZerothConfig.NODE_TYPE.EXACT.name() 
//				: "XXXX");
//		return sb.toString();
//	}
//	
//	public static String viewHybridNode_semie(long node, int N, int M){
//		int[] array = toHybridNodeArray(node);
//		StringBuilder sb = new StringBuilder();
//		sb.append('[');
//		sb.append(array[0]);
//		sb.append(',');
////		sb.append(array[4] ==0 || array[4] ==1 ? N + 2-array[1] : "--");
////		sb.append(',');
//		sb.append(array[3] == 0 ? "--" 
//				: array[3] == M + 3 ? "??EXACT_START"
//				: array[3] == M + 4 ? "??ROOT"
//				: array[3]-1);
//		sb.append(']');
//		sb.append(array[4] == FlatSemiConfig.NODE_TYPE.ROOT.ordinal() ? FlatSemiConfig.NODE_TYPE.ROOT.name()
//				: array[4] == FlatSemiConfig.NODE_TYPE.BEFORE.ordinal() ? FlatSemiConfig.NODE_TYPE.BEFORE.name() 
//				: array[4] == FlatSemiConfig.NODE_TYPE.EXACT.ordinal() ? FlatSemiConfig.NODE_TYPE.EXACT.name() 
//				: "XXXX");
//		return sb.toString();
//	}
	
	public static int getElementId(int[] array, int element){
		return array[element];
	}
	
	public static int[] getCapacity(){
		return _CAPACITY_NETWORK;
	}
	
	public static long maxForestNodeID(){
		return toForestNodeID(new int[]{NetworkConfig._FOREST_MAX_HEIGHT, 
				NetworkConfig._FOREST_MAX_WIDTH});
	}
	
	public static long maxHybridNodeID(){
		int[] _RESULT = new int[5];
		for(int k = 0; k<_CAPACITY_NETWORK.length; k++)
			_RESULT[k] = _CAPACITY_NETWORK[k]-1;
		return toHybridNodeID(_RESULT);
	}
	
	public static int sizeOfHybridNode(int value){
		int[] array = toHybridNodeArray(value);
		return getElementId(array, ID_SRC_FOREST_HEIGHT) 
				* getElementId(array, ID_TGT_FOREST_HEIGHT);
	}
	
	//(height, position)
	public static int[] toForestNodeArray(long value){
		return new int[]{(int)(value / _CAPACITY_FOREST[1]),
				(int)(value % _CAPACITY_FOREST[1])};
	}
	
	public static long toForestNodeID(int[] array){
		assert array.length == 2;
		return array[0]*_CAPACITY_FOREST[1] + array[1];
	}
	
	public static int[] toHybridNodeArray(long value){
		int[] _RESULT = new int[5];
		for(int k = _RESULT.length-1 ; k>=1; k--){
			long v = value / _CAPACITY_NETWORK[k];
			_RESULT[k] = (int) (value % _CAPACITY_NETWORK[k]);
			value = v;
		}
		_RESULT[0] = (int)value;
		return _RESULT;
	}
	
	public static long toHybridNodeID(int[] array){
		assert array.length == 5;
		long v = array[0];
		for(int k = 1 ; k<array.length; k++)
			v = v* _CAPACITY_NETWORK[k] + array[k];
		return v;
	}
	
	public static long toHybridNodeID(long srcId, long tgtId, int patternId){
		long v = srcId * _CAPACITY_NETWORK[2] * _CAPACITY_NETWORK[3] + tgtId;
		v = v* _CAPACITY_NETWORK[4] + patternId;
		return v;
	}
	
}