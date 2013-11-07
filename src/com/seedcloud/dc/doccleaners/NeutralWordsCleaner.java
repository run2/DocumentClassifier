package com.seedcloud.dc.doccleaners;


public class NeutralWordsCleaner implements DocumentCleaner{
	static String before = "(?i)(\\b";
	static String after = "\\s+)+|";

	final static String[] commonWords = {"a","able","about","across","after","all","almost","also","am","among","an","and","any","are","as","at","be","because","been","but","by","can","cannot","could","dear","did","do","does","either","else","ever","every","for","from","get","got","had","has","have","he","her","hers","him","his","how","however","i","if","in","into","is","it","its","just","least","let","like","likely","may","me","might","most","must","my","neither","no","nor","not","of","off","often","on","only","or","other","our","own","rather","said","say","says","she","should","since","so","some","than","that","the","their","them","then","there","these","they","this","tis","to","too","twas","us","wants","was","we","were","what","when","where","which","while","who","whom","why","will","with","would","yet","you","your","localhost"};
	final static String[] weekdays = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
	final static String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	final static String[] timezones = {"\\(*ACDT\\)*","\\(*ACST\\)*","\\(*ACT\\)*","\\(*ADT\\)*","\\(*AEDT\\)*","\\(*AEST\\)*","\\(*AFT\\)*","\\(*AKDT\\)*","\\(*AKST\\)*","\\(*AMST\\)*","\\(*AMST\\)*","\\(*AMT\\)*","\\(*AMT\\)*","\\(*ART\\)*","\\(*AST\\)*","\\(*AST\\)*","\\(*AWDT\\)*","\\(*AWST\\)*","\\(*AZOST\\)*","\\(*AZT\\)*","\\(*BDT\\)*","\\(*BIOT\\)*","\\(*BIT\\)*","\\(*BOT\\)*","\\(*BRT\\)*","\\(*BST\\)*","\\(*BST\\)*","\\(*BTT\\)*","\\(*\\)*","\\(*CAT\\)*","\\(*CCT\\)*","\\(*CDT\\)*","\\(*CDT\\)*","\\(*CEDT\\)*","\\(*CEST\\)*","\\(*CET\\)*","\\(*CHADT\\)*","\\(*CHAST\\)*","\\(*CHOT\\)*","\\(*ChST\\)*","\\(*CHUT\\)*","\\(*CIST\\)*","\\(*CIT\\)*","\\(*CKT\\)*","\\(*CLST\\)*","\\(*CLT\\)*","\\(*COST\\)*","\\(*COT\\)*","\\(*CST\\)*","\\(*CST\\)*","\\(*CST\\)*","\\(*CST\\)*","\\(*CST\\)*","\\(*CT\\)*","\\(*CVT\\)*","\\(*CWST\\)*","\\(*CXT\\)*","\\(*DAVT\\)*","\\(*DDUT\\)*","\\(*DFT\\)*","\\(*EASST\\)*","\\(*EAST\\)*","\\(*EAT\\)*","\\(*ECT\\)*","\\(*ECT\\)*","\\(*EDT\\)*","\\(*EEDT\\)*","\\(*EEST\\)*","\\(*EET\\)*","\\(*EGST\\)*","\\(*EGT\\)*","\\(*EIT\\)*","\\(*EST\\)*","\\(*EST\\)*","\\(*FET\\)*","\\(*FJT\\)*","\\(*FKST\\)*","\\(*FKT\\)*","\\(*FNT\\)*","\\(*GALT\\)*","\\(*GAMT\\)*","\\(*GET\\)*","\\(*GFT\\)*","\\(*GILT\\)*","\\(*GIT\\)*","\\(*GMT\\)*","\\(*GST\\)*","\\(*GST\\)*","\\(*GYT\\)*","\\(*HADT\\)*","\\(*HAEC\\)*","\\(*HAST\\)*","\\(*HKT\\)*","\\(*HMT\\)*","\\(*HOVT\\)*","\\(*HST\\)*","\\(*ICT\\)*","\\(*IDT\\)*","\\(*IOT\\)*","\\(*IRDT\\)*","\\(*IRKT\\)*","\\(*IRST\\)*","\\(*IST\\)*","\\(*IST\\)*","\\(*IST\\)*","\\(*JST\\)*","\\(*KGT\\)*","\\(*KOST\\)*","\\(*KRAT\\)*","\\(*KST\\)*","\\(*LHST\\)*","\\(*LHST\\)*","\\(*LINT\\)*","\\(*MAGT\\)*","\\(*MART\\)*","\\(*MAWT\\)*","\\(*MDT\\)*","\\(*MET\\)*","\\(*MEST\\)*","\\(*MHT\\)*","\\(*MIST\\)*","\\(*MIT\\)*","\\(*MMT\\)*","\\(*MSK\\)*","\\(*MST\\)*","\\(*MST\\)*","\\(*MST\\)*","\\(*MUT\\)*","\\(*MVT\\)*","\\(*MYT\\)*","\\(*NCT\\)*","\\(*NDT\\)*","\\(*NFT\\)*","\\(*NPT\\)*","\\(*NST\\)*","\\(*NT\\)*","\\(*NUT\\)*","\\(*NZDT\\)*","\\(*NZST\\)*","\\(*OMST\\)*","\\(*ORAT\\)*","\\(*PDT\\)*","\\(*PET\\)*","\\(*PETT\\)*","\\(*PGT\\)*","\\(*PHOT\\)*","\\(*PHT\\)*","\\(*PKT\\)*","\\(*PMDT\\)*","\\(*PMST\\)*","\\(*PONT\\)*","\\(*PST\\)*","\\(*PYST\\)*","\\(*PYT\\)*","\\(*RET\\)*","\\(*ROTT\\)*","\\(*SAKT\\)*","\\(*SAMT\\)*","\\(*SAST\\)*","\\(*SBT\\)*","\\(*SCT\\)*","\\(*SGT\\)*","\\(*SLT\\)*","\\(*SRT\\)*","\\(*SST\\)*","\\(*SST\\)*","\\(*SYOT\\)*","\\(*TAHT\\)*","\\(*THA\\)*","\\(*TFT\\)*","\\(*TJT\\)*","\\(*TKT\\)*","\\(*TLT\\)*","\\(*TMT\\)*","\\(*TOT\\)*","\\(*TVT\\)*","\\(*UCT\\)*","\\(*ULAT\\)*","\\(*UTC\\)*","\\(*UYST\\)*","\\(*UYT\\)*","\\(*UZT\\)*","\\(*VET\\)*","\\(*VLAT\\)*","\\(*VOLT\\)*","\\(*VOST\\)*","\\(*VUT\\)*","\\(*WAKT\\)*","\\(*WAST\\)*","\\(*WAT\\)*","\\(*WEDT\\)*","\\(*WEST\\)*","\\(*WET\\)*","\\(*WST\\)*","\\(*YAKT\\)*","\\(*YEKT\\)*","\\(*Z\\)*"};
	
	
	static StringBuffer removeCommonWords;
	static StringBuffer removeSingleCharacterOrOnlyDigits;
	static StringBuffer removeTime;

