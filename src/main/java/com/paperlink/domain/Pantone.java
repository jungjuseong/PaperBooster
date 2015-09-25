package com.paperlink.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pantone {
    private String pantoneId;
	private int cyan;
	private int magenta;
	private int yellow;
	private int black;
	private int rgbColor;
}