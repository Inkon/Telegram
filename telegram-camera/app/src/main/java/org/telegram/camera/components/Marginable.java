package org.telegram.camera.components;

/**
 * Interface for components that can be placed on a {@link FrameLayoutWithMargin}. Their margin will be set according to
 * their height and value of {@link Marginable#getMarginPart()}
 *
 * @author Danil Kolikov
 */
public interface Marginable {
    /**
     * Get value of margin. If it's equals to 1, then margin from each size of a component will be equal to height of a
     * component. If 0.5 then 0.5 height of component and so on
     *
     * @return Value of ratio
     * @see org.telegram.camera.utils.ComponentUtils#countMargin(int, float)
     */
    float getMarginPart();
}
