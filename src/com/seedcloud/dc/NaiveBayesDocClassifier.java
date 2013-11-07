package com.seedcloud.dc;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.seedcloud.dc.doccleaners.DocumentCleaner;
import com.seedcloud.dc.doccleaners.NeutralWordsCleaner;
import com.seedcloud.dc.doccleaners.Stemmer;
import com.seedcloud.dc.tester.DocumentClassifierTestNV;
import com.seedcloud.dc.trainer.DocumentClassifierTrainNV;
import com.seedcloud.dc.util.DocumentType;
import com.seedcloud.dc.util.MapSort;
import com.seedcloud.dc.util.WordInfo;
import com.seedcloud.dc.util.WordsInDocument;


public class NaiveBayesDocClassifier implements Serializable{
	    /**
		 * 
		 */
		transient final static DocumentCleaner neutralCleaner  = new NeutralWordsCleaner();
		transient final static DocumentCleaner stemmer  = new Stemmer();
		transient WordsInDocument wordsInDocument;

		public transient static int numberofFeatures;
		private static final long serialVersionUID = 7813230068976024039L;
		public static final boolean DEBUG = false;
		public static boolean train = false;
		private static void debug(String debug){
	    	if (DEBUG){
	    		if(train)
	    			DocumentClassifierTrainNV.logger.info(debug);
	    		else
	    			DocumentClassifierTestNV.logger.info(debug);
	    	}
	    }
		transient long filesTrainedOn = 0;
		transient String trainingDirectoryParentPath = null;
		transient File trainingDirectoryType = null;
		transient File trainingDirectoryOtherTypes = null;

		transient Integer noOfTypes = 0;
		transient Integer noOfNotTypes = 0;
		transient Map<String,Long> vocabularyType ;
		transient Map<String,Long> vocabularyNotType ;
		Map<String,WordInfo> fullvocabulary ;

		transient Long nciType ;
		transient Long nciNotType ;
		transient Long vocabularyCount ;
		
		public static DocumentType type;		
		
		Double priorProbabilityOfTypes ;
		Double priorProbabilityOfNotTypes ;
		transient Long wordCountType = new Long(0);
		transient Long wordCountNotType  = new Long(0);
		private static Long getTotalWordCountFromHashMap(
				Map<String, Long> map) {
			long count = 0;
			for (Entry<String, Long> entry : map
					.entrySet())
				count += entry.getValue();

			return count;

		}
		
