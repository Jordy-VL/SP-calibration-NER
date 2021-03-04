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
package com.statnlp.ie.mention.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import com.statnlp.ie.mention.core.IELinearFeatureManager;
import com.statnlp.ie.mention.core.IELinearInstance;
import com.statnlp.ie.mention.core.IELinearModel;
import com.statnlp.ie.types.Event;
import com.statnlp.ie.types.EventRole;
import com.statnlp.ie.types.EventTemplate;
import com.statnlp.ie.types.IEManager;
import com.statnlp.ie.types.LabeledTextSpan;
import com.statnlp.ie.types.Mention;
import com.statnlp.ie.types.MentionType;
import com.statnlp.ie.types.UnlabeledTextSpan;
import com.statnlp.learn.core.AttributedWord;
import com.statnlp.learn.core.NetworkConfig;
import com.statnlp.learn.core.NetworkParam;
import com.statnlp.learn.core.WordUtil;

public class MentionLearnerAndExtractor {
	
	private static ArrayList<String> findFuncWords(ArrayList<LabeledTextSpan> spans_train){
		HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
		ArrayList<String> words = new ArrayList<String>();
		for(int k = 0; k<spans_train.size(); k++){
			LabeledTextSpan span = spans_train.get(k);
			ArrayList<Mention> mentions = span.getAllMentions();
			for(Mention mention : mentions){
				int bIndex = mention.getSegment().getBIndex();
				int eIndex = mention.getSegment().getEIndex();
				for(int index = bIndex; index<eIndex; index++){
					String word = span.getWord(index).getName();
					if(!WordUtil.isAllLowerCase(word)){
						continue;
					}
					if(WordUtil.isNumber(word)){
						continue;
					}
					if(!WordUtil.isAllLetters(word)){
						continue;
					}
					if(WordUtil.isPunctuationMark(word)){
						continue;
					}
					if(!wordMap.containsKey(word)){
						wordMap.put(word, 1);
					} else {
						int oldCount = wordMap.get(word);
						wordMap.put(word, oldCount+1);
					}
				}
			}
		}
		
		Iterator<String> keys = wordMap.keySet().iterator();
		while(keys.hasNext()){
			String word = keys.next();
			int count = wordMap.get(word);
			if(count>=3){
//				System.err.println(word);
				words.add(word);
			}
		}
		
		return words;
	}
	
