package wtf.tatp.meowtils.extension;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;
import wtf.tatp.meowtils.gui.values.BindValue;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;

public abstract class Extension extends Module {
    private final String author;
    private final String moduleName;
    public final Object owner = this;

    protected Extension(String moduleName, @Nullable String author) {
        super(moduleName, Module.Category.Extensions);
        this.tag(Module.ModuleTag.LEGIT);
        this.author = author;
        this.moduleName = moduleName;
    }

    public Object getOwner() {
        return this;
    }

    public void toggle(String name, String config) {
        try {
            this.addToggle(new ToggleValue(name, config, this));
        } catch (Throwable var4) {
            Meowtils.error(this.moduleName + " failed to add toggle component");
            var4.printStackTrace();
        }
    }

    public void check(String name, String config) {
        try {
            this.addCheck(new CheckValue(name, config, this));
        } catch (Throwable var4) {
            Meowtils.error(this.moduleName + " failed to add check component");
            var4.printStackTrace();
        }
    }

    public void slider(String name, double min, double max, double increment, @Nullable String valueType, String config, Class<?> targetType) {
        try {
            this.addSlider(new SliderValue(name, min, max, increment, valueType, config, this, targetType));
        } catch (Throwable var12) {
            Meowtils.error(this.moduleName + " failed to add slider component");
            var12.printStackTrace();
        }
    }

    public void mode(String name, List<String> modes, String config) {
        try {
            this.addMode(new ModeValue(name, modes, config, this));
        } catch (Throwable var5) {
            Meowtils.error(this.moduleName + " failed to add mode component");
            var5.printStackTrace();
        }
    }

    public void text(@Nullable String name, String config) {
        try {
            this.addText(new TextValue(name, config, this));
        } catch (Throwable var4) {
            Meowtils.error(this.moduleName + " failed to add text component");
            var4.printStackTrace();
        }
    }

    public void text(@Nullable String name, String description, String config) {
        try {
            this.addText(new TextValue(name, description, config, this));
        } catch (Throwable var5) {
            Meowtils.error(this.moduleName + " failed to add text component");
            var5.printStackTrace();
        }
    }

    public void color(String name, @Nonnull ColorLink link) {
        try {
            this.addColor(new ColorValue(name, link));
        } catch (Throwable var4) {
            Meowtils.error(this.moduleName + " failed to add color component");
            var4.printStackTrace();
        }
    }

    public void saturation(@Nonnull ColorLink link) {
        try {
            this.addSaturation(new SaturationValue(link));
        } catch (Throwable var3) {
            Meowtils.error(this.moduleName + " failed to add saturation component");
            var3.printStackTrace();
        }
    }

    public void brightness(@Nonnull ColorLink link) {
        try {
            this.addBrightness(new BrightnessValue(link));
        } catch (Throwable var3) {
            Meowtils.error(this.moduleName + " failed to add brightness component");
            var3.printStackTrace();
        }
    }

    public void opacity(String name, String config) {
        try {
            this.addOpacity(new OpacityValue(name, config, this));
        } catch (Throwable var4) {
            Meowtils.error(this.moduleName + " failed to add opacity component");
            var4.printStackTrace();
        }
    }

    @Deprecated
    public void opacity(String name, String config, ColorLink link) {
        try {
            this.addOpacity(new OpacityValue(name, config, this));
        } catch (Throwable var5) {
            Meowtils.error(this.moduleName + " failed to add opacity component");
            var5.printStackTrace();
        }
    }

    public void info(String name) {
        try {
            if (this.author != null) {
                this.tooltip(name + "\n§9Author: §9" + this.author);
            } else {
                this.tooltip(name);
            }
        } catch (Throwable var3) {
            Meowtils.error(this.moduleName + " failed to add info component");
            var3.printStackTrace();
        }
    }

    public void button(String name, float textScale, Runnable action) {
        try {
            this.addButton(new ButtonValue(name, textScale, action));
        } catch (Throwable var5) {
            Meowtils.error(this.moduleName + " failed to add button component");
            var5.printStackTrace();
        }
    }

    public void bind(String name, String config) {
        try {
            this.addBind(new BindValue(name, config, this));
        } catch (Throwable var4) {
            Meowtils.error(this.moduleName + " failed to add bind component");
            var4.printStackTrace();
        }
    }

    public void expand(String name, Consumer<ExpandValue> builder) {
        try {
            this.addExpand(new ExpandValue(name, builder, this));
        } catch (Throwable var4) {
            Meowtils.error(this.moduleName + " failed to add expand component");
            var4.printStackTrace();
        }
    }

    public ColorLink linkColor(String redConfig, String greenConfig, String blueConfig) {
        return new ColorLink(redConfig, greenConfig, blueConfig, this);
    }

    public static void registerModule(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null");
        } else if (ModuleManager.getModules().contains(module)) {
            throw new IllegalStateException("Duplicate module name: " + module);
        } else {
            ModuleManager.register(module);
            ExtensionManager.EXTENSION_MODULES.add(module);
        }
    }

    public static void registerEvent(Object listener) {
        try {
            EventManager.register(listener);
            ExtensionManager.EXTENSION_LISTENERS.add(listener);
        } catch (Throwable var2) {
            Meowtils.error("Unable to register event " + listener);
            var2.printStackTrace();
        }
    }
}
