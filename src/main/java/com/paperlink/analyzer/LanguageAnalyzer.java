package com.paperlink.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import com.paperlink.domain.Token;

public class LanguageAnalyzer {
	
	private Analyzer lucene_analyzer;

	public LanguageAnalyzer(String language) {
				
		if (true == language.equals("JP")) {
			lucene_analyzer = new JapaneseAnalyzer();
		}
		else if (true == language.equals("KO")) {
			lucene_analyzer = new KoreanAnalyzer();
		}
		else if (true == language.equals("EN")) {
			lucene_analyzer = new EnglishAnalyzer();
		}
		else if (true == language.equals("CN")) {
			lucene_analyzer = new SmartChineseAnalyzer();
		}
		else 
			lucene_analyzer = new StandardAnalyzer();
	}
	
	public String getSearchURL(String language) {
		if (language.equals("KR")) {
			return "krdic.naver.com/search.nhn?dic_where=krdic&query=";
		}
		else if (language.equals("EN")) {
			return "endic.naver.com/search.nhn?dicWhere=endic&query=";
		}
		else if (language.equals("CN")) {
			return "cndic.naver.com/search.nhn?dicWhere=cndic&query=";
		}
		else if (language.equals("JP")) {
			return "jpdic.naver.com/search.nhn?dicWhere=jpdic&query=";
		}		
		return "";
	}
	
	public List<Token> analyze(String text) {

		List<Token> tokenList = new ArrayList<Token>();
	    //MorphAnalyzer kmorpher = new MorphAnalyzer();

		try {

			TokenStream tokenStream = lucene_analyzer.tokenStream("contents", new StringReader(text));

			OffsetAttribute offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);
			CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);

			tokenStream.reset();

			while (tokenStream.incrementToken()) {
				String term = termAtt.toString();
				tokenList.add(new Token(term, offsetAtt.startOffset(), offsetAtt.endOffset()));
/*
	            for (AnalysisOutput anOutput : kmorpher.analyze(term)) {
	            	System.out.println("@LanuageAnalyzer: " + term + " --> " + anOutput.getStem());
					tokenList.add(new Token(anOutput.getStem(), offsetAtt.startOffset(), offsetAtt.endOffset()));
					break;
	            }
*/
	        }
			tokenStream.end();
			tokenStream.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		lucene_analyzer.close();
		
		return tokenList;
	}
	
}
