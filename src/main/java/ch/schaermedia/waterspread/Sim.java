/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.schaermedia.waterspread;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

/**
 *
 * @author Quentin
 */
public class Sim extends PApplet {

    private final float DEPTH_MIN = -127;
    private final float DEPTH_MAX = 127;

    private final int NUMBER_OF_LAYERS = 2;
    private final int VALUE_LAYER_IDX = 0;
    private final int DIFF_LAYER_IDX = 1;

    private int cols;
    private int rows;

    private float zoom;

    //grids: [layer][x][y]
    private float[][][] grid;

    @Override
    public void draw() {

        background(0);

        PGraphics gr = createGraphics(cols, rows);
        gr.beginDraw();
        gr.loadPixels();

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                calculate(x, y);
            }
        }
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                update(x, y);
                renderToGraphic(gr, x, y);
            }
        }

        gr.updatePixels();
        gr.endDraw();

        scale(zoom);
        image(gr, 0, 0);
    }

    private float toWithinValueRange(float value) {
      if(value > DEPTH_MAX){
        return DEPTH_MAX;
      }

      if(value < DEPTH_MIN) {
        return DEPTH_MIN;
      }

      return value;
    }

    private void clickModifyCoordinate(int x, int y, MouseEvent event) {
      float add = 0;

      if (event.getButton() == LEFT) {
        add = 50;
      } else {
        add = -50;
      }

      grid[DIFF_LAYER_IDX][x][y] = toWithinValueRange(grid[DIFF_LAYER_IDX][x][y] + add);
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (mouseX < 0 || mouseX >= width || mouseY < 0 || mouseY >= height) {
            return;
        }
        int mx = (int) Math.floor(mouseX / zoom);
        int my = (int) Math.floor(mouseY / zoom);

        int radius = 3;

        for (int x = mx - radius; x <= mx + radius; x++) {
            for (int y = my - radius; y <= my + radius; y++) {
              if (x < 0 || x >= cols || y < 0 || y >= rows) {
                  continue;
              }

              clickModifyCoordinate(x, y, event);
          }
        }
    }

    public void mousePressed(MouseEvent event) {
        if (mouseX < 0 || mouseX >= width || mouseY < 0 || mouseY >= height) {
            return;
        }
        int x = (int) Math.floor(mouseX / zoom);
        int y = (int) Math.floor(mouseY / zoom);

       clickModifyCoordinate(x, y, event);
    }

    private void renderToGraphic(PGraphics g, int x, int y) {
        //float value = (toUpdate[VALUE_LAYER_IDX][x][y]  + toCalculate[VALUE_LAYER_IDX][x][y]) / 2;
        float value = grid[VALUE_LAYER_IDX][x][y];
        int idx = x + y * cols;
        if (value < DEPTH_MIN) {
            // Render pixels with a negative (invalid) value in blue
            g.pixels[idx] = color(0, 0, 255);
        } if (value > DEPTH_MAX) {
            // Render pixels with a negative (invalid) value in red
            g.pixels[idx] = color(255, 0, 0);
        } //else if (value - grid[AVERAGE_LAYER_IDX][x][y] < 0) {
        // For debugging render pixels with a lower than average value in yellow
        //            g.pixels[idx] = color(255, 255, 0);
        //}
        else {
            // Render the pixel in a rage from black to grey
            g.pixels[idx] = color(-DEPTH_MIN + value);
        }
    }

    private void update(int x, int y) {
        grid[VALUE_LAYER_IDX][x][y] = toWithinValueRange(grid[VALUE_LAYER_IDX][x][y] + grid[DIFF_LAYER_IDX][x][y]);
        grid[DIFF_LAYER_IDX][x][y] = 0;
    }

    private void calculate(int centerX, int centerY) {
        float value = grid[VALUE_LAYER_IDX][centerX][centerY];

        int range = 3;
        int neighborsInRange = 0;
        float perNeighborDiff  = (value * 0.005);

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                if (x == 0 && y == 0) {
                  continue;
                }

                int cx = centerX + x;
                int cy = centerY + y;

                if (cx < 0 || cy < 0 || cx > cols - 1 || cy > rows - 1) {
                    continue;
                }

                neighborsInRange++;

                grid[DIFF_LAYER_IDX][cx][cy] += perNeighborDiff;
            }
        }

        grid[DIFF_LAYER_IDX][centerX][centerY] -= perNeighborDiff * neighborsInRange;
    }

    @Override
    public void settings() {
        size(800, 800);
    }

    @Override
    public void setup() {
        cols = 150;
        rows = 150;
        zoom = (float) Math.sqrt((width * height) / (cols * rows));

        System.out.println("zoom: " + zoom);

        grid = new float[NUMBER_OF_LAYERS][cols][rows];

        frameRate(60);
    }

    public static void main(String[] args) {
        PApplet.main(Sim.class,
                args);
    }

}
