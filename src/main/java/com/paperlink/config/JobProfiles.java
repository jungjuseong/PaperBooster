package com.paperlink.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "job")
public class JobProfiles {
	private String bookTitle;
	private String languages;
	private String pdfResource;
	private String preFilter;
	private int minWordSize;
	private String patternFilter;
	private String patternSelector;
	
	private String patternType;
	private int minPatternSize;
	private String patternURL;
	private String searchSite;
	private String userDefinedLinkFile;
	
	private String coverText;
	private String textColor; // panetone color
	private String isFillBackground;
	private String backgroundColor;
	private float boxUprise;
	private float boxWidthGrow;
	private float boxHeightGrow;

	private float minFontSize;
	private float maxFontSize;
	private String imageResource;
	private String stampMessage;
	private String includeCoordImage;  // 퐈표이미지를 포함할지 여부
	private String coordImageClipping;  // 좌표이미지를 박스로 클리핑할지
	private String coordImageResource;
	private String targetPrinter;
	private String madeby;
	private int startPage;
	private int endPage; // coord image를 씌울 페이지 한계 (테스트용)
	private String docRoot;
	//private Date date;
	
	private String textListener; // ITEXT or PDFBOX

}
