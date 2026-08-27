# Regex Named Group Companion

IntelliJ-family plugin. Shows the name of every named group
(`(?<year>\d{4})`) inline, right on the line of any Java or Kotlin
regex literal that has one — no need to open a separate tool window
just to remember what a pattern's groups are called. A companion tool
window (reusing Regex Preview Companion's own matching engine) lets you
test a pattern against real sample text and see every named group's
actual extracted value per match (`year: "2024"`), not just the
overall match highlighted.

## Why it exists

An original idea, not a port of an existing competitor — validated
against this catalog's own idea-validation discipline before
being built: (1) confirmed no plugin in this catalog or in JetBrains
Marketplace does exactly this (the closest, "Regex Tool" at 17,961
downloads, is a generic tester with no inline-in-source-code angle at
all); (2) confirmed buildable in the ~10-day budget with techniques
this catalog already has proven (PSI walking for Java/Kotlin, an
`EditorCustomElementRenderer` inlay, a tool window) — no new
technique invented for this plugin, just two existing ones combined.
Same "apuesta consciente sin ancla de mercado" treatment as Refactor
Simulator / Test Scaffold Companion: v0.1 ships free, no
time/marketing investment disproportionate to real demand signal
until there's evidence of adoption.

## Why built this way

- **Reuses, never rewrites, Regex Preview Companion's matching core.**
  `RegexMatcher.find()` is ported unchanged; the new
  `findWithNamedGroups()` is a real extension of the same object, same
  file, same honest-error philosophy — an invalid pattern always
  returns the real `PatternSyntaxException` message, never a silent
  blank or a generic "invalid".
- **Group names are read from the pattern *text*, never from
  `Pattern.namedGroups()`'s `Map`.** That method is real public JDK API
  (since JDK 20, and every IDE this plugin targets bundles JDK 21+),
  but a live check during development confirmed its iteration order is
  **not** guaranteed to be source order — `(?<z>a)(?<a>b)(?<m>c)` came
  back in a different order than written. Both the inlay hint and the
  tool window need groups in the order the developer actually wrote
  them, so `NamedGroupExtractor` scans the pattern text with a small,
  targeted regex instead (careful to exclude `(?<=`/`(?<!` lookbehind,
  which share the same `(?<` prefix but are never named groups).
- **The inlay hint scans the whole file's PSI, not just
  `Pattern.compile(...)` call sites.** A regex is routinely assigned to
  a constant first and used elsewhere (see the demo project) — scoping
  detection to call sites would silently miss that, the most common
  real-world shape. A plain string with zero named groups costs one
  cheap regex scan and produces no hit; there's no false-positive risk
  from scanning broadly.
- **Kotlin string interpolation is a hard stop, not a best-effort
  guess.** `KotlinRegexLiteralFinder` rebuilds a literal's text
  entry-by-entry (`KtLiteralStringTemplateEntry` +
  `KtEscapeStringTemplateEntry`) and returns nothing at all the moment
  it hits real interpolation (`$name`/`${expr}`) — the actual pattern
  isn't knowable from source, so guessing at it would be actively
  misleading, not merely incomplete.
- **Same inlay rendering pattern as Error Lens Companion**
  (`TextEditorHighlightingPassFactoryRegistrar` +
  `EditorCustomElementRenderer` + `InlayModel.addAfterLineEndElement`),
  the one plugin in this catalog that already proved this exact
  integration point works — no new rendering mechanism invented here.
- **`supportsKotlinPluginMode` declared explicitly.** Same real,
  evidence-derived gotcha already documented for Highlight Companion:
  without it, the whole plugin can silently fail to load once the
  Kotlin plugin's K2 mode is the default. Neither Kotlin-side class
  here calls any resolve/analysis API, so both K1 and K2 are genuinely
  supported.
- **100% local** — no network call, no account, no telemetry.

## Usage

- **Inlay hint**: open any `.java` or `.kt` file with a regex string
  literal containing named groups — the group names appear inline at
  the end of that line automatically, no action needed.
- **Tool window**: open **Regex Named Groups** (bottom of the IDE) →
  type a pattern with named groups → type or paste sample text →
  matches highlight live, and each match's named-group values appear
  in the "Named groups" section below.

See `demo/README.md` for a full walkthrough with realistic sample code
and expected results.

## v0.1 scope

Free, all of it — no paywall, nothing held back for a future tier.
Detects named groups in Java and Kotlin regex literals only. Deferred
to a possible future v0.2 (not started, not promised): .NET/PCRE-style
named group syntax (`(?'name'...)`), saved test cases.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us
at **gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
