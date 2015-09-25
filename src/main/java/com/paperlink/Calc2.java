 package com.paperlink;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.hibernate.Session;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.awt.geom.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.paperlink.analyzer.LanguageAnalyzer;
import com.paperlink.analyzer.LanguageCheck;
import com.paperlink.analyzer.PatternAnalyzer;
import com.paperlink.config.JobProfileService;
import com.paperlink.config.JobProfiles;
import com.paperlink.domain.UserDefinedLink;
import com.paperlink.domain.PaperGlyphs;
import com.paperlink.domain.TextLink;
import com.paperlink.domain.Token;
import com.paperlink.service.PDTextLocationListener;
import com.paperlink.service.PlainTextRenderListener;

@Component
public class Calc2 implements Calculator {
    private static final Logger logger = LogManager.getFormatterLogger("Calc2");

	// analyze parameters
	private String BOOK_TITLE;
	private String PRE_FILTER;
	private String LANGUAGE;
	private String[] LANGUAGES;
	private int MIN_WORDSIZE;

	private String SEARCH_SITE;
	private String USER_DEFINED_LINK_FILE;

	private String PATTERN_FILTER;
	private String PATTERN_SELECTOR;
	private int MIN_PATTERNSIZE;
	private String PATTERN_URL;
	private String PATTERN_TYPE;

	// painting parameters
	private boolean BG_FILL;
	private String BG_COLOR;
	
	private boolean COVER_TEXT;
	private String TEXT_COLOR;
	private float MIN_FONTSIZE;
	private float MAX_FONTSIZE;

	private float BOX_UPRISE;
	private float BOX_WIDTHGROW;
	private float BOX_HEIGHTGROW;
	
	private boolean INCLUDE_COORDIMAGE;
	private boolean CLIPPING_COORDIMAGE;
	private String COORDIMAGE_RESOURCE;
	private String TARGET_PRINTER;

	private String STAMP_MESSAGE;
	private String MADEBY;
	private String DOC_ROOT;
	private String PDF_RESOURCE;
	private int START_PAGE;
	private int END_PAGE;

	
	private String TEXT_LISTENER;
	
	private void init_profile(JobProfiles job) {
		DOC_ROOT = job.getDocRoot();

		BOOK_TITLE = job.getBookTitle();
		PDF_RESOURCE = job.getPdfResource();
		PRE_FILTER = job.getPreFilter();
		LANGUAGE = job.getLanguages();
		LANGUAGES = job.getLanguages().split("\\,");
		MIN_WORDSIZE = job.getMinWordSize();

		SEARCH_SITE = job.getSearchSite();
		USER_DEFINED_LINK_FILE = job.getUserDefinedLinkFile();

		PATTERN_FILTER = job.getPatternFilter();
		PATTERN_SELECTOR = job.getPatternSelector();
		MIN_PATTERNSIZE = job.getMinPatternSize();
		PATTERN_URL = job.getPatternURL();
		PATTERN_TYPE = job.getPatternType();

		// painting parameters
		COVER_TEXT = job.getCoverText().equals("yes");
		TEXT_COLOR = job.getTextColor();

		BG_COLOR = job.getBackgroundColor();
		BG_FILL = job.getIsFillBackground().equals("yes");
		MIN_FONTSIZE = job.getMinFontSize();
		MAX_FONTSIZE = job.getMaxFontSize();
		BOX_UPRISE = job.getBoxUprise();
		BOX_WIDTHGROW = job.getBoxWidthGrow();
		BOX_HEIGHTGROW = job.getBoxHeightGrow();
		
		INCLUDE_COORDIMAGE = job.getIncludeCoordImage().equals("yes");
		CLIPPING_COORDIMAGE = job.getCoordImageClipping().equals("yes");
		COORDIMAGE_RESOURCE = job.getCoordImageResource();
		TARGET_PRINTER = job.getTargetPrinter();

		START_PAGE = job.getStartPage();
		END_PAGE = job.getEndPage();

		STAMP_MESSAGE = job.getStampMessage();
		MADEBY = job.getMadeby();
		
		TEXT_LISTENER = job.getTextListener();
	}

