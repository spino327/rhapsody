/*
 * Scene2.fx
 *
 * Created on 10-jul-2009, 8:31:54
 */

package animation;

import javafx.scene.paint.Color;

import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.Node;
/**
 * @author sergio
 */

public class Scene2 extends AbstractScene {

    var mitHeadImg: Image;
    var fanHeadImg:Image;
    var fanBodyImg:Image;
    var otherHeadImg:Image;
    var mitBodyImg:Image;
    var otherBodyImg:Image;

    var fanHead:ImageView;
    var otherHead:ImageView;
    var bodyOthers:Node;
    var fanBody:ImageView;

    init {
        mitHeadImg = Image{ url:"{__DIR__}resources/mitnick.png"};
        fanHeadImg = Image{ url:"{__DIR__}resources/hoover-left.png"};
        
        mitBodyImg = Image{ url:"{__DIR__}resources/bodyMit.png" };
        otherBodyImg = Image{ url:"{__DIR__}resources/bodyOther.png" };
        fanBodyImg = Image{ url:"{__DIR__}resources/body.png" };
        this.title = "7:00 PM Campus Party, Colombia..."
    }


    override function start() {

        //
        this.fill = Color.DARKGRAY;
        this.content = Group {

                    content: [

                          
                           ImageView {

                                translateX:140
                                translateY:15
                                image:mitBodyImg
                           },
                           //group the people
                           Group{
                                content: [
                                    ImageView{
                                        image:otherBodyImg
                                        translateY:50
                                    },
                                    ImageView{
                                        image:otherBodyImg
                                        translateX:40
                                        translateY:60
                                    },
                                    ImageView{
                                        image:otherBodyImg
                                        translateX:55
                                        translateY:60
                                    },
                                    ImageView{
                                        image:otherBodyImg
                                        translateX:65
                                        translateY:50
                                    }
                                ]
                           },
                           
                           ImageView{
                                translateX:200
                                translateY:125
                                image:fanBodyImg
                           },
                           ImageView{
                                translateX:200
                                translateY:105
                                image:fanHeadImg
                           },

                    ]

                }
         }

}

