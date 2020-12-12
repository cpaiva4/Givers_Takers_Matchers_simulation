package mas.objects;

import java.awt.Color;

import sim.portrayal.Portrayal;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 * Bruno Leão
 * Carlos Paiva
 */

public class EnvironmentObject {

    public int type1;
    public int type2;
    public Color color;
    public double size;
    public Double2D loc;

    public EnvironmentObject(){};


    public EnvironmentObject(Double2D l, int type1, int type2){
        this.loc = l;
        this.size=5;
        switch (type1){
            case 0:
                color=new Color(255, 0, 0);
                break;
            case 1:
                color=new Color(0, 255, 0);
                break;
            default:
                color=new Color(0, 0, 255);
                break;}
    }

    public Double2D getLoc() {
        return loc;
    }

    public Portrayal getPortrayal(){
        return new RectanglePortrayal2D(this.color, this.size);
    }
}
