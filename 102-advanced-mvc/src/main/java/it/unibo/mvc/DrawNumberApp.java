package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private int min;
    private int max;
    private int attempts;

    private final DrawNumber model;
    private final List<DrawNumberView> views;
   
    private void loadConfiguration() {
        InputStream config = ClassLoader.getSystemResourceAsStream("config.yml");
        try(final BufferedReader br = new BufferedReader(new InputStreamReader(config))) {
            int nProperties = 3;
            final List<Integer> properties = new ArrayList();
            for(int i=0;i < nProperties; i++){
                String line = br.readLine();
                final String[] lineSplitted = line.split(": ");
                properties.add(Integer.parseInt(lineSplitted[1]));
            }
            min = properties.get(0);
            max = properties.get(1);
            attempts = properties.get(2);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * @param views
     *            the views to attach
     * @throws IOException
     */
    public DrawNumberApp(final DrawNumberView... views){
        loadConfiguration();

        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        this.model = new DrawNumberImpl(min, max, attempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
