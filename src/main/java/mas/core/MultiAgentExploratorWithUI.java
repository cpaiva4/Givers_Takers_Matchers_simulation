package mas.core;

import sim.display.*;
import sim.engine.*;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

import javax.swing.*;
import java.awt.*;

/**
 * Bruno Leão
 * Carlos Paiva
 */
public class MultiAgentExploratorWithUI extends GUIState{

    public Display2D display;
    public JFrame displayFrame;
    ContinuousPortrayal2D fieldPortrayal = new ContinuousPortrayal2D();


    public Object getSimulationInspectedObject() { return state; }

    public Inspector getInspector() {
        Inspector i = super.getInspector();
        i.setVolatile(true);
        return i;
    }


    public static void main(String[] args) {
        MultiAgentExploratorWithUI vid = new MultiAgentExploratorWithUI();
        Console c = new Console(vid);
        c.setVisible(true);
    }

    public MultiAgentExploratorWithUI() { super(new MultiAgentExplorator(666)); }
    public MultiAgentExploratorWithUI(SimState state) { super(state); }
    public static String getName() { return "Multi Agent Exploration - GTM Strategy"; }


    public void start() {

        super.start();
        setupPortrayal();

    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayal();
    }

    private void setupPortrayal() {
        MultiAgentExplorator mae = (MultiAgentExplorator) state;

        // tell the portray what to portray and how to portray them
        fieldPortrayal.setField(mae.explorationField);
        fieldPortrayal.setPortrayalForAll(new OvalPortrayal2D());

        // reschedule the displayer
        display.reset();
        display.setBackdrop(Color.WHITE);

        // redraw the display
        display.repaint();
    }

    public void init(Controller c) {

        MultiAgentExplorator mae = (MultiAgentExplorator) state;

        super.init(c);

        display = new Display2D(600, 600, this);
        display.setClipping(true);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Giver: " + mae.numAgents*mae.giverPercentage
                                + ", Taker: " + mae.numAgents*mae.takerPercentage
                                + ", Matcher: " + mae.numAgents*mae.matcherPercentage);
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        display.attach(fieldPortrayal, "Field");
    }
    


    public void quit() {
        super.quit();
        if(displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

   /* public boolean step(){
        MultiAgentExplorator mae = (MultiAgentExplorator) state;
        boolean retval= super.step();
        mae.step(state);
        return retval;
    }*/
}
