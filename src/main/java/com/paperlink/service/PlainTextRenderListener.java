package com.paperlink.service;

import com.itextpdf.awt.geom.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.parser.*;
import com.paperlink.domain.PaperGlyphs;

import java.io.PrintWriter;

public class PlainTextRenderListener extends LocationTextExtractionStrategy {


	private PaperGlyphs glyphs;
	private String prefilter;
	private StringBuilder textBuilder; // for debugging
	
	private float MIN_FONTSIZE;
	private float BOX_UPRISE;
	private float XGROW;
	private float YGROW;

	
	public PlainTextRenderListener(PaperGlyphs glyphs, String prefilter, float MIN_FONTSIZE, float BOX_UPRISE, float XGROW, float YGROW) {
		this.glyphs = glyphs;
		this.prefilter = prefilter; // regex
		
		this.MIN_FONTSIZE = MIN_FONTSIZE;
		this.BOX_UPRISE = BOX_UPRISE;
		this.XGROW = XGROW;
		this.YGROW = YGROW;

		this.textBuilder = new StringBuilder();
	}

	public void renderImage(ImageRenderInfo renderInfo) {

		Matrix ctm = renderInfo.getImageCTM();
		float x = ctm.get(Matrix.I31);
		float y = ctm.get(Matrix.I32);

		float width = ctm.get(Matrix.I11);
		float height = ctm.get(Matrix.I22);

		// if (width > 9.0f && height > 9.0f) {
		// System.out.printf("Image Info: [%.2f,%.2f,%.2f,%.2f]\n", x, y, width,
		// height);
		// }
	}

	public String getText() {
		return textBuilder.toString();
	}

	static float previousBaseline = -9999f;
	static boolean wasBreaked = true;
	

	final static String TEXTBLOCK_DELIMETER = "\n";
	final static String NEWLINE = "\n";
	float x_diff = 0f;
	float lineBaseHeight = 0;
	float height_diff = 0f;

	@Override
	public void beginTextBlock() {
		textBuilder.append("<BT>\n");
		//glyphs.addOneGlyph(TEXTBLOCK_BREAK, null, null, null, null);
		//glyphs.addOneGlyph(TEXTBLOCK_DELIMETER, getZeroRect(textRenderInfo), textRenderInfo.getBaseline(), BaseColor.WHITE, textRenderInfo.getFont());
		
	}

	@Override
	public void endTextBlock() {
		textBuilder.append("\n<ET>\n");
		//glyphs.addOneGlyph(TEXTBLOCK_BREAK, null, null, null, null);
		//glyphs.addOneGlyph(WORD_BREAK, getZeroRect(textRenderInfo), textRenderInfo.getBaseline(), BaseColor.WHITE, textRenderInfo.getFont());
	}
	
	public void renderText1(TextRenderInfo textRenderInfo) {
		float fontHeight = textRenderInfo.getAscentLine().getStartPoint().get(1) - textRenderInfo.getDescentLine().getStartPoint().get(1);

		float currentBaseline = getBaseline(textRenderInfo).getStartPoint().get(1);
		float currentlineFontHeight = (float) getBoundingRect(textRenderInfo).getHeight();
		boolean x_diff_flag = true;
		
		if (Math.abs(previousBaseline - currentBaseline) > 2.0f) { 
			textBuilder.append(NEWLINE);	// insert word break
			glyphs.addOneGlyph(NEWLINE, getZeroRect(textRenderInfo), textRenderInfo.getBaseline(), BaseColor.WHITE, textRenderInfo.getFont(), fontHeight);
			previousBaseline = currentBaseline;
			
			currentlineFontHeight = (float) getBoundingRect(textRenderInfo).getHeight();
			x_diff = 0.0f;
			x_diff_flag = false;
		}

		if (prefilter.length() < 1) {

			if (textRenderInfo.getText().length() > 0) {
				textBuilder.append(String.format("<{%s:%.1f} %.1f:%.1f>\n",textRenderInfo.getText(), 
						getBoundingRect(textRenderInfo).getHeight(),
						textRenderInfo.getSingleSpaceWidth(), getBoundingRect(textRenderInfo).getWidth()));				
			}
						
			if (currentlineFontHeight != getBoundingRect(textRenderInfo).getHeight()) {
				x_diff_flag = false;
			}
			
			for (TextRenderInfo chInfo : textRenderInfo.getCharacterRenderInfos()) {
				
				if (chInfo.getText().equals(" ") && x_diff_flag) {
					x_diff += chInfo.getSingleSpaceWidth() - getBoundingRect(chInfo).getWidth();
				}
				
				if (chInfo.getText().length() > 0) {
					Rectangle movedRect = getBoundingRect(chInfo);
					movedRect.setLocation(movedRect.getX() + (double) x_diff, movedRect.getY());
				
					float chFontHeight = chInfo.getAscentLine().getStartPoint().get(1) - chInfo.getDescentLine().getStartPoint().get(1);

					//textBuilder.append(String.format("{%s:%.1f}", chInfo.getText(), getBoundingRect(chInfo).getWidth()));
					glyphs.addOneGlyph(chInfo.getText(), movedRect, moveBaseline(getBaseline(chInfo), x_diff, 0f), chInfo.getFillColor(), chInfo.getFont(), chFontHeight);
				}
			}

		} 
		else {
			if (textRenderInfo.getText().matches(prefilter)) {

				for (TextRenderInfo chInfo : textRenderInfo.getCharacterRenderInfos()) {
					float chFontHeight = chInfo.getAscentLine().getStartPoint().get(1) - chInfo.getDescentLine().getStartPoint().get(1);

						textBuilder.append(chInfo.getText() + "|");
						glyphs.addOneGlyph(chInfo.getText(), getBoundingRect(chInfo), getBaseline(chInfo), chInfo.getFillColor(), chInfo.getFont(), chFontHeight);
					
				}
			}
		}
	}
	
