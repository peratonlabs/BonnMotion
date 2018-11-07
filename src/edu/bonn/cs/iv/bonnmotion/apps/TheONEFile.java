/*******************************************************************************
 ** BonnMotion - a mobility scenario generation and analysis tool             **
 ** Copyright (C) 2002-2010 University of Bonn                                **
 ** Code: Matthias Schwamborn                                                 **
 **                                                                           **
 ** This program is free software; you can redistribute it and/or modify      **
 ** it under the terms of the GNU General Public License as published by      **
 ** the Free Software Foundation; either version 2 of the License, or         **
 ** (at your option) any later version.                                       **
 **                                                                           **
 ** This program is distributed in the hope that it will be useful,           **
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of            **
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             **
 ** GNU General Public License for more details.                              **
 **                                                                           **
 ** You should have received a copy of the GNU General Public License         **
 ** along with this program; if not, write to the Free Software               **
 ** Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA **
 *******************************************************************************/

package edu.bonn.cs.iv.bonnmotion.apps;

import java.io.*;
import edu.bonn.cs.iv.bonnmotion.*;

/** The ONE file format
 * according to:
 * http://www.netlab.tkk.fi/tutkimus/dtn/theone/javadoc/input/ExternalMovementReader.html
 * 
 * @author schwambo
 */
public class TheONEFile extends App {
	protected static final String fileSuffix = ".one";

	protected String name = null;
	protected double intervalLength = 1.0;

	public TheONEFile(String[] args) {
		go(args);
	}

	public void go(String[] args) {
		parse(args);

		Scenario s = null;
		if (name == null) {
			printHelp();
			System.exit(0);
		}

		try {
			s = Scenario.getScenario(name);
		} catch (Exception e) {
			e.printStackTrace();
			App.exceptionHandler("Error reading file", e);
		}

		PrintWriter out = openPrintWriter(name + fileSuffix);
		/** print header line:
		 * minTime maxTime minX maxX minY maxY [minZ maxZ]
		 * */
		out.println(0.0 + " " + s.getDuration() + " " + 0.0 + " " + s.getX() + " " + 0.0 + " " + s.getY());

		MobileNode[] node = s.getNode();
		double duration = s.getDuration();
		double t = 0.0;
		while (t < duration) {
			for (int i = 0; i < node.length; i++) {
				Position p = node[i].positionAt(t);
				out.println(t + " " + i + " " + p.x + " " + p.y);
			}
			t += intervalLength;
		}

		out.close();
	}

	protected boolean parseArg(char key, String val) {
		switch (key) {
			case 'f':
				this.name = val;
				return true;
			case 'l':
				this.intervalLength = Double.parseDouble(val);
				return true;
			default:
				return super.parseArg(key, val);
		}
	}

	public static void printHelp() {
		System.out.println();
		App.printHelp();
		System.out.println("TheONEFile:");
		System.out.println("Outputs node movement in The ONE format");
		System.out.println("\t-f <filename> (scenario)");
		System.out.println("\t-l <double> (sample interval length, default is 1s)");
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		new TheONEFile(args);
	}
}
