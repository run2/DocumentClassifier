package com.seedcloud.dc.tester;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.seedcloud.dc.NaiveBayesDocClassifier;
import com.seedcloud.dc.doccleaners.DocumentCleaner;
import com.seedcloud.dc.doccleaners.NeutralWordsCleaner;
import com.seedcloud.dc.doccleaners.Stemmer;
import com.seedcloud.dc.util.DocumentType;
import com.seedcloud.dc.util.Util;
import com.seedcloud.dc.util.WordsInDocument;

public class DocumentClassifierTestNV{
	public static final Logger logger = Logger.getLogger(DocumentClassifierTestNV.class
			.getCanonicalName());
	final static DocumentCleaner neutralCleaner  = new NeutralWordsCleaner();
	final static DocumentCleaner stemmer  = new Stemmer();
	static int totalFilesIdentifiedCorrectly = 0;
	static int totalFilesIdentifiedInCorrectly = 0;
	static int totalFiles = 0;
	static Map<String,String> settingsMap ;
	//static int totalFilesOfNotType = 0;
	
	static WordsInDocument testDoc ;
	public static final boolean DEBUG = false;
	static int gtotalFilesCorrectlyClassified = 0;
	static int gtotalFilesInCorrectlyClassified = 0;
	static int gtotalFiles = 0;
	//static int gtotalFilesOfNotType = 0;
	
	static String fileName = "";
	public static void setUpLogging() throws Exception {
		Handler fhandler = new FileHandler("documentClassifier.log", false);
		fhandler.setFormatter(new SimpleFormatter());
		fhandler.setLevel(java.util.logging.Level.FINEST);
		logger.setLevel(java.util.logging.Level.FINEST);
		logger.setUseParentHandlers(false);

		logger.addHandler(fhandler);

	}
	public DocumentClassifierTestNV(){
	}	
	
	public void run() {
		// TODO Auto-generated method stub
		try{
			
			DocumentClassifierTestNV.setUpLogging();
			NaiveBayesDocClassifier.train = false;
			testDoc = new WordsInDocument();
			testDoc.addDocumentCleaners(neutralCleaner);
			testDoc.addDocumentCleaners(stemmer);
			settingsMap = Util.getSettingsFromPropertiesFile("Settings.properties");
			if(settingsMap.get("usengrams").equals("true")){
				WordsInDocument.ngrams = true;
			}else{
				WordsInDocument.ngrams = false;
			}
			
			System.out.println("Testing on 10 fold");
			
			int bestBusinessFold = -1;
			float bestBusinessPrecision = 0 ;
			int bestSportFold = -1;
			float bestSportPrecision = 0 ;
			int bestTechnologyFold = -1;
			float bestTechnologyPrecision = 0 ;
			
			for(DocumentType docType : DocumentType.values()){
				System.out.println("Testing started on 10 fold for type " + docType.toString());
				for(int i = 0 ; i <= 9 ; i ++){
					System.out.println("Testing started on fold " + i + " for type " + docType.toString());
					test("trainingCorpus",i, docType);

/*					System.out.println("***************************************************************");
					System.out.println("Testing completed fold " + i + " for type " + docType.toString());
					debug("Total Total files identified correctly " + totalFilesIdentifiedCorrectly);
					System.out.println("Total Total files identified correctly " + totalFilesIdentifiedCorrectly);
					debug("Total Total files identified in correctly " + totalFilesIdentifiedInCorrectly);
					System.out.println("Total Total files identified in correctly " + totalFilesIdentifiedInCorrectly);
					System.out.println("Precision on type " +  docType.toString() + " = " + precision);
					System.out.println("Recall on type " +  docType.toString() + " = " + recall);
					System.out.println("***************************************************************");
*/
					float precision = (float)totalFilesIdentifiedCorrectly/(totalFilesIdentifiedCorrectly+totalFilesIdentifiedInCorrectly);
					float recall = (float)(totalFilesIdentifiedCorrectly/totalFiles);
					
					switch(docType){
						case BUSINESS:
							if(precision > bestBusinessPrecision){
								bestBusinessFold = i;
								bestBusinessPrecision = precision;
							}
							break;
						case SPORT:
							if(precision > bestSportPrecision){
								bestSportFold = i;
								bestSportPrecision = precision;
							}
							break;
						case TECHNOLOGY:
							if(precision > bestTechnologyPrecision){
								bestTechnologyFold = i;
								bestTechnologyPrecision = precision;
							}
							break;
							
					}
				}

				System.out.println("***************************************************************");
				System.out.println("Testing completed 10 fold for type " + docType.toString());
				debug("Total Total files identified correctly " + gtotalFilesCorrectlyClassified);
				System.out.println("Total Total files identified correctly " + gtotalFilesCorrectlyClassified);
				debug("Total Total files identified in correctly " + gtotalFilesInCorrectlyClassified);
				System.out.println("Total Total files identified in correctly " + gtotalFilesInCorrectlyClassified);
				
				System.out.println("Precision on type " +  docType.toString() + " = " + (float)gtotalFilesCorrectlyClassified/(gtotalFilesCorrectlyClassified+gtotalFilesInCorrectlyClassified));
				System.out.println("Recall on type " +  docType.toString() + " = " + (float)gtotalFilesCorrectlyClassified/gtotalFiles);
				System.out.println("***************************************************************");
				
				
				switch(docType){
				case BUSINESS:
					System.out.println("Best fold " + bestBusinessFold + " with precision " + bestBusinessPrecision);
					break;
				case SPORT:
					System.out.println("Best fold " + bestSportFold + " with precision " + bestSportPrecision);
					break;
				case TECHNOLOGY:
					System.out.println("Best fold " + bestTechnologyFold + " with precision " + bestTechnologyPrecision);
					break;
					
			}
				
				
				gtotalFilesCorrectlyClassified = 0;
				gtotalFilesInCorrectlyClassified = 0;
				gtotalFiles = 0;
			}
			System.out.println("Testing completed 10 fold for all types");
			
		}catch(Exception e){
			debug("Exception " + e.getMessage());
			
		}
		
	}

