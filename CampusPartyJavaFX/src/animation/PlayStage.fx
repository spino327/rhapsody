/*
 * Play.fx
 *
 * Created on 10-jul-2009, 6:25:37
 */

package animation;

import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.ListIterator;
import animation.AbstractScene;
import javafx.stage.StageStyle;

/**
 * @author sergio
 */

public class PlayStage extends Stage {


    var currentScene: AbstractScene on replace {
        println("cambio la scene");
        this.scene = currentScene;
    };
    var iteratorScene: ListIterator;

    init {

        //creando las scenas de la pelicula
        var arrayScene: ArrayList = new ArrayList();
        arrayScene.add(AbstractScene.getScene1(this));

        iteratorScene = arrayScene.listIterator();
        
    }


    public function start(): Void {
        println("Start in PlayStage");
        var intro: IntroAnimation = AbstractScene.getIntroAnimation(this);
        currentScene = intro;
        intro.start();
    }

    public function next(): Void {

        println("next in PlayStage");

        if(iteratorScene.hasNext()) {
            currentScene = iteratorScene.next() as AbstractScene;
//            this.scene = currentScene;
            currentScene.start();
        }

    }

    public function previous(): Void {
        
        println("previous in PlayStage");

        if(iteratorScene.hasPrevious()) {
            currentScene = iteratorScene.previous() as AbstractScene;
//            this.scene = currentScene;
            currentScene.start();
        }
    }

}


function run(args:String[])
{
    var play: PlayStage = PlayStage{
            style: StageStyle.TRANSPARENT;
            width: 600;
            height: 400;
        };

    play.start();
}