		private static void appendHashMapsWordInfo(Map<String,WordInfo> oldMap,Map<String,Long> newMap,boolean isOfType){
			
			for (Entry<String,Long> entry : newMap.entrySet() ){
				if(oldMap.containsKey(entry.getKey())){
					WordInfo wordInfo = oldMap.get(entry.getKey());
					wordInfo.addFoundInDocsCount(1);
					if(isOfType)
						wordInfo.addFoundInTypeDocsCount(1);
					else
						wordInfo.addFoundInNotTypeDocsCount(1);
				}
				else{
					WordInfo wordInfo = new WordInfo(type); 
					wordInfo.addFoundInDocsCount(1);
					if(isOfType)
						wordInfo.addFoundInTypeDocsCount(1);
					else
						wordInfo.addFoundInNotTypeDocsCount(1);

					oldMap.put(entry.getKey(), wordInfo );
				}
			}
			
		}    		
		private static void appendHashMapsLong(Map<String,Long> oldMap,Map<String,Long> newMap){
			
			for (Entry<String,Long> entry : newMap.entrySet() ){
				if(oldMap.containsKey(entry.getKey()))
					oldMap.put(entry.getKey(), oldMap.get(entry.getKey()) + entry.getValue() );
				else
					oldMap.put(entry.getKey(), entry.getValue() );
			}
			
		}
		/**
		 * Remove words occurring less than these many times
		 * across all docs - from the vocabulary.
		 * This will mean these words will need to be
		 * removed from type and not type hashmaps to
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void removeFromVocabularyMinOccurringWords(){
			debug("Full vocabulary size" + fullvocabulary.size());
			int i = 0;
			fullvocabulary = MapSort.sortByValue(fullvocabulary);
			Iterator it = fullvocabulary.entrySet().iterator();
			while(it.hasNext()){
				it.next();
				i++;
				if(i > numberofFeatures) // considering first 1000 words
					break;
			}
			while(it!= null && it.hasNext())
				if(null!=it.next())it.remove();
			for (Entry e : fullvocabulary.entrySet()){
				//DocumentClassifierTrainNV.logger.info(e.getKey() + " " + ((WordInfo)e.getValue()).toString());
				i++;
			}
			//debug(fullvocabulary.toString());

		}
		public long getFilesTrainedOn(){
			return filesTrainedOn;
		}
		
		public void getInformationGainOfAllWords(){
			String nextWordOrPhrase;

			Set<String> uniqueWordsInVocabulary = getAllUniqueWords();// fullvocabulary.

			Iterator<String> vocabularyUniqueWordIterator = uniqueWordsInVocabulary
					.iterator();

			while (vocabularyUniqueWordIterator.hasNext()) {
				nextWordOrPhrase = vocabularyUniqueWordIterator.next();
				debug("The next word to test is " + nextWordOrPhrase);
				updateProbabilityAndInformationGainOnWord(nextWordOrPhrase);
				//
			}
			
		}

		public NaiveBayesDocClassifier(String trainingDirectory){
			this.trainingDirectoryParentPath = trainingDirectory;
		}
		
		private double getNoOfDocsWithWord(String word){
			return fullvocabulary.get(word).getFoundInDocsCount();
		}
		public double getStoredProbability(String word,boolean typeOrNotType, boolean presence){
			if(!fullvocabulary.containsKey(word) )
				return 0; // ignore
			else{
				if(typeOrNotType){
					if(presence)
						return fullvocabulary.get(word).getProbabilityOfWordGivenType();
					else
						return fullvocabulary.get(word).getProbabilityOfNotWordGivenType();
				}else{
					if(presence)
						return fullvocabulary.get(word).getProbabilityOfWordGivenNotType();
					else
						return fullvocabulary.get(word).getProbabilityOfNotWordGivenNotType();
				}
			}
		}


		private double getProbability(String word,boolean typeOrNotType, boolean presence){
			if(!fullvocabulary.containsKey(word) )
				return 0; // ignore
			else{
				long nciwj = 0; // set to zero (if not present - laplace correction will take care)
				if(typeOrNotType){
					if(vocabularyType.containsKey(word)) 
						nciwj = vocabularyType.get(word).longValue();
					if(!presence){
						nciwj = nciType - nciwj;
					}
					return Math.log((double)(nciwj+1)/(double)(nciType + vocabularyCount));
				}else{
					if(vocabularyNotType.containsKey(word)) 
						nciwj = vocabularyNotType.get(word).longValue();
					if(!presence){
						nciwj = nciNotType - nciwj;
					}					
					return Math.log((double)(nciwj+1)/(double)(nciNotType + vocabularyCount));
				}
			}
		}
		
		private boolean trainOnSingleFile(File trainingFile,boolean isOfType,int fold) throws Exception{
			
			long wordCount = 0;
			boolean test = true;
			
			int pos = Integer.valueOf(trainingFile.getName().substring(0,trainingFile.getName().indexOf(".txt"))) % 10;
			test = (pos == fold ? false : true);  
			if(!test){
				return false;
			}
			
			wordsInDocument.setCurrentDocPath(trainingFile.getAbsolutePath());
			wordsInDocument.populateWords();
			
			wordCount = wordsInDocument.getWordCountWithDuplicates();
			if(isOfType){
				wordCountType += wordCount;
				debug("Adding " + wordCount + " to word count " + type.toString() + " to make it " + wordCountType);

				appendHashMapsLong(vocabularyType, wordsInDocument.getWordCountMap());
				appendHashMapsWordInfo(fullvocabulary, wordsInDocument.getWordCountMap(),true);
				noOfTypes++;
			}
			else{
				wordCountNotType += wordCount;
				debug("Adding " + wordCount + " to word count NOT " + type.toString() +  " to make it " + wordCountNotType);

				appendHashMapsLong(vocabularyNotType, wordsInDocument.getWordCountMap());
				appendHashMapsWordInfo(fullvocabulary, wordsInDocument.getWordCountMap(),false);

				noOfNotTypes++;
			}			
			
			return true;
		}
		public Set getAllFeatures(){
			return fullvocabulary.keySet();
		}
		public void train(int fold)throws Exception{

			wordsInDocument = new WordsInDocument();
			wordsInDocument.addDocumentCleaners(neutralCleaner); // always add neutral cleaner first
			wordsInDocument.addDocumentCleaners(stemmer);
			
			Integer fileCounter = 0;
			vocabularyType = new HashMap<String,Long>();
			vocabularyNotType = new HashMap<String,Long>();
			fullvocabulary = new TreeMap<String,WordInfo>();

			filesTrainedOn = 0;
			
			trainingDirectoryType = new File(trainingDirectoryParentPath + "/" + type.toString() );
			File[] trainingFiles = trainingDirectoryType.listFiles();
			
			boolean isTrained = false; 
			for(File file : trainingFiles){
				isTrained = trainOnSingleFile(file,true,fold);
				if(isTrained)
					filesTrainedOn++;
			}
			
			
			for(DocumentType otherTypes:DocumentType.values() ){
				if(otherTypes.toString().equals(type.toString())) continue;
				
				trainingDirectoryOtherTypes = new File(trainingDirectoryParentPath + "/" + otherTypes.toString() );
				
				trainingFiles = trainingDirectoryOtherTypes.listFiles();

				for(File file : trainingFiles){
					isTrained = trainOnSingleFile(file,false,fold);
					if(isTrained)
						filesTrainedOn++;
				}
			}
			
			
			if(noOfTypes>0 && noOfNotTypes >0){
				priorProbabilityOfTypes = (double)noOfTypes/(double)(noOfNotTypes+noOfTypes);
				priorProbabilityOfNotTypes = (double)noOfNotTypes/(double)(noOfNotTypes+noOfTypes);
			}else{
				throw new Exception("There are not enough documents to classify both classes of type and not types in the training directory " + trainingDirectoryType);
			}
			
			if(fullvocabulary.isEmpty() || vocabularyNotType.isEmpty() || vocabularyType.isEmpty()){
				throw new Exception("There are not enough words in the training files to sufficiently classify types and not types " + trainingDirectoryType);
			}
			
			nciType = getTotalWordCountFromHashMap(vocabularyType);
			nciNotType = getTotalWordCountFromHashMap(vocabularyNotType);
			vocabularyCount = new Long( fullvocabulary.entrySet().size() );
		}
		
		public double getPriorProbabilityOfTypes() {
			return Math.log(priorProbabilityOfTypes);
		}
		public double getPriorProbabilityOfNotTypes() {
			return Math.log(priorProbabilityOfNotTypes);
		}
		public void summarize(){
			removeFromVocabularyMinOccurringWords();
		}
		private Set<String> getAllUniqueWords() {
			return fullvocabulary.keySet();
		}		
		/**
		 * This can be derived from the following formula
		 * 
		 * Nwt = No of training documents with word W
		 * Nwf = No of training documents without word W
		 * N= No of training document
		 * Ns = No of training documents of type
		 * Nh = No of training documents of NOT type
		 * P(Type) => Probability of documents with type = Ns/N
		 * P(Not Type) => Probability of documents of not type = Nh/N
		 * Initial Entropy = -(P(Type)Log(P(Not Type))+P(NotType)Log(P(NOtType)))
		 * 
		 * 
		 * If we choose word W for classification/Splitting
		 * 
		 * P(Type|w) => Probability of Type document given word w
		 * P(Not Type|w) => Probability of Not Type document given word w
		 * 
		 * P(Type|¬w) => Probability of Type document given there is no word w
		 * P(Not Type|¬w) => Probability of Not Type document given there is no word w
		 *
		 *Final entropy = Nwt/N*(P(Type|w)Log(P(Type|w)) + P(NotType|w)Log(P(NotType|w))) + Nwf/N*(P(Type|¬w)Log(P(Type|¬w)) + P(Not Type|¬w)Log(P(Not Type|¬w)))
		 * 
		 * 
		 */
		private void updateProbabilityAndInformationGainOnWord(String nextWord){
			double typeProbabilityPresence = 0;
			double typeProbabilityAbsence = 0;
			double notTypeProbabilityPresence = 0;
			double notTypeProbabilityAbsence = 0;
			double numberofDocsWithWord = 0;
			double numberofDocsWithoutWord = 0;
			double classPurityPresence = 0;
			double classPurityAbsence = 0;
			double informationGainPresence = 0;
			double informationGainAbsence = 0;
			double informationGain = 0;
			typeProbabilityPresence = getProbability(nextWord,true,true);
			debug("Type probability of presence " + typeProbabilityPresence);
			fullvocabulary.get(nextWord).setProbabilityOfWordGivenType(typeProbabilityPresence);
			
			typeProbabilityAbsence = getProbability(nextWord,true,false);
			debug("Type probability of absence " + typeProbabilityAbsence);
			fullvocabulary.get(nextWord).setProbabilityOfNotWordGivenType(typeProbabilityAbsence);
			
			notTypeProbabilityPresence = getProbability(nextWord,false,true);
			debug("Not Type probability of presence " + notTypeProbabilityPresence);
			fullvocabulary.get(nextWord).setProbabilityOfWordGivenNotType(notTypeProbabilityPresence);
			
			
			notTypeProbabilityAbsence = getProbability(nextWord,false,false);
			debug("Not Type probability of absence " + notTypeProbabilityAbsence);
			fullvocabulary.get(nextWord).setProbabilityOfNotWordGivenNotType(notTypeProbabilityAbsence);
			
			
			numberofDocsWithWord = getNoOfDocsWithWord(nextWord);
			debug("Docs with this word " + numberofDocsWithWord);
			numberofDocsWithoutWord = noOfNotTypes + noOfTypes - numberofDocsWithWord;
			debug("No of docs without this word " + numberofDocsWithoutWord);
			
			classPurityPresence = Math.pow( Math.E,typeProbabilityPresence) * typeProbabilityPresence + Math.pow(Math.E,notTypeProbabilityPresence) * notTypeProbabilityPresence;
			debug("Class purity of presence " + classPurityPresence);
			
			classPurityAbsence = Math.pow(Math.E,typeProbabilityAbsence) * typeProbabilityAbsence + Math.pow(Math.E,notTypeProbabilityAbsence) * notTypeProbabilityAbsence;
			debug("Class purity of absence " + classPurityAbsence);

			informationGainPresence = (numberofDocsWithWord/(noOfNotTypes + noOfTypes))*classPurityPresence;
			debug("Information gain presence " + informationGainPresence);

			informationGainAbsence = (numberofDocsWithoutWord/(noOfNotTypes + noOfTypes))*classPurityAbsence;
			debug("Information gain absence " + informationGainAbsence);
			informationGain = informationGainPresence + informationGainAbsence;
			debug("Information gain " + informationGain);
			
			fullvocabulary.get(nextWord).setInformationGain(informationGain);
		}
		
		
	}
