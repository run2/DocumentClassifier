package com.seedcloud.dc;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.seedcloud.dc.doccleaners.DocumentCleaner;
import com.seedcloud.dc.doccleaners.NeutralWordsCleaner;
import com.seedcloud.dc.doccleaners.Stemmer;
import com.seedcloud.dc.util.DocumentType;
import com.seedcloud.dc.util.Util;
import com.seedcloud.dc.util.WordsInDocument;

public class DocumentAdaBoostTrainer{
	public static final Logger logger = Logger.getLogger(DocumentAdaBoostTrainer.class
			.getCanonicalName());
	final static DocumentCleaner neutralCleaner  = new NeutralWordsCleaner();
	final static DocumentCleaner stemmer  = new Stemmer();
	static Map<String,String> settingsMap ;
	static WordsInDocument testDoc ;
	public static final boolean DEBUG = false;
	//static int gtotalFilesOfNotType = 0;
	static TreeMap<String,Float> featureWeights ;
	static TreeMap<String,Float> featureErrors ; 
	
	static String fileName = "";
	public static void setUpLogging() throws Exception {
		Handler fhandler = new FileHandler("documentClassifier.log", false);
		fhandler.setFormatter(new SimpleFormatter());
		fhandler.setLevel(java.util.logging.Level.FINEST);
		logger.setLevel(java.util.logging.Level.FINEST);
		logger.setUseParentHandlers(false);
		logger.addHandler(fhandler);
	}
	public DocumentAdaBoostTrainer(){
	}	
	
	public void run() {
		// TODO Auto-generated method stub
		try{
			
			DocumentAdaBoostTrainer.setUpLogging();
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

			for(DocumentType docType : DocumentType.values()){
				System.out.println("Testing started on 10 fold for type " + docType.toString());
				for(int i = 0 ; i <= 9 ; i ++){
					System.out.println("Testing started on fold " + i + " for type " + docType.toString());
					test("trainingCorpus",i, docType);
				}

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
		DocumentAdaBoostTrainer test = new DocumentAdaBoostTrainer();
		test.run();
		
	}
	public static void test(String directory,int fold,DocumentType type ){
		try{
			
			
			boolean test = true;
			
			FileInputStream fin ;
			fin = new FileInputStream(type.toString() + "training_" + fold + ".dat");
			
			ObjectInputStream ois = new ObjectInputStream(fin);
			NaiveBayesDocClassifier docClassifier = (NaiveBayesDocClassifier) ois.readObject();
			ois.close();					

			File trainingDirectoryType = new File(directory + "/" + type.toString() );
			File[] trainingFiles = trainingDirectoryType.listFiles();
			
			Set<String> setOfFeatures = docClassifier.getAllFeatures();
			Iterator<String> featureIterator = setOfFeatures.iterator();
			
			featureWeights = new TreeMap<String,Float>();
			featureErrors = new TreeMap<String,Float>(); 

			while(featureIterator.hasNext()){
				String feature = featureIterator.next();
				featureWeights.put(feature, (float)1/setOfFeatures.size());
				featureErrors.put(feature, (float)0);
			}
			
			int featureCounter = 0;
			while(featureIterator.hasNext()){
				String feature = featureIterator.next();
				
				for(File testingFile : trainingFiles){
					test = false;

					int pos = Integer.valueOf(testingFile.getName().substring(0,testingFile.getName().indexOf(".txt"))) % 10;
					test = (pos == fold ? true : false);  
					if(!test){
						continue;
					}

					featureErrors.put(feature,featureErrors.get(feature)  + testSingleFile(testingFile,docClassifier,true,type, feature));
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

						featureErrors.put(feature,featureErrors.get(feature)  + testSingleFile(testingFile,docClassifier,false,type,feature));
					}
				}							
			}
			
			

		}catch(Exception e){
			//e.printStackTrace();
			debug("Exception " + e.getMessage());
		}

	}
	private static float testSingleFile(File testDocFile,NaiveBayesDocClassifier docClassifier,boolean isOfType, DocumentType type,String feature)throws IOException{

		testDoc.setCurrentDocPath(testDocFile.getPath());
		testDoc.populateWords();
		double probabilityPresenceInType = 0;
		double probabilityAbsenceInNotType = 0;

		float error = 0.0f;
		
		boolean present = testDoc.getAllUniqueWords().contains(feature);
				
		if(present){

			probabilityPresenceInType = docClassifier.getStoredProbability(feature,true,true);
			error = featureWeights.get(feature)* (float)Math.abs((probabilityPresenceInType) - ((isOfType==true)?1:0));
			
		}else{
			
			probabilityAbsenceInNotType = docClassifier.getStoredProbability(feature,true,false);
			error = featureWeights.get(feature)* (float)Math.abs((probabilityAbsenceInNotType - ((isOfType==true)?1:0)));
		
		}
		
		return error;
		
	}
	
}
