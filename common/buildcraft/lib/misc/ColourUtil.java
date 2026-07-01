/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.core.Direction;
import net.minecraft.ChatFormatting;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.render.font.SpecialColourFontRenderer;

public class ColourUtil {
    public static final char MINECRAFT_FORMAT_CHAR;
    public static final String COLOUR_SPECIAL_START;

    public static final Function<ChatFormatting, ChatFormatting> getTextFormatForBlack =
        ColourUtil::getTextFormatForBlack;
    public static final Function<ChatFormatting, ChatFormatting> getTextFormatForWhite =
        ColourUtil::getTextFormatForWhite;

    public static final EnumDyeColor[] COLOURS = EnumDyeColor.values();

    private static final String[] NAMES = { //
        "Black", "Red", "Green", "Brown", //
        "Blue", "Purple", "Cyan", "LightGray", //
        "Gray", "Pink", "Lime", "Yellow", //
        "LightBlue", "Magenta", "Orange", "White"//
    };
    private static final int[] DARK_HEX = { //
        0x2D2D2D, 0xA33835, 0x394C1E, 0x5C3A24, //
        0x3441A2, 0x843FBF, 0x36809E, 0x888888, //
        0x444444, 0xE585A0, 0x3FAA36, 0xCFC231, //
        0x7F9AD1, 0xFF64FF, 0xFF6A00, 0xFFFFFF //
    };
    private static final int[] LIGHT_HEX = { //
        0x181414, 0xBE2B27, 0x007F0E, 0x89502D, //
        0x253193, 0x7e34bf, 0x299799, 0xa0a7a7, //
        0x7A7A7A, 0xD97199, 0x39D52E, 0xFFD91C, //
        0x66AAFF, 0xD943C6, 0xEA7835, 0xe4e4e4 //
    };
    private static final String[] DYES = new String[16];
    private static final Map<String, EnumDyeColor> nameToColourMap;
    private static final int[] FACE_TO_COLOUR;

    private static final ChatFormatting[] FORMATTING_VALUES = ChatFormatting.values();

    private static final ChatFormatting[] COLOUR_TO_FORMAT = new ChatFormatting[16];
    private static final ChatFormatting[] REPLACE_FOR_WHITE = new ChatFormatting[16];
    private static final ChatFormatting[] REPLACE_FOR_BLACK = new ChatFormatting[16];
    private static final ChatFormatting[] REPLACE_FOR_WHITE_HIGH_CONTRAST = new ChatFormatting[16];
    private static final ChatFormatting[] REPLACE_FOR_BLACK_HIGH_CONTRAST = new ChatFormatting[16];
    private static final ChatFormatting[] FACE_TO_FORMAT = new ChatFormatting[6];

    private static final Pattern ALL_FORMAT_MATCHER = Pattern.compile("(?i)\u00a7[0-9A-Za-z]");