	public static void main(String args[])throws IOException, ClassNotFoundException{
		int num_itrs = Integer.parseInt(System.getProperty("numIter", "2000"));
		
		System.err.println();
		System.err.println("Mention Extraction System with Mention Hypergraphs");
		System.err.println();
		System.err.println("This software is strictly for research purposes.");
				
		if(args.length<3){
			System.err.println("Invalid arguments.");
			System.exit(1);
		}
		
		String filename_template = args[0];
		String filename_model = args[1];
		String filename_train = args[2];
		String filename_test = args[3];
		
		long bTime_overall = System.currentTimeMillis();

		NetworkConfig.L2_REGULARIZATION_CONSTANT = 0.01;
		NetworkConfig.TRAIN_MODE_IS_GENERATIVE = false;
		
		IEManager manager = readIEManager(filename_template);
		NetworkParam param = new NetworkParam();
		
		IELinearFeatureManager fm = new IELinearFeatureManager(null, param);
		
		String filename_input;
		Scanner scan;
		
		filename_input = filename_train;
		scan = new Scanner(new File(filename_input));
		ArrayList<LabeledTextSpan> spans_train; 
		ArrayList<LabeledTextSpan> spans_dev;
		
		spans_train = readTextSpans(scan, manager, true);
		spans_dev = new ArrayList<LabeledTextSpan>();
		
		int train_size = spans_train.size();
		
		ArrayList<IELinearInstance> instances = new ArrayList<IELinearInstance>();
		for(int k = 0; k<train_size; k++){
			MentionLinearInstance inst = new MentionLinearInstance(k+1, spans_train.get(k), manager.getMentionTemplate());
			instances.add(inst);
		}
		
		System.err.println("There are "+instances.size()+" training instances.");
		System.err.println("There are "+spans_dev.size()+" development instances.");
		
		IELinearModel model = new IELinearModel(fm, manager.getMentionTemplate().getAllTypesExcludingStartAndFinish_arr());
		
		try{
			model.train(instances, num_itrs, manager, param, filename_model);
		} catch(Exception e){
			System.err.println("Okay done.");
		}
		
		long eTime_overall = System.currentTimeMillis();
		
		System.err.println("Learning completes. Overall time:"+(eTime_overall-bTime_overall)/1000.0+" secs.");
		
		System.err.println("Saving Model...");
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename_model));
		out.writeObject(param);
		out.flush();
		out.writeObject(manager);
		out.flush();
		out.writeObject(WordUtil._func_words);
		out.flush();
		out.close();
		System.err.println("Model Saved.");
		
		{
			bTime_overall = System.currentTimeMillis();
			
			filename_input = filename_test;
			scan = new Scanner(new File(filename_input));
			ArrayList<LabeledTextSpan> spans_test;
			spans_test = readTextSpans(scan, manager, false);
			
			int test_size = spans_test.size();
			
			double count_pred;
			double count_corr_span;
			double count_expt;
			
			double P;
			double R;
			double F;
			
			count_corr_span = 0;
			count_pred = 0;
			count_expt = 0;
			
			double bTime = System.currentTimeMillis();
			double num_words = 0;
			
			System.err.println("=Evaluation Result=");
			for(int k = 0; k<test_size; k++){
				MentionLinearInstance inst = new MentionLinearInstance(k+1, spans_test.get(k), manager.getMentionTemplate());
				MentionLinearInstance inst_unlabeled = inst.removeLabels();
				
				model.max(inst_unlabeled);
				
				UnlabeledTextSpan span = (UnlabeledTextSpan)inst_unlabeled.getSpan();
				count_corr_span += span.countCorrect_span();
				count_pred += span.countPredicted();
				count_expt += span.countExpected();
				
				num_words += inst.length();
			}
			double eTime = System.currentTimeMillis();
			
			P = count_corr_span/count_pred;
			R = count_corr_span/count_expt;
			F = 2/(1/P+1/R);
			
			double time = (eTime-bTime)/1000.0;
			System.err.printf("Time: %.3fs seconds\n", time);
			System.err.printf("#words = %.0f\n", num_words);
			System.err.printf("#words/sec = %.2f\n", num_words/time);

	        System.err.printf("P: %6.2f%% = %.0f/%.0f\n", 100*P, count_corr_span, count_pred);
	        System.err.printf("R: %6.2f%% = %.0f/%.0f\n", 100*R, count_corr_span, count_expt);
	        System.err.printf("F: %6.2f%%\n", 100*F);
			
			eTime_overall = System.currentTimeMillis();
			
			System.err.println("Evaluation completes. Overall time: "+(eTime_overall-bTime_overall)/1000.0+" secs.");
			
		}
		
	}
	
	public static ArrayList<LabeledTextSpan> readTextSpans(Scanner scan, IEManager manager, boolean isTrain) throws FileNotFoundException{
		
		ArrayList<LabeledTextSpan> spans = new ArrayList<LabeledTextSpan>();
		
//		int num_instances = 0;
		while(scan.hasNextLine()){
			String[] words = scan.nextLine().split("\\s");
			String[] tags = scan.nextLine().split("\\s");
			String annot = scan.nextLine();
			String[] annotations;
			if(!annot.equals("")){
				annotations = annot.split("\\|");
			} else {
				annotations = new String[0];
			}
			
			AttributedWord[] aws = new AttributedWord[words.length];
			for(int k = 0; k<words.length; k++){
				aws[k] = new AttributedWord(words[k]);
				aws[k].addAttribute("POS", tags[k]);
			}
			
			LabeledTextSpan span = new LabeledTextSpan(aws);
			for(int k = 0; k<annotations.length; k++){
				String[] annotation = annotations[k].split("\\s");
				String[] indices = annotation[0].split(",");
				int bIndex, eIndex, head_bIndex, head_eIndex;
				if(indices.length == 2){
					bIndex = Integer.parseInt(indices[0]);
					eIndex = Integer.parseInt(indices[1]);
					head_bIndex = Integer.parseInt(indices[0]);
					head_eIndex = Integer.parseInt(indices[1]);
				} else if(indices.length == 4){
					bIndex = Integer.parseInt(indices[0]);
					eIndex = Integer.parseInt(indices[1]);
					head_bIndex = Integer.parseInt(indices[2]);
					head_eIndex = Integer.parseInt(indices[3]);
				} else {
					throw new RuntimeException("The number of indices is "+indices.length);
				}
				String label = annotation[1];
				span.label(bIndex, eIndex, new Mention(bIndex, eIndex, head_bIndex, head_eIndex, manager.toMentionType(label)));
			}
			
			spans.add(span);
			
			if(words.length!=tags.length){
				throw new RuntimeException("The lengths between words and tags are not the same!");
			}
			
			scan.nextLine();
		}
		
		if(isTrain){
			ArrayList<String> funcwords = findFuncWords(spans);
			WordUtil.setFunctionWords(funcwords);
		}
		
		String ne_word_type = "NE_WORD_TYPE";
		
		for(LabeledTextSpan span : spans){
			for(int i = 0; i<span.length(); i++){
				AttributedWord word = span.getWord(i);
				word.addAttribute(ne_word_type, WordUtil.getNEWordType(word.getName()));
			}
		}
		
		for(LabeledTextSpan span : spans){
			span.expandAtt_WORD();
			span.expandAtt_POS();
			span.expandAtt_BOW();
//			span.expandAtt_NER();
			span.expandAtt_NE_WORD_TYPE();
		}
		
		return spans;
	}
	
	public static IEManager readIEManager(String filename) throws IOException{
		
		IEManager manager = new IEManager();
		
		Scanner scan = new Scanner(new File(filename));
		while(scan.hasNextLine()){
			String line = scan.nextLine();
			
			int bIndex = line.indexOf("{");
			int eIndex = line.lastIndexOf("}");
			String[] eventNames = line.substring(0, bIndex).trim().split(":");
			Event event = manager.toEvent(eventNames[0], eventNames[1]);
			
			String[] roles_with_mentions = line.substring(bIndex+1, eIndex).trim().split("\\s");
			EventRole[] roles = new EventRole[roles_with_mentions.length];
			
			for(int i = 0; i<roles.length; i++){
				String role_with_mentions = roles_with_mentions[i];
				bIndex = role_with_mentions.indexOf("[");
				eIndex = role_with_mentions.lastIndexOf("]");
				String roleName = role_with_mentions.substring(0, bIndex).trim();
				String[] mentions = role_with_mentions.substring(bIndex+1, eIndex).trim().split("\\|");
				MentionType[] types = new MentionType[mentions.length];
				for(int k = 0; k<mentions.length; k++)
					types[k] = manager.toMentionType(mentions[k]);
				roles[i] = manager.toEventRole(event, roleName);
				roles[i].setCompatibleTypes(types);
			}
			
			EventTemplate template = new EventTemplate(manager, event, roles);
			manager.addEventTemplate(template);
		}
		
		manager.finalize();
		
		return manager;
		
	}
}
