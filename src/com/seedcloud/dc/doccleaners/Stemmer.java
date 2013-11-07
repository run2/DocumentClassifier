package com.seedcloud.dc.doccleaners;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;


public class Stemmer implements DocumentCleaner{

	@SuppressWarnings("deprecation")
	public String clean(String input)throws IOException {
	    Set<String> stopWords = new HashSet<String>();
	    //for(String word : commonWords)
	    	//stopWords.add(word);

	    TokenStream tokenStream = new StandardTokenizer(
	            Version.LUCENE_30, new StringReader(input));
	    tokenStream = new StopFilter(true, tokenStream, stopWords);
	    tokenStream = new PorterStemFilter(tokenStream);

	    StringBuilder sb = new StringBuilder();
	    TermAttribute termAttr = tokenStream.getAttribute(TermAttribute.class);
	    while (tokenStream.incrementToken()) {
	        if (sb.length() > 0) {
	            sb.append(" ");
	        }
	        sb.append(termAttr.term());
	    }
	    tokenStream.close();
	    return sb.toString();
	}		
}