	static{
		removeCommonWords = new StringBuffer();
		for(String word : commonWords){
			removeCommonWords.append(before);
			removeCommonWords.append(word);
			removeCommonWords.append(after);
		}
		for(String word : weekdays){
			removeCommonWords.append(before);
			removeCommonWords.append(word);
			removeCommonWords.append(after);
		}
		for(String word : months){
			removeCommonWords.append(before);
			removeCommonWords.append(word);
			removeCommonWords.append(after);
		}
		for(String word : timezones){
			removeCommonWords.append(before);
			removeCommonWords.append(word);
			removeCommonWords.append(after);
		}
		removeCommonWords.append("(\\&nbsp;)+");
		
		//removeSingleCharacterOrOnlyDigits = new StringBuffer();
		//removeSingleCharacterOrOnlyDigits.append("(?i)(\\b[a-z]\\s+)|(\\b\\d+\\)*\\s+)|(\\s+[0-9]+\\.[0-9]+\\s+)|(\\s+\\\"\\s+)+|(\\s+,\\s+)+|(\\s+\\.+\\s+)+");
		
		//removeTime = new StringBuffer();
		//removeTime.append("([0-2]?[0-3]:[0-5][0-9][AM|am|PM|pm])");
		
	}
	@Override
	public String clean(String input) {
		// TODO Auto-generated method stub
		input = input.replaceAll(removeCommonWords.toString(), " ");
		//input = input.replaceAll(removeSingleCharacterOrOnlyDigits.toString(), " ");
		//input = input.replaceAll(removeTime.toString(), " ");
		//input = input.replaceAll("(\\s+>+\\s+)+", " ");
		return input;
	}
	public static void main(String args[]){
		System.out.println(removeCommonWords.toString());
		System.out.println(removeSingleCharacterOrOnlyDigits.toString());
		String toTest = "1) Hello( ( b.debanjan1 At, 2) , i 1 >>>> (EDT) ) ) ) 0.7% 0.8 at j (EST) thinking 3) 23:30am IMAP (IMAP) Imap Imapped \"how\" \" 3 in 34 into p To are. you . the thetoday ........ The And what from fromx is going . on. in your to life &nbsp; apart.. from&nbsp; the usual&nbsp;&nbsp;&nbsp;";
		NeutralWordsCleaner cleaner = new NeutralWordsCleaner();
		try {
			System.out.println(cleaner.clean(toTest));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
