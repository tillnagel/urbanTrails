package com.tillnagel.urbantrail;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import codeanticode.glgraphics.GLConstants;
import controlP5.ControlEvent;
import controlP5.ControlP5;
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

	float simplificationTolerance = 1;
	int averageNumber = 5;
	boolean showOriginal = true;
	boolean showSimplified = true;
	boolean showAveraged = true;

	ControlP5 cp5;

	public void setup() {
		size(800, 600, GLConstants.GLGRAPHICS);

		map = new UnfoldingMap(this, 0, 60, 800, 540, new MBTilesMapProvider("jdbc:sqlite:./berlin-dark.mbtiles"));
		// map = new UnfoldingMap(this, 0, 60, 800, 540, new StamenMapProvider.TonerLite());
		map.zoomAndPanTo(new Location(52.5f, 13.4f), 15);
		map.setZoomRange(10, 17);
		MapUtils.createDefaultEventDispatcher(this, map);

		// Create marker
		List<Feature> features = GPXReader.loadData(this, "RK_gpx _2012-09-13_1030.gpx");
		MarkerFactory markerFactory = new MarkerFactory();
		List<Marker> markers = markerFactory.createMarkers(features);
		map.addMarkers(markers);

		// Center around bike path (by panning to center of all features)
		locations = GeoUtils.getLocationsFromFeatures(features);
		Location center = GeoUtils.getCentroid(locations);
		map.panTo(center);

		// UI
		cp5 = new ControlP5(this);
		cp5.addTextlabel("original").setText("ORIGINAL").setPosition(140, 12).setColorValue(color(255));
		cp5.addSlider("simplificationTolerance").setPosition(20, 25).setRange(0, 25).setCaptionLabel("Simplification");
		cp5.addSlider("averageNumber").setPosition(20, 40).setRange(1, 10).setCaptionLabel("Average");

		cp5.addToggle("showOriginal").setPosition(190, 10).setSize(10, 10).setLabelVisible(false);
		cp5.addToggle("showSimplified").setPosition(190, 25).setSize(10, 10).setLabelVisible(false);
		cp5.addToggle("showAveraged").setPosition(190, 40).setSize(10, 10).setLabelVisible(false);
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

	}

	public void draw() {
		background(0);
		tint(255, 50);
		map.draw();
		tint(255, 255);

		// Draw points
		List<PVector> points = new ArrayList<PVector>();
		for (Location location : locations) {
			ScreenPosition pos = map.getScreenPosition(location);
			points.add(pos);
		}
		if (!points.isEmpty()) {
			// original below
			// drawLine(points, color(0, 50), color(0, 50), 10);
			// drawLine(points, color(255, 255, 0, 200), color(255, 255, 0, 200), 5);

			// simplified
			if (showSimplified) {
				List<PVector> simplifiedPoints = GeneralizationUtils.simplify(points, simplificationTolerance, true);
				drawLine(simplifiedPoints, color(255, 0, 255, 160), color(255, 0, 255, 160), 5);
			}

			if (showAveraged) {
				// moving average
				List<PVector> averagedPoints = computeMovingAverage(points, averageNumber);
				drawLine(averagedPoints, color(0, 255, 255, 160), color(0, 255, 255, 160), 5);
			}

			if (showOriginal) {
				// original on top
				drawLine(points, color(255, 255, 0, 200), color(255, 255, 0, 200), 2);
			}
		}

		// UI background
		fill(0);
		rect(0, 0, width, 60);

		stroke(255, 255, 0, 200);
		strokeWeight(2);
		line(205, 15, 280, 15);

		stroke(255, 0, 255, 160);
		strokeWeight(5);
		line(205, 30, 280, 30);

		stroke(0, 255, 255, 160);
		strokeWeight(5);
		line(205, 45, 280, 45);

		// UI will be drawn in postDraw() by ControlP5
	}

	public List<PVector> computeMovingAverage(List<PVector> vertices, int np) {
		float[] xValues = new float[vertices.size()];
		float[] yValues = new float[vertices.size()];
		for (int i = 0; i < vertices.size(); i++) {
			xValues[i] = vertices.get(i).x;
			yValues[i] = vertices.get(i).y;
		}

		float[] xAvgValues = computeMovingAverage(xValues, np);
		float[] yAvgValues = computeMovingAverage(yValues, np);

		List<PVector> averageVertices = new ArrayList<PVector>();
		for (int i = 0; i < xAvgValues.length; i++) {
			averageVertices.add(new PVector(xAvgValues[i], yAvgValues[i]));
		}
		return averageVertices;
	}

	/**
	 * @param np
	 *            number of points to average over
	 */
	public float[] computeMovingAverage(float[] values, int np) {
		float[] mm = new float[values.length - np];
		for (int i = 0; i < mm.length; i++) {
			float sum = 0;
			for (int j = 0; j < np; j++) {
				sum = sum + values[i + j];
			}
			mm[i] = sum / np;
		}
		return mm;
	}

	public void keyPressed() {
		if (key == 'T') {
			simplificationTolerance++;
		}
		if (key == 't') {
			simplificationTolerance--;
		}
		println(simplificationTolerance);
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
