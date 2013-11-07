package com.seedcloud.dc.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;


import com.seedcloud.dc.doccleaners.DocumentCleaner;
import com.seedcloud.dc.trainer.DocumentClassifierTrainNV;

public class WordsInDocument {
	// add the concept of position of word
	Map<String, Long> words;
	Scanner fileScanner;
	
	// Iterator<String> wordsIterator;
	long wordCountNoDuplicates = 0;
	long wordCountWithDuplicates = 0;
	public static final boolean DEBUG = false;
	public static boolean ngrams = false;

	List<DocumentCleaner> cleaners;
	
	String docPath ;
	private static void debug(String debug) {
		if (DEBUG)
			DocumentClassifierTrainNV.logger.info(debug);
			//System.out.println(debug);
	}
	
	public void addDocumentCleaners(DocumentCleaner cleaner){
		if(null==cleaners)
			cleaners = new ArrayList<DocumentCleaner>();
		
		cleaners.add(cleaner);
	}


	public void setCurrentDocPath(String path){
		this.docPath = path;
	}
	public void populateWords() throws FileNotFoundException,IOException {
		debug("Populating words");
		words = new HashMap<String, Long>();

		File file = new File(docPath);
	    FileInputStream fis = new FileInputStream(file);
	    byte[] contentInBytes = new byte[(int)file.length()];
	    fis.read(contentInBytes);
	    fis.close();		
	    
	    String content = new String(contentInBytes);
	    
	    for(DocumentCleaner cleaner: cleaners)
	    	content = cleaner.clean(content);
	    
		String nextWord;
		if(ngrams){
			
			Tokenizer wsTokenizer = new WhitespaceTokenizer(new StringReader(content)); 
			
			ShingleFilter filter = new ShingleFilter(wsTokenizer, 3);
			
			filter.setOutputUnigrams(true);
			
			TermAttribute termAtt = (TermAttribute) filter.getAttribute(TermAttribute.class); 
			
			while (filter.incrementToken()){
				
				nextWord = termAtt.term();
				
				String[] grams = nextWord.split(" ");
				if(grams.length > 1){
					String ngram = "";
					for(String gram: grams){
						if( !( gram.equals("(")||gram.equals(")")||gram.equals(">")||gram.equals("<") )){
							ngram = ngram + " " + gram;
						}
					}
					nextWord = ngram.trim();				
				}else{
					if(nextWord.equals("(")||nextWord.equals(")")||nextWord.equals(">")||nextWord.equals("<"))
						continue;
				}
				
				
				debug("Adding " + nextWord);

				if (!words.containsKey(nextWord)) {
					words.put(nextWord, (long) 1);
					wordCountNoDuplicates++;
				} else {
					words.put(nextWord, words.get(nextWord) + 1);
					debug("Word already exists - incresing word count");
				}

				wordCountWithDuplicates++;
			}			
			filter.close();
			
		}else{
			fileScanner = new Scanner(content);

			while (fileScanner.hasNext()) {
				nextWord = fileScanner.next();
				if(nextWord.equals("(")||nextWord.equals(")")||nextWord.equals(">")||nextWord.equals("<"))
					continue;
				
				debug("Adding " + nextWord);

				if (!words.containsKey(nextWord)) {
					words.put(nextWord, (long) 1);
					wordCountNoDuplicates++;
				} else {
					words.put(nextWord, words.get(nextWord) + 1);
					debug("Word already exists - incresing word count");
				}

				wordCountWithDuplicates++;
			}
			fileScanner.close();

		}

		
		debug("Finished populating words");
		debug("There were " + wordCountNoDuplicates + " unique words");
		debug("There were " + wordCountWithDuplicates + " words");

	}

	public long getWordCountWithDuplicates() {
		return wordCountWithDuplicates;
	}

	public Map<String, Long> getWordCountMap() {
		return words;
	}
	public Set<String> getAllUniqueWords() {
		return words.keySet();
	}


}