	private void printProfile(PrintWriter writer) {
        writer.println("###### Job Profile ###");
        writer.println("# book title: " + BOOK_TITLE);
        writer.println("# languages: " + LANGUAGE);

        writer.println("# minWordSize: " + MIN_WORDSIZE);
    	writer.println("# pdfResource: " + PDF_RESOURCE);
    	writer.println("# preFilter: " + PRE_FILTER);
    	writer.println("# patternFilter: " + PATTERN_FILTER);
    	writer.println("# patternSelector: " + PATTERN_SELECTOR);

    	writer.println("# patternType: " + PATTERN_TYPE);
    	writer.println("# patternURL: " + PATTERN_URL);

    	writer.println("# minPatternSize: " +  MIN_PATTERNSIZE);
    	writer.println("# searchSite: " + SEARCH_SITE);
    	writer.println("# coverText: " + (COVER_TEXT ? "yes" : "no"));
    	writer.println("# textColor: " + TEXT_COLOR);
    	writer.println("# isFillBackground: " + (BG_FILL ? "yes" : "no"));
    	writer.println("# backgroundColor: " + BG_COLOR);
    	writer.println("# box uprise: " + BOX_UPRISE);

    	//writer.println("imageResource:" + jobProfileService.getJob().getImageResource());
    	writer.println("# stampMessage: " + STAMP_MESSAGE);
    	writer.println("# includeCoordImage: " + INCLUDE_COORDIMAGE);
    	writer.println("# coordImageClipping: " + CLIPPING_COORDIMAGE);
    	writer.println("# coordImageResource: " + COORDIMAGE_RESOURCE);
    	writer.println("# targetPrinter: " + TARGET_PRINTER);
    	writer.println("# made by: " + MADEBY);
    	writer.println("# start page: " + START_PAGE);
    	writer.println("# end page: " + END_PAGE);

    	writer.println("# DocRoot: " + DOC_ROOT);
    	writer.println("# produced: " + DateUtil.now());
    	
    	writer.println("### end of Job Profile ###");
    	
        logger.info("###### Job Profile ###");
        logger.info("# book title: " + BOOK_TITLE);
        logger.info("# languages: " + LANGUAGE);

        logger.info("# minWordSize: " + MIN_WORDSIZE);
    	logger.info("# pdfResource: " + PDF_RESOURCE);
    	logger.info("# preFilter: " + PRE_FILTER);
    	logger.info("# patternFilter: " + PATTERN_FILTER);
    	logger.info("# patternSelector: " + PATTERN_SELECTOR);

    	logger.info("# patternType: " + PATTERN_TYPE);
    	logger.info("# patternURL: " + PATTERN_URL);

    	logger.info("# minPatternSize: " +  MIN_PATTERNSIZE);
    	logger.info("# searchSite: " + SEARCH_SITE);
    	logger.info("# coverText: " + (COVER_TEXT ? "yes" : "no"));
    	logger.info("# textColor: " + TEXT_COLOR);
    	logger.info("# isFillBackground: " + (BG_FILL ? "yes" : "no"));
    	logger.info("# backgroundColor: " + BG_COLOR);
    	logger.info("# box uprise: " + BOX_UPRISE);

    	//logger.info("imageResource:" + jobProfileService.getJob().getImageResource());
    	logger.info("# stampMessage: " + STAMP_MESSAGE);
    	logger.info("# includeCoordImage: " + INCLUDE_COORDIMAGE);
    	logger.info("# coordImageClipping: " + CLIPPING_COORDIMAGE);
    	logger.info("# coordImageResource: " + COORDIMAGE_RESOURCE);
    	logger.info("# targetPrinter: " + TARGET_PRINTER);
    	logger.info("# made by: " + MADEBY);
    	logger.info("# start page: " + START_PAGE);
    	logger.info("# end page: " + END_PAGE);

    	logger.info("# DocRoot: " + DOC_ROOT);
    	logger.info("# produced: " + DateUtil.now());
    	logger.info("### end of Job Profile ###");

	}
	
