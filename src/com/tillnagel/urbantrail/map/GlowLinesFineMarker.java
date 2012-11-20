package com.tillnagel.urbantrail.map;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import processing.core.PConstants;
import processing.core.PGraphics;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.utils.MapPosition;

public class GlowLinesFineMarker extends SimpleLinesMarker {

	private int alpha = 50;
	private int glowStrokeWeight = 6;
	private DateTime startDateTime;
	private DateTime endDateTime;

	public GlowLinesFineMarker(List<Location> locations, HashMap<String, Object> properties) {
		super(locations, properties);
		this.selected = true;
	}

	@Override
	public void draw(PGraphics pg, List<MapPosition> mapPositions) {
		if (mapPositions.isEmpty())
			return;

		// Draw glow around lines in lightened color
		pg.pushStyle();
		pg.noFill();
		if (isSelected()) {
			pg.stroke(highlightColor, alpha);
		} else {
			pg.stroke(color, alpha);
		}
		pg.strokeWeight(glowStrokeWeight);
		pg.smooth();

		// Get times for each pos
		List<String> trackPointTimes = (List<String>) properties.get("trackPointTimes");

		pg.beginShape(PConstants.LINES);
		MapPosition last = null;
		for (int i = 0; i < mapPositions.size(); ++i) {

			String timeString = trackPointTimes.get(i);
			DateTime trackPointTime = new DateTime(timeString);
			// PApplet.println("t:" + trackPointTime + ", s:" + startDateTime + ", e:" + endDateTime);
			if (trackPointTime.isAfter(startDateTime) && trackPointTime.isBefore(endDateTime)) {
				MapPosition mp = mapPositions.get(i);
				if (last != null) {
					// PApplet.println("\tAdded");
					pg.vertex(last.x, last.y);
					pg.vertex(mp.x, mp.y);
				}
				last = mp;
			}
		}
		pg.endShape();
		pg.popStyle();

		// Draw normal lines
		// super.draw(pg, mapPositions);
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public void setGlowStrokeWeight(int glowStrokeWeight) {
		this.glowStrokeWeight = glowStrokeWeight;
	}

	public void setSelectedTimeRange(DateTime startDateTime, DateTime endDateTime) {
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}

}
