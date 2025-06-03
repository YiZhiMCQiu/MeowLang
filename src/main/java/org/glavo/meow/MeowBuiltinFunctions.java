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
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.glavo.meow.ast.MeowExpression;
import org.glavo.meow.ast.MeowIdentifier;
import org.glavo.meow.ast.MeowExpressionList;
import org.glavo.meow.value.MeowFunction;
import org.glavo.meow.value.MeowIntegerValue;
import org.glavo.meow.value.MeowLambda;
import org.glavo.meow.value.MeowList;
import org.glavo.meow.value.MeowText;
import org.glavo.meow.value.MeowUnit;
import org.glavo.meow.value.MeowValue;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@SuppressWarnings("resource")
public final class MeowBuiltinFunctions {

    // Macros

    public static MeowValue let(MeowContext context, List<MeowExpression> args) {
        context.logMacroCall("let", args);

        MeowUtils.checkArgsCount(args, 2);


        MeowExpression name = args.getFirst();
        if (!(name instanceof MeowIdentifier(Meow meow))) {
            throw new IllegalArgumentException("Expected identifier, but got " + name);
        }

        MeowValue value = args.get(1).eval(context);
        context.setValue(meow, value);
        return value;
    }

    public static MeowValue lambda(MeowContext context, List<MeowExpression> args) {
        context.logMacroCall("lambda", args);

        if (args.size() < 1) {
            throw new IllegalArgumentException("Expected at least 2 arguments, but got " + args.size() + ": " + args);
        }

        List<Meow> parameters;
        if (args.getFirst() instanceof MeowIdentifier(Meow meow)) {
            parameters = List.of(meow);
        } else if (args.getFirst() instanceof MeowExpressionList(List<MeowExpression> nodes)) {
            parameters = nodes.stream().map(node -> {
                if (!(node instanceof MeowIdentifier(Meow meow))) {
                    throw new IllegalArgumentException("Expected identifier, but got " + node);
                }
                return meow;
            }).toList();
        } else {
            throw new IllegalArgumentException("Expected parameter list, but got " + args.getFirst());
        }

        return new MeowLambda(context, parameters, args.subList(1, args.size()));
    }

    public static MeowValue integer(MeowContext context, List<MeowExpression> args) {
        return new MeowIntegerValue(args.size());
    }

    // Functions

    public static MeowValue print(MeowContext context, List<MeowValue> args) {
        context.logFunctionCall("print", args);
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Nothing to print");
        }

        StringBuilder builder = new StringBuilder();

        boolean first = true;
        for (MeowValue value : args) {
            if (first) {
                first = false;
            } else {
                builder.append('\n');
            }

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
                            AnsiString.Attribute backgroundColor = MeowUtils.attributeOf(node.getTextHighlightColor());
                            if (backgroundColor != null) {
                                text = text.overlay(backgroundColor);
                            }
                        }

                        builder.append(text);
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

                        if (!builder.isEmpty() && builder.charAt(builder.length() - 1) != '\n') {
                            builder.append("\n");
                        }

                        // TODO: Scale image to fit terminal width
                        for (int y = 0; y * 2 < image.getHeight(); y++) {
                            for (int x = 0; x < image.getWidth(); x++) {
                                MeowUtils.appendTrueRgbEscape(builder, false, image.getRGB(x, y * 2));
                                MeowUtils.appendTrueRgbEscape(builder, true, image.getRGB(x, y * 2 + 1));
                                builder.append('▀');
                            }

                            builder.append("\u001b[0m\n");
                        }
                    }
                }
            } else {
                builder.append(value.toDisplayString(context));
            }
        }

        context.getTerminal().writer().println(builder);
        return MeowUnit.UNIT;
    }

    public static MeowValue readInt(MeowContext context, List<MeowValue> args) {
        context.logFunctionCall("readInt", args);
        String input;
        if (args.isEmpty()) {
            input = context.getReader().readLine();
        } else if (args.size() == 1) {
            input = context.getReader().readLine(args.getFirst().toDisplayString(context));
        } else {
            throw new IllegalArgumentException("Expected 0 or 1 argument, but got " + args.size() + ": " + args);
        }

        return new MeowIntegerValue(Long.parseLong(input));
    }

    public static MeowValue list(MeowContext context, List<MeowValue> args) {
        return new MeowList(args);
    }

    private MeowBuiltinFunctions() {
    }
}