	private static void debug(String debug){
		if (DEBUG)
			logger.info(debug);
		//System.out.println(debug);

	}
	public static void main(String args[]){
		DocumentClassifierTestNV test = new DocumentClassifierTestNV();
		test.run();
		
	}
	public static void test(String directory,int fold,DocumentType type ){
		try{
			
			
			boolean test = true;
			totalFilesIdentifiedCorrectly = 0;
			totalFilesIdentifiedInCorrectly = 0;
			totalFiles = 0;
			
			FileInputStream fin ;
			fin = new FileInputStream(type.toString() + "training_" + fold + ".dat");
			
			ObjectInputStream ois = new ObjectInputStream(fin);
			NaiveBayesDocClassifier docClassifier = (NaiveBayesDocClassifier) ois.readObject();
			ois.close();
			fin.close();

			File trainingDirectoryType = new File(directory + "/" + type.toString() );
			File[] trainingFiles = trainingDirectoryType.listFiles();
			
			for(File testingFile : trainingFiles){
				test = false;

				int pos = Integer.valueOf(testingFile.getName().substring(0,testingFile.getName().indexOf(".txt"))) % 10;
				test = (pos == fold ? true : false);  
				if(!test){
					continue;
				}

				testSingleFile(testingFile,docClassifier,true,type);
				totalFiles++;
			}
			
			
			for(DocumentType otherTypes:DocumentType.values() ){
				if(otherTypes.toString().equals(type.toString())) continue;
				
				File trainingDirectoryOtherTypes = new File(directory + "/" + otherTypes.toString() );
				
				trainingFiles = trainingDirectoryOtherTypes.listFiles();

				for(File testingFile : trainingFiles){
					int pos = Integer.valueOf(testingFile.getName().substring(0,testingFile.getName().indexOf(".txt"))) % 10;
					test = (pos == fold ? true : false);  
					if(!test){
						continue;
					}

					testSingleFile(testingFile,docClassifier,false,type);
					totalFiles++;
				}
			}			
			
			//debug("Total files identified correctly " + totalFilesIdentifiedCorrectly);
			//System.out.println("Total files identified correctly " + totalFilesIdentifiedCorrectly);
			gtotalFilesCorrectlyClassified+=totalFilesIdentifiedCorrectly;
			//debug("Total files identified in correctly " + totalFilesIdentifiedInCorrectly);
			//System.out.println("Total files identified in correctly " + totalFilesIdentifiedInCorrectly);
			gtotalFilesInCorrectlyClassified+=totalFilesIdentifiedInCorrectly;
			gtotalFiles+=totalFiles;
			gtotalFiles+=totalFiles;

		}catch(Exception e){
			//e.printStackTrace();
			debug("Exception " + e.getMessage());
		}

	}
	private static void testSingleFile(File testDocFile,NaiveBayesDocClassifier docClassifier,boolean isOfType, DocumentType type)throws IOException{

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
		notTypeProbability = notTypeProbability + docClassifier.getPriorProbabilityOfNotTypes();

		if(typeProbability>notTypeProbability){
			if(isOfType){
				//System.out.println("Predicting currectly document of type " + type.toString());				
				totalFilesIdentifiedCorrectly++;
			}
			else{
				//System.out.println("Predicting incurrectly document of type " + type.toString());				
				totalFilesIdentifiedInCorrectly++;
				debug("Did not match " + testDocFile.getName());
			}	
		}
		else{
			if(!isOfType){
				//System.out.println("Predicting currectly document not of type " + type.toString());
				totalFilesIdentifiedCorrectly++;
			}
			else{
				//System.out.println("Predicting incurrectly document not of type " + type.toString());
				
				totalFilesIdentifiedInCorrectly++;
				debug("Did not match " + testDocFile.getName());
			}
		}
	}
	
}
