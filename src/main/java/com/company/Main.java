package com.company;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.util.Pair;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.List;

public class Main {

    private static Image makeScreenshotBetween(Point loc1, Point loc2) throws java.awt.image.RasterFormatException {
        BufferedImage image = null;
        try {
            image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            Point diff = new Point((int) (loc2.getX() - loc1.getX()), (int) (loc2.getY() - loc1.getY()));
            if (diff.getX() <= 0 || diff.getY() <= 0) {
                return null;
            }
            return image.getSubimage(
                (int)loc1.getX(),
                (int)loc1.getY(),
                (int)diff.getX(),
                (int)diff.getY()
            );
        } catch (RasterFormatException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Point getMousePosition() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(
            img.getWidth(null),
            img.getHeight(null),
            BufferedImage.TYPE_INT_ARGB
        );

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    private static Answer[] loadAnswers() throws FileNotFoundException {
        Gson gson = new Gson();
        return gson.fromJson(new JsonReader(new FileReader("answers.json")), Answer[].class);
    }

    private static Pair<Answer, String> tryToFindAnyQuestionAnswer(Answer[] answers, String text) {
        Set<String> textWords = new HashSet<String>(Arrays.asList(text.split(" ")));
        int maxIntersection = 0;
        Answer bestAnswer = null;
        for (Answer answer : answers) {
            Set<String> questionWords = new HashSet<String>(Arrays.asList(answer.question.toLowerCase().split(" ")));
            Set<String> intersection = new HashSet<String>(textWords);
            intersection.retainAll(questionWords);
            int intersectionSize = intersection.size();
            if (intersectionSize > maxIntersection) {
                maxIntersection = intersectionSize;
                bestAnswer = answer;
            }
        }
        if (bestAnswer != null) {
            String shortBestAnswer = typeLeastRequiredLetters(bestAnswer.options ,bestAnswer.answer);
            return new Pair<Answer, String>(
                bestAnswer,
                shortBestAnswer
            );
        } else {
            return new Pair<Answer, String>(null, text + "?");
        }
    }

    private static String typeLeastRequiredLetters(List<String> options, String correct) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < correct.length(); i++) {
            sb.append(correct.charAt(i));
            boolean noneWithSameChar = true;
            for (int j = 0; j < options.size(); j++) {
                String anotherStr = options.get(j);
                if (!anotherStr.equals(correct) && anotherStr.length() > i
                        && anotherStr.charAt(i) == correct.charAt(i)) {
                    noneWithSameChar = false;
                    break;
                }
            }
            if (noneWithSameChar) {
                break;
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        final HintWindow window = new HintWindow();
        window.createWindow();

        final Clock clock = new Clock();

        final int min = 60000;
        final int anHour = 60 * min;

        final ITesseract instance = new Tesseract();  // JNA Interface Mapping
        instance.setDatapath("tessdata"); // path to tessdata directory
        instance.setLanguage("RUS");

        final Runnable runner = new Runnable() {
            public void run() {
                int delay = 20000;
                window.setStatus(HintWindow.HintWindowStatus.STARTED);
                for (int i = 0; i < anHour / delay; i++) {
                    try {
                        System.out.println("Running a thread...");
                        clock.update();
                        Answer[] answers = loadAnswers();
                        window.setStatus(HintWindow.HintWindowStatus.WAITING);
                        Thread.sleep(2500);
                        Point pos1 = getMousePosition();
                        window.setStatus(HintWindow.HintWindowStatus.FIRST_TAKEN);
                        Thread.sleep(5000);
                        Point pos2 = getMousePosition();
                        window.setStatus(HintWindow.HintWindowStatus.SECOND_TAKEN);
                        Image image = makeScreenshotBetween(pos1, pos2);
                        if (image != null) {
                            String parsedText = instance.doOCR(toBufferedImage(image)).toLowerCase();
                            Pair<Answer, String> result = tryToFindAnyQuestionAnswer(answers, parsedText);
                            if (result.getKey() != null) {
                                window.setHint(result.getValue(), result.getKey().answer);
                                window.setStatus(HintWindow.HintWindowStatus.RESULT);
                            } else {
                                window.setHint("?", "?");
                                window.setStatus(HintWindow.HintWindowStatus.RESULT);
                            }
                            Thread.sleep(12500); // MAKE LOOP HERE
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (RasterFormatException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread watchingThread = new Thread(new Runnable() {
            public void run() {
                int delay = 5000;
                for (int i = 0; i < anHour / delay; i++) {
                    try {
                        System.out.println("watching...");
                        if (clock.hasExpired()) {
                            System.out.println("had to restart a runner thread");
                            new Thread(runner).start();
                        }
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        watchingThread.start();
    }
}
