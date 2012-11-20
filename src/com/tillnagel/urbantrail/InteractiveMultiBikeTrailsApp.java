package com.tillnagel.urbantrail;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import processing.core.PApplet;
import codeanticode.glgraphics.GLConstants;

import com.tillnagel.urbantrail.map.GlowLinesMarker;

import de.fhpotsdam.rangeslider.TimeRangeSlider;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GPXReader;
import de.fhpotsdam.unfolding.data.MarkerFactory;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.MapUtils;

/**
 * Loads and displays multiple bike trails.
 */
public class InteractiveMultiBikeTrailsApp extends PApplet {

	// Start location
	Location berlinLocation = new Location(52.5f, 13.4f);

	// Interactive background map
	UnfoldingMap map;

	// To create markers
	MarkerFactory markerFactory;

	// To interactively select time ranges to filter markers
	TimeRangeSlider timeRangeSlider;

	public void setup() {
		size(1200, 780, GLConstants.GLGRAPHICS);

		// map = new UnfoldingMap(this, 0, 0, 1200, 700, new MyMapBox.WorldDarkMapProvider());
		map = new UnfoldingMap(this, 0, 0, 1200, 700, new MBTilesMapProvider("jdbc:sqlite:./berlin-dark.mbtiles"));
		// map = new UnfoldingMap(this, 0, 0, 1200, 700);
		// map = new UnfoldingMap(this, 0, 0, 1200, 700, new MapBox.WorldLightProvider());
		map.zoomAndPanTo(berlinLocation, 13);
		map.setZoomRange(10, 17);
		MapUtils.createMouseEventDispatcher(this, map);
		//MapUtils.createDefaultEventDispatcher(this, map);

		// Create line markers with outer glow
		markerFactory = new MarkerFactory();
		markerFactory.setLineClass(GlowLinesMarker.class);

		// Load bike trails from GPX files
		//String dir = sketchPath("runkeeper-2012-Aug-Sep");
		String dir = sketchPath("test");
		String[] gpsFileNames = FileUtils.listFile(dir, "gpx");
		for (String gpsFileName : gpsFileNames) {
			loadAndCreateMarkers(gpsFileName);
		}
		centerAroundAllMarkers(map.getMarkers());

		// UI
		timeRangeSlider = new StyledDateRangeSlider(this, width / 2 - 300 / 2, 740, 300, 16, new DateTime(2012, 8, 1,
				0, 0, 0), new DateTime(2012, 9, 20, 0, 0, 0), 60 * 60 * 24);
		timeRangeSlider.setCurrentRange(new DateTime(2012, 8, 10, 0, 0, 0), new DateTime(2012, 8, 20, 0, 0, 0));
	}

	public void draw() {
		map.draw();

		noStroke();
		fill(58, 63, 66);
		rect(0, 700, width, 100);
		timeRangeSlider.draw();
	}

	// Gets called each time the time ranger slider has changed, both by user interaction as well as by animation
	public void timeUpdated(DateTime startDateTime, DateTime endDateTime) {
		filterMarkersByTime(startDateTime, endDateTime);
	}

	// Interaction --------------------------------------------------

	public void keyPressed() {
		if (key == 'c') {
			centerAroundAllMarkers(map.getMarkers());
		}

		if (key == 'f') {
			println("fps: " + frameRate);
		}

		// Enable key interaction
		timeRangeSlider.onKeyPressed(key, keyCode);
	}

	public void mouseMoved() {
		timeRangeSlider.onMoved(mouseX, mouseY);
	}

	public void mouseDragged() {
		timeRangeSlider.onDragged(mouseX, mouseY, pmouseX, pmouseY);
	}

	// Data ---------------------------------------------------------

	private void filterMarkersByTime(DateTime startDateTime, DateTime endDateTime) {
		MarkerManager<Marker> mm = map.getDefaultMarkerManager();
		for (Marker m : mm.getMarkers()) {
			HashMap<String, Object> properties = m.getProperties();
			String timeString = (String) properties.get("time");
			DateTime markerTime = new DateTime(timeString);

			if (markerTime.isAfter(startDateTime) && markerTime.isBefore(endDateTime)) {
				m.setSelected(true);
			} else {
				m.setSelected(false);
			}
		}
	}

	public void centerAroundAllMarkers(List<Marker> markers) {
		List<Location> locations = GeoUtils.getLocationsFromMarkers(markers);
		Location center = GeoUtils.getEuclideanCentroid(locations);
		map.panTo(center);
	}

	public void loadAndCreateMarkers(String gpxFileName) {
		println("Loading " + gpxFileName);
		List<Feature> features = GPXReader.loadData(this, gpxFileName);

		// Only add markers in and around Berlin
		Location centroid = GeoUtils.getEuclideanCentroid(GeoUtils.getLocationsFromFeatures(features));
		if (GeoUtils.getDistance(centroid, berlinLocation) > 100) {
			println("\tToo far away! Ommitting.");
			return;
		}

		// Create markers and set style
		List<Marker> markers = markerFactory.createMarkers(features);
		for (Marker marker : markers) {
			GlowLinesMarker glowlineMarker = (GlowLinesMarker) marker;
			glowlineMarker.setColor(color(255, 0, 0, 10));
			glowlineMarker.setHighlightColor(color(255, 0, 0, 120));
			glowlineMarker.setStrokeWeight(2);
			glowlineMarker.setAlpha(20);
			glowlineMarker.setGlowStrokeWeight(6);
		}
		map.addMarkers(markers);
	}

}
