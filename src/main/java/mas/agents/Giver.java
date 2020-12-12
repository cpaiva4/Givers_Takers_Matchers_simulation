package mas.agents;

import mas.objects.EnvironmentObject;
import sim.util.Int2D;

/**
 * Bruno Leão
 * Carlos Paiva
 */
public class Giver extends ExplorationAgent {

    public Giver(int t,int i){
        super(t,i);
    }

    //metodo a chamar depois de receber a mensgem se target==null
    public synchronized void help(ExplorationAgent ag, EnvironmentObject targ) {
        //responde a mensagem
        //verifica se foi escolhido
        //caso seja:
        helping=ag; //para que o agente ajudado possa ganhar pontos
        target=new Int2D((int)targ.loc.x,(int)targ.loc.y);
        if (helping instanceof Matcher){
        	//System.out.print("match!!!!");
            ((Matcher) helping).matches[id][1]++;
            ((Matcher) helping).helpedTotal--;}
    }
}
