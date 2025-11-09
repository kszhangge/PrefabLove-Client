package dev.diona.southside.gui.hud.islandhud;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.diona.southside.PrefabLove;
import dev.diona.southside.util.render.blur.KawaseBlur;
import dev.diona.southside.util.render.RenderUtil;
import dev.diona.southside.util.render.RoundUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.Minecraft;
import java.awt.*;
import org.lwjgl.nanovg.NanoVG;
import java.util.List;

public class IslandHud extends Hud {
    public final Switch shadow = new Switch("Shadow", "阴影", true);
    public final cc.polyfrost.oneconfig.config.options.impl.Slider bgAlpha = new cc.polyfrost.oneconfig.config.options.impl.Slider("Background Alpha", 60, 0, 255, 1);
    public final cc.polyfrost.oneconfig.config.options.impl.Slider animationSpeed = new cc.polyfrost.oneconfig.config.options.impl.Slider("Animation Speed", 3, 1, 100, 1);
    public final cc.polyfrost.oneconfig.config.options.impl.Slider toggleBallSpeed = new cc.polyfrost.oneconfig.config.options.impl.Slider("Toggle Ball Speed", 2, 1, 100, 1);
    public final cc.polyfrost.oneconfig.config.options.impl.Slider toggleBgSpeed = new cc.polyfrost.oneconfig.config.options.impl.Slider("Toggle BG Speed", 20, 1, 100, 1);
    public final cc.polyfrost.oneconfig.config.options.impl.Slider scaffoldBarSpeed = new cc.polyfrost.oneconfig.config.options.impl.Slider("Scaffold Bar Speed", 5, 1, 100, 1);
    public final cc.polyfrost.oneconfig.config.options.impl.Slider blurStrength = new cc.polyfrost.oneconfig.config.options.impl.Slider("Blur Strength", 5, 0, 30, 1);
    
    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    private float lastTextWidth = 0;
    private float lastTextHeight = 0;
    private final Minecraft mc = Minecraft.getMinecraft();

    private double currentWidth = 0;
    private double currentHeight = 0;
    private double targetWidth = 0;
    private double targetHeight = 0;
    private long animationStartTime = 0;
    private double lastTargetWidth = 0;
    private double lastTargetHeight = 0;
    private boolean isExpanding = false;
    private int bounceCount = 0;
    private long bounceStartTime = 0;
    private boolean wasInNoticeState = false;


    public IslandHud() {
        super(0, 20, 1, 1f);
        position.getValue().x = 0;
        position.getValue().y = 20;
    }

    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        NanoVGHelper nanovg = NanoVGHelper.INSTANCE;

        int fps = Minecraft.getDebugFPS();

        String username = mc.player != null ? mc.player.getName() : "Player";
        String displayText = String.format("%s %s | %s | %d FPS",
                PrefabLove.CLIENT_NAME,
                PrefabLove.CLIENT_VERSION,
                username,
                fps
        );

        boolean isTabActive = TabManager.getInstance().isTabActive();
        boolean isBlinkActive = BlinkManager.getInstance().isBlinkActive();
        boolean isGappleActive = GappleManager.getInstance().isGappleActive();
        boolean isScaffoldActive = ScaManager.getInstance().isScaffoldActive();
        List<Toggle> activeNotifications = (isTabActive || isBlinkActive || isGappleActive || isScaffoldActive) ?
            java.util.Collections.emptyList() : 
            Manager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty();

        double padding = 8;

        if (isTabActive) {
            targetWidth = 200;
            targetHeight = TabManager.getInstance().calculateTabHeight() + 35 ;
        } else if (isBlinkActive) {
            targetWidth = 130;
            targetHeight = 40;
        } else if (isGappleActive) {
            targetWidth = 170;
            targetHeight = 50;
        } else if (isScaffoldActive) {
            targetWidth = lastTextWidth + 60 + 30;
            targetHeight = lastTextHeight + 20;
        } else if (hasNotifications) {
            double notificationContentWidth = calculateNotificationContentWidth(activeNotifications, nanovg);
            targetWidth = notificationContentWidth + padding * 2;

            float itemHeight = 42f;
            float itemSpacing = 5f;
            float totalHeight = activeNotifications.size() * itemHeight + (activeNotifications.size() - 1) * itemSpacing;
            targetHeight = totalHeight + padding * 2;
        } else {
            nanovg.setupAndDraw(false, vg -> {
                lastTextWidth = nanovg.getTextWidth(vg, displayText, 12, Fonts.WQY) - 2.5f;
                lastTextHeight = nanovg.getTextHeight(vg, 12, Fonts.WQY);
            });
            
            targetWidth = lastTextWidth + 60;
            targetHeight = lastTextHeight + 20;
        }

        updateAnimations();

        final double finalWidth;
        final double finalHeight;

        if (animationStartTime > 0) {
            finalWidth = Math.max(currentWidth, getBounceWidth());
            finalHeight = Math.max(currentHeight, getBounceHeight());
        } else {
            finalWidth = currentWidth;
            finalHeight = currentHeight;
        }

