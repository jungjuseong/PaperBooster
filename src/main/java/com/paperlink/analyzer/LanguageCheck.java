package com.paperlink.analyzer;

import java.util.ArrayList;
import java.util.List;

public class LanguageCheck {
	static String HANGUL_P = "[\\x{1100}-\\x{11FF}\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]+";
	static String CJK_SHARE_HAN_P = "[\\x{2E80}-\\x{2EFF}\\x{31C0}-\\x{31EF}\\x{3200}-\\x{32FF}\\x{3400}-\\x{4DBF}\\x{4E00}-\\x{9FBF}\\x{F900}-\\x{FAFF}\\x{20000}-\\x{2A6DF}\\x{2F800}-\\x{2FA1F}]+";
	static String JAPANESE_P = "[\\x{3040}-\\x{309F}\\x{30A0}-\\x{30FF}\\x{31F0}-\\x{31FF}]+)";
	static String SIMPLECHINESE_P = "[\\p{script=Han}]+";
	static String ALPHABET_P = "[a-zA-Z}]+";

	static String NUMBER_P = "[\\p{N}]+";
	static String LETTER_P = "[\\p{L}]+";
	static String SYMBOL_P = "[\\p{S}]+";
	static String MARK_P = "[\\p{M}]+";
	static String FULLWIDTH_SYMBOL_P = "[。、？！（）]+";
	static String PUNCTUATION_P = "[\\p{P}]+";
	static String SEPARATOR_P = "[\\p{Z}]+";
	
    public LanguageCheck(){}

    public static List<String> getPatternList(String language) {
    	
    	List<String> patternList = new ArrayList<String>();
    	
		patternList.add(PUNCTUATION_P);
		patternList.add(SYMBOL_P);
		patternList.add(SEPARATOR_P);

    	if (language.equals("KR")) {
    		patternList.add(HANGUL_P);
    		patternList.add(CJK_SHARE_HAN_P);

    	}
    	
    	else if (language.equals("EN")) {
    		patternList.add(ALPHABET_P);
    	}
    	
    	else if (language.equals("CN")) {
    		patternList.add(SIMPLECHINESE_P);
    		patternList.add(FULLWIDTH_SYMBOL_P);
    	}
    	
    	else if (language.equals("JP")) {
    		patternList.add(JAPANESE_P);
    	}    	    
    	else
    		patternList.add(LETTER_P);

    	return patternList;
    }
    
    public static boolean onlyLanguage(String language, String word) {
    	
    	List<String> patterns = getPatternList(language);
    	
    	for (int i = 0; i < word.length(); i++) {
    		for (String p : patterns) {
    	    	
    			if (word.substring(i,i+1).matches(p))
    					return true;
    		}
    	}
    	
    	return false;
    }

}