/*
 * Copyright (C) 2025 Glavo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.glavo.meow;

import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class Meow {
    private static final Set<String> MEOW = Set.of("meow", "miaou", "miao", "nyan", "にゃん", "야옹", "喵");

    public static Meow of(XWPFRun term) {
        if (!term.getEmbeddedPictures().isEmpty()) {
            return null;
        }

        if (MEOW.contains(term.text().trim().toLowerCase(Locale.ROOT))) {
            return new Meow(term);
        } else {
            return null;
        }
    }

    private final boolean isBracket;
    private final UnderlinePatterns underline;
    private final int color;
    private final boolean isBold;
    private final String font;
    private final Double fontSize;

    public Meow(XWPFRun term) {
        this.isBracket = term.isItalic();
        this.color = term.getColor() == null ? 0 : Integer.parseInt(term.getColor(), 16);
        this.isBold = term.isBold();
        this.underline = term.getUnderline();
        this.font = term.getFontFamily();
        this.fontSize = term.getFontSizeAsDouble();
    }

    public Meow(int color, boolean isBold) {
        this.isBracket = false;
        this.underline = UnderlinePatterns.NONE;
        this.color = color;
        this.isBold = isBold;
        this.font = null;
        this.fontSize = null;
    }

    public boolean isBracket() {
        return isBracket;
    }

    public UnderlinePatterns getUnderline() {
        return underline;
    }

    public int getColor() {
        return color;
    }

    public String getFont() {
        return font;
    }

    public Double getFontSize() {
        return fontSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isBracket, color, isBold, underline, font, fontSize);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Meow that
               && this.isBracket == that.isBracket
               && this.color == that.color
               && this.isBold == that.isBold
               && this.underline == that.underline
               && Objects.equals(this.font, that.font)
               && Objects.equals(this.fontSize, that.fontSize);
    }

    @Override
    public String toString() {
        return "Meow[isBracket=%s, color=%06X, isBold=%s, underline=%s, font=%s, fontSize=%s]"
                .formatted(isBracket, color, isBold, underline, font, fontSize);
    }
}
