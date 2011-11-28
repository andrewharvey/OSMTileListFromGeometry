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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TileToMercBoundsTest {
	
	private double MAX_X = 20037508;
	private double MAX_Y = 19929239;
	
	@Before
	public void setUp() throws Exception {
	
	}
	
	@Test
	public void test_z0() {
		double[] bounds = TileToMercBounds.tileToMercBounds(0,0,0);
		assertTrue(bounds[0] == -MAX_X);
		assertTrue(bounds[1] == -MAX_Y);
		assertTrue(bounds[2] == MAX_X);
		assertTrue(bounds[3] == MAX_Y);
	}
	
	@Test
	public void test_z1_00() {
		double[] bounds = TileToMercBounds.tileToMercBounds(1,0,0);
		assertTrue(bounds[0] == -MAX_X);
		assertTrue(bounds[1] == 0);
		assertTrue(bounds[2] == 0);
		assertTrue(bounds[3] == MAX_Y);
	}
	
	@Test
	public void test_z1_10() {
		double[] bounds = TileToMercBounds.tileToMercBounds(1,1,0);
		assertTrue(bounds[0] == 0);
		assertTrue(bounds[1] == 0);
		assertTrue(bounds[2] == MAX_X);
		assertTrue(bounds[3] == MAX_Y);
	}

	@Test
	public void test_z2_22() {
		double[] bounds = TileToMercBounds.tileToMercBounds(2,2,2);
		assertTrue(bounds[0] == 0);
		assertTrue(bounds[1] == -MAX_Y/2); //-9964619.5
		assertTrue(bounds[2] == MAX_X/2); //10018754
		assertTrue(bounds[3] == 0);
	}
	
	@Test
	public void test_z3_22() {
		double[] bounds = TileToMercBounds.tileToMercBounds(3,2,2);
		assertTrue(bounds[0] == -MAX_X/2);
		assertTrue(bounds[1] == MAX_Y/4);
		assertTrue(bounds[2] == -MAX_X/4);
		assertTrue(bounds[3] == MAX_Y/2);
	}
	
	@Test
	public void test_z3_67() {
		double[] bounds = TileToMercBounds.tileToMercBounds(3,6,7);
		assertTrue(bounds[0] == MAX_X/2);
		assertTrue(bounds[1] == -MAX_Y);
		assertTrue(bounds[2] == (MAX_X/4)*3);
		assertTrue(bounds[3] == -(MAX_Y/4)*3);
	}
	
	// just checking the quadrant here
	@Test
	public void test_z9_q4() {
		double[] bounds = TileToMercBounds.tileToMercBounds(9,470,307);
		assertTrue(bounds[0] > 0);
		assertTrue(bounds[1] < 0);
		assertTrue(bounds[2] > 0);
		assertTrue(bounds[3] < 0);
	}
	
	// just checking the quadrant here
	@Test
	public void test_z25_bottom_right() {
		double[] bounds = TileToMercBounds.tileToMercBounds(25,(int) Math.pow(2, 25) - 1, (int) Math.pow(2, 25) - 1);
		assertTrue(bounds[0] > 0);
		assertTrue(bounds[1] == -MAX_Y);
		assertTrue(bounds[2] == MAX_X);
		assertTrue(bounds[3] < 0);
	}
}
