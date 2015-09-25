package com.paperlink.domain;

import com.itextpdf.text.BaseColor;

public class PaperColor {
    public static boolean hasBlackColor(BaseColor color, final int THRESHOLD) {
    	return (color.getRGB() < 0x0001ff);
    }
}
