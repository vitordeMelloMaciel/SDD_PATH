import { createHash } from 'node:crypto';
import { mkdir, readFile, rename, rm, stat, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import sharp from 'sharp';

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const skillDirectory = path.resolve(scriptDirectory, '..');
const equipmentPath = path.join(skillDirectory, 'references', 'equipment.json');
const defaultWorkDirectory = path.resolve(process.cwd(), '.local', 'pixabay-equipment');
const cacheLifetimeMs = 24 * 60 * 60 * 1000;
const visualChecks = ['noPeople', 'noReadableBrandOrLogo', 'noText', 'noScreenContent'];

function option(name, fallback) {
  const index = process.argv.indexOf(name);
  return index >= 0 && process.argv[index + 1] ? path.resolve(process.argv[index + 1]) : fallback;
}

function slug(value) {
  return value.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
}

async function readJson(file) {
  return JSON.parse(await readFile(file, 'utf8'));
}

async function writeJson(file, value) {
  await writeFile(file, `${JSON.stringify(value, null, 2)}\n`, 'utf8');
}

async function cachedSearch(equipment, cacheDirectory, apiKey) {
  const cacheFile = path.join(cacheDirectory, `${equipment.assetCode}-${slug(equipment.query)}.json`);

  try {
    const cacheStat = await stat(cacheFile);
    if (Date.now() - cacheStat.mtimeMs < cacheLifetimeMs) return readJson(cacheFile);
  } catch (error) {
    if (error.code !== 'ENOENT') throw error;
  }

  const requestUrl = new URL('https://pixabay.com/api/');
  requestUrl.search = new URLSearchParams({
    key: apiKey,
    q: equipment.query,
    image_type: 'photo',
    orientation: 'horizontal',
    safesearch: 'true',
    min_width: '800',
    min_height: '600',
    per_page: '20',
    order: 'popular',
  });

  const response = await fetch(requestUrl, { signal: AbortSignal.timeout(20_000) });
  if (!response.ok) throw new Error(`Pixabay respondeu HTTP ${response.status} para ${equipment.assetCode}.`);
  const body = await response.json();
  await writeJson(cacheFile, body);
  return body;
}

async function download(url, destination) {
  const response = await fetch(url, { signal: AbortSignal.timeout(30_000) });
  if (!response.ok) throw new Error(`Falha HTTP ${response.status} ao baixar candidato.`);
  await writeFile(destination, Buffer.from(await response.arrayBuffer()));
}

function contributorProfile(hit) {
  return `https://pixabay.com/users/${encodeURIComponent(hit.user)}-${hit.user_id}/`;
}

async function search(workDirectory) {
  const apiKey = process.env.PIXABAY_API_KEY;
  if (!apiKey) throw new Error('Defina PIXABAY_API_KEY no ambiente; nao passe a chave na linha de comando.');

  const equipment = await readJson(equipmentPath);
  const cacheDirectory = path.join(workDirectory, 'cache');
  const candidateDirectory = path.join(workDirectory, 'candidates');
  await mkdir(cacheDirectory, { recursive: true });
  await mkdir(candidateDirectory, { recursive: true });
  const usedIds = new Set();
  const manifest = [];

  for (const item of equipment) {
    const body = await cachedSearch(item, cacheDirectory, apiKey);
    const hits = (body.hits ?? []).filter((hit) => !usedIds.has(hit.id)).slice(0, 3);
    if (hits.length < 3) throw new Error(`Menos de tres candidatos unicos para ${item.assetCode}.`);
    const itemDirectory = path.join(candidateDirectory, item.assetCode);
    await mkdir(itemDirectory, { recursive: true });
    const candidates = [];

    for (const hit of hits) {
      usedIds.add(hit.id);
      const file = path.join(itemDirectory, `${hit.id}.jpg`);
      const sourceUrl = hit.largeImageURL || hit.webformatURL?.replace('_640', '_960');
      if (!sourceUrl) throw new Error(`Pixabay nao retornou URL utilizavel para a imagem ${hit.id}.`);
      await download(sourceUrl, file);
      candidates.push({
        pixabayId: hit.id,
        file: path.relative(workDirectory, file).replaceAll('\\', '/'),
        sourcePage: hit.pageURL,
        contributor: hit.user,
        contributorProfile: contributorProfile(hit),
        license: 'Pixabay Content License',
        licenseUrl: 'https://pixabay.com/service/license-summary/',
      });
    }
    manifest.push({ ...item, candidates });
  }

  await writeJson(path.join(workDirectory, 'candidate-manifest.json'), manifest);
  const approvalsFile = path.join(workDirectory, 'approvals.json');
  try {
    await stat(approvalsFile);
  } catch (error) {
    if (error.code !== 'ENOENT') throw error;
    await writeJson(approvalsFile, manifest.map((item) => ({
      assetCode: item.assetCode,
      pixabayId: null,
      reviewer: '',
      checks: Object.fromEntries(visualChecks.map((check) => [check, false])),
    })));
  }

  console.log(`Candidatos preparados em ${candidateDirectory}`);
  console.log(`Revise e preencha ${approvalsFile} antes de finalizar.`);
}

async function finalize(workDirectory) {
  const manifest = await readJson(path.join(workDirectory, 'candidate-manifest.json'));
  const approvals = await readJson(path.join(workDirectory, 'approvals.json'));
  const approvalByAsset = new Map(approvals.map((approval) => [approval.assetCode, approval]));
  const selectedIds = new Set();
  const outputHashes = new Set();
  const outputDirectory = path.join(workDirectory, 'output');
  await mkdir(outputDirectory, { recursive: true });
  const finalManifest = [];

  for (const item of manifest) {
    const approval = approvalByAsset.get(item.assetCode);
    if (!approval || !approval.reviewer?.trim()) throw new Error(`Revisao ausente para ${item.assetCode}.`);
    if (!visualChecks.every((check) => approval.checks?.[check] === true)) throw new Error(`Controles visuais incompletos para ${item.assetCode}.`);
    if (selectedIds.has(approval.pixabayId)) throw new Error(`Imagem Pixabay ${approval.pixabayId} foi escolhida mais de uma vez.`);
    const candidate = item.candidates.find((entry) => entry.pixabayId === approval.pixabayId);
    if (!candidate) throw new Error(`Candidato aprovado nao pertence a ${item.assetCode}.`);
    selectedIds.add(approval.pixabayId);

    const source = path.join(workDirectory, candidate.file);
    const output = path.join(outputDirectory, `${item.assetCode}.jpg`);
    const temporary = `${output}.tmp`;
    await rm(temporary, { force: true });
    await sharp(source).rotate().resize(800, 600, { fit: 'cover', position: 'attention' }).jpeg({ quality: 88, mozjpeg: true }).toFile(temporary);
    const metadata = await sharp(temporary).metadata();
    if (metadata.width !== 800 || metadata.height !== 600) throw new Error(`Dimensao invalida para ${item.assetCode}.`);
    const bytes = await readFile(temporary);
    const sha256 = createHash('sha256').update(bytes).digest('hex');
    if (outputHashes.has(sha256)) throw new Error(`Conteudo final repetido para ${item.assetCode}.`);
    outputHashes.add(sha256);
    await rm(output, { force: true });
    await rename(temporary, output);

    finalManifest.push({
      assetCode: item.assetCode,
      name: item.name,
      category: item.category,
      status: item.status,
      file: path.relative(workDirectory, output).replaceAll('\\', '/'),
      width: 800,
      height: 600,
      sha256,
      storageObjectKey: `equipment/${item.assetCode}.jpg`,
      pixabayId: candidate.pixabayId,
      sourcePage: candidate.sourcePage,
      contributor: candidate.contributor,
      contributorProfile: candidate.contributorProfile,
      license: candidate.license,
      licenseUrl: candidate.licenseUrl,
      reviewedBy: approval.reviewer,
      creditLabel: 'Fotos: Pixabay',
      creditUrl: 'https://pixabay.com/',
    });
  }

  await writeJson(path.join(workDirectory, 'final-manifest.json'), finalManifest);
  console.log(`12 imagens 800x600 preparadas em ${outputDirectory}.`);
  console.log('Nenhum arquivo foi enviado ao Supabase ou a outro servico remoto.');
}

const command = process.argv[2];
const workDirectory = option('--work-dir', defaultWorkDirectory);

if (command === 'search') await search(workDirectory);
else if (command === 'finalize') await finalize(workDirectory);
else throw new Error('Use: prepare-images.mjs <search|finalize> [--work-dir caminho]');
