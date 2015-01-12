package com.tillnagel.urbantrail.bike;

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
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.MapUtils;

/**
 * Loads and displays multiple bike trails.
 */
public class InteractiveMultiBikeTrailsApp extends PApplet {

	// Start location
	Location berlinLocation = new Location(52.5f, 13.4f);

	//Location netherlandsLocation = new Location(51.05f, 3.71f);

	// Interactive background map
	UnfoldingMap map;

	// To create markers
	MarkerFactory markerFactory;

	// To interactively select time ranges to filter markers
	TimeRangeSlider timeRangeSlider;

	int width = 1850, height = 1000;
	int mapWidth = width, mapHeight = 1000;

	public void setup() {
		size(width, height, GLConstants.GLGRAPHICS);

		// map = new UnfoldingMap(this, 0, 0, 1200, 700, new MyMapBox.WorldDarkMapProvider());
		// map = new UnfoldingMap(this, 0, 0, mapWidth, mapHeight, new
		// MBTilesMapProvider("jdbc:sqlite:./berlin-dark.mbtiles"));
		map = new UnfoldingMap(this, 0, 0, mapWidth, mapHeight);
		//map = new UnfoldingMap(this, 0, 0, 1200, 700, new MapBox.WorldLightProvider());
		map.zoomAndPanTo(berlinLocation, 13);
		map.setZoomRange(10, 17);
		MapUtils.createMouseEventDispatcher(this, map);
		// MapUtils.createDefaultEventDispatcher(this, map);

		// Create line markers with outer glow
		markerFactory = new MarkerFactory();
		// markerFactory.setLineClass(GlowLinesMarker.class);
		markerFactory.setLineClass(GlowLinesMarker.class);

		// Load bike trails from GPX files
		// String dir = sketchPath("runkeeper-2012-Aug-Sep");
		// String dir = sketchPath("runkeeper-schinkomat");
		String dir = sketchPath("test");
		//String dir = sketchPath("runkeeper-vantomme");
		String[] gpsFileNames = FileUtils.listFile(dir, "gpx");
		for (String gpsFileName : gpsFileNames) {
			loadAndCreateMarkers(gpsFileName);
		}
		centerMap();

		// UI
		timeRangeSlider = new StyledDateRangeSlider(this, width / 2 - 300 / 2, height - 40, 300, 16, new DateTime(2012,
				7, 1,
				0, 0, 0), new DateTime(2013, 6, 30, 0, 0, 0), 60 * 60 * 24);
		timeRangeSlider.setCurrentRange(new DateTime(2012, 6, 1, 0, 0, 0), new DateTime(2013, 5, 31, 0, 0, 0));
		timeRangeSlider.setAnimationDelay(1);
		timeRangeSlider.setAnimationIntervalSeconds(60 * 60 * 24);
	}

	public void draw() {
		map.draw();

		noStroke();
		fill(58, 63, 66);
		rect(0, height - 80, width, 80);
		timeRangeSlider.draw();
	}

	// Gets called each time the time ranger slider has changed, both by user interaction as well as by animation
	public void timeUpdated(DateTime startDateTime, DateTime endDateTime) {
		filterMarkersByTime(startDateTime, endDateTime);
	}

	// Interaction --------------------------------------------------

	public void centerMap() {
		centerAroundAllMarkers(map.getMarkers());
		map.panBy(0, 100);

	}

	public void keyPressed() {
		if (key == 'c') {
			centerAroundAllMarkers(map.getMarkers());
		}

		if (key == 'f') {
			println("fps: " + frameRate);
		}

		if (key == 'M') {
			DateTime startDateTime = timeRangeSlider.getCurrentStartDateTime();
			DateTime newStartDateTime = startDateTime.plusMonths(1);
			DateTime newEndDateTime = newStartDateTime.plusMonths(1).minusDays(1);
			timeRangeSlider.setCurrentRange(newStartDateTime, newEndDateTime);
		}
		if (key == 'm') {
			DateTime startDateTime = timeRangeSlider.getCurrentStartDateTime();
			DateTime newStartDateTime = startDateTime.minusMonths(1);
			DateTime newEndDateTime = newStartDateTime.plusMonths(1).minusDays(1);
			timeRangeSlider.setCurrentRange(newStartDateTime, newEndDateTime);
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
				m.setHidden(false);
			} else {
				m.setSelected(false);
				m.setHidden(true);
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

		// Only add markers for routes in and around Berlin
		Location centroid = GeoUtils.getEuclideanCentroid(GeoUtils.getLocationsFromFeatures(features));
		if (GeoUtils.getDistance(centroid, berlinLocation) > 100) {
			println("\tToo far away! Ommitting.");
			return;
		}

		// Only add markers for cycling routes
		String trackName = features.get(0).getStringProperty("name");
		if (trackName != null && !trackName.contains("Cycling")) {
			println("\tOmmiting non-cycling track.");
			return;
		}

		// Create markers and set style
		List<Marker> markers = markerFactory.createMarkers(features);
		for (Marker marker : markers) {
			GlowLinesMarker glowlineMarker = (GlowLinesMarker) marker;
			glowlineMarker.setColor(color(255, 0, 0, 0));
			glowlineMarker.setHighlightColor(color(255, 0, 0, 200));
			glowlineMarker.setStrokeWeight(1);
			glowlineMarker.setAlpha(2);
			glowlineMarker.setGlowStrokeWeight(6);
		}
		map.addMarkers(markers);
	}

}