	@Override
	public void renderText(TextRenderInfo textRenderInfo) {

		float textFontHeight = textRenderInfo.getAscentLine().getStartPoint().get(1) - textRenderInfo.getDescentLine().getStartPoint().get(1);
		if (textFontHeight < MIN_FONTSIZE)
			return;
					
		float currentBaseline = getBaseline(textRenderInfo).getStartPoint().get(1);
		
		if (Math.abs(previousBaseline - currentBaseline) > 2.0f) { 
			textBuilder.append("\n^");	// insert word break
			glyphs.addOneGlyph(NEWLINE, getZeroRect(textRenderInfo), textRenderInfo.getBaseline(), BaseColor.WHITE, textRenderInfo.getFont(), textFontHeight);
			previousBaseline = currentBaseline;
			
			x_diff = 0.0f;
		}

		if (prefilter.length() < 1 || textRenderInfo.getText().matches(prefilter)) {

			if (textRenderInfo.getText().length() > 0) {
				textBuilder.append(String.format("<{%s:%.1f} %.1f:%.1f>\n",textRenderInfo.getText(), 
						getBoundingRect(textRenderInfo).getHeight(),
						textRenderInfo.getSingleSpaceWidth(), getBoundingRect(textRenderInfo).getWidth()));				
			}						
			
			for (TextRenderInfo chInfo : textRenderInfo.getCharacterRenderInfos()) {
				
				if (chInfo.getText().equals(" ")) {
					x_diff += chInfo.getSingleSpaceWidth() - getBoundingRect(chInfo).getWidth();
				}
				
				if (chInfo.getText().length() > 0) {
					Rectangle movedRect = getBoundingRect(chInfo);
					movedRect.setLocation(movedRect.getX() + (double) x_diff, movedRect.getY() + BOX_UPRISE);
					
					movedRect.grow(XGROW,  YGROW);
					
					float chFontHeight = chInfo.getAscentLine().getStartPoint().get(1) - chInfo.getDescentLine().getStartPoint().get(1);
					glyphs.addOneGlyph(chInfo.getText(), movedRect, moveBaseline(getBaseline(chInfo), x_diff, 0f), chInfo.getFillColor(), chInfo.getFont(), chFontHeight);
				}
			}

		} 

	}

	private LineSegment moveBaseline(LineSegment baseline, float dx, float dy) {
		
		float x1 = baseline.getStartPoint().get(0);
		float y1 = baseline.getStartPoint().get(1);

		float x2 = baseline.getEndPoint().get(0);
		float y2 = baseline.getEndPoint().get(1);
		
		return new LineSegment(new Vector(x1+dx, y1+dy,0f), new Vector(x2+dx, y2+dy,0f));
	}

	private LineSegment getBaseline(TextRenderInfo textRenderInfo) {
		
		float start_x = textRenderInfo.getBaseline().getStartPoint().get(0);
		float start_y = textRenderInfo.getBaseline().getStartPoint().get(1);
		float end_x = textRenderInfo.getBaseline().getEndPoint().get(0);
		float end_y = textRenderInfo.getBaseline().getEndPoint().get(1);

		float llx = start_x;
		float lly = start_y;
		float urx = end_x;
		float ury = end_y;

		return new LineSegment(new Vector(llx, lly + BOX_UPRISE, .0f), new Vector(urx, ury, .0f));
	}

	private Rectangle getBoundingRect(TextRenderInfo textRenderInfo) {

		float start_x = textRenderInfo.getDescentLine().getStartPoint().get(0);
		float start_y = textRenderInfo.getDescentLine().getStartPoint().get(1);
		float end_x = textRenderInfo.getAscentLine().getEndPoint().get(0);
		float end_y = textRenderInfo.getAscentLine().getEndPoint().get(1);

		float llx = start_x;
		float lly = start_y;
		float urx = end_x;
		float ury = end_y;

/*
		if (rotation == 270) { 
			llx = pageBox.getHeight() - start_y;
			lly = start_x;
			urx = pageBox.getHeight() - end_y;
			ury = end_x;
		} else if (rotation == 90 || rotation == 180) {
			System.err.printf("not yet implemented about rotated documents!!");
		}
*/
		return new Rectangle(llx, lly, urx-llx, ury-lly);

	}

	private Rectangle getZeroRect(TextRenderInfo textRenderInfo) {

		float start_x = textRenderInfo.getDescentLine().getStartPoint().get(0);
		float start_y = textRenderInfo.getDescentLine().getStartPoint().get(1);

		return new Rectangle(start_x, start_y, 0f, 0f);
	}
	
	private void printTextInfo(TextRenderInfo rInfo, String mark) {

		Rectangle r = getBoundingRect(rInfo);

		System.out.printf("{%s%s}:[%.1f %.1f %.1f %.1f] \n", mark, rInfo.getText(), r.getX(), r.getY(), r.getWidth(), r.getHeight());
		// printBaseLine(rInfo);
	}
}
