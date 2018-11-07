package edu.bonn.cs.iv.bonnmotion;

import java.io.*;

public class RandomSpeedBaseDisasterArea extends ScenarioDisasterArea {
    protected double minspeed = 0.5;
    protected double maxspeed = 1.5;
    protected double maxpause = 60.0;

    public RandomSpeedBaseDisasterArea(int nodes, double x, double y, double duration, double ignore, long randomSeed, double minspeed, double maxspeed, double maxpause) {
		super(nodes, x, y, duration, ignore, randomSeed);
		this.minspeed = minspeed;
		this.maxspeed = maxspeed;
		this.maxpause = maxpause;
    }

    public RandomSpeedBaseDisasterArea() {
	
    }

    public void write(String basename, String[] params) throws FileNotFoundException, IOException {
	    String[] p = new String[3];
	    p[0] = "minspeed=" + minspeed;
	    p[1] = "maxspeed=" + maxspeed;
	    p[2] = "maxpause=" + maxpause;
	    super.write(basename, App.stringArrayConcat(params, p));
    }

    protected boolean parseArg(char key, String val) {
	    switch (key) {
		    case 'h': // "high"
			    maxspeed = Double.parseDouble(val);
			    return true;
		    case 'l': // "low"
			    minspeed = Double.parseDouble(val);
			    return true;
		    case 'p': // "pause"
			    maxpause = Double.parseDouble(val);
			    return true;
		    default:
			    return super.parseArg(key, val);
	    }
    }

    protected boolean parseArg(String key, String val) {
	    if (key.equals("minspeed") ) {
		    minspeed = Double.parseDouble(val);
		    return true;
	    } else if (	key.equals("maxspeed") ) {
		    maxspeed = Double.parseDouble(val);
		    return true;
	    } else if (	key.equals("maxpause") ) {
		    maxpause = Double.parseDouble(val);
		    return true;
	    } else return super.parseArg(key, val);
    }

    public static void printHelp() {
	    Scenario.printHelp();
	    System.out.println("RandomSpeedBaseDisasterArea:");
	    System.out.println("\t-h <max. speed>");
	    System.out.println("\t-l <min. speed>");
	    System.out.println("\t-p <max. pause time>");
    }
}