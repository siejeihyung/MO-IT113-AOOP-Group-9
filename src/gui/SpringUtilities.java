/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for creating compact grid layouts using SpringLayout.
 * This helps to position components in a grid-like structure with equal spacing.
 */
public class SpringUtilities {

    // Creates a compact grid of components within a container using SpringLayout
    public static void makeCompactGrid(Container parent, int rows, int cols,
                                       int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout;

        // Ensure the container is using SpringLayout, or throw an error
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            throw new IllegalArgumentException("Container must use SpringLayout.");
        }

        // Create Spring constants for padding and initial positions
        Spring xPadSpring = Spring.constant(xPad);
        Spring yPadSpring = Spring.constant(yPad);
        Spring initialXSpring = Spring.constant(initialX);
        Spring initialYSpring = Spring.constant(initialY);
        int max = rows * cols;

        // Determine the maximum width and height among all components
        Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
        Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0)).getHeight();

        for (int i = 1; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
            maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
            maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
        }

        // Set all components to have the same width and height (uniform sizing)
        for (int i = 0; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
            cons.setWidth(maxWidthSpring);
            cons.setHeight(maxHeightSpring);
        }

        // Arrange the components in a grid
        SpringLayout.Constraints lastCons = null;
        SpringLayout.Constraints lastRowCons = null;
        for (int i = 0; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
            
            // If at the start of a new row
            if (i % cols == 0) {
                lastRowCons = lastCons;
                cons.setX(initialXSpring); // Align to the left margin
            } else {
                // Position component to the right of the previous one
                cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST), xPadSpring));
            }

            // If in the first row
            if (i / cols == 0) {
                cons.setY(initialYSpring); // Align to the top margin
            } else {
                // Position below the component in the previous row
                cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH), yPadSpring));
            }

            lastCons = cons;
        }

        // Set the parent container size to fit all components plus padding
        SpringLayout.Constraints parentCons = layout.getConstraints(parent);
        parentCons.setConstraint(SpringLayout.SOUTH,
                Spring.sum(Spring.constant(yPad),
                        lastCons.getConstraint(SpringLayout.SOUTH)));
        parentCons.setConstraint(SpringLayout.EAST,
                Spring.sum(Spring.constant(xPad),
                        lastCons.getConstraint(SpringLayout.EAST)));
    }
}
