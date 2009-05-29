/*
 * SimpleMediaPlayer.fx
 *
 * Created on 29-may-2009, 16:18:40
 */

package org.u2u.gui.scene.extra;

import javafx.scene.control.Control;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaError;
import javafx.scene.media.Media;
/**
 * @author sergio
 */



public class SimpleMediaPlayer extends Control {

  public var source: String on replace {
    player.media.source = source;
  }

  var player: MediaPlayer = MediaPlayer {
    autoPlay:true;
    media: Media {
      source: source
    }

    onError: function( me:MediaError ) {
        println( "Error Occurred: {me.message}" );
    }

    onEndOfMedia: function() {
      player.stop();
    }
  }

  override function create() {


    MediaPlayerControls {

      width: bind width
      isPlaying: bind player.status == MediaPlayer.PLAYING
      mediaDuration: bind player.media.duration
      currentTime: bind player.currentTime with inverse

      onTogglePlayPause: function( play:Boolean ) {
        if (play) {
          player.play();
        } else {
          player.pause();
        }
      }
    }
  }
}