	public void writeDocumentInfo(PrintWriter writer, int START_PAGE, int maxPage) {
		printProfile(writer);
		
		writer.println(String.format("processed pages: %d ~ %d", START_PAGE, maxPage));
		
	}
	
	public void writePageInfo(PrintWriter writer, int page, int nLinks, Rectangle box) {
		
		writer.println(String.format("Page %d: [%.0f %.0f %.0f %.0f], %d links", 
				page, box.getX(), box.getY(), box.getX()+box.getWidth(), box.getY() + box.getHeight(), nLinks));
	}
	
	
	// 루신 분석기로 언어 분석
	private List<TextLink> makeTextLinklist(String language, String text, List<UserDefinedLink> userLinkList, String linkType, String url, int minWordLength, String pSelector) {

		List<Token> tokenList = null;
		List<TextLink> textlinkListbyLanguage = new ArrayList<TextLink>();
		
		if (language.equals("USER_DICT") && userLinkList.size() > 0) {
			
			for (UserDefinedLink userLink : userLinkList) {
				tokenList = PatternAnalyzer.getTokensByPattern(text, userLink.getName());
				
				if (tokenList.size() > 0)
					logger.trace("@makeTextLinklist: USER_DICT - %d of {%s} found  !\n", tokenList.size(), userLink.getName() );
			

				for (Token token : tokenList) {
					
					textlinkListbyLanguage.add(new TextLink(userLink.getResource(), linkType, userLink.getUrl(), token.getStart(), token.getEnd()));
				}
			}
		}
		else if (language.equals("PATTERN")) {
			tokenList = PatternAnalyzer.getTokensByPattern(text, PATTERN_FILTER);

			StringBuilder queryString = new StringBuilder();
	        
			//System.out.println("pattern: " + pSelector);
			for (Token token : tokenList) {
				String word = text.substring(token.getStart(), token.getEnd());
				
				if (pSelector != null && pSelector.length() > 0) {
					Pattern pattern = Pattern.compile(pSelector);
			        Matcher matcher = pattern.matcher(word);
			       
			        while(matcher.find()) {
			            queryString.append(matcher.group(1) + matcher.group(2)); // it MUST MODIFIED! only process two groups
	
			        }
			        //System.out.println("from: " + getText().substring(start, end) + " found: " + param.toString());
		        }
				else
					queryString.append(word);
			
				textlinkListbyLanguage.add(new TextLink(queryString.toString(), linkType, url, token.getStart(), token.getEnd()));
				
				logger.info("[%s] %s", language, word);
				
			}
		}
		else { // (language.equals("KR","CN","EN","JP") ) {
			
			LanguageAnalyzer analyzer = new LanguageAnalyzer(language);
			tokenList = analyzer.analyze(text);
			
			for (Token token : tokenList) {

				//String word = text.substring(token.getStart(), token.getEnd());
				
				if (token.getStem().length() >= MIN_WORDSIZE && LanguageCheck.onlyLanguage(language, token.getStem()) ) {
					textlinkListbyLanguage.add(new TextLink(token.getStem(), linkType, analyzer.getSearchURL(language), token.getStart(), token.getEnd()));
				
					logger.info("[%s] %s", language, token.getStem());
				}

			}
		}
		logger.trace("@makeTextLinklist:Total %d [%s] tokens found", tokenList.size(), language);
	
		return textlinkListbyLanguage;
	}
		

	private PaperGlyphs getGlyphsFromTextListener(PdfReader reader, int page) throws IOException {

		PaperGlyphs glyphs = new PaperGlyphs();
		Rectangle pageRect = new Rectangle(reader.getPageSize(page));

		RenderListener listener = new PlainTextRenderListener(glyphs, PRE_FILTER, MIN_FONTSIZE, BOX_UPRISE, BOX_WIDTHGROW, BOX_HEIGHTGROW);
		PdfContentStreamProcessor streamProcessor = new PdfContentStreamProcessor(listener);
		streamProcessor.processContent(ContentByteUtils.getContentBytesForPage(reader, page),
				reader.getPageN(page).getAsDict(PdfName.RESOURCES));
		streamProcessor.reset();

		//logger.trace("@getGlyphsFromTextListener: [%s]\n", ((PlainTextRenderListener) listener).getText());

		return glyphs;
	}
	
