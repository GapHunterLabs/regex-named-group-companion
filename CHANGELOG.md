<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Regex Named Group Companion Changelog

## [Unreleased]

## [0.1.1]

### Fixed

- **Tool window keyboard input bug** (discovered 2026-08-19,
  root-caused and fixed 2026-08-24): the sample text area
  looked focused (highlighted border, blinking caret) but rejected
  all keyboard input, with a real visual artifact (clipped text from
  the read-only "Named groups" area bleeding into the sample area's
  top-left corner). Root cause: two `JBTextArea` instances competing
  for vertical space inside the same `BorderLayout` -- `JTextArea`'s
  own preferred-size contract is documented as unreliable when its
  height depends on a width not yet known at layout time, and with
  two such areas stacked in `BorderLayout` (CENTER + SOUTH), this
  produced ambiguous sizing that manifested as both a rendering
  artifact and (empirically, not yet fully explained by Swing docs
  alone) broken keyboard focus. Fixed by giving the sample area and
  the named-groups block an explicit, stable split via `JSplitPane`
  instead of leaving two competing preferred sizes for `BorderLayout`
  to reconcile.

## [0.1.0]

### Added

- **Inlay hint**: any Java or Kotlin regex string literal with named
  groups (`(?<year>\d{4})`) shows the group names inline, at the end
  of the line -- no need to open a tool window just to see what a
  pattern's groups are called.
- **Regex Named Groups tool window**: extends Regex Preview
  Companion's matching engine to also extract and show each named
  group's real value per match (`year: "2024"`), not just the overall
  match highlight/count.
- Invalid patterns show the real `PatternSyntaxException` message
  instead of a silent blank, in both the inlay scan and the tool
  window.
- A pattern with zero named groups produces no inlay and no false
  "0 groups" noise in the tool window.

[Unreleased]: https://github.com/GapHunterLabs/regex-named-group-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/regex-named-group-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/regex-named-group-companion/commits/0.1.0
