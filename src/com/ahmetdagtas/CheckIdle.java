package com.ahmetdagtas;

import javax.sound.sampled.*;
import java.awt.AWTException;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Timestamp;

public class CheckIdle extends Thread {
    private Robot robot;
    private double threshHold = 0.05;
    private int activeTime;
    private int idleTime;
    private boolean idle;
    private Rectangle screenDimenstions;

    public CheckIdle(int activeTime, int idleTime) {
        this.activeTime = activeTime;
        this.idleTime = idleTime;

        // Get the screen dimensions
        // MultiMonitor support.
        int screenWidth = 0;
        int screenHeight = 0;

        GraphicsEnvironment graphicsEnv = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice[] graphicsDevices = graphicsEnv.getScreenDevices();

        for (GraphicsDevice screens : graphicsDevices) {
            DisplayMode mode = screens.getDisplayMode();
            screenWidth += mode.getWidth();

            if (mode.getHeight() > screenHeight) {
                screenHeight = mode.getHeight();
            }
        }

        screenDimenstions = new Rectangle(0, 0, screenWidth, screenHeight);

        // setup the robot.
        robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e1) {
            e1.printStackTrace();
        }

        idle = false;
    }

    public void run() {
        while (true) {
            BufferedImage screenShot = robot
                    .createScreenCapture(screenDimenstions);

            try {
                Thread.sleep(idle ? idleTime : activeTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedImage screenShot2 = robot
                    .createScreenCapture(screenDimenstions);

            if (compareScreens(screenShot, screenShot2) < threshHold) {
                idle = true;
                System.out.println("idle " + new Timestamp(System.currentTimeMillis()).toString());
                try {
                    playSound("sound.wav");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            } else {
                idle = false;
                System.out.println("active " + new Timestamp(System.currentTimeMillis()).toString());
            }
        }
    }

    private double compareScreens(BufferedImage screen1, BufferedImage screen2) {
        int counter = 0;
        boolean changed = false;

        // Count the amount of change.
        for (int i = 0; i < screen1.getWidth() && !changed; i++) {
            for (int j = 0; j < screen1.getHeight(); j++) {
                if (screen1.getRGB(i, j) != screen2.getRGB(i, j)) {
                    counter++;
                }
            }
        }

        return (double) counter
                / (double) (screen1.getHeight() * screen1.getWidth()) * 100;
    }

    public static void main(String[] args) {
        CheckIdle idleChecker = new CheckIdle(9000, 7000);
        idleChecker.run();
    }

    void playSound(String soundFile) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        File f = new File("./" + soundFile);
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
    }
}


// Sources:
// https://stackoverflow.com/questions/2777594/java-checking-if-pc-is-idle
// https://stackoverflow.com/questions/26305/how-can-i-play-sound-in-java
// Sound source: https://www.prokerala.com/downloads/ringtones/download.php?id=51753