	private PaperGlyphs getGlyphsFromPDTextListener(String job_file, int page) throws Exception {

		PaperGlyphs glyphs = new PaperGlyphs();
		//Rectangle pageRect = new Rectangle(reader.getPageSize(page));

		PDTextLocationListener text_run = null;
        try {
        	PDDocument document = PDDocument.load(new File(job_file));
            
            if (document.isEncrypted()) {
                document.decrypt("");
            }
            
            //PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDPage pdPage = (PDPage) document.getDocumentCatalog().getAllPages().get(page-1); // in pdfbox, page starts from 0
            
            PDRectangle cropBox = pdPage.getCropBox();
            if (cropBox == null)
                cropBox = pdPage.getMediaBox();

            text_run = new PDTextLocationListener(glyphs, cropBox.getHeight(), PRE_FILTER, MIN_FONTSIZE, BOX_UPRISE, BOX_WIDTHGROW, BOX_HEIGHTGROW);
            
            if (pdPage.getContents() != null)
            	text_run.processStream(pdPage, pdPage.findResources(), pdPage.getContents().getStream());
            
            document.close();
        }
        finally {
        	
        }
        
		logger.info("@getGlyphsFromPDTextListener: [%s]", text_run.getText());

  
		return glyphs;
	}
	
    private void creatJobPdf(PdfReader reader, int start_page, int end_page, String file) 
    		throws DocumentException, IOException {
        Document document = new Document();

        PdfCopy copy = new PdfCopy(document, new FileOutputStream(file));
        document.open();
        
        for (int i = start_page; i <= end_page; i++)
            copy.addPage(copy.getImportedPage(reader, i));
        
        document.close();
    }
    
	@Override
	public int doJob(JobProfileService jobProfileService, List<UserDefinedLink> userDict, int textColor, int backgroundColor) {

		init_profile(jobProfileService.getJob());

		final String OUTPUT_FOLDER = DOC_ROOT + PDF_RESOURCE;
		File file = new File(OUTPUT_FOLDER);
		if (!file.exists()) {
			if (file.mkdirs())
				logger.trace("@doJob:Directory is created!");
			else
				logger.error("@doJob:Failed to create directory!");
		}
		
		try {						
			PdfReader reader = new PdfReader(DOC_ROOT + PDF_RESOURCE + ".pdf");

			if (reader.isEncrypted()) {
				logger.error("@doJob: Encripted pdf!");
				return 0;
			}

			logger.trace("@doJob:+++++++++ 1. begin DoJob %s (%s) ++++++++\n", PDF_RESOURCE, BOOK_TITLE);

			String job_file = OUTPUT_FOLDER + "\\" + PDF_RESOURCE + "_copied.pdf";
			String stamperd_file = OUTPUT_FOLDER + "\\" + PDF_RESOURCE + ".pdf";

			creatJobPdf(reader, START_PAGE, END_PAGE, job_file);			
			reader.close();
			
			// reopen
			reader = new PdfReader(job_file);
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(stamperd_file));
			
			PrintWriter writer = new PrintWriter(new FileOutputStream(OUTPUT_FOLDER + "\\" + PDF_RESOURCE + "_coord.txt"));
			writeDocumentInfo(writer, START_PAGE, END_PAGE);
			
			// 좌표이미지 파일
	        Document document = new Document();

	        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(OUTPUT_FOLDER + "\\" + PDF_RESOURCE + "_coord.pdf"));
	        // step 3
	        document.open();
				        
