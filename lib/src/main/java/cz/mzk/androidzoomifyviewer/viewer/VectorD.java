package cz.mzk.androidzoomifyviewer.viewer;

/**
 * @author Martin Řehánek
 */
public class VectorD {

    public static final VectorD ZERO_VECTOR = new VectorD(0.0, 0.0);
    public final double x;
    public final double y;

    public VectorD(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static VectorD sum(VectorD... vectors) {
        double x = 0.0;
        double y = 0.0;
        for (VectorD vector : vectors) {
            x += vector.x;
            y += vector.y;
        }
        return new VectorD(x, y);
    }

    public VectorD plus(double x, double y) {
        return new VectorD(this.x + x, this.y + y);
    }

    public VectorD plus(VectorD newVector) {
        return new VectorD(this.x + newVector.x, this.y + newVector.y);
    }

    public VectorD minus(VectorD newVector) {
        return new VectorD(this.x - newVector.x, this.y - newVector.y);
    }

    public VectorD plusX(double x) {
        return new VectorD(this.x + x, this.y);
    }

    public VectorD plusY(double y) {
        return new VectorD(this.x, this.y + y);
    }

    public String toString() {
        return String.format("[%.2f;%.2f]", x, y);
    }

    public double getSize() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

}
