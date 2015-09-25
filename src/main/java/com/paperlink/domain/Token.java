package com.paperlink.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Token {
	private String stem;
    private int start;
    private int end;
}
