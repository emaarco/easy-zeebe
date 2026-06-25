#!/usr/bin/env node
// Variant 3 — FULL auto-layout (the escape hatch).
//
// Throws away the diagram interchange and regenerates a complete left-to-right
// layout from the semantic model with bpmn.io's bpmn-auto-layout. Unlike the
// surgical fixer (fix.mjs), this does NOT preserve hand-tuned geometry — it is
// for models with no/garbage DI, or as a last resort.
//
// Known limits of bpmn-auto-layout: only the first participant of a
// collaboration is laid out; sub-processes are rendered collapsed; groups, text
// annotations, associations and message flows are not laid out.
//
// Usage:  npm --prefix tools run auto-layout:bpmn -- <file.bpmn> [--write]
//   (default: writes <file>.laidout.bpmn; --write overwrites in place)
//   <file.bpmn> is resolved from the directory you ran npm in (repo root).

import { layoutProcess } from 'bpmn-auto-layout';
import { readFileSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const [arg, ...flags] = process.argv.slice(2);
const write = flags.includes('--write');
if (!arg) {
  console.error('usage: npm --prefix tools run auto-layout:bpmn -- <file.bpmn> [--write]');
  process.exit(2);
}
// npm runs this script with cwd=tools/; resolve the file against the dir the
// user invoked npm in (INIT_CWD = repo root) so paths stay repo-root-relative.
const file = resolve(process.env.INIT_CWD || process.cwd(), arg);

const laidOut = await layoutProcess(readFileSync(file, 'utf8'));
const out = write ? file : file.replace(/\.bpmn$/, '.laidout.bpmn');
writeFileSync(out, laidOut);
console.log(`  ♻️  regenerated full layout → ${out}`);
console.log('  ⚠️  hand-tuned positions were discarded (this regenerates ALL diagram interchange).');
