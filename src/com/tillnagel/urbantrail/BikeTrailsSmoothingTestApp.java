package com.tillnagel.urbantrail;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Label;
import controlP5.Textlabel;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GPXReader;
import de.fhpotsdam.unfolding.data.MarkerFactory;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.GeneralizationUtils;
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

/**
 * Smoothes the lines of a bike trail. The original, the simplified and the moving-averaged lines are shown. You can
 * tweak the algorithms by interactive sliders.
 */
public class BikeTrailsSmoothingTestApp extends PApplet {

	UnfoldingMap map;
	List<Location> locations;

	float simplificationTolerance = 4;
	int averageNumber = 5;
	boolean showOriginal = true;
	boolean showSimplified = true;
	boolean showAveraged = true;
	boolean showCombined = false;

	ControlP5 cp5;
	Textlabel numOriginalTL;
	Textlabel numSimplifiedTL;
	Textlabel numAverageTL;
	Textlabel numCombinedTL;

	public void setup() {
		size(800, 600);

		map = new UnfoldingMap(this, 0, 60, 800, 540, new MBTilesMapProvider("jdbc:sqlite:./berlin-dark.mbtiles"));
		// map = new UnfoldingMap(this, 0, 60, 800, 540, new StamenMapProvider.TonerLite());
		map.zoomAndPanTo(15, new Location(52.5f, 13.4f));
		map.setZoomRange(10, 17);
		MapUtils.createDefaultEventDispatcher(this, map);

		// Create marker
		List<Feature> features = GPXReader.loadData(this, "runkeeper-2012-Aug-Sep/RK_gpx _2012-09-13_1030.gpx");
		println("Loaded " + features.size() + " features");
		MarkerFactory markerFactory = new MarkerFactory();
		List<Marker> markers = markerFactory.createMarkers(features);
		map.addMarkers(markers);

		// Center around bike path (by panning to center of all features)
		locations = GeoUtils.getLocationsFromFeatures(features);
		Location center = GeoUtils.getEuclideanCentroid(locations);
		map.panTo(center);

		// UI
		cp5 = new ControlP5(this);
		cp5.addSlider("simplificationTolerance").setPosition(20, 25).setRange(0, 25).setCaptionLabel("Simplification");
		Label label = cp5.addSlider("averageNumber").setPosition(20, 40).setRange(1, 10).setCaptionLabel("Average").getCaptionLabel();
		
		cp5.addTextlabel("original").setText("ORIGINAL").setPosition(144, 12).setFont(label.getFont()).setColorValue(color(255));
		cp5.addTextlabel("combined").setText("SIMPL+AVG").setPosition(131, 55).setFont(label.getFont()).setColorValue(color(255));

		cp5.addToggle("showOriginal").setPosition(190, 10).setSize(10, 10).setLabelVisible(false);
		cp5.addToggle("showSimplified").setPosition(190, 25).setSize(10, 10).setLabelVisible(false);
		cp5.addToggle("showAveraged").setPosition(190, 40).setSize(10, 10).setLabelVisible(false);
		cp5.addToggle("showCombined").setPosition(190, 55).setSize(10, 10).setLabelVisible(false);

		numOriginalTL = cp5.addTextlabel("numOriginal").setPosition(300, 12)
				.setColorValue(color(200));
		numSimplifiedTL = cp5.addTextlabel("numSimplified").setPosition(300, 27).setColorValue(color(200));
		numAverageTL = cp5.addTextlabel("numAverage").setPosition(300, 42).setColorValue(color(200));
		numCombinedTL = cp5.addTextlabel("numCombined").setPosition(300, 57).setColorValue(color(200));
	}

	public void controlEvent(ControlEvent theEvent) {
		if (theEvent.isFrom(cp5.getController("simplificationTolerance"))) {
			simplificationTolerance = theEvent.getController().getValue();
		}
		if (theEvent.isFrom(cp5.getController("averageNumber"))) {
			averageNumber = Math.round(theEvent.getController().getValue());
		}
		if (theEvent.isFrom(cp5.getController("showSimplified"))) {
			showSimplified = (theEvent.getController().getValue() > 0);
		}
		if (theEvent.isFrom(cp5.getController("showAveraged"))) {
			showAveraged = (theEvent.getController().getValue() > 0);
		}
		if (theEvent.isFrom(cp5.getController("showOriginal"))) {
			showOriginal = (theEvent.getController().getValue() > 0);
		}
		if (theEvent.isFrom(cp5.getController("showCombined"))) {
			showCombined = (theEvent.getController().getValue() > 0);
		}

	}

	public void draw() {
		background(0);
		tint(255, 200);
		map.draw();
		tint(255, 255);

		// Update list of points
		List<PVector> points = new ArrayList<PVector>();
		for (Location location : locations) {
			ScreenPosition pos = map.getScreenPosition(location);
			points.add(pos);
		}

		if (!points.isEmpty()) {

			if (showSimplified) {
				// simplified
				List<PVector> simplifiedPoints = GeneralizationUtils.simplify(points, simplificationTolerance, true);
				drawLine(simplifiedPoints, color(255, 0, 255, 160), color(255, 0, 255, 160), 5);
				numSimplifiedTL.setText("(" + simplifiedPoints.size() + ")");
			}

			if (showAveraged) {
				// moving average
				List<PVector> averagedPoints = GeneralizationUtils.computeMovingAverage(points, averageNumber);
				drawLine(averagedPoints, color(0, 255, 255, 160), color(0, 255, 255, 160), 5);
				numAverageTL.setText("(" + averagedPoints.size() + ")");
			}

			if (showCombined) {
				List<PVector> averagedPoints = GeneralizationUtils.computeMovingAverage(points, averageNumber);
				List<PVector> simplifiedAveragedPoints = GeneralizationUtils.simplify(averagedPoints,
						simplificationTolerance, true);
				drawLine(simplifiedAveragedPoints, color(255, 255, 255, 160), color(255, 255, 255, 160), 5);
				numCombinedTL.setText("(" + simplifiedAveragedPoints.size() + ")");
			}

			if (showOriginal) {
				// original on top
				drawLine(points, color(255, 255, 0, 200), color(255, 255, 0, 200), 2);
				numOriginalTL.setText("(" + points.size() + ")");
			}
		}

		// UI background
		fill(0);
		rect(0, 0, width, 70);

		stroke(255, 255, 0, 200);
		strokeWeight(2);
		line(205, 15, 280, 15);

		stroke(255, 0, 255, 160);
		strokeWeight(5);
		line(205, 30, 280, 30);

		stroke(0, 255, 255, 160);
		strokeWeight(5);
		line(205, 45, 280, 45);

		stroke(255, 255, 255, 160);
		strokeWeight(5);
		line(205, 60, 280, 60);

		// UI will be drawn in postDraw() by ControlP5
	}

	public void drawLine(List<PVector> points, int strokeColor, int color) {
		drawLine(points, strokeColor, color, 2);
	}

	public void drawLine(List<PVector> points, int strokeColor, int color, int strokeWeight) {
		stroke(strokeColor);
		strokeWeight(strokeWeight);
		noFill();
		beginShape();
		for (PVector p : points) {
			vertex(p.x, p.y);
		}
		endShape();

		noStroke();
		fill(color);
		for (PVector p : points) {
			// ellipse(p.x, p.y, strokeWeight, strokeWeight);
		}
	}

}
