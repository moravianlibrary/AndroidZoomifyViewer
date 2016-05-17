package cz.mzk.tiledimageview.demonstration;

import cz.mzk.tiledimageview.TiledImageView;

/**
 * Created by Martin Řehánek on 17.5.16.
 */
public class Utils {

    public static String toSimplerString(TiledImageView.ViewMode viewMode) {
        switch (viewMode) {
            case FIT_TO_SCREEN:
                return "FIT_IN_CONTAINER";
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_TOP:
                return "FILL_CONTAINER_ALIGN_LEFT_TOP";
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_CENTER:
                return "FILL_CONTAINER_ALIGN_LEFT_CENTER";
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_BOTTOM:
                return "FILL_CONTAINER_ALIGN_LEFT_BOTTOM";
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_TOP:
                return "FILL_CONTAINER_ALIGN_CENTER_TOP";
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_CENTER:
                return "FILL_CONTAINER_ALIGN_CENTER_CENTER";
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_BOTTOM:
                return "FILL_CONTAINER_ALIGN_CENTER_BOTTOM";
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_TOP:
                return "FILL_CONTAINER_ALIGN_RIGHT_TOP";
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_CENTER:
                return "FILL_CONTAINER_ALIGN_RIGHT_CENTER";
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_BOTTOM:
                return "FILL_CONTAINER_ALIGN_RIGHT_BOTTOM";
            default:
                return "";
        }
    }

}
