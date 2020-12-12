package mas.core;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Vector;

import mas.agents.*;
import mas.objects.EnvironmentObject;
import org.jfree.data.xy.XYSeries;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.media.chart.TimeSeriesChartGenerator;

/**
 * Bruno Leão
 * Carlos Paiva
 */
public class MultiAgentExplorator extends SimState implements Steppable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public final static int WIDTH = 200;
    public final static int HEIGHT = 200;
    public Continuous2D explorationField = new Continuous2D(1.0, WIDTH, HEIGHT);

    public double randomMultiplier = 0.5;
    public int numAgents = 60;
    public double giverPercentage = 0.8;
    public double takerPercentage = 0.1;
    public double matcherPercentage = 0.1;
    private Vector<ExplorationAgent> explorers;
    public int[][] occupied= new int[WIDTH][HEIGHT];
    public int MAX_1_CLASS=3;

    //statistics
    public Giver[] givers;
    public Taker[] takers;
    public Matcher[] matchers;
    public int identifiedObjects = 0;

    //statistics
    public int totalHelpedAgentes = 0;
    public final int NUM_OBJECTS_PER_CLASS=300;
    public final int MAX_NUM_CLASSES = 3;
    public int numObjects = NUM_OBJECTS_PER_CLASS*MAX_NUM_CLASSES;
    public ArrayList<Integer> iops = new ArrayList<Integer>();


    public int[] getGiverPerfomance() {
        if(givers == null)
            return new int[0];
        int[] distro = new int[givers.length];
        for(int i = 0; i < givers.length; i++) {
            //System.out.println("score: " + givers[i].score + " ite: " + i);
            distro[i] = (int) givers[i].score;
        }
        return distro;
    }
    
    public int[] getGiverHelped() {
        if(givers == null)
            return new int[0];
        int[] distro = new int[givers.length];
        for(int i = 0; i < givers.length; i++) {
            //System.out.println("score: " + givers[i].score + " ite: " + i);
            distro[i] = (int) givers[i].helped;
        }
        return distro;
    }
    
    public int[] getTakerPerfomance() {
        if(takers == null)
            return new int[0];
        int[] distro = new int[takers.length];
        for(int i = 0; i < takers.length; i++) {
            //System.out.println("score: " + givers[i].score + " ite: " + i);
            distro[i] = (int) takers[i].score;
        }
        return distro;
    }
    
    public int[] getMatcherPerfomance() {
        if(matchers == null)
            return new int[0];
        int[] distro = new int[matchers.length];
        for(int i = 0; i < matchers.length; i++) {
            //System.out.println("score: " + givers[i].score + " ite: " + i);
            distro[i] = (int) matchers[i].score;
        }
        return distro;
    }
    
    public int[] getMatcherHelped() {
        if(matchers == null)
            return new int[0];
        int[] distro = new int[matchers.length];
        for(int i = 0; i < matchers.length; i++) {
            //System.out.println("score: " + givers[i].score + " ite: " + i);
            distro[i] = matchers[i].helped;
        }
        return distro;
    }

    public int getNumHelped() {
        return totalHelpedAgentes;
    }

    public int getOverallPerformance() {
        return identifiedObjects;
    }



    public MultiAgentExplorator(long seed) {
        super(seed);
    }

    public Continuous2D createEmptyEnvironment() {
        return new Continuous2D(1.0,WIDTH, HEIGHT);
    }

    public void start() {
        super.start();

        //explorationField = createEmptyEnvironment();

        // A little protection
        assert numAgents > 0 && giverPercentage + takerPercentage + matcherPercentage == 1;

        int numGivers = (new Double((numAgents*giverPercentage))).intValue();
        int numTakers = (new Double((numAgents*takerPercentage))).intValue();
        int numMatchers = (new Double((numAgents*matcherPercentage))).intValue();

        assert numGivers + numMatchers + numTakers == numAgents;

        int id=0;

        givers = new Giver[numGivers];
        takers = new Taker[numTakers];
        matchers = new Matcher[numMatchers];

        // clear the field
        explorationField.clear();
        buildRandomMap(this);
        // add some GIVERS to the field
        for (int i = 0; i < numGivers; i++) {
            Giver giver = new Giver(i%3,id);
            givers[i] = giver;
            // put the agent on the center of the field
            Double2D agLoc = new Double2D(explorationField.getWidth() * .5 *random.nextDouble() - 0.5,
                    explorationField.getHeight() * 0.5 *random.nextDouble() - 0.5);
            giver.setLoc(agLoc);
            explorationField.setObjectLocation(giver, agLoc);

            schedule.scheduleRepeating(giver);
            id++;
        }

        // add some TAKERS to the field
        for (int i = 0; i < numTakers; i++) {
            Taker taker = new Taker(i%3,id);
            takers[i] = taker;
            // put the agent on the center of the field
            Double2D agLoc = new Double2D(explorationField.getWidth() * .5 *random.nextDouble() - 0.5,
                    explorationField.getHeight() * 0.5 *random.nextDouble() - 0.5);
            taker.setLoc(agLoc);
            explorationField.setObjectLocation(taker, agLoc);
            schedule.scheduleRepeating(taker);
            id++;
        }

        // add some MATCHERS to the field
        for (int i = 0; i < numMatchers; i++) {
            Matcher matcher = new Matcher(i%3,id, this);
            matchers[i] = matcher;
            // put the agent on the center of the field
            Double2D agLoc = new Double2D(explorationField.getWidth() * .5 *random.nextDouble() - 0.5,
                    explorationField.getHeight() * 0.5 *random.nextDouble() - 0.5);
            matcher.setLoc(agLoc);
            explorationField.setObjectLocation(matcher, agLoc);
            schedule.scheduleRepeating(matcher);
            id++;
        }
    }

    public void updateLocation(ExplorationAgent agent, Double2D loc) {

        explorationField.setObjectLocation(agent, loc);
    }

    private void addObject(int t, Double2D loc) {
        EnvironmentObject obj;

        obj = new EnvironmentObject(loc,t/3,t%3);

        explorationField.setObjectLocation(obj,loc);
        occupied[(int)loc.x][(int)loc.y] = 1;
    }

    private void buildRandomMap(SimState state) {

        //int numberOfInstances[]= new int[9];
        Double2D loc;

        for (int i = 0; i < MAX_NUM_CLASSES; i++) {
            //int a=state.random.nextInt(MAX_1_CLASS);
            for(int j = 0; j < NUM_OBJECTS_PER_CLASS; j++) {
                do { loc = new Double2D((double)state.random.nextInt((int)explorationField.getWidth()-2)+1  ,(double)state.random.nextInt((int) explorationField.getHeight()-2)+1); }
                while (occupied[(int)loc.x][(int)loc.y] !=0);

                addObject(i, loc);

            }

        }
    }

    public EnvironmentObject getObj(Double2D loc){
        Bag here = explorationField.getObjectsAtLocation(loc);
        int i = 0;

        if(here == null){
            return null;
        }

        while((here.get(i) instanceof ExplorationAgent) && i<here.numObjs) i++;

        EnvironmentObject real = (EnvironmentObject) here.get(i);

        return real;
    }

    //@Override
    public void step(SimState state) {
        iops.add(identifiedObjects);
    	System.out.println(identifiedObjects);
    	if(identifiedObjects==NUM_OBJECTS_PER_CLASS*MAX_NUM_CLASSES){
            TimeSeriesChartGenerator chart = new TimeSeriesChartGenerator();
            XYSeries xy= new XYSeries("Performance vs Steps");

        	try {
				super.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    public void statistics(){

    }


    public static void main(String[] args) {
        doLoop(MultiAgentExplorator.class, args);
        System.exit(0);
    }


}
