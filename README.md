# sirius-parent
![sirius](https://raw.githubusercontent.com/scireum/sirius-kernel/main/docs/sirius.jpg)
[![Build Status](https://github.com/scireum/sirius-parent/actions/workflows/validate.yml/badge.svg?branch=main)](https://github.com/scireum/sirius-parent/actions/workflows/validate.yml)

Provides the parent POM used by all Sirius and scireum projects. It enforces a sane build process: compiler
settings, plugin versions, test setup, code coverage, deployment and the shared IntelliJ IDEA project settings.

This is a module of the Sirius OpenSource project by scireum GmbH. For further information visit the project
website: [sirius-lib.net](https://www.sirius-lib.net)

## Usage

To use this POM in your own project include:

```xml
<parent>
    <groupId>com.scireum</groupId>
    <artifactId>sirius-parent</artifactId>
    <version>PICK A VERSION</version>
</parent>
```

An overview of all versions can be found on [mvn.scireum.com](https://mvn.scireum.com/com/scireum/sirius-parent/).

## Shared IntelliJ IDEA project settings

If a project contains an `.idea` directory, the `idea` profile unpacks `.idea/**` from the parent artifact into the
project during the `validate` phase. Every build therefore refreshes these files:

* `.idea/codeStyles/**` and `.idea/codeStyleSettings.xml` — the scireum code style
* `.idea/copyright/**` — the copyright header
* `.idea/inspectionProfiles/**` — the *Sirius* and *Sirius_Heavy* inspection profiles
* `.idea/encodings.xml`, `.idea/kotlinc.xml`, `.idea/misc.xml`, `.idea/scopes/**`

The source of these files is `src/main/resources/.idea/`. Change them here, not in the products — the unpack runs
with `overWrite=true` and discards local changes.

Companies outside of scireum can exclude single files with the `sirius.ideaExcludes` property, for example:

```xml
<properties>
    <sirius.ideaExcludes>.idea/copyright/**</sirius.ideaExcludes>
</properties>
```

## IDE setup for Pasta templates (manual, one time per developer)

Tagliatelle templates use a double extension, for example `page.html.pasta` or `core.js.pasta`. IntelliJ IDEA shows
these files as plain text until you map the patterns. The mapping is an application setting. IDEA stores it outside
of the project, so this POM cannot deliver it — each developer sets it once, and it then applies to all projects.

Open **Settings | Editor | File Types**, select the target file type and add the pattern:

| Pattern | File type |
|---|---|
| `*.html.pasta` | HTML |
| `*.pdf.pasta` | HTML |
| `*.js.pasta` | JavaScript |
| `*.css.pasta` | CSS |
| `*.md.pasta` | Markdown |
| `*.mustache.pasta` | Handlebars/Mustache |
| `*.pasta` | Plain Text |

Map `*.pasta` last. It catches the remaining combinations.

You can also edit the configuration file directly. Close the IDE first, then add the mappings to the
`extensionMap` element in `filetypes.xml`:

* Linux: `~/.config/JetBrains/IntelliJIdea<version>/options/filetypes.xml`
* macOS: `~/Library/Application Support/JetBrains/IntelliJIdea<version>/options/filetypes.xml`
* Windows: `%APPDATA%\JetBrains\IntelliJIdea<version>\options\filetypes.xml`

```xml
<mapping pattern="*.html.pasta" type="HTML" />
<mapping pattern="*.pdf.pasta" type="HTML" />
<mapping pattern="*.js.pasta" type="JavaScript" />
<mapping pattern="*.css.pasta" type="CSS" />
<mapping pattern="*.md.pasta" type="Markdown" />
<mapping pattern="*.mustache.pasta" type="Handlebars/Mustache" />
<mapping ext="pasta" type="PLAIN_TEXT" />
```

The IDE then highlights the surrounding HTML, JavaScript or CSS. It does not understand the Tagliatelle parts
(`<i:...>` tags and `@` expressions) and reports the unknown tags as warnings. Turn **Settings | Editor |
Inspections | HTML | Unknown HTML tag** off if the warnings disturb you.

To share the setting with your own machines use **Settings Sync**. To share it with a team use the
**Settings Repository** plugin.

## Contributions

Contributions as issues or pull requests are always welcome. Please [sign off](https://developercertificate.org)
all your commits by adding a line like "Signed-off-by: Name <email>" at the end of each commit, indicating that
you wrote the code and have the right to pass it on as an open source.

## License

Sirius is licensed under the [MIT License](https://opensource.org/licenses/MIT).
