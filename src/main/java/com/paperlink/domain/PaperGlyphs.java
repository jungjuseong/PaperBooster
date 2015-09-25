package com.paperlink.domain;

import com.itextpdf.awt.geom.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.parser.LineSegment;

import com.paperlink.PaperLinkApplication;
import com.paperlink.analyzer.HexStringConverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// PaperGlyphs는 한 페이지에 있는 모든 글자(glyph)에 대한 각각의 정보를 리스트로 담는다

public class PaperGlyphs {
	private StringBuilder text;
	private List<Rectangle> boundingRectList;
	private List<LineSegment> baselineList;
	private List<BaseColor> fillColorList;
	private List<Float> fontHeightList;

	private List<BaseFont> fontList;

	public PaperGlyphs() {
		text = new StringBuilder();
		boundingRectList = new ArrayList<Rectangle>();
		fontList = new ArrayList<BaseFont>();
		fontHeightList = new ArrayList<Float>();

		fillColorList = new ArrayList<BaseColor>();
		baselineList = new ArrayList<LineSegment>();		
}

	public Rectangle getBoundingRect(int index) {

		return boundingRectList.get(index);
	}

	public void addOneGlyph(String ch, Rectangle rect, LineSegment baseline, BaseColor fill_color, BaseFont font, float fontHeight) {
		

		if (ch.length() != 1) {
			System.out.printf("Error: {%s} %d Its not one character!\n", ch, ch.length());
			return;
		}
		text.append(ch);
		boundingRectList.add(rect);
		baselineList.add(baseline);

		fillColorList.add(fill_color);
		fontList.add(font);
		fontHeightList.add(fontHeight);
	}

	public float getStartPointX(int index) {
		return baselineList.get(index).getStartPoint().get(0);
		
	}

	public float getStartPointY(int index) {		
		return baselineList.get(index).getStartPoint().get(1);
	}

	public BaseFont getFont(int index) {

		return fontList.get(index);
	}


	public BaseColor getFillColor(int index) {
			return fillColorList.get(index);
	}

	public float getFontHeight(int index) {
		
		if (fontHeightList.size() <= index) {
			System.out.printf("# index error %d, %d\n", index, fontHeightList.size());
			return 0f;
		}
		
		return (float) fontHeightList.get(index);
	}

	public String getText() {
		return text.toString();
	}

	public String getText(int start, int end) {
		return getText().substring(start, end);
	}

	public Rectangle getBoundingRectBetween(int start, int end) {

			return boundingRectList.get(start).union(boundingRectList.get(end-1));		
	}
	
	// 두 라인에 걸쳐 있는 단어를 다룰 때
	private int getNewlineIndex(int start, int end) {

		float BaseLine = (float) baselineList.get(start).getStartPoint().get(1);
		
		for (int index = start; index < end; index++) {
			if (BaseLine != (float) baselineList.get(index).getStartPoint().get(1)) {
				
				return index;
			}
		}
		return end;		
	}
	
