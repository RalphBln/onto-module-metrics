package xyz.aspectowl.ontometrics.gui;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

class LatexEscaper {

  private final List<Replacement> texReplacements = Lists.newArrayList();

  {
    texReplacements.add(new Replacement("\\", "\\textbackslash "));
    for (final String x : ImmutableList.of("_", "^", "~", "$", "%", "#", "&", "{", "}")) {
      texReplacements.add(new Replacement(x, "\\" + x));
    }
  }

  public String escape(final String input) {
    String escaped = input;
    for (final Replacement replacement : texReplacements) {
      escaped = replacement.applyTo(escaped);
    }
    return escaped;
  }

  private class Replacement {

    final String toReplace;
    final String replaceWith;

    Replacement(final String toReplace, final String replaceWith) {
      this.toReplace = toReplace;
      this.replaceWith = replaceWith;
    }

    String applyTo(final String input) {
      return input.replace(toReplace, replaceWith);
    }
  }
}
