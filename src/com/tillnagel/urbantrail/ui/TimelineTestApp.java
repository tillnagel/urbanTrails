package com.tillnagel.urbantrail.ui;

import processing.core.PApplet;

public class TimelineTestApp extends PApplet {
	
	int shadowColor = color(0, 200);
	int whiteColor = color(255);
	int highlightColor = color(231, 68, 48);
	
	float y = 500;
	float xStart = 100;
	float xEnd = 300;
	
	public void setup() {
		size(800, 600);
		smooth();
		
	}

	public void draw() {
		background(43);
		
		fill(shadowColor);
		noStroke();
		ellipse(mouseX, y, 20, 20);
		
		stroke(shadowColor);
		strokeWeight(6);
		line(xStart, y, xEnd, y);
		
		stroke(whiteColor);
		strokeWeight(3);
		line(xStart, y, xEnd, y);
		
		noStroke();
		fill(whiteColor);
		ellipse(mouseX, y, 16, 16);
		
		fill(highlightColor);
		ellipse(mouseX, y, 12, 12);
		
	}

}
