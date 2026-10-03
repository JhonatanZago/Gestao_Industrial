import { copyFileSync, createReadStream, existsSync, mkdirSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const frontendRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const source = resolve(frontendRoot, '..', 'docs', 'templates', 'PRODUCAO_PADRAO.xlsx');
const target = resolve(frontendRoot, 'public', 'templates', 'PRODUCAO_PADRAO.xlsx');

if (!existsSync(source)) throw new Error(`Template oficial não encontrado: ${source}`);
mkdirSync(dirname(target), { recursive: true });

const digest = path => new Promise((resolveDigest, reject) => {
  const hash = createHash('sha256');
  createReadStream(path).on('error', reject).on('data', chunk => hash.update(chunk)).on('end', () => resolveDigest(hash.digest('hex')));
});

const sourceHash = await digest(source);
const targetHash = existsSync(target) ? await digest(target) : '';
if (sourceHash !== targetHash) copyFileSync(source, target);
if (await digest(target) !== sourceHash) throw new Error('A cópia pública do template diverge do arquivo oficial.');

console.log(`Template XLSX sincronizado (${sourceHash.slice(0, 12)}).`);
