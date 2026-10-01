# Easy Español data guide

How to read, edit and extend the phrase data. The files live in `app/src/main/assets/`:

| File | Contents |
|---|---|
| `phrases_spain.csv` | Phrases, Castilian Spanish |
| `phrases_latam.csv` | The same phrases, Latin American Spanish (Mexican usage) |
| `expressions_spain.csv` | Colloquial expressions used in Spain |
| `expressions_latam.csv` | Colloquial expressions used in Mexico / Latin America |

Run `python tools/validate.py` from the repo root after every edit.

All files are UTF-8 without a BOM, with standard CSV quoting. Many Spanish fields contain commas, so the app needs a quote-aware CSV parser (the `line.split(",")` approach in EasyJapanesey will break).

## Phrase columns

`id, topic, scene, grammar, vocab, spanish, english, note`

- **id**: `topic-scene-NN`, e.g. `hotel-checkin-03`. Permanent: progress is stored against it, so never renumber or reuse one. New phrases take the next free number in their scene, even if inserted mid-scene.
- **topic / scene**: the two grouping levels. A topic's scenes sit together in the file, and rows are in teaching order within each scene: simplest first, building towards the hardest. A new scene for an existing topic goes after that topic's last scene. Every row sharing an id prefix must use exactly the same topic and scene name (the validator rejects, say, `Errands & services` alongside `Services & errands`).
- **grammar / vocab**: 1, 2 or 3 each (see below).
- **spanish / english**: colour-linked markup.
- **note**: optional one-line hint, shown on tap. Use it to name the structure that's new in that phrase.

Both phrase files have identical ids, order, topic, scene, levels and English. Only `spanish` and `note` may differ.

## Expression columns

`id, group, register, spanish, literal, meaning, note`

- **register**: `neutral`, `informal` or `slang`.
- **literal**: a word-for-word gloss, colour-linked to the Spanish. This is where the fun is.
- **meaning**: the real English equivalent, plain text.
- Expressions in both regions share an id. Regional ones appear in one file only.

## Markup

```
¿{1:De dónde} {2:eres}?          {1:Where} {2:are you} {1:from}?
{1:Estoy} {2:cansad[o|a]}.       {1:I'm} {2:tired}.
¿{1:Puedo} {2:probár}me{3:lo}?   {1:Can I} {2:try} {3:it} {2:on}?
```

- `{n:text}` is a chunk in colour group n. The same n on both sides is the link.
- A group can appear more than once on a side (discontiguous), as with "Where ... from".
- Chunks can be part of a word: `{2:probár}me{3:lo}`, `{3:is}{2:n't}`.
- Text outside braces is neutral (no colour). Use it for punctuation, for `¿ ¡`, and for words with no counterpart: personal *a*, *que* where English drops "that", the *me* in *llevarse*.
- Number groups 1, 2, 3... in order of first appearance in the Spanish. Maximum 9 groups.
- `[masculine|feminine]` marks a word that changes with the speaker's gender: `cansad[o|a]`, `profesor[|a]`. Spanish only, and only for words that describe the speaker. Other people in a phrase have a fixed gender.

### Chunking guidelines

Make each chunk the smallest unit that maps cleanly onto something in the other language. A verb carries its subject (*vivo* = "I live"). Keep idioms whole when splitting them would mislead (*me llamo* = "my name is"). Split where word order differs, as that's what the colours are for teaching: adjective after noun, pronouns before the verb, *n't*. Aim for no more than 6 groups at grammar level 1.

## Difficulty levels

The hardest single element decides the level. One B2 structure makes a phrase grammar 3, however simple the rest.

Fixed formulas that learners pick up as single chunks count as vocabulary, not grammar: *perdone*, *disculpe*, *perdona*, *¿cómo se dice…?*, *aquí tiene*. So *Perdone, ¿dónde está la estación?* is grammar 1 even though *perdone* is technically a command. Productive uses of the same structure (*siga todo recto*, *gire a la izquierda*) are graded normally.

**Grammar**

| Level | Roughly | Includes |
|---|---|---|
| 1 | A1 | Present tense (regular, common irregulars, stem-changers); *estar* + gerund; *ir a* + infinitive; modal + infinitive (*quiero, puedo, tengo que, hay que*); *hay*; *gustar*-type verbs; reflexive verbs in any position; a single object pronoun before the verb |
| 2 | A2–B1 | Preterite; imperfect; present perfect; *hace* + time; *llevar* + time; *acabar de*; affirmative *usted* and *tú* commands; pronouns attached to infinitives or commands; two pronouns together (*me lo, se lo*); future; polite conditional (*podría, me gustaría*); simple *se* passive |
| 3 | B1+–B2 | Any subjunctive; conditional in hypotheticals; *si* clauses about unreal situations; pluperfect; future perfect; reported speech with tense shifts; negative commands; long multi-clause sentences |

**Vocabulary**

| Level | Meaning |
|---|---|
| 1 | Core words a beginner meets in the first weeks: family, food basics, days, numbers, common verbs and adjectives |
| 2 | Everyday words for the topic: reservation, towel, prescription, delay, meeting |
| 3 | Specific or less frequent words: layover, drowsiness, refund, deposit, loading bay, plumber |

Aim for every scene to start at grammar 1 / vocab 1, and to include at least one "hard grammar, easy words" phrase.

## Dialect conventions

- The Latin America file follows Mexican usage, which matches the es-MX TTS voice.
- Spain uses *vosotros* for plural "you"; Latin America uses *ustedes*.
- Spain uses the present perfect for anything earlier today (*he llegado tarde*); Latin America uses the preterite (*llegué tarde*). The English stays the same in both files.
- Common vocabulary swaps: coche / carro, móvil / celular, ordenador / computadora, zumo / jugo, patata / papa, piso / departamento, billete / boleto, conducir / manejar, aparcar / estacionar, coger / tomar, ducha / regadera, camarero / mesero, nevera / refrigerador.
- Watch out for words that change meaning between regions. *Coger* is vulgar in much of Latin America, and *me da pena* means "it makes me sad" in Spain but "I'm embarrassed" in Mexico.
- English is British throughout (flat, lift, mobile, bill, trainers) and shared by both files.

## Editing in a spreadsheet

Google Sheets opens these files correctly. In Excel, use Data → From Text/CSV and choose UTF-8, or accents will be garbled. Save back as CSV UTF-8, then run `python tools/validate.py`.

The validator checks structure (markup, groups, levels, IDs, the two files staying in step), not whether the Spanish is right. New phrases still need a read-through by a confident speaker.
