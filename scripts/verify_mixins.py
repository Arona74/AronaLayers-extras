"""Check that every mixin target in a built jar actually resolves.

There are two modes, because the supported versions differ in how mixins map.

Obfuscated versions (1.20.1 - 1.21.11)
    Loom 1.17 emits no refmap: it rewrites the mixin annotations in place. When
    it cannot resolve a target it silently leaves the developer-mappings name
    behind, the build still succeeds, and the mod dies at mixin-apply with
    "could not find any targets matching '<name>'". So every method= value must
    be <init>, an intermediary method_NNNN, or a name that is never obfuscated.

Deobfuscated versions (26.x)
    Nothing is remapped, so real names are correct there and the check above is
    meaningless. Instead the target method must actually exist on the class the
    mixin targets, looked up in the Minecraft jar.

Usage:
    python scripts/verify_mixins.py <module_dir> [<module_dir> ...]

Exits non-zero if any target fails to resolve.
"""
import glob
import json
import os
import re
import subprocess
import sys
import tempfile
import zipfile

# inherited from the JDK, never obfuscated, so these legitimately stay as-is
NEVER_OBFUSCATED = {"close", "toString", "equals", "hashCode", "run", "get", "accept"}

INTERMEDIARY = re.compile(r"^(?:<init>|<clinit>|method_\d+)(?:\(.*)?$")


def javap(path, cls=None):
    cmd = ["javap", "-v", "-p"] + (["-cp", path, cls] if cls else [path])
    return subprocess.run(cmd, capture_output=True, text=True).stdout


def module_version(module):
    for line in open(os.path.join(module, "gradle.properties"), encoding="utf-8"):
        if line.startswith("minecraft_version="):
            return line.split("=", 1)[1].strip()
    return ""


def find_minecraft_jar(version):
    home = os.path.expanduser("~").replace("\\", "/")
    for pat in (f".gradle/loom-cache/**/*{version}*.jar",
                f"{home}/.gradle/caches/fabric-loom/**/*{version}*.jar"):
        for hit in glob.glob(pat, recursive=True):
            if "sources" not in hit and "merged" in hit:
                return hit
    return None


def targets_of(out):
    """(mixin target classes, method= values) parsed from a compiled mixin."""
    pool = re.findall(r"=\s+Utf8\s+(\S+)", out)
    classes = []
    for i, s in enumerate(pool):
        if s == "Lorg/spongepowered/asm/mixin/Mixin;":
            for nxt in pool[i + 1:i + 4]:
                m = re.fullmatch(r"L(net/minecraft/[\w/$]+);", nxt)
                if m:
                    classes.append(m.group(1))
    methods = []
    for m in re.finditer(r"method=\[([^\]]*)\]", out):
        methods += [x.strip().strip('"') for x in m.group(1).split(",") if x.strip()]
    return classes, methods


def check(jar, mc_jar, deobf):
    z = zipfile.ZipFile(jar)
    # Find the mixin config rather than hardcoding the mod id, so this script
    # serves both AronaLayers mods unchanged.
    cfg_name = next(n for n in z.namelist()
                    if n.endswith(".mixins.json") and "/" not in n)
    cfg = json.loads(z.read(cfg_name).decode())
    pkg = cfg["package"].replace(".", "/")
    tmp = tempfile.mkdtemp()
    problems, checked = [], 0
    members = {}

    # Loom 1.7 emits a refmap and leaves developer-mappings names in the
    # annotation; Loom 1.17 emits none and rewrites the annotation in place.
    # Both are correct, so which question to ask depends on which is present.
    # Asking Loom 1.17's question of a Loom 1.7 jar fails every target.
    refmap_name = next((n for n in z.namelist() if n.endswith("-refmap.json")), None)
    refmap = (json.loads(z.read(refmap_name).decode()).get("mappings", {})
              if refmap_name else None)

    for cls in cfg["mixins"]:
        entry = f"{pkg}/{cls.replace('.', '/')}.class"
        if entry not in z.namelist():
            problems.append((cls, "class missing from jar"))
            continue
        p = os.path.join(tmp, cls + ".class")
        open(p, "wb").write(z.read(entry))
        out = javap(p)
        tgt_classes, methods = targets_of(out)
        entries = refmap.get(f"{pkg}/{cls.replace('.', '/')}", {}) if refmap is not None else {}

        for name in methods:
            checked += 1
            bare = name.split("(")[0]
            if not deobf:
                if refmap is not None:
                    # With a refmap the annotation name is only a key; what has
                    # to be intermediary is what it resolves to.
                    resolved = entries.get(name) or entries.get(bare)
                    if resolved is None:
                        if bare not in NEVER_OBFUSCATED and not INTERMEDIARY.match(name):
                            problems.append((cls, f'method="{name}" has no refmap entry'))
                    elif not re.search(r";(?:<init>|<clinit>|method_\d+)", resolved):
                        problems.append((cls, f'method="{name}" resolves to {resolved}'))
                elif not INTERMEDIARY.match(name) and bare not in NEVER_OBFUSCATED:
                    problems.append((cls, f'method="{name}" not remapped'))
                continue
            if bare in ("<init>", "<clinit>") or bare in NEVER_OBFUSCATED:
                continue
            if not tgt_classes or not mc_jar:
                continue                        # nothing to resolve against
            for tc in tgt_classes:
                if tc not in members:
                    members[tc] = set(re.findall(r"\b(\w+)\(",
                                                 javap(mc_jar, tc.replace("/", "."))))
                if bare in members[tc]:
                    break
            else:
                problems.append((cls, f'method="{name}" not found on {", ".join(tgt_classes)}'))
    return cfg["mixins"], checked, problems


def main():
    bad = 0
    for mod in sys.argv[1:]:
        # Normally the module's own build/libs. Also honours ARONALAYERS_BUILD_DIR,
        # the opt-in output redirect in the root build.gradle, so this keeps
        # checking the right jar when that is in use.
        roots = [os.path.join(mod, "build/libs/*.jar")]
        out = os.environ.get("ARONALAYERS_BUILD_DIR")
        if out:
            roots.append(os.path.join(out, os.path.basename(mod.rstrip("/\\")), "libs/*.jar"))
        jars = [x for r in roots for x in glob.glob(r) if "sources" not in x]
        if not jars:
            print(f"{mod}: no jar built")
            continue
        jar = max(jars, key=os.path.getmtime)
        version = module_version(mod)
        deobf = not version.startswith("1.")     # 26.x and later ship deobfuscated
        mc_jar = find_minecraft_jar(version) if deobf else None
        mixins, checked, problems = check(jar, mc_jar, deobf)
        mode = "deobfuscated" if deobf else "intermediary"
        print(f"{mod:10s} {os.path.basename(jar):40s} {mode:14s} "
              f"{len(mixins)} mixins, {checked} targets: {'OK' if not problems else 'FAILED'}")
        for cls, why in problems:
            print(f"    !! {cls}: {why}")
            bad += 1
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main())
