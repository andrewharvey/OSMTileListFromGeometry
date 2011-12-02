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

public class TileToMercBounds {
	
	private static int MAX_X = 20037508;
	private static int MAX_Y = 20037508;
	
	/**
	 * @param z
	 * @param x
	 * @param y
	 * @return "left bottom right top" bounds in google merc projection
	 */
	public static double[] tileToMercBounds(int z, int x, int y) {
	    if (z == 0) {
	    	double[] result = {-MAX_X, -MAX_Y, MAX_X, MAX_Y}; 
	        return result;
	    }
	    
	    // x = 0 .. (2^z - 1)
	    // y = 0 .. (2^z - 1)
	    
	    int tilePastCentreAxis = (int)((Math.pow(2,z)) / 2);
	    
	    double bound_left;
	    double bound_right;
	    double bound_top;
	    double bound_bottom;
	    
	    boolean mirrored_x = false;
	    boolean mirrored_y = false;
	    
	    // flip all tiles left/above of the centre axis across to the right/bottom side for now
	    // in other words move everything to the bottom right quadrant
	    if (x < tilePastCentreAxis) {
	        mirrored_x = true;
	        x = ((int)Math.pow(2, z) - 1) - x;
	    }
	    
	    if (y < tilePastCentreAxis) {
	        mirrored_y = true;
	        y = ((int)Math.pow(2, z) - 1) - y;
	    }
	    
	    // 2^z / 2 <= x < 2^z
	    assert ((tilePastCentreAxis <= x) && (x < (Math.pow(2, z))));
	    assert ((tilePastCentreAxis <= y) && (y < (Math.pow(2, z))));
	    
	    // need to cast the numerator to double incase both would have been integers, but true answer should be a floating point number
	    // we also need to cast the tile number to double so that when we multiply it by MAX_X, we don't overfloat the int!
	    bound_left = ((double) (((double)x - tilePastCentreAxis) * (MAX_X)) / tilePastCentreAxis);
	    bound_right = ((double) (((double)(x + 1) - tilePastCentreAxis)  * (MAX_X)) / tilePastCentreAxis);
	    
	    bound_top = ((double) (((double)y - tilePastCentreAxis) * (MAX_Y)) / tilePastCentreAxis) * -1;
	    bound_bottom = ((double) (((double)(y + 1) - tilePastCentreAxis) * (MAX_Y)) / tilePastCentreAxis) * -1;
	    
	    // if we mirrored the tiles earlier, fix them up now that we have the merc bounds
	    if (mirrored_x) {
	        double br = bound_right;
	        double bl = bound_left;
	        
	        bound_left = -1 * br;
	        bound_right = -1 * bl;
	    }
	    
	    if (mirrored_y) {
	        double bb = bound_bottom;
	        double bt = bound_top;
	        
	        bound_bottom = -1 * bt;
	        bound_top = -1 * bb;
	    }
	    
	    double[] result = {bound_left, bound_bottom, bound_right, bound_top};
	    return result;
	}
}
