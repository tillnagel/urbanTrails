package com.tillnagel.urbantrail.map;

import java.util.HashMap;
import java.util.List;

import processing.core.PConstants;
import processing.core.PGraphics;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.utils.MapPosition;

public class GlowLinesMarker extends SimpleLinesMarker {

	private int alpha = 50;
	private int glowStrokeWeight = 6;

	public GlowLinesMarker(List<Location> locations, HashMap<String, Object> properties) {
		super(locations, properties);
	}

	@Override
	public void draw(PGraphics pg, List<MapPosition> mapPositions) {
		if (mapPositions.isEmpty() || isHidden())
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

		pg.beginShape(PConstants.LINES);
		MapPosition last = mapPositions.get(0);
		for (int i = 1; i < mapPositions.size(); ++i) {
			MapPosition mp = mapPositions.get(i);
			pg.vertex(last.x, last.y);
			pg.vertex(mp.x, mp.y);

			last = mp;
		}
		pg.endShape();
		pg.popStyle();

		// Draw normal lines
		super.draw(pg, mapPositions);
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public void setGlowStrokeWeight(int glowStrokeWeight) {
		this.glowStrokeWeight = glowStrokeWeight;
	}

}