        float radius = (hasNotifications || isTabActive || isBlinkActive || isGappleActive || isScaffoldActive) ? 18.0f : 17.5f;

        drawIsland(nanovg, x, y, displayText, activeNotifications, hasNotifications, isTabActive, isBlinkActive, isGappleActive, isScaffoldActive, finalWidth, finalHeight, radius, padding);
    }

    private double calculateNotificationContentWidth(List<Toggle> notifications, NanoVGHelper nanovg) {
        double maxWidth = 0;
        
        for (Toggle notification : notifications) {
            final double[] contentWidth = {0};
            
            nanovg.setupAndDraw(false, vg -> {
                String title = "Module Toggled";
                double titleWidth = nanovg.getTextWidth(vg, title, 12, Fonts.MEDIUM);
                
                String moduleName = notification.getModuleName();
                String hasBeenText = " has been ";
                String statusText = notification.isEnabled() ? "Enabled" : "Disabled";
                String exclamation = "!";
                
                double statusLineWidth = nanovg.getTextWidth(vg, moduleName + hasBeenText + statusText + exclamation, 10.5f, Fonts.MEDIUM);
                
                double switchWidth = 32;
                double switchPadding = 10;
                double leftPadding = 6;
                
                double maxTextWidth = Math.max(titleWidth, statusLineWidth);
                contentWidth[0] = leftPadding + switchWidth + switchPadding + maxTextWidth;
            });
            
            maxWidth = Math.max(maxWidth, contentWidth[0]);
        }
        
        return maxWidth;
    }

    
    private void drawIsland(NanoVGHelper nanovg, float x, float y, String displayText,
                            List<Toggle> activeNotifications, boolean hasNotifications, boolean isTabActive, boolean isBlinkActive, boolean isGappleActive, boolean isScaffoldActive,
                            double finalWidth, double finalHeight, float radius, double padding) {
        
        float blurStrengthValue = blurStrength.getValue().floatValue();
        
        if (blurStrengthValue > 0) {
            stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);
            RoundUtil.drawRound(x, y, (float) finalWidth, (float) finalHeight, radius, Color.WHITE);
            stencilFramebuffer.unbindFramebuffer();
            
            int iterations = Math.max(1, (int)(blurStrengthValue / 10));
            int offset = (int)blurStrengthValue;
            KawaseBlur.renderBlur(stencilFramebuffer.framebufferTexture, iterations, offset);
            
            mc.getFramebuffer().bindFramebuffer(true);
        }
        
        nanovg.setupAndDraw(true, vg -> {
            int bgAlphaValue = Math.max(0, Math.min(255, (int) bgAlpha.getValue().floatValue()));
            
            nanovg.drawRoundedRect(vg, x, y, (float) finalWidth, (float) finalHeight, new Color(0, 0, 0, bgAlphaValue).getRGB(), radius);
            
            if (shadow.getValue()) {
                nanovg.drawDropShadow(vg, x, y, (float) finalWidth, (float) finalHeight, 10, 0F, radius, new Color(0, 0, 0, bgAlphaValue));
            }

            NanoVG.nvgSave(vg);
            NanoVG.nvgIntersectScissor(vg, x, y, (float) finalWidth, (float) finalHeight);

            if (isTabActive) {
                TabRenderer.drawTabList(vg, nanovg, x, y, (float) finalWidth, (float) finalHeight);
            } else if (isBlinkActive) {
                BlinkRenderer.drawBlinkNotification(vg, nanovg, x, y, (float) finalWidth, (float) finalHeight);
            } else if (isGappleActive) {
                GappleRenderer.drawGappleNotification(vg, nanovg, x, y, (float) finalWidth, (float) finalHeight);
            } else if (isScaffoldActive) {
                ScaRenderer.barAnimSpeed = scaffoldBarSpeed.getValue().floatValue();
                ScaRenderer.drawScaffoldNotification(vg, nanovg, x, y, (float) finalWidth, (float) finalHeight);
            } else if (hasNotifications) {
                Renderer.toggleBallSpeed = toggleBallSpeed.getValue().floatValue();
                Renderer.toggleBgSpeed = toggleBgSpeed.getValue().floatValue();
                Renderer.drawMultipleNotifications(activeNotifications, x + (float)padding, y + (float)padding, (float) finalWidth - (float)padding * 2, (float) finalHeight - (float)padding * 2);
            } else {
                float iconSize = 20;
                float iconX = x + 10;
                float iconY = y + (float)(finalHeight - iconSize) / 2;
                nanovg.drawSvg(vg, SVGs.PREFABLOVE, iconX, iconY, iconSize, iconSize, Color.WHITE.getRGB(), 255);
                
                float textX = iconX + iconSize + 8;
                float textHeight = nanovg.getTextHeight(vg, 12, Fonts.WQY);
                float textY = y + (float)finalHeight / 2 + textHeight / 2 - 5.5f;
                nanovg.drawText(vg, displayText, textX, textY, -1, 12, Fonts.WQY);
            }

            NanoVG.nvgRestore(vg);
        });
        
        if (isScaffoldActive && !isTabActive && !isBlinkActive && !isGappleActive) {
            ScaRenderer.drawScaffoldIcon(
                ScaManager.getInstance().getCurrentBlock(),
                x + 10,
                y + (float)(finalHeight - 16) / 2
            );
        }
        
        if (isGappleActive && !isTabActive && !isBlinkActive) {
            GappleRenderer.drawGappleIcon(x, y, (float) finalWidth, (float) finalHeight);
        }
    }
    
    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();

        List<Toggle> activeNotifications = Manager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty();

        if (targetWidth != lastTargetWidth || targetHeight != lastTargetHeight) {
            animationStartTime = currentTime;
            bounceStartTime = currentTime;
            lastTargetWidth = targetWidth;
            lastTargetHeight = targetHeight;
            isExpanding = targetWidth > currentWidth || targetHeight > currentHeight;
            bounceCount = 0;
        }

        wasInNoticeState = hasNotifications;

        double widthDiff = targetWidth - currentWidth;
        double heightDiff = targetHeight - currentHeight;

        if (Math.abs(widthDiff) > 0.5 || Math.abs(heightDiff) > 0.5) {
            double animSpeed = animationSpeed.getValue().floatValue() * 0.01;
            currentWidth += widthDiff * animSpeed;
            currentHeight += heightDiff * animSpeed;
        } else {
            currentWidth = targetWidth;
            currentHeight = targetHeight;
        }
    }

    private double getBounceWidth() {
        long currentTime = System.currentTimeMillis();

        if (targetWidth != lastTargetWidth || targetHeight != lastTargetHeight) {
            return currentWidth;
        }

        List<Toggle> activeNotifications = Manager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty();
        boolean isEnteringNoticeState = hasNotifications && !wasInNoticeState;

        float animationDuration = isExpanding ? 450.0f : 500.0f;
        int maxBounces = (isExpanding && isEnteringNoticeState) ? 2 : 0;
        float bounceDuration = isExpanding ? (bounceCount == 0 ? 0.0f : 450.0f) : 0.0f;

        float totalProgress = Math.min((currentTime - animationStartTime) / animationDuration, 1.0f);
        float bounceProgress = Math.min((currentTime - bounceStartTime) / bounceDuration, 1.0f);

        if (totalProgress < 1.0f && bounceCount < maxBounces) {
            if (bounceProgress >= 1.0f) {
                bounceCount++;
                bounceStartTime = currentTime;
                bounceProgress = 0;
            }

            float bounceIntensity;
            if (isExpanding) {
                bounceIntensity = bounceCount == 0 ? 0.0f : 0.09f;
            } else {
                bounceIntensity = 0.0f;
            }

            if (bounceProgress < 0.5f) {
                float expandProgress = bounceProgress * 2;
                float overshoot = (float) Math.sin(expandProgress * Math.PI) * bounceIntensity;
                return currentWidth * (1.0f + overshoot);
            } else {
                float contractProgress = (bounceProgress - 0.5f) * 2;
                float undershoot = (float) Math.sin(contractProgress * Math.PI) * bounceIntensity * 0.5f;
                return currentWidth * (1.0f - undershoot);
            }
        }

        return currentWidth;
    }

    private double getBounceHeight() {
        long currentTime = System.currentTimeMillis();

        if (targetWidth != lastTargetWidth || targetHeight != lastTargetHeight) {
            return currentHeight;
        }

        List<Toggle> activeNotifications = Manager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty();
        boolean isEnteringNoticeState = hasNotifications && !wasInNoticeState;

        float animationDuration = isExpanding ? 450.0f : 500.0f;
        int maxBounces = (isExpanding && isEnteringNoticeState) ? 2 : 0;
        float bounceDuration = isExpanding ? (bounceCount == 0 ? 0.0f : 450.0f) : 0.0f;

        float totalProgress = Math.min((currentTime - animationStartTime) / animationDuration, 1.0f);
        float bounceProgress = Math.min((currentTime - bounceStartTime) / bounceDuration, 1.0f);

        if (totalProgress < 1.0f && bounceCount < maxBounces) {
            if (bounceProgress >= 1.0f) {
                bounceCount++;
                bounceStartTime = currentTime;
                bounceProgress = 0;
            }

            float bounceIntensity;
            if (isExpanding) {
                bounceIntensity = bounceCount == 0 ? 0.0f : 0.09f;
            } else {
                bounceIntensity = 0.0f;
            }

            if (bounceProgress < 0.5f) {
                float expandProgress = bounceProgress * 2;
                float overshoot = (float) Math.sin(expandProgress * Math.PI) * bounceIntensity;
                return currentHeight * (1.0f + overshoot);
            } else {
                float contractProgress = (bounceProgress - 0.5f) * 2;
                float undershoot = (float) Math.sin(contractProgress * Math.PI) * bounceIntensity * 0.5f;
                return currentHeight * (1.0f - undershoot);
            }
        }

        return currentHeight;
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        return (float) currentWidth * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return (float) currentHeight * scale;
    }
}