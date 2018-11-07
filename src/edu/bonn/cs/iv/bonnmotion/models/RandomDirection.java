package edu.bonn.cs.iv.bonnmotion.models;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.bonn.cs.iv.bonnmotion.MobileNode;
import edu.bonn.cs.iv.bonnmotion.Position;
import edu.bonn.cs.iv.bonnmotion.RandomSpeedBase;
import edu.bonn.cs.iv.bonnmotion.Scenario;
import edu.bonn.cs.iv.bonnmotion.ScenarioLinkException;
import edu.bonn.cs.iv.bonnmotion.Waypoint;

/** Application to construct RandomDirection mobility scenarios. */
/** 
 *  Chris Walsh
 *  June 2009
 * 
 *  Nodes will select a random direction and speed. They travel
 *  until the reach the edge of the simulation. They then pause
 *  and pick a new direction and speed.
 *  
 *  
 */

public class RandomDirection extends RandomSpeedBase {

	private static final String MODEL_NAME = "RandomDirection";

	protected double minpause = 0.0;

	public RandomDirection(int nodes, double x, double y, double duration, double ignore, long randomSeed, double minspeed, double maxspeed, double maxpause, double minpause) {
		super(nodes, x, y, duration, ignore, randomSeed, minspeed, maxspeed, maxpause);
		this.minpause = minpause;
		generate();
	}
	
	public RandomDirection(String[] args) {
		go(args);
	}

	public void go(String[] args) {
		super.go(args);
		generate();
	}

	public RandomDirection(String args[], Scenario _pre, Integer _transitionMode) {
		// we've got a predecessor, so a transition is needed
		predecessorScenario = _pre;
		transitionMode = _transitionMode.intValue();
		isTransition = true;
		go(args);
	}
	
