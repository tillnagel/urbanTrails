package com.tillnagel.urbantrail.bike;

import org.joda.time.DateTime;

import processing.core.PApplet;
import processing.core.PFont;
import de.fhpotsdam.rangeslider.StyledTimeRangeSlider;
import de.fhpotsdam.utils.FontManager;

public class StyledDateRangeSlider extends StyledTimeRangeSlider {

	public StyledDateRangeSlider(PApplet p, float x, float y, float width, float height, DateTime startDateTime,
			DateTime endDateTime, int aggregationIntervalSeconds) {
		super(p, x, y, width, height, startDateTime, endDateTime, aggregationIntervalSeconds);
	}

	protected void drawStartEndTimeLabels() {
		PFont font = FontManager.getInstance().getMiniLabelFont();
		p.fill(230, 200);
		p.textFont(font);

		labelPadding = 12;
		String startTimeLabel = startDateTime.toString("dd.MM.");
		int startLabelX = (int) (x - p.textWidth(startTimeLabel) - labelPadding);
		int labelY = (int) (y + font.getSize() / 2 - 2);
		p.text(startTimeLabel, startLabelX, labelY);

		String endTimeLabel = endDateTime.toString("dd.MM.");
		int endLabelX = (int) (x + width + labelPadding);
		p.text(endTimeLabel, endLabelX, labelY);
	}
	
	protected void drawTimeRangeLabels() {
		String timeRangeLabel = currentStartDateTime.toString("dd.MM.") + " - " + currentEndDateTime.toString("dd.MM.");

		labelPadding = 20;
		PFont font = FontManager.getInstance().getMiniLabelFont();
		p.textFont(font);
		int labelX = (int) (currentStartX + (currentEndX - currentStartX) / 2 - p.textWidth(timeRangeLabel) / 2);
		int labelY = (int) (y + font.getSize() + labelPadding / 2);
		p.fill(230, 200);
		p.text(timeRangeLabel, labelX, labelY);
	}
	
}
