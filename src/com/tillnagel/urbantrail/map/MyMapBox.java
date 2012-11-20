package com.tillnagel.urbantrail.map;

import de.fhpotsdam.unfolding.core.Coordinate;
import de.fhpotsdam.unfolding.providers.MapBox.MapBoxProvider;

public class MyMapBox extends MapBoxProvider {

	// http://c.tiles.mapbox.com/v3/examples.map-vyofok3q/

	public static class StreetMapProvider extends MapBoxProvider {
		public String[] getTileUrls(Coordinate coordinate) {
			String url = "http://c.tile.mapbox.com/v3/examples.map-vyofok3q/" + getPositiveZoomString(coordinate) + ".png";
			return new String[] { url };
		}
	}
	
	public static class WorldDarkMapProvider extends MapBoxProvider {
		public String[] getTileUrls(Coordinate coordinate) {
			String url = "http://d.tile.mapbox.com/v3/mapbox.world-dark/" + getPositiveZoomString(coordinate) + ".png";
			return new String[] { url };
		}
	}
	

}