    static {
        MINECRAFT_FORMAT_CHAR = '\u00a7';
        COLOUR_SPECIAL_START = MINECRAFT_FORMAT_CHAR + "z" + MINECRAFT_FORMAT_CHAR;
        for (int i = 0; i < 16; i++) {
            DYES[i] = "dye" + NAMES[i];
            REPLACE_FOR_WHITE[i] = REPLACE_FOR_WHITE_HIGH_CONTRAST[i] = FORMATTING_VALUES[i];
            REPLACE_FOR_BLACK[i] = REPLACE_FOR_BLACK_HIGH_CONTRAST[i] = FORMATTING_VALUES[i];
        }

        replaceColourForWhite(ChatFormatting.WHITE, ChatFormatting.GRAY);
        replaceColourForWhite(ChatFormatting.YELLOW, ChatFormatting.GOLD);
        replaceColourForWhite(ChatFormatting.AQUA, ChatFormatting.BLUE);
        replaceColourForWhite(ChatFormatting.GREEN, ChatFormatting.DARK_GREEN);

        replaceColourForBlack(ChatFormatting.BLACK, ChatFormatting.GRAY);
        replaceColourForBlack(ChatFormatting.DARK_GRAY, ChatFormatting.GRAY);
        replaceColourForBlack(ChatFormatting.DARK_BLUE, ChatFormatting.BLUE, ChatFormatting.AQUA);
        replaceColourForBlack(ChatFormatting.BLUE, ChatFormatting.BLUE, ChatFormatting.AQUA);
        replaceColourForBlack(ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE);
        replaceColourForBlack(ChatFormatting.DARK_RED, ChatFormatting.RED);
        replaceColourForBlack(ChatFormatting.DARK_GREEN, ChatFormatting.GREEN);

        COLOUR_TO_FORMAT[EnumDyeColor.BLACK.ordinal()] = ChatFormatting.BLACK;
        COLOUR_TO_FORMAT[EnumDyeColor.GRAY.ordinal()] = ChatFormatting.DARK_GRAY;
        COLOUR_TO_FORMAT[EnumDyeColor.SILVER.ordinal()] = ChatFormatting.GRAY;
        COLOUR_TO_FORMAT[EnumDyeColor.WHITE.ordinal()] = ChatFormatting.WHITE;

        COLOUR_TO_FORMAT[EnumDyeColor.RED.ordinal()] = ChatFormatting.DARK_RED;
        COLOUR_TO_FORMAT[EnumDyeColor.BLUE.ordinal()] = ChatFormatting.BLUE;
        COLOUR_TO_FORMAT[EnumDyeColor.CYAN.ordinal()] = ChatFormatting.DARK_AQUA;
        COLOUR_TO_FORMAT[EnumDyeColor.LIGHT_BLUE.ordinal()] = ChatFormatting.AQUA;

        COLOUR_TO_FORMAT[EnumDyeColor.GREEN.ordinal()] = ChatFormatting.DARK_GREEN;
        COLOUR_TO_FORMAT[EnumDyeColor.LIME.ordinal()] = ChatFormatting.GREEN;
        COLOUR_TO_FORMAT[EnumDyeColor.BROWN.ordinal()] = ChatFormatting.GOLD;
        COLOUR_TO_FORMAT[EnumDyeColor.YELLOW.ordinal()] = ChatFormatting.YELLOW;

        COLOUR_TO_FORMAT[EnumDyeColor.ORANGE.ordinal()] = ChatFormatting.GOLD;
        COLOUR_TO_FORMAT[EnumDyeColor.PURPLE.ordinal()] = ChatFormatting.DARK_PURPLE;
        COLOUR_TO_FORMAT[EnumDyeColor.MAGENTA.ordinal()] = ChatFormatting.LIGHT_PURPLE;
        COLOUR_TO_FORMAT[EnumDyeColor.PINK.ordinal()] = ChatFormatting.LIGHT_PURPLE;

        FACE_TO_FORMAT[Direction.UP.ordinal()] = ChatFormatting.WHITE;
        FACE_TO_FORMAT[Direction.DOWN.ordinal()] = ChatFormatting.BLACK;
        FACE_TO_FORMAT[Direction.NORTH.ordinal()] = ChatFormatting.RED;
        FACE_TO_FORMAT[Direction.SOUTH.ordinal()] = ChatFormatting.BLUE;
        FACE_TO_FORMAT[Direction.EAST.ordinal()] = ChatFormatting.YELLOW;
        FACE_TO_FORMAT[Direction.WEST.ordinal()] = ChatFormatting.GREEN;

        ImmutableMap.Builder<String, EnumDyeColor> builder = ImmutableMap.builder();
        for (EnumDyeColor c : COLOURS) {
            builder.put(c.getName(), c);
        }
        nameToColourMap = builder.build();

        FACE_TO_COLOUR = new int[6];
        FACE_TO_COLOUR[Direction.DOWN.ordinal()] = 0xFF_33_33_33;
        FACE_TO_COLOUR[Direction.UP.ordinal()] = 0xFF_CC_CC_CC;
    }

    private static void replaceColourForBlack(ChatFormatting colour, ChatFormatting with) {
        replaceColourForBlack(colour, with, with);
    }

    private static void replaceColourForBlack(ChatFormatting colour, ChatFormatting normal,
        ChatFormatting highContrast) {
        REPLACE_FOR_BLACK[colour.ordinal()] = normal;
        REPLACE_FOR_BLACK_HIGH_CONTRAST[colour.ordinal()] = highContrast;
    }

    private static void replaceColourForWhite(ChatFormatting colour, ChatFormatting with) {
        replaceColourForWhite(colour, with, with);
    }

    private static void replaceColourForWhite(ChatFormatting colour, ChatFormatting normal,
        ChatFormatting highContrast) {
        REPLACE_FOR_WHITE[colour.ordinal()] = normal;
        REPLACE_FOR_WHITE_HIGH_CONTRAST[colour.ordinal()] = highContrast;
    }

    @Nullable
    public static EnumDyeColor parseColourOrNull(String string) {
        return nameToColourMap.get(string);
    }

    public static String getDyeName(EnumDyeColor colour) {
        return DYES[colour.getDyeDamage()];
    }

    public static String getName(EnumDyeColor colour) {
        return NAMES[colour.getDyeDamage()];
    }

    public static int getDarkHex(EnumDyeColor colour) {
        return DARK_HEX[colour.getDyeDamage()];
    }

    public static int getLightHex(EnumDyeColor colour) {
        return LIGHT_HEX[colour.getDyeDamage()];
    }

