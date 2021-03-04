package com.statnlp.ie.mention.main;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import com.statnlp.ie.types.IEManager;
import com.statnlp.ie.types.LabeledTextSpan;
import com.statnlp.ie.types.Mention;
import com.statnlp.ie.types.UnlabeledTextSpan;

public class MentionEvaluator {
	
	public static void main(String[] args) throws Exception{
		IEManager manager = new IEManager();
		Scanner goldReader = new Scanner(new File(args[0]));
		ArrayList<LabeledTextSpan> spans_gold = MentionLearner.readTextSpans(goldReader, manager, true);
		goldReader.close();
		Scanner predReader = new Scanner(new File(args[1]));
		ArrayList<LabeledTextSpan> spans_pred = MentionLearner.readTextSpans(predReader, manager, false);
		predReader.close();
		

		double count_pred;
		double count_corr_span;
		double count_expt;
		
		double P;
		double R;
		double F;
		
		count_corr_span = 0;
		count_pred = 0;
		count_expt = 0;
		
		System.err.println("=Evaluation Result=");
		for(int k = 0; k<spans_gold.size(); k++){
			MentionLinearInstance inst = new MentionLinearInstance(k+1, spans_gold.get(k), manager.getMentionTemplate());
			MentionLinearInstance inst_unlabeled = inst.removeLabels();
			UnlabeledTextSpan span = (UnlabeledTextSpan)inst_unlabeled.getSpan();
			
			MentionLinearInstance inst_predicted = new MentionLinearInstance(k+1, spans_pred.get(k), manager.getMentionTemplate());
			for(Mention mention: ((LabeledTextSpan)inst_predicted.getSpan()).getAllMentions()){
				span.label_predict(mention.getSegment().getBIndex(), mention.getSegment().getEIndex(), mention);
			}
			
			count_corr_span += span.countCorrect_span();
			count_pred += span.countPredicted();
			count_expt += span.countExpected();
		}
		
		P = count_corr_span/count_pred;
		R = count_corr_span/count_expt;
		F = 2/(1/P+1/R);

        System.err.printf("P: %6.2f%% = %.0f/%.0f\n", 100*P, count_corr_span, count_pred);
        System.err.printf("R: %6.2f%% = %.0f/%.0f\n", 100*R, count_corr_span, count_expt);
        System.err.printf("F: %6.2f%%\n", 100*F);
	}

}
