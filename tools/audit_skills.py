#!/usr/bin/env python3
"""Audita SkillData.bin.json: consistencia entre descricao e efeitos (stun, silence, etc.)."""
import json, re, sys, pathlib

root = pathlib.Path(__file__).resolve().parents[1]
skills = json.load(open(root / "src/AikaEmu.GameServer/Data/Game/SkillData.bin.json", encoding="utf-8"))

# ItemEffectId.cs: 73 STUN, 140/141 SILENCE, 226 = medo (descricao "atordoado pelo medo"), 235 FREEZE
CC_EFFECTS = {73: "stun", 140: "silence", 141: "silence", 226: "fear", 235: "freeze"}
CC_WORDS = re.compile(r"atordo|paraliz|silenc|congel|medo", re.I)
SECS = re.compile(r"(\d+)\s*segundo", re.I)

issues = []
for s in skills:
    desc = s["Description"] or ""
    eff_id = s["SpellDurationEffect"][0]
    has_cc = eff_id in CC_EFFECTS
    if CC_WORDS.search(desc) and not has_cc and s["Id"] > 0:
        issues.append((s["Id"], s["Name2"], "descricao cita controle, mas SpellDurationEffect[0]=%d nao e CC" % eff_id))
    if has_cc and s["EffectDuration"] == 0:
        issues.append((s["Id"], s["Name2"], "efeito CC %s com EffectDuration=0" % CC_EFFECTS[eff_id]))
    m = SECS.search(desc)
    if m and has_cc and int(m.group(1)) != s["EffectDuration"]:
        issues.append((s["Id"], s["Name2"], "descricao diz %ss, EffectDuration=%d" % (m.group(1), s["EffectDuration"])))

for i in issues:
    print("%5d  %-28s %s" % i)
print("\n%d inconsistencias em %d skills" % (len(issues), len(skills)), file=sys.stderr)