	public void generate() {	
		double xTime, yTime, speed, newX, newY, angle;
		preGeneration();

		for (int i = 0; i < node.length; i++) {
			node[i] = new MobileNode();
			double t = 0.0;
			Position src = null;
			
			if (isTransition) {
				try {
					Waypoint lastW = transition(predecessorScenario, transitionMode, i);
					src = lastW.pos;
					t = lastW.time;
				} 
				catch (ScenarioLinkException e) {
					e.printStackTrace();
				}
			} 
			else src = randomNextPosition();
			
			angle = randomNextDouble() * 2 * Math.PI;
			
			while (t < duration) {
				Position dst;
				
				if (!node[i].add(t, src))
					throw new RuntimeException(MODEL_NAME + ".go: error while adding waypoint (1)");
				
				speed = (maxspeed - minspeed) * randomNextDouble() + minspeed;
				
				if (angle >= 0 && angle < Math.PI/2)
				{
					xTime = (x - src.x)/(speed*Math.cos(angle));
					yTime = (y - src.y)/(speed*Math.sin(angle));
					
					if(xTime < yTime) // hit right wall first 
					{
						newX = x;
						newY = (speed*xTime*Math.sin(angle)) + src.y;
						angle = (randomNextDouble() * Math.PI) + (Math.PI/2);
					}
					else if (yTime < xTime) // hit top wall first 
					{
						newX = (speed*yTime*Math.cos(angle)) + src.x;
						newY = y;
						angle = (randomNextDouble() * Math.PI) + (Math.PI);
					}
					else // hit corner angle = Math.PI/2
					{
						newX = x;
						newY = y;
						angle = (randomNextDouble() * Math.PI/2) + (Math.PI);
					}					
				}
				else if (angle >= Math.PI/2 && angle < Math.PI)
				{
					xTime = (0 - src.x)/(speed*Math.cos(angle));
					yTime = (y - src.y)/(speed*Math.sin(angle));
					
					if(xTime < yTime) // hit left wall first
					{
						newX = 0;
						newY = (speed*xTime*Math.sin(angle)) + src.y;
						angle = ((randomNextDouble() * Math.PI) + (Math.PI*3/2)) % (2*Math.PI);
					}
					else if (yTime < xTime) // hit top wall first
					{
						newX = (speed*yTime*Math.cos(angle)) + src.x;
						newY = y;
						angle = (randomNextDouble() * Math.PI) + (Math.PI);
					}
					else // hit corner angle = Math.PI/2
					{
						newX = 0;
						newY = y;
						angle = (randomNextDouble() * Math.PI/2) + (Math.PI*3/2);
					}	
				}
				else if (angle >= Math.PI && angle < Math.PI*3/2)
				{
					xTime = (0 - src.x)/(speed*Math.cos(angle));
					yTime = (0 - src.y)/(speed*Math.sin(angle));
					
					if(xTime < yTime) // hit left wall first
					{
						newX = 0;
						newY = (speed*xTime*Math.sin(angle)) + src.y;
						angle = ((randomNextDouble() * Math.PI) + (Math.PI*3/2)) % (2*Math.PI);
					}
					else if (yTime < xTime) // hit bottom wall first
					{
						newX = (speed*yTime*Math.cos(angle)) + src.x;
						newY = 0;
						angle = (randomNextDouble() * Math.PI);
					}
					else // hit corner angle = Math.PI/2
					{
						newX = 0;
						newY = 0;
						angle = (randomNextDouble() * Math.PI/2);
					}	
				}
				else if (angle >= Math.PI*3/2 && angle < Math.PI*2)
				{
					xTime = (x - src.x)/(speed*Math.cos(angle));
					yTime = (0 - src.y)/(speed*Math.sin(angle));
					
					if(xTime < yTime) // hit right wall first
					{
						newX = x;
						newY = (speed*xTime*Math.sin(angle)) + src.y;
						angle = (randomNextDouble() * Math.PI) + (Math.PI/2);
					}
					else if (yTime < xTime) // hit bottom wall first
					{
						newX = (speed*yTime*Math.cos(angle)) + src.x;
						newY = 0;
						angle = (randomNextDouble() * Math.PI);
					}
					else // hit corner angle = Math.PI/2
					{
						newX = x;
						newY = 0;
						angle = (randomNextDouble() * Math.PI/2) + (Math.PI/2);
					}
				}
				else throw new RuntimeException(MODEL_NAME + ".go: error angle didn't fall into any of the four quadrants. (Something blew up?)");
				
				dst = new Position(newX, newY);
				t += src.distance(dst) / speed;
				
				if (!node[i].add(t, dst))
					throw new RuntimeException(MODEL_NAME + ".go: error while adding waypoint (2)");
				
				if ((t < duration) && (maxpause > 0.0)) {
					double pause = (maxpause-minpause) * randomNextDouble() + minpause;
					t += pause;
				}
				src = dst;
			}
		}

		postGeneration();
	}

	protected boolean parseArg(String key, String value) {
		if (key.equals("minpause")) {
			minpause = Double.parseDouble(value);
			return true;
		} 
		else return super.parseArg(key, value);
	}

	public void write(String _name) throws FileNotFoundException, IOException {
		String[] p = new String[1];
		p[0] = "minpause=" + minpause;
		super.write(_name, p);
	}

	protected boolean parseArg(char key, String val) {
		switch (key) {
			case 'O':
			case 'o': // "minimum pause time"
				minpause = Double.parseDouble(val);
				return true;
			default:
				return super.parseArg(key, val);
		}
	}
	
	public static void printHelp() {
		RandomSpeedBase.printHelp();
		System.out.println( MODEL_NAME + ":");
		System.out.println("\t-o <minimum pause time>");
	}

	protected void postGeneration() {
		for (int i = 0; i < node.length; i++) {
			Waypoint l = node[i].getLastWaypoint();
			if (l.time > duration) {
				Position p = node[i].positionAt(duration);
				node[i].removeLastElement();
				node[i].add(duration, p);
			}
		}
		super.postGeneration();
	}
}