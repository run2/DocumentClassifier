package com.seedcloud.dc.tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Set;

import com.seedcloud.dc.NaiveBayesDocClassifier;
import com.seedcloud.dc.doccleaners.DocumentCleaner;
import com.seedcloud.dc.doccleaners.NeutralWordsCleaner;
import com.seedcloud.dc.doccleaners.Stemmer;
import com.seedcloud.dc.util.DocumentType;
import com.seedcloud.dc.util.WordsInDocument;

public class Classify {
	static WordsInDocument testDoc ;
	final static DocumentCleaner neutralCleaner  = new NeutralWordsCleaner();
	final static DocumentCleaner stemmer  = new Stemmer();
	
	public static void main(String args[]){
		testDoc = new WordsInDocument();
		testDoc.addDocumentCleaners(neutralCleaner);
		testDoc.addDocumentCleaners(stemmer);
		double pBusiness = 0;
		double pSport = 0;
		double pTechnology = 0;
		try{
			String fileName = args[0];
			File testFile = new File(fileName);
			for(DocumentType docType : DocumentType.values()){
				FileInputStream fin ;
				fin = new FileInputStream(docType.toString() + "training.dat");
				
				ObjectInputStream ois = new ObjectInputStream(fin);
				NaiveBayesDocClassifier docClassifier = (NaiveBayesDocClassifier) ois.readObject();
				ois.close();					
				fin.close();
				double probability = testSingleFile(testFile,docClassifier);
				switch(docType){
					case BUSINESS:
						pBusiness = probability;
						System.out.println("BUSINESS probability " + pBusiness);
						break;
					case SPORT:
						pSport = probability;
						System.out.println("SPORT probability " + pSport);
						break;
					case TECHNOLOGY:
						pTechnology = probability;
						System.out.println("TECHNOLOGY  probability " + pTechnology);
						break;
				}
			}
			
			if(pBusiness>=pSport && pBusiness>=pTechnology)
				System.out.println("BUSINESS DOCUMENT");
			else if(pSport>=pBusiness && pSport>=pTechnology)
				System.out.println("SPORT DOCUMENT");
			else if(pTechnology>=pBusiness && pTechnology>=pSport)
				System.out.println("TECHNOLOGY DOCUMENT");
			else
				System.out.println("AMBIGUOUS");
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static double testSingleFile(File testDocFile,NaiveBayesDocClassifier docClassifier)throws IOException{

		testDoc.setCurrentDocPath(testDocFile.getPath());
		testDoc.populateWords();
		String nextWord;
		double typeProbability = 0;
		double notTypeProbability = 0;
		double probability = 0;

		Set<String> uniqueWordsInDoc = testDoc.getAllUniqueWords();
		
		Iterator<String> testDocUniqueWordIterator = uniqueWordsInDoc.iterator();

		while(testDocUniqueWordIterator.hasNext()){
			nextWord = testDocUniqueWordIterator.next();
			probability = docClassifier.getStoredProbability(nextWord,true,true);
			typeProbability = typeProbability + probability;

			probability = docClassifier.getStoredProbability(nextWord,false,true);
			notTypeProbability = notTypeProbability + probability;
		}
		
		typeProbability = typeProbability + docClassifier.getPriorProbabilityOfTypes();
		return typeProbability;
		
	}
}	
