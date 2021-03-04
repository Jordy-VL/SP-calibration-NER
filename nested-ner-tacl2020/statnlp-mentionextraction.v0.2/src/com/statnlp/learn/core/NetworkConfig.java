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

public class NetworkConfig {
	
	public static boolean CACHE_FEATURES_AT_CONJUNCTIVE_CELLS = true;
	public static double FEATURE_INIT_WEIGHT = 0;//-Math.random();//0;//Math.log(1);
	public static final int EXP_LOCAL = 0;//LOCAL will be used for both generative and discriminative learning.
	public static final int EXP_GLOBAL = 1;//GLOBAL will be used for discriminative learning.
	public static int PRUNE_MIN_LENGTH = 1000;
	public static double PRUNE_THRESHOLD = 1000;
	public static boolean TRAIN_MODE_IS_GENERATIVE = true;
	public static boolean CACHE_FEATURE_SCORES = false;
	public static int EXP_MODE = EXP_LOCAL;
	public static boolean diagco = false;
	public static int[] iprint = {0,0};
	public static double eps = 10e-3;
	public static double xtol = 10e-16;
	public static int[] iflag = {0};
	public static double L2_REGULARIZATION_CONSTANT = 0.01;
	public static int _FOREST_MAX_HEIGHT = 10000;
	public static int _FOREST_MAX_WIDTH = 10000;
	public static int _NETWORK_MAX_DEPTH = 901;
	public static int _nGRAM = 1;//2;//1;
	
	public static boolean DEBUG_MODE = false;//true;//false;//true;
	public static boolean REBUILD_FOREST_EVERY_TIME = false;
	
}