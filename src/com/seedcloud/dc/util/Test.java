package com.seedcloud.dc.util;

import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

public class Test {
	public static void main(String args[]){
		try{
			Tokenizer wsTokenizer = new WhitespaceTokenizer(new StringReader("aa bb")); 
			ShingleFilter filter = new ShingleFilter(wsTokenizer, 3);
			filter.setOutputUnigrams(true);
			TermAttribute termAtt = (TermAttribute) filter.getAttribute(TermAttribute.class); 
			while (filter.incrementToken())            
				System.out.println(termAtt.term()); 
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
}
