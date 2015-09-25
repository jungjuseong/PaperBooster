package com.paperlink.analyzer;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.paperlink.domain.Token;

public class PatternAnalyzer {
		
	static String[] defaultPatterns = {
			"^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$", // URL
			"^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$" // email
	};
		
	
	public static List<Token> getTokensByPattern(String text, String patternString) {

        List<Token> tokenOffsetList = new ArrayList<Token>();

		Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {

        	tokenOffsetList.add(new Token(matcher.group(), matcher.start(), matcher.end()));
        }
		return tokenOffsetList;
	}
	
}
