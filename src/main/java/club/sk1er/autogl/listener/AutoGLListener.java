package club.sk1er.autogl.listener;

import club.sk1er.autogl.AutoGL;
import club.sk1er.autogl.config.AutoGLConfig;
import club.sk1er.mods.core.util.MinecraftUtils;
import club.sk1er.mods.core.util.Multithreading;
import club.sk1er.vigilance.data.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AutoGLListener {

    private boolean invoked;

    @SubscribeEvent
    public void worldSwap(WorldEvent.Unload event) {
        invoked = false;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String s = event.message.getUnformattedText().toLowerCase(Locale.ENGLISH);
        if (AutoGL.instance.getAutoGLConfig().isAntiGLEnabled() && invoked) {
            for (String primaryString : getPrimaryStrings()) {
                if (s.contains(primaryString.toLowerCase(Locale.ENGLISH)))
                    event.setCanceled(true);
            }
        }

        String unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        if (!MinecraftUtils.isHypixel() || !AutoGL.instance.getAutoGLConfig().isAutoGLEnabled() || AutoGL.instance.isRunning()) {
            return;
        }
        if (unformattedText.startsWith("The game starts in 5 seconds!")) {
            AutoGL.instance.setRunning(true);
            invoked = true;
            Multithreading.schedule(() -> {
                try {
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(
                        "/achat " + (getPrimaryString())
                    );
                    end();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    private void end() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AutoGL.instance.setRunning(false);
    }

    private String getPrimaryString() {
        int autoGGPhrase = AutoGL.instance.getAutoGLConfig().getAutoGLPhrase();
        String[] primaryStrings = getPrimaryStrings();
        if (autoGGPhrase > 0 && autoGGPhrase < primaryStrings.length) {
            return primaryStrings[autoGGPhrase];
        }

        return "gl";
    }

    private String[] getPrimaryStrings() {
        try {
            Property autoGGPhrase = AutoGLConfig.class.getDeclaredField("autoGLPhrase").getAnnotation(Property.class);
            return autoGGPhrase.options();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return new String[0];
    }

}
