package UPsay.decouverteAndroid.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.Arrays;
import java.util.Map;

import UPsay.decouverteAndroid.Joystick;
import UPsay.decouverteAndroid.MainActivity;
import UPsay.decouverteAndroid.Mansion;
import UPsay.decouverteAndroid.Player;
import UPsay.decouverteAndroid.R;

public class MapManager {

    //Initialize Maps
    private TileMap currentMap;
    private int previousMapId;
    private Map1 map1;
    private Map2 map2;
    private Map3 map3;
    private Map4 map4;
    private Map5 map5;
    //Initialize Camera
    private double x;
    private double y;
    //camera speed
    public static final double MAX_SPEED = 20;

    //Mediaplayer for doors
    MediaPlayer mediaPlayer;
    private MediaPlayer jumpscare;

    //GameState
    private Mansion mansion;

    public MapManager(Context context, Mansion mansion) {

        this.mansion = mansion;
        //initialize Map
        map1 = new Map1(context);
        map2 = new Map2(context);
        map3 = new Map3(context);
        map4 = new Map4(context);
        map5 = new Map5(context);
        currentMap = map1;
        previousMapId = currentMap.id;
        x = currentMap.startX;
        y = currentMap.startY;

        //Create Mediaplayer for doors
        mediaPlayer = MediaPlayer.create(context, R.raw.door);
        //for jumpscares
        jumpscare = MediaPlayer.create(context, R.raw.jumpscare);
    }
    public void draw(Canvas canvas){
        for (int i = 0; i < currentMap.getLength(); i++) {
            for (int j = 0; j < currentMap.getLength(); j++) {
                if(previousMapId != currentMap.id){
                    previousMapId = currentMap.id;
                    return;
                }
                int tileId = currentMap.getId(i,j);
                Bitmap tile = currentMap.getBmp(tileId);

                // Dessiner la tile à la position (j * tileWidth, i * tileHeight) sur le canvas
                canvas.drawBitmap(tile, j * tile.getWidth()+(float)x, i * tile.getHeight()+(float)y, null);
            }
        }
    }

    public void update(Joystick joystick) {
        double valueX = joystick.getRadiusRateX();
        double valueY = joystick.getRadiusRateY();
        if(!joystick.getPressed()){
            return;
        }
        if(Math.abs(valueX)> Math.abs(valueY)){
            double moveX = joystick.getRadiusRateX()*MAX_SPEED/joystick.getRadius();
            if(canMoveTo(getPxlToIdx(x+moveX*-1,MainActivity.PHONE_WIDTH),getPxlToIdx(y,MainActivity.PHONE_HEIGHT)))
                x=x+moveX*-1;
        }else{
            double moveY = joystick.getRadiusRateY()*MAX_SPEED/joystick.getRadius();
            if(canMoveTo(getPxlToIdx(x,MainActivity.PHONE_WIDTH),getPxlToIdx(y+moveY*-1,MainActivity.PHONE_HEIGHT)))
                y=y+moveY*-1;
        }
        int tileId =currentMap.getId((int)getPxlToIdx(y,MainActivity.PHONE_HEIGHT),(int)getPxlToIdx(x,MainActivity.PHONE_WIDTH));
        //Log.d("TILE","tile n "+tileId);
        if(isIn(TileMap.exitTiles,tileId)){
            changeToRoom(currentMap.updateRoom(tileId));
        }
    }

    private void changeToRoom(int i) {
        mediaPlayer.start();
        switch (i){
            case 1:
                currentMap = map1;
                break;
            case 2:
                currentMap = map2;
                break;
            case 3:
                currentMap = map3;
                break;
            case 4:
                jumpscare.start();
                currentMap = map4;
                break;
            case 5:
                currentMap = map5;
                break;
            case 6:
                mansion.setGameState(Mansion.GameState.GAME_OVER);
                break;
            default:
                currentMap = map1;
        }
        x = currentMap.startX;
        y = currentMap.startY;
    }

    public boolean canMoveTo(double _x, double _y){
        if(_x>49 || _y>49 || _x<0 || _y<0)return false;
        return !isIn(TileMap.boundTiles, currentMap.getId((int) _y , (int) _x ));
    }
    private boolean isIn(int[] key,int value){
        for (int j : key) {
            if (j == value)
                return true;
        }
        return false;
    }
    private double getPxlToIdx(double pxl,double phoneDim){
        return (pxl-phoneDim/2.0)/-96;
    }

    public void restart() {
        changeToRoom(1);
    }
}
