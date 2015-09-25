package com.paperlink.service;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import com.itextpdf.awt.geom.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.Vector;
import com.paperlink.domain.PaperGlyphs;

public class PDTextLocationListener extends PDFTextStripper {

	private PaperGlyphs glyphs;
	float pageHeight;
	private StringBuilder textBuilder; // for debugging
	
	private String PRE_FILTER;
	private float MIN_FONTSIZE;
	private float BOX_UPRISE;
	private float XGROW;
	private float YGROW;
	
	public PDTextLocationListener(PaperGlyphs glyphs, float pageHeight, String PRE_FILTER, float MIN_FONTSIZE, float BOX_UPRISE, float XGROW, float YGROW) throws IOException {
        super.setSortByPosition(true);
        
        this.glyphs = glyphs;
        this.pageHeight = pageHeight;
        
		this.PRE_FILTER = PRE_FILTER; // regex
		
		this.MIN_FONTSIZE = MIN_FONTSIZE;
		this.BOX_UPRISE = BOX_UPRISE;
		this.XGROW = XGROW;
		this.YGROW = YGROW;

		this.textBuilder = new StringBuilder();
	}

	static float previousBaseline = -9999f;
	static float previousXpos = 0f;

	final static String TEXTBLOCK_DELIMETER = "\n";
	final static String SPACE = " ";
	final static String NEWLINE = "\n";
	
    @Override
    protected void processTextPosition(TextPosition text) {

        String tChar = text.getCharacter();

        float x = text.getX();
        float xa = text.getXDirAdj();

        float y = pageHeight - text.getY();
        float ya = pageHeight - text.getYDirAdj();

        float fontSize = text.getFontSizeInPt();
        //PDFont pdFont = text.getFont();
        
        float textWidth = text.getWidth();
        float textHeight = text.getHeightDir(); //.getHeight();
        
        float spaceWidth = text.getWidthOfSpace();
        float xScale = text.getXScale();
        float yScale = text.getYScale();
        
       
		//if (textHeight < MIN_FONTSIZE)
		//	return;
       
        //System.out.printf("{%s} [%.1f,%.1f] <%.1fx%.1f> %.1f pt, scale(%.1f %.1f) sw: %.1f", tChar, xa, ya, textWidth, textHeight, fontSize, xScale, yScale, spaceWidth);
        //System.out.println();
		
		BaseFont bf = null;
		try {
			bf = BaseFont.createFont("c:\\Windows\\Fonts\\NanumGothic.ttf",BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
		}
		
		LineSegment baseLine = new LineSegment(new Vector(x, y + BOX_UPRISE, 0f), new Vector(x + textWidth, y + BOX_UPRISE, 0f));
		Rectangle rect = new Rectangle(x, y + BOX_UPRISE, textWidth, textHeight);
		rect.grow(0f, textHeight * 0.2);
		
		float currentBaseline = y;
				
		if (Math.abs(previousBaseline - currentBaseline) > 2.0f) { 
			textBuilder.append(NEWLINE);	// insert word break
			glyphs.addOneGlyph(NEWLINE, rect, baseLine, BaseColor.ORANGE, bf, textHeight);
			previousBaseline = currentBaseline;
			previousXpos = text.getX();			
		}
		else if (x - previousXpos > textWidth) { 
			textBuilder.append(SPACE);	// insert space to break word
			glyphs.addOneGlyph(SPACE, rect, baseLine, BaseColor.ORANGE, bf, textHeight);
			previousXpos = text.getX();
			
		}	
		
		if (PRE_FILTER.length() < 1 || (PRE_FILTER.length() > 1 && tChar.matches(PRE_FILTER))) {
			textBuilder.append(tChar); // String.format("{%s:%.1f}", tChar, rect.getWidth()));
			glyphs.addOneGlyph(tChar, rect, baseLine,  BaseColor.ORANGE, bf, (float) fontSize);
			
			previousXpos = text.getX();

		}
			

    }

	public String getText() {
		return textBuilder.toString();
	}
}
