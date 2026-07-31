"""Translate a captured mixin refmap into Mojang-mappings member names.

Mixin `method=` values are the one part of a port that the class-rename pass
deliberately leaves alone: `methods_qualified` is keyed by owner#name with no
descriptor, so overloads collapse and the answer is silently wrong. The refmap
that Loom emitted *before* the migration does not have that problem — it names
each target by its intermediary id, which is unique.

So resolve those ids the exact way instead of guessing:

    intermediary owner+method_NNNN  ->  official (obf) owner+name+desc  ->  mojmap

Usage:
    python resolve_refmap.py <intermediary-vN-v2.jar> <mojmap-vN.txt> <refmap.json>

Prints one line per refmap entry with the Mojang name the mixin must use.
"""
import json
import re
import sys

from build_map import parse_tiny, read_tiny, parse_proguard

REF = re.compile(r"^L(?P<owner>[\w/$]+);(?P<name>[\w<>$]+)(?P<desc>\(.*)?$")


def main(inter_jar, mojmap_txt, refmap_json):
    i_classes, i_methods, i_fields = parse_tiny(read_tiny(inter_jar))
    m_classes, m_methods, m_fields = parse_proguard(mojmap_txt)

    # intermediary -> official, for classes and for methods
    inter_to_off_class = {v: k for k, v in i_classes.items()}
    inter_to_off_method = {}
    # An intermediary method id is unique across the whole hierarchy, so it also
    # serves as a global key. That matters because a refmap names the *target*
    # class while the id is often declared on a supertype -- EatBlockGoal's
    # method_6264 is declared on Goal, Sheep's method_5983 on Mob. Keying only
    # by (target owner, id) leaves every inherited target unresolved.
    by_id = {}
    for (own_off, name_off, desc_off), name_int in i_methods.items():
        own_int = i_classes.get(own_off)
        if own_int:
            inter_to_off_method.setdefault((own_int, name_int), []).append(
                (own_off, name_off, desc_off))
        by_id.setdefault(name_int, []).append((own_off, name_off, desc_off))

    refmap = json.load(open(refmap_json, encoding="utf-8"))
    for mixin, entries in sorted(refmap.get("mappings", {}).items()):
        print(f"\n{mixin.split('/')[-1]}")
        for src, target in sorted(entries.items()):
            mo = REF.match(target)
            if not mo:
                print(f"    {src!r:44s} -> UNPARSED {target}")
                continue
            owner_int, name_int = mo.group("owner"), mo.group("name")
            owner_off = inter_to_off_class.get(owner_int)
            moj_owner = m_classes.get(owner_off, owner_off)
            if name_int.startswith("<"):
                print(f"    {src!r:44s} -> {name_int}   on {moj_owner}")
                continue
            keys = inter_to_off_method.get((owner_int, name_int)) or by_id.get(name_int, [])
            cands = {m_methods[k] for k in keys if k in m_methods}
            decl = {m_classes.get(k[0], k[0]).split("/")[-1] for k in keys}
            got = ", ".join(sorted(cands)) if cands else "!! UNRESOLVED"
            where = f"on {moj_owner.split('/')[-1]}"
            if decl and decl != {moj_owner.split("/")[-1]}:
                where += f"  (declared on {', '.join(sorted(decl))})"
            print(f"    {src!r:44s} -> {got:34s} {where}")


if __name__ == "__main__":
    main(*sys.argv[1:4])
