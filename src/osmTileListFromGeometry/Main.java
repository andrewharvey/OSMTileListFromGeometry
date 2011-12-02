/*****************************************************************************
 *
 * Copyright (C) 2011 Andrew Harvey <andrew.harvey4@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *****************************************************************************/

package osmTileListFromGeometry;

import java.io.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;

import java.sql.*;
import java.util.ArrayList;

import org.apache.commons.cli.*;

/**
 * @author Andrew Harvey
 * 
 * Generates a list of OSM tiles which fall within OSM polygons.
 * 
 * We retieve the OSM polygons from an osm2pgsql database.
 *
 */
/**
 * @author lorenz
 *
 */
public class Main {
	
	// the polygons to use as our source polygons for which we find all the OSM tiles within
	static ArrayList<Geometry> imageryBoundaries;
	
	// JTS geometry factory
	static GeometryFactory gf;
	
	// count of how many tiles we have found so far
	static int tileCount;
	
	// writter for the file storing the tile list
	static BufferedWriter tileListWriter;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		try {
			/* parse the command line arguments */
			// create the command line parser
			CommandLineParser parser = new PosixParser();
	
			// create the Options
			Options options = new Options();
			options.addOption("o", "output", true, "File to write list of tiles to.");
			options.addOption("h", "host", true, "osm2pgsql db host");
			options.addOption("p", "port", true, "osm2pgsql db port");
			options.addOption("d", "db", true, "osm2pgsql db name");
			options.addOption("u", "user", true, "osm2pgsql db user");
			options.addOption("w", "password", true, "osm2pgsql db password");
			
			// parse the command line arguments
			CommandLine line = parser.parse( options, args );
	
			String outputFileName;
			if (!line.hasOption("output"))
				outputFileName = "tilesToRender.txt";
			else
				outputFileName = line.getOptionValue("output");
			
			String dbHost;
			if (!line.hasOption("host"))
				dbHost = "localhost";
			else
				dbHost = line.getOptionValue("host");
			
			String dbPort;
			if (!line.hasOption("port"))
				dbPort = "5432";
			else
				dbPort = line.getOptionValue("port");
			
			String dbName;
			if (!line.hasOption("db"))
				dbName = "osm";
			else
				dbName = line.getOptionValue("db");
			
			String dbUser;
			if (!line.hasOption("user"))
				dbUser = "osm";
			else
				dbUser = line.getOptionValue("user");
			
			String dbPassword;
			if (!line.hasOption("password"))
				dbPassword = "osm";
			else
				dbPassword = line.getOptionValue("password");
	
			FileWriter tileListFileWriter = new FileWriter(outputFileName);
			tileListWriter = new BufferedWriter(tileListFileWriter);		  
			
			gf = new GeometryFactory();
			
			// define an arbitary area to generate tiles within (this one is NSW, Australia)
			ArrayList<Geometry> nsw = new ArrayList<Geometry>();
			nsw.add(gf.toGeometry(new Envelope(15676317.2673864,17701592.7270857, -4444354.61727114,-3338769.41530374)));
			
			// everything worldwide -- bluemarble
			imageryBoundaries = nsw;
			renderAllTiles(0,0,0, 0, 8);
			
			// landsat
			imageryBoundaries = nsw;
			renderAllTiles(0,0,0, 9, 12);
	
			// nearmap
			// connect to a local postgresql
			Class.forName("org.postgresql.Driver");
			java.sql.Connection conn = DriverManager.getConnection("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName, dbUser, dbPassword);
			imageryBoundaries = grabBoundaryPolygonsFromPostgres(conn, "nearmap");
			renderAllTiles(0,0,0, 13, 17); // start at 0,0,0 to get up to z13 quickly, but just start printing from z13 up
			
			// finish up
			tileListWriter.close();
	
			// print tile summary
			System.out.println("");
			System.out.println("###");
			
			int totalTiles = totalTiles(0, 15);
			System.out.println(tileCount + " of " + totalTiles + " tiles (" + String.format("%.4f", (float)(tileCount * 100) / totalTiles) + "%)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Finds the number of tiles worldwide within a zoom range
	 * 
	 * TODO: accept bounds in projected coordinates to limit tile count 
	 * 
	 * @param zoomFrom
	 * @param zoomTo
	 * @return Total number of tiles for all zooms within range.
	 */
	public static int totalTiles(int zoomFrom, int zoomTo) {
		if (zoomFrom > zoomTo)
			return 0;
		
		int totalTilesAtThisZoom = (int) Math.pow(2, zoomFrom) * (int) Math.pow(2, zoomFrom);
		return totalTilesAtThisZoom + totalTiles(zoomFrom + 1, zoomTo); 
	}
	
	/**
	 * For a given tile find if we need to render this tile. If yes, recursivly check all below
	 * @param zoom
	 * @param z_start only start printing tiles when we reach this level.
	 * @throws IOException 
	 */
	public static void renderAllTiles(int z, int x, int y, int z_start, int z_stop) throws IOException {
		// find the projected bounds of this tile in osm projection
		double[] tile = TileToMercBounds.tileToMercBounds(z, x, y);
		
		// convert the bounds to a jts geometry
		Envelope envolope = new Envelope(tile[0], tile[2], tile[1], tile[3]);
		Geometry bbox = gf.toGeometry(envolope);
		
		// see if our tile intersects with any of the polygons for the regions to render
		boolean intersects = false;
		
		if (imageryBoundaries == null) {
			intersects = true;
		}else{
			for (Geometry i : imageryBoundaries) {
				intersects = intersects || i.intersects(bbox);
				if (intersects)
					break; // no need to check the rest of the candidates if we already have a match
			}
		}
		
		if (intersects) {
			if (z >= z_start) {
				tileListWriter.write(z + "/" + x + "/" + y + "\n");
				tileCount++;
			}
			
			// move on to a higher zoom level
			if (z < z_stop) {
				renderAllTiles(z + 1, x*2, y*2, z_start, z_stop);
				renderAllTiles(z + 1, x*2 + 1, y*2, z_start, z_stop);
				renderAllTiles(z + 1, x*2, y*2 + 1, z_start, z_stop);
				renderAllTiles(z + 1, x*2 + 1, y*2 + 1, z_start, z_stop);
			}
		}
		
		return;
	}
	
	/**
	 * Retrieve OSM polygons from an osm2pgsql database, and add them to a JTS geometry list.
	 * @param area
	 * @return
	 */
	public static ArrayList<Geometry> grabBoundaryPolygonsFromPostgres(java.sql.Connection conn, String area) {
		
		// FIXME: use a WKB reader instead, should be faster
		WKTReader wktReader = new WKTReader();
		ArrayList<Geometry> geom = new ArrayList<Geometry>();

		try {
			Statement s = conn.createStatement();
			ResultSet r;
			
			if (area.equals("nearmap"))
				r = s.executeQuery("select ST_AsText(way) from planet_osm_line where boundary in ('nearmap');");
			else if (area.equals("australia"))
				r = s.executeQuery("select ST_AsText(way) from planet_osm_line where (admin_level in ('2') and name in ('Australia'));");
			else
				return null;
			
			while (r.next()) {
				geom.add(wktReader.read(r.getString(1)));
			}
			s.close();
			conn.close();
			System.out.println("Found " + geom.size() + " imagery boundaries.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return geom;
	}

}
