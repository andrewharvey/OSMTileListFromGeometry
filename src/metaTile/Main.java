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

package metaTile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.cli.*;

/**
 * @author Andrew Harvey
 *
 * Take a list of OSM tiles, and group them together into meta-tiles.
 * 
 * FIXME: avoid overshoots by having varing size of the meta-tiles.
 */
public class Main {

	
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
			options.addOption("i", "input", true, "File to read original tile list from.");
			options.addOption("o", "output", true, "File to write shorter meta-tile list to.");
			options.addOption("m", "metatiles", true, "Number of tiles in x and y direction to group into one meta-tile.");
			
			// parse the command line arguments
			CommandLine commandLine = parser.parse( options, args );
	
			if (!commandLine.hasOption("input") || !commandLine.hasOption("output") || !commandLine.hasOption("metatiles"))
				printUsage(options);
			
			String inputFileName = commandLine.getOptionValue("input");
			String outputFileName = commandLine.getOptionValue("output");
			int metaTileSize = Integer.parseInt(commandLine.getOptionValue("metatiles"));
			
			
			ArrayList<RenderingTile> tiles = new ArrayList<RenderingTile>();
			
			BufferedReader tileListReader = new BufferedReader(new FileReader(new File(inputFileName)));
			
			BufferedWriter renderMetatileListWriter = new BufferedWriter(new FileWriter(new File(outputFileName)));
	
			
			String line = tileListReader.readLine();
			while (line != null) {
				String[] columns = line.split("/");
				
				if (columns.length == 3)
					tiles.add(new RenderingTile(Integer.parseInt(columns[0]), Integer.parseInt(columns[1]), Integer.parseInt(columns[2])));
				
				line = tileListReader.readLine();
			}
			
			tileListReader.close();
			
			int hits = 0;
			
			// tiles which we are already rendering as the top left corner of 4x4 metatiles
			HashSet<RenderingTile> whitelist = new HashSet<RenderingTile>();
			
			// for each tile in the list see if it has a meta-tile in the whitelist already
			for (int i = 0; i < tiles.size(); i++) {
				boolean hit = false; // by default we aren't already rendering this tile as part of another metatile
				for (int dx = 0; dx < metaTileSize; dx++) {
					for (int dy = 0; dy < metaTileSize; dy++) {
						RenderingTile candidate = new RenderingTile(tiles.get(i).z, tiles.get(i).x - dx, tiles.get(i).y - dy);
						if (whitelist.contains(candidate)) {
							hit = true;
							// now exit the two for loops iterating over tiles inside a meta-tile
							dx = metaTileSize;
							dy = metaTileSize;
						}
					}
				}
				
				// if this tile doesn't already have a meta-tile in the whitelist, add it
				if (hit == false) {
					hits++;
					renderMetatileListWriter.write(tiles.get(i).toString() + "/" + metaTileSize + "\n");
					whitelist.add(tiles.get(i));
				}
			}
			renderMetatileListWriter.close();
			System.out.println("Reduced " + tiles.size() + " tiles into " + hits + " metatiles of size " + metaTileSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void printUsage(Options options) {
		System.out.println("java -jar metaTile.jar [options]");
		System.out.println();
		System.out.println("    Options:");
		System.out.println("    -i, --input      " + options.getOption("input").getDescription());
		System.out.println("    -o, --output     " + options.getOption("output").getDescription());
		System.out.println("    -m, --metatiles  " + options.getOption("metatiles").getDescription());
		System.exit(1);
	}

}
