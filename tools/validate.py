#!/usr/bin/env python3
"""Validate the Easy Español phrase and expression CSVs.

Usage:  python tools/validate.py [data_dir]

data_dir defaults to app/src/main/assets in this repo.

Checks markup, levels, IDs, and that the Spain and Latin America files
stay in step. Prints a level summary and exits with code 1 on any error.
"""
import csv
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

CHUNK = re.compile(r"\{(\d+):([^{}]*)\}")
GENDER = re.compile(r"\[([^\[\]|]*)\|([^\[\]|]*)\]")
MAX_GROUPS = 9
LEVELS = {"1", "2", "3"}
REGISTERS = {"neutral", "informal", "slang"}
PHRASE_COLS = ["id", "topic", "scene", "grammar", "vocab", "spanish", "english", "note"]
EXPR_COLS = ["id", "group", "register", "spanish", "literal", "meaning", "note"]
DIALECTS = ["spain", "latam"]


def parse(text):
    """Split marked-up text into (group, text) segments; group is None for neutral text."""
    segs, pos = [], 0
    for m in CHUNK.finditer(text):
        if m.start() > pos:
            segs.append((None, text[pos:m.start()]))
        segs.append((int(m.group(1)), m.group(2)))
        pos = m.end()
    if pos < len(text):
        segs.append((None, text[pos:]))
    for g, t in segs:
        if g is None and ("{" in t or "}" in t):
            raise ValueError(f"stray brace near {t!r}")
        if g is not None and not t.strip():
            raise ValueError(f"empty chunk {{{g}:}}")
    return segs


def render(text, gender="m"):
    """Plain text as the app would display it, with gender markup resolved."""
    plain = "".join(t for _, t in parse(text))
    return GENDER.sub(lambda m: m.group(1) if gender == "m" else m.group(2), plain)


def _spacing_ok(s):
    return not ("  " in s or s != s.strip() or re.search(r"\s[,.;:?!)]", s))


def check_pair(rid, spanish, other, label, errs):
    try:
        es_segs, ot_segs = parse(spanish), parse(other)
    except ValueError as e:
        errs.append(f"{rid}: {e}")
        return
    es_groups = [g for g, _ in es_segs if g is not None]
    ot_groups = {g for g, _ in ot_segs if g is not None}
    order = list(dict.fromkeys(es_groups))
    if order != list(range(1, len(order) + 1)):
        errs.append(f"{rid}: number groups 1, 2, 3... in order of first appearance in the Spanish (found {order})")
    if set(es_groups) != ot_groups:
        errs.append(f"{rid}: Spanish groups {sorted(set(es_groups))} don't match {label} groups {sorted(ot_groups)}")
    if len(order) > MAX_GROUPS:
        errs.append(f"{rid}: {len(order)} groups; the palette has {MAX_GROUPS}, so chunk more coarsely")
    if any(c in other for c in "[]|"):
        errs.append(f"{rid}: gender markup [m|f] belongs in the Spanish only")
    if any(c in GENDER.sub("", spanish) for c in "[]|"):
        errs.append(f"{rid}: malformed gender markup, expected [masculine|feminine]")
    for variant in ("m", "f"):
        if not _spacing_ok(render(spanish, variant)):
            errs.append(f"{rid}: spacing problem in Spanish: {render(spanish, variant)!r}")
    if not _spacing_ok(render(other)):
        errs.append(f"{rid}: spacing problem in {label}: {render(other)!r}")


def read_csv(path, cols, errs):
    with open(path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        if reader.fieldnames != cols:
            errs.append(f"{path.name}: columns should be {cols}, found {reader.fieldnames}")
            return []
        return list(reader)


def check_phrases(path, errs):
    rows = read_csv(path, PHRASE_COLS, errs)
    seen = set()
    names = {}  # id prefix -> (topic, scene) names, which must stay consistent
    for r in rows:
        rid = f"{path.name}:{r['id']}"
        if r["id"] in seen:
            errs.append(f"{rid}: duplicate id")
        seen.add(r["id"])
        parts = r["id"].split("-")
        for key, col in ((parts[0], "topic"), ("-".join(parts[:2]), "scene")):
            first = names.setdefault((key, col), r[col])
            if r[col] != first:
                errs.append(f"{rid}: {col} '{r[col]}' differs from '{first}' used earlier for {key}")
        for col in ("id", "topic", "scene", "spanish", "english"):
            if not r[col].strip():
                errs.append(f"{rid}: empty {col}")
        if r["grammar"] not in LEVELS or r["vocab"] not in LEVELS:
            errs.append(f"{rid}: grammar and vocab must be 1, 2 or 3")
        check_pair(rid, r["spanish"], r["english"], "English", errs)
    return rows


def check_expressions(path, errs):
    rows = read_csv(path, EXPR_COLS, errs)
    seen = set()
    for r in rows:
        rid = f"{path.name}:{r['id']}"
        if r["id"] in seen:
            errs.append(f"{rid}: duplicate id")
        seen.add(r["id"])
        if r["register"] not in REGISTERS:
            errs.append(f"{rid}: register must be one of {sorted(REGISTERS)}")
        if any(c in r["meaning"] for c in "{}[]|"):
            errs.append(f"{rid}: meaning should be plain text")
        check_pair(rid, r["spanish"], r["literal"], "literal", errs)
    return rows


def check_dialects_match(spain, latam, errs):
    a, b = {r["id"]: r for r in spain}, {r["id"]: r for r in latam}
    for rid in sorted(a.keys() - b.keys()):
        errs.append(f"{rid}: in phrases_spain.csv but missing from phrases_latam.csv")
    for rid in sorted(b.keys() - a.keys()):
        errs.append(f"{rid}: in phrases_latam.csv but missing from phrases_spain.csv")
    for rid in sorted(a.keys() & b.keys()):
        for col in ("topic", "scene", "grammar", "vocab", "english"):
            if a[rid][col] != b[rid][col]:
                errs.append(f"{rid}: {col} differs between Spain and Latin America files")
    if [r["id"] for r in spain] != [r["id"] for r in latam]:
        errs.append("phrases_spain.csv and phrases_latam.csv list phrases in a different order")


def summary(rows):
    grid = defaultdict(Counter)
    for r in rows:
        grid[r["topic"]][(r["grammar"], r["vocab"])] += 1
    print(f"\n{len(rows)} phrases. Counts by grammar (G) and vocab (V) level:\n")
    cells = [(g, v) for g in "123" for v in "123"]
    print(f"{'topic':<26}" + "".join(f"G{g}V{v} " for g, v in cells) + " total")
    for topic, c in grid.items():
        print(f"{topic:<26}" + "".join(f"{c[k]:>4} " for k in cells) + f"{sum(c.values()):>6}")


def main(data_dir):
    d = Path(data_dir)
    errs = []
    phrases = {k: check_phrases(d / f"phrases_{k}.csv", errs) for k in DIALECTS}
    for k in DIALECTS:
        check_expressions(d / f"expressions_{k}.csv", errs)
    check_dialects_match(phrases["spain"], phrases["latam"], errs)
    summary(phrases["spain"])
    if errs:
        print(f"\n{len(errs)} problem(s):")
        for e in errs:
            print("  " + e)
        return 1
    print("\nAll checks passed.")
    return 0


if __name__ == "__main__":
    default = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "assets"
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else default))
