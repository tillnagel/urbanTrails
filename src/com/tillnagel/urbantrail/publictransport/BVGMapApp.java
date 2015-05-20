package com.tillnagel.urbantrail.publictransport;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PGraphics;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.utils.MapPosition;
import de.fhpotsdam.unfolding.utils.MapUtils;

public class BVGMapApp extends PApplet {

	UnfoldingMap map;
	PGraphics pdfG;

	public void setup() {
		size(1024, 768, OPENGL);
		smooth();

		map = new UnfoldingMap(this);
		map.zoomAndPanTo(new Location(52.5f, 13.4f), 11);
		MapUtils.createDefaultEventDispatcher(this, map);

		List<Feature> transitLines = GeoJSONReader.loadData(this, "berlin-subway.geojson");
		List<Marker> transitMarkers = MapUtils.createSimpleMarkers(transitLines);
		map.addMarkers(transitMarkers);

		noLoop();
		pdfG = beginRecord(PDF, "ubahn-lines.pdf");
	}

	public void draw() {
		// map.draw();
		background(255);

		for (Marker m : map.getMarkers()) {
			SimpleLinesMarker lineMarker = (SimpleLinesMarker) m;

			// Convert locations to map positions in order to use marker's draw.
			List<MapPosition> mapPositions = new ArrayList<MapPosition>();
			for (Location loc : lineMarker.getLocations()) {
				float[] xy = map.mapDisplay.getObjectFromLocation(loc);
				mapPositions.add(new MapPosition(xy));
			}
			// Use original drawing style, but draw on PApplet's own canvas (instead of map)
			lineMarker.draw(pdfG, mapPositions);
		}
		endRecord();
		println("Done");
	}
}