			for (int page = START_PAGE; page <= END_PAGE; page++) {
			
				Rectangle pageSizeWithRotation = new Rectangle(reader.getPageSizeWithRotation(page));
				
				PaperGlyphs glyphs = null;
				
				if (TEXT_LISTENER != null && TEXT_LISTENER.equals("PDFBOX")) /** needs refactoring **/
					glyphs = getGlyphsFromPDTextListener(job_file, page); 
				else
					glyphs = getGlyphsFromTextListener(reader, page);
	
				List<TextLink> textlinkList = new ArrayList<TextLink>();

				// Step 1: 토큰 분석해서 텍스트 모두 가져오기
				for (String language : LANGUAGES) {
					if (language.length() < 2)
						break;
					
					logger.trace("@doJob: ########## %s analyze of %d page #########\n", language, page);

					textlinkList.addAll(
							makeTextLinklist(language, glyphs.getText(), userDict, "TEXT", "", MIN_WORDSIZE, ""));


					logger.trace("@doJob: ------ end of %s analyze %d page -----\n\n", language, page);
				}

				// 패턴으로 링크 만들기
				if (PATTERN_FILTER.length() > 1) {
					logger.trace("@doJob:########## PATTERN analyze of %d page #########\n", page);

					textlinkList.addAll(
							makeTextLinklist("PATTERN", glyphs.getText(), userDict, PATTERN_TYPE, PATTERN_URL, MIN_PATTERNSIZE, PATTERN_SELECTOR));
					
					logger.trace("@doJob: ----- end of PATTERN analyze of %d page ----- \n\n", page);

				}
				
				// user-dict의 토큰으로 링크 만들기
				if (userDict.size() > 1) {
					System.out.printf("@doJob:########## USERDICT analyze of %d page #########\n", page);

					textlinkList.addAll(
							makeTextLinklist("USER_DICT", glyphs.getText(), userDict, "LINK", "", MIN_WORDSIZE, ""));
					
					logger.trace("@doJob: ----- end of USERDICT analyze of %d page ----- \n\n", page);
				}
				
				if (textlinkList.size() > 0) {
					writePageInfo(writer, page, textlinkList.size(), pageSizeWithRotation);

					logger.trace("@doJob:Total %d tokens found at %d page #######\n\n", textlinkList.size(), page);
				}
				// Step 2

				// 복사된 PDF파일에 마킹
				PdfContentByte canvas = stamper.getOverContent(page);

				for (TextLink textlink : textlinkList) {
					if (textlink.getLinkType().equals("LINK")) {
						glyphs.setLink(canvas, textlink.getQueryString(), textlink.getLinkURL(), textlink.getStart(), textlink.getEnd());
						glyphs.showLinkOutlines(canvas, textlink.getStart(), textlink.getEnd(), textColor);

						if (COVER_TEXT) {
							glyphs.coverTexts(canvas, textlink.getStart(), textlink.getEnd(), textColor, backgroundColor, BG_FILL);
						}
					}
				}

				// Step 3
				// 좌표 이미지 만들기
				document.setPageSize(reader.getPageSizeWithRotation(page));
				
				if (INCLUDE_COORDIMAGE && canvas != null) {
					glyphs.addCoordImage(canvas, textlinkList, String.format(COORDIMAGE_RESOURCE, page),
							pageSizeWithRotation, CLIPPING_COORDIMAGE);
				}
				
				if (pdfWriter != null) {
					glyphs.addCoordImage(pdfWriter.getDirectContent(), textlinkList, String.format(COORDIMAGE_RESOURCE, page),
							pageSizeWithRotation, CLIPPING_COORDIMAGE);
				}
				
				document.newPage();
				
				// Step 4
				// 좌표 파일 만들기
				for (TextLink textlink : textlinkList) {
					glyphs.printLink(writer, page, textlink.getQueryString(), textlink.getLinkType(), textlink.getLinkURL(), 
							textlink.getStart(),textlink.getEnd());
				}
								
			}

			if (stamper != null)
				stamper.close();

			writer.close();
			
			document.close();
			
			reader.close();


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		logger.trace("+++++++++ end DoJob %s (%s) +++++\n\n", PDF_RESOURCE, BOOK_TITLE);

		return 0;
	}

	@Override
	public void doScan(JobProfileService jobProfileService) {

	}

	@Override
	public void showInfo(JobProfileService jobProfileService) {

	}
}
