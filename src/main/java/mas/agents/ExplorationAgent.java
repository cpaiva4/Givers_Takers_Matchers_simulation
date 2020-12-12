package mas.agents;

import mas.core.MultiAgentExplorator;
import mas.objects.EnvironmentObject;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableDouble2D;

/**
 * Bruno Leão
 * Carlos Paiva
 */
public abstract class ExplorationAgent implements Steppable {


    public static double STEP = Math.sqrt(2);
    private static final long serialVersionUID = 1L;
    //private float INTEREST_THRESHOLD = 65;
    private final int viewRange = 3;
    public int score=0;
    public ExplorationAgent helping=null;

    private int identifyClock;

    public int helped = 0;
    private Double2D loc;
    public Int2D target=null;
    private double orientation=-Math.PI/2;
    private int steps=0;
    public Int2D nope=null;

    //public SimEnvironment env;
    //public BrokerAgent broker;
    //public MapperAgent mapper;
    //private Vector<Prototype> knownObjects;

    public void setLoc(Double2D loc) {
        this.loc = loc;
    }

    public synchronized Int2D getTarget() {
        return this.target;
    }

    public synchronized void setTarget(Int2D target) {
        this.target = target;
    }

    private boolean GLOBAL_KNOWLEDGE = true;
    private int IDENTIFY_TIME = 15;
    public MultiAgentExplorator mae;
    int type;
    int id;


    public ExplorationAgent(int t, int i){
        type=t;
        id=i;
    }

    // Decide which agent will come here on the next turn
    public synchronized boolean broadcast(MultiAgentExplorator mae, EnvironmentObject obj) {
        ExplorationAgent choice = null;
        double distance = Double.MAX_VALUE;
        double aux;

        for(ExplorationAgent ag : mae.givers) {
            if(ag.type == obj.type1 && ag.getTarget() == null) {
                aux = euclidianDist(ag, obj);

                if(aux < distance) {
                    distance = aux;
                    choice = ag;
                }
            }
        }

        for(Matcher ag : mae.matchers) {
            if(ag.type == obj.type1 && ag.getTarget() == null) {
                aux = euclidianDist(ag, obj);

                if(aux < distance && ag.decide(this)) {
                    distance = aux;
                    choice = ag;
                }
            }
        }

        if(choice != null) {
            //System.out.println("Agent " + choice.id + " will explore the object: (" + obj.loc + ", " + obj.type1 + ")");
            choice.help(this, obj);
            return true;
        } else {
            mae.occupied[(int)(obj.loc.x )][(int)(obj.loc.y)]=1;
            return false;
        }
    }

    public double euclidianDist(ExplorationAgent ag, EnvironmentObject obj) {
        return Math.sqrt(Math.pow(ag.loc.x - obj.loc.x, 2) + Math.pow(ag.loc.y - obj.loc.y, 2));
    }

    public abstract void help(ExplorationAgent ag, EnvironmentObject targ);

    public void step(SimState state) {
        this.mae = (MultiAgentExplorator) state;
        Continuous2D field = mae.explorationField;

        Double2D loc = field.getObjectLocation(this);

        // All implementations will identify the object when on the target
        if (target != null) {
            if (loc.distance(target) == 0) {

                EnvironmentObject obj= mae.getObj(loc);
                identify(obj);


                identifyClock = IDENTIFY_TIME;
            }
        }

        // If the explorer does not get a new target, he has to request a new one from
        // the broker
        if (target == null) {
            if (identifyClock == 0) {
                Bag visible = field.getObjectsWithinDistance(loc,1.0*viewRange);

                // -------------------------------------------------------------
                int r=0;
                while (visible.size()>r){
                    if (visible.get(r) instanceof EnvironmentObject){
                        //Hashtable<Class, Double> probs = getProbabilityDist(obj);
                        EnvironmentObject obj=(EnvironmentObject) visible.get(r);
                        if (nope!=null){
                        if(obj.loc.x==nope.x && obj.loc.y==nope.y){
                        	r++;
                        	continue;
                        }}
                        if(mae.occupied[(int)(obj.loc.x )][(int)(obj.loc.y)]<2){
                            target= new Int2D((int)(obj.loc.x ),(int)(obj.loc.y));
                            mae.occupied[(int)(obj.loc.x )][(int)(obj.loc.y)]=2;
                            break;}}
                    r++;

                }

                //System.out.println("NEW TARGET: X: " + target.x + " Y: "
                //		+ target.y);
            }
        }


        // Agent movement
        Double2D step;
        double[] limstep;
        int f=0;
        if (target!=null){

            step = new Double2D(target.x - loc.x, target.y - loc.y);
        }
        else
        {

            double a=Math.random()*Math.PI;
            if (loc.x>=mae.WIDTH){

                //System.out.println(a);
                orientation=Math.PI/2+a;
                //System.out.println("dvs "+orientation);
                //System.out.println("c1");
                f=1;
            }
            if (loc.x<=0){
                orientation=-Math.PI/2+a;
                //System.out.println("c2");
                f=1;}
            if (loc.y>=mae.HEIGHT){
                orientation=-Math.PI+a;
                //System.out.println("c3");
                f=1;}
            if (loc.y<=0){
                orientation=a;
                //System.out.println("c4");
                f=1;}

            step= new Double2D((STEP/Math.sqrt(2))*Math.cos(orientation),(STEP/Math.sqrt(2))*Math.sin(orientation));
        }
        limstep=limit(step);

        loc= new Double2D(loc.x+limstep[0],loc.y+limstep[1]);

        mae.updateLocation(this, loc);


        orientation = Math.atan2(limstep[1], limstep[0]);

        if (identifyClock > 0)
            identifyClock--;
    }


    // Can identify only if the same type of the specialist
    public synchronized void identify(EnvironmentObject obj) {
        boolean flag=false;
    	if (obj.type1!=type){
            flag=broadcast(mae, obj);
        }
        else{
            //System.out.println("Agent " + this.id + " will explore the object: (" + obj.loc + ", " + obj.type1 + ")");
            mae.occupied[(int)(obj.loc.x )][(int)(obj.loc.y)]=3;
            if (helping==null) {
                score++;
            } else
            {
                helped++;
                mae.totalHelpedAgentes++;
                //System.out.println(id + "  " + helped);
                score++;
                helping.score++;
                helping=null;
            }
            mae.identifiedObjects++;
        }
    	if (!flag){
    		nope=target;
    	}
        setTarget(null);
    }

    public double orientation2D() {
        return orientation;
    }

    public Double2D getLoc() {
        return loc;
    }

    public double getOrientation() {
        return orientation;
    }
    public double[] limit(Double2D d) {
        double[] ret={d.x,d.y};
        //System.out.println(ret[0]+" vdsv "+ret[1]);
        if(Math.abs(d.x)>Math.abs(d.y)){
            if(Math.abs(d.x)>STEP){
                ret[1]=d.y/(Math.abs(d.x)/STEP);
                ret[0]=STEP*d.x/Math.abs(d.x);
            }
        }
        else{
            if(Math.abs(d.y)>STEP){
                ret[0]=d.x/(Math.abs(d.y)/STEP);
                ret[1]=STEP*d.y/Math.abs(d.y);
            }
        }
        return ret;
    }


}