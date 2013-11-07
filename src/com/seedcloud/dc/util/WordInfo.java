package com.seedcloud.dc.util;
import java.io.Serializable;


@SuppressWarnings("rawtypes")
public class WordInfo implements Serializable, Comparable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2855013022839644076L;
	private DocumentType type ; 
	long foundInDocsCount = 0;
	long foundInTypeDocsCount = 0;
	long foundInNotTypeDocsCount = 0;
	double informationGain = 0;
	double probabilityOfWordGivenType = 0;
	double probabilityOfNotWordGivenType = 0;
	double probabilityOfWordGivenNotType = 0;
	double probabilityOfNotWordGivenNotType = 0;
	
	public WordInfo(DocumentType type){
		this.type = type;
	}
	public double getProbabilityOfWordGivenType() {
		return probabilityOfWordGivenType;
	}

	public void setProbabilityOfWordGivenType(double probabilityOfWordGivenType) {
		this.probabilityOfWordGivenType = probabilityOfWordGivenType;
	}

	public double getProbabilityOfNotWordGivenType() {
		return probabilityOfNotWordGivenType;
	}

	public void setProbabilityOfNotWordGivenType(
			double probabilityOfNotWordGivenType) {
		this.probabilityOfNotWordGivenType = probabilityOfNotWordGivenType;
	}

	public double getProbabilityOfWordGivenNotType() {
		return probabilityOfWordGivenNotType;
	}

	public void setProbabilityOfWordGivenNotType(double probabilityOfWordGivenNotType) {
		this.probabilityOfWordGivenNotType = probabilityOfWordGivenNotType;
	}

	public double getProbabilityOfNotWordGivenNotType() {
		return probabilityOfNotWordGivenNotType;
	}

	public void setProbabilityOfNotWordGivenNotType(double probabilityOfNotWordGivenHam) {
		this.probabilityOfNotWordGivenNotType = probabilityOfNotWordGivenHam;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return new Double(informationGain).compareTo(new Double( ((WordInfo)o).getInformationGain()));
	}

	public double getInformationGain() {
		return informationGain;
	}

	public void setInformationGain(double informationGain) {
		this.informationGain = informationGain;
	}


	public long getFoundInDocsCount() {
		return foundInDocsCount;
	}
	public long getFoundInTypeDocsCount() {
		return foundInTypeDocsCount;
	}
	public long getFoundInNotTypeDocsCount() {
		return foundInNotTypeDocsCount;
	}	

	public void addFoundInDocsCount(long value) {
		this.foundInDocsCount += value;
	}
	public void addFoundInTypeDocsCount(long value) {
		this.foundInTypeDocsCount += value;
	}
	public void addFoundInNotTypeDocsCount(long value) {
		this.foundInNotTypeDocsCount += value;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub

		return "Found in Docs " + 	
		foundInDocsCount +
		" Found in " + type.toString() +  " Docs " + 
		foundInTypeDocsCount +
		" Found in Not " + type.toString() + " Docs " + 
		foundInNotTypeDocsCount +
		" Information Gain " + 
		informationGain +
		" Probability of word given Type " + 
		probabilityOfWordGivenType +
		" Probability of not word given Type  " +
		probabilityOfNotWordGivenType +
		" Probability of word given Not Type " +
		probabilityOfWordGivenNotType+
		" Probability of not word given Not Type " +
		probabilityOfNotWordGivenNotType;
	
	}	

	
}
