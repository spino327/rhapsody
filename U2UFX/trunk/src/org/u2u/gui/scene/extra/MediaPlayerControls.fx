/*
 * MediaPlayerControls.fx
 *
 * Created on 29-may-2009, 16:09:58
 */

package org.u2u.gui.scene.extra;

import javafx.scene.CustomNode;
import javafx.scene.image.*;
import javafx.scene.effect.*;
import javafx.scene.input.*;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.*;
/**
 * @author sergio
 */

public class MediaPlayerControls extends CustomNode {

  public var width = 100.0;
  public var isPlaying = false;
  public var mediaDuration: Duration;

  public var currentTime: Duration;
  public var onTogglePlayPause: function( play:Boolean ):Void;

  var percentComplete = bind currentTime.toSeconds() /
        mediaDuration.toSeconds();
  var height = bind playImg.height;

  def CONTROLS_PADDING = 5;
  def CONTROLS_SPACING = 10;

  def playImg = Image {
    url: "{__DIR__}resources/play.png"
  }

  def pauseImg = Image {
    url: "{__DIR__}resources/pause.png"
  }

  def playPauseButton: ImageView = ImageView {
    var hoverEffect = Glow {}

    image: bind if (isPlaying) pauseImg else playImg
    effect: bind if (playPauseButton.hover) hoverEffect else null
    onMousePressed: function( me:MouseEvent ) {
      onTogglePlayPause( isPlaying == false );
    }
  }

  def progressGroup = Group {

    def progressWidth = bind {
      width - (playImg.width + CONTROLS_SPACING + 2 *CONTROLS_PADDING)
    }

    content: [

      Rectangle {
        //id: "progressBackground"
        fill: Color.SNOW;
        width: bind progressWidth
        height: bind height
      },
      Rectangle {
        fill: Color.CYAN;
        width: bind  percentComplete * progressWidth
        height: bind height
      },
      Line {
        fill: Color.YELLOW;
        startY: 1
        endY: bind height - 1
        startX: bind percentComplete * progressWidth
        endX: bind percentComplete * progressWidth
      }
    ]

    onMouseDragged: function(e) {

        var newTime = mediaDuration * e.x / progressWidth;
        currentTime = if (newTime < mediaDuration) newTime else
        mediaDuration - 1ms;
     }
    onMousePressed: function(e) {
      currentTime = mediaDuration * e.x / progressWidth
    }
  }

  override function create() {
    Group {
      content: [
        HBox {
          translateX: CONTROLS_PADDING
          translateY: CONTROLS_PADDING
          spacing: CONTROLS_SPACING
          content: [
            playPauseButton,
            progressGroup
          ]
        }
      ]
    }
  }
}


