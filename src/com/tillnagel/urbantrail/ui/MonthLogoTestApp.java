package com.tillnagel.urbantrail.ui;

import de.looksgood.ani.Ani;
import processing.core.PApplet;
import processing.core.PImage;

public class MonthLogoTestApp extends PApplet {

	int shadowColor = color(0, 200);
	int whiteColor = color(255);
	int highlightColor = color(231, 68, 48);

	PImage yearOfBikingImg;
	PImage augImg;
	PImage sepImg;
	
	int yOffset = 0;
	int yTarget = 50;

	public void setup() {
		size(920, 600);
		smooth();

		yearOfBikingImg = loadImage("ui/label-yearofbiking.png");
		augImg = loadImage("ui/aug12.png");
		sepImg = loadImage("ui/sep12.png");
		
		Ani.init(this);
	}

	public void draw() {
		background(43);

		int x = 100;
		int y = 100;
		int yStep = 48;
		
		Ani.to(this, 1.0f, "yOffset", yTarget);
		
		image(augImg, x + 40, y + 64 + yOffset);
		image(sepImg, x + 40, y + 64 + yOffset + yStep);

		noStroke();
		fill(43, 200);
		rect(x + 40, y, 300, 70);
		rect(x + 40, y + 119, 300, 70);
		
		image(yearOfBikingImg, x, y);
	}
	
	public void keyPressed() {
		if (key == '1') {
			yTarget = 55;
		} else if (key == '2') {
			yTarget = 0;
		} else if (key == '3') {
			yTarget = -55;
		}
	}
}
