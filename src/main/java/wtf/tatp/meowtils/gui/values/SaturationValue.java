package wtf.tatp.meowtils.gui.values;

import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.component.ModuleComponent;
import wtf.tatp.meowtils.gui.component.subcomponents.SaturationComponent;

public class SaturationValue {
    private final double min = 0.0;
    private final double max = 100.0;
    private final ColorLink link;

    public SaturationValue(ColorLink link) {
        this.link = link;
    }

    public double get() {
        return this.link.getSaturation() * 100.0;
    }

    public void set(double newSaturation) {
        double clamped = this.clamp(newSaturation, 0.0, 100.0);
        float s = (float)(clamped / 100.0);
        this.link.apply(this.link.getHue(), s, this.link.getBrightness());
    }

    public ColorLink getLink() {
        return this.link;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public SaturationComponent createComponent(ModuleComponent parent, int y) {
        return new SaturationComponent(this, parent, y);
    }
}
