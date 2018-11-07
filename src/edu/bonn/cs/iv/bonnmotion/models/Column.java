package edu.bonn.cs.iv.bonnmotion.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import edu.bonn.cs.iv.bonnmotion.GroupNode;
import edu.bonn.cs.iv.bonnmotion.MobileNode;
import edu.bonn.cs.iv.bonnmotion.Position;
import edu.bonn.cs.iv.bonnmotion.RandomSpeedBase;

/** Application to create movement scenarios according to the Column model. */

public class Column extends RandomSpeedBase {

	private static final String MODEL_NAME = "Column";
	/** Maximum deviation from group center [m]. */
	protected double maxdist = 2.5;
	/** Number of Groups */
	protected int numGroups = 0;
	/** Reference point separation **/
	protected double refPtSeparation = 10.0;
	/** Number of nodes per Group (no input Parameter) */
	protected int nodesPerGroup = 0;

	public Column(int nodes, double x, double y, double duration, double ignore, long randomSeed, 
			double minspeed, double maxspeed, double maxpause, int numGroups,
			double refPtSeparation, double maxdist) {
		super(nodes, x, y, duration, ignore, randomSeed, minspeed, maxspeed, maxpause);
		this.numGroups = numGroups;
		this.refPtSeparation = refPtSeparation;
		this.maxdist = maxdist;
		
		generate();
	}

	public Column(String[] args) {
		go(args);
	}

	public void go(String args[]) {
		super.go(args);
		generate();
	}

