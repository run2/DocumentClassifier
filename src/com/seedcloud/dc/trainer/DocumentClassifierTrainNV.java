package com.seedcloud.dc.trainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.seedcloud.dc.NaiveBayesDocClassifier;
import com.seedcloud.dc.util.DocumentType;
import com.seedcloud.dc.util.Util;
import com.seedcloud.dc.util.WordsInDocument;

public class DocumentClassifierTrainNV {
	public static final Logger logger = Logger.getLogger(DocumentClassifierTrainNV.class
			.getCanonicalName());

	public static boolean DEBUG = false;
	static Map<String,String> settingsMap ;
	private static void debug(String debug) {
		if (DEBUG)
			logger.info(debug);

	}
	public DocumentClassifierTrainNV(){
		
	}
	
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			
			DocumentClassifierTrainNV.setUpLogging();
			NaiveBayesDocClassifier.train = true;
			settingsMap = Util.getSettingsFromPropertiesFile("Settings.properties");
			if(settingsMap.get("usengrams").equals("true")){
				WordsInDocument.ngrams = true;
			}else{
				WordsInDocument.ngrams = false;
			}
			NaiveBayesDocClassifier.numberofFeatures = Integer.valueOf(settingsMap.get("numberoffeatures"));
			
			for(DocumentType type : DocumentType.values()){
				for(int i = 0 ; i <= 9 ; i ++){
					train("trainingCorpus",i, type);
				}
			}
			
			System.out.println("Finished training");
		} catch (Exception e) {
			e.printStackTrace();
			debug("Exception " + e.getMessage());
		}
		
	}

	public static void setUpLogging() throws Exception {

		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new SimpleFormatter());
		consoleHandler.setLevel(java.util.logging.Level.FINEST);
		
		
		Handler fhandler = new FileHandler("nvtraining.log", false);
		fhandler.setFormatter(new SimpleFormatter());
		fhandler.setLevel(java.util.logging.Level.FINEST);
		logger.setLevel(java.util.logging.Level.FINEST);
		logger.setUseParentHandlers(false);
		logger.addHandler(fhandler);
		
		logger.addHandler(consoleHandler);

	}

	public static void main(String args[]) {
		DocumentClassifierTrainNV train = new DocumentClassifierTrainNV();
		train.run();
	}
	public static void train(String folder, int fold, DocumentType type) throws Exception{
		File trainingFolder = new File(folder);
		
		if (!(trainingFolder.isDirectory() && trainingFolder.exists())) {
			debug("Not a valid directory");
			throw new Exception(folder
					+ " not a valid directory");
		} else{
			
			File trainingFolderType = new File(folder+"/"+type.toString());
			debug("There are " + trainingFolderType.list().length
					+ " files in directory " + trainingFolderType);

			for(DocumentType otherTypes : DocumentType.values()){
				if(!otherTypes.toString().equals(type.toString())){
					File trainingFolderOtherTypes = new File(folder+"/"+otherTypes.toString());
					debug("There are " + trainingFolderType.list().length
							+ " files in directory " + trainingFolderOtherTypes);
					
				}
			}
			
		}

		System.out.println("Training on fold " + fold);
		debug("**************** START Training on " + fold + " fold *******************");
		
		//MailReader.setUpLogging();
		NaiveBayesDocClassifier docClassifier = new NaiveBayesDocClassifier(trainingFolder.getAbsolutePath());
		NaiveBayesDocClassifier.type = type;

		docClassifier.train(fold);
		docClassifier.getInformationGainOfAllWords();
		docClassifier.summarize();
		
		debug(" Trained on " + docClassifier.getFilesTrainedOn() + " Files");
		
		String trainingFileName = "";
		trainingFileName = type.toString() + "training_" + fold + ".dat";
		
		FileOutputStream fout = new FileOutputStream(trainingFileName);
	    ObjectOutputStream oos = new ObjectOutputStream(fout);
	    oos.writeObject(docClassifier);
	    oos.close();				

		debug("**************** END Training on " + fold + " fold *******************");
	    
	}

}