	public void setLink(PdfContentByte canvas, String queryString, String linkURL, int start, int end) {
		
		String param = queryString;

		if (queryString != null && queryString.length() > 1) {
	
			try {
				if (!param.matches("[a-zA-z0-9\\-\\.]+"))
					param = HexStringConverter.getHexStringConverterInstance().stringToHex(queryString);
			} catch (UnsupportedEncodingException ex) {
				Logger.getLogger(PaperLinkApplication.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		Rectangle rect = getBoundingRectBetween(start, end);
		
		canvas.setAction(new PdfAction(linkURL + param), 
				(float) rect.getX(), (float) rect.getY(), (float) rect.getX()+(float) rect.getWidth(), (float) rect.getY()+ (float) rect.getHeight()); // add the link
	}

	public void setLinks(PdfContentByte canvas, String queryString, String linkURL, int start, int end) {

		int newlineIndex = getNewlineIndex(start, end);
		
		if (newlineIndex < end) {
			setLink(canvas, queryString, linkURL, start, newlineIndex);
			setLink(canvas, queryString, linkURL, newlineIndex+1, end);
		}
		else
			setLink(canvas, queryString, linkURL, start, end);

	}
	
	public void printLink(PrintWriter writer, int page, String queryString, String linkType, String linkURL, int start, int end) {
		
		Rectangle rect = getBoundingRectBetween(start, end);
		
		if (linkType.equals("TEXT")) {
			writer.printf("{\"%s\",\"%d\",\"%.0f %.0f %.0f %.0f\",\"%s\"}", getText().substring(start, end), page,
					rect.getX(), rect.getY(), rect.getX()+rect.getWidth(), rect.getY()+rect.getHeight(), linkType);
			writer.println();
			
			return;
		}
		
		String linkParam = queryString;
		try {
			if (!linkParam.matches("[a-zA-z0-9\\-\\.]+"))
				linkParam = HexStringConverter.getHexStringConverterInstance().stringToHex(queryString);
			
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(PaperLinkApplication.class.getName()).log(Level.SEVERE, null, ex);
		}
		
			
		writer.printf("{\"%s\",\"%d\",\"%.0f %.0f %.0f %.0f\",\"%s\",\"%s\"}", getText().substring(start, end), page,
				rect.getX(), rect.getY(), rect.getX()+rect.getWidth(), rect.getY()+rect.getHeight(), linkType, linkURL + linkParam);
		writer.println();
	}

	public void printLinks(PrintWriter writer, int page, String queryString, String linkType, String linkURL, int start, int end) {

		int newlineIndex = getNewlineIndex(start, end);
		
		if (newlineIndex < end) {
			printLink(writer, page, queryString,linkType, linkURL, start, newlineIndex);
			printLink(writer, page, queryString, linkType, linkURL, newlineIndex+1, end);
		}
		else
			printLink(writer, page, queryString, linkType, linkURL, start, end);

	}
	
	public void coverText(PdfContentByte canvas, int start, int end, int textColor, int backGroundColor, boolean BG_FILL) {

		int BG_CYAN = (backGroundColor>> 24) & 0x000000ff;
		int BG_MAGENTA = (backGroundColor >> 16) & 0x000000ff;
		int BG_YELLOW = (backGroundColor >> 8) & 0x000000ff;
			
		int TEXT_CYAN = (textColor >> 24) & 0x000000ff;
		int TEXT_MAGENTA = (textColor >> 16) & 0x000000ff;
		int TEXT_YELLOW = (textColor >> 8) & 0x000000ff;
		//int TEXT_BLACK = (textColor >> 0) & 0x000000ff;

		if (BG_FILL) 
			fillBackground(canvas, getBoundingRectBetween(start, end), BG_CYAN, BG_MAGENTA, BG_YELLOW);
		
		for (int pos = start; pos < end; pos++) {
			
			if (true) { //PaperColor.hasBlackColor(getFillColor(pos), 10)) { // always
				
	
					canvas.saveState();
					canvas.beginText();
					
					canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
					canvas.setCMYKColorFill(TEXT_CYAN, TEXT_MAGENTA, TEXT_YELLOW, 0x00);

					canvas.setFontAndSize(getFont(pos), (float) getFontHeight(pos));
					canvas.setTextMatrix(getStartPointX(pos), getStartPointY(pos)); 
					canvas.showText(getText().substring(pos, pos + 1));
					
					canvas.endText();
					canvas.restoreState();

			}
		}

	}

	public void coverTexts(PdfContentByte canvas, int start, int end, int TEXT_COLOR, int BG_COLOR, boolean BG_FILL) {
		
		int newlineIndex = getNewlineIndex(start, end);
		
		if (newlineIndex < end) {
			coverText(canvas,start, newlineIndex, TEXT_COLOR, BG_COLOR, BG_FILL);
			coverText(canvas, newlineIndex+1, end, TEXT_COLOR, BG_COLOR, BG_FILL);
		}
		else
			coverText(canvas, start, end, TEXT_COLOR, BG_COLOR, BG_FILL);

	}
	
	public void fillBackground(PdfContentByte canvas, Rectangle rect, int C, int M, int Y) {
		canvas.saveState();

		canvas.setLineWidth(.0f);

		canvas.setCMYKColorFill(C, M, Y, 0x00);	

		canvas.rectangle((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float)  rect.getHeight());
		canvas.fill();

		canvas.restoreState();
	}

	public void showLinkOutline(PdfContentByte canvas, int start, int end, int strokeColor) {
			
		int C = (strokeColor >> 24) & 0x000000ff;
		int M = (strokeColor >> 16) & 0x000000ff;
		int Y = (strokeColor >> 8) & 0x000000ff;
		int K = (strokeColor >> 0) & 0x000000ff;

		canvas.saveState();

		canvas.setLineWidth(.1f);
		canvas.setCMYKColorFill(C, M, Y, K);

		Rectangle rect = getBoundingRectBetween(start, end);

		canvas.rectangle((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float)  rect.getHeight());
		canvas.stroke();

		canvas.restoreState();
	}
	
	public void showLinkOutlines(PdfContentByte canvas, int start, int end, int strokeColor) {
	
		int newlineIndex = getNewlineIndex(start, end);
		
		if (newlineIndex < end) {
			showLinkOutline(canvas, start, newlineIndex, strokeColor);
			showLinkOutline(canvas, newlineIndex+1, end, strokeColor);

		}
		else
			showLinkOutline(canvas, start, end, strokeColor);

	}
	
	public static final float IMAGE_SCALE_FACTOR = 6f;

	public void addCoordImage(PdfContentByte canvas, List<TextLink> textLinkList, final String RESOURCE,
			Rectangle cropBox, boolean coordImageClipping) throws DocumentException, IOException {

		if (textLinkList.size() > 0) {
			canvas.saveState();
			
			if (coordImageClipping) {
				canvas.setLineWidth(0.0f);
	
				for (TextLink textLink : textLinkList) {
					Rectangle rect = getBoundingRectBetween(textLink.getStart(), textLink.getEnd());					
					
					canvas.rectangle((float) rect.getX(), (float) rect.getY(), (float)  rect.getWidth(), (float) rect.getHeight());
				}
				canvas.clip();
				canvas.newPath();
			}
			
			Image coordImage = Image.getInstance(RESOURCE);
			if (coordImage.isMaskCandidate())
				coordImage.makeMask();

			coordImage.scalePercent(IMAGE_SCALE_FACTOR, IMAGE_SCALE_FACTOR);

			float yOffset = coordImage.getScaledHeight() - (float) cropBox.getHeight();
			if (yOffset < 0.0f) {
				yOffset = 0.0f;
			}
			coordImage.setAbsolutePosition((float) cropBox.getX(), (float) (cropBox.getY() - yOffset));

			canvas.addImage(coordImage);
			
			canvas.restoreState();
		}
	}
}