	public void generate() {
		nodesPerGroup = node.length / numGroups;
		
		if (node.length % numGroups != 0) {
			System.out.println(MODEL_NAME+ ".go: Error: Must use an even multiple of nodes for the number of groups specified.");
			System.exit(1);
		}
		
		if ((nodesPerGroup - 1) * refPtSeparation > x || (nodesPerGroup - 1) * refPtSeparation > y) {
			throw new RuntimeException(MODEL_NAME+ ".go: Error: The line of reference points with " +
					"the given parameters (nodes, numGroups, and refPtSeparation) exceeds the dimensions of " +
					"the simulation. Please either increase the size of the simulation or decrese " +
					"the nodesPerGroup, or decrease the refPtSeparation");
		}
		
		preGeneration();

		GroupNode[] node = new GroupNode[this.node.length];
		Vector<Vector<MobileNode>> rpoints = new Vector<Vector<MobileNode>>();

		// create groups of ref points
		for (int i = 0; i < numGroups; i++)
		{
			rpoints.add(new Vector<MobileNode>());
			double dir = 0;
			double refPtSeparationX = 0;
			double refPtSeparationY = 0;
			
			for (int j = 0; j < nodesPerGroup; j++)
			{
				MobileNode ref = new MobileNode();
				rpoints.get(i).addElement(ref);
				
				Position src;
				
				if (j == 0) {
					//first refPt of the group. Pick a direction the column should extend. Pick a random location.
					dir = (Math.PI * 2 * randomNextDouble());
					refPtSeparationX = refPtSeparation * Math.cos(dir);
					refPtSeparationX = refPtSeparation * Math.sin(dir);
					src = new Position((x - 2 * maxdist) * randomNextDouble() + maxdist, (y - 2 * maxdist) * randomNextDouble() + maxdist);
				} else {
					// All other following refPts in this group. Location based on head refPt + some offset
					double groupHeadX = rpoints.get(i).get(0).positionAt(0.0).x;
					double groupHeadY = rpoints.get(i).get(0).positionAt(0.0).y;
					double srcX = groupHeadX + (j * refPtSeparationX);
					double srcY = groupHeadY + (j * refPtSeparationY);
					
					//If refPt is placed outside of simulation, put it at the edge instead.
					if (srcX > x) srcX = x;
					else if (srcX < 0) srcX = 0;
					
					if (srcY > y) srcY = y;
					else if (srcY < 0) srcY = 0;
					
					src = new Position(srcX, srcY);
				}
				
				if (!ref.add(0.0, src)) {
					System.out.println(MODEL_NAME + ".generate: error while adding group movement (1)");
					System.exit(0);
				}
			}
		}
		
		// Now create the reference point movements
		Position dst, src;
		double t = 0.0;
		
		while (t < duration)
		{
			for (int i = 0; i < numGroups; i++)
			{
				double dir = 0;
				double refPtSeparationX = 0;
				double refPtSeparationY = 0;

				for (int j = 0; j < nodesPerGroup; j++)
				{	
					src = new Position(rpoints.get(i).get(j).positionAt(t).x, rpoints.get(i).get(j).positionAt(t).y);
					if (!rpoints.get(i).get(j).add(t, src)) {
						System.out.println(MODEL_NAME + ".generate: error while adding group movement (2)");
						System.exit(0);
					}
					
					if (j == 0) {
						//first refPt of the group. Pick a direction the column should extend. Pick a random location.
						dir = (Math.PI * 2 * randomNextDouble());
						refPtSeparationX = refPtSeparation * Math.cos(dir);
						refPtSeparationY = refPtSeparation * Math.sin(dir);
						dst = new Position((x - 2 * maxdist) * randomNextDouble() + maxdist, (y - 2 * maxdist) * randomNextDouble() + maxdist);
					} else {
						// All other following refPts in this group. Location based on head refPt + some offset
						double groupHeadX = rpoints.get(i).get(0).positionAt(t).x;
						double groupHeadY = rpoints.get(i).get(0).positionAt(t).y;
						double srcX = groupHeadX + (j * refPtSeparationX);
						double srcY = groupHeadY + (j * refPtSeparationY);
						
						//If refPt is placed outside of simulation, put it at the edge instead.
						if (srcX > x) srcX = x;
						else if (srcX < 0) srcX = 0;
						
						if (srcY > y) srcY = y;
						else if (srcY < 0) srcY = 0;
						
						dst = new Position(srcX, srcY);
					}
					
					double speed = (maxspeed - minspeed) * randomNextDouble() + minspeed;
					t += src.distance(dst) / speed;
					
					if (!rpoints.get(i).get(j).add(t, dst)) {
						System.out.println(MODEL_NAME + ".generate: error while adding group movement (3)");
						System.exit(0);
					}
					
					if ((t < duration) && (maxpause > 0.0)) {
						double pause = maxpause * randomNextDouble();
						if (pause > 0.0) {
							t += pause;
							
							if (!rpoints.get(i).get(j).add(t, dst)) {
								System.out.println(MODEL_NAME + ".generate: error while adding node movement (4)");
								System.exit(0);
							}
						}
					}

					src = dst;
				}
			}
		}
				
		// assign nodes to each reference point
		for (int i = 0; i < numGroups; i++) {			
			for(int j = 0; j < nodesPerGroup; j++) {
				node[(i * nodesPerGroup) + j] = new GroupNode(rpoints.get(i).get(j));
			}
		}
				
		// nodes follow their reference points:
		for (int i = 0; i < node.length; i++) {
			t = 0.0;
			MobileNode group = node[i].group();

			src = group.positionAt(t).rndprox(maxdist, randomNextDouble(), randomNextDouble());
			
			if (!node[i].add(0.0, src)) {
				System.out.println(MODEL_NAME + ".generate: error while adding node movement (5)");
				System.exit(0);
			}
			
			double[] gm = group.changeTimes();
			
			while (t < duration) {
				
				int gmi = 0;
				while ((gmi < gm.length) && (gm[gmi] <= t)) gmi++;
				
				/* next absolute time a change happens or the simulation time is over */
				double next = (gmi < gm.length) ? gm[gmi] : duration;
				
				dst = group.positionAt(next).rndprox(maxdist, randomNextDouble(), randomNextDouble());
				double speed = src.distance(dst) / (next - t);

				if (speed > maxspeed) {
					double c_dst = ((maxspeed - minspeed) * randomNextDouble() + minspeed) / speed;
					double c_src = 1 - c_dst;
	
					dst = new Position(c_src * src.x + c_dst * dst.x, c_src * src.y + c_dst * dst.y);
					
					// ref point isn't pausing, we need to move with it
					if (maxpause == 0.0) {
						t = next;
						
						if (!node[i].add(t, dst)) {
							System.out.println(MODEL_NAME + ".generate: error while adding group movement (2)");
							System.exit(0);
						}
					} else { //ref pt is pausing, we don't have to time = next
						speed = (maxspeed - minspeed) * randomNextDouble() + minspeed;
						t += src.distance(dst) / speed;
						
						if (!node[i].add(t, dst)) {
							System.out.println(MODEL_NAME + ".generate: error while adding group movement (2)");
							System.exit(0);
						}
						
						if ((t < duration) && (maxpause > 0.0)) {
							double nodePause = maxpause * randomNextDouble();
							if (nodePause > 0.0) {
								// check if our pause time is larger than when our ref pt changes
								if (nodePause + t > next) t = next;
								else t += nodePause;
								
								if (!node[i].add(t, dst)) {
									System.out.println(MODEL_NAME + ".generate: error while adding node movement (3)");
									System.exit(0);
								}
							}
						}
					}
				} else {
					// there's time to take a pause, push the simulation time ahead: movement time + pause time
					speed = (maxspeed - minspeed) * randomNextDouble() + minspeed;
					t += src.distance(dst) / speed;
					
					if (!node[i].add(t, dst)) {
						System.out.println(MODEL_NAME + ".generate: error while adding group movement (2)");
						System.exit(0);
					}

					if ((t < duration) && (maxpause > 0.0)) {
						double nodePause = maxpause * randomNextDouble();
						
						if (nodePause > 0.0) {
							// check if our pause time is larger than when our ref pt changes
							if (nodePause + t > next) t = next;
							else t += nodePause;
							
							if (!node[i].add(t, dst)) {
								System.out.println(MODEL_NAME + ".generate: error while adding node movement (3)");
								System.exit(0);
							}
						}
					}
				}
				
				src = dst;
			}
		}

		// write the nodes into our base
		this.node = node;

		postGeneration();
	}
	
	protected boolean parseArg(String key, String value) {
		if (key.equals("numgroups")) {
			numGroups = Integer.parseInt(value);
			return true;
		} else if (key.equals("refptseparation")) {
			refPtSeparation = Double.parseDouble(value);
			return true;
		} else if (key.equals("maxdist")) {
			maxdist = Double.parseDouble(value);
			return true;
		} else return super.parseArg(key, value);
	}

	public void write(String _name) throws FileNotFoundException, IOException {
		String[] p = new String[3];

		p[0] = "numgroups=" + numGroups;
		p[1] = "refptseparation=" + refPtSeparation;
		p[2] = "maxdist=" + maxdist;

		super.write(_name, p);
	}

	protected boolean parseArg(char key, String val) {
		switch (key) {
		case 'a': // "number of groups"
			numGroups = Integer.parseInt(val);
			return true;
		case 'r': // "reference point separation"
			refPtSeparation = Double.parseDouble(val);
			return true;
		case 's': // "maxDist"
			maxdist = Double.parseDouble(val);
			return true;
		default:
			return super.parseArg(key, val);
		}
	}

	public static void printHelp() {
		RandomSpeedBase.printHelp();
		System.out.println( MODEL_NAME + ":" );
		System.out.println("\t-a <number of groups>");
		System.out.println("\t-r <reference point separation>");
		System.out.println("\t-s <max. distance to group center>");
	}
}