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
		ArrayList<RenderingTile> tiles = new ArrayList<RenderingTile>();
		
		BufferedReader tileListReader = new BufferedReader(new FileReader(new File("tilesToRender_sorted.txt")));
		
		BufferedWriter renderMetatileListWriter = new BufferedWriter(new FileWriter(new File("tilesToRender_sorted_metatiles.txt")));

		
		String line = tileListReader.readLine();
		while (line != null) {
			String[] columns = line.split("/");
			
			if (columns.length == 3)
				tiles.add(new RenderingTile(Integer.parseInt(columns[0]), Integer.parseInt(columns[1]), Integer.parseInt(columns[2])));
			
			line = tileListReader.readLine();
		}
		
		tileListReader.close();
		
		int metaTileSize = 4;
		
		int hits = 0;
		
		// tiles which we are already rendering as the top left corner of 4x4 metatiles
		ArrayList<RenderingTile> whitelist = new ArrayList<RenderingTile>();
		
		// for each tile
		for (int i = 0; i < tiles.size(); i++) {
			boolean hit = false; // by default we aren't already rendering this tile as part of another metatile
			for (RenderingTile j : whitelist) {
				int dx = tiles.get(i).x - j.x;
				int dy = tiles.get(i).y - j.y;
				
				if ( (j.z == tiles.get(i).z) &&
				     ((dx >= 0) && (dx <= (metaTileSize - 1))) && 
					 ((dy >= 0) && (dy <= (metaTileSize - 1)))  ) {
					hit = true;
					break;
				}
			}
			
			if (hit == false) {
				hits++;
				renderMetatileListWriter.write(tiles.get(i).toString() + "/" + metaTileSize + "\n");
				whitelist.add(tiles.get(i));
			}
		}
		renderMetatileListWriter.close();
		System.out.println("Reduced " + tiles.size() + " tiles into " + hits + " metatiles of size " + metaTileSize);
	}

}
