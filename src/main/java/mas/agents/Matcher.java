package mas.agents;

import mas.core.MultiAgentExplorator;
import mas.objects.EnvironmentObject;
import sim.engine.SimState;
import sim.util.Int2D;

/**
 * Bruno Leão
 * Carlos Paiva
 */
public class Matcher extends ExplorationAgent {

    int[][] matches;
    public int helpedTotal = 0;
    public double prop=0.2;

    public Matcher(int t, int i, MultiAgentExplorator mae) {
        super(t, i);
        initMatches(mae);
    }

    public void initMatches(SimState state) {
        MultiAgentExplorator mae = (MultiAgentExplorator) state;
        matches= new int[mae.numAgents][2];

        for (int j = 0; j < mae.numAgents; j++) {
            matches[j][0]=j;
            matches[j][1]=0;
        }
    }


   /* public void step(SimState state) {
        super.step(state);
    }*/

    //metodo a chamar depois de receber a mensgem se target==null
    public synchronized void help(ExplorationAgent ag, EnvironmentObject targ) {
            //responde � mensagem
            //verifica se foi escolhido
            //caso seja:
            helping=ag; //para que o agente ajudado possa ganhar pontos
            target=new Int2D((int)targ.loc.x,(int)targ.loc.y);
            if (helping instanceof Matcher){
            	//System.out.print("match!!!!");
                ((Matcher) helping).matches[id][1]++;
                ((Matcher) helping).helpedTotal--;
            }
            matches[ag.id][1]--;
            helpedTotal++;
        }

    public boolean decide(ExplorationAgent agent) {

        if(matches[agent.id][1] > -1){
//        	if (matches[agent.id][1]==0 && helpedTotal>prop*mae.numAgents){
//        		return false;
//        	}
            return true;
        }  else {
            return false;
        }

    }
}
