package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Card;
import model.ConcentrationModel;
import model.Observer;

import java.lang.reflect.Array;
import java.util.*;

/**
 * The ConcentrationGUI application is the UI for Concentration.
 *
 * @author Giovanni Coppola
 */
public class ConcentrationGUI extends Application
        implements Observer< ConcentrationModel, Object > {

    /**
     * Private state variables for the ConcentrationGUI class:
     * Includes variables for the size of the array, the array of images, the model, the labels, and the different panes
     */
    private final static int size = 4;
    private Image[] pokemonImages = {new Image(getClass().getResourceAsStream("resources/pokeball.png")),
            new Image(getClass().getResourceAsStream("resources/abra.png")),
            new Image(getClass().getResourceAsStream("resources/bulbasaur.png")),
            new Image(getClass().getResourceAsStream("resources/charmander.png")),
            new Image(getClass().getResourceAsStream("resources/jigglypuff.png")),
            new Image(getClass().getResourceAsStream("resources/meowth.png")),
            new Image(getClass().getResourceAsStream("resources/pikachu.png")),
            new Image(getClass().getResourceAsStream("resources/squirtle.png")),
            new Image(getClass().getResourceAsStream("resources/venomoth.png"))};
    private ArrayList<Button> pokemonButtonsInGrid = new ArrayList<>();
    private ConcentrationModel model;
    private Label topLabel;
    private BorderPane borderPane;
    private GridPane gridPane;
    private Label bottomBorderLabel;
    private GridPane cheatPane;
    private Stage cheatStage;

    /**
     * init method to assign model to a new concentration model and add an observer
     */
    @Override
    public void init() {
        this.model = new ConcentrationModel();
        this.model.addObserver(this);
    }

    /**
     * start method to set the stage and the scene for the initial state of the concentration puzzle
     *
     * @param stage - the stage that will be shown for the initial state
     * @throws Exception - throws an exception if there is one
     */
    @Override
    public void start( Stage stage ) throws Exception {
        // Create the border pane, grid pane, and HBox
        borderPane = new BorderPane();
        BorderPane bottomBorder = new BorderPane();
        gridPane = new GridPane();
        HBox bottomBox = new HBox();

        // Set the label for the top of the border pane
        topLabel = new Label("Select the first card.");
        borderPane.setTop(topLabel);
        int count = 0;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                // Create a new button for the grid pane
                Button button = new Button();
                int finalCount = count;
                button.setOnAction(event ->
                        // Add an event to select the card when it is pressed
                        this.model.selectCard(finalCount));
                count++;
                // Add the button to the array list of buttons
                pokemonButtonsInGrid.add(button);

                // Set the image of the button and resize it (to fit my screen)
                ImageView view = new ImageView(pokemonImages[0]);
                view.setFitHeight(75);
                view.setFitWidth(75);
                button.setGraphic(view);

                // Add the pane to the grid
                gridPane.add(button, col, row);
            }
        }

        // Set the grid pane into the center of the border pane
        borderPane.setCenter(gridPane);

        // Set the reset button for the HBox in the border pane
        Button reset = new Button("Reset");
        // Reset the grid and set the label of the top
        reset.setOnAction(event -> {this.model.reset(); topLabel.setText("Select the first card.");});
        bottomBox.getChildren().add(reset);

        // Set the undo button for the HBox in the border pane
        Button undo = new Button("Undo");
        undo.setOnAction(event -> this.model.undo());
        bottomBox.getChildren().add(undo);

        // Set the cheat button for the HBox in the border pane
        Button cheat = new Button("Cheat");
        // Show the stage with all the cards now flipped
        cheat.setOnAction(event -> {this.model.cheat(); cheatStage.show();});
        bottomBox.getChildren().add(cheat);

        // Set the bottom of the bottom pane to add the HBox and number of moves
        bottomBox.setAlignment(Pos.CENTER);
        bottomBorder.setCenter(bottomBox);
        bottomBorderLabel = new Label(this.model.getMoveCount() + " Moves");
        bottomBorder.setRight(bottomBorderLabel);
        borderPane.setBottom(bottomBorder);

        // Make the new scene with the border pane in it and show the stage
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setTitle("Concentration");
        stage.setResizable(false);
        stage.show();
    }



    /**
     * Method to update the states of the puzzle after every event
     *
     * @param concentrationModel - the model that will be used
     * @param o - Object to determine if the user is cheating or not
     */
    @Override
    public void update( ConcentrationModel concentrationModel, Object o ) {
        // Get the cards from the model (current state of the grid)
        ArrayList<Card> cards = model.getCards();
        for (int index = 0; index < 16; index++) {
            if (cards.get(index).isFaceUp()) {
                // If the card is face up, set the image of the button to the correct pokemon
                ImageView image = new ImageView(pokemonImages[cards.get(index).getNumber()+1]);
                image.setFitWidth(75);
                image.setFitHeight(75);
                pokemonButtonsInGrid.get(index).setGraphic(image);
            } else {
                // if the card is face down, set the image of the card to a pokeball
                ImageView image = new ImageView(pokemonImages[0]);
                image.setFitWidth(75);
                image.setFitHeight(75);
                pokemonButtonsInGrid.get(index).setGraphic(image);
            }

            // Reset the move counter
            bottomBorderLabel.setText(this.model.getMoveCount() + " Moves");
        }

        // Switch statement to determine what the top header will be based on the number of current face up cards
        switch(this.model.howManyCardsUp()) {
            case 2:
                topLabel.setText("No Match: Undo or select a card.");
                break;
            case 0:
                topLabel.setText("Select the first card.");
                break;
            case 1:
                topLabel.setText("Select the second card.");
                break;
        }

        // Boolean to determine if there are face up cards
        boolean uncoveredCard = false;
        for (Card card : cards) {
            if (card.isFaceUp()) {
                uncoveredCard = true;
            } else{
                uncoveredCard = false;
            }
        }

        if (uncoveredCard) {
            // Set the label to be YOU WIN if there are only face up cards
            topLabel.setText("YOU WIN");
        }

        if (o != null) {
            // If the user wants to cheat, then create a new stage and grid pane
            cards = model.getCheat();
            cheatStage = new Stage();
            cheatPane = new GridPane();
            for (int value = 0; value < 16; value++) {
                // Loop through and set the image of each button to their corresponding flipped pokemon
                Button cheatButton = new Button();
                ImageView image = new ImageView(pokemonImages[cards.get(value).getNumber()+1]);
                image.setFitWidth(75);
                image.setFitHeight(75);
                cheatButton.setGraphic(image);
                cheatPane.add(cheatButton, value%size, value/size);
            }

            // Create the new scene and add it to the stage, but hide the stage. It will only be shown from the event
            //      handler for the cheat button, in start
            Scene cheatScene = new Scene(cheatPane);
            cheatStage.setScene(cheatScene);
            cheatStage.setTitle("Cheat window");
            cheatStage.hide();
        }
    }

    /**
     * main entry point launches the JavaFX GUI.
     *
     * @param args not used
     */
    public static void main( String[] args ) {
        Application.launch( args );
    }
}
