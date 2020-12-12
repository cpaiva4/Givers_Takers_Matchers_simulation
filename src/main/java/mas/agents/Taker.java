package mas.agents;

import mas.objects.EnvironmentObject;
import sim.util.Int2D;

/**
 * Bruno Leão
 * Carlos Paiva
 */
public class Taker extends ExplorationAgent {
    public Taker(int t, int i){
        super(t,i);
    }

    @Override
    public synchronized void help(ExplorationAgent ag, EnvironmentObject targ) {

    }

    //metodo a chamar depois de receber a mensgem se target==null
    public void help(double ratio, ExplorationAgent ag, EnvironmentObject targ) {
        if (Math.random()<ratio){
            //responde � mensagem
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
}
