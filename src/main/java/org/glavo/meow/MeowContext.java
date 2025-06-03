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

import kala.ansi.AnsiString;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jline.terminal.Terminal;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MeowContext {

    public static final MeowContext ROOT = new MeowContext(null, null);

    static {
        ROOT.setValue(new Meow(0x00B0F0, true), (MeowMacro) MeowContext::let);
        ROOT.setValue(new Meow(0xEE0000, true), (MeowFunction) MeowContext::print);
    }

    // Built-in functions

    private static MeowValue let(MeowContext context, List<MeowExpression> args) {
        MeowDebug.debugLog("let({0}, {1})", context, args);

        if (args.size() < 2) {
            throw new IllegalArgumentException("Expected 2 argument, but got " + args.size() + ": " + args);
        }
        MeowExpression name = args.getFirst();
        if (!(name instanceof MeowExpression.Identifier(Meow meow))) {
            throw new IllegalArgumentException("Expected identifier, but got " + name);
        }

        MeowValue value = args.get(1).eval(context);
        context.setValue(meow, value);
        return value;
    }

    private static MeowValue print(MeowContext context, List<MeowValue> args) {
        MeowDebug.debugLog("print({0}, {1})", context, args);

        //noinspection resource
        PrintWriter writer = context.getTerminal().writer();
        for (MeowValue value : args) {
            if (value instanceof MeowText(List<XWPFRun> content)) {
                for (XWPFRun node : content) {
                    var text = AnsiString.ofPlain(node.text());
                    if (!text.isEmpty()) {
                        if (node.isBold()) {
                            text = text.overlay(AnsiString.Bold.On);
                        }
                        if (node.getUnderline() != UnderlinePatterns.NONE) {
                            text = text.overlay(AnsiString.Underlined.On);
                        }
                        if (node.getColor() != null) {
                            int rgb = Integer.parseInt(node.getColor(), 16);
                            if (rgb != 0) {
                                text = text.overlay(AnsiString.Color.True(rgb >> 16, (rgb >> 8) & 0xFF, rgb & 0xFF));
                            }
                        }
                        if (node.getTextHighlightColor() != STHighlightColor.NONE) {
                            AnsiString.Attribute backgroundColor = null;
                            if (node.getTextHighlightColor() == STHighlightColor.BLACK) {
                                backgroundColor = AnsiString.Back.Black;
                            } else if (node.getTextHighlightColor() == STHighlightColor.BLUE) {
                                backgroundColor = AnsiString.Back.Blue;
                            } else if (node.getTextHighlightColor() == STHighlightColor.CYAN) {
                                backgroundColor = AnsiString.Back.Cyan;
                            } else if (node.getTextHighlightColor() == STHighlightColor.GREEN) {
                                backgroundColor = AnsiString.Back.Green;
                            } else if (node.getTextHighlightColor() == STHighlightColor.MAGENTA) {
                                backgroundColor = AnsiString.Back.Magenta;
                            } else if (node.getTextHighlightColor() == STHighlightColor.RED) {
                                backgroundColor = AnsiString.Back.Red;
                            } else if (node.getTextHighlightColor() == STHighlightColor.YELLOW) {
                                backgroundColor = AnsiString.Back.Yellow;
                            } else if (node.getTextHighlightColor() == STHighlightColor.WHITE) {
                                backgroundColor = AnsiString.Back.White;
                            } else {
                                // TODO: Dark colors
                            }

                            if (backgroundColor != null) {
                                text = text.overlay(backgroundColor);
                            }

                        }

                        writer.print(text);
                    }

                    for (XWPFPicture picture : node.getEmbeddedPictures()) {
                        XWPFPictureData pictureData = picture.getPictureData();
                        byte[] data = pictureData.getData();
                        BufferedImage image;
                        try {
                            image = ImageIO.read(new ByteArrayInputStream(data));
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }

                        StringBuilder builder = new StringBuilder();
                        for (int y = 0; y * 2 < image.getHeight(); y++) {
                            if (y > 0) {
                                builder.append(AnsiString.Reset.overlay("\n"));
                            }
                            for (int x = 0; x < image.getWidth(); x++) {
                                int rgb = image.getRGB(x, y * 2);
                                int rgb2 = image.getRGB(x, y * 2 + 1);

                                AnsiString.Attribute a1 = AnsiString.Color.True(
                                        (rgb >> 16) & 0xFF,
                                        (rgb >> 8) & 0xFF,
                                        rgb & 0xFF);

                                AnsiString.Attribute a2 = AnsiString.Back.True(
                                        (rgb2 >> 16) & 0xFF,
                                        (rgb2 >> 8) & 0xFF,
                                        rgb2 & 0xFF);
                                builder.append(AnsiString.ofPlain("▀").overlay(a1).overlay(a2));
                            }
                        }
                        writer.print(builder);
                    }
                }
                writer.println();
            } else {
                writer.println(value.toString());
            }
        }

        return MeowUnit.INSTANCE;
    }


    // ---------------

    private final MeowContext parent;
    private final Terminal terminal;
    private final Map<Meow, MeowValue> binding = new LinkedHashMap<>();

    public MeowContext(MeowContext parent, Terminal terminal) {
        this.parent = parent;
        this.terminal = terminal;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public MeowValue getValue(Meow key) {
        MeowContext context = this;
        while (context != null) {
            MeowValue value = context.binding.get(key);
            if (value != null) {
                return value;
            } else {
                context = context.parent;
            }
        }

        return MeowUnit.INSTANCE;
    }

    public void setValue(Meow key, MeowValue value) {
        binding.put(key, value);
    }

    public void evalFile(Path file) throws IOException {
        try (var xwpfDocument = new XWPFDocument(Files.newInputStream(file))) {
            evalDocument(xwpfDocument);
        }
    }

    public void evalDocument(XWPFDocument document) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            MeowExpression expr = MeowParser.parse(paragraph.getRuns(), true);
            MeowDebug.debugLog("evalExpr({0})", expr);
            expr.eval(this);
        }
    }
}
