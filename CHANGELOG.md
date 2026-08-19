<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Regex Named Group Companion Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/GapHunterLabs/regex-named-group-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/regex-named-group-companion/commits/0.1.0