    public static int getColourForSide(Direction face) {
        return FACE_TO_COLOUR[face.ordinal()];
    }

    public static String[] getNameArray() {
        return Arrays.copyOf(NAMES, NAMES.length);
    }

    /** Returns a string formatted for use in a tooltip (or anything else with a black background). If
     * {@link BCLibConfig#useColouredLabels} is true then this will make prefix the string with an appropriate
     * {@link ChatFormatting} colour, and postfix with {@link ChatFormatting#RESET} */
    public static String getTextFullTooltip(EnumDyeColor colour) {
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour)
                + ChatFormatting.RESET;
        } else {
            return LocaleUtil.localizeColour(colour);
        }
    }

    /** Similar to {@link #getTextFullTooltip(EnumDyeColor)}, but outputs a string specifically designed for
     * {@link SpecialColourFontRenderer}. MUST be the first string used! */
    public static String getTextFullTooltipSpecial(EnumDyeColor colour) {
        if (colour == EnumDyeColor.BLACK || colour == EnumDyeColor.BLUE) {
            return getTextFullTooltip(colour);
        }
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
            return COLOUR_SPECIAL_START + Integer.toHexString(colour.getMetadata())//
                + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour) + ChatFormatting.RESET;
        }
        return LocaleUtil.localizeColour(colour);
    }

    /** Returns a string formatted for use in a tooltip (or anything else with a black background). If
     * {@link BCLibConfig#useColouredLabels} is true then this will make prefix the string with an appropriate
     * {@link ChatFormatting} colour, and postfixed with {@link ChatFormatting#RESET} */
    public static String getTextFullTooltip(Direction face) {
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertFaceToTextFormat(face);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeFacing(face)
                + ChatFormatting.RESET;
        } else {
            return LocaleUtil.localizeFacing(face);
        }
    }

    /** Returns a {@link ChatFormatting} colour that will display correctly on a black background, so it won't use any
     * of the darker colours (as they will be difficult to see). */
    public static ChatFormatting getTextFormatForBlack(ChatFormatting in) {
        if (in.isColor()) {
            if (BCLibConfig.useHighContrastLabelColours) {
                return REPLACE_FOR_BLACK_HIGH_CONTRAST[in.ordinal()];
            } else {
                return REPLACE_FOR_BLACK[in.ordinal()];
            }
        } else {
            return in;
        }
    }

    /** Returns a {@link ChatFormatting} colour that will display correctly on a white background, so it won't use any
     * of the lighter colours (as they will be difficult to see). */
    public static ChatFormatting getTextFormatForWhite(ChatFormatting in) {
        if (in.isColor()) {
            if (BCLibConfig.useHighContrastLabelColours) {
                return REPLACE_FOR_WHITE_HIGH_CONTRAST[in.ordinal()];
            } else {
                return REPLACE_FOR_WHITE[in.ordinal()];
            }
        } else {
            return in;
        }
    }

    /** Converts an {@link EnumDyeColor} into an equivalent {@link ChatFormatting} for display. */
    public static ChatFormatting convertColourToTextFormat(EnumDyeColor colour) {
        return COLOUR_TO_FORMAT[colour.ordinal()];
    }

    /** Converts an {@link Direction} into an equivalent {@link ChatFormatting} for display. */
    public static ChatFormatting convertFaceToTextFormat(Direction face) {
        return FACE_TO_FORMAT[face.ordinal()];
    }

    public static int swapArgbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb >> 0) & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static EnumDyeColor getNext(EnumDyeColor colour) {
        int ord = colour.ordinal() + 1;
        return COLOURS[ord & 15];
    }

    public static EnumDyeColor getNextOrNull(@Nullable EnumDyeColor colour) {
        if (colour == null) {
            return COLOURS[0];
        } else if (colour == COLOURS[COLOURS.length - 1]) {
            return null;
        } else {
            return getNext(colour);
        }
    }

    public static EnumDyeColor getPrev(EnumDyeColor colour) {
        int ord = colour.ordinal() + 16 - 1;
        return COLOURS[ord & 15];
    }

    public static EnumDyeColor getPrevOrNull(@Nullable EnumDyeColor colour) {
        if (colour == null) {
            return COLOURS[COLOURS.length - 1];
        } else if (colour == COLOURS[0]) {
            return null;
        } else {
            return getPrev(colour);
        }
    }

    /** Similar to {@link ChatFormatting#getTextWithoutFormattingCodes(String)}, but also removes every special char
     * that {@link #getTextFullTooltipSpecial(EnumDyeColor)} can add. */
    public static String stripAllFormatCodes(String string) {
        return ALL_FORMAT_MATCHER.matcher(string).replaceAll("");
    }
